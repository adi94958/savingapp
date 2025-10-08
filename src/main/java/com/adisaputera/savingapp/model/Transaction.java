package com.adisaputera.savingapp.model;

import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transactions")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false, unique = true)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "account_code", referencedColumnName = "account_code", nullable = false)
    private Account accountCode;
    
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private TypeTransactionEnum type;

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "balance", nullable = false)
    private Long balance;

    @Column(name = "note", nullable = true)
    @Size(max = 200)
    private String note;

    @Column(name = "occurred_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime occurredAt;

    @Column(name = "created_at", nullable = true, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
}
