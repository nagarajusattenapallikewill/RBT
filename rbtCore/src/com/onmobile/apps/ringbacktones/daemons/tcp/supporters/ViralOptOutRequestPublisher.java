package com.onmobile.apps.ringbacktones.daemons.tcp.supporters;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.executor.QueuePublisher;
import com.onmobile.apps.ringbacktones.daemons.tcp.requests.ViralPromotionOptOutRequest;

/**
 * @author sridhar.sindiri
 * 
 */
public class ViralOptOutRequestPublisher extends AbstractQueuePublisher {

	private static Logger logger = Logger
			.getLogger(ViralOptOutRequestPublisher.class);

	/**
	 * @param publishInterval
	 * @param timeUnit
	 */
	public ViralOptOutRequestPublisher(long publishInterval, TimeUnit timeUnit) {
		super(publishInterval, timeUnit, 1.0f, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.reliance.QueuePublisher#publish
	 * ()
	 */
	@Override
	protected void publish() {
		int fetchSize = getFetchSize();
		int validityMins = RBTParametersUtils.getParamAsInt("DAEMON",
				"VIRAL_OPTOUT_VALIDITY_MINS", 30);
		ViralSMSTable[] viralDatas = RBTDBManager.getInstance()
				.getViralSMSByTypeAndLimitAndTime("VIRAL_OPTOUT", validityMins,
						fetchSize);

		if (viralDatas != null) {
			for (ViralSMSTable viralData : viralDatas) {
				ViralPromotionOptOutRequest promotionRequest = buildViralPromotionOptOutRequest(viralData);
				executor.execute(promotionRequest);
			}

			logger.debug("Published Records: " + viralDatas.length);
		}
	}

	private ViralPromotionOptOutRequest buildViralPromotionOptOutRequest(
			ViralSMSTable viralData) {
		ViralPromotionOptOutRequest promotionRequest = new ViralPromotionOptOutRequest();

		promotionRequest.setCallerID(viralData.subID());
		promotionRequest.setCalledID(viralData.callerID());
		promotionRequest.setCalledTime(viralData.sentTime().getTime());
		promotionRequest.setExtraInfo(viralData.extraInfo());
		promotionRequest.setClipID(viralData.clipID());
		promotionRequest.setSmsID(viralData.getSmsId());

		return promotionRequest;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.onmobile.apps.ringbacktones.daemons.executor.AbstractQueuePublisher
	 * #clone()
	 */
	@Override
	protected QueuePublisher clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}
