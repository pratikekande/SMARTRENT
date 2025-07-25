package com.smartrent.View.Tenant;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Tenant.PaymentData;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

public class Payment {

    private VBox dynamicFieldsContainer;
    private TextField amountField;
    private DatePicker datePicker;
    private TextField upiIdField;
    private TextField tenantNameField;
    private TextField tenantEmailField;
    private Label statusLabel; // Made a member variable to be accessible in the load method

    // NEW: Member variables to store the dataservice and tenant's email
    private final dataservice dataService;
    private final String tenantEmail;

    /**
     * MODIFIED: The constructor now accepts the logged-in tenant's email.
     * @param tenantEmail The email address of the currently logged-in tenant.
     */
    public Payment(String tenantEmail) {
        this.dataService = new dataservice();
        this.tenantEmail = tenantEmail;
    }

    public VBox getView(Runnable onBack) {

        // --- Title ---
        Label title = new Label("Pay Rent");
        title.setFont(Font.font("Arial", 28));
        title.setStyle("-fx-font-weight: bold;");
        HBox titleBox = new HBox(title);
        titleBox.setPadding(new Insets(20, 40, 0, 40));

        // --- Payment Form ---
        Label paymentLabel = new Label("Select Payment Method:");
        ComboBox<String> paymentMethod = new ComboBox<>();
        paymentMethod.getItems().addAll("Credit/Debit Card", "UPI", "Net Banking");
        paymentMethod.setValue("Credit/Debit Card");

        // NEW: Initialize the common fields here, just once.
        amountField = new TextField();
        amountField.setPromptText("Add Rent Amount");

        datePicker = new DatePicker();
        datePicker.setPromptText("Date");
        datePicker.setValue(LocalDate.now());

        tenantNameField = new TextField();
        tenantNameField.setPromptText("Enter Tenant Name");

        tenantEmailField = new TextField();
        tenantEmailField.setPromptText("Enter Tenant Email ID");

        dynamicFieldsContainer = new VBox(10);
        updatePaymentFields("Credit/Debit Card");
        paymentMethod.valueProperty().addListener((obs, oldVal, newVal) -> updatePaymentFields(newVal));

        TextArea notes = new TextArea();
        notes.setPromptText("Add a note to the landlord (optional)");
        notes.setPrefRowCount(2);

        CheckBox terms = new CheckBox("I agree to the Terms and Conditions");
        CheckBox defaultPay = new CheckBox("Use as default payment method");
        CheckBox autoPay = new CheckBox("Enable AutoPay monthly");

        Button payButton = new Button("Pay Rent Now");
        payButton.setPrefHeight(45);
        payButton.setMaxWidth(Double.MAX_VALUE);
        payButton.setStyle("-fx-background-color: #6A42E4; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 10;");
        payButton.setCursor(Cursor.HAND);
        statusLabel = new Label();
        statusLabel.setPadding(new Insets(8, 0, 0, 0));

        VBox rightBox = new VBox(15,
                paymentLabel, paymentMethod, dynamicFieldsContainer,
                notes, terms, defaultPay, autoPay, payButton, statusLabel);
        rightBox.setPrefWidth(600);

        HBox mainContent = new HBox(40, rightBox);
        mainContent.setPadding(new Insets(20, 40, 20, 40));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // --- Back Button ---
        Button backButton = new Button("← Back to Dashboard");
        backButton.setFont(Font.font("Arial", 14));
        backButton.setStyle("-fx-background-color: #EAEAEA; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        backButton.setPrefHeight(40);
        backButton.setOnAction(e -> onBack.run());

        HBox buttonContainer = new HBox(backButton);
        buttonContainer.setPadding(new Insets(0, 40, 20, 40));
        buttonContainer.setAlignment(Pos.CENTER_LEFT);

        // --- Main Layout ---
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(titleBox);
        contentPane.setCenter(mainContent);
        contentPane.setBottom(buttonContainer);

        VBox root = new VBox(contentPane);
        root.setStyle("-fx-background-color: white;");
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        // --- Logic for the "Pay Rent Now" button (Unchanged as requested) ---
        payButton.setOnAction(e -> {
            if (!paymentMethod.getValue().equals("UPI")) {
                statusLabel.setText("This functionality is only for UPI payments.");
                statusLabel.setTextFill(Color.ORANGE);
                return;
            }

            String rentStr = amountField.getText();
            LocalDate localDate = datePicker.getValue();
            String upiId = upiIdField.getText();
            String tenantName = tenantNameField.getText();
            String tenantEmail = tenantEmailField.getText();

            if (rentStr.isEmpty() || localDate == null || upiId.isEmpty() || tenantName.isEmpty() || tenantEmail.isEmpty() || !tenantEmail.contains("@") || !upiId.contains("@")) {
                statusLabel.setText("Please fill all fields with valid data.");
                statusLabel.setTextFill(Color.RED);
                return;
            }

            try {
                PaymentData paymentData = new PaymentData();
                paymentData.setRentAmount(Double.parseDouble(rentStr));
                paymentData.setUpiId(upiId);
                paymentData.setTenantName(tenantName);
                paymentData.setTenantEmail(tenantEmail);
                
                Date date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                paymentData.setPaymentDate(Timestamp.of(date));

                dataservice ds = new dataservice();
                
                String documentId = tenantEmail + "_" + System.currentTimeMillis();
                ds.addPayment("Payments", documentId, paymentData);

                statusLabel.setText("Payment successful!");
                statusLabel.setTextFill(Color.GREEN);

                amountField.clear();
                upiIdField.clear();
                // Name and email are not cleared because they are auto-filled
                datePicker.setValue(LocalDate.now());

            } catch (NumberFormatException nfe) {
                statusLabel.setText("Invalid rent amount. Please enter a number.");
                statusLabel.setTextFill(Color.RED);
            } catch (Exception ex) {
                statusLabel.setText("An error occurred during payment. Please try again.");
                statusLabel.setTextFill(Color.RED);
                ex.printStackTrace();
            }
        });

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
                // Use the getSignupData method to fetch the tenant's profile from the "users" collection
                return dataService.getSignupData("users", tenantEmail);
            }
        };

        loadDataTask.setOnSucceeded(e -> {
            DocumentSnapshot doc = loadDataTask.getValue();
            if (doc != null && doc.exists()) {
                Platform.runLater(() -> {
                    // The field name "firstName" must match your Firestore database exactly
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
     * MODIFIED: This method no longer re-creates the common fields.
     * It clears the container and adds back the existing common fields,
     * ensuring that the auto-filled data is preserved.
     */
    private void updatePaymentFields(String method) {
        dynamicFieldsContainer.getChildren().clear();

        // Add the existing common fields back to the container
        dynamicFieldsContainer.getChildren().addAll(tenantNameField, tenantEmailField, amountField, datePicker);

        // Add specific fields based on the selected method
        switch (method) {
            case "Credit/Debit Card":
                TextField cardNumber = new TextField();
                cardNumber.setPromptText("Card Number");
                TextField expiry = new TextField();
                expiry.setPromptText("MM/YY");
                PasswordField cvv = new PasswordField();
                cvv.setPromptText("CVV");
                TextField nameOnCard = new TextField();
                nameOnCard.setPromptText("Name on Card");
                dynamicFieldsContainer.getChildren().addAll(cardNumber, expiry, cvv, nameOnCard);
                break;

            case "UPI":
                upiIdField = new TextField();
                upiIdField.setPromptText("Enter UPI ID (e.g., name@bank)");
                dynamicFieldsContainer.getChildren().add(upiIdField);
                break;

            case "Net Banking":
                ComboBox<String> banks = new ComboBox<>();
                banks.getItems().addAll("SBI", "HDFC", "ICICI", "Axis", "Kotak");
                banks.setPromptText("Select Your Bank");
                dynamicFieldsContainer.getChildren().add(banks);
                break;
        }
    }
}
