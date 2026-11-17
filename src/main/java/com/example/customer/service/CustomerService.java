package com.example.customer.service;

import com.example.customer.entity.Customer;
import com.example.customer.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository repo;

    @Transactional
    public void updateProfile(Long id, String newEmail, String newPhone, int formVersion) {
        Customer customer = repo.findById(id).orElseThrow();

        // Check if the version on the admin's screen still matches the database
        if (customer.getVersion() != formVersion) {
            throw new ObjectOptimisticLockingFailureException(Customer.class, id);
        }

        customer.setEmail(newEmail);
        customer.setPhone(newPhone);
        // When the method finishes, JPA saves the changes and increases version (1 -> 2)
    }
}
