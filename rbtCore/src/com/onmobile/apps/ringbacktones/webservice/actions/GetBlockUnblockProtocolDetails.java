package com.onmobile.apps.ringbacktones.webservice.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;
import com.onmobile.apps.ringbacktones.webservice.implementation.util.RBTProtocolBean;

public class GetBlockUnblockProtocolDetails implements WebServiceAction, WebServiceConstants{
	Logger logger = Logger.getLogger(GetBlockUnblockProtocolDetails.class);
	
	private Document responseDoc;
	
	public WebServiceResponse processAction(WebServiceContext webServiceContext){
		String protocolLogFilePath = RBTParametersUtils.getParamAsString(
				"COMMON", "PROTOCOL_LOGFILE_PATH", null);
		
		String subscriberId = webServiceContext.getString(param_subscriberID);
		
		
		if(null == protocolLogFilePath){
			logger.info("Protocol log file path not configured!!");
			return null;
		}
		
		logger.info("Protocol log file path:"+ protocolLogFilePath);
		
		File file = new File(protocolLogFilePath);
		
		if(!file.exists()){
			logger.info("Protocol log file not found in the configured path!!");
			return null;
		}
		
		try {
			responseDoc = populateTransactionDetails( file,subscriberId );
		} catch (FileNotFoundException e) {
			logger.error("Error while reading Protocol Transactions"+e);
		} catch(IOException e){
			logger.error("Error while reading Protocol Transactions"+e);
		} catch(ParseException e){
			logger.error("Error while parsing request time"+e);
		}
		
		return getWebServiceResponse(responseDoc);
	}

	private Document populateTransactionDetails( File protocolLogFile,String subscriberId) throws IOException, ParseException {
		List<RBTProtocolBean> protocolBeanList = new ArrayList<RBTProtocolBean>();
		BufferedReader protocolReader = new BufferedReader(new FileReader(protocolLogFile));
		String transactionEntry = protocolReader.readLine();
		
		while(null !=transactionEntry){
			populateProtocolBeanList(protocolBeanList, transactionEntry, subscriberId);	
			transactionEntry = protocolReader.readLine();
		}
		
		protocolReader.close();

		return generateProtocolTransResponse(protocolBeanList);
	}

	private Document generateProtocolTransResponse(
			List<RBTProtocolBean> protocolBeanList) {
		DocumentBuilder documentBuilder = XMLUtils.getDocumentBuilder();

		Document document = documentBuilder.newDocument();
		Element rootElement = document.createElement(PROTOCOLS);
		
		for( RBTProtocolBean bean : protocolBeanList ){
			Element ele = document.createElement(PROTOCOL);
			
			Element transactionType = document.createElement("transactionType");
			transactionType.appendChild(document.createTextNode(bean.getTransactionType()));
            ele.appendChild(transactionType);
            
            Element protocolNum = document.createElement("protocolNum");
            protocolNum.appendChild(document.createTextNode(bean.getProtocolNum().toString()));
            ele.appendChild(protocolNum);
            
            Element subscriberId = document.createElement("subscriberId");
            subscriberId.appendChild(document.createTextNode(bean.getSubscriberId()));
            ele.appendChild(subscriberId);
            
            Element requestTime = document.createElement("requestTime");
            requestTime.appendChild(document.createTextNode(bean.getRequestTime()));
            ele.appendChild(requestTime);
			
			rootElement.appendChild(ele);
		}
		document.appendChild(rootElement);
		return document;
	}

	private void populateProtocolBeanList(List<RBTProtocolBean> protocolBeanList, String transactionEntry, String subscriberId) throws ParseException {
		String entries[] = transactionEntry.split(",");
		
		if(subscriberId.equalsIgnoreCase(entries[1])){
			RBTProtocolBean protocolBean = new RBTProtocolBean();
			protocolBean.setSubscriberId(entries[1]);
			protocolBean.setTransactionType(entries[2]);
			protocolBean.setProtocolNum(Long.parseLong(entries[3]));
			protocolBean.setRequestTime(entries[0]);
			
			protocolBeanList.add(protocolBean);
		}
	}

	private WebServiceResponse getWebServiceResponse(Document document) {
		WebServiceResponse webServiceResponse = Utility.getWebServiceResponseXML(document);
		return webServiceResponse;
	}
}
