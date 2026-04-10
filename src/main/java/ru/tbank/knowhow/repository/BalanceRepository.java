package ru.tbank.knowhow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.tbank.knowhow.model.Balance;

@Repository
public interface BalanceRepository extends JpaRepository<Balance, Long> {
}
