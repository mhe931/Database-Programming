# Advanced Database Operations: Managing One-to-Many Relationships
**Course:** Database Programming  
**University:** University of Vaasa  
**Author:** Daniel Ebrahimzadeh (Master's Student in Artificial Intelligence)

---

## 1. Introduction and Personal Reflection
Salaam! Welcome to my academic report for the "Advanced Database Operations" exercise. As a Master’s student focusing primarily on Artificial Intelligence here at the University of Vaasa, my usual daily routine involves working with continuous arrays, tensors, and unstructured data streams. So, navigating the strict, organized world of relational databases through the Java Persistence API (JPA) and Hibernate framework has been an eye-opening, and occasionally humbling, journey for me. 

First of all, I consciously wanted to avoid just copying the standard "Employee" and "Department" mapping examples that we saw so many times in our tutorials. It is always better for my own learning and conceptualization to construct an architecture slightly different. So, I decided to model an "Inventory System" with `Category` and `Product`. In this structural relationship, one Category can represent many Products, but a distinct Product belongs to only one specific Category. 

In this reflection document, I will walk you through my technical design rationale, present some of the Java code snippets that instrument this relationship, and honestly discuss some specific edge-case struggles I faced—especially surrounding SQLite and lazy initializing data sets.

*(Page 1 of 4)*

<br><br><br><br><br><br><br><br><br><br><br><br><br><br>

---

## 2. Rationale for the Category-Product Relationship

When designing machine learning classification engines, we often cluster data points into discrete buckets or labels. I found myself thinking about e-commerce tabular datasets where "Category" (such as Electronics, Home Appliances, or Clothing) acts as the central node, and specific transaction items like a "Laptop" or "Microwave" are the attached leaves. 

This is logically a pure **One-to-Many relationship**. 

From an underlying database perspective, the parent layout (`categories`) needs an indexed primary key, and the child (`products`) must carry a foreign key parameter (`category_id`) that references the parent. In an object-oriented paradigm like Java, we want to mirror this cleanly. Ideally, if I invoke a `Category` object context, I expect to naturally call `.getProducts()` to see everything registered inside it. 

I set the constraint to strictly deploy onto **SQLite**. Why SQLite? Because in typical academic AI projects, or just for rapid deployment validation, having a lightweight, serverless database file is incredibly resourceful. There is no need to spool up a heavy PostgreSQL or MySQL Docker container solely to test basic CRUD capabilities. The data resides comfortably in `inventory.db`.

*(Page 2 of 4)*

<br><br><br><br><br><br><br><br><br><br><br><br><br><br>

---

## 3. Relationship Mapping Details

To implement this bidirectional relationship properly, I mapped standard `jakarta.persistence` annotations rather than legacy `javax`. Here is a breakdown of the structural entities:

### 3.1 The Category Entity (The "One" Side)
In `Category.java`, I instantiated the relationship using `@OneToMany`. It was strategically critical to include `CascadeType.ALL` so that when my service logic commits a Category, all its assigned products propagate to the persist phase automatically. Additionally, if I drop the Category, the embedded products must vanish hierarchically due to `orphanRemoval = true`.

```java
@Entity
@Table(name = "categories")
public class Category {
    // ... basic parameter block

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Product> products = new ArrayList<>();

    // Internal helper method to maintain synchronization 
    public void addProduct(Product product) {
        products.add(product);
        product.setCategory(this);
    }
}
```
Notice the `addProduct` method syntax above? In the AI domain, we generally operate statelessly, so we sometimes overlook how strictly stateful JVM environments are. I learned the hard way that JPA frameworks require setting both sides of a bidirectional association manually before a flush sequence. This helper method guarantees I never forget to anchor the product back to its parent category instance.

### 3.2 The Product Entity (The "Many" Side)
In `Product.java`, the configuration handles the exact foreign key schema in the backend table infrastructure.

```java
@Entity
@Table(name = "products")
public class Product {
    // ... metric configurations

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
}
```
Deploying the `@JoinColumn` explicitly commands the Hibernate ORM dialect generator to insert a localized integer property tagged `category_id` into the `products` table space. 

*(Page 3 of 4)*

<br><br><br><br><br><br><br><br><br><br><br><br><br><br>

---

## 4. Challenges Faced

### 4.1 The Dreaded `LazyInitializationException`
Oh, this was indeed a headache! During the Read transaction phase inside `InventoryService.java`, I retrieved a targeted Category structure and gracefully closed out the `EntityManager` node. Subsequence to that, inside my localized `Main.java` loop, I requested iterate through `category.getProducts()`. My application crashed abruptly throwing a severe `LazyInitializationException`.

Because I strictly deployed `@ManyToOne(fetch = FetchType.LAZY)` and by global default `@OneToMany` arrays are inherently lazy mappings, Hibernate systematically prevents loading the associated products sequence from the database node until explicitly evaluated. However, because I had safely closed the Java `EntityManager` connection (effectively destroying the active session), Hibernate could no longer query the storage medium! 

**My Resolution Approach:** Directly inside my isolated service layer, while the standard `try-finally` sequence was still processing and the resource transaction remained open, I forcefully instantiated the list memory tree by executing `category.getProducts().size()`. It feels slightly like a workaround algorithm, but it efficiently compiles the subset items into ram before the DB session concludes execution.

### 4.2 SQLite Integrity Constraints
A secondary challenge formulated via the SQLite architecture itself. The SQLite local implementation possesses a quirky trait where Foreign Key limits are frequently defaulted to "disabled" unless forcefully overridden by the specific driver connection pool. Furthermore, maneuvering dialects inside Hibernate 6.4 forced me to inject the `hibernate-community-dialects` library dependency, since raw SQLite has been deprecated from the central ORM core in newer release bands. 

When drafting the manual raw schema script (`schema.sql`), I incorporated definitive cascade sequences natively:
```sql
CREATE TABLE IF NOT EXISTS products (
    -- ... schema variables ...
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
);
```
Simultaneously, JPA's internal `CascadeType.ALL` directive parses the cascading deletes locally on the Persistence Context layer, subsequently streaming distinct DELETE statements recursively to the SQLite handler.

## 5. Console Execution Trace
Attached below is a terminal extraction serving as execution verification. It proves that the designed `InventoryService.java` lifecycle validates thoroughly without logic errors across Create, Read, Update mappings, and Cascading Delete thresholds.

```text
=========================================================
--- Starting Advanced Database Operations (One-to-Many) ---
=========================================================

[STEP 1] Creating Categories and Products...
Hibernate: insert into categories (name) values (?) returning id
Hibernate: insert into products (category_id,name,price) values (?,?,?) returning id
Hibernate: insert into products (category_id,name,price) values (?,?,?) returning id
>>> SUCCESS: Created Category with Products: Electronics
Hibernate: insert into categories (name) values (?) returning id
Hibernate: insert into products (category_id,name,price) values (?,?,?) returning id
>>> SUCCESS: Created Category with Products: Home Appliances

[STEP 2] Fetching Category with Products...
Hibernate: select c1_0.id,c1_0.name from categories c1_0 where c1_0.id=?
Hibernate: select p1_0.category_id,p1_0.id,p1_0.name,p1_0.price from products p1_0 where p1_0.category_id=?
Fetched Category: Electronics
  - Laptop ($1200.0)
  - Smartphone ($800.0)

[STEP 3] Moving a Product between Categories...
Hibernate: select p1_0.id,p1_0.category_id,p1_0.name,p1_0.price from products p1_0 where p1_0.id=?
Hibernate: select c1_0.id,c1_0.name from categories c1_0 where c1_0.id=?
Hibernate: select p1_0.category_id,p1_0.id,p1_0.name,p1_0.price from products p1_0 where p1_0.category_id=?
Hibernate: update products set category_id=?,name=?,price=? where id=?
>>> SUCCESS: Moved Product ID 3 to Category ID 1
Hibernate: select c1_0.id,c1_0.name from categories c1_0 where c1_0.id=?
Hibernate: select p1_0.category_id,p1_0.id,p1_0.name,p1_0.price from products p1_0 where p1_0.category_id=?
Updated Electronics Products:
  - Laptop
  - Smartphone
  - Microwave

[STEP 4] Deleting Category to trigger Cascade Delete...
Hibernate: select c1_0.id,c1_0.name from categories c1_0 where c1_0.id=?
Hibernate: select p1_0.category_id,p1_0.id,p1_0.name,p1_0.price from products p1_0 where p1_0.category_id=?
Hibernate: delete from products where id=?
Hibernate: delete from products where id=?
Hibernate: delete from products where id=?
Hibernate: delete from categories where id=?
>>> SUCCESS: Deleted Category ID 1 (cascading deletes to products triggered)

Verification: Trying to fetch deleted category...
Hibernate: select c1_0.id,c1_0.name from categories c1_0 where c1_0.id=?
>>> Verified: Electronics Category and its associated Products are completely deleted.

=========================================================
--- Application Finished Successfully ---
=========================================================
```

Thank you taking the time to review this implementation. Iterating through this procedural database model definitely solidified my competency inside standard relational frameworks!

*(Page 4 of 4)*
