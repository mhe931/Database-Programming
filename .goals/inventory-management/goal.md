# Goal: Complete the Inventory Management project

## User Request

Take end-to-end responsibility for completing the clean `Database-Programming` repository as a working Java 17, Maven, Jakarta Persistence/Hibernate, and SQLite inventory-management course project. Implement, test, run, verify the database, document the result, review the changes, commit, push to `origin/main`, and verify the remote. Preserve the reset repository and do not restore old project files. Keep the implementation straightforward and factual; do not make authorship or AI-detection claims.

## Refined Goal

Complete the clean root Maven scaffold into a small console Inventory Management System using five JPA entities, their specified relationships, SQLite constraints and active foreign-key enforcement, understandable data access and transactional behavior, and a meaningful console demonstration. Add automated coverage, concise technical and evidence documentation, and a schema artifact verified against the actual generated database. Validate the application end-to-end, then commit and normally push the intended changes to `main` without force-pushing or adding generated database/build output.

## Acceptance Criteria

- [ ] The project remains a root-level Java 17 Maven project and builds with `mvn clean verify`.
- [ ] Category, Product, Supplier, Warehouse, and InventoryItem are mapped to SQLite with the required fields, validation, constraints, and relationships, including a genuine Product/Supplier many-to-many join table.
- [ ] Runtime SQLite connections enforce foreign keys; the application demonstrates a rejected invalid FK and a rollback without inconsistent writes.
- [ ] Understandable CRUD/data access and useful service behavior exist for all five entities, with safe EntityManager and transaction handling.
- [ ] A runnable console demo exercises fictional multilingual data, CRUD, relationships, queries, validation, database failure, rollback, and cleanup.
- [ ] Automated tests cover validation, CRUD, relationships, database constraints, persistence/reread, FK enforcement, and rollback using an isolated/clean test database.
- [ ] README, a schema artifact checked against the running SQLite schema, factual engineering/report support, and a real-evidence checklist are present.
- [ ] The application runs successfully; actual SQLite metadata verifies expected tables, keys, constraints, and Unicode round-tripping.
- [ ] Changes pass `git diff --check` and final review; no old project files, secrets, runtime DB, or `target/` are committed.
- [ ] Changes are committed on `main`, pushed normally to `origin/main`, and the remote state is verified.
- [ ] Final delivery states actual test/build/database/Git results, review findings, limitations, and items the user should personally review for coursework.

## Scope Boundaries

**In scope:**
- The current clean root project, its source, Maven configuration, tests, README, schema/evidence/report-support documentation, Git commit and normal push.
- Simple Java/JPA/SQLite design and course-level clarity.

**Out of scope:**
- Restarting the repository reset or restoring `1/`, `2/`, `3/`, `InventoryManagement_DanielEbrahimzadeh/`, old reports, the old Product-only project, or generated `target/` files.
- Spring Boot, REST, web UI, Docker, cloud, Lombok, dependency-injection frameworks, or unnecessary generic/enterprise abstractions.
- Deceptive authorship claims, detector evasion, or fabricated evidence.
- Force-pushes, history rewriting, or resurrecting old branches.

## Applicable Project Conventions

**Quality gate command:**
- `mvn clean verify`, plus the configured project run command.

**Commit convention:**
- No documented or enforced repository convention; recent history is inconsistent. Use conventional commits with the task-required Copilot co-author trailer.

**Guidelines:**
- No `AGENTS.md`, `CONSTITUTION.md`, `.agents/guidelines/`, or `.github/guidelines/` found.

**Rules:**
- Preserve unrelated work; never restore excluded legacy content or commit generated artifacts.
- `mvn clean verify` is the only repository-configured quality gate; Maven currently has no additional test/lint plugins beyond the project test configuration.
