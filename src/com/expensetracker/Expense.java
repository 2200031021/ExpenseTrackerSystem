
package com.expensetracker;

import java.time.LocalDate;

public class Expense {

    private int id;
    private double amount;
    private String category;
    private LocalDate date;
    private String description;

    // Constructor
    public Expense(int id, double amount, String category,
                   LocalDate date, String description) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    // Getter methods
    public int getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public String getCategory() {
        return category;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    // Setter methods
    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Convert expense details into a string for file storage
    public String toFileString() {
        return id + "|" + amount + "|" + category + "|"
                + date + "|" + description;
    }

    // Convert a saved file line back into an Expense object
    public static Expense fromFileString(String line) {
        String[] data = line.split("\\|", 5);

        return new Expense(
                Integer.parseInt(data[0]),
                Double.parseDouble(data[1]),
                data[2],
                LocalDate.parse(data[3]),
                data[4]
        );
    }

    // Display expense details
    @Override
    public String toString() {
        return "ID: " + id
                + " | Amount: Rs." + amount
                + " | Category: " + category
                + " | Date: " + date
                + " | Description: " + description;
    }
}
