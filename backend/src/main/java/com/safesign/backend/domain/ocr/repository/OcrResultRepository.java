package com.safesign.backend.domain.ocr.repository;

import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.ocr.entity.OcrResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OcrResultRepository extends JpaRepository<OcrResult, Long> {

    @Query("""
    select o
    from OcrResult o
    where o.contract = :contract
    order by o.ocrResultId desc
""")
    Optional<OcrResult> findLatestByContract(@Param("contract") Contract contract);
}