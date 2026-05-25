package com.safesign.backend.domain.contract.service;

import com.safesign.backend.domain.contract.dto.response.ClauseResponse;
import com.safesign.backend.domain.contract.dto.response.HeaderResponse;
import com.safesign.backend.domain.contract.dto.response.ParsingResponse;
import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.contract.entity.ContractClause;
import com.safesign.backend.domain.contract.entity.ContractHeaderInfo;
import com.safesign.backend.domain.contract.repository.ContractClauseRepository;
import com.safesign.backend.domain.contract.repository.ContractHeaderInfoRepository;
import com.safesign.backend.domain.contract.repository.ContractRepository;
import com.safesign.backend.global.exception.CustomException;
import com.safesign.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContractParsingQueryService {

    private final ContractRepository contractRepository;
    private final ContractClauseRepository clauseRepository;
    private final ContractHeaderInfoRepository headerRepository;

    public ParsingResponse getParsedResult(Long userId, Long contractId) {
        Contract contract = contractRepository
                .findByContractIdAndUser_UserId(contractId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.CONTRACT_NOT_FOUND));

        List<ContractClause> clauseEntities = clauseRepository.findByContractOrderByOrderNoAsc(contract);

        if (clauseEntities.isEmpty()) {
            throw new CustomException(ErrorCode.PARSE_RESULT_NOT_FOUND);
        }

        List<ClauseResponse> clauses = clauseEntities.stream()
                .map(clause -> new ClauseResponse(
                        clause.getClauseTitle(),
                        clause.getClauseText(),
                        clause.getOrderNo()
                ))
                .toList();

        List<ContractHeaderInfo> headerEntities = headerRepository.findByContract(contract);

        ContractHeaderInfo headerEntity = headerEntities.isEmpty()
                ? null
                : headerEntities.get(headerEntities.size() - 1);

        HeaderResponse header = toHeaderResponse(headerEntity);

        return new ParsingResponse(
                contract.getContractId(),
                clauses.size(),
                "파싱 결과 조회 완료",
                List.of(),
                clauses,
                header
        );
    }

    private HeaderResponse toHeaderResponse(ContractHeaderInfo header) {
        if (header == null) {
            return new HeaderResponse(
                    null, null, null, null, null, null, null,
                    null, null, null, null, null, null
            );
        }

        return new HeaderResponse(
                header.getPropertyAddress(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                toStringValue(header.getDepositAmount()),
                toStringValue(header.getContractPayment()),
                null,
                null,
                toStringValue(header.getMonthlyRent())
        );
    }

    private String toStringValue(Long value) {
        return value == null ? null : String.valueOf(value);
    }
}