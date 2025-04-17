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
