package com.onmobile.apps.ringbacktones.webservice.common;

import org.apache.log4j.Logger;

/**
 * 
 * @author gaurav.khandelwal
 *
 */

public class URLBuilder {

	private static Logger logger = Logger.getLogger(URLBuilder.class);

	private String url;

	public URLBuilder(String url) {
		this.url = url;
	}

	public URLBuilder replaceMsisdn(String msisdn) {

		if(msisdn != null && !msisdn.isEmpty())
			this.url = this.url.replace("<MSISDN>", msisdn);
		return this;
	}

	public URLBuilder replaceVCode(String vCode) {

		if (vCode != null && !vCode.isEmpty()) {
			this.url = this.url.replace("<VCODE>", vCode);
		} else {
			this.url = this.url.replace("<VCODE>", "");
		}

		return this;
	}

	public URLBuilder replaceUCode(String uCode) {

		if (uCode != null && !uCode.isEmpty()) {
			this.url = this.url.replace("<UCODE>", uCode);
		} else {
			this.url = this.url.replace("<UCODE>", "");
		}

		return this;
	}

	public URLBuilder replaceCallerId(String callerId) {

		if (callerId != null && !callerId.isEmpty()) {
			this.url = this.url.replace("<CBMSISDN>", callerId);
		} else {
			this.url = this.url.replace("<CBMSISDN>", "");
		}

		return this;
	}
	
	public URLBuilder replaceCircle(String circle) {
		if(circle != null && !circle.isEmpty())
			this.url = this.url.replace("<CIRCLEID>", circle);
		return this;
	}
	
	public URLBuilder replaceOperator(String operator) {
		if(operator != null && !operator.isEmpty())
			this.url = this.url.replace("<OPERATOR>", operator);
		return this;
	}
	
	public URLBuilder replaceWavFileName(String wavFileName) {
		if(wavFileName != null && !wavFileName.isEmpty())
			this.url = this.url.replace("<WAVFILENAME>", wavFileName);
		return this;
	}
	
	public URLBuilder replacePresentFlag(String presentFlag) {
		if(presentFlag != null && !presentFlag.isEmpty())
			this.url = this.url.replace("<PRESENTFLAG>", presentFlag);
		return this;
	}
	
	//RBT-16252	Comviva addselection api->Static value is passed in ConsentParam parameter
	public URLBuilder replaceConsentParam(boolean isActiveUser) {

		if (isActiveUser) {
			this.url = this.url.replace("<CONSENTPARAM>", "");
		} else {
			this.url = this.url.replace("<CONSENTPARAM>", "C");
		}
		return this;
	}

	
	public URLBuilder replaceConsentParam(String consentParam) {

		if(consentParam != null && !consentParam.isEmpty())
			this.url = this.url.replace("<CONSENTPARAM>", consentParam);
		return this;
	}
	
	public URLBuilder replaceCptId(String cptid) {

		if(cptid != null && !cptid.isEmpty())
			this.url = this.url.replace("<CPTID>", cptid);
		return this;
	}
	
	public URLBuilder replaceFileName(String fileName) {
		
		if(fileName != null && !fileName.isEmpty()){
			this.url = this.url.replace("<FILENAME>", fileName);
		}
		return this;
	}

	public URLBuilder replaceMode(String mode) {

		if (mode != null && !mode.isEmpty()) {
			this.url = this.url.replace("<CHANNEL>", mode);
		} else {
			this.url = this.url.replace("<CHANNEL>", "");
		}

		return this;
	}

	public String buildUrl() {
		return this.url;
	}
	
	

}
