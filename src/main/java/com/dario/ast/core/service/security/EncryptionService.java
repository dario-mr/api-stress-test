package com.dario.ast.core.service.security;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Service responsible for encrypting and decrypting sensitive string values using AES encryption with GCM
 * (Galois/Counter Mode).
 * <p>
 * The encryption key is expected to be a 256-bit AES key provided securely via an environment variable,
 * base64-encoded.
 * <p>
 * This service is useful for securing data such as tokens or identifiers stored in cookies or transient storage.
 * <p>
 * Notes:
 * <ul>
 *   <li>Uses AES/GCM/NoPadding for authenticated encryption.</li>
 *   <li>Each encryption call generates a new random IV (12 bytes), which is
 *       prepended to the ciphertext.</li>
 *   <li>IV and ciphertext are Base64-encoded together into a single string
 *       for storage or transport.</li>
 * </ul>
 */
@Service
public class EncryptionService {

  private static final String ALGORITHM = "AES/GCM/NoPadding";
  private static final int IV_LENGTH = 12; // GCM standard
  private static final int TAG_LENGTH_BIT = 128;

  private final SecretKey secretKey;

  public EncryptionService(
      @Value("${app.encryption.secret-key}") String base64Key
  ) {
    var decodedKey = Base64.getDecoder().decode(base64Key);
    if (decodedKey.length != 32) { // AES-256 requires 32 bytes
      throw new IllegalArgumentException("Invalid key length, expected 256-bit (32-byte) key");
    }
    this.secretKey = new SecretKeySpec(decodedKey, "AES");
  }

  public String encrypt(String value) {
    try {
      var iv = new byte[IV_LENGTH];
      new SecureRandom().nextBytes(iv);

      var spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
      var cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec);

      var encrypted = cipher.doFinal(value.getBytes(UTF_8));
      var combined = ByteBuffer.allocate(iv.length + encrypted.length)
          .put(iv)
          .put(encrypted)
          .array();

      return Base64.getEncoder().encodeToString(combined);
    } catch (Exception e) {
      throw new RuntimeException("Failed to encrypt value", e);
    }
  }

  public String decrypt(String encryptedValue) {
    try {
      var combined = Base64.getDecoder().decode(encryptedValue);
      var iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
      var encrypted = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);

      var spec = new GCMParameterSpec(TAG_LENGTH_BIT, iv);
      var cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

      var decrypted = cipher.doFinal(encrypted);
      return new String(decrypted, UTF_8);
    } catch (Exception e) {
      throw new RuntimeException("Failed to decrypt value", e);
    }
  }

}