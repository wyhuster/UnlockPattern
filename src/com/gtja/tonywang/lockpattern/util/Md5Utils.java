/* Copyright (c) ViaCube.  All worldwide rights reserved. */
package com.gtja.tonywang.lockpattern.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Utils {

	/**
	 * get string md5
	 * 
	 * @param original
	 *            string under encrypt
	 * @param separator
	 *            separator between each byte in encrypt string
	 * @return
	 */
	public static String toMd5(String original, String separator) {
		try {
			String result;
			byte[] bytes = original.getBytes();
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(bytes);
			result = toHexString(algorithm.digest(), separator);
			return result;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	private static String toHexString(byte[] bytes, String separator) {
		StringBuilder hexString = new StringBuilder();
		for (byte b : bytes) {
			hexString.append(String.format("%02x", 0xFF & b)).append(separator);
		}
		return hexString.toString();
	}
}
