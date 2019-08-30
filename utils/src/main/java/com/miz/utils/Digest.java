package com.miz.utils;


import javax.crypto.spec.IvParameterSpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Digest {

	private static String CHARSET = "utf-8";

	public static String MD5 = "MD5";
	public static String MD2 = "MD2";
	public static String SHA1 = "SHA1";
	public static String SHA256 = "SHA256";

	private Digest(){}
	
	public static byte[] md5(byte[] input) {
		return messageDigest(MD5, input);
	}
	
	public static byte[] md2(byte[] input) {
		return messageDigest(MD2, input);
	}
	
	public static byte[] sha1(byte[] input) {
		return messageDigest(SHA1, input);
	}
	
	public static byte[] sha256(byte[] input) {
		return messageDigest(SHA256, input);
	}
	
	private static byte[] messageDigest(String algorithm, byte[] input) {
		try {
			MessageDigest digest = MessageDigest.getInstance(algorithm);
			return digest.digest(input);
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
	
	public static String bytesToHexString(byte[] src) {
		StringBuilder builder = new StringBuilder();
	    if (src == null || src.length <= 0) {   
	        throw new NullPointerException();
	    }

	    for(byte b : src) {
	    	String hv = Integer.toHexString(b & 0xff);
	    	if(hv.length() < 2) {
				builder.append(0);
			}
			builder.append(hv);
		}
		return builder.toString();
	}
	
	public static byte[] hexStringToBytes(String hexString) {
		byte[] bArray = new BigInteger("10" + hexString, 16).toByteArray();
        return Arrays.copyOfRange(bArray, 1, bArray.length);
	} 
	
	/**
	 * AES/CBC/PKCS7Padding 加密
	 * @param clearText 明文
	 * @param key 秘钥
	 * @param iv iv偏移量
	 * @return 加密后的16进制码
	 */
	public static String AESEncryption(String clearText, String key, String iv) throws Exception {
		byte[] bytes = AES.encrypt(clearText.getBytes(), md5(key.getBytes()), new IvParameterSpec(iv.getBytes()));
		return bytesToHexString(bytes);
	}
	
	/**
	 * AES/CBC/PKCS7Padding 解密
	 * @param cipherText 密文
	 * @param key 秘钥
	 * @param iv iv偏移量
	 * @return 解密后的明文
	 */
	public static String AESDecryption(String cipherText, String key, String iv) throws Exception {
		byte[] bytes = AES.decrypt(hexStringToBytes(cipherText), md5(key.getBytes()), new IvParameterSpec(iv.getBytes()));
		return new String(bytes);
	}


	public static byte[] hmacBy(String crypto, byte[] keyBytes, byte[] text) {
		return null;
	}

	public static byte[] hmacByMD5(byte[] key, byte[] text) throws NoSuchAlgorithmException {
		byte kiPad[] = new byte[64];
		byte koPad[] = new byte[64];

		Arrays.fill(kiPad, key.length, 64, (byte) 54);
		Arrays.fill(koPad, key.length, 64, (byte) 92);
		for (int i = 0; i < key.length; i++) {
			kiPad[i] = (byte) (key[i] ^ 0x36);
			koPad[i] = (byte) (key[i] ^ 0x5c);
		}

		MessageDigest md;
		try {
			md = MessageDigest.getInstance(MD5);
		} catch (NoSuchAlgorithmException e) {

			return null;
		}
		md.update(kiPad);
		md.update(text);
		byte dg[] = md.digest();
		md.reset();
		md.update(koPad);
		md.update(dg, 0, 16);
		return md.digest();
	}

	public static String hmacSign(String aValue, String aKey) {
		byte kiPad[] = new byte[64];
		byte koPad[] = new byte[64];
		byte key[];
		byte value[];
		try {
			key = aKey.getBytes(CHARSET);
			value = aValue.getBytes(CHARSET);
		} catch (UnsupportedEncodingException e) {
			key = aKey.getBytes();
			value = aValue.getBytes();
		}

		Arrays.fill(kiPad, key.length, 64, (byte) 54);
		Arrays.fill(koPad, key.length, 64, (byte) 92);
		for (int i = 0; i < key.length; i++) {
			kiPad[i] = (byte) (key[i] ^ 0x36);
			koPad[i] = (byte) (key[i] ^ 0x5c);
		}

		MessageDigest md;
		try {
			md = MessageDigest.getInstance(MD5);
		} catch (NoSuchAlgorithmException e) {

			return null;
		}
		md.update(kiPad);
		md.update(value);
		byte dg[] = md.digest();
		md.reset();
		md.update(koPad);
		md.update(dg, 0, 16);
		dg = md.digest();
		return bytesToHexString(dg);
	}

}
