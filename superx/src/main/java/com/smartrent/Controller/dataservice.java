package com.smartrent.Controller;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class dataservice {

    private static Firestore db;
    private static final String BUCKET = "smartrent-7d090.firebasestorage.app";

    /**
     * Initializes Firebase using the correct classpath resource stream method.
     */
    public static void initializefirebase() {
        if (!FirebaseApp.getApps().isEmpty()) {
            db = FirestoreClient.getFirestore();
            return;
        }

        try {
            InputStream serviceAccount = dataservice.class.getClassLoader().getResourceAsStream("firebase-key.json");

            if (serviceAccount == null) {
                System.err.println("CRITICAL ERROR: 'firebase-key.json' not found in resources folder.");
                return;
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket(BUCKET)
                    .build();

            FirebaseApp.initializeApp(options);
            db = FirestoreClient.getFirestore();
            System.out.println("Firebase has been initialized correctly.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // --- User and Profile Data Methods ---

    public void addSignupdata(String collection, String Document, Map<String, Object> data)
            throws InterruptedException, ExecutionException {
        DocumentReference docref = db.collection(collection).document(Document);
        ApiFuture<WriteResult> result = docref.set(data);
        result.get();
        System.out.println("data added");
    }

    public DocumentSnapshot getSignupData(String collection, String Document)
            throws InterruptedException, ExecutionException {
        DocumentReference docref = db.collection(collection).document(Document);
        ApiFuture<DocumentSnapshot> result = docref.get();
        return result.get();
    }

    public void addOwnerdata(String collection, String document, Map<String, Object> data)
            throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection(collection).document(document);
        ApiFuture<WriteResult> result = docRef.set(data);
        result.get(); // Wait for the operation to complete
        System.out.println("Data added to document: " + document);
    }

    public DocumentSnapshot getOwnerData(String collection, String document)
            throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection(collection).document(document);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        return future.get(); // Wait for the operation to complete and return the result
    }

    public void updateOwnerData(String collection, String document, Map<String, Object> data)
            throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection(collection).document(document);
        ApiFuture<WriteResult> future = docRef.update(data);
        future.get(); // Wait for the update to complete
        System.out.println("Data updated for document: " + document);
    }

    public void addTenantData(String collection, String document, Map<String, Object> data)
            throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection(collection).document(document);
        ApiFuture<WriteResult> result = docRef.set(data);
        result.get();
        System.out.println("Tenant data added to document: " + document);
    }

    public DocumentSnapshot getTenantProfileData(String collection, String document)
            throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection(collection).document(document);
        ApiFuture<DocumentSnapshot> future = docRef.get();
        return future.get();
    }

    public void updateTenantData(String collection, String document, Map<String, Object> data)
            throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection(collection).document(document);
        ApiFuture<WriteResult> future = docRef.update(data);
        future.get();
        System.out.println("Tenant data updated for document: " + document);
    }

    // --- Flat Data Methods ---

    public void addFlat(String collection, String document, Map<String, Object> data)
            throws InterruptedException, ExecutionException {
        DocumentReference docref = db.collection(collection).document(document);
        ApiFuture<WriteResult> result = docref.set(data);
        result.get();
        System.out.println("Data added to " + document);
    }

    public List<QueryDocumentSnapshot> getFlatsByOwner(String ownerEmail) throws ExecutionException, InterruptedException {
        Query query = db.collection("flats").whereEqualTo("ownerEmail", ownerEmail);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        return querySnapshot.get().getDocuments();
    }

    public List<QueryDocumentSnapshot> getFlatByTenant(String tenantEmail) throws ExecutionException, InterruptedException {
        Query query = db.collection("flats").whereEqualTo("tenantEmail", tenantEmail).limit(1);
        ApiFuture<QuerySnapshot> querySnapshot = query.get();
        return querySnapshot.get().getDocuments();
    }

    /**
     * Updates a flat document to remove tenant information by setting tenant fields to null.
     * This effectively makes the flat vacant without deleting the property record.
     * @param flatId The document ID of the flat to update.
     * @throws ExecutionException if the database operation fails.
     * @throws InterruptedException if the thread is interrupted while waiting.
     */
    public void removeTenantFromFlat(String flatId) throws ExecutionException, InterruptedException {
        DocumentReference flatRef = db.collection("flats").document(flatId);
        Map<String, Object> updates = new HashMap<>();
        updates.put("tenantName", null);
        updates.put("tenantEmail", null);
        flatRef.update(updates).get(); // .get() waits for the operation to complete
        System.out.println("Tenant removed from flat: " + flatId);
    }

    // --- Payment Data Methods ---

    public void addPayment(String collection, String document, Object data)
            throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection(collection).document(document);
        ApiFuture<WriteResult> result = docRef.set(data);
        result.get();
        System.out.println("Payment data successfully written to " + document);
    }

    public List<QueryDocumentSnapshot> getPaymentData(String collection)
            throws InterruptedException, ExecutionException {
        if (db == null) {
            System.err.println("Firestore database is not initialized.");
            return null;
        }
        ApiFuture<QuerySnapshot> future = db.collection(collection).get();
        return future.get().getDocuments();
    }

    public List<QueryDocumentSnapshot> getPaymentsForTenant(String tenantEmail)
            throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = db.collection("Payments")
                .whereEqualTo("tenantEmail", tenantEmail)
                .get();
        return future.get().getDocuments();
    }

    // --- Maintenance Request Methods ---

    public void addMaintenanceRequest(String collection, String documentId, Object data)
            throws InterruptedException, ExecutionException {
        DocumentReference docRef = db.collection(collection).document(documentId);
        docRef.set(data).get();
    }

    public List<QueryDocumentSnapshot> getMaintenanceRequestsForTenant(String tenantEmail)
            throws InterruptedException, ExecutionException {
        ApiFuture<QuerySnapshot> future = db.collection("MaintenanceRequests")
                .whereEqualTo("tenantEmail", tenantEmail)
                .get();
        return future.get().getDocuments();
    }

    public ApiFuture<WriteResult> updateMaintenanceStatus(String documentId, String newStatus) {
        DocumentReference docRef = db.collection("MaintenanceRequests").document(documentId);
        return docRef.update("status", newStatus);
    }

    /**
     * Retrieves all documents from a collection.
     * If the collection is "MaintenanceRequests", it sorts them by date in descending order.
     */
    public List<QueryDocumentSnapshot> getAllData(String collection) throws InterruptedException, ExecutionException {
        if (db == null) {
            System.err.println("Firestore database is not initialized. Call initializefirebase() first.");
            return null;
        }

        Query query = db.collection(collection);

        if ("MaintenanceRequests".equals(collection)) {
            query = query.orderBy("submittedAt", Query.Direction.DESCENDING);
        }

        ApiFuture<QuerySnapshot> future = query.get();
        return future.get().getDocuments();
    }
}
