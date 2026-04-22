package com.safesign.backend.domain.contract.repository;
import com.safesign.backend.domain.contract.entity.ContractClause;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ContractClauseRepository extends JpaRepository<ContractClause, Long> {
}