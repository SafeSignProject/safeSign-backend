package com.safesign.backend.domain.contract.entity;

import com.safesign.backend.domain.contract.enums.ContractStatus;
import com.safesign.backend.domain.contract.enums.UploadSource;
import com.safesign.backend.domain.contract.enums.UploadType;
import com.safesign.backend.domain.user.entity.User;
import com.safesign.backend.domain.ocr.entity.OcrResult;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Setter
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contract")
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contractId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(length = 50)
    private String contractType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UploadType uploadType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private UploadSource uploadSource;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ContractStatus status;

    private Integer pageCount;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractFile> contractFiles = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractClause> contractClauses = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractHeaderInfo> contractHeaderInfos = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OcrResult> ocrResults = new ArrayList<>();

    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContractAnalysisResult> analysisResults = new ArrayList<>();

    @Builder
    public Contract(
            User user,
            String title,
            String contractType,
            UploadType uploadType,
            UploadSource uploadSource,
            ContractStatus status,
            Integer pageCount,
            LocalDateTime uploadedAt
    ) {
        this.user = user;
        this.title = title;
        this.contractType = contractType;
        this.uploadType = uploadType;
        this.uploadSource = uploadSource;
        this.status = status;
        this.pageCount = pageCount;
        this.uploadedAt = uploadedAt;
    }

    public void addContractFile(ContractFile contractFile) {
        this.contractFiles.add(contractFile);
        contractFile.assignContract(this);
    }

    public void addContractClause(ContractClause contractClause) {
        this.contractClauses.add(contractClause);
        contractClause.setContract(this);
    }

    public void updatePageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public void updateStatus(ContractStatus status) {
        this.status = status;
    }

    public void updateFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        if (this.uploadedAt == null) {
            this.uploadedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}