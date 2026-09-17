-- Accounts. Single seller shop, so ADMIN is the operator and CUSTOMER is a buyer.

CREATE TABLE users (
    id            uuid         NOT NULL,
    email         varchar(255) NOT NULL,
    password_hash varchar(255) NOT NULL,
    role          varchar(32)  NOT NULL,
    created_at    timestamptz  NOT NULL,
    CONSTRAINT pk_users PRIMARY KEY (id),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT ck_users_role CHECK (role IN ('CUSTOMER', 'ADMIN'))
);

-- Orders may belong to an account, but guest checkout keeps this null
ALTER TABLE orders ADD COLUMN user_id uuid;
ALTER TABLE orders ADD CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id);
CREATE INDEX ix_orders_user ON orders (user_id);

-- Carts belong to a browser session, so a leaked cart id alone is not enough
ALTER TABLE carts ADD COLUMN session_id varchar(128);
CREATE INDEX ix_carts_session ON carts (session_id);
