package com.onmobile.apps.ringbacktones.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;


public class Log4jErrorConnector extends OutputStream 
{
	 static
	    {	System.out.println("Initializing the Error Connector ");
			init();
	    }
	    private static Logger logger = Logger.getLogger(Log4jErrorConnector.class);
	    private int lineEnd = (int) '\n';
	    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

	    public static void init()
	    {	
	        PrintStream ps = new PrintStream(new Log4jErrorConnector());
	        System.setErr(ps);
	    }
	    public void write(int b) throws IOException
	    {
//	        if (b == lineEnd)
//	        {
//	            logger.error(baos.toString());
//	            baos.reset();
//	        }
//	        else
//	        {
//	            baos.write(b);
//	        }
	    }
}
