package com.welltech.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class CloudinaryUtil {
    private static Cloudinary cloudinary;
    private static final Properties properties = new Properties();

    static {
        try {
            // Load configuration from properties file
            String configPath = "src/main/resources/config.properties";
            try (FileInputStream fis = new FileInputStream(configPath)) {
                properties.load(fis);
            }
            
            // Use direct string values for Cloudinary initialization to avoid any formatting issues
            String cloudName = "dmeyuv2o5";
            String apiKey = "815165984455313";
            String apiSecret = "XsaOrB8hBnKYIMobGrUUc3CbfT0";
            
            // Initialize Cloudinary with hardcoded configuration
            cloudinary = new Cloudinary(
                "cloudinary://" + apiKey + ":" + apiSecret + "@" + cloudName
            );
            
            System.out.println("Cloudinary initialized with cloud name: " + cloudName);
        } catch (IOException e) {
            System.err.println("Failed to initialize Cloudinary: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Cloudinary initialization failed", e);
        }
    }

    /**
     * Upload an image file to Cloudinary
     * @param file The image file to upload
     * @return The URL of the uploaded image
     * @throws IOException If there's an error during upload
     */
    public static String uploadImage(File file) throws IOException {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }

        if (!file.exists()) {
            throw new IOException("File does not exist: " + file.getAbsolutePath());
        }

        // Validate file is an image
        String contentType = Files.probeContentType(file.toPath());
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("Selected file is not a valid image: " + file.getName());
        }

        System.out.println("Uploading file: " + file.getAbsolutePath());
        System.out.println("File size: " + file.length() + " bytes");
        System.out.println("Content type: " + contentType);

        try {
            // Generate a unique public_id to avoid collisions
            String uniqueId = UUID.randomUUID().toString().substring(0, 8);
            
            // Force JPEG format instead of WEBP for better compatibility with JavaFX
            Map<String, Object> params = ObjectUtils.asMap(
                "resource_type", "image",
                "public_id", uniqueId,
                "format", "jpg",           // Force JPEG format
                "transformation", "q_auto,f_jpg", // Force JPEG format with auto quality
                "overwrite", true,         // Overwrite existing images
                "use_filename", false,     // Don't use original filename
                "unique_filename", true,   // Ensure filename is unique
                "invalidate", true         // Invalidate CDN cache if image exists
            );
            
            // Perform upload with retry
            Map uploadResult = null;
            int maxRetries = 3;
            int retryCount = 0;
            Exception lastException = null;
            
            while (retryCount < maxRetries) {
                try {
                    uploadResult = cloudinary.uploader().upload(file, params);
                    // If we get here, upload succeeded
                    break;
                } catch (Exception e) {
                    lastException = e;
                    retryCount++;
                    System.err.println("Upload attempt " + retryCount + " failed: " + e.getMessage());
                    // Wait before retrying
                    if (retryCount < maxRetries) {
                        Thread.sleep(1000 * retryCount); // Exponential backoff
                    }
                }
            }
            
            // Check if all retries failed
            if (uploadResult == null) {
                if (lastException != null) {
                    throw lastException;
                } else {
                    throw new IOException("Failed to upload image after " + maxRetries + " attempts");
                }
            }
            
            // Get the secure URL from the result
            String secureUrl = (String) uploadResult.get("secure_url");
            
            // Modify the URL to ensure format consistency
            if (secureUrl != null && !secureUrl.contains("/f_jpg")) {
                // Find the upload part of the URL
                int uploadIndex = secureUrl.indexOf("/upload/");
                if (uploadIndex > 0) {
                    String prefix = secureUrl.substring(0, uploadIndex + 8); // after "/upload/"
                    String suffix = secureUrl.substring(uploadIndex + 8);
                    
                    // Insert format transformation
                    secureUrl = prefix + "f_jpg,q_auto/" + suffix;
                    
                    // Ensure jpg extension
                    if (secureUrl.lastIndexOf('.') > 0) {
                        secureUrl = secureUrl.substring(0, secureUrl.lastIndexOf('.')) + ".jpg";
                    } else {
                        secureUrl += ".jpg";
                    }
                }
            }
            
            System.out.println("Upload successful! Image URL: " + secureUrl);
            return secureUrl;
        } catch (Exception e) {
            System.err.println("Error uploading to Cloudinary: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Delete an image from Cloudinary
     * @param publicId The public ID of the image to delete
     * @throws IOException If there's an error during deletion
     */
    public static void deleteImage(String publicId) throws IOException {
        if (publicId == null || publicId.isEmpty()) {
            throw new IllegalArgumentException("Public ID cannot be null or empty");
        }

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            System.out.println("Successfully deleted image with public ID: " + publicId);
        } catch (Exception e) {
            System.err.println("Error deleting from Cloudinary: " + e.getMessage());
            e.printStackTrace();
            throw new IOException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }
    
    /**
     * Extract public ID from a Cloudinary URL
     * @param url The Cloudinary URL
     * @return The public ID, or null if the URL isn't a valid Cloudinary URL
     */
    public static String getPublicIdFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return null;
        }
        
        try {
            // Example URL: https://res.cloudinary.com/cloud_name/image/upload/v1234567890/folder/public_id.jpg
            String cloudName = "dmeyuv2o5";
            String pattern = "https://res.cloudinary.com/" + cloudName + "/image/upload/";
            
            if (url.startsWith(pattern)) {
                // Remove the prefix
                String remainder = url.substring(pattern.length());
                
                // Remove version if present (v1234567890/)
                if (remainder.contains("/")) {
                    if (remainder.startsWith("v")) {
                        int firstSlash = remainder.indexOf("/");
                        if (firstSlash != -1) {
                            remainder = remainder.substring(firstSlash + 1);
                        }
                    }
                }
                
                // Remove file extension
                int lastDot = remainder.lastIndexOf(".");
                if (lastDot != -1) {
                    remainder = remainder.substring(0, lastDot);
                }
                
                return remainder;
            }
        } catch (Exception e) {
            System.err.println("Error extracting public ID from URL: " + e.getMessage());
        }
        
        return null;
    }
}