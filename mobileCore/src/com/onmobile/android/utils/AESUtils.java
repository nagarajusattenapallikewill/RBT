package com.onmobile.android.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;

public class AESUtils {
	static Logger logger = Logger.getLogger(AESUtils.class);

	public static String encrypt(String plainText, String key) {
		String encryptedString = null;
		try {
			if (key == null || key.trim().length() == 0) {
				throw new InvalidKeyException("Invalid key: '" + key + "'. Encryption failed!");
			}
			if (plainText == null) {
				logger.error("Invalid plainText: '" + plainText + "'. Encryption failed!");
				return null;
			}
			byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec keySpec;

			keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			// Instantiate the cipher
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

			byte[] encryptedTextBytes = cipher.doFinal(plainText.getBytes("UTF-8"));
			encryptedString = new Base64().encodeAsString(encryptedTextBytes);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			logger.error(e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			logger.error(e.getMessage(), e);
		} catch (BadPaddingException e) {
			logger.error(e.getMessage(), e);
		}
		return encryptedString;
	}

	public static String decrypt(String encryptedText, String key) {
		String decryptedString = null;
		try {
			if (key == null || key.trim().length() == 0) {
				throw new InvalidKeyException("Invalid key: '" + key + "'. Decryption failed!");
			}
			if (encryptedText == null) {
				logger.error("Invalid encryptedText: '" + encryptedText + "'. Decryption failed!");
				return null;
			}
			byte[] ivBytes = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
			AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
			SecretKeySpec keySpec;

			keySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			// Instantiate the cipher
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

			byte[] encryptedTextBytes = Base64.decodeBase64(encryptedText);
			byte[] decryptedTextBytes = cipher.doFinal(encryptedTextBytes);
			decryptedString = new String(decryptedTextBytes);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchAlgorithmException e) {
			logger.error(e.getMessage(), e);
		} catch (NoSuchPaddingException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidKeyException e) {
			logger.error(e.getMessage(), e);
		} catch (InvalidAlgorithmParameterException e) {
			logger.error(e.getMessage(), e);
		} catch (IllegalBlockSizeException e) {
			logger.error(e.getMessage(), e);
		} catch (BadPaddingException e) {
			logger.error(e.getMessage(), e);
		}
		return decryptedString;
	}
}