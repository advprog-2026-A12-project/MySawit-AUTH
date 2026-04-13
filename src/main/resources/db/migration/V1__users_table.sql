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


CREATE TABLE IF NOT EXISTS buruh_mandor_assignments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    buruh_id UUID NOT NULL,
    mandor_id UUID NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    unassigned_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_bma_buruh FOREIGN KEY (buruh_id) REFERENCES users (id),
    CONSTRAINT fk_bma_mandor FOREIGN KEY (mandor_id) REFERENCES users (id)

    
);

CREATE UNIQUE INDEX idx_bma_buruh_active ON buruh_mandor_assignments (buruh_id)
    WHERE is_active = TRUE;

CREATE INDEX idx_bma_mandor_active ON buruh_mandor_assignments (mandor_id)
    WHERE is_active = TRUE;

-- SELECT idx_bma_mandor_active from buruh_mandor_assignments


CREATE TABLE IF NOT EXISTS refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL,
    device_info VARCHAR(255),
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_rt_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT uq_rt_token_hash UNIQUE (token_hash)
);

CREATE UNIQUE INDEX idx_rt_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_rt_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_rt_expires ON refresh_tokens (expires_at);
