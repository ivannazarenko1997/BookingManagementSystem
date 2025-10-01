Bookstore Service

Bookstore Inventory Management System

🔎 Overview

The Bookstore Inventory Management System is a RESTful web application designed to help bookstore owners efficiently manage their inventory.

It provides a secure API for:

Adding, updating, deleting, and searching books

Role-based authentication & authorization

Search functionality with Elasticsearch

This project was implemented as a contractor-style assignment, simulating a real-world delivery of a first production-ready version for a customer. The focus is on clean design, extensibility, and maintainability.

✨ Key Features

Book CRUD Operations (REST API)

Add new books (title, author, genre, price, etc.)

Update existing book information

Delete books from inventory

Search Functionality

Search by title, author, or genre

Paginated search results

Authentication & Authorization

Basic authentication for staff

Admins → full CRUD access

Users → read-only access

Database Design

PostgreSQL as persistence layer

Entities: Book, Author, Genre with relationships

🛠 Tech Stack

Java + Spring Boot

PostgreSQL (data persistence)

Elasticsearch (search engine)

Flyway (database migrations)

Spring Security (authentication & authorization)

Docker / Docker Compose (containerization)

Spring Actuator & Zipkin (monitoring & tracing)

🚀 Getting Started
1. Start PostgreSQL
   docker compose up -d db

2. Run the Application

Local JVM

mvn spring-boot:run


Containerized Build

mvn -q -DskipTests clean package && docker compose up --build

🔐 Authentication

Available Users:

admin / admin123 → ADMIN role

user / user123 → USER role

📖 API Documentation

Swagger UI → http://localhost:8080/swagger-ui.html

Usage

Open Swagger UI

Click 🔒 Authorize

Enter credentials:

Username: admin
Password: admin123


Try endpoints → POST / PUT / DELETE require ADMIN

📡 API Examples
List Books
curl -u admin:admin123 "http://localhost:8080/api/v1/books?page=0&size=5&sort=title,asc"

Create a Book
curl -u admin:admin123 -H "Content-Type: application/json" -X POST \
-d '{
"title": "Domain-Driven Design",
"authorId": 1,
"genreId": 1,
"price": 49.99
}' \
http://localhost:8080/api/v1/books

Update a Book
curl -u admin:admin123 -H "Content-Type: application/json" -X PUT \
-d '{
"title": "Domain-Driven Design (Updated)",
"authorId": 1,
"genreId": 1,
"price": 45.00
}' \
http://localhost:8080/api/v1/books/1

Delete a Book
curl -u admin:admin123 -X DELETE http://localhost:8080/api/v1/books/1

Search via Elasticsearch
curl -u admin:admin123 "http://localhost:8080/api/v1/search/books?q=clean&page=0&size=10"

Raw HTTP Header Example
Authorization: Basic YWRtaW46YWRtaW4xMjM=


(Base64 of admin:admin123)

curl -H "Authorization: Basic YWRtaW46YWRtaW4xMjM=" http://localhost:8080/api/v1/books

🛠 Database Migrations (Flyway)
mvn -Dflyway.url=jdbc:postgresql://localhost:5432/bookstore \
-Dflyway.user=bookstore \
-Dflyway.password=bookstore \
flyway:repair flyway:migrate

🧪 Advanced Book API Examples
Extended Create
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

Extended Update
curl -X PUT 'http://localhost:8080/api/v1/books/9' \
-u admin:admin123 \
-H 'Content-Type: application/json' \
-d '{
"title": "Domain-Driven Design (Updated)",
"authorId": 2,
"genreId": 1,
"price": 59.99,
"caption": "Blue hardcover, 2nd print",
"description": "Evans classic on DDD — updated notes",
"isbn": "978-0321125217",
"publishedYear": 2003,
"publisher": "Addison-Wesley",
"pageCount": 560,
"language": "en",
"stock": 12,
"coverImageUrl": "https://example.com/ddd-v2.jpg"
}'

📊 Monitoring & Tracing

Actuator Metrics

Book Create Counter

Book Search Timer

Zipkin Tracing

http://localhost:9411/zipkin