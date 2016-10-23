package com.rbt.webservice.nemo.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.common.debug.DebugManager;
import com.rbt.webservice.nemo.plugin.constants.NemoPluginConstants;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.xml.sax.SAXException;

import net.sf.jxls.reader.ReaderBuilder;
import net.sf.jxls.reader.XLSReader;

public class Utils implements NemoPluginConstants {

	public static HashMap<String, String> constructSubscriptionClassSetterMapping(
			HashMap<String, String> map) {
		map = new HashMap<String, String>();
		map.put("SUBSCRIPTION_CLASS", "subscriptionClass");
		map.put("SUBSCRIPTION_AMOUNT", "subscriptionAmount");
		map.put("RENEWAL_AMOUNT", "renewalAmount");
		map.put("SUBSCRIPTION_PERIOD", "subscriptionPeriod");
		map.put("RENEWAL_PERIOD", "renewalPeriod");
		map.put("SUBSCRIPTION_RENEWAL", "subscriptionRenewal");
		map.put("SMS_ON_SUBSCRIPTION", "smsOnSubscription");
		map.put("SMS_ON_SUBSCRIPTION_FAILURE", "smsOnSubscriptionFailure");
		map.put("SMS_ALERT_BEFORE_RENEWAL", "smsAlertBeforeRenewal");
		map.put("SMS_RENEWAL_SUCCESS", "smsRenewalSuccess");
		map.put("RETRY_PERIOD", "retryPeriod");
		map.put("OPERATOR_CODE1", "operatorCode1");
		map.put("OPERATOR_CODE2", "operatorCode2");
		map.put("OPERATOR_CODE3", "operatorCode3");
		map.put("OPERATOR_CODE4", "operatorCode4");
		map.put("GRACE_PERIOD", "gracePeriod");
		map.put("CHARGE_AFTER_SUBSCRIPTION", "chargeAfterSubscription");
		map.put("SMS_RENEWAL_FAILURE", "smsRenewalFailure");
		map.put("SMS_ALERT_RETRY", "smsAlertRetry");
		map.put("SMS_ALERT_GRACE", "smsAlertGrace");
		map.put("SMS_DEACT_FAILURE", "smsDeactFailure");
		map.put("AUTO_DEACTIVATION_PERIOD", "autoDeactivationPeriod");
		map.put("SHOW_ON_GUI", "showOnGui");
		map.put("FREE_SELECTIONS", "freeSelections");
		map.put("CIRCLE_ID", "circleID");
		return map;
	}

	public static HashMap<String, String> constructChargeClassSetterMapping(
			HashMap<String, String> map) {
		map = new HashMap<String, String>();
		map.put("CLASS_TYPE", "chargeClass");
		map.put("AMOUNT", "amount");
		map.put("OPERATOR_CODE", "operatorCode");
		map.put("PROVIDER_CODE", "providerCode");
		map.put("FREE_SELECTIONS", "freeSelection");
		map.put("SELECTION_TYPE", "selectionType");
		map.put("SELECTION_PERIOD", "selectionPeriod");
		map.put("OPERATORCODE1", "operatorCode1");
		map.put("OPERATORCODE2", "operatorCode2");
		map.put("RENEWAL_PERIOD", "renewalPeriod");
		map.put("RENEWAL_AMOUNT", "renewalAmount");
		map.put("SMS_CHARGE_SUCCESS", "smschargeSuccess");
		map.put("SMS_CHARGE_FAILURE", "smschargeFailure");
		map.put("SMS_RENEWAL_SUCCESS", "smsrenewalSuccess");
		map.put("SMS_RENEWAL_FAILURE", "smsrenewalFailure");
		map.put("SHOW_ON_GUI", "showonGui");
		map.put("CIRCLE_ID", "circleID");
		return map;
	}

	public static HashMap<String, String> constructCosDetailsSetterMapping(
			HashMap<String, String> map) {
		map = new HashMap<String, String>();
		map.put("SUBSCRIPTION_CLASS", "subscriptionClass");
		map.put("COS_ID", "cosId");
		map.put("START_DATE", "startDate");
		map.put("END_DATE", "endDate");
		map.put("CIRCLE_ID", "circleId");
		map.put("PREPAID_YES", "prepaidYes");
		map.put("FREE_CHARGE_CLASS", "freechargeClass");
		map.put("VALID_DAYS", "validDays");
		map.put("FREE_SONGS", "freeSongs");
		map.put("FREE_MUSICBOXES", "freeMusicboxes");
		map.put("RENEWAL_ALLOWED", "renewalAllowed");
		map.put("ACCEPT_RENEWAL", "acceptRenewal");
		map.put("CATEGORY_ID", "categoryId");
		map.put("RENEWAL_COS_ID", "renewalCosid");
		map.put("ACTIVATION_PROMPT", "activationPrompt");
		map.put("SELECTION_PROMPT", "selectionPrompt");
		map.put("SMS_PROMO_CLIPS", "smspromoClips");
		map.put("NUM_SUBSCRIPTIONS_ALLOWED", "numsubscriptionAllowed");
		map.put("IS_DEFAULT", "isDefault");
		map.put("ACCESS_MODE", "accessMode");
		map.put("SMS_KEYWORD", "smsKeyword");
		map.put("OPERATOR", "operator");
		map.put("COS_TYPE", "cosType");
		map.put("CONTENT_TYPES", "contentTypes");
		return map;
	}

	/**
	 * Parses an excel file into a list of beans.
	 * 
	 * @param xlsFile
	 *            the excel data file to parse
	 * @param jxlsConfigFile
	 *            the jxls config file describing how to map rows to beans
	 * @return the list of beans or an empty list there are none
	 * @throws Exception
	 *             if there is a problem parsing the file
	 */
	public static List<?> parseExcelFileToBeans(final File xlsFile,
			final File jxlsConfigFile, final String type) {
		List<?> result = null;
		if (type.equalsIgnoreCase(RBT_TEXT_TYPE)) {
			result = new ArrayList<RBTText>();
		} else if (type.equalsIgnoreCase(PARAM_FILE_TYPE)) {
			result = new ArrayList<Parameters>();
		}
		try {
			final XLSReader xlsReader = ReaderBuilder
					.buildFromXML(jxlsConfigFile);
			final Map<String, Object> beans = new HashMap<String, Object>();
			DebugManager.detail("Inside parseExcelFileToBeans",
					"RBTConfigDevPlugin", "xlsReader" + xlsReader,
					"parseExcelFileToBeans", Thread.currentThread().getName(),
					null);
			beans.put("result", result);
			InputStream inputStream = new BufferedInputStream(
					new FileInputStream(xlsFile));
			DebugManager.detail("Inside parseExcelFileToBeans",
					"RBTConfigDevPlugin", "inputStream" + inputStream,
					"parseExcelFileToBeans", Thread.currentThread().getName(),
					null);
			xlsReader.read(inputStream, beans);
		} catch (InvalidFormatException e) {
		} catch (IOException e) {
		} catch (SAXException e) {
		}
		return result;
	}
}