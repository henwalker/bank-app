package com.bank.backend.controller;

import com.bank.backend.dto.LoginRequest;
import com.bank.backend.dto.LoginResponse;
import com.bank.backend.model.Customer;
import com.bank.backend.repository.CustomerRepository;
import com.bank.backend.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthController(CustomerRepository customerRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        Optional<Customer> customer = customerRepository.findByUsername(request.username());

        if (customer.isEmpty() || !passwordEncoder.matches(request.password(), customer.get().getPasswordHash())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid username or password"));
        }

        Integer customerNumber = customer.get().getCustomerNumber();
        String token = jwtService.generateToken(customerNumber);
        return ResponseEntity.ok(new LoginResponse(token, customerNumber));
    }
}
