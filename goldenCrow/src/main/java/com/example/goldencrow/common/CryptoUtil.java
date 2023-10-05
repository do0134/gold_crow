package com.example.goldencrow.common;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Sha256으로 encoding, decoding 하는 서비스
 */
public class CryptoUtil {

    public static class Sha256 {
        private Sha256() {
        }

        public static String hash(String input) {
            return Sha256.hash(input, null);
        }

        public static String hash(String input, String fallback) {
            try {
                MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
                messageDigest.reset();
                messageDigest.update(input.getBytes(StandardCharsets.UTF_8));
                return String.format("%064x", new BigInteger(1, messageDigest.digest()));

            } catch (NoSuchAlgorithmException ignored) {
                throw new UnsupportedOperationException();

            } catch (Exception ignored) {
                return fallback;

            }

        }

    }

}
