/**
 * 
 */
package com.onmobile.apps.ringbacktones.genericcache;

import java.util.ArrayList;
import java.util.List;

import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeSMS;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;

/**
 * @author vinayasimha.patil
 *
 */
public class ChargeSMSCacheManager
{
	/**
	 * 
	 */
	private ChargeSMSCacheManager()
	{

	}

	public ChargeSMS getChargeSMS(String chargeClass, String classType)
	{
		return getChargeSMS(chargeClass, classType, null);
	}

	public ChargeSMS getChargeSMS(String chargeClass, String classType, String language)
	{
		String type = "CHARGE_SMS_" + classType + "_" + chargeClass;

		ChargeSMS chargeSMS = new ChargeSMS();
		chargeSMS.setChargeClass(chargeClass);
		chargeSMS.setClassType(classType);

		RBTText rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, "PREPAID_SUCCESS", language);
		if (rbtText == null)
			return null;

		chargeSMS.setLanguage(rbtText.getLanguage());
		chargeSMS.setPrepaidSuccess(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, "PREPAID_FAILURE", language);
		if (rbtText != null)
			chargeSMS.setPrepaidFailure(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, "POSTPAID_SUCCESS", language);
		if (rbtText != null)
			chargeSMS.setPostpaidSuccess(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, "POSTPAID_FAILURE", language);
		if (rbtText != null)
			chargeSMS.setPostpaidFailure(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, "PREPAID_NEF_SUCCESS", language);
		if (rbtText != null)
			chargeSMS.setPrepaidNEFSuccess(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, "PREPAID_RENEWAL_SUCCESS", language);
		if (rbtText != null)
			chargeSMS.setPrepaidRenewalSuccess(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, "PREPAID_RENEWAL_FAILURE", language);
		if (rbtText != null)
			chargeSMS.setPrepaidRenewalFailure(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, "POSTPAID_RENEWAL_SUCCESS", language);
		if (rbtText != null)
			chargeSMS.setPostpaidRenewalSuccess(rbtText.getText());

		rbtText = CacheManagerUtil.getRbtTextCacheManager().getRBTText(type, "POSTPAID_RENEWAL_FAILURE", language);
		if (rbtText != null)
			chargeSMS.setPostpaidRenewalFailure(rbtText.getText());

		return chargeSMS;
	}

	public List<ChargeSMS> getChargeSMSes(String classType)
	{
		return getChargeSMSes(classType, null);
	}

	public List<ChargeSMS> getChargeSMSes(String classType, String language)
	{
		List<ChargeSMS> chargeSMSList = new ArrayList<ChargeSMS>();
		if (classType.equalsIgnoreCase("SUB"))
		{
			List<SubscriptionClass> subscriptionClassList = CacheManagerUtil.getSubscriptionClassCacheManager().getAllSubscriptionClasses();
			for (SubscriptionClass subscriptionClass : subscriptionClassList)
			{
				ChargeSMS chargeSMS = getChargeSMS(subscriptionClass.getSubscriptionClass(), classType, language);
				if (chargeSMS != null)
					chargeSMSList.add(chargeSMS);
			}
		}
		else
		{
			List<ChargeClass> chargeClassList = CacheManagerUtil.getChargeClassCacheManager().getAllChargeClass();
			for (ChargeClass chargeClass : chargeClassList)
			{
				ChargeSMS chargeSMS = getChargeSMS(chargeClass.getChargeClass(), classType, language);
				if (chargeSMS != null)
					chargeSMSList.add(chargeSMS);
			}
		}

		return chargeSMSList;
	}

	public boolean updateChargeSMS(ChargeSMS chargeSMS)
	{
		String type = "CHARGE_SMS_" + chargeSMS.getClassType() + "_" + chargeSMS.getChargeClass();
		String language = chargeSMS.getLanguage();

		RBTTextCacheManager rbtTextCacheManager = CacheManagerUtil.getRbtTextCacheManager();

		boolean updated = rbtTextCacheManager.updateRBTText(type, "PREPAID_SUCCESS", language, chargeSMS.getPrepaidSuccess());
		updated = rbtTextCacheManager.updateRBTText(type, "PREPAID_FAILURE", language, chargeSMS.getPrepaidFailure());
		updated = rbtTextCacheManager.updateRBTText(type, "POSTPAID_SUCCESS", language, chargeSMS.getPostpaidSuccess());
		updated = rbtTextCacheManager.updateRBTText(type, "POSTPAID_FAILURE", language, chargeSMS.getPostpaidFailure());
		updated = rbtTextCacheManager.updateRBTText(type, "PREPAID_NEF_SUCCESS", language, chargeSMS.getPrepaidNEFSuccess());
		updated = rbtTextCacheManager.updateRBTText(type, "PREPAID_RENEWAL_SUCCESS", language, chargeSMS.getPrepaidRenewalSuccess());
		updated = rbtTextCacheManager.updateRBTText(type, "PREPAID_RENEWAL_FAILURE", language, chargeSMS.getPrepaidRenewalFailure());
		updated = rbtTextCacheManager.updateRBTText(type, "POSTPAID_RENEWAL_SUCCESS", language, chargeSMS.getPostpaidRenewalSuccess());
		updated = rbtTextCacheManager.updateRBTText(type, "POSTPAID_RENEWAL_FAILURE", language, chargeSMS.getPostpaidRenewalFailure());

		return updated;
	}
}
