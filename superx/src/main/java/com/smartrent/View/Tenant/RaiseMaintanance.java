package com.smartrent.View.Tenant;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.smartrent.Controller.dataservice;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RaiseMaintanance {

    // --- Member Variables ---
    private final dataservice dataService;
    private final String tenantEmail; // NEW: To store the logged-in tenant's email
    private File selectedFile;
    private String uploadedFileUrl; // Stores the URL after immediate upload
    private TextField titleField;
    private TextArea descriptionArea;
    private TextField tenantNameField;
    private TextField tenantEmailField;
    private Label fileStatusLabel;
    private Label statusLabel;
    private Button submitBtn;
    private Button uploadBtn; // To disable during upload

    /**
     * MODIFIED: The constructor now accepts the logged-in tenant's email.
     * @param tenantEmail The email address of the currently logged-in tenant.
     */
    public RaiseMaintanance(String tenantEmail) {
        this.dataService = new dataservice();
        this.tenantEmail = tenantEmail; // Store the email
    }

    /**
     * Creates and returns the entire view for the maintenance request form.
     */
    public VBox getView(Runnable onBack) {
        // --- Header Section ---
        ImageView logoview = new ImageView(new Image("Assets/Images/logo.png"));
        logoview.setPreserveRatio(true);
        logoview.setFitWidth(130);

        Text heading = new Text("Raise a Complaint");
        heading.setFont(new Font("Arial", 24));
        heading.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");

        Text sectionTitle = new Text("Please fill in the form to report a maintenance issue.");
        sectionTitle.setFont(new Font("Arial", 13));
        sectionTitle.setStyle("-fx-text-fill: #374151;");

        // --- Form Fields ---
        tenantNameField = new TextField();
        tenantNameField.setPromptText("Loading name..."); // Will be auto-filled
        tenantEmailField = new TextField();
        tenantEmailField.setPromptText("Loading email..."); // Will be auto-filled
        titleField = new TextField();
        titleField.setPromptText("Enter a brief title (e.g., Leaky Faucet)");
        descriptionArea = new TextArea();
        descriptionArea.setPromptText("Describe the problem in detail...");
        descriptionArea.setWrapText(true);
        descriptionArea.setPrefRowCount(5);

        Node[] fields = {tenantNameField, tenantEmailField, titleField, descriptionArea};
        for (Node field : fields) {
            String style = "-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 13px; -fx-padding: 8px;";
            field.setStyle(style);
        }

        // --- File Uploader Section ---
        uploadBtn = new Button("Choose File");
        uploadBtn.setPrefHeight(35);
        uploadBtn.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-font-weight: bold; -fx-font-size: 13px;");

        fileStatusLabel = new Label("No file chosen");
        fileStatusLabel.setPadding(new Insets(0, 0, 0, 10));
        fileStatusLabel.setStyle("-fx-text-fill: #6b7280;");

        HBox fileChooserBox = new HBox(8, uploadBtn, fileStatusLabel);
        fileChooserBox.setAlignment(Pos.CENTER_LEFT);

        // Action for the "Choose File" button now triggers immediate upload
        uploadBtn.setOnAction(e -> handleFileUpload());

        // --- Form Assembly ---
        VBox form = new VBox(20,
                labeledSection("Your Name", tenantNameField),
                labeledSection("Your Email ID", tenantEmailField),
                labeledSection("Title", titleField),
                labeledSection("Description", descriptionArea),
                labeledSection("Attach Image (Optional)", fileChooserBox)
        );
        form.setAlignment(Pos.TOP_LEFT);

        // --- Button Section ---
        Button backBtn = new Button("← Back");
        backBtn.setPrefWidth(140);
        backBtn.setPrefHeight(36);
        
        backBtn.setStyle("-fx-background-color: #d1d5db; -fx-text-fill: #1f2937; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-cursor: hand; -fx-font-size: 14px;");
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnAction(e -> onBack.run());

        submitBtn = new Button("Submit");
        submitBtn.setPrefWidth(140);
        submitBtn.setPrefHeight(36);
        submitBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-background-radius: 10px; -fx-cursor: hand;");
        submitBtn.setCursor(Cursor.HAND);
        HBox buttonBox = new HBox(15, backBtn, submitBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        statusLabel = new Label();
        statusLabel.setPadding(new Insets(8, 0, 0, 0));

        // --- Submit Button Logic ---
        submitBtn.setOnAction(e -> handleSubmit());

        // --- Main Content Container ---
        VBox innerContent = new VBox(12, logoview, heading, sectionTitle, form, buttonBox, statusLabel);
        innerContent.setAlignment(Pos.CENTER);
        innerContent.setPadding(new Insets(25));
        innerContent.setSpacing(13);

        ScrollPane scrollPane = new ScrollPane(innerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox contentBox = new VBox(scrollPane);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(450);
        contentBox.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #6366F1;" +
                "-fx-border-width: 2px;");

        VBox root = new VBox(contentBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F3F4F6;");
        root.setPadding(new Insets(15));
        
        // NEW: Call the method to fetch and display tenant data
        loadAndPopulateTenantData();

        return root;
    }

    /**
     * NEW: Fetches the logged-in tenant's data and populates the form fields.
     */
    private void loadAndPopulateTenantData() {
        Task<DocumentSnapshot> loadDataTask = new Task<>() {
            @Override
            protected DocumentSnapshot call() throws Exception {
                // This now correctly looks in the "users" collection
                return dataService.getSignupData("users", tenantEmail);
            }
        };

        loadDataTask.setOnSucceeded(e -> {
            DocumentSnapshot doc = loadDataTask.getValue();
            if (doc != null && doc.exists()) {
                Platform.runLater(() -> {
                    // CORRECTED: The field name now matches your database ("firstName")
                    String firstName = doc.getString("firstName");
                    tenantNameField.setText(firstName);
                    tenantEmailField.setText(tenantEmail);

                    // Make the fields non-editable as they are pre-filled
                    tenantNameField.setEditable(false);
                    tenantEmailField.setEditable(false);
                });
            } else {
                Platform.runLater(() -> {
                    tenantNameField.setText("Could not find name");
                    tenantEmailField.setText(tenantEmail);
                    tenantEmailField.setEditable(false);
                });
            }
        });

        loadDataTask.setOnFailed(e -> {
            loadDataTask.getException().printStackTrace();
            Platform.runLater(() -> {
                statusLabel.setText("Error loading tenant data.");
                statusLabel.setTextFill(Color.RED);
            });
        });

        new Thread(loadDataTask).start();
    }

    /**
     * Opens FileChooser and triggers the upload task.
     * (This method is unchanged as per your request).
     */
    private void handleFileUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        this.selectedFile = fileChooser.showOpenDialog(uploadBtn.getScene().getWindow());

        if (this.selectedFile != null) {
            Task<String> uploadTask = new Task<>() {
                @Override
                protected String call() throws Exception {
                    Bucket bucket = StorageClient.getInstance().bucket();
                    String objectName = "maintenance/" + UUID.randomUUID().toString() + "-" + selectedFile.getName();
                    
                    try (InputStream fileStream = new FileInputStream(selectedFile)) {
                        String contentType = "image/jpeg"; // Default
                        try {
                            contentType = selectedFile.toURI().toURL().openConnection().getContentType();
                        } catch (Exception e) {
                            System.err.println("Could not determine content type, defaulting to image/jpeg");
                        }
                        
                        Blob blob = bucket.create(objectName, fileStream, contentType);
                        return blob.signUrl(3650, TimeUnit.DAYS).toString();
                    }
                }
            };

            uploadTask.setOnRunning(e -> {
                uploadBtn.setDisable(true);
                fileStatusLabel.setText("Uploading: " + selectedFile.getName());
                fileStatusLabel.setStyle("-fx-text-fill: #f97316; -fx-font-weight: bold;");
            });

            uploadTask.setOnSucceeded(e -> {
                this.uploadedFileUrl = uploadTask.getValue();
                uploadBtn.setDisable(false);
                fileStatusLabel.setText("Upload successful!");
                fileStatusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            });

            uploadTask.setOnFailed(e -> {
                uploadBtn.setDisable(false);
                fileStatusLabel.setText("Upload failed. Please try again.");
                fileStatusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                uploadTask.getException().printStackTrace();
            });

            new Thread(uploadTask).start();
        }
    }

    /**
     * Handles the form submission logic.
     * (This method is unchanged as per your request).
     */
    private void handleSubmit() {
        String tenantName = tenantNameField.getText();
        String tenantEmail = tenantEmailField.getText();
        String title = titleField.getText();
        String description = descriptionArea.getText();

        if (tenantName.isEmpty() || tenantEmail.isEmpty() || !tenantEmail.contains("@") || title.isEmpty() || description.isEmpty()) {
            statusLabel.setText("All fields are required. Please enter a valid email.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                saveMaintenanceRequest(tenantName, tenantEmail, title, description, uploadedFileUrl);
                return null;
            }
        };

        saveTask.setOnRunning(e -> {
            submitBtn.setDisable(true);
            statusLabel.setText("Submitting complaint...");
            statusLabel.setTextFill(Color.ORANGE);
        });

        saveTask.setOnSucceeded(e -> {
            submitBtn.setDisable(false);
            statusLabel.setText("Complaint submitted successfully!");
            statusLabel.setTextFill(Color.GREEN);
            clearForm();
        });

        saveTask.setOnFailed(e -> {
            submitBtn.setDisable(false);
            statusLabel.setText("An error occurred. Please try again.");
            statusLabel.setTextFill(Color.RED);
            saveTask.getException().printStackTrace();
        });

        new Thread(saveTask).start();
    }

    /**
     * Creates a Map for the request and saves it to Firestore.
     * (This method is unchanged as per your request).
     */
    private void saveMaintenanceRequest(String tenantName, String tenantEmail, String title, String description, String imageUrl) throws Exception {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("tenantName", tenantName);
        requestData.put("tenantEmail", tenantEmail);
        requestData.put("title", title);
        requestData.put("description", description);
        requestData.put("imageUrl", imageUrl);
        requestData.put("status", "Pending");
        requestData.put("submittedAt", Timestamp.now());

        String documentId = tenantEmail + "_" + System.currentTimeMillis();
        this.dataService.addMaintenanceRequest("MaintenanceRequests", documentId, requestData);
        System.out.println("Maintenance request saved successfully: " + documentId);
    }

    /**
     * Clears all form fields and resets status labels.
     */
    private void clearForm() {
        // Name and Email are not cleared as they are auto-filled
        titleField.clear();
        descriptionArea.clear();
        this.selectedFile = null;
        this.uploadedFileUrl = null;
        fileStatusLabel.setText("No file chosen");
        fileStatusLabel.setStyle("-fx-text-fill: #6b7280;");
    }

    /**
     * Helper method to create a labeled section for the form.
     */
    private VBox labeledSection(String labelText, Node field) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 13));
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        return new VBox(4, label, field);
    }
}
