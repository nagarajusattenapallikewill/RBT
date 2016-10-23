package com.onmobile.apps.ringbacktones.utils;

import org.apache.log4j.Logger;

import com.onmobile.encryptor.PasswordEncryptor;

public class URLEncryptDecryptUtil {

	private static Logger logger = Logger
			.getLogger(URLEncryptDecryptUtil.class);

	public static String decryptAndMerge(String dbUrl) {
		String[] urlParams = null;
		String decryptedUrl = "";
		logger.info("B4 decryption: " + dbUrl);
		if (dbUrl != null && !dbUrl.isEmpty()) {
			try {
				if (dbUrl.contains("amp;")) {
					urlParams = dbUrl.split("&amp;");
				} else {
					urlParams = dbUrl.split("&");
				}
				if (urlParams != null && urlParams.length > 0) {
					int i = urlParams.length;
					for (String urlParam : urlParams) {
						if (urlParam.contains("user=")
								|| urlParam.contains("password=")) {
							String decryptedParamValue = PasswordEncryptor
									.decrypt(urlParam.substring(
											urlParam.indexOf("=") + 1,
											urlParam.length()));
							urlParam = urlParam.substring(0,
									urlParam.indexOf("="));
							urlParam = urlParam.concat("="
									+ decryptedParamValue);
							logger.info("Decrypted Value for " + urlParam
									+ " is:" + decryptedParamValue);
						}
						decryptedUrl = decryptedUrl.concat(urlParam);
						i--;
						if (i != 0)
							decryptedUrl = decryptedUrl.concat("&amp;");
					}
				} else {
					return null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			logger.info("After decryption: " + decryptedUrl);
			return decryptedUrl;
		} else {
			return null;
		}
	}

	public static String decryptUserNamePassword(String userNamePassword) {
		String decryptedUNamePassword = null;
		if (userNamePassword != null && !userNamePassword.isEmpty()) {
			try {
				decryptedUNamePassword = PasswordEncryptor
						.decrypt(userNamePassword);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return decryptedUNamePassword;
	}

	public static String encryptUserNamePassword(String userNamePassword) {
		String encryptedUNamePassword = null;
		if (userNamePassword != null && !userNamePassword.isEmpty()) {
			try {
				encryptedUNamePassword = PasswordEncryptor
						.encrypt(userNamePassword);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return encryptedUNamePassword;
	}

	public static void loadDecryptedDataToFile(String dbURL){
		
	}
	public static void main(String[] args) {

	}

}
