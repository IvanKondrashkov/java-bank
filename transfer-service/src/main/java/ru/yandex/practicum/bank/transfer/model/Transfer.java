package ru.yandex.practicum.bank.transfer.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import ru.yandex.practicum.commons.dto.bank.PaymentStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transfers", schema = "svc_transfers")
@EqualsAndHashCode(of = "id")
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "payer_account_number", nullable = false)
    private String payerAccountNumber;
    @Column(name = "payee_account_number", nullable = false)
    private String payeeAccountNumber;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    @Column
    private String description;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}