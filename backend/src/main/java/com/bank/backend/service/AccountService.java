package com.bank.backend.service;

import com.bank.backend.dto.AccountResponse;
import com.bank.backend.model.Account;
import com.bank.backend.repository.AccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public List<AccountResponse> getAccounts(Integer customerNumber) {
        return accountRepository.findByCustomerNumber(customerNumber)
                .stream()
                .map(AccountResponse::from)
                .toList();
    }

    public AccountResponse getAccount(String accountNumber, Integer customerNumber) {
        return AccountResponse.from(findOwnedAccount(accountNumber, customerNumber));
    }

    @Transactional
    public AccountResponse updateNickname(String accountNumber, Integer customerNumber, String nickname) {
        Account account = findOwnedAccount(accountNumber, customerNumber);
        String cleaned = (nickname == null || nickname.isBlank()) ? null : nickname.trim();
        account.setNickname(cleaned);
        return AccountResponse.from(accountRepository.save(account));
    }

    private Account findOwnedAccount(String accountNumber, Integer customerNumber) {
        return accountRepository.findByAccountNumberAndCustomerNumber(accountNumber, customerNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
    }
}
