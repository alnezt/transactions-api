CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    status VARCHAR(20) NOT NULL,
    sale_amount NUMERIC(19, 2) NOT NULL CHECK (sale_amount > 0),
    commission_amount NUMERIC(19, 2) NOT NULL CHECK (commission_amount > 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
