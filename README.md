# Transporter Assignment on Lanes

A Spring Boot application that optimizes transporter allocation across multiple lanes based on transporter quotes.

## Features

- Accepts lane and transporter quote data through a REST API.
- Stores lanes, transporters, and quotes in MySQL.
- Minimizes total transportation cost.
- Maximizes transporter utilization up to a specified limit.
- Ensures every lane receives an assignment.
- Validates input and handles API errors.

## Tech Stack

- Java 21
- Spring Boot 4.1.2
- Spring Data JPA / Hibernate
- MySQL
- Maven
- JUnit 5 and Mockito
- Postman

## Prerequisites

- JDK 21 or compatible configured JDK
- MySQL 8+
- IntelliJ IDEA
- Maven support

## Database Setup

Create the database:

```sql
CREATE DATABASE freightfox_db;
```

Configure your database connection in `src/main/resources/application.properties` with your local MySQL username and password. Do not commit real credentials.

## Running the Application

Open the project in IntelliJ IDEA and run:

`TransporterAssignmentApplication.java`

The application starts at:

`http://localhost:8080`

## API Endpoints

### 1. Submit Input Data

**POST** `/api/v1/transporters/input`

Example request:

```json
{
  "lanes": [
    {
      "id": 1,
      "origin": "Mumbai",
      "destination": "Delhi"
    }
  ],
  "transporters": [
    {
      "id": 1,
      "name": "Transporter T1",
      "laneQuotes": [
        {
          "laneId": 1,
          "quote": 5000
        }
      ]
    }
  ]
}
```

Example response:

```json
{
  "status": "success",
  "message": "Input data saved successfully."
}
```

### 2. Get Optimized Assignments

**POST** `/api/v1/transporters/assignment`

Example request:

```json
{
  "maxTransporters": 3
}
```

Example response:

```json
{
  "status": "success",
  "totalCost": 5000,
  "assignments": [
    {
      "laneId": 1,
      "transporterId": 1
    }
  ],
  "selectedTransporters": [1]
}
```

## Optimization Approach

The application uses Dynamic Programming with Bitmasking.

- Each bit represents whether a transporter has been selected.
- For each lane, the algorithm considers available transporter quotes.
- It tracks the minimum cost for each transporter-selection state.
- It first maximizes the number of selected transporters up to the specified limit, then minimizes the total assignment cost.
- Every lane must receive an assignment.

The implementation limits the number of transporters to 20 because it uses bitmask states.

## Testing

The project includes unit tests for:

- Minimum-cost assignment.
- Invalid transporter limits.
- Empty lane data.
- Empty transporter data.
- Lanes without available quotes.

Run the tests using the Maven Lifecycle panel in IntelliJ IDEA.

## Author

Developed as part of the Transporter Assignment on Lanes backend assignment.