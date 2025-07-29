package com.smartrent.View.Owner;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class AIChatPage {

    private VBox chatMessagesContainer;
    private TextField messageInput;
    private ScrollPane scrollPane;

    public Node getView(Runnable onBackAction) {
        // --- Main Layout ---
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20, 40, 20, 40));
        mainLayout.setStyle("-fx-background-color: #F9FAFB;");

        // --- Header ---
        Button backButton = new Button("← Back to Dashboard");
        backButton.setFont(Font.font("System", FontWeight.SEMI_BOLD, 14));
        backButton.setTextFill(Color.web("#374151"));
        backButton.setStyle("-fx-background-color: transparent;");
        backButton.setCursor(Cursor.HAND);
        backButton.setOnAction(e -> onBackAction.run());

        Label title = new Label("AI Chat Assistant");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#111827"));

        HBox header = new HBox(backButton);
        header.setAlignment(Pos.CENTER_LEFT);
        
        // --- Chat History Area (The "Dialog Box") ---
        chatMessagesContainer = new VBox(10);
        chatMessagesContainer.setPadding(new Insets(20));

        scrollPane = new ScrollPane(chatMessagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 12;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS); // Crucial for making it expand

        // --- Message Input Area ---
        messageInput = new TextField();
        messageInput.setPromptText("Ask about your properties, tenants, or payments...");
        messageInput.setFont(Font.font("System", 14));
        messageInput.setStyle("-fx-background-radius: 20px; -fx-border-radius: 20px; -fx-border-color: #D1D5DB; -fx-padding: 8px 15px;");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        Button sendButton = new Button("Send");
        sendButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        sendButton.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-background-radius: 20px; -fx-padding: 8px 20px;");
        sendButton.setCursor(Cursor.HAND);

        HBox inputContainer = new HBox(10, messageInput, sendButton);
        inputContainer.setAlignment(Pos.CENTER);

        // --- Event Handling for Sending Messages ---
        Runnable sendMessageAction = () -> {
            String messageText = messageInput.getText().trim();
            if (!messageText.isEmpty()) {
                addMessage(messageText, true); // Add user's message bubble
                messageInput.clear();
            }
        };

        sendButton.setOnAction(e -> sendMessageAction.run());
        messageInput.setOnAction(e -> sendMessageAction.run()); // Allows sending with Enter key

        // Add initial greeting message from the AI
        addInitialGreeting();
        
        // --- Assemble View ---
        mainLayout.getChildren().addAll(header, title, scrollPane, inputContainer);
        
        return mainLayout;
    }

    /**
     * Adds a message bubble to the chat container.
     * @param message The text of the message.
     * @param isUser True if the message is from the user, false if from the AI.
     */
    private void addMessage(String message, boolean isUser) {
        Node chatBubble = createChatBubble(message, isUser);
        chatMessagesContainer.getChildren().add(chatBubble);
        
        // Auto-scroll to the bottom to show the latest message
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    /**
     * Creates a styled chat bubble Node.
     */
    private Node createChatBubble(String text, boolean isUser) {
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(10, 15, 10, 15));
        messageLabel.setFont(Font.font("System", 14.5));

        VBox bubble = new VBox(messageLabel);
        bubble.setMaxWidth(500); // Max width of a bubble

        if (isUser) {
            // Style for user's message bubble
            messageLabel.setTextFill(Color.WHITE);
            bubble.setStyle("-fx-background-color: #4F46E5; -fx-background-radius: 15 15 0 15;");
        } else {
            // Style for AI's message bubble
            messageLabel.setTextFill(Color.web("#1F2937"));
            bubble.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 15 15 15 0;");
        }
        
        // Use an HBox to align the bubble to the right (user) or left (AI)
        HBox container = new HBox(bubble);
        container.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        
        return container;
    }
    
    /**
     * Adds the initial welcome message when the page loads.
     */
    private void addInitialGreeting() {
        Platform.runLater(() -> {
            addMessage("Hello! I'm your AI assistant. How can I help you today?", false);
        });
    }
}
