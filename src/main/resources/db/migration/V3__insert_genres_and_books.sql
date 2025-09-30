INSERT INTO genres(name) VALUES
                             ('Software'),
                             ('Fantasy'),
                             ('Fiction');


INSERT INTO books(title, price, author_id, genre_id) VALUES
                ('Clean Code', 42.99, (SELECT id FROM authors WHERE name='Robert C. Martin'), (SELECT id FROM genres WHERE name='Software')),
                ('Refactoring', 39.99, (SELECT id FROM authors WHERE name='Martin Fowler'), (SELECT id FROM genres WHERE name='Software')),
                ('Harry Potter and the Philosopher''s Stone', 19.99, (SELECT id FROM authors WHERE name='J. K. Rowling'), (SELECT id FROM genres WHERE name='Fantasy'));