package com.onmobile.apps.ringbacktones.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

/**
 * @author vinayasimha.patil
 *  
 */
public class SDRParser
{
	private static Logger logger = Logger.getLogger(SDRParser.class);
	
    public static String[][] getSDRData(String workingDir, String subscriberID,
            int days)
    {
        //Tools.logDetail("SDRParser", "getSDRData", "RBT:: workingDir = "+
        // workingDir + ": subscriberID = "+ subscriberID + ": days = "+ days);
        String[][] sdrData = null;
        ArrayList sdrDataList = new ArrayList();

        if (RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "WRITE_SMSSDR_INTO_DB", "FALSE"))
        {
            RBTSMS rbtSms = new RBTSMSImpl();
            sdrDataList = rbtSms.getSubscriberSMS(subscriberID);
        }
        else
        {
            sdrDataList = getSMSDataFromFile(workingDir, subscriberID, days);
        }

        if (sdrDataList != null && sdrDataList.size() > 0)
            sdrData = (String[][]) sdrDataList.toArray(new String[0][]);

        return sdrData;

    }

    private static ArrayList getSMSDataFromFile(String workingDir,
            String subscriberID, int days)
    {

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, (-1 * days));

        File workingDirFile = new File(workingDir + File.separator
                + "SMSDetails" + File.separator + "smssdr");

        if (!workingDirFile.exists() && !workingDirFile.isDirectory())
            return null;

        SDRFileFilter sdrFileFilter = new SDRParser.SDRFileFilter(cal.getTime()
                .getTime());
        File[] sdrFiles = workingDirFile.listFiles(sdrFileFilter);
        if (sdrFiles == null && sdrFiles.length == 0)
            return null;

        sdrFiles = sortFilesOnLastModifiedTime(sdrFiles);

        FileReader fr = null;
        BufferedReader br = null;

        ArrayList sdrDataList = new ArrayList();

        for (int i = 0; i < sdrFiles.length; i++)
        {
            try
            {
                if (sdrFiles[i].isDirectory())
                    continue;

                logger.info("RBT:: Parsing SDR File = "
                                        + sdrFiles[i].getAbsolutePath());
                fr = new FileReader(sdrFiles[i].getAbsolutePath());
                br = new BufferedReader(fr);
                String srdLog = null;

                br.readLine(); //To ignore header
                while ((srdLog = br.readLine()) != null)
                {
                    if (srdLog.indexOf("," + subscriberID + ",") < 0)
                        continue;

                    srdLog = srdLog.trim();
                    if (srdLog.equalsIgnoreCase(""))
                        continue;

                    String[] tempSDRData = new String[10];
                    StringTokenizer sdrTokens = new StringTokenizer(srdLog, ",");

                    ArrayList sdrTokensList = new ArrayList();
                    while (sdrTokens.hasMoreTokens())
                    {
                        sdrTokensList.add(sdrTokens.nextToken());
                    }

                    tempSDRData[0] = (String) sdrTokensList.get(0);
                    tempSDRData[1] = (String) sdrTokensList.get(1);

                    if (!tempSDRData[1].equalsIgnoreCase(subscriberID))
                        continue;

                    tempSDRData[2] = (String) sdrTokensList.get(2);
                    tempSDRData[5] = (String) sdrTokensList.get(sdrTokensList
                            .size() - 5);
                    tempSDRData[6] = (String) sdrTokensList.get(sdrTokensList
                            .size() - 4);
                    tempSDRData[7] = (String) sdrTokensList.get(sdrTokensList
                            .size() - 3);
                    tempSDRData[8] = (String) sdrTokensList.get(sdrTokensList
                            .size() - 2);
                    tempSDRData[9] = (String) sdrTokensList.get(sdrTokensList
                            .size() - 1);

                    int noOfRequestTokens = Integer.parseInt(tempSDRData[8]);
                    int noOfReplyTokens = Integer.parseInt(tempSDRData[9]);

                    tempSDRData[3] = "";
                    for (int j = 3; j < (noOfRequestTokens + 3); j++)
                        tempSDRData[3] += (String) sdrTokensList.get(j) + ",";
                    tempSDRData[3] = tempSDRData[3].substring(0, tempSDRData[3]
                            .length() - 1);

                    tempSDRData[4] = "";
                    for (int j = 3 + noOfRequestTokens; j < (noOfReplyTokens + 3 + noOfRequestTokens); j++)
                        tempSDRData[4] += (String) sdrTokensList.get(j) + ",";
                    tempSDRData[4] = tempSDRData[4].substring(0, tempSDRData[4]
                            .length() - 1);

                    sdrDataList.add(tempSDRData);
                }
            }
            catch (FileNotFoundException e)
            {
            	logger.error("", e);
            }
            catch (IOException e)
            {
            	logger.error("", e);
            }
            finally
            {
                try
                {
                    br.close();
                    fr.close();
                }
                catch (IOException e)
                {
                	logger.error("", e);
                }

            }
        }
        /*
         * if(sdrDataList.size() > 0) sdrData = (String[][])
         * sdrDataList.toArray(new String[0][]);
         */

        return sdrDataList;
    }

    private static File[] sortFilesOnLastModifiedTime(File[] files)
    {
        File[] sortedFiles = (File[]) files.clone();

        File tempFile = null;
        for (int i = 0; i < sortedFiles.length; i++)
        {
            for (int j = i + 1; j < sortedFiles.length; j++)
            {
                if (sortedFiles[j].lastModified() < sortedFiles[i]
                        .lastModified())
                {
                    tempFile = sortedFiles[i];
                    sortedFiles[i] = sortedFiles[j];
                    sortedFiles[j] = tempFile;
                }
            }
        }

        return sortedFiles;
    }

    private static class SDRFileFilter implements FileFilter
    {
        private long days = 0L;

        public SDRFileFilter(long days)
        {
            this.days = days;
        }

        public boolean accept(File pathname)
        {
            logger.info("RBT:: File Name = " + pathname.getAbsolutePath());
            logger.info( "RBT::  = " + new Date(pathname.lastModified()));
            logger.info( "RBT::  = " + new Date(days));
            logger.info( "RBT::  = " + new Date(System.currentTimeMillis()));

            if (days <= pathname.lastModified()
                    && pathname.lastModified() <= System.currentTimeMillis())
                return true;

            return false;
        }
    }

    public static void main(String[] args)
    {
        //String[][] sdrData = getSDRData("e:/SDR", "9986026979", 30);
        //start = System.currentTimeMillis();
        //new SDRParser1().work();
    }

}