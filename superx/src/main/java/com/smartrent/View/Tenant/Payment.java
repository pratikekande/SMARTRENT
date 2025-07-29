package com.smartrent.View.Tenant;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Tenant.PaymentData;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;

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
    private Label statusLabel;

    private StackPane qrPopup;
    private ImageView qrImageView;
    private StackPane successOverlay;

    private final dataservice dataService;
    private final String tenantEmail;

    public Payment(String tenantEmail) {
        this.dataService = new dataservice();
        this.tenantEmail = tenantEmail;
    }

    public Pane getView(Runnable onBack) {

        // --- Title ---
        Label title = new Label("Pay Rent");
        title.setFont(Font.font("Arial", 28));
        title.setStyle("-fx-font-weight: bold;");
        HBox titleBox = new HBox(title);
        titleBox.setPadding(new Insets(20, 40, 20, 40));

        // --- Payment Form ---
        Label paymentLabel = new Label("Select Payment Method:");
        ComboBox<String> paymentMethod = new ComboBox<>();
        paymentMethod.getItems().addAll("UPI", "Credit/Debit Card", "Net Banking");
        paymentMethod.setValue("UPI");

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
        updatePaymentFields("UPI", paymentMethod);
        paymentMethod.valueProperty().addListener((obs, oldVal, newVal) -> updatePaymentFields(newVal, paymentMethod));

        TextArea notes = new TextArea();
        notes.setPromptText("Add a note to the landlord (optional)");
        notes.setPrefRowCount(2);

        Button payButton = new Button("Pay Rent Now");
        payButton.setPrefHeight(45);
        payButton.setMaxWidth(Double.MAX_VALUE);
        payButton.setStyle("-fx-background-color: #6A42E4; -fx-text-fill: white; -fx-font-size: 16px; -fx-background-radius: 10;");
        payButton.setCursor(Cursor.HAND);
        statusLabel = new Label();
        statusLabel.setPadding(new Insets(8, 0, 0, 0));

        VBox rightBox = new VBox(15,
                paymentLabel, paymentMethod, dynamicFieldsContainer,
                notes, payButton, statusLabel);
        rightBox.setPrefWidth(600);
        rightBox.setPadding(new Insets(0, 0, 20, 0));

        HBox mainContent = new HBox(40, rightBox);
        mainContent.setPadding(new Insets(0, 40, 0, 40));
        mainContent.setAlignment(Pos.TOP_CENTER);

        // --- Back Button ---
        Button backButton = new Button("← Back to Dashboard");
        backButton.setFont(Font.font("Arial", 14));
        backButton.setStyle("-fx-background-color: #EAEAEA; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        backButton.setPrefHeight(40);
        backButton.setOnAction(e -> onBack.run());

        HBox buttonContainer = new HBox(backButton);
        buttonContainer.setPadding(new Insets(20, 40, 20, 40));
        buttonContainer.setAlignment(Pos.CENTER_LEFT);

        // --- Main Layout ---
        BorderPane contentPane = new BorderPane();
        contentPane.setTop(titleBox);
        contentPane.setCenter(mainContent);
        contentPane.setBottom(buttonContainer);

        VBox mainLayout = new VBox(contentPane);
        mainLayout.setStyle("-fx-background-color: white;");
        VBox.setVgrow(contentPane, Priority.ALWAYS);

        // --- Popup Creation ---
        createQrPopup();
        createSuccessAnimation();

        StackPane rootStack = new StackPane();
        rootStack.getChildren().addAll(mainLayout, qrPopup, successOverlay);

        // --- Event Handlers ---
        payButton.setOnAction(e -> handlePayment(paymentMethod));

        loadAndPopulateTenantData();

        return rootStack;
    }

    private void loadAndPopulateTenantData() {
        Task<DocumentSnapshot> loadDataTask = new Task<>() {
            @Override
            protected DocumentSnapshot call() throws Exception {
                return dataService.getSignupData("users", tenantEmail);
            }
        };

        loadDataTask.setOnSucceeded(e -> {
            DocumentSnapshot doc = loadDataTask.getValue();
            if (doc != null && doc.exists()) {
                Platform.runLater(() -> {
                    String firstName = doc.getString("firstName");
                    tenantNameField.setText(firstName);
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

    private void updatePaymentFields(String method, ComboBox<String> paymentMethod) {
        dynamicFieldsContainer.getChildren().clear();
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

                Label orLabel = new Label("OR");
                orLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

                Button qrButton = new Button("Scan QR Code");
                qrButton.setCursor(Cursor.HAND);
                qrButton.setMaxWidth(Double.MAX_VALUE);

                qrButton.setOnAction(e -> {
                    try {
                        String rentAmount = amountField.getText().isEmpty() ? "0" : amountField.getText();
                        String qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=upi://pay?pa=placeholder@upi&pn=SmartRent&am=" + rentAmount + "&cu=INR";
                        qrImageView.setImage(new Image(qrCodeUrl));
                        qrPopup.setVisible(true);
                    } catch (Exception ex) {
                        statusLabel.setText("Could not load QR code.");
                        statusLabel.setTextFill(Color.RED);
                    }
                });
                
                VBox upiLayout = new VBox(10, upiIdField, orLabel, qrButton);
                upiLayout.setAlignment(Pos.CENTER);

                dynamicFieldsContainer.getChildren().add(upiLayout);
                break;

            case "Net Banking":
                ComboBox<String> banks = new ComboBox<>();
                banks.getItems().addAll("SBI", "HDFC", "ICICI", "Axis", "Kotak");
                banks.setPromptText("Select Your Bank");
                dynamicFieldsContainer.getChildren().add(banks);
                break;
        }
    }
    
    private void handlePayment(ComboBox<String> paymentMethod) {
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

            playSuccessAnimation();

            amountField.clear();
            upiIdField.clear();
            datePicker.setValue(LocalDate.now());

        } catch (NumberFormatException nfe) {
            statusLabel.setText("Invalid rent amount. Please enter a number.");
            statusLabel.setTextFill(Color.RED);
        } catch (Exception ex) {
            statusLabel.setText("An error occurred during payment. Please try again.");
            statusLabel.setTextFill(Color.RED);
            ex.printStackTrace();
        }
    }
    
    private void createQrPopup() {
        this.qrImageView = new ImageView();
        qrImageView.setFitWidth(200);
        qrImageView.setFitHeight(200);

        Button closePopupButton = new Button("Close");
        closePopupButton.setStyle("-fx-background-color: #EAEAEA; -fx-text-fill: black; -fx-font-weight: bold; -fx-background-radius: 8;");
        closePopupButton.setCursor(Cursor.HAND);
        closePopupButton.setOnAction(e -> qrPopup.setVisible(false));

        VBox popupContent = new VBox(20, qrImageView, closePopupButton);
        popupContent.setAlignment(Pos.CENTER);
        popupContent.setPadding(new Insets(30));
        popupContent.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 20, 0, 0, 5);");
        popupContent.setMaxSize(300, 320);

        this.qrPopup = new StackPane(popupContent);
        qrPopup.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        qrPopup.setVisible(false);
    }

    private void createSuccessAnimation() {
        Circle backgroundCircle = new Circle(50);
        backgroundCircle.setFill(Color.web("#2ECC71"));
        backgroundCircle.setScaleX(0);
        backgroundCircle.setScaleY(0);

        Path checkMark = new Path();
        checkMark.getElements().addAll(
                new MoveTo(35, 50),
                new LineTo(50, 65),
                new LineTo(75, 40)
        );
        checkMark.setStroke(Color.WHITE);
        checkMark.setStrokeWidth(5);
        checkMark.setStrokeLineCap(StrokeLineCap.ROUND);
        checkMark.setOpacity(0);

        StackPane animationContent = new StackPane(backgroundCircle, checkMark);
        animationContent.setPrefSize(100, 100);

        successOverlay = new StackPane(animationContent);
        successOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        successOverlay.setVisible(false);
    }

    private void playSuccessAnimation() {
        successOverlay.setVisible(true);

        StackPane animationContent = (StackPane) successOverlay.getChildren().get(0);
        Circle backgroundCircle = (Circle) animationContent.getChildren().get(0);
        Path checkMark = (Path) animationContent.getChildren().get(1);

        backgroundCircle.setScaleX(0);
        backgroundCircle.setScaleY(0);
        checkMark.setOpacity(0);

        ScaleTransition scaleCircle = new ScaleTransition(Duration.millis(300), backgroundCircle);
        scaleCircle.setToX(1);
        scaleCircle.setToY(1);

        FadeTransition fadeInCheck = new FadeTransition(Duration.millis(200), checkMark);
        fadeInCheck.setToValue(1);

        PauseTransition pause = new PauseTransition(Duration.seconds(1.5));

        FadeTransition fadeOutOverlay = new FadeTransition(Duration.millis(500), successOverlay);
        fadeOutOverlay.setToValue(0);
        fadeOutOverlay.setOnFinished(e -> {
            successOverlay.setVisible(false);
            successOverlay.setOpacity(1);
        });

        SequentialTransition sequence = new SequentialTransition(scaleCircle, fadeInCheck, pause, fadeOutOverlay);
        sequence.play();
    }
}
