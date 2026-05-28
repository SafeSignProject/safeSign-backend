package com.safesign.backend.domain.contract.entity;

import com.safesign.backend.global.util.KstTime;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "contract_clause")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractClause {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clauseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contract_id")
    private Contract contract;

    // 제1조 / 1.
    @Column(length = 50)
    private String clauseNo;

    // 조항 제목
    private String clauseTitle;

    // 조항 내용
    @Column(columnDefinition = "TEXT")
    private String clauseText;

    // STANDARD / SPECIAL
    @Column(length = 30)
    private String clauseType;

    // 표시 순서
    private Integer orderNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = KstTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = KstTime.now();
    }
}
