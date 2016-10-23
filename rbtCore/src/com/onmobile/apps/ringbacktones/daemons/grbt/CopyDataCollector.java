package com.onmobile.apps.ringbacktones.daemons.grbt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

public class CopyDataCollector extends Thread implements iRBTConstant
{
	private static String gathererPath = null;
	private static Logger logger = Logger.getLogger(CopyDataCollector.class);
	private static SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
	private static SimpleDateFormat sdfGrbt = new SimpleDateFormat("yyyyMMddHH");
	private static SimpleDateFormat sdfCopyLine = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static long sleepTime = 5*60*1000;
	private static String grbtDataFolderStr = null;
	private static String operatorName = null;
	private static String fileType = "COPY";
	
	private static String grbt_logger_type = null;
	
	static
	{
		try
		{
			gathererPath = RBTParametersUtils.getParamAsString(GATHERER, "GATHERER_PATH", ".") + File.separator + "Trans" + File.separator;
			operatorName = RBTParametersUtils.getParamAsString("GRBT", "OPERATOR_NAME", null);
			operatorName = operatorName.trim();
			grbtDataFolderStr = "/var/RBT_SYSTEM_LOGS/GRBT/"+operatorName+"_"+fileType+"/";
			File grbtDataFolder = new File(grbtDataFolderStr);
			if(!grbtDataFolder.exists())
				grbtDataFolder.mkdirs();
			
			try {
				grbt_logger_type = ResourceBundle.getBundle("rbt").getString("GRBT_LOGGER_TYPE");
			}
			catch(Exception e) {
				
			}
		}
		catch(Exception e)
		{
			logger.error("Error initializing CopyDataCollector", e);
		}
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try
			{
				String hostname = InetAddress.getLocalHost().getHostName();
				if(grbt_logger_type != null) {
					hostname = grbt_logger_type;
				}
				Calendar currentCal = Calendar.getInstance();
				currentCal.add(Calendar.HOUR, -1);
				Date lastHoursDate = currentCal.getTime();
				String copyTransFormattedDate = sdf.format(lastHoursDate);
				String copyGrbtFormattedDate = sdfGrbt.format(lastHoursDate);
				currentCal.set(Calendar.MINUTE, 0);
				Date copyStartTime = currentCal.getTime();
				currentCal.add(Calendar.HOUR, 1);
				Date copyEndTime = currentCal.getTime();
				String copyTransFileName = gathererPath + "COPY_TRANS_" + copyTransFormattedDate + ".csv";
				String copyGrbtFileName = grbtDataFolderStr + operatorName + "_" + fileType + "_" + hostname + "_" + copyGrbtFormattedDate + ".csv";
				String tempGrbtFileName = grbtDataFolderStr + operatorName + "_" + fileType + ".csv";
				String copyUploadedGrbtFileName = grbtDataFolderStr + operatorName + "_" + fileType + "_" + hostname + "_" + copyGrbtFormattedDate + ".csv.done";
				File copyTransFile = new File(copyTransFileName);
				File copyGrbtFile = new File(copyGrbtFileName);
				File tempFile = new File(tempGrbtFileName);
				File copyUploadedGrbtFile = new File(copyUploadedGrbtFileName);
				
				if(!copyTransFile.exists() || copyGrbtFile.exists() || copyUploadedGrbtFile.exists())
				{
					logger.info(copyTransFileName + " not found or " + copyGrbtFileName +" already exists, continuig..");
					continue;
				}
				String lineRead = null;
				LineNumberReader lnr = new LineNumberReader(new FileReader(copyTransFile));
				HashSet<String> copySet = new HashSet<String>();
				while((lineRead = lnr.readLine()) != null)
				{
					logger.info("Got Line as " + lineRead + " from " + copyTransFile.getName());
					try
					{
//						StringTokenizer stk = new StringTokenizer(lineRead,",");
						
						String[] stkArr = lineRead.split("\\,");
						
						if(stkArr.length < 6)
							continue;
						logger.info("Token count is fine");
						String called = stkArr[0];
						String caller = stkArr[1];
						String song = stkArr[2];
						String category = stkArr[3];
						String copyTimeStr = stkArr[4];
						String copierType = stkArr[5];
						
						logger.info("CopierType: " + copierType + " !copierType.equals(\"INCIRCLE\"):" + !copierType.equals("INCIRCLE"));
						logger.info("song: " + song + " song.indexOf(\"MISSING\"):" + song.indexOf("MISSING"));
						logger.info("song: " + song.toLowerCase() + " song.toLowerCase().indexOf(\"default\"):" + song.toLowerCase().indexOf("default"));
						
						if(!copierType.equals("INCIRCLE") || song.indexOf("MISSING") != -1 || song.toLowerCase().indexOf("default") != -1)
							continue;
						Date copyTime = sdfCopyLine.parse(copyTimeStr);
						logger.info("converted copytime is " + copyTime);
						if(copyTime.before(copyStartTime)){
							logger.info(copyTime + " is before " + copyStartTime);
							continue;
						}
						if(copyTime.after(copyEndTime)){
							logger.info(copyTime + " is after " + copyEndTime);
							break;
						}
						StringBuilder strBuilder = new StringBuilder();
						strBuilder.append(called);strBuilder.append(",");
						strBuilder.append(caller);strBuilder.append(",");
						strBuilder.append(song);strBuilder.append(",");
						strBuilder.append(copyTimeStr);
						String copyLine = strBuilder.toString();
						logger.info("copyLine is " + copyLine);
						if(!copySet.contains(copyLine))
							copySet.add(copyLine);
					}
					catch(Exception e)
					{
						logger.error("Error parsing copy trans line "+lineRead, e);
					}
				}
				lnr.close();
				BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));
				Iterator<String> copySetIterator = copySet.iterator();
				logger.info("Iterating on copySet");
				while(copySetIterator.hasNext()){
					writer.append(copySetIterator.next());
					writer.newLine();
					writer.flush();
				}
				writer.close();
				
				logger.info("Temp File rename status " + tempFile.renameTo(copyGrbtFile));
			}
			catch(Exception e)
			{
				logger.error("Error in infinite loop", e);
			}
			finally
			{
				sleepNow();
			}
		}	
	}
	
	private static void sleepNow()
	{
		try
		{
			logger.info("sleeping for 5 mins");
			Thread.sleep(sleepTime);
			logger.info("waking up");
		}
		catch(Exception e)
		{
			logger.error("Got error " , e);
		}
	}
}
