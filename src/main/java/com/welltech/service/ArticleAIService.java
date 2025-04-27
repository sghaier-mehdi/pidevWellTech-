package com.welltech.service;

import com.welltech.dao.ArticleDAO;
import com.welltech.model.Article;

// === Imports needed for HTTP Client and JSON Parsing ===
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
// =====================================================

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ArticleAIService {

    private final ArticleDAO articleDAO;
    // === AI Provider Configuration for DeepSeek API ===
    // Based on DeepSeek API documentation, the endpoint is likely chat/completions
    private final String aiApiUrl = "https://openrouter.ai/api/v1/chat/completions"; // DeepSeek Chat Endpoint
    private final String aiModelName = "deepseek/deepseek-chat"; // Use OpenRouter's model identifier // DeepSeek Chat v0324 model name

    // === API Key (Required by DeepSeek API) ===
    // Get the key from environment variable
    private final String apiKey = System.getenv("DEEPSEEK_API_KEY"); // Windows: set DEEPSEEK_API_KEY=sk-or-v1...
    // ==========================================

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    // --- RAG Configuration ---
    private static final int MAX_CONTEXT_TOKENS = 3000;
    private static final int MAX_RELEVANT_ARTICLES = 5;

    // ==============================================================================


    public ArticleAIService(ArticleDAO articleDAO) {
        this.articleDAO = articleDAO;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20)) // Connection timeout
                .build();
        this.objectMapper = new ObjectMapper();

        // === API Key Check (Important for DeepSeek API) ===
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("FATAL ERROR: DEEPSEEK_API_KEY environment variable is not set!");
            System.err.println("AI Chatbot will not be able to function.");
            // In a real application, you might throw an exception here or disable the chatbot feature
        } else {
            System.out.println("ArticleAIService: DeepSeek API Key loaded."); // Debug print
        }
        // ===============================================
    }

    /**
     * Gets an AI-generated answer for a user query based on relevant articles.
     * @param userQuery The user's question.
     * @return The AI's answer string.
     */
    public String getAnswer(String userQuery) {
        // Check for API key before proceeding with the request
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "AI service is not configured. Please set the DEEPSEEK_API_KEY environment variable.";
        }


        // 1. Retrieve relevant articles based on the query
        // Use the search method from ArticleDAO
        List<Article> relevantArticles = articleDAO.searchPublishedArticlesByKeywords(userQuery.split("\\s+"), MAX_RELEVANT_ARTICLES);

        if (relevantArticles == null || relevantArticles.isEmpty()) {
            return "I couldn't find any relevant articles in the WellTech library to answer your question.";
        }

        // 2. Prepare context string from retrieved articles (limit size)
        String context = buildContextFromArticles(relevantArticles);
        if (context == null || context.trim().isEmpty()) {
            // This case means articles were found, but their content couldn't be processed into context.
            // Unlikely with current buildContext, but good to handle.
            return "Found articles, but their content was not suitable for the AI context.";
        }

        // 3. Construct the prompt for the AI model
        String requestBodyJson;
        try {
            requestBodyJson = buildApiRequestJson(context, userQuery);
            if (requestBodyJson == null) {
                // Error occurred during JSON building (handled in buildApiRequestJson)
                return "An internal error occurred while preparing the AI request.";
            }
        } catch (IllegalArgumentException e) {
            // Handle specific validation errors from buildApiRequestJson
            System.err.println("ArticleAIService: Invalid arguments for API request JSON: " + e.getMessage());
            return "An error occurred with the request: " + e.getMessage(); // User might see this
        } catch (Exception e) {
            System.err.println("ArticleAIService: Unexpected error building API request JSON: " + e.getMessage());
            e.printStackTrace();
            return "An unexpected error occurred while preparing the AI request.";
        }


        // 4. Call the AI API and get the response
        try {
            return callAiApi(requestBodyJson);

        } catch (IOException | InterruptedException e) {
            System.err.println("ArticleAIService: Network or interruption error calling AI API: " + e.getMessage());
            e.printStackTrace();
            return "Sorry, I encountered a problem communicating with the AI service. Check your internet connection.";
        } catch (Exception e) { // Catch API-specific errors (like non-200 status codes, parsing)
            System.err.println("ArticleAIService: Error response or unexpected issue from AI API: " + e.getMessage());
            e.printStackTrace();
            // Display the API error message if it's not too technical
            String displayMessage = e.getMessage();
            if (displayMessage != null && displayMessage.length() > 200) { // Truncate long messages for UI
                displayMessage = displayMessage.substring(0, 200) + "...";
            }
            return "AI service error: " + (displayMessage != null && !displayMessage.isEmpty() ? displayMessage : "Unknown API error."); // Provide more specific error
        }
    }

    /**
     * Builds the context string from the content of relevant articles.
     * Includes basic truncation to manage context window size.
     */
    private String buildContextFromArticles(List<Article> articles) {
        if (articles == null || articles.isEmpty()) return null;

        StringBuilder contextBuilder = new StringBuilder();
        int currentTokenEstimate = 0;

        for (Article article : articles) {
            if (article == null || article.getTitle() == null || article.getContent() == null) continue;

            String articleHeader = "Article Title: " + article.getTitle() + "\nContent:\n";
            String articleContent = article.getContent();
            String articleSeparator = "\n\n---\n\n";

            int headerTokenEstimate = articleHeader.length() / 4;
            int contentTokenEstimate = articleContent.length() / 4;
            int separatorTokenEstimate = articleSeparator.length() / 4;
            int totalArticleTokenEstimate = headerTokenEstimate + contentTokenEstimate + separatorTokenEstimate;


            if (currentTokenEstimate + totalArticleTokenEstimate < MAX_CONTEXT_TOKENS) {
                contextBuilder.append(articleHeader).append(articleContent).append(articleSeparator);
                currentTokenEstimate += totalArticleTokenEstimate;
            } else {
                int remainingTokens = MAX_CONTEXT_TOKENS - currentTokenEstimate;
                int remainingChars = remainingTokens * 4;

                if (remainingChars > headerTokenEstimate + separatorTokenEstimate + 50) { // Ensure enough space for header + meaningful snippet (>50 chars)
                    int charsToTakeFromContent = remainingChars - headerTokenEstimate - separatorTokenEstimate;
                    if (charsToTakeFromContent > 0) {
                        String snippet = articleContent.substring(0, Math.min(articleContent.length(), charsToTakeFromContent));
                        contextBuilder.append(articleHeader).append(snippet).append("...\n").append(articleSeparator);
                        currentTokenEstimate += headerTokenEstimate + snippet.length() / 4 + separatorTokenEstimate;
                    }
                }
                break; // Stop adding articles
            }
        }

        return contextBuilder.toString().trim(); // Trim any trailing separator/whitespace
    }


    /**
     * Constructs the final JSON request body for a Chat-Completion API.
     */
    private String buildApiRequestJson(String context, String query) throws Exception {
        if (context == null || context.trim().isEmpty()) {
            throw new IllegalArgumentException("Context for AI prompt is empty after article processing.");
        }
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query for AI prompt is empty.");
        }

        String systemMessage = "You are a helpful assistant for the WellTech Psychiatry Platform. Answer the user's question based *only* on the provided article text from the platform's library. Be concise and informative. If the answer is not found in the text, clearly state that the information is not available in the provided articles and suggest reading the articles.";

        String userMessageContent = String.format(
                "=== Provided Article Text Start ===\n%s\n=== Provided Article Text End ===\n\nQuestion: %s",
                context, query
        );

        ArrayNode messages = objectMapper.createArrayNode();

        ObjectNode systemMsg = messages.addObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemMessage);

        ObjectNode userMsg = messages.addObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessageContent);

        // For DeepSeek Chat API
        ObjectNode requestBodyJson = objectMapper.createObjectNode();
        requestBodyJson.put("model", aiModelName); // e.g., "deepseek-chat-v0324"
        requestBodyJson.set("messages", messages);
        // Optional parameters (check DeepSeek API docs):
        // requestBodyJson.put("max_tokens", 500);
        // requestBodyJson.put("temperature", 0.5);
        // requestBodyJson.put("stream", false);

        return objectMapper.writeValueAsString(requestBodyJson);
    }


    /**
     * Calls the external AI API to get a response.
     * @param requestBodyJson The JSON string request body for the API.
     * @return The AI's response text.
     * @throws IOException If a network or I/O error occurs.
     * @throws InterruptedException If the HTTP request is interrupted.
     * @throws Exception For API-specific errors (like non-200 status codes or parsing issues).
     */
    private String callAiApi(String requestBodyJson) throws IOException, InterruptedException, Exception {
        if (requestBodyJson == null) {
            throw new IllegalArgumentException("API request body is null.");
        }
        if (aiApiUrl == null || aiApiUrl.trim().isEmpty() || aiModelName == null || aiModelName.trim().isEmpty()) {
            System.err.println("ArticleAIService: AI API URL or Model Name is not configured.");
            throw new IllegalStateException("AI service is not configured.");
        }
        if (apiKey == null || apiKey.trim().isEmpty()) {
            System.err.println("ArticleAIService: API Key is not set.");
            throw new IllegalStateException("AI service API Key is missing.");
        }


        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(aiApiUrl))
                .header("Content-Type", "application/json")
                // === Add DeepSeek API Key Header ===
                // The header name for DeepSeek API is 'Authorization' with 'Bearer' token
                .header("Authorization", "Bearer " + apiKey)
                // ==================================
                .POST(HttpRequest.BodyPublishers.ofString(requestBodyJson))
                .timeout(Duration.ofSeconds(60));


        HttpRequest request = requestBuilder.build();
        System.out.println("ArticleAIService: Calling DeepSeek API: " + aiApiUrl);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("ArticleAIService: Received DeepSeek API response status: " + response.statusCode());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            JsonNode rootNode = objectMapper.readTree(response.body());
            // Path to content for DeepSeek Chat API (similar to OpenAI/Ollama)
            JsonNode contentNode = rootNode.path("choices").path(0).path("message").path("content");

            if (contentNode.isTextual()) {
                return contentNode.asText();
            } else {
                System.err.println("ArticleAIService: Unexpected JSON response structure or content type from DeepSeek API: " + response.body());
                throw new Exception("Received unexpected response format or content type from AI service.");
            }

        } else {
            String errorBody = response.body();
            String errorMessage = "DeepSeek service returned status " + response.statusCode();

            try {
                JsonNode errorNode = objectMapper.readTree(errorBody);
                String apiMessage = errorNode.path("error").path("message").asText("");
                if (apiMessage.isEmpty()) {
                    apiMessage = errorNode.path("detail").asText("");
                }
                if (apiMessage.isEmpty()) {
                    apiMessage = errorNode.path("message").asText("");
                }

                if (!apiMessage.isEmpty()) {
                    errorMessage += ": " + apiMessage;
                } else {
                    errorMessage += ". Raw response body: " + (errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody);
                }
            } catch (Exception parseException) {
                System.err.println("ArticleAIService: Could not parse DeepSeek error response body as JSON: " + errorBody);
                errorMessage += ". Raw response body: " + (errorBody.length() > 200 ? errorBody.substring(0, 200) + "..." : errorBody);
            }

            System.err.println("ArticleAIService: DeepSeek API call failed with status " + response.statusCode() + " Error: " + errorMessage);
            throw new Exception(errorMessage);
        }
    }

    // Assume ArticleDAO has searchPublishedArticlesByKeywords(String[] keywords, int limit)
}