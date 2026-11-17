package com.example.customer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.customer.entity.Customer;
import com.example.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CustomerOptimisticLockingTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CustomerRepository repo;

    @Test
    void optimisticLockingBlocksStaleAdminBAndPreservesAdminAPhone() throws Exception {
        // Admin A saves phone = 222-2222 with version = 1 -> 200 OK (version becomes 2)
        mockMvc.perform(put("/api/customers/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@old.com\",\"phone\":\"222-2222\",\"version\":1}"))
                .andExpect(status().isOk())
                .andExpect(content().string("Profile updated."));

        // Admin B sends stale version = 1 -> 409 Conflict
        mockMvc.perform(put("/api/customers/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@new.com\",\"phone\":\"111-1111\",\"version\":1}"))
                .andExpect(status().isConflict())
                .andExpect(content().string(
                        "This profile was updated by someone else. Please refresh before saving."));

        // Verify Admin A's phone update was preserved at version = 2
        Customer saved = repo.findById(42L).orElseThrow();
        assertThat(saved.getPhone()).isEqualTo("222-2222");
        assertThat(saved.getEmail()).isEqualTo("john@old.com");
        assertThat(saved.getVersion()).isEqualTo(2);
    }
}
