package com.smartrent.View.Tenant;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Tenant.PaymentData;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class TenantPaymentHistory {

    // NEW: A member variable to store the tenant's email.
    private final String tenantEmail;

    /**
     * NEW: The constructor.
     * When you create this class, you must provide the email of the logged-in tenant.
     * @param tenantEmail The email address of the currently logged-in tenant.
     */
    public TenantPaymentHistory(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    /**
     * MODIFIED: The getView method no longer takes any arguments.
     * This will fix the error in your dashboard.
     * @return A VBox containing the complete view.
     */
    public VBox getView() {

        // --- UI Setup ---
        VBox paymentList = new VBox(15);
        paymentList.setPadding(new Insets(10));

        // MODIFIED: Pass the tenant's email to the method that populates the list
        populatePaymentHistory(paymentList, this.tenantEmail);

        ScrollPane scrollPane = new ScrollPane(paymentList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setStyle("-fx-background-color: transparent;");

        Label title = new Label("Payment History");
        title.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 32));
        title.setTextFill(Color.web("#1f2937"));

        Label subtitle = new Label("Track all your rent payments and download receipts");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        subtitle.setTextFill(Color.web("#6b7280"));

        VBox headingBox = new VBox(6, title, subtitle);
        headingBox.setAlignment(Pos.TOP_LEFT);
        headingBox.setPadding(new Insets(20, 0, 10, 10));

        VBox mainContent = new VBox(10, headingBox, scrollPane);
        mainContent.setPadding(new Insets(30, 40, 30, 30));
        mainContent.setStyle("-fx-background-color: #f3f4f6;");
        mainContent.setPrefWidth(1000);

        HBox root = new HBox(mainContent);
        root.setPrefSize(1200, 800);

        VBox container = new VBox(root);
        return container;
    }

    /**
     * MODIFIED: This method now fetches payments only for a specific tenant.
     * @param container   The VBox to populate with payment cards.
     * @param tenantEmail The email to filter the payments by.
     */
    private void populatePaymentHistory(VBox container, String tenantEmail) {
        ProgressIndicator loading = new ProgressIndicator();
        container.getChildren().setAll(loading);

        Task<List<PaymentData>> fetchPaymentsTask = new Task<>() {
            @Override
            protected List<PaymentData> call() throws Exception {
                dataservice ds = new dataservice();
                // MODIFIED: Call the filtered method from your dataservice
                List<QueryDocumentSnapshot> documents = ds.getPaymentsForTenant(tenantEmail);
                List<PaymentData> payments = new ArrayList<>();
                if (documents != null) {
                    for (QueryDocumentSnapshot doc : documents) {
                        payments.add(doc.toObject(PaymentData.class));
                    }
                }
                return payments;
            }
        };

        fetchPaymentsTask.setOnSucceeded(e -> {
            container.getChildren().clear();
            List<PaymentData> payments = fetchPaymentsTask.getValue();
            if (payments.isEmpty()) {
                container.getChildren().add(new Label("No payment history found."));
            } else {
                for (PaymentData payment : payments) {
                    container.getChildren().add(createPaymentCard(payment));
                }
            }
        });

        fetchPaymentsTask.setOnFailed(e -> {
            container.getChildren().setAll(new Label("Error: Failed to load payment history."));
            fetchPaymentsTask.getException().printStackTrace();
        });

        new Thread(fetchPaymentsTask).start();
    }

    /**
     * Creates a card using a real PaymentData object from Firestore.
     */
    private VBox createPaymentCard(PaymentData payment) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String date = sdf.format(payment.getPaymentDate().toDate());
        String amount = "₹" + String.format("%,.0f", payment.getRentAmount());
        String status = "Paid"; // Status is hardcoded as it is not in the model

        Label dateLabel = new Label("Payment Date: " + date);
        dateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        dateLabel.setTextFill(Color.web("#1f2937"));

        Label amountLabel = new Label("Amount: " + amount);
        amountLabel.setTextFill(Color.web("#6b7280"));
        amountLabel.setFont(Font.font("Arial", 13));

        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        statusLabel.setTextFill(Color.WHITE);

        String bgColor;
        switch (status) {
            case "Paid":    bgColor = "#22c55e"; break;
            case "Pending": bgColor = "#3b82f6"; break;
            default:        bgColor = "#ef4444"; break;
        }
        statusLabel.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-padding: 5 14;");

        Button downloadBtn = new Button("Download");
        downloadBtn.setFont(Font.font("Arial", FontWeight.SEMI_BOLD, 12));
        downloadBtn.setStyle("-fx-background-color: #636ae8; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 6 14;");
        downloadBtn.setOnMouseEntered(e -> downloadBtn.setStyle(downloadBtn.getStyle() + "-fx-cursor: hand; -fx-opacity: 0.9;"));
        downloadBtn.setOnMouseExited(e -> downloadBtn.setStyle(downloadBtn.getStyle().replace("-fx-cursor: hand; -fx-opacity: 0.9;", "")));
        downloadBtn.setOnAction(e -> System.out.println("Download clicked for: " + date));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(dateLabel, spacer, statusLabel, downloadBtn);
        topRow.setSpacing(10);
        topRow.setAlignment(Pos.CENTER_LEFT);

        VBox card = new VBox(8, topRow, amountLabel);
        card.setPadding(new Insets(16));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 4, 0, 0, 2); -fx-border-color: #e5e7eb; -fx-border-radius: 12;");
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 3);"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.12), 8, 0, 0, 3);", "")));

        return card;
    }
}
