package com.onmobile.apps.ringbacktones.daemons.tcp.requests;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

/**
 * @author sridhar.sindiri
 * 
 */
public class ViralPromotionOptOutRequest implements Runnable {

	private static Logger logger = Logger
			.getLogger(ViralPromotionOptOutRequest.class);

	private String callerID = null;
	private String calledID = null;
	private long calledTime;
	private String clipID = null;
	private String extraInfo = null;
	private long smsID;

	/**
	 * @return the callerID
	 */
	public String getCallerID() {
		return callerID;
	}

	/**
	 * @param callerID
	 *            the callerID to set
	 */
	public void setCallerID(String callerID) {
		this.callerID = callerID;
	}

	/**
	 * @return the calledID
	 */
	public String getCalledID() {
		return calledID;
	}

	/**
	 * @param calledID
	 *            the calledID to set
	 */
	public void setCalledID(String calledID) {
		this.calledID = calledID;
	}

	/**
	 * @return the calledTime
	 */
	public long getCalledTime() {
		return calledTime;
	}

	/**
	 * @param calledTime
	 *            the calledTime to set
	 */
	public void setCalledTime(long calledTime) {
		this.calledTime = calledTime;
	}

	/**
	 * @return the rbtWavFile
	 */
	public String getClipID() {
		return clipID;
	}

	/**
	 * @param rbtWavFile
	 *            the rbtWavFile to set
	 */
	public void setClipID(String clipID) {
		this.clipID = clipID;
	}

	/**
	 * @return
	 */
	public String getExtraInfo() {
		return extraInfo;
	}

	/**
	 * @param extraInfo
	 */
	public void setExtraInfo(String extraInfo) {
		this.extraInfo = extraInfo;
	}

	/**
	 * @return
	 */
	public long getSmsID() {
		return smsID;
	}

	/**
	 * @param smsID
	 */
	public void setSmsID(long smsID) {
		this.smsID = smsID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		if (logger.isInfoEnabled())
			logger.info("Processing : " + this);

		try {
			if (callerID == null || calledID == null || clipID == null)
			{
				logger.info("Invalid parameters for the request");
				expireViralData(smsID);

				writeTrans("INVALID_VIRAL_RECORD");
				return;
			}

			DataRequest dataRequest = new DataRequest(callerID, calledID, "VIRAL_OPTOUT");
			ViralData[] viralData = RBTClient.getInstance().getViralData(dataRequest);
			if (viralData == null || viralData.length != 1)
			{
				expireViralData(smsID);

				writeTrans("OLD_VIRAL_RECORD");
				return;
			}

			SelectionRequest selRequest = new SelectionRequest(callerID, clipID);
			selRequest.setCategoryID("3");

			/*
			 * Below parameter consists of
			 * <ViralConfirmationKeyword>,<ActivatedBy>,<SelectedBy>,<SubscriptionClass>,<CategoryID>.
			 * 
			 * Only configuring <ViralConfirmationKeyword> is mandatory, rest are optional.
			 */
			String[] tokens = RBTParametersUtils.getParamAsString(
					iRBTConstant.SMS, Constants.VIRAL_KEYWORD, "").split(",");
			if (tokens.length > 1)
				selRequest.setActivationMode(tokens[1].trim());
			if (tokens.length > 2)
				selRequest.setMode(tokens[2].trim());
			if (tokens.length > 3)
				selRequest.setSubscriptionClass(tokens[3].trim());
			if (tokens.length > 4)
				selRequest.setCategoryID(tokens[4].trim());
			if (tokens.length > 5)
			{
				selRequest.setChargeClass(tokens[5].trim());
				selRequest.setUseUIChargeClass(true);
			}

			String actInfo = "called:" + calledID + "|";
			selRequest.setModeInfo(actInfo);
			
			
			//Getting offer from prism
			boolean allowGetOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter(iRBTConstant.COMMON, iRBTConstant.ALLOW_GET_OFFER, "FALSE").equalsIgnoreCase("TRUE");
			boolean allowOnlyBaseOffer = RBTConnector.getInstance().getRbtGenericCache().getParameter(iRBTConstant.COMMON, iRBTConstant.ALLOW_ONLY_BASE_OFFER, "FALSE").equalsIgnoreCase("TRUE");
			Subscriber subscriber = RBTClient.getInstance().getSubscriber(new RbtDetailsRequest(callerID));
			if(!isUserActive(subscriber.getStatus()) && (allowGetOffer || allowOnlyBaseOffer)) {
				Offer offer = getBaseOffer(callerID);
				if(offer != null) {
					selRequest.setSubscriptionClass(offer.getSrvKey());
					selRequest.setSubscriptionOfferID(offer.getOfferID());
				}
			}
			
			if(allowGetOffer) {
				Offer offer = getSelOffer(callerID, clipID);
				selRequest.setChargeClass(offer.getSrvKey());
				selRequest.setOfferID(offer.getOfferID());
				selRequest.setUseUIChargeClass(true);
			}
			
			HashMap<String, String> contextInfoMap = viralData[0].getInfoMap();
			if (contextInfoMap != null) {
				String inLoop = contextInfoMap.get("inLoop");
				if (inLoop != null && inLoop.equalsIgnoreCase("true")) {
					selRequest.setInLoop(true);
				}
			}

			/*
			 * Adds a selection if the user is already active.
			 * Activates and adds a selection if new-user.
			 */
			RBTClient.getInstance().addSubscriberSelection(selRequest);
			String response = selRequest.getResponse();

			/*
			 * writes trans logs for the request with the response from webservice.
			 */
			writeTrans(response);

			/*
			 * Updates the corresponding viral entry to VIRAL_EXPIRED.
			 */
			expireViralData(smsID);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param smsID
	 * @return
	 */
	private boolean expireViralData(long smsID)
	{
		DataRequest dataRequest = new DataRequest(null, "VIRAL_EXPIRED");
		dataRequest.setSmsID(smsID);
		RBTClient.getInstance().updateViralData(dataRequest);

		String response = dataRequest.getResponse();
		if (logger.isDebugEnabled())
			logger.debug("Response while updating the viral data : " + response);

		return response.equalsIgnoreCase("SUCCESS");
	}

	/**
	 * @param response
	 */
	private void writeTrans(String response)
	{
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(callerID).append(", ")
				.append(calledID).append(", ").append(clipID)
				.append(", ").append(calledTime).append(" - ")
				.append(response);
		RBTEventLogger.logEvent(RBTEventLogger.Event.VIRAL_OPTOUT,
				logBuilder.toString());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ViralPromotionRequest [calledID=");
		builder.append(calledID);
		builder.append(", calledTime=");
		builder.append(calledTime);
		builder.append(", callerID=");
		builder.append(callerID);
		builder.append(", clipID=");
		builder.append(clipID);
		builder.append(", extraInfo=");
		builder.append(extraInfo);
		builder.append(", smsID=");
		builder.append(smsID);
		builder.append("]");
		return builder.toString();
	}
	
	private static boolean isUserActive(String subscriberStatus)
    {
          if (subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.LOCKED)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.GRACE)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
                return true;

          return false;
    }
	
	private static Offer getBaseOffer(String subscriberID) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		int offerType = Offer.OFFER_TYPE_SUBSCRIPTION;
		rbtDetailsRequest.setMode("RBT");
		rbtDetailsRequest.setType(offerType+"");
		Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
		if(offers != null && offers.length > 0 && offers[0].getOfferID() != null)
		{
			String offerId = offers[0].getOfferID();
			if(offers[0].getSrvKey() != null)
				return offers[0];
		}
		return null;
	}
	
	private static Offer getSelOffer(String subscriberID, String clipId) {
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
		int offerType = Offer.OFFER_TYPE_SELECTION;
		rbtDetailsRequest.setMode("RBT");
		rbtDetailsRequest.setType(offerType+"");
		HashMap<String, String> extraInfoMap = new HashMap<String, String>();
		extraInfoMap.put(Offer.CLIP_ID, clipId);
		rbtDetailsRequest.setExtraInfoMap(extraInfoMap);
		Offer[] offers = RBTClient.getInstance().getOffers(rbtDetailsRequest);
		if(offers != null && offers.length > 0 && offers[0].getOfferID() != null)
		{
			String offerId = offers[0].getOfferID();
			if(offers[0].getSrvKey() != null)
				return offers[0];
		}
		return null;
	}
}
