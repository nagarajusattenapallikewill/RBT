package com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.impl;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ContentRequest;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Content_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Contents_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Property_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Rbt_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Search;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.SearchDOSkeleton;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.SearchResponse;

public class SearchDOSkeletonImpl extends SearchDOSkeleton {
	@Override
	public SearchResponse search(Search search0) {
		Logger logger = Logger.getLogger(SearchDOSkeletonImpl.class);
		Rbt_type0 rbtType = new Rbt_type0();
		SearchResponse response = new SearchResponse();
		ContentRequest request = new ContentRequest();
		request.setSearchText(search0.getSearchText());
		int maxResults=search0.getMaxResult();
		int pageNo = search0.getPageNo();
		request.setMaxResult((maxResults>0)?String.valueOf(search0.getMaxResult()):"100");
		request.setPageNo((pageNo>0)?search0.getPageNo():0);
		request.setType(search0.getType());
		try {
			Document document = RBTClient.getInstance().searchAction(request);
			Contents_type0 contentsType = new Contents_type0();
			NodeList contentsList = document.getElementsByTagName("contents");
			Element contentsElement = (Element) contentsList.item(0);
			if (contentsElement.hasChildNodes()) {
				NodeList contentList = contentsElement
						.getElementsByTagName("content");
				for (int x = 0; x < contentList.getLength(); x++) {
					Element contentElement = (Element) contentList.item(x);
					NodeList propertyList = contentElement
							.getElementsByTagName("property");
					Content_type0 contentObj = new Content_type0();
					contentObj.setId(contentElement.getAttribute("id"));
					contentObj.setName(contentElement.getAttribute("name"));
					contentObj.setType(contentElement.getAttribute("type"));
					for (int i = 0; i < propertyList.getLength(); i++) {
						Element propertyElement = (Element) propertyList
								.item(i);
						Property_type0 propertyType = new Property_type0();
						propertyType.setName(propertyElement
								.getAttribute("name"));
						propertyType.setType(propertyElement
								.getAttribute("type"));
						propertyType.setValue(propertyElement
								.getAttribute("value"));
						contentObj.addProperty(propertyType);
					}

					contentsType.addContent(contentObj);
				}
			}
			rbtType.setContents(contentsType);
			logger.info("RBT_TYPE: "+rbtType);
			response.setRbt(rbtType);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

}
