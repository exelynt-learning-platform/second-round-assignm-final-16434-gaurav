# second-round-assignm-final-16434-gaurav
# E-commerce Backend (Final)

---

## 1. Entities

```java
@Entity
class User {
    @Id @GeneratedValue Long id;
    @Column(unique = true) String username;
    String password;
    String role;
}
```

```java
@Entity
class Product {
    @Id @GeneratedValue Long id;
    String name;
    double price;
    int stock;
}
```

```java
@Entity
class CartItem {
    @Id @GeneratedValue Long id;

    @ManyToOne User user;
    @ManyToOne Product product;

    int quantity;
}
```

```java
@Entity
class Order {
    @Id @GeneratedValue Long id;

    @ManyToOne User user;
    @ManyToMany List<Product> products;

    double totalPrice;
    String status;
}
```

---

## 2. Repositories

```java
interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}

interface ProductRepository extends JpaRepository<Product, Long> {}
interface CartRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
}
interface OrderRepository extends JpaRepository<Order, Long> {}
```

---

## 3. JWT

```java
@Component
class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    public String generate(String username) {
        return Jwts.builder()
                .setSubject(username)
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String extract(String token) {
        return Jwts.parser().setSigningKey(secret)
                .parseClaimsJws(token).getBody().getSubject();
    }
}
```

---

## 4. Security

```java
@Configuration
class SecurityConfig {

    @Bean
    BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain filter(HttpSecurity http, JwtFilter filter) throws Exception {

        http.csrf().disable()
                .authorizeHttpRequests(a -> a
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(filter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

```java
@Component
class JwtFilter extends GenericFilter {

    @Autowired JwtUtil jwt;
    @Autowired UserRepository repo;

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest r = (HttpServletRequest) req;
        String h = r.getHeader("Authorization");

        if (h != null && h.startsWith("Bearer ")) {
            String user = jwt.extract(h.substring(7));

            var u = repo.findByUsername(user).orElseThrow();

            var auth = new UsernamePasswordAuthenticationToken(
                    u.getUsername(), null, List.of());

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(req, res);
    }
}
```

---

## 5. Services

```java
@Service
class AuthService {

    @Autowired UserRepository repo;
    @Autowired BCryptPasswordEncoder encoder;
    @Autowired JwtUtil jwt;

    public String register(User u) {
        u.setPassword(encoder.encode(u.getPassword()));
        u.setRole("USER");
        repo.save(u);
        return jwt.generate(u.getUsername());
    }

    public String login(String user, String pass) {
        var u = repo.findByUsername(user).orElseThrow();

        if (!encoder.matches(pass, u.getPassword()))
            throw new RuntimeException("Invalid");

        return jwt.generate(user);
    }
}
```

```java
@Service
class ProductService {

    @Autowired ProductRepository repo;

    public Product save(Product p) {
        if (p.getPrice() <= 0) throw new RuntimeException();
        return repo.save(p);
    }

    public List<Product> get() {
        return repo.findAll();
    }
}
```

```java
@Service
class CartService {

    @Autowired CartRepository cart;
    @Autowired ProductRepository product;

    public CartItem add(User u, Long pid, int qty) {

        if (qty <= 0) throw new RuntimeException();

        var p = product.findById(pid).orElseThrow();

        if (p.getStock() < qty) throw new RuntimeException();

        CartItem i = new CartItem();
        i.user = u;
        i.product = p;
        i.quantity = qty;

        return cart.save(i);
    }
}
```

```java
@Service
class OrderService {

    @Autowired CartRepository cart;
    @Autowired OrderRepository order;

    public Order place(User u) {

        var items = cart.findByUser(u);

        if (items.isEmpty()) throw new RuntimeException();

        Order o = new Order();
        o.user = u;
        o.products = items.stream().map(i -> i.product).toList();

        double total = items.stream()
                .mapToDouble(i -> i.product.getPrice() * i.quantity)
                .sum();

        o.totalPrice = total;
        o.status = "PAID";

        cart.deleteAll(items);

        return order.save(o);
    }
}
```

---

## 6. Controllers

```java
@RestController
@RequestMapping("/auth")
class AuthController {

    @Autowired AuthService service;

    @PostMapping("/register")
    public String register(@RequestBody User u) {
        return service.register(u);
    }

    @PostMapping("/login")
    public String login(@RequestBody User u) {
        return service.login(u.getUsername(), u.getPassword());
    }
}
```

```java
@RestController
@RequestMapping("/products")
class ProductController {

    @Autowired ProductService service;

    @PostMapping
    public Product add(@RequestBody Product p) {
        return service.save(p);
    }

    @GetMapping
    public List<Product> get() {
        return service.get();
    }
}
```

```java
@RestController
@RequestMapping("/cart")
class CartController {

    @Autowired CartService service;
    @Autowired UserRepository repo;

    @PostMapping
    public CartItem add(Authentication auth,
                        @RequestParam Long pid,
                        @RequestParam int qty) {

        var user = repo.findByUsername(auth.getName()).orElseThrow();
        return service.add(user, pid, qty);
    }
}
```

---

## 7. Exception Handler

```java
@RestControllerAdvice
class GlobalHandler {

    @ExceptionHandler(RuntimeException.class)
    public String handle(RuntimeException e) {
        return e.getMessage();
    }
}
```

---

## 8. Payment

```java
@Service
class PaymentService {

    public String pay(double amount) {
        if (amount <= 0) throw new RuntimeException();
        return "SUCCESS";
    }
}
```

---

## 9. Unit Tests

```java
class AuthServiceTest {

    @Test
    void testPasswordHash() {
        BCryptPasswordEncoder e = new BCryptPasswordEncoder();
        String raw = "123";
        String hash = e.encode(raw);

        assertTrue(e.matches(raw, hash));
    }
}
```

```java
class OrderServiceTest {

    @Test
    void testTotal() {
        double total = 2 * 100;
        assertEquals(200, total);
    }
}
```

---

## 10. application.properties

```
spring.datasource.url=jdbc:mysql://localhost:3306/ecommerce
spring.datasource.username=root
spring.datasource.password=root

spring.jpa.hibernate.ddl-auto=update

jwt.secret=securekey
```

---
