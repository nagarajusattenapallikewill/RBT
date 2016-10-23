package com.onmobile.apps.ringbacktones.daemons.interoperator.threads;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.Header;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorCopyRequestBean;
import com.onmobile.apps.ringbacktones.daemons.interoperator.bean.InterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.interoperator.dao.InterOperatorCopyRequestDao;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.interoperator.tools.InterOperatorUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class InterOperatorContentResolvingThread extends Thread
{
	private InterOperatorDBFetcher dbFetcher = null;
	static Logger logger = Logger.getLogger(InterOperatorDBFetcher.class);
	static Logger contentResolveLogger = Logger.getLogger(InterOperatorContentResolvingThread.class);
	static Logger copyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestDao.class);
	static Logger oldCopyTransactionLogger = Logger.getLogger(InterOperatorCopyRequestBean.class);
	
	public InterOperatorContentResolvingThread(InterOperatorDBFetcher dbFetcher)
	{
		this.dbFetcher = dbFetcher;
	}
	
	public void run()
	{
		while(true)
		{
			InterOperatorCopyRequestBean copyRequest = null; 
			try
			{
				synchronized (dbFetcher.copyQueue)
				{
					if(dbFetcher.copyQueue.size() > 0)
					{
						logger.info("Content thread found copyrequest."+dbFetcher.copyQueue.get(0));
						copyRequest = dbFetcher.copyQueue.remove(0);
						dbFetcher.pendingQueue.add(copyRequest);
					}
					else
					{
						try {
							logger.info("Content thread waiting as queue size="+dbFetcher.copyQueue.size());
							dbFetcher.copyQueue.wait();
						} catch (InterruptedException e) {
							logger.info("Content thread interrupted. Will check queue now");
						}
						continue;
					}	
				}
				
				Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("RDC", "CONTENT_RESOLUTION_URL", null);
				logger.info("atlantis url="+parameter);
				if(parameter == null || parameter.getValue() == null || parameter.getValue().trim().length() == 0)
					return;
				
				String url = parameter.getValue().trim();
				int copierOperatorId = copyRequest.getCopierOperatorId();
				int copieeOperatorId = copyRequest.getCopieeOperatorId();
				String srcContentId = copyRequest.getSourceContentId();
				String info = copyRequest.getCopierMdn()+","+copyRequest.getCopieeMdn();
				
				
				Map<String,List<String>> atlantisOptMap = getAtlantisOptMap();

				String copieeOperatorName = InterOperatorUtility.getRBTOperatorNameFromOperatorID(copieeOperatorId+"");
				String copierOperatorName = InterOperatorUtility.getRBTOperatorNameFromOperatorID(copierOperatorId+"");
				
				Set<String> atlantisOptKeySet = atlantisOptMap.keySet();
				
				for(String atlantisOptKey : atlantisOptKeySet) {
					List<String> optList = atlantisOptMap.get(atlantisOptKey);
					if(optList.contains(copieeOperatorName)) {
						copieeOperatorName = atlantisOptKey;
					}
					if(optList.contains(copierOperatorName)) {
						copierOperatorName = atlantisOptKey;
					}
				}
				
//				if (copieeOperatorName.equals("TTML") || copieeOperatorName.equals("TTSL"))
//					copieeOperatorName = RBTParametersUtils.getParamAsString("RDC", "ATLANTIS_OPERATOR_NAME_FOR_TATA", "TATACDMA");
//				
//				if (copieeOperatorName.equals("IDEA") || copieeOperatorName.equals("IDEARN"))
//					copieeOperatorName = RBTParametersUtils.getParamAsString("RDC", "ATLANTIS_OPERATOR_NAME_FOR_IDEA", "IDEA");
//
//				
//				if (copierOperatorName.equals("TTML") || copierOperatorName.equals("TTSL"))
//					copierOperatorName = RBTParametersUtils.getParamAsString("RDC", "ATLANTIS_OPERATOR_NAME_FOR_TATA", "TATACDMA");
//				
//				if (copierOperatorName.equals("IDEA") || copierOperatorName.equals("IDEARN"))
//					copierOperatorName = RBTParametersUtils.getParamAsString("RDC", "ATLANTIS_OPERATOR_NAME_FOR_IDEA", "IDEA");

				if(!InterOperatorUtility.isCopyAllowedBetweenOperator(copyRequest))
				{
					copyTransactionLogger.info(InterOperatorUtility.getLoggableBean(copyRequest));
					oldCopyTransactionLogger.info(InterOperatorUtility.getTransLoggableBean(copyRequest));
					InterOperatorCopyRequestDao.delete(copyRequest.getCopyId());
					logger.info("Deleting request as copy not allowed between opeartors for copyId="+copyRequest.getCopyId());
					dbFetcher.pendingQueue.remove(copyRequest);
					InterOperatorUtility.writeEventLog(copyRequest, "OPR_COMBO_NOT_ALLOWED");
				}

				else if (copieeOperatorName.equals(copierOperatorName))
				{
					copyRequest.setTargetContentId(srcContentId);
					copyRequest.setContentResolveTime(Calendar.getInstance().getTime());
					copyRequest.setStatus(4);
					InterOperatorUtility.writeEventLog(copyRequest, "CONTENT_MAPPING_FOUND");
					InterOperatorCopyRequestDao.update(copyRequest);
				}

				else {
					HashMap<String, String> requestParameters = new HashMap<String, String>();
					requestParameters.put("sourceCLIPID", srcContentId);
					requestParameters.put("sourceOperator", copieeOperatorName);
					requestParameters.put("targetOperator", copierOperatorName);
					requestParameters.put("extraLogInfo", info);
					InterOperatorHttpResponse ioHttpResponse = null;
				
		
					if(srcContentId == null || srcContentId.equalsIgnoreCase("-1") || srcContentId.equals("MISSING"))
					{
						logger.info("Found copyRequest with sourceContentId="+srcContentId+" for copyId="+copyRequest.getCopyId()+". Not hitting atlantis.");
						copyRequest.setStatus(5);
						copyRequest.setTargetContentId("MISSING");
						copyRequest.setContentResolveTime(Calendar.getInstance().getTime());
						InterOperatorCopyRequestDao.update(copyRequest);
						InterOperatorUtility.writeEventLog(copyRequest, "NON_COPY_CONTENT");
					}	
					else
						ioHttpResponse = InterOperatorHttpUtils.getResponse(url, requestParameters, null);
				
					if(ioHttpResponse == null)
					{
						contentResolveLogger.info("null!-1!null");
					}	
					else if(ioHttpResponse.getHttpResponseCode() == 204)
					{
						copyRequest.setStatus(5);
						contentResolveLogger.info(ioHttpResponse.getFinalUrl()+"!204!null");
						copyRequest.setTargetContentId("MISSING");
						InterOperatorCopyRequestDao.update(copyRequest);
						InterOperatorUtility.writeEventLog(copyRequest, "CONTENT_MAPPING_MISSING");
					}
					else if (ioHttpResponse != null &&  ioHttpResponse.getHttpResponseCode() == 200)
					{
						String targetContentId = getClipFromHeader(ioHttpResponse);
						if(targetContentId == null)
						{
							copyRequest.setStatus(5);
							copyRequest.setTargetContentId("MISSING");
							copyRequest.setContentResolveTime(Calendar.getInstance().getTime());
							InterOperatorUtility.writeEventLog(copyRequest, "CONTENT_MAPPING_MISSING");
						}
						else
						{
							copyRequest.setTargetContentId(targetContentId);
							copyRequest.setContentResolveTime(Calendar.getInstance().getTime());
							copyRequest.setStatus(4);
							InterOperatorUtility.writeEventLog(copyRequest, "CONTENT_MAPPING_FOUND");
						}
						contentResolveLogger.info(ioHttpResponse.getFinalUrl()+"!200!"+targetContentId);
						InterOperatorCopyRequestDao.update(copyRequest);
					}
					else
					{
						contentResolveLogger.info(ioHttpResponse.getFinalUrl()+"!"+ioHttpResponse.getHttpResponseCode()+"!"+null);
					}
				}
			}
			catch(Exception e)
			{
				logger.error("Exception", e);
			}
			if(copyRequest != null)
				dbFetcher.pendingQueue.remove(copyRequest);
		}
	}
	
	private String getClipFromHeader(InterOperatorHttpResponse ioHttpResponse)
	{
		Header[] headers = ioHttpResponse.getHttpHeaders();
		String targetClipId = null;
		for (Header header : headers)
		{
			if(header.getName().equalsIgnoreCase("TARGET_CLIPID"))
				targetClipId = header.getValue();
		}
		return targetClipId;
	}
	
	private Map<String,List<String>> getAtlantisOptMap() {
		String strAtlantisOptMap = RBTParametersUtils.getParamAsString("RDC", "ATLANTIS_OPERATOR_NAMES_FOR_CONTENT_RESOLVING", "TATACDMA:TTML,TTSL;IDEA:IDEA,IDEARN");
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
