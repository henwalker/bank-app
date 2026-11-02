package com.bank.backend.controller;

import com.bank.backend.dto.AccountResponse;
import com.bank.backend.dto.NicknameRequest;
import com.bank.backend.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountResponse> getAccounts(@AuthenticationPrincipal Integer customerNumber) {
        return accountService.getAccounts(customerNumber);
    }

    @GetMapping("/{accountNumber}")
    public AccountResponse getAccount(@PathVariable String accountNumber,
                                      @AuthenticationPrincipal Integer customerNumber) {
        return accountService.getAccount(accountNumber, customerNumber);
    }

    @PatchMapping("/{accountNumber}/nickname")
    public AccountResponse updateNickname(@PathVariable String accountNumber,
                                          @Valid @RequestBody NicknameRequest request,
                                          @AuthenticationPrincipal Integer customerNumber) {
        return accountService.updateNickname(accountNumber, customerNumber, request.nickname());
    }
}
