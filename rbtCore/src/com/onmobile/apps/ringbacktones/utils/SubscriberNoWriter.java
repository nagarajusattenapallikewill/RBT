/*
 * Created on Mar 22, 2007
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.onmobile.apps.ringbacktones.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.BulkPromo;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.subscriptionsImpl.RBTSubUnsub;
import com.onmobile.apps.ringbacktones.timer.BatchUpdateActivity;

/**
 * @author manoj.jaiswal
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class SubscriberNoWriter {

	private static Logger logger = Logger.getLogger(SubscriberNoWriter.class);
    protected BatchUpdateActivity updateActivity;
    protected long mobileNumber;
    private long insertCount = 0;
    private long batchSize;
    private String activatedBy;
    private boolean prePaidYes;
    private String subscriberFile;
    private String wavFile;
    private boolean isClipWavFile;
    private boolean isWavFileGiven;
    private boolean firstSel;

    /**
     * @param updateActivity
     */
    public SubscriberNoWriter(BatchUpdateActivity updateActivity) {
        this.updateActivity = updateActivity;
        this.batchSize = updateActivity.getBatchSize();
        this.activatedBy = updateActivity.getActivatedBy();
        this.prePaidYes = updateActivity.isPrePaidYes();
        this.subscriberFile = updateActivity.getSubscriberFile();
        this.wavFile = updateActivity.getWavFile();
        this.isClipWavFile = updateActivity.isClipWavFile();
        this.isWavFileGiven = updateActivity.isWavFileGiven();
        this.firstSel = updateActivity.isFirstSel();
    }

    public void export() throws Exception {
        insert();
    }

    protected void insert() throws Exception {
        logger.info("Inserting ");
        long duration = this.updateActivity.getEndTime().getTime()
                - this.updateActivity.getStartTime().getTime();
        long subscriberSize = getNoOfSubscribers();
        long slabSize = subscriberSize / this.batchSize;
        long slabTime =0;
        if(slabSize!=0){
        slabTime = duration / slabSize;
        }
        long nextStartTime = this.updateActivity.getStartTime().getTime()
                + slabTime;
        logger.info("duration :: "+duration+" ms");
        logger.info("subscriberSize :: "+subscriberSize);
        logger.info("slabSize :: "+slabSize);
        logger.info("slabTime :: "+slabTime+" ms");
        logger.info("Next Batch Update at :: "+new Date(nextStartTime));

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		
		List<SitePrefix> circles = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
        
        BulkPromo bulkPromo = rbtDBManager.getBulkPromo(this.activatedBy);
        CosDetails cos = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(bulkPromo.cosID());
        this.prePaidYes = cos.prepaidYes();
        
        String workingDir = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null) + File.separator +  this.activatedBy + "_act_insert_failure";
		int rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);
		String eventType = "BULK_ACTIVATION";
		String subscriberType = (this.prePaidYes) ? "PRE_PAID" : "POST_PAID";
		String requestStr= "activation";
		String responseStr = "initial_failure";
		String requestedTimeStamp = null;
		String responseTimeInMillis = "NA";
		String referenceID = this.activatedBy;
		String requestDetail = "insert_into_RBT_SUBSCRIBER";
		String responseDetail = "not_inserted";
        
		long subID;
		
        RBTSubUnsub subUnsub = RBTSubUnsub.init();
        FileReader fileReader = new FileReader(getFileName());
        BufferedReader reader = new BufferedReader(fileReader);
        String nextLine;
        String subscriberID;
        logger.info("Trying to read the file for inserting into DB");
        while ((nextLine = reader.readLine()) != null)
        {
            logger.info("Subscriber No :: "+nextLine);
            subscriberID = nextLine.trim();
            if(!subscriberID.equals(""))
			{
            	subscriberID = subID(subscriberID);
            	try
				{
            		subID = Long.parseLong(subscriberID);
					subscriberID = String.valueOf(subID);
					if(subscriberID.length() != 10 || subID <=0)
					{
						responseStr = "not_a_valid_number";
						requestedTimeStamp = (new Date()).toString();
						WriteSDR.addToAccounting(workingDir, rotationSize, eventType, subscriberID, subscriberType, requestStr, responseStr, requestedTimeStamp, responseTimeInMillis, referenceID, requestDetail, responseDetail);
					}
					else if(!cos.getCircleId().equals("ALL") && !(subUnsub.getCircleId(circles, subscriberID).equals(cos.getCircleId())))
					{
						responseStr = "does_not_belong_to_circle_"+cos.getCircleId();
						requestedTimeStamp = (new Date()).toString();
						WriteSDR.addToAccounting(workingDir, rotationSize, eventType, subscriberID, subscriberType, requestStr, responseStr, requestedTimeStamp, responseTimeInMillis, referenceID, requestDetail, responseDetail);
					}
					else
					{
						logger.info("Calling activateSubscriberByBulkPromo()");
						boolean actStatus = subUnsub.activateSubscriberByBulkPromo(subscriberID, this.updateActivity.getActivatedBy(), this.prePaidYes, cos);
						
						logger.info("actStatus :: " +actStatus);
						logger.info("isWavFileGiven :: " +this.isWavFileGiven);
			            if(actStatus && this.isWavFileGiven){
			                logger.info("Calling addSelectionsforBulkPromo()");
			                subUnsub.addSelectionsforBulkPromo(subscriberID, this.wavFile,
			                        this.activatedBy, this.isClipWavFile, this.firstSel);
			            }
					}
				}
            	catch(NumberFormatException e)
				{
					responseStr = "not_a_valid_number";
					requestedTimeStamp = (new Date()).toString();
					WriteSDR.addToAccounting(workingDir, rotationSize, eventType, subscriberID, subscriberType, requestStr, responseStr, requestedTimeStamp, responseTimeInMillis, referenceID, requestDetail, responseDetail);
				}
			}
            
            insertCount++;
            logger.info("Inserted "+insertCount+ " records in DB in this batch");
            if (insertCount == batchSize) {
                logger.info("Insert Count "+insertCount+ " = "+batchSize+", Batch Size in this batch");
                if (System.currentTimeMillis() < nextStartTime) {
                    long sleepPeriod= nextStartTime - System.currentTimeMillis();
                    logger.info("Sleeping for :: "+ sleepPeriod + " ms");
                    Thread.sleep(sleepPeriod);
                }
                nextStartTime = System.currentTimeMillis() + slabTime;
                logger.info("Next Batch Update at :: "+new Date(nextStartTime));
                insertCount = 0;
            }
        } 
        logger.info("Update Over");
        reader.close();
    }
    
    public String subID(String subscriberID)
	{
        return (RBTDBManager.getInstance().subID(subscriberID));
	}

    /**
     * @return
     */
    private long getNoOfSubscribers() {
        long countRec = 0;
        try {
            logger.info("Trying to read the file for the subscriber size");
            FileReader fileReader = new FileReader(getFileName());
            BufferedReader reader = new BufferedReader(fileReader);
            while ((reader.readLine()) != null) {
                countRec++; 
            }
            fileReader.close();
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("No of Lines :: "+countRec);
        return countRec;
    }

    private String getFileName() {
        String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
        String fileName = this.subscriberFile;
        logger.info("File Name :: " + pathDir + java.io.File.separator + fileName);
        return pathDir + java.io.File.separator + fileName;
    }

}