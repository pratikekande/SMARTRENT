package com.smartrent.View.Tenant;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Owner.Flat;
import com.smartrent.View.LandingPage;
import com.smartrent.View.Tenant.Component.Sidebar;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TenantDashboard {

    private Stage primaryStage;
    private Sidebar sidebar;
    private BorderPane contentWrapper;
    private VBox dashboardContent;

    private dataservice ds;
    private String tenantId;
    private Label tenantNameLabel;
    private ImageView propertyImageView;
    private Label societyNameValueLabel, flatNoValueLabel, addressValueLabel, tenantNameValueLabel, tenantEmailValueLabel, rentValueLabel;
    private Text welcomeText;

    public Scene createScene(Stage primaryStage, String tenantId) {
        this.primaryStage = primaryStage;
        this.tenantId = tenantId;
        this.ds = new dataservice();

        BorderPane root = new BorderPane();
        sidebar = new Sidebar("Tenant Dashboard");
        root.setLeft(sidebar);

        contentWrapper = new BorderPane();
        Node profileHeader = createProfileHeader();
        contentWrapper.setTop(profileHeader);
        root.setCenter(contentWrapper);

        this.welcomeText = new Text("Welcome");
        welcomeText.setFont(Font.font("System", FontWeight.BOLD, 26));
        welcomeText.setFill(Color.web("#34495E"));

        Text subtitleText = new Text("Here’s your Property.");
        subtitleText.setFont(Font.font("System", 14));
        subtitleText.setFill(Color.GRAY);

        String cardBaseStyle = "-fx-background-color: #636ae8; " +
                "-fx-border-radius: 16; " +
                "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0.2, 0, 3);";

        VBox payRentCard = createDashboardCard("Pay Rent", "Payment made Easy", cardBaseStyle);
        VBox maintenanceCard = createDashboardCard("Maintenance Request", "Raise a Complaint", cardBaseStyle);
        VBox upcomingRentCard = createDashboardCard("Upcoming Rent", "Your next rent payment is due on August 5th, 2025.", cardBaseStyle);

        HBox cardRow = new HBox(25, payRentCard, maintenanceCard, upcomingRentCard);
        HBox.setHgrow(payRentCard, Priority.ALWAYS);
        HBox.setHgrow(maintenanceCard, Priority.ALWAYS);
        HBox.setHgrow(upcomingRentCard, Priority.ALWAYS);
        cardRow.setCursor(Cursor.HAND);
        cardRow.setAlignment(Pos.CENTER);

        VBox notifBox = createNotificationsBox();

        Node propertyDetailsBox = createPropertyDetailsBox();
        HBox bottomRow = new HBox(30, propertyDetailsBox, notifBox);
        bottomRow.setAlignment(Pos.TOP_LEFT);

        dashboardContent = new VBox(25, welcomeText, subtitleText, cardRow, bottomRow);
        dashboardContent.setPadding(new Insets(20, 40, 40, 40));
        dashboardContent.setStyle("-fx-background-color: #f8fafc;");
        contentWrapper.setCenter(dashboardContent);

        setupNavigation(payRentCard, maintenanceCard);

        // Load both tenant name and flat details
        loadTenantName();
        loadTenantFlatDetails();

        Scene scene = new Scene(root, 1280, 720);
        return scene;
    }

    /**
     * Loads the tenant's name and updates the UI.
     * It updates both the header label and the main welcome text.
     */
    private void loadTenantName() {
        CompletableFuture.supplyAsync(() -> {
            try {
                // First, try to get data from 'TenantProfile'
                return ds.getTenantProfileData("TenantProfile", this.tenantId);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(profileSnapshot -> {
            if (profileSnapshot != null && profileSnapshot.exists() && profileSnapshot.getString("name") != null && !profileSnapshot.getString("name").isEmpty()) {
                String name = profileSnapshot.getString("name");
                Platform.runLater(() -> {
                    tenantNameLabel.setText(name);
                    welcomeText.setText("Welcome, " + name);
                });
            } else {
                // Fallback: If no profile, get the name from the assigned flat
                CompletableFuture.supplyAsync(() -> {
                    try {
                        List<QueryDocumentSnapshot> documents = ds.getFlatByTenant(this.tenantId);
                        if (documents != null && !documents.isEmpty()) {
                            return documents.get(0).toObject(Flat.class);
                        }
                        return null;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                }).thenAccept(flat -> {
                    Platform.runLater(() -> {
                        if (flat != null && flat.getTenantName() != null) {
                            String name = flat.getTenantName();
                            tenantNameLabel.setText(name);
                            welcomeText.setText("Welcome, " + name);
                        } else {
                            // If no data is found anywhere, just show the ID
                            tenantNameLabel.setText(this.tenantId);
                            welcomeText.setText("Welcome");
                        }
                    });
                });
            }
        });
    }


    private void loadTenantFlatDetails() {
        CompletableFuture.runAsync(() -> {
            try {
                List<QueryDocumentSnapshot> documents = ds.getFlatByTenant(this.tenantId);
                if (documents != null && !documents.isEmpty()) {
                    DocumentSnapshot doc = documents.get(0);
                    Flat flat = doc.toObject(Flat.class);
                    Platform.runLater(() -> updateFlatDetailsUI(flat));
                } else {
                    Platform.runLater(() -> showNoFlatAssigned());
                }
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> showErrorLoadingFlat());
            }
        });
    }

    private void setupNavigation(VBox payRentCard, VBox maintenanceCard) {
        sidebar.getMenuButtons()[0].setOnAction(e -> {
            sidebar.highlight("Tenant Dashboard");
            contentWrapper.setCenter(dashboardContent);
        });

        sidebar.getMenuButtons()[1].setOnAction(e -> {
            sidebar.highlight("Maintenance History");
            TenantMaintananceHistory maintPage = new TenantMaintananceHistory(this.tenantId);
            VBox view = maintPage.getView();
            contentWrapper.setCenter(view);
        });

        sidebar.getMenuButtons()[2].setOnAction(e -> {
            sidebar.highlight("Rent History");
            TenantPaymentHistory payPage = new TenantPaymentHistory(this.tenantId);
            VBox view = payPage.getView();
            contentWrapper.setCenter(view);
        });

        sidebar.getMenuButtons()[3].setOnAction(e -> {
            System.out.println("Logging out... Navigating to Landing Page.");
            LandingPage landingPage = new LandingPage();
            try {
                landingPage.start(primaryStage);
            } catch (Exception ex) {
                System.err.println("Error returning to landing page.");
                ex.printStackTrace();
            }
        });

        payRentCard.setOnMouseClicked((MouseEvent e) -> {
            sidebar.highlight("");
            Payment paymentPage = new Payment(this.tenantId);
            Pane view = paymentPage.getView(() -> {
                sidebar.highlight("Tenant Dashboard");
                contentWrapper.setCenter(dashboardContent);
            });
            contentWrapper.setCenter(view);
        });

        maintenanceCard.setOnMouseClicked((MouseEvent e) -> {
            sidebar.highlight("");
            RaiseMaintanance raisePage = new RaiseMaintanance(this.tenantId);
            Pane view = raisePage.getView(() -> {
                sidebar.highlight("Tenant Dashboard");
                contentWrapper.setCenter(dashboardContent);
            });
            contentWrapper.setCenter(view);
        });
    }

    private Node createProfileHeader() {
        ImageView profileImageView = new ImageView();
        try {
            profileImageView.setImage(new Image("Assets//Images//profile.jpeg"));
        } catch (Exception e) {
            profileImageView.setImage(new Image("https://i.imgur.com/S12F2sC.png"));
        }

        profileImageView.setFitWidth(36);
        profileImageView.setFitHeight(36);
        profileImageView.setClip(new Circle(18, 18, 18));

        tenantNameLabel = new Label("Loading...");
        tenantNameLabel.setFont(Font.font("System", FontWeight.SEMI_BOLD, 15));

        HBox profileBox = new HBox(10, profileImageView, tenantNameLabel);
        profileBox.setAlignment(Pos.CENTER);
        profileBox.setCursor(Cursor.HAND);

        profileBox.setOnMouseClicked(e -> {
            sidebar.highlight("");
            TenantProfilePage profilePage = new TenantProfilePage();

            // This action updates both the header and the welcome text immediately for a smooth UX
            Consumer<String> onNameUpdateAction = newName -> {
                tenantNameLabel.setText(newName);
                welcomeText.setText("Welcome, " + newName);
            };

            // This action runs when returning from the profile page
            Runnable onBackAction = () -> {
                sidebar.highlight("Tenant Dashboard");
                contentWrapper.setCenter(dashboardContent);
                // Reload the name from Firestore to ensure it's the latest version
                loadTenantName();
            };

            Node profileView = profilePage.getView(onBackAction, this.tenantId, onNameUpdateAction);

            contentWrapper.setCenter(profileView);
        });

        HBox headerBar = new HBox(profileBox);
        headerBar.setAlignment(Pos.CENTER_RIGHT);
        headerBar.setPadding(new Insets(10, 40, 10, 40));
        headerBar.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-width: 0 0 1 0;");

        return headerBar;
    }

    private VBox createDashboardCard(String titleText, String style) {
        return createDashboardCard(titleText, null, style);
    }

    private VBox createDashboardCard(String titleText, String descText, String style) {
        Text title = new Text(titleText);
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setFill(Color.WHITE);

        VBox card = new VBox(10, title);
        if (descText != null && !descText.isEmpty()) {
            Text desc = new Text(descText);
            desc.setFont(Font.font("System", 13));
            desc.setFill(Color.web("#D1D5DB"));
            desc.setWrappingWidth(220);
            card.getChildren().add(desc);
        }

        card.setPadding(new Insets(14));
        card.setStyle(style);
        card.setCursor(Cursor.HAND);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private VBox createNotificationsBox() {
        Label notifTitle = new Label("Recent Notifications");
        notifTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        notifTitle.setTextFill(Color.web("#111827"));
        VBox notificationsContainer = new VBox();
        notificationsContainer.getChildren().addAll(
                createNotificationItem("👤", "#3B82F6", "New Community Poll", "1 day ago"),
                new Separator(),
                createNotificationItem("📦", "#8B5CF6", "Package Delivered", "2 days ago")
        );

        VBox notifBox = new VBox(15, notifTitle, notificationsContainer);
        notifBox.setPadding(new Insets(25));
        notifBox.setPrefWidth(350);
        notifBox.setStyle(
                "-fx-background-color: #ffffff; " +
                        "-fx-background-radius: 12; " +
                        "-fx-border-color: #E5E7EB; " +
                        "-fx-border-width: 1;"
        );
        return notifBox;
    }

    private Node createNotificationItem(String iconChar, String iconColorHex, String titleText, String timeText) {
        StackPane iconPane = new StackPane();
        Circle iconBackground = new Circle(20, Color.web(iconColorHex));
        iconBackground.setOpacity(0.12);
        Label iconLabel = new Label(iconChar);
        iconLabel.setFont(Font.font("System", 16));
        iconPane.getChildren().addAll(iconBackground, iconLabel);


        Label title = new Label(titleText);
        title.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        title.setTextFill(Color.web("#374151"));


        Label time = new Label(timeText);
        time.setFont(Font.font("System", 12));
        time.setTextFill(Color.web("#6B7280"));
        VBox textPane = new VBox(4, title, time);


        HBox notificationRow = new HBox(15, iconPane, textPane);
        notificationRow.setAlignment(Pos.CENTER_LEFT);
        notificationRow.setPadding(new Insets(12, 0, 12, 0));


        return notificationRow;
    }

    private Node createPropertyDetailsBox() {
        VBox propertyBox = new VBox();
        propertyBox.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12; -fx-border-color: #E5E7EB; -fx-border-width: 1;");
        propertyBox.setPrefHeight(450);
        HBox.setHgrow(propertyBox, Priority.ALWAYS);

        VBox scrollableContent = new VBox(15);
        scrollableContent.setPadding(new Insets(20));

        propertyImageView = new ImageView();
        propertyImageView.setFitHeight(250);
        propertyImageView.setPreserveRatio(true);

        StackPane imageContainer = new StackPane(propertyImageView);
        imageContainer.setPrefHeight(250);
        imageContainer.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 8;");
        imageContainer.setClip(new Rectangle(imageContainer.getPrefWidth(), imageContainer.getPrefHeight()) {
            {
                setArcWidth(16);
                setArcHeight(16);
                widthProperty().bind(imageContainer.widthProperty());
                heightProperty().bind(imageContainer.heightProperty());
            }
        });

        Label detailsTitle = new Label("Property Details");
        detailsTitle.setFont(Font.font("System", FontWeight.BOLD, 18));
        detailsTitle.setTextFill(Color.web("#111827"));
        VBox.setMargin(detailsTitle, new Insets(10, 0, 5, 0));

        GridPane detailsGrid = new GridPane();
        detailsGrid.setVgap(12);
        detailsGrid.setHgap(10);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPrefWidth(120);
        detailsGrid.getColumnConstraints().addAll(col1);

        societyNameValueLabel = createDetailValue("Loading...");
        flatNoValueLabel = createDetailValue("Loading...");
        addressValueLabel = createDetailValue("Loading...");
        tenantNameValueLabel = createDetailValue("Loading...");
        tenantEmailValueLabel = createDetailValue("Loading...");
        rentValueLabel = createDetailValue("Loading...");

        detailsGrid.add(createDetailLabel("Society Name:"), 0, 0);
        detailsGrid.add(societyNameValueLabel, 1, 0);
        detailsGrid.add(createDetailLabel("Flat No:"), 0, 1);
        detailsGrid.add(flatNoValueLabel, 1, 1);
        detailsGrid.add(createDetailLabel("Address:"), 0, 2);
        detailsGrid.add(addressValueLabel, 1, 2);
        detailsGrid.add(createDetailLabel("Tenant Name:"), 0, 3);
        detailsGrid.add(tenantNameValueLabel, 1, 3);
        detailsGrid.add(createDetailLabel("Tenant Email:"), 0, 4);
        detailsGrid.add(tenantEmailValueLabel, 1, 4);
        detailsGrid.add(createDetailLabel("Monthly Rent:"), 0, 5);
        detailsGrid.add(rentValueLabel, 1, 5);

        scrollableContent.getChildren().addAll(imageContainer, detailsTitle, detailsGrid);

        ScrollPane scrollPane = new ScrollPane(scrollableContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        propertyBox.getChildren().add(scrollPane);

        return propertyBox;
    }

    private Label createDetailLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 14));
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

    private void updateFlatDetailsUI(Flat flat) {
        if (flat == null) return;

        // Note: The main tenantNameLabel in the header is updated by loadTenantName()
        societyNameValueLabel.setText(flat.getSocietyName());
        flatNoValueLabel.setText(flat.getFlatNo());
        addressValueLabel.setText(flat.getAddress());
        tenantNameValueLabel.setText(flat.getTenantName());
        tenantEmailValueLabel.setText(flat.getTenantEmail());
        rentValueLabel.setText("₹" + flat.getRent());

        try {
            if (flat.getImageUrl() != null && !flat.getImageUrl().isEmpty()) {
                propertyImageView.setImage(new Image(flat.getImageUrl()));
            } else {
                propertyImageView.setImage(new Image("https://i.imgur.com/W2036eG.jpg"));
            }
        } catch (Exception e) {
            System.err.println("Failed to load flat image, using fallback.");
            propertyImageView.setImage(new Image("https://i.imgur.com/W2036eG.jpg"));
        }
    }

    private void showNoFlatAssigned() {
        tenantNameLabel.setText(this.tenantId);
        societyNameValueLabel.setText("N/A");
        flatNoValueLabel.setText("N/A");
        addressValueLabel.setText("No flat has been assigned to you.");
        tenantNameValueLabel.setText(this.tenantId);
        tenantEmailValueLabel.setText("N/A");
        rentValueLabel.setText("N/A");
    }

    private void showErrorLoadingFlat() {
        tenantNameLabel.setText("Error");
        societyNameValueLabel.setText("Error");
        flatNoValueLabel.setText("Error");
        addressValueLabel.setText("Could not load property details.");
        tenantNameValueLabel.setText("Error");
        tenantEmailValueLabel.setText("Error");
        rentValueLabel.setText("Error");
    }
}
