package com.welltech.dao;

// Make sure these imports are correct for your project structure
import com.welltech.db.DatabaseConnection;
import com.welltech.model.Article;
import com.welltech.model.Category; // *** Import Category ***
import java.util.Arrays;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ArticleDAO {

    // --- Helper Method to Extract Article ---
// Updated to handle Category object and join results
    private Article extractArticleFromResultSet(ResultSet rs) throws SQLException {
        int articleId = rs.getInt("a_id"); // Use alias
        String title = rs.getString("a_title");
        String content = rs.getString("a_content");
        int authorId = rs.getInt("a_author_id");
        String authorName = rs.getString("author_name"); // From users table join
        boolean isPublished = rs.getBoolean("a_is_published");
        String imageUrl = rs.getString("a_image_url"); // Optional image url

        // Handle Timestamps -> LocalDateTime
        Timestamp createdTimestamp = rs.getTimestamp("a_created_at");
        LocalDateTime createdAt = (createdTimestamp != null) ? createdTimestamp.toLocalDateTime() : null;
        Timestamp updatedTimestamp = rs.getTimestamp("a_updated_at");
        LocalDateTime updatedAt = (updatedTimestamp != null) ? updatedTimestamp.toLocalDateTime() : null;

        // --- Category Handling ---
        int categoryId = rs.getInt("c_id"); // Use alias
        Category category = null;
        if (!rs.wasNull()) { // Check if category_id was not NULL in the DB
            String categoryName = rs.getString("c_name"); // Use alias
            if (categoryName != null) { // Ensure name is not null (important for LEFT JOIN)
                category = new Category(categoryId, categoryName);
            }
        }
        // --- End Category Handling ---

        return new Article(articleId, title, content, authorId, authorName,
                category, createdAt, updatedAt, isPublished, imageUrl);
    }

    // --- Base SQL Query Fragment with Joins ---
// Define this once to avoid repetition and ensure consistency
    private final String BASE_ARTICLE_QUERY =
            "SELECT " +
                    "a.id AS a_id, a.title AS a_title, a.content AS a_content, a.author_id AS a_author_id, " +
                    "a.category_id AS a_category_id, a.created_at AS a_created_at, a.updated_at AS a_updated_at, " +
                    "a.is_published AS a_is_published, a.image_url AS a_image_url, " + // Select image_url if you have it
                    "u.full_name AS author_name, " +
                    "c.id AS c_id, c.name AS c_name " + // Select category id and name
                    "FROM articles a " +
                    "JOIN users u ON a.author_id = u.id " + // Assuming 'users' table with 'id' and 'full_name'
                    "LEFT JOIN category c ON a.category_id = c.id "; // LEFT JOIN to include articles even if category is NULL


// --- CRUD Methods ---

    /**
     * Insert a new article into the database.
     * Requires category_id now.
     * @param article Article to insert (should have Category object set)
     * @return ID of the inserted article, or -1 if insertion failed
     */
    public int insertArticle(Article article) {
        // *** Updated query to insert category_id ***
        // Add image_url if you included it
        String query = "INSERT INTO articles (title, content, author_id, category_id, is_published, image_url) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, article.getTitle());
            statement.setString(2, article.getContent());
            statement.setInt(3, article.getAuthorId());

            // --- Handle Category ID ---
            if (article.getCategory() != null && article.getCategory().getId() > 0) {
                statement.setInt(4, article.getCategory().getId());
            } else {
                statement.setNull(4, Types.INTEGER); // Set category_id to NULL if no category selected
            }
            // --- End Category ID Handling ---

            statement.setBoolean(5, article.isPublished());
            statement.setString(6, article.getImageUrl()); // Handle image_url

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newId = generatedKeys.getInt(1);
                        article.setId(newId); // Set the ID back on the object
                        return newId;
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error inserting article: " + e.getMessage());
            e.printStackTrace();
        }

        return -1; // Return -1 on failure
    }

    /**
     * Update an existing article in the database.
     * @param article Article to update
     * @return true if the update was successful
     */
    public boolean updateArticle(Article article) {
        // *** Updated query to update category_id ***
        // Add image_url if you included it
        String query = "UPDATE articles SET title = ?, content = ?, category_id = ?, is_published = ?, image_url = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";

        if (article.getId() <= 0) {
            System.err.println("Error updating article: Invalid ID (" + article.getId() + ")");
            return false;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setString(1, article.getTitle());
            statement.setString(2, article.getContent());

            // --- Handle Category ID ---
            if (article.getCategory() != null && article.getCategory().getId() > 0) {
                statement.setInt(3, article.getCategory().getId());
            } else {
                statement.setNull(3, Types.INTEGER);
            }
            // --- End Category ID Handling ---

            statement.setBoolean(4, article.isPublished());
            statement.setString(5, article.getImageUrl()); // Handle image_url
            // We skip author_id as it usually doesn't change
            // updated_at is handled by CURRENT_TIMESTAMP in the query
            statement.setInt(6, article.getId()); // WHERE clause

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error updating article (ID: " + article.getId() + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Delete an article from the database
     * @param articleId ID of the article to delete
     * @return true if the deletion was successful
     */
    public boolean deleteArticle(int articleId) {
        // This method doesn't need changes related to category handling
        String query = "DELETE FROM articles WHERE id = ?";

        if (articleId <= 0) {
            System.err.println("Error deleting article: Invalid ID (" + articleId + ")");
            return false;
        }

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, articleId);

            int affectedRows = statement.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error deleting article (ID: " + articleId + "): " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get an article by ID, including category info.
     * @param articleId ID of the article to get
     * @return Article object if found, null otherwise
     */
    public Article getArticleById(int articleId) {
        // Use the BASE_ARTICLE_QUERY and add WHERE clause
        String query = BASE_ARTICLE_QUERY + " WHERE a.id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, articleId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return extractArticleFromResultSet(resultSet);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting article by ID (" + articleId + "): " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Get all articles, including category info.
     * @return List of all articles
     */
    public List<Article> getAllArticles() {
        List<Article> articles = new ArrayList<>();
        // Use BASE_ARTICLE_QUERY and add ORDER BY
        String query = BASE_ARTICLE_QUERY + " ORDER BY a.created_at DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement(); // Use simple Statement
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                articles.add(extractArticleFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Error getting all articles: " + e.getMessage());
            e.printStackTrace();
        }

        return articles;
    }

    /**
     * Get all published articles, including category info.
     * @return List of published articles
     */
    public List<Article> getPublishedArticles() {
        List<Article> articles = new ArrayList<>();
        // Use BASE_ARTICLE_QUERY and add WHERE/ORDER BY
        String query = BASE_ARTICLE_QUERY + " WHERE a.is_published = TRUE ORDER BY a.created_at DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            while (resultSet.next()) {
                articles.add(extractArticleFromResultSet(resultSet));
            }

        } catch (SQLException e) {
            System.err.println("Error getting published articles: " + e.getMessage());
            e.printStackTrace();
        }

        return articles;
    }

    /**
     * Get articles by author, including category info.
     * @param authorId ID of the author
     * @return List of articles by the author
     */
    public List<Article> getArticlesByAuthor(int authorId) {
        List<Article> articles = new ArrayList<>();
        // Use BASE_ARTICLE_QUERY and add WHERE/ORDER BY
        String query = BASE_ARTICLE_QUERY + " WHERE a.author_id = ? ORDER BY a.created_at DESC";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {

            statement.setInt(1, authorId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    articles.add(extractArticleFromResultSet(resultSet));
                }
            }

        } catch (SQLException e) {
            System.err.println("Error getting articles by author (" + authorId + "): " + e.getMessage());
            e.printStackTrace();
        }

        return articles;
    }

// Add other specific query methods if needed (e.g., search, filter by category)


    // === ADD THIS METHOD FOR SEARCHING PUBLISHED ARTICLES ===
    /**
     * Searches published articles by keywords in title or content.
     * @param keywords An array of keywords to search for.
     * @param limit The maximum number of articles to return.
     * @return A list of matching published articles.
     */
    public List<Article> searchPublishedArticlesByKeywords(String[] keywords, int limit) {
        List<Article> articles = new ArrayList<>();
        if (keywords == null || keywords.length == 0) {
            System.out.println("ArticleDAO: No keywords provided for search."); // Debug
            return articles; // Return empty list if no keywords
        }

        StringBuilder sqlBuilder = new StringBuilder(BASE_ARTICLE_QUERY); // Start with base query
        sqlBuilder.append(" WHERE a.is_published = TRUE AND ("); // Only search published articles

        // Append conditions for each keyword (search in title OR content)
        for (int i = 0; i < keywords.length; i++) {
            sqlBuilder.append("(LOWER(a.title) LIKE ? OR LOWER(a.content) LIKE ?)");
            if (i < keywords.length - 1) {
                sqlBuilder.append(" OR ");
            }
        }
        sqlBuilder.append(")"); // Close the WHERE clause parentheses

        // Add ordering (e.g., by updated date) and limit
        sqlBuilder.append(" ORDER BY a.updated_at DESC LIMIT ?");

        String sql = sqlBuilder.toString();
        System.out.println("ArticleDAO: Executing search query: " + sql + " with keywords: " + Arrays.toString(keywords)); // Debug query

        try (Connection conn = DatabaseConnection.getConnection(); // Use your DB connection class
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            int paramIndex = 1;
            // Set parameters for each keyword (two placeholders per keyword)
            for (String keyword : keywords) {
                String pattern = "%" + keyword.toLowerCase() + "%"; // Use lowercase for case-insensitive search
                pstmt.setString(paramIndex++, pattern); // For title LIKE
                pstmt.setString(paramIndex++, pattern); // For content LIKE
            }
            pstmt.setInt(paramIndex, limit); // Set the limit parameter

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    articles.add(extractArticleFromResultSet(rs)); // Use the helper method
                }
            }

        } catch (SQLException e) {
            System.err.println("ArticleDAO: SQL Error searching articles: " + e.getMessage());
            e.printStackTrace(); // Handle appropriately
        }
        System.out.println("ArticleDAO: searchPublishedArticlesByKeywords returned " + articles.size() + " articles."); // Debug print
        return articles;
    }
    // ==========================================================
}