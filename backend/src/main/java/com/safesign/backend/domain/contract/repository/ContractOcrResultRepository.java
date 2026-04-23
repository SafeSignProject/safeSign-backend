package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.ContractOcrResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractOcrResultRepository extends JpaRepository<ContractOcrResult, Long> {

    // 최신 OCR 결과 가져오기
    Optional<ContractOcrResult> findTopByContract_ContractIdOrderByOcrResultIdDesc(Long contractId);
}