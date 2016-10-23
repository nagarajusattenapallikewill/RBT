package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.mnp.MnpService;
import com.onmobile.mnp.MnpServiceFactory;
import com.onmobile.mnp.dataStore.Circle;


public class MnpStorefrontImpl implements MSISDNServiceDefinition {

	private static Logger logger = Logger.getLogger(MnpStorefrontImpl.class);
	private static Map<String, String> commonParams = new HashMap<String, String>();
	private static Map<String, String> gathererParams = new HashMap<String, String>();
	private static Map<String, String[]> circleIdToSitePrefixMap = new HashMap<String, String[]>();
	private static Map<String, String> circleIdToSiteUrlMap = new HashMap<String, String>();

	static {
		RBTClient rbtClient = RBTClient.getInstance();
		ApplicationDetailsRequest applicationDetailsRequest = new ApplicationDetailsRequest();
		applicationDetailsRequest.setType("COMMON");
		Parameter[] params = rbtClient.getParameters(applicationDetailsRequest);
		if (params != null) {
			for (Parameter param : params) {
				commonParams.put(param.getName(), param.getValue());
			}
		}
		applicationDetailsRequest.setType("GATHERER");
		params = rbtClient.getParameters(applicationDetailsRequest);
		if (params != null) {
			for (Parameter param : params) {
				gathererParams.put(param.getName(), param.getValue());
			}
		}
		applicationDetailsRequest.setType(null);
		Site[] sites = rbtClient.getSites(applicationDetailsRequest);
		if (sites != null) {
			for (Site site : sites) {
				circleIdToSitePrefixMap.put(site.getCircleID(), site.getSitePrefixes());
				circleIdToSiteUrlMap.put(site.getCircleID(), site.getSiteURL());
			}
		}
	}

	@Override
	public SubscriberDetail getSubscriberDetail(MNPContext mnpContext) {
		String subscriberID = trimCountryPrefix(mnpContext.getSubscriberID());
		String circleID = null;
		boolean isPrepaid = false;
		boolean isValidSubscriber = false;
		HashMap<String, String> subscriberDetailsMap = null;
		if(!isValidNumber(subscriberID)) {
			return new SubscriberDetail(subscriberID,circleID,isPrepaid,isValidSubscriber,subscriberDetailsMap);
		}

		//Area Code to be in the form of 11:9,12:8,13:10 etc. i.e. areaCode:length
		String areaCodeForPhoneNumberLength = commonParams.get("AREA_CODE_FOR_PHONE_NUMBER_LENGTH");
		if(areaCodeForPhoneNumberLength != null && subscriberID != null){
			if(areaCodeForPhoneNumberLength != null){
				String token[] = areaCodeForPhoneNumberLength.split(",");
				for(int i = 0; i < token.length; i++){
					String areaCodeToken[] = token[i].split(":");
					if(subscriberID.startsWith(areaCodeToken[0])){
						try{
							int areaCodeLength = Integer.parseInt(areaCodeToken[1]);
							if(subscriberID.length()== (areaCodeLength+areaCodeToken[0].length())) {
								subscriberID = subscriberID.substring(areaCodeToken[0].length());
							}
						}catch(Exception e){
							e.printStackTrace();
						}
					}
				}
			}
		}

		String customerName = commonParams.get("CUSTOMER_NAME");

		boolean isOnlineDip = false;
		String isOnlineDipString = null; 
		if(mnpContext.isOnlineDip()) {
			isOnlineDip = true;
		} else {
			isOnlineDipString = commonParams.get("MNP_ONLINE_DIP");
			if (isOnlineDipString != null) {
				isOnlineDip = isOnlineDipString.equalsIgnoreCase("TRUE");
			}
		}

		try {
			MnpServiceFactory  mnpServiceFactory = MnpServiceFactory.getInstance();
			MnpService  mnpService = mnpServiceFactory.getMnpService();
			Circle circle = mnpService.getCircle(customerName, subscriberID, isOnlineDip);
			if (circle != null) {
				int mnpCircleID = circle.getCircleId();
				circleID = getMappedCircleID(String.valueOf(mnpCircleID));

				if (circleID != null) {
					circleID = circleID.toLowerCase();
					String[] sitePrefixes = circleIdToSitePrefixMap.get(circleID);
					String siteUrl = circleIdToSiteUrlMap.get(circleID);
					if (sitePrefixes != null && siteUrl == null) {
						isValidSubscriber = true;
					}
				}
				logger.debug("Info obtained from MNP - Circle: " + circle);
			}
			else {
				logger.info("Circle ID not found in MNP");
			}
		}
		catch (Exception e) {
			logger.error("", e);
		}

		String defaultUserType = commonParams.get("DEFAULT_USER_TYPE");
		if (defaultUserType != null) {
			isPrepaid = defaultUserType.equalsIgnoreCase("PREPAID");
		} else {
			isPrepaid = false; //Postpaid
		}

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID,
				circleID, isPrepaid, isValidSubscriber, subscriberDetailsMap);
		logger.info("RBT:: subscriberDetail: " + subscriberDetail);

		return subscriberDetail;
	}

	private String trimCountryPrefix(String subscriberID) {
		if (subscriberID != null) {
			try {
				String countryCodePrefix = null;;
				int minPhoneNumberLen = 10;

				countryCodePrefix = commonParams.get("COUNTRY_PREFIX");
				if (countryCodePrefix == null || countryCodePrefix.trim().equals("")) {
					countryCodePrefix = "91";
				}
				try {
					minPhoneNumberLen =  Integer.parseInt(commonParams.get("MIN_PHONE_NUMBER_LEN"));
				} catch (Exception e) {
					minPhoneNumberLen = 10;
				}

				String[] countryCodePrefixes = countryCodePrefix.split(",");
				for (String prefix : countryCodePrefixes) {
					if (subscriberID.startsWith("00")) {
						subscriberID = subscriberID.substring(2);
					}
					if (subscriberID.startsWith("+")
							|| subscriberID.startsWith("0")
							|| subscriberID.startsWith("-")) {
						subscriberID = subscriberID.substring(1);
					}
					if (subscriberID.startsWith(prefix)
							&& (subscriberID.length() >= (minPhoneNumberLen + prefix.length()))) {
						subscriberID = subscriberID.substring(prefix.length());
						break;
					}
				}
			} finally {
				if (subscriberID.startsWith("00")) {
					subscriberID = subscriberID.substring(2);
				}
				if (subscriberID.startsWith("+")
						|| subscriberID.startsWith("0")
						|| subscriberID.startsWith("-")) {
					subscriberID = subscriberID.substring(1);
				}
			}
		}
		return subscriberID;
	}

	private boolean isValidNumber(String subscriberID) {
		subscriberID = trimCountryPrefix(subscriberID);

		int minPhoneNumberLength;
		try {
			minPhoneNumberLength =  Integer.parseInt(gathererParams.get("PHONE_NUMBER_LENGTH_MIN"));
		} catch (Exception e) {
			minPhoneNumberLength = 10;
		}
		int maxPhoneNumberLength;
		try {
			maxPhoneNumberLength =  Integer.parseInt(gathererParams.get("PHONE_NUMBER_LENGTH_MAX"));
		} catch (Exception e) {
			maxPhoneNumberLength = 10;
		}

		//Area Code to be in the form of 11:9,12:8,13:10 etc. i.e. areaCode:length
		String areaCodeForPhoneNumberLength = commonParams.get("AREA_CODE_FOR_PHONE_NUMBER_LENGTH");
		if(areaCodeForPhoneNumberLength != null && subscriberID != null){
			if(areaCodeForPhoneNumberLength != null){
				String token[] = areaCodeForPhoneNumberLength.split(",");

				for(int i = 0; i < token.length; i++){
					String areaCodeToken[] = token[i].split(":");
					if(subscriberID.startsWith(areaCodeToken[0])){
						try {
							int areaCodeLength = Integer.parseInt(areaCodeToken[1]);
							if (subscriberID.length() == (areaCodeLength + areaCodeToken[0].length())) {
								subscriberID = subscriberID.substring(areaCodeToken[0].length());
								if (subscriberID.length() == Integer.parseInt(areaCodeToken[1])) {
									return true;
								} else {
									return false;
								}
							}
						} catch(Throwable e){
							return false;	    	
						}
					}
				}
			}
		}
		if (subscriberID == null || subscriberID.length() < minPhoneNumberLength || subscriberID.length() > maxPhoneNumberLength) {
			return false;
		}
		try {
			Long.parseLong(subscriberID);
		} catch (Throwable e) {
			return false;
		}
		return true;
	}

	private String getMappedCircleID(String circleID) {
		String thirdPartyCircleMaps = commonParams.get("THIRD_PARTY_CIRCLE_MAPS");
		if (thirdPartyCircleMaps != null) {
			String[] circleIDMaps = thirdPartyCircleMaps.split(",");
			for (String circleIDMap : circleIDMaps) {
				int index = circleIDMap.indexOf(":");
				String thirdPartyCircleID = circleIDMap;
				String rbtCircleID = circleIDMap;
				if (index != -1) {
					thirdPartyCircleID = circleIDMap.substring(0, index);
					rbtCircleID = circleIDMap.substring(index + 1);
				}
				if (thirdPartyCircleID.equals(circleID)) {
					return rbtCircleID;
				}
			}
		}
		return null;
	}
}