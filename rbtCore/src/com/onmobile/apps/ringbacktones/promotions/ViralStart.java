
package com.onmobile.apps.ringbacktones.promotions;



import java.util.Vector;

import org.tanukisoftware.wrapper.WrapperListener;

import org.tanukisoftware.wrapper.WrapperManager;



public class ViralStart extends Thread implements WrapperListener

{

   private ViralStart m_viralStart;
   public static int m_numThreads = 5;


   public void run()

   {

       Vector vector;
       try
       {
           vector = new Vector();
           RBTViral rbtViral = new RBTViral();
           rbtViral.init(vector);
           rbtViral.start();

       }
       catch (Exception e)
       {
           e.printStackTrace();
           //System.exit(-1);
       }
       catch (Throwable e)
       {
           e.printStackTrace();
           //System.exit(-1);
       }
       
   }



   public Integer start(String[] arg0)

   {

       m_viralStart = new ViralStart();

       m_viralStart.start();

       return null;

   }



   public int stop(int exitCode)

   {

       m_viralStart.stop();

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

       WrapperManager.start(new ViralStart(), args);

   }



}
