package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorUtility;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.services.common.Utility;

public class DTOCMnpOnlineImpl implements MSISDNServiceDefinition{

	private static Logger logger = Logger.getLogger(MnpOnlineImpl.class);

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.services.msisdninfo.MSISDNServiceDefinition#getSubscriberDetail(java.lang.String)
	 */
	@Override
	public SubscriberDetail getSubscriberDetail(MNPContext mnpContext) {
		String subscriberID = Utility.trimCountryPrefix(mnpContext.getSubscriberID());
		String circleID = null;
		boolean isPrepaid = false;
		boolean isValidSubscriber = false;
		HashMap<String, String> subscriberDetailsMap = null;
		if(!Utility.isValidNumber(subscriberID)) {
			return new SubscriberDetail(subscriberID,circleID,isPrepaid,isValidSubscriber,subscriberDetailsMap);
		}
		
		//Area Code to be in the form of 11:9,12:8,13:10 etc. i.e. areaCode:length
		Parameters areaParameter = CacheManagerUtil.getParametersCacheManager().
		          					getParameter(iRBTConstant.COMMON, "AREA_CODE_FOR_PHONE_NUMBER_LENGTH", null);
		if(areaParameter!=null && subscriberID != null){
		  String paramVal = areaParameter.getValue();
		  if(paramVal!=null){
			 String token[] = paramVal.split(",");
               for(int i=0;i<token.length;i++){
					String areaCodeToken[] = token[i].split(":");
					if(subscriberID.startsWith(areaCodeToken[0])){
						try{
							int areaCodeLength = Integer.parseInt(areaCodeToken[1]);
							if(subscriberID.length()== (areaCodeLength+areaCodeToken[0].length()))
							  subscriberID = subscriberID.substring(areaCodeToken[0].length());
						}catch(Exception e){
							e.printStackTrace();
						}
 				  }
              }
		   }
		}
		
		// Make a hit to MNP URL
		String mnpUrl = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "MNP_URL", null);
		
		if(mnpUrl == null) {
			return new SubscriberDetail(subscriberID,circleID,isPrepaid,isValidSubscriber,subscriberDetailsMap);
		}
		
		HashMap<String, String> mnpRequestParameters = new HashMap<String, String>();
		mnpRequestParameters.put("msisdn", subscriberID);
		ContentInterOperatorHttpResponse mnpHttpResponse = ContentInterOperatorHttpUtils
				.getResponse(mnpUrl, mnpRequestParameters, null);

		String mnpResponse = mnpHttpResponse.getHttpResponseString();
		Document mnpResponseDoc = null;
		try {
			mnpResponseDoc = getDocumentFromResponse(mnpResponse);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(mnpResponseDoc == null) {
			return new SubscriberDetail(subscriberID,circleID,isPrepaid,isValidSubscriber,subscriberDetailsMap);
		}
		
		String operatorName = getValueFomDoc(mnpResponseDoc, "Customer");
		String circleName = getValueFomDoc(mnpResponseDoc, "Circle");

		if(null == operatorName || null == circleName) {
			return new SubscriberDetail(subscriberID,circleID,isPrepaid,isValidSubscriber,subscriberDetailsMap,operatorName,circleName);
		}
		
		logger.debug("Made a hit to MNP_URL: " + mnpUrl
				+ ", subscriberId: " + subscriberID + ", mnpResponse: "
				+ mnpResponse + ", operatorName: " + operatorName
				+ ", circleName: " + circleName);
		
		
		int operatorId = ContentInterOperatorUtility
				.getOperatorIDFromMNPOperatorName(operatorName);
		int interchangedOperatorId = ContentInterOperatorUtility
				.getInterchangedOperatorId(operatorId, circleName);
		String rbtOperatorName = ContentInterOperatorUtility
				.getRBTOperatorNameFromOperatorID(String
						.valueOf(interchangedOperatorId));
		
		
		if(rbtOperatorName != null) {
			isValidSubscriber = true;
		}
		
		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID,
				rbtOperatorName+"_"+circleName, isPrepaid, isValidSubscriber, subscriberDetailsMap,rbtOperatorName,circleName);
		

		String otpSupportedOperators = CacheManagerUtil.getParametersCacheManager().getParameterValue(iRBTConstant.COMMON, "OTP_SUPPORTED_OPERATOR", null);
		List<String> otpSupportedOperatorList = null;
		if (otpSupportedOperators != null) {
			otpSupportedOperatorList = Arrays.asList(otpSupportedOperators.split(","));
		}
		if (otpSupportedOperatorList != null
				&& ((operatorName != null && !otpSupportedOperatorList
				.contains(operatorName)) || (subscriberDetail != null && !otpSupportedOperatorList
				.contains(subscriberDetail.getCircleID())))) {
			subscriberDetail.setValidOperator(false);
		}
		
		logger.info("RBT:: subscriberDetail: " + subscriberDetail);
		
		return subscriberDetail;

		
//		String operatorUrl = ContentInterOperatorUtility.operatorNameUrlMap
//				.get(rbtOperatorName);
//
//		logger.debug("Configured operatorUrl: " + operatorUrl
//				+ " for rbtOperatorName: " + rbtOperatorName);

		
	}
	
	private Document getDocumentFromResponse(String mnpResponse)
			throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		if (null != mnpResponse) {
			InputStream is = new ByteArrayInputStream(mnpResponse.getBytes());
			Document mnpResponseDoc = builder.parse(is);
			return mnpResponseDoc;
		}
		return builder.newDocument();
	}
	
	private String getValueFomDoc(Document mnpResponseDoc, String tagName) {
		if (null != mnpResponseDoc) {
			return mnpResponseDoc.getElementsByTagName(tagName).item(0)
					.getFirstChild().getNodeValue();
		}
		return null;
	}
	
	

}

