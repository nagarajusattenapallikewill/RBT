package com.onmobile.apps.ringbacktones.services.msisdninfo;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.bean.ContentInterOperatorHttpResponse;
import com.onmobile.apps.ringbacktones.daemons.contentinteroperator.tools.ContentInterOperatorHttpUtils;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.common.Utility;

public class RDCMNPImpl implements MSISDNServiceDefinition {

	private static Logger logger = Logger.getLogger(RDCMNPImpl.class);

	@Override
	public SubscriberDetail getSubscriberDetail(MNPContext mnpContext) {
		logger.info(mnpContext.toString());
		String subscriberID = Utility.trimCountryPrefix(mnpContext.getSubscriberID());
		String circleID = null;
		boolean isValidSubscriber = false;
		boolean isPrepaid = false;
		HashMap<String, String> subscriberDetailsMap = null;

		String mnpUrl = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "MNP_RDC_URL", null);

		if (mnpUrl == null) {
			return null;
		}

		HashMap<String, String> mnpRequestParameters = new HashMap<String, String>();
		mnpRequestParameters.put("msisdn", subscriberID);
		ContentInterOperatorHttpResponse mnpHttpResponse = ContentInterOperatorHttpUtils.getResponse(mnpUrl,
				mnpRequestParameters, null);

		String mnpResponse = mnpHttpResponse.getHttpResponseString();
		Document mnpResponseDoc = null;
		try {
			mnpResponseDoc = getDocumentFromResponse(mnpResponse);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (mnpResponseDoc == null) {
			return null;
		}

		String operatorName = getValueFomDoc(mnpResponseDoc, "Customer");
		String circleName = getValueFomDoc(mnpResponseDoc, "Circle");
		
		if (circleName != null && operatorName != null) {
			String thirdPartyCircleName = Utility.getThirdPartyMappedCircleID(operatorName+"_"+circleName);
			circleID = Utility.getMappedCircleID(thirdPartyCircleName);

			if (circleID != null) {
				SitePrefix sitePrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(circleID);
				if (sitePrefix != null && sitePrefix.getSiteUrl() == null)
					isValidSubscriber = true;

			}
		} else
			logger.info("Circle ID not found in MNP");

		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.COMMON,
				"DEFAULT_USER_TYPE", "POSTPAID");
		if (parameter != null)
			isPrepaid = parameter.getValue().trim().equalsIgnoreCase("PREPAID");

		SubscriberDetail subscriberDetail = new SubscriberDetail(subscriberID, circleID, isPrepaid, isValidSubscriber,
				subscriberDetailsMap);
		logger.info("subscriberDetail: "+subscriberDetail);
		return subscriberDetail;
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
			return mnpResponseDoc.getElementsByTagName(tagName).item(0).getFirstChild().getNodeValue();
		}
		return null;
	}
	

}
