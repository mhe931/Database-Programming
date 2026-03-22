# Final Phase Report: Inventory Management System (JPA/Hibernate Integration)
**Engineer:** Daniel Ebrahimzadeh
**Course:** Database Programming, University of Vaasa

---

## 1. Relational Database Overview: The `products` Table
A standard relational approach for modeling inventory focuses strictly on core transactional matrices rather than abstracted personnel data such as the canonical `EMPLOYEE` table schema often found in elementary tutorials. The foundational data structure adopted here is the `products` table, consisting of `id`, `name`, `category`, `price`, and `stock_quantity`.

### Relevance
The introduction of `stock_quantity` explicitly validates this schema's relevance to commercial and retail enterprise logic. High-throughput inventory systems hinge on tracking accurate stock aggregates and fluctuating prices to drive upstream purchasing decisions. Isolating these exact scalar fields enables a clean translation into JPA metadata processing, highlighting deterministic entity state evaluations rather than opaque SQL script injection.

---

## 2. Java Persistence Architecture (Implementation Snippets)
Deploying the application required transitioning the logic layer into robust JPA boundaries leveraging standard `EntityManager` interactions.

### 2.1 Entity Mapping (`Product.java`)
The domain object is explicitly bound utilizing class-level and field-level JPA specifications.

```java
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false)
    private Double price;

    @Column(name = "stock_quantity", nullable = false)
    private Integer stockQuantity;
    
    // Default constructor mandated by JPA standard proxy architecture
    public Product() {}

    // Object constructors, getters, and setters isolated below
    // ...
}
```

### 2.2 Orchestrated Service Logic (`ProductService.java`)
Service layer functions handle the full lifecycle transaction management directly against the `EntityManager` session instance. JPQL handles generic retrievals while Hibernate resolves dynamic state generation (Dirty Checking).

**Read (JPQL Iteration):**
```java
public List<Product> getAllProducts() {
    EntityManager em = null;
    try {
        em = emf.createEntityManager();
        TypedQuery<Product> query = em.createQuery("SELECT p FROM Product p", Product.class);
        return query.getResultList();
    } finally {
        if (em != null) em.close();
    }
}
```

**Update (Dynamic Dirty Checking):**
```java
public void updateProduct(Long id, Double newPrice, Integer newStock) {
    EntityManager em = null;
    EntityTransaction tx = null;
    try {
        em = emf.createEntityManager();
        tx = em.getTransaction();
        tx.begin();
        
        Product product = em.find(Product.class, id);
        if (product != null) {
            product.setPrice(newPrice);
            product.setStockQuantity(newStock);
            tx.commit(); // Transaction commit implicitly triggers SQL UPDATE
        }
    } catch (RuntimeException e) {
        if (tx != null && tx.isActive()) tx.rollback();
        throw e;
    } finally {
        if (em != null) em.close();
    }
}
```

---

## 3. Deployment Challenges & Technical Resolutions

**1. Configuring the Persistence Unit for SQLite:**
Standard Jakarta distributions omit native support for SQLite databases because the protocol historically lacks robust container features. Bootstrapping the `<persistence-unit>` within `persistence.xml` required injecting the explicit `org.hibernate.community.dialect.SQLiteDialect` property mapping. To fully resolve this at the container layer, the `hibernate-community-dialects` library dependency was injected into the Maven POM execution phase. Without this mapping, Hibernate throws dialect generation errors inhibiting the system startup.

**2. State Isolation & Transaction Exceptions:**
Architecting enterprise-tier interactions necessitates explicit error capturing to deter transactional leaks. Operations lacking a bounded `tx.begin()` and `tx.commit()` call map fail directly by throwing `TransactionRequiredException`. To resolve this, `try-catch-finally` cascades were rigorously employed throughout the `ProductService.java` lifecycle paths. Should any persistence logic fail at runtime, active boundaries fire `tx.rollback()` deterministically and safely decouple the underlying pool context utilizing `.close()`.

---

## 4. Execution Trace Validation
The following trace mirrors the execution logic dictated within `Main.java`, mapping entity persistence from startup parameters down through terminal updates and database deletion executions.

```text
=======================================================================
        Inventory Management System — JPA Validation Subroutine
=======================================================================

[BOOTSTRAP] Spawning EntityManagerFactory [InventoryPU]...

[1] --- SYSTEM INGESTION (CREATE) ---
[CREATE] Product [ID=1, Name=Ultra-wide Monitor  , Category=Hardware       , Price= 849.00, Stock=  42]
[CREATE] Product [ID=2, Name=Developer Keyboard  , Category=Peripherals    , Price= 129.50, Stock= 105]
[CREATE] Product [ID=3, Name=Ergo Standing Desk  , Category=Furniture      , Price= 550.00, Stock=  18]
[CREATE] Product [ID=4, Name=Optical Gaming Mouse, Category=Peripherals    , Price=  65.00, Stock= 210]

[2] --- INVENTORY AUDIT (READ ALL) ---
    AUDIT -> Product [ID=1, Name=Ultra-wide Monitor  , Category=Hardware       , Price= 849.00, Stock=  42]
    AUDIT -> Product [ID=2, Name=Developer Keyboard  , Category=Peripherals    , Price= 129.50, Stock= 105]
    AUDIT -> Product [ID=3, Name=Ergo Standing Desk  , Category=Furniture      , Price= 550.00, Stock=  18]
    AUDIT -> Product [ID=4, Name=Optical Gaming Mouse, Category=Peripherals    , Price=  65.00, Stock= 210]

[3] --- INVENTORY FLIGHT (UPDATE) ---
[UPDATE] ID 2 modified -> Product [ID=2, Name=Developer Keyboard  , Category=Peripherals    , Price= 115.00, Stock= 140]

[4] --- AUDIT POST-FLIGHT ---
    AUDIT -> Product [ID=1, Name=Ultra-wide Monitor  , Category=Hardware       , Price= 849.00, Stock=  42]
    AUDIT -> Product [ID=2, Name=Developer Keyboard  , Category=Peripherals    , Price= 115.00, Stock= 140]
    AUDIT -> Product [ID=3, Name=Ergo Standing Desk  , Category=Furniture      , Price= 550.00, Stock=  18]
    AUDIT -> Product [ID=4, Name=Optical Gaming Mouse, Category=Peripherals    , Price=  65.00, Stock= 210]

[5] --- SYSTEM SCRUB (DELETE) ---
[DELETE] Discontinued Product ID 3 successfully scrubbed from database.

[6] --- FINAL STATE AUDIT (READ ALL) ---
    AUDIT -> Product [ID=1, Name=Ultra-wide Monitor  , Category=Hardware       , Price= 849.00, Stock=  42]
    AUDIT -> Product [ID=2, Name=Developer Keyboard  , Category=Peripherals    , Price= 115.00, Stock= 140]
    AUDIT -> Product [ID=4, Name=Optical Gaming Mouse, Category=Peripherals    , Price=  65.00, Stock= 210]

[BOOTSTRAP] EntityManagerFactory gracefully collapsed. Execution resolved safely.
```

---
*End of Report*
