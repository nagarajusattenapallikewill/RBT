/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.vodacom;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class VodacomRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(VodacomRBTProcessor.class);

	public static String PARAM_MODES_FOR_ADRBT_ACTIVATION_DEACTIVATION = "MODES_FOR_ADRBT_ACTIVATION_DEACTIVATION";
	public static String ADRBT_TO_DEFAULT_SUB_CLASS = "ADRBT_TO_DEFAULT_SUB_CLASS";
	private RBTDBManager rbtDBManager;
	String modesForADRBTActivationDeactivationString = null;
	String ADRBTToDefaultSubClassParam = null;
	List<String> modesForADRBTActivationDeactivationList = null;

	public VodacomRBTProcessor() {
		rbtDBManager = RBTDBManager.getInstance();
		modesForADRBTActivationDeactivationString = getParamAsString(iRBTConstant.PROMOTION, PARAM_MODES_FOR_ADRBT_ACTIVATION_DEACTIVATION, null);
		if (modesForADRBTActivationDeactivationString != null) {
			modesForADRBTActivationDeactivationList = Arrays.asList(modesForADRBTActivationDeactivationString.split(","));
		}
		ADRBTToDefaultSubClassParam = getParamAsString(iRBTConstant.PROMOTION, ADRBT_TO_DEFAULT_SUB_CLASS, "DEFAULT");
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getCos(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber)
	{
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}

	@Override
	public String processActivation(WebServiceContext task) {
		String requestRbtType = task.getString(param_rbtType);
		String mode = task.getString(param_mode);
		if (requestRbtType != null && requestRbtType.equals("1")) {
			if (modesForADRBTActivationDeactivationString != null && (!modesForADRBTActivationDeactivationList.contains(mode))) {
				logger.error("DB Parameter "
						+ VodacomRBTProcessor.PARAM_MODES_FOR_ADRBT_ACTIVATION_DEACTIVATION
						+ " does not contain, mode = "
						+ mode);
				return FAILED;
			}		

		}
		return super.processActivation(task);
	}

	@Override
	public String processDeactivation(WebServiceContext task) {
		String requestRbtType = task.getString(param_rbtType);
		Subscriber subscriber = null;
		try {
			subscriber = DataUtils.getSubscriber(task);
		} catch (RBTException e) {
			logger.error(e, e);
		}
		if (subscriber == null) {
			logger.error("Subcriber is null. Returning now.");
			return FAILED;
		}
		String mode = task.getString(param_mode);
		logger.info("requestRbtType: " + requestRbtType);
		if (requestRbtType != null && requestRbtType.equals("1")) { 		//ADRBT deactivation request
			logger.info("ADRBT deactivation request received");
			if (modesForADRBTActivationDeactivationString != null && (!modesForADRBTActivationDeactivationList.contains(mode))) {
				logger.error("DB Parameter "
						+ PARAM_MODES_FOR_ADRBT_ACTIVATION_DEACTIVATION
						+ " does not conatin, mode = "
						+ mode);
				return FAILED;
			}
			if (subscriber != null && subscriber.rbtType() == 1) { 			//Subscriber is an adrbt subscriber
				logger.info("Subscriber is an adrbt subscriber. i.e. subscriber.rbtType = 1");
				SubscriberDownloads[] downloads = rbtDBManager.getActiveSubscriberDownloads(subscriber.subID());
				if (downloads == null || downloads.length == 0) {			//No downloads exist
					logger.info("No downloads exist. Hence deactivating the base.");
					return super.processDeactivation(task); 
				} else {													//There are downloads. So changing rbtType and subscription Class.
					String subscriptionClass = ADRBTToDefaultSubClassParam;
					logger.info("There are downloads. So changing rbtType to 0 and subscription Class (as configued in DB) to: " + subscriptionClass);
					task.put(param_rentalPack, subscriptionClass);
					task.put(param_rbtType, "0");
					return super.processActivation(task);
				}
			} else {														//Subscriber is not an ADRBT subscriber.
				logger.error("ADRBT deactivation request receieved and the subscriber is not an ADRBT subscriber!");
				return NOT_AN_ADRBT_USER;
			}
		} else if (requestRbtType == null || requestRbtType.equals("0")) { 	//Not an ADRBT deactivation request
			logger.info("Request is not an ADRBT deactivation request.");
			if (subscriber.rbtType() == 1) {								//Subscriber is an adrbt subscriber
				logger.info("Subscriber is an adrbt subscriber. i.e. subscriber.rbtType = 1");
				SubscriberDownloads[] downloads = rbtDBManager.getActiveSubscriberDownloads(subscriber.subID());
				if (downloads == null || downloads.length == 0) {			//No downloads exist
					logger.error("No active downloads exist! As its an adrbt subscriber, cannot deactivate him/her");
					return ADRBT_USER; 
				} else {													//As its an ADRBT subscriber and there are downloads, the downloads will be expired.
					logger.info("Active downloads exist! As its an adrbt subscriber, downloads will be expired now.");
					rbtDBManager.expireAllSubscriberDownload(subscriber.subID(), mode);
					return SUCCESS;
				}
			}
		} 
		return super.processDeactivation(task);
	} 
}