package com.smartrent.View.Owner;

import com.smartrent.Model.Owner.OwnerProfile;
import com.smartrent.Controller.*;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class OwnerProfilePage {

    // UI Components
    private TextField nameField, phoneField, emailField, addressField;
    private Label nameValueLabel, phoneValueLabel, emailValueLabel, addressValueLabel;
    private Button editButton, saveButton;
    private Node displayView, editView;
    private StackPane profileDetailsContainer;
    private ProgressIndicator loadingIndicator;

    // Data and Services
    private dataservice ds;
    private String ownerId;
    private boolean profileExists = false;
    private OwnerProfile currentOwnerProfile;
    private Consumer<String> onNameUpdateCallback;

    public Node getView(Runnable onBack, String ownerId, Consumer<String> onNameUpdate) {
        this.ds = new dataservice();
        this.ownerId = ownerId;
        this.currentOwnerProfile = new OwnerProfile();
        this.onNameUpdateCallback = onNameUpdate;

        // Header
        Text title = new Text("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        // Back button has been removed from here
        HBox header = new HBox(title);
        header.setAlignment(Pos.CENTER_LEFT);

        // Profile Image
        ImageView profileImageView = createProfileImageView();

        // Views (Display and Edit)
        displayView = createDisplayView();
        editView = createEditView();

        // Loading Indicator
        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);

        profileDetailsContainer = new StackPane(loadingIndicator, displayView, editView);
        profileDetailsContainer.setAlignment(Pos.CENTER);

        // Buttons
        editButton = new Button("Edit Profile");
        saveButton = new Button("Save Changes");
        HBox buttonsBox = new HBox(10, saveButton, editButton);
        buttonsBox.setAlignment(Pos.BOTTOM_RIGHT);

        // Button Actions
        editButton.setOnAction(e -> switchToEditMode());
        // The onBack runnable is now only used here, to return after saving
        saveButton.setOnAction(e -> saveChanges(onBack));

        // Main content box
        VBox profileDetailsBox = new VBox(30, profileImageView, profileDetailsContainer, buttonsBox);
        profileDetailsBox.setAlignment(Pos.CENTER);
        profileDetailsBox.setPadding(new Insets(40));
        profileDetailsBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        profileDetailsBox.setMaxWidth(800);
        profileDetailsBox.setMinHeight(350);

        // Main layout
        VBox mainLayout = new VBox(30, header, profileDetailsBox);
        mainLayout.setPadding(new Insets(40));
        mainLayout.setStyle("-fx-background-color: #f8fafc;");
        mainLayout.setAlignment(Pos.TOP_CENTER);

        setupInitialState();
        loadProfileData();

        return mainLayout;
    }

    private void setupInitialState() {
        displayView.setVisible(true);
        editView.setVisible(false);
        saveButton.setVisible(false);
        editButton.setVisible(true);
        editButton.setDisable(true);

        // Bind save button's disable property to text fields being empty
        saveButton.disableProperty().bind(
                Bindings.isEmpty(nameField.textProperty())
                .or(Bindings.isEmpty(emailField.textProperty()))
                .or(Bindings.isEmpty(phoneField.textProperty()))
                .or(Bindings.isEmpty(addressField.textProperty()))
        );
    }

    private void loadProfileData() {
        loadingIndicator.setVisible(true);
        displayView.setVisible(false);
        editView.setVisible(false);

        CompletableFuture.supplyAsync(() -> {
            try {
                return ds.getOwnerData("OwnerProfile", this.ownerId);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }).thenAccept(documentSnapshot -> Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
            displayView.setVisible(true);
            editButton.setDisable(false);

            if (documentSnapshot != null && documentSnapshot.exists()) {
                this.profileExists = true;
                this.currentOwnerProfile = documentSnapshot.toObject(OwnerProfile.class);
            } else {
                this.profileExists = false;
                this.currentOwnerProfile.setEmail(this.ownerId); // Pre-fill email if new
            }
            populateFields(this.currentOwnerProfile);
        }));
    }

    private void populateFields(OwnerProfile profile) {
        String name = profile.getName() != null ? profile.getName() : "";
        String email = profile.getEmail() != null ? profile.getEmail() : "";
        String phone = profile.getPhone() != null ? profile.getPhone() : "";
        String address = profile.getAddress() != null ? profile.getAddress() : "";

        nameValueLabel.setText(name);
        emailValueLabel.setText(email);
        phoneValueLabel.setText(phone);
        addressValueLabel.setText(address);

        nameField.setText(name);
        emailField.setText(email);
        phoneField.setText(phone);
        addressField.setText(address);

        emailField.setDisable(true); // Email should not be editable
    }

    private void switchToEditMode() {
        displayView.setVisible(false);
        editView.setVisible(true);
        saveButton.setVisible(true);
        editButton.setVisible(false);
    }

    private void saveChanges(Runnable back) {
        // Update the local profile object from the text fields
        currentOwnerProfile.setName(nameField.getText());
        currentOwnerProfile.setEmail(emailField.getText());
        currentOwnerProfile.setPhone(phoneField.getText());
        currentOwnerProfile.setAddress(addressField.getText());

        // Create a Map for Firestore
        Map<String, Object> ownerProfileData = new HashMap<>();
        ownerProfileData.put("name", currentOwnerProfile.getName());
        ownerProfileData.put("email", currentOwnerProfile.getEmail());
        ownerProfileData.put("phone", currentOwnerProfile.getPhone());
        ownerProfileData.put("address", currentOwnerProfile.getAddress());

        loadingIndicator.setVisible(true);
        editView.setVisible(false);

        CompletableFuture.runAsync(() -> {
            try {
                if (profileExists) {
                    ds.updateOwnerData("OwnerProfile", this.ownerId, ownerProfileData);
                } else {
                    ds.addOwnerdata("OwnerProfile", this.ownerId, ownerProfileData);
                    this.profileExists = true; // It exists now
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).thenRun(() -> Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
            populateFields(currentOwnerProfile); // Update labels with new data

            // Execute the dashboard callback with the new name
            if (onNameUpdateCallback != null) {
                onNameUpdateCallback.accept(nameField.getText());
            }
            // --- START: ADDED SUCCESS POP-UP ---
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Success");
            alert.setHeaderText("Profile Saved");
            alert.setContentText("Your profile has been successfully updated.");
            alert.showAndWait();
            // --- END: ADDED SUCCESS POP-UP ---

            back.run();
        }));
    }

    // --- UI Creation Helper Methods (Unchanged) ---

    private ImageView createProfileImageView() {
        ImageView profileImageView = new ImageView();
        try {
            Image profileImg = new Image("Assets/Images/profile.jpeg");
            profileImageView.setImage(profileImg);
        } catch (Exception e) {
            System.err.println("Profile image not found. Using a placeholder. Error: " + e.getMessage());
            // A more reliable placeholder image
            profileImageView.setImage(new Image("https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&f=y&s=100"));
        }
        profileImageView.setFitWidth(100);
        profileImageView.setFitHeight(100);
        profileImageView.setClip(new Circle(50, 50, 50));
        return profileImageView;
    }

    private Node createDisplayView() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(18);

        nameValueLabel = createValueLabel("");
        emailValueLabel = createValueLabel("");
        phoneValueLabel = createValueLabel("");
        addressValueLabel = createValueLabel("");

        grid.add(createLabel("Name:"), 0, 0);
        grid.add(nameValueLabel, 1, 0);
        grid.add(createLabel("Email:"), 0, 1);
        grid.add(emailValueLabel, 1, 1);
        grid.add(createLabel("Phone:"), 0, 2);
        grid.add(phoneValueLabel, 1, 2);
        grid.add(createLabel("Property Address:"), 0, 3);
        grid.add(addressValueLabel, 1, 3);

        return grid;
    }

    private Node createEditView() {
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(18);

        nameField = new TextField();
        emailField = new TextField();
        phoneField = new TextField();
        addressField = new TextField();

        nameField.setPromptText("Enter your full name");
        phoneField.setPromptText("Enter your phone number");
        addressField.setPromptText("Enter your property address");

        grid.add(createLabel("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(createLabel("Email:"), 0, 1);
        grid.add(emailField, 1, 1);
        grid.add(createLabel("Phone:"), 0, 2);
        grid.add(phoneField, 1, 2);
        grid.add(createLabel("Property Address:"), 0, 3);
        grid.add(addressField, 1, 3);

        return grid;
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 16));
        return label;
    }

    private Label createValueLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.NORMAL, 16));
        return label;
    }
}
