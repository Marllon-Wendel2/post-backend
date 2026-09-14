CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY,
    seller_name VARCHAR(255),
    email VARCHAR(255) NOT NULL UNIQUE,
    hash_password VARCHAR(255),
    phone_number VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    title VARCHAR(255),
    price DOUBLE PRECISION,
    image_url VARCHAR(255),
    is_active BOOLEAN,
    user_id UUID NOT NULL REFERENCES users(id)
);
