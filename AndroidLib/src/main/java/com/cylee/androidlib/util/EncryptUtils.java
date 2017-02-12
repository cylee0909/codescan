package com.cylee.androidlib.util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class EncryptUtils {
  public static final String TAG = "Encrypt";
  public static final String DEFAULT_CHARSET = "UTF-8";
  private static final String AES_KEY = "8b318785b67d4ae69f92ec14192a5436";
  private static final String AES_ENCRYPT_MODE = "AES/CBC/PKCS5Padding";

  /**
   * generate a random aes key
   * @return
   * @throws NoSuchAlgorithmException
   */
  public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
    KeyGenerator kgen = KeyGenerator.getInstance("AES");
    kgen.init(256);
    return kgen.generateKey();
  }

  public static String getAESKey() {
    return AES_KEY;
  }


  /**
   * AES加密
   * @param encryptBytes 待加密的byte[]
   * @return 加密后的byte[]
   * @throws Exception
   */
  public static byte[] aesEncrypt(byte[] encryptBytes) throws Exception {
    return aesEncrypt(encryptBytes, getAESKey(), generateIV());
  }

  /**
   * AES加密
   * @param encryptBytes 待加密的byte[]
   * @return 加密后的byte[]
   * @throws Exception
   */
  public static byte[] aesEncrypt(byte[] encryptBytes, byte[] iv) throws Exception {
    return aesEncrypt(encryptBytes, getAESKey(), iv);
  }

  /**
   * AES加密
   * @param encryptBytes 待加密的byte[]
   * @param encryptKey 加密密钥
   * @return 加密后的byte[]
   * @throws Exception
   */
  public static byte[] aesEncrypt(byte[] encryptBytes, String encryptKey) throws Exception {
    return aesEncrypt(encryptBytes, encryptKey, null);
  }

  /**
   * AES加密
   * @param content 待加密的内容
   * @param encryptKey 加密密钥
   * @param IV iv
   * @return 加密后的byte[]
   * @throws Exception
   */
  public static byte[] aesEncrypt(byte[] content, String encryptKey, byte[] IV) throws Exception {
    Cipher cipher = Cipher.getInstance(AES_ENCRYPT_MODE);
    if (IV != null) {
      IvParameterSpec ips = new IvParameterSpec(IV);
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(defaultCharset()), "AES"), ips);
      // we should add our iv ahead
      byte[] rawContent = cipher.doFinal(content);
      byte[] result = new byte[rawContent.length + IV.length];
      System.arraycopy(IV, 0, result, 0, IV.length);
      System.arraycopy(rawContent, 0, result, IV.length, rawContent.length);
      return result;
    } else {
      cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(encryptKey.getBytes(defaultCharset()), "AES"));
      return cipher.doFinal(content);
    }
  }

  // ============================================ decrypt ============================================ //

  /**
   * AES解密
   * @param decryptBytes 待解密的byte[]
   * @return 解密后的byte[]
   * @throws Exception
   */
  public static byte[] aesDecrypt(byte[] decryptBytes) throws Exception {
    return aesDecrypt(decryptBytes, true);
  }

  /**
   * AES解密
   * @param encryptBytes 待解密的byte[]
   * @return 解密后的byte[]
   * @throws Exception
   */
  public static byte[] aesDecrypt(byte[] encryptBytes, byte[] iv) throws Exception {
    return aesDecrypt(encryptBytes, getAESKey(), iv);
  }

  /**
   * AES解密
   * @param encryptBytes 待解密的byte[]
   * @param decryptKey 解密密钥
   * @return 解密后的byte[]
   * @throws Exception
   */
  public static byte[] aesDecrypt(byte[] encryptBytes, String decryptKey) throws Exception {
    return aesDecrypt(encryptBytes, decryptKey, null);
  }

  /**
   * AES解密
   * @param decryptBytes 待解密的byte[]
   * @return 解密后的byte[]
   * @throws Exception
   */
  public static byte[] aesDecrypt(byte[] decryptBytes, boolean hasIV) throws Exception {
    if (hasIV) {
      byte[] iv = new byte[16];
      byte[] data = new byte[decryptBytes.length - iv.length];
      System.arraycopy(decryptBytes, 0, iv, 0, iv.length);
      System.arraycopy(decryptBytes, iv.length, data, 0, data.length);
      return aesDecrypt(data, getAESKey(), iv);
    }
    return aesDecrypt(decryptBytes, getAESKey(), null);
  }

  /**
   * AES解密
   * @param decryptBytes 待解密的byte[]
   * @param decryptKey 解密密钥
   * @return 解密后的byte[]
   * @throws Exception
   */
  public static byte[] aesDecrypt(byte[] decryptBytes, String decryptKey, byte[] IV) throws Exception {
    Cipher cipher = Cipher.getInstance(AES_ENCRYPT_MODE);
    if (IV != null) {
      IvParameterSpec ips = new IvParameterSpec(IV);
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(defaultCharset()), "AES"), ips);
    } else {
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(decryptKey.getBytes(defaultCharset()), "AES"));
    }
    return cipher.doFinal(decryptBytes);
  }

  private static byte[] generateIV() throws NoSuchAlgorithmException {
    final byte[] nonce = new byte[16];
    Random rand = SecureRandom.getInstance("SHA1PRNG");
    rand.nextBytes(nonce);
    return nonce;
  }

  private static Charset defaultCharset() {
    try {
      return Charset.forName(DEFAULT_CHARSET);
    } catch (UnsupportedCharsetException e) {
      return Charset.defaultCharset();
    }
  }

  public static byte[] double2ByteArray(double value) {
    byte[] bytes = new byte[8];
    ByteBuffer.wrap(bytes).putDouble(value);
    return bytes;
  }

  public static double byteArr2Double(byte[] bytes) {
    return ByteBuffer.wrap(bytes).getDouble();
  }
  public static byte[] long2ByteArray(long value) {
    byte[] bytes = new byte[8];
    ByteBuffer.wrap(bytes).putLong(value);
    return bytes;
  }

  public static long byteArr2Long(byte[] bytes) {
    return ByteBuffer.wrap(bytes).getLong();
  }
  public static byte[] int2ByteArray(int value) {
    byte[] bytes = new byte[4];
    ByteBuffer.wrap(bytes).putInt(value);
    return bytes;
  }

  public static int byteArr2Int(byte[] bytes) {
    return ByteBuffer.wrap(bytes).getInt();
  }

  public static byte[] aesEncryptDouble(double d) throws Exception {
    return aesEncrypt(double2ByteArray(d));
  }
  public static double aesDecryptDouble(byte[] arr) throws Exception {
    return byteArr2Double(aesDecrypt(arr));
  }
  public static byte[] aesEncryptLong(long l) throws Exception {
    return aesEncrypt(long2ByteArray(l));
  }
  public static long aesDecryptLong(byte[] arr) throws Exception {
    return byteArr2Long(aesDecrypt(arr));
  }

  public static byte[] aesEncryptInt(int i) throws Exception {
    return aesEncrypt(int2ByteArray(i));
  }
  public static int aesDecryptInt(byte[] arr) throws Exception {
    return byteArr2Int(aesDecrypt(arr));
  }
}

