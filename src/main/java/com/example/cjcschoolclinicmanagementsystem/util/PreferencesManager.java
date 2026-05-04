package com.example.cjcschoolclinicmanagementsystem.util;

import java.util.prefs.Preferences;
import java.util.Base64;

/**
 * Manages user preferences including "Remember Me" functionality.
 * Stores credentials securely using Java Preferences API with basic Base64 encoding.
 */
public class PreferencesManager {
    private static final Preferences prefs = Preferences.userNodeForPackage(PreferencesManager.class);

    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    /**
     * Saves the username and password if the "Remember Me" option is enabled.
     *
     * @param username The username to save
     * @param password The password to save
     * @param rememberMe Whether to save the credentials
     */
    public static void saveCredentials(String username, String password, boolean rememberMe) {
        if (rememberMe) {
            prefs.putBoolean(KEY_REMEMBER_ME, true);
            prefs.put(KEY_USERNAME, username);
            // Store password with basic encoding (not encryption for now)
            prefs.put(KEY_PASSWORD, encodePassword(password));
        } else {
            clearCredentials();
        }
    }

    /**
     * Retrieves the saved username if "Remember Me" is enabled.
     *
     * @return The saved username, or empty string if not found
     */
    public static String getSavedUsername() {
        if (isRememberMeEnabled()) {
            return prefs.get(KEY_USERNAME, "");
        }
        return "";
    }

    /**
     * Retrieves the saved password if "Remember Me" is enabled.
     *
     * @return The saved password, or empty string if not found
     */
    public static String getSavedPassword() {
        if (isRememberMeEnabled()) {
            String encoded = prefs.get(KEY_PASSWORD, "");
            if (!encoded.isEmpty()) {
                return decodePassword(encoded);
            }
        }
        return "";
    }

    /**
     * Checks if the "Remember Me" preference is enabled.
     *
     * @return True if "Remember Me" is enabled, false otherwise
     */
    public static boolean isRememberMeEnabled() {
        return prefs.getBoolean(KEY_REMEMBER_ME, false);
    }

    /**
     * Clears all saved credentials and disables the "Remember Me" preference.
     */
    public static void clearCredentials() {
        prefs.putBoolean(KEY_REMEMBER_ME, false);
        prefs.remove(KEY_USERNAME);
        prefs.remove(KEY_PASSWORD);
    }

    /**
     * Encodes password using Base64 (basic encoding, not secure encryption).
     * For production use, implement proper encryption.
     *
     * @param password The password to encode
     * @return The encoded password
     */
    private static String encodePassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    /**
     * Decodes password from Base64.
     *
     * @param encoded The encoded password
     * @return The decoded password
     */
    private static String decodePassword(String encoded) {
        try {
            return new String(Base64.getDecoder().decode(encoded));
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}

