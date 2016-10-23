package com.onmobile.apps.ringbacktones.subscriptions;

import java.net.ServerSocket;

import org.apache.log4j.Logger;
import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;

public class CommonCopyStart extends Thread implements WrapperListener
{
	private static Logger logger = Logger.getLogger(CommonCopyStart.class);
   private CommonCopyStart m_commonCopyStart;

   public void run()
   {
		try
        {
        	new ServerSocket (14000);
    	}
        catch(Exception e)
        {
			System.err.println("The port 16000 is in use or another rbt copier is already running");
			System.out.println("The port 16000 is in use or another rbt copier is already running");
			System.exit(-1);
		}
        
        CommonCopyHelper m_ccHelper = CommonCopyHelper.getInstance();
        System.out.println("Started ...");
        try
        {
            if (m_ccHelper.init())
            	m_ccHelper.start();
        }
        catch (Throwable e)
        {
            logger.error("", e);
            e.printStackTrace();
        }
        finally
        {
//            System.exit(0);
        }
    }
   
   public Integer start(String[] arg0)

   {
	   m_commonCopyStart = new CommonCopyStart();
	   m_commonCopyStart.start();
       return null;
   }

   public int stop(int exitCode)
   {
	   m_commonCopyStart.stop();
       return exitCode;
   }

   public void controlEvent(int event)
   {
       if (WrapperManager.isControlledByNativeWrapper())
       {
           // The Wrapper will take care of this event
       }
       else
       {
           // We are not being controlled by the Wrapper, so
           //  handle the event ourselves.
           if ((event == WrapperManager.WRAPPER_CTRL_C_EVENT)
                   || (event == WrapperManager.WRAPPER_CTRL_CLOSE_EVENT)
                   || (event == WrapperManager.WRAPPER_CTRL_SHUTDOWN_EVENT))
           {
               WrapperManager.stop(0);
           }
       }
   }

   public static void main(String[] args)
   {
       // Start the application. If the JVM was launched from the native
       //  Wrapper then the application will wait for the native Wrapper to
       //  call the application's start method. Otherwise the start method
       //  will be called immediately.
       WrapperManager.start(new CommonCopyStart(), args);
   }
}
