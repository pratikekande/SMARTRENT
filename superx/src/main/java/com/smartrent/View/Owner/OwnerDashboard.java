package com.smartrent.View.Owner;


import com.smartrent.View.Signin;
import com.smartrent.View.Owner.Component.OSidebar;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartrent.Controller.*;
import com.smartrent.Model.Owner.Flat;


import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
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


import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;


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


        return new Scene(root, 1280, 720);
    }


    private void loadOwnerName() {
        CompletableFuture.supplyAsync(() -> {
            try {
                return ds.getOwnerData("users", this.ownerId);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(documentSnapshot -> {
            Platform.runLater(() -> {
                if (documentSnapshot != null && documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("firstName") + " " + documentSnapshot.getString("lastName");
                    ownerName.setText(name);
                } else {
                    ownerName.setText(this.ownerId);
                }
            });
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
                        for (DocumentSnapshot doc : documents) {
                            Flat flat = doc.toObject(Flat.class);
                            if (flat != null) {
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
            Consumer<String> onNameUpdateAction = newName -> ownerName.setText(newName);
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
            sidebar.highlight("Rent Management");
            RentManagement rentPage = new RentManagement();
            contentWrapper.setCenter(rentPage.getView());
        });


        sidebar.getMenuButtons()[3].setOnAction(e -> {
            sidebar.highlight("Maintenance");
            MaintananceRequest maintenancePage = new MaintananceRequest();
            contentWrapper.setCenter(maintenancePage.getView());
        });


        sidebar.getMenuButtons()[4].setOnAction(e -> {
            System.out.println("Logging out... Navigating to Signin Page.");
            Signin signinPage = new Signin();
            signinPage.setSigninStage(primaryStage);
            Runnable doNothing = () -> {};
            HBox signinRoot = signinPage.createSigninPage(doNothing);
            Scene signinScene = new Scene(signinRoot, 1280, 720);
            primaryStage.setScene(signinScene);
        });
    }


    private Node createDashboardView() {
        // --- LEFT PANEL (Main Content) ---
        Text welcomeTitle = new Text("Welcome");
        welcomeTitle.setFont(Font.font("System", FontWeight.BOLD, 28));
        welcomeTitle.setFill(Color.web("#111827"));
        Text welcomeSubtitle = new Text("Here is the latest summary of your properties.");
        welcomeSubtitle.setFont(Font.font("System", 15));
        welcomeSubtitle.setFill(Color.web("#6B7280"));
        VBox welcomeHeader = new VBox(5, welcomeTitle, welcomeSubtitle);


        Label propertiesTitle = new Label("Property Overview");
        propertiesTitle.setFont(Font.font("System", FontWeight.BOLD, 22));


        this.flatsListContainer = new VBox(20);
        flatsListContainer.setPadding(new Insets(10));


        ScrollPane scrollableFlats = new ScrollPane(flatsListContainer);
        scrollableFlats.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollableFlats.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollableFlats.setFitToWidth(true);
        VBox.setVgrow(scrollableFlats, Priority.ALWAYS);


        VBox propertiesCard = new VBox(20);
        propertiesCard.setPadding(new Insets(20));
        propertiesCard.setStyle("-fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-width: 1;");
        propertiesCard.getChildren().addAll(propertiesTitle, scrollableFlats);
        VBox.setVgrow(propertiesCard, Priority.ALWAYS);


        VBox mainContent = new VBox(25, welcomeHeader, propertiesCard);
        mainContent.setPadding(new Insets(20, 40, 40, 40));
        HBox.setHgrow(mainContent, Priority.ALWAYS);


        // --- RIGHT PANEL ---
        HBox addFlatCard = new HBox(10);
        addFlatCard.setAlignment(Pos.CENTER_LEFT);
        addFlatCard.setPadding(new Insets(12, 30, 12, 30));
        addFlatCard.setStyle("-fx-background-color: #4338CA; -fx-background-radius: 12;");
        addFlatCard.setCursor(Cursor.HAND);
        addFlatCard.setOnMouseClicked(e -> {
            sidebar.highlight("");
            FlatDetails flatDetailsPage = new FlatDetails(this.ds, this.ownerId);
            Node flatDetailsView = flatDetailsPage.getView(() -> {
                sidebar.highlight("Owner Dashboard");
                contentWrapper.setCenter(this.dashboardView);
                loadOwnerFlats(); // Reload flats after adding a new one
            });
            contentWrapper.setCenter(flatDetailsView);
        });


        Label plusSign = new Label("+");
        plusSign.setFont(Font.font("System", FontWeight.BOLD, 24));
        plusSign.setTextFill(Color.WHITE);


        Label addFlatLabel = new Label("Add New Flat");
        addFlatLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        addFlatLabel.setTextFill(Color.WHITE);


        addFlatCard.getChildren().addAll(plusSign, addFlatLabel);


        VBox upcomingRentCard = new VBox(8);
        upcomingRentCard.setPadding(new Insets(10));
        upcomingRentCard.setStyle("-fx-background-color: #F1F5F9; -fx-background-radius: 12;");
        upcomingRentCard.setAlignment(Pos.TOP_LEFT);


        Label upcomingRentTitle = new Label("Upcoming Rent");
        upcomingRentTitle.setFont(Font.font("System", FontWeight.BOLD, 20));
        upcomingRentTitle.setTextFill(Color.web("#1f2937"));


        Label rentAmountLabel = new Label("₹12,000");
        rentAmountLabel.setFont(Font.font("System", FontWeight.BOLD, 30));
        rentAmountLabel.setTextFill(Color.web("#10B981"));
        rentAmountLabel.setPadding(new Insets(5, 0, 8, 0));


        VBox rentDetails = new VBox(8);
        HBox rentRow3 = new HBox(10, new Label("Due Date:"), new Label("05 Aug 2025"));
        rentRow3.getChildren().forEach(node -> {
            if (node instanceof Label) {
                ((Label) node).setFont(Font.font("System", ((Label) node).getText().equals("Due Date:") ? FontWeight.SEMI_BOLD : FontWeight.NORMAL, 14));
                ((Label) node).setTextFill(Color.web(((Label) node).getText().equals("Due Date:") ? "#374151" : "#6b7280"));
            }
        });
        rentDetails.getChildren().addAll(rentRow3);
        upcomingRentCard.getChildren().addAll(upcomingRentTitle, rentAmountLabel, rentDetails);


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


        VBox alertsContainer = new VBox(20);
        alertsContainer.getChildren().addAll(
                createAlert("⚠", "#FEE2E2", "#B91C1C", "Urgent Approval Required", "Maintenance for Apt 203"),
                createAlert("📅", "#DBEAFE", "#1E40AF", "Rent Payment Due Soon", "Apt 301 - Due Aug 05, 2025"),
                createAlert("👤", "#FEF3C7", "#92400E", "New Tenant Application", "Review application for Apt 105")
        );
        maintenanceBox.getChildren().addAll(maintenanceTitle, maintenanceDesc, alertsContainer);


        // --- NEW: Create AI Chat Button ---
        Node aiChatButton = createAIChatButton();
        // Wrap the button in an HBox to align it to the right
        HBox chatButtonContainer = new HBox(aiChatButton);
        chatButtonContainer.setAlignment(Pos.CENTER_RIGHT);

        VBox rightPanel = new VBox(20);
        rightPanel.setPadding(new Insets(20, 40, 40, 0));
        rightPanel.setPrefWidth(420);
        // Add all right-side components, including the new chat button container
        rightPanel.getChildren().addAll(addFlatCard, upcomingRentCard, maintenanceBox, chatButtonContainer);


        HBox dashboardLayout = new HBox(mainContent, rightPanel);
        dashboardLayout.setStyle("-fx-background-color: #F9FAFB;");


        return dashboardLayout;
    }


    /**
     * Creates a button for the AI Chat feature.
     * @return A Node representing the AI Chat button.
     */
    private Node createAIChatButton() {
        StackPane aiChatButton = new StackPane();
        aiChatButton.setCursor(Cursor.HAND);

        // The purple circle background for the button
        Circle background = new Circle(35, Color.web("#4F46E5"));

        // A robot emoji as the icon
        Label icon = new Label("🤖");
        icon.setFont(Font.font(30));

        aiChatButton.getChildren().addAll(background, icon);

        Tooltip.install(aiChatButton, new Tooltip("Chat with AI Assistant"));

        // Set the action to navigate to the chat page
        aiChatButton.setOnMouseClicked(e -> {
            sidebar.highlight(""); // Deselect any active sidebar item
            
            AIChatPage chatPage = new AIChatPage();
            Node chatView = chatPage.getView(() -> {
                // Action for the 'back' button inside the chat page
                sidebar.highlight("Owner Dashboard");
                contentWrapper.setCenter(this.dashboardView);
            });
            contentWrapper.setCenter(chatView);
        });

        return aiChatButton;
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
                    : "https://i.imgur.com/S12F2sC.png"; // Fallback URL
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


        Button updateBtn = new Button("Update");
        updateBtn.setFont(Font.font("System", FontWeight.BOLD, 13));
        updateBtn.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-background-radius: 6;");
        updateBtn.setCursor(Cursor.HAND);


        HBox buttonBox = new HBox(updateBtn);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

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
