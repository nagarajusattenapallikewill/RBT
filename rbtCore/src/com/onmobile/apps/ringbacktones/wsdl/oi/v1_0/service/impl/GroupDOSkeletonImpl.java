package com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.impl;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GroupRequest;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.AddMembers;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Content_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Contents_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.CreateGroup;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.CreateGroupResponse;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.GroupDOSkeleton;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.GroupDetails_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.GroupMembers_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Group_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Groups_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.ModifyGroup;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Property_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.Rbt_type0;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.RemoveGroup;
import com.onmobile.apps.ringbacktones.wsdl.oi.v1_0.service.RemoveMember;

public class GroupDOSkeletonImpl extends GroupDOSkeleton {
	Logger logger = Logger.getLogger(GroupDOSkeletonImpl.class);

	@Override
	public CreateGroupResponse modifyGroup(ModifyGroup modifyGroup10) {
		GroupRequest groupRequest = new GroupRequest(
				String.valueOf(modifyGroup10.getSubscriberID()));
		groupRequest.setGroupName(modifyGroup10.getGroupName());
		groupRequest.setGroupID(modifyGroup10.getGroupId());
		return groupOperations(groupRequest, modifyGroup10.getAction());
	}

	@Override
	public CreateGroupResponse addMembers(AddMembers addMembers6) {
		GroupRequest groupRequest = new GroupRequest(String.valueOf(addMembers6
				.getSubscriberID()));
		groupRequest.setGroupID(addMembers6.getGroupId());
		groupRequest.setMemberID(String.valueOf(addMembers6.getMemberId()));
		groupRequest.setMemberName(addMembers6.getMemberName());
		return groupOperations(groupRequest, addMembers6.getAction());
	}

	@Override
	public CreateGroupResponse createGroup(CreateGroup createGroup4) {
		GroupRequest groupRequest = new GroupRequest(
				String.valueOf(createGroup4.getSubscriberID()));
		groupRequest.setGroupName(createGroup4.getGroupName());
		String predefinedGroupID = createGroup4.getPredefinedGroupID() > 0 ? String.valueOf(createGroup4.getPredefinedGroupID()) : null; 
		groupRequest.setPredefinedGroupID(predefinedGroupID);
		return groupOperations(groupRequest, createGroup4.getAction());
	}

	@Override
	public CreateGroupResponse removeGroup(RemoveGroup removeGroup8) {
		GroupRequest groupRequest = new GroupRequest(
				String.valueOf(removeGroup8.getSubscriberID()));
		groupRequest.setGroupID(removeGroup8.getGroupId());
		return groupOperations(groupRequest, removeGroup8.getAction());
	}

	@Override
	public CreateGroupResponse removeMember(RemoveMember removeMember12) {
		GroupRequest groupRequest = new GroupRequest(
				String.valueOf(removeMember12.getSubscriberID()));
		groupRequest.setGroupID(removeMember12.getGroupId());
		groupRequest.setMemberID(String.valueOf(removeMember12.getMemberId()));
		return groupOperations(groupRequest, removeMember12.getAction());
	}

	private CreateGroupResponse groupOperations(GroupRequest groupRequest,
			String action) {
		CreateGroupResponse response = new CreateGroupResponse();
		Rbt_type0 rbtType = new Rbt_type0();
		try {
			Document document = RBTClient.getInstance().groupAction(
					groupRequest, action);
			Element responseElement = (Element) document.getElementsByTagName(
					"response").item(0);
			String responseMsg = responseElement.getTextContent();
			logger.info("Webservice response: " + responseMsg);
			rbtType.setResponse(responseMsg);
			if (!responseMsg.equalsIgnoreCase("success")) {
				response.setRbt(rbtType);
				return response;
			}
			GroupDetails_type0 groupDetails = new GroupDetails_type0();
			Groups_type0 groupsType = new Groups_type0();
			GroupMembers_type0 groupMembersType = new GroupMembers_type0();
			Contents_type0 contentsType = new Contents_type0();

			Element groupDetailsElement = (Element) document
					.getElementsByTagName("group_details").item(0);
			Element groupsElement = (Element) groupDetailsElement
					.getElementsByTagName("groups").item(0);
			groupsType.setNoOfActiveGroups(Integer.parseInt(groupsElement
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
					Element contentElement = (Element) contentList.item(x);
					NodeList propertyList = contentElement
							.getElementsByTagName("property");
					Content_type0 contentObj = new Content_type0();
					contentObj.setId(contentElement.getAttribute("id"));
					contentObj.setName(contentElement.getAttribute("name"));
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
			Element groupMembersElement = (Element) groupMembersList.item(0);
			groupMembersType.setAllMembers(groupMembersElement
					.getAttribute("all_members"));
			if (groupMembersElement.hasChildNodes()) {
				NodeList groupList = groupMembersElement.getChildNodes();
				logger.info("Group List" + groupList);
				for (int x = 0; x < groupList.getLength(); x++) {
					Group_type0 groupType = new Group_type0();
					Contents_type0 contentsType1 = new Contents_type0();
					Element groupElement = (Element) groupList.item(x);
					groupType.setGroupId(groupElement.getNodeName().substring(6));
					groupType.setNoOfActiveMembers(Integer
							.parseInt(groupElement
									.getAttribute("no_of_active_members")));
					groupType.setNoOfMembers(Integer.parseInt(groupElement
							.getAttribute("no_of_members")));
					NodeList contentsList1 = groupElement
							.getElementsByTagName("contents");
					Element contentsElement1 = (Element) contentsList1.item(0);
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
							contentsType1.addContent(contentObj);
						}
						groupType.setContents(contentsType1);
					}
					groupMembersType.addGroup(groupType);
				}
			}
			groupDetails.setGroupMembers(groupMembersType);
			rbtType.setGroupDetails(groupDetails);
			response.setRbt(rbtType);

		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

}
