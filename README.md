e-Commerce API

A complete e-Commerce API that allows users to browse and select products, add them to a basket, checkout, and pay using PayPal.

Overview

The API is built using Spring Boot and features Spring Security for authentication and authorization. Upon login, a basket is created (if not already active), allowing users to add products. If a new user wants to access the platform, they must register, with built-in validation mechanisms ensuring secure input handling.

The stock updates in real-time whenever a product is added to or removed from the basket.

If a user abandons their basket, it remains active for 24 hours before being deactivated, restoring items to stock.

A successful checkout generates an order containing all item details, quantities, and total price.

Users can complete purchases through PayPal, integrating a secure payment gateway.

Key Technologies

Spring Boot - Backend framework

Spring Security - Authentication and authorization

JUnit 5 & Mockito - Unit testing

JPA/Hibernate - ORM for database interaction

MySQL - Persistent storage

OpenAPI/Swagger-UI - API documentation

Docker - Containerization

Postman - API integration testing

Git - Version control

Features

Secure account management with user authentication

Real-time stock updates based on basket activity

Basket persistence for 24 hours in case of abandonment

REST API with full CRUD operations

Order management with itemized breakdown

PayPal payment integration

How to Run the Application

Prerequisites

Ensure you have Docker installed. If not, download and install Docker from here.

Steps to Run

Start Docker: Open Docker Desktop and ensure it's running.

Navigate to the Project Directory:

cd /path/to/your/project

Verify Required Files: Ensure Dockerfile and docker-compose.yml are present.

Build the Docker Image:

docker build -t ecommerce-api .

Start the Application:

docker-compose up

This starts the following containers:

ecommerce_api_service on port 8080

phpMyAdmin on port 8090

MySQL on port 3306

Accessing the Database

Open phpMyAdmin

Open your browser and go to: localhost:8090

Login Credentials

Username: ecommerce_admin

Password: ecommerce123

Database Schema

The database schema ecommerce_db stores all application data.

Running Integration Tests with Postman

Steps:

Locate the Postman directory in the project folder.

Open Postman and import:

Environment configuration files.

API request collections.

Select the imported collection and execute tests to verify API responses.

API Documentation (Swagger-UI)

You can view and interact with the API using Swagger UI.

Accessing Swagger UI

Open your browser and go to: localhost:8080/swagger-ui.html

Use the interactive interface to test API endpoints.

Contributing

If you'd like to contribute:

Fork the repository.

Create a new branch (feature-branch-name).

Commit your changes and push the branch.

Open a Pull Request for review.

License

This project is licensed under the MIT License.

Contact

For any inquiries or support, reach out via email: fabiosdl85@gmail.com
