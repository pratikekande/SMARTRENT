package com.smartrent.View.Owner;

import com.smartrent.View.LandingPage;
import com.smartrent.View.Owner.Component.OSidebar;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartrent.Controller.*;
import com.smartrent.Model.Owner.Flat;
import com.smartrent.Model.Tenant.MaintenanceRequest;
import com.smartrent.Model.Tenant.PaymentData;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class OwnerDashboard {

    private BorderPane root;
    private OSidebar sidebar;
    private Stage primaryStage;
    private Node dashboardView;
    private BorderPane contentWrapper;
    private String ownerId;
    private dataservice ds;
    private Label ownerName;
    private VBox flatsListContainer;
    private Text welcomeTitle;
    private VBox alertsContainer; 
    private ListenerRegistration maintenanceListener;
    private ListenerRegistration paymentListener; 

    public Scene createScene(Stage stage, String ownerId) {
        this.primaryStage = stage;
        this.ownerId = ownerId;
        this.ds = new dataservice();
        root = new BorderPane();

        sidebar = new OSidebar("Owner Dashboard");
        root.setLeft(sidebar);

        contentWrapper = new BorderPane();
        Node profileHeader = createProfileHeader();
        contentWrapper.setTop(profileHeader);
        root.setCenter(contentWrapper);

        this.dashboardView = createDashboardView();
        contentWrapper.setCenter(this.dashboardView);

        setupNavigation();
        loadOwnerName();
        loadOwnerFlats();
        
        setupMaintenanceListener();
        setupPaymentListener(); 

        return new Scene(root, 1280, 720);
    }
    
    private void setupMaintenanceListener() {
        if (maintenanceListener != null) {
            maintenanceListener.remove();
        }
        
        final boolean[] isInitialDataProcessed = {false};

        this.maintenanceListener = ds.listenForMaintenanceRequests(this.ownerId, (snapshots, error) -> {
            if (error != null) {
                System.err.println("Listen failed: " + error);
                return;
            }

            if (snapshots != null) {
                Platform.runLater(() -> {
                    if (!isInitialDataProcessed[0]) {
                        alertsContainer.getChildren().clear();
                        List<MaintenanceRequest> activeRequests = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : snapshots.getDocuments()) {
                            MaintenanceRequest request = doc.toObject(MaintenanceRequest.class);
                            if (!"Completed".equalsIgnoreCase(request.getStatus())) {
                                request.setDocumentId(doc.getId());
                                activeRequests.add(request);
                            }
                        }

                        if (activeRequests.isEmpty()) {
                            Label placeholder = new Label("No new notifications.");
                            placeholder.setId("placeholder-notification");
                            alertsContainer.getChildren().add(placeholder);
                        } else {
                            for (MaintenanceRequest request : activeRequests) {
                                Node newNotification = createAlert("⚠", "#FEE2E2", "#B91C1C", 
                                    "New Maintenance Request", 
                                    "From: " + request.getTenantName() + " for Flat " + request.getFlatNo(),
                                    request.getDocumentId());
                                alertsContainer.getChildren().add(newNotification);
                            }
                        }
                        isInitialDataProcessed[0] = true; 
                    } 
                    else {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            MaintenanceRequest request = dc.getDocument().toObject(MaintenanceRequest.class);
                            request.setDocumentId(dc.getDocument().getId());

                            switch (dc.getType()) {
                                case ADDED:
                                    Node newNotification = createAlert("⚠", "#FEE2E2", "#B91C1C", 
                                        "New Maintenance Request", 
                                        "From: " + request.getTenantName() + " for Flat " + request.getFlatNo(),
                                        request.getDocumentId());
                                    
                                    alertsContainer.getChildren().removeIf(node -> "placeholder-notification".equals(node.getId()));
                                    alertsContainer.getChildren().add(0, newNotification);
                                    break;
                                case MODIFIED:
                                    if ("Completed".equalsIgnoreCase(request.getStatus())) {
                                        alertsContainer.getChildren().removeIf(node -> request.getDocumentId().equals(node.getId()));
                                    }
                                    break;
                                case REMOVED:
                                    alertsContainer.getChildren().removeIf(node -> request.getDocumentId().equals(node.getId()));
                                    break;
                            }
                        }
                    }
                });
            }
        });
    }

    private void setupPaymentListener() {
        if (paymentListener != null) {
            paymentListener.remove();
        }
        
        this.paymentListener = ds.listenForPayments(this.ownerId, (snapshots, error) -> {
            if (error != null) {
                System.err.println("Payment listen failed: " + error);
                return;
            }

            if (snapshots != null) {
                for (DocumentChange dc : snapshots.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        PaymentData payment = dc.getDocument().toObject(PaymentData.class);
                        Platform.runLater(() -> {
                            Node newNotification = createAlert("💰", "#DBEAFE", "#1E40AF", 
                                "Rent Payment Received", 
                                "From: " + payment.getTenantName() + " - Rs. " + String.format("%,.0f", payment.getRentAmount()));
                            
                            alertsContainer.getChildren().removeIf(node -> "placeholder-notification".equals(node.getId()));
                            alertsContainer.getChildren().add(0, newNotification);
                        });
                    }
                }
            }
        });
    }

    private void loadOwnerName() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return ds.getOwnerData("OwnerProfile", this.ownerId);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(profileSnapshot -> {
            if (profileSnapshot != null && profileSnapshot.exists() && profileSnapshot.getString("name") != null && !profileSnapshot.getString("name").isEmpty()) {
                String name = profileSnapshot.getString("name");
                Platform.runLater(() -> {
                    ownerName.setText(name);
                    if (welcomeTitle != null) {
                        welcomeTitle.setText("Welcome, " + name);
                    }
                });
            } else {
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return ds.getOwnerData("users", this.ownerId);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }).thenAccept(userSnapshot -> {
                    Platform.runLater(() -> {
                        if (userSnapshot != null && userSnapshot.exists()) {
                            String name = userSnapshot.getString("firstName") + " " + userSnapshot.getString("lastName");
                            ownerName.setText(name);
                            if (welcomeTitle != null) {
                                welcomeTitle.setText("Welcome, " + name);
                            }
                        } else {
                            ownerName.setText(this.ownerId);
                             if (welcomeTitle != null) {
                                 welcomeTitle.setText("Welcome");
                             }
                        }
                    });
                });
            }
        });
    }


    private void loadOwnerFlats() {
        if (flatsListContainer == null) return;

        flatsListContainer.getChildren().clear();
        flatsListContainer.getChildren().add(new Label("Loading properties..."));

        CompletableFuture.runAsync(() -> {
            try {
                List<QueryDocumentSnapshot> documents = ds.getFlatsByOwner(this.ownerId);

                Platform.runLater(() -> {
                    flatsListContainer.getChildren().clear();
                    if (documents.isEmpty()) {
                        flatsListContainer.getChildren().add(new Label("No properties found. Click 'Add New Flat' to get started."));
                    } else {
                        int propertyCounter = 1;
                        for (QueryDocumentSnapshot doc : documents) {
                            Flat flat = doc.toObject(Flat.class);
                            if (flat != null) {
                                flat.setFlatId(doc.getId());
                                Node flatNode = createFlatEntry(flat, propertyCounter++);
                                flatsListContainer.getChildren().add(flatNode);
                            }
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    flatsListContainer.getChildren().clear();
                    flatsListContainer.getChildren().add(new Label("Error: Could not load properties."));
                });
            }
        });
    }

    private Node createProfileHeader() {
        ImageView profileImageView = new ImageView();
        try {
            profileImageView.setImage(new Image("Assets/Images/profile.jpeg"));
        } catch (Exception e) {
            System.err.println("Could not load profile.jpeg. Using a placeholder. Error: " + e.getMessage());
            profileImageView.setImage(new Image("https://i.imgur.com/S12F2sC.png"));
        }

        profileImageView.setFitWidth(36);
        profileImageView.setFitHeight(36);
        profileImageView.setClip(new Circle(18, 18, 18));

        ownerName = new Label("Loading...");
        ownerName.setFont(Font.font("System", FontWeight.SEMI_BOLD, 15));

        HBox profileBox = new HBox(10, profileImageView, ownerName);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.setCursor(Cursor.HAND);

        profileBox.setOnMouseClicked(e -> {
            sidebar.highlight("");
            OwnerProfilePage profilePage = new OwnerProfilePage();
            Consumer<String> onNameUpdateAction = newName -> {
                ownerName.setText(newName);
                if (welcomeTitle != null) {
                    welcomeTitle.setText("Welcome, " + newName);
                }
            };
            Runnable onBackAction = () -> {
                sidebar.highlight("Owner Dashboard");
                contentWrapper.setCenter(dashboardView);
                loadOwnerName();
            };
            Node profileView = profilePage.getView(onBackAction, this.ownerId, onNameUpdateAction);
            contentWrapper.setCenter(profileView);
        });

        HBox headerBar = new HBox(profileBox);
        headerBar.setAlignment(Pos.CENTER_RIGHT);
        headerBar.setPadding(new Insets(15, 40, 15, 40));
        headerBar.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        return headerBar;
    }

    private void setupNavigation() {
        sidebar.getMenuButtons()[0].setOnAction(e -> {
            sidebar.highlight("Owner Dashboard");
            contentWrapper.setCenter(this.dashboardView);
            loadOwnerFlats();
        });

        sidebar.getMenuButtons()[1].setOnAction(e -> {
            sidebar.highlight("Tenant Management");
            TenantManagement tenantPage = new TenantManagement(this.ds, this.ownerId);
            Node tenantView = tenantPage.getView(() -> {
                sidebar.highlight("Owner Dashboard");
                contentWrapper.setCenter(this.dashboardView);
            });
            contentWrapper.setCenter(tenantView);
        });

        sidebar.getMenuButtons()[2].setOnAction(e -> {
            // CORRECTED: The string now matches the sidebar item text.
            sidebar.highlight("Rent History");
            RentManagement rentPage = new RentManagement(this.ownerId);
            contentWrapper.setCenter(rentPage.getView());
        });

        sidebar.getMenuButtons()[3].setOnAction(e -> {
            sidebar.highlight("Maintenance");
            MaintananceRequest maintenancePage = new MaintananceRequest(this.ownerId);
            contentWrapper.setCenter(maintenancePage.getView());
        });

        sidebar.getMenuButtons()[4].setOnAction(e -> {
            if (maintenanceListener != null) {
                maintenanceListener.remove();
            }
            if (paymentListener != null) {
                paymentListener.remove();
            }
            System.out.println("Logging out... Navigating to Landing Page.");
            LandingPage landingPage = new LandingPage();
            try {
                landingPage.start(primaryStage);
            } catch (Exception ex) {
                System.err.println("Error returning to landing page.");
                ex.printStackTrace();
            }
        });
    }

    private Node createDashboardView() {
        welcomeTitle = new Text("Welcome");
        welcomeTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        welcomeTitle.setFill(Color.web("#111827"));

        Text welcomeSubtitle = new Text("Here is the latest summary of your properties.");
        welcomeSubtitle.setFont(Font.font("System", 15));
        welcomeSubtitle.setFill(Color.web("#6B7280"));
        VBox welcomeHeader = new VBox(5, welcomeTitle, welcomeSubtitle);

        Node aiChatButton = createAIChatButton();

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);
        HBox topHeader = new HBox(welcomeHeader, topSpacer, aiChatButton);
        topHeader.setAlignment(Pos.TOP_LEFT);

        Label propertiesTitle = new Label("Property Overview");
        propertiesTitle.setFont(Font.font("System", FontWeight.BOLD, 22));

        HBox addFlatCard = new HBox(10);
        addFlatCard.setAlignment(Pos.CENTER_LEFT);
        addFlatCard.setPadding(new Insets(8, 15, 8, 15));
        addFlatCard.setStyle("-fx-background-color: #4338CA; -fx-background-radius: 8;");
        addFlatCard.setCursor(Cursor.HAND);
        addFlatCard.setOnMouseClicked(e -> {
            sidebar.highlight("");
            FlatDetails flatDetailsPage = new FlatDetails(this.ds, this.ownerId);
            Node flatDetailsView = flatDetailsPage.getView(() -> {
                sidebar.highlight("Owner Dashboard");
                contentWrapper.setCenter(this.dashboardView);
                loadOwnerFlats();
            });
            contentWrapper.setCenter(flatDetailsView);
        });

        Label plusSign = new Label("+");
        plusSign.setFont(Font.font("System", FontWeight.BOLD, 16));
        plusSign.setTextFill(Color.WHITE);
        Label addFlatLabel = new Label("Add New Flat");
        addFlatLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        addFlatLabel.setTextFill(Color.WHITE);
        addFlatCard.getChildren().addAll(plusSign, addFlatLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox propertiesHeader = new HBox(propertiesTitle, spacer, addFlatCard);
        propertiesHeader.setAlignment(Pos.CENTER_LEFT);

        this.flatsListContainer = new VBox(20);
        flatsListContainer.setPadding(new Insets(10));

        ScrollPane scrollableFlats = new ScrollPane(flatsListContainer);
        scrollableFlats.setFitToWidth(true);
        VBox.setVgrow(scrollableFlats, Priority.ALWAYS);

        VBox propertiesCard = new VBox(20, propertiesHeader, scrollableFlats);
        propertiesCard.setPadding(new Insets(20));
        propertiesCard.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-width: 1;");
        VBox.setVgrow(propertiesCard, Priority.ALWAYS);
        HBox.setHgrow(propertiesCard, Priority.ALWAYS);

        VBox maintenanceBox = new VBox(15);
        maintenanceBox.setPadding(new Insets(14));
        maintenanceBox.setStyle("-fx-background-color: #ffffff; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-background-radius: 12;");

        Label maintenanceTitle = new Label("Recent Notifications");
        maintenanceTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        maintenanceTitle.setTextFill(Color.web("#111827"));

        Label maintenanceDesc = new Label("Important alerts and reminders.");
        maintenanceDesc.setFont(Font.font("System", 14));
        maintenanceDesc.setTextFill(Color.web("#6B7280"));
        VBox.setMargin(maintenanceDesc, new Insets(0, 0, 10, 0));

        alertsContainer = new VBox(20);
        alertsContainer.setPadding(new Insets(10));
        Label placeholder = new Label("No new notifications.");
        placeholder.setId("placeholder-notification");
        alertsContainer.getChildren().add(placeholder);
        
        ScrollPane notificationScrollPane = new ScrollPane(alertsContainer);
        notificationScrollPane.setFitToWidth(true);
        notificationScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        notificationScrollPane.setPrefHeight(200); 
        
        maintenanceBox.getChildren().addAll(maintenanceTitle, maintenanceDesc, notificationScrollPane);
        VBox.setVgrow(notificationScrollPane, Priority.ALWAYS);

        VBox rightColumn = new VBox(maintenanceBox);
        rightColumn.setPrefWidth(420);

        HBox mainContentArea = new HBox(25, propertiesCard, rightColumn);

        VBox dashboardLayout = new VBox(25, topHeader, mainContentArea);
        dashboardLayout.setPadding(new Insets(20, 40, 40, 40));
        dashboardLayout.setStyle("-fx-background-color: #F9FAFB;");

        return dashboardLayout;
    }

    private Node createAIChatButton() {
        StackPane aiChatButton = new StackPane();
        aiChatButton.setCursor(Cursor.HAND);

        Circle background = new Circle(35, Color.web("#4F46E5"));
        Label icon = new Label("🤖");
        icon.setFont(Font.font(30));

        aiChatButton.getChildren().addAll(background, icon);

        Tooltip.install(aiChatButton, new Tooltip("Chat with AI Assistant"));

        aiChatButton.setOnMouseClicked(e -> {
            sidebar.highlight("");
            AIChatPage chatPage = new AIChatPage(this.ds, this.ownerId);
            Node chatView = chatPage.getView(() -> {
                sidebar.highlight("Owner Dashboard");
                contentWrapper.setCenter(this.dashboardView);
            });
            contentWrapper.setCenter(chatView);
        });

        return aiChatButton;
    }

    private HBox createAlert(String icon, String bgColor, String iconColor, String title, String subtitle, String id) {
        HBox alert = createAlert(icon, bgColor, iconColor, title, subtitle);
        alert.setId(id);
        return alert;
    }
    
    private HBox createAlert(String icon, String bgColor, String iconColor, String title, String subtitle) {
        StackPane iconPane = new StackPane();
        Circle iconBg = new Circle(18, Color.web(bgColor));
        Label iconTxt = new Label(icon);
        iconTxt.setFont(Font.font(16));
        iconTxt.setTextFill(Color.web(iconColor));
        iconPane.getChildren().addAll(iconBg, iconTxt);

        VBox textPane = new VBox(2);
        Label mainTxt = new Label(title);
        mainTxt.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14.5));
        mainTxt.setTextFill(Color.web("#374151"));
        Label subTxt = new Label(subtitle);
        subTxt.setFont(Font.font("System", 13));
        subTxt.setTextFill(Color.web("#6B7280"));
        textPane.getChildren().addAll(mainTxt, subTxt);

        HBox alert = new HBox(15, iconPane, textPane);
        alert.setAlignment(Pos.CENTER_LEFT);
        return alert;
    }

    private Node createFlatEntry(Flat flat, int propertyNumber) {
        VBox entryCard = new VBox(15);
        entryCard.setPadding(new Insets(20));
        entryCard.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-background-radius: 12;");

        Label titleLabel = new Label("Property #" + propertyNumber);
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.web("#111827"));

        ImageView imageView = new ImageView();
        try {
            String imageUrl = (flat.getImageUrl() != null && !flat.getImageUrl().isEmpty())
                    ? flat.getImageUrl()
                    : "https://i.imgur.com/S12F2sC.png";
            imageView.setImage(new Image(imageUrl, true));
        } catch (Exception e) {
            imageView.setImage(new Image("https://i.imgur.com/S12F2sC.png"));
        }

        imageView.setFitWidth(250);
        imageView.setPreserveRatio(true);

        StackPane imageContainer = new StackPane(imageView);
        imageContainer.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 8;");
        Rectangle clip = new Rectangle();
        clip.setArcWidth(16);
        clip.setArcHeight(16);
        clip.widthProperty().bind(imageContainer.widthProperty());
        clip.heightProperty().bind(imageContainer.heightProperty());
        imageContainer.setClip(clip);

        GridPane detailsGrid = new GridPane();
        detailsGrid.setVgap(8);
        detailsGrid.setHgap(10);
        detailsGrid.add(createDetailLabel("Society Name:"), 0, 0);
        detailsGrid.add(createDetailValue(flat.getSocietyName()), 1, 0);
        detailsGrid.add(createDetailLabel("Flat No:"), 0, 1);
        detailsGrid.add(createDetailValue(flat.getFlatNo()), 1, 1);
        detailsGrid.add(createDetailLabel("Address:"), 0, 2);
        detailsGrid.add(createDetailValue(flat.getAddress()), 1, 2);
        detailsGrid.add(createDetailLabel("Tenant Name:"), 0, 3);
        detailsGrid.add(createDetailValue(flat.getTenantName() != null ? flat.getTenantName() : "N/A"), 1, 3);
        detailsGrid.add(createDetailLabel("Tenant Email:"), 0, 4);
        detailsGrid.add(createDetailValue(flat.getTenantEmail() != null ? flat.getTenantEmail() : "N/A"), 1, 4);
        detailsGrid.add(createDetailLabel("Monthly Rent:"), 0, 5);
        detailsGrid.add(createDetailValue("₹" + flat.getRent()), 1, 5);

        Button removeButton = new Button("Remove Flat");
        removeButton.setFont(Font.font("System", FontWeight.BOLD, 13));
        removeButton.setCursor(Cursor.HAND);
        removeButton.setStyle("-fx-background-color: #EF4444; -fx-text-fill: white; -fx-background-radius: 6;");

        removeButton.setOnAction(e -> {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Are you sure you want to remove this property?");
            confirmationAlert.setContentText("This action cannot be undone and will permanently delete the flat's data.");

            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                ds.deleteFlat(flat.getFlatId()).whenComplete((v, ex) -> {
                    Platform.runLater(() -> {
                        if (ex != null) {
                            ex.printStackTrace();
                            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                            errorAlert.setTitle("Deletion Failed");
                            errorAlert.setHeaderText("Could not remove the property.");
                            errorAlert.setContentText("An error occurred: " + ex.getMessage());
                            errorAlert.showAndWait();
                        } else {
                            System.out.println("Successfully deleted flat: " + flat.getFlatId());
                            loadOwnerFlats();
                        }
                    });
                });
            }
        });

        Button editButton = new Button("Edit Details");
        editButton.setFont(Font.font("System", FontWeight.BOLD, 13));
        editButton.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-background-radius: 6;");

        boolean isOccupied = flat.getTenantName() != null && !flat.getTenantName().isEmpty();

        if (isOccupied) {
            editButton.setDisable(true);
            removeButton.setDisable(true);
            Tooltip.install(editButton, new Tooltip("Property is occupied. Please remove the current tenant first to edit."));
            Tooltip.install(removeButton, new Tooltip("Property is occupied. Please remove the current tenant first to remove."));
        } else {
            editButton.setCursor(Cursor.HAND);
            editButton.setOnAction(e -> {
                EditFlatPage editPage = new EditFlatPage(this.ds, this.ownerId, flat);
                Node editView = editPage.getView(() -> {
                    sidebar.highlight("Owner Dashboard");
                    contentWrapper.setCenter(this.dashboardView);
                    loadOwnerFlats();
                });
                contentWrapper.setCenter(editView);
            });
        }

        Region buttonSpacer = new Region();
        HBox.setHgrow(buttonSpacer, Priority.ALWAYS);

        HBox buttonBox = new HBox(10, removeButton, buttonSpacer, editButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        entryCard.getChildren().addAll(titleLabel, imageContainer, detailsGrid, buttonBox);
        return entryCard;
    }

    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        label.setTextFill(Color.web("#374151"));
        return label;
    }

    private Label createDetailValue(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", 14));
        label.setTextFill(Color.web("#6B7280"));
        label.setWrapText(true);
        return label;
    }
}
