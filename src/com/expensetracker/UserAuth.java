
package com.expensetracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Scanner;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

public class UserAuth {

    private static final String USER_FILE = "users.txt";

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private final Scanner sc;
    private String loggedInUsername;

    public UserAuth(Scanner sc) {
        this.sc = sc;
    }

    // Register a new user
    public void register() {

        System.out.println("\n===== USER REGISTRATION =====");

        System.out.print("Enter username (3-30 letters, numbers or _): ");
        String username = sc.nextLine().trim();

        if (!username.matches("[A-Za-z0-9_]{3,30}")) {
            System.out.println("Invalid username!");
            System.out.println(
                "Use 3-30 letters, numbers, or underscores."
            );
            return;
        }

        if (userExists(username)) {
            System.out.println("Username already exists!");
            return;
        }

        System.out.print("Enter password (8-128 characters): ");
        char[] password = sc.nextLine().toCharArray();

        try {
            if (password.length < 8 || password.length > 128) {
                System.out.println(
                    "Password must contain 8-128 characters."
                );
                return;
            }

            System.out.print("Confirm password: ");
            char[] confirmPassword = sc.nextLine().toCharArray();

            try {
                if (!Arrays.equals(password, confirmPassword)) {
                    System.out.println("Passwords do not match!");
                    return;
                }
            } finally {
                Arrays.fill(confirmPassword, '\0');
            }

            byte[] salt = new byte[SALT_LENGTH];
            new SecureRandom().nextBytes(salt);

            byte[] passwordHash = hashPassword(password, salt);

            String saltText = Base64.getEncoder()
                                    .encodeToString(salt);

            String hashText = Base64.getEncoder()
                                    .encodeToString(passwordHash);

            try (BufferedWriter writer = new BufferedWriter(
                    new FileWriter(
                        USER_FILE,
                        StandardCharsets.UTF_8,
                        true
                    ))) {

                writer.write(username + "|" + saltText + "|" + hashText);
                writer.newLine();
            }

            System.out.println("Registration successful!");

        } catch (GeneralSecurityException e) {
            System.out.println("Could not secure the password.");
            e.printStackTrace();

        } catch (IOException e) {
            System.out.println("Could not save user details.");
            e.printStackTrace();

        } finally {
            Arrays.fill(password, '\0');
        }
    }

    // Login with hidden password input
    public boolean login() {

        System.out.println("\n===== USER LOGIN =====");

        System.out.print("Enter username: ");
        String username = sc.nextLine().trim();

        char[] password = readHiddenPassword(
            "Enter password for " + username
        );

        if (password == null) {
            System.out.println("Login cancelled.");
            return false;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(
                    USER_FILE,
                    StandardCharsets.UTF_8
                ))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split("\\|", -1);

                if (data.length != 3) {
                    continue;
                }

                if (!data[0].equalsIgnoreCase(username)) {
                    continue;
                }

                byte[] salt;
                byte[] storedHash;

                try {
                    salt = Base64.getDecoder().decode(data[1]);
                    storedHash = Base64.getDecoder().decode(data[2]);

                } catch (IllegalArgumentException e) {
                    System.out.println("Invalid saved user record.");
                    return false;
                }

                byte[] enteredHash = hashPassword(password, salt);

                if (MessageDigest.isEqual(storedHash, enteredHash)) {

                    loggedInUsername = data[0];

                    System.out.println("Login successful!");
                    return true;
                }

                System.out.println("Incorrect username or password.");
                return false;
            }

            System.out.println("Incorrect username or password.");
            return false;

        } catch (IOException e) {
            System.out.println(
                "Could not read users.txt. Register a user first."
            );
            return false;

        } catch (GeneralSecurityException e) {
            System.out.println("Could not verify the password.");
            e.printStackTrace();
            return false;

        } finally {
            Arrays.fill(password, '\0');
        }
    }

    // Display a dialog where password characters are hidden
    private char[] readHiddenPassword(String message) {

        JPasswordField passwordField = new JPasswordField(20);

        JOptionPane pane = new JOptionPane(
            passwordField,
            JOptionPane.QUESTION_MESSAGE,
            JOptionPane.OK_CANCEL_OPTION
        );

        JDialog dialog = pane.createDialog(null, message);

        dialog.setAlwaysOnTop(true);
        dialog.setModal(true);
        dialog.setVisible(true);

        Object result = pane.getValue();

        dialog.dispose();

        if (!(result instanceof Integer)
                || ((Integer) result) != JOptionPane.OK_OPTION) {
            return null;
        }

        return passwordField.getPassword();
    }

    // Return the successfully logged-in username
    public String getLoggedInUsername() {
        return loggedInUsername;
    }

    // Check whether a username already exists
    private boolean userExists(String username) {

        File file = new File(USER_FILE);

        if (!file.exists()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(
                new FileReader(
                    file,
                    StandardCharsets.UTF_8
                ))) {

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split("\\|", -1);

                if (data.length == 3
                        && data[0].equalsIgnoreCase(username)) {
                    return true;
                }
            }

            return false;

        } catch (IOException e) {
            System.out.println("Could not check existing users.");
            return true;
        }
    }

    // Hash the password using PBKDF2
    private byte[] hashPassword(char[] password, byte[] salt)
            throws GeneralSecurityException {

        PBEKeySpec spec = new PBEKeySpec(
            password,
            salt,
            ITERATIONS,
            KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } finally {
            spec.clearPassword();
        }
    }
}
