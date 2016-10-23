package com.onmobile.apps.ringbacktones.daemons.nametunes;

import java.sql.Timestamp;
import java.util.Date;

import org.apache.log4j.Logger;




import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RbtNameTuneTracking.Status;

public class NameTuneRetryThread extends Thread {
	private static Logger logger = Logger.getLogger(NameTuneRetryThread.class);
	RbtNameTuneTracking nmtObject = null;
	private Integer retryTime = Integer.parseInt(NameTuneConstants.MAX_RETRY_TIME);
	private Integer maxRetryCount = Integer.parseInt(NameTuneConstants.MAX_RETRY_COUNT);
	private String webServiceResponse = null;
	public NameTuneRetryThread(RbtNameTuneTracking retryObject){
		this.nmtObject = retryObject;
	}
	public void run(){
		Thread.currentThread().setName("NAME_TUNE_RETRY_THREAD_TR_ID_"+nmtObject.getTransactionId());
		while (true) {
			try {
				if (maxRetryCount <= nmtObject.getRetryCount()) {
					nmtObject.setStatus(Status.FAILURE.name());
					nmtObject.setRetryCount(nmtObject.getRetryCount());
					break;
				}
				long timeToSleep = nmtObject.getRetryCount() * retryTime * 60 * 1000;
				logger.debug(Thread.currentThread().getName()+ "IS SLEEPING FOR ..." + timeToSleep / 60000 + " MINUTES ");
				Thread.sleep(timeToSleep);
				if (nmtObject != null) {
					webServiceResponse = NameTuneDBUtils.getInstance().callProcessSelectionRequests(nmtObject);
				}
				if (webServiceResponse != null) {
					if (webServiceResponse.indexOf("success") != -1) {
						nmtObject.setStatus(Status.COMPLETED.name());
						nmtObject.setModifiedDate(new Timestamp(System.currentTimeMillis()));
						break;
					} else if (webServiceResponse.indexOf("clip_not_exists") != -1) {
						nmtObject.setRetryCount(nmtObject.getRetryCount() + 1);
						nmtObject.setModifiedDate(new Timestamp(System.currentTimeMillis()));
						NameTuneDBUtils.updateNameTuneTrackingObject(nmtObject);
					} else {
						nmtObject.setStatus(Status.FAILURE.name());
						nmtObject.setModifiedDate(new Timestamp(System.currentTimeMillis()));
						break;
					}
				} else {
					nmtObject.setStatus(Status.FAILURE.name());
					nmtObject.setModifiedDate(new Timestamp(System.currentTimeMillis()));
					break;
				}

			} catch (InterruptedException e) {
				logger.error("EXECPTION"+ e +"IN "+ Thread.currentThread().getName());
				e.printStackTrace();
			} catch (Exception e) {
				logger.error("EXECPTION"+ e +"IN "+ Thread.currentThread().getName());
				e.printStackTrace();
			}
		}
		NameTuneDBUtils.updateNameTuneTrackingObject(nmtObject);
	}
	
	
}
