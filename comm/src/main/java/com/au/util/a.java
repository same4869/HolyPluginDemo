package com.au.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.wenba.comm.BBLog;
import com.wenba.comm.WenbaApplication;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.security.MessageDigest;

/*
 * class used by JNI, DO NOT obfuscate
 */
public class a {
	public static int ANCHOR = 0xabcdef;

	/**
	 * 获取Context, jni需要使用
	 * 
	 * @return
	 */
	public static Context getContext() {
		return WenbaApplication.getInstance();
	}
	
	/**
	 * 生成本应用的签名SHA1摘要值
	 * 
	 * @return 签名摘要值字符串
	 */
	public static String b() {
		return digest(getSign(getContext()));
	}

	public static byte[] getSign(Context context) {
		PackageManager pm = context.getPackageManager();

		try {
			PackageInfo info = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);

			return info.signatures[0].toByteArray();
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			BBLog.w("wenba", e);
		}

		return null;
	}

	public static String digest(String data) {
		return digest(data.getBytes(), "SHA1");
	}

	public static String digest(byte[] data) {
		return digest(data, "SHA1");
	}
	
	public static String digest(byte[] data, String alg) {
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
	
	public static String digest256(String data) {
		return digest256(data.getBytes());
	}
	
	public static String digest256(byte[] data) {
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

	public static String format(byte[] bytes) {
		int len = bytes.length;
		StringBuilder buf = new StringBuilder(len * 2);

		char[] array = getMappingChars();

		for (int j = 0; j < len; j++) {
			buf.append(array[(bytes[j] >> 4) & 0x0f]);
			buf.append(array[bytes[j] & 0x0f]);
		}

		return buf.toString();
	}

	public static char[] getMappingChars() {
		char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

		return HEX_DIGITS;
	}
	
	public static boolean isArmV7() {
		//first try abi list, eg. "arm64-v8a,armeabi-v7a,armeabi"
		String abiListString = null;
		
		try {
			Class cls = Class.forName("android.os.SystemProperties");
			
			Method getMethod = cls.getDeclaredMethod("get", String.class);
			getMethod.setAccessible(true);
			
			abiListString = (String) getMethod.invoke(null, "ro.product.cpu.abilist");
			
			if (abiListString == null || abiListString.length() == 0) {
				abiListString = (String) getMethod.invoke(null, "ro.product.cpu.abi");
			}
			
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		if (abiListString != null && abiListString.length() > 0) {
			String[] abis = abiListString.split(",");
			
			for (String abi : abis) {
				if ("armeabi-v7a".equals(abi.trim())) {
					return true;
				}
			}
		}
		
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/cpuinfo")));

			String line = null;
			while ((line = reader.readLine()) != null) {
				if ((line = line.toLowerCase()).contains("processor")) {
					String[] items = line.split(":");
					if (!items[0].contains("processor")) {
						continue;
					}

					String processor = items[1];
					if (processor.contains("armv7") || processor.contains("aarch64")) {
						return true;
					}
				}
			}

			return false;
		} catch (Exception e) {
			// TODO: handle exception
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}

		return false;
	}
}
