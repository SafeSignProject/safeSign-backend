package com.safesign.backend.domain.ocr.repository;

import com.safesign.backend.domain.ocr.entity.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OcrResultRepository extends JpaRepository<OcrResult, Long> {

    @Query("""
        select o
        from OcrResult o
        where o.contract.contractId = :contractId
        order by o.ocrResultId desc
    """)
    Optional<OcrResult> findLatestByContractId(@Param("contractId") Long contractId);
}