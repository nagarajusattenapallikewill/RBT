package com.onmobile.apps.ringbacktones.webservice.client;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ComvivaConsent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class SaxHandler extends DefaultHandler {

	private Settings settings = null;
	private Setting setting = null;
	private List<Setting> settingList = null;
	private ComvivaConsent comvivaConsent = null;
	private Setting[] settingArr = null;
	private SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	private String codeValue = null;
	private String subscriptionDate = null;
	private String lastRenewalDate = null;
	private String nextChargingDate = null;
	private String numOfDays = null;
	private String userType = null;
	private String lastSongChangeDate = null;
	private RBTCacheManager rbtCacheManager = null;
	private Subscriber subscriber = null;
	private String subscriberId = null;
	private static Logger logger = Logger.getLogger(SaxHandler.class);
	
	public SaxHandler(){
		super();
	}
	
	public SaxHandler(String subscriberId) {
		super();
		this.subscriberId = subscriberId;		
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {
		
		
		if(qName.equalsIgnoreCase("settings")) {
			String noOfDefaultSettings = attributes.getValue("no_of_default_cnt");
			String noOfSpecialSettings = attributes.getValue("no_of_special_cnt");
			
			subscriptionDate = attributes.getValue("subsciption_date");
			lastRenewalDate = attributes.getValue("last_renewal_date");
			nextChargingDate = attributes.getValue("next_charging_date");
			numOfDays = attributes.getValue("no_of_days");
			userType = attributes.getValue("user_type");
			lastSongChangeDate = attributes.getValue("last_song_change_date");
			
			settings = new Settings();
			settings.setNoOfDefaultSettings(Integer.parseInt(noOfDefaultSettings));
			settings.setNoOfSpecialSettings(Integer.parseInt(noOfSpecialSettings));
			
			
			String noOfCount = attributes.getValue("no_of_cnt");
			int iNoOfCount = -2;
			try{
				iNoOfCount = Integer.parseInt(noOfCount);
			}
			catch(NumberFormatException e) {}
			
			//Construct subscriberObject
			subscriber = getSubscriber(iNoOfCount);
			
		} else if(qName.equalsIgnoreCase("content")) {
			String toneName = attributes.getValue("name");			
			String toneType = attributes.getValue("type");
			
			setting = new Setting();
			setting.setToneName(toneName);			
			setting.setLoopStatus("B");
			setting.setCategoryID(3);			
			setting.setToneType(toneType);
			setting.setFromTime(0);
			setting.setFromTimeMinutes(0);
			setting.setToTime(23);
			setting.setToTimeMinutes(59);
			setting.setStatus(1);
			setting.setSelectedBy("COMVIVA");
			setting.setSelectionStatus("ACTIVE");
			try {
				setting.setStartTime(formatter.parse(lastRenewalDate));
				setting.setNextChargingDate(formatter.parse(lastRenewalDate));
				setting.setEndTime(formatter.parse(nextChargingDate));
				setting.setNextBillingDate(formatter.parse(nextChargingDate));
			} catch (ParseException e) {
				logger.error("Exception Occured: " + e, e);
			}
		} else if(qName.equalsIgnoreCase("property")) {
			if(attributes.getValue("name").equalsIgnoreCase("caller_id")) {
				String callerId = attributes.getValue("value");
				setting.setCallerID(callerId);
			}  else if(attributes.getValue("name").equalsIgnoreCase("preview_file")) {
				String previewFile = attributes.getValue("value");
				String rbtWavFileName = previewFile.trim().substring(0, previewFile.length()-4);
				setting.setRbtFile(rbtWavFileName);
				if(rbtCacheManager == null)
					rbtCacheManager = RBTCacheManager.getInstance();
				Clip clip = rbtCacheManager.getClipByRbtWavFileName(rbtWavFileName);
				if(clip != null) {
					setting.setToneID(clip.getClipId());
				}
				setting.setPreviewFile(previewFile);
			}
		} else if(qName.equalsIgnoreCase("parameters")) {
			
			
			if(comvivaConsent == null) {
				comvivaConsent = new ComvivaConsent();
				 comvivaConsent.setHandleHTNewActivation(attributes.getValue("mth"));
				 comvivaConsent.setMsisdn(attributes.getValue("m"));
				 comvivaConsent.setSongname(attributes.getValue("son"));
				 comvivaConsent.setSongProdId(attributes.getValue("sopid"));
				 comvivaConsent.setSubProdId(attributes.getValue("Sbid"));
				 comvivaConsent.setSongVcode(attributes.getValue("sovc"));
				 comvivaConsent.setSongCpID(attributes.getValue("soci"));
				 comvivaConsent.setSongCopyRightID(attributes.getValue("crid"));
				 comvivaConsent.setGiftReceiverMsisdn(attributes.getValue("mr"));
				 comvivaConsent.setDedicateeMsisdn(attributes.getValue("md"));
				 comvivaConsent.setCopySongMsisdn(attributes.getValue("mc"));
				 comvivaConsent.setSubsOrSongPricePointPricePoint(attributes.getValue("pp"));
				 comvivaConsent.setSubsOrSongPriceUnit(attributes.getValue("pu"));
				 comvivaConsent.setSubsOrTonePricevalidity(attributes.getValue("pv"));
				 comvivaConsent.setSongCategoryName(attributes.getValue("soc"));
				 comvivaConsent.setCpTransactionId(attributes.getValue("cpt"));
				 comvivaConsent.setOpt1(attributes.getValue("opt1"));
				 comvivaConsent.setOpt2(attributes.getValue("opt2"));
				 comvivaConsent.setOpt3(attributes.getValue("opt3"));
				 comvivaConsent.setOpt4(attributes.getValue("opt4"));
				 comvivaConsent.setOpt5(attributes.getValue("opt5"));
			}
		}  else if(qName.equalsIgnoreCase("errorcode")) {
			codeValue = attributes.getValue("value");
		}
	}


	private Subscriber getSubscriber(int iNoOfCount) {
		
		codeValue = "0";
		Subscriber sub = new Subscriber();
		sub.setSubscriberID(subscriberId);
		if(iNoOfCount == -130) {
			sub.setStatus(WebServiceConstants.NEW_USER);
		}
		else if(iNoOfCount == -1){
			codeValue = "-1";
			return null;
		}
		else{
			sub.setStatus(WebServiceConstants.ACTIVE);
		}
		
		try {
			sub.setStartDate(formatter.parse(subscriptionDate));
			sub.setEndDate(formatter.parse(lastRenewalDate));
			sub.setNextBillingDate(formatter.parse(nextChargingDate));
		} catch (ParseException e) {
			logger.error("Parsing Exception:: " + e);
		} catch (Exception e){
			logger.error("Exception:: " + e);
		}
		
		
		return sub;
	}


	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if(qName.equalsIgnoreCase("content")) {
			if(settingList == null)
				settingList = new ArrayList<Setting>();
			settingList.add(setting);
		} 
	}
	
	public Settings getSettings() {
		if(settingArr == null && settingList !=null) {
			settingArr = settingList.toArray(new Setting[settingList.size()]);
			settings.setSettings(settingArr);
		}
		return settings;
	}
	
	public ComvivaConsent getComvivaConsent() {
		return comvivaConsent;
	}

	public String getCodeValue() {
		return codeValue;
	}


	@Override
	public void characters(char ch[], int start, int length) throws SAXException {

	}

	public Subscriber getSubscriber() {
		return subscriber;
	}
}
