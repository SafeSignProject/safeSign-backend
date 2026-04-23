package com.safesign.backend.domain.contract.repository;
import com.safesign.backend.domain.contract.entity.ContractHeaderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractHeaderRepository extends JpaRepository<ContractHeaderInfo, Long> {
}