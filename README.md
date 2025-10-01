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

📊 Monitoring & Observability

The Bookstore Service is instrumented with Spring Boot Actuator and Micrometer. This enables developers to inspect runtime metrics, timers, and integrate with distributed tracing systems such as Zipkin.

🔢 Actuator Metrics

Book Creation Counter
Endpoint: http://localhost:8080/actuator/metrics/book.create.count

This metric tracks the number of books created via the API.

Response includes:

The total count of created books since the application started

Optional tags (if configured, e.g., by user/role)

Example response (JSON):

{
"name": "book.create.count",
"measurements": [
{ "statistic": "COUNT", "value": 5.0 }
],
"availableTags": []
}


Book Search Timer
Endpoint: http://localhost:8080/actuator/metrics/books.search.timer

This metric measures the time spent searching for books.

Useful for performance monitoring of Elasticsearch queries or repository lookups.

Response includes:

COUNT: how many times the search endpoint has been called

TOTAL_TIME: cumulative execution time (in seconds)

MAX: maximum time observed for a single search

Example response (JSON):

{
"name": "books.search.timer",
"measurements": [
{ "statistic": "COUNT", "value": 12.0 },
{ "statistic": "TOTAL_TIME", "value": 4.182 },
{ "statistic": "MAX", "value": 0.792 }
],
"availableTags": []
}

🔍 Distributed Tracing with Zipkin

Traces are exported to Zipkin (default URL: http://localhost:9411/zipkin
)

Each API request (e.g., creating a book or performing a search) will generate a trace ID and span IDs.

Architecture Overview
📌 Current Monolithic Architecture

The Bookstore Inventory Management System is currently implemented as a monolithic Spring Boot application. All functionality is contained in a single codebase but logically separated into layers and packages.

🔹 Components

Admin Controller

Provides CRUD operations (create, update, delete, get) for Book, Author, and Genre.

When a book is saved or updated:

Data is persisted into PostgreSQL

A corresponding event is sent to Kafka for asynchronous processing

Kafka Integration

The monolith includes a producer (Admin Service → Kafka)

And a consumer (inside the same app) which listens to Kafka topics

Messages represent book changes (BookCreated, BookUpdated, BookDeleted)

Elasticsearch Synchronization

Kafka consumer updates Elasticsearch indexes with the latest book data

Ensures fast full-text search capability by title, author, genre, price

User Service

Exposes endpoints to search books using Elasticsearch

Example:

GET http://localhost:8080/api/v1/books?page=0&size=10&sort=price,asc&title=Domain-Driven Design


Supports filters (title, author, genre, price) and sorting (by price, title, etc.)

All additional book details (publisher, description, stock, etc.) are loaded from Redis cache for performance

Database → Startup Sync

On application startup, all book records from PostgreSQL are synchronized into Elasticsearch

Ensures search index is always up-to-date, even if Kafka missed events

🔹 Example Flow

Admin adds a new book → record saved to PostgreSQL → event published to Kafka

Kafka consumer processes the event → book indexed into Elasticsearch

User performs a search → query served from Elasticsearch + details loaded from Redis cache

📌 Future Microservices Architecture

As the system grows, it can be refactored into independent microservices for scalability, maintainability, and fault isolation.

🔹 Proposed Services

Admin Service

Handles CRUD operations (DB persistence only)

Publishes events (BookCreated, BookUpdated, etc.) to Kafka

No direct dependency on Elasticsearch

Consumer Service

Dedicated to listening to Kafka events

Responsible for updating Elasticsearch indexes

Ensures that the search layer remains decoupled from write operations

Search Service

Provides search API for end users

Queries Elasticsearch for full-text search and filtering

Enriches results with data from Redis cache

Allows caching frequently requested books for faster response times

🔹 Benefits of Microservices

Scalability → Search service and consumer can scale independently

Resilience → If Elasticsearch is down, CRUD operations (Admin Service) still work

Performance → Redis caching ensures fast reads

Maintainability → Clear separation of responsibilities between services

Extensibility → Easy to add new services (e.g., Recommendation Engine, Analytics Service)

🖼️ Current Data Flow (Monolith)
flowchart TD
A[Admin Controller] -->|Save/Update/Delete| B[(PostgreSQL DB)]
A -->|Send Event| C[(Kafka Topic)]
C --> D[Kafka Consumer]
D -->|Index| E[(Elasticsearch)]
E -->|Search Query| F[User Service]
F -->|Load Details| G[(Redis Cache)]

🖼️ Future Data Flow (Microservices)
flowchart TD
A[Admin Service] -->|Persist| B[(PostgreSQL DB)]
A -->|Publish Event| C[(Kafka Topic)]

    C --> D[Consumer Service]
    D -->|Index Books| E[(Elasticsearch)]

    F[User/Search Service] -->|Query| E
    F -->|Load Details| G[(Redis Cache)]

🔮 Roadmap

Implement monolithic architecture with Admin + User services

Integrate Kafka + Elasticsearch + Redis

Extract Consumer Service into standalone microservice

Extract Search Service into standalone microservice

Deploy using Docker Compose → migrate to Kubernetes for scaling

Add API Gateway + centralized authentication

Add Monitoring dashboards with Prometheus + Grafana