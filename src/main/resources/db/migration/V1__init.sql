CREATE TABLE authors (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE genres (
                        id BIGSERIAL PRIMARY KEY,
                        name VARCHAR(255) NOT NULL UNIQUE
);


CREATE TABLE books (
                       id BIGSERIAL PRIMARY KEY,
                       title VARCHAR(512) NOT NULL,
                       price NUMERIC(10,2) NOT NULL CHECK (price > 0),
                       author_id BIGINT NOT NULL REFERENCES authors(id),
                       genre_id BIGINT NOT NULL REFERENCES genres(id),
                       created_at TIMESTAMPTZ DEFAULT now(),
                       updated_at TIMESTAMPTZ DEFAULT now()
);


CREATE INDEX idx_books_title ON books(title);