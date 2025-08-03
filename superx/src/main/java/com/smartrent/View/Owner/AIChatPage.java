package com.smartrent.View.Owner;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.smartrent.Controller.dataservice;
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
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class AIChatPage {

    private static final String API_KEY = "AIzaSyA31qdHZAQSivAn88iImkAlzk_VLKv41so"; // Replace with your actual Gemini API Key
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash-latest:generateContent?key=" + API_KEY;

    private VBox chatMessagesContainer;
    private TextField messageInput;
    private ScrollPane scrollPane;
    private Button sendButton;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private final dataservice ds;
    private final String ownerId;

    public AIChatPage(dataservice ds, String ownerId) {
        this.ds = ds;
        this.ownerId = ownerId;
    }

    public Node getView(Runnable onBackAction) {
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(20, 40, 20, 40));
        mainLayout.setStyle("-fx-background-color: #F9FAFB;");

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

        chatMessagesContainer = new VBox(10);
        chatMessagesContainer.setPadding(new Insets(20));

        scrollPane = new ScrollPane(chatMessagesContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setStyle("-fx-background: #FFFFFF; -fx-background-color: #FFFFFF; -fx-border-color: #E5E7EB; -fx-border-width: 1; -fx-border-radius: 12;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        messageInput = new TextField();
        messageInput.setPromptText("Ask about tenants, rent payments, or maintenance...");
        messageInput.setFont(Font.font("System", 14));
        messageInput.setStyle("-fx-background-radius: 20px; -fx-border-radius: 20px; -fx-border-color: #D1D5DB; -fx-padding: 8px 15px;");
        HBox.setHgrow(messageInput, Priority.ALWAYS);

        sendButton = new Button("Send");
        sendButton.setFont(Font.font("System", FontWeight.BOLD, 14));
        sendButton.setStyle("-fx-background-color: #4F46E5; -fx-text-fill: white; -fx-background-radius: 20px; -fx-padding: 8px 20px;");
        sendButton.setCursor(Cursor.HAND);

        HBox inputContainer = new HBox(10, messageInput, sendButton);
        inputContainer.setAlignment(Pos.CENTER);

        Runnable sendMessageAction = this::sendMessageAndGetResponse;
        sendButton.setOnAction(e -> sendMessageAction.run());
        messageInput.setOnAction(e -> sendMessageAction.run());

        addInitialGreeting();

        mainLayout.getChildren().addAll(header, title, scrollPane, inputContainer);

        return mainLayout;
    }

    private void sendMessageAndGetResponse() {
        String messageText = messageInput.getText().trim();
        if (messageText.isEmpty()) {
            return;
        }

        addMessage(messageText, true);
        messageInput.clear();
        showTypingIndicator(true);

        fetchContextData().thenAccept(context -> {
            String fullPrompt = context + "\n\nBased on the context above, answer the following question concisely: " + messageText;
            getGeminiResponse(fullPrompt)
                .thenAccept(aiResponse -> Platform.runLater(() -> {
                    showTypingIndicator(false);
                    addMessage(aiResponse, false);
                }))
                .exceptionally(this::handleApiError);
        }).exceptionally(this::handleApiError);
    }

    /**
    * MODIFIED: This method now fetches all data and filters it to provide context.
    */
    private CompletableFuture<String> fetchContextData() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // --- Fetch Tenant and Flat Data ---
                List<QueryDocumentSnapshot> flats = ds.getFlatsByOwner(this.ownerId);
                String tenantContext;
                if (flats.isEmpty()) {
                    tenantContext = "Property and Tenant Overview:\nYou have no properties listed.";
                } else {
                    tenantContext = "Property and Tenant Overview:\n" + flats.stream()
                        .map(doc -> {
                            String tenantName = doc.getString("tenantName");
                            if (tenantName != null && !tenantName.isEmpty()) {
                                return String.format("- Flat %s in %s is occupied by %s (Email: %s, Rent: ₹%.0f).",
                                        doc.getString("flatNo"),
                                        doc.getString("societyName"),
                                        tenantName,
                                        doc.getString("tenantEmail"),
                                        doc.getDouble("rent"));
                            } else {
                                 return String.format("- Flat %s in %s is currently vacant.",
                                        doc.getString("flatNo"),
                                        doc.getString("societyName"));
                            }
                        })
                        .collect(Collectors.joining("\n"));
                }

                // --- Fetch Maintenance Request Data ---
                List<QueryDocumentSnapshot> requests = ds.getMaintenanceRequestsByOwner(this.ownerId);
                String maintenanceContext;
                if (requests.isEmpty()) {
                    maintenanceContext = "Recent Maintenance Requests:\nThere are no recent maintenance requests.";
                } else {
                    maintenanceContext = "Recent Maintenance Requests:\n" + requests.stream()
                        .map(doc -> {
                            String society = doc.getString("societyName");
                            String flat = doc.getString("flatNo");
                            String location = (society != null && flat != null)
                                    ? String.format("%s, Flat %s", society, flat)
                                    : doc.getString("flatId"); // Fallback
                            return String.format("- Request from '%s' for '%s': '%s' (Status: %s)",
                                    doc.getString("tenantName"),
                                    location,
                                    doc.getString("description"),
                                    doc.getString("status"));
                        })
                        .collect(Collectors.joining("\n"));
                }

                // --- NEW: Fetch Rent Payment Data ---
                // FIXED: This now calls the correct two-argument method.
                List<QueryDocumentSnapshot> ownerPayments = ds.getPaymentData("Payments", this.ownerId);
                String paymentContext;
                if (ownerPayments == null || ownerPayments.isEmpty()) {
                    paymentContext = "Rent Payment History:\nNo payments found for you.";
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    paymentContext = "Rent Payment History (most recent first):\n" + ownerPayments.stream()
                        .map(doc -> String.format("- Tenant '%s' paid ₹%.0f on %s.",
                                doc.getString("tenantName"),
                                doc.getDouble("rentAmount"),
                                sdf.format(doc.getTimestamp("paymentDate").toDate())))
                        .collect(Collectors.joining("\n"));
                }

                // --- Combine all context ---
                return "Context:\n" + tenantContext + "\n\n" + maintenanceContext + "\n\n" + paymentContext;

            } catch (Exception e) {
                e.printStackTrace();
                return "Context: Could not retrieve data. Error: " + e.getMessage();
            }
        });
    }


    private Void handleApiError(Throwable ex) {
        Platform.runLater(() -> {
            showTypingIndicator(false);
            String errorMessage = "Sorry, an error occurred. Please check your API key and internet connection.";
            if (ex.getCause() != null) {
                 errorMessage += " Details: " + ex.getCause().getMessage();
            }
            addMessage(errorMessage, false);
        });
        return null;
    }

    private CompletableFuture<String> getGeminiResponse(String userMessage) {
        JSONObject textPart = new JSONObject();
        textPart.put("text", userMessage);
        JSONObject parts = new JSONObject();
        parts.put("parts", new JSONArray().put(textPart));
        JSONObject payload = new JSONObject();
        payload.put("contents", new JSONArray().put(parts));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .build();

        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() == 200) {
                        return parseResponse(response.body());
                    } else {
                        throw new RuntimeException("API request failed: " + response.body());
                    }
                });
    }

    private String parseResponse(String responseBody) {
        try {
            JSONObject jsonResponse = new JSONObject(responseBody);
            JSONArray candidates = jsonResponse.getJSONArray("candidates");
            if (candidates.length() > 0) {
                return candidates.getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
            }
            return "Sorry, I received an empty response.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Sorry, I couldn't understand the response.";
        }
    }

    private void addMessage(String message, boolean isUser) {
        Node chatBubble = createChatBubble(message, isUser);
        chatMessagesContainer.getChildren().add(chatBubble);
        Platform.runLater(() -> scrollPane.setVvalue(1.0));
    }

    private Node createChatBubble(String text, boolean isUser) {
        Label messageLabel = new Label(text);
        messageLabel.setWrapText(true);
        messageLabel.setPadding(new Insets(10, 15, 10, 15));
        messageLabel.setFont(Font.font("System", 14.5));
        VBox bubble = new VBox(messageLabel);
        bubble.setMaxWidth(500);

        if (isUser) {
            messageLabel.setTextFill(Color.WHITE);
            bubble.setStyle("-fx-background-color: #4F46E5; -fx-background-radius: 15 15 0 15;");
        } else {
            messageLabel.setTextFill(Color.web("#1F2937"));
            bubble.setStyle("-fx-background-color: #E5E7EB; -fx-background-radius: 15 15 15 0;");
        }
        HBox container = new HBox(bubble);
        container.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        return container;
    }

    private void showTypingIndicator(boolean show) {
        final String typingIndicatorId = "typing-indicator";
        if (show) {
            messageInput.setDisable(true);
            sendButton.setDisable(true);
            Node typingBubble = createChatBubble("...", false);
            typingBubble.setId(typingIndicatorId);
            chatMessagesContainer.getChildren().add(typingBubble);
            Platform.runLater(() -> scrollPane.setVvalue(1.0));
        } else {
            messageInput.setDisable(false);
            sendButton.setDisable(false);
            chatMessagesContainer.getChildren().removeIf(node -> typingIndicatorId.equals(node.getId()));
            messageInput.requestFocus();
        }
    }

    private void addInitialGreeting() {
        Platform.runLater(() -> {
            addMessage("Hello! I can provide info on your tenants, properties, rent history, and recent maintenance requests. How can I help?", false);
        });
    }
}
