package com.safesign.backend.domain.admin.repository;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class AdminAnalysisRepository {

    private final EntityManager em;

    public List<Object[]> findUserAnalysisHistories(Long userId) {

        String sql = """
            SELECT
                ar.created_at,
                c.title,
                CASE
                    WHEN c.status IN ('OCR_COMPLETED', 'AI_COMPLETED') THEN 'SUCCESS'
                    WHEN c.status IN ('OCR_FAILED', 'AI_FAILED') THEN 'FAILED'
                    ELSE c.status
                END AS status,
                ar.overall_risk_score
            FROM contract_analysis_result ar
            JOIN contract c
                ON ar.contract_id = c.contract_id
            WHERE c.user_id = :userId
            ORDER BY ar.created_at DESC
        """;

        return em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .getResultList();
    }

    public Long countUserAnalysis(Long userId) {

        String sql = """
            SELECT COUNT(*)
            FROM contract_analysis_result ar
            JOIN contract c
                ON ar.contract_id = c.contract_id
            WHERE c.user_id = :userId
        """;

        Number result = (Number) em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .getSingleResult();

        return result.longValue();
    }

    public Long countAllAnalysis() {

        String sql = """
            SELECT COUNT(*)
            FROM contract_analysis_result
        """;

        Number result = (Number) em.createNativeQuery(sql)
                .getSingleResult();

        return result.longValue();
    }

    public List<Object[]> findAnalysisLogs() {

        String sql = """
            SELECT
                ar.analysis_result_id,
                c.title AS file_name,
                CASE
                    WHEN c.status IN ('OCR_COMPLETED', 'AI_COMPLETED') THEN 'SUCCESS'
                    WHEN c.status IN ('OCR_FAILED', 'AI_FAILED') THEN 'FAILED'
                    ELSE c.status
                END AS status,
                u.name AS user_name,
                CONCAT('U', LPAD(CAST(u.user_id AS TEXT), 4, '0')) AS user_code,
                ar.created_at AS analyzed_at,
                COALESCE(EXTRACT(EPOCH FROM (ocr.completed_at - ocr.started_at)), 0.0) AS ocr_time_seconds,
                COALESCE(ar.analysis_time_seconds, 0.0) AS analysis_time_seconds,
                ar.overall_risk_score,
                COALESCE(issue.issue_count, 0) AS issue_count
            FROM contract_analysis_result ar
            JOIN contract c
                ON ar.contract_id = c.contract_id
            JOIN users u
                ON c.user_id = u.user_id
            LEFT JOIN (
                SELECT DISTINCT ON (contract_id)
                    contract_id,
                    started_at,
                    completed_at
                FROM ocr_result
                ORDER BY contract_id, created_at DESC
            ) ocr
                ON c.contract_id = ocr.contract_id
            LEFT JOIN (
                SELECT
                    analysis_result_id,
                    COUNT(*) AS issue_count
                FROM contract_clause_analysis
                GROUP BY analysis_result_id
            ) issue
                ON ar.analysis_result_id = issue.analysis_result_id
            ORDER BY ar.created_at DESC
        """;

        return em.createNativeQuery(sql)
                .getResultList();
    }

    public List<Object[]> findFilteredAnalysisLogs(
            LocalDateTime startAt,
            LocalDateTime endAt,
            String status,
            String keyword
    ) {

        String sql = """
        SELECT
            ar.analysis_result_id,
            c.title AS file_name,
            CASE
                WHEN c.status IN ('OCR_COMPLETED', 'AI_COMPLETED') THEN 'SUCCESS'
                WHEN c.status IN ('OCR_FAILED', 'AI_FAILED') THEN 'FAILED'
                ELSE c.status
            END AS status,
            u.name AS user_name,
            CONCAT('U', LPAD(CAST(u.user_id AS TEXT), 4, '0')) AS user_code,
            ar.created_at AS analyzed_at,
            COALESCE(EXTRACT(EPOCH FROM (ocr.completed_at - ocr.started_at)), 0.0) AS ocr_time_seconds,
            COALESCE(ar.analysis_time_seconds, 0.0) AS analysis_time_seconds,
            ar.overall_risk_score,
            COALESCE(issue.issue_count, 0) AS issue_count
        FROM contract_analysis_result ar
        JOIN contract c
            ON ar.contract_id = c.contract_id
        JOIN users u
            ON c.user_id = u.user_id
        LEFT JOIN (
            SELECT DISTINCT ON (contract_id)
                contract_id,
                started_at,
                completed_at
            FROM ocr_result
            ORDER BY contract_id, created_at DESC
        ) ocr
            ON c.contract_id = ocr.contract_id
        LEFT JOIN (
            SELECT
                analysis_result_id,
                COUNT(*) AS issue_count
            FROM contract_clause_analysis
            GROUP BY analysis_result_id
        ) issue
            ON ar.analysis_result_id = issue.analysis_result_id
        WHERE (CAST(:startAt AS timestamp) IS NULL OR ar.created_at >= CAST(:startAt AS timestamp))
          AND (CAST(:endAt AS timestamp) IS NULL OR ar.created_at < CAST(:endAt AS timestamp))
          AND (
                CAST(:status AS varchar) IS NULL
                OR CASE
                    WHEN c.status IN ('OCR_COMPLETED', 'AI_COMPLETED') THEN 'SUCCESS'
                    WHEN c.status IN ('OCR_FAILED', 'AI_FAILED') THEN 'FAILED'
                    ELSE c.status
                END = CAST(:status AS varchar)
              )
          AND (
                CAST(:keyword AS varchar) IS NULL
                OR LOWER(c.title) LIKE LOWER(CONCAT('%', CAST(:keyword AS varchar), '%'))
                OR LOWER(u.name) LIKE LOWER(CONCAT('%', CAST(:keyword AS varchar), '%'))
                OR LOWER(CONCAT('U', LPAD(CAST(u.user_id AS TEXT), 4, '0'))) LIKE LOWER(CONCAT('%', CAST(:keyword AS varchar), '%'))
              )
        ORDER BY ar.created_at DESC
    """;

        return em.createNativeQuery(sql)
                .setParameter("startAt", startAt)
                .setParameter("endAt", endAt)
                .setParameter("status", status)
                .setParameter("keyword", keyword)
                .getResultList();
    }

    public Optional<Object[]> findAnalysisLogDetail(Long analysisId) {

        String sql = """
            SELECT
                ar.analysis_result_id,
                c.title AS file_name,
                ar.created_at AS analyzed_at,
                ( COALESCE(EXTRACT(EPOCH FROM (ocr.completed_at - ocr.started_at)), 0.0)
                    + COALESCE(ar.analysis_time_seconds, 0.0)
                ) AS total_time_seconds,
                ar.overall_risk_score
            FROM contract_analysis_result ar
            JOIN contract c
                ON ar.contract_id = c.contract_id
            LEFT JOIN (
                SELECT DISTINCT ON (contract_id)
                    contract_id,
                    started_at,
                    completed_at
                FROM ocr_result
                ORDER BY contract_id, created_at DESC
            ) ocr
                ON c.contract_id = ocr.contract_id
            WHERE ar.analysis_result_id = :analysisId
        """;

        List<Object[]> rows = em.createNativeQuery(sql)
                .setParameter("analysisId", analysisId)
                .getResultList();

        return rows.stream().findFirst();
    }

    public List<Object[]> findAnalysisIssues(Long analysisId) {

        String sql = """
        SELECT
            ca.title,
            ca.reason,
            ca.risk_type
        FROM contract_clause_analysis ca
        WHERE ca.analysis_result_id = :analysisId
        ORDER BY ca.risk_score DESC NULLS LAST, ca.clause_analysis_id ASC
    """;

        return em.createNativeQuery(sql)
                .setParameter("analysisId", analysisId)
                .getResultList();
    }
}
