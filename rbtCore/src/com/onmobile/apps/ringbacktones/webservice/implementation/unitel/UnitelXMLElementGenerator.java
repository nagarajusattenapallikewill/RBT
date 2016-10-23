package com.onmobile.apps.ringbacktones.webservice.implementation.unitel;

import java.text.SimpleDateFormat;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberDownload;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceSubscriberSetting;
//RBT-6459 : Unitel-Angola---- API Development for Online CRM System
public class UnitelXMLElementGenerator implements WebServiceConstants {
	
	public static Element addSubscriberSettingContentElement(Document document, Element element,
			WebServiceContext task, WebServiceSubscriberSetting webServiceSubscriberSetting) {
        Utility.addPropertyElement(document, element, param_album, DATA, webServiceSubscriberSetting.getAlbumName());
		Utility.addPropertyElement(document, element, param_artist, DATA, webServiceSubscriberSetting.getArtistName());
		Utility.addPropertyElement(document, element, param_genre, DATA, webServiceSubscriberSetting.getCategoryName());
		Utility.addPropertyElement(document, element, param_price, DATA, webServiceSubscriberSetting.getTonePrice());
		Utility.addPropertyElement(document, element, param_default_music, DATA, 
				Boolean.valueOf(webServiceSubscriberSetting.isDefaultMusic()).toString());
		return element;
	}

	public static Element addSubscriberDownloadContentElement(Document document, Element element,
			WebServiceContext task, WebServiceSubscriberDownload webServiceSubscriberDownload) {
		Utility.addPropertyElement(document, element, param_album, DATA, webServiceSubscriberDownload.getAlbumName());
		Utility.addPropertyElement(document, element, param_artist, DATA, webServiceSubscriberDownload.getArtistName());
		Utility.addPropertyElement(document, element, param_genre, DATA, webServiceSubscriberDownload.getCategoryName());
		Utility.addPropertyElement(document, element, param_price, DATA, webServiceSubscriberDownload.getTonePrice());
		Utility.addPropertyElement(document, element, param_default_music, DATA, 
				Boolean.valueOf(webServiceSubscriberDownload.isDefaultMusic()).toString());
		return element;
	}
	
	public static Element getSubscriberDefaultSettingContentElement(Document document,
			WebServiceContext task, WebServiceSubscriberSetting webServiceSubscriberSetting) {
	Element element = document.createElement(CONTENT);
	element.setAttribute(ID, String.valueOf(webServiceSubscriberSetting.getToneID()));
	element.setAttribute(NAME, webServiceSubscriberSetting.getToneName());
	element.setAttribute(TYPE, webServiceSubscriberSetting.getToneType());

	Utility.addPropertyElement(document, element, CALLER_ID, DATA, webServiceSubscriberSetting.getCallerID());
	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	
	String[] previewFiles = webServiceSubscriberSetting.getPreviewFiles();
	String[] rbtFiles = webServiceSubscriberSetting.getRbtFiles();
	Utility.addPropertyElement(document, element, PREVIEW_FILE, PROMPT, Utility.getPromptName(previewFiles[0]));
	Utility.addPropertyElement(document, element, RBT_FILE, PROMPT, Utility.getPromptName(rbtFiles[0]));
	Utility.addPropertyElement(document, element, param_album, DATA, webServiceSubscriberSetting.getAlbumName());
	Utility.addPropertyElement(document, element, param_artist, DATA, webServiceSubscriberSetting.getArtistName());
	if(DBConfigTools.getParameter(iRBTConstant.COMMON, iRBTConstant.ADD_VCODE_IN_XML_RESPONSE, false)){
	   Utility.addPropertyElement(document, element, VCODE, DATA, webServiceSubscriberSetting.getClipVcode());
	}
	Utility.addPropertyElement(document, element, param_genre, DATA, webServiceSubscriberSetting.getCategoryName());
	Utility.addPropertyElement(document, element, param_price, DATA, webServiceSubscriberSetting.getTonePrice());
	Utility.addPropertyElement(document, element, SET_TIME, DATA, dateFormat.format(webServiceSubscriberSetting.getSetTime()));
	Utility.addPropertyElement(document, element, NEXT_BILLING_DATE, DATA, "20370101000000");
	Utility.addPropertyElement(document, element, param_default_music, DATA, 
			Boolean.valueOf(webServiceSubscriberSetting.isDefaultMusic()).toString());
	return element;
	}
	
}
