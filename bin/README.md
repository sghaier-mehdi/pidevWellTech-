# WellTech Psychiatry Platform

A comprehensive JavaFX application for psychiatry services, including appointment scheduling, consultations, and patient management.

## Prerequisites

- Java JDK 17 or later
- MySQL Database Server
- Maven (or use the included Maven Wrapper)

## Setup Instructions

### 1. Install Java JDK

1. Download and install Java JDK 17 or later from:

   - Oracle: https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html
   - OpenJDK: https://adoptium.net/temurin/releases/?version=17

2. Set JAVA_HOME environment variable:
   - Windows: `set JAVA_HOME=C:\Path\To\Your\JDK`
   - Add to PATH: `%JAVA_HOME%\bin`

### 2. Database Setup

1. Install MySQL Server if not already installed
2. Create the database and tables:
   ```
   mysql -u root -p < db_setup.sql
   ```
3. Update database connection information in `src/main/java/com/welltech/util/DatabaseConnection.java` if needed

### 3. Running the Application

#### Using Maven:

```bash
# Navigate to project directory
cd welltech

# Using Maven Wrapper (Windows)
mvnw.cmd clean javafx:run

# Using Maven Wrapper (Unix)
./mvnw clean javafx:run

# Or if Maven is installed
mvn clean javafx:run
```

#### Using an IDE (IntelliJ IDEA, Eclipse, Cursor):

1. Import the project as a Maven project
2. Configure VM options:
   ```
   --module-path "path/to/javafx-sdk/lib" --add-modules javafx.controls,javafx.fxml
   ```
3. Run the main class: `com.welltech.WellTechApplication`

## Project Structure

- `src/main/java/com/welltech/model`: Data models
- `src/main/java/com/welltech/view`: View components
- `src/main/java/com/welltech/controller`: Controllers for FXML
- `src/main/java/com/welltech/dao`: Data Access Objects
- `src/main/java/com/welltech/util`: Utility classes
- `src/main/resources/fxml`: FXML view files
- `src/main/resources/css`: Stylesheets
- `src/main/resources/images`: Image assets

## Initial Login Credentials

- **Admin**:

  - Username: admin
  - Password: admin123

- **Doctor**:

  - Username: doctor
  - Password: doctor123

- **Patient**:
  - Username: patient
  - Password: patient123
