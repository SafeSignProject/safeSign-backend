package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByContractIdAndUser_UserId(Long contractId, Long userId);

    List<Contract> findByUser_UserIdOrderByUploadedAtDesc(Long userId);

    List<Contract> findByUser_UserIdAndTitleContainingIgnoreCaseOrderByUploadedAtDesc(
            Long userId,
            String keyword
    );

    List<Contract> findByUser_UserIdOrderByUploadedAtAsc(Long userId);

    List<Contract> findByUser_UserIdAndTitleContainingIgnoreCaseOrderByUploadedAtAsc(
            Long userId,
            String keyword
    );

    Long countByUser_UserId(Long userId);
}