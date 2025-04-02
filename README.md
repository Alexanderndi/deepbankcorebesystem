# Core Banking API - README

## Overview

This repository contains the backend implementation of a Core Banking API built with Java and Spring Boot. The API provides essential banking functionalities, including user authentication, account management, and transaction processing. It aims to be scalable, secure, and resilient, leveraging modern architectural patterns and technologies.

## Features

* **User Authentication and Authorization:**
    * User registration and login
    * JWT-based authentication
    * Role-Based Access Control (RBAC)
    * Account verification via email
* **Account Management:**
    * Account creation and retrieval
    * Balance inquiries
    * Account updates and deletion
* **Transaction Processing:**
    * Fund transfers between accounts
    * Deposits and withdrawals
    * Transaction history retrieval
    * Basic fraud detection
* **Savings & Investments:**
    * Savings plan creation and management
    * Fixed deposit creation and management
* **Notifications:**
    * Email notifications for transactions and user events
* **API Documentation:**
    * Swagger UI for interactive API exploration and documentation: [http://localhost:8989/swagger-ui/index.html](http://localhost:8989/swagger-ui/index.html)
    * Postman documentation: [https://documenter.getpostman.com/view/24872313/2sB2cREQZE](https://documenter.getpostman.com/view/24872313/2sB2cREQZE)

## Technology Stack

* Java
* Spring Boot
* Spring Security
* Spring Data JPA
* MySQL
* MailDev (for local email testing)
* Maven
* Docker (for running RabbitMQ and MailDev - if applicable)

## Architecture

The application follows a layered architecture:

* **Presentation Layer (Controllers):** Handles HTTP requests and responses.
* **Service Layer:** Contains the core business logic.
* **Data Access Layer (Repositories):** Interacts with the database.
* **Model Layer (Entities):** Represents the data structure (tables).
* **Util Layer:** Contains utility classes.
* **Config Layer:** Contains configuration classes.
* **Auth Layer:** Contains authentication-related classes.

## Setup and Installation

1.  **Prerequisites:**
    * Java Development Kit (JDK) 17 or later
    * Maven or Gradle
    * MySQL database
    * Docker (for running RabbitMQ and MailDev - if applicable)

2.  **Clone the Repository:**

    ```bash
    git clone https://github.com/Alexanderndi/deepbankcorebesystem
    cd core-banking-api
    ```

3.  **Configure Database:**

    * Create a MySQL database.
    * Update the database connection properties in `src/main/resources/application.properties` or `application.yml`:

        ```properties
        spring.datasource.url=jdbc:mysql://localhost:3306/your_database_name
        spring.datasource.username=your_username
        spring.datasource.password=your_password
        spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
        ```

4.  **Build the Application:**

    * **Maven:** `mvn clean install`
    * **Gradle:** `./gradlew clean build`

5.  **Run with Docker Compose (Recommended):**

    * Ensure you have Docker and Docker Compose installed.
    * Use the provided `docker-compose.yml` to set up RabbitMQ and MailDev.
    * Run: `docker-compose up -d`

6.  **Run Locally (Alternative):**

    * If you choose not to use Docker, ensure you have RabbitMQ and a compatible SMTP server (like MailDev) running locally.
    * Run the application from your IDE or using the built JAR file.

## API Endpoints (Examples)

* **Authentication:**
    * `POST /api/auth/register`: Register a new user.
    * `POST /api/auth/login`: Authenticate and get a JWT.
    * `POST /api/auth/verify`: Verify a user account.
* **Accounts:**
    * `POST /api/accounts`: Create a new account.
    * `GET /api/accounts/{accountId}`: Get account details.
    * `GET /api/accounts/user/{userId}`: Get accounts for a user.
    * `PUT /api/accounts/{accountId}`: Update account details.
    * `DELETE /api/accounts/{accountId}`: Delete an account.
    * `GET /api/accounts/{accountId}/balance`: Get account balance.
* **Transactions:**
    * `POST /api/transactions/transfer`: Transfer funds.
    * `POST /api/transactions/deposit/{accountId}`: Deposit funds.
    * `POST /api/transactions/withdraw/{accountId}`: Withdraw funds.
    * `GET /api/transactions/history/{accountId}`: Get transaction history.
* **Savings & Investments:**
    * `POST /api/savings-plans`: Create a new savings plan.
    * `GET /api/savings-plans`: Get all savings plans for the authenticated user.
    * `GET /api/savings-plans/{planId}`: Get a savings plan by ID.
    * `POST /api/savings-plans/{planId}/deposit`: Deposit funds into a savings plan.
    * `POST /api/savings-plans/{planId}/withdraw`: Withdraw funds from a savings plan.
    * `POST /api/fixed-deposits`: Create a new fixed deposit.
    * `GET /api/fixed-deposits`: Get all fixed deposits for the authenticated user.
    * `GET /api/fixed-deposits/{depositId}`: Get a fixed deposit by ID.
    * `POST /api/fixed-deposits/{depositId}/withdraw`: Withdraw funds from a fixed deposit.

## Security

* JWT-based authentication is used to secure API endpoints.
* Role-Based Access Control (RBAC) is implemented using Spring Security's `@PreAuthorize` annotation.

## Future Enhancements

* Implement more robust fraud detection using machine learning.
* Add support for more complex financial products.
* Improve reporting and analytics features.
* Enhance system observability.

## Contributing

* Feel free to contribute to this project by submitting pull requests.
