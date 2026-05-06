package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.ContractUploadResponse;
import com.safesign.backend.domain.contract.dto.response.StoredFileInfo;
import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.contract.entity.ContractFile;
import com.safesign.backend.domain.contract.enums.ContractStatus;
import com.safesign.backend.domain.contract.enums.UploadSource;
import com.safesign.backend.domain.contract.enums.UploadType;
import com.safesign.backend.domain.contract.repository.ContractFileRepository;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.user.repository.UserRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractFileRepository contractFileRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    public ContractUploadResponse uploadContract(Long userId, List<MultipartFile> files, String title, UploadType uploadType) {
        validateFilesExist(files);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return switch (uploadType) {
            case PDF -> uploadPdf(files, title, user);
            case IMAGE -> uploadImages(files, title, user);
        };
    }

    private ContractUploadResponse uploadPdf(List<MultipartFile> files, String title, User user) {
        if (files.size() != 1) {
            throw new CustomException(ErrorCode.INVALID_PDF_FILE_COUNT);
        }

        MultipartFile file = files.get(0);
        validatePdfFile(file);

        int pageCount = extractPdfPageCount(file);

        Contract contract = createContract(user, title, UploadType.PDF, pageCount);
        contractRepository.save(contract);

        StoredFileInfo storedFileInfo = fileStorageService.storeFile(file, contract.getContractId());

        ContractFile contractFile = ContractFile.builder()
                .pageNo(null)
                .fileName(storedFileInfo.getOriginalFileName())
                .storedFileName(storedFileInfo.getStoredFileName())
                .fileUrl(storedFileInfo.getFileUrl())
                .mimeType(storedFileInfo.getMimeType())
                .fileSize(storedFileInfo.getFileSize())
                .build();

        contract.addContractFile(contractFile);
        contractFileRepository.save(contractFile);

        return buildUploadResponse(contract);
    }

    private ContractUploadResponse uploadImages(List<MultipartFile> files, String title, User user) {
        validateImageFiles(files);

        int pageCount = files.size();

        Contract contract = createContract(user, title, UploadType.IMAGE, pageCount);
        contractRepository.save(contract);

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            StoredFileInfo storedFileInfo = fileStorageService.storeFile(file, contract.getContractId());

            ContractFile contractFile = ContractFile.builder()
                    .pageNo(i + 1)
                    .fileName(storedFileInfo.getOriginalFileName())
                    .storedFileName(storedFileInfo.getStoredFileName())
                    .fileUrl(storedFileInfo.getFileUrl())
                    .mimeType(storedFileInfo.getMimeType())
                    .fileSize(storedFileInfo.getFileSize())
                    .build();

            contract.addContractFile(contractFile);
            contractFileRepository.save(contractFile);
        }

        return buildUploadResponse(contract);
    }

    private Contract createContract(User user, String title, UploadType uploadType, int pageCount) {
        return Contract.builder()
                .user(user)
                .title(title)
                .contractType(null)
                .uploadType(uploadType)
                .uploadSource(UploadSource.WEB)
                .status(ContractStatus.UPLOADED)
                .pageCount(pageCount)
                .build();
    }

    private ContractUploadResponse buildUploadResponse(Contract contract) {
        return ContractUploadResponse.builder()
                .contractId(contract.getContractId())
                .title(contract.getTitle())
                .uploadType(contract.getUploadType())
                .pageCount(contract.getPageCount())
                .status(contract.getStatus())
                .uploadedAt(contract.getUploadedAt())
                .build();
    }

    private void validateFilesExist(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_UPLOAD_FILE);
        }
    }

    private void validatePdfFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_UPLOAD_FILE);
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new CustomException(ErrorCode.INVALID_PDF_UPLOAD);
        }
    }

    private void validateImageFiles(List<MultipartFile> files) {
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                throw new CustomException(ErrorCode.EMPTY_UPLOAD_FILE);
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new CustomException(ErrorCode.INVALID_IMAGE_UPLOAD);
            }
        }
    }

    private int extractPdfPageCount(MultipartFile file) {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            throw new CustomException(ErrorCode.PDF_PAGE_COUNT_READ_FAILED);
        }
    }
}