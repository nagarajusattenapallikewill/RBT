package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;

public class FileOrganizer extends Thread
{
    private static Logger logger = Logger.getLogger(FileOrganizer.class);
    
    private static RbtGenericCacheWrapper rbtGenericCacheWrapper = null;
    
    public FileOrganizer m_fileOrganizer = null;
    private static String m_dirDate = null;

    private static final String PARAMETER_TYPE_GATHERER = "GATHERER";
    private static final String PARAMETER_TYPE_DAEMON = "DAEMON";

    public FileOrganizer() throws Exception
    {

        logger.info("Entered");
        
        rbtGenericCacheWrapper = RbtGenericCacheWrapper.getInstance();
        logger.info("Exiting");

    }

    public void run()
    {
        logger.info("Entered");

        try
        {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String gpOrganizeDate = getParamAsString("GATHERER", "FILE_ORGANIZE_DATE", null);
            String gpOrganizeLastDate = getParamAsString("GATHERER","FILE_ORGANIZE_LAST_DATE", null);

            if (!(gpOrganizeDate == null || gpOrganizeLastDate == null))
            {
                Calendar currCal = Calendar.getInstance();
                Calendar lastOrganizedCal = Calendar.getInstance();
                lastOrganizedCal.setTime(sdf.parse(gpOrganizeLastDate));
                Calendar configureCal = Calendar.getInstance();
                configureCal.setTime(sdf.parse(gpOrganizeDate));

                logger.info(" Current Time is "
                        + currCal.getTime());
                logger.info(" Last Organized Date is "
                        + lastOrganizedCal.getTime());
                logger.info(" Configured Time is "
                        + configureCal.getTime());

                if (
                		(
                			currCal.get(Calendar.MONTH) > lastOrganizedCal.get(Calendar.MONTH) 
                			||
                			currCal.get(Calendar.YEAR) > lastOrganizedCal.get(Calendar.YEAR)
                		)
                		&&
                		currCal.get(Calendar.DATE) >= configureCal.get(Calendar.DATE)
                	)
                {
                    HashMap dirMap = new HashMap();
                    String moHelperSdrDir = getParamAsString("SMS", "SDR_WORKING_DIR", null);
                    if (moHelperSdrDir != null)
                    {
                        dirMap.put(moHelperSdrDir, "ddMMyyyy");
                        
                        logger.info(" Added to dirMap " + moHelperSdrDir);
                    }

                    if (getParamAsString("COMMON", "USE_SUBSCRIPTION_MANAGER", "TRUE").equalsIgnoreCase("TRUE"))
                    {
                     //   Parameters dp = rbtDBManager.getParameter(PARAMETER_TYPE_DAEMON,"SUBMGR_SDR_WORKING_DIR");
                    	String subMgrSdrDir = getParamAsString("DAEMON", "SUBMGR_SDR_WORKING_DIR", null);
                        if (subMgrSdrDir != null)
                        /*
                         * dirMap.put(dp.value()+File.separator + "smssdr",
                         * "yyyyMMdd"); }
                         * 
                         * Parameters gpTelServer =
                         * rbtDBManager.getParameter(PARAMETER_TYPE_GATHERER,"TELEPHONY_SERVERS");
                         * Parameters gpCdrRoot =
                         * rbtDBManager.getParameter(PARAMETER_TYPE_GATHERER,"CDR_ROOT");
                         * ArrayList telServerList = null; Iterator it = null;
                         * if(gpTelServer != null && gpCdrRoot != null)
                         */
                        {
                            /*
                             * telServerList =
                             * Tools.tokenizeArrayList(gpTelServer.value(),
                             * null); it = telServerList.iterator();
                             * while(it.hasNext())
                             * dirMap.put(locateCDRFiles((String)it.next(),
                             * gpCdrRoot.value()), "yyyyMMdd");
                             */
                            dirMap.put(subMgrSdrDir + File.separator + "smssdr", "yyyyMMdd");
                            logger.info(" Added to dirMap " + subMgrSdrDir
                                                       + File.separator
                                                       + "smssdr");
                        }
                        //Parameters gpExtraFolders =
                        // rbtDBManager.getParameter(PARAMETER_TYPE_GATHERER,"EXTRA_FOLDERS_TO_ORGANIZE");
                     //   dp = rbtDBManager.getParameter(PARAMETER_TYPE_DAEMON,"SDR_WORKING_DIR");
                        String sdrDir = getParamAsString("DAEMON", "SDR_WORKING_DIR", null);
                        /*
                         * if(gpExtraFolders != null && gpExtraFolders.value() !=
                         * null) { StringTokenizer st1 = new
                         * StringTokenizer(gpExtraFolders.value(), ";");
                         * while(st1.hasMoreTokens()) { StringTokenizer st2= new
                         * StringTokenizer(st1.nextToken(), ",");
                         * if(st2.countTokens() == 2)
                         * dirMap.put(st2.nextToken(),st2.nextToken());
                         *  } }
                         */
                        if (sdrDir != null)
                        {
                            dirMap.put(sdrDir + File.separator + "smssdr",
                                       "yyyyMMdd");
                            logger.info(" Added to dirMap " + sdrDir
                                                       + File.separator
                                                       + "smssdr");
                        }
                        arrangeFiles(dirMap);
                        logger.info(" Returned after call to arrangeFiles() : Current time is "
                                                + Calendar.getInstance()
                                                        .getTime());
                        rbtGenericCacheWrapper.updateParameter(PARAMETER_TYPE_GATHERER, "FILE_ORGANIZE_LAST_DATE", sdf.format(currCal.getTime()));
//                        rbtDBManager.updateParameter(PARAMETER_TYPE_GATHERER, "FILE_ORGANIZE_LAST_DATE", sdf.format(currCal.getTime()));
                        logger.info(" Updated  FILE_ORGANIZE_LAST_DATE to "
                                                   + sdf.format(currCal
                                                           .getTime()));

                    }
                    else
                    {
                        logger.info(" Not Organizing Files because date is not right yet. ");
                    }
                }
                else
                {
                    logger.info(" Either FILE_ORGANIZE_LAST_DATE or FILE_ORGANIZE_DATE is not set. So not organizing files.");
                }
            }
        }
        catch (Exception e)
        {
        }
    }

    private void arrangeFiles(HashMap dirMap)
    {
        Calendar currCal = Calendar.getInstance();
        int currMonth = currCal.get(Calendar.MONTH);
        int currYear = currCal.get(Calendar.YEAR);
        Iterator dirIt = dirMap.keySet().iterator();
        String _method = "arrangeFiles()";
        while (dirIt.hasNext())
        {
            try
            {
                String dirName = (String) dirIt.next();
                m_dirDate = (String) dirMap.get(dirName);
                logger.info(" dirName is " + dirName);
                logger.info(" m_dirDate is " + m_dirDate);

                File cdrDir = new File(dirName);
                if (!cdrDir.exists() || !cdrDir.isDirectory())
                {
                    logger.info(" Either " + dirName
                            + " doesnot exist or is not a directory.");
                    continue;
                }

                File[] fileList = cdrDir.getAbsoluteFile()
                        .listFiles(new FilenameFilter()
                        {
                            public boolean accept(File dir, String name)
                            {
                                logger.info("dir="+dir+", name="+name);
                            	if (name.endsWith(".LOG")
                                        && name.length() >= m_dirDate.length() + 4)
                                {
                                    return true;
                                }
                                return false;
                            }
                        });
                if (fileList == null)
                {
                    logger.info(" fileList in " + dirName
                            + " is null.");
                    continue;
                }
                int dateStartInFile = -1;
                for (int dateIndex = 0; dateIndex <= fileList[0].getName()
                        .length()
                        - (4 + m_dirDate.length()); dateIndex++)
                {
                    try
                    {
                    	Integer.parseInt(fileList[0].getName()
                                .substring(dateIndex,
                                           dateIndex + m_dirDate.length()));
                        dateStartInFile = dateIndex;
                        break;
                    }
                    catch (Exception e)
                    {
                        logger.info(" Exception Thrown while getting startIndex of Date in file Name "
                                                + fileList[0].getName()
                                                + " inside  " + dirName);
                        continue;
                    }
                }
                logger.info(" dateStartInFile is"
                        + dateStartInFile);
                logger.info(" m_dirDate.length() is "
                        + m_dirDate.length());
                logger.info(" STARTING movement of files in " + dirName
                                        + " || Current time is "
                                        + Calendar.getInstance().getTime());
                for (int i = 0; i < fileList.length; i++)
                {
                    try
                    {
                        String FileName = fileList[i].getName();

                        String dateInFileName = fileList[i]
                                .getName()
                                .substring(dateStartInFile,
                                           dateStartInFile + m_dirDate.length());
                        SimpleDateFormat sdf = new SimpleDateFormat(m_dirDate);
                        Date date = sdf.parse(dateInFileName);
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int iYear = cal.get(Calendar.YEAR);
                        int iMonth = cal.get(Calendar.MONTH);
                        if (iYear < currYear || iMonth < currMonth)
                        {
                            File containingFolder = new File(dirName
                                    + File.separator + iYear + File.separator
                                    + (iMonth + 1));
                            if (!containingFolder.exists())
                                containingFolder.mkdirs();
                            Tools.moveFile(containingFolder.getCanonicalPath(),
                                           fileList[i]);
                            fileList[i].delete();

                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("", e);
                        continue;
                    }
                }
                logger.info(" AFTER movement of files in "
                        + dirName + " || Current time is "
                        + Calendar.getInstance().getTime());
            }
            catch (Exception e)
            {
                logger.error("", e);
                continue;
            }
        }

    }
    
    public String getParamAsString(String type, String param, String defualtVal)
    {
    	try{
    		return rbtGenericCacheWrapper.getParameter(type, param, defualtVal);
    	}catch(Exception e){
    		logger.info("Unable to get param ->"+param +"  type ->"+type);
    		return defualtVal;
    	}
    }
    private int getParamAsInt(String type, String param, int defaultVal)
	{
		try{
			String paramVal = rbtGenericCacheWrapper.getParameter(type, param, defaultVal+"");
			return Integer.valueOf(paramVal);   		
		}catch(Exception e){
			logger.info("Unable to get param ->"+param +" returning defaultVal >"+defaultVal);
			return defaultVal;
		}
	}

}