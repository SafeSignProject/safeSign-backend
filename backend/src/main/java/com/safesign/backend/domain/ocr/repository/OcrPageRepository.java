package com.safesign.backend.domain.ocr.repository;

import com.safesign.backend.domain.ocr.entity.OcrPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OcrPageRepository extends JpaRepository<OcrPage, Long> {

    @Query("""
        select p
        from OcrPage p
        where p.ocrResult.ocrResultId = :ocrResultId
        order by p.pageNumber asc
    """)
    List<OcrPage> findAllByOcrResultId(@Param("ocrResultId") Long ocrResultId);
}