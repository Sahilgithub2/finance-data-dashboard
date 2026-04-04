CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    role            VARCHAR(32)  NOT NULL CHECK (role IN ('ADMIN', 'ANALYST', 'VIEWER')),
    is_active       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE transactions (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT         NOT NULL REFERENCES users (id),
    amount      DECIMAL(15, 2) NOT NULL,
    type        VARCHAR(32)    NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    category    VARCHAR(100)   NOT NULL,
    date        DATE           NOT NULL,
    notes       VARCHAR(2000),
    is_deleted  BOOLEAN        NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_user_date ON transactions (user_id, date) WHERE is_deleted = FALSE;
CREATE INDEX idx_transactions_type ON transactions (type) WHERE is_deleted = FALSE;
CREATE INDEX idx_transactions_category ON transactions (category) WHERE is_deleted = FALSE;

CREATE TABLE audit_log (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT REFERENCES users (id),
    action     VARCHAR(32)   NOT NULL,
    entity     VARCHAR(100)  NOT NULL,
    entity_id  BIGINT,
    details    VARCHAR(2000),
    logged_at  TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_logged_at ON audit_log (logged_at);
