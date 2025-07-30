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
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class AIChatPage {

    // --- IMPORTANT: REPLACE WITH YOUR GOOGLE AI STUDIO API KEY ---
    private static final String API_KEY = "AIzaSyA31qdHZAQSivAn88iImkAlzk_VLKv41so";
    // --- FIXED: Updated the model name to a valid and current one ---
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + API_KEY;

    private VBox chatMessagesContainer;
    private TextField messageInput;
    private ScrollPane scrollPane;
    private Button sendButton;
    private final HttpClient httpClient = HttpClient.newHttpClient();

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

        // --- Chat History Area ---
        chatMessagesContainer = new VBox(10);
        chatMessagesContainer.setPadding(new Insets(20));

        scrollPane = new ScrollPane(chatMessagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 12;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // --- Message Input Area ---
        messageInput = new TextField();
        messageInput.setPromptText("Ask about your properties, tenants, or payments...");
        messageInput.setFont(Font.font("System", 14));
        messageInput.setStyle("-fx-background-radius: 20px; -fx-border-radius: 20px; -fx-border-color: #D1D5DB; -fx-padding: 8px 15px;");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        sendButton = new Button("Send");
        sendButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        sendButton.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-background-radius: 20px; -fx-padding: 8px 20px;");
        sendButton.setCursor(Cursor.HAND);

        HBox inputContainer = new HBox(10, messageInput, sendButton);
        inputContainer.setAlignment(Pos.CENTER);

        // --- Event Handling ---
        Runnable sendMessageAction = this::sendMessageAndGetResponse;
        sendButton.setOnAction(e -> sendMessageAction.run());
        messageInput.setOnAction(e -> sendMessageAction.run());

        addInitialGreeting();

        // --- Assemble View ---
        mainLayout.getChildren().addAll(header, title, scrollPane, inputContainer);

        return mainLayout;
    }

    /**
     * Handles sending the user's message, displaying it, and fetching the AI response.
     */
    private void sendMessageAndGetResponse() {
        String messageText = messageInput.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }

        // 1. Add the user's message to the chat UI
        addMessage(messageText, true);
        messageInput.clear();

        // 2. Show a "typing" indicator and disable input
        showTypingIndicator(true);

        // 3. Call the Gemini API in a background thread
        getGeminiResponse(messageText).thenAccept(aiResponse -> {
            // 4. When the response arrives, update the UI on the JavaFX thread
            Platform.runLater(() -> {
                showTypingIndicator(false); // Hide "typing" indicator
                addMessage(aiResponse, false); // Add AI's response bubble
            });
        }).exceptionally(ex -> {
            // Handle any errors during the API call
            Platform.runLater(() -> {
                showTypingIndicator(false);
                addMessage("Sorry, I couldn't get a response. Please check your API key and internet connection. Error: " + ex.getMessage(), false);
            });
            return null;
        });
    }

    /**
     * Sends a prompt to the Gemini API and returns the response.
     * @param userMessage The user's message to send to the AI.
     * @return A CompletableFuture that will complete with the AI's text response.
     */
    private CompletableFuture<String> getGeminiResponse(String userMessage) {
        // Create a JSON payload following the Gemini API's structure
        JSONObject textPart = new JSONObject();
        textPart.put("text", userMessage);

        JSONObject parts = new JSONObject();
        parts.put("parts", new JSONArray().put(textPart));

        JSONObject payload = new JSONObject();
        payload.put("contents", new JSONArray().put(parts));

        // Build the HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        // Send the request asynchronously and process the response
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseResponse(response.body());
                    } else {
                        // If the API returns an error, pass the error message forward
                        throw new RuntimeException("API request failed with status code " + response.statusCode() + ": " + response.body());
                    }
                });
    }

    /**
     * Parses the JSON response from the Gemini API to extract the content.
     * @param responseBody The JSON string from the API.
     * @return The extracted text content from the AI.
     */
    private String parseResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() > 0) {
                JSONObject firstCandidate = candidates.getJSONObject(0);
                JSONObject content = firstCandidate.getJSONObject("content");
                JSONArray parts = content.getJSONArray("parts");
                if (parts.length() > 0) {
                    return parts.getJSONObject(0).getString("text");
                }
            }
            return "Sorry, I received an empty response.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't understand the response.";
        }
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
     * Shows or hides a "typing..." indicator and disables/enables the input field.
     */
    private void showTypingIndicator(boolean show) {
        final String typingIndicatorId = "typing-indicator";

        if (show) {
            messageInput.setDisable(true);
            sendButton.setDisable(true);

            Node typingBubble = createChatBubble("...", false);
            typingBubble.setId(typingIndicatorId); // Set an ID to find it later
            chatMessagesContainer.getChildren().add(typingBubble);
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        } else {
            messageInput.setDisable(false);
            sendButton.setDisable(false);
            // Remove the typing indicator by its ID
            chatMessagesContainer.getChildren().removeIf(node -> typingIndicatorId.equals(node.getId()));
            messageInput.requestFocus(); // Set focus back to the input field
        }
    }

    /**
     * Adds the initial welcome message when the page loads.
     */
    private void addInitialGreeting() {
        Platform.runLater(() -> {
            addMessage("Hello! I'm your AI assistant powered by Gemini. How can I help you today?", false);
        });
    }
}
