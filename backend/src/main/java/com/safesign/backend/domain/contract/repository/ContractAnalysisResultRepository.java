package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.ContractAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ContractAnalysisResultRepository extends JpaRepository<ContractAnalysisResult, Long> {

    Optional<ContractAnalysisResult> findTopByContract_ContractIdOrderByCreatedAtDesc(Long contractId);

    List<ContractAnalysisResult> findByContract_User_UserId(Long userId);

    @Query("""
        select count(ar)
        from ContractAnalysisResult ar
        where ar.contract.user.userId = :userId
          and ar.overallRiskScore >= :riskScore
    """)
    Long countRiskyAnalysesByUserId(Long userId, Integer riskScore);

    @Query("""
        select count(ar)
        from ContractAnalysisResult ar
        where ar.contract.user.userId = :userId
          and ar.createdAt >= :startOfMonth
          and ar.createdAt < :startOfNextMonth
    """)
    Long countMonthlyAnalysesByUserId(
            Long userId,
            LocalDateTime startOfMonth,
            LocalDateTime startOfNextMonth
    );

    @Query("""
        select ar
        from ContractAnalysisResult ar
        join fetch ar.contract c
        where c.user.userId = :userId
        order by ar.createdAt desc
    """)
    List<ContractAnalysisResult> findRecentAnalysesByUserId(
            Long userId,
            Pageable pageable
    );
}