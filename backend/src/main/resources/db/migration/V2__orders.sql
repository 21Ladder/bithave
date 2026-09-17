-- Orders, the record of what was actually bought, plus cart lifecycle columns.

ALTER TABLE carts ADD COLUMN status varchar(32) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE carts ADD COLUMN expires_at timestamptz;
ALTER TABLE carts ADD CONSTRAINT ck_carts_status CHECK (status IN ('ACTIVE', 'CONVERTED', 'EXPIRED'));

-- sweeper looks for active carts past their expiry
CREATE INDEX ix_carts_status_expires_at ON carts (status, expires_at);

CREATE TABLE orders (
    id                  uuid         NOT NULL,
    order_number        varchar(32)  NOT NULL,
    status              varchar(32)  NOT NULL,
    cart_id             uuid,
    email               varchar(255) NOT NULL,
    shipping_name       varchar(160) NOT NULL,
    shipping_street     varchar(255) NOT NULL,
    shipping_postal_code varchar(32) NOT NULL,
    shipping_city       varchar(160) NOT NULL,
    shipping_country    varchar(2)   NOT NULL,
    total_cents         bigint       NOT NULL,
    currency            varchar(3)   NOT NULL,
    btcpay_invoice_id   varchar(128),
    version             bigint       NOT NULL DEFAULT 0,
    created_at          timestamptz  NOT NULL,
    updated_at          timestamptz  NOT NULL,
    paid_at             timestamptz,
    CONSTRAINT pk_orders PRIMARY KEY (id),
    CONSTRAINT uq_orders_order_number UNIQUE (order_number),
    CONSTRAINT uq_orders_btcpay_invoice UNIQUE (btcpay_invoice_id),
    CONSTRAINT fk_orders_cart FOREIGN KEY (cart_id) REFERENCES carts (id),
    CONSTRAINT ck_orders_status CHECK (status IN ('PENDING_PAYMENT', 'PAID', 'FULFILLED', 'EXPIRED', 'CANCELLED')),
    CONSTRAINT ck_orders_total_positive CHECK (total_cents > 0)
);

-- the settlement poller scans pending orders by age
CREATE INDEX ix_orders_status_created_at ON orders (status, created_at);
CREATE INDEX ix_orders_email ON orders (email);

CREATE TABLE order_items (
    id               uuid         NOT NULL,
    order_id         uuid         NOT NULL,
    listing_id       uuid         NOT NULL,
    title_snapshot   varchar(120) NOT NULL,
    unit_price_cents bigint       NOT NULL,
    quantity         integer      NOT NULL,
    CONSTRAINT pk_order_items PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT ck_order_items_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX ix_order_items_order ON order_items (order_id);

-- Webhook replay protection, one row per BTCPay delivery
CREATE TABLE processed_webhook_events (
    delivery_id varchar(128) NOT NULL,
    event_type  varchar(64)  NOT NULL,
    received_at timestamptz  NOT NULL,
    CONSTRAINT pk_processed_webhook_events PRIMARY KEY (delivery_id)
);
