package com.smartrent.View;

import com.google.cloud.firestore.DocumentSnapshot;
import com.smartrent.Controller.*;
import com.smartrent.Controller.FireBaseAuth;
import com.smartrent.Controller.dataservice;
import com.smartrent.View.Owner.OwnerDashboard;
import com.smartrent.View.Tenant.TenantDashboard;
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
import java.util.concurrent.ExecutionException;

public class Signin {

    private final dataservice dataService = new dataservice();
    private Scene SigninScene, signupScene;
    private Stage SigninStage;
    public static String usernameText;

    // --- UI Member Variables ---
    private TextField userTextField;
    private PasswordField passwordField;
    private Button signInBtn;
    private VBox loginForm;
    private StackPane rightPaneStack;
    private VBox loadingBox;
    private VBox successBox;
    private Label statusLabel;
    private int loginAttempts = 0;
    private static final int MAX_ATTEMPTS = 2;

    public void setSigninStage(Stage SigninStage) {
        this.SigninStage = SigninStage;
    }

    public void setSigninScene(Scene signinScene) {
        this.SigninScene = signinScene;
    }

    public HBox createSigninPage(Runnable back) {
        // --- Left Panel ---
        Label greetTitle = new Label("Great to have you\nback!");
        greetTitle.setFont(Font.font("Arial", FontWeight.BOLD,40));
        greetTitle.setStyle("-fx-text-fill: #6B7280; -fx-font-weight: bold;");
        Label greetSub = new Label("SmartRent – Where Tenants and Owners\nMeet Simplicity.\nA smarter way to manage rentals, together.");
        greetSub.setFont(Font.font("Arial", FontWeight.BOLD,15));
        greetSub.setStyle("-fx-text-fill: black;");
        VBox leftPane = new VBox(10, greetTitle, greetSub);
        leftPane.setPadding(new Insets(40, 40, 40, 170));
        leftPane.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(leftPane, Priority.ALWAYS);


        // --- Right Panel (Login Form) ---
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



        Text welText = new Text("SmartRent+");
        welText.setStyle("-fx-font-weight: bold;");
        welText.setFont(Font.font("Arial", FontWeight.BOLD,24));

        Text user = new Text("Email");
        user.setFont(Font.font("Arial", 13));
        userTextField = new TextField();
        userTextField.setFocusTraversable(false);
        userTextField.setPromptText("Enter Email");
        userTextField.setStyle("-fx-background-color: #F3F4F6; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-padding: 10;");
        VBox userVBox = new VBox(2, user, userTextField);

        Text pass = new Text("PassWord");
        pass.setFont(Font.font("Arial", 13));
        passwordField = new PasswordField();
        passwordField.setFocusTraversable(false);
        passwordField.setPromptText("Enter Password");
        passwordField.setStyle("-fx-background-color: #F3F4F6; -fx-border-color: #ccc; -fx-border-radius: 5px; -fx-padding: 10;");

        Hyperlink forgotPassword = new Hyperlink("Forgot Password?");
        forgotPassword.setStyle("-fx-text-fill: #000000ff; -fx-font-size: 13px;");
        forgotPassword.setBorder(Border.EMPTY);
        forgotPassword.setPadding(new Insets(0));
        HBox forgotPassBox = new HBox(forgotPassword);
        forgotPassBox.setAlignment(Pos.CENTER_RIGHT);
        VBox passVBox = new VBox(2, pass, passwordField, forgotPassBox);

        CheckBox rememberMe = new CheckBox("Remember me");
        rememberMe.setFont(Font.font("Arial",FontWeight.BOLD,12));;
        HBox rememberRow = new HBox(rememberMe);
        rememberRow.setAlignment(Pos.CENTER_LEFT);
        rememberRow.setPadding(new Insets(5, 0, 0, 0));

        signInBtn = new Button("Sign in");
        signInBtn.setMaxWidth(Double.MAX_VALUE);
        String originalStyle = "-fx-background-color: #6366F1; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 6px;";
        String hoverStyle = "-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold; -fx-background-radius: 6px;";
        signInBtn.setStyle(originalStyle);
        signInBtn.setOnMouseEntered(event -> {
            signInBtn.setStyle(hoverStyle);
            signInBtn.setCursor(Cursor.HAND);
        });
        signInBtn.setOnMouseExited(event -> {
            signInBtn.setStyle(originalStyle);
            signInBtn.setCursor(Cursor.DEFAULT);
        });

        statusLabel = new Label();
        statusLabel.setFont(Font.font("Arial", 12));
        statusLabel.setStyle("-fx-text-fill: red;");

        Text orText = new Text("Or sign in with");
        Line dividerLeft = new Line(0, 0, 100, 0);
        Line dividerRight = new Line(0, 0, 100, 0);
        dividerLeft.setStroke(Color.LIGHTGRAY);
        dividerRight.setStroke(Color.LIGHTGRAY);
        HBox orBox = new HBox(dividerLeft, orText, dividerRight);
        orBox.setAlignment(Pos.CENTER);

        ImageView googleIcon = new ImageView("Assets//Images//google.png");
        googleIcon.setFitWidth(20);
        googleIcon.setPreserveRatio(true);
        Button googleBtn = new Button("", googleIcon);
        googleBtn.setStyle("-fx-background-color: #FEE2E2; -fx-border-radius: 100; -fx-background-radius: 100; -fx-padding: 5 20 5 20;");

        Text noAccount = new Text("Don't have an Account?");
        noAccount.setFont(Font.font("Arial", 14));

        Hyperlink signUpTab = new Hyperlink("Sign Up");
        signUpTab.setStyle("-fx-text-fill: #000000ff; -fx-font-size: 15px;");
        signUpTab.setBorder(Border.EMPTY);
        signUpTab.setOnAction(e -> {
            initializeSignupPage();
            SigninStage.setScene(signupScene);
        });

        Hyperlink home = new Hyperlink("Home");
        home.setStyle("-fx-text-fill: black; -fx-font-size: 15px;");
        home.setBorder(Border.EMPTY);
        home.setOnAction(e -> back.run());

        VBox noAccBox = new VBox(10, orBox, googleBtn, noAccount, signUpTab, home);
        noAccBox.setAlignment(Pos.CENTER);

        loginForm = new VBox(15, logimage, welText, userVBox, passVBox, rememberRow, signInBtn, noAccBox, statusLabel);
        loginForm.setPadding(new Insets(15, 40, 18, 40));
        loginForm.setStyle("-fx-border-color: black; -fx-border-width: 2px; -fx-border-radius: 15px; -fx-background-radius: 15px;");
        loginForm.setMaxWidth(500);
        loginForm.setAlignment(Pos.TOP_CENTER);

        setupAnimationStates();

        rightPaneStack = new StackPane(loginForm, loadingBox, successBox);
        rightPaneStack.setAlignment(Pos.CENTER);
        VBox.setVgrow(rightPaneStack, Priority.ALWAYS);
        rightPaneStack.setPadding(new Insets(40, 185, 40, 40));

        signInBtn.setOnAction(event -> handleSignIn());

        HBox root = new HBox(leftPane, rightPaneStack);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
          // --- MODIFICATION START ---
        // The original white background style was replaced with the requested
        // background image.
        BackgroundSize backgroundSize = new BackgroundSize(
                100, 100, true, true, true, true);
        BackgroundImage backgroundImage = new BackgroundImage(
                new Image("Assets//Images//layout.jpeg", true),
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.DEFAULT,
                backgroundSize);
        root.setBackground(new Background(backgroundImage));
        // --- MODIFICATION END ---

        return root;
    }


    private void setupAnimationStates() {
        // Loading State
        ProgressIndicator loadingIndicator = new ProgressIndicator();
        loadingIndicator.setPrefSize(50, 50);
        Text signingInText = new Text("Signing in, please wait...");
        signingInText.setFont(Font.font("Arial", 14));
        signingInText.setFill(Color.GRAY);
        loadingBox = new VBox(20, loadingIndicator, signingInText);
        loadingBox.setAlignment(Pos.CENTER);
        loadingBox.setVisible(false);

        // Success State
        Text checkMark = new Text("✓");
        checkMark.setFont(Font.font("Arial", 60));
        checkMark.setFill(Color.GREEN);
        Text successText = new Text("Signin successful!");
        successText.setFont(Font.font("Arial", 18));
        successText.setFill(Color.DARKGREEN);
        successBox = new VBox(20, checkMark, successText);
        successBox.setAlignment(Pos.CENTER);
        successBox.setVisible(false);
    }

    /**
     * Handles the entire sign-in flow.
     */
    private void handleSignIn() {
        String email = userTextField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter both email and password.");
            return;
        }

        // Show loading animation
        loginForm.setVisible(false);
        loadingBox.setVisible(true);

        PauseTransition authPause = new PauseTransition(Duration.seconds(1));
        authPause.setOnFinished(e -> {
            // Step 1: Authenticate with Firebase Auth
            boolean isAuthenticated = FireBaseAuth.signWithEmailAndPassword(email, password, "signin");

            if (isAuthenticated) {
                // Step 2: If authentication is successful, fetch the user's profile from Firestore
                try {
                    DocumentSnapshot userProfile = dataService.getSignupData("users", email);

                    if (userProfile.exists()) {
                        String userType = userProfile.getString("userType");

                        // Step 3: Check the user's role to authorize and navigate
                        if ("Owner".equals(userType)) {
                            showSuccessAndNavigate("owner"); // User is an Owner, proceed
                        } else if ("Tenant".equals(userType)) {
                            showSuccessAndNavigate("tenant"); // User is a Tenant
                        } else {
                            // User is valid but doesn't have a proper role assigned in the database
                            showFormState("Login failed: User role is not defined.");
                        }
                    } else {
                        // This is an edge case: auth succeeded but no profile exists in the database
                        showFormState("Login failed: User profile does not exist.");
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    // This catches errors during the database call
                    ex.printStackTrace();
                    showFormState("Error: Could not retrieve user profile.");
                }
            } else {
                // This handles authentication failure (wrong email/password)
                handleLoginFailure();
            }
        });
        authPause.play();
    }

    /**
     * Handles the UI updates for a failed login attempt.
     */
    private void handleLoginFailure() {
        loginAttempts++;
        passwordField.clear();
        loadingBox.setVisible(false);
        loginForm.setVisible(true);

        if (loginAttempts < MAX_ATTEMPTS) {
            int attemptsLeft = MAX_ATTEMPTS - loginAttempts;
            statusLabel.setText("Invalid credentials. You have " + attemptsLeft + " attempt(s) left.");
        } else {
            statusLabel.setText("Signin failed. Maximum attempts reached.");
            userTextField.setDisable(true);
            passwordField.setDisable(true);
            signInBtn.setDisable(true);
        }
    }

    /**
     * Shows the success animation and navigates to the correct dashboard based on the role.
     * @param role The role of the user ("owner" or "tenant").
     */
    private void showSuccessAndNavigate(String role) {
        loadingBox.setVisible(false);
        successBox.setVisible(true);

        PauseTransition successPause = new PauseTransition(Duration.seconds(1.5));
        successPause.setOnFinished(ev -> {
            try {
                if (role.equals("owner")) {
                    OwnerDashboard ownerDashboard = new OwnerDashboard();
                    usernameText = userTextField.getText(); // Pass username to dashboard
                    Scene dashboardScene = ownerDashboard.createScene(SigninStage, usernameText);
                    SigninStage.setScene(dashboardScene);
                    SigninStage.setTitle("SmartRent+ Owner Dashboard");
                } else if (role.equals("tenant")) {
                    TenantDashboard tenantDashboard = new TenantDashboard();
                    usernameText = userTextField.getText();
                    Scene dashboardScene = tenantDashboard.createScene(SigninStage,usernameText);
                    SigninStage.setScene(dashboardScene);
                    SigninStage.setTitle("SmartRent+ Tenant Dashboard");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                showFormState("Error: Failed to load dashboard.");
            }
        });
        successPause.play();
    }

    /**
     * Reverts the view to the login form and displays an error message.
     * @param message The error message to display.
     */
    private void showFormState(String message) {
        loadingBox.setVisible(false);
        successBox.setVisible(false);
        loginForm.setVisible(true);
        statusLabel.setText(message);
    }

    private void handleBackButton() {
        if (SigninStage != null && SigninScene != null) {
            SigninStage.setScene(SigninScene);
        }
    }

    protected void initializeSignupPage() {
        // This assumes you have a Signup class that provides the signup page UI
        Signup signup = new Signup();
        signup.setSignupStage(SigninStage);
        signupScene = new Scene(signup.createSignupPage(this::handleBackButton), 1280, 720);
        signup.setSignupScene(signupScene);
    }
}