package com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.impl;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Content_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Contents_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Downloads_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Library_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Property_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Rbt_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.RemoveSongFromLibrary;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.RemoveSongFromSelection;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Selection;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.SelectionDOSkeleton;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.SelectionResponse;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Settings_type0;

public class SelectionDOSkeletonImpl extends SelectionDOSkeleton {
	
	@Override
	public SelectionResponse selection(Selection selection2) {
		SelectionRequest selectionRequest = new SelectionRequest(
				String.valueOf(selection2.getSubscriberID()));
		selectionRequest.setCallerID(selection2.getCallerID());
		selectionRequest.setCategoryID(String.valueOf(selection2
				.getCategoryID()));
		selectionRequest.setChargeClass(selection2.getChargeClass());
		selectionRequest.setClipID(String.valueOf(selection2.getClipID()));
		selectionRequest.setInLoop(selection2.getInLoop());
		selectionRequest.setMode(selection2.getMode());
		selectionRequest.setModeInfo(selection2.getModeInfo());
		int offerId=selection2.getOfferID();
		selectionRequest.setOfferID((offerId>0)?String.valueOf(offerId):"");
		selectionRequest
				.setSubscriptionClass(selection2.getSubscriptionClass());
		int subscriptionOfferID = selection2.getSubscriptionOfferID();
		selectionRequest
				.setSubscriptionOfferID((subscriptionOfferID > 0) ? String
						.valueOf(subscriptionOfferID) :"");
		selectionRequest.setUseUIChargeClass(selection2.getUseUIChargeClass());
		return getSelection(selectionRequest, selection2.getAction());
	}
	
	
	@Override
	public SelectionResponse removeSongFromLibrary(
			RemoveSongFromLibrary removeSongFromLibrary4) {
		SelectionRequest request = new SelectionRequest(
				String.valueOf(removeSongFromLibrary4.getSubscriberID()));
		request.setClipID(String.valueOf(removeSongFromLibrary4.getClipID()));
		request.setMode(removeSongFromLibrary4.getMode());
		return getSelection(request, removeSongFromLibrary4.getAction());
	}

	
	@Override
	public SelectionResponse removeSongFromSelection(
			RemoveSongFromSelection removSongFromSelection6) {
		SelectionRequest request = new SelectionRequest(
				String.valueOf(removSongFromSelection6.getSubscriberID()));
		request.setClipID(removSongFromSelection6.getClipID()+"");
		request.setCallerID(String.valueOf(removSongFromSelection6
				.getCallerID()));
		request.setMode(removSongFromSelection6.getMode());
		return getSelection(request, removSongFromSelection6.getAction());
	}
	
	
	private SelectionResponse getSelection(SelectionRequest selectionRequest,
			String action) {
		Logger logger = Logger.getLogger(SelectionDOSkeletonImpl.class);
		Rbt_type0 rbtType = new Rbt_type0();
		SelectionResponse response = new SelectionResponse();
		try {
			Document document = RBTClient.getInstance().selectionAction(
					selectionRequest, action);
			Element responseElement = (Element) document.getElementsByTagName(
					"response").item(0);
			String responseMsg = responseElement.getTextContent();
			logger.info("Webservice response: " + responseMsg);
			rbtType.setResponse(responseMsg);
			if (!responseMsg.equalsIgnoreCase("success")) {
				response.setRbt(rbtType);
				return response;
			}
			Contents_type0 contentsType = new Contents_type0();
			Settings_type0 settingsType = new Settings_type0();
			Library_type0 libraryType = new Library_type0();
			Element settingsElement = (Element) document.getElementsByTagName(
					"settings").item(0);
			settingsType.setNoOfDefaultSettings(Integer
					.parseInt(settingsElement
							.getAttribute("no_of_default_settings")));
			settingsType.setNoOfSettings(Integer.parseInt(settingsElement
					.getAttribute("no_of_settings")));
			settingsType.setNoOfSpectialSettings(Integer
					.parseInt(settingsElement
							.getAttribute("no_of_special_settings")));

			NodeList contentsList = settingsElement
					.getElementsByTagName("contents");
			Element contentsElement = (Element) contentsList.item(0);
			NodeList contentList = contentsElement
					.getElementsByTagName("content");
			for (int x = 0; x < contentList.getLength(); x++) {
				Element contentElement = (Element) contentList.item(x);
				NodeList propertyList = contentElement
						.getElementsByTagName("property");
				Content_type0 contentObj = new Content_type0();
				contentObj.setId(contentElement.getAttribute("id"));
//				contentObj.setId(Long.parseLong(contentElement
//						.getAttribute("id")));
				contentObj.setName(contentElement.getAttribute("name"));
				contentObj.setType(contentElement.getAttribute("type"));
				for (int i = 0; i < propertyList.getLength(); i++) {
					Element propertyElement = (Element) propertyList.item(i);
					Property_type0 propertyType = new Property_type0();
					propertyType.setName(propertyElement.getAttribute("name"));
					propertyType.setType(propertyElement.getAttribute("type"));
					propertyType
							.setValue(propertyElement.getAttribute("value"));
					contentObj.addProperty(propertyType);
				}
				contentsType.addContent(contentObj);
			}
			settingsType.setContents(contentsType);
			libraryType.setSettings(settingsType);
			Element downloadsElement = (Element) document.getElementsByTagName(
					"downloads").item(0);
			Downloads_type0 downloadsType = new Downloads_type0();
			Contents_type0 downloadsContentsType = new Contents_type0();
			downloadsType.setNoOfActiveDownloads(Integer
					.parseInt(downloadsElement
							.getAttribute("no_of_active_downloads")));
			downloadsType.setNoOfDownloads(Integer.parseInt(downloadsElement
					.getAttribute("no_of_downloads")));
			NodeList downloadsContentsList = downloadsElement
					.getElementsByTagName("contents");
			Element downloadsContentsElement = (Element) downloadsContentsList
					.item(0);
			NodeList downloadsContentList = downloadsContentsElement
					.getElementsByTagName("content");
			for (int x = 0; x < downloadsContentList.getLength(); x++) {
				Element contentElement = (Element) downloadsContentList.item(x);
				NodeList propertyList = contentElement
						.getElementsByTagName("property");
				Content_type0 contentObj = new Content_type0();
				contentObj.setId(contentElement.getAttribute("id"));
				contentObj.setName(contentElement.getAttribute("name"));
				contentObj.setType(contentElement.getAttribute("type"));
				for (int i = 0; i < propertyList.getLength(); i++) {
					Element propertyElement = (Element) propertyList.item(i);
					Property_type0 propertyType = new Property_type0();
					propertyType.setName(propertyElement.getAttribute("name"));
					propertyType.setType(propertyElement.getAttribute("type"));
					propertyType
							.setValue(propertyElement.getAttribute("value"));
					contentObj.addProperty(propertyType);
				}
				downloadsContentsType.addContent(contentObj);
			}
			downloadsType.setContents(downloadsContentsType);
			libraryType.setDownloads(downloadsType);
			rbtType.setLibrary(libraryType);
			response.setRbt(rbtType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
