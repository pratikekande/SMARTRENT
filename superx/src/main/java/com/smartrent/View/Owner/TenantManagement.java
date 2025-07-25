package com.smartrent.View.Owner;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Owner.Flat;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TenantManagement {

    private dataservice ds;
    private String ownerId;
    private VBox tenantListContainer;

    public TenantManagement(dataservice ds, String ownerId) {
        this.ds = ds;
        this.ownerId = ownerId;
    }

    public Node getView(Runnable onBackToDashboard) {
        // --- Header Section ---
        Text title = new Text("Tenants");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));

        Region spacerHeader = new Region();
        HBox.setHgrow(spacerHeader, Priority.ALWAYS);

        HBox headerBox = new HBox(20, title, spacerHeader);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // --- Left Side: Scrollable Tenant List ---
        tenantListContainer = new VBox(20);
        tenantListContainer.setPadding(new Insets(10, 10, 10, 0));

        ScrollPane scrollPane = new ScrollPane(tenantListContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        // --- Right Side: Documents Card (Static example) ---
        VBox rightColumn = createDocumentsCard();

        // --- Main Layout ---
        HBox mainContentBox = new HBox(30, scrollPane, rightColumn);
        HBox.setHgrow(scrollPane, Priority.ALWAYS);
        VBox managementViewLayout = new VBox(20, headerBox, mainContentBox);
        managementViewLayout.setAlignment(Pos.TOP_CENTER);
        managementViewLayout.setPadding(new Insets(40, 50, 50, 50));
        managementViewLayout.setStyle("-fx-background-color: #f1f5f9;");

        loadTenants();

        return managementViewLayout;
    }

    private void loadTenants() {
        tenantListContainer.getChildren().setAll(new Label("Loading tenants..."));

        Task<List<Flat>> fetchTenantsTask = new Task<>() {
            @Override
            protected List<Flat> call() throws Exception {
                List<QueryDocumentSnapshot> documents = ds.getFlatsByOwner(ownerId);
                List<Flat> flats = new ArrayList<>();
                for (QueryDocumentSnapshot doc : documents) {
                    // ✅ MODIFIED: Convert the document to a Flat object AND set its ID.
                    Flat flat = doc.toObject(Flat.class);
                    flat.setFlatId(doc.getId()); // This is the crucial step
                    flats.add(flat);
                }
                return flats;
            }
        };

        fetchTenantsTask.setOnSucceeded(event -> {
            List<Flat> flats = fetchTenantsTask.getValue();
            tenantListContainer.getChildren().clear();
            if (flats.isEmpty()) {
                tenantListContainer.getChildren().add(new Label("No tenants found."));
            } else {
                for (Flat flat : flats) {
                    if (flat.getTenantName() != null && !flat.getTenantName().isEmpty()) {
                        tenantListContainer.getChildren().add(createTenantCard(flat));
                    }
                }
            }
        });

        fetchTenantsTask.setOnFailed(event -> {
            tenantListContainer.getChildren().setAll(new Label("Error loading tenants."));
            fetchTenantsTask.getException().printStackTrace();
        });

        new Thread(fetchTenantsTask).start();
    }

    private VBox createTenantCard(Flat flat) {
        VBox card = new VBox(15);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10;");
        card.setCursor(Cursor.HAND);
        Text tenantName = new Text(flat.getTenantName());
        tenantName.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        GridPane detailsGrid = new GridPane();
        detailsGrid.setHgap(40);
        detailsGrid.setVgap(12);

        detailsGrid.add(createDetailLabel("Society Name"), 0, 0);
        detailsGrid.add(createDetailValue(flat.getSocietyName()), 1, 0);
        detailsGrid.add(createDetailLabel("Flat No"), 0, 1);
        detailsGrid.add(createDetailValue(flat.getFlatNo()), 1, 1);
        detailsGrid.add(createDetailLabel("Address"), 0, 2);
        detailsGrid.add(createDetailValue(flat.getAddress()), 1, 2);
        detailsGrid.add(createDetailLabel("Monthly Rent"), 0, 3);
        detailsGrid.add(createDetailValue("₹" + flat.getRent()), 1, 3);
        detailsGrid.add(createDetailLabel("Email Address"), 0, 4);
        detailsGrid.add(createDetailValue(flat.getTenantEmail()), 1, 4);

        // --- ✅ MODIFIED: Delete Button with Logic ---
        Button deleteButton = new Button("Delete Tenant");
        deleteButton.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
        deleteButton.setCursor(Cursor.HAND);
        // Attach the action handler which calls our new method
        deleteButton.setOnAction(e -> handleDeleteTenant(flat, card));

        HBox buttonContainer = new HBox(deleteButton);
        buttonContainer.setAlignment(Pos.CENTER_RIGHT);
        buttonContainer.setPadding(new Insets(10, 0, 0, 0));

        // ✅ MODIFIED: Add the button container to the card's layout
        card.getChildren().addAll(tenantName, new Separator(), detailsGrid, buttonContainer);
        return card;
    }

    // ✅ ADDED: This new method handles the full deletion process with user confirmation.
    private void handleDeleteTenant(Flat flat, VBox cardToRemove) {
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Deletion");
        confirmationAlert.setHeaderText("Remove Tenant: " + flat.getTenantName());
        confirmationAlert.setContentText("Are you sure you want to remove this tenant from the property? This action cannot be undone.");

        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // User confirmed, so proceed on a background thread
            Task<Void> deleteTask = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    ds.removeTenantFromFlat(flat.getFlatId());
                    return null;
                }
            };

            deleteTask.setOnSucceeded(e -> {
                Platform.runLater(() -> {
                    tenantListContainer.getChildren().remove(cardToRemove);
                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Tenant successfully removed.");
                    successAlert.showAndWait();
                });
            });

            deleteTask.setOnFailed(e -> {
                Platform.runLater(() -> {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Could not remove tenant.");
                    errorAlert.setContentText("An error occurred while trying to update the database. Please try again.");
                    errorAlert.showAndWait();
                });
                deleteTask.getException().printStackTrace();
            });

            new Thread(deleteTask).start();
        }
    }

    // --- Helper Methods (Unchanged) ---

    private VBox createDocumentsCard() {
        Text docTitle = new Text("Documents");
        docTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20));
        VBox docList = new VBox(15);
        String[] docNames = {"Lease Agreement PDF", "ID Scan JPEG", "Utility Bill PNG"};
        for (String name : docNames) {
            HBox row = new HBox(10, new Label("📄"), new Label(name), new Region() {{ HBox.setHgrow(this, Priority.ALWAYS); }}, new Label("⬇"));
            row.setAlignment(Pos.CENTER_LEFT);
            docList.getChildren().add(row);
        }
        Button downloadDoc = new Button("⬇ Download Selected");
        downloadDoc.setMaxWidth(Double.MAX_VALUE);
        downloadDoc.setCursor(Cursor.HAND);
        VBox docsSection = new VBox(20, docTitle, docList, downloadDoc);
        docsSection.setPadding(new Insets(25));
        docsSection.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #d1d5db; -fx-border-radius: 15;");

        docsSection.setPrefWidth(360);
        VBox rightColumn = new VBox(docsSection);
        rightColumn.setAlignment(Pos.CENTER);
        return rightColumn;
    }

    private Text createDetailLabel(String text) {
        Text label = new Text(text);
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        label.setStyle("-fx-fill: #6b7280;");
        return label;
    }

    private Text createDetailValue(String text) {
        Text value = new Text(text);
        value.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 14));
        value.setStyle("-fx-fill: #111827;");
        return value;
    }
}
