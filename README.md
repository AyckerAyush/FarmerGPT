# FarmerGPT

**FarmerGPT** is an AI-powered agricultural assistant designed to help farmers with crop insights, disease diagnosis, and expert advice. It leverages Google's Gemini AI to provide accurate, context-aware responses and supports multi-language interactions (English & Hindi).

## Features

*   **AI Chat Interface**: Real-time Q&A with context-aware AI.
*   **Image Analysis**: Upload crop photos for disease diagnosis and advice.
*   **Multi-language Support**: Seamless toggle between English and Hindi.
*   **Secure Authentication**:
    *   Signup/Login with Auto-login.
    *   Secure "Forgot Password" flow using Security Questions (No email dependency).
    *   User Profile management.
*   **Chat History**: Saves conversation history for later reference.
*   **Shared Chats**: Generate public links to share helpful AI conversations.
*   **Premium UI**: Glassmorphism design with modern typography and animations.

## Tech Stack

*   **Backend**: Java, Spring Boot (Web, Data JPA, Security)
*   **Frontend**: Thymeleaf, HTML5, CSS3, JavaScript
*   **Database**: H2 (Development) / PostgreSQL (Production ready)
*   **AI Model**: Google Gemini 1.5 Flash (via REST API)
*   **Build Tool**: Maven

## Prerequisites

*   Java 17 or higher
*   Maven 3.6+

## Setup & specific configurations

1.  **Clone the Repository**
    ```bash
    git clone https://github.com/yourusername/FarmerGPT.git
    cd FarmerGPT
    ```

2.  **Configure Secrets**
    The project uses a `secrets.properties` file to secure sensitive keys. This file is **ignored by Git**. You must create it manually.

    Create a file at: `src/main/resources/secrets.properties`

    Add the following content:
    ```properties
    # Google Gemini API Key
    gemini.api.key=YOUR_GEMINI_API_KEY_HERE

    # Database Credentials
    db.username=sa
    db.password=password
    ```

3.  **Build the Project**
    ```bash
    mvn clean install
    ```

## Running the Application

Run the application using Maven:

```bash
mvn spring-boot:run
```

Once started, open your browser and go to:
**http://localhost:8080**

## Project Structure

*   `src/main/java`: Backend source code (Controllers, Services, Models).
*   `src/main/resources/templates`: Thymeleaf HTML templates.
*   `src/main/resources/static`: Static assets (CSS, JS, Images).
*   `data/`: Local H2 database files (git-ignored).
*   `uploads/`: User uploaded images (git-ignored).

## Default Login

If you haven't signed up, you can register a new account on the Signup page.
For development, the database restarts fresh on every run unless configured otherwise.

## License

This project is open-source and available under the simple license.
