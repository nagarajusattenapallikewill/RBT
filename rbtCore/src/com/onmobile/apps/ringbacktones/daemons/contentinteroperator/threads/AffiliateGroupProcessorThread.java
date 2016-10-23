package com.onmobile.apps.ringbacktones.daemons.contentinteroperator.threads;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.content.RDCGroups;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorUtility;

public class AffiliateGroupProcessorThread extends Thread
{
	private AffiliateGroupDBFetcher dbFetcher = null;
	private static Logger logger = Logger.getLogger(ContentInterOperatorDBFetcher.class);
	private static Logger mnpPushLogger = Logger.getLogger("mnpPushLogger");
	
	/**
	 * @param dbFetcher
	 */
	public AffiliateGroupProcessorThread(AffiliateGroupDBFetcher dbFetcher)
	{
		this.dbFetcher = dbFetcher;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run()
	{
		while(true)
		{
			RDCGroups copyRequest = null; 
			synchronized (dbFetcher.contentQueue)
			{
				if(dbFetcher.contentQueue.size() > 0)
				{
					logger.info("Mnp thread found contentrequest, " + dbFetcher.contentQueue.get(0));
					copyRequest = dbFetcher.contentQueue.remove(0);
					dbFetcher.pendingQueue.add(copyRequest);
				}
				else
				{
					try
					{
						logger.info("Mnp thread waiting as queue size="+dbFetcher.contentQueue.size());
						dbFetcher.contentQueue.wait();
					}
					catch (InterruptedException e)
					{
						logger.info("Mnp thread interrupted. Will check queue now");
					}
					continue;
				}	
			}
			createDeleteGroup(copyRequest);
			dbFetcher.pendingQueue.remove(copyRequest);
		}	
	}
	
	/**
	 * @param group
	 */
	private String createDeleteGroup(RDCGroups group)
	{
		String operatorName = group.optName();
		String url = getOperatorUrl(operatorName);
		if(url == null)
		{
			logger.info("Operator url not found for sequenceId = " + group.groupID());
			return "INVALID_OPERATOR_ID";
		}

		if(!operatorName.equals("RELIANCE") && !operatorName.equals("UNINOR_COMVIVA")) {
			if (url != null && url.indexOf("//") != -1 && url.indexOf("/", url.indexOf("//") + 2) != -1)
				url = url.substring(0, url.indexOf("/", url.indexOf("//") + 2));
			
			url = url + "/rbt/Group.do?";
		}
		
		HashMap<String, String> parametersMap = new HashMap<String, String>();

		int groupId = group.groupID();
		String groupName = group.groupName();
		String extraInfo = group.extraInfo();
		int groupStatus = Integer.parseInt(group.groupStatus());
		
		
		if (groupStatus != 5) {
			// pass charge class, time based settings
			parametersMap.put("groupID", "G" +groupId);
			parametersMap.put("groupName", groupName);
			parametersMap.put("processAllCircles", "true");
			parametersMap.put("action", "add");
			
			if(groupStatus == 2 || groupStatus == 4) {
				parametersMap.put("action", "remove");
			}
			
			Map<String, String> extraInfoMap = DBUtility.getAttributeMapFromXML(extraInfo);
			if(extraInfoMap != null && extraInfoMap.containsKey("failedCircles")) {
				parametersMap.put("toBeProcessCircles", extraInfoMap.get("failedCircles"));
			}
			
			ContentInterOperatorHttpResponse ioHttpResponse = ContentInterOperatorHttpUtils.getResponse(url, parametersMap, null);
			String httpResponseString = ioHttpResponse.getHttpResponseString();
			String response = null;
			if (httpResponseString != null) {			
				try {
					ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(httpResponseString.getBytes("UTF-8"));
					DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					Document document = documentBuilder.parse(byteArrayInputStream);
					Element responseElem = (Element) document.getElementsByTagName("response").item(0);
					Text responseText = (Text) responseElem.getFirstChild();
					response = responseText.getNodeValue();							
				}
				catch (Exception e) {
					logger.debug(
							"Failed to parse response: "
									+ httpResponseString);
					logger.error(e);
				}
			}
			
			extraInfo = null;
			if(!"success".equalsIgnoreCase(response)) {
				if(groupStatus == 1 || groupStatus == 3) {
					groupStatus = 3;				
				}
				else if(groupStatus == 2 || groupStatus == 4) {
					groupStatus = 4;				
				}
				extraInfoMap = new HashMap<String, String>();
				extraInfoMap.put("failedCircles", response);
				extraInfo = DBUtility.getAttributeXMLFromMap(extraInfoMap);
			}
			else {
				groupStatus = 5;
			}
			
			RBTDBManager.getInstance().updateAffiliateGroupStatus(groupId, groupStatus, extraInfo);		
			
		}
		else {
			String affiliateUrl = RBTParametersUtils.getParamAsString("CONTENT_INTER_OPERATORABILITY", "AFFILIATE_CALLBACK_URL", null);
			if(affiliateUrl != null) {
				affiliateUrl = affiliateUrl.replace("<groupId>", groupId+"");
				affiliateUrl = affiliateUrl.replace("<optname>", operatorName);
				
				ContentInterOperatorHttpResponse ioHttpResponse = ContentInterOperatorHttpUtils.getResponse(affiliateUrl, null, null);
				
				if(ioHttpResponse.getHttpResponseCode() == 200)
				{
					String responseString = ioHttpResponse.getHttpResponseString();
					responseString = responseString.trim();
					if (responseString.equalsIgnoreCase("SUCCESS")) {
						RBTDBManager.getInstance().updateAffiliateGroupStatus(groupId, 6, null);
					}
				}
			}
		}
		return "SUCCESS";
	}
	
	private String getOperatorUrl(String  operatorName)
	{
		if(operatorName == null)
			return null;

		String url = ContentInterOperatorUtility.operatorNameUrlMap.get(operatorName);
		return url;
	}
	
	public static void main(String[] args) {
		String groupId = null;
		if(args != null) {
			groupId = args[0].trim();
			RDCGroups group = RBTDBManager.getInstance().getRDCGroup(Integer.parseInt(groupId));
			System.out.println(new AffiliateGroupProcessorThread(null).createDeleteGroup(group));
		}
		
	}
}

