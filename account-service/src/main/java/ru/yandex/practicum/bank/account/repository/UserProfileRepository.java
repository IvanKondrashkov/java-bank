package ru.yandex.practicum.bank.account.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.bank.account.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUsername(String username);
}