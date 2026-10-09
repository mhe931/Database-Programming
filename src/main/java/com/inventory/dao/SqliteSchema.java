package com.inventory.dao;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Applies the SQLite DDL that Hibernate's SQLite dialect cannot fully express. */
public final class SqliteSchema {
    private SqliteSchema() {
    }

    public static void initialize(String jdbcUrl) {
        try (InputStream stream = SqliteSchema.class.getResourceAsStream("/db/schema.sql")) {
            if (stream == null) {
                throw new IllegalStateException("Missing /db/schema.sql");
            }
            String script = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            try (var connection = DriverManager.getConnection(jdbcUrl);
                 var statement = connection.createStatement()) {
                try (var result = statement.executeQuery("PRAGMA foreign_keys")) {
                    if (!result.next() || result.getInt(1) != 1) {
                        throw new IllegalStateException("SQLite foreign-key enforcement must be enabled");
                    }
                }
                for (String sql : script.split(";")) {
                    if (!sql.isBlank()) {
                        statement.execute(sql);
                    }
                }
                verifySchema(connection);
            }
        } catch (IOException | SQLException e) {
            throw new IllegalStateException("Could not initialize SQLite schema", e);
        }
    }

    private static void verifySchema(java.sql.Connection connection) throws SQLException {
        Set<String> expectedTables = Set.of(
                "categories", "products", "suppliers", "warehouses", "product_suppliers", "inventory_items");
        List<String> actualTables = new ArrayList<>();
        try (var statement = connection.createStatement();
             var result = statement.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'")) {
            while (result.next()) {
                actualTables.add(result.getString(1));
            }
        }
        if (!expectedTables.equals(Set.copyOf(actualTables))) {
            throw new IllegalStateException("SQLite tables do not match the inventory schema: " + actualTables);
        }

        Map<String, Set<String>> expectedForeignKeys = Map.of(
                "products", Set.of("categories"),
                "product_suppliers", Set.of("products", "suppliers"),
                "inventory_items", Set.of("products", "warehouses"),
                "categories", Set.of(), "suppliers", Set.of(), "warehouses", Set.of());
        for (var expected : expectedForeignKeys.entrySet()) {
            Set<String> referencedTables = new HashSet<>();
            try (var statement = connection.createStatement();
                 var result = statement.executeQuery("PRAGMA foreign_key_list(" + expected.getKey() + ")")) {
                while (result.next()) {
                    referencedTables.add(result.getString("table"));
                }
            }
            if (!referencedTables.equals(expected.getValue())) {
                throw new IllegalStateException("Unexpected foreign keys for " + expected.getKey() + ": "
                        + referencedTables);
            }
        }
        try (var statement = connection.createStatement();
             var result = statement.executeQuery("PRAGMA foreign_key_check")) {
            if (result.next()) {
                throw new IllegalStateException("SQLite contains rows with invalid foreign keys");
            }
        }

        verifyUniqueIndex(connection, "categories", List.of("name"));
        verifyUniqueIndex(connection, "products", List.of("sku"));
        verifyUniqueIndex(connection, "suppliers", List.of("name"));
        verifyUniqueIndex(connection, "warehouses", List.of("name"));
        verifyUniqueIndex(connection, "product_suppliers", List.of("product_id", "supplier_id"));
        verifyUniqueIndex(connection, "inventory_items", List.of("product_id", "warehouse_id"));

        Map<String, List<String>> requiredChecks = Map.of(
                "categories", List.of(requiredTextCheck("name", 100)),
                "suppliers", List.of(requiredTextCheck("name", 120)),
                "warehouses", List.of(
                        requiredTextCheck("name", 120),
                        requiredTextCheck("location", 240)),
                "products", List.of("price >= 0",
                        requiredTextCheck("name", 160),
                        requiredTextCheck("sku", 40)),
                "inventory_items", List.of("quantity >= 0"));
        for (var required : requiredChecks.entrySet()) {
            String ddl;
            try (var statement = connection.createStatement();
                 var result = statement.executeQuery("SELECT sql FROM sqlite_master WHERE type='table' AND name='"
                         + required.getKey() + "'")) {
                if (!result.next()) {
                    throw new IllegalStateException("Missing table " + required.getKey());
                }
                ddl = result.getString(1).toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
            }
            for (String expression : required.getValue()) {
                if (!ddl.contains(expression)) {
                    throw new IllegalStateException("Missing CHECK constraint for " + required.getKey() + ": "
                            + expression);
                }
            }
        }
    }

    private static String requiredTextCheck(String column, int maxLength) {
        return "length(trim(" + column
                + ", char(9) || char(10) || char(11) || char(12) || char(13) || ' ')) between 1 and "
                + maxLength;
    }

    private static void verifyUniqueIndex(java.sql.Connection connection, String table, List<String> expectedColumns)
            throws SQLException {
        try (var statement = connection.createStatement();
             var indexes = statement.executeQuery("PRAGMA index_list(" + table + ")")) {
            while (indexes.next()) {
                if (indexes.getInt("unique") != 1) {
                    continue;
                }
                String indexName = indexes.getString("name");
                List<String> columns = new ArrayList<>();
                try (var indexStatement = connection.createStatement();
                     var indexColumns = indexStatement.executeQuery("PRAGMA index_info('" + indexName + "')")) {
                    while (indexColumns.next()) {
                        columns.add(indexColumns.getString("name"));
                    }
                }
                if (columns.equals(expectedColumns)) {
                    return;
                }
            }
        }
        throw new IllegalStateException("Missing unique index on " + table + expectedColumns);
    }
}
