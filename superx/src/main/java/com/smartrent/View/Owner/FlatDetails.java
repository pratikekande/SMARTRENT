package com.smartrent.View.Owner;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.smartrent.Controller.dataservice;
//import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class FlatDetails {

    // --- Member Variables ---
    private dataservice dataService;
    private String ownerEmail;
    private File selectedFile;
    private String uploadedFileUrl;
    private TextField societyNameField, rentAmountField, tenantNameField, flatNoField, addressField, tenantEmailField;
    private Label fileStatusLabel;
    private Label statusLabel;
    private Button submitBtn;
    private Button uploadBtn;
    private Stage flat; // For compatibility with OwnerDashboard

    /**
     * No-argument constructor for compatibility with other classes.
     */
    public FlatDetails() {
        // This constructor is called from OwnerDashboard
    }

    /**
     * Constructor to inject dependencies.
     */
    public FlatDetails(dataservice dataService, String ownerEmail) {
        this.dataService = dataService;
        this.ownerEmail = ownerEmail;
    }

    /**
     * setFlat method for compatibility.
     */
    public void setFlat(Stage flat) {
        this.flat = flat;
    }

    public Node getView(Runnable onBack) {
        // --- Header Section ---
        ImageView logoview = new ImageView(new Image("Assets/Images/logo.png"));
        logoview.setPreserveRatio(true);
        logoview.setFitWidth(100);

        Text heading = new Text("Flat Details");
        heading.setFont(new Font("Arial", 29));
        heading.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");

        Text sectionTitle = new Text("Flat Information");
        sectionTitle.setFont(new Font("Arial", 18));
        sectionTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        // --- Form Fields ---
        societyNameField = new TextField();
        societyNameField.setPromptText("Enter Society Name");
        flatNoField = new TextField();
        flatNoField.setPromptText("Enter Flat No");
        addressField = new TextField();
        addressField.setPromptText("Enter Full Address");
        tenantNameField = new TextField();
        tenantNameField.setPromptText("Enter Tenant's Name");
        tenantEmailField = new TextField();
        tenantEmailField.setPromptText("Enter Tenant's Email Address");
        rentAmountField = new TextField();
        rentAmountField.setPromptText("Enter Monthly Rent (e.g., 15000)");

        TextField[] fields = {societyNameField, flatNoField, addressField, tenantNameField, tenantEmailField, rentAmountField};
        for (TextField tf : fields) {
            tf.setPrefWidth(400);
            tf.setPrefHeight(40);
            tf.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 14px; -fx-padding: 0 10 0 10;");
        }

        // --- File Uploader Section ---
        uploadBtn = new Button("Choose File");
        uploadBtn.setPrefHeight(40);
        uploadBtn.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-radius: 6px; -fx-font-weight: bold;");

        fileStatusLabel = new Label("No file chosen");
        fileStatusLabel.setPadding(new Insets(0, 0, 0, 10));
        fileStatusLabel.setStyle("-fx-text-fill: #6b7280;");

        HBox fileChooserBox = new HBox(10, uploadBtn, fileStatusLabel);
        fileChooserBox.setAlignment(Pos.CENTER_LEFT);

        uploadBtn.setOnAction(e -> handleFileUpload());

        // --- Form Assembly ---
        VBox form = new VBox(12,
                labeledSection("Society Name", societyNameField),
                labeledSection("Flat No", flatNoField),
                labeledSection("Address", addressField),
                labeledSection("Tenant Name", tenantNameField),
                labeledSection("Tenant Email Address", tenantEmailField),
                labeledSection("Set Rent Amount", rentAmountField),
                labeledSection("Upload Flat Image", fileChooserBox)
        );
        form.setAlignment(Pos.TOP_LEFT);

        // --- Button Section ---
        Button backBtn = new Button("← Back");
        backBtn.setPrefWidth(180);
        backBtn.setPrefHeight(45);
        backBtn.setStyle("-fx-background-color: #d1d5db; -fx-text-fill: #1f2937; -fx-font-weight: bold; -fx-background-radius: 15px;");
        backBtn.setCursor(Cursor.HAND);
        backBtn.setOnAction(e -> onBack.run());

        submitBtn = new Button("Submit");
        submitBtn.setPrefWidth(180);
        submitBtn.setPrefHeight(45);
        submitBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 15px;");
        submitBtn.setCursor(Cursor.HAND);
        statusLabel = new Label();
        statusLabel.setPadding(new Insets(8, 0, 0, 0));

        submitBtn.setOnAction(e -> handleSubmit());

        HBox buttonBox = new HBox(20, backBtn, submitBtn);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));

        // --- Main Content Container ---
        VBox innerContent = new VBox(13, logoview, heading, sectionTitle, form, buttonBox, statusLabel);
        innerContent.setAlignment(Pos.CENTER);
        innerContent.setPadding(new Insets(25));

        ScrollPane scrollPane = new ScrollPane(innerContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        VBox contentBox = new VBox(scrollPane);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setMaxWidth(450);
        // Using sharp corners to prevent layout issues
        contentBox.setStyle(
                "-fx-background-color: white;" +
                "-fx-border-color: #6366F1;" +
                "-fx-border-width: 2px;");

        VBox root = new VBox(contentBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F3F4F6;");
        root.setPadding(new Insets(18, 0, 18, 0));

        return root;
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
                    String objectName = "flats/" + UUID.randomUUID().toString() + "-" + selectedFile.getName();
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
        // --- Validation ---
        if (societyNameField.getText().isEmpty() || flatNoField.getText().isEmpty() || addressField.getText().isEmpty() || tenantNameField.getText().isEmpty() || tenantEmailField.getText().isEmpty() || rentAmountField.getText().isEmpty()) {
            statusLabel.setText("All fields are required.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        int rent;
        try {
            rent = Integer.parseInt(rentAmountField.getText());
        } catch (NumberFormatException nfe) {
            statusLabel.setText("Invalid rent amount. Please enter a number.");
            statusLabel.setTextFill(Color.RED);
            return;
        }

        Task<Void> saveTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                saveFlatDetails(
                        societyNameField.getText(),
                        tenantNameField.getText(),
                        tenantEmailField.getText(),
                        flatNoField.getText(),
                        addressField.getText(),
                        rent,
                        uploadedFileUrl
                );
                return null;
            }
        };

        saveTask.setOnRunning(e -> {
            submitBtn.setDisable(true);
            statusLabel.setText("Saving flat details...");
            statusLabel.setTextFill(Color.ORANGE);
        });

        saveTask.setOnSucceeded(e -> {
            submitBtn.setDisable(false);
            statusLabel.setText("Flat details saved successfully!");
            statusLabel.setTextFill(Color.GREEN);
            clearForm();
        });

        saveTask.setOnFailed(e -> {
            submitBtn.setDisable(false);
            statusLabel.setText("Failed to save flat details. Please try again.");
            statusLabel.setTextFill(Color.RED);
            saveTask.getException().printStackTrace();
        });

        new Thread(saveTask).start();
    }

    private void saveFlatDetails(String societyName, String tenantName, String tenantEmail, String flatNo, String address, int rent, String imageUrl) throws Exception {
        Map<String, Object> flatData = new HashMap<>();
        flatData.put("societyName", societyName);
        flatData.put("flatNo", flatNo);
        flatData.put("address", address);
        flatData.put("tenantName", tenantName);
        flatData.put("tenantEmail", tenantEmail);
        flatData.put("rent", rent);
        flatData.put("imageUrl", imageUrl);
        flatData.put("ownerEmail", this.ownerEmail);

        String documentId = this.ownerEmail + "_" + flatNo;
        dataService.addFlat("flats", documentId, flatData);
        System.out.println("Flat details saved successfully for document: " + documentId);
    }

    private void clearForm() {
        societyNameField.clear();
        flatNoField.clear();
        addressField.clear();
        tenantNameField.clear();
        tenantEmailField.clear();
        rentAmountField.clear();
        fileStatusLabel.setText("No file chosen");
        fileStatusLabel.setStyle("-fx-text-fill: #6b7280;");
        uploadedFileUrl = null;
        selectedFile = null;
    }

    private VBox labeledSection(String labelText, Node field) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 14));
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        return new VBox(5, label, field);
    }
}
