package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.contract.entity.ContractClause;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ContractClauseRepository
        extends JpaRepository<ContractClause, Long> {

    Optional<ContractClause> findFirstByContract_ContractIdAndClauseNoAndClauseText(
            Long contractId,
            String clauseNo,
            String clauseText
    );

    List<ContractClause> findByContractOrderByOrderNoAsc(Contract contract);
}
