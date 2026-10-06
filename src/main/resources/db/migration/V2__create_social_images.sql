
CREATE TABLE IF NOT EXISTS social_images (

    id UUID PRIMARY KEY,

    cache_key VARCHAR(64) NOT NULL UNIQUE,

    product_id UUID REFERENCES products (id) ON DELETE SET NULL,

    product_title  VARCHAR(255),
    product_price  DOUBLE PRECISION,
    product_image_url TEXT,
    brand          VARCHAR(255),

    r2_key   VARCHAR(512) NOT NULL,

    image_url VARCHAR(512) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_social_images_product_id
    ON social_images (product_id);

COMMENT ON TABLE social_images IS
    'Cache das imagens sociais ja geradas: uma linha por combinacao unica de produto + preco + foto + titulo (cache_key = sha-256)';
