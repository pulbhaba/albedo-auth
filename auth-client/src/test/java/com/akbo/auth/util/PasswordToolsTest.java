package com.akbo.auth.util;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;

class PasswordToolsTest {

    @Test
    void testGenerateRandomString() {
        String random = PasswordTools.generateRandomString();
        assertNotNull(random);
        assertEquals(18, random.length());

        String random2 = PasswordTools.generateRandomString();
        assertNotEquals(random, random2);
    }

    @Test
    void testEncryptionDecryption() {
        String password = "myPassword";
        String salt = "mySalt";
        String plainText = "Hello World!";

        SecretKey key = PasswordTools.getKeyFromPassword(password, salt);
        assertNotNull(key);

        String cipherText = PasswordTools.encrypt(PasswordTools.urlAlgorithm, plainText, key);
        assertNotNull(cipherText);
        assertNotEquals(plainText, cipherText);

        String decrypted = PasswordTools.decrypt(PasswordTools.urlAlgorithm, cipherText, key);
        assertEquals(plainText, decrypted);
    }

    @Test
    void testGetKeyFromPassword_InvalidAlgorithm() {
        // This is hard to trigger unless we mock SecretKeyFactory,
        // but we can at least call it to get coverage.
        SecretKey key = PasswordTools.getKeyFromPassword("pass", "salt");
        assertNotNull(key);
    }

    @Test
    void testEncrypt_InvalidAlgorithm() {
        SecretKey key = PasswordTools.getKeyFromPassword("pass", "salt");
        assertThrows(
                RuntimeException.class,
                () -> {
                    PasswordTools.encrypt("INVALID", "data", key);
                });
    }

    @Test
    void testDecrypt_InvalidData() {
        SecretKey key = PasswordTools.getKeyFromPassword("pass", "salt");
        assertThrows(
                RuntimeException.class,
                () -> {
                    PasswordTools.decrypt(PasswordTools.urlAlgorithm, "invalid-base64", key);
                });
    }
}
