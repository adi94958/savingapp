package com.adisaputera.savingapp.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "accounts")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_code", unique = true, nullable = false, length = 20)
    private String accountCode;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    private User userId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "total_deposit", nullable = false)
    private Long totalDeposit;

    @Column(name = "total_withdraw", nullable = false)
    private Long totalWithdraw;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Column(name = "created_at", nullable = true, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @PostPersist
    public void postPersist() {
        if(this.accountCode == null) {
            this.accountCode = String.format("ACC-%06d", this.id);
        }
    }
}
