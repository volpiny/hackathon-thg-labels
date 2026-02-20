# Architecture & Maintainability Guide

This document outlines the design decisions, technical architecture, and maintainability strategies implemented in the THG Label Manager to ensure it is robust, scalable, and easy to maintain.

## 🏗️ System Architecture

The application follows a clean, modular architecture:

-   **Frontend**: Angular 14+ SPA. Uses a component-based architecture with dedicated services for API interaction. Implements a modern glassmorphism UI for premium user experience.
-   **Backend**: Spring Boot 3.x (Java 17). Organized into Controllers, Services, and Repositories.
-   **Storage**: MinIO (S3-compatible) for object storage of PDF labels.
-   **Database**: PostgreSQL for structured metadata (Products, Labels).
-   **Proxy Layer**: Custom Node.js/Angular proxy (`proxy.conf.js`) configured for VPN-safe external API connectivity (MilkyWay, Catalogue Service).

## 🛠️ Key Maintainability Features

### 1. Robust Versioning & Soft Delete
Instead of destructive hard deletes, we implemented a **Soft Delete** mechanism.
-   **Impact**: Preserves audit trails and historical compliance data.
-   **Implementation**: `Label` entity has a `deleted` boolean. `LabelRepository` uses custom queries to ensure only non-deleted labels are served to the UI.
-   **Auto-Recovery**: Deleting an active label automatically triggers the reactivation of the previous non-deleted version, ensuring zero downtime for compliance status.

### 2. Automated Validation Pipeline (OCR)
We integrated **Apache PDFBox** to automate manual label checks.
-   **Smart Logic**: During upload, the system extracts text from the PDF and verifies the SKU against the record metadata.
-   **Benefits**: Reduces human error and provides immediate feedback (✅/❌) to the operator.

### 3. Modular Service Design
-   `FileStorageService`: Abstracted S3-compatible operations. This allows for easy swapping between MinIO (dev) and AWS S3 (prod) with zero changes to business logic.
-   `DashboardService`: Decoupled analytics logic from core CRUD operations, allowing for independent scaling of reporting features.

### 4. Code Quality & Documentation
-   **Javadoc**: Comprehensive documentation for all public APIs and core service methods.
-   **Type Safety**: Strict TypeScript interfaces in the frontend to prevent runtime errors.
-   **Consistent Styling**: Global SCSS variables (CSS Tokens) for theming, ensuring UI consistency across all future components revisited.

## 🚀 Scalability Considerations
-   **Stateless Containers**: The backend is fully stateless, allowing for horizontal scaling behind a load balancer.
-   **Asynchronous Readiness**: The OCR extraction is currently synchronous but is designed to be easily offloaded to a background worker or serverless function if volume increases.

## 📂 Project Structure
-   `/backend`: Spring Boot source code.
-   `/frontend`: Angular application.
-   `/brain`: Architectural documentation and task tracking.
-   `/docker`: Infrastructure-as-code (Docker Compose).
