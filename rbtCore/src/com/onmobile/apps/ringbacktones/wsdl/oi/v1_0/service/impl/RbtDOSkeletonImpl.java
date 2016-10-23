package com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.impl;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Content_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Contents_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Downloads_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.GroupDetails_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.GroupMembers_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Group_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Groups_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Library_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Property_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.QueryRbt;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.QueryRbtResponse;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.RbtDOSkeleton;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Rbt_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Settings_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Subscriber_type0;

public class RbtDOSkeletonImpl extends RbtDOSkeleton {

	@Override
	public QueryRbtResponse queryRbt(QueryRbt queryRbt0) {
		Logger logger = Logger.getLogger(RbtDOSkeletonImpl.class);
		Rbt_type0 rbtType = new Rbt_type0();
		QueryRbtResponse response = new QueryRbtResponse();
		RbtDetailsRequest detailsRequest = new RbtDetailsRequest(
				String.valueOf(queryRbt0.getSubscriberID()));
		detailsRequest.setMode(queryRbt0.getMode());
		detailsRequest.setInfo(queryRbt0.getInfo());
		try {
			Document document = RBTClient.getInstance().rbtAction(
					detailsRequest);
			Element responseElement = (Element) document.getElementsByTagName(
					"response").item(0);
			String responseMsg = responseElement.getTextContent();
			logger.info("Webservice response: " + responseMsg);
			if (!responseMsg.equalsIgnoreCase("success")) {
				rbtType.setResponse(responseMsg);
				response.setRbt(rbtType);
				return response;
			}
			rbtType.setResponse(responseElement.getTextContent());

			String info = queryRbt0.getInfo();
			String[] arrInfo = info.split("\\,");

			for (String strInfo : arrInfo) {
				if (strInfo.equalsIgnoreCase("library")) {
					Contents_type0 contentsType = new Contents_type0();
					Settings_type0 settingsType = new Settings_type0();
					Library_type0 libraryType = new Library_type0();
					Element settingsElement = (Element) document
							.getElementsByTagName("settings").item(0);
					settingsType.setNoOfDefaultSettings(Integer
							.parseInt(settingsElement
									.getAttribute("no_of_default_settings")));
					settingsType.setNoOfSettings(Integer
							.parseInt(settingsElement
									.getAttribute("no_of_settings")));
					settingsType.setNoOfSpectialSettings(Integer
							.parseInt(settingsElement
									.getAttribute("no_of_special_settings")));
					NodeList contentsList = settingsElement
							.getElementsByTagName("contents");
					Element contentsElement = (Element) contentsList.item(0);
					if (contentsElement.hasChildNodes()) {
						NodeList contentList = contentsElement
								.getElementsByTagName("content");
						for (int x = 0; x < contentList.getLength(); x++) {
							Element contentElement = (Element) contentList
									.item(x);
							NodeList propertyList = contentElement
									.getElementsByTagName("property");
							Content_type0 contentObj = new Content_type0();
							contentObj.setId(contentElement.getAttribute("id"));
							contentObj.setName(contentElement
									.getAttribute("name"));
							contentObj.setType(contentElement
									.getAttribute("type"));
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
					settingsType.setContents(contentsType);
					libraryType.setSettings(settingsType);
					Element downloadsElement = (Element) document
							.getElementsByTagName("downloads").item(0);
					Downloads_type0 downloadsType = new Downloads_type0();
					Contents_type0 downloadsContentsType = new Contents_type0();
					downloadsType.setNoOfActiveDownloads(Integer
							.parseInt(downloadsElement
									.getAttribute("no_of_active_downloads")));
					downloadsType.setNoOfDownloads(Integer
							.parseInt(downloadsElement
									.getAttribute("no_of_downloads")));
					if (downloadsElement.hasChildNodes()) {
						NodeList downloadsContentsList = downloadsElement
								.getElementsByTagName("contents");
						Element downloadsContentsElement = (Element) downloadsContentsList
								.item(0);
						NodeList downloadsContentList = downloadsContentsElement
								.getElementsByTagName("content");
						for (int x = 0; x < downloadsContentList.getLength(); x++) {
							Element contentElement = (Element) downloadsContentList
									.item(x);
							NodeList propertyList = contentElement
									.getElementsByTagName("property");
							Content_type0 contentObj = new Content_type0();
							contentObj.setId(contentElement.getAttribute("id"));
							contentObj.setName(contentElement
									.getAttribute("name"));
							contentObj.setType(contentElement
									.getAttribute("type"));
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
							downloadsContentsType.addContent(contentObj);
						}
					}
					downloadsType.setContents(downloadsContentsType);
					libraryType.setDownloads(downloadsType);
					rbtType.setLibrary(libraryType);
				} else if (strInfo.equalsIgnoreCase("subscriber")) {
					Subscriber_type0 subscriberType = new Subscriber_type0();
					Element subscriberElement = (Element) document
							.getElementsByTagName("subscriber").item(0);
					subscriberType.setAccessCount(subscriberElement
							.getAttribute("access_count"));
					subscriberType.setActivatedBy(subscriberElement
							.getAttribute("activated_by"));
					subscriberType.setCanAllow(subscriberElement
							.getAttribute("can_allow"));
					subscriberType.setCircleId(subscriberElement
							.getAttribute("circle_id"));
					if (subscriberElement.hasAttribute("cos_id")) {
						subscriberType.setCosId(Integer
								.parseInt(subscriberElement
										.getAttribute("cos_id")));
					}
					if (subscriberElement.hasAttribute("is_prepaid")) {
						subscriberType.setIsPrepaid(subscriberElement
								.getAttribute("is_prepaid"));
					}
					if (subscriberElement.hasAttribute("is_valid_prefix")) {
						subscriberType.setIsValidPrefix(subscriberElement
								.getAttribute("is_valid_prefix"));
					}
					if (subscriberElement.hasAttribute("language")) {
						subscriberType.setLanguage(subscriberElement
								.getAttribute("language"));
					}
					if (subscriberElement.hasAttribute("next_billing_date")) {
						subscriberType.setNextBillingDate(Long
								.parseLong(subscriberElement
										.getAttribute("next_billing_date")));
					}
					if (subscriberElement.hasAttribute("ref_id")) {
						subscriberType.setRefId(subscriberElement
								.getAttribute("ref_id"));
					}
					if (subscriberElement.hasAttribute("status")) {
						subscriberType.setStatus(subscriberElement
								.getAttribute("status"));
					}
					if (subscriberElement.hasAttribute("subscriber_id")) {
						subscriberType.setSubscriebrId(Long
								.parseLong(subscriberElement
										.getAttribute("subscriber_id")));
					}
					if (subscriberElement.hasAttribute("subscription_class")) {
						subscriberType.setSubscriptionClass(subscriberElement
								.getAttribute("subscription_class"));
					}
					if (subscriberElement.hasAttribute("user_type")) {
						subscriberType.setUserType(subscriberElement
								.getAttribute("user_type"));
					}
					if (subscriberElement.hasAttribute("VOLUNTARY")) {
						subscriberType.setVoluntary(subscriberElement
								.getAttribute("VOLUNTARY"));
					}
					if (subscriberElement.hasAttribute("activation_date")) {
						subscriberType.setActivation_date(subscriberElement
								.getAttribute("activation_date"));
					}
					if (subscriberElement.hasAttribute("start_date")) {
						subscriberType.setStart_date(subscriberElement
								.getAttribute("start_date"));
					}
					if (subscriberElement.hasAttribute("end_date")) {
						subscriberType.setEnd_date(subscriberElement
								.getAttribute("end_date"));
					}
					rbtType.setSubscriber(subscriberType);
				} else if (strInfo.equalsIgnoreCase("group_details")) {
					GroupDetails_type0 groupDetails = new GroupDetails_type0();
					Groups_type0 groupsType = new Groups_type0();
					GroupMembers_type0 groupMembersType = new GroupMembers_type0();
					Contents_type0 contentsType = new Contents_type0();

					Element groupDetailsElement = (Element) document
							.getElementsByTagName("group_details").item(0);
					Element groupsElement = (Element) groupDetailsElement
							.getElementsByTagName("groups").item(0);
					groupsType.setNoOfActiveGroups(Integer
							.parseInt(groupsElement
									.getAttribute("no_of_active_groups")));
					groupsType.setNoOfGroups(Integer.parseInt(groupsElement
							.getAttribute("no_of_groups")));
					NodeList contentsList = groupsElement
							.getElementsByTagName("contents");
					Element contentsElement = (Element) contentsList.item(0);
					logger.info("contentsElement: " + contentsElement);
					if (contentsElement.hasChildNodes()) {
						NodeList contentList = contentsElement
								.getElementsByTagName("content");
						for (int x = 0; x < contentList.getLength(); x++) {
							Element contentElement = (Element) contentList
									.item(x);
							NodeList propertyList = contentElement
									.getElementsByTagName("property");
							Content_type0 contentObj = new Content_type0();
							contentObj.setId(contentElement.getAttribute("id"));
							contentObj.setName(contentElement
									.getAttribute("name"));
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
					groupsType.setContents(contentsType);
					groupDetails.setGroups(groupsType);
					NodeList groupMembersList = groupDetailsElement
							.getElementsByTagName("group_members");
					logger.info("groupMembersList: " + groupMembersList);
					Element groupMembersElement = (Element) groupMembersList
							.item(0);
					groupMembersType.setAllMembers(groupMembersElement
							.getAttribute("all_members"));
					if (groupMembersElement.hasChildNodes()) {
						NodeList groupList = groupMembersElement
								.getChildNodes();
						logger.info("Group List" + groupList);
						for (int x = 0; x < groupList.getLength(); x++) {
							Group_type0 groupType = new Group_type0();
							Contents_type0 contentsType1 = new Contents_type0();
							Element groupElement = (Element) groupList.item(x);
							groupType.setGroupId(groupElement.getNodeName()
									.substring(6));
							groupType
									.setNoOfActiveMembers(Integer.parseInt(groupElement
											.getAttribute("no_of_active_members")));
							groupType.setNoOfMembers(Integer
									.parseInt(groupElement
											.getAttribute("no_of_members")));
							NodeList contentsList1 = groupElement
									.getElementsByTagName("contents");
							Element contentsElement1 = (Element) contentsList1
									.item(0);
							logger.info("contents : " + contentsElement1);
							if (contentsElement1.hasChildNodes()) {
								NodeList contentList1 = contentsElement1
										.getElementsByTagName("content");
								for (int x1 = 0; x1 < contentList1.getLength(); x1++) {
									Element contentElement = (Element) contentList1
											.item(x1);
									NodeList propertyList = contentElement
											.getElementsByTagName("property");
									Content_type0 contentObj = new Content_type0();
									contentObj.setId(contentElement.getAttribute("id"));
									contentObj.setName(contentElement
											.getAttribute("name"));
									for (int i = 0; i < propertyList
											.getLength(); i++) {
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
									contentsType1.addContent(contentObj);
								}
								groupType.setContents(contentsType1);
							}
							groupMembersType.addGroup(groupType);
						}
						groupDetails.setGroupMembers(groupMembersType);
					}
					rbtType.setGroupDetails(groupDetails);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		response.setRbt(rbtType);
		return response;

	}
}
