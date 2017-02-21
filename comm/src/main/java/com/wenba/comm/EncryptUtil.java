package com.wenba.comm;

import android.util.Base64;

import com.au.util.c;

import java.security.MessageDigest;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Lijj on 16/8/9.
 */
/**
 * 基础加密组件
 *
 */
public class EncryptUtil {

    /**
     * 加密保存在本地的字节，加密前的字节内容必须是 [字符串]
     *
     * @param value
     * @return
     */
    public static byte[] soEncryptValue(byte[] value) {
        return c.fun_7755b6a06640489a9e8857e9f18da2d1(value);
    }

    /**
     * 解密本地加密的字节，解密后对应的字节内容是 [字符串]
     *
     * @param value
     * @return
     */
    public static byte[] soDecryptValue(byte[] value) {
        return c.fun_3e29f9b64a5f4f3eafd2aaab1c0900f2(value);
    }

    /**
     * BASE64解密
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] decryptBASE64(String key) throws Exception {
        return Base64.decode(key, Base64.DEFAULT);
    }

    /**
     * BASE64加密
     *
     * @param key
     * @return
     * @throws Exception
     */
    public static String encryptBASE64(byte[] key) throws Exception {
        return new String(Base64.encode(key, Base64.DEFAULT));
    }

    /**
     * MD5加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encryptMD5(byte[] data) throws Exception {

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(data);

        return md5.digest();

    }

    /**
     * SHA加密
     *
     * @param data
     * @return
     * @throws Exception
     */
    public static byte[] encryptSHA(byte[] data) throws Exception {

        MessageDigest sha = MessageDigest.getInstance("SHA");
        sha.update(data);

        return sha.digest();

    }

    /**
     * 初始化HMAC密钥
     *
     * @return
     * @throws Exception
     */
    public static String initMacKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacMD5");

        SecretKey secretKey = keyGenerator.generateKey();
        return encryptBASE64(secretKey.getEncoded());
    }

    /**
     * HMAC加密
     *
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptHMAC(byte[] data, String key) throws Exception {

        SecretKey secretKey = new SecretKeySpec(decryptBASE64(key), "HmacMD5");
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        mac.init(secretKey);

        return mac.doFinal(data);

    }

    public static String encryptSHA1(String data) {
        return encryptSHA1(data.getBytes(), "SHA1");
    }

    public static String encryptSHA1(byte[] data) {
        return encryptSHA1(data, "SHA1");
    }

    public static String encryptSHA1(byte[] data, String alg) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(alg);
            messageDigest.update(data);
            return format(messageDigest.digest());
        } catch (Exception e) {
        }

        return null;
    }

    public static String encryptSHA256(String data) {
        return encryptSHA256(data.getBytes());
    }

    public static String encryptSHA256(byte[] data) {
        if (data == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA256");
            messageDigest.update(data);
            return format(messageDigest.digest());
        } catch (Exception e) {
        }

        return null;
    }

    private static String format(byte[] bytes) {
        int len = bytes.length;
        StringBuilder buf = new StringBuilder(len * 2);

        char[] array = getMappingChars();

        for (int j = 0; j < len; j++) {
            buf.append(array[(bytes[j] >> 4) & 0x0f]);
            buf.append(array[bytes[j] & 0x0f]);
        }

        return buf.toString();
    }

    private static char[] getMappingChars() {
        char[] HEX_DIGITS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        return HEX_DIGITS;
    }
}
