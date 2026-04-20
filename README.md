# Fake Store API – REST Assured Test Suite

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk" />
  <img src="https://img.shields.io/badge/REST_Assured-5.5.0-4CAF50?style=flat-square" />
  <img src="https://img.shields.io/badge/JUnit_5-5.11.4-25A162?style=flat-square&logo=junit5" />
  <img src="https://img.shields.io/badge/Allure-2.29.1-FF6B6B?style=flat-square" />
  <img src="https://img.shields.io/badge/Docker-ready-2496ED?style=flat-square&logo=docker" />
</p>

> **Production-ready REST API test automation framework** for the
> [Fake Store API](https://fakestoreapi.com/), built with Java 21, REST Assured 5,
> JUnit 5, Allure Reports, SLF4J + Logback, and Docker.

---

## Table of Contents

- [Project Description](#project-description)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Prerequisites](#prerequisites)
- [Running Tests Locally](#running-tests-locally)
- [Generating and Viewing the Allure Report](#generating-and-viewing-the-allure-report)
- [Running Tests Inside Docker](#running-tests-inside-docker)
- [Viewing the Allure Report from Docker](#viewing-the-allure-report-from-docker)
- [Configuration](#configuration)
- [Test Coverage](#test-coverage)
- [Design Decisions](#design-decisions)

---

## Project Description

This framework provides a **clean, maintainable, and fully containerised** solution
for automated API testing against `https://fakestoreapi.com`.

Key highlights:

| Feature | Detail |
|---|---|
| **Full CRUD coverage** | Products, Carts, Users, and Auth endpoints |
| **Validations** | HTTP status codes, response body fields, headers, JSON Schema |
| **JSON Schema** | 5 schema files covering GET, POST, PUT, DELETE responses |
| **Allure** | `@Epic`, `@Feature`, `@Story`, `@Severity`, `@Step`, `@Description`, environment properties |
| **Logging** | SLF4J + Logback — DEBUG for framework, INFO for root, rolling file appender |
| **Docker** | Multi-stage build: tests in Stage 1, Allure report server in Stage 2 |
| **Config** | Zero hard-coded values — all overridable via env vars or `-D` flags |
| **Timeouts** | Connection and socket timeouts wired from AppConfig into HttpClientConfig |

---

## Technology Stack

| Technology | Version |
|---|---|
| Java | 21 (Temurin) |
| Maven | 3.9+ |
| REST Assured | 5.5.0 |
| JUnit Jupiter | 5.11.4 |
| Allure JUnit 5 | 2.29.1 |
| AssertJ | 3.26.3 |
| Jackson Databind | 2.18.2 |
| SLF4J | 2.0.16 |
| Logback Classic | 1.5.16 |
| AspectJ Weaver | 1.9.22.1 |
| Docker | 24+ |

---

## Project Structure

```
fakestoreapi-restassured-tests/
├── pom.xml                                              ← All dependencies and plugins
├── Dockerfile                                           ← Multi-stage: test + report stages
├── docker-compose.yml                                   ← Orchestrates tests + report services
├── README.md
└── src/
    ├── main/
    │   └── java/com/fakestore/
    │       ├── config/
    │       │   └── AppConfig.java                       ← Centralised runtime configuration
    │       └── utils/
    │           └── JsonUtils.java                       ← Jackson serialisation helpers
    └── test/
        ├── java/com/fakestore/
        │   ├── base/
        │   │   └── BaseTest.java                        ← Shared RequestSpec, timeouts, Allure filter
        │   ├── pojos/
        │   │   ├── Product.java                         ← Product POJO (with nested Rating)
        │   │   ├── Cart.java                            ← Cart POJO (with nested CartProduct)
        │   │   └── User.java                            ← User POJO (with nested Name, Address, Geolocation)
        │   ├── tests/
        │   │   ├── ProductTests.java                    ← 13 tests: CRUD + negative + sort
        │   │   ├── CartTests.java                       ← 7 tests: CRUD + negative
        │   │   ├── UserTests.java                       ← 6 tests: CRUD + negative
        │   │   └── AuthTests.java                       ← 3 tests: login + negative
        │   └── utils/
        │       ├── AllureEnvironmentWriter.java         ← Writes environment.properties for Allure
        │       ├── AssertionUtils.java                  ← @Step-annotated assertion helpers
        │       ├── Endpoints.java                       ← Compile-time endpoint path constants
        │       └── TestDataFactory.java                 ← Centralised test payload factory
        └── resources/
            ├── logback-test.xml                         ← Console + rolling file logging config
            ├── allure.properties                        ← Points Allure to target/allure-results
            ├── allure-environment.properties            ← Template for Allure environment page
            └── schemas/
                ├── products-list-schema.json            ← Schema: GET /products (array)
                ├── product-single-schema.json           ← Schema: GET /products/{id}
                ├── product-create-schema.json           ← Schema: POST /products response
                ├── product-update-schema.json           ← Schema: PUT /products/{id} response
                ├── product-delete-schema.json           ← Schema: DELETE /products/{id} response
                └── categories-schema.json               ← Schema: GET /products/categories
```

---

## Prerequisites

| Requirement | Check command |
|---|---|
| Java 21+ | `java -version` |
| Maven 3.9+ | `mvn -version` |
| Allure CLI (for local report) | `allure --version` |
| Docker 24+ (for containerised run) | `docker --version` |

### Installing Allure CLI (local report only)

**macOS / Linux (Homebrew):**
```bash
brew install allure
```

**Windows (Scoop):**
```powershell
scoop install allure
```

**Manual:** Download from [Allure Releases](https://github.com/allure-framework/allure2/releases)
and add `bin/` to your `PATH`.

---

## Running Tests Locally

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd fakestoreapi-restassured-tests

# 2. Run all tests
mvn clean test

# 3. Override the base URL at runtime (optional)
mvn clean test -Dbase.url=https://fakestoreapi.com

# 4. Run a specific test class only
mvn clean test -Dtest=ProductTests

# 5. Run tests for a specific endpoint
mvn clean test -Dtest=CartTests,UserTests
```

> Logs are written to both the console and `target/logs/fakestore-tests.log`.

---

## Generating and Viewing the Allure Report

### Option A – `allure serve` (interactive, recommended)

Generates the report on the fly and opens it in your default browser:

```bash
allure serve target/allure-results
```

### Option B – Generate static HTML then open manually

```bash
# Generate static HTML report in target/allure-report/
mvn allure:report

# Or using the Allure CLI directly
allure generate target/allure-results --clean -o target/allure-report

# Open the report in your browser
allure open target/allure-report
```

### Option C – Run tests and open report in one command

```bash
mvn clean test && allure serve target/allure-results
```

---

## Running Tests Inside Docker

### Method 1 – docker-compose (recommended)

```bash
# Build images and run tests + report server in one command
docker-compose up --build

# Run tests only (no report server)
docker-compose up --build tests

# Override base URL via environment variable
BASE_URL=https://fakestoreapi.com docker-compose up --build

# Tear down containers and volumes
docker-compose down -v
```

### Method 2 – Plain docker build + docker run

```bash
# Build the test stage image
docker build --target builder -t fakestore-tests .

# Run tests and copy results to host
docker run --rm \
  -v "$(pwd)/target/allure-results:/workspace/target/allure-results" \
  fakestore-tests \
  mvn clean test -B
```

---

## Viewing the Allure Report from Docker

```bash
# Build the full image (includes the report stage)
docker build -t fakestore-report .

# Run the report server (serves on http://localhost:8080)
docker run --rm -p 8080:8080 fakestore-report
```

Then open **http://localhost:8080** in your browser.

With docker-compose, the report service starts automatically after tests finish
and is available at **http://localhost:8080**.

---

## Configuration

All configuration values can be set via **environment variables** or
**Maven `-D` system properties** — no code changes required:

| Key | Env Var | System Property | Default |
|---|---|---|---|
| API Base URL | `BASE_URL` | `base.url` | `https://fakestoreapi.com` |
| Connection timeout (ms) | `CONNECTION_TIMEOUT_MS` | `connection.timeout.ms` | `30000` |
| Socket timeout (ms) | `SOCKET_TIMEOUT_MS` | `socket.timeout.ms` | `30000` |

### Examples

```bash
# Via Maven system property
mvn clean test -Dbase.url=https://my-staging-api.com -Dconnection.timeout.ms=10000

# Via environment variable (Docker / CI)
export BASE_URL=https://my-staging-api.com
mvn clean test
```

---

## Test Coverage

### Products (`/products`) – 13 tests

| # | Test | Method | Endpoint | Validations |
|---|---|---|---|---|
| 1 | Fetch all products | GET | `/products` | 200, non-empty array, fields, schema |
| 2 | Fetch limited list | GET | `/products?limit=5` | 200, exactly 5 items |
| 3 | Fetch single product | GET | `/products/{id}` | 200, correct ID/fields, rating, schema |
| 4 | Fetch all categories | GET | `/products/categories` | 200, exact 4 categories |
| 5 | Fetch by category | GET | `/products/category/electronics` | 200, all items in correct category |
| 6 | Create product | POST | `/products` | 200, echoed fields + new ID, schema |
| 7 | Update product | PUT | `/products/{id}` | 200, echoed updated fields, schema |
| 8 | Delete product | DELETE | `/products/{id}` | 200, echoed product details, schema |
| 9 | Invalid ID (negative) | GET | `/products/999999` | 200, null body |
| 10 | Empty body POST (negative) | POST | `/products` | 200, ID still assigned |
| 11 | Invalid ID PUT (negative) | PUT | `/products/999999` | 200, API is lenient |
| 12 | Invalid ID DELETE (negative) | DELETE | `/products/999999` | 200, API is lenient |
| 13 | Sort descending | GET | `/products?sort=desc` | 200, IDs in descending order |

### Carts (`/carts`) – 7 tests

| # | Test | Method | Endpoint | Validations |
|---|---|---|---|---|
| 1 | Fetch all carts | GET | `/carts` | 200, non-empty array, fields |
| 2 | Fetch single cart | GET | `/carts/{id}` | 200, correct ID/fields, products list |
| 3 | Fetch carts by user | GET | `/carts/user/{userId}` | 200, all carts belong to user |
| 4 | Create cart | POST | `/carts` | 200, echoed fields + new ID |
| 5 | Update cart | PUT | `/carts/{id}` | 200, echoed updated fields |
| 6 | Delete cart | DELETE | `/carts/{id}` | 200, echoed cart details |
| 7 | Invalid ID (negative) | GET | `/carts/999999` | 200, null body |

### Users (`/users`) – 6 tests

| # | Test | Method | Endpoint | Validations |
|---|---|---|---|---|
| 1 | Fetch all users | GET | `/users` | 200, non-empty array, fields |
| 2 | Fetch single user | GET | `/users/{id}` | 200, correct ID/fields, name, address |
| 3 | Create user | POST | `/users` | 200, echoed fields + new ID |
| 4 | Update user | PUT | `/users/{id}` | 200, echoed updated fields |
| 5 | Delete user | DELETE | `/users/{id}` | 200, echoed user details |
| 6 | Invalid ID (negative) | GET | `/users/999999` | 200, null body |

### Auth (`/auth/login`) – 3 tests

| # | Test | Method | Endpoint | Validations |
|---|---|---|---|---|
| 1 | Valid login | POST | `/auth/login` | 200, JWT token present, 3-part format |
| 2 | Invalid credentials (negative) | POST | `/auth/login` | Non-200 status |
| 3 | Missing fields (negative) | POST | `/auth/login` | Non-200 status |

**Total: 29 tests**

---

## Design Decisions

| Decision | Rationale |
|---|---|
| `RequestSpecBuilder` | DRY — base URL, content type, timeouts, and filters defined once in BaseTest |
| `ResponseSpecBuilder` | Shared 15-second response-time guard; tests extend with specific matchers |
| `HttpClientConfig` timeouts | Connection and socket timeouts wired from AppConfig — no hard-coded values |
| `AllureRestAssured` filter | Auto-attaches every HTTP request/response to Allure without per-test boilerplate |
| `@BeforeAll` in `BaseTest` | Initialised once per JVM, not before every single test method |
| `AllureEnvironmentWriter` | Writes environment.properties at runtime so the Allure overview page shows live config |
| `TestDataFactory` | All payloads in one class — changing a field doesn't require hunting through tests |
| `Endpoints` constants | Compiler catches typos in endpoint paths; single point of change for URL updates |
| `@Step` on utilities | Every assertion and payload build appears as a named step in the Allure timeline |
| Multi-stage `Dockerfile` | Builder stage caches Maven deps; report stage is a lean JRE image |
| Named volumes in compose | Maven cache persists across container restarts; allure-results shared between services |
| `\|\| true` in Dockerfile RUN | Ensures Docker doesn't abort the build on test failures so results are always collected |
