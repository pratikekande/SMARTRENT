package com.smartrent.View.Tenant;

import com.google.cloud.firestore.DocumentSnapshot;
import com.smartrent.Controller.dataservice;
import com.smartrent.Model.Tenant.TenantProfileModel;
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

public class TenantProfilePage {

    private TextField nameField, emailField, phoneField, addressField;
    private Label nameValueLabel, emailValueLabel, phoneValueLabel, addressValueLabel;
    private Button editButton, saveButton;
    private Node displayView, editView;
    private StackPane profileDetailsContainer;
    private ProgressIndicator loadingIndicator;

    private dataservice ds;
    private String tenantId;
    private boolean profileExists = false;
    private TenantProfileModel currentTenantProfile;
    private Consumer<String> onNameUpdateCallback;

    public Node getView(Runnable onBack, String tenantId, Consumer<String> onNameUpdate) {
        this.ds = new dataservice();
        this.tenantId = tenantId;
        this.currentTenantProfile = new TenantProfileModel();
        this.onNameUpdateCallback = onNameUpdate;

        Text title = new Text("My Profile");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox header = new HBox(20, title, spacer);
        header.setAlignment(Pos.CENTER_LEFT);

        ImageView profileImageView = createProfileImageView();

        displayView = createDisplayView();
        editView = createEditView();

        loadingIndicator = new ProgressIndicator();
        loadingIndicator.setVisible(false);

        profileDetailsContainer = new StackPane(loadingIndicator, displayView, editView);
        profileDetailsContainer.setAlignment(Pos.CENTER);

        editButton = new Button("Edit Profile");
        saveButton = new Button("Save Changes");
        HBox buttonsBox = new HBox(10, saveButton, editButton);
        buttonsBox.setAlignment(Pos.BOTTOM_RIGHT);

        editButton.setOnAction(e -> switchToEditMode());
        saveButton.setOnAction(e -> saveChanges(onBack));

        VBox profileDetailsBox = new VBox(30, profileImageView, profileDetailsContainer, buttonsBox);
        profileDetailsBox.setAlignment(Pos.CENTER);
        profileDetailsBox.setPadding(new Insets(40));
        profileDetailsBox.setStyle("-fx-background-color: white; -fx-background-radius: 12;");
        profileDetailsBox.setMaxWidth(800);
        profileDetailsBox.setMinHeight(350);

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
                return ds.getTenantProfileData("TenantProfile", this.tenantId);
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
                this.currentTenantProfile = documentSnapshot.toObject(TenantProfileModel.class);
            } else {
                this.profileExists = false;
                this.currentTenantProfile.setEmail(this.tenantId);
            }
            populateFields(this.currentTenantProfile);
        }));
    }

    private void populateFields(TenantProfileModel profile) {
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

        emailField.setDisable(true);
    }

    private void switchToEditMode() {
        displayView.setVisible(false);
        editView.setVisible(true);
        saveButton.setVisible(true);
        editButton.setVisible(false);
    }

    // NO NEED FOR THIS
    // private void switchToDisplayMode() {
    //     displayView.setVisible(true);
    //     editView.setVisible(false);
    //     saveButton.setVisible(false);
    //     editButton.setVisible(true);
    // }

       private void saveChanges(Runnable back) {
        currentTenantProfile.setName(nameField.getText());
        currentTenantProfile.setEmail(emailField.getText());
        currentTenantProfile.setPhone(phoneField.getText());
        currentTenantProfile.setAddress(addressField.getText());

        Map<String, Object> tenantProfileData = new HashMap<>();
        tenantProfileData.put("name", currentTenantProfile.getName());
        tenantProfileData.put("email", currentTenantProfile.getEmail());
        tenantProfileData.put("phone", currentTenantProfile.getPhone());
        tenantProfileData.put("address", currentTenantProfile.getAddress());

        loadingIndicator.setVisible(true);
        editView.setVisible(false);

        CompletableFuture.runAsync(() -> {
            try {
                // --- MODIFIED: Calling the new tenant-specific methods ---
                if (profileExists) {
                    ds.updateTenantData("TenantProfile", this.tenantId, tenantProfileData);
                } else {
                    ds.addTenantData("TenantProfile", this.tenantId, tenantProfileData);
                    this.profileExists = true;
                }
            } catch (Exception e) {
                e.printStackTrace();
                // In a real app, you would show an error pop-up here as well
            }
        }).thenRun(() -> Platform.runLater(() -> {
            loadingIndicator.setVisible(false);
            populateFields(currentTenantProfile);

            if (onNameUpdateCallback != null) {
                onNameUpdateCallback.accept(currentTenantProfile.getName());
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


    private ImageView createProfileImageView() {
        ImageView profileImageView = new ImageView();
        try {
            profileImageView.setImage(new Image("Assets/Images/profile.jpeg"));
        } catch (Exception e) {
            System.err.println("Profile image not found. Using placeholder. Error: " + e.getMessage());
            profileImageView.setImage(new Image("https://i.imgur.com/S12F2sC.png"));
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