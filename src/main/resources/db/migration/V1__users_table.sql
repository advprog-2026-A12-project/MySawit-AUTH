-- Enable trigram extension for name search
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    name VARCHAR(100) NOT NULL,
    password_hash VARCHAR(255),
    role VARCHAR(20) NOT NULL,
    mandor_certification_number VARCHAR(100),
    oauth_provider VARCHAR(20),
    oauth_provider_id VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT uq_users_email UNIQUE (email),
    CONSTRAINT uq_users_mandor_cert UNIQUE (mandor_certification_number),
    CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'BURUH', 'MANDOR', 'SUPIR_TRUK')),
    CONSTRAINT chk_mandor_cert CHECK (
        (role != 'MANDOR') OR (mandor_certification_number IS NOT NULL)
    ),
    CONSTRAINT chk_password_or_oauth CHECK (
        password_hash IS NOT NULL OR oauth_provider IS NOT NULL
    )
);

CREATE UNIQUE INDEX idx_users_email ON users (email);
CREATE INDEX idx_users_role ON users (role);
CREATE UNIQUE INDEX idx_users_oauth ON users (oauth_provider, oauth_provider_id)
    WHERE oauth_provider IS NOT NULL;
CREATE INDEX idx_users_name_trgm ON users USING GIN (name gin_trgm_ops);
