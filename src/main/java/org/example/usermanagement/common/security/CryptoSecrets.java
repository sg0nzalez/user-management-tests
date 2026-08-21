package org.example.usermanagement.common.security;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.experimental.UtilityClass;

/**
 * {@code ENC(...)} helper for committed secrets. Master key is read from {@code
 * ENCRYPTION_MASTER_KEY}.
 *
 * <p>New values use AES-256-GCM: {@code ENC(Base64(IV || ciphertext+tag))}. Decrypt also accepts
 * legacy AES/ECB payloads so existing properties keep working. For encrypt/decrypt from the shell,
 * use {@link CryptoSecretsCli}.
 */
@UtilityClass
public class CryptoSecrets {

  public final String MASTER_KEY_ENV = "ENCRYPTION_MASTER_KEY";

  private final Pattern MARKER_PATTERN = Pattern.compile("^ENC\\((.+)\\)$");
  private final String GCM_TRANSFORM = "AES/GCM/NoPadding";
  private final String ECB_TRANSFORM = "AES/ECB/PKCS5Padding";
  private final String KEY_ALGORITHM = "AES";
  private final int GCM_IV_LENGTH = 12;
  private final int GCM_TAG_LENGTH_BITS = 128;

  public boolean isEncrypted(String value) {
    return value != null && MARKER_PATTERN.matcher(value).matches();
  }

  public String encryptString(String plainText) {
    try {
      byte[] iv = new byte[GCM_IV_LENGTH];
      new SecureRandom().nextBytes(iv);

      Cipher cipher = Cipher.getInstance(GCM_TRANSFORM);
      cipher.init(
          Cipher.ENCRYPT_MODE,
          buildAes256Key(requireMasterKey()),
          new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
      byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

      ByteBuffer buffer = ByteBuffer.allocate(iv.length + cipherText.length);
      buffer.put(iv);
      buffer.put(cipherText);
      return "ENC(" + Base64.getEncoder().encodeToString(buffer.array()) + ")";
    } catch (GeneralSecurityException ex) {
      throw new IllegalStateException("Unable to encrypt value", ex);
    }
  }

  public String decryptString(String encryptedValue) {
    byte[] payload = decodeBase64Payload(encryptedValue);
    try {
      return decryptGcm(payload);
    } catch (GeneralSecurityException | IllegalArgumentException gcmFailure) {
      try {
        return decryptLegacyEcb(payload);
      } catch (GeneralSecurityException ecbFailure) {
        IllegalStateException failure =
            new IllegalStateException("Unable to decrypt ENC(...) value", gcmFailure);
        failure.addSuppressed(ecbFailure);
        throw failure;
      }
    }
  }

  /** Decrypts when value is {@code ENC(...)}; otherwise returns the value unchanged. */
  public String maybeDecrypt(String value) {
    if (value == null) {
      return null;
    }
    return isEncrypted(value) ? decryptString(value) : value;
  }

  private String decryptGcm(byte[] payload) throws GeneralSecurityException {
    if (payload.length <= GCM_IV_LENGTH) {
      throw new IllegalArgumentException("Encrypted payload is too short for GCM");
    }
    byte[] iv = Arrays.copyOfRange(payload, 0, GCM_IV_LENGTH);
    byte[] cipherText = Arrays.copyOfRange(payload, GCM_IV_LENGTH, payload.length);
    Cipher cipher = Cipher.getInstance(GCM_TRANSFORM);
    cipher.init(
        Cipher.DECRYPT_MODE,
        buildAes256Key(requireMasterKey()),
        new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv));
    return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
  }

  /** Legacy AES/ECB used by earlier ENC(...) values in this repo. */
  private String decryptLegacyEcb(byte[] payload) throws GeneralSecurityException {
    Cipher cipher = Cipher.getInstance(ECB_TRANSFORM);
    cipher.init(Cipher.DECRYPT_MODE, buildLegacyAes128Key(requireMasterKey()));
    return new String(cipher.doFinal(payload), StandardCharsets.UTF_8);
  }

  private String requireMasterKey() {
    String key = System.getenv(MASTER_KEY_ENV);
    if (key == null || key.isBlank()) {
      key = System.getProperty(MASTER_KEY_ENV);
    }
    if (key == null || key.isBlank()) {
      throw new IllegalStateException(
          "Missing "
              + MASTER_KEY_ENV
              + " as environment variable or system property (required to decrypt ENC(...) values)");
    }
    return key;
  }

  @SuppressWarnings("PMD.HardCodedCryptoKey") // key bytes are derived from ENCRYPTION_MASTER_KEY
  private SecretKeySpec buildAes256Key(String masterKey) throws GeneralSecurityException {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] keyBytes = digest.digest(masterKey.getBytes(StandardCharsets.UTF_8));
    return new SecretKeySpec(keyBytes, KEY_ALGORITHM);
  }

  // legacy key bytes derived from ENCRYPTION_MASTER_KEY
  private SecretKeySpec buildLegacyAes128Key(String masterKey) throws GeneralSecurityException {
    MessageDigest sha = MessageDigest.getInstance("SHA-1");
    byte[] digest = sha.digest(masterKey.getBytes(StandardCharsets.UTF_8));
    return new SecretKeySpec(Arrays.copyOf(digest, 16), KEY_ALGORITHM);
  }

  private byte[] decodeBase64Payload(String encryptedValue) {
    Matcher matcher = MARKER_PATTERN.matcher(encryptedValue);
    if (!matcher.matches()) {
      throw new IllegalArgumentException("Value is not ENC(...)");
    }
    return Base64.getDecoder().decode(matcher.group(1));
  }
}
