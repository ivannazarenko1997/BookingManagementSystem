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
1. Start PostgreSQL,Kafka, Redis, Elascticsearch.
    You can find yml file in docker-compose folder
   docker compose up -d db

2. Run the Application

Local JVM

mvn spring-boot:run


Containerized Build

mvn -q -DskipTests clean package && docker compose up --build

Steps on Windows

Open Command Prompt:

Press Win + R, type cmd, then hit Enter.

Navigate to the target folder (optional, but makes the command shorter):

cd C:\sendai\booking-management-system-v2\target


Run the JAR file:

java -jar bookstore-inventory-0.1.0.jar


Or, if you want to run it directly with the full path:

java -jar C:\sendai\booking-management-system-v2\target\bookstore-inventory-0.1.0.jar

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


Future Architecture (System Design)

The monolith evolves into three independently scalable services with a message-driven backbone and read-optimized search.

1) High-Level Service Topology
   flowchart LR
   subgraph Client
   U[Users / Admins]
   end

   U -->|HTTPS/JSON| G[API Gateway / Ingress]

   subgraph ControlPlane[Observability & Ops]
   ZP[Zipkin/Tempo]
   PR[Prometheus]
   GF[Grafana]
   LG[Log Aggregation]
   end

   subgraph DataPlane[Core Platform]
   subgraph Writes
   AS[Admin Service]
   PG[(PostgreSQL)]
   K[(Kafka)]
   CS[Consumer Service]
   ES[(Elasticsearch)]
   end

   subgraph Reads
   SS[Search Service]
   R[(Redis Cache)]
   end
   end

   %% Requests
   G --> AS
   AS --> PG
   AS -->|Book events| K
   K --> CS
   CS -->|Index| ES

   %% Search path
   G --> SS
   SS --> ES
   SS <--> R

   %% Telemetry
   AS --> ZP
   SS --> ZP
   CS --> ZP
   AS --> PR
   SS --> PR
   CS --> PR
   AS --> LG
   SS --> LG
   CS --> LG


Key ideas

Admin Service owns writes to PostgreSQL and publishes domain events to Kafka.

Consumer Service materializes search views in Elasticsearch from Kafka events.

Search Service serves read queries from Elasticsearch and enriches / caches hot data in Redis.

API Gateway centralizes routing, rate limiting, and authN/Z (can be Spring Cloud Gateway, NGINX, Traefik, or Kong).

2) Core Request Flows (Sequence)
   2.1 Book Create/Update (Write Path)
   sequenceDiagram
   actor Admin
   participant GW as API Gateway
   participant AS as Admin Service
   participant PG as PostgreSQL
   participant K as Kafka
   participant CS as Consumer Service
   participant ES as Elasticsearch

   Admin->>GW: POST /api/admin/books
   GW->>AS: Forward
   AS->>PG: upsert(Book)
   PG-->>AS: ok (id, version)
   AS->>K: publish(BookCreated/Updated)
   K-->>CS: event(Book...)
   CS->>ES: index(bookDocument)
   ES-->>CS: ack
   AS-->>GW: 201 Created (bookId)
   GW-->>Admin: 201 Created

2.2 Search (Read Path)
sequenceDiagram
actor User
participant GW as API Gateway
participant SS as Search Service
participant ES as Elasticsearch
participant R as Redis

    User->>GW: GET /api/search/books?title=...&sort=price,asc
    GW->>SS: Forward
    SS->>ES: query(title/author/genre/price + pagination/sort)
    ES-->>SS: hits(ids + lightweight fields)
    SS->>R: MGET(details by ids)
    R-->>SS: cached subset
    SS->>AS: (optional) fallback to DB for missing enrich fields (if needed)
    SS-->>GW: 200 OK (results)
    GW-->>User: 200 OK

3) Data Ownership & Contracts

PostgreSQL is the system of record (normalized entities: Book, Author, Genre).

Elasticsearch stores denormalized documents optimized for search & sorting.

Kafka carries immutable domain events (BookCreated, BookUpdated, BookDeleted)—the contract between write and read sides.

Redis caches search results and/or book details by ID (TTL + cache invalidation on events).

4) Deployment Sketch (Kubernetes-Friendly)
   flowchart TB
   subgraph Edge
   LB[External Load Balancer]
   end

   subgraph K8sCluster[Kubernetes]
   IG[Ingress / API Gateway]
   AS[Admin Service (Deployment)]
   SS[Search Service (Deployment)]
   CS[Consumer Service (Deployment)]

   subgraph DataStores
   PG[(PostgreSQL StatefulSet)]
   ES[(Elasticsearch StatefulSet / Cluster)]
   K[(Kafka StatefulSet)]
   R[(Redis StatefulSet)]
   end

   subgraph Observability
   PR[Prometheus]
   GF[Grafana]
   ZP[Zipkin/Tempo]
   LG[Log Collector (Fluent Bit/Vector)]
   end
   end

   LB --> IG
   IG --> AS
   IG --> SS

   AS --> PG
   AS --> K
   CS --> K
   CS --> ES

   SS --> ES
   SS --> R

   AS --> PR
   SS --> PR
   CS --> PR
   AS --> ZP
   SS --> ZP
   CS --> ZP
   AS --> LG
   SS --> LG
   CS --> LG

5) Scaling & Reliability

Scale reads by horizontally scaling Search Service and Elasticsearch.

Scale writes by scaling Admin Service; Kafka absorbs bursts with backpressure.

Eventual consistency between PostgreSQL and Elasticsearch; consumers retry with idempotent indexing (use doc _id = book id).

Resilience

Outages in Elasticsearch do not block writes.

Dead-letter topic for poison events.

Circuit breakers & timeouts around ES/Redis.

6) Caching Strategy

Redis

BOOK:{id} → book details for result enrichment

SEARCH:{normalizedQueryHash} → paginated results (short TTL)

Invalidation

On BookUpdated/Deleted, publish cache invalidation messages (or use cache-aside + versioning).

7) Contracts & Schema (event-driven)

BookEvent (example)

{
"eventType": "BOOK_UPDATED",
"occurredAt": "2025-10-01T09:00:00Z",
"payload": {
"id": 9,
"title": "Domain-Driven Design",
"authorId": 2,
"authorName": "Eric Evans",
"genreId": 1,
"genreName": "Software",
"price": 59.99,
"isbn": "978-0321125217",
"stock": 12,
"publisher": "Addison-Wesley",
"publishedYear": 2003,
"language": "en"
},
"version": 3
}


Use schema registry (e.g., Confluent/Redpanda) to evolve fields safely.

Consumers are forward/backward compatible.

8) Security & Access

API Gateway enforces AuthN/Z (e.g., Basic now → JWT/OAuth2 later).

Admin Service: ADMIN-only routes to mutate data.

Search Service: public or authenticated read (rate-limited).

Network policies to restrict direct access to datastores.

9) Migration Path from Monolith

Extract Consumer first (lowest coupling) → point to monolith’s Kafka.

Extract Search API next → monolith delegates search requests to Search Service.

Finally, extract Admin (CRUD) → monolith becomes a thin façade or is retired.

Introduce API Gateway once services are separated.


Cloud Deployment & Migration (AWS Ready)

The Bookstore Inventory Management System can be deployed both on-premises (Docker/Kubernetes with PostgreSQL, Kafka, Elasticsearch, Redis) and in the cloud.
When targeting AWS, the architecture can be adapted to leverage managed services for scalability, resilience, and reduced ops overhead.

🔄 Service Mapping: On-Prem → AWS
Current Component	AWS Replacement	Benefit
PostgreSQL (RDS/Postgres container)	Amazon RDS (PostgreSQL)	Managed DB with backups, HA, auto-scaling storage
Kafka	Amazon SQS (or Amazon MSK if strict Kafka needed)	Event bus decoupling services; SQS is simpler & serverless; MSK provides Kafka compatibility
Elasticsearch	Amazon OpenSearch Service (AWS ELK)	Fully managed Elasticsearch/OpenSearch cluster; integrates with Kibana
Redis Cache	Amazon ElastiCache (Redis)	Managed Redis cluster for caching & session storage
Spring Security (Basic Auth)	Amazon Cognito	Federated identity, user pool management, JWT-based authentication & authorization
Zipkin/Prometheus/Grafana	AWS X-Ray + CloudWatch + Managed Grafana	Distributed tracing & monitoring out of the box
🔹 Example Cloud Architecture
flowchart TB
subgraph Client
U[User / Admin]
end

    subgraph AWS
      AGW[API Gateway]
      COG[Amazon Cognito]
      AS[Admin Service (ECS/EKS)]
      SS[Search Service (ECS/EKS)]
      CS[Consumer Service (ECS/EKS)]
      RDS[(Amazon RDS Postgres)]
      SQS[(Amazon SQS Queue)]
      OS[(Amazon OpenSearch Service)]
      REDIS[(Amazon ElastiCache Redis)]
      XR[AWS X-Ray/CloudWatch]
    end

    U -->|HTTPS| AGW
    AGW --> COG
    AGW --> AS
    AGW --> SS

    AS --> RDS
    AS --> SQS
    CS --> SQS
    CS --> OS
    SS --> OS
    SS --> REDIS

    AS --> XR
    SS --> XR
    CS --> XR

🔐 Authentication & Authorization (Cognito)

Replace Basic Auth with JWT tokens issued by Amazon Cognito.

Benefits:

Supports user pools (staff, admins)

MFA, SSO, and social login (Google, Facebook, etc.)

Fine-grained role mapping (Admin → full CRUD, User → read-only)

📡 Messaging (Kafka → SQS)

Replace Kafka event publishing with Amazon SQS queues.

Admin Service sends messages (BookCreated, BookUpdated, BookDeleted) to an SQS queue.

Consumer Service reads from the queue and updates Amazon OpenSearch.

If you require Kafka-specific features (ordered partitions, streams), you can migrate to Amazon MSK (Managed Kafka) instead.

🔍 Search (Elasticsearch → OpenSearch)

Replace Elasticsearch cluster with Amazon OpenSearch Service (ELK stack).

Benefits:

No cluster management (AWS manages scaling, snapshots, patching)

Direct integration with CloudWatch for monitoring

Kibana dashboards for log and search analytics

⚡ Caching (Redis → ElastiCache)

Replace self-hosted Redis with Amazon ElastiCache for Redis.

Benefits:

Managed replication & failover

Auto-scaling

Encryption in-transit and at-rest

📊 Observability

Replace Zipkin + Prometheus + Grafana with:

AWS X-Ray → distributed tracing

Amazon CloudWatch → metrics, logs, alarms

Amazon Managed Grafana → dashboards


Screens of work results added to following directory  
https://github.com/ivannazarenko1997/BookingManagementSystem/tree/main/docs/screens

 
 
MONOLIT DESIGN
![Monolit_Design Screen](docs/screens/monolit_design.png)


MICROSERVISE SYSTEM DESIGN
![Microservice_Design Screen](docs/screens/future_microservice_system_design_project.png)


AWS SYSTEM DESIGN
![Microservice_Design Screen](docs/screens/future_aws_system_design.png)

GRUD OPERATIONS
![Create_Book Screen](docs/screens/createBookPostman.png)
![Update_Book Screen](docs/screens/book_update_postman.png)
![Get_Book Screen](docs/screens/getBookPostman.png)
![Delete_Book Screen](docs/screens/deleteBookPostman.png)
![Find_Filter_sort_Book Screen](docs/screens/find_filter_sort_BookPostman.png)

DATABASE_TABLES
![DB_table_authors](docs/screens/database_table_authors_postgres_ui.png)
![DB_table_books](docs/screens/database_table_books_postgres_ui.png)
![DB_table_genres](docs/screens/database_table_genres_postgres_ui.png)
![DB_tables_all](docs/screens/database_tables_postgres_ui.png)

ZIPKIN
![Actuator_Design Screen](docs/screens/zipkin_trace_history.png)
 
ACTUATOR
![Actuator_Design Screen](docs/screens/book_create_count_actuator.png)
![Actuator_Design Screen](docs/screens/book_delete_time_actuator.png)
![Actuator_Design Screen](docs/screens/book_search_time_actuator.png)
![Actuator_Design Screen](docs/screens/book_update_time_actuator.png)
 
KAFKA
![kafka Screen](docs/screens/kafka_ui.png)
![kafka Screen](docs/screens/kafka_ui_events.png)


ELASTICTICSEARCH
![ElasticSearch Screen](docs/screens/elasticsearch_data.png)
![ElasticSearch Screen](docs/screens/elasticsearch_cluster.png)
![ElasticSearch Screen](docs/screens/elasticsearch_cluster_info.png)
![ElasticSearch Screen](docs/screens/elasticsearch_index.png)


TESTS
![Tests Screen](docs/screens/tests_green.png)