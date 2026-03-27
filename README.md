# second-round-assignm-final-16434-gaurav

# E-commerce Backend System

Spring Boot backend for an e-commerce platform with authentication, product management, cart, and order processing.

## Project Structure
com.ecommerce
 ├── entity
 ├── repository
 ├── service
 ├── controller

## 1. Main Application
package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }
}

## 2. Entities
package com.ecommerce.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;
    private String role;

    @OneToMany(mappedBy = "user")
    private List<Order> orders;

    public User() {}

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }
}

### Product
package com.ecommerce.entity;

import jakarta.persistence.*;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private double price;
    private int stock;
    private String imageUrl;

    public Product() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
}
```

---

### CartItem

```java
package com.ecommerce.entity;

import jakarta.persistence.*;

@Entity
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Product product;

    private int quantity;

    public CartItem() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setProduct(Product product) { this.product = product; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
```

---

### Order

```java
package com.ecommerce.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @ManyToMany
    private List<Product> products;

    private double totalPrice;
    private String status;
    private String address;

    public Order() {}

    public Long getId() { return id; }
    public User getUser() { return user; }
    public List<Product> getProducts() { return products; }
    public double getTotalPrice() { return totalPrice; }

    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setProducts(List<Product> products) { this.products = products; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setStatus(String status) { this.status = status; }
    public void setAddress(String address) { this.address = address; }
}
```

---

## 3. Repositories

```java
package com.ecommerce.repository;

import com.ecommerce.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

public interface ProductRepository extends JpaRepository<Product, Long> {}

public interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
}

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
```

---

## 4. Services

```java
package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class AuthService {

    private final UserRepository repo;
    private final BCryptPasswordEncoder encoder;

    public AuthService(UserRepository repo, BCryptPasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setRole("USER");
        return repo.save(user);
    }
}

@Service
public class ProductService {

    private final ProductRepository repo;

    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }

    public Product save(Product p) {
        return repo.save(p);
    }

    public List<Product> getAll() {
        return repo.findAll();
    }

    public void delete(Long id) {
        repo.deleteById(id);
    }
}

@Service
public class CartService {

    private final CartRepository cartRepo;
    private final ProductRepository productRepo;

    public CartService(CartRepository cartRepo, ProductRepository productRepo) {
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
    }

    public CartItem add(User user, Long productId, int qty) {
        Product p = productRepo.findById(productId).orElseThrow();
        CartItem item = new CartItem();
        item.setUser(user);
        item.setProduct(p);
        item.setQuantity(qty);
        return cartRepo.save(item);
    }

    public List<CartItem> get(User user) {
        return cartRepo.findByUser(user);
    }
}

@Service
public class OrderService {

    private final OrderRepository orderRepo;
    private final CartRepository cartRepo;

    public OrderService(OrderRepository orderRepo, CartRepository cartRepo) {
        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
    }

    public Order place(User user, String address) {

        List<CartItem> items = cartRepo.findByUser(user);

        Order order = new Order();
        order.setUser(user);
        order.setProducts(items.stream().map(CartItem::getProduct).toList());

        double total = items.stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();

        order.setTotalPrice(total);
        order.setStatus("CREATED");
        order.setAddress(address);

        cartRepo.deleteAll(items);

        return orderRepo.save(order);
    }
}
```

---

## 5. Controllers

```java
package com.ecommerce.controller;

import com.ecommerce.entity.*;
import com.ecommerce.service.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/auth")
class AuthController {

    private final AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return service.register(user);
    }
}

@RestController
@RequestMapping("/products")
class ProductController {

    private final ProductService service;

    public ProductController(ProductService service) {
        this.service = service;
    }

    @PostMapping
    public Product create(@RequestBody Product p) {
        return service.save(p);
    }

    @GetMapping
    public List<Product> getAll() {
        return service.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
```
---

## 6. Unit Tests

### AuthService Test

```java
package com.ecommerce.service;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest {

    @Test
    void registerUser() {
        UserRepository repo = Mockito.mock(UserRepository.class);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        AuthService service = new AuthService(repo, encoder);

        User user = new User();
        user.setUsername("test");
        user.setPassword("1234");

        Mockito.when(repo.save(Mockito.any(User.class))).thenReturn(user);

        User saved = service.register(user);

        assertNotNull(saved);
        assertNotEquals("1234", saved.getPassword());
    }
}
```

---

### CartService Test

```java
package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest {

    @Test
    void addToCart() {
        CartRepository cartRepo = Mockito.mock(CartRepository.class);
        ProductRepository productRepo = Mockito.mock(ProductRepository.class);

        CartService service = new CartService(cartRepo, productRepo);

        User user = new User();
        Product product = new Product();
        product.setId(1L);

        Mockito.when(productRepo.findById(1L)).thenReturn(Optional.of(product));

        CartItem item = service.add(user, 1L, 2);

        assertEquals(2, item.getQuantity());
        assertEquals(product, item.getProduct());
    }
}
```

---

### OrderService Test

```java
package com.ecommerce.service;

import com.ecommerce.entity.*;
import com.ecommerce.repository.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    @Test
    void placeOrder() {
        OrderRepository orderRepo = Mockito.mock(OrderRepository.class);
        CartRepository cartRepo = Mockito.mock(CartRepository.class);

        OrderService service = new OrderService(orderRepo, cartRepo);

        User user = new User();

        Product product = new Product();
        product.setPrice(100);

        CartItem item = new CartItem();
        item.setProduct(product);
        item.setQuantity(2);

        Mockito.when(cartRepo.findByUser(user)).thenReturn(List.of(item));

        Order order = service.place(user, "address");

        assertEquals(200, order.getTotalPrice());
        assertEquals("CREATED", order.getStatus());
    }
}
```
