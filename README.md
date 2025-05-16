# Backend Development for E-commerce Platform

E-Shop is a modern e-commerce platform built with Spring Boot following the RESTful API style. It enables users to browse products, manage shopping carts, and process orders. The application leverages a relational database for data persistence, Redis for caching, and JWT for secure authentication.

## Table of Contents
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Running the Application](#running-the-application)
- [Running Tests](#running-tests)
- [Tech Stack](#tech-stack)
- [License](#license)

## Prerequisites

Before you begin, ensure you have the following installed:
- **Java**: Version 17 or higher
- **Maven**: Version 3.6.0 or higher
- **Git**: For cloning the repository
- **Redis**: A running Redis instance (local)
- **Database**: A relational database (e.g., MYSQL, H2 for testing)
- **IDE**: IntelliJ IDEA
- **Tools**: Docker, Postman

## Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/kieulam191/e-shop.git
   ```

2. Navigate to the project directory:
   ```bash
   cd e-shop
   ```

3. Open the project in your IDE (e.g., IntelliJ IDEA).

## Configuration

### Environment Variables

Create a `.env` file in the project root and add the following variables:

```bash
DB_URL=<your_database_url>              # e.g., jdbc:mysql://localhost:3306/eshop
DB_USERNAME=<your_database_username>     # your_db_username
DB_PASSWORD=<your_database_password>     # your_db_password
SECRET_KEY=<your_jwt_secret_key>         # my_secure_jwt_secret
REDIS_HOST=<your_redis_host>            # e.g., localhost
REDIS_PORT=<your_redis_port>            # e.g., 6379
REDIS_PASSWORD=<your_redis_password>     # your_redis_password
```

> **Note**: Use a third-party library like dotenv-java to automatically load them during development.


> ⚠️Security:
Do not commit this file to version control (e.g., GitHub). Add .env to your .gitignore file.
Use strong, unpredictable values for passwords and secrets to enhance security. 


For running tests, create an `application-test.properties` file in `src/test/resources/`:

```properties
# H2 in-memory database for testing
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.username=sa
spring.datasource.password=
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect

# JWT secret for testing
jwt.secret=dummy_secret_key

# Redis configuration for testing
spring.cache.type=redis
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
```

## Running the Application

1. **Start Docker Desktop**

   Ensure Docker Desktop is running on your machine, especially if you are using Docker containers for services like Redis or the database.


2. **Run the application**

   In IntelliJ IDEA, right-click the `EshopApplication` class inside the `e_shop` package and select **Run 'EshopApplication.main()'**.


3. **Access the application**

   The application will start on [http://localhost:8080](http://localhost:8080).  
   Use Postman or your browser to test API endpoints.


## Running Tests

To execute the test suite:

1. Ensure the `application-test.properties` file is configured as described above.
2. Run the tests using Maven:
   ```bash
   mvn test
   ```

> **IDE Option**: In IntelliJ IDEA, right-click the `src/test/java` folder and select `Run 'All Tests'`.

## Tech Stack

- **Backend**: Spring Boot, Spring Data JPA, Spring Security 
- **Database**: MYSQL, H2 (testing)
- **Caching**: Redis
- **Authentication**: JSON Web Tokens (JWT), OWASP (BASIC)
- **Build Tool**: Maven
- **Testing**: JUnit 5, Mockito
- **Tools**: Docker, Postman

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.