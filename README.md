# Inventory Management

A small Java 17 console application using Jakarta Persistence, Hibernate 6, and SQLite. It models categories, products, suppliers, warehouses, and per-warehouse stock. Product/supplier is a many-to-many relationship; inventory is unique for each product/warehouse pair.

## Build and run

With Java 17 and Maven installed:

```text
mvn clean verify
mvn exec:java
```

The demo uses fictional multilingual values, creates and queries related records, adjusts stock, exercises validation and SQLite foreign-key rejection, demonstrates a rolled-back write, then removes the demo records. It initializes `inventory_db.sqlite` in the project root. The runtime database and Maven `target/` directory are ignored by Git.

Use a different database with a JVM property, for example:

```text
mvn exec:java -Dinventory.jdbc.url="jdbc:sqlite:demo.sqlite?foreign_keys=on"
```

`src/main/resources/db/schema.sql` is the SQLite schema source applied by `SqliteSchema`; Hibernate's SQLite dialect does not emit the full foreign-key and uniqueness DDL required by this project. The JDBC URL enables SQLite foreign keys for each connection. Schema initialization creates missing tables but is not a versioned migration system; back up any existing database before changing the schema.

## Project layout

- `src/main/java/com/inventory/entity/` — five JPA entities and mapping/validation annotations.
- `src/main/java/com/inventory/dao/` — transaction-safe repository operations and SQLite schema initialization.
- `src/main/java/com/inventory/service/` — entity CRUD services and product/stock queries.
- `src/test/java/com/inventory/` — isolated SQLite persistence, relationship, validation, constraint, rollback, and Unicode tests.
- `src/main/resources/META-INF/persistence.xml` — local JPA configuration.
- `src/main/resources/db/schema.sql` — checked SQLite DDL.
- `docs/` — implementation notes and a verification checklist.

This is an educational single-process application. The built-in Hibernate connection pool and create-if-missing schema initialization are suitable for the demo, not a production deployment or multi-user inventory service.
