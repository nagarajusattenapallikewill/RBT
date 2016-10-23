package com.onmobile.apps.ringbacktones.common;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

/**
 * This class has all the utility methods related to exception
 * 
 * @author nandakishore
 *
 */
public class ExceptionUtil
{
    /**
     * This method returns the stack trace of the Throwable object passed as a
     * string. If the stack trace is empty, this method returns the exception 
     * message only.
     * 
     * @param e The Throwable for whom the stack trace is needed.
     * @return  The stack trace as a string.
     */
    public static String toString(Throwable e)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(byteArrayOutputStream));
        String result = new String(byteArrayOutputStream.toByteArray());
        return result;
    }

}
