package com.rbt.webservice.nemo.plugin.implementation;

import java.io.File;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.nemo.content.CBinary;
import com.onmobile.apps.nemo.content.CData;
import com.onmobile.apps.nemo.content.Content;
import com.onmobile.apps.nemo.content.ContentManager;
import com.onmobile.apps.nemo.daemon.ContentDaemonPlugin;
import com.onmobile.apps.nemo.distribution.DistributionManager;
import com.onmobile.apps.nemo.distribution.DistributionRelease;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.RBTText;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.common.db.OnMobileDBServices;
import com.onmobile.common.debug.DebugManager;
import com.onmobile.common.exception.OnMobileException;
import com.onmobile.common.serviceparameters.utils.SPInteractor;
import com.rbt.webservice.nemo.plugin.constants.NemoPluginConstants;
import com.rbt.webservice.nemo.utils.Utils;

public class RBTConfigDevPlugin extends Utils implements ContentDaemonPlugin,
		NemoPluginConstants {

	private static HashMap<String, String> SubscriptionClassColumnToSetterMapping = null;
	private static HashMap<String, String> ChargeClassColumnToSetterMapping = null;
	private static HashMap<String, String> CosDetailsColumnToSetterMapping = null;

	static Logger logger = Logger.getLogger(RBTConfigDevPlugin.class);

	static {
		SubscriptionClassColumnToSetterMapping = constructSubscriptionClassSetterMapping(SubscriptionClassColumnToSetterMapping);
		DebugManager.detail("SubscriptionClassColumnToSetterMapping: "
				+ SubscriptionClassColumnToSetterMapping, "RBTConfigDevPlugin",
				"StaticBlock", "Initialize..",
				Thread.currentThread().getName(), null);

		ChargeClassColumnToSetterMapping = constructChargeClassSetterMapping(ChargeClassColumnToSetterMapping);
		DebugManager.detail("ChargeClassColumnToSetterMapping: "
				+ ChargeClassColumnToSetterMapping, "RBTConfigDevPlugin",
				"StaticBlock", "StartedProcess..", Thread.currentThread()
						.getName(), null);
		CosDetailsColumnToSetterMapping = constructCosDetailsSetterMapping(CosDetailsColumnToSetterMapping);
		DebugManager.detail("CosDetailsColumnToSetterMapping: "
				+ CosDetailsColumnToSetterMapping, "RBTConfigDevPlugin",
				"StaticBlock", "Initialize..",
				Thread.currentThread().getName(), null);
		CacheManagerUtil.getBeanFactory();
	}

	@Override
	public void startProcessing(String customer, String site,
			String application, Date schedule, String status,
			long files_downloaded, long tries) {
		try {
			DebugManager.detail("startProcessing", "RBTConfigDevPlugin",
					"startProcessing", "StartedProcess..", Thread
							.currentThread().getName(), null);
			ContentManager cm = ContentManager.getInstance();
			Connection con = null;
			try {
				con = OnMobileDBServices.getDBConnection(SPInteractor
						.getParameter("APP", "GLOBAL", "NEMO_DB_URL"));
			} catch (OnMobileException e) {
				DebugManager.exception("rbtNemoPlugin", "PluginThread",
						"Exception occured", e, Thread.currentThread()
								.getName(), null);
			}
			try {
				DistributionRelease[] release = DistributionManager
						.getInstance().getDistributionRelease(con,
								customer + "-" + site, customer, site,
								application, schedule);
				int releaseCount = 0;
				while (release != null && releaseCount < release.length) {
					Content content = cm.getContent(con,
							release[releaseCount].getReleaseId());
					Content[] contents = null;
					DebugManager.detail(
							"content.isGroup(): " + content.isGroup(),
							"RBTConfigDevPlugin", "Inside StartProcessing",
							"StartedProcess..", Thread.currentThread()
									.getName(), null);
					if (content.isGroup()) {
						contents = content.getAllContent(con);
						DebugManager.detail("Length of contents: "
								+ contents.length, "RBTConfigDevPlugin",
								"Inside StartProcessing", "StartedProcess..",
								Thread.currentThread().getName(), null);
					} else {
						contents = new Content[1];
						contents[0] = content;
					}
					DebugManager.detail("contents in the Group or Contents: "
							+ contents, "RBTConfigDevPlugin",
							"Inside StartProcessing", "StartedProcess..",
							Thread.currentThread().getName(), null);
					processContent(contents, con, content);
					releaseCount++;
				}

			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (Throwable th) {
			DebugManager.exception("RBTConfigDevPlugin",
					"ExeceptionStartProcessing", "Exception occured:", th,
					Thread.currentThread().getName(), null);
		}
	}

	private void processContent(Content[] contents, Connection con,
			Content parentContent) {
		int count = 0;
		String contentType = "";
		String parentContentType = parentContent.getContentType();
		while (contents != null && count < contents.length) {
			DebugManager.detail("Start:processContent", "RBTConfigDevPlugin",
					"Inside ProcessContent",
					"CONTENT_ID:" + contents[count].getContentId()
							+ " COMMENTS:" + contents[count].getComments(),
					Thread.currentThread().getName(), null);
			contentType = contents[count].getContentType();
			DebugManager.detail("contentType: " + contentType,
					"RBTConfigDevPlugin", "Inside ProcessContent",
					"CONTENT_ID:" + contents[count].getContentId()
							+ " COMMENTS:" + contents[count].getComments(),
					Thread.currentThread().getName(), null);
			if (null != contentType && !contentType.isEmpty()) {
				if (contentType.equalsIgnoreCase(RBT_SUBSCRIPTION_CLASS_TYPE)) {
					DebugManager.detail(
							"contentType: " + contentType,
							"RBTConfigDevPlugin",
							"Inside processContent",
							"CONTENT_ID:" + contents[count].getContentId()
									+ " COMMENTS:"
									+ contents[count].getComments(), Thread
									.currentThread().getName(), null);
					process_RbtSubscriptionClass_Content(contents[count], con,
							SUBSCRIPTIONCLASS);
				} else if (contentType.equalsIgnoreCase(RBT_CHARGE_CLASS_TYPE)) {
					DebugManager.detail(
							"contentType: " + contentType,
							"RBTConfigDevPlugin",
							"Inside processContent",
							"CONTENT_ID:" + contents[count].getContentId()
									+ " COMMENTS:"
									+ contents[count].getComments(), Thread
									.currentThread().getName(), null);
					process_RbtChargeClass_Content(contents[count], con,
							CHARGECLASS);
				} else if (contentType.equalsIgnoreCase(RBT_PARAMETERS_TYPE)) {
					DebugManager.detail(
							"contentType: " + contentType,
							"RBTConfigDevPlugin",
							"Inside processContent",
							"CONTENT_ID:" + contents[count].getContentId()
									+ " COMMENTS:"
									+ contents[count].getComments(), Thread
									.currentThread().getName(), null);
					Content content = contents[count];
					if (content != null) {
						CBinary[] binaries = null;
						int dataCnt = 0;
						try {
							binaries = content.getBinaries(con);
						} catch (SQLException e) {
							DebugManager.exception("RBTConfigDevPlugin",
									"Inside processContent",
									"Exception occured:", e, Thread
											.currentThread().getName(), null);
						}
						processRBTParameters(dataCnt, binaries);
					}
				} else if (contentType.equalsIgnoreCase(RBT_COS_DETAIL_TYPE)) {
					DebugManager.detail(
							"contentType: " + contentType,
							"RBTConfigDevPlugin",
							"Inside processContent",
							"CONTENT_ID:" + contents[count].getContentId()
									+ " COMMENTS:"
									+ contents[count].getComments(), Thread
									.currentThread().getName(), null);
					process_CosDetails_Content(contents[count], con,
							COSDETAILSCLASS);

				} else if (contentType.equalsIgnoreCase(RBT_SMS_TYPE)) {
					Content content = contents[count];
					if (content != null) {
						CBinary[] binaries = null;
						int dataCnt = 0;
						try {
							binaries = content.getBinaries(con);
						} catch (SQLException e) {
							DebugManager.exception("RBTConfigDevPlugin",
									"InsideprocessContent",
									"Exception occured:", e, Thread
											.currentThread().getName(), null);
						}
						processRBTText(dataCnt, binaries);
					}
				}
			}
			count++;
		}
		if (parentContentType.equalsIgnoreCase(RBT_COS_DETAIL_TYPE)) {
			DebugManager.detail("parentContentType: " + parentContentType,
					"RBTConfigDevPlugin", "Inside processContent",
					"CONTENT_ID:" + parentContent.getContentId() + " COMMENTS:"
							+ parentContent.getComments(), Thread
							.currentThread().getName(), null);
			process_CosDetails_Content(parentContent, con, COSDETAILSCLASS);

		}
		DebugManager.detail("End:processContent", "RBTConfigDevPlugin",
				"EndprocessContent", "Content Count:" + count, Thread
						.currentThread().getName(), null);
	}

	@SuppressWarnings("unchecked")
	private void process_CosDetails_Content(Content content, Connection con,
			String className) {
		DebugManager.detail("Start process_CosDetails_Content",
				"RBTConfigDevPlugin", "StartProcessing",
				"Processing_CosDetails_Content", Thread.currentThread()
						.getName(), null);
		CosDetails constructedCosDetailsObject = (CosDetails) processRbtContents(
				content, con, className);
		if (constructedCosDetailsObject != null) {
			try {
				CosDetails cosDetailsClassObject = CacheManagerUtil
						.getCosDetailsCacheManager().getCosDetail(
								constructedCosDetailsObject.getCosId());
				DebugManager.detail("cosDetailsClassObject: "
						+ constructedCosDetailsObject
						+ " cosDetailsClassObject: " + cosDetailsClassObject,
						"RBTConfigDevPlugin",
						"Inside Process_CosDetails_Content",
						"Processing_CosDetails_Content", Thread.currentThread()
								.getName(), null);
				if (cosDetailsClassObject == null) {
					DebugManager.detail("Add cosDetail class file data's",
							"RBTConfigDevPlugin", "StartProcess",
							"Processing_CosDetails_Content", Thread
									.currentThread().getName(), null);
					CacheManagerUtil.getCosDetailsCacheManager().addCosDetail(
							constructedCosDetailsObject);
				} else {
					DebugManager.detail("Update cosdetail class file data's",
							"RBTConfigDevPlugin", "StartProcess",
							"Processing_CosDetails_Content", Thread
									.currentThread().getName(), null);
					CacheManagerUtil.getCosDetailsCacheManager()
							.updateCosDetail(constructedCosDetailsObject);
				}
			} catch (IllegalArgumentException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside Process_CosDetails_Content",
						"Exception occured:", e, Thread.currentThread()
								.getName(), null);
			}
			DebugManager.detail("End process_CosDetails_Content",
					"RBTConfigDevPlugin", "Ending Process",
					"Processing_CosDetails_Content", Thread.currentThread()
							.getName(), null);
		}
	}

	@SuppressWarnings("unchecked")
	private void process_RbtSubscriptionClass_Content(Content content,
			Connection con, String className) {
		DebugManager.detail("Start process_RbtSubscriptionClass_Content",
				"RBTConfigDevPlugin", "StartProcessing",
				"Processing_RbtSubscriptionClass_Content", Thread
						.currentThread().getName(), null);
		SubscriptionClass constrctedSubClass = (SubscriptionClass) processRbtContents(
				content, con, className);
		if (null != constrctedSubClass) {
			try {
				SubscriptionClass subClassObject = com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil
						.getSubscriptionClassCacheManager()
						.getSubscriptionClass(
								constrctedSubClass.getSubscriptionClass());
				DebugManager.detail("constrctedSubClass: " + constrctedSubClass
						+ " subClassObject: " + subClassObject,
						"RBTConfigDevPlugin",
						"Inside Process_RbtSubscriptionClass_Content",
						"Processing_RbtSubscriptionClass_Content", Thread
								.currentThread().getName(), null);
				if (null != subClassObject) {
					DebugManager.detail("Add subscrption class file data's",
							"RBTConfigDevPlugin", "StartProcess",
							"Processing_RbtSubscriptionClass_Content", Thread
									.currentThread().getName(), null);
					com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.updateSubscriptionClass(constrctedSubClass);
				} else {
					DebugManager.detail("Update subscrption class file data's",
							"RBTConfigDevPlugin", "StartProcess",
							"Processing_RbtSubscriptionClass_Content", Thread
									.currentThread().getName(), null);
					com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil
							.getSubscriptionClassCacheManager()
							.addSubscriptionClass(constrctedSubClass);
				}
			} catch (IllegalArgumentException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside Process_RbtSubscriptionClass_Content",
						"Exception occured:", e, Thread.currentThread()
								.getName(), null);
			}
			DebugManager.detail("End process_RbtSubscriptionClass_Content",
					"RBTConfigDevPlugin",
					"Inside Process_RbtSubscriptionClass_Content",
					"Processing_RbtSubscriptionClass_Content", Thread
							.currentThread().getName(), null);
		}
	}

	@SuppressWarnings("unchecked")
	private void process_RbtChargeClass_Content(Content content,
			Connection con, String className) {
		DebugManager.detail("Start process_RbtChargeClass_Content",
				"RBTConfigDevPlugin", "StartProcessing",
				"Processing_RbtChargeClass_Content", Thread.currentThread()
						.getName(), null);
		ChargeClass constrctedChargeClass = (ChargeClass) processRbtContents(
				content, con, className);
		if (null != constrctedChargeClass) {
			try {
				ChargeClass chargeClassObject = CacheManagerUtil
						.getChargeClassCacheManager().getChargeClass(
								constrctedChargeClass.getChargeClass());
				DebugManager.detail("constrctedChargeClass: "
						+ constrctedChargeClass + " chargeClassObject: "
						+ chargeClassObject, "RBTConfigDevPlugin",
						"StartProcessing",
						"Inside Process_RbtChargeClass_Content", Thread
								.currentThread().getName(), null);
				if (null != chargeClassObject) {
					DebugManager.detail("Add the charge class file data's",
							"RBTConfigDevPlugin", "StartProcess",
							"Processing_RbtChargeClass_Content", Thread
									.currentThread().getName(), null);
					CacheManagerUtil.getChargeClassCacheManager()
							.updateChargeClass(constrctedChargeClass);
				} else {
					DebugManager.detail("Update the charge class file data's",
							"RBTConfigDevPlugin", "StartProcess",
							"Processing_RbtChargeClass_Content", Thread
									.currentThread().getName(), null);
					CacheManagerUtil.getChargeClassCacheManager()
							.addChargeClass(constrctedChargeClass);
				}
			} catch (IllegalArgumentException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside Process_RbtChargeClass_Content",
						"Exception occured:", e, Thread.currentThread()
								.getName(), null);
			}
			DebugManager.detail("End process_RbtChargeClass_Content",
					"RBTConfigDevPlugin",
					"Inside Process_RbtChargeClass_Content",
					"Processing_RbtChargeClass_Content", Thread.currentThread()
							.getName(), null);
		}
	}

	@SuppressWarnings("unchecked")
	private void processRBTText(int dataCnt, CBinary[] binaries) {
		File readFileConfig = new File(
				"/opt/ozone/bin/jlib/ContentDaemon/conf/RBTTextConfig.xml");
		DebugManager.detail("Starting processRBTText", "RBTConfigDevPlugin",
				"StartProcessing", "processRBTText", Thread.currentThread()
						.getName(), null);
		while (binaries != null && dataCnt < binaries.length) {
			String filePath = binaries[dataCnt].getPath();
			DebugManager.detail("Inside processRBTText", "RBTConfigDevPlugin",
					"filePath" + filePath, "processRBTText", Thread
							.currentThread().getName(), null);
			String fileName = binaries[dataCnt].getName();
			DebugManager.detail("Inside processRBTText", "RBTConfigDevPlugin",
					"fileName" + fileName, "processRBTText", Thread
							.currentThread().getName(), null);
			dataCnt++;
			File readFile = new File(filePath);
			List<RBTText> result = null;
			DebugManager.detail("Inside processRBTText", "RBTConfigDevPlugin",
					"readFile.exists(): " + readFile.exists(),
					"readFileConfig.exists(): " + readFileConfig.exists(),
					Thread.currentThread().getName(), null);
			if (readFile.exists() && readFileConfig.exists()) {
				result = (List<RBTText>) parseExcelFileToBeans(readFile,
						readFileConfig, RBT_TEXT_TYPE);
			}
			DebugManager.detail("Inside processRBTText", "RBTConfigDevPlugin",
					"result" + result, "processRBTText", Thread.currentThread()
							.getName(), null);
			RBTText existingRbtText = null;
			for (RBTText rbtText : result) {
				try {

					String subType = rbtText.getSubType() == null ? ""
							: rbtText.getSubType();
					String type = rbtText.getType() == null ? "" : rbtText
							.getType();
					String language = rbtText.getLanguage() == null ? ""
							: rbtText.getLanguage();
					String text = rbtText.getText();
					DebugManager.detail("subType: " + subType + " type: "
							+ type + " language: " + language + " text: "
							+ text, "RBTConfigDevPlugin", "StartProcessing",
							"processRBTText", Thread.currentThread().getName(),
							null);
					existingRbtText = CacheManagerUtil.getRbtTextCacheManager()
							.getRBTText(type, subType, language,null,true);
					DebugManager.detail("existingRbtText: " + existingRbtText,
							"RBTConfigDevPlugin", "StartProcessing",
							"processRBTText", Thread.currentThread().getName(),
							null);
					if (existingRbtText == null) {
						DebugManager.detail(
								"subType: " + rbtText.getSubType() + " type: "
										+ rbtText.getType() + " language: "
										+ rbtText.getLanguage() + " text: "
										+ rbtText.getText(),
								"RBTConfigDevPlugin", "StartProcessing",
								"processRBTText", Thread.currentThread()
										.getName(), null);
						rbtText.setLanguage(language);
						rbtText.setSubType(subType);
						rbtText.setText(text);
						rbtText.setType(type);
						DebugManager.detail("Add the text file data's",
								"RBTConfigDevPlugin", "StartProcessing",
								"processRBTText", Thread.currentThread()
										.getName(), null);
						CacheManagerUtil.getRbtTextCacheManager().addRBTText(
								rbtText);
					} else {
						DebugManager.detail("Update the text file data's",
								"RBTConfigDevPlugin", "StartProcessing",
								"processRBTText", Thread.currentThread()
										.getName(), null);
						CacheManagerUtil.getRbtTextCacheManager()
								.updateRBTText(type, subType, language, text);
					}
				} catch (Exception e) {
					DebugManager.exception("RBTConfigDevPlugin",
							"In processRBTText", "Exception occured:", e,
							Thread.currentThread().getName(), null);
				}
			}
			DebugManager.detail("Going to read file", "RBTConfigDevPlugin",
					"StartProcessing", "processRBTText", Thread.currentThread()
							.getName(), null);
		}
		DebugManager.detail("End processRBTText", "RBTConfigDevPlugin",
				"StartProcessing", "processRBTText", Thread.currentThread()
						.getName(), null);
	}

	@SuppressWarnings("unchecked")
	private void processRBTParameters(int dataCnt, CBinary[] binaries) {
		File readFileConfig = new File(
				"/opt/ozone/bin/jlib/ContentDaemon/conf/RBTParametersConfig.xml");
		DebugManager.detail("Starting processRBTParameters",
				"RBTConfigDevPlugin", "StartProcessing",
				"processRBTParameters", Thread.currentThread().getName(), null);
		while (binaries != null && dataCnt < binaries.length) {
			String filePath = binaries[dataCnt].getPath();
			dataCnt++;
			File readFile = new File(filePath);
			List<Parameters> result = null;
			if (readFile.exists() && readFileConfig.exists()) {
				result = (List<Parameters>) parseExcelFileToBeans(readFile,
						readFileConfig, PARAM_FILE_TYPE);
			}
			Parameters existingRbtParam = null;
			for (Parameters rbtParameters : result) {
				try {
					String type = rbtParameters.getType();
					String paramName = rbtParameters.getParam();
					String value = rbtParameters.getValue();
					String info = rbtParameters.getInfo();
					DebugManager.detail("type: " + type + " param: "
							+ existingRbtParam + " value: " + value + " info: "
							+ info, "RBTConfigDevPlugin", "StartProcessing",
							"processRBTParameters", Thread.currentThread()
									.getName(), null);
					existingRbtParam = CacheManagerUtil
							.getParametersCacheManager().getParameter(type,
									paramName);

					logger.info("existingRbtParam: " + existingRbtParam);
					DebugManager.detail(
							"existingRbtParam: " + existingRbtParam,
							"RBTConfigDevPlugin", "StartProcessing",
							"processRBTParameters", Thread.currentThread()
									.getName(), null);
					if (existingRbtParam == null) {
						DebugManager.detail("Add the text file data's",
								"RBTConfigDevPlugin", "StartProcessing",
								"processRBTParameters", Thread.currentThread()
										.getName(), null);
						CacheManagerUtil.getParametersCacheManager()
								.addParameter(type, paramName, value, info);
					} else {
						DebugManager.detail("Update the text file data's",
								"RBTConfigDevPlugin", "StartProcessing",
								"processRBTParameters", Thread.currentThread()
										.getName(), null);
						CacheManagerUtil.getParametersCacheManager()
								.updateParameter(type, paramName, value, info);
					}
				} catch (Exception e) {
					DebugManager.exception("RBTConfigDevPlugin",
							"In processRBTParameters", "Exception occured:", e,
							Thread.currentThread().getName(), null);
				}
			}
			DebugManager.detail("Going to read file", "RBTConfigDevPlugin",
					"StartProcessing", "processRBTParameters", Thread
							.currentThread().getName(), null);
			dataCnt++;
		}
		DebugManager.detail("End processRBTParameters", "RBTConfigDevPlugin",
				"StartProcessing", "processRBTParameters", Thread
						.currentThread().getName(), null);
	}

	private Object processRbtContents(Content content, Connection con,
			String className) {
		CData[] datas = null;
		String fieldName = null;
		String fieldValue = null;
		int dataCnt = 0;
		Class<?> clazz = null;
		Field field = null;
		CBinary[] binaries = null;
		HashMap<String, String> setterMap = new HashMap<String, String>();
		SubscriptionClass sub = null;
		CosDetails cos = null;
		ChargeClass charge = null;
		Object fieldType = null;
		int tempInt = 0;
		Timestamp tempTs = null;

		if (null != content) {
			try {
				datas = content.getData(con);
				binaries = content.getBinaries(con);
				DebugManager.detail("datas: " + datas.toString()
						+ " binaries: " + binaries.toString(),
						"RBTConfigDevPlugin", "StartProcessing",
						"processRbtContents", Thread.currentThread().getName(),
						null);
				processRBTText(dataCnt, binaries);
				dataCnt = 0;
				clazz = Class.forName(className);
				if (className.equalsIgnoreCase(SUBSCRIPTIONCLASS)) {
					setterMap = SubscriptionClassColumnToSetterMapping;
					sub = (SubscriptionClass) clazz.newInstance();
				} else if (className.equalsIgnoreCase(CHARGECLASS)) {
					setterMap = ChargeClassColumnToSetterMapping;
					charge = (ChargeClass) clazz.newInstance();
				} else if (className.equalsIgnoreCase(COSDETAILSCLASS)) {
					setterMap = CosDetailsColumnToSetterMapping;
					cos = (CosDetails) clazz.newInstance();
				}
				while (datas != null && dataCnt < datas.length) {
					fieldName = setterMap.get(datas[dataCnt].getName());
					if (fieldName != null && !fieldName.isEmpty()) {
						fieldValue = datas[dataCnt].getValue();
						field = clazz.getDeclaredField(fieldName);
						field.setAccessible(true);
						fieldType = field.getType();
						DebugManager.detail("fieldName: " + fieldName
								+ " fieldValue: " + fieldValue,
								"RBTConfigDevPlugin", "StartProcessing",
								"processRbtContents", Thread.currentThread()
										.getName(), null);
						if (fieldType.toString().equalsIgnoreCase("int")) {
							tempInt = Integer.parseInt((String) fieldValue);
							if (className.equalsIgnoreCase(SUBSCRIPTIONCLASS)) {
								field.set(sub, tempInt);
							} else if (className.equalsIgnoreCase(CHARGECLASS)) {
								field.set(charge, tempInt);
							} else if (className
									.equalsIgnoreCase(COSDETAILSCLASS)) {
								field.set(cos, tempInt);
							}
						} else if (fieldType.toString().endsWith("String")) {
							if (className.equalsIgnoreCase(SUBSCRIPTIONCLASS)) {
								field.set(sub, fieldValue);
							} else if (className.equalsIgnoreCase(CHARGECLASS)) {
								field.set(charge, fieldValue);
							} else if (className
									.equalsIgnoreCase(COSDETAILSCLASS)) {
								field.set(cos, fieldValue);
							}
						} else if (fieldType.toString().endsWith("Timestamp")) {
							SimpleDateFormat sf = new SimpleDateFormat(
									"yyyy-MM-dd");
							try {
								Date dt = sf.parse(fieldValue);
								tempTs = new Timestamp(dt.getTime());
							} catch (ParseException e) {
							}
							if (className.equalsIgnoreCase(SUBSCRIPTIONCLASS)) {
								field.set(sub, tempTs);
							} else if (className.equalsIgnoreCase(CHARGECLASS)) {
								field.set(charge, tempTs);
							} else if (className
									.equalsIgnoreCase(COSDETAILSCLASS)) {
								field.set(cos, tempTs);
							}
						}
					}
					dataCnt++;
				}
				if (sub != null) {
					return sub;
				} else if (cos != null) {
					return cos;
				} else if (charge != null) {
					return charge;
				}
			} catch (SQLException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside ProcessRbtContents", "Exception occured:", e,
						Thread.currentThread().getName(), null);
			} catch (ClassNotFoundException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside ProcessRbtContents", "Exception occured:", e,
						Thread.currentThread().getName(), null);
			} catch (SecurityException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside ProcessRbtContents", "Exception occured:", e,
						Thread.currentThread().getName(), null);
			} catch (NoSuchFieldException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside ProcessRbtContents", "Exception occured:", e,
						Thread.currentThread().getName(), null);
			} catch (IllegalArgumentException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside ProcessRbtContents", "Exception occured:", e,
						Thread.currentThread().getName(), null);
			} catch (IllegalAccessException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside ProcessRbtContents", "Exception occured:", e,
						Thread.currentThread().getName(), null);
			} catch (InstantiationException e) {
				DebugManager.exception("RBTConfigDevPlugin",
						"Inside ProcessRbtContents", "Exception occured:", e,
						Thread.currentThread().getName(), null);
			}
		}
		return null;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
	}

}
