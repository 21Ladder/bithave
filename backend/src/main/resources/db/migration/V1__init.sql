-- Baseline schema. Prices are stored in EUR minor units (cents).

CREATE TABLE categories (
    id        uuid         NOT NULL,
    path      varchar(120) NOT NULL,
    parent_id uuid,
    CONSTRAINT pk_categories PRIMARY KEY (id),
    CONSTRAINT uq_categories_path UNIQUE (path),
    CONSTRAINT fk_categories_parent FOREIGN KEY (parent_id) REFERENCES categories (id)
);

CREATE INDEX ix_categories_parent ON categories (parent_id);

CREATE TABLE listings (
    id                uuid         NOT NULL,
    title             varchar(120) NOT NULL,
    price_cents       bigint       NOT NULL,
    currency          varchar(3)   NOT NULL DEFAULT 'EUR',
    stock_quantity    integer      NOT NULL DEFAULT 0,
    reserved_quantity integer      NOT NULL DEFAULT 0,
    status            varchar(32)  NOT NULL,
    seller_id         uuid         NOT NULL,
    category_id       uuid         NOT NULL,
    version           bigint       NOT NULL DEFAULT 0,
    created_at        timestamptz  NOT NULL,
    updated_at        timestamptz  NOT NULL,
    CONSTRAINT pk_listings PRIMARY KEY (id),
    CONSTRAINT fk_listings_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT ck_listings_status CHECK (status IN ('ACTIVE', 'SOLD', 'ARCHIVED')),
    CONSTRAINT ck_listings_price_positive CHECK (price_cents >= 0),
    CONSTRAINT ck_listings_stock_non_negative CHECK (stock_quantity >= 0),
    CONSTRAINT ck_listings_reserved_within_stock CHECK (reserved_quantity >= 0 AND reserved_quantity <= stock_quantity)
);

-- supports the default listing query: active listings, newest first
CREATE INDEX ix_listings_status_created_at ON listings (status, created_at DESC);
CREATE INDEX ix_listings_category ON listings (category_id);
CREATE INDEX ix_listings_seller ON listings (seller_id);

CREATE TABLE listing_images (
    listing_id uuid    NOT NULL,
    url        text    NOT NULL,
    position   integer NOT NULL,
    CONSTRAINT pk_listing_images PRIMARY KEY (listing_id, position),
    CONSTRAINT fk_listing_images_listing FOREIGN KEY (listing_id) REFERENCES listings (id) ON DELETE CASCADE
);

CREATE TABLE carts (
    id         uuid        NOT NULL,
    created_at timestamptz NOT NULL,
    updated_at timestamptz NOT NULL,
    CONSTRAINT pk_carts PRIMARY KEY (id)
);

CREATE TABLE cart_items (
    id               uuid        NOT NULL,
    cart_id          uuid        NOT NULL,
    listing_id       uuid        NOT NULL,
    quantity         integer     NOT NULL,
    unit_price_cents bigint      NOT NULL,
    created_at       timestamptz NOT NULL,
    updated_at       timestamptz NOT NULL,
    CONSTRAINT pk_cart_items PRIMARY KEY (id),
    CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts (id) ON DELETE CASCADE,
    CONSTRAINT fk_cart_items_listing FOREIGN KEY (listing_id) REFERENCES listings (id),
    CONSTRAINT uq_cart_items_cart_listing UNIQUE (cart_id, listing_id),
    CONSTRAINT ck_cart_items_quantity_positive CHECK (quantity > 0)
);

CREATE INDEX ix_cart_items_cart ON cart_items (cart_id);

CREATE TABLE btc_price (
    id              bigint NOT NULL,
    price_euro      numeric(19, 2),
    price_timestamp timestamptz,
    fetched_at      timestamptz,
    CONSTRAINT pk_btc_price PRIMARY KEY (id)
);
