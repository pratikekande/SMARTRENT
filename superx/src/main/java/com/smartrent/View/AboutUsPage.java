package com.smartrent.View;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;

public class AboutUsPage {

    /**
     * Creates and returns the root pane for the redesigned About Us page.
     * @param backAction The action to execute when the "Back" button is clicked.
     * @return A Parent node representing the About Us page.
     */
    public Parent createAboutUsPage(Runnable backAction) {
        // --- Professional Style Definitions ---
        String FONT_FAMILY = "System";
        Color PRIMARY_TEXT_COLOR = Color.WHITE;
        String MAIN_GRADIENT = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #2C3E50, #4A235A);";

        // --- Main Layout ---
        VBox pageContent = new VBox(55); // Adjusted spacing for distinct sections
        pageContent.setStyle(MAIN_GRADIENT);
        pageContent.setAlignment(Pos.TOP_CENTER);
        pageContent.setPadding(new Insets(20, 60, 60, 60));

        // --- Back Button ---
        Button backButton = new Button("← Back to Home");
        backButton.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        backButton.setTextFill(PRIMARY_TEXT_COLOR);
        backButton.setStyle("-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 20; -fx-padding: 8 16;");
        backButton.setCursor(Cursor.HAND);
        backButton.setOnMouseEntered(e -> backButton.setStyle("-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 20; -fx-padding: 8 16;"));
        backButton.setOnMouseExited(e -> backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 20; -fx-padding: 8 16;"));
        backButton.setOnAction(e -> backAction.run());

        HBox header = new HBox(backButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 0, 10, 0));


        // --- Core2Web Acknowledgement Section ---
        VBox c2wAcknowledgementSection = new VBox(35);
        c2wAcknowledgementSection.setAlignment(Pos.CENTER);

        // Logo and Title
        HBox c2wHeader = new HBox(20);
        c2wHeader.setAlignment(Pos.CENTER);
        Node c2wLogo = createImageView("Assets/Images/c2w.jpeg", 80); // Ensure this path is correct for your c2w logo

        Label c2wTitle = new Label("Core2Web Technologies");
        c2wTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 48));
        c2wTitle.setTextFill(PRIMARY_TEXT_COLOR);
        c2wTitle.setEffect(new DropShadow(15, Color.BLACK));
        c2wHeader.getChildren().addAll(c2wLogo, c2wTitle);

        // Acknowledgement Text
        Label acknowledgementText = new Label("We extend our deepest gratitude to our pillars of support at Core2Web. Their invaluable guidance and unwavering encouragement were instrumental in the success of this project.");
        acknowledgementText.setFont(Font.font("System", FontWeight.NORMAL, 18));
        acknowledgementText.setTextFill(Color.web("#E0E0E0"));
        acknowledgementText.setWrapText(true);
        acknowledgementText.setTextAlignment(TextAlignment.CENTER);
        acknowledgementText.setMaxWidth(800);

        c2wAcknowledgementSection.getChildren().addAll(c2wHeader, acknowledgementText);


        // --- Our Pillars of Support Section ---
        VBox pillarsOfSupportSection = new VBox(35);
        pillarsOfSupportSection.setAlignment(Pos.CENTER);
        Label pillarsTitle = createSectionTitle("Our Pillars of Support");

        // SHASHI BAGAL - Centered Card WITH Image
        // Ensure you have a 'shashi_bagal.jpeg' in Assets/Images
        Node shashiCard = createMemberCard("Assets/Images/shashi_bagal.jpeg", "SHASHI BAGAL", "Sir", true); // true for image
        HBox shashiContainer = new HBox(shashiCard); // Use HBox to center the single card
        shashiContainer.setAlignment(Pos.CENTER);


        // Other three Sirs - Arranged horizontally below Shashi Sir, WITHOUT images and WITHOUT role
        HBox otherSirsContainer = new HBox(40); // Spacing between cards
        otherSirsContainer.setAlignment(Pos.CENTER);
        otherSirsContainer.getChildren().addAll(
            createMemberCard(null, "Sachin Sir", "", false), // "" for NO role, false for NO image
            createMemberCard(null, "Pramod Sir", "", false), // "" for NO role, false for NO image
            createMemberCard(null, "Akshay Sir", "", false)  // "" for NO role, false for NO image
        );

        pillarsOfSupportSection.getChildren().addAll(pillarsTitle, shashiContainer, otherSirsContainer);


        // --- Development Team Section ---
        VBox teamSection = new VBox(35);
        teamSection.setAlignment(Pos.CENTER);
        Label teamTitle = createSectionTitle("Meet the Developers");

        GridPane teamGrid = new GridPane();
        teamGrid.setAlignment(Pos.CENTER);
        teamGrid.setHgap(40);
        teamGrid.setVgap(40);

        // Developers - WITHOUT images, only name and role
        teamGrid.add(createMemberCard(null, "Pratik Ekande", "Developer", false), 0, 0); // false for NO image
        teamGrid.add(createMemberCard(null, "Aadesh Aaglave", "Developer", false), 1, 0); // false for NO image
        teamGrid.add(createMemberCard(null, "Yash Targe", "Developer", false), 0, 1); // false for NO image
        teamGrid.add(createMemberCard(null, "Sahil Patil", "Developer", false), 1, 1); // false for NO image

        teamSection.getChildren().addAll(teamTitle, teamGrid);

        // --- Our Mentor Section ---
        VBox mentorSection = new VBox(35);
        mentorSection.setAlignment(Pos.CENTER);
        Label mentorTitle = createSectionTitle("Our Mentor");

        // Last Mentor - WITHOUT image, only name and role
        Node mentorCard = createMemberCard(null, "Adsare Gandharv", "Mentor", false); // false for NO image

        mentorSection.getChildren().addAll(mentorTitle, mentorCard);

        // --- Assemble Page (Order adjusted as per requirements) ---
        pageContent.getChildren().addAll(header, c2wAcknowledgementSection, pillarsOfSupportSection, teamSection, mentorSection);

        // Use a ScrollPane to ensure content fits on all screen sizes
        ScrollPane scrollPane = new ScrollPane(pageContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scrollPane;
    }

    /**
     * Helper method to create a styled section title.
     */
    private Label createSectionTitle(String title) {
        Label label = new Label(title);
        label.setFont(Font.font("System", FontWeight.BOLD, 32));
        label.setTextFill(Color.WHITE);
        label.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(0, 0, 2, 0))));
        label.setPadding(new Insets(0, 0, 5, 0));
        return label;
    }

    /**
     * Helper method to create a styled card for a team member or mentor.
     * Includes a boolean to control whether an image is displayed.
     */
    private VBox createMemberCard(String imagePath, String name, String role, boolean includeImage) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(250); // Keep consistent width for cards
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.07); -fx-background-radius: 20;");
        card.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.3)));

        if (includeImage) {
            Node avatar = createImageView(imagePath, 120);
            card.getChildren().add(avatar);
        } else {
            // Adjusted top padding for text-only card to maintain visual balance
            // Only apply this padding if the role is also empty, making it just a name card.
            if (role.isEmpty()) {
                 card.setPadding(new Insets(65, 30, 30, 30)); // Further adjust for name only
            } else {
                 card.setPadding(new Insets(50, 30, 30, 30)); // Standard for text-only with role
            }
        }

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        nameLabel.setTextFill(Color.WHITE);

        // Only add roleLabel if the role string is not empty
        if (!role.isEmpty()) {
            Label roleLabel = new Label(role);
            roleLabel.setFont(Font.font("System", 18));
            roleLabel.setTextFill(Color.web("#BDBDBD"));
            card.getChildren().addAll(nameLabel, roleLabel);
        } else {
            card.getChildren().add(nameLabel); // Just add the name if role is empty
        }
        
        return card;
    }

    /**
     * Helper method to create a circular ImageView with a fallback and border.
     * This method is only called if 'includeImage' is true in createMemberCard.
     */
    private Node createImageView(String path, int size) {
        ImageView imageView = new ImageView();
        Image image = null;

        try {
            image = new Image(path);
            if (image.isError() || image.getWidth() == 0 || image.getHeight() == 0) {
                image = null; // Treat as if loading failed
            }
        } catch (Exception e) {
            System.err.println("Warning: Image not found or could not be loaded: " + path + " - " + e.getMessage());
            image = null; // Ensure image is null on error
        }

        if (image == null) {
            // Fallback to a generic "Photo" placeholder if image loading failed for SHASHI BAGAL (though path is provided)
            imageView.setImage(new Image("https://via.placeholder.com/" + size + "/FFFFFF/2C3E50?text=Photo"));
        } else {
            imageView.setImage(image);
        }

        imageView.setFitWidth(size);
        imageView.setFitHeight(size);

        // Create a circular clip
        Circle clip = new Circle(size / 2.0, size / 2.0, size / 2.0);
        imageView.setClip(clip);

        // Add a border effect using a StackPane
        Circle border = new Circle(size / 2.0);
        border.setStroke(Color.WHITE);
        border.setStrokeWidth(2);
        border.setFill(Color.TRANSPARENT);

        StackPane stack = new StackPane(imageView, border);
        stack.setAlignment(Pos.CENTER);
        stack.setEffect(new DropShadow(10, Color.rgb(0,0,0,0.5)));

        return stack;
    }

    /**
     * Helper method to create styled Text nodes for the TextFlow.
     * (This method is not used in the current version of the code, but kept for completeness based on original extract)
     */
    private Text createText(String content, boolean isHighlight) {
        Text text = new Text(content);
        text.setFont(Font.font("System", isHighlight ? FontWeight.BOLD : FontWeight.NORMAL, 18));
        text.setFill(isHighlight ? Color.web("#D7BDE2") : Color.web("#E0E0E0"));
        return text;
    }
}