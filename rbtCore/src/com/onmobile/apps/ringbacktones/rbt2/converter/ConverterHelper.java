package com.onmobile.apps.ringbacktones.rbt2.converter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.livewiremobile.store.storefront.dto.user.Subscription;
import com.livewiremobile.store.storefront.dto.user.Subscription.SubscriptionType;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.SubscriberImpl;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedSubStatus;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.common.SubscriptionStatus;
import com.onmobile.apps.ringbacktones.rbt2.db.ISubscriber;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

@Component(value=BeanConstant.CONVERTER_HELPER_UTIL)
@Lazy(value=true)
public class ConverterHelper {
	
	private static Logger logger = Logger.getLogger(ConverterHelper.class);
	
	public Setting convertSubStatusToSetting(ExtendedSubStatus subscriberStatus)  {
		Setting setting = null;

		if (subscriberStatus != null) {
			Clip clip = null;
			if(subscriberStatus.categoryType() == iRBTConstant.RECORD) {
				try {
					IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
					RBTUgcWavfile ugcWavfile = ugcWavfileDao.getUgcWavFile(Long.parseLong(subscriberStatus.subID()), subscriberStatus.subscriberFile());
					clip = ServiceUtil.getClip(ugcWavfile.getUgcId(), "RBTUGC");

				} catch (NumberFormatException e) {
					logger.error("Exception Occured: "+e, e);
				} catch (DataAccessException e) {
					logger.error("Exception Occured: "+e, e);
				} catch (Exception e) {
					logger.error("Exception Occured: "+e, e);
				}
			} else {
				clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(subscriberStatus.subscriberFile());
			}
			if(clip != null) {
				setting = new Setting();

				setting.setCallerID(subscriberStatus.callerID());
				setting.setCategoryID(subscriberStatus.categoryID());
				setting.setChargeClass(subscriberStatus.classType());
				//setting.setChargingModel(subscriberStatus.);
				setting.setDeselectedBy(subscriberStatus.deSelectedBy());
				//setting.setDeselectionInfo(subscriberStatus.);
				setting.setEndTime(subscriberStatus.endTime());
				setting.setFromTime(subscriberStatus.fromTime());
				//setting.setFromTimeMinutes(subscriberStatus.);
				//setting.setIsCurrentSetting(subscriberStatus.);
				//setting.setLastChargeAmount(subscriberStatus.);
				setting.setLoopStatus(subscriberStatus.loopStatus()+"");
				//setting.setNextBillingDate(subscriberStatus.);
				setting.setNextChargingDate(subscriberStatus.nextChargingDate());
				//setting.setOptInOutModel(subscriberStatus.o);
				setting.setPreviewFile(clip.getClipPreviewWavFile());
				setting.setRbtFile(subscriberStatus.subscriberFile());
				setting.setRefID(subscriberStatus.refID());
				setting.setSelectedBy(subscriberStatus.selectedBy());
				setting.setSelectionInfo(subscriberStatus.selectionInfo());
				setting.setSelectionInfoMap(DBUtility.getAttributeMapFromXML(subscriberStatus.extraInfo()));
				setting.setSelectionStatus(subscriberStatus.selStatus());
				//setting.setSelectionStatusID(subscriberStatus.status()+"");
				setting.setSelectionType(subscriberStatus.selType()+"");
				setting.setSelInterval(subscriberStatus.selInterval());
				setting.setSetTime(subscriberStatus.setTime());
				//setting.setShuffleID(subscriberStatus.);
				setting.setStartTime(subscriberStatus.startTime());
				setting.setStatus(subscriberStatus.status());
				setting.setSubscriberID(subscriberStatus.subID());
				setting.setToneID(clip.getClipId());
				setting.setToneName(clip.getClipName());
				setting.setToTime(subscriberStatus.toTime());
				//setting.setToTimeMinutes(toTimeMinutes);
				if (subscriberStatus.udpId() != null)
					setting.setUdpId(Integer.parseInt(subscriberStatus.udpId()));

			}
		}

		return setting;
	}
	
	public List<Setting> convertSubStatusListToSettingList(
			List<ExtendedSubStatus> extendedSubStatusList) {

		List<Setting> settings = null;
		Map<String, String> udpMap = null;
		if (extendedSubStatusList != null && !extendedSubStatusList.isEmpty()) {
			for (ExtendedSubStatus extendedSubStatus : extendedSubStatusList) {
				if (settings == null)
					settings = new ArrayList<Setting>();

				if (udpMap == null)
					udpMap = new HashMap<String, String>();

				Setting setting = convertSubStatusToSetting(extendedSubStatus);
				
				if(setting.getUdpId() == -1) {
					settings.add(setting);
				} else if(!udpMap.containsKey(setting.getUdpId()+"_"+setting.getCallerID()) && (setting.getLoopStatus().equalsIgnoreCase("O") || setting.getLoopStatus().equalsIgnoreCase("B"))) {
					udpMap.put(setting.getUdpId()+"_"+setting.getCallerID(), setting.getLoopStatus());
					settings.add(setting);
				} 
			}
		}

		return settings;
	}

	/*
	 * Instead of calling this method client should call non Deprected method
	 */
	@Deprecated
	public Subscription convertSubscriberToSubscription(Subscriber subscriber) {
		Subscription subscription = null;
		if (subscriber != null) {
			subscription = new Subscription();
			subscription.setID(Long.parseLong(subscriber.subID()));
			if (subscriber.subscriptionClass() != null)
				subscription.setSrvKey(subscriber.subscriptionClass());
			subscription.setType(SubscriptionType.RINGBACK);
//			subscription.setStatus(SubscriptionStatus.ACTIVE.toString());
			
			String subscriptionYes = subscriber.subYes();
			Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
			
			if(extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.VOLUNTARY) && extraInfoMap.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("TRUE")) {
				subscriptionYes = "UZ";
			}
			
			
			subscription.setStatus(SubscriptionStatus.getSubscriptionStatus(subscriptionYes));
			
			String circle = subscriber.circleID();
			String[] circle_operator = null;
			String operatorName = subscriber.operatorName();
			if (operatorName != null && !operatorName.isEmpty()) {
				subscription.setOperator(operatorName);
				subscription.setCircle(circle);
			} else {
				if (circle != null) {
					circle_operator = circle.split("_");

				}
				if (circle_operator != null && circle_operator.length == 2) {
					subscription.setOperator(circle_operator[0]);
					subscription.setCircle(circle_operator[1]);
				} else if (circle != null) {
					subscription.setOperator(ConsentPropertyConfigurator.getOperatorFormConfig());
					subscription.setCircle(circle);
				} else {
					logger.info("No circle found  for fro subscriber:" + subscriber.subID());
				}}
		}
		return subscription;
	}
	
	/*
	 * Instead of calling this method client should call non Deprected method
	 */
	@Deprecated
	public Subscription convertSubscriberToSubscriptionObj(Subscriber subscriber) {
		Subscription subscription = null;
		if (subscriber != null) {
			subscription = new Subscription();
			
			Date serviceStartDate = new Date();
			Date serviceEndDate = new Date();
			if(subscriber.nextChargingDate() != null){
				serviceStartDate = subscriber.nextChargingDate();
			}
			
			if(subscriber.prismNextBillingDate() != null) {
				serviceEndDate = subscriber.prismNextBillingDate();
			}
		
			subscription.setStartDate(serviceStartDate);
			subscription.setEndDate(serviceEndDate);
			
			
			subscription.setID(Long.parseLong(subscriber.subID()));
			if (subscriber.subscriptionClass() != null){
				subscription.setSrvKey(subscriber.subscriptionClass());
				
				SubscriptionClass subscriptionClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subscriber.subscriptionClass());
				if(subscriptionClass != null && subscriptionClass.getRenewalAmount() != null && subscriptionClass.getRenewalAmount().trim().equals("0")){
				       subscription.setAutomaticRenewal(false);
				}else{
					subscription.setAutomaticRenewal(true);
				}
				
			}
			subscription.setType(SubscriptionType.RINGBACK);
			
			String subscriptionYes = subscriber.subYes();
			Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(subscriber.extraInfo());
			
			if(extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.VOLUNTARY) && extraInfoMap.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("TRUE")) {
				subscriptionYes = "UZ";
			}
			
			if(extraInfoMap != null && extraInfoMap.containsKey("DELAY_DEACT") && extraInfoMap.get("DELAY_DEACT").equalsIgnoreCase("TRUE")) {
				subscriptionYes = "UX";
			}
			ISubscriber iSubscriber = (ISubscriber) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_IMPL);
			Subscriber subs = iSubscriber.getSubscriber(subscriber.subID());
			if(subs != null) {
				subscription.setStatus(SubscriptionStatus.getSubscriptionStatus(subs.subYes()));
			} else {
				Consent consentObj = iSubscriber.getConsentObject(subscriber.subID());
				if(consentObj != null) {
					subscription.setStatus("Consent_Pending");
				} else {
					subscription.setStatus(SubscriptionStatus.getSubscriptionStatus(subscriptionYes));
				}
			}
			//Airtel_bangalore
			String circle = subscriber.circleID();
			String[] circle_operator = null;
			String operatorName = subscriber.operatorName();
			if (operatorName != null && !operatorName.isEmpty()) {
				subscription.setOperator(operatorName);
				subscription.setCircle(circle);
			} else {

				if (circle != null) {
					circle_operator = circle.split("_");

				}
				if (circle_operator != null && circle_operator.length == 2) {
					subscription.setOperator(circle_operator[0]);
					subscription.setCircle(circle_operator[1]);
				} else if (circle != null) {
					subscription.setOperator(ConsentPropertyConfigurator.getOperatorFormConfig());
					subscription.setCircle(circle);
				} else {
					logger.info("No circle found  for fro subscriber:" + subscriber.subID());
				}
			}
			
			
		}
		return subscription;
	}
	
	public Subscription convertSubscriberToSubscription(com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber) {
		Subscription subscription = null;
		if (subscriber != null) {
			subscription = new Subscription();
			subscription.setID(Long.parseLong(subscriber.getSubscriberID()));
			if (subscriber.getSubscriptionClass() != null)
				subscription.setSrvKey(subscriber.getSubscriptionClass());
			subscription.setType(SubscriptionType.RINGBACK);
			//subscription.setStatus(SubscriptionStatus.ACTIVE.toString());
			
			//String subscriptionYes = subscriber.getSub();
			/*Map<String, String> extraInfoMap = subscriber.getUserInfoMap();
			
			if(extraInfoMap != null && extraInfoMap.containsKey(iRBTConstant.VOLUNTARY) && extraInfoMap.get(iRBTConstant.VOLUNTARY).equalsIgnoreCase("TRUE")) {
				subscriptionYes = "UZ";
			}*/
			ISubscriber iSubscriber = (ISubscriber) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_IMPL);
			Subscriber subs = iSubscriber.getSubscriber(subscriber.getSubscriberID());
			if(subs != null) {
				subscription.setStatus(SubscriptionStatus.getSubscriptionStatus(subs.subYes()));
			} else {
				Consent consentObj = iSubscriber.getConsentObject(subscriber.getSubscriberID());
				if(consentObj != null) {
					subscription.setStatus("Consent_Pending");
					subscription.setSrvKey(consentObj.getSubClass());
				} else {
					subscription.setStatus(subscriber.getStatus());
				}
			}	
			
			String circle = subscriber.getCircleID();
			String[] circle_operator = null;
			String operatorName = subscriber.getOperatorName();
			if (operatorName != null && !operatorName.isEmpty()) {
				subscription.setOperator(operatorName);
				subscription.setCircle(circle);
			} else {

				if (circle != null) {
					circle_operator = circle.split("_");

				}
				if (circle_operator != null && circle_operator.length == 2) {
					subscription.setOperator(circle_operator[0]);
					subscription.setCircle(circle_operator[1]);
				} else if (circle != null) {
					subscription.setOperator(ConsentPropertyConfigurator.getOperatorFormConfig());
					subscription.setCircle(circle);
				} else {
					logger.info("No circle found  for fro subscriber:" + subscriber.getSubscriberID());
				}
			}
			
			subscription.setOperatorUserType(subscriber.getOperatorUserType());
			if ((subscriber.getOperatorUserType().equals(OperatorUserTypes.LEGACY.getDefaultValue()))
					|| (subscriber.getOperatorUserType()
							.equals(OperatorUserTypes.LEGACY_FREE_TRIAL.getDefaultValue())))
				subscription.setStatus(Constants.NEW_USER);
			
			if(subscription.getStatus().equalsIgnoreCase(Constants.NEW_USER))
			{
				subscription.setSrvKey("FREE");
			}
				
		}
		
		return subscription;
	}

	public Subscriber convertClientSubscriberToContentSubscriber(
			com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber clientsubscriber) {
		logger.info(":---> INSIDE CONVERTER");
		SubscriberImpl contentSubscriber = null;
		if (clientsubscriber != null) {
			String subscriberID = clientsubscriber.getSubscriberID();
			String activateBy = clientsubscriber.getActivatedBy();
			String deactivatedBy = clientsubscriber.getDeactivatedBy();
			Date startDate = clientsubscriber.getStartDate();
			Date endDate = clientsubscriber.getEndDate();
			String prepaid = clientsubscriber.isPrepaid() ? "y" : "n";
			Date accessDate = null;
			Date nextChargingDate = clientsubscriber.getNextChargingDate();
			int access = 1;
			String info = clientsubscriber.getActivationInfo();
			String subscriptionClass = clientsubscriber.getSubscriptionClass();
			String status = clientsubscriber.getStatus();
			String subscription = null;
			String lastDeactivationInfo = clientsubscriber.getLastDeactivationInfo();
			Date lastDeactivationDate = clientsubscriber.getLastDeactivationDate();
			Date activationDate = clientsubscriber.getActivationDate();
			String old_class_type = clientsubscriber.getSubscriptionClass();
			int maxSelections = clientsubscriber.getNumMaxSelections();
			String cosID = clientsubscriber.getCosID();
			String activatedCosID = clientsubscriber.getCosID();
			int rbt_type = 0;

			if (null != clientsubscriber.getUserType()) {

				if (clientsubscriber.getUserType().equalsIgnoreCase("corporate"))
					rbt_type = 2;
				else if (clientsubscriber.getUserType().equalsIgnoreCase("ad_rbt"))
					rbt_type = 1;
				else
					rbt_type = 0;

			}

			String language = clientsubscriber.getLanguage();
			String extraInfo = DBUtility.getAttributeXMLFromMap(clientsubscriber.getUserInfoMap());
			String circleID = clientsubscriber.getCircleID();
			String refID = clientsubscriber.getRefID();

			if (status.equals("active")) {
				subscription = iRBTConstant.STATE_ACTIVATED;
			} else if (status.equals("deactive")) {
				subscription = iRBTConstant.STATE_DEACTIVATED;
			} else if (status.equals("act_pending")) {
				subscription = iRBTConstant.STATE_ACTIVATION_PENDING;
			} else if (status.equals("deact_pending")) {
				subscription = iRBTConstant.STATE_DEACTIVATION_PENDING;
			} else if (status.equals("grace")) {
				subscription = iRBTConstant.STATE_GRACE;
			} else if (status.equals("suspended")) {
				subscription = iRBTConstant.STATE_SUSPENDED;
			} else {
				return contentSubscriber;
			}

			contentSubscriber = new SubscriberImpl(subscriberID, activateBy, deactivatedBy, startDate, endDate, prepaid,
					accessDate, nextChargingDate, access, info, subscriptionClass, subscription, lastDeactivationInfo,
					lastDeactivationDate, activationDate, maxSelections, cosID, activatedCosID, rbt_type, language,
					old_class_type, extraInfo, circleID, refID);

			contentSubscriber.setSubYes(subscription);
			logger.info("\n\n\t:---> contentSubscriber " + contentSubscriber + "\n\n");

		}
		return contentSubscriber;

	}

}
