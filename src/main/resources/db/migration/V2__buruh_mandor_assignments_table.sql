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