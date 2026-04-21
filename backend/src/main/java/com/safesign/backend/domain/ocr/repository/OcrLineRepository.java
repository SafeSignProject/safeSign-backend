package com.safesign.backend.domain.ocr.repository;

import com.safesign.backend.domain.ocr.entity.OcrLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OcrLineRepository extends JpaRepository<OcrLine, Long> {

    @Query("""
        select l
        from OcrLine l
        where l.ocrPage.ocrPageId = :ocrPageId
        order by l.lineNo asc
    """)
    List<OcrLine> findAllByOcrPageId(@Param("ocrPageId") Long ocrPageId);
}