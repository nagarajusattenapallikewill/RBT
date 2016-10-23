package com.onmobile.apps.ringbacktones.daemons.smcallback;

import static com.onmobile.apps.ringbacktones.common.iRBTConstant.CHARGE_CLASS;
import static com.onmobile.apps.ringbacktones.common.iRBTConstant.COMMON;
import static com.onmobile.apps.ringbacktones.common.iRBTConstant.GIFTTYPE_ATTR;
import static com.onmobile.apps.ringbacktones.common.iRBTConstant.GIFT_TRANSACTION_ID;
import static com.onmobile.apps.ringbacktones.common.iRBTConstant.SUBSCRIPTION_CLASS;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.SRBTUtility;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.SubscriberActivityCounts;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.SubscriberActivityCountsDAO;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author sridhar.sindiri
 *
 */
public class SMGiftCallback implements SMCallback
{
	private static Logger logger = Logger.getLogger(SMGiftCallback.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallback#processCallback(com.onmobile.apps.ringbacktones.daemons.smcallback.SMCallbackContext)
	 */
	@Override
	public SMCallbackResponse processCallback(SMCallbackContext smCallbackContext)
	{
		try
		{
			String refID = smCallbackContext.getRefid();
			StringTokenizer stk = new StringTokenizer(refID, ":");
			if (stk.hasMoreTokens())
			{
				stk.nextToken();
				String gifter = smCallbackContext.getMsisdn();
				String giftedTo = null;
				String clipID = null;
				String giftTime = null;
				if (stk.hasMoreTokens())
					giftedTo = stk.nextToken();
				if (stk.hasMoreTokens())
					clipID = stk.nextToken();
				if (stk.hasMoreTokens())
					giftTime = stk.nextToken();

				String amt = null;
				String amountCharged = smCallbackContext.getAmountCharged();
				if (amountCharged != null)
					amt = ":" + amountCharged;

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
				Date giftSentTime = sdf.parse(giftTime);

				String srvKey = smCallbackContext.getSrvkey();
				if (srvKey != null && srvKey.indexOf("_GIFT") != -1)
					srvKey = srvKey.substring(0, srvKey.indexOf("_GIFT"));
				if(srvKey != null && srvKey.indexOf("_RBT_ACT") != -1)
					srvKey = srvKey.substring(8, srvKey.indexOf("_RBT_ACT"));

				String eventKey = smCallbackContext.getEventkey();
				if (eventKey != null && eventKey.startsWith("RBT_SEL_"))
				{
					eventKey = eventKey.substring(8);
					if (eventKey.indexOf("_GIFT") != -1)
						eventKey = eventKey.substring(0, eventKey.indexOf("_GIFT"));
				}

				ViralSMSTable viralSMSTable = RBTDBManager.getInstance().getViralPromotion(gifter, giftedTo, giftSentTime, "GIFTCHRGPENDING");
				if (viralSMSTable == null)
					return new SMCallbackResponse("INVALID|GIFT ENTRY NOT FOUND");

				String mode = viralSMSTable.selectedBy();
				String extraInfo = null;
				extraInfo = DBUtility.setXMLAttribute(viralSMSTable.extraInfo(), SUBSCRIPTION_CLASS, srvKey);
				extraInfo = DBUtility.setXMLAttribute(extraInfo, CHARGE_CLASS, eventKey);

				String transID = smCallbackContext.getTransId();
				if (transID != null)
					extraInfo = DBUtility.setXMLAttribute(extraInfo, GIFT_TRANSACTION_ID, transID);

				if (gifter != null && giftedTo != null && giftTime != null)
				{
					String status = smCallbackContext.getStatus();
					if (!status.equalsIgnoreCase("SUCCESS"))
					{
						int giftLimit = 0;
						if (clipID == null || clipID.equalsIgnoreCase("NULL"))
							giftLimit = RBTParametersUtils.getParamAsInt(COMMON, "SERVICE_GIFT_LIMIT", 0);
						else
							giftLimit = RBTParametersUtils.getParamAsInt(COMMON, "TONE_GIFT_LIMIT", 0);

						if (giftLimit > 0)
						{
							SubscriberActivityCounts subscriberActivityCounts = SubscriberActivityCountsDAO
									.getSubscriberActivityCountsForDate(gifter, giftSentTime);
							if (subscriberActivityCounts != null)
							{
								if (clipID == null || clipID.equalsIgnoreCase("NULL"))
									subscriberActivityCounts.decrementServiceGiftsCount();
								else
									subscriberActivityCounts.decrementToneGiftsCount();

								subscriberActivityCounts.update();
							}
						}

						String gifterSms = CacheManagerUtil.getSmsTextCacheManager().getSmsText("DAEMON",
								"GIFT_FAILED_LOW_BALANCE_TEXT", null);
						String gifteeSms = CacheManagerUtil.getSmsTextCacheManager().getSmsText("DAEMON",
								"GIFT_FAILED_LOW_BALANCE_TEXT_TO_GIFTEE", null);

						String songName = null;
						if (gifterSms != null || gifteeSms != null)
						{
							boolean isShuffle = false;
							int cID = -1;
							String contentID = viralSMSTable.clipID();
							if (contentID.startsWith("C"))
							{
								isShuffle = true;
								contentID = contentID.substring(1);
							}
							try
							{
								cID = Integer.parseInt(contentID);

								if (isShuffle)
									songName = RBTCacheManager.getInstance().getCategory(cID).getCategoryName();
								else
									songName = RBTCacheManager.getInstance().getClip(cID).getClipName();
							}
							catch(Exception e)
							{
								cID = -1;
							}
						}

						if (gifterSms != null)
						{
							gifterSms = gifterSms.replaceAll("%SONG_NAME",
									(songName == null) ? "" : songName);
							gifterSms = gifterSms.replaceAll("%GIFTEE",
									viralSMSTable.callerID());
							Tools.sendSMS(RBTParametersUtils.getParamAsString("DAEMON",
									"GIFT_SENDER_NUMBER", "123456"), viralSMSTable.subID(),
									gifterSms, RBTParametersUtils.getParamAsBoolean("DAEMON",
											"SEND_SMS_MASS_PUSH", "FALSE"));
						}

						if (gifteeSms != null)
						{
							gifteeSms = gifteeSms.replaceAll("%SONG_NAME",
									(songName == null) ? "" : songName);
							gifteeSms = gifteeSms.replaceAll("%GIFTER",
									viralSMSTable.subID());
							Tools.sendSMS(RBTParametersUtils.getParamAsString("DAEMON",
									"GIFT_SENDER_NUMBER", "123456"), viralSMSTable.callerID(),
									gifteeSms, RBTParametersUtils.getParamAsBoolean("DAEMON",
											"SEND_SMS_MASS_PUSH", "FALSE"));
						}
					}

					// checks if the gifted mode is supported for direct copy or not
					String directGiftSupportedModes = RBTParametersUtils.getParamAsString(COMMON, "DIRECT_GIFT_SUPPORTED_MODES", "");
					List<String> directGiftModesList = Arrays.asList(directGiftSupportedModes.split(","));
					if (directGiftModesList.contains(mode))
						extraInfo = DBUtility.setXMLAttribute(extraInfo, GIFTTYPE_ATTR, "direct");
					
					
					boolean isToUpdateAmtInSelBy = RBTParametersUtils.getParamAsString(COMMON, "TO_UPDATE_GIFT_AMT_IN_SEL_BY_COL", "TRUE").equalsIgnoreCase("TRUE");
					
					if(!isToUpdateAmtInSelBy) {
						extraInfo = DBUtility.setXMLAttribute(extraInfo, "aountCharged", amt);
						amt = null;						
					}
					// RBT-14301: Uninor MNP changes.
					if (RBTDBManager.getInstance().updateGiftCharge(gifter, giftedTo, clipID, giftTime, status, amt, extraInfo, null))
					{
						boolean m_socialRBTAllowed = RBTParametersUtils.getParamAsBoolean(COMMON, "SOCIAL_RBT_ALLOWED", "false");
						int clipId =-1;
						if(clipID != null){
							try {
								clipId =Integer.parseInt(clipID);
							} catch (Exception e) {
								clipId =-1;
								e.printStackTrace();
							}
						}
						String toneType = getToneType(clipId);
						logger.debug("ToneType : " + toneType);
						SRBTUtility.updateSocialGiftForSuccess(m_socialRBTAllowed, extraInfo, giftedTo, gifter, giftSentTime, giftTime, amt, srvKey, directGiftSupportedModes, toneType, clipId);
						return new SMCallbackResponse("SUCCESS");
					}
					else
						return new SMCallbackResponse("FAILURE");
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
			return new SMCallbackResponse("INVALID|DATABASE EXCEPTION");
		}

		return new SMCallbackResponse("FAILURE");
	}

	/**
	 * @param clipId
	 * @return
	 */
	private String getToneType(int clipId)
	{
		String toneType = null;
		if (clipId == -1)
			toneType = WebServiceConstants.CATEGORY_SHUFFLE;
		else
			toneType = WebServiceConstants.CLIP;

		return toneType;
	}
}
