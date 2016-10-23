
package com.onmobile.apps.ringbacktones.daemons;

import org.tanukisoftware.wrapper.WrapperListener;
import org.tanukisoftware.wrapper.WrapperManager;


public class DaemonManagerStart extends Thread implements WrapperListener

{

   private DaemonManagerStart m_daemonManagerStart;



   public void run()

   {

	   RBTDaemonManager.m_rbtDaemonManager = new RBTDaemonManager();
       if ( RBTDaemonManager.m_rbtDaemonManager.getConfigValues())
       {
           System.out.println("RBT daemon started...");
           RBTDaemonManager.m_rbtDaemonManager.start();
           System.out.println("RBT daemon stopped...");
       }
       else
       {
           System.out.println("Error in config parameters. Exiting...");
       }

   }



   public Integer start(String[] arg0)

   {

       m_daemonManagerStart = new DaemonManagerStart();

       m_daemonManagerStart.start();

       return null;

   }



   public int stop(int exitCode)

   {

       m_daemonManagerStart.stop();

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

       WrapperManager.start(new DaemonManagerStart(), args);

   }



}
