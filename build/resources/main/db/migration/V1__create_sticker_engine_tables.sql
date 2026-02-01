-- Flyway migration V1: Create sticker engine tables

-- Shoppers table to track sticker balances
CREATE TABLE IF NOT EXISTS shoppers (
    shopper_id VARCHAR(64) PRIMARY KEY,
    total_stickers INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Transactions table to store purchase transactions
CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id VARCHAR(64) NOT NULL UNIQUE,
    shopper_id VARCHAR(64) NOT NULL,
    store_id VARCHAR(64) NOT NULL,
    timestamp TIMESTAMP NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    stickers_earned INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_shopper FOREIGN KEY (shopper_id) REFERENCES shoppers(shopper_id)
);

-- Transaction items table to store individual items in a transaction
CREATE TABLE IF NOT EXISTS transaction_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    sku VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    category VARCHAR(64) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_items_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

-- Indexes for common queries
CREATE INDEX idx_transactions_transaction_id ON transactions(transaction_id);
CREATE INDEX idx_transactions_shopper_id ON transactions(shopper_id);
CREATE INDEX idx_transactions_store_id ON transactions(store_id);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp);
CREATE INDEX idx_transaction_items_transaction_id ON transaction_items(transaction_id);
CREATE INDEX idx_transaction_items_sku ON transaction_items(sku);
CREATE INDEX idx_transaction_items_category ON transaction_items(category);
