# 1) Start Postgres

docker compose up -d db

# 2) Run the app (local JVM)

mvn spring-boot:run

# or build & run container

mvn -q -DskipTests clean package && docker compose up --build

Auth:

ADMIN → admin/admin123

USER → user/user123

Open Swagger UI: http://localhost:8080/swagger-ui.html

1) Swagger UI (easiest)

Open: http://localhost:8080/swagger-ui.html

Click the Authorize lock → enter
Username: admin
Password: admin123

Try endpoints (POST/PUT/DELETE will work since you’re ADMIN).

2) cURL (HTTP Basic)

List books

curl -u admin:admin123 "http://localhost:8080/api/v1/books?page=0&size=5&sort=title,asc"

Create a book (use authorId/genreId you see in GET responses)

curl -u admin:admin123 -H "Content-Type: application/json" -X POST \
-d '{
"title": "Domain-Driven Design",
"authorId": 1,
"genreId": 1,
"price": 49.99
}' \
http://localhost:8080/api/v1/books

Update a book

curl -u admin:admin123 -H "Content-Type: application/json" -X PUT \
-d '{
"title": "Domain-Driven Design (Updated)",
"authorId": 1,
"genreId": 1,
"price": 45.00
}' \
http://localhost:8080/api/v1/books/1

Delete a book

curl -u admin:admin123 -X DELETE http://localhost:8080/api/v1/books/1

Search via Elasticsearch

curl -u admin:admin123 "http://localhost:8080/api/v1/search/books?q=clean&page=0&size=10"

3) Raw HTTP header (if you need it)

Authorization: Basic YWRtaW46YWRtaW4xMjM=

(That’s base64("admin:admin123").)

Example:

curl -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" http://localhost:8080/api/v1/books

curl -u admin:admin123 -H "Content-Type: application/json" -X POST \
-d '{"title":"Clean Architecture","authorId":1,"genreId":1,"price":44.50}' \
http://localhost:8080/api/v1/books

mvn -Dflyway.url=jdbc:postgresql://localhost:5432/bookstore \
-Dflyway.user=bookstore \
-Dflyway.password=bookstore \
flyway:repair flyway:migrate

curl -X POST 'http://localhost:8080/api/v1/books' \
-u admin:admin123 \
-H 'Content-Type: application/json' \
-d '{
"title": "Domain-Driven Design",
"authorId": 2,
"genreId": 1,
"price": 55.99
}'

curl -X POST 'http://localhost:8080/api/v1/books' \
-u admin:admin123 \
-H 'Content-Type: application/json' \
-d '{
"title": "Domain-Driven Design",
"authorId": 2,
"genreId": 1,
"price": 55.99,
"caption": "Blue hardcover",
"description": "Evans classic on DDD",
"isbn": "978-0321125217",
"publishedYear": 2003,
"publisher": "Addison-Wesley",
"pageCount": 560,
"language": "en",
"stock": 10,
"coverImageUrl": "https://example.com/ddd.jpg"
}'

http://localhost:8080/actuator/metrics/book.create.count

http://localhost:8080/actuator/metrics/books.search.timer