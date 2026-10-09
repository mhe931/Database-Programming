# Verification and evidence checklist

## Checks run for this iteration

- [x] Loaded `C:\Users\k430533\tools\database-programming-env.ps1`; verified OpenJDK/Javac 17.0.20.1 and Maven 3.9.16.
- [x] `mvn -q clean verify` completed successfully after implementation (8 isolated SQLite tests, 0 failures).
- [x] `mvn -q exec:java '-Dinventory.jdbc.url=jdbc:sqlite:target/final-verification.sqlite?foreign_keys=on'` completed successfully on a fresh SQLite file and printed the Persian/Arabic sample, queries, validation rejection, foreign-key rejection, rollback, and cleanup.
- [x] Automated isolated-database tests verified expected tables, SQLite FK pragma/rejection, uniqueness and nonnegative constraints, relationships/reread, rollback, validation, and Unicode.
- [x] Inspected `target/final-verification.sqlite` with Python's SQLite driver: all six expected tables, columns, PKs, FKs, unique indexes, and check constraints are present; `PRAGMA foreign_keys` is `1`, `foreign_key_check` is empty, and demo cleanup left no rows.
- [x] With SQLite FK enforcement enabled, confirmed `PRAGMA foreign_keys` is `1`; direct SQL rejected invalid foreign keys and whitespace-only required fields. Automated tests verified Unicode persistence/reread and rollback consistency.
- [ ] Personal review: run the commands above on your own machine and inspect the demo output.
- [ ] Personal review: compare the mappings/schema and design decisions with course requirements; add any required screenshots or instructor-specific evidence yourself.

The checked boxes above record implementation checks only; they do not claim that screenshots, instructor review, or external acceptance have occurred. Runtime database/build artifacts are ignored and are not evidence files to submit; create any course-required screenshots or exports yourself.
