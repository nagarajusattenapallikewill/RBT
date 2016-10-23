package com.onmobile.apps.ringbacktones.daemons.multioperator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

public class RBTResolveMultiOpCopyContent implements Runnable {

	private static Logger logger = Logger
			.getLogger(RBTResolveMultiOpCopyContent.class);
	private static final Logger MULTI_OP_COPY_CONTENT_TXN_LOG = Logger
			.getLogger("MultiOpCopyContentTxnLogger");
	private RBTMultiOpCopyRequest rbtMultiOpCopyRequest;
	private String contentResolutionUrl;
	private int maxRetries;

	private static final int COPIEER_OPERATOR_IDENTIFIED = 2;
	private static final int CONTENT_IDENTIFACTION_SUCCESS = 4;
	private static final int CONTENT_IDENTIFACTION_FAILED = 5;
	private static final String MISSING = "MISSING";

	public RBTResolveMultiOpCopyContent(String contentResolutionUrl,
			RBTMultiOpCopyRequest rbtMultiOpCopyRequest, int maxRetries) {
		this.contentResolutionUrl = contentResolutionUrl;
		this.rbtMultiOpCopyRequest = rbtMultiOpCopyRequest;
		this.maxRetries = maxRetries;
	}

	@Override
	public void run() {

		// process one by one record.
		if (isSourceContentMissing()) {
			updateOrDeleteRequest(MISSING, CONTENT_IDENTIFACTION_FAILED);
		} else {

			Map<String, String> targetContentIdsMap = new HashMap<String, String>();
			List<String> operatorIdsList = new ArrayList<String>();
			String operatorIdsStr = rbtMultiOpCopyRequest
					.getCopierOperatorIds();
			String targetContentIds = rbtMultiOpCopyRequest
					.getTargetContentIds();
			if (null != targetContentIds || !"".equals(targetContentIds)) {
				targetContentIdsMap = MapUtils.convertToMap(targetContentIds,
						",", "=", null);
			}
			if (null != operatorIdsStr || !"".equals(operatorIdsStr)) {
				operatorIdsList = ListUtils.convertToList(operatorIdsStr, ",");
				logger.debug("Processing operatorIdsList: " + operatorIdsList);
				for (String operatorId : operatorIdsList) {
					if (!targetContentIdsMap.containsKey(operatorId)) {
						logger.debug("Processing to operatorId: " + operatorId);
						String contentId = hitAtlantisAndUpdateStatus(operatorId);
						if (null != contentId) {
							targetContentIdsMap.put(operatorId, contentId);
						}
					}
				}
				// update retry count.
				String resolvedContentIds = trimResolvedContentIds(targetContentIdsMap
						.toString());
				int updatedStatus = updateAndGetStatus(
						targetContentIdsMap.size(), operatorIdsList.size());
				updateOrDeleteRequest(resolvedContentIds, updatedStatus);
			}

		}

	}

	private int updateAndGetStatus(int targetContentsSize,
			int operatorIdsListSize) {
		int status = COPIEER_OPERATOR_IDENTIFIED;

		if (targetContentsSize == operatorIdsListSize) {
			status = CONTENT_IDENTIFACTION_SUCCESS;
		} else if (targetContentsSize > 0) {
			status = COPIEER_OPERATOR_IDENTIFIED;
		}
		return status;
	}

	private String trimResolvedContentIds(String resolvedContentIds) {
		int len = resolvedContentIds.length();
		resolvedContentIds = resolvedContentIds.substring(1, len - 1);
		return resolvedContentIds;
	}

	private boolean isSourceContentMissing() {
		String srcContentId = rbtMultiOpCopyRequest.getSourceContentId();
		String copierOperatorIds = rbtMultiOpCopyRequest.getCopierOperatorIds();
		boolean isSourceContentMissing = (srcContentId == null)
				|| srcContentId.equalsIgnoreCase("-1")
				|| srcContentId.equals(MISSING);
		boolean isCopierOperatorIdsMissing = (copierOperatorIds == null) ? true
				: false;
		boolean isContentMissing = isSourceContentMissing
				|| isCopierOperatorIdsMissing;
		logger.info("Returning isContentMissing: " + isContentMissing
				+ ", isSourceContentMissing: " + isSourceContentMissing
				+ ", isCopierOperatorIdsMissing: " + isCopierOperatorIdsMissing);
		return isContentMissing;
	}

	private void updateOrDeleteRequest(String targetContentIds, int status) {
		int contentResolveRetryCount = rbtMultiOpCopyRequest
				.getContentResolveRetryCount();
		contentResolveRetryCount++;
		rbtMultiOpCopyRequest
				.setContentResolveRetryCount(contentResolveRetryCount);

		if (contentResolveRetryCount == maxRetries) {

			if (null == rbtMultiOpCopyRequest.getTargetContentIds()) {

				logger.debug("Non of the content is resolved after max retries,"
						+ " so deleting rbtMultiOpCopyRequest: "
						+ rbtMultiOpCopyRequest);
				// write trans log
				RBTMultiOpCopyHibernateDao.getInstance().delete(
						rbtMultiOpCopyRequest);
				logger.info("Reached max retries, deleted rbtMultiOpCopyRequest: "
						+ rbtMultiOpCopyRequest);
				MULTI_OP_COPY_CONTENT_TXN_LOG.info(rbtMultiOpCopyRequest
						.writeTxnLog("DELETED", "REACHED_MAX_RETRIES", false,
								null, null));
			} else {

				rbtMultiOpCopyRequest.setStatus(CONTENT_IDENTIFACTION_SUCCESS);

				RBTMultiOpCopyHibernateDao.getInstance().update(
						rbtMultiOpCopyRequest);
				logger.info("Maximum number of times retried. Updating "
						+ "status to 4 since some of targetContentIds"
						+ " are resloved." + " rbtMultiOpCopyRequest: "
						+ rbtMultiOpCopyRequest + ", targetContentIds: "
						+ targetContentIds);
			}
		} else {

			rbtMultiOpCopyRequest.setStatus(status);
			if (null != targetContentIds && !"".equals(targetContentIds)) {
				rbtMultiOpCopyRequest.setTargetContentIds(targetContentIds);
			}
			rbtMultiOpCopyRequest.setContentResolveTime(Calendar.getInstance()
					.getTime());

			RBTMultiOpCopyHibernateDao.getInstance().update(
					rbtMultiOpCopyRequest);
			logger.info("Updated rbtMultiOpCopyRequest: "
					+ rbtMultiOpCopyRequest);

			if (status == CONTENT_IDENTIFACTION_FAILED) {
				MULTI_OP_COPY_CONTENT_TXN_LOG.info(rbtMultiOpCopyRequest
						.writeTxnLog(
								String.valueOf(CONTENT_IDENTIFACTION_FAILED),
								targetContentIds, false, null, null));
			}
		}
	}

	/**
	 * 4-success 5-failure
	 */
	private String hitAtlantisAndUpdateStatus(String operatorId) {

		String operatorName = RBTMultiOpCopyParams
				.getOperatorIdOperatorRBTNameMap().get(operatorId);
		if (null == operatorName) {
			logger.warn("Returning null, operatorName is not found for operatorId: "
					+ operatorId);
			return null;
		}

		String clipId = null;
		HttpGet httpGet = null;
		DefaultHttpClient httpClient = null;
		String finalContentResolutionUrl = null;
		try {
			HashMap<String, String> requestParameters = getRequestParams(operatorName);
			finalContentResolutionUrl = appendReqParamsToUrl(requestParameters);

			logger.info("Making hit to atlantis. operatorId: " + operatorId
					+ ", operatorName: " + operatorName
					+ ", contentResolutionUrl: " + contentResolutionUrl);
			long st = System.currentTimeMillis();

			httpGet = new HttpGet(finalContentResolutionUrl);
			httpClient = new DefaultHttpClient();
			HttpParams params = httpClient.getParams();
			HttpConnectionParams.setConnectionTimeout(params, 3000);
			HttpConnectionParams.setSoTimeout(params, 3000);
			HttpResponse response = httpClient.execute(httpGet);
			long en = System.currentTimeMillis();
			clipId = getClipIdFromHeader(response);
			int statusCode = response.getStatusLine().getStatusCode();
			MULTI_OP_COPY_CONTENT_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Made HTTP call",
							"Response clipId: " + clipId, true, statusCode,
							finalContentResolutionUrl));
			logger.info("Sucessfully hit altantis. operatorId: " + operatorId
					+ ", operatorName: " + operatorName + ", responseCode: "
					+ statusCode + ", time taken: " + (en - st) + " ms");

		} catch (HttpException e) {
			logger.error("HttpException occured for operatorId: " + operatorId,
					e);
			MULTI_OP_COPY_CONTENT_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Made HTTP call",
							"HttpException" + e.getMessage(), true, null,
							finalContentResolutionUrl));
		} catch (IOException e) {
			logger.error("IOException occured for operatorId: " + operatorId, e);
			MULTI_OP_COPY_CONTENT_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Made HTTP call",
							"IOException" + e.getMessage(), true, null,
							finalContentResolutionUrl));
		} catch (Exception e) {
			logger.error("Exception occured for operatorId: " + operatorId, e);
			MULTI_OP_COPY_CONTENT_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Made HTTP call",
							"Exception" + e.getMessage(), true, null,
							finalContentResolutionUrl));
		} catch (Throwable t) {
			logger.error("Throwable occured for operatorId: " + operatorId, t);
			MULTI_OP_COPY_CONTENT_TXN_LOG.info(rbtMultiOpCopyRequest
					.writeTxnLog("Made HTTP call",
							"HttpException" + t.getMessage(), true, null,
							finalContentResolutionUrl));
		} finally {
			// TODO: CLOSE CONNECTIONS
		}

		logger.info("Returning contentId: " + clipId + " for operatorId: "
				+ operatorId);
		return clipId;
	}

	private HashMap<String, String> getRequestParams(String operatorName) {
		String srcContentId = rbtMultiOpCopyRequest.getSourceContentId();
		// String copierOperatorId =
		// rbtMultiOpCopyRequest.getCopierOperatorIds();
		String copieeOperatorIds = rbtMultiOpCopyRequest.getCopieeOperatorIds();
		String copieeOperatorName = RBTMultiOpCopyParams
				.getOperatorIdOperatorRBTNameMap().get(copieeOperatorIds);
		// As there are multiple operators can be present in the
		// copierOperatorName,
		// OperatorName is resolved before calling ResolveCopyContent.
		String copierOperatorName = operatorName;
		String info = String.valueOf(rbtMultiOpCopyRequest.getCopierMdn())
				.concat(",")
				.concat(String.valueOf(rbtMultiOpCopyRequest.getCopieeMdn()));

		HashMap<String, String> requestParameters = new HashMap<String, String>();
		requestParameters.put("sourceCLIPID", srcContentId);
		requestParameters.put("sourceOperator", copieeOperatorName);
		requestParameters.put("targetOperator", copierOperatorName);
		requestParameters.put("extraLogInfo", info);
		return requestParameters;
	}

	public String appendReqParamsToUrl(HashMap<String, String> requestParameters) {
		StringBuilder updatedUrl = new StringBuilder(
				contentResolutionUrl.trim());
		if (requestParameters == null || requestParameters.size() == 0) {
			logger.warn("contentResolutionUrl is null");
			return contentResolutionUrl;
		}

		if (contentResolutionUrl.indexOf("?") == -1
				&& requestParameters.size() > 0) {
			updatedUrl.append("?");

			Set<String> keySet = requestParameters.keySet();
			int keySetSize = keySet.size();
			int counter = 0;
			for (String paramName : keySet) {
				String paramValue = requestParameters.get(paramName);
				updatedUrl.append(paramName).append("=")
						.append(getEncodedValue(paramValue));
				if (counter < keySetSize - 1) {
					updatedUrl.append("&");
				}

				counter++;
			}
		}

		logger.info("Updated url. updatedUrl: " + updatedUrl.toString());
		return updatedUrl.toString();
	}

	public String getEncodedValue(String paramValue) {
		if (paramValue == null)
			return null;
		String ret = null;
		try {
			URLCodec urlCodec = new URLCodec();
			ret = urlCodec.encode(paramValue, "UTF-8");
		} catch (Throwable t) {
			ret = null;
		}
		return ret;
	}

	private String getClipIdFromHeader(HttpResponse response) {
		org.apache.http.Header[] headers = response.getAllHeaders();
		String clipId = null;
		for (org.apache.http.Header header : headers) {
			if (header.getName().equalsIgnoreCase("TARGET_CLIPID")) {
				clipId = header.getValue();
			}
		}
		logger.info("Returning contentId: " + clipId + " found from header");
		return clipId;
	}

}
