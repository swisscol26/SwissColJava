USE database_swisscol;

-- Retira temporalmente las relaciones con la tabla users.
ALTER TABLE orders
    DROP FOREIGN KEY fk_orders_users;

ALTER TABLE shopping_cart
    DROP FOREIGN KEY fk_shopping_cart_users;

ALTER TABLE notifications
    DROP FOREIGN KEY fk_notifications_users;

-- Prepara users para el registro y el inicio de sesión.
ALTER TABLE users
    MODIFY user_id INT NOT NULL AUTO_INCREMENT,
    ADD email VARCHAR(100) NOT NULL AFTER user_name,
    ADD user_role VARCHAR(20) NOT NULL
        DEFAULT 'CUSTOMER' AFTER password,
    ADD created_at TIMESTAMP NOT NULL
        DEFAULT CURRENT_TIMESTAMP AFTER user_role,
    ADD CONSTRAINT uq_users_email UNIQUE (email);

-- Restaura las relaciones originales.
ALTER TABLE orders
    ADD CONSTRAINT fk_orders_users
    FOREIGN KEY (users_id)
    REFERENCES users (user_id);

ALTER TABLE shopping_cart
    ADD CONSTRAINT fk_shopping_cart_users
    FOREIGN KEY (users_id)
    REFERENCES users (user_id);

ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_users
    FOREIGN KEY (users_id)
    REFERENCES users (user_id);