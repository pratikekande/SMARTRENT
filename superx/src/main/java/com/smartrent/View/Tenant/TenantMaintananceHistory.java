package com.smartrent.View.Tenant;

import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Tenant.MaintenanceRequest;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
//import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.text.SimpleDateFormat;
// import java.util.ArrayList;
// import java.util.List;

public class TenantMaintananceHistory {

    private final String tenantEmail;
    private VBox requestList;
    private ListenerRegistration maintenanceListener; // To manage the listener
    private final ObservableList<MaintenanceRequest> maintenanceRequests = FXCollections.observableArrayList();

    public TenantMaintananceHistory(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public VBox getView() {
        requestList = new VBox(15);
        requestList.setPadding(new Insets(10));

        // Start listening for real-time updates
        setupMaintenanceListener();

        ScrollPane scrollPane = new ScrollPane(requestList);
        scrollPane.setFitToWidth(true);
        scrollPane.setPadding(new Insets(10));
        scrollPane.setStyle("-fx-background-color: transparent;");

        Label heading = new Label("Maintenance History");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        VBox mainContent = new VBox(20, heading, scrollPane);
        mainContent.setPadding(new Insets(20));
        mainContent.setStyle("-fx-background-color: #f3f4f6;");
        mainContent.setPrefWidth(1000);

        HBox root = new HBox(mainContent);
        root.setPrefSize(1200, 800);

        VBox container = new VBox(root);
        return container;
    }

    /**
     * MODIFIED: This method now sets up a real-time listener to handle updates automatically.
     */
    private void setupMaintenanceListener() {
        ProgressIndicator loading = new ProgressIndicator();
        requestList.getChildren().setAll(loading);

        if (maintenanceListener != null) {
            maintenanceListener.remove();
        }

        dataservice ds = new dataservice();
        maintenanceListener = ds.listenForMaintenanceRequestsForTenant(tenantEmail, (snapshots, error) -> {
            if (error != null) {
                System.err.println("Listen failed: " + error);
                Platform.runLater(() -> requestList.getChildren().setAll(new Label("Error loading data.")));
                return;
            }

            if (snapshots != null) {
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    MaintenanceRequest request = dc.getDocument().toObject(MaintenanceRequest.class);
                    request.setDocumentId(dc.getDocument().getId());

                    switch (dc.getType()) {
                        case ADDED:
                            maintenanceRequests.add(0, request); // Add new requests to the top
                            break;
                        case MODIFIED:
                            // Find the existing request and update it
                            int index = -1;
                            for (int i = 0; i < maintenanceRequests.size(); i++) {
                                if (maintenanceRequests.get(i).getDocumentId().equals(request.getDocumentId())) {
                                    index = i;
                                    break;
                                }
                            }
                            if (index != -1) {
                                maintenanceRequests.set(index, request);
                            }
                            break;
                        case REMOVED:
                            maintenanceRequests.removeIf(r -> r.getDocumentId().equals(request.getDocumentId()));
                            break;
                    }
                }
                
                // Update the UI on the JavaFX application thread
                Platform.runLater(this::updateUIFromList);
            }
        });
    }
    
    /**
     * NEW: This helper method redraws the list based on the current state of the observable list.
     */
    private void updateUIFromList() {
        requestList.getChildren().clear();
        if (maintenanceRequests.isEmpty()) {
            requestList.getChildren().add(new Label("No maintenance requests found."));
        } else {
            for (MaintenanceRequest request : maintenanceRequests) {
                requestList.getChildren().add(createMaintenanceCard(request));
            }
        }
    }

    private VBox createMaintenanceCard(MaintenanceRequest request) {
        String issue = request.getTitle();
        String status = request.getStatus() != null ? request.getStatus() : "Unknown";
        
        String date = "N/A";
        if (request.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.format(request.getTimestamp().toDate());
        }

        Label issueLabel = new Label("Issue: " + issue);
        issueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label dateLabel = new Label("Date: " + date);
        dateLabel.setTextFill(Color.GRAY);

        Label statusLabel = new Label(status);
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        String bgColor;
        switch (status) {
            case "Completed":   bgColor = "#22c55e"; break;
            case "In Progress": bgColor = "#fbbf24"; break;
            default:            bgColor = "#3b82f6"; break; // Pending or other
        }

        statusLabel.setStyle("-fx-background-color: " + bgColor + "; -fx-text-fill: white; -fx-background-radius: 10; -fx-padding: 4 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(issueLabel, spacer, statusLabel);
        VBox card = new VBox(5, topRow, dateLabel);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 2, 0, 0, 2);");
        card.setCursor(Cursor.HAND);
        return card;
    }
}
