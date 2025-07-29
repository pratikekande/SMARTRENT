package com.smartrent.View.Tenant.Component;

import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.util.Duration;

public class Sidebar extends VBox {

    private Button[] menuButtons;
    private final String[] menuItems = {
            "Tenant Dashboard", "Maintenance History", "Rent History", "Logout"
    };

    private String currentActiveItem;
    private final String defaultStyle = "-fx-background-color: transparent; -fx-text-fill: #E2E8F0; -fx-font-size: 15; -fx-alignment: CENTER_LEFT; -fx-padding: 10 0 10 30;";
    private final String activeStyle = "-fx-background-color: #3B82F6; -fx-text-fill: white; -fx-font-size: 15; -fx-font-weight: bold; -fx-alignment: CENTER_LEFT; -fx-padding: 10 0 10 30;";
    private final String hoverStyle = "-fx-background-color: #1E293B; -fx-text-fill: white; -fx-font-size: 15; -fx-alignment: CENTER_LEFT; -fx-padding: 10 0 10 30;";

    public Sidebar(String activeItem) {
        this.currentActiveItem = activeItem; // Set initial active item
        setSpacing(20);
        setPrefWidth(200);
        setStyle("-fx-background-color: #0F172A;");
        setPadding(new Insets(50, 0, 0, 0));
        setAlignment(Pos.TOP_CENTER);

        // Profile Section
        ImageView profileImage = new ImageView("Assets//Images//logo.png");
        profileImage.setFitWidth(160);
        profileImage.setFitHeight(200);
        profileImage.setClip(new Circle(80, 80, 120)); // CenterX, CenterY, Radius

        // Enable 3D rotation
        profileImage.setRotationAxis(Rotate.Y_AXIS);

        // Create the rotation animation
        RotateTransition rotate = new RotateTransition(Duration.seconds(3), profileImage);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rotate.play();
        Text userRole = new Text("Tenant");
        userRole.setFont(Font.font("System", FontWeight.NORMAL, 20));
        userRole.setFill(Color.LIGHTGRAY);

        VBox userInfo = new VBox(5, userRole);
        userInfo.setAlignment(Pos.CENTER);

        VBox profileSection = new VBox(15, profileImage, userInfo);
        profileSection.setAlignment(Pos.CENTER);

        // Nav Menu
        VBox navMenu = new VBox(10);
        navMenu.setAlignment(Pos.TOP_CENTER);
        navMenu.setPadding(new Insets(30, 0, 0, 0));

        menuButtons = new Button[menuItems.length];

        for (int i = 0; i < menuItems.length; i++) {
            String item = menuItems[i];
            Button btn = new Button(item);
            btn.setStyle(item.equals(this.currentActiveItem) ? activeStyle : defaultStyle);
            btn.setMaxWidth(Double.MAX_VALUE);

            // Set the event handler for when the mouse enters the button
            btn.setOnMouseEntered(event -> {
                // Apply hover style only if the button is NOT the active one
                if (!item.equals(this.currentActiveItem)) {
                    btn.setStyle(hoverStyle);
                }
                btn.setCursor(Cursor.HAND); // Change cursor to a hand icon
            });

            // Set the event handler for when the mouse exits the button
            btn.setOnMouseExited(event -> {
                // Revert to the correct style (active or default)
                btn.setStyle(item.equals(this.currentActiveItem) ? activeStyle : defaultStyle);
                btn.setCursor(Cursor.DEFAULT); // Change cursor back to default
            });
            
            menuButtons[i] = btn;
            navMenu.getChildren().add(btn);
        }

        getChildren().addAll(profileSection, navMenu);

    }

    // Public method to set active style by button name
    public void highlight(String activeItem) {
        this.currentActiveItem = activeItem; // Update the state
        for (int i = 0; i < menuButtons.length; i++) {
            Button btn = menuButtons[i];
            if (menuItems[i].equals(activeItem)) {
                btn.setStyle(activeStyle);
            } else {
                btn.setStyle(defaultStyle);
            }
        }
    }

    // Public getter for buttons array so caller can attach handlers
    public Button[] getMenuButtons() {
        return menuButtons;
    }
}
