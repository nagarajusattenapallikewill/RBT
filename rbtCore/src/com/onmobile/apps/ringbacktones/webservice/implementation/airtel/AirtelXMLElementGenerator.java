/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.airtel;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.FeedSchedule;
import com.onmobile.apps.ringbacktones.content.FeedStatus;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.TransData;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriber;
import com.onmobile.apps.ringbacktones.webservice.content.RBTContentProviderFactory;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicXMLElementGenerator;

/**
 * @author vinayasimha.patil
 *
 */
public class AirtelXMLElementGenerator extends BasicXMLElementGenerator
{
	public static Element generateCallDetailsElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber)
	{
		Element element = document.createElement(CALL_DETAILS);

		Element languagesElem = generateLanguagesElement(document, webServiceSubscriber);
		element.appendChild(languagesElem);

		Element mmRequestElem = generateMultiModelRequestElement(document, task, webServiceSubscriber);
		if (mmRequestElem != null)
			element.appendChild(mmRequestElem);

		if (mmRequestElem == null)
		{
			Element mmConetentElem = generateContentCallBackElement(document, task, webServiceSubscriber);
			if (mmConetentElem != null)
				element.appendChild(mmConetentElem);
		}

		Element pickOfTheDayElem = generatePickOfTheDayElement(document, task, webServiceSubscriber);
		if (pickOfTheDayElem != null)
			element.appendChild(pickOfTheDayElem);

		Element hotSongElem = generateHotSongElement(document, task, webServiceSubscriber.getCircleID());
		if (hotSongElem != null)
			element.appendChild(hotSongElem);

		return element;
	}

	public static Element generateHotSongElement(Document document, WebServiceContext task,
			String circleID)
	{
		Element element = document.createElement(HOT_SONG);

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		
		PickOfTheDay[] pickOfTheDays = DataUtils.getPickOfTheDay(task, circleID, 'b', "HOT_CRICKET");
		PickOfTheDay hotCricket = null;
		if (pickOfTheDays.length > 0)
			hotCricket = pickOfTheDays[0];
		
		if (hotCricket != null)
		{
			String subscriberID = task.getString(param_subscriberID);
			SubscriberStatus status = rbtDBManager.getActiveSubscriberRecord(subscriberID, null, 90, 0, 2359);
			if (status == null)
			{
				Element cricketElem = getCricketElement(document, task);
				element.appendChild(cricketElem);
				element.setAttribute(TYPE, CRICKET);
				return element;
			}
		}
		else
		{
			PickOfTheDay[] hotSongs = DataUtils.getPickOfTheDay(task, circleID, 'b', "HOT_SONG");
			PickOfTheDay hotSong = null;
			if (hotSongs.length > 0)
				hotSong = hotSongs[0];
			
			if (hotSong != null)
			{
				Element contentsElem = document.createElement(CONTENTS);
				element.appendChild(contentsElem);
				String browsingLanguage = task.getString(param_browsingLanguage);

				Category category = rbtCacheManager.getCategory(hotSong.categoryID(), browsingLanguage);
				if (hotSong.clipID() == 0)
				{
					if (category != null)
					{
						Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, category);
						contentsElem.appendChild(contentElem);
						element.setAttribute(TYPE, CATEGORY);
						return element;
					}
				}
				else
				{
					Clip songOfTheDay = rbtCacheManager.getClip(hotSong.clipID(), browsingLanguage);
					if (songOfTheDay != null && category != null)
					{
						Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getClipContentElement(document, category, songOfTheDay);
						contentsElem.appendChild(contentElem);
						element.setAttribute(TYPE, CLIP);
						return element;
					}
				}
			}
		}

		return null;
	}

	public static Element generateContentCallBackElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber)
	{
		if (!task.containsKey(param_calledNo))
			return null;

		Element element = document.createElement(MM_CONTENT);
		element.setAttribute(IS_TOLL_FREE_NO, NO);

		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		String promoNo = null;
		boolean isCallBackBaseNo = false;
		boolean mmContentAvailable = false;

		Parameters baseNumbersParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "BASENUMBERS", null);
		Parameters callbackBaseNumbersParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "CALLBACK_BASENUMBERS", null);
		if (baseNumbersParam != null || callbackBaseNumbersParam != null)
		{
			String calledNo = task.getString(param_calledNo);

			String ivrNo = null;

			if (baseNumbersParam != null)
			{
				List<String> baseNumbersList = Arrays.asList(baseNumbersParam.getValue().trim().split(","));

				if (baseNumbersList.contains(calledNo))
					return null;

				for (String baseNumber : baseNumbersList)
				{
					if (calledNo.startsWith(baseNumber))
					{
						ivrNo = baseNumber;
						promoNo = calledNo.substring(calledNo.indexOf(ivrNo) + ivrNo.length());
						break;
					}
				}
			}

			if (ivrNo == null && callbackBaseNumbersParam != null)
			{
				List<String> callbackBaseNumbersList = Arrays.asList(callbackBaseNumbersParam.getValue().trim().split(","));

				element.setAttribute(IS_TOLL_FREE_NO, YES);
				if (callbackBaseNumbersList.contains(calledNo))
					return element;

				for (String callbackBaseNumber : callbackBaseNumbersList)
				{
					if (calledNo.startsWith(callbackBaseNumber))
					{
						ivrNo = callbackBaseNumber;
						promoNo = calledNo.substring(calledNo.indexOf(ivrNo) + ivrNo.length());
						isCallBackBaseNo = true;
						break;
					}
				}
			}

			if (promoNo != null)
			{
				if (isTollFreeNumber(promoNo))
					element.setAttribute(IS_TOLL_FREE_NO, YES);

				String browsingLanguage = task.getString(param_browsingLanguage);
				Date currentDate = new Date();
				Clip clip = rbtCacheManager.getClipByPromoId(promoNo, browsingLanguage);
				if (clip != null && clip.getClipEndTime().after(currentDate))
				{
					Parameters callbackCatgoryParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, ivrNo+ "_CALLBACK_CATEGORY", null);
					int categoryID = Integer.parseInt(callbackCatgoryParam.getValue());
					Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);

					Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getClipContentElement(document, category, clip);
					contentsElem.appendChild(contentElem);

					Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(categoryID));

					mmContentAvailable = true;
				}
				else if (!isCallBackBaseNo)
				{
					Category category = rbtCacheManager.getCategoryByMmNumber(promoNo, browsingLanguage);
					if (category != null && category.getCategoryEndTime().after(currentDate))
					{
						Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, category);
						contentsElem.appendChild(contentElem);

						mmContentAvailable = true;
					}
				}
			}
		}

		if (mmContentAvailable || isTollFreeNumber(promoNo) || isCallBackBaseNo)
			return element;

		return null;
	}

	private static boolean isTollFreeNumber(String promoNo)
	{
		Parameters tollFreeMMNosParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "TOLL_FREE_MM_NUMBERS", null);
		if (tollFreeMMNosParam != null && promoNo != null)
		{
			RE re;
			try {
				re = new RE(tollFreeMMNosParam.getValue());
				return(re.match(promoNo));
			} catch (RESyntaxException e) {
				Logger.getLogger(AirtelXMLElementGenerator.class).error(e);
			}
		}

		return false;
	}

	public static Element generateMultiModelRequestElement(Document document,
			WebServiceContext task, WebServiceSubscriber webServiceSubscriber)
	{
		if (!task.containsKey(param_mmContext))
			return null;

		Element element = null;

		String[] mmContext = task.getString(param_mmContext).split("\\|");

		String subscriberID = task.getString(param_subscriberID);
		String browsingLanguage = task.getString(param_browsingLanguage);

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		if (mmContext[0].equalsIgnoreCase("RBT_EASY_CHARGE"))
		{
			ViralSMSTable[] retailRequests = rbtDBManager.getViralSMSesByType(subscriberID, "EC");

			element = document.createElement(EASY_CHARGE);
			element.setAttribute(CAN_ALLOW, NO);

			if (retailRequests != null)
			{
				if (mmContext[1].equalsIgnoreCase("ACTIVATION"))
				{
					for (ViralSMSTable retailRequest : retailRequests)
					{
						if (retailRequest.clipID() == null)
						{
							element.setAttribute(CAN_ALLOW, YES);
							break;
						}
					}
				}
				else if (mmContext[1].equalsIgnoreCase("SELECTION"))
				{
					String subscriberStatus = webServiceSubscriber.getStatus();
					if (!subscriberStatus.equalsIgnoreCase(NEW_USER) && !subscriberStatus.equalsIgnoreCase(DEACTIVE))
					{
						for (ViralSMSTable retailRequest : retailRequests)
						{
							if (retailRequest.clipID() != null)
							{
								element.setAttribute(CAN_ALLOW, YES);
								break;
							}
						}
					}
				}
			}
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_ADVANCE_RENTAL"))
		{
			element = document.createElement(ADVANCE_RENTAL_PACKS);

			String subscriptionClass = webServiceSubscriber.getSubscriptionClass();
			if (subscriptionClass != null && Utility.isAdvanceRentalPack(subscriptionClass))
			{
				element.setAttribute(CAN_ALLOW, NO);
			}
			else
			{
				element.setAttribute(CAN_ALLOW, YES);
				String[] advanceRentals = rbtDBManager.getAdvancedRentalValuesDB();
				if (advanceRentals != null && advanceRentals.length > 0)
				{
					String packs = "";
					for (String advanceRental : advanceRentals)
						packs += advanceRental + ",";
					packs = packs.substring(0, packs.length() - 1);

					element.setAttribute(PACKS, packs);
				}
			}
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_SPECIAL_PACKS"))
		{
			element = document.createElement(ADVANCE_RENTAL_PACKS);

			String packs = "";
			for (int i = 1; i < mmContext.length -1; i++)
				packs += mmContext[i] + ",";
			if (mmContext.length -1 >= 1)
				packs += mmContext[mmContext.length -1];

			element.setAttribute(PACKS, packs);
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_INTERNATIONAL_ROAMING") || mmContext[0].equalsIgnoreCase("RBT_PERSONALIZED_HELLOTUNE"))
		{
			int categoryID = Integer.parseInt(mmContext[1]);
			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);

			element = document.createElement(MM_CONTENT);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			if (category != null)
			{
				Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, category);
				contentsElem.appendChild(contentElem);
			}
		}
		else if (mmContext[0].startsWith("RBT_SCRATCHCARD"))
		{
			int categoryID = Integer.parseInt(mmContext[1]);

			element = document.createElement(SCRATCHCARD_DETAILS);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			String context = "scratchcard";
			if (task.containsKey(param_context))
				context = task.getString(param_context);

			TransData scratchcard = null;
			TransData[] transDatas = rbtDBManager.getTransDataBySubscriberID(subscriberID, context);
			if (transDatas != null)
			{
				for (TransData transData : transDatas)
				{
					if (transData.transDate() != null && transData.transDate().after(new Date()))
					{
						scratchcard = transData;
						break;
					}
				}
			}

			String askNumber = YES;
			if (scratchcard != null)
			{
				askNumber = NO;
				element.setAttribute(NUMBER, scratchcard.transID());

				String accessCount = "0";
				String noOfDownloads = "0";
				String transCount = scratchcard.accessCount();
				if (transCount != null)
				{
					String[] transCounts = transCount.split(":");
					if (transCounts.length > 0)
						accessCount = transCounts[0];
					if (transCounts.length > 1)
						noOfDownloads = transCounts[1];
				}

				element.setAttribute(ACCESS_COUNT, accessCount);
				element.setAttribute(NO_OF_DOWNLOADS, noOfDownloads);
			}
			element.setAttribute(ASK_NUMBER, askNumber);

			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
			if (category != null)
			{
				Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, category);
				contentsElem.appendChild(contentElem);
			}
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_TNB"))
		{
			element = document.createElement(TNB_DETAILS);
			element.setAttribute(CAN_ALLOW, NO);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			if (!rbtDBManager.isBlackListedForTNB(subscriberID))
			{
				String subscriberStatus = webServiceSubscriber.getStatus();
				if (subscriberStatus.equalsIgnoreCase(NEW_USER) || subscriberStatus.equalsIgnoreCase(DEACTIVE))
				{
					Parameters parameter = parametersCacheManager.getParameter(iRBTConstant.COMMON, "MAX_TNB_ACCESS", "4");
					int maxTNBAccess = Integer.parseInt(parameter.getValue().trim());

					SubscriberPromo subscriberPromo = rbtDBManager.getSubscriberPromo(subscriberID, "TNB");
					if (subscriberPromo == null || subscriberPromo.freedays() < maxTNBAccess)
					{
						element.setAttribute(CAN_ALLOW, YES);

						Category tnbCategory = null;
						Parameters tnbCategoryParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "TNB_CATEGORY", null);
						if (tnbCategoryParam != null)
							tnbCategory = rbtCacheManager.getCategory(Integer.parseInt(tnbCategoryParam.getValue()), browsingLanguage);

						if (tnbCategory != null)
						{
							int freeDays = 1;
							if (subscriberPromo != null)
							{
								freeDays = subscriberPromo.freedays() + 1;
								rbtDBManager.changeSubscriberPromoFreeDays(subscriberID, freeDays);
							}
							else
								rbtDBManager.createSubscriberPromo(subscriberID, freeDays, true, null, "TNB");

							element.setAttribute(ACCESS_COUNT, String.valueOf(freeDays));
							element.setAttribute(MAX_ACCESS, String.valueOf(maxTNBAccess));
							if (freeDays == maxTNBAccess)
							{
								rbtDBManager.addToTNBBlackList(subscriberID);
								rbtDBManager.removeSubscriberPromo(subscriberID, null, "TNB");
							}

							Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, tnbCategory);
							contentsElem.appendChild(contentElem);
						}
					}
				}
				else
				{
					rbtDBManager.addToTNBBlackList(subscriberID);
				}
			}
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_CRICKET"))
		{
			element = getCricketElement(document, task);
		}
		else if (mmContext[0].equalsIgnoreCase("RBT_ALBUM"))
		{
			element = document.createElement(ALBUM_DETAILS);
			Element contentsElem = document.createElement(CONTENTS);
			element.appendChild(contentsElem);

			int categoryID = 98;
			if (mmContext.length > 1)
			{
				try
				{
					categoryID = Integer.parseInt(mmContext[1]);
				}
				catch (NumberFormatException e)
				{
					categoryID = 98;
				}
			}
			Category category = rbtCacheManager.getCategory(categoryID, browsingLanguage);
			if (category != null)
			{
				Element contentElem = RBTContentProviderFactory.getRBTContentProvider().getCategoryContentElement(document, null, category);
				contentsElem.appendChild(contentElem);
			}
		}

		return element;
	}

	private static Element getCricketElement(Document document, WebServiceContext task)
	{
		Element element = document.createElement(CRICKET_DETAILS);
		Element contentsElem = document.createElement(CONTENTS);
		element.appendChild(contentsElem);

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		FeedSchedule schedule = rbtDBManager.getFeedSchedule("CRICKET", "SP");

		if (schedule == null)
		{
			Parameters criIntvlParm = parametersCacheManager.getParameter(iRBTConstant.COMMON, "CRICKET_INTERVAL", "2");
			int cricketInterval = Integer.parseInt(criIntvlParm.getValue().trim());

			schedule = getNextCricketSchedule("SP", cricketInterval);
		}

		if (schedule != null)
		{
			FeedStatus feedStatus = rbtDBManager.getFeedStatus("CRICKET");

			String feedFile = null;
			if (feedStatus != null)
			{
				feedFile = feedStatus.file();
				if (feedFile != null && feedFile.indexOf(",") != -1)
				{
					feedFile = feedFile.substring(feedFile.lastIndexOf(",") + 1);
				}
				if (feedStatus.status().equalsIgnoreCase("OFF"))
					feedFile = null;
			}

			if (feedFile == null)
				feedFile = "cricket_default";

			Parameters cricketCategoryParam = parametersCacheManager.getParameter(iRBTConstant.COMMON, "CRICKET_CATEGORY", "10");
			int categoryID = Integer.parseInt(cricketCategoryParam.getValue());

			Element contentElem = document.createElement(CONTENT);
			contentElem.setAttribute(ID, "SP");
			contentsElem.appendChild(contentElem);

			Utility.addPropertyElement(document, contentElem, CRICKET_PREVIEW_FILE, PROMPT, Utility.getPromptName(feedFile));
			Utility.addPropertyElement(document, contentElem, CATEGORY_ID, DATA, String.valueOf(categoryID));
		}

		String subscriberID = task.getString(param_subscriberID);
		String isCricketUser = NO;
		SubscriberStatus[] settings = rbtDBManager.getAllActiveSubSelectionRecords(subscriberID);

		if (settings != null)
		{
			for (SubscriberStatus setting : settings)
			{
				if (setting.status() == 90)
				{
					isCricketUser = YES;
					break;
				}
			}
		}
		element.setAttribute(IS_CRICKET_USER, isCricketUser);

		return element;
	}

	private static FeedSchedule getNextCricketSchedule(String pass, int interval)
	{
		FeedSchedule[] schedule = RBTDBManager.getInstance().getFeedSchedules("CRICKET", pass, interval);
		if (schedule == null || schedule.length == 0)
			return null;

		return (schedule[0]);
	}
}
