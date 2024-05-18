package com.akbo.auth.util;

import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/*
    String input = "baeldung";
    SecretKey key = AESUtil.generateKey(128);
    IvParameterSpec ivParameterSpec = AESUtil.generateIv();
    String algorithm = "AES/CBC/PKCS5Padding";
    String cipherText = AESUtil.encrypt(algorithm, input, key, ivParameterSpec);
    String plainText = AESUtil.decrypt(algorithm, cipherText, key, ivParameterSpec);
    Assertions.assertEquals(input, plainText);
 */

public class PasswordTools {
    private static final byte[] IV_DATA = {
            (byte) 0x01, (byte) 0x02, (byte) 0x02, (byte) 0x01,
            (byte) 0x01, (byte) 0x02, (byte) 0x02, (byte) 0x01,
            (byte) 0x01, (byte) 0x02, (byte) 0x02, (byte) 0x01,
            (byte) 0x01, (byte) 0x02, (byte) 0x02, (byte) 0x01,
    };
    public static String urlAlgorithm = "AES/CBC/PKCS5Padding";

    public static String generateRandomString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;
    }

    public static SecretKey getKeyFromPassword(final String password, final String salt) {

        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey secret = new SecretKeySpec(factory.generateSecret(spec)
                    .getEncoded(), "AES");
            return secret;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid infomation provided for key generation", e);
        } catch (InvalidKeySpecException e) {
            throw new RuntimeException("Provided key is invalid.", e);
        }
    }

    public static String encrypt(final String algorithm, final String input, final SecretKey key) {

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, getIv());
            byte[] cipherText = cipher.doFinal(input.getBytes());
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed.", e);
        }
    }

    public static String decrypt(final String algorithm, final String cipherText, final SecretKey key) {

        Cipher cipher;
        try {
            cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, getIv());
            byte[] plainText = cipher.doFinal(Base64.getDecoder()
                    .decode(cipherText));
            return new String(plainText);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed.", e);
        }
    }

    private static IvParameterSpec getIv() {
        return new IvParameterSpec(IV_DATA);
    }
}
