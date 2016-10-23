package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorRequestBean;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.dao.ContentInterOperatorRequestDao;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorUtility;

/**
 * @author sridhar.sindiri
 *
 */
public class InterOperatorContentResolvingThread extends Thread
{
	private ContentInterOperatorDBFetcher dbFetcher = null;
	private static Logger logger = Logger.getLogger(ContentInterOperatorDBFetcher.class);
	private static Logger contentResolveLogger = Logger.getLogger("contentLogger");

	public InterOperatorContentResolvingThread(ContentInterOperatorDBFetcher dbFetcher)
	{
		this.dbFetcher = dbFetcher;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		while (true)
		{
			ContentInterOperatorRequestBean contentRequest = null;
			synchronized (dbFetcher.contentQueue)
			{
				if (dbFetcher.contentQueue.size() > 0)
				{
					logger.info("Content thread found contentQueue." + dbFetcher.contentQueue.get(0));
					contentRequest = dbFetcher.contentQueue.remove(0);
					dbFetcher.pendingQueue.add(contentRequest);
				}
				else
				{
					try
					{
						logger.info("Content thread waiting as queue size=" + dbFetcher.contentQueue.size());
						dbFetcher.contentQueue.wait();
					}
					catch (InterruptedException e)
					{
						logger.info("Content thread interrupted. Will check queue now");
					}
					continue;
				}
			}

			String contentResolutionUrl = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "CONTENT_RESOLUTION_URL", null);
			logger.info("atlantis url = " + contentResolutionUrl);
			if (contentResolutionUrl == null || contentResolutionUrl.trim().length() == 0)
				return;

			int operatorId = contentRequest.getOperatorID();
			String srcContentId = contentRequest.getSourceContentID();
			String srcContentOperator = contentRequest.getSourceContentOperator();
			String info = contentRequest.getMsisdn() + ",CENTRAL_WEB";

			if (srcContentOperator == null || srcContentOperator.length() == 0)
			{
				srcContentOperator = RBTParametersUtils.getParamAsString("RDC", "DEFAULT_CONTENT_PROVIDER", "VODAFONE");
			}
			
			Map<String,List<String>> atlantisOptMap = getAtlantisOptMap();

			if (srcContentOperator.equals("TTML") || srcContentOperator.equals("TTSL"))
				srcContentOperator = RBTParametersUtils.getParamAsString("RDC", "ATLANTIS_OPERATOR_NAME_FOR_TATA", "TATACDMA");

			String targetContentOperator = ContentInterOperatorUtility.getRBTOperatorNameFromOperatorID(String.valueOf(operatorId));
			if (targetContentOperator.equals("TTML") || targetContentOperator.equals("TTSL"))
				targetContentOperator = RBTParametersUtils.getParamAsString("RDC", "ATLANTIS_OPERATOR_NAME_FOR_TATA", "TATACDMA");

			Set<String> atlantisOptKeySet = atlantisOptMap.keySet();
			
			for(String atlantisOptKey : atlantisOptKeySet) {
				List<String> optList = atlantisOptMap.get(atlantisOptKey);
				if(optList.contains(srcContentOperator)) {
					srcContentOperator = atlantisOptKey;
				}
				if(optList.contains(targetContentOperator)) {
					targetContentOperator = atlantisOptKey;
				}
			}
			
			if (srcContentOperator.equals(targetContentOperator))
			{
				contentRequest.setTargetContentID(srcContentId);
				contentRequest.setContentResolveTime(Calendar.getInstance().getTime());
				contentRequest.setStatus(4);
				ContentInterOperatorRequestDao.update(contentRequest);
				continue;
			}

			HashMap<String, String> requestParameters = new HashMap<String, String>();
			requestParameters.put("sourceCLIPID", srcContentId);
			requestParameters.put("sourceOperator", srcContentOperator);
			requestParameters.put("targetOperator", targetContentOperator);
			requestParameters.put("extraLogInfo", info);
			ContentInterOperatorHttpResponse ioHttpResponse = null;

			if (srcContentId == null || srcContentId.equalsIgnoreCase("-1") || srcContentId.equals("MISSING"))
			{
				logger.info("Found contentRequest with sourceContentId = " + srcContentId + ". Not hitting atlantis.");
				contentRequest.setStatus(5);
				contentRequest.setTargetContentID("MISSING");
				contentRequest.setContentResolveTime(Calendar.getInstance().getTime());
				ContentInterOperatorRequestDao.update(contentRequest);
			}
			else
				ioHttpResponse = ContentInterOperatorHttpUtils.getResponse(contentResolutionUrl, requestParameters, null);

			if (ioHttpResponse == null)
			{
				contentResolveLogger.info("null!-1!null");
			}
			else if (ioHttpResponse.getHttpResponseCode() == 204)
			{
				contentRequest.setStatus(5);
				contentResolveLogger.info(ioHttpResponse.getFinalUrl() + "!204!null");
				contentRequest.setTargetContentID("MISSING");
				ContentInterOperatorRequestDao.update(contentRequest);
			}
			else if (ioHttpResponse != null && ioHttpResponse.getHttpResponseCode() == 200)
			{
				String targetContentId = getClipFromHeader(ioHttpResponse);
				if (targetContentId == null)
				{
					contentRequest.setStatus(5);
					contentRequest.setTargetContentID("MISSING");
					contentRequest.setContentResolveTime(Calendar.getInstance().getTime());
				}
				else
				{
					contentRequest.setTargetContentID(targetContentId);
					contentRequest.setContentResolveTime(Calendar.getInstance().getTime());
					contentRequest.setStatus(4);
				}
				contentResolveLogger.info(ioHttpResponse.getFinalUrl() + "!200!" + targetContentId);
				ContentInterOperatorRequestDao.update(contentRequest);
			}
			else
			{
				contentResolveLogger.info(ioHttpResponse.getFinalUrl() + "!" + ioHttpResponse.getHttpResponseCode() + "!" + null);
			}
			dbFetcher.pendingQueue.remove(contentRequest);
		}
	}

	/**
	 * @param ioHttpResponse
	 * @return
	 */
	private String getClipFromHeader(ContentInterOperatorHttpResponse ioHttpResponse)
	{
		Header[] headers = ioHttpResponse.getHttpHeaders();
		String targetClipId = null;
		for (Header header : headers)
		{
			if (header.getName().equalsIgnoreCase("TARGET_CLIPID"))
				targetClipId = header.getValue();
		}
		return targetClipId;
	}

	private Map<String,List<String>> getAtlantisOptMap() {
		String strAtlantisOptMap = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "ATLANTIS_OPERATOR_NAMES_FOR_CONTENT_RESOLVING", "TATACDMA:TTML,TTSL;IDEA:IDEA,IDEARN");
		Map<String,List<String>> atlantisOptMap = new HashMap<String, List<String>>();
		String[] atlantisOptNamesArr = strAtlantisOptMap.split("\\;");
		for(String atlantisOptNames : atlantisOptNamesArr) {
			String[] atlantisOptNameArr = atlantisOptNames.split("\\:");
			if(atlantisOptNameArr.length != 2) {
				continue;
			}
			List<String> atlantisOptList = Arrays.asList(atlantisOptNameArr[1].split("\\,"));
			atlantisOptMap.put(atlantisOptNameArr[0], atlantisOptList);			
		}
		return atlantisOptMap;
	}
}
