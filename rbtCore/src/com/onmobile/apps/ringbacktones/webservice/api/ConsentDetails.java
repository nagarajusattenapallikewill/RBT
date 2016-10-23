package com.onmobile.apps.ringbacktones.webservice.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.bean.DoubleConfirmationRequestBean;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.provisioning.Processor;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.provisioning.implementation.service.ServiceProcessor;
import com.onmobile.apps.ringbacktones.utils.MapUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.common.exception.OnMobileException;

public class ConsentDetails extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static Logger logger = Logger.getLogger(ConsentDetails.class);
	protected static RBTClient rbtClient = null;
	protected static Map<String, List<String>> externalToInternalModeMapping = null;
	protected static RBTCacheManager rbtCacheManager = null;
	protected static RBTDBManager rbtDbMgr = null;

	public ConsentDetails() {
		rbtClient = RBTClient.getInstance();
		String internalToExternalModeMappingStr = RBTParametersUtils
				.getParamAsString("DOUBLE_CONFIRMATION",
						"INTERNAL_EXTERNAL_MODES_MAP", null);
		externalToInternalModeMapping = MapUtils.convertToMapList(
				internalToExternalModeMappingStr, ";", "=", ",");
		logger.info("externalToInternalModeMapping="
				+ externalToInternalModeMapping);
		rbtCacheManager = RBTCacheManager.getInstance();
		rbtDbMgr = RBTDBManager.getInstance();

	}

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		PrintWriter out = null;
		DoubleConfirmationRequestBean doubleConfirmationRequestBean = null;

		String msisdn = request.getParameter(Constants.param_VODACT_MSISDN);
		String mode = request.getParameter(Constants.param_VODACT_MODE);
		String srvClass = request.getParameter(Constants.param_VODACT_CLASS);
		String srvId = request
				.getParameter(Constants.param_VODACT_SERVICE_CODE);
		boolean isShuffle = false;

		try {
			out = response.getWriter();

			if (msisdn == null || mode == null || srvClass == null
					|| srvId == null) {
				logger.info("Parameters missing, not processing further...");
				out.print(Constants.ERR_RESPONSE_SONG_CODE);
				return;
			}

			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(msisdn);
			Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
			if (subscriber == null) {
				logger.info("Subscriber doesn't exist");
				out.print(Constants.ERR_RESPONSE_SONG_CODE);
				return;
			}

			msisdn = subscriber.getSubscriberID();

			String circleID = null;
			if (!subscriber.isValidPrefix()) {
				// Invalid Circle Id
				circleID = subscriber.getCircleID();
				if (circleID == null) {
					logger.info("RBT:: Circle Id is null");
					out.print(Constants.ERR_RESPONSE_SONG_CODE);
					return;
				}

				HashMap<String, String> requestParams = new HashMap<String, String>();
				requestParams.put(Constants.param_VODACT_MSISDN, msisdn);
				requestParams.put(Constants.param_VODACT_MODE, mode);
				requestParams.put(Constants.param_VODACT_CLASS, srvClass);
				requestParams.put(Constants.param_VODACT_SERVICE_CODE, srvId);
				HashMap<String, Object> taskSession = new HashMap<String, Object>();
				taskSession.putAll(requestParams);

				Task task = new Task(null, taskSession);
				task.setObject(Constants.param_URL,
						"rbt_song_code_for_consent.do");
				task.setObject(Constants.param_CIRCLE_ID, circleID);
				task.setObject(Constants.param_subscriber, subscriber);
				String redirectionURL = Processor.getRedirectionURL(task);

				if (redirectionURL != null) {
					HttpParameters httpParameters = new HttpParameters(
							redirectionURL);
					try {
						HttpResponse httpResponse = RBTHttpClient
								.makeRequestByGet(httpParameters, requestParams);
						logger.info("RBT:: httpResponse: " + httpResponse);

						String resp = httpResponse.getResponse();
						if (response != null) {
							out.print(resp);
						}
						return;

					} catch (Exception e) {
						logger.error("RBT:: " + e.getMessage(), e);
						out.print(Constants.ERR_RESPONSE_SONG_CODE);
						return;
					}
				}
			}

			List<String> modeMappingList = (externalToInternalModeMapping != null && null != externalToInternalModeMapping
					.get(mode)) ? externalToInternalModeMapping.get(mode)
					: new ArrayList<String>();
			modeMappingList.add(mode);

			String toBeUpdatedWithMode = Utility
					.listToStringWithQuots(modeMappingList);

			logger.info("UPDATED MODE IS:" + toBeUpdatedWithMode);

			List<DoubleConfirmationRequestBean> doubleConfirmationRequestBeans = rbtDbMgr
					.getConsentRecordListForStatusNMsisdnMode("1", msisdn,
							toBeUpdatedWithMode, false);

			ServiceProcessor serviceProcesor = new ServiceProcessor();
			serviceProcesor
					.filterConsentBeansBasedOnServiceClassAndServiceId(
							doubleConfirmationRequestBeans, subscriber, srvId,
							srvClass);

			if (doubleConfirmationRequestBeans.size() == 0) {
				logger.info("doubleConfirmationRequestBeans SIZE IS : "
						+ doubleConfirmationRequestBeans.size());
				out.print(Constants.ERR_RESPONSE_SONG_CODE);
				return;
			}

			doubleConfirmationRequestBean = doubleConfirmationRequestBeans
					.get(doubleConfirmationRequestBeans.size() - 1);

			if (doubleConfirmationRequestBean == null
					|| doubleConfirmationRequestBean.getRequestType() == null) {
				logger.info("No pending record found...possible config issue");
				out.print(Constants.ERR_RESPONSE_SONG_CODE);
				return;
			}
			logger.info("been identified for callback "
					+ doubleConfirmationRequestBean);
			String baseTrnxId = doubleConfirmationRequestBean.getTransId();
			String xtraInfo = doubleConfirmationRequestBean.getExtraInfo();
			Map<String, String> xtraInfoMap = null;
			String comboTransID = null;
			if (xtraInfo != null && !xtraInfo.equalsIgnoreCase("null"))
				xtraInfoMap = DBUtility.getAttributeMapFromXML(xtraInfo);
			if (xtraInfoMap == null)
				xtraInfoMap = new HashMap<String, String>();
			if (xtraInfoMap.containsKey("TRANS_ID")) {
				comboTransID = xtraInfoMap.get("TRANS_ID");
			}

			String wavFileName = doubleConfirmationRequestBean.getWavFileName();
			Clip clip = null;
			List<DoubleConfirmationRequestBean> doubleConfirmReqBeans = null;
			if (comboTransID != null) {
				doubleConfirmReqBeans = RBTDBManager
						.getInstance()
						.getDoubleConfirmationRequestBeanForStatus(
								null,
								comboTransID,
								doubleConfirmationRequestBean.getSubscriberID(),
								null, true);
				if (doubleConfirmReqBeans != null
						&& doubleConfirmReqBeans.size() > 0) {
					doubleConfirmationRequestBean = doubleConfirmReqBeans
							.get(0);
				}
			}

			if (doubleConfirmationRequestBean != null) {

				// In case of Shuffle category type then , we are returning
				// category promo id
				if (doubleConfirmationRequestBean.getCategoryType() != null) {
					int categoryType = Integer
							.valueOf(doubleConfirmationRequestBean
									.getCategoryType());
					if (Utility.isShuffleCategory(categoryType)) {
						isShuffle = true;
						Category categoryObj = rbtCacheManager
								.getCategory(doubleConfirmationRequestBean
										.getCategoryID());
						if (categoryObj != null) {
							out.print("SONG_CODE="
									+ categoryObj.getCategoryPromoId());
						}
					}
				}
				wavFileName = doubleConfirmationRequestBean.getWavFileName();
				if (wavFileName != null) {
					clip = rbtCacheManager.getClipByRbtWavFileName(wavFileName);
					if (clip != null) {

						// Deleting the Concent data
						logger.info("INFO : DELETING CONCENT DATA IS SUBSCRIBER ID :"
								+ baseTrnxId
								+ " | TRANSACTION ID:"
								+ doubleConfirmationRequestBean.getTransId());
						rbtDbMgr.deleteConsentRequestByTransIdAndMSISDN(
								baseTrnxId,
								doubleConfirmationRequestBean.getSubscriberID());
						if (comboTransID != null) {
							rbtDbMgr.deleteConsentRequestByTransIdAndMSISDN(
									comboTransID, doubleConfirmationRequestBean
											.getSubscriberID());
							logger.info("INFO : DELETING CONCENT DATA IS SUBSCRIBER ID :"
									+ doubleConfirmationRequestBean
											.getSubscriberID()
									+ " | COMBO TRX ID:" + comboTransID);
						}
						if (!isShuffle) {
							out.print("SONG_CODE=" + clip.getClipPromoId());
						}
						return;
					}
				}

				// Either wav file is null or clip is null
				out.print(Constants.ERR_RESPONSE_SONG_CODE);
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("ERROR:" + e.getMessage(), e);
		} catch (OnMobileException e) {
			e.printStackTrace();
			logger.info("ERROR:" + e.getMessage(), e);
		} catch (Exception e) {
			e.printStackTrace();
			logger.info("ERROR:" + e.getMessage(), e);
		} finally {
			out.close();
		}

	}

}
