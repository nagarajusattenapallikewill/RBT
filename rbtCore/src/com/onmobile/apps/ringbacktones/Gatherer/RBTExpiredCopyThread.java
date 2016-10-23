package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.Date;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.provisioning.common.SmsKeywordsStore;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.wrappers.RbtGenericCacheWrapper;
import com.onmobile.apps.ringbacktones.wrappers.SubscriberRbtClientWrapper;

public class RBTExpiredCopyThread extends Thread {
	
	private static final String _class = "RBTFailedCopyThread";

	private static Logger logger = Logger.getLogger(RBTExpiredCopyThread.class);

	private RbtGenericCacheWrapper rbtGenericCacheWrapper = null;
	private SubscriberRbtClientWrapper subRbtClient = null;
	private RBTGatherer m_parentGathererThread = null;
	private int sleepTimeInMin = 5;
	long m_nextConfPendingUploadTime = -1;
	//RBT-14671 - # like
	private RBTCopyLikeUtils m_rbtCopyLikeUtils = new RBTCopyLikeUtils();
	
	public RBTExpiredCopyThread(RBTGatherer m_gathererThread) {
		logger.info("initting...");
		m_parentGathererThread = m_gathererThread;
	}

	public void initialize() {
		rbtGenericCacheWrapper = RbtGenericCacheWrapper.getInstance();
		subRbtClient = SubscriberRbtClientWrapper.getInstance();
		sleepTimeInMin = m_parentGathererThread.getParamAsInt("GATHERER", "GATHERER_SLEEP_INTERVAL", 5);
	}

	public void run() {
		while (m_parentGathererThread.isAlive())
		{
			try
			{
				removeExpiredCopyEntries();
				try {
					Date next_run_time = m_parentGathererThread.roundToNearestInterVal(sleepTimeInMin);
					long sleeptime = m_parentGathererThread.getSleepTime(next_run_time);
					if (sleeptime < 1000) {
						sleeptime = 5000;
					}
					sleeptime = (3 * sleeptime);
					logger.info(_class + " Thread : sleeping for " + sleeptime + " mSecs.");
					Thread.sleep(sleeptime);
					logger.info(_class + " Thread : waking up.");
				} catch (InterruptedException ie) {
					logger.error("", ie);
					break;
				}
			}catch (Exception e) {
					e.printStackTrace();
					logger.info("got IOException " + e.getMessage());
					logger.info("Again Initializing parameters for ftpclient");
					initialize();
				}
			 catch (Throwable t) {
					logger.info("got throwable " + t);
				}
		}
	}

	private void removeExpiredCopyEntries()
	{
		ViralSMSTable[] context = null;
		if(m_parentGathererThread.getParamAsBoolean("UPLOAD_PENDING_COPY_REQUESTS", "FALSE"))
		{
			if(m_nextConfPendingUploadTime == -1 || System.currentTimeMillis() >= m_nextConfPendingUploadTime)
			{	
				int waitTime = m_parentGathererThread.getParamAsInt("WAIT_TIME_UPLOAD",0);
				if(waitTime == 0)
					waitTime = m_parentGathererThread.getParamAsInt("WAIT_TIME_DOUBLE_CONFIRMATION",30);
				context = m_parentGathererThread.rbtCopyProcessor.rbtDBManager.getViralSMSByTypeAndLimitAndTime(m_parentGathererThread.rbtCopyProcessor.COPYCONFPENDING, waitTime, m_parentGathererThread.getParamAsInt("COPY_PROCESSING_COUNT", 5000));
				//				TODO needs to check the expire copy request RBT-319
				//				if expire copy request count > 0 invoke the prepareAndSendXml
				if(context != null && context.length > 0 && m_parentGathererThread.rbtCopyProcessor.prepareAndSendXml(context))
				{
					for (ViralSMSTable vst : context)
					{
						String keyPressed = "NA";
						String copyType="-";
						String confMode="-";
						String extraInfoStr = vst.extraInfo();
						HashMap<String, String> viralInfoMap = DBUtility.getAttributeMapFromXML(extraInfoStr);
						if(vst.type().equalsIgnoreCase(m_parentGathererThread.rbtCopyProcessor.COPY) || vst.type().equalsIgnoreCase(m_parentGathererThread.rbtCopyProcessor.COPYCONFIRM))
							{
							copyType=iRBTConstant.DIRECTCOPY;
							keyPressed = "s9";
							}
						else if(vst.type().equalsIgnoreCase(m_parentGathererThread.rbtCopyProcessor.COPYCONFIRMED))
							{
							copyType=iRBTConstant.OPTINCOPY;
							keyPressed = "s";
							}
						String sourceClipName = "";
						if (viralInfoMap != null && viralInfoMap.containsKey(iRBTConstant.KEYPRESSED_ATTR))
							keyPressed = viralInfoMap.get(iRBTConstant.KEYPRESSED_ATTR);
						if(m_parentGathererThread.getParamAsBoolean("WRITE_TRANS","FALSE"))
						{//RBT-14671 - # like
							Subscriber subscriber = m_rbtCopyLikeUtils.getSubscriber(vst.callerID());
							String wasActive = "YES";
							if(m_parentGathererThread.rbtCopyProcessor.isSubActive(subscriber))
								wasActive = "NO";
							m_parentGathererThread.rbtCopyProcessor.removeViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), m_parentGathererThread.rbtCopyProcessor.COPYCONFPENDING);
							try
							{
								if(SmsKeywordsStore.likeKeywordsSet.contains(keyPressed)) {//RBT-14671 - # like
									// Character l will be prepended to the RBT Like request to differentiate it.
									m_parentGathererThread.rbtCopyProcessor.eventLogger.copyTrans(vst.subID(), vst.callerID(), wasActive, m_parentGathererThread.rbtCopyProcessor.m_localType, "-", "-", vst.sentTime(), iRBTConstant.OPTINCOPY, "l".concat(keyPressed), 
											m_parentGathererThread.rbtCopyProcessor.COPYEXPIRED, vst.clipID(),confMode,m_parentGathererThread.rbtCopyProcessor.getCalleeOperator(m_rbtCopyLikeUtils.getSubscriber(vst.subID()), vst.selectedBy()),new Date());
									RBTCopyProcessor.writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-", Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), m_parentGathererThread.rbtCopyProcessor.m_localType,wasActive, "-",m_parentGathererThread.rbtCopyProcessor.COPYEXPIRED,"l".concat(keyPressed),iRBTConstant.OPTINCOPY, "-");

								} else {
									RBTCopyProcessor.writeTrans(vst.subID(), vst.callerID(), vst.clipID(), "-", Tools.getFormattedDate( vst.sentTime(), "yyyy-MM-dd HH:mm:ss"), m_parentGathererThread.rbtCopyProcessor.m_localType,wasActive, "-",m_parentGathererThread.rbtCopyProcessor.COPYEXPIRED,keyPressed,iRBTConstant.OPTINCOPY, "-");
									m_parentGathererThread.rbtCopyProcessor.eventLogger.copyTrans(vst.subID(), vst.callerID(), wasActive, m_parentGathererThread.rbtCopyProcessor.m_localType, "-", "-", vst.sentTime(), iRBTConstant.OPTINCOPY, keyPressed, 
											m_parentGathererThread.rbtCopyProcessor.COPYEXPIRED, vst.clipID(),confMode,m_parentGathererThread.rbtCopyProcessor.getCalleeOperator(m_rbtCopyLikeUtils.getSubscriber(vst.subID()), vst.selectedBy()),new Date());//RBT-14671 - # like
								}
							}
							catch(Exception e)
							{
								logger.error("Exception while eventlogging", e);
							}
						}
						else
							m_parentGathererThread.rbtCopyProcessor.updateViralPromotion(vst.subID(), vst.callerID(), vst.sentTime(), m_parentGathererThread.rbtCopyProcessor.COPYCONFPENDING, m_parentGathererThread.rbtCopyProcessor.COPYEXPIRED, null);
					}
				}
				m_nextConfPendingUploadTime = System.currentTimeMillis() + m_parentGathererThread.getParamAsInt("UPLOAD_PENDING_COPY_INTERVAL", 30) *60*1000;

			}
		}
		else
		{	
			context = m_parentGathererThread.rbtCopyProcessor.rbtDBManager.getViralSMSByTypeAndLimitAndTime(m_parentGathererThread.rbtCopyProcessor.COPYCONFPENDING, m_parentGathererThread.getParamAsInt("WAIT_TIME_DOUBLE_CONFIRMATION",30), m_parentGathererThread.getParamAsInt("COPY_PROCESSING_COUNT", 5000));
			if (context == null || context.length <= 0)
			{
				logger.info("Context is null or count <= 0");
				return;
			}
			logger.info("Count of Double Confirm copyContext is "+ context.length);
			for(int i = 0; i < context.length; i++)
			{	
				m_parentGathererThread.rbtCopyProcessor.copyExpired(context[i], m_parentGathererThread.rbtCopyProcessor.m_localType);
			}
		}
	}


}
