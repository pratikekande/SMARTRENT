package com.smartrent.View;

import javafx.animation.RotateTransition;
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

public class AboutUsPage {

    public Parent createAboutUsPage(Runnable backAction) {
        String FONT_FAMILY = "System";
        Color PRIMARY_TEXT_COLOR = Color.WHITE;
        String MAIN_GRADIENT = "-fx-background-color: linear-gradient(from 0% 0% to 100% 100%, #2C3E50, #4A235A);";

        VBox pageContent = new VBox(55);
        pageContent.setStyle(MAIN_GRADIENT);
        pageContent.setAlignment(Pos.TOP_CENTER);
        pageContent.setPadding(new Insets(20, 60, 60, 60));

        Button backButton = new Button("← Back to Home");
        backButton.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 16));
        backButton.setTextFill(PRIMARY_TEXT_COLOR);
        backButton.setStyle(
                "-fx-background-color: transparent; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 20; -fx-padding: 8 16;");
        backButton.setCursor(Cursor.HAND);
        backButton.setOnMouseEntered(e -> backButton.setStyle(
                "-fx-background-color: white; -fx-text-fill: black; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 20; -fx-padding: 8 16;"));
        backButton.setOnMouseExited(e -> backButton.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-width: 2; -fx-border-radius: 20; -fx-padding: 8 16;"));
        backButton.setOnAction(e -> backAction.run());

        HBox header = new HBox(backButton);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(10, 0, 10, 0));

        VBox c2wAcknowledgementSection = new VBox(35);
        c2wAcknowledgementSection.setAlignment(Pos.CENTER);
        HBox c2wHeader = new HBox(20);
        c2wHeader.setAlignment(Pos.CENTER);

        Node c2wLogo = createImageView("Assets/Images/c2w.jpeg", 80);

        // ✅ Apply 3D rotation animation
        RotateTransition rotate = new RotateTransition(javafx.util.Duration.seconds(8), c2wLogo);
        rotate.setByAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rotate.setAxis(javafx.geometry.Point3D.ZERO.add(0, 1, 1)); // 3D axis
        rotate.play();

        // c2wHeader.getChildren().addAll(c2wLogo, c2wTitle);
        Label c2wTitle = new Label("Core2Web Technologies");
        c2wTitle.setFont(Font.font(FONT_FAMILY, FontWeight.BOLD, 48));
        c2wTitle.setTextFill(PRIMARY_TEXT_COLOR);
        c2wTitle.setEffect(new DropShadow(15, Color.BLACK));
        c2wHeader.getChildren().addAll(c2wLogo, c2wTitle);

        Label acknowledgementText = new Label(
                "We extend our deepest gratitude to our pillars of support at Core2Web. Their invaluable guidance and unwavering encouragement were instrumental in the success of this project.");
        acknowledgementText.setFont(Font.font("System", FontWeight.NORMAL, 18));
        acknowledgementText.setTextFill(Color.web("#E0E0E0"));
        acknowledgementText.setWrapText(true);
        acknowledgementText.setTextAlignment(TextAlignment.CENTER);
        acknowledgementText.setMaxWidth(800);

        c2wAcknowledgementSection.getChildren().addAll(c2wHeader, acknowledgementText);

        VBox pillarsOfSupportSection = new VBox(35);
        pillarsOfSupportSection.setAlignment(Pos.CENTER);
        Label pillarsTitle = createSectionTitle("Our Pillars of Support");

        Node shashiCard = createMemberCard("Assets/Images/shashi_bagal.jpeg", "SHASHI BAGAL", "MASTER", true);
        HBox shashiContainer = new HBox(shashiCard);
        shashiContainer.setAlignment(Pos.CENTER);

        HBox otherSirsContainer = new HBox(30);
        otherSirsContainer.setAlignment(Pos.CENTER);

        VBox sachinSirBox = new VBox(createCompactMemberCard(null, "Sachin Sir", "", true));
        VBox pramodSirBox = new VBox(createCompactMemberCard(null, "Pramod Sir", "", true));
        VBox akshaySirBox = new VBox(createCompactMemberCard(null, "Akshay Sir", "", true));

        sachinSirBox.setPrefSize(180, 100);
        pramodSirBox.setPrefSize(180, 100);
        akshaySirBox.setPrefSize(180, 100);

        otherSirsContainer.getChildren().addAll(sachinSirBox, pramodSirBox, akshaySirBox);

        Label belovedInstructorLabel = new Label("Our Beloved Instructors");
        belovedInstructorLabel.setFont(Font.font("System", FontWeight.BOLD, 30));
        belovedInstructorLabel.setTextFill(Color.web("#E0E0E0"));
        belovedInstructorLabel.setPadding(new Insets(0, 0, -20, 0));
        belovedInstructorLabel.setAlignment(Pos.CENTER);
        belovedInstructorLabel.setUnderline(true);

        pillarsOfSupportSection.getChildren().addAll(pillarsTitle, shashiContainer, belovedInstructorLabel,
                otherSirsContainer);

        VBox teamSection = new VBox(35);
        teamSection.setAlignment(Pos.CENTER);
        Label teamTitle = createSectionTitle("Meet the Developers");

        GridPane teamGrid = new GridPane();
        teamGrid.setAlignment(Pos.CENTER);
        teamGrid.setHgap(40);
        teamGrid.setVgap(40);
        teamGrid.add(createMemberCard(null, "Pratik Ekande", "Developer", false), 0, 0);
        teamGrid.add(createMemberCard(null, "Aadesh Aaglave", "Developer", false), 1, 0);
        teamGrid.add(createMemberCard(null, "Yash Targe", "Developer", false), 0, 1);
        teamGrid.add(createMemberCard(null, "Sahil Patil", "Developer", false), 1, 1);

        teamSection.getChildren().addAll(teamTitle, teamGrid);

        VBox mentorSection = new VBox(35);
        mentorSection.setAlignment(Pos.CENTER);
        Label mentorTitle = createSectionTitle("Team Leader");

        // ⬇ Updated this line to make "Team Leader" role bold
        Node mentorCard = createMemberCard(null, "Adsare Gandharv", "", false);

        mentorSection.getChildren().addAll(mentorTitle, mentorCard);

        pageContent.getChildren().addAll(header, c2wAcknowledgementSection, pillarsOfSupportSection, teamSection,
                mentorSection);

        ScrollPane scrollPane = new ScrollPane(pageContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        return scrollPane;
    }

    private Label createSectionTitle(String title) {
        Label label = new Label(title);
        label.setFont(Font.font("System", FontWeight.BOLD, 32));
        label.setTextFill(Color.WHITE);
        label.setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY,
                new BorderWidths(0, 0, 2, 0))));
        label.setPadding(new Insets(0, 0, 5, 0));
        return label;
    }

    private VBox createMemberCard(String imagePath, String name, String role, boolean includeImage) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(250);
        card.setPadding(new Insets(30));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.07); -fx-background-radius: 20;");
        card.setEffect(new DropShadow(20, Color.rgb(0, 0, 0, 0.3)));

        if (includeImage) {
            Node avatar = createImageView(imagePath, 120);
            card.getChildren().add(avatar);
        } else {
            if (role.isEmpty()) {
                card.setPadding(new Insets(65, 30, 30, 30));
            } else {
                card.setPadding(new Insets(50, 30, 30, 30));
            }
        }

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 22));
        nameLabel.setTextFill(Color.WHITE);

        if (!role.isEmpty()) {
            // ⬇ Make both MASTER and TEAM LEADER bold
            FontWeight weight = role.equalsIgnoreCase("MASTER") || role.equalsIgnoreCase("TEAM LEADER")
                    ? FontWeight.BOLD
                    : FontWeight.NORMAL;

            Label roleLabel = new Label(role);
            roleLabel.setFont(Font.font("System", weight, 18));
            roleLabel.setTextFill(Color.web("#BDBDBD"));
            card.getChildren().addAll(nameLabel, roleLabel);
        } else {
            card.getChildren().add(nameLabel);
        }

        return card;
    }

    private Node createCompactMemberCard(String imagePath, String name, String role, boolean isBold) {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(2, 6, 6, 6));
        card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 15;");
        card.setEffect(new DropShadow(5, Color.rgb(0, 0, 0, 0.2)));
        card.setMinSize(100, 80);

        if (imagePath != null && !imagePath.isEmpty()) {
            ImageView imageView = new ImageView(new Image(imagePath));
            imageView.setFitWidth(50);
            imageView.setFitHeight(50);
            imageView.setClip(new Circle(25, 25, 25));
            card.getChildren().add(imageView);
        }

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 20)); // ⬅️ Increased font size
        nameLabel.setTextFill(Color.WHITE);
        nameLabel.setWrapText(true);
        nameLabel.setTextAlignment(TextAlignment.CENTER);
        card.getChildren().add(nameLabel);

        if (role != null && !role.isEmpty()) {
            Label roleLabel = new Label(role);
            roleLabel.setFont(Font.font("Segoe UI", isBold ? FontWeight.BOLD : FontWeight.NORMAL, 14)); // ⬅️ Increased
                                                                                                        // size
            roleLabel.setTextFill(Color.web("#E0E0E0"));
            roleLabel.setWrapText(true);
            card.getChildren().add(roleLabel);
        }

        return card;
    }

    private Node createImageView(String path, int size) {
        ImageView imageView = new ImageView();
        Image image = null;

        try {
            image = new Image(path);
            if (image.isError() || image.getWidth() == 0 || image.getHeight() == 0) {
                image = null;
            }
        } catch (Exception e) {
            System.err.println("Warning: Image not found or could not be loaded: " + path + " - " + e.getMessage());
            image = null;
        }

        if (image == null) {
            imageView.setImage(new Image("https://via.placeholder.com/" + size + "/FFFFFF/2C3E50?text=Photo"));
        } else {
            imageView.setImage(image);
        }

        imageView.setFitWidth(size);
        imageView.setFitHeight(size);

        Circle clip = new Circle(size / 2.0, size / 2.0, size / 2.0);
        imageView.setClip(clip);

        Circle border = new Circle(size / 2.0);
        border.setStroke(Color.WHITE);
        border.setStrokeWidth(2);
        border.setFill(Color.TRANSPARENT);

        StackPane stack = new StackPane(imageView, border);
        stack.setAlignment(Pos.CENTER);
        stack.setEffect(new DropShadow(10, Color.rgb(0, 0, 0, 0.5)));

        return stack;
    }

    private Text createText(String content, boolean isHighlight) {
        Text text = new Text(content);
        text.setFont(Font.font("System", isHighlight ? FontWeight.BOLD : FontWeight.NORMAL, 18));
        text.setFill(isHighlight ? Color.web("#D7BDE2") : Color.web("#E0E0E0"));
        return text;
    }
}