package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.contract.entity.ContractOcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ContractOcrResultRepository extends JpaRepository<ContractOcrResult, Long> {

    @Query("""
        SELECT o
        FROM ContractOcrResult o
        WHERE o.contract = :contract
        ORDER BY o.ocrResultId DESC
    """)
    Optional<ContractOcrResult> findLatestOcrResult(@Param("contract") Contract contract);
}