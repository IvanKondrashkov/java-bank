package ru.yandex.practicum.bank.cash.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.bank.cash.model.CashOperation;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface CashOperationRepository extends JpaRepository<CashOperation, Long> {
    List<CashOperation> findByAccountNumber(String accountNumber);
}