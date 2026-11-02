package com.bank.backend.repository;

import com.bank.backend.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findByCustomerNumber(Integer customerNumber);
    Optional<Account> findByAccountNumberAndCustomerNumber(String accountNumber, Integer customerNumber);
}
