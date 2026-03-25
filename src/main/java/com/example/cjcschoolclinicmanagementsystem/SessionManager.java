package com.example.cjcschoolclinicmanagementsystem;

/**
 * Simple static session holder.
 * Call SessionManager.setCurrentUser(username) right after a successful login.
 * Read SessionManager.getCurrentUser() anywhere to get the logged-in nurse's name.
 */
public class SessionManager {

    private static String currentUser = "unknown";

    public static void setCurrentUser(String username) {
        currentUser = (username != null && !username.isBlank()) ? username : "unknown";
    }

    public static String getCurrentUser() {
        return currentUser;
    }

    public static void clear() {
        currentUser = "unknown";
    }
}
