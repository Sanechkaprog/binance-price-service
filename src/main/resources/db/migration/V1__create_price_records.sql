CREATE TABLE price_records(
    id BIGSERIAL,
    symbol VARCHAR(20) NOT NULL,
    price NUMERIC(30, 12) NOT NULL,
    fetched_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT price_records_pk PRIMARY KEY(id)

);

CREATE INDEX idx_price_symbol_fetched_at ON price_records(symbol, fetched_at)