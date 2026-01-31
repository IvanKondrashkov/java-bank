package ru.yandex.practicum.bank.transfer.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.bank.transfer.model.Transfer;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, Long> {
    List<Transfer> findAllByPayerAccountNumber(String accountNumber);
    List<Transfer> findAllByPayeeAccountNumber(String accountNumber);
}