package com.wenba.comm.encrypt;


import android.annotation.SuppressLint;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;

public class AesUtilCBCS7Padding {

	private final static String AES_TYPE_CBC = "AES/CBC/PKCS7Padding";
	
	private Cipher encryptCipher = null;
	private Cipher decryptCipher = null;

	@SuppressLint("TrulyRandom")
	public AesUtilCBCS7Padding(String strKey,String ivStr) throws Exception {
		Key key = getKey(strKey.getBytes());
		encryptCipher = Cipher.getInstance(AES_TYPE_CBC);
		decryptCipher = Cipher.getInstance(AES_TYPE_CBC);
		
		if(ivStr != null){
			IvParameterSpec iv = new IvParameterSpec(ivStr.getBytes());//使用CBC模式，需要一个向量iv，可增加加密算法的强度
			encryptCipher.init(Cipher.ENCRYPT_MODE, key,iv);
			decryptCipher.init(Cipher.DECRYPT_MODE, key,iv);
		}else{
			encryptCipher.init(Cipher.ENCRYPT_MODE, key);
			decryptCipher.init(Cipher.DECRYPT_MODE, key);
		}
	}

	/**
	 * 加密字节数组
	 * 
	 * @param arrB
	 *            需加密的字节数组
	 * @return 加密后的字节数组
	 * @throws Exception
	 */
	public byte[] encrypt(byte[] arrB) throws Exception {
		return encryptCipher.doFinal(arrB);
	}

	/**
	 * 加密字符串
	 * 
	 * @param strIn
	 *            需加密的字符串
	 * @return 加密后的字符串
	 * @throws Exception
	 */
	public byte[] encrypt(String strIn) throws Exception {
		return encrypt(strIn.getBytes());
	}

	/**
	 * 解密字节数组
	 * 
	 * @param arrB
	 *            需解密的字节数组
	 * @return 解密后的字节数组
	 * @throws Exception
	 */
	public byte[] decrypt(byte[] arrB) throws Exception {
		return decryptCipher.doFinal(arrB);
	}

	public String decrypt(String strIn) throws Exception {
		return new String(decrypt(strIn.getBytes()));
	}

	private Key getKey(byte[] arrBTmp) throws Exception {
		Key key = new javax.crypto.spec.SecretKeySpec(arrBTmp, "AES");
		return key;
	}
}
