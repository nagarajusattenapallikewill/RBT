/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.tcp.supporters;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.tcp.MessageType;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.ViralPromotionRequest;

/**
 * @author vinayasimha.patil
 * 
 */
public class ViralPendingRequestPublisher extends AbstractQueuePublisher
{
	private static Logger logger = Logger
			.getLogger(ViralPendingRequestPublisher.class);

	/**
	 * @param publishInterval
	 * @param timeUnit
	 */
	public ViralPendingRequestPublisher(long publishInterval,
			TimeUnit timeUnit)
	{
		super(publishInterval, timeUnit, 0.80f, false);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.reliance.QueuePublisher#publish
	 * ()
	 */
	@Override
	protected void publish()
	{
		int fetchSize = getFetchSize();
		ViralSMSTable[] viralDatas = RBTDBManager.getInstance()
				.getViralSMSByTypeAndLimitAndTime("VIRAL_PENDING",
						ViralPromotion.getDelayTimeInMins(), fetchSize);
		if (viralDatas != null)
		{
			for (ViralSMSTable viralData : viralDatas)
			{
				ViralPromotionRequest promotionRequest = buildViralPromotionRequest(viralData);
				executor.execute(promotionRequest);
			}

			logger.debug("Published Records: " + viralDatas.length);

			for (ViralSMSTable viralData : viralDatas)
			{
				RBTDBManager.getInstance().deleteViralPromotionBySMSID(
						viralData.getSmsId(), viralData.type());
			}

			logger.debug("Deleted published records");
		}
	}

	private ViralPromotionRequest buildViralPromotionRequest(
			ViralSMSTable viralData)
	{
		ViralPromotionRequest promotionRequest = new ViralPromotionRequest(
				executor, null, MessageType.VIRAL_PROMOTION);

		promotionRequest.setCallerID(viralData.subID());
		promotionRequest.setCalledID(viralData.callerID());
		promotionRequest.setCalledTime(viralData.sentTime().getTime());
		promotionRequest.setCallDuration((short) viralData.count());
		promotionRequest.setRbtWavFile(viralData.clipID());
		promotionRequest.setCircleID(viralData.getCircleId());

		// If validation is already happened when RbtTCPServerHandlerExecutor
		// received the from the player then information will be stored in the
		// extra info as VALIDATED="TRUE"
		Map<String, String> extraInfoMap = DBUtility
				.getAttributeMapFromXML(viralData.extraInfo());
		if (extraInfoMap != null)
		{
			String validated = extraInfoMap.get("VALIDATED");
			boolean validationRequired = (validated == null)
					|| validated.equals("FALSE");

			promotionRequest.setValidationRequired(validationRequired);
			promotionRequest.setCallerLanguage(extraInfoMap.get("CALLER_LANG"));
		}

		return promotionRequest;
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher#clone()
	 */
	@Override
	protected QueuePublisher clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}
}
