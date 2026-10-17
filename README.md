
# Expense Tracker System

A console-based expense management application developed using Core Java and file handling.

## Features

- User registration and login
- Password hashing
- Hidden password entry during login
- Add, view, search, update, and delete expenses
- Total and category-wise expense reports
- Highest expense and expense count
- Separate expense files for each user
- Persistent storage using text files

## Technologies Used

- Java
- Object-Oriented Programming
- Java Collections Framework
- File Handling
- Exception Handling
- Java Swing (hidden-password dialog)

## Project Structure

- `Expense.java` - Expense data model
- `ExpenseTracker.java` - Main menu and expense operations
- `UserAuth.java` - Registration and login

## How to Run

1. Install a JDK.
2. Import the project into Eclipse.
3. Run `ExpenseTracker.java` as a Java Application.
4. Register an account and log in.

## Storage

User accounts are stored in `users.txt`.
Each user's expenses are stored in a separate text file.

The application uses local file storage and does not require a database.
