package com.emergency.alert.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashingTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);

    @Test
    @DisplayName("BCrypt password encoder produces unique salted hashes and verifies successfully")
    void testBCryptHashingAndVerification() {
        String rawPassword = "Admin@12345SecurePassword";

        String hash1 = passwordEncoder.encode(rawPassword);
        String hash2 = passwordEncoder.encode(rawPassword);

        // Hashes should differ due to random salt
        assertNotEquals(hash1, hash2);

        // Both hashes should match the raw password
        assertTrue(passwordEncoder.matches(rawPassword, hash1));
        assertTrue(passwordEncoder.matches(rawPassword, hash2));

        // Wrong password should not match
        assertFalse(passwordEncoder.matches("WrongPassword123", hash1));
    }
}
