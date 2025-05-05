package com.welltech.service;
 // Ensure this package exists

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.exception.ApiException; // Import specific exception

/**
 * Service class for sending SMS messages using the Twilio API.
 * Reads credentials (Account SID, Auth Token, Twilio Phone Number)
 * from system environment variables.
 * Validates recipient numbers for Tunisian E.164 format (+216xxxxxxxx).
 */
public class SmsService {

    // --- Credentials - Read securely from environment variables ---
    // These are read ONLY when the SmsService class is first loaded by Java.
    private static final String ACCOUNT_SID = System.getenv("TWILIO_ACCOUNT_SID");
    private static final String AUTH_TOKEN = System.getenv("TWILIO_AUTH_TOKEN");
    private static final String TWILIO_NUMBER = System.getenv("TWILIO_PHONE_NUMBER"); // Your Twilio provided number

    // Flag to track if initialization was successful
    private static boolean twilioInitialized = false;

    // Static initializer block: This code runs automatically ONCE when the class is loaded.
    static {
        System.out.println("Attempting to initialize Twilio client...");

        // Validate that environment variables were actually found and are not blank
        if (ACCOUNT_SID == null || AUTH_TOKEN == null || TWILIO_NUMBER == null ||
                ACCOUNT_SID.isBlank() || AUTH_TOKEN.isBlank() || TWILIO_NUMBER.isBlank()) {

            System.err.println("*********************************************************************");
            System.err.println("FATAL ERROR: Twilio environment variables missing or empty!");
            System.err.println("Please set TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN, and TWILIO_PHONE_NUMBER");
            System.err.println("in your IDE's Run Configuration or System Environment.");
            System.err.println("SMS Functionality will be DISABLED.");
            System.err.println("*********************************************************************");
            twilioInitialized = false; // Ensure flag is false

        } else {
            // Credentials seem to be present, attempt initialization
            try {
                System.out.println("Found Twilio credentials, initializing...");
                // Initialize the Twilio library with your credentials
                Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                twilioInitialized = true; // Set flag to true ONLY on successful initialization
                System.out.println("Twilio client initialized successfully.");
            } catch (Exception e) {
                // Catch potential errors during Twilio.init() itself (e.g., authentication failure)
                System.err.println("*********************************************************************");
                System.err.println("FATAL ERROR: Failed to initialize Twilio client library:");
                System.err.println("Message: " + e.getMessage());
                System.err.println("Check Account SID/Auth Token validity and network connection.");
                System.err.println("SMS Functionality will be DISABLED.");
                System.err.println("*********************************************************************");
                twilioInitialized = false; // Ensure flag is false on error
                // e.printStackTrace(); // Uncomment for full stack trace if needed for debugging init
            }
        }
    }

    /**
     * Sends an SMS message using Twilio, specifically validating for Tunisian numbers.
     *
     * @param toPhoneNumber The recipient's phone number **MUST be in E.164 format**
     *                      For Tunisia: +216 followed by 8 digits (e.g., "+21622123456").
     * @param messageBody   The text message content (keep concise for SMS).
     * @return true if the message request was successfully sent to the Twilio API, false otherwise.
     */
    public boolean sendSms(String toPhoneNumber, String messageBody) {
        // 1. Check if Twilio initialization succeeded
        if (!twilioInitialized) {
            System.err.println("SMS Sending Skipped: Twilio client was not initialized (check application startup logs for errors).");
            return false;
        }

        // 2. Validate the recipient phone number format for Tunisian E.164
        //    Regex: starts with literal '+216' followed by exactly 8 digits (\d{8})
        if (toPhoneNumber == null || !toPhoneNumber.matches("^\\+216\\d{8}$")) {
            System.err.println("SMS Sending Failed: Invalid 'to' phone number format for Tunisia.");
            System.err.println("Expected: +216 followed by 8 digits (e.g., +21622123456).");
            System.err.println("Provided: [" + toPhoneNumber + "]");
            // Consider logging the message body too for context if needed: System.err.println("Message: " + messageBody);
            return false;
        }
        // No trimming needed here as regex ensures no leading/trailing spaces

        // 3. Validate the message body (cannot be null or only whitespace)
        if (messageBody == null || messageBody.isBlank()) {
            System.err.println("SMS Sending Failed: Message body cannot be empty.");
            return false;
        }

        // 4. Attempt to send the SMS via Twilio API
        System.out.println("Attempting to send SMS via Twilio to: " + toPhoneNumber);
        try {
            // Create the Twilio message object
            Message message = Message.creator(
                            new com.twilio.type.PhoneNumber(toPhoneNumber), // Use fully qualified name or ensure correct import
                            new com.twilio.type.PhoneNumber(TWILIO_NUMBER), // Your Twilio number from env var
                            messageBody)                                   // The message text
                    .create(); // This makes the API call to Twilio

            // Log success if the API call to Twilio didn't throw an exception
            // The SID is Twilio's unique ID for this message attempt.
            // Status is often 'queued' or 'sending' initially, delivery confirmation is asynchronous.
            System.out.println("Twilio API request successful! SID: " + message.getSid() + ", Status: " + message.getStatus());
            return true; // Indicate the API request was accepted by Twilio

        } catch (ApiException e) {
            // Handle specific errors returned BY the Twilio API server
            System.err.println("*********************************************************************");
            System.err.println("SMS Sending Failed: Twilio API Error!");
            System.err.println("Twilio Error Code: " + e.getCode()); // The numerical error code from Twilio
            System.err.println("Twilio Message: " + e.getMessage()); // The error description from Twilio
            // Provide hints based on common error codes:
            if (e.getCode() == 21211) { System.err.println("Hint: The 'To' phone number ["+toPhoneNumber+"] might be invalid, non-existent, not SMS-capable, or not formatted correctly (E.164 required)."); }
            if (e.getCode() == 21614) { System.err.println("Hint: Using a Twilio trial account? The 'To' number ["+toPhoneNumber+"] must be verified in your Twilio console first."); }
            if (e.getCode() == 20003) { System.err.println("Hint: Authentication failed. Double-check your TWILIO_ACCOUNT_SID and TWILIO_AUTH_TOKEN environment variables are correct."); }
            if (e.getCode() == 21608) { System.err.println("Hint: The 'From' number ["+TWILIO_NUMBER+"] is not a valid Twilio number you own, is not SMS-capable, or isn't configured correctly in your Twilio account."); }
            if (e.getCode() == 21704) { System.err.println("Hint: The 'To' number ["+toPhoneNumber+"] might be blocked by carrier settings or is a type (like landline) that cannot receive SMS from your 'From' number."); }
            if (e.getCode() == 21610) { System.err.println("Hint: Attempting to send SMS to an unsupported region or a blacklisted number?"); }
            System.err.println("See Twilio Docs for error code " + e.getCode() + ": https://www.twilio.com/docs/api/errors/" + e.getCode());
            System.err.println("*********************************************************************");
            // e.printStackTrace(); // Uncomment for full stack trace if needed for deeper debugging
            return false; // Indicate failure
        } catch (Exception e) {
            // Handle other potential exceptions during the process (e.g., network issues, library problems)
            System.err.println("*********************************************************************");
            System.err.println("SMS Sending Failed: An unexpected error occurred before or during API call.");
            System.err.println("Error Type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("*********************************************************************");
            e.printStackTrace(); // Print stack trace for unexpected errors
            return false; // Indicate failure
        }
    }
}