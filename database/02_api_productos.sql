USE database_swisscol;

-- Estructura reproducible del recurso category.
CREATE TABLE IF NOT EXISTS category (
    category_id INT NOT NULL,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    PRIMARY KEY (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3;

-- Estructura reproducible del recurso product.
CREATE TABLE IF NOT EXISTS product (
    product_id INT NOT NULL,
    name VARCHAR(45) NOT NULL,
    description TEXT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    image VARCHAR(45) NULL,
    category_id INT NOT NULL,
    PRIMARY KEY (product_id),
    INDEX category_id_idx (category_id),
    CONSTRAINT fk_product_category
        FOREIGN KEY (category_id)
        REFERENCES category (category_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb3;

-- Categorías mínimas empleadas por las pruebas de la API.
INSERT IGNORE INTO category (category_id, name, type)
VALUES
    (1, 'Navajas', 'Camping'),
    (2, 'Cuchillos', 'Cocina'),
    (3, 'Herramientas', 'Outdoor'),
    (4, 'Accesorios', 'Premium');

-- Comprobación final sin modificar los productos existentes.
SELECT
    product_id,
    name,
    price,
    stock,
    category_id
FROM product
ORDER BY product_id;
