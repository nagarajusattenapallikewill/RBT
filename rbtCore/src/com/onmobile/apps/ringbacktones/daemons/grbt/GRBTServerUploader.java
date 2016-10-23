package com.onmobile.apps.ringbacktones.daemons.grbt;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Random;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class GRBTServerUploader extends Thread
{
	public static Logger logger = Logger.getLogger(GRBTServerUploader.class);
//	public static boolean uploadToGrbtServer = false;
//	public static boolean uploadToOperatorCentralServer = false;
	public static RBTHttpClient rbtHttpClient = null;
	private static String grbtDataFolderStr = null;
	private static String operatorName = null;
	private static String grbtCopyDataFolderStr = null;
	private static String grbtDownloadsDataFolderStr = null;
	private static String grbtTPDataFolderStr = null;
	private static File copyFolder = null;
	private static File downloadsFolder = null;
	private static File tpFolder = null;
	private static FilenameFilter filenameFilter = null;
	private static Random random = new Random(System.currentTimeMillis());
	
	public GRBTServerUploader()
	{
//		uploadToGrbtServer = RBTParametersUtils.getParamAsBoolean("GRBT", "UPLOAD_TO_GRBT_SERVER", "false");
//		uploadToOperatorCentralServer = RBTParametersUtils.getParamAsBoolean("GRBT", "UPLOAD_TO_OPR_CENTRAL_SERVER", "false");
		HttpParameters httpParameters = new HttpParameters();
		httpParameters.setMaxTotalConnections(20);
		httpParameters.setMaxHostConnections(10);
		httpParameters.setConnectionTimeout(6*1000);
		httpParameters.setSoTimeout(6*1000);
		rbtHttpClient = new RBTHttpClient(httpParameters);
		operatorName = RBTParametersUtils.getParamAsString("GRBT", "OPERATOR_NAME", null);
		operatorName = operatorName.trim();
		grbtDataFolderStr = "/var/RBT_SYSTEM_LOGS/GRBT/";
		grbtCopyDataFolderStr = grbtDataFolderStr + operatorName + "_COPY";
		grbtDownloadsDataFolderStr = grbtDataFolderStr + operatorName + "_DOWNLOADS";
		grbtTPDataFolderStr = grbtDataFolderStr + operatorName + "_TP";
		
		copyFolder = new File(grbtCopyDataFolderStr);
		downloadsFolder = new File(grbtDownloadsDataFolderStr);
		tpFolder = new File(grbtTPDataFolderStr);
		filenameFilter = new FilenameFilter()
		{
			
			@Override
			public boolean accept(File folder, String fileName)
			{
				
				StringTokenizer stk = new StringTokenizer(fileName, "_");
				String opname = "";
				String timestamp = "";
				int count = stk.countTokens();
				if(count>3){
					opname = stk.nextToken();
					int i=1;
					while(i<count-1){
						stk.nextToken();
						i++;
					}
					timestamp = stk.nextToken().replaceFirst(".csv", "");
				}
				int time =0;
				try{
					time = Integer.parseInt(timestamp);
				}catch(Exception e){
				}

				
				if(opname.equalsIgnoreCase(operatorName) && fileName.endsWith(".csv") && timestamp.length()==10 && time>0){
					logger.info("Filtered and returning " + fileName);
					return true;
				}
				logger.info("Filtered failed for " + fileName);
				return false;
			}
		};
	}
	
	@Override
	public void run()
	{
		try
		{
			
			String copyPaths = RBTParametersUtils.getParamAsString("GRBT", "COPY_FILE_PATHS", grbtCopyDataFolderStr);
			StringTokenizer stk = new StringTokenizer(copyPaths, "|");
			while(stk.hasMoreTokens()){
				String temp = stk.nextToken();
				File copyPath = new File(temp);
				if(RBTParametersUtils.getParamAsBoolean("GRBT", "UPLOAD_DATA_COPY", "FALSE"))
					uploadFile(copyPath);
			}

			String downloadPaths = RBTParametersUtils.getParamAsString("GRBT", "DOWNLOAD_FILE_PATHS", grbtDownloadsDataFolderStr);
			stk = new StringTokenizer(downloadPaths, "|");
			while(stk.hasMoreTokens()){
				String temp = stk.nextToken();
				File downloadPath = new File(temp);
				if(RBTParametersUtils.getParamAsBoolean("GRBT", "UPLOAD_DATA_PURCHASE", "FALSE"))
					uploadFile(downloadPath);
			}

			String TPPaths = RBTParametersUtils.getParamAsString("GRBT", "TP_FILE_PATHS", grbtTPDataFolderStr);
			stk = new StringTokenizer(TPPaths, "|");
			while(stk.hasMoreTokens()){
				String temp = stk.nextToken();
				File TPPath = new File(temp);
				if(RBTParametersUtils.getParamAsBoolean("GRBT", "UPLOAD_DATA_TP", "FALSE"))
					uploadFile(TPPath);
			}
			
		}
		catch(Exception e)
		{
			logger.error("Errorin infinite loop", e);
		}
		finally
		{
			sleepNow();
		}
	}

	private void uploadFile(File folder)
	{
		String url = RBTParametersUtils.getParamAsString("GRBT", "GRBT_SERVER_URL", null);
		if(url == null)
		{
			logger.info("Not uploading data to grbt server as param GRBT, GRBT_SERVER_URL is missing");
			return;
		}	
		File[] Files = folder.listFiles(filenameFilter);
		logger.info("Number of files found in " + folder.getAbsolutePath() + "  = "+(Files == null ? 0 : Files.length));
		for (File grbtFile : Files)
		{
			try
			{
				logger.info("uploading file "+grbtFile.getAbsolutePath());
				HashMap<String, File> fileMap = new HashMap<String, File>();
				fileMap.put(grbtFile.getName(), grbtFile);
				HttpResponse httpResponse = rbtHttpClient.makeRequestByPost(url, null, fileMap);
				if(httpResponse.getResponseCode() == 200 && httpResponse.getResponse() != null && httpResponse.getResponse().trim().toLowerCase().equalsIgnoreCase("SUCCESS")){
					renameFile(grbtFile);	
				}else
					logger.info("Response code is not 200 or response is null or response is not success");
			}
			catch(Exception e)
			{
				logger.error("Error uploading file "+grbtFile.getAbsolutePath() + " to grbt server",e);
			}
		}
		
	}

	private void renameFile(File grbtFile)
	{
		try
		{
			String destinationFileName = grbtFile.getAbsolutePath()+".done";
			File destinationFile = new File(destinationFileName);
			FileUtils.copyFile(grbtFile, destinationFile);
			logger.info("going to delete " + grbtFile + " as it is available now as " + destinationFile);
			FileUtils.forceDelete(grbtFile);
		}
		catch(Exception e)
		{
			logger.error("Error renaming file "+grbtFile.getAbsolutePath(),e);
		}
	}
	
	private static void sleepNow()
	{
		try
		{
			int sleepTime = random.nextInt(50)+1;
			logger.info("sleeping for " + sleepTime + " mins");
			Thread.sleep(sleepTime*60*1000); // this ensures that upload from all rbtboxes to grbt server is spread uniformly over time,
			// sleep time of just 5 mins would mean that all servers uplaod all their in the first 5 mins of every hour, 
			// thus placing grbt server under stress 
			logger.info("waking up");
		}
		catch(Exception e)
		{
			logger.info("Got Exception " , e );
		}
	}
}
