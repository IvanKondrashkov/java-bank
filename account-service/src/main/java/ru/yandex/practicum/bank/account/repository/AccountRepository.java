package ru.yandex.practicum.bank.account.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.bank.account.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUser_UsernameAndAccountNumber(String username, String accountNumber);
    Optional<Account> findByUser_Username(String username);
    Optional<Account> findByAccountNumber(String accountNumber);
    List<Account> findAllByUser_Username(String username);
    boolean existsByAccountNumber(String accountNumber);
}