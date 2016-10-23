package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;

public class FileOrganizer2
{
    private static Logger logger = Logger.getLogger(FileOrganizer2.class);

    public static void main(String args[]) throws Exception
    {
        Tools.init("FileOrganizer2", true);
        logger.info("Entered");
        BufferedReader bufferReader = null;
        File file = new File("." + File.separator + "input.txt");
        if (!file.exists() || !file.isFile() || file.length() <= 0)
            logger.info("File input.txt doesnot exist or its length is zero");
        bufferReader = new BufferedReader(new FileReader(file));
        String line = null;

        logger.info("Reading file " + file);
        while ((line = bufferReader.readLine()) != null)
        {
            try
            {
                String inputDir = null;
                String dateFormat = null;
                String outputDir = null;
                StringTokenizer st = new StringTokenizer(line, " ");
                if (st.hasMoreTokens())
                    inputDir = st.nextToken();
                if (st.hasMoreTokens())
                    dateFormat = st.nextToken();
                else
                    dateFormat = "yyyyMMdd";
                if (st.hasMoreTokens())
                    outputDir = st.nextToken();
                else
                    outputDir = inputDir;
                logger.info(" Calling arrangeFiles() : for " + inputDir
                                        + " Current time is "
                                        + Calendar.getInstance().getTime());
                arrangeFiles(inputDir, outputDir, dateFormat);
                logger.info(" Returned after call to arrangeFiles() : Current time is "
                                        + Calendar.getInstance().getTime());
            }
            catch (Exception e)
            {
                logger.error("", e);
                continue;
            }
        }

    }

    private static void arrangeFiles(String inputDir, String outputDir,
            String dateFormat)
    {
        logger.info(" input dirName is " + inputDir);
        logger.info(" output dirName is " + outputDir);
        logger.info(" dateFormat is " + dateFormat);

        Calendar currCal = Calendar.getInstance();
        int currYear = currCal.get(Calendar.YEAR);
        int currMonth = currCal.get(Calendar.MONTH);

        File parentDir = new File(inputDir);
        if (!parentDir.exists() || !parentDir.isDirectory())
        {
            logger.info(" Either " + inputDir
                    + " doesnot exist or is not a directory.");
            return;
        }

        File[] fileList = parentDir.getAbsoluteFile()
                .listFiles(new FilenameFilter()
                {
                    public boolean accept(File dir, String name)
                    {
                        if (name.endsWith(".txt"))
                            return true;
                        return false;
                    }
                });
        if (fileList == null)
        {
            logger.info(" fileList in " + inputDir
                    + " is null.");
            return;
        }
        int dateStartInFile = -1;
        for (int dateIndex = 0; dateIndex < fileList[0].getName().length()
                - (4 + dateFormat.length()); dateIndex++)
        {
            try
            {
                Integer.parseInt(fileList[0].getName()
                        .substring(dateIndex, dateIndex + dateFormat.length()));
                dateStartInFile = dateIndex;
                break;
            }
            catch (Exception e)
            {
                logger.info(" Exception Thrown while getting startIndex of Date in file Name "
                                        + fileList[0].getName() + " inside  "
                                        + inputDir);
                return;

            }
        }

        logger.info(" dateStartInFile is"
                + dateStartInFile);
        logger.info(" dateFormat.length() is "
                + dateFormat.length());
        logger.info(" STARTING movement of files in "
                + inputDir + " || Current time is "
                + Calendar.getInstance().getTime());

        for (int i = 0; i < fileList.length; i++)
        {
            try
            {
                String FileName = fileList[i].getName();

                String dateInFileName = fileList[i].getName()
                        .substring(dateStartInFile,
                                   dateStartInFile + dateFormat.length());
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
                Date date = sdf.parse(dateInFileName);
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int iYear = cal.get(Calendar.YEAR);
                int iMonth = cal.get(Calendar.MONTH);
                if (iYear < currYear || iMonth < currMonth)
                {
                    File containingFolder = new File(outputDir + File.separator
                            + iYear + File.separator + (iMonth + 1));
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

            }
        }
        logger.info(" AFTER movement of files in "
                + inputDir + " to " + outputDir + " || Current time is "
                + Calendar.getInstance().getTime());
    }

}