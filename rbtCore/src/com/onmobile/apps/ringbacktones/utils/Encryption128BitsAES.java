package com.onmobile.apps.ringbacktones.utils;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Encryption128BitsAES {
	public static String key="abhinav.anand@OM";
    /**
    * Turns array of bytes into string
    *
    * @param buf	Array of bytes to convert to hex string
    * @return	Generated hex string
    */
    public static String asHex (byte buf[]) {
     StringBuffer strbuf = new StringBuffer(buf.length * 2);
     int i;

     for (i = 0; i < buf.length; i++) {
      if (((int) buf[i] & 0xff) < 0x10)
	    strbuf.append("0");

      strbuf.append(Long.toString((int) buf[i] & 0xff, 16));
     }

     return strbuf.toString();
    }
    public static String encryptAES128Bits(String inputString) {
    	String encryptedStr=null;
    	//System.out.println("InputString string before encryption : " + inputString);
    	 /** 
        // Get the KeyGenerator
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); // 192 and 256 bits may not be available
        // Generate the secret key specs.
        SecretKey skey = kgen.generateKey();
        //    byte[] raw = skey.getEncoded();
        **/
    	 byte[] raw = key.getBytes();
         SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

         try {
			// Instantiate the cipher
			 Cipher cipher = Cipher.getInstance("AES");
			 cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			 
			 if(inputString!=null){
			     byte[] encrypted =cipher.doFinal(inputString.getBytes());
			     BASE64Encoder encoder = new BASE64Encoder();
			     encryptedStr=encoder.encode(encrypted);
			 }
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("encrypted String : " + encryptedStr);
    	return encryptedStr;
    }
    public static String encryptAES128Bits(String inputString,String keyPassed) {
    	String encryptedStr=null;
    	//System.out.println("InputString string before encryption : " + inputString);
    	 /** 
        // Get the KeyGenerator
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); // 192 and 256 bits may not be available
        // Generate the secret key specs.
        SecretKey skey = kgen.generateKey();
        //    byte[] raw = skey.getEncoded();
        **/
    	 byte[] raw = keyPassed.getBytes();
         SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");

         try {
			// Instantiate the cipher
			 Cipher cipher = Cipher.getInstance("AES");
			 cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
			 
			 if(inputString!=null){
			     byte[] encrypted =cipher.doFinal(inputString.getBytes());
			     BASE64Encoder encoder = new BASE64Encoder();
			     encryptedStr=encoder.encode(encrypted);
			 }
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        System.out.println("encrypted String : " + encryptedStr);
    	return encryptedStr;
    }
    public static String decryptAES128Bits(String inputString) {
    	String decryptedStr=null;
    	System.out.println("InputString string before decryption : " + inputString);
    	 /** 
        // Get the KeyGenerator
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); // 192 and 256 bits may not be available
        // Generate the secret key specs.
        SecretKey skey = kgen.generateKey();
        //    byte[] raw = skey.getEncoded();
        **/
	   	 byte[] raw = key.getBytes();
	     SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	
	     // Instantiate the cipher
	     try {
			Cipher cipher = Cipher.getInstance("AES");
			 cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			 
			 if(inputString!=null){
				 BASE64Decoder decoder = new BASE64Decoder();
			     byte[] tempArr=decoder.decodeBuffer(inputString);
			     byte[] original = cipher.doFinal(tempArr);
			     decryptedStr = new String(original);
			 }
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//System.out.println(" decrypted String : " + decryptedStr);
    	return decryptedStr;
    }
    public static String decryptAES128Bits(String inputString,String keyPassed) {
    	String decryptedStr=null;
    	System.out.println("InputString string before decryption : " + inputString);
    	 /** 
        // Get the KeyGenerator
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); // 192 and 256 bits may not be available
        // Generate the secret key specs.
        SecretKey skey = kgen.generateKey();
        //    byte[] raw = skey.getEncoded();
        **/
	   	 byte[] raw = keyPassed.getBytes();
	     SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
	
	     // Instantiate the cipher
	     try {
			Cipher cipher = Cipher.getInstance("AES");
			 cipher.init(Cipher.DECRYPT_MODE, skeySpec);
			 
			 if(inputString!=null){
				 BASE64Decoder decoder = new BASE64Decoder();
			     byte[] tempArr=decoder.decodeBuffer(inputString);
			     byte[] original = cipher.doFinal(tempArr);
			     decryptedStr = new String(original);
			 }
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BadPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//System.out.println(" decrypted String : " + decryptedStr);
    	return decryptedStr;
    }
    public static void main(String[] args) throws Exception {

      String message="timepassonmobile";
      System.out.println("message:" + message);
      
      String encryptedStr=encryptAES128Bits(message);
      System.out.println("encrypted message:" + encryptedStr);
      String decryptedStr=decryptAES128Bits(encryptedStr);
      System.out.println("decrypted message:" + decryptedStr);
    }
}
