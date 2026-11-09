# 🏨 Hotel Room Booking Management System

## 📌 Project Overview

The **Hotel Room Booking Management System** is a desktop-based application developed using Java Swing and SQLite. The primary objective of this project is to simplify and automate hotel management operations, including room management, customer registration, room reservations, check-in, check-out, and bill generation.

The application provides a user-friendly graphical interface and uses JDBC for database connectivity to ensure that all customer, room, and reservation details are stored and managed efficiently.

## 🎯 Objectives

- To automate hotel room booking and reservation activities.
- To maintain customer records and room information.
- To manage room availability and prevent overlapping reservations.
- To simplify customer check-in and check-out processes.
- To calculate accommodation charges based on the duration of stay.
- To generate customer bills and maintain payment records.

## ✨ Key Features

### 1. Dashboard
- Displays total rooms, available rooms, occupied rooms, and customers.
- Shows active reservations and revenue information.
- Provides easy navigation to different application modules.

### 2. Room Management
- Add, update, delete, and search room details.
- Manage room categories such as Single, Double, Deluxe, and Suite.
- Maintain room capacity and pricing information.
- Track room availability and maintenance status.

### 3. Customer Management
- Register new customers.
- Store personal and contact information.
- Update and search customer records.
- Maintain customer identity details.

### 4. Room Booking and Reservation
- Create and manage room reservations.
- Check room availability for selected dates.
- Calculate the number of nights automatically.
- Calculate estimated accommodation charges.
- Prevent overlapping room reservations.

### 5. Check-in and Check-out
- Manage customer check-in and check-out activities.
- Update reservation status.
- Record actual check-in and check-out date and time.
- Calculate final accommodation charges.

### 6. Billing and Payment
- Generate customer invoices.
- Calculate accommodation charges automatically.
- Include applicable taxes and discounts.
- Maintain payment status and payment methods.
- Print customer bills.

## 🛠️ Technologies Used

| Technology | Purpose |
|---|---|
| Java | Core Programming Language |
| Java Swing | Graphical User Interface |
| SQLite | Database Management |
| JDBC | Database Connectivity |
| Maven | Dependency Management |
| Java 17+ | Development Environment |

## 🗄️ Database Design

The application uses SQLite as its backend database.

The database consists of the following main tables:

- **Rooms:** Stores room number, room type, capacity, price, and status.
- **Customers:** Stores customer details and contact information.
- **Reservations:** Stores booking details, reservation dates, and booking status.
- **Bills:** Stores invoice details, accommodation charges, taxes, discounts, and payment information.

Primary keys and foreign keys are used to maintain relationships between database tables.

## 🏗️ Project Architecture

The project follows a layered architecture to maintain code readability and modularity.

- **Model Layer:** Represents application entities such as Room, Customer, Reservation, and Bill.
- **DAO Layer:** Handles database operations.
- **Service Layer:** Contains business logic and validation.
- **UI Layer:** Implements the Java Swing graphical interface.
- **Database Layer:** Manages SQLite database connectivity and initialization.
- **Utility Layer:** Contains common validation, date, and billing utilities.

## ⚙️ Installation and Execution

### Prerequisites

- Java Development Kit (JDK 17 or later)
- IntelliJ IDEA / Eclipse / NetBeans
- Maven
- SQLite JDBC Driver

### Steps to Run

1. Clone this repository:

   ```bash
   git clone https://github.com/YOUR-USERNAME/Hotel-Room-Booking-Management-System.git
   ```

2. Open the project in your preferred Java IDE.

3. Import the project as a Maven project.

4. Allow Maven to download the required dependencies.

5. Locate the `Main.java` file.

6. Run the application.

7. The SQLite database and required tables are initialized automatically when the application starts.

## 📸 Application Screenshots

Screenshots of the application interface can be added here.

- Dashboard
- Room Management
- Customer Management
- Room Booking
- Check-in and Check-out
- Billing and Invoice Generation

## 🔮 Future Enhancements

- Online room booking integration.
- Email confirmation for reservations.
- Advanced reports and revenue analytics.
- Role-based login for administrators and hotel staff.
- PDF invoice generation.
- Integration with online payment gateways.

## 🎓 Project Information

**Project Title:** Hotel Room Booking Management System

**Project Type:** Java Desktop Application

**Domain:** Hotel Management System

**Frontend:** Java Swing

**Backend:** SQLite Database

**Database Connectivity:** JDBC

## 👨‍💻 Author

**Name:** Apurba Kayet

**Project:** Hotel Room Booking Management System

**Language:** Java

---

⭐ If you find this project useful, consider giving the repository a star!

