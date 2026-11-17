package com.example.customer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Customer {
    @Id
    @GeneratedValue
    private Long id;

    private String email;
    private String phone;

    @Version // Hidden version number checked automatically on save
    private int version;

    public Customer() {}
    public Customer(String email, String phone) {
        this.email = email;
        this.phone = phone;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public int getVersion() { return version; }
}
