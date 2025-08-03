package com.smartrent.View.Tenant;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.QueryDocumentSnapshot;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RaiseMaintanance {

    private final dataservice dataService;
    private final String tenantEmail;
    private File selectedFile;
    private String uploadedFileUrl;
    private TextField titleField;
    private TextArea descriptionArea;
    private TextField tenantNameField;
    private TextField tenantEmailField;
    private Label fileStatusLabel;
    private Label statusLabel;
    private Button submitBtn;
    private Button uploadBtn;

    public RaiseMaintanance(String tenantEmail) {
        this.dataService = new dataservice();
        this.tenantEmail = tenantEmail;
    }

    public VBox getView(Runnable onBack) {
        ImageView logoview = new ImageView(new Image("Assets/Images/logo.png"));
        logoview.setPreserveRatio(true);
        logoview.setFitWidth(130);

        Text heading = new Text("Raise a Complaint");
        heading.setFont(new Font("Arial", 24));
        heading.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");

        Text sectionTitle = new Text("Please fill in the form to report a maintenance issue.");
        sectionTitle.setFont(new Font("Arial", 13));
        sectionTitle.setStyle("-fx-text-fill: #374151;");

        tenantNameField = new TextField();
        tenantNameField.setPromptText("Loading name...");
        tenantEmailField = new TextField();
        tenantEmailField.setPromptText("Loading email...");
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

        uploadBtn = new Button("Choose File");
        uploadBtn.setPrefHeight(35);
        uploadBtn.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-font-weight: bold; -fx-font-size: 13px;");

        fileStatusLabel = new Label("No file chosen");
        fileStatusLabel.setPadding(new Insets(0, 0, 0, 10));
        fileStatusLabel.setStyle("-fx-text-fill: #6b7280;");

        HBox fileChooserBox = new HBox(8, uploadBtn, fileStatusLabel);
        fileChooserBox.setAlignment(Pos.CENTER_LEFT);
        uploadBtn.setOnAction(e -> handleFileUpload());

        VBox form = new VBox(20,
                labeledSection("Your Name", tenantNameField),
                labeledSection("Your Email ID", tenantEmailField),
                labeledSection("Title", titleField),
                labeledSection("Description", descriptionArea),
                labeledSection("Attach Image (Optional)", fileChooserBox)
        );
        form.setAlignment(Pos.TOP_LEFT);

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
        submitBtn.setOnAction(e -> handleSubmit());

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
        contentBox.setStyle("-fx-background-color: white;-fx-border-color: #6366F1;-fx-border-width: 2px;");

        VBox root = new VBox(contentBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F3F4F6;");
        root.setPadding(new Insets(15));
        
        loadAndPopulateTenantData();

        return root;
    }

    private void loadAndPopulateTenantData() {
        Task<QueryDocumentSnapshot> loadDataTask = new Task<>() {
            @Override
            protected QueryDocumentSnapshot call() throws Exception {
                List<QueryDocumentSnapshot> profileDocs = dataService.getFlatByTenant(tenantEmail);
                if (profileDocs != null && !profileDocs.isEmpty()) {
                    return profileDocs.get(0);
                }
                return null;
            }
        };
        loadDataTask.setOnSucceeded(e -> {
            QueryDocumentSnapshot doc = loadDataTask.getValue();
            if (doc != null && doc.exists()) {
                Platform.runLater(() -> {
                    tenantNameField.setText(doc.getString("tenantName"));
                    tenantEmailField.setText(tenantEmail);
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
                        String contentType = "image/jpeg";
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

    private void handleSubmit() {
        String title = titleField.getText();
        String description = descriptionArea.getText();

        if (title.isEmpty() || description.isEmpty()) {
            statusLabel.setText("Title and description are required.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                List<QueryDocumentSnapshot> flatDocs = dataService.getFlatByTenant(tenantEmail);
                if (flatDocs.isEmpty()) {
                    throw new Exception("Could not find an assigned flat for this tenant.");
                }
                QueryDocumentSnapshot flatDoc = flatDocs.get(0);
                String ownerId = flatDoc.getString("ownerEmail");
                String flatId = flatDoc.getId();
                String societyName = flatDoc.getString("societyName");
                String flatNo = flatDoc.getString("flatNo");

                if (ownerId == null) {
                    throw new Exception("Could not find owner for this flat.");
                }

                saveMaintenanceRequest(title, description, uploadedFileUrl, ownerId, flatId, societyName, flatNo);
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
            e.getSource().getException().printStackTrace();
        });
        new Thread(saveTask).start();
    }

    private void saveMaintenanceRequest(String title, String description, String imageUrl, String ownerId, String flatId, String societyName, String flatNo) throws Exception {
        Map<String, Object> requestData = new HashMap<>();
        requestData.put("title", title);
        requestData.put("description", description);
        requestData.put("imageUrl", imageUrl);
        requestData.put("status", "Pending");
        requestData.put("timestamp", Timestamp.now());
        requestData.put("tenantId", this.tenantEmail);
        requestData.put("tenantName", tenantNameField.getText());
        requestData.put("ownerId", ownerId);
        requestData.put("flatId", flatId);
        requestData.put("societyName", societyName);
        requestData.put("flatNo", flatNo);

        String documentId = tenantEmail + "_" + System.currentTimeMillis();
        this.dataService.addMaintenanceRequest("MaintenanceRequests", documentId, requestData);
        System.out.println("Maintenance request saved successfully: " + documentId);
    }

    private void clearForm() {
        titleField.clear();
        descriptionArea.clear();
        this.selectedFile = null;
        this.uploadedFileUrl = null;
        fileStatusLabel.setText("No file chosen");
        fileStatusLabel.setStyle("-fx-text-fill: #6b7280;");
    }

    private VBox labeledSection(String labelText, Node field) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 13));
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        return new VBox(4, label, field);
    }
}
