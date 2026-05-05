package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    Optional<Contract> findByContractIdAndUser_UserId(Long contractId, Long userId);
}