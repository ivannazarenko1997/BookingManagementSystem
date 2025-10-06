CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    enabled BOOLEAN NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ
    );


CREATE TABLE role (
                      id BIGSERIAL PRIMARY KEY,
                      name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS user_to_role (
                                            id BIGSERIAL PRIMARY KEY,
                                            user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id BIGINT NOT NULL REFERENCES role(id)  ON DELETE CASCADE,
    CONSTRAINT uq_user_role UNIQUE (user_id, role_id)
    );


INSERT INTO role(name) VALUES ('ADMIN') ON CONFLICT DO NOTHING;
INSERT INTO role(name) VALUES ('USER')  ON CONFLICT DO NOTHING;

-- Admin (idempotent)
INSERT INTO users(username, password_hash, enabled)
VALUES ('admin', '$2b$10$.YdRsjaO4akzXxJuSSV/C.iH5WyzBp/G.lLggtYpFgnZmtTJyLBf.', TRUE)
    ON CONFLICT (username) DO NOTHING;

INSERT INTO user_to_role(user_id, role_id)
SELECT u.id, r.id FROM users u, role r
WHERE u.username='admin' AND r.name='ADMIN'
    ON CONFLICT DO NOTHING;

-- USER: alice.user / UserPass1!
INSERT INTO users(username, password_hash, enabled)
VALUES ('alice.user', '$2a$10$kgrdXE8kSN6CmUeyfx8oi.KktLEwrYoWktceLhq97WxK4E59Elz.u', TRUE)
    ON CONFLICT (username) DO NOTHING;

INSERT INTO user_to_role(user_id, role_id)
SELECT u.id, r.id FROM users u, role r
WHERE u.username='alice.user' AND r.name='USER'
    ON CONFLICT DO NOTHING;

-- USER: bob.user / UserPass2!
INSERT INTO users(username, password_hash, enabled)
VALUES ('bob.user', '$2a$10$kgrdXE8kSN6CmUeyfx8oi.KktLEwrYoWktceLhq97WxK4E59Elz.u', TRUE)
    ON CONFLICT (username) DO NOTHING;

INSERT INTO user_to_role(user_id, role_id)
SELECT u.id, r.id FROM users u, role r
WHERE u.username='bob.user' AND r.name='USER'
    ON CONFLICT DO NOTHING;