# SmartRent+  

[![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white)](https://www.java.com/)  
[![Firebase](https://img.shields.io/badge/Firebase-ffca28?style=flat&logo=firebase&logoColor=black)](https://firebase.google.com/)  
[![JavaFX](https://img.shields.io/badge/JavaFX-0078D7?style=flat&logo=javafx&logoColor=white)](https://openjfx.io/)  

**SmartRent+** is a desktop-based rental management system built with JavaFX and Firebase.
It provides separate dashboards for **Owners** and **Tenants** to manage properties, track
rent payments, handle maintenance requests, and get AI-powered insights — all in real time.
---

## Table of Contents
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Screenshots](#screenshots)
- [Setup & Installation](#setup--installation)

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

## Project Structure

```
src/main/java/com/smartrent/
│
├── Controller/
│   ├── dataservice.java          → All Firestore CRUD & real-time listeners
│   ├── FireBaseAuth.java         → Firebase Auth via REST API
│   └── ReceiptService.java       → PDF receipt generation (PDFBox)
│
├── Model/
│   ├── Owner/
│   │   ├── Flat.java
│   │   ├── Osignup.java
│   │   ├── OwnerProfile.java
│   │   └── Tenant.java
│   └── Tenant/
│       ├── MaintenanceRequest.java
│       ├── PaymentData.java
│       └── TenantProfileModel.java
│
└── View/
├── Signin.java
├── Signup.java
├── LandingPage.java
├── Owner/
│   ├── OwnerDashboard.java
│   ├── FlatDetails.java
│   ├── EditFlatPage.java
│   ├── TenantManagement.java
│   ├── RentManagement.java
│   ├── MaintananceRequest.java
│   ├── OwnerProfilePage.java
│   ├── AIChatPage.java
│   └── Component/OSidebar.java
└── Tenant/
├── TenantDashboard.java
├── Payment.java
├── RaiseMaintanance.java
├── TenantMaintananceHistory.java
├── TenantPaymentHistory.java
├── TenantProfilePage.java
└── Component/Sidebar.java
```


7. **Screenshots**
   * HomePage
   <img width="1920" height="1200" alt="HomePage" src="https://github.com/user-attachments/assets/622d8714-08c4-4c4d-ab79-b5de14fb1e09" />
   
   
   * Signin
   <img width="1920" height="1200" alt="Sigin" src="https://github.com/user-attachments/assets/1df1f1a3-833a-410a-bcff-dcf4f42ad310" />
   
   
   * Owner Dashboard
     <img width="1919" height="1128" alt="Owner Dashboard" src="https://github.com/user-attachments/assets/30157b0b-b96f-48d4-8d16-bce2b191f46c" />
     

   * Tenant Management
    <img width="1919" height="1126" alt="Tenat-Managemet" src="https://github.com/user-attachments/assets/937ed834-a185-4e2f-8b8e-c2a3cfb2b1ef" />
    

    * Rent History (Owner)
    <img width="1915" height="1123" alt="Rent-History" src="https://github.com/user-attachments/assets/730727db-05ff-4eef-af9e-ac7a8883d60f" />
    

    * Maintenance Management
<img width="1912" height="1120" alt="Maintennace" src="https://github.com/user-attachments/assets/057844aa-5fbf-41a0-8641-a672f2ffb763" />


    ### AI Chat Assistant (Lily)
<img width="1910" height="1124" alt="AI-ChatBot" src="https://github.com/user-attachments/assets/bec4931e-d687-4ff9-a0bb-2ca2a0caa864" />


   * Tenant Dashboard
     <img width="1919" height="1124" alt="Tenant Dashboard" src="https://github.com/user-attachments/assets/cd325dfb-cdb8-4d58-b05a-3fc835110582" />
     

   * Pay Rent
     <img width="1919" height="1121" alt="Payment Page" src="https://github.com/user-attachments/assets/f89d1948-54cd-46ec-8fcf-2edbdc82a00a" />
     

   * Maintenance
     <img width="1919" height="1124" alt="Maintenance" src="https://github.com/user-attachments/assets/5f16248e-bd59-4f20-a300-52246cbe6c4b" />
     

   * Rent Receipt (PDF)
<img width="1245" height="944" alt="Rent-Receipt" src="https://github.com/user-attachments/assets/40363f85-a950-4fd2-9008-b2898bc5d735" />



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
  



