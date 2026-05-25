package com.safesign.backend.domain.contract.repository;

import com.safesign.backend.domain.contract.entity.Contract;
import com.safesign.backend.domain.contract.entity.ContractHeaderInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractHeaderInfoRepository
        extends JpaRepository<ContractHeaderInfo, Long> {

    List<ContractHeaderInfo> findByContract(Contract contract);
}
