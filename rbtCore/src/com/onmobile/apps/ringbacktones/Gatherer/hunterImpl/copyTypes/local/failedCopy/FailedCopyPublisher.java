package com.onmobile.apps.ringbacktones.Gatherer.hunterImpl.copyTypes.local.failedCopy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import com.enterprisedt.net.ftp.FTPClient;
import com.enterprisedt.net.ftp.FTPException;
import com.onmobile.apps.ringbacktones.Gatherer.Utility;
import com.onmobile.apps.ringbacktones.common.ThreadUtil;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.ViralSMSTableImpl;
import com.onmobile.apps.ringbacktones.daemons.FtpDownloader;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterException;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressivePublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class FailedCopyPublisher extends ProgressivePublisher
{
    private static String fileLocation = "someting";
    private FileReader fileReader = null;
    private FileDeleteListener fileDeleteListener = null;
    private LineNumberReader lineNumberReader = null;
    private int fileId = 0;
    String currectFile = null;
    private Vector<String> filesToProcess = new Vector<String>(); 
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    
    static private String ftpIp = null;
    static private int ftpPort = 21;
    static private String ftpUsername = null;
    static private String ftpPasswd = null;
    static private String ftpCircleDir = null;
    static private String localDir = null;
    static private int sleepTimeInMin = 5;

	static 
    {
		ftpIp = Utility.getParamAsString("DAEMON", "FTP_IP_FOR_RETRY_COPY", null);
		ftpPort = Utility.getParamAsInt("DAEMON", "FTP_PORT_FOR_RETRY_COPY", 21);
		ftpUsername = Utility.getParamAsString("DAEMON", "FTP_USERNAME_FOR_RETRY_COPY",	null);
		ftpPasswd = Utility.getParamAsString("DAEMON", "FTP_PASSWORD_FOR_RETRY_COPY", null);
		ftpCircleDir = Utility.getParamAsString("DAEMON", "FTP_CIRCLE_DIR_FOR_RETRY_COPY", null);
		localDir = Utility.getParamAsString("DAEMON", "LOCAL_DIR_FOR_RETRY_COPY", "./ftpLocation");
		sleepTimeInMin = Utility.getParamAsInt("GATHERER", "GATHERER_SLEEP_INTERVAL", 5);
		File file = new File(localDir);
		if (!file.exists()) {
			// local directory is does not exist
			file.mkdirs();
		}
	}
    
    @Override
    protected void executeQuery(int count) throws HunterException
    {
        try
        {
	        if(initIfRequired())
	        {
	            currectFile = getFileName();
	            fileReader = new FileReader(currectFile);
	            lineNumberReader = new LineNumberReader(fileReader);
	            fileDeleteListener = new FileDeleteListener(currectFile);
	        }
	        else
	        {
	            ThreadUtil.sleepMin(3);
	        }
        }
        catch(FileNotFoundException e)
        {
        	throw new HunterException(e);
        }
        catch(FTPException e)
        {
        	throw new HunterException(e);
        }
        catch(IOException e)
        {
        	throw new HunterException(e);
        }
    }

    private boolean initIfRequired() throws IOException, FTPException 
    {
        if(filesToProcess.size() == 0)
        {
            getAllFilesFromFTPServer();
        	File localPath = new File(localDir);// Get the FTP desti path name;
            File files[] = localPath.listFiles();
            if(files != null && files.length != 0)
            {
                for (int i = 0; i < files.length; i++)
                {
                    filesToProcess.add(files[i].getAbsolutePath());
                }
            }
        }
        return filesToProcess.size() != 0;
    }

    private String getFileName()
    {
        String fileName = filesToProcess.get(0);
        return fileName;
    }

    @Override
    protected void finaliseQuery()
    {
        try
        {
            fileReader.close();
        }
        catch (Exception e)
        {
        }
        if(fileDeleteListener != null)
        {
            fileDeleteListener.setFileReadCompleted(true);
        }
        if(filesToProcess != null)
        {
            filesToProcess.remove(currectFile);
        }
    }

    @Override
    protected QueueComponent getNextQueueComponent() throws HunterException
    {
        return queueComponent;
    }
    private QueueComponent queueComponent = null; 
    @Override
    protected boolean hasMoreQueueComponents() throws HunterException
    {
        try
        {
            if(lineNumberReader == null)
            {
                return false;
            }
	    	String lineData = lineNumberReader.readLine();
	        if(lineData == null)
	        {
	            return false;
	        }
	        
	        String[] strAr = lineData.split(",");
			String strTime = strAr[1];
			Date time = null;
			time = sdf.parse(strTime);
			String keyPressed = "s";
			if(strAr != null && strAr.length > 2)
				keyPressed = strAr[2].trim();
			String copyContent = strAr[0];
			StringTokenizer st = new StringTokenizer(strAr[0], ":");
			String subscriberID = null;
			String callerID = null;
			String rbtWavFile = null;
			String catId = null;
			String status = null;
			if (st.hasMoreTokens())
				subscriberID = st.nextToken();
			if (st.hasMoreTokens())
				callerID = st.nextToken();
			if (st.hasMoreTokens())
				rbtWavFile = st.nextToken();
			if (st.hasMoreTokens())
				catId = st.nextToken();
			if (st.hasMoreTokens())
				status = st.nextToken();
			HashMap<String, String> hashMap = new HashMap<String,String>();
			if(keyPressed != null && keyPressed.length() > 0 && !keyPressed.equalsIgnoreCase("null"))
				hashMap.put(iRBTConstant.KEYPRESSED_ATTR, keyPressed);
			
			ViralSMSTableImpl vst = new ViralSMSTableImpl(subscriberID, time, "COPY", callerID, rbtWavFile+":"+catId+":"+status, 0, null, null, null);
			queueComponent = new FailedCopyQueueComponent(vst,null,fileDeleteListener,hashMap);
			//TODO create the Queue Component.
	        fileDeleteListener.incrementTotal();
	        return true;
        }
        catch(IOException e)
        {
        	throw new HunterException(e);
        }
        catch(ParseException e)
        {
        	throw new HunterException(e);
        }
    }

    @Override
    protected void setPresentQueryCount(int addCount, int count)
    {
        if(addCount == 0)
        {
            ThreadUtil.sleepSec(30);// TODO read from config
        }

    }

    @Override
    public int getWorkerThreadPriority()
    {
        return 5;
    }
    
	private void getAllFilesFromFTPServer() throws IOException, FTPException {
		String method = "getAllFilsFormFTPServer";
		FTPClient ftpClient = null;
		if (ftpIp != null && ftpUsername != null) {
			ftpClient = new FtpDownloader().ftpConnect(ftpIp, ftpPort, ftpUsername, ftpPasswd, ftpCircleDir);
			if (ftpClient == null) {
				StringBuilder builder = new StringBuilder("Unable to initialize the FTPClient ");
				builder.append(ftpIp + " ");
				builder.append(ftpPort + " ");
				builder.append(ftpUsername + " ");
				builder.append(ftpPasswd + " ");
				builder = null;
				return;
			}
		}
		if(ftpClient != null)
		{
    		String[] fileList = ftpClient.dir();
    		if (fileList == null || fileList.length == 0) {
    			// no files are there in the FTP dir close the ftp client and return
    			ftpClient.quit();
    			return;
    		}
    		try {
    			for (int i = 0; i < fileList.length; i++) {
    				String fileName = fileList[i];
    				fileName = fileName.substring(fileName.lastIndexOf(File.separator) + 1);
    				fileName = fileName.toUpperCase();
    				if (fileName.indexOf("RBT_COPY_RETRY_CDR") == -1 || fileName.indexOf(".TXT") == -1) {
    					// file name format is wrong
    					continue;
    				}
    				// copying the file from FTP server to local dir. Changing the
    				// file name in the local dir to upper case.
    				ftpClient.get(localDir + File.separator + fileName, fileList[i]);
    				ftpClient.delete(fileList[i]);
    			}
    		} finally {
    			// close the FTP connection
    			ftpClient.quit();
    		}
		}
	}
}
