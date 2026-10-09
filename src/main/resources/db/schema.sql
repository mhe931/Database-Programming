CREATE TABLE IF NOT EXISTS categories (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (
        length(trim(name, char(9) || char(10) || char(11) || char(12) || char(13) || ' ')) BETWEEN 1 AND 100
    ),
    description TEXT CHECK (description IS NULL OR length(description) <= 500)
);

CREATE TABLE IF NOT EXISTS suppliers (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (
        length(trim(name, char(9) || char(10) || char(11) || char(12) || char(13) || ' ')) BETWEEN 1 AND 120
    ),
    email TEXT CHECK (email IS NULL OR length(email) <= 254)
);

CREATE TABLE IF NOT EXISTS warehouses (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL UNIQUE CHECK (
        length(trim(name, char(9) || char(10) || char(11) || char(12) || char(13) || ' ')) BETWEEN 1 AND 120
    ),
    location TEXT NOT NULL CHECK (
        length(trim(location, char(9) || char(10) || char(11) || char(12) || char(13) || ' ')) BETWEEN 1 AND 240
    )
);

CREATE TABLE IF NOT EXISTS products (
    id INTEGER PRIMARY KEY,
    name TEXT NOT NULL CHECK (
        length(trim(name, char(9) || char(10) || char(11) || char(12) || char(13) || ' ')) BETWEEN 1 AND 160
    ),
    sku TEXT NOT NULL UNIQUE CHECK (
        length(trim(sku, char(9) || char(10) || char(11) || char(12) || char(13) || ' ')) BETWEEN 1 AND 40
    ),
    description TEXT CHECK (description IS NULL OR length(description) <= 500),
    price NUMERIC NOT NULL CHECK (typeof(price) IN ('integer', 'real') AND price >= 0),
    category_id INTEGER NOT NULL REFERENCES categories(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS product_suppliers (
    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    supplier_id INTEGER NOT NULL REFERENCES suppliers(id) ON DELETE CASCADE,
    PRIMARY KEY (product_id, supplier_id)
);

CREATE TABLE IF NOT EXISTS inventory_items (
    id INTEGER PRIMARY KEY,
    product_id INTEGER NOT NULL REFERENCES products(id) ON DELETE RESTRICT,
    warehouse_id INTEGER NOT NULL REFERENCES warehouses(id) ON DELETE RESTRICT,
    quantity INTEGER NOT NULL CHECK (typeof(quantity) = 'integer' AND quantity >= 0),
    UNIQUE (product_id, warehouse_id)
);
