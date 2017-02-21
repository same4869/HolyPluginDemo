package com.au.util;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;

import com.wenba.comm.BBLog;
import com.wenba.comm.WenbaThreadPool;
import com.wenba.comm.encrypt.AesUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*
 * class used by JNI, DO NOT obfuscate
 */
public class c {
	public final static int EXCEPTION_UUID_OUT_OF_SYNC = 1;

	private static ICrashUpdate crashUpdater;


	static {
		System.loadLibrary("base");
		System.loadLibrary("WenbaCrashCatcher");
	}

	/**
	 * Handler
	 */
	private static Handler sHandler;

	public static void setHandler(Handler handler) {
		sHandler = handler;
	}

	public static Handler getHandler() {
		return sHandler;
	}

	public static void sendMessage(final int id, final String info) {
		WenbaThreadPool.poolExecute(new Runnable() {

			@Override
			public void run() {
				BBLog.d("lqp", "sendMessage: " + info);
				if(crashUpdater != null) {
					crashUpdater.sendMessage(info);
				}
			}
		});
	}

	/**
	 * 该函数主要用来读取加密后的设置文件, 若不存在, 则创建
	 */

	public static String a(String fileName) throws Exception {

		File file = new File(a.getContext().getFilesDir(), fileName);
		if (!file.exists()) {
			file.createNewFile();
			return "";
		}

		if (file.isDirectory()) {
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException e) {
				BBLog.w("wenba", e);
			}

			file.createNewFile();
			return "";
		}

		StringBuffer buffer = new StringBuffer();

		InputStream inputStream = new FileInputStream(file);

		byte[] dataBuffer = new byte[4096];
		int count = 0;

		while ((count = inputStream.read(dataBuffer)) > 0) {
			buffer.append(new String(dataBuffer, 0, count));
		}

		inputStream.close();

		return buffer.toString();
	}

	/**
	 * 该函数主要用来读取加密后的设置文件, 若不存在, 则创建
	 */

	public static byte[] ab(String fileName) throws Exception {

		File file = new File(a.getContext().getFilesDir(), fileName);
		if (!file.exists()) {
			file.createNewFile();
			return null;
		}

		if (file.isDirectory()) {
			try {
				FileUtils.deleteDirectory(file);
			} catch (IOException e) {
				BBLog.w("wenba", e);
			}

			file.createNewFile();
			return null;
		}

		if (file.length() == 0) {
			return null;
		}

		if (file.length() > 6 * 1024 * 1024) {
			file.delete();
			return null;
		}

		InputStream inputStream = new FileInputStream(file);

		byte[] dataBuffer = new byte[(int) file.length()];

		inputStream.read(dataBuffer);
		inputStream.close();

		return dataBuffer;
	}

	/**
	 * 该函数用来写入加密后的设置文件
	 */

	public static void b(String fileName, String content) throws Exception {

		if (fileName == null || content == null) {
			return;
		}

		File file = new File(a.getContext().getFilesDir(), fileName);
		if (file.isDirectory()) {
			try {
				FileUtils.deleteDirectory(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		OutputStream outputStream = new FileOutputStream(file);
		outputStream.write(content.getBytes());
		outputStream.flush();
		outputStream.close();
	}

	/**
	 * 该函数用来写入加密后的设置文件
	 */

	public static void b(String fileName, byte[] content) throws Exception {

		if (fileName == null || content == null) {
			return;
		}

		File file = new File(a.getContext().getFilesDir(), fileName);
		if (file.isDirectory()) {
			try {
				FileUtils.deleteDirectory(file);
			} catch (Exception e) {
				BBLog.w("wenba", e);
			}
		}

		if (!file.exists()) {
			file.getParentFile().mkdirs();
			file.createNewFile();
		}

		OutputStream outputStream = new FileOutputStream(file);
		outputStream.write(content);
		outputStream.flush();
		outputStream.close();
	}

	/**
	 * 用来获取加密后的asset文件
	 */

	public static String c(String name) throws Exception {
		Context context = a.getContext();

		if (context == null) {
			return null;
		}

		StringBuffer buffer = new StringBuffer();

		InputStream inputStream = context.getAssets().open(name);

		byte[] dataBuffer = new byte[4096];
		int count = 0;

		while ((count = inputStream.read(dataBuffer)) > 0) {
			buffer.append(new String(dataBuffer, 0, count));
		}

		inputStream.close();

		return buffer.toString();
	}

	public static byte[] cb(String name) throws Exception {
		Context context = a.getContext();

		if (context == null) {
			return null;
		}

		InputStream inputStream = context.getAssets().open(name);

		byte[] dataBuffer = new byte[inputStream.available()];

		inputStream.read(dataBuffer);
		inputStream.close();

		return dataBuffer;
	}

	/**
	 * 本地jni调用的解密接口
	 * 
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public static String d(String key, String body) throws Exception {
		AesUtil aesUtils = new AesUtil(key);

		return aesUtils.decrypt(body);
	}

	/**
	 * 本地jni调用的加密接口
	 * 
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public static String e(String key, String body) throws Exception {
		AesUtil aesUtils = new AesUtil(key);

		return aesUtils.encrypt(body);
	}

	/**
	 * 本地jni调用的将byte数组转换为String的函数
	 * 
	 * @param array
	 * @return
	 * @throws Exception
	 */
	public static String f(byte[] array) throws Exception {
		String body = null;

		try {
			body = new String(array);
		} catch (Exception e) {
			BBLog.w("wenba", e);
		}

		return body;
	}

	/**
	 * 
	 */

	public static long g() {
		return SystemClock.uptimeMillis();
	}
	
	/**
	 * 本地jni崩溃的回调, 请做尽量少的事情,比如报告崩溃, 必须同步等待完成, 不保证正常运行到结束
	 */
	
	public static void fun_8501948e333b47b095c5b21d8370c121(String crashInfo) {
		BBLog.e("lqp", "java crash catcher: \n" + crashInfo);

		if ( crashUpdater != null ){
			crashUpdater.updateCrash(crashInfo, true);
		}
	}

	/**
	 * 获取key对应的属性
	 * 
	 * @param key
	 * @return
	 */

	public static native String fun_0b35c5d258794c93ab906d426d7525f0(String key);

	/**
	 * 从只读的设置文件中获取key对应的熟悉
	 * 
	 * @param key
	 * @return
	 */
	public static native String fun_603eba83a976468caecfc29db3869a8f(String key);

	/**
	 * 保存key对应的属性
	 * 
	 * @param key
	 * @param value
	 */
	public static native void fun_f6a2a835bdde4886909460659dacf353(String key, String value);

	/**
	 * 加密字节
	 * 
	 * @param input
	 * @return
	 */
	public static native byte[] fun_7755b6a06640489a9e8857e9f18da2d1(byte[] input);

	/**
	 * 解密字节
	 * 
	 * @param input
	 * @return
	 */
	public static native byte[] fun_3e29f9b64a5f4f3eafd2aaab1c0900f2(byte[] input);

	/**
	 * 解密服务器字节
	 * 
	 * @param timeInSec
	 *            服务器秒数
	 * @param input
	 * @return
	 */
	public static native byte[] fun_664f2794a6764a1994a0d837732ef7e4(long timeInSec, byte[] input);

	/**
	 * 加密服务器字节
	 * 
	 * @param timeInSec
	 *            服务器秒数
	 * @param input
	 * @return
	 */
	public static native byte[] fun_5b7a9e379e95435180245b726904bb25(long timeInSec, byte[] input);

	/**
	 * 获得本地token
	 * 
	 * @param data
	 * @return
	 */
	public static native String fun_e6a70f06a15f4b728ce6aee5dadfaddc(long timeInsec, String data);

	/**
	 * 生成deviceId
	 */
	public static  native String fun_8148f9430ef9491cb42882452085d881(byte[] seed);

	/**
	 * 主要用来调用以便载入该类, 不做任何事情
	 */

	public static String s() {
		return c.class.getName();
	}

	public static void setCrashUpdater(ICrashUpdate updater){
		crashUpdater = updater;
	}
}
