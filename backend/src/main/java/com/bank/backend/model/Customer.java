package com.bank.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @Column(name = "customer_number", nullable = false)
    private Integer customerNumber;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    protected Customer() {}

    public Customer(Integer customerNumber, String username, String passwordHash) {
        this.customerNumber = customerNumber;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public Integer getCustomerNumber() { return customerNumber; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
}
