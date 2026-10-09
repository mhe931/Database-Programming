# Engineering notes

- Persistence uses resource-local JPA transactions. Repository writes each run in a transaction; `JpaRepository.inTransaction` is available to group work. Entity managers are closed per operation and the console closes its entity manager factory.
- Bean Validation annotations guard application input. The SQLite DDL independently enforces required fields, unique identifiers and stock pairs, nonnegative prices and quantities, and foreign keys.
- The SQLite Hibernate dialect did not generate the full foreign-key/unique schema needed here. `SqliteSchema` applies the checked-in DDL before Hibernate opens the unit; `foreign_keys=on` activates enforcement on each SQLite connection. The database is used with Hibernate schema generation disabled to avoid silently creating a weaker schema.
- Supplier/product membership is stored in the owning `product_suppliers` join table. Inventory uses one row per product and warehouse.
- The tests use a fresh database under JUnit's temporary directory. The console's default database is a local ignored runtime artifact; it is not checked in.
- There is no migration history, concurrent stock reservation, user interface beyond the demonstration, or multi-process coordination. Changing fields or constraints requires deliberate schema evolution before an existing database can safely be reused.
