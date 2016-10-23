package com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.impl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ContentRequest;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.ContentDOSkeleton;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Content_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Contents_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Property_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.QueryContent;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.QueryContentResponse;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Rbt_type0;

public class ContentDOSkeletonImpl extends ContentDOSkeleton {
	@Override
	public QueryContentResponse queryContent(QueryContent queryContent0) {
		Rbt_type0 rbtType = new Rbt_type0();
		Contents_type0 contentsType = new Contents_type0();
		QueryContentResponse response = new QueryContentResponse();
		ContentRequest contentRequest = new ContentRequest();

		try {
			contentRequest.setContentID(queryContent0.getContentId());
			contentRequest.setCircleID(queryContent0.getCircleID());
			contentRequest.setContentType(queryContent0.getContentType());
			contentRequest.setPageNo(queryContent0.getPageNo());
			contentRequest.setLanguage(queryContent0.getLanguage());
			Document document = RBTClient.getInstance().getContent(
					contentRequest);
			NodeList contentsList = document.getElementsByTagName("contents");
			Element contentsElement = (Element) contentsList.item(0);
			contentsType.setEndIndex(Integer.parseInt(contentsElement
					.getAttribute("end_index")));
			contentsType.setNoOfContents(Integer.parseInt(contentsElement
					.getAttribute("no_of_contents")));
			contentsType.setStartIndex(Integer.parseInt(contentsElement
					.getAttribute("start_index")));
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
			response.setRbt(rbtType);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return response;
	}
}
