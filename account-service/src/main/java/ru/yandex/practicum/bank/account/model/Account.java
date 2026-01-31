package ru.yandex.practicum.bank.account.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import ru.yandex.practicum.commons.dto.bank.Currency;
import ru.yandex.practicum.commons.dto.bank.AccountStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "accounts", schema = "svc_accounts")
@EqualsAndHashCode(of = "id")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "account_number", unique = true, nullable = false, length = 20)
    private String accountNumber;
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserProfile user;
    @Version
    private Long version;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return AccountStatus.ACTIVE.equals(status);
    }

    public boolean hasSufficientFunds(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
}