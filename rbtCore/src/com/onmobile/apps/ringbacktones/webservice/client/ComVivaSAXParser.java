package com.onmobile.apps.ringbacktones.webservice.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.common.ComVivaConfigurations;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class ComVivaSAXParser implements IXMLParser, WebServiceConstants{

	private static Logger logger = Logger.getLogger(ComVivaSAXParser.class);
	
	@Override
	public Rbt getRBT(Parser parser) {
		SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
		String response = parser.getResponse();
		
		Rbt rbt = null;
		SaxHandler handler = null;
		if(response != null) {
			try {
				SAXParser saxParser = saxParserFactory.newSAXParser();
				handler = new SaxHandler(parser.getSubscriberId());
				ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(response.trim().getBytes("UTF-8"));
				saxParser.parse(byteArrayInputStream, handler);
			} catch (ParserConfigurationException e) {
				logger.error("Exception Occured: "+e, e);
			} catch (SAXException e) {
				logger.error("Exception Occured: "+e, e);
			} catch (IOException e) {
				logger.error("Exception Occured: "+e, e);
			}
			
			if(handler.getComvivaConsent() != null) {
				rbt = new Rbt();
				
				if(handler.getComvivaConsent().getCpTransactionId()!=null && !handler.getComvivaConsent().getCpTransactionId().trim().equals("")){
				 rbt.setConsent(handler.getComvivaConsent());
				}
				 if(ComVivaConfigurations.getInstance().getValueFromResourceBundle("RESPONSE_CODE_" + handler.getCodeValue()) !=  null) {
						parser.getRequest().setResponse(ComVivaConfigurations.getInstance().getValueFromResourceBundle("RESPONSE_CODE_" + handler.getCodeValue()));
					}else {
						parser.getRequest().setResponse("FAILURE");
					}
			} else if(handler.getSettings() != null) {
				rbt = new Rbt();
				if(handler.getCodeValue() != null && handler.getCodeValue().equals("-1")){
					parser.getRequest().setResponse(WebServiceConstants.FAILURE);
				}else{
					parser.getRequest().setResponse(WebServiceConstants.SUCCESS);
				}
				
				Library library = new Library();
				library.setSettings(handler.getSettings());
				rbt.setSubscriber(handler.getSubscriber());
				rbt.setLibrary(library);
			}
		}
		return rbt;
	}

}
