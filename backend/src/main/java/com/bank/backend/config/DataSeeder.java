package com.bank.backend.config;

import com.bank.backend.model.Account;
import com.bank.backend.model.Customer;
import com.bank.backend.repository.AccountRepository;
import com.bank.backend.repository.CustomerRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CustomerRepository customerRepository,
                      AccountRepository accountRepository,
                      PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (customerRepository.count() > 0) return;

        customerRepository.save(new Customer(100000001, "ahmad", passwordEncoder.encode("password123")));
        customerRepository.save(new Customer(100000002, "sara", passwordEncoder.encode("password123")));

        accountRepository.save(new Account("200000001", 100000001, 1500.75f, "Salary"));
        accountRepository.save(new Account("200000002", 100000001, 320.00f, null));
        accountRepository.save(new Account("200000003", 100000002, 9800.50f, "Savings"));
    }
}
