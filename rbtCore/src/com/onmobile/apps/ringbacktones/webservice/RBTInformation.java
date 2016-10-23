/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice;

import java.awt.image.BufferedImage;

import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vinayasimha.patil
 *
 */
public interface RBTInformation
{
	public Document getRBTInformationDocument(WebServiceContext task);
	public Document getSpecificRBTInformationDocument(WebServiceContext task);
	public Document getSubscriptionResponseDocument(WebServiceContext task);
	public Document getSubscriptionPreConsentResponseDocument(WebServiceContext task);
	public Document getSelectionPreConsentResponseDocument(WebServiceContext task);
	public Document getSelIntegrationPreConsentResponseDocument(WebServiceContext task);
	public Document getSelectionResponseDocument(WebServiceContext task);
	public Document getBookMarkResponseDocument(WebServiceContext task);
	public Document getGroupResponseDocument(WebServiceContext task);
	public Document getAffiliateGroupResponseDocument(WebServiceContext task);
	public Document getCopyResponseDocument(WebServiceContext task);
	public Document getGiftResponseDocument(WebServiceContext task);
	public Document getScratchCardResponseDocument(WebServiceContext task);
	public Document getValidateNumberResponseDocument(WebServiceContext task);
	public Document getSetSubscriberDetailsResponseDocument(WebServiceContext task);
	public Document getUtilsResponseDocument(WebServiceContext task);
	public Document getApplicationDetailsDocument(WebServiceContext task);
	public Document getDataDocument(WebServiceContext task);
	public Document getBulkUploadTasks(WebServiceContext task);
	public Document getSngResponseDocument(WebServiceContext task);
	/**
	 * 
	 * @author Sreekar
	 */
	public Document getOfferDocument(WebServiceContext task);
	
	public BufferedImage getQRCodeImage(WebServiceContext task);
}
