package com.onmobile.apps.ringbacktones.daemons.genericftp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.StringUtil;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.CampaignRequest;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.ChargeClassMap;
import com.onmobile.apps.ringbacktones.daemons.genericftp.beans.SubClassMap;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author sridhar.sindiri
 *
 */
public class CampaignThread implements Runnable
{
	private static Logger logger = Logger.getLogger(CampaignThread.class);

	private FTPCampaign ftpCampaign;
	private File file;

	/**
	 * @param ftpCampaign
	 */
	public CampaignThread(FTPCampaign ftpCampaign, File file)
	{
		this.ftpCampaign = ftpCampaign;
		this.file = file;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		BufferedReader in = null;
		BufferedWriter bw = null;
		boolean isFileProcessed = false;
		try
		{
			logger.info("Started processing file. ftpCampaign: "+ftpCampaign);
			String delimiter = ftpCampaign.getDelimiter();
			String inputFormat = ftpCampaign.getFormat();
			String[] inputTokens = inputFormat.split(",", -1);
			logger.info("inputFormat: " + inputFormat);
			in = new BufferedReader(new FileReader(this.file));
			File parentDir = new File(this.file.getParent(), "processed");
			if (!parentDir.exists())
				parentDir.mkdirs();

			String className = "com.onmobile.apps.ringbacktones.daemons.genericftp.beans.CampaignRequest";
			
			@SuppressWarnings("unchecked")
			Class<CampaignRequest> campaignClass = (Class<CampaignRequest>) Class.forName(className);
			bw = new BufferedWriter(new FileWriter(new File(parentDir, this.file.getName() + "_done")));
			String str;
			while ((str = in.readLine()) != null)
			{
				
				if (str.trim().length() == 0) {
					logger.info("Not processing line: " + str + ", it is empty");
					continue;
				}

				String[] tokens = str.split(delimiter, -1);
				CampaignRequest campaignRequest = campaignClass.newInstance();
				logger.info("Not processing line: " + str);
				for (int i = 0; i < inputTokens.length; i++)
				{
					if (inputTokens[i].trim().length() == 0)
						continue;

					Method method = campaignClass.getDeclaredMethod("set"
							+ StringUtil.toUpperCaseFirstChar(inputTokens[i].trim()),
							String.class);
					method.invoke(campaignRequest, tokens[i].trim());
				}

				logger.info("Processing campaign request: " + campaignRequest);
				String response = processRecord(campaignRequest);
				logger.info("Processed campaign request, response: " + response
						+ ", campaignRequest: " + campaignRequest);
				bw.write(str + " - " + response);
				bw.newLine();
			}

			if (ftpCampaign.getRetailerID() != null && ftpCampaign.getRetailerSmsText() != null && ftpCampaign.getSenderID() != null)
			{
				String retailerSmsText = ftpCampaign.getRetailerSmsText();
				retailerSmsText = retailerSmsText.replaceAll("%FILE_NAME", file.getName());
				sendSMS(ftpCampaign.getSenderID(), ftpCampaign.getRetailerID(), retailerSmsText);
			}

			isFileProcessed = true;
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			try
			{
				if (in != null)
					in.close();
				if (bw != null)
					bw.close();

				if (isFileProcessed)
					file.delete();
			}
			catch (IOException e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}

	/**
	 * @param campaignRequest
	 */
	private String processRecord(CampaignRequest campaignRequest)
	{
		logger.info("Processing campaignRequest: " + campaignRequest);
		String response = "ERROR";
		String retailerSmsText = null;
		String retailerID = campaignRequest.getRetailerID();
		String senderID = ftpCampaign.getSenderID();
		try
		{
			String subscriberID = campaignRequest.getMsisdn();
			if (subscriberID == null || subscriberID.length() == 0)
			{
				logger.info("Invalid parameter, msisdn missing");
				return "INVALID_PARAMETER";
			}
			String activationMode = campaignRequest.getActivationMode();
			if (activationMode == null || activationMode.length() == 0)
				activationMode = ftpCampaign.getBaseConfig().getActivatedBy();

			String selectionMode = campaignRequest.getSongMode();
			if (selectionMode == null || selectionMode.length() == 0)
				selectionMode = ftpCampaign.getSelConfig().getSelectedBy();

			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberID);

			if (ftpCampaign.getBaseConfig().getAccept()
					.equalsIgnoreCase("active")
					&& (subscriber == null || subscriber.subYes().equals(
							iRBTConstant.STATE_DEACTIVATED)))
			{
				logger.info("Cannot process inactive numbers due to config in the xml");
				return "USER_INACTIVE";
			}

			if (ftpCampaign.getBaseConfig().getAccept()
					.equalsIgnoreCase("inactive")
					&& (subscriber != null && (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
							|| subscriber.subYes().equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
							|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_PENDING)
							|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_GRACE))))
			{
				logger.info("Cannot process active numbers due to config in the xml");
				return "USER_ACTIVE";
			}

			String subClass = campaignRequest.getSubClass();


			String subClassMapIDProcessed = null;
			if (subscriber == null || subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED))
			{
				logger.info("Since subscriber is new or deactivated, activating subscriber: "
						+ subscriberID);
				if (!(ftpCampaign.getSelConfig().getAccept() != null && (ftpCampaign.getSelConfig().getAccept()
						.equalsIgnoreCase("inactive") || ftpCampaign.getSelConfig().getAccept()
						.equalsIgnoreCase("both"))))
				{
					if (subClass == null || subClass.length() == 0)
						subClass = ftpCampaign.getBaseConfig().getSubscriptionClass();

					String cosID = ftpCampaign.getBaseConfig().getCosID();

					SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriberID);
					if (subClass != null)
						subscriptionRequest.setSubscriptionClass(subClass);
					if (activationMode != null)
						subscriptionRequest.setActivationMode(activationMode);
					if (cosID != null)
						subscriptionRequest.setCosID(Integer.parseInt(cosID));
					if (retailerID != null)
						subscriptionRequest.setModeInfo(retailerID);
					logger.info("Activating subscriber. subscriptionRequest: "
							+ subscriptionRequest);
					RBTClient.getInstance().activateSubscriber(subscriptionRequest);
					response = subscriptionRequest.getResponse();
					logger.debug("Activated subscriber. response: " + response);
					if (!response.equalsIgnoreCase("SUCCESS"))
					{
						logger.info("Response is not success, not sending SMS. response: "+response);
						return response;
					}
					retailerSmsText = ftpCampaign.getRetailerSmsTextPerMsisdn();
					retailerSmsText = retailerSmsText.replaceAll("%MSISDN", subscriberID);
					logger.info("Successfully activated subscriber, replaced MSISDN in the retailer sms text. retailerSmsText: "
							+ retailerSmsText);
				}
				// Else it will be handled in the selection block
			}
			else if (subscriber != null && (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
					|| subscriber.subYes().equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
					|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_PENDING)
					|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_GRACE)))
			{
				logger.info("Since subscriber is already exist, upgrading"
						+ " validity." + " subscriberId: " + subscriber.subID()
						+ ", status: "	+ subscriber.subYes());
		
				SubClassMap subClassMap = getBaseActionBasedOnSubClass(subscriber.subscriptionClass(), subscriber.circleID());
				if (subClassMap != null)
				{
					if (subClass == null || subClass.length() == 0)
						subClass = subClassMap.getSubClassMapping().get(subscriber.subscriptionClass());
					if (subClass == null || subClass.length() == 0)
						subClass = subClassMap.getSubClassMapping().get("ALL");

					String baseAction = subClassMap.getAction();
					subClassMapIDProcessed = subClassMap.getId();
					if (baseAction.equals("extendValidity"))
					{
						response = processUpgradeValidity(subscriberID, subClass, activationMode, retailerID);
					}
					else if (baseAction.equals("baseUpgrade"))
					{
						response = processUpgradeSubscription(subscriberID, subClass, activationMode, retailerID);
					}
					logger.info("Successfully upgraded subscriber. subscriberId: "
							+ subscriber.subID() + ", response: "	+ response);
					if (!response.equalsIgnoreCase("SUCCESS"))
					{
						logger.info("Not sending SMS, upgrade is not success. subscriberId: "
								+ subscriber.subID()
								+ ", response: "
								+ response);
						return response;
					}
					retailerSmsText = ftpCampaign.getRetailerSmsTextPerMsisdn();
					retailerSmsText = retailerSmsText.replaceAll("%MSISDN", subscriberID);
					logger.info("Replaced MSISDN in RetailerSmsTextPerMsisdn. retailerSmsText: "
							+ retailerSmsText);
				}
			}
			else
			{
				logger.info("Not processing, invalid subscriber status. subscriber: "
						+ subscriber);
				return "INVALID_SUBSCRIBER_STATUS";
			}

			if (ftpCampaign.getSelConfig().getAccept() != null && (ftpCampaign.getSelConfig().getAccept()
					.equalsIgnoreCase("inactive") || ftpCampaign.getSelConfig().getAccept()
					.equalsIgnoreCase("both")))
			{
				logger.info("In ftpCompaign setSelConfig as inactive or both. ");
				if (subscriber == null || subscriber.subYes().equals(iRBTConstant.STATE_DEACTIVATED))
				{
					logger.info("Processing selection, campaignRequest: "+campaignRequest);
					response = processSelection(campaignRequest);
					logger.info("Processed selection, response: " + response
							+ ", campaignRequest: " + campaignRequest);
					if (!response.equalsIgnoreCase("SUCCESS"))
					{
						logger.info("Response is not success, not sending retailer sms text. response: " + response);
						return response;
					}
					retailerSmsText = ftpCampaign.getRetailerSmsTextPerMsisdn();
					retailerSmsText = retailerSmsText.replaceAll("%MSISDN", subscriberID);
				}
				logger.info("Response success, sending SMS to subscriber. retailerSmsText: " + retailerSmsText);
			}
			else if (ftpCampaign.getSelConfig().getAccept() != null && (ftpCampaign.getSelConfig().getAccept()
					.equalsIgnoreCase("active") || ftpCampaign.getSelConfig().getAccept()
					.equalsIgnoreCase("both")))
			{
				logger.info("In ftpCompaign setSelConfig as active or both. ");
				if (subscriber != null && (subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATED)
						|| subscriber.subYes().equals(iRBTConstant.STATE_TO_BE_ACTIVATED)
						|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_PENDING)
						|| subscriber.subYes().equals(iRBTConstant.STATE_ACTIVATION_GRACE)))
				{
					logger.info("Processing selection, campaignRequest: "+campaignRequest);
					response = processSelection(campaignRequest);
					logger.info("Processed selection, response: " + response
							+ ", campaignRequest: " + campaignRequest);
					if (!response.equalsIgnoreCase("SUCCESS"))
					{
						logger.info("Response is not success, not sending retailer sms text. response: " + response);
						return response;
					}
					retailerSmsText = ftpCampaign.getRetailerSmsTextPerMsisdn();
					retailerSmsText = retailerSmsText.replaceAll("%MSISDN", subscriberID);
				}
				logger.info("Response success, sending SMS to subscriber. retailerSmsText: " + retailerSmsText);
			}

			List<ChargeClassMap> chargeClassMappingList = getChargeClassMapsForSubClassMapID(subClassMapIDProcessed);
			for (ChargeClassMap eachChargeClassMap : chargeClassMappingList)
			{
				if (eachChargeClassMap.getAction().equals("selUpgrade"))
				{
					String mode = campaignRequest.getSongMode();
					if (mode == null)
						mode = ftpCampaign.getSelConfig().getSelectedBy();

					logger.info("Processing upgrade selection pack,"
							+ " subscriberId: " + subscriberID + ", mode: "
							+ mode + ", retailerId: " + retailerID);
					response = upgradeSelectionPack(subscriberID, null, mode, retailerID);
					logger.info("Processed upgrade selection pack,"
							+ " response: " + response + ", subscriberId: "
							+ subscriberID + ", mode: " + mode
							+ ", retailerId: " + retailerID);

					if (!response.equalsIgnoreCase("SUCCESS"))
					{
						logger.info("Response of upgrade selection pack is not success, not sending retailer sms text. response: " + response);
						return response;
					}
					retailerSmsText = ftpCampaign.getRetailerSmsTextPerMsisdn();
					retailerSmsText = retailerSmsText.replaceAll("%MSISDN", subscriberID);
				}
			}
			logger.info("Finally, retailerSmsText: " + retailerSmsText);
		}
		catch (Exception e)
		{
			logger.error(e.getMessage(), e);
		}
		finally
		{
			logger.info("Configured senderID: " + senderID+", reailerID: "+retailerID+", retailerSmsText: "
					+retailerSmsText);
			if (senderID != null && retailerID != null && retailerSmsText != null)
			{
				sendSMS(senderID, retailerID, retailerSmsText);
			} else {
				logger.info("Not sending SMS. One of the mandatory parameter is null. senderID: "
						+ senderID
						+ ", reailerID: "
						+ retailerID
						+ ", retailerSmsText: " + retailerSmsText);
			}
		}
		logger.info("Successfully processed record, response: " + response
				+ ", campaignRequest: " + campaignRequest);
		return response;
	}

	/**
	 * @param contentID
	 * @param contentIDType
	 * @return
	 */
	private String getClipIDFromContentIDAndType(String contentID,
			String contentIDType)
	{
		Clip clip = null;
		if (contentIDType.equalsIgnoreCase("promoID"))
			clip = RBTCacheManager.getInstance().getClipByPromoId(contentID);
		else if (contentIDType.equalsIgnoreCase("wavFile"))
			clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(contentID);
		else if (contentIDType.equalsIgnoreCase("smsAlias"))
			clip = RBTCacheManager.getInstance().getClipBySMSAlias(contentID);
		else
			clip = RBTCacheManager.getInstance().getClip(contentID);

		String clipID = null;
		if (clip != null)
			clipID = String.valueOf(clip.getClipId());

		return clipID;
	}

	/**
	 * @param subscriptionClass
	 * @param circleID
	 * @return
	 */
	private SubClassMap getBaseActionBasedOnSubClass(String subscriptionClass, String circleID)
	{
		List<SubClassMap> subClassMappingList = ftpCampaign.getSubClassMappingList();
		for (SubClassMap subClassMap : subClassMappingList)
		{
			if (subClassMap != null)
			{
				if (!subClassMap.getCircle().equalsIgnoreCase(circleID))
					continue;

				Map<String, String> subClassMapping = subClassMap.getSubClassMapping();
				if (subClassMapping != null)
				{
					if (subClassMapping.containsKey(subscriptionClass) || subClassMapping.containsKey("ALL"))
						return subClassMap;
				}
			}
		}

		for (SubClassMap subClassMap : subClassMappingList)
		{
			if (subClassMap != null)
			{
				if (!subClassMap.getCircle().equalsIgnoreCase("ALL"))
					continue;

				Map<String, String> subClassMapping = subClassMap.getSubClassMapping();
				if (subClassMapping != null)
				{
					if (subClassMapping.containsKey(subscriptionClass) || subClassMapping.containsKey("ALL"))
						return subClassMap;
				}
			}
		}
		return null;
	}

	/**
	 * @param subscriberID
	 * @param subClass
	 * @param mode
	 * @return
	 */
	public String processUpgradeValidity(String subscriberID, String subClass, String mode, String modeInfo)
	{
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriberID);
		subscriptionRequest.setInfo(WebServiceConstants.UPGRADE_VALIDITY);
		subscriptionRequest.setMode(mode);

		if (modeInfo != null)
			subscriptionRequest.setModeInfo(modeInfo);

		subscriptionRequest.setSubscriptionClass(subClass);

		RBTClient.getInstance().updateSubscription(subscriptionRequest);
		logger.info("Processed upgrade validity. response: "
				+ subscriptionRequest.getResponse() + ", subscriptionRequest: "
				+ subscriptionRequest);
		return subscriptionRequest.getResponse();
	}

	/**
	 * @param subscriberID
	 * @param rentalPack
	 * @param mode
	 * @return
	 */
	private String processUpgradeSubscription(String subscriberID, String rentalPack, String mode, String modeInfo)
	{
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriberID);
		subscriptionRequest.setRentalPack(rentalPack);
		subscriptionRequest.setMode(mode);

		if (modeInfo != null)
			subscriptionRequest.setModeInfo(modeInfo);

		RBTClient.getInstance().activateSubscriber(subscriptionRequest);
		logger.info("Processed upgrade subscription. response: "
				+ subscriptionRequest.getResponse() + ", subscriptionRequest: "
				+ subscriptionRequest);
		return subscriptionRequest.getResponse();
	}

	/**
	 * @param campaignRequest
	 * @return
	 */
	private String processSelection(CampaignRequest campaignRequest)
	{
		String subscriberID = campaignRequest.getMsisdn();
		String chargeClass = campaignRequest.getChargeClass();
		if (chargeClass == null || chargeClass.length() == 0)
			chargeClass = ftpCampaign.getSelConfig().getChargeClass();

		String mode = campaignRequest.getSongMode();
		if (mode == null || mode.length() == 0)
			mode = ftpCampaign.getSelConfig().getSelectedBy();

		String contentID = campaignRequest.getContentID();
		if (contentID == null || contentID.length() == 0)
			contentID = ftpCampaign.getSelConfig().getContentId();

		String categoryID = ftpCampaign.getSelConfig().getCategoryId();
		String inLoop = ftpCampaign.getSelConfig().getInLoop();
		String contentIDType = ftpCampaign.getSelConfig().getContentIdType();

		String activationMode = campaignRequest.getActivationMode();
		if (activationMode == null || activationMode.length() == 0)
			activationMode = ftpCampaign.getBaseConfig().getActivatedBy();

		SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
		String subClass = campaignRequest.getSubClass();
		if (subClass == null || subClass.length() == 0)
			subClass = ftpCampaign.getBaseConfig().getSubscriptionClass();

		if (subClass != null)
			selectionRequest.setSubscriptionClass(subClass);
		if (activationMode != null)
			selectionRequest.setActivationMode(activationMode);
		if (chargeClass != null)
		{
			selectionRequest.setChargeClass(chargeClass);
			selectionRequest.setUseUIChargeClass(true);
		}
		if (mode != null)
			selectionRequest.setMode(mode);
		if (categoryID != null)
			selectionRequest.setCategoryID(categoryID);
		else
			return "CATEGORY_NOT_EXISTS";

		Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(categoryID));
		if (category == null)
			return "CATEGORY_NOT_EXISTS";

		if (contentID != null && !Utility.isShuffleCategory(category.getCategoryTpe()))
		{
			String clipID = getClipIDFromContentIDAndType(contentID, contentIDType);
			if (clipID == null)
			{
				logger.info("Invalid contentID for the subscriber : " + subscriberID);
				return "CLIP_NOT_EXISTS";
			}
			selectionRequest.setClipID(clipID);
		}

		if (inLoop != null && inLoop.equalsIgnoreCase("y"))
			selectionRequest.setInLoop(true);
		
		String retailerID = campaignRequest.getRetailerID();
		if (retailerID != null)
			selectionRequest.setModeInfo(retailerID);

		RBTClient.getInstance().addSubscriberSelection(selectionRequest);
		logger.info("Processed selection. selection response: "
				+ selectionRequest.getResponse() + ", selectioinRequest: "
				+ selectionRequest);
		return selectionRequest.getResponse();
	}

	/**
	 * @param subClassMapID
	 * @return
	 */
	private List<ChargeClassMap> getChargeClassMapsForSubClassMapID(String subClassMapID)
	{
		List<ChargeClassMap> chargeClassMappingList = ftpCampaign.getChargeClassMappingList();
		List<ChargeClassMap> tempChargeClassMappingList = new ArrayList<ChargeClassMap>();
		for (ChargeClassMap eachMap : chargeClassMappingList)
		{
			if (eachMap.getSubClassMapId().equals(subClassMapID) || eachMap.getSubClassMapId().equals("ALL"))
				tempChargeClassMappingList.add(eachMap);
		}
		return tempChargeClassMappingList;
	}

	/**
	 * @param subscriberID
	 * @param clipID
	 * @param mode
	 * @return
	 */
	private String upgradeSelectionPack(String subscriberID, String clipID, String mode, String retailerID)
	{
        SelectionRequest selectionRequest = new SelectionRequest(subscriberID);
        if (clipID != null)
        	selectionRequest.setClipID(clipID);

        selectionRequest.setMode(mode);

        if (retailerID != null)
        	selectionRequest.setModeInfo(retailerID);

	    RBTClient.getInstance().upgradeAllSelections(selectionRequest);
	    logger.info("Upgraded selection pack. selection response: "
				+ selectionRequest.getResponse() + ", selectioinRequest: "
				+ selectionRequest);
	    return selectionRequest.getResponse();
	}

	/**
	 * @param task
	 */
	public void sendSMS(String senderNo, String retailerID, String smsText)
	{
		logger.info("Sending SMS, senderNo: " + senderNo + ", retailerId: "
				+ retailerID + ", smsText: " + smsText);
		UtilsRequest utilsRequest = new UtilsRequest(senderNo, retailerID, smsText);
		RBTClient.getInstance().sendSMS(utilsRequest);
	}
}
