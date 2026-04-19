package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.Contract;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {
}
