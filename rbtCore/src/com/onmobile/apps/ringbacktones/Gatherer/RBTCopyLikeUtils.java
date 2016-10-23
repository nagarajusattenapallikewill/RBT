package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.CurrencyUtil;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.SmsTextCacheManager;
import com.onmobile.apps.ringbacktones.promotions.contest.ContestUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.wrappers.RBTConnector;

public class RBTCopyLikeUtils {
	private static Logger logger = Logger.getLogger(RBTCopyLikeUtils.class);
	private static SmsTextCacheManager parametersCacheManager = CacheManagerUtil
			.getSmsTextCacheManager();

	RBTCopyLikeUtils() {
	}

	public static String getSMSText(String type, String subType,
			String defaultValue, String language) {
		String smsText = parametersCacheManager.getSmsText(type, subType,
				language);
		if (smsText != null)
			return smsText;
		else
			return defaultValue;
	}

	public String getSubstituedSMS(String smsText, String str1, String str2,
			String str3, String actAmt, String selAmt, Clip clip,
			Category category, String calledID, String renewalAmount,String renewalPeriod) {
		return getSubstituedSMS(smsText, str1, str2, str3, actAmt, selAmt,
				clip, category, calledID, -1,renewalAmount, renewalPeriod);
	}

	public String getSubstituedSMS(String smsText, String str1, String str2,
			String str3, String actAmt, String selAmt, Clip clip,
			Category category, String calledID, long likeCount,String renewalAmount,String renewalPeriod) {
		if (smsText == null)
			return null;
		String artistName = null;
		String contentName = null;
		String albumName = null;
		String clipPromoId = null;
		if (category != null
				&& isShuffleCategory(category.getCategoryId() + "")) {
			if (clip != null) {
				artistName = clip.getArtist();
				albumName = clip.getAlbum();
			}
			contentName = category.getCategoryName();
			clipPromoId = category.getCategoryPromoId();
		} else if (clip != null) {
			artistName = clip.getArtist();
			contentName = clip.getClipName();
			albumName = clip.getAlbum();
		}
		if (artistName == null)
			artistName = "";
		if (contentName == null)
			contentName = "";
		if (calledID != null) {
			// FIXME check whether
			// the same is being
			// used already. If not,
			// remove the same.
			smsText = smsText.replaceAll("%CALLED_ID%", calledID);
			smsText = smsText.replaceAll("%CALLED_ID", calledID);
		}
		int configuredArtistLength =  Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.SMS,
				"ARTIST_NAME_LENGTH","0")) ; 
		int configuredSongLength = Integer.parseInt(CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.SMS,
				"SONG_NAME_LENGTH","0")) ;
		
		if (artistName != null && smsText.indexOf("%ARTIST") != -1) {
			if(configuredArtistLength >0 && artistName!= null && !artistName.isEmpty() && configuredArtistLength <= artistName.length() ){
				artistName = artistName.substring(0, configuredArtistLength);
				artistName= artistName.trim();
			}
			
			
			smsText = smsText.substring(0, smsText.indexOf("%ARTIST"))
					+ artistName
					+ smsText.substring(smsText.indexOf("%ARTIST") + 7);

		}
		if (contentName != null && smsText.indexOf("%CONTENT_NAME") != -1) {
			if(configuredSongLength >0 && !contentName.isEmpty() && configuredSongLength <= contentName.length() ){
				contentName = contentName.substring(0, configuredSongLength);
				contentName = contentName.trim();
			}
			smsText = smsText.substring(0, smsText.indexOf("%CONTENT_NAME"))
					+ contentName
					+ smsText.substring(smsText.indexOf("%CONTENT_NAME") + 13);
		}
		if (likeCount > 0 && smsText.indexOf("%LIKECOUNT") != -1) {
			smsText = smsText.substring(0, smsText.indexOf("%LIKECOUNT"))
					+ likeCount
					+ smsText.substring(smsText.indexOf("%LIKECOUNT") + 10);
		}
		if (albumName != null) {
			smsText = smsText.replaceAll("%ALBUM_NAME", albumName);
		}
		if (actAmt != null) {
			actAmt = getInLocalCurrencyFormat(actAmt);
			while (smsText.indexOf("%ACT_AMT") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%ACT_AMT"))
						+ actAmt
						+ smsText.substring(smsText.indexOf("%ACT_AMT") + 8);
			}
		}
		
		
		if (selAmt != null) {
			String specialAmtChar = null;
			specialAmtChar = CacheManagerUtil.getParametersCacheManager()
					.getParameterValue(iRBTConstant.COMMON,
							"SPECIAL_CHAR_CONF_FOR_AMOUNT", ".");
			double selectionAmount = Double.parseDouble(selAmt.replace(specialAmtChar,"."));
			selAmt = getInLocalCurrencyFormat(selAmt);
			if (null != smsText && smsText.contains("%FREE_CHARGE_TEXT")
					&& selectionAmount == 0) {
				while (smsText.indexOf("%FREE_CHARGE_TEXT") != -1) {
					smsText = smsText.substring(0,
							smsText.indexOf("%FREE_CHARGE_TEXT"))
							+ renewalPeriod
							+ smsText.substring(smsText
									.indexOf("%FREE_CHARGE_TEXT") + 17);
				}
				while (smsText.indexOf("%SEL_AMT") != -1) {
					smsText = smsText.substring(0, smsText.indexOf("%SEL_AMT"))
							+ renewalAmount
							+ smsText
									.substring(smsText.indexOf("%SEL_AMT") + 8);
				}
			} else {
				while (smsText.indexOf("%SEL_AMT") != -1) {
					smsText = smsText.replace("%FREE_CHARGE_TEXT","");
					
					smsText = smsText.substring(0, smsText.indexOf("%SEL_AMT"))
							+ selAmt
							+ smsText.substring(smsText.indexOf("%SEL_AMT") + 8);
				}
			}
		}

		String contestHour = ContestUtils.getContestEndTime();
		if (contestHour == null)
			contestHour = "";
		while (smsText.indexOf("%CONTEST_HOUR%") != -1) {
			smsText = smsText.replaceAll("%CONTEST_HOUR%", contestHour);
		}

		if (str2 == null) {
			if (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str1
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
		} else if (str3 == null) {
			if(configuredSongLength >0 && str1!= null && !str1.isEmpty() && configuredSongLength <= str1.length() ){
				str1 = str1.substring(0, configuredSongLength);
				str1 = str1.trim();
			}
			while (smsText.indexOf("%S") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
						+ smsText.substring(smsText.indexOf("%S") + 2);
			}
			while (smsText.indexOf("%C") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
						+ smsText.substring(smsText.indexOf("%C") + 2);
			}
			while (smsText.indexOf("%L") != -1) {
				smsText = smsText.replace(" %L", "");
			}
		} else {
			while (smsText.indexOf("%S") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%S")) + str1
						+ smsText.substring(smsText.indexOf("%S") + 2);
			}
			while (smsText.indexOf("%C") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%C")) + str2
						+ smsText.substring(smsText.indexOf("%C") + 2);
			}
			while (smsText.indexOf("%L") != -1) {
				smsText = smsText.substring(0, smsText.indexOf("%L")) + str3
						+ smsText.substring(smsText.indexOf("%L") + 2);
			}
		}

		// RBT-12607 Added to support promo code in sms text
		if (clipPromoId == null && clip!= null ) {
			clipPromoId = clip.getClipPromoId();
		}
		if (clipPromoId != null && !clipPromoId.trim().equals("")) {
			while (smsText.indexOf("%PROMO_CODE") != -1) {
				if (clipPromoId.indexOf(",") != -1) {
					clipPromoId = clipPromoId.substring(0,
							clipPromoId.indexOf(","));
				}
				smsText = smsText.substring(0, smsText.indexOf("%PROMO_CODE"))
						+ clipPromoId
						+ smsText
								.substring(smsText.indexOf("%PROMO_CODE") + 11);
			}
		} else {
			while (smsText.indexOf("%PROMO_CODE") != -1) {
				smsText = smsText.replace("%PROMO_CODE", "");
			}
		}

		return smsText;
	}

	public boolean isShuffleCategory(String catToken) {
		boolean response = false;
		try {
			int catId = Integer.parseInt(catToken);
			Category category = RBTConnector.getInstance().getMemCache()
					.getCategory(catId);
			if (category != null
					&& com.onmobile.apps.ringbacktones.webservice.common.Utility
							.isShuffleCategory(category.getCategoryTpe())
					&& category.getCategoryEndTime().after(new Date())) {
				response = true;
			}
		} catch (NumberFormatException nfe) {
			response = false;
		}
		return response;
	}

	public String getInLocalCurrencyFormat(String selAmt) {
		String returnValue = selAmt;
		try {
			//double amount = Double.parseDouble(selAmt.replace(",","."));
			returnValue = CurrencyUtil.getFormattedCurrency(null, selAmt);
		} catch (Exception e) {
			logger.warn("In correct value of charge class amout", e);
		}
		return returnValue;
	}

	public Subscriber getSubscriber(String strSubID) {
		return RBTConnector.getInstance().getSubscriberRbtclient()
				.getSubscriber(strSubID, "GATHERER");
	}

	public boolean isTNBuser(String subscriptionClass) {
		List<String> tnbSubscriptionClasses = ListUtils.convertToList(
				CacheManagerUtil.getParametersCacheManager().getParameterValue(
						"COMMON", "TNB_SUBSCRIPTION_CLASSES", "ZERO"), ",");
		boolean isTnBUser = false;
		if (!tnbSubscriptionClasses.isEmpty()
				&& tnbSubscriptionClasses.contains(subscriptionClass)) {
			isTnBUser = true;
		}
		return isTnBUser;
	}

	public boolean isBlockedCatTypeOrCatInfo(boolean isNotCopyShuffle,
			String catTypes, int catType, String catInf) {
		if (!isNotCopyShuffle && null != catTypes
				&& !catTypes.equalsIgnoreCase("")) {
			isNotCopyShuffle = (Arrays.asList(catTypes.split(","))
					.contains(String.valueOf(catType)));
			if (isNotCopyShuffle
					&& null != RBTLikeDaemon.m_blockedCategoryInfoList
					&& !RBTLikeDaemon.m_blockedCategoryInfoList.isEmpty()
					&& null != catInf) {
				catInf = catInf.trim().toUpperCase();
				isNotCopyShuffle = RBTLikeDaemon.m_blockedCategoryInfoList
						.contains(catInf);
			}
		}
		return isNotCopyShuffle;
	}

	public boolean isUserHavingSelection(Subscriber subscriber) {
		SubscriberStatus[] subscriberStatus = RBTDBManager.getInstance()
				.getAllActiveSubscriberSettings(subscriber.getSubscriberID());
		if (subscriberStatus != null && subscriberStatus.length != 0) {
			return true;
		}
		return false;
	}

	public boolean allowDefaultCopyUserHasSelection(Subscriber callerSub,
			Subscriber calledSub) {
		if (!RBTParametersUtils.getParamAsBoolean("GATHERER",
				"COPY_DEFAULT_CALLER_HAS_NO_SELECTON", "FALSE")) {
			// Parameter not configured
			return true;
		}

		if (!Utility.isSubActive(callerSub)
				|| !isUserHavingSelection(callerSub)) {
			return true;
		}

		return false;
	}
}
