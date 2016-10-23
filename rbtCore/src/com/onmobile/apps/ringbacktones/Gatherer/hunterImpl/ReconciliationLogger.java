package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.apache.log4j.Logger;

public class ReconciliationLogger
{
	private static Logger logger = Logger.getLogger(ReconciliationLogger.class);
	
    private static File ManualReconciliationLocation = new File("./manualReconciliation");
    public static void log(long id, Throwable t)
    {
        ManualReconciliationLocation.mkdirs();
        File file = new File(ManualReconciliationLocation,id+".log");
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(file);
            PrintStream ps = new PrintStream(fos);
            t.printStackTrace(ps);
            ps.flush();
            fos.flush();
        }
        catch (Throwable e)
        {
            logger.error("", e);
        }
        finally
        {
            try
            {
                fos.close();
            }
            catch (Exception e2)
            {
            } 
            
        }
    }
}
