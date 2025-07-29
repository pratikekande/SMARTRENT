package com.smartrent.View;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LandingPage extends Application {

    // Scenes for all pages are managed here
    Scene landingScene, signinScene, signupScene, aboutUsScene;
    Stage landingStage; // The one and only stage for the application

    @Override
    public void start(Stage primaryStage) {
        landingStage = primaryStage;

        // --- Professional Style Definitions ---
        String FONT_FAMILY = "System";

        // Colors
        Color PRIMARY_TEXT_COLOR = Color.WHITE;
        Color SECONDARY_TEXT_COLOR = Color.web("#E0E0E0");
        Color ACCENT_COLOR_PRIMARY = Color.web("#8E44AD"); // A vibrant purple
        Color ACCENT_COLOR_SECONDARY = Color.web("#9B59B6"); // A lighter purple for hover
        String CARD_BACKGROUND = "-fx-background-color: rgba(255, 255, 255, 0.1); -fx-background-radius: 15;";
        String FOOTER_BACKGROUND = "-fx-background-color: #1A1A1A;";
        String MAIN_GRADIENT = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #2C3E50, #5B2C6F);";

        // Effects
        DropShadow cardShadow = new DropShadow(20, Color.rgb(0, 0, 0, 0.2));
        DropShadow heroImageShadow = new DropShadow(30, Color.rgb(0, 0, 0, 0.4));

        // --- UI Construction ---
        VBox mainLayout = new VBox();
        mainLayout.setStyle(MAIN_GRADIENT);
        mainLayout.setAlignment(Pos.TOP_CENTER);

        VBox contentWrapper = new VBox(80); // Increased spacing between sections
        contentWrapper.setAlignment(Pos.TOP_CENTER);
        contentWrapper.setMaxWidth(1400); // Optimal width for content
        contentWrapper.setPadding(new Insets(0, 40, 80, 40)); // Overall padding

        // --- Navigation Bar ---
        HBox navBar = new HBox();
        navBar.setPadding(new Insets(15, 25, 15, 25));
        navBar.setStyle("-fx-background-color: rgba(0, 0, 0, 0.2);"); // Semi-transparent background
        navBar.setAlignment(Pos.CENTER);

        HBox navContent = new HBox(40);
        navContent.setAlignment(Pos.CENTER);
        navContent.setMaxWidth(1400);
        HBox.setHgrow(navContent, Priority.ALWAYS);

        ImageView logimage = new ImageView("Assets//Images//logo.png");
        logimage.setFitWidth(100);
        logimage.setFitHeight(100);
        logimage.setClip(new Circle(50, 50, 50)); // Correctly centered clip for a perfect circle
        logimage.setEffect(new DropShadow(10, Color.WHITE));

        // Logo rotation animation
        logimage.setRotationAxis(Rotate.Y_AXIS);
        RotateTransition rotate = new RotateTransition(Duration.seconds(4), logimage);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rotate.play();

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox navItems = new HBox(20);
        navItems.setAlignment(Pos.CENTER);

        Button signInBtn = new Button("Sign In");

        // --- DEFINE UI SECTIONS BEFORE THEY ARE REFERENCED IN NAVBAR ---

        // Feature Section
        VBox featuresSection = new VBox(40);
        featuresSection.setAlignment(Pos.CENTER);

        // How It Works Section
        VBox howItWorks = new VBox(50);
        howItWorks.setAlignment(Pos.CENTER);
        howItWorks.setPadding(new Insets(40, 0, 40, 0));

        // Testimonials Section
        VBox testimonials = new VBox(50);
        testimonials.setAlignment(Pos.CENTER);
        testimonials.setPadding(new Insets(30));

        // Footer Section
        VBox footerWrapper = new VBox();
        footerWrapper.setStyle(FOOTER_BACKGROUND);
        footerWrapper.setAlignment(Pos.CENTER);

        // ScrollPane must be defined here to be used in the click handler
        ScrollPane scrollPane = new ScrollPane();

        // --- Navigation Links with Scroll Functionality ---
        // Inside your layout logic
        String[] navTexts = { "About Us", "Features", "How it Works", "Testimonials", "Contact" };

        for (String text : navTexts) {
            Label nav = new Label(text);
            nav.setFont(Font.font(FONT_FAMILY, FontWeight.MEDIUM, 18));
            nav.setTextFill(SECONDARY_TEXT_COLOR);
            nav.setCursor(Cursor.HAND);

            // Hover animation setup
            ScaleTransition scaleIn = new ScaleTransition(Duration.millis(200), nav);
            scaleIn.setToX(1.1);
            scaleIn.setToY(1.1);

            ScaleTransition scaleOut = new ScaleTransition(Duration.millis(200), nav);
            scaleOut.setToX(1.0);
            scaleOut.setToY(1.0);

            nav.setOnMouseEntered(e -> {
                nav.setTextFill(PRIMARY_TEXT_COLOR);
                nav.setUnderline(true);
                scaleIn.playFromStart();
            });

            nav.setOnMouseExited(e -> {
                nav.setTextFill(SECONDARY_TEXT_COLOR);
                nav.setUnderline(false);
                scaleOut.playFromStart();
            });

            // Click behavior
            if (text.equals("About Us")) {
                nav.setOnMouseClicked(event -> {
                    if (aboutUsScene == null) {
                        initializeAboutUsPage();
                    }
                    landingStage.setScene(aboutUsScene);
                });
            } else {
                Node targetNode;
                switch (text) {
                    case "Features":
                        targetNode = featuresSection;
                        break;
                    case "How it Works":
                        targetNode = howItWorks;
                        break;
                    case "Testimonials":
                        targetNode = testimonials;
                        break;
                    case "Contact":
                        targetNode = footerWrapper;
                        break;
                    default:
                        targetNode = null;
                        break;
                }

                if (targetNode != null) {
                    nav.setOnMouseClicked(event -> {
                        scrollToNode(scrollPane, mainLayout, targetNode);
                    });
                }
            }

            navItems.getChildren().add(nav);
        }
        String signInBtnStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 25;",
                ACCENT_COLOR_PRIMARY.toString().replace("0x", "#"));
        String signInBtnHoverStyle = String.format(
                "-fx-background-color: %s; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 25;",
                ACCENT_COLOR_SECONDARY.toString().replace("0x", "#"));

        signInBtn.setStyle(signInBtnStyle);
        signInBtn.setCursor(Cursor.HAND);
        signInBtn.setOnMouseEntered(e -> signInBtn.setStyle(signInBtnHoverStyle));
        signInBtn.setOnMouseExited(e -> signInBtn.setStyle(signInBtnStyle));

        signInBtn.setOnAction(event -> {
            if (signinScene == null) {
                initializeSigninPage();
            }
            landingStage.setScene(signinScene);
        });

        navItems.getChildren().add(signInBtn);
        navContent.getChildren().addAll(logimage, spacer, navItems);
        navBar.getChildren().add(navContent);

        // --- Hero Section ---
        HBox heroSection = new HBox(80);
        heroSection.setAlignment(Pos.CENTER);
        heroSection.setPadding(new Insets(80, 0, 80, 0));

        VBox heroText = new VBox(30);
        heroText.setAlignment(Pos.CENTER_LEFT);
        heroText.setMaxWidth(600);

        Label heroTitle = new Label("Smart Property. Smarter Living.");
        heroTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 56));
        heroTitle.setTextFill(PRIMARY_TEXT_COLOR);
        heroTitle.setWrapText(true);

        Label heroSubtitle = new Label(
                "Revolutionize your property management experience with SmartRent+. The all-in-one solution for modern landlords and tenants.");
        heroSubtitle.setFont(Font.font(FONT_FAMILY, FontWeight.NORMAL, 22));
        heroSubtitle.setTextFill(SECONDARY_TEXT_COLOR);
        heroSubtitle.setWrapText(true);
        heroSubtitle.setLineSpacing(5);

        Button watchDemo = new Button("Watch Demo");
        watchDemo.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        watchDemo.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-padding: 15 35; -fx-border-radius: 30;");
        watchDemo.setCursor(Cursor.HAND);
        watchDemo.setOnMouseEntered(e -> watchDemo.setStyle(
                "-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-width: 2; -fx-padding: 15 35; -fx-border-radius: 30;"));
        watchDemo.setOnMouseExited(e -> watchDemo.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-padding: 15 35; -fx-border-radius: 30;"));

        heroText.getChildren().addAll(heroTitle, heroSubtitle, watchDemo);

        // Use a try-catch block for local images to prevent crashing if not found
        ImageView heroImage = new ImageView();
        try {
            heroImage.setImage(new Image("Assets/Images/hero.jpeg"));
        } catch (IllegalArgumentException e) {
            System.err.println("Warning: Hero image not found. Using placeholder.");
            // Optional: Set a placeholder color or shape if image fails
            heroImage.setImage(new Image("https://via.placeholder.com/550x380/FFFFFF/000000?text=Image+Not+Found"));
        }
        heroImage.setFitWidth(550);
        heroImage.setPreserveRatio(true);
        heroImage.setEffect(heroImageShadow);

        heroSection.getChildren().addAll(heroText, heroImage);

        // --- POPULATE UI SECTIONS (defined earlier) ---

        // Feature Section Population
        Label featuresTitle = new Label("Everything You Need in One Place");
        featuresTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 42));
        featuresTitle.setTextFill(PRIMARY_TEXT_COLOR);

        GridPane featureGrid = new GridPane();
        featureGrid.setHgap(40);
        featureGrid.setVgap(40);
        featureGrid.setAlignment(Pos.CENTER);

        String[][] features = {
                { "Owner Dashboard", "Manage tenant and owner data from a unified dashboard." },
                { "Rent Tracking", "Track rent payments and reminders in real time." },
                { "Maintenance Hub", "Receive and resolve maintenance requests swiftly." },
                { "Smart Notifications", "Send smart alerts to tenants and owners automatically." }
        };

        for (int i = 0; i < features.length; i++) {
            VBox card = new VBox(20);
            card.setStyle(CARD_BACKGROUND);
            card.setEffect(cardShadow);
            card.setAlignment(Pos.TOP_LEFT);
            card.setPadding(new Insets(30));
            card.setPrefSize(400, 200);

            Label title = new Label(features[i][0]);
            title.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 22));
            title.setTextFill(PRIMARY_TEXT_COLOR);

            Label desc = new Label(features[i][1]);
            desc.setWrapText(true);
            desc.setFont(Font.font(FONT_FAMILY, 16));
            desc.setTextFill(SECONDARY_TEXT_COLOR);

            card.getChildren().addAll(title, desc);
            featureGrid.add(card, i % 2, i / 2);
        }
        featuresSection.getChildren().addAll(featuresTitle, featureGrid);

        // How It Works Section Population
        Label howTitle = new Label("How It Works");
        howTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 42));
        howTitle.setTextFill(PRIMARY_TEXT_COLOR);

        HBox steps = new HBox(80);
        steps.setAlignment(Pos.CENTER);
        String[] stepTexts = { "1. Sign Up", "2. Configure", "3. Manage" };

        for (String step : stepTexts) {
            VBox stepBox = new VBox(20);
            stepBox.setAlignment(Pos.CENTER);

            Label stepLabel = new Label(step);
            stepLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
            stepLabel.setTextFill(PRIMARY_TEXT_COLOR);

            Label stepDesc = new Label("Quickly create your account to get started.");
            if (step.contains("Configure"))
                stepDesc.setText("Add your properties and tenant details.");
            if (step.contains("Manage"))
                stepDesc.setText("Enjoy streamlined property management.");
            stepDesc.setFont(Font.font(FONT_FAMILY, 16));
            stepDesc.setTextFill(SECONDARY_TEXT_COLOR);

            stepBox.getChildren().addAll(stepLabel, stepDesc);
            steps.getChildren().add(stepBox);
        }
        howItWorks.getChildren().addAll(howTitle, steps);

        // Testimonials Section Population
        Label testiTitle = new Label("What Our Users Say");
        testiTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 42));
        testiTitle.setTextFill(PRIMARY_TEXT_COLOR);

        HBox testiCards = new HBox(40);
        testiCards.setAlignment(Pos.CENTER);

        String[][] testiTexts = {
                { "Sashi Sir",
                        "\"SmartRent+ made managing my properties a breeze! The dashboard is intuitive and saves me hours every week.\"" },
                { "Sachin Sir",
                        "\"My tenants love the instant notifications for rent reminders and announcements. It has improved communication tenfold.\"" },
                { "Pramod Sir",
                        "\"I track all my rent collections in one place now. No more messy spreadsheets. It's truly a game-changer for landlords.\"" }
        };

        for (String[] entry : testiTexts) {
            VBox card = new VBox(20);
            card.setPadding(new Insets(30));
            card.setStyle(CARD_BACKGROUND);
            card.setEffect(cardShadow);
            card.setAlignment(Pos.TOP_LEFT);
            card.setPrefWidth(350);
            card.setMinHeight(250);

            HBox authorInfo = new HBox(15);
            authorInfo.setAlignment(Pos.CENTER_LEFT);

            ImageView avatar = new ImageView(new Image("Assets/Images/image1.jpeg"));
            avatar.setFitWidth(60);
            avatar.setFitHeight(60);
            avatar.setClip(new Circle(30, 30, 30));

            Label name = new Label(entry[0]);
            name.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
            name.setTextFill(PRIMARY_TEXT_COLOR);
            authorInfo.getChildren().addAll(avatar, name);

            Label quote = new Label(entry[1]);
            quote.setWrapText(true);
            quote.setFont(Font.font(FONT_FAMILY, 16));
            quote.setTextFill(SECONDARY_TEXT_COLOR);
            quote.setLineSpacing(4);

            card.getChildren().addAll(authorInfo, quote);
            testiCards.getChildren().add(card);
        }

        testimonials.getChildren().addAll(testiTitle, testiCards);

        // Call to Action (CTA) Section
        VBox ctaSection = new VBox(30);
        ctaSection.setPadding(new Insets(80));
        ctaSection.setAlignment(Pos.CENTER);
        ctaSection.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 20;",
                ACCENT_COLOR_PRIMARY.toString().replace("0x", "#")));
        ctaSection.setEffect(cardShadow);

        Label ctaLabel = new Label("Ready to Get Started?");
        ctaLabel.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 42));
        ctaLabel.setTextFill(Color.WHITE);

        Button ctaButton = new Button("Create Your Free Account");
        ctaButton.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        ctaButton.setStyle(
                "-fx-background-color: white; -fx-text-fill: #333; -fx-padding: 20 40; -fx-background-radius: 30;");
        ctaButton.setCursor(Cursor.HAND);
        ctaButton.setOnMouseEntered(e -> ctaButton.setStyle(
                "-fx-background-color: #f0f0f0; -fx-text-fill: #333; -fx-padding: 20 40; -fx-background-radius: 30;"));
        ctaButton.setOnMouseExited(e -> ctaButton.setStyle(
                "-fx-background-color: white; -fx-text-fill: #333; -fx-padding: 20 40; -fx-background-radius: 30;"));

        ctaSection.getChildren().addAll(ctaLabel, ctaButton);

        // --- Footer Section Population ---
        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(60, 40, 60, 40));
        footer.setMaxWidth(1400);

        VBox left = new VBox(20);
        left.setPrefWidth(400);
        Label leftLogo = new Label("SmartRent+");
        leftLogo.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 24));
        leftLogo.setTextFill(PRIMARY_TEXT_COLOR);

        Label leftDesc = new Label(
                "Smart property management for the modern world. Connecting property owners and tenants through intelligent technology.");
        leftDesc.setWrapText(true);
        leftDesc.setFont(Font.font(FONT_FAMILY, 16));
        leftDesc.setTextFill(SECONDARY_TEXT_COLOR);

        // Create email contact info
        ImageView mailIcon = new ImageView(
                new Image("https://cdn-icons-png.flaticon.com/512/732/732200.png", 18, 18, true, true));
        Label emailLabel = new Label("smartrent2025@gmail.com");
        emailLabel.setFont(Font.font(FONT_FAMILY, 16));
        emailLabel.setTextFill(SECONDARY_TEXT_COLOR);
        HBox emailBox = new HBox(10);
        emailBox.setAlignment(Pos.CENTER_LEFT);
        emailBox.getChildren().addAll(mailIcon, emailLabel);

        HBox socialIcons = new HBox(20);
        ImageView fb = new ImageView(
                new Image("https://cdn-icons-png.flaticon.com/512/733/733547.png", 24, 24, true, true));
        ImageView tw = new ImageView(
                new Image("https://cdn-icons-png.flaticon.com/512/733/733579.png", 24, 24, true, true));
        ImageView ig = new ImageView(
                new Image("https://cdn-icons-png.flaticon.com/512/733/733558.png", 24, 24, true, true));
        socialIcons.getChildren().addAll(fb, tw, ig);
        socialIcons.getChildren().forEach(node -> {
            node.setCursor(Cursor.HAND);
            node.setOnMouseEntered(e -> node.setOpacity(0.7));
            node.setOnMouseExited(e -> node.setOpacity(1.0));
        });

        // Add the new email box to the left column of the footer
        left.getChildren().addAll(leftLogo, leftDesc, emailBox, socialIcons);

        Region footerSpacer1 = new Region();
        HBox.setHgrow(footerSpacer1, Priority.ALWAYS);

        VBox center = new VBox(15);
        Label company = new Label("Company");
        company.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        company.setTextFill(PRIMARY_TEXT_COLOR);
        center.getChildren().add(company);
        for (String link : new String[] { "About", "Careers", "Blog", "Press" }) {
            Label l = new Label(link);
            l.setFont(Font.font(FONT_FAMILY, 16));
            l.setTextFill(SECONDARY_TEXT_COLOR);
            l.setCursor(Cursor.HAND);
            l.setOnMouseEntered(e -> l.setTextFill(ACCENT_COLOR_SECONDARY));
            l.setOnMouseExited(e -> l.setTextFill(SECONDARY_TEXT_COLOR));
            center.getChildren().add(l);
        }

        VBox right = new VBox(15);
        Label support = new Label("Support");
        support.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 18));
        support.setTextFill(PRIMARY_TEXT_COLOR);
        right.getChildren().add(support);
        for (String link : new String[] { "Contact", "Help Center", "Terms", "Privacy" }) {
            Label l = new Label(link);
            l.setFont(Font.font(FONT_FAMILY, 16));
            l.setTextFill(SECONDARY_TEXT_COLOR);
            l.setCursor(Cursor.HAND);
            l.setOnMouseEntered(e -> l.setTextFill(ACCENT_COLOR_SECONDARY));
            l.setOnMouseExited(e -> l.setTextFill(SECONDARY_TEXT_COLOR));
            right.getChildren().add(l);
        }

        Region footerSpacer2 = new Region();
        HBox.setHgrow(footerSpacer2, Priority.ALWAYS);

        footer.getChildren().addAll(left, footerSpacer1, center, footerSpacer2, right);
        footerWrapper.getChildren().add(footer);

        // --- Final Assembly ---
        contentWrapper.getChildren().addAll(heroSection, featuresSection, howItWorks, testimonials, ctaSection);
        mainLayout.getChildren().addAll(navBar, contentWrapper, footerWrapper);

        scrollPane.setContent(mainLayout); // Set content for ScrollPane
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: #2C3E50;");

        // Standardized scene size
        landingScene = new Scene(scrollPane, 1280, 720);

        landingStage.setScene(landingScene);
        landingStage.setTitle("SmartRent+ ");
        landingStage.getIcons().add(new Image("Assets/Images/logo.png"));

        // --- FIX ---
        // This is the crucial change. By setting resizable to false, the Stage's
        // size is locked and will not change when you switch scenes.
        landingStage.setResizable(false);

        landingStage.show();
    }

    /**
     * Helper method to smoothly scroll to a specific node within a ScrollPane.
     * * @param scrollPane  The ScrollPane to scroll.
     * @param contentPane The root content pane inside the ScrollPane.
     * @param targetNode  The node to scroll to.
     */
    private void scrollToNode(ScrollPane scrollPane, Pane contentPane, Node targetNode) {
        Platform.runLater(() -> {
            Bounds contentBounds = contentPane.getBoundsInLocal();
            Bounds targetBounds = targetNode.localToScene(targetNode.getBoundsInLocal());
            Bounds scrollPaneBounds = scrollPane.localToScene(scrollPane.getBoundsInLocal());

            double targetYinScrollPane = targetBounds.getMinY() - scrollPaneBounds.getMinY();
            double vValue = targetYinScrollPane
                    / (contentBounds.getHeight() - scrollPane.getViewportBounds().getHeight());

            // Clamp vValue to be between 0 and 1
            vValue = Math.max(0, Math.min(1, vValue));

            // Animate the scroll
            Timeline timeline = new Timeline();
            KeyValue kv = new KeyValue(scrollPane.vvalueProperty(), vValue, Interpolator.EASE_BOTH);
            KeyFrame kf = new KeyFrame(Duration.seconds(0.7), kv);
            timeline.getKeyFrames().add(kf);
            timeline.play();
        });
    }

    /**
     * Initializes the Signin page and its scene.
     */
    private void initializeSigninPage() {
        Signin signin = new Signin();
        signin.setSigninStage(landingStage);
        Parent signinRoot = signin.createSigninPage(this::handleBackToLanding);
        signinScene = new Scene(signinRoot, 1280, 720);
        signin.setSigninScene(signinScene);
    }

    /**
     * Initializes the About Us page and its scene.
     */
    private void initializeAboutUsPage() {
        AboutUsPage aboutUsPage = new AboutUsPage();
        Parent aboutUsRoot = aboutUsPage.createAboutUsPage(this::handleBackToLanding);
        aboutUsScene = new Scene(aboutUsRoot, 1280, 720);
    }

    /**
     * Handles the action to return to the main landing page.
     */
    private void handleBackToLanding() {
        landingStage.setScene(landingScene);
    }
}
