ALTER TABLE books ADD COLUMN IF NOT EXISTS caption VARCHAR(255);
ALTER TABLE books ADD COLUMN IF NOT EXISTS description TEXT;
ALTER TABLE books ADD COLUMN IF NOT EXISTS isbn VARCHAR(20);
ALTER TABLE books ADD COLUMN IF NOT EXISTS published_year INTEGER;
ALTER TABLE books ADD COLUMN IF NOT EXISTS publisher VARCHAR(255);
ALTER TABLE books ADD COLUMN IF NOT EXISTS page_count INTEGER;
ALTER TABLE books ADD COLUMN IF NOT EXISTS language VARCHAR(10);
ALTER TABLE books ADD COLUMN IF NOT EXISTS stock INTEGER NOT NULL DEFAULT 0;
ALTER TABLE books ADD COLUMN IF NOT EXISTS cover_image_url VARCHAR(1024);


-- Unique (when not empty) ISBN using functional index
DO $$
BEGIN
IF NOT EXISTS (
SELECT 1 FROM pg_indexes WHERE indexname = 'uk_books_isbn'
) THEN
CREATE UNIQUE INDEX uk_books_isbn ON books ((NULLIF(isbn, '')));
END IF;
END$$;


ALTER TABLE books
    ADD CONSTRAINT chk_books_page_count_nonneg CHECK (page_count IS NULL OR page_count >= 0) NOT VALID;
ALTER TABLE books VALIDATE CONSTRAINT chk_books_page_count_nonneg;


CREATE INDEX IF NOT EXISTS idx_books_isbn ON books(isbn);