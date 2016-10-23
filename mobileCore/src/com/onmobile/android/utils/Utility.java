package com.onmobile.android.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import com.onmobile.android.beans.CallerIdGroupBean;
import com.onmobile.android.beans.ExtendedClipBean;
import com.onmobile.android.beans.ExtendedDownloadBean;
import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category.CategoryInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.StringUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * @author sridhar.sindiri
 *
 */
public class Utility
{
	private static Logger logger = Logger.getLogger(Utility.class);
	private static Set<String> loopStatusesForLoop = new HashSet<String>();

	static {
		loopStatusesForLoop.add(String.valueOf(iRBTConstant.LOOP_STATUS_LOOP_INIT));
		loopStatusesForLoop.add(String.valueOf(iRBTConstant.LOOP_STATUS_LOOP));
		loopStatusesForLoop.add(String.valueOf(iRBTConstant.LOOP_STATUS_LOOP_FINAL));
	}

	public static boolean isShuffleCategory(int categoryType) {
		return (categoryType == iRBTConstant.SHUFFLE
				|| categoryType == iRBTConstant.WEEKLY_SHUFFLE
				|| categoryType == iRBTConstant.DAILY_SHUFFLE
				|| categoryType == iRBTConstant.MONTHLY_SHUFFLE
				|| categoryType == iRBTConstant.TIME_OF_DAY_SHUFFLE
				|| categoryType == iRBTConstant.ODA_SHUFFLE
				|| categoryType == iRBTConstant.BOX_OFFICE_SHUFFLE
				|| categoryType == iRBTConstant.FESTIVAL_SHUFFLE
				|| categoryType == iRBTConstant.FEED_SHUFFLE
				|| categoryType == iRBTConstant.MONTHLY_ODA_SHUFFLE
				|| categoryType == iRBTConstant.AUTO_DOWNLOAD_SHUFFLE
				|| categoryType == iRBTConstant.PLAYLIST_ODA_SHUFFLE);
	}

	public static boolean isStringValid(String str) {
		if (str == null || str.trim().isEmpty()) {
			return false;
		}
		return true;
	}

	public static String getURLEncodedValue(String param) {
		if (param == null) {
			return null;
		}
		try {
			return URLEncoder.encode(param, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("Encoding error. " + e, e);
			return null;
		}
	}

	public static Map<String, String> getRequestParamsMap(HttpServletRequest request) {
		Map<String, String> requestParams = new HashMap<String, String>();
		@SuppressWarnings("unchecked")
		Enumeration<String> params = request.getParameterNames();
		while (params.hasMoreElements()) {
			String key = params.nextElement();
			String value = request.getParameter(key).trim();
			requestParams.put(key, value);
		}
		return requestParams;
	}

	public static Map<String,String[]> convertListMapToStringArrayMap(Map<String, List<String>> listMap) {
		if (listMap == null) {
			return null;
		}
		Map<String,String[]> stringArrayMap = new HashMap<String,String[]>();
		Set<String> keySet = listMap.keySet();
		for (String key: keySet) {
			List<String> valueList = listMap.get(key);
			if (valueList != null) {
				String valueArray[] = valueList.toArray(new String[valueList.size()]);
				stringArrayMap.put(key, valueArray);
			}
		}
		return stringArrayMap;

	}
	public static Map<String, List<String>> convertStringIntoURLDecodedStringListtMap(String str,
			String delimiter1, String delimiter2) {
		logger.debug("Converting str to map: " + str + ", delimeter1: " + delimiter1
				+ ", delimeter2: " + delimiter2);
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		if (null == str || null == delimiter1) {
			logger.warn("Returning empty map for str: "+str);
			return map;
		}
		List<String> list = ListUtils.convertToList(str, delimiter1);
		System.out.println(list); 
		for (String s : list) {
			if (null != delimiter2) {
				String[] keyValuePair = StringUtils
						.toStringArray(s, delimiter2);
				System.out.println("key = "+keyValuePair); 
				if (keyValuePair.length > 1) {
					String key = keyValuePair[0];
					String value = keyValuePair[1];
					System.out.println("key = "+key+",value="+value); 
					List<String> valueList = map.get(key);
					if (valueList == null) {
						valueList = new ArrayList<String>();
						map.put(key, valueList);
					}
					try {
						value = URLDecoder.decode(value,"UTF-8");
					} catch (UnsupportedEncodingException e) {
						logger.error("UnsupportedEncodingException caught " + e, e);
					}
					valueList.add(value);
				}
			}
		}
		logger.info("Converted the given string : " + str + ", to map: " + map);
		return map;
	}

	public static String replaceStringInString(String fullString,
			String toBeReplaced, String newString) {
		if (newString != null) {

			// return fullString.replaceAll(toBeReplaced,
			// URLEncoder.encode(newString, "UTF-8"));
		 fullString = fullString.replaceAll(toBeReplaced, newString);
		return 	 fullString;

		} else {
			return fullString.replaceAll(toBeReplaced, "");
		}
	}

	public static Subscriber getSubscriber(String subId) {
		logger.debug("subId: " + subId);
		if (!isStringValid(subId)) {
			logger.info("SubscriberId is null. Returning null.");
			return null;
		}
		RbtDetailsRequest rbtRequest = new RbtDetailsRequest(subId,null);
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtRequest);
		logger.debug("Subscriber: " + subscriber);
		return subscriber;
	}

	public static String getCircleId(String subscriberId) {
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(new RbtDetailsRequest(subscriberId));
		String circleId = PropertyConfigurator.getDefaultCircleId();
		if(subscriber!=null){
			if(subscriber.getCircleID() != null) {
				circleId = subscriber.getCircleID();
			}
		}
		if(circleId != null && circleId.equalsIgnoreCase("NON_ONMOBILE")){
			SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(subscriberId, "RBTCLIENT"));
			if (subscriberDetail != null)
				circleId = subscriberDetail.getCircleID();
		}
		if(circleId == null || circleId.length() == 0) {
			circleId = "Default";
		}
		logger.info("Circle Id: " + circleId);
		return circleId;
	}

	public static String getChargeClass(String msisdn, String clipId, String subCategoryId) {
		SelectionRequest selectionRequest = new SelectionRequest(msisdn);
		selectionRequest.setClipID(clipId);
		selectionRequest.setCategoryID(subCategoryId);
		ChargeClass chargeClass = RBTClient.getInstance().getNextChargeClass(selectionRequest);
		String period = "NA";
		String amount = "NA";
		String renewalPeriod = "NA";
		String renewalAmount = "NA";
		if(chargeClass!=null) {
			period = getDisplayForSubPeriod(chargeClass.getPeriod());
			renewalPeriod = getDisplayForSubPeriod(chargeClass.getRenewalPeriod());
			amount = chargeClass.getAmount();
			renewalAmount = chargeClass.getRenewalAmount();
			return period + ":"+amount + ":" + renewalPeriod + ":" + renewalAmount;
		}
		return null;
	}


	public static String getDisplayForSubPeriod(String subPeriod){
		String days = PropertyConfigurator.getDayFormat();;
		String Month = PropertyConfigurator.getMonthFormat();
		if(days == null) {
			days = "Days";
		} 
		if(Month == null) {
			Month = "Month";
		}
		if(days != null && days.length()>0) {
			if(subPeriod.contains("M")) {
				logger.info("month: "+ subPeriod);
				return (Integer.parseInt(subPeriod.substring(1))*30 + " " + days);
			} else if(subPeriod.contains("D")) {
				logger.info("days: "+ subPeriod);
				return (subPeriod.substring(1) + " " + days);
			}
		} else {
			if(subPeriod.contains("M")) {
				logger.info("month: "+ subPeriod);
				return (subPeriod.substring(1) + " " + Month);
			} else if(subPeriod.contains("D")) {
				logger.info("days: "+ subPeriod);
				return (subPeriod.substring(1) + " " + days);
			}
		}
		logger.info("subPeriod: "+ subPeriod);
		return subPeriod;
	}

	public static String getInLoop(String loopStatus) {
		if (loopStatus != null && loopStatusesForLoop.contains(loopStatus)) {
			return "y";
		}
		return "n";
	}

	public static boolean isSelectionAllCallerDefaultSelection(Setting setting) {
		if ((setting.getCallerID() == null || setting.getCallerID().equalsIgnoreCase("ALL"))
				&& setting.getStatus() == 1) {
			return true;
		}
		return false;
	}

	public static boolean isSelectionActive(String selStatus) {
		return selStatus != null && (selStatus
				.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| selStatus.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
				|| selStatus.equalsIgnoreCase(WebServiceConstants.GRACE)
				|| selStatus.equalsIgnoreCase(WebServiceConstants.SUSPENDED));
	}

	public static CallerIdGroupBean getCallerIdGroupBean(Setting setting) {
		CallerIdGroupBean callerIdGroupBean = new CallerIdGroupBean();
		if (setting.getCallerID() == null) {
			callerIdGroupBean.setCallerId("all");
		} else {
			callerIdGroupBean.setCallerId(setting.getCallerID());
		}
		callerIdGroupBean.setStatus(setting.getStatus());
		callerIdGroupBean.setFromTime(String.format("%02d", setting.getFromTime()) + String.format("%02d", setting.getFromTimeMinutes()));
		callerIdGroupBean.setToTime(String.format("%02d", setting.getToTime()) + String.format("%02d", setting.getToTimeMinutes()));
		callerIdGroupBean.setInterval(setting.getSelInterval());
		return callerIdGroupBean;
	}

	public static void addSettingToDownloadBean(ExtendedDownloadBean extDownloadBean,
			Setting setting) {
		String selStatus = setting.getSelectionStatus();
		if (isSelectionActive(selStatus)) {
			if (isSelectionAllCallerDefaultSelection(setting)) {
				extDownloadBean.setSetForAll(true);
			} else {
				List<CallerIdGroupBean> callerIdGroupList = extDownloadBean.getCallerIdGroup();
				if (callerIdGroupList == null) {
					callerIdGroupList = new ArrayList<CallerIdGroupBean>();
					extDownloadBean.setCallerIdGroup(callerIdGroupList);
				}
				CallerIdGroupBean callerIdGroupBean = getCallerIdGroupBean(setting);
				callerIdGroupList.add(callerIdGroupBean);
			}
		}
	}

	public static String getImagePath(Category category,
			ExtendedClipBean clipObj) {
		String imagePath = null;
		if (category != null && isShuffleCategory(category.getCategoryTpe())) {
			imagePath = PropertyConfigurator.getCategoryImagePath(String.valueOf(category.getCategoryId()));
			if (imagePath == null) {
				imagePath = category.getCategoryInfo(CategoryInfoKeys.IMG_URL);
				if (imagePath == null) {
					imagePath = category.getCategoryInfo(CategoryInfoKeys.IMG);
				}
			}
			if (imagePath == null) {
				logger.debug("categoryImagePath is null. Fetching from first clip.");
				Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(category.getCategoryId());
				if (clips != null && clips.length > 0) {
					Clip firstClip = clips[0];
					if (firstClip != null) {
						imagePath = firstClip.getClipInfo(Clip.ClipInfoKeys.IMG_URL);
						logger.debug("categoryImagePath: " + imagePath);
					}
				}
			}
		}
		if (imagePath == null && clipObj != null) {
			imagePath = clipObj.getClipInfo(Clip.ClipInfoKeys.IMG_URL);
			logger.debug("clipImagePath: " + imagePath);
		}
		return imagePath;
	}
	
	public static void writeRegistrationLogs(String circleId, String msisdn,
			String registrationsource, String userAgent) {
		final Logger registrationLogger = Logger.getLogger("REGISTRATION_LOGGER");
		StringBuffer sb = new StringBuffer();
		if (circleId == null) {
			circleId = Utility.getCircleId(msisdn);
		}
		sb.append("\""+circleId);
		sb.append("\",\"").append(msisdn);
		sb.append("\",\"").append(registrationsource);
		sb.append("\",\"").append(userAgent + "\"");
		registrationLogger.info(sb);
	}
}
