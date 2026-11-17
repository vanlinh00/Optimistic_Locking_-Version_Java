# Optimistic Locking in Spring Boot (Simple Guide)

This project shows how **Optimistic Locking** stops two users from accidentally overwriting each other's changes in a database.

---

## I) The Problem: Silent Overwrite (Lost Update)

Imagine two admins (**Admin A** and **Admin B**) open the same customer profile (**User #42 — John Doe**) at the exact same time. Standard web forms send the **whole profile** back to the server when you click Save.

1. **Admin A** and **Admin B** both open John's profile:
   * `email = "john@old.com"`
   * `phone = "111-1111"`
2. **Admin A** changes the phone number to `"222-2222"` and clicks **Save**.
3. **Admin B** changes the email to `"john@new.com"` and clicks **Save**. Admin B does not know that Admin A just updated the phone number, so Admin B's form still has the old phone number (`"111-1111"`).
4. **Result:** Admin B's save overwrites the entire record (`email = "john@new.com"`, `phone = "111-1111"`). Admin A's phone number update is **silently deleted** with no warning.

---

## II) How Optimistic Locking Solves It

Optimistic Locking adds a hidden **`version`** number to the customer record and sends it with the form. Before saving, the database checks if the version number is still the same.

1. Both admins open John's profile with **`version = 1`**.
2. **Admin A** saves first:
   * The database checks: `WHERE id = 42 AND version = 1` → **Match!**
   * It saves `phone = "222-2222"` and increases the version to **`version = 2`**.
3. **Admin B** clicks Save next, sending back their old **`version = 1`**.
4. The database checks: `WHERE id = 42 AND version = 1` → **No match!** (The database is already at `version = 2`).
5. JPA stops the save and throws `ObjectOptimisticLockingFailureException`. Your app can now show a safe message to **Admin B**:
   > *"This profile was updated by someone else. Please refresh the page to see the latest changes before saving."*

---

## III) Short, Easy-to-Understand Code

You only need three small files to use Optimistic Locking in Spring Boot.

### 1. Entity (`Customer.java`) — Add `@Version`

Add the `@Version` annotation to a field. Spring Data JPA will automatically manage and increase this number on every update.

```java
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
```

### 2. Repository (`CustomerRepository.java`)

No special code is needed in the repository.

```java
public interface CustomerRepository extends JpaRepository<Customer, Long> {
}
```

### 3. Service (`CustomerService.java`) — Check the Version Before Saving

Compare the `formVersion` sent by the user's screen with the current `version` in the database.

```java
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
```

---

## Summary: When Should You Use It?

* **Use Optimistic Locking** when many people read data, and updates rarely happen at the exact same second (for example: user profiles, articles, settings, or medical charts).
* **Benefits:** It is fast, does not lock database rows while users are reading, and prevents silent data loss.