
package com.expensetracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Scanner;

public class ExpenseTracker {

    private static String currentUser;

    private static final ArrayList<Expense> expenses = new ArrayList<>();
    private static final Scanner sc = new Scanner(System.in);

    private static int nextId = 1;

    // Generate a separate expense file for each user
    private static String getExpenseFileName() {
        return "expenses_" + currentUser + ".txt";
    }

    // Main method
    public static void main(String[] args) {

        UserAuth auth = new UserAuth(sc);

        while (true) {

            System.out.println("\n===== EXPENSE TRACKER SYSTEM =====");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {

                case "1":
                    auth.register();
                    break;

                case "2":
                    if (auth.login()) {

                        currentUser = auth.getLoggedInUsername();

                        // Clear the previous user's data from memory
                        expenses.clear();
                        nextId = 1;

                        // Load only the logged-in user's expenses
                        loadExpenses();

                        System.out.println(
                            "\nWelcome, " + currentUser + "!"
                        );

                        runExpenseMenu();

                        // Clear user data after logout
                        expenses.clear();
                        nextId = 1;
                        currentUser = null;
                    }
                    break;

                case "3":
                    System.out.println("Thank you for using Expense Tracker!");
                    sc.close();
                    return;

                default:
                    System.out.println("Invalid choice! Enter 1, 2, or 3.");
            }
        }
    }

    // Expense menu
    private static void runExpenseMenu() {

        while (true) {

            System.out.println("\n===== EXPENSE MENU =====");
            System.out.println("1. Add Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Search Expense by ID");
            System.out.println("4. Search Expenses by Category");
            System.out.println("5. Update Expense");
            System.out.println("6. Delete Expense");
            System.out.println("7. Show Total Expenses");
            System.out.println("8. Category-wise Summary");
            System.out.println("9. Show Highest Expense");
            System.out.println("10. Count Expenses");
            System.out.println("11. Logout");
            System.out.print("Enter your choice: ");

            String choice = sc.nextLine().trim();

            switch (choice) {

                case "1":
                    addExpense();
                    break;

                case "2":
                    viewExpenses();
                    break;

                case "3":
                    searchById();
                    break;

                case "4":
                    searchByCategory();
                    break;

                case "5":
                    updateExpense();
                    break;

                case "6":
                    deleteExpense();
                    break;

                case "7":
                    showTotalExpenses();
                    break;

                case "8":
                    showCategorySummary();
                    break;

                case "9":
                    showHighestExpense();
                    break;

                case "10":
                    showExpenseCount();
                    break;

                case "11":
                    System.out.println("Logged out successfully!");
                    return;

                default:
                    System.out.println(
                        "Invalid choice! Enter a number from 1 to 11."
                    );
            }
        }
    }

    // Add a new expense
    private static void addExpense() {

        System.out.println("\n===== ADD EXPENSE =====");

        double amount;

        try {
            System.out.print("Enter amount: Rs.");
            amount = Double.parseDouble(sc.nextLine().trim());

            if (!Double.isFinite(amount) || amount <= 0) {
                System.out.println("Amount must be a positive number.");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid amount!");
            return;
        }

        System.out.print("Enter category: ");
        String category = sc.nextLine().trim();

        if (category.isEmpty() || category.contains("|")) {
            System.out.println("Category cannot be empty or contain |.");
            return;
        }

        LocalDate date;

        try {
            System.out.print("Enter date (YYYY-MM-DD): ");
            date = LocalDate.parse(sc.nextLine().trim());

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date! Use YYYY-MM-DD.");
            return;
        }

        System.out.print("Enter description: ");
        String description = sc.nextLine().trim();

        if (description.isEmpty() || description.contains("|")) {
            System.out.println(
                "Description cannot be empty or contain |."
            );
            return;
        }

        Expense expense = new Expense(
            nextId, amount, category, date, description
        );

        // Save to this user's file before adding to memory
        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(
                    getExpenseFileName(),
                    StandardCharsets.UTF_8,
                    true
                ))) {

            writer.write(expense.toFileString());
            writer.newLine();

            expenses.add(expense);
            nextId++;

            System.out.println("Expense added successfully!");

        } catch (IOException e) {
            System.out.println("Could not save the expense.");
            e.printStackTrace();
        }
    }

    // Load only the current user's expenses
    private static void loadExpenses() {

        File file = new File(getExpenseFileName());

        if (!file.exists()) {
            System.out.println("No previous expenses found for this user.");
            return;
        }

        int maxId = 0;

        try (BufferedReader reader = new BufferedReader(
                new FileReader(file, StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {

                try {
                    Expense expense = Expense.fromFileString(line);

                    expenses.add(expense);

                    if (expense.getId() > maxId) {
                        maxId = expense.getId();
                    }

                } catch (Exception e) {
                    System.out.println(
                        "Skipping an invalid expense record."
                    );
                }
            }

            nextId = maxId + 1;

            System.out.println("Expenses loaded successfully.");

        } catch (IOException e) {
            System.out.println("Could not read this user's expense file.");
            e.printStackTrace();
        }
    }

    // Display all expenses
    private static void viewExpenses() {

        System.out.println("\n===== ALL EXPENSES =====");

        if (expenses.isEmpty()) {
            System.out.println("No expenses found.");
            return;
        }

        System.out.printf(
            "%-5s %-12s %-15s %-15s %-30s%n",
            "ID", "Amount", "Category", "Date", "Description"
        );

        System.out.println(
            "--------------------------------------------------------------------------"
        );

        for (Expense expense : expenses) {

            System.out.printf(
                "%-5d Rs.%-9.2f %-15s %-15s %-30s%n",
                expense.getId(),
                expense.getAmount(),
                expense.getCategory(),
                expense.getDate(),
                expense.getDescription()
            );
        }
    }

    // Find an expense by ID
    private static Expense findExpenseById(int id) {

        for (Expense expense : expenses) {
            if (expense.getId() == id) {
                return expense;
            }
        }

        return null;
    }

    // Search by ID
    private static void searchById() {

        System.out.print("Enter expense ID: ");

        try {
            int id = Integer.parseInt(sc.nextLine().trim());

            Expense expense = findExpenseById(id);

            if (expense == null) {
                System.out.println("Expense not found.");
            } else {
                System.out.println(expense);
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid ID!");
        }
    }

    // Search by category
    private static void searchByCategory() {

        System.out.print("Enter category to search: ");
        String category = sc.nextLine().trim();

        if (category.isEmpty()) {
            System.out.println("Category cannot be empty.");
            return;
        }

        boolean found = false;

        for (Expense expense : expenses) {

            if (expense.getCategory().equalsIgnoreCase(category)) {
                System.out.println(expense);
                found = true;
            }
        }

        if (!found) {
            System.out.println("No expenses found in this category.");
        }
    }

    // Rewrite the current user's expense file
    private static boolean rewriteExpenses() {

        try (BufferedWriter writer = new BufferedWriter(
                new FileWriter(
                    getExpenseFileName(),
                    StandardCharsets.UTF_8,
                    false
                ))) {

            for (Expense expense : expenses) {
                writer.write(expense.toFileString());
                writer.newLine();
            }

            return true;

        } catch (IOException e) {
            System.out.println("Could not update the expense file.");
            e.printStackTrace();
            return false;
        }
    }

    // Update an expense
    private static void updateExpense() {

        System.out.print("Enter expense ID to update: ");

        int id;

        try {
            id = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID!");
            return;
        }

        Expense expense = findExpenseById(id);

        if (expense == null) {
            System.out.println("Expense not found.");
            return;
        }

        System.out.println("Current details: " + expense);
        System.out.println("Enter the new details.");

        double amount;

        try {
            System.out.print("Enter new amount: Rs.");
            amount = Double.parseDouble(sc.nextLine().trim());

            if (!Double.isFinite(amount) || amount <= 0) {
                System.out.println("Amount must be a positive number.");
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid amount!");
            return;
        }

        System.out.print("Enter new category: ");
        String category = sc.nextLine().trim();

        if (category.isEmpty() || category.contains("|")) {
            System.out.println("Category cannot be empty or contain |.");
            return;
        }

        LocalDate date;

        try {
            System.out.print("Enter new date (YYYY-MM-DD): ");
            date = LocalDate.parse(sc.nextLine().trim());

        } catch (DateTimeParseException e) {
            System.out.println("Invalid date!");
            return;
        }

        System.out.print("Enter new description: ");
        String description = sc.nextLine().trim();

        if (description.isEmpty() || description.contains("|")) {
            System.out.println(
                "Description cannot be empty or contain |."
            );
            return;
        }

        // Keep old details in case saving fails
        double oldAmount = expense.getAmount();
        String oldCategory = expense.getCategory();
        LocalDate oldDate = expense.getDate();
        String oldDescription = expense.getDescription();

        expense.setAmount(amount);
        expense.setCategory(category);
        expense.setDate(date);
        expense.setDescription(description);

        if (rewriteExpenses()) {
            System.out.println("Expense updated successfully!");
        } else {
            // Restore old details if saving fails
            expense.setAmount(oldAmount);
            expense.setCategory(oldCategory);
            expense.setDate(oldDate);
            expense.setDescription(oldDescription);

            System.out.println("Update failed. Old details restored.");
        }
    }

    // Delete an expense
    private static void deleteExpense() {

        System.out.print("Enter expense ID to delete: ");

        int id;

        try {
            id = Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID!");
            return;
        }

        Expense expense = findExpenseById(id);

        if (expense == null) {
            System.out.println("Expense not found.");
            return;
        }

        System.out.println("Expense to delete: " + expense);
        System.out.print("Are you sure? (yes/no): ");

        String confirmation = sc.nextLine().trim();

        if (!confirmation.equalsIgnoreCase("yes")) {
            System.out.println("Deletion cancelled.");
            return;
        }

        int index = expenses.indexOf(expense);
        expenses.remove(index);

        if (rewriteExpenses()) {
            System.out.println("Expense deleted successfully!");
        } else {
            // Restore the expense if saving fails
            expenses.add(index, expense);
            System.out.println("Deletion failed. Expense restored.");
        }
    }

    // Show total expenses
    private static void showTotalExpenses() {

        double total = 0;

        for (Expense expense : expenses) {
            total += expense.getAmount();
        }

        System.out.printf("Total expenses: Rs.%.2f%n", total);
    }

    // Show category-wise summary
    private static void showCategorySummary() {

        if (expenses.isEmpty()) {
            System.out.println("No expenses available for summary.");
            return;
        }

        Map<String, Double> summary = new LinkedHashMap<>();

        for (Expense expense : expenses) {

            String category = expense.getCategory().toLowerCase();

            summary.put(
                category,
                summary.getOrDefault(category, 0.0)
                    + expense.getAmount()
            );
        }

        System.out.println("\n===== CATEGORY-WISE SUMMARY =====");

        for (Map.Entry<String, Double> entry : summary.entrySet()) {

            System.out.printf(
                "%-20s Rs.%.2f%n",
                entry.getKey(),
                entry.getValue()
            );
        }
    }

    // Show the highest expense
    private static void showHighestExpense() {

        if (expenses.isEmpty()) {
            System.out.println("No expenses available.");
            return;
        }

        Expense highest = expenses.get(0);

        for (Expense expense : expenses) {

            if (expense.getAmount() > highest.getAmount()) {
                highest = expense;
            }
        }

        System.out.println("Highest expense:");
        System.out.println(highest);
    }

    // Count expenses
    private static void showExpenseCount() {

        System.out.println(
            "Total number of expenses: " + expenses.size()
        );
    }
}
