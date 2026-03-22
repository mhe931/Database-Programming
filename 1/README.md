# Exercise 1 — JDBC CRUD Operations with SQLite

A Java Maven project demonstrating the four fundamental database operations — **Create, Read, Update, Delete** — against an SQLite database, using JDBC and the `sqlite-jdbc` driver.

---

## Objective

Build a clean, modular Java application that manages a **products** table in a local SQLite database. The exercise covers:

- Establishing a JDBC connection to an embedded SQLite database.
- Executing parameterised SQL statements with `PreparedStatement`.
- Implementing safe resource management with `try-with-resources`.
- Performing a full CRUD lifecycle (INSERT → SELECT → UPDATE → DELETE).

---

## Project Structure

```
1/
├── pom.xml                                  # Maven build configuration
├── README.md                                # This file
└── src/
    └── main/
        └── java/
            └── com/
                └── inventory/
                    ├── DatabaseConfig.java   # Connection string & schema setup
                    ├── ProductDAO.java        # Data Access Object (CRUD logic)
                    └── Main.java             # Entry point — demo lifecycle
```

---

## Database Schema

The `products` table is created automatically on first run:

| Column     | Type              | Description                        |
|------------|-------------------|------------------------------------|
| `id`       | INTEGER (PK, AI)  | Auto-incrementing primary key      |
| `name`     | TEXT NOT NULL      | Product name                       |
| `category` | TEXT NOT NULL      | Logical grouping (e.g. Electronics)|
| `price`    | REAL NOT NULL      | Unit price                         |

The database file `inventory.db` is generated in the working directory at runtime.

---

## Class Overview

### `DatabaseConfig`
Centralises the database connection logic. Provides:
- `getConnection()` — returns a `java.sql.Connection` to the SQLite file.
- `initializeDatabase()` — creates the `products` table if it does not exist.

### `ProductDAO`
Encapsulates all SQL operations using `PreparedStatement` for safety:
- `createProduct(String name, String category, double price)` — inserts a new product.
- `getAllProducts()` — retrieves all products as a formatted list.
- `updateProductPrice(int id, double newPrice)` — updates price by product ID.
- `deleteProduct(int id)` — deletes a product by ID.

### `Main`
Demonstrates the complete CRUD lifecycle by inserting sample products, listing them, updating a price, deleting a record, and listing the final state.

---

## Prerequisites

| Tool   | Version  | Notes                                             |
|--------|----------|---------------------------------------------------|
| Java   | 17+      | Tested with Eclipse Temurin JDK 17                |
| Maven  | 3.9+     | For dependency resolution and build                |

> The `sqlite-jdbc` driver is declared in `pom.xml` and downloaded automatically by Maven — no manual JAR management required.

---

## How to Run

```powershell
# 1. Navigate to the exercise folder
cd "Exercise\1"

# 2. (Optional) Delete old database to start fresh
Remove-Item -Path inventory.db -ErrorAction SilentlyContinue

# 3. Build and run
mvn clean compile exec:java
```

### Expected Output

```
═══════════════════════════════════════════════════════
       Product Inventory — JDBC CRUD Demo
═══════════════════════════════════════════════════════

[DB] Database initialized — 'products' table is ready.

── INSERT Operations ──────────────────────────────────
[CREATE] Product added: Wireless Mouse | Electronics | 29.99
[CREATE] Product added: USB-C Hub | Electronics | 49.95
[CREATE] Product added: Standing Desk | Furniture | 349.00
[CREATE] Product added: Mechanical Keyboard | Electronics | 89.50
[CREATE] Product added: Monitor Arm | Accessories | 44.75

── SELECT ALL Products ────────────────────────────────
  ID: 1 | Name: Wireless Mouse     | Category: Electronics  | Price: 29.99
  ID: 2 | Name: USB-C Hub          | Category: Electronics  | Price: 49.95
  ID: 3 | Name: Standing Desk      | Category: Furniture    | Price: 349.00
  ID: 4 | Name: Mechanical Keyboard| Category: Electronics  | Price: 89.50
  ID: 5 | Name: Monitor Arm        | Category: Accessories  | Price: 44.75

── UPDATE Operation ───────────────────────────────────
[UPDATE] Product ID 1 — new price: 24.99

── Products after UPDATE ──────────────────────────────
  ID: 1 | Name: Wireless Mouse     | Category: Electronics  | Price: 24.99
  ...

── DELETE Operation ───────────────────────────────────
[DELETE] Product ID 3 removed.

── Products after DELETE ──────────────────────────────
  (Standing Desk no longer appears)

═══════════════════════════════════════════════════════
       CRUD lifecycle complete.
═══════════════════════════════════════════════════════
```

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **SQLite** | Zero-configuration embedded database — no server setup needed, ideal for coursework. |
| **`PreparedStatement`** | Prevents SQL injection by binding parameters with typed setters instead of string concatenation. |
| **`try-with-resources`** | Guarantees `Connection`, `Statement`, and `ResultSet` are closed automatically, even on exceptions. |
| **Separate `DatabaseConfig`** | Decouples connection details from business logic — changing the database only requires editing one class. |
| **DAO pattern** | Keeps SQL isolated in `ProductDAO`, making `Main` focused purely on the workflow. |

---

## Notes

- The SLF4J warning in the console (`Failed to load class "org.slf4j.impl.StaticLoggerBinder"`) is harmless — the SQLite driver ships with an optional SLF4J dependency. It does not affect functionality.
- Running the application multiple times **without** deleting `inventory.db` will insert duplicate products, since the table uses `AUTOINCREMENT` and does not enforce unique names. Delete the database file between runs for clean results.

---

## Author

Daniel Ebrahimzadeh — University of Vaasa, Database Programming Course
