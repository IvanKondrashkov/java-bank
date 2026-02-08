package ru.yandex.practicum.bank.account.repository;

import org.springframework.stereotype.Repository;
import ru.yandex.practicum.bank.account.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    boolean existsByOperationId(String operationId);
}