CREATE TABLE IF NOT EXISTSusers (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    CONSTRAINT chk_users_role CHECK (role IN ('Admin', 'Buruh', 'Mandor', 'Supir'))
);
