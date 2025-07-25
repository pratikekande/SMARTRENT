package com.smartrent.View.Owner;

import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Owner.Flat;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.util.HashMap;
import java.util.Map;

public class EditFlatPage {

    // Member Variables
    private dataservice dataService;
    private String ownerEmail;
    private Flat flatToEdit; // The existing flat object to be edited
    private TextField societyNameField, rentAmountField, tenantNameField, flatNoField, addressField, tenantEmailField;
    private Label statusLabel;
    private Button submitBtn;

    /**
     * Constructor for editing an existing flat.
     * @param dataService The service to interact with the database.
     * @param ownerEmail The email of the owner.
     * @param flatToEdit The flat object containing the data to be edited.
     */
    public EditFlatPage(dataservice dataService, String ownerEmail, Flat flatToEdit) {
        this.dataService = dataService;
        this.ownerEmail = ownerEmail;
        this.flatToEdit = flatToEdit;
    }

    public Node getView(Runnable onBack) {
        // --- Header Section ---
        ImageView logoview = new ImageView(new Image("Assets/Images/logo.png"));
        logoview.setPreserveRatio(true);
        logoview.setFitWidth(100);

        Text heading = new Text("Edit Flat Details"); // Changed Title
        heading.setFont(new Font("Arial", 29));
        heading.setStyle("-fx-font-weight: bold; -fx-text-fill: #111827;");

        Text sectionTitle = new Text("Update Flat Information"); // Changed Subtitle
        sectionTitle.setFont(new Font("Arial", 18));
        sectionTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        // --- Form Fields ---
        societyNameField = new TextField();
        flatNoField = new TextField();
        addressField = new TextField();
        tenantNameField = new TextField();
        tenantEmailField = new TextField();
        rentAmountField = new TextField();

        TextField[] fields = {societyNameField, flatNoField, addressField, tenantNameField, tenantEmailField, rentAmountField};
        for (TextField tf : fields) {
            tf.setPrefWidth(400);
            tf.setPrefHeight(40);
            tf.setStyle("-fx-background-color: #f9fafb; -fx-border-color: #e5e7eb; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 14px; -fx-padding: 0 10 0 10;");
        }
        
        // --- Form Assembly ---
        VBox form = new VBox(12,
                labeledSection("Society Name", societyNameField),
                labeledSection("Flat No", flatNoField),
                labeledSection("Address", addressField),
                labeledSection("Tenant Name", tenantNameField),
                labeledSection("Tenant Email Address", tenantEmailField),
                labeledSection("Set Rent Amount", rentAmountField)
        );
        form.setAlignment(Pos.TOP_LEFT);
        
        // Pre-fill the form with existing data
        populateForm();

        // --- Button Section ---
        Button backBtn = new Button("← Back");
        backBtn.setPrefWidth(180);
        backBtn.setPrefHeight(45);
        backBtn.setStyle("-fx-background-color: #d1d5db; -fx-text-fill: #1f2937; -fx-font-weight: bold; -fx-background-radius: 15px;");
        backBtn.setOnAction(e -> onBack.run());

        submitBtn = new Button("Update Details"); // Changed Button Text
        submitBtn.setPrefWidth(180);
        submitBtn.setPrefHeight(45);
        submitBtn.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-background-radius: 15px;");
        
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
        contentBox.setStyle("-fx-background-color: white; -fx-border-color: #6366F1; -fx-border-width: 2px;");

        VBox root = new VBox(contentBox);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F3F4F6;");
        root.setPadding(new Insets(18, 0, 18, 0));

        return root;
    }

    /**
     * Populates the form fields with data from the flatToEdit object.
     */
    private void populateForm() {
        societyNameField.setText(flatToEdit.getSocietyName());
        flatNoField.setText(flatToEdit.getFlatNo());
        addressField.setText(flatToEdit.getAddress());
        tenantNameField.setText(flatToEdit.getTenantName() != null ? flatToEdit.getTenantName() : "");
        tenantEmailField.setText(flatToEdit.getTenantEmail() != null ? flatToEdit.getTenantEmail() : "");
        rentAmountField.setText(String.valueOf(flatToEdit.getRent()));
    }

    private void handleSubmit() {
        // --- Validation ---
        if (societyNameField.getText().isEmpty() || flatNoField.getText().isEmpty() || addressField.getText().isEmpty() || rentAmountField.getText().isEmpty()) {
            statusLabel.setText("Society, Flat No, Address, and Rent are required.");
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

        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Prepare data map for update
                Map<String, Object> flatData = new HashMap<>();
                flatData.put("societyName", societyNameField.getText());
                flatData.put("flatNo", flatNoField.getText());
                flatData.put("address", addressField.getText());
                flatData.put("tenantName", tenantNameField.getText().isEmpty() ? null : tenantNameField.getText());
                flatData.put("tenantEmail", tenantEmailField.getText().isEmpty() ? null : tenantEmailField.getText());
                flatData.put("rent", rent);
                flatData.put("ownerEmail", ownerEmail);
                // Note: We are not updating the image URL in this form.

                // Call the update service method
                dataService.updateFlat(flatToEdit.getFlatId(), flatData);
                return null;
            }
        };

        updateTask.setOnRunning(e -> {
            submitBtn.setDisable(true);
            statusLabel.setText("Updating flat details...");
            statusLabel.setTextFill(Color.ORANGE);
        });

        updateTask.setOnSucceeded(e -> {
            submitBtn.setDisable(false);
            statusLabel.setText("Flat details updated successfully!");
            statusLabel.setTextFill(Color.GREEN);
        });

        updateTask.setOnFailed(e -> {
            submitBtn.setDisable(false);
            statusLabel.setText("Failed to update flat details. Please try again.");
            statusLabel.setTextFill(Color.RED);
            updateTask.getException().printStackTrace();
        });

        new Thread(updateTask).start();
    }

    private VBox labeledSection(String labelText, Node field) {
        Label label = new Label(labelText);
        label.setFont(Font.font("Arial", 14));
        label.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        return new VBox(5, label, field);
    }
}
