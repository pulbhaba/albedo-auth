package com.akbo.auth.api.security;

import static org.junit.jupiter.api.Assertions.*;
import com.akbo.auth.util.PasswordTools;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import javax.crypto.SecretKey;

class PasswordResetSecurityTest {

    @Test
    void demonstrateWeakRandomString() {
        // Simple test to show that Random is used, though it's hard to prove "weakness" without a
        // lot of samples.
        // But we can check for the length and character set.
        String random = PasswordTools.generateRandomString();
        assertEquals(18, random.length());
        assertTrue(random.matches("[A-Z0-9]+"));

        // Demonstrating it's now using SecureRandom (this is a security improvement)
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            String r = PasswordTools.generateRandomString();
            assertFalse(seen.contains(r), "Collision detected in only 1000 samples!");
            seen.add(r);
        }
    }

    @Test
    void demonstrateFixedIVUsage() {
        SecretKey key = PasswordTools.getKeyFromPassword("secret", "salt");
        String input = "123|ABC";

        String encrypted1 = PasswordTools.encrypt(PasswordTools.urlAlgorithm, input, key);
        String encrypted2 = PasswordTools.encrypt(PasswordTools.urlAlgorithm, input, key);

        // With a random IV, same input + same key = different output.
        assertNotEquals(
                encrypted1,
                encrypted2,
                "Encrypted values should be different for same input due to random IV");

        // Decryption should still work
        String decrypted1 = PasswordTools.decrypt(PasswordTools.urlAlgorithm, encrypted1, key);
        String decrypted2 = PasswordTools.decrypt(PasswordTools.urlAlgorithm, encrypted2, key);

        assertEquals(input, decrypted1);
        assertEquals(input, decrypted2);
    }
}
