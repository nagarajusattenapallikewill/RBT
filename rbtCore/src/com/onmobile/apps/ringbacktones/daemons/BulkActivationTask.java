/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.BulkActivation;
import com.onmobile.apps.ringbacktones.content.BulkPromo;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.subscriptionsImpl.RBTSubUnsub;

/**
 * @author vinayasimha.patil
 *
 */
public class BulkActivationTask 
{
	private static Logger logger = Logger.getLogger(BulkActivationTask.class);
	
	private static RBTDBManager rbtDBManager = null;
	
	private String pathDir = null;
	private static String fileName = null;
	private static int m_nConn=4;
	private String bulkPromoID = null;
	private boolean prepaidYes = false;
	private String promoID = null;
	private int categoryType;
	private boolean isClipWavFile = true;
	
	private BufferedReader br = null;
	
	private int total = 0, success = 0, failure = 0;
	
	public BulkActivationTask()
	{
		rbtDBManager = RBTDBManager.getInstance();
		
		Tools.init("BulkActivation_Schedule", true);
	}
	
	public void initializeActivation() throws FileNotFoundException
	{
		pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);		
		
		BulkActivation bulkActivation;
		File subscriberFile;
		
		do
		{
			bulkActivation = rbtDBManager.getPendingBulkActivation();
			if(bulkActivation == null)
			{
				logger.info("RBT:: There is no pending Bulk Activation");
				System.exit(0);
			}
				
			fileName = bulkActivation.fileName();
			bulkPromoID = bulkActivation.bulkPromoID();
			promoID = bulkActivation.promoID();
			categoryType = bulkActivation.categoryType();
			isClipWavFile = (categoryType == 7) ? true : false;
			
			subscriberFile = new File(pathDir + java.io.File.separator + fileName);
			if(!subscriberFile.exists())
			{
				rbtDBManager.updateBulkActivationStatus(fileName, "FILE MISSING");
			}
		}
		while(!subscriberFile.exists());	
		
		FileReader fr = new FileReader(pathDir + java.io.File.separator + fileName);
		br =new BufferedReader(fr);
		
		rbtDBManager.updateBulkActivationStatus(fileName, "PROGRESS");
		
		logger.info("RBT:: File Name = "+ fileName);
	}
	
	public void bulkActivation() throws IOException
	{
		String subscriberID;
		
		List<SitePrefix> circles = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix(); 
		 
		RBTSubUnsub loginUser = RBTSubUnsub.init();
		long subID;
		BulkPromo bulkPromo = rbtDBManager.getBulkPromo(bulkPromoID);
		CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(bulkPromo.cosID());
		prepaidYes = cos.prepaidYes();
		
		String workingDir = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null) + File.separator +  bulkPromoID + "_act_insert_failure";
		int rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);
		String eventType = "BULK_ACTIVATION";
		String subscriberType = (prepaidYes) ? "PRE_PAID" : "POST_PAID";
		String requestStr= "activation";
		String responseStr = "initial_failure";
		String requestedTimeStamp = null;
		String responseTimeInMillis = "NA";
		String referenceID = bulkPromoID;
		String requestDetail = "insert_into_RBT_SUBSCRIBER";
		String responseDetail = "not_inserted";
		
		while((subscriberID = br.readLine()) != null)
		{
			subscriberID = subscriberID.trim();
			if(!subscriberID.equals(""))
			{
				total++;
				subscriberID = subID(subscriberID);
				try
				{
					subID = Long.parseLong(subscriberID);
					subscriberID = String.valueOf(subID);
					if(subscriberID.length() != 10 || subID <=0)
					{
						failure++;
						responseStr = "not_a_valid_number";
						requestedTimeStamp = (new Date()).toString();
						WriteSDR.addToAccounting(workingDir, rotationSize, eventType, subscriberID, subscriberType, requestStr, responseStr, requestedTimeStamp, responseTimeInMillis, referenceID, requestDetail, responseDetail);
					}
					else if(!cos.getCircleId().equals("ALL") && !(loginUser.getCircleId(circles, subscriberID).equals(cos.getCircleId())))
					{
						failure++;
						responseStr = "does_not_belong_to_circle_"+cos.getCircleId();
						requestedTimeStamp = (new Date()).toString();
						WriteSDR.addToAccounting(workingDir, rotationSize, eventType, subscriberID, subscriberType, requestStr, responseStr, requestedTimeStamp, responseTimeInMillis, referenceID, requestDetail, responseDetail);
					}
					else
					{
						boolean actStatus = loginUser.activateSubscriberByBulkPromo(subscriberID, bulkPromoID, prepaidYes, cos);
						logger.info("RBT:: result for subscriberID = " + subscriberID + " is " +actStatus );
						if(actStatus)
						{
							success++;
							
							boolean selStatus = loginUser.addSelectionsforBulkPromo(subscriberID, promoID, bulkPromoID, isClipWavFile, true);
							logger.info("RBT:: selection result for subscriberID = " + subscriberID +" is "+ selStatus );
						}
						else
							failure++;
					}
				}
				catch(NumberFormatException e)
				{
					failure++;
					responseStr = "not_a_valid_number";
					requestedTimeStamp = (new Date()).toString();
					WriteSDR.addToAccounting(workingDir, rotationSize, eventType, subscriberID, subscriberType, requestStr, responseStr, requestedTimeStamp, responseTimeInMillis, referenceID, requestDetail, responseDetail);
				}
			}
		}
		
		logger.info("RBT:: Final Status: Total = "+ total + " Success = "+ success + " failure = "+ failure);
		br.close();
	}
	
	public String subID(String subscriberID)
	{
		return (rbtDBManager.subID(subscriberID));
	}
	
	public void completeActivation()
	{
		File subscriberFile = new File(pathDir + java.io.File.separator + fileName);
		File completedDir = new File(pathDir + java.io.File.separator + "completed");
		if(!completedDir.exists())
		{
			completedDir.mkdir();
		}
		File activationDir = new File(completedDir, "activation");
		if(!activationDir.exists())
		{
			activationDir.mkdir();
		}
		
		File completedFile = new File(activationDir, fileName);
		subscriberFile.renameTo(completedFile);
		
		rbtDBManager.updateBulkActivationStatus(fileName, "COMPLETED");
		
		logger.info("RBT:: Moved File "+ fileName +" to "+ completedDir);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		
		try 
		{
			BulkActivationTask bulkActivationTask = new BulkActivationTask();
			bulkActivationTask.initializeActivation();
			bulkActivationTask.bulkActivation();
			bulkActivationTask.completeActivation();
		} 
		catch (FileNotFoundException e) 
		{
			logger.error("", e);
			rbtDBManager.updateBulkActivationStatus(fileName, "FILE MISSING");
		} 
		catch (IOException e) 
		{
			logger.error("", e);
			rbtDBManager.updateBulkActivationStatus(fileName, "EXCEPTION");
		}

		System.exit(0);
	}
}
