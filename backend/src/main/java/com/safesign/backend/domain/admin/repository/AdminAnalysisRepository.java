package com.safesign.backend.domain.admin.repository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AdminAnalysisRepository {

    private final EntityManager em;

    public List<Object[]> findUserAnalysisHistories(Long userId) {

        String sql = """
            SELECT
                ar.created_at,
                c.title,
                c.status,
                ar.overall_risk_score
            FROM analysis_result ar
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
            FROM analysis_result ar
            JOIN contract c
                ON ar.contract_id = c.contract_id
            WHERE c.user_id = :userId
        """;

        Number result = (Number) em.createNativeQuery(sql)
                .setParameter("userId", userId)
                .getSingleResult();

        return result.longValue();
    }
}