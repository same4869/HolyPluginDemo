package com.au.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;

public class MD5Checksum {
	public static byte[] createChecksum(InputStream inputStream) throws Exception {
		InputStream fis = inputStream;

		byte[] buffer = new byte[4096];
		MessageDigest complete = MessageDigest.getInstance("MD5");
		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();
		return complete.digest();
	}

	public static byte[] createChecksum(byte[] array) throws Exception {
		MessageDigest complete = MessageDigest.getInstance("MD5");

		complete.update(array, 0, array.length);

		return complete.digest();
	}

	public static String getMD5Checksum(byte[] dataArray) throws Exception {
		byte[] b = createChecksum(dataArray);

		return a.format(b);
	}

	public static String getMD5Checksum(InputStream inputStream) throws Exception {
		byte[] b = createChecksum(inputStream);

		return a.format(b);
	}

	public static String getMD5Checksum(File file) throws Exception {
		byte[] b = createChecksum(new FileInputStream(file));

		return a.format(b);
	}

	public static String getMD5Checksum(String filename) throws Exception {
		byte[] b = createChecksum(new FileInputStream(filename));

		return a.format(b);
	}
}
