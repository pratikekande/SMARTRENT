package com.smartrent.View;

import com.smartrent.Controller.dataservice;
import com.smartrent.Controller.FireBaseAuth;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import javafx.animation.PauseTransition;
import javafx.animation.RotateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Signup {

    private final dataservice dataService = new dataservice(); // Instance of your data service
    private Scene signupScene;
    private Stage SignupStage;

    // --- UI Member Variables ---
    private VBox signupForm;
    private StackPane signupStackPane;
    private VBox loadingBox;
    private VBox successBox;
    private Label statusLabel;
    private TextField firstNamField, lastNamField, emailField;
    private PasswordField passwordField;
    private ComboBox<String> userTypeCombo;
    private CheckBox termsCheck;
    private Button registerBtn;

    public void setSignupStage(Stage SignupStage) {
        this.SignupStage = SignupStage;
    }

    public void setSignupScene(Scene signupScene) {
        this.signupScene = signupScene;
    }

    public HBox createSignupPage(Runnable back) {
        // --- Form Components Initialization ---
        ImageView logimage = new ImageView("Assets//Images//logo.png");
        logimage.setFitWidth(150);
        logimage.setFitHeight(150);
        logimage.setClip(new Circle(80, 80, 120)); // CenterX, CenterY, Radius

        // Enable 3D rotation
        logimage.setRotationAxis(Rotate.Y_AXIS);

        // Create the rotation animation
        RotateTransition rotate = new RotateTransition(Duration.seconds(3), logimage);
        rotate.setFromAngle(0);
        rotate.setToAngle(360);
        rotate.setCycleCount(RotateTransition.INDEFINITE);
        rotate.setInterpolator(javafx.animation.Interpolator.LINEAR);
        rotate.play();

        userTypeCombo = new ComboBox<>();
        userTypeCombo.getItems().addAll("Select User", "Owner", "Tenant");
        userTypeCombo.setValue("Select User");
        userTypeCombo.setPrefWidth(220);
        userTypeCombo.setStyle(
                "-fx-font-weight: bold; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-border-color: #6366F1;");

        VBox logoVBox = new VBox(10, logimage, userTypeCombo);
        logoVBox.setAlignment(Pos.CENTER);

        Text firstName = new Text("First Name");
        firstName.setFont(Font.font("Arial", 13));
        firstNamField = new TextField();
        firstNamField.setFocusTraversable(false);
        firstNamField.setPromptText("Enter First Name");
        firstNamField.setStyle(
                "-fx-background-color: #F3F4F6; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-padding: 10;");
        VBox fnameVBox = new VBox(2, firstName, firstNamField);

        Text lastName = new Text("Last Name");
        lastName.setFont(Font.font("Arial", 13));
        lastNamField = new TextField();
        lastNamField.setFocusTraversable(false);
        lastNamField.setPromptText("Enter Last Name");
        lastNamField.setStyle(
                "-fx-background-color: #F3F4F6; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-padding: 10;");
        VBox lnameBox = new VBox(2, lastName, lastNamField);

        HBox nameBox = new HBox(30, fnameVBox, lnameBox);

        Text email = new Text("Email");
        email.setFont(Font.font("Arial", 13));
        emailField = new TextField();
        emailField.setFocusTraversable(false);
        emailField.setPromptText("example@gmail.com");
        emailField.setStyle(
                "-fx-background-color: #F3F4F6; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-padding: 10;");
        VBox emailBox = new VBox(2, email, emailField);

        Text pass = new Text("Password");
        pass.setFont(Font.font("Arial", 13));
        passwordField = new PasswordField();
        passwordField.setFocusTraversable(false);
        passwordField.setPromptText("Enter Password");
        passwordField.setStyle(
                "-fx-background-color: #F3F4F6; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-padding: 10;");
        VBox passVBox = new VBox(2, pass, passwordField);

        termsCheck = new CheckBox();
        Label termsLabel = new Label("By signing up, I agree with the");
        Hyperlink termsLink = new Hyperlink("Terms of Use & Privacy Policy");
        HBox termsBox = new HBox(5, termsCheck, termsLabel, termsLink);
        termsBox.setAlignment(Pos.CENTER_LEFT);

        statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", 12));
        statusLabel.setStyle("-fx-text-fill: red;");

        registerBtn = new Button("Signup");
        registerBtn.setPrefWidth(300);
        registerBtn.setPrefHeight(42);
        registerBtn.setStyle(
                "-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 15px; -fx-background-radius: 8px;");
        registerBtn.setCursor(Cursor.HAND);

        Text orText = new Text("Or Signup with");
        Line dividerLeft = new Line(0, 0, 100, 0);
        Line dividerRight = new Line(0, 0, 100, 0);
        dividerLeft.setStroke(Color.LIGHTGRAY);
        dividerRight.setStroke(Color.LIGHTGRAY);
        HBox orBox = new HBox(10, dividerLeft, orText, dividerRight);
        orBox.setAlignment(Pos.CENTER);

        ImageView googleIcon = new ImageView("Assets//Images//google.png");
        googleIcon.setFitWidth(20);
        googleIcon.setPreserveRatio(true);
        Button googleBtn = new Button("", googleIcon);
        googleBtn.setStyle(
                "-fx-background-color: #FEE2E2; -fx-border-radius: 100; -fx-background-radius: 100; -fx-padding: 5 20 5 20;");

        Hyperlink loginLink = new Hyperlink("Signin");
        loginLink.setStyle("-fx-text-fill: #6366F1; -fx-font-size: 15px;");
        loginLink.setBorder(Border.EMPTY);
        loginLink.setOnAction(event -> back.run());

        Text alreadyHaveAccountText = new Text("Already have an account?");
        alreadyHaveAccountText.setFont(Font.font("Arial", 13));
        HBox loginBox = new HBox(5, alreadyHaveAccountText, loginLink);
        loginBox.setAlignment(Pos.CENTER);

        signupForm = new VBox(15, logoVBox, nameBox, emailBox, passVBox, termsBox, registerBtn, orBox, googleIcon,
                loginBox, statusLabel);
        signupForm.setPadding(new Insets(30, 20, 20, 20));
        signupForm.setStyle(
                "-fx-border-color: #6366F1; -fx-border-width: 2px; -fx-border-radius: 15px; -fx-background-radius: 15px;");
        signupForm.setMaxWidth(400);
        signupForm.setAlignment(Pos.TOP_CENTER);

        setupAnimationStates();

        signupStackPane = new StackPane(signupForm, loadingBox, successBox);
        signupStackPane.setAlignment(Pos.CENTER);

        registerBtn.setOnAction(event -> handleSignup(back));

        // --- Right Panel ---
        Label greet = new Label("Come join us");
        greet.setFont(Font.font("Arial", FontWeight.BOLD,40));
        greet.setStyle("-fx-text-fill: #1c1d1dff; -fx-font-weight: bold;");
        Text text1 = new Text("🔢 Manage properties, track rent, and stay organized—made simple.");
        text1.setFont(Font.font("Arial",FontWeight.BOLD,18));
        Text text2 = new Text("⏱ Smart tips to manage rentals and stay on top of rent.");
         text2.setFont(Font.font("Arial",FontWeight.BOLD,18));
        Text text3 = new Text("🌐 Helping tenants and owners stay in sync");
         text3.setFont(Font.font("Arial",FontWeight.BOLD,18));
        VBox textList = new VBox(25, text1, text2, text3);
        VBox rightPanel = new VBox(30, greet, textList);
        rightPanel.setAlignment(Pos.TOP_LEFT);
        rightPanel.setPadding(new Insets(40));

        HBox root = new HBox(40, signupStackPane, rightPanel);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        // root.setStyle("-fx-background-color: #F3F4F6;");

        // --- MODIFICATION START: Set Background Image ---
        Image bgImage = new Image("Assets//Images//layout.jpeg", true); // Use file: path and forward slashes
        BackgroundSize backgroundSize = new BackgroundSize(
                100, 100, true, true, true, true); // last `false` to preserve ratio
        BackgroundImage backgroundImage = new BackgroundImage(
                bgImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                backgroundSize);
        Background background = new Background(backgroundImage);
        root.setBackground(background);
        return root;
    }

    private void setupAnimationStates() {
        // Loading State
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);
        Text creatingAccountText = new Text("Creating account...");
        creatingAccountText.setFont(Font.font("Arial", 14));
        creatingAccountText.setFill(Color.GRAY);
        loadingBox = new VBox(20, loadingIndicator, creatingAccountText);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setVisible(false);

        // Success State
        Text checkMark = new Text("✓");
        checkMark.setFont(Font.font("Arial", 60));
        checkMark.setFill(Color.GREEN);
        Text successText = new Text("Signup successful!");
        successText.setFont(Font.font("Arial", 18));
        successText.setFill(Color.DARKGREEN);
        successBox = new VBox(20, checkMark, successText);
        successBox.setAlignment(Pos.CENTER);
        successBox.setVisible(false);
    }

    private void handleSignup(Runnable back) {
        if (firstNamField.getText().isEmpty() || lastNamField.getText().isEmpty() || emailField.getText().isEmpty()
                || passwordField.getText().isEmpty()) {
            statusLabel.setText("Please enter all the fields");
            return;
        }
        if (userTypeCombo.getValue().equals("Select User")) {
            statusLabel.setText("Please select a user type (Owner/Tenant).");
            return;
        }
        if (!termsCheck.isSelected()) {
            statusLabel.setText("You must agree to the Terms of Use.");
            return;
        }

        signupForm.setVisible(false);
        loadingBox.setVisible(true);

        PauseTransition registrationPause = new PauseTransition(Duration.seconds(2));
        registrationPause.setOnFinished(e -> {
            String email = emailField.getText();
            String password = passwordField.getText();

            if (FireBaseAuth.signWithEmailAndPassword(email, password, "signup")) {
                try {
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("firstName", firstNamField.getText());
                    userData.put("lastName", lastNamField.getText());
                    userData.put("email", email);
                    userData.put("userType", userTypeCombo.getValue());

                    String collection = "users";
                    String documentId = email;

                    dataService.addSignupdata(collection, documentId, userData);

                    loadingBox.setVisible(false);
                    successBox.setVisible(true);

                    PauseTransition successPause = new PauseTransition(Duration.seconds(1.5));
                    successPause.setOnFinished(ev -> back.run());
                    successPause.play();

                } catch (ExecutionException | InterruptedException ex) {
                    ex.printStackTrace();
                    loadingBox.setVisible(false);
                    signupForm.setVisible(true);
                    statusLabel.setText("Account created, but failed to save profile.");
                }
            } else {
                loadingBox.setVisible(false);
                signupForm.setVisible(true);
                statusLabel.setText("Signup failed. Email may already be in use.");
            }
        });
        registrationPause.play();
    }
}