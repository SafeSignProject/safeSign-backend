package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.ContractFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractFileRepository extends JpaRepository<ContractFile, Long> {
}