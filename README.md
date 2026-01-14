# SmartRent+  

[![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white)](https://www.java.com/)  
[![Firebase](https://img.shields.io/badge/Firebase-ffca28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/)  
[![JavaFX](https://img.shields.io/badge/JavaFX-0078D7?style=flat&logo=javafx&logoColor=white)](https://openjfx.io/)  

**SmartRent+** is a comprehensive **rental management system** that connects property owners and tenants, streamlines rent management, maintenance requests, and document handling, while integrating AI-powered assistance via Gemini API.

---

## Table of Contents
- [Features](#features)  
- [Tech Stack](#tech-stack)  
- [Screenshots](#screenshots)  

---

## Features

- **Secure Authentication & Role-Based Access**  
  - Firebase Authentication ensures secure login and registration.  
  - Owners and tenants have separate dashboards tailored to their roles.  

- **Dashboard & Rent Management**  
  - Owners can manage multiple properties, track monthly rents, and generate rent receipts.  
  - Tenants can view payment history, download receipts, and submit rent payments.  

- **Maintenance Requests**  
  - Tenants submit maintenance requests with image attachments.  
  - Owners can track request status in real-time.  

- **Document & Image Uploads**  
  - Supports uploading property images, rent receipts, and documents via Firebase Storage.  

- **AI-Powered Chatbot (Gemini API)**  
  - Chatbot allows owners to query property details, tenants, rent status, and maintenance updates.  
  - Provides automated information retrieval for faster decision-making.  

- **Real-Time Data Updates**  
  - Firestore ensures dashboards and requests reflect changes instantly.  

---

## Tech Stack

| Layer           | Technology |
|-----------------|------------|
| Frontend        | JavaFX     |
| Backend/Database| Firebase Firestore |
| Authentication  | Firebase Authentication |
| Storage         | Firebase Storage |
| AI Integration  | Gemini API |

---


## 🚀 Setup & Installation

1. **Clone the repository**
```bash
git clone https://github.com/pratikekande/SMARTRENT.git
cd SMARTRENT
````

2. **Open the project in your IDE**

   * Recommended: **IntelliJ IDEA** or **VS Code** with Java and Maven plugins.

3. **Set up Firebase**

   * Go to [Firebase Console](https://console.firebase.google.com/).
   * Create a new project.
   * Enable **Authentication**, **Firestore Database**, and **Storage**.
   * Generate a service account key → download the `firebase-key.json`.
   * Place it inside:

   ```
   superx/src/main/resources/
   ```

4. **Build the project (Maven)**

```bash
mvn clean install
```

5. **Run the project**

```bash
mvn javafx:run
```

6. **Login / Register**

   * Owners and tenants can register and access their role-based dashboards.
   * Explore rent tracking, maintenance requests, file uploads, and AI chatbot features.

7. **Screenshots**
   * HomePage
   <img width="1920" height="1200" alt="HomePage" src="https://github.com/user-attachments/assets/622d8714-08c4-4c4d-ab79-b5de14fb1e09" />
   
   * Signin
   <img width="1920" height="1200" alt="Sigin" src="https://github.com/user-attachments/assets/1df1f1a3-833a-410a-bcff-dcf4f42ad310" />
   
   * Owner Dashboard
     <img width="1919" height="1128" alt="Owner Dashboard" src="https://github.com/user-attachments/assets/30157b0b-b96f-48d4-8d16-bce2b191f46c" />
     
   * Tenant Dashboard
     <img width="1919" height="1124" alt="Tenant Dashboard" src="https://github.com/user-attachments/assets/cd325dfb-cdb8-4d58-b05a-3fc835110582" />

   * Payment
     <img width="1919" height="1121" alt="Payment Page" src="https://github.com/user-attachments/assets/f89d1948-54cd-46ec-8fcf-2edbdc82a00a" />

   * Maintenance
     <img width="1919" height="1124" alt="Maintenance" src="https://github.com/user-attachments/assets/5f16248e-bd59-4f20-a300-52246cbe6c4b" />

   * Rent
     <img width="1919" height="1123" alt="Rent Management" src="https://github.com/user-attachments/assets/5d84bb2d-7ba1-4eb0-ad16-f6e73825a077" />

     
  



