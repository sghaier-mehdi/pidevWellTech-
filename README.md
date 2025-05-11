# WellTech Psychiatry Platform

A comprehensive JavaFX application for psychiatry services, including appointment scheduling, consultations, and patient management.

## Features

- User authentication with role-based access (Admin, Psychiatrist, Patient)
- Patient and psychiatrist profile management
- Appointment scheduling and tracking
- Medical records management
- Article sharing and educational resources
- Modern, user-friendly interface

## Prerequisites

- Java JDK 11
- MySQL 8.0+
- Git

## Setup Instructions

### 1. Clone the Repository

```bash
git clone <repository-url>
cd wellTech
```

### 2. Install Java JDK 11

1. Download and install Java JDK 11 from:

   - Oracle: https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html
   - OpenJDK/Adoptium: https://adoptium.net/temurin/releases/?version=11

2. Set JAVA_HOME environment variable:
   - Windows:
     ```
     set JAVA_HOME=C:\Path\To\Your\JDK11
     ```
     Add to PATH: `%JAVA_HOME%\bin`
   - Linux/macOS:
     ```
     export JAVA_HOME=/path/to/jdk11
     export PATH=$JAVA_HOME/bin:$PATH
     ```

### 3. Database Setup

1. Install MySQL Server if not already installed
2. Log in to MySQL and create the database:
   ```
   mysql -u root -p
   CREATE DATABASE pi;
   exit
   ```
3. Import the database schema and sample data:
   ```
   mysql -u root -p pi < db_setup.sql
   ```
4. Verify database connection settings in `src/main/java/com/welltech/db/DatabaseConnection.java`:
   ```java
   private static final String URL = "jdbc:mysql://localhost:3306/pi";
   private static final String USER = "root";
   private static final String PASSWORD = "root";
   ```
   Update these values if your MySQL configuration differs.

### 4. Running the Application

```bash
# Using Maven Wrapper (Windows)
.\mvnw.cmd clean javafx:run

# Using Maven Wrapper (Unix)
./mvnw clean javafx:run

# Or if Maven is installed
mvn clean javafx:run
```

## Project Structure

- `src/main/java/com/welltech/model`: Data models and entities
- `src/main/java/com/welltech/controller`: JavaFX controllers
- `src/main/java/com/welltech/dao`: Data Access Objects for database operations
- `src/main/java/com/welltech/db`: Database connection and initialization
- `src/main/java/com/welltech/util`: Utility classes and helpers
- `src/main/resources/fxml`: FXML view files
- `src/main/resources/css`: CSS stylesheets
- `src/main/resources/images`: Image assets


## Troubleshooting

- **Java version issues**: Ensure you're using JDK 11, not a newer or older version
- **JavaFX not found**: The Maven configuration should handle dependencies, but check your JDK includes JavaFX modules
- **Database connection errors**: Verify MySQL is running and credentials are correct
- **Compilation errors**: Ensure Maven is properly configured with `.\mvnw.cmd -v`

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Security Configuration

This application uses external services that require API keys and credentials. For security reasons, these keys are not included in the repository.

### Setting Up API Keys

To run the application, you need to provide your own API keys using one of these methods:

1. **Environment Variables (Recommended)**

   Set the following environment variables:
   ```
   STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key
   STRIPE_SECRET_KEY=your_stripe_secret_key
   TWILIO_ACCOUNT_SID=your_twilio_account_sid
   TWILIO_AUTH_TOKEN=your_twilio_auth_token
   TWILIO_FROM_PHONE=your_twilio_phone_number
   CLOUDINARY_CLOUD_NAME=your_cloudinary_cloud_name
   CLOUDINARY_API_KEY=your_cloudinary_api_key
   CLOUDINARY_API_SECRET=your_cloudinary_api_secret
   ```

2. **Local Configuration File**

   Create a copy of `src/main/resources/config.properties` named `src/main/resources/config.properties.local` and add your keys:
   ```
   # Stripe API Configuration
   STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key
   STRIPE_SECRET_KEY=your_stripe_secret_key
   
   # Twilio API Configuration
   TWILIO_ACCOUNT_SID=your_twilio_account_sid
   TWILIO_AUTH_TOKEN=your_twilio_auth_token
   TWILIO_FROM_PHONE=your_twilio_phone_number
   
   # Cloudinary Configuration
   cloudinary.cloud_name=your_cloudinary_cloud_name
   cloudinary.api_key=your_cloudinary_api_key
   cloudinary.api_secret=your_cloudinary_api_secret
   ```

3. **Environment-specific .env File**

   Create a `.env.local` file in the project root with your keys:
   ```
   STRIPE_PUBLISHABLE_KEY=your_stripe_publishable_key
   STRIPE_SECRET_KEY=your_stripe_secret_key
   TWILIO_ACCOUNT_SID=your_twilio_account_sid
   TWILIO_AUTH_TOKEN=your_twilio_auth_token
   TWILIO_FROM_PHONE=your_twilio_phone_number
   cloudinary.cloud_name=your_cloudinary_cloud_name
   cloudinary.api_key=your_cloudinary_api_key
   cloudinary.api_secret=your_cloudinary_api_secret
   ```

### Important Security Notes

- Never commit API keys or credentials to the version control system
- The `.gitignore` file is configured to exclude local configuration files
- In production, always use environment variables instead of configuration files
- Rotate your API keys regularly for better security
- Use restricted API keys with only the permissions your application needs

## Running the Application

1. Ensure you have Java 11+ installed
2. Set up your API keys using one of the methods above
3. Run the application:
   ```
   mvn clean javafx:run
   ```

## Features

- Product management with image uploads using Cloudinary
- Order processing and management
- Payment integration with Stripe
- SMS notifications using Twilio

## Development

If you're developing this application:

1. Use placeholder API keys in development
2. Test with sandbox/test API keys before using production credentials
3. Check the console logs to ensure keys are loaded correctly
