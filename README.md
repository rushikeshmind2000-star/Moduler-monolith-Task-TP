# Modular Monolith Enterprise Service

A production-ready **Modular Monolith** application built with **Spring Boot 3 (Java 21)**. This service bundles multiple enterprise capabilities into a clean, cohesive modular architecture, featuring customer and order management, external email validation and reputation scoring, headless HTML-to-image template rendering with Microsoft Playwright, and automated Google Drive Word document processing with Apache POI.

---

## Table of Contents

- [Core Modules](#core-modules)
- [Architecture & Tech Stack](#architecture--tech-stack)
- [Project Directory Structure](#project-directory-structure)
- [Prerequisites](#prerequisites)
- [Configuration & Environment Variables](#configuration--environment-variables)
- [Installation & Getting Started](#installation--getting-started)
- [API Documentation](#api-documentation)
  - [1. Customer Management](#1-customer-management)
  - [2. Order Management](#2-order-management)
  - [3. Email Validation & Reputation](#3-email-validation--reputation)
  - [4. HTML Template Preview & Screenshot](#4-html-template-preview--screenshot)
  - [5. Google Drive Document Automation](#5-google-drive-document-automation)
- [Interactive API Docs (Swagger UI)](#interactive-api-docs-swagger-ui)
- [Testing](#testing)

---

## Core Modules

1. **Customer Management**:
   - Complete CRUD operations for customers with relational mappings to orders.
   - Pre-persistence email verification ensures only valid and deliverable email addresses are registered.

2. **Order Management**:
   - Manages customer purchase orders, pricing, order statuses, and relational querying by customer ID.

3. **Email Validation & Reputation Module**:
   - Integrated with **Abstract API** via non-blocking Spring **WebClient**.
   - Evaluates deliverability, SMTP checks, MX records, disposable email flags, domain age, and fraud risk scores.

4. **HTML Template Preview & Screenshot (Playwright)**:
   - Uses headless **Microsoft Playwright** browser automation to render HTML code into high-fidelity PNG preview images.
   - Supports direct binary image streaming (for instant Postman/browser viewing) as well as file saving with download links.

5. **Google Drive Document Automation (Apache POI)**:
   - Integrates with **Google Drive API v3** using a Service Account.
   - Downloads a master Microsoft Word template (`.docx`), replaces placeholders (`{{customerName}}`, `{{customerAddress}}`, `{{customerEmail}}`, `{{customerPhone}}`) using **Apache POI**, re-uploads the updated file back to Google Drive, and returns the generated document as a downloadable file.
   - Thread-safe concurrency controls prevent race conditions during concurrent document modifications.

6. **Offer Letter Module (OpenPDF & Spring Mail)**:
   - Generates high-fidelity, professional Employment Offer Letter PDFs dynamically populated with candidate details (employee name, email, residential address, compensation package, designation, and joining date).
   - Automatically dispatches a branded congratulatory HTML email to the candidate's email address with the generated PDF attached.
   - Persists all generated offer letters and binary PDF records to MySQL for compliance and audit history.
   - Supports direct binary PDF streaming endpoints for previewing and instant download.

---

## Architecture & Tech Stack

- **Java**: 21 (LTS)
- **Framework**: Spring Boot 3.5.14
- **ORM / Persistence**: Spring Data JPA, Hibernate, MySQL Connector/J
- **Database**: MySQL 8.0+
- **HTTP Client**: Spring WebFlux (`WebClient`)
- **Document Processing**: Apache POI OOXML (`poi-ooxml 5.2.5`)
- **Browser Automation / Headless Rendering**: Microsoft Playwright (`1.56.0`)
- **Cloud Storage**: Google Drive API v3 & Google Auth Library (`google-api-services-drive`)
- **API Documentation**: SpringDoc OpenAPI UI / Swagger 3 (`2.8.9`)
- **Utilities**: Project Lombok, Jakarta Validation (`spring-boot-starter-validation`)
- **Testing**: JUnit 5, Mockito, Spring Boot Test

---

## Project Directory Structure

```text
email-validation/
├── .mvn/                                # Maven wrapper configuration
├── src/
│   ├── main/
│   │   ├── java/com/tech/emailvalidation/
│   │   │   ├── congif/                  # WebClient & global configs
│   │   │   ├── controller/              # Customer, Order, Validation & Template controllers
│   │   │   ├── document/                # Google Drive Document Module
│   │   │   │   ├── config/              # Google Drive client & credentials bean
│   │   │   │   ├── controller/          # Document download/update controller
│   │   │   │   ├── dto/                 # Document-specific DTOs
│   │   │   │   ├── exception/           # Document & Drive custom exception handlers
│   │   │   │   └── service/             # POI replacement & Drive sync services
│   │   │   ├── dto/                     # Shared DTOs, API wrappers, and request/response models
│   │   │   ├── entity/                  # JPA Entities (Customer, Order)
│   │   │   ├── exception/               # Global exception handlers & custom errors
│   │   │   ├── repository/             # Spring Data JPA repositories
│   │   │   └── service/                # Business logic services & implementations
│   │   └── resources/
│   │       ├── application.yml          # Main application properties & external credentials
│   │       ├── credentials/             # Google Service Account JSON (ignored by git)
│   │       └── templates/               # Text and document template files
│   └── test/                            # Unit and integration test suites
├── pom.xml                              # Maven build dependencies
└── README.md
```

---

## Prerequisites

- **Java Development Kit (JDK)**: Version 21 or later
- **Maven**: Version 3.8+ (or use included `./mvnw`)
- **MySQL Server**: Version 8.0 or higher
- **Abstract API Key**: For email reputation and validation
- **Google Cloud Service Account Key**: With Google Drive access enabled

---

## Configuration & Environment Variables

Key settings in `src/main/resources/application.yml` can be overridden using environment variables:

| Setting / Variable | Default Value | Description |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://localhost:3306/email_validation_db...` | MySQL JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | `root` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `root` | Database password |
| `abstract.api.base-url` | `https://emailreputation.abstractapi.com/v1/` | Abstract API endpoint |
| `abstract.api.key` | `YOUR_ABSTRACT_API_KEY` | Abstract API access key |
| `GOOGLE_DRIVE_CREDENTIALS_FILE` | `src/main/resources/credentials/service-account.json` | Path to Google Service Account JSON |
| `GOOGLE_DRIVE_FILE_ID` | `YOUR_GOOGLE_DRIVE_FILE_ID` | File ID of the master `.docx` template in Drive |
| `GOOGLE_DRIVE_APP_NAME` | `CustomerDocumentService` | Application name passed to Google API |

---

## Installation & Getting Started

### 1. Clone Repository
```bash
git clone https://github.com/rushikeshmind2000-star/Moduler-monolith-Task-TP.git
cd Moduler-monolith-Task-TP
```

### 2. Configure Database
Create a MySQL database named `email_validation_db`:
```sql
CREATE DATABASE IF NOT EXISTS email_validation_db;
```

### 3. Add Google Service Account (Optional for Drive Module)
Place your Google Cloud Service Account JSON credentials file at:
```text
src/main/resources/credentials/service-account.json
```
*(Ensure the Service Account email has **Editor** access to the Google Drive template file).*

### 4. Build and Run
```bash
# Using Maven Wrapper (Windows PowerShell)
.\mvnw clean spring-boot:run

# Using standard Maven
mvn clean spring-boot:run
```

The application starts on `http://localhost:8080`.

---

## API Documentation

### 1. Customer Management

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/customers` | Create a customer (validates email via Abstract API) |
| `GET` | `/customers` | Retrieve all customers |
| `GET` | `/customers/{id}` | Retrieve customer by ID |
| `PUT` | `/customers/{id}` | Update customer details |
| `DELETE` | `/customers/{id}` | Delete customer by ID |

#### Create Customer Request Example (`POST /customers`):
```json
{
  "name": "Jane Doe",
  "email": "jane.doe@example.com",
  "phoneNumber": "9876543210",
  "address": "123 Innovation Boulevard, Suite 500"
}
```

---

### 2. Order Management

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/orders` | Create an order linked to a customer |
| `GET` | `/orders` | List all orders (filter with `?customerId={id}`) |
| `GET` | `/orders/{id}` | Get order details by ID |
| `GET` | `/orders/customer/{customerId}` | Retrieve all orders for a specific customer |
| `PUT` | `/orders/{id}` | Update order details or status |
| `DELETE` | `/orders/{id}` | Cancel/delete an order |

#### Create Order Request Example (`POST /orders`):
```json
{
  "customerId": 1,
  "productName": "MacBook Pro M3",
  "quantity": 1,
  "price": 1999.99,
  "status": "PENDING"
}
```

---

### 3. Email Validation & Reputation

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/validation/email?email={email}` | Perform live verification & risk assessment |

#### Sample Response:
```json
{
  "success": true,
  "email": "jane.doe@example.com",
  "deliverability": "DELIVERABLE",
  "isSmtpValid": true,
  "isDisposable": false,
  "riskScore": 0.05
}
```

---

### 4. HTML Template Preview & Screenshot

Renders raw HTML/CSS into a rendered PNG image using Microsoft Playwright.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/templates/preview` | Returns raw PNG image bytes (`image/png`) |
| `POST` | `/api/templates/preview/json` | Saves image and returns metadata & download URL |
| `GET` | `/api/templates/download/{fileName}` | Download saved template screenshot |

#### Render Request Example (`POST /api/templates/preview`):
```json
{
  "templateName": "order-invoice-preview",
  "htmlContent": "<div style='padding: 20px; font-family: Arial; background: #f0f4f8;'><h1>Invoice #1024</h1><p>Thank you for your order, Jane Doe!</p></div>"
}
```

---

### 5. Google Drive Document Automation

Automates placeholder substitution in a cloud-hosted Microsoft Word (`.docx`) file.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/documents/update-and-download` | Updates master Drive template & returns modified `.docx` |

#### Request Body (`CustomerRequest`):
```json
{
  "customerName": "Rahul Sharma",
  "customerAddress": "Pune, Maharashtra, India",
  "customerEmail": "rahul.sharma@example.com",
  "customerPhone": "9876543210"
}
```

#### cURL Command:
```bash
curl -X 'POST' \
  'http://localhost:8080/api/documents/update-and-download' \
  -H 'Content-Type: application/json' \
  -d '{
    "customerName": "Rahul Sharma",
    "customerAddress": "Pune, Maharashtra, India",
    "customerEmail": "rahul.sharma@example.com",
    "customerPhone": "9876543210"
  }' \
  --output CustomerDetails.docx
```

---

### 6. Offer Letter Module

Generates official employment offer letter PDFs dynamically and automatically dispatches a congratulatory email with the PDF attached.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/offer-letter/generate-and-send` | Generates offer letter PDF, attaches to congratulatory email, and saves to DB |
| `POST` | `/api/offer-letter/generate-pdf` | Streams generated PDF directly (binary download / preview) |
| `GET` | `/api/offer-letter/{id}/download` | Re-downloads the PDF for a previously generated offer letter by ID |
| `GET` | `/api/offer-letter/{id}` | Fetches metadata and delivery status for an offer letter by ID |
| `GET` | `/api/offer-letter` | Lists all generated offer letters |

#### Request Body (`OfferLetterRequest`):
Supports both standard and shorthand/alias field names:
```json
{
  "employeeName": "Rushikesh Shinde",
  "email": "rushikesh@example.com",
  "address": "Flat 402, Skyline Towers, Hinjewadi, Pune 411057",
  "packageAmount": "12 LPA",
  "designation": "Senior Java Developer",
  "department": "Engineering",
  "joiningDate": "2026-11-15",
  "sendEmail": true
}
```

*Shorthand / Alternate Aliases:*
```json
{
  "employee_name": "Rushikesh Shinde",
  "mail": "rushikesh@example.com",
  "address": "Flat 402, Skyline Towers, Hinjewadi, Pune 411057",
  "package": "12 LPA"
}
```

#### Response Body:
```json
{
  "id": 1,
  "referenceNumber": "TP-OFFER-2026-4821",
  "employeeName": "Rushikesh Shinde",
  "email": "rushikesh@example.com",
  "address": "Flat 402, Skyline Towers, Hinjewadi, Pune 411057",
  "packageAmount": "12 LPA",
  "designation": "Senior Java Developer",
  "department": "Engineering",
  "joiningDate": "2026-11-15",
  "companyName": "TechPlus Technologies Pvt. Ltd.",
  "status": "SUCCESS",
  "message": "Offer letter generated successfully and dispatched to rushikesh@example.com",
  "emailSent": true,
  "emailDeliveryStatus": "Offer letter email successfully delivered to rushikesh@example.com",
  "generatedAt": "2026-10-26T11:00:00",
  "pdfDownloadUrl": "/api/offer-letter/1/download"
}
```

#### cURL Commands:

**1. Generate Offer Letter & Send Congratulatory Email:**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/offer-letter/generate-and-send' \
  -H 'Content-Type: application/json' \
  -d '{
    "employeeName": "Rushikesh Shinde",
    "email": "candidate@example.com",
    "address": "Flat 402, Skyline Towers, Hinjewadi, Pune 411057",
    "packageAmount": "12 LPA",
    "designation": "Senior Java Developer",
    "department": "Engineering",
    "joiningDate": "2026-11-15"
  }'
```

**2. Direct PDF Stream (Instant Download / Browser Preview):**
```bash
curl -X 'POST' \
  'http://localhost:8080/api/offer-letter/generate-pdf' \
  -H 'Content-Type: application/json' \
  -d '{
    "employeeName": "Rushikesh Shinde",
    "email": "candidate@example.com",
    "address": "Flat 402, Skyline Towers, Hinjewadi, Pune 411057",
    "packageAmount": "12 LPA"
  }' \
  --output Offer_Letter.pdf
```

---

## Interactive API Docs (Swagger UI)

Interactive OpenAPI / Swagger documentation is enabled out-of-the-box:

- **Swagger UI**: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON**: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

---

## Testing

Run unit and integration tests using Maven:
```bash
.\mvnw test
```
The test suite includes validation tests, REST controller tests, and document manipulation tests via `DocumentGenerationServiceTest`.
