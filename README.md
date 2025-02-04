# e-Commerce-API

An online shopping API developed with Spring Boot, providing RESTful API services. 
The API allows users to browse products, manage their baskets, complete orders, and process payments via PayPal. 
It integrates MySQL for data storage and uses Docker for containerization to simplify deployment and scalability. 

## Key Technologies:
- **Spring Boot** for the backend API
- **Spring Security** for authentication and authorization
- **JUnit5 & Mockito** for unit testing
- **JPA/Hibernate** for database interaction
- **MySQL** for persistent storage
- **OpenAPI/Swagger-UI** for API documentation
- **Docker** for containerization
- **Postman** for integration testing
- **Git** for version control

## Features:
- Secure account management with user authentication
- Real-time stock updates based on basket activity
- Basket persistence for 24 hours in case of abandonment
- REST API with full CRUD operations
- Order management with itemized breakdown
- PayPal payment integration

## How to Run the App

1. **Install Docker**:
   - Ensure Docker is installed on your computer. If not, download and install Docker from [here](https://www.docker.com/get-started).

2. **Start Docker**:
   - Open Docker Desktop and ensure it is running.

3. **Navigate to the Project Directory**:
   - Open a terminal and navigate to the directory where the project is located.

4. **Verify Docker Files**:
   - Confirm that both `Dockerfile` and `docker-compose.yml` are present in the directory.

5. **Build the Docker Image**:
   ```bash
   docker build -t ecommerce-api .
   ```
 
6. **Start the Application**:
   - Run the following command to start the containers:
   ```bash
   docker-compose up
   ```
   - This will start 3 containers:
      ecommerce_api_service on port 8080
      phpmyadmin on port 8090
      mysql on port 3306
    
## Accessing the Database

1. **Open phpMyAdmin**:
   - Open your browser and go to: `localhost:8090` to access **phpMyAdmin**.

2. **Login Credentials**:
   - On the login page, enter the following credentials (as specified in the `docker-compose.yml` file):
     - **Username**: `fabiolima`
     - **Password**: `fabiolima123`

3. **View the Database Schema**:
   - Once logged in, you will see the schema `ecommerce_db`, which stores the application's data.

## Running Integration Tests with Postman

1. **Navigate to the Postman Directory**:
   - In the project directory, locate the `Postman` folder, which contains:
     - **Environment**: Postman environment configuration files.
     - **Collections**: A set of API request files.

2. **Import Files into Postman**:
   - Open **Postman** and import both the environment and collection files.

3. **Select the Collection**:
   - Once imported, go to the **Collections** tab in Postman and select the imported collection.

4. **Run the Collection**:
   - Execute the collection to run the integration tests. These tests include scripts designed to verify that the API is functioning correctly and responding as expected.

## API Documentation with OpenAPI/Swagger

You can view and interact with the API documentation using the Swagger UI:

1. **Access Swagger UI**:
   - Open your browser and go to: `localhost:8080/swagger-ui.html`.

2. **Explore API Endpoints**:
   - The Swagger UI provides a detailed, interactive interface where you can explore all available API endpoints and test their functionality directly from your browser.
