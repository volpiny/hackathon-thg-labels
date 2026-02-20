# 🏷️ THG Label Manager

A robust solution for managing product labels with full version history, master/child product relationships, and secure external API integration. Built for the THG Hackathon.

## ✅ Hackathon Requirements Checklist

### Functional Requirements
- [x] **Product Catalogue Integration**: Linked reference data (title, image) directly from Catalogue service.
- [x] **Catalogue Discovery**: Search by SKU, Barcode, and Title to bridge Catalogue -> Label Manager.
- [x] **Product Management**: Manual creation and local overrides for categories/types/territories.
- [x] **Rich Metadata**: Manage Category (Food/Supp), Type (Solid/Liquid/Powder), and Markets (EU/AU/IN).
- [x] **Product Detail View**: Displays core identities, high-res images, and hierarchy status.
- [x] **Master/Child Hierarchy**: Master products display child links; Labels restricted to child products only.
- [x] **Label Lifecycle**: Secure upload/preview/delete with active versioning logic.
- [x] **Audit Trail (WWW)**: Full tracking of Who, What, When for every label change.
- [x] **Bulk Operations**: High-performance ZIP generation for batch label downloads (100s of MBs).
- [x] **Production Operations Mode**: Fast filtering to show only products with active, verified labels.

### ⭐ Standard & Winning Enhancements
- [x] **Smart OCR Validation**: Automated SKU verification inside uploaded PDF labels.
- [x] **Operational Dashboard**: Real-time analytics on readiness levels and product distribution.
- [x] **Interactive Preview**: In-app PDF viewer with glassmorphism styling.
- [x] **Audit Timeline**: Sequential visual feed of all historical label movements.
- [x] **Premium UX**: Liquid grid search, animated toast notifications, and custom-styled form controls.

## 🛠️ Technology Stack
- **Backend**: Spring Boot 3.4, Java 17, JPA/Hibernate.
- **Frontend**: Angular 13, Vanilla CSS, Reactive Forms.
- **Infrastructure**: PostgreSQL 13, MinIO (S3-compatible), Docker Compose.
- **Security**: Custom SSL Truststore for VPN connectivity.

## 📖 Setup & Verification

### 1. Start Infrastructure & Backend
Run the backend and databases using Docker Compose from the root:
```bash
docker-compose up --build -d
```

### 2. Start Frontend UI
Install dependencies and start the Angular development server from the frontend directory:
```bash
cd frontend/label-manager-ui
npm install
npm run start
```

### 3. Access the Application
- **UI**: [http://localhost:4200](http://localhost:4200)
- **Backend API**: [http://localhost:8080/api](http://localhost:8080/api)
- **S3 Console (MinIO)**: [http://localhost:9001](http://localhost:9001) (Credentials: `minioadmin` / `minioadmin`)

Check the [ARCH-AND-MAINTAINABILITY.md](file:///Users/volpiny/Desktop/thg-label-manager/ARCH-AND-MAINTAINABILITY.md) for architectural details and codebase design.
