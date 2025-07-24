package com.smartrent.View.Tenant;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Tenant.MaintenanceRequest;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

public class TenantMaintananceHistory {

    // NEW: A member variable to store the tenant's email.
    private final String tenantEmail;

    /**
     * NEW: The constructor.
     * When you create an instance of this class, you must provide the
     * email of the logged-in tenant.
     * @param tenantEmail The email address of the currently logged-in tenant.
     */
    public TenantMaintananceHistory(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    /**
     * MODIFIED: The getView method now has no parameters.
     * This will fix the "wiggly line" error in your dashboard.
     * @return A VBox containing the complete view.
     */
    public VBox getView() {

        // --- UI Setup ---
        VBox requestList = new VBox(15);
        requestList.setPadding(new Insets(10));

        // MODIFIED: The tenant's email is now passed from the member variable.
        populateMaintenanceHistory(requestList, this.tenantEmail);

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
     * This method now fetches requests only for a specific tenant.
     * @param container   The VBox to populate with cards.
     * @param tenantEmail The email to filter the requests by.
     */
    private void populateMaintenanceHistory(VBox container, String tenantEmail) {
        ProgressIndicator loading = new ProgressIndicator();
        container.getChildren().setAll(loading);

        Task<List<MaintenanceRequest>> fetchRequestsTask = new Task<>() {
            @Override
            protected List<MaintenanceRequest> call() throws Exception {
                dataservice ds = new dataservice();
                // This correctly calls the filtered method from your dataservice
                List<QueryDocumentSnapshot> documents = ds.getMaintenanceRequestsForTenant(tenantEmail);
                List<MaintenanceRequest> requests = new ArrayList<>();
                if (documents != null) {
                    for (QueryDocumentSnapshot doc : documents) {
                        requests.add(doc.toObject(MaintenanceRequest.class));
                    }
                }
                return requests;
            }
        };

        fetchRequestsTask.setOnSucceeded(e -> {
            container.getChildren().clear();
            List<MaintenanceRequest> requests = fetchRequestsTask.getValue();
            if (requests.isEmpty()) {
                container.getChildren().add(new Label("No maintenance requests found for this tenant."));
            } else {
                for (MaintenanceRequest request : requests) {
                    container.getChildren().add(createMaintenanceCard(request));
                }
            }
        });

        fetchRequestsTask.setOnFailed(e -> {
            container.getChildren().setAll(new Label("Error: Failed to load maintenance history."));
            fetchRequestsTask.getException().printStackTrace();
        });

        new Thread(fetchRequestsTask).start();
    }

    /**
     * Creates a card using a real MaintenanceRequest object from Firestore.
     */
    private VBox createMaintenanceCard(MaintenanceRequest request) {
        String issue = request.getTitle();
        String status = request.getStatus() != null ? request.getStatus() : "Unknown";
        
        String date = "N/A";
        if (request.getSubmittedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.format(request.getSubmittedAt().toDate());
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

        return card;
    }
}
