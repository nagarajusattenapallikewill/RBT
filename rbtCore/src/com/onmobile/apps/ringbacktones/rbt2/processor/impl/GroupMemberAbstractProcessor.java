package com.onmobile.apps.ringbacktones.rbt2.processor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.rbt.CallingParty.CallingPartyType;
import com.onmobile.apps.ringbacktones.content.GroupMembers;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedGroups;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedSubStatus;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.daemon.RBTPlayerUpdateDaemonWrapper;
import com.onmobile.apps.ringbacktones.rbt2.db.SubscriberSelection;
import com.onmobile.apps.ringbacktones.rbt2.db.impl.GroupMembersDBImpl;
import com.onmobile.apps.ringbacktones.rbt2.db.impl.GroupsDBImpl;
import com.onmobile.apps.ringbacktones.rbt2.processor.IGroupMemberProcessor;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.webservice.RBTAdminFacade;
import com.onmobile.apps.ringbacktones.webservice.RBTProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public abstract class GroupMemberAbstractProcessor implements IGroupMemberProcessor, Constants{
	
	
	static Logger logger = Logger.getLogger(GroupMemberAbstractProcessor.class);

	public GroupMemberAbstractProcessor() {
	}
	
	
	public final List<GroupMember> addMemberToGroup(String msisdn, String mode, int groupId, List<GroupMember> groupMembers) throws Exception{
		
		//1. Subscriber Validation check
		//Predefiend group validation, if not exist, create predefined group -- super class has contorl and subcall can modifiy the implementation
		//2. Group id is belongs to subscriber, if not to create group or not --- subclass to decide 
		//3. add members to the group
		//4. return members (recently added) from group
		
		List<GroupMember> memberList = new ArrayList<GroupMember>();
		
		try{
			if (!isSubscriberActive(msisdn)) {
				throw new Exception(SUB_DONT_EXIST);
			}
			if(!isValidGroup(msisdn,groupId)){
				throw new Exception(INVALID_GROUP_ID);
			}
			memberList = addGroupMembers(msisdn,groupId, groupMembers,mode);
		}catch(Exception e){
			logger.info("Exception occured while adding member from group: "+e.getMessage());
			throw new Exception(e.getMessage());
		}
		return memberList;
	}
	
	public final List<GroupMember> removeMemberFromGroup(String msisdn, String mode,
			int groupId, List<GroupMember> groupMembers) throws Exception {
		
		List<GroupMember> memberList = new ArrayList<GroupMember>();
		
		try{
			if (!isSubscriberActive(msisdn)) {
				throw new Exception(SUB_DONT_EXIST);
			}
			
			if(!isValidGroup(msisdn,groupId)){
				throw new Exception(INVALID_GROUP_ID);
			}
			memberList = deleteGroupMembers(msisdn, groupId,groupMembers);
		}catch(Exception e){
			logger.info("Exception occured while removing member from group: "+e.getMessage());
			throw new Exception(e.getMessage());
		}
		return memberList;
	}
	
	

	public final List<GroupMember> getAllMembersFromGroup(String msisdn,int groupId) throws Exception{
		List<GroupMember> memberList = new ArrayList<GroupMember>();
		
		try{
			if (!isSubscriberActive(msisdn)) {
				throw new Exception(SUB_DONT_EXIST);
			}
			
			if(!isValidGroup(msisdn,groupId)){
				throw new Exception(INVALID_GROUP_ID);
			}
			memberList = getAllGroupMembers(msisdn, groupId);
		}catch(Exception e){
			logger.info("Exception occured while getting member from group: "+e.getMessage());
			throw new Exception(e.getMessage());
		}
		return memberList;
	
	}

	
	protected List<GroupMember> getValidMemberList(int groupId, List<GroupMember> groupMembers, boolean isAddMember) throws Exception {
		
		List<GroupMember> memberList = new ArrayList<GroupMember>();
		String memberIDs = null;
		boolean isValid = isAddMember;
		
		GroupMember groupMember = null;
		if(groupMembers !=null && groupMembers.size() > 0){
			groupMember = groupMembers.get(0);
			
		if(groupMember.getMemberID() == null) {
			throw new Exception(MEMBER_ID_NULL);
		}
	
		memberIDs = groupMember.getMemberID();
		
		String[] validCallerIds = DataUtils.getValidCallerIds(memberIDs);
		if(validCallerIds != null && validCallerIds.length > 0){
			GroupMembers[] members = RBTDBManager.getInstance().getActiveMembersForGroupID(groupId);
			if(members != null && members.length > 0){
				for(GroupMembers member : members){
					if(member.callerID().equals(validCallerIds[0])){
						if(isAddMember){
							throw new Exception(MEMBER_ALREADY_PRESENT);
						}
						else{
							isValid = true;
						}
						break;
					}
						
				}
				if(isValid){
					groupMember.setMemberID(validCallerIds[0]);
					memberList.add(groupMember);
					return memberList;
				}
				else{
					throw new Exception(MEMBER_NOT_PRESENT);
				}
			}
			else{
				if(!isAddMember) {
					throw new Exception(MEMBER_NOT_PRESENT);
				}
				else{
					groupMember.setMemberID(validCallerIds[0]);
					memberList.add(groupMember);
					return memberList;
				}
			}
		}
		 return null;
		}else{
			throw new Exception();
		}
	}

	
	protected boolean isSubscriberActive(String subscriberID){
		
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
		boolean status = false;

		if (subscriber != null
				&& (rbtDBManager.isSubscriberActivated(subscriber)
				|| rbtDBManager.isSubscriberActivationPending(subscriber))) {
			status = true;
		}
		logger.info("isSubscriberActive is returning :"+status);
		return status;
		
	}
	
	private boolean isValidGroup(String subscriberID, int groupid){
		boolean validGroup = false;
		if(groupid == 99){
			return true;
		}
		
		List<ExtendedGroups> activeGroups = getGroupsBySubscriberId(subscriberID);
		if(activeGroups != null && activeGroups.size() > 0){
			for(ExtendedGroups group: activeGroups){
				if(groupid == group.groupID()){
					validGroup = true;
				}
			}
		}
		logger.info("isValidGroup is returning: "+validGroup);
		return validGroup;
	}
	
	@Override
	public List<GroupMember> deleteGroupMembers(String msisdn, int groupId, List<GroupMember> groupMembers) throws Exception {
		List<GroupMember> memberlist = new ArrayList<GroupMember>();
		try{
		if(groupMembers != null && groupMembers.size() >0){
			int preGroupId = -1;
			if(groupId == 99){
				preGroupId = groupId;
				ExtendedGroups group = getBlockGroupID(msisdn);
				if(group != null){
					groupId = group.groupID();
				}
			}
			List<GroupMember> validMemberList =null;
			  validMemberList = getValidMemberList(groupId,groupMembers, false);
			 if(validMemberList != null && validMemberList.size() > 0){
				boolean deleted = GroupMembersDBImpl.deleteGroupMembers(groupId, validMemberList);
				if(deleted){
					memberlist.add(groupMembers.get(0));
					//RBT-16132 added for calling tone player
					if(isUpdateTonePlayer()) {
						updateTonePlayer(msisdn,groupId,preGroupId);
					}
				}
		    }else{
		    	throw new Exception(INVALID_GROUP_MEMBER_ID);
		    }
		}
		}catch(Exception e){
			throw e;
		}
		return memberlist;
	}
	
	@Override
	public List<GroupMember> addGroupMembers(String msisdn, int groupId, List<GroupMember> groupMembers, String mode) throws Exception{
		logger.info("inside addGroupMembers ");
		List<GroupMember> memberlist = new ArrayList<GroupMember>();
		try{
			int preGroupId = -1;
			if(groupMembers != null && groupMembers.size() >0){
				if(groupId == 99){
					ExtendedGroups group = getBlockGroupID(msisdn);
					if(group != null){
						groupId = group.groupID();
						preGroupId = Integer.parseInt((group.preGroupID() != null ? group.preGroupID() : "-1"));
					}
				}
				 List<GroupMember> validMemberList =null;
					validMemberList = getValidMemberList(groupId,groupMembers,true);
					if(validMemberList != null && validMemberList.size() > 0){					
						validMemberList.get(0).setMemberStatus(STATE_ACTIVATED);
						boolean added = GroupMembersDBImpl.addGroupMembers(groupId, validMemberList);
						if(added){
							memberlist = validMemberList;
							if(preGroupId == 99){
								HashMap<String, String> map = new HashMap<String, String>();
								map.put(WebServiceConstants.param_subscriberID, msisdn);
								map.put(WebServiceConstants.param_callerID, validMemberList.get(0).getMemberID());
								map.put(WebServiceConstants.param_isDirectDeactivation, WebServiceConstants.YES);
								map.put(WebServiceConstants.param_dtocRequest, WebServiceConstants.YES);
								map.put(WebServiceConstants.param_mode, mode);
								WebServiceContext task = Utility.getTask(map);
								RBTAdminFacade.initialize();
								RBTProcessor rbtProcessor = RBTAdminFacade.getRBTProcessorObject(task);
								String deleteContentResponse = rbtProcessor.deleteSetting(task);
								logger.info("Selection deactivation status for Bloccked user is : "+deleteContentResponse);
							}
							//RBT-16132 added for calling tone player
							if(isUpdateTonePlayer()) {
								updateTonePlayer(msisdn,groupId,preGroupId);
							}
					 }
				    }else{
			    	throw new Exception(INVALID_GROUP_MEMBER_ID);
			    }
			}
		}catch(Exception e){
			throw e;
		}
		return memberlist;
	}
	
	@Override
	public List<GroupMember> getAllGroupMembers(String msisdn, int groupId){
		List<GroupMember> memberlist = new ArrayList<GroupMember>();
		if(groupId == 99){
			ExtendedGroups group = getBlockGroupID(msisdn);
			if(group != null ){
				groupId = group.groupID();
			}
		}
		GroupMembers[] members = RBTDBManager.getInstance().getActiveMembersForGroupID(groupId);
		if(members != null && members.length > 0){
			memberlist = convertGroupMembers(Arrays.asList(members));
		}
		return memberlist;
	}
	
	private List<GroupMember> convertGroupMembers(List<GroupMembers> groupMembers){
		List<GroupMember> memberlist = new ArrayList<GroupMember>();
		for(GroupMembers members:groupMembers){
			GroupMember member = new GroupMember(String.valueOf(members.groupID()), members.callerID(),members.callerName(),members.status());
			memberlist.add(member);
		}
		return memberlist;
	}
	
	public ExtendedGroups getBlockGroupID(String msisdn){
		
		List<ExtendedGroups> activeGroups = getGroupsBySubscriberId(msisdn);
		if(activeGroups != null && activeGroups.size() > 0){
			for (ExtendedGroups group : activeGroups) {
				if(group.preGroupID()!= null && group.preGroupID().equals("99")) {
					return group;
				}
			}
		   }
		return createBlockdGroup(msisdn);
	}
	
	
	protected ExtendedGroups createBlockdGroup(String msisdn){
		String response = RBTDBManager.getInstance().addGroupForSubscriberID("99", "BLOCKED", msisdn, null);
		if(response.equals("GROUP_ADDED_SUCCESFULLY")){
			return getBlockGroupID(msisdn);
		}
		return null;
	}
	
	protected List<ExtendedGroups> getGroupsBySubscriberId(String msisdn){
		ExtendedGroups groupReqBean = new ExtendedGroups(-1, null, msisdn, null, null, null, true, false);
		List<ExtendedGroups> groups = GroupsDBImpl.getGroups(groupReqBean);
		logger.info(" getGroupsBySubscriberId returning groups size: "+groups);
		return groups;
	}
	
	protected boolean isUpdateTonePlayer(){
		return true;		
	}
	
	//RBT-16132 added for calling tone player
	protected final void updateTonePlayer(String msisdn,int groupId,int preGroupId){
		logger.info("updateTonePlayer called for msisdn: "+msisdn +" ,groupId : "+groupId +" , preGroupId: "+preGroupId);
		if(preGroupId == 99){
		   logger.info("Updating tone player for group Id: "+groupId);
		   RBTPlayerUpdateDaemonWrapper.getInstance().updateSubscribersInTonePlayer(RBTDBManager.getInstance().getSubscriber(msisdn), false);
		}else{
			SubscriberSelection subscriberSelection = (SubscriberSelection) ConfigUtil.getBean(BeanConstant.SUBSCRIBER_SELECTION_IMPL);
			ExtendedSubStatus extendedSubStatus = new ExtendedSubStatus();
			extendedSubStatus.setType("GROUP");
			extendedSubStatus.setSubId(msisdn);
			extendedSubStatus.setCallerId("G"+groupId);
			List<ExtendedSubStatus> extendedSubStatusList = subscriberSelection.getSelections(extendedSubStatus);
			if(extendedSubStatusList != null && extendedSubStatusList.size() >0){
				logger.info("Updating tone player for group Id: "+groupId);
				RBTPlayerUpdateDaemonWrapper.getInstance().addSelectionsToTonePlayer(RBTDBManager.getInstance().getSubscriber(msisdn));
			}
		  }
		}
}

