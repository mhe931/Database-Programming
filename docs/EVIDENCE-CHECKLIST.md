# Verification and evidence checklist

## Checks run for this iteration

- [x] Loaded `C:\Users\k430533\tools\database-programming-env.ps1`; verified OpenJDK/Javac 17.0.20.1 and Maven 3.9.16.
- [x] `mvn -q clean verify` completed successfully after implementation (JUnit suite included).
- [x] `mvn -q exec:java` completed successfully and printed successful CRUD/query, validation rejection, foreign-key rejection, rollback, and cleanup steps.
- [x] Automated isolated-database tests verified expected tables, SQLite FK pragma/rejection, uniqueness and nonnegative constraints, relationships/reread, rollback, validation, and Unicode.
- [x] Inspected `inventory_db.sqlite` with Python's SQLite driver: all six expected tables exist; products has its category FK, inventory has both FKs, and the join table has both FKs. Unique indexes and check constraints are present.
- [x] On that actual database, enabled `PRAGMA foreign_keys`, rejected an invalid join-table FK, round-tripped multilingual text, and verified rolled-back fixture rows were absent afterwards.
- [ ] Personal review: run the commands above on your own machine and inspect the demo output.
- [ ] Personal review: compare the mappings/schema and design decisions with course requirements; add any required screenshots or instructor-specific evidence yourself.

The checked boxes above record implementation checks only; they do not claim that screenshots, instructor review, or external acceptance have occurred. Runtime database/build artifacts are ignored and are not evidence files to submit; create any course-required screenshots or exports yourself.
