package com.smartrent.View.Owner;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Tenant.MaintenanceRequest;
import javafx.concurrent.Task;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MaintananceRequest {

    // Member variables for UI components
    private VBox historyList;
    private Label title;
    private Label tenantNameLabel;
    private Label societyNameLabel; 
    private Label flatNoLabel;      
    private ImageView imgView;
    private Label descLabel;
    private ProgressIndicator imageLoadingIndicator;
    private Button updateStatusBtn; 
    private ComboBox<String> statusComboBox; 
    private MaintenanceRequest activeRequest; 
    private final String ownerId; 

    public MaintananceRequest(String ownerId) {
        this.ownerId = ownerId;
    }

    public HBox getView() {
        // --- Left: Maintenance Request Details ---
        title = new Label("No Maintenance Requests");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        title.setWrapText(true);
        title.setMaxWidth(500);

        Label submittedByTitle = new Label("Submitted by:");
        submittedByTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        
        tenantNameLabel = new Label("N/A");
        tenantNameLabel.setFont(Font.font("Segoe UI", 16));
        tenantNameLabel.setTextFill(Color.GRAY);
        
        societyNameLabel = new Label("N/A");
        societyNameLabel.setFont(Font.font("Segoe UI", 16));
        societyNameLabel.setTextFill(Color.GRAY);
        
        flatNoLabel = new Label("N/A");
        flatNoLabel.setFont(Font.font("Segoe UI", 16));
        flatNoLabel.setTextFill(Color.GRAY);

        imgView = new ImageView();
        imgView.setFitWidth(200);
        imgView.setFitHeight(200);
        imgView.setPreserveRatio(true);

        imageLoadingIndicator = new ProgressIndicator();
        imageLoadingIndicator.setVisible(false);
        imageLoadingIndicator.setMaxSize(50, 50);

        StackPane imageContainer = new StackPane(imgView, imageLoadingIndicator);
        imageContainer.setAlignment(Pos.CENTER);
        imageContainer.setPadding(new Insets(10, 0, 10, 0));

        Label descTitle = new Label("Description");
        descTitle.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        
        descLabel = new Label("No description available.");
        descLabel.setFont(Font.font("Segoe UI", 14));
        descLabel.setWrapText(true);
        descLabel.setMaxWidth(500);

        statusComboBox = new ComboBox<>();
        statusComboBox.getItems().addAll("In Progress", "Completed");
        statusComboBox.setPromptText("Update Status");

        updateStatusBtn = new Button("Update Status");
        updateStatusBtn.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));
        updateStatusBtn.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 8 18;");
        updateStatusBtn.setCursor(Cursor.HAND);
        updateStatusBtn.setDisable(true); 
        updateStatusBtn.setOnAction(e -> handleUpdateRequest());
        
        HBox updateBox = new HBox(10, statusComboBox, updateStatusBtn);
        updateBox.setAlignment(Pos.CENTER_RIGHT);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setVgap(15);
        detailsGrid.setHgap(10);
        detailsGrid.add(title, 0, 0, 2, 1);
        detailsGrid.add(submittedByTitle, 0, 1);
        detailsGrid.add(tenantNameLabel, 1, 1);
        detailsGrid.add(new Label("Society:") {{ setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16)); }}, 0, 2);
        detailsGrid.add(societyNameLabel, 1, 2);
        detailsGrid.add(new Label("Flat No:") {{ setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16)); }}, 0, 3);
        detailsGrid.add(flatNoLabel, 1, 3);
        detailsGrid.add(imageContainer, 0, 4, 2, 1);
        detailsGrid.add(descTitle, 0, 5);
        detailsGrid.add(descLabel, 0, 6, 2, 1);
        detailsGrid.add(updateBox, 0, 7, 2, 1); 
        GridPane.setHalignment(updateBox, HPos.RIGHT); 

        VBox formSection = new VBox(detailsGrid);
        formSection.setPadding(new Insets(40));
        formSection.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1;");
        formSection.setPrefWidth(650);

        // --- Right: Scrollable Maintenance History ---
        Label histTitle = new Label("Maintenance History");
        histTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        Label histSubtitle = new Label("Recent maintenance requests and their statuses.");
        histSubtitle.setTextFill(Color.GRAY);
        histSubtitle.setFont(Font.font("Segoe UI", 12));
        Separator separator = new Separator();

        historyList = new VBox(15);
        historyList.setPadding(new Insets(10));

        populateMaintenanceData();

        ScrollPane scroll = new ScrollPane(historyList);
        scroll.setFitToWidth(true);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        VBox historyCard = new VBox(20, histTitle, histSubtitle, separator, scroll);
        historyCard.setPadding(new Insets(20));
        historyCard.setStyle("-fx-background-color: white; -fx-border-color: #e5e7eb; -fx-border-width: 1;");
        VBox.setVgrow(historyCard, Priority.ALWAYS);

        VBox historySection = new VBox(historyCard);
        historySection.setPadding(new Insets(40, 40, 40, 0));
        historySection.setPrefWidth(600);

        HBox content = new HBox(40, formSection, historySection);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: #f1f5f9;");

        return content;
    }

    private void populateMaintenanceData() {
        ProgressIndicator loading = new ProgressIndicator();
        historyList.getChildren().setAll(loading);

        Task<List<MaintenanceRequest>> fetchTask = new Task<>() {
            @Override
            protected List<MaintenanceRequest> call() throws Exception {
                dataservice ds = new dataservice();
                List<QueryDocumentSnapshot> documents = ds.getMaintenanceRequestsByOwner(ownerId);
                List<MaintenanceRequest> requests = new ArrayList<>();
                if (documents != null) {
                    for (QueryDocumentSnapshot doc : documents) {
                        MaintenanceRequest request = doc.toObject(MaintenanceRequest.class);
                        request.setDocumentId(doc.getId());
                        requests.add(request);
                    }
                }
                return requests;
            }
        };

        fetchTask.setOnSucceeded(e -> {
            historyList.getChildren().clear();
            List<MaintenanceRequest> requests = fetchTask.getValue();

            if (requests.isEmpty()) {
                historyList.getChildren().add(new Label("No maintenance history found."));
                updateFormDetails(null);
            } else {
                for (MaintenanceRequest request : requests) {
                    historyList.getChildren().add(createHistoryRow(request));
                }
                updateFormDetails(requests.get(0));
            }
        });

        fetchTask.setOnFailed(e -> {
            historyList.getChildren().setAll(new Label("Error: Failed to load history."));
            updateFormDetails(null);
            fetchTask.getException().printStackTrace();
        });

        new Thread(fetchTask).start();
    }

    private void updateFormDetails(MaintenanceRequest request) {
        this.activeRequest = request; 

        if (request == null) {
            title.setText("No Maintenance Requests");
            tenantNameLabel.setText("N/A");
            societyNameLabel.setText("N/A");
            flatNoLabel.setText("N/A");
            descLabel.setText("No description available.");
            imgView.setImage(null);
            imageLoadingIndicator.setVisible(false);
            updateStatusBtn.setDisable(true); 
            statusComboBox.setDisable(true);
        } else {
            title.setText(request.getTitle());
            tenantNameLabel.setText(request.getTenantName());
            societyNameLabel.setText(request.getSocietyName());
            flatNoLabel.setText(request.getFlatNo());
            descLabel.setText(request.getDescription());

            statusComboBox.setValue(request.getStatus());
            
            boolean isCompleted = "Completed".equalsIgnoreCase(request.getStatus());
            updateStatusBtn.setDisable(isCompleted);
            statusComboBox.setDisable(isCompleted);

            imgView.setImage(null);

            if (request.getImageUrl() != null && !request.getImageUrl().isEmpty()) {
                imageLoadingIndicator.setVisible(true);
                Image image = new Image(request.getImageUrl(), true);

                image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() >= 1.0) {
                        imgView.setImage(image);
                        imageLoadingIndicator.setVisible(false);
                    }
                });

                image.errorProperty().addListener((obs, wasError, isError) -> {
                    if (isError) {
                        System.err.println("Failed to load image: " + request.getImageUrl());
                        imageLoadingIndicator.setVisible(false);
                    }
                });
            } else {
                imageLoadingIndicator.setVisible(false);
            }
        }
    }

    private void handleUpdateRequest() {
        if (activeRequest == null || activeRequest.getDocumentId() == null || statusComboBox.getValue() == null) {
            return;
        }

        String newStatus = statusComboBox.getValue();
        if (newStatus.equals(activeRequest.getStatus())) {
            return;
        }

        updateStatusBtn.setDisable(true); 
        Task<Void> updateTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                dataservice ds = new dataservice();
                ds.updateMaintenanceStatus(activeRequest.getDocumentId(), newStatus).get();
                return null;
            }
        };

        updateTask.setOnSucceeded(event -> {
            populateMaintenanceData(); 
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Status updated successfully.");
            alert.showAndWait();
        });

        updateTask.setOnFailed(event -> {
            updateTask.getException().printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to update status. Please try again.");
            alert.showAndWait();
            if (activeRequest != null && !"Completed".equalsIgnoreCase(activeRequest.getStatus())) {
                updateStatusBtn.setDisable(false);
            }
        });

        new Thread(updateTask).start();
    }

    private Node createHistoryRow(MaintenanceRequest request) {
        String issueText = request.getTitle();
        String date = "N/A";
        if (request.getTimestamp() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.format(request.getTimestamp().toDate());
        }
        String statusText = request.getStatus() != null ? request.getStatus() : "Pending";

        Label issue = new Label(issueText);
        issue.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 14));

        Label dateLabel = new Label(date);
        dateLabel.setFont(Font.font("Segoe UI", 12));
        dateLabel.setTextFill(Color.GRAY);
        
        String propertyInfo = String.format("%s, Flat %s", request.getSocietyName(), request.getFlatNo());
        Label propertyLabel = new Label(propertyInfo);
        propertyLabel.setFont(Font.font("Segoe UI", 12));
        propertyLabel.setTextFill(Color.GRAY);

        String statusStyle = switch (statusText) {
            case "Completed" -> "-fx-background-color: #dcfce7; -fx-text-fill: #15803d;";
            case "In Progress" -> "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;";
            default -> "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
        };

        Label status = new Label(statusText);
        status.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        status.setPadding(new Insets(2, 10, 2, 10));
        status.setStyle(statusStyle + " -fx-background-radius: 10;");

        VBox textBox = new VBox(5, issue, propertyLabel, dateLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        status.setMinWidth(100);
        status.setAlignment(Pos.CENTER);

        HBox row = new HBox(20, textBox, status);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 10;");
        row.setCursor(Cursor.HAND);

        row.setOnMouseClicked(e -> updateFormDetails(request));

        return row;
    }
}
