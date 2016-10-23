package com.onmobile.apps.ringbacktones.daemons.multioperator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.utils.ListUtils;
import com.onmobile.apps.ringbacktones.utils.MapUtils;

class RBTMultiOpCopyOperatorUpdater implements Runnable {

	private static Logger logger = Logger
			.getLogger(RBTMultiOpCopyOperatorUpdater.class);
	private static final Logger MULTI_OP_COPY_OPERATOR_TXN_LOG = Logger
			.getLogger("MultiOpCopyOperatorTxnLogger");
	private RBTMultiOpCopyRequest rbtMultiOpCopyRequest;
	private int maxRetries;

	public RBTMultiOpCopyOperatorUpdater(int maxRetries,
			RBTMultiOpCopyRequest rbtMultiOpCopyRequest) {
		this.maxRetries = maxRetries;
		this.rbtMultiOpCopyRequest = rbtMultiOpCopyRequest;
	}

	@Override
	public void run() {

		if (isTargetContentMissing()) {
			logger.info("Deleting since targetContentIds missing");
			RBTMultiOpCopyHibernateDao.getInstance().delete(
					rbtMultiOpCopyRequest);
			MULTI_OP_COPY_OPERATOR_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("DELETED", "MISSING_TARGET_CONTENT_IDS",
							false, null, null));

		} else {

			String targetContentIds = rbtMultiOpCopyRequest
					.getTargetContentIds();
			Map<String, String> targetContentIdsMap = MapUtils.convertToMap(
					targetContentIds, ",", "=", null);
			Set<String> set = targetContentIdsMap.keySet();

			logger.info("Processing to targetContentIdsMap: "
					+ targetContentIdsMap);

			String transferedOperatorIds = rbtMultiOpCopyRequest
					.getTransferedOperatorIds();

			List<String> transferredOperatorIdsList = null;
			if (null != transferedOperatorIds) {
				List<String> convertToList = ListUtils.convertToList(
						transferedOperatorIds, ",");
				transferredOperatorIdsList = new ArrayList<String>(
						convertToList);
			} else {
				transferredOperatorIdsList = new ArrayList<String>();
			}
			for (String operatorId : set) {

				try {
					if (transferredOperatorIdsList.contains(operatorId)) {
						logger.info("Already processed operatorId: "
								+ operatorId);
					} else {
						String operatorName = RBTMultiOpCopyParams
								.getOperatorIdOperatorRBTNameMap().get(
										operatorId);
						logger.info("Processing to operatorName: "
								+ operatorName + ", operatorId: " + operatorId);
						if (null != operatorName) {
							String operatorUrl = RBTMultiOpCopyParams
									.getOperatorNameToUrlMap()
									.get(operatorName);
							if (null != operatorUrl) {
								int statusCode = hitOperator(operatorId,
										operatorName, operatorUrl);
								if (200 == statusCode) {
									transferredOperatorIdsList.add(operatorId);
								}
								logger.info("Successfully hit operatorName: "
										+ operatorName + ", operatorUrl: "
										+ operatorUrl + ", statusCode: "
										+ statusCode
										+ ", transferredOperatorIdsList: "
										+ transferredOperatorIdsList);
							} else {
								logger.error("OperatorUrl not found for operatorName: "
										+ operatorName
										+ ", operatorId: "
										+ operatorId);
							}
						} else {
							logger.error("operatorName not found for"
									+ " operatorId: " + operatorId);
						}
					}
				} catch (Exception e) {
					logger.error(
							"Exception while processing to operators. Exception: "
									+ e.getMessage(), e);
				} catch (Throwable t) {
					logger.error(
							"Exception while processing to operators. Throwable: "
									+ t.getMessage(), t);
				}
			}
			updateDB(targetContentIdsMap.size(), transferredOperatorIdsList);
		}

	}

	private void updateDB(int targetContentIdsSize,
			List<String> transferredOperatorIdsList) {
		int transferRetryCount = rbtMultiOpCopyRequest.getTransferRetryCount();
		if (transferRetryCount == (maxRetries - 1)) {
			logger.info("Deleting record, retried for maximum times.");
			// delete
			RBTMultiOpCopyHibernateDao.getInstance().delete(
					rbtMultiOpCopyRequest);
			MULTI_OP_COPY_OPERATOR_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("DELETED", "REACHED MAX RETRIES", false, null,
							null));
		} else if (transferredOperatorIdsList.size() == targetContentIdsSize) {
			logger.info("Deleting record, processed to all operators.");
			// delete
			RBTMultiOpCopyHibernateDao.getInstance().delete(
					rbtMultiOpCopyRequest);
			MULTI_OP_COPY_OPERATOR_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("DELETED", "PROCESSED", false, null, null));
		} else {

			transferRetryCount++;
			rbtMultiOpCopyRequest.setTransferRetryCount(transferRetryCount);

			if (transferredOperatorIdsList.size() > 0) {
				rbtMultiOpCopyRequest
						.setTransferedOperatorIds(trimResolvedContentIds(transferredOperatorIdsList
								.toString()));
			}
			rbtMultiOpCopyRequest.setRequestTime(new Date());
			RBTMultiOpCopyHibernateDao.getInstance().update(
					rbtMultiOpCopyRequest);
		}
	}

	private boolean isTargetContentMissing() {
		if (null != rbtMultiOpCopyRequest.getTargetContentIds()) {
			return false;
		}
		return true;
	}

	private int hitOperator(String operatorId, String operatorName,
			String operatorUrl) {

		HashMap<String, String> requestParameters = getParamsMap(operatorId,
				operatorName);

		int responseCode = -1;

		HttpGet httpGet = null;
		DefaultHttpClient httpClient = null;
		String finalOperatorUrl = null;
		try {
			finalOperatorUrl = appendReqParamsToUrl(operatorUrl,
					requestParameters);

			logger.info("Making hit to operator operatorId: " + operatorId
					+ ", operatorName: " + operatorName + ", operatorUrl: "
					+ finalOperatorUrl);
			long st = System.currentTimeMillis();
			httpGet = new HttpGet(finalOperatorUrl);
			httpClient = new DefaultHttpClient();
			HttpParams params = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 3000);
			HttpConnectionParams.setSoTimeout(params, 3000);
			HttpResponse response = httpClient.execute(httpGet);
			long en = System.currentTimeMillis();
			responseCode = response.getStatusLine().getStatusCode();
			logger.info("Sucessfully hit operator. operatorName: "
					+ operatorName + ", operatorId: " + operatorId
					+ ", response: " + responseCode + ", time taken: "
					+ (en - st) + " ms");
			MULTI_OP_COPY_OPERATOR_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Requested operator: " + operatorName,
							"SUCCESS", true, responseCode, finalOperatorUrl));

		} catch (HttpException e) {
			logger.error("HttpException occured", e);
			MULTI_OP_COPY_OPERATOR_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Requested operator: " + operatorName,
							"FAILURE", true, responseCode, finalOperatorUrl));
		} catch (IOException e) {
			logger.error("IOException occured", e);
			MULTI_OP_COPY_OPERATOR_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Requested operator: " + operatorName,
							"FAILURE", true, responseCode, finalOperatorUrl));
		} catch (Exception e) {
			logger.error("Exception occured", e);
			MULTI_OP_COPY_OPERATOR_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Requested operator: " + operatorName,
							"FAILURE", true, responseCode, finalOperatorUrl));
		} catch (Throwable t) {
			logger.error("Throwable occured", t);
			MULTI_OP_COPY_OPERATOR_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Requested operator: " + operatorName,
							"FAILURE", true, responseCode, finalOperatorUrl));
		} finally {
		}
		logger.info("Returning responseCode: " + responseCode);
		return responseCode;
	}

	public String appendReqParamsToUrl(String operatorUrl,
			HashMap<String, String> requestParameters) {

		if (requestParameters == null || requestParameters.size() == 0) {
			return operatorUrl;
		}

		StringBuilder updatedUrl = new StringBuilder(operatorUrl.trim());

		if (operatorUrl.indexOf("?") == -1 && requestParameters.size() > 0) {
			updatedUrl.append("?");

			Set<String> keySet = requestParameters.keySet();
			int keySetSize = keySet.size();
			int counter = 0;
			for (String paramName : keySet) {
				String paramValue = requestParameters.get(paramName);
				updatedUrl.append(paramName).append("=")
						.append(getEncodedUrlString(paramValue));
				if (counter < keySetSize - 1) {
					updatedUrl.append("&");
				}

				counter++;
			}
		}

		logger.info("Updated url. updatedUrl: " + updatedUrl.toString());
		return updatedUrl.toString();
	}

	private HashMap<String, String> getParamsMap(String operatorId,
			String operatorName) {
		HashMap<String, String> parametersMap = new HashMap<String, String>();
		long copierMdn = rbtMultiOpCopyRequest.getCopierMdn();
		long copieeMdn = rbtMultiOpCopyRequest.getCopieeMdn();
		String sourceMode = rbtMultiOpCopyRequest.getSourceMode();
		String sourcePromoCode = rbtMultiOpCopyRequest.getSourcePromoCode();
		String targetContentIds = rbtMultiOpCopyRequest.getTargetContentIds();
		String sourceContentDetails = rbtMultiOpCopyRequest
				.getSourceContentDetails();
		String sourceSongName = rbtMultiOpCopyRequest.getSourceSongName();
		String keyPressed = rbtMultiOpCopyRequest.getKeyPressed();

		parametersMap.put("caller_id", String.valueOf(copierMdn));
		parametersMap.put("subscriber_id", String.valueOf(copieeMdn));
		parametersMap.put("sel_by", sourceMode);
		parametersMap.put("clip_id", getEncodedUrlString(sourcePromoCode));
		if ("MISSING".contains(targetContentIds)) {
			parametersMap.put("clip_id", targetContentIds + ":"
					+ sourceContentDetails);
		} else {
			Map<String, String> targetContentIdsMap = MapUtils.convertToMap(
					targetContentIds, ",", "=", null);
			String targetContentId = null;
			for (Entry<String, String> entry : targetContentIdsMap.entrySet()) {
				if (operatorId.equals(entry.getKey())) {
					targetContentId = entry.getValue();
					break;
				}
			}
			logger.info("Sending targetContentId: " + targetContentId
					+ ", for operatorId: " + operatorId
					+ ", targetContentIds: " + targetContentIds);
			parametersMap.put("clip_id", targetContentId);
		}
		parametersMap.put("sms_type", "COPY");
		parametersMap.put("opr_flag", "1");
		parametersMap.put("songname", getEncodedUrlString(sourceSongName));
		parametersMap.put("keypressed", getEncodedUrlString(keyPressed));

		return parametersMap;
	}

	private String trimResolvedContentIds(String resolvedContentIds) {
		int len = resolvedContentIds.length();
		resolvedContentIds = resolvedContentIds.substring(1, len - 1);
		return resolvedContentIds;
	}

	private String getEncodedUrlString(String param) {
		String ret = null;
		try {
			URLCodec urlCodec = new URLCodec();
			ret = urlCodec.encode(param, "UTF-8");
		} catch (Throwable t) {
			ret = null;
		}
		return ret;
	}

}