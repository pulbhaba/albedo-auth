package com.akbo.auth.util;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
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
        SecureRandom rnd = new SecureRandom();
        while (salt.length() < 18) { // length of the random string.
            int index = rnd.nextInt(SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        return salt.toString();
    }

    public static SecretKey getKeyFromPassword(final String password, final String salt) {

        SecretKeyFactory factory;
        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
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
            byte[] iv = new byte[cipher.getBlockSize()];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
            byte[] cipherText = cipher.doFinal(input.getBytes());
            byte[] combined = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherText, 0, combined, iv.length, cipherText.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed.", e);
        }
    }

    public static String decrypt(
            final String algorithm, final String cipherText, final SecretKey key) {

        Cipher cipher;
        try {
            byte[] combined = Base64.getDecoder().decode(cipherText);
            cipher = Cipher.getInstance(algorithm);
            int ivLength = cipher.getBlockSize();
            byte[] iv = new byte[ivLength];
            System.arraycopy(combined, 0, iv, 0, ivLength);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            byte[] cipherTextActual = new byte[combined.length - ivLength];
            System.arraycopy(combined, ivLength, cipherTextActual, 0, cipherTextActual.length);

            cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
            byte[] plainText = cipher.doFinal(cipherTextActual);
            return new String(plainText);
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed.", e);
        }
    }

    private static IvParameterSpec getIv() {
        return new IvParameterSpec(IV_DATA);
    }
}
