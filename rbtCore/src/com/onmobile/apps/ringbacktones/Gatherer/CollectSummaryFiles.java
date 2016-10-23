package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;

class CollectSummaryFiles
{

    private static Logger logger = Logger.getLogger(CollectSummaryFiles.class);
    
    private String m_prefix;
    private int m_db_days;

    public boolean getFiles(String dir, String prefix, int days)
    {
        String summerydir = null;
        
        m_prefix = prefix;
        m_db_days = days;
        File summary_dir = null;
        if (new File(dir).exists())
            summary_dir = new File(dir);
        else
        {
            logger.info("Summary Folder " + dir
                    + " does not exist");
            Tools.addToLogFile("Summary Folder " + dir + " does not exist");
            return false;

        }
        try
        {
        	summerydir = RbtGenericCacheWrapper.getInstance().getParameter("GATHERER", "GATHERER_PATH", null);
        	
            if (!new File(summerydir).exists())
            {
                new File(summerydir).mkdirs();

            }
        }
        catch (Exception E)
        {
            logger.error("", E);
        }
        File[] list = summary_dir.listFiles(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                Calendar cal = Calendar.getInstance();
                Date endDate = Tools.changeDateFormat(cal.getTime());
                cal.add(Calendar.DATE, -m_db_days);
                Date startDate = Tools.changeDateFormat(cal.getTime());
                if (name.startsWith(m_prefix))
                {
                    Date file_date = Tools.getSummaryDate(name
                            .substring(m_prefix.length() + 1,
                                       m_prefix.length() + 9));
                    if (file_date.equals(startDate)
                            || file_date.equals(endDate)
                            || (file_date.after(startDate) && file_date
                                    .before(endDate)))
                        return true;
                    else
                        return false;
                }
                else
                {
                    return false;
                }
            }
        });
        if (list == null || list.length == 0)
        {
            logger.info("No valid files in folder " + dir);
            Tools.addToLogFile("No valid files in folder " + dir);
            return false;
        }
        for (int i = 0; i < list.length; i++)
        {
        	logger.info("moving to summary folder file : "
                    + list[i]);
            Tools.moveFile(summerydir, list[i]);
        }
        return true;
    }
}