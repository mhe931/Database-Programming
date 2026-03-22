# Database Programming: Exercise 2 Report (JPA Transition)
**Topic:** Transitioning from Raw JDBC to JPA/Hibernate ORM
**Student Name:** Daniel Ebrahimzadeh
**Course:** Database Programming, University of Vaasa

---

## 1. Relevance: The `products` Table for Inventory Management
While many academic tutorials default to an "Employee" database, this exercise strictly models an inventory management paradigm using a `products` table. Within a commercial architecture, inventory data structures represent the backbone of e-commerce and retail backend systems. 

The `products` table (`id`, `name`, `category`, `price`) was retained from Exercise 1 because it allows for a direct comparison between raw JDBC and JPA mapping frameworks. Utilizing standard JPA annotations (`@Entity`, `@Table`, `@Id`, `@Column`), this schema translates effortlessly into an Object-Relational Mapping (ORM) proxy, allowing the Java domain model to drive the database structure rather than writing manual SQL scripts.

---

## 2. Technical Implementation: Code Blocks

The transition from JDBC required substituting SQL statements with object-state manipulations via the `EntityManager`.

### 2.1 The JPA Entity (`Product.java`)
The domain class maps to the SQLite database via standard JPA annotations.

```java
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Double price;
    
    // Default constructor required by JPA
    public Product() {}

    public Product(String name, String category, Double price) {
        this.name = name;
        this.category = category;
        this.price = price;
    }
    
    // ... Getters, Setters, and toString() omitted for brevity ...
}
```

### 2.2 The Data Access Object (`ProductDAO.java`)
Unlike raw JDBC which requires `PreparedStatement` interfaces, CRUD operations are now managed by analyzing the object lifecycle.

```java
/**
 * Creates and persists a new Product entity into the database.
 */
public void createProduct(String name, String category, Double price) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();
        Product product = new Product(name, category, price);
        em.persist(product); // JPA handles the INSERT
        tx.commit();
    } catch (RuntimeException e) {
        if (tx.isActive()) tx.rollback();
        throw e;
    } finally {
        em.close();
    }
}

/**
 * Updates an existing product's category and price utilizing dirty checking.
 */
public void updateProduct(Long id, String newCategory, Double newPrice) {
    EntityManager em = emf.createEntityManager();
    EntityTransaction tx = em.getTransaction();
    try {
        tx.begin();
        Product product = em.find(Product.class, id);
        if (product != null) {
            product.setCategory(newCategory);
            product.setPrice(newPrice);
            // JPA automatically issues an UPDATE statement upon commit due to dirty checking
            tx.commit(); 
        } else {
            tx.rollback();
        }
    } catch (RuntimeException e) {
         // ... error handling
    } finally {
        em.close();
    }
}

/**
 * Executes JPQL to retrieve all persisted Product records.
 */
public List<Product> getAllProducts() {
    EntityManager em = emf.createEntityManager();
    try {
        // JPQL queries the object model, not the database tables directly
        return em.createQuery("SELECT p FROM Product p", Product.class).getResultList();
    } finally {
        em.close();
    }
}
```

---

## 3. Troubleshooting: JPA Challenges & Resolutions

Integrating Hibernate with SQLite presented distinct architectural challenges compared to native JDBC:

1. **Configuring the Persistence Unit (`persistence.xml`):** 
   SQLite is not natively fully supported by standard Jakarta specifications. Resolving this required utilizing the `hibernate-community-dialects` module (specifically `org.hibernate.community.dialect.SQLiteDialect`) in Maven. Without this extension, Hibernate defaults to generic JDBC interactions, disabling critical ORM features like primary key identity generation (`AUTOINCREMENT`).
2. **Handling Transaction Boundaries:**
   In raw JDBC, autocommit is generally implicitly enabled. In JPA, data modification operations (INSERT, UPDATE, DELETE) explicitly throw `TransactionRequiredException` if executed outside an active transaction. The architectural solution required structuring every mutating method in the DAO within strict `try-catch-finally` blocks initialized by `EntityManager.getTransaction().begin()` and finalized via `.commit()`. 
3. **Ghost Connections and Connection Leaks:**
   Unlike the `Connection` block from Exercise 1, `EntityManager` objects do not autonomously garbage-collect their underlying socket bindings gracefully. Ensuring the `EntityManager` is closed in a `finally` block, and the overarching `EntityManagerFactory` is systematically closed upon application termination (`emf.close()`), was paramount to preventing SQLite database locks.

---

## 4. Execution Output Matrix
Below is a consolidated terminal slice demonstrating the complete lifecycle, executed by the `Main.java` bootstrapper.

```text
===================================================================
           JPA / Hibernate CRUD Operations Demo
===================================================================
[BOOTSTRAP] Initializing EntityManagerFactory...

[1] --- INSERT OPERATIONS ---
[CREATE] Product [ID=1, Name=Ergonomic Chair, Category=Furniture   , Price=299.50]
[CREATE] Product [ID=2, Name=Bluetooth Adapter, Category=Electronics , Price=15.00]
[CREATE] Product [ID=3, Name=Mechanical Keyboard, Category=Electronics , Price=120.00]

[2] --- READ (SELECT ALL) ---
    FOUND -> Product [ID=1, Name=Ergonomic Chair, Category=Furniture   , Price=299.50]
    FOUND -> Product [ID=2, Name=Bluetooth Adapter, Category=Electronics , Price=15.00]
    FOUND -> Product [ID=3, Name=Mechanical Keyboard, Category=Electronics , Price=120.00]

[3] --- UPDATE OPERATION ---
[UPDATE] ID 2 updated -> Product [ID=2, Name=Bluetooth Adapter, Category=Accessories , Price=18.75]

[4] --- SELECT AFTER UPDATE ---
    FOUND -> Product [ID=1, Name=Ergonomic Chair, Category=Furniture   , Price=299.50]
    FOUND -> Product [ID=2, Name=Bluetooth Adapter, Category=Accessories , Price=18.75]
    FOUND -> Product [ID=3, Name=Mechanical Keyboard, Category=Electronics , Price=120.00]

[5] --- DELETE OPERATION ---
[DELETE] Product ID 3 removed.

[6] --- FINAL STATE (SELECT ALL) ---
    FOUND -> Product [ID=1, Name=Ergonomic Chair, Category=Furniture   , Price=299.50]
    FOUND -> Product [ID=2, Name=Bluetooth Adapter, Category=Accessories , Price=18.75]

[BOOTSTRAP] EntityManagerFactory gracefully closed.
```

---
*End of Report*
