package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.ContractAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractAnalysisResultRepository extends JpaRepository<ContractAnalysisResult, Long> {

    Optional<ContractAnalysisResult> findTopByContract_ContractIdOrderByCreatedAtDesc(Long contractId);

    List<ContractAnalysisResult> findByContract_User_UserId(Long userId);
}