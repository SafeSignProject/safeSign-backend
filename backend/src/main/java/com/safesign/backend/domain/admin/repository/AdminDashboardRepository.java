package com.safesign.backend.domain.admin.repository;

import jakarta.persistence.EntityManager;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminDashboardRepository {

    private final EntityManager em;

    public Long countUsers() {

        String sql = """
            SELECT COUNT(*)
            FROM users u
            WHERE u.deleted_at IS NULL
        """;

        Number result = (Number) em.createNativeQuery(sql)
                .getSingleResult();

        return result.longValue();
    }

    public Long countUsersCreatedBetween(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {

        String sql = """
            SELECT COUNT(*)
            FROM users u
            WHERE u.deleted_at IS NULL
              AND u.created_at >= :startAt
              AND u.created_at < :endAt
        """;

        Number result = (Number) em.createNativeQuery(sql)
                .setParameter("startAt", startAt)
                .setParameter("endAt", endAt)
                .getSingleResult();

        return result.longValue();
    }

    public Long countAnalysisCompleted() {

        String sql = """
            SELECT COUNT(*)
            FROM contract_analysis_result ar
        """;

        Number result = (Number) em.createNativeQuery(sql)
                .getSingleResult();

        return result.longValue();
    }

    public Long countTodayContracts(
            LocalDateTime startAt,
            LocalDateTime endAt
    ) {

        String sql = """
            SELECT COUNT(*)
            FROM contract c
            WHERE c.created_at >= :startAt
              AND c.created_at < :endAt
        """;

        Number result = (Number) em.createNativeQuery(sql)
                .setParameter("startAt", startAt)
                .setParameter("endAt", endAt)
                .getSingleResult();

        return result.longValue();
    }

    public List<Object[]> findRecentUsers() {

        String sql = """
            SELECT
                u.user_id,
                u.name,
                u.email,
                u.provider_type,
                u.created_at
            FROM users u
            WHERE u.deleted_at IS NULL
            ORDER BY u.created_at DESC
            LIMIT 5
        """;

        return em.createNativeQuery(sql)
                .getResultList();
    }

    public List<Object[]> findRecentAnalysisLogs() {

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
            LIMIT 5
        """;

        return em.createNativeQuery(sql)
                .getResultList();
    }
}