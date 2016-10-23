package com.onmobile.apps.ringbacktones.daemons.doubleConfirmation.threads;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;


/**
 * This class is responsible to handle the XML response when a request is made to CG.
 * @author manjunatha.c
 *
 */
public class ComvivaResponseHandler extends BasicResponseHandler{
	static Logger logger = Logger.getLogger(ComvivaResponseHandler.class);
	
	public String processResponse(int respCode, String responseStr ){
		ComvivaResponse consentObj = null;
		String responseString  = "ERROR";
		
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance( ComvivaResponse.class );
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			
				StringReader responseReader = new StringReader( responseStr );
				consentObj = ( ComvivaResponse ) jaxbUnmarshaller.unmarshal( responseReader );
		} catch (JAXBException e) {
			logger.error("Unable to parse xml consent response from CG!!"+ e);
		}
		
		
		if( null !=  consentObj ){
			if( null != consentObj.getError_code() && consentObj.getError_code().equalsIgnoreCase( "0" ) ){
				responseString = "SUCCESS";
			}
		}
		
		
		return responseString;
	}
	
}
