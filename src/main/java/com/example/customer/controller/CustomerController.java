package com.example.customer.controller;

import com.example.customer.service.CustomerService;
import java.util.NoSuchElementException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomerController {

    @Autowired
    private CustomerService service;

    public record UpdateProfileRequest(String email, String phone, int version) {}

    @PutMapping("/api/customers/{id}")
    public ResponseEntity<String> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest request
    ) {
        service.updateProfile(id, request.email(), request.phone(), request.version());
        return ResponseEntity.ok("Profile updated.");
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<String> handleConflict() {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("This profile was updated by someone else. Please refresh before saving.");
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound() {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found.");
    }
}
