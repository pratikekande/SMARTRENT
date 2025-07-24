package com.smartrent.View.Tenant;

import com.google.cloud.Timestamp;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Tenant.PaymentData;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

        Label statusLabel = new Label();
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

        // --- Logic for the "Pay Rent Now" button ---
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
                
                // MODIFIED: Create a unique document ID for each payment.
                // This prevents overwriting the old data.
                String documentId = tenantEmail + "_" + System.currentTimeMillis();
                ds.addPayment("Payments", documentId, paymentData);

                statusLabel.setText("Payment successful!");
                statusLabel.setTextFill(Color.GREEN);

                amountField.clear();
                upiIdField.clear();
                tenantNameField.clear();
                tenantEmailField.clear();
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

        return root;
    }

    private void updatePaymentFields(String method) {
        dynamicFieldsContainer.getChildren().clear();

        amountField = new TextField();
        amountField.setPromptText("Add Rent Amount");

        datePicker = new DatePicker();
        datePicker.setPromptText("Date");
        datePicker.setValue(LocalDate.now());

        tenantNameField = new TextField();
        tenantNameField.setPromptText("Enter Tenant Name");

        tenantEmailField = new TextField();
        tenantEmailField.setPromptText("Enter Tenant Email ID");

        dynamicFieldsContainer.getChildren().addAll(tenantNameField, tenantEmailField, amountField, datePicker);

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
