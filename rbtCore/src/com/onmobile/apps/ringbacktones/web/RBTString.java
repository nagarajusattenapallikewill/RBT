package com.onmobile.apps.ringbacktones.web;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

import sun.misc.BASE64Encoder;

/**
 * @author laxmankumar
 *  
 */
public final class RBTString
{

    public static Logger basicLogger = Logger.getLogger(RBTString.class
            .getName());

    private static RBTString instance;

    private RBTString()
    {
    }

    public synchronized String encrypt(String plaintext)
    {
        MessageDigest md = null;
        try
        {
            md = MessageDigest.getInstance("SHA");
        }
        catch (NoSuchAlgorithmException e)
        {
            basicLogger
                    .error(
                           "Exception while gettting MessageDigest instance for encrypting pwd...",
                           e);
        }
        try
        {
            md.update(plaintext.getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            basicLogger
                    .error(
                           "Exception while updating the MessageDigest instace for encrypting pwd...",
                           e);
        }

        byte raw[] = md.digest();
        String hash = (new BASE64Encoder()).encode(raw);
        return hash;
    }

    public static synchronized RBTString getInstance()
    {
        if (instance == null)
        {
            instance = new RBTString();
        }
        return instance;
    }

}