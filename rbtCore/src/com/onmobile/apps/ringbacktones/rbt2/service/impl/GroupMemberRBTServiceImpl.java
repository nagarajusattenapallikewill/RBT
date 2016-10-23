package com.onmobile.apps.ringbacktones.rbt2.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.processor.impl.GroupMemberAbstractProcessor;
import com.onmobile.apps.ringbacktones.rbt2.service.IGroupMemberService;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.CommonValidation;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMemberList;

public class GroupMemberRBTServiceImpl implements IGroupMemberService, Constants{
	
	
	
	private static Logger logger = Logger.getLogger(GroupMemberRBTServiceImpl.class);
	
	private ResponseErrorCodeMapping errorCodeMapping;
	
	
	@Autowired
	private ApplicationContext context;
	@Autowired
	private CommonValidation commonValidation;
	
	
	public void setCommonValidation(CommonValidation commonValidation) {
		this.commonValidation = commonValidation;
	}

	public void setContext(ApplicationContext context) {
		this.context = context;
	}


	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}

	@Override
	public List<GroupMember> removeMemberFromGroup(String msisdn, String mode,
			int groupId, String callerId) throws UserException {
		
		logger.info("inside removeMemberFromGroup in GroupMemberRBTServiceImpl msisdn: "+msisdn +" , mode :" +mode +" , groupId :"+groupId +" , callerId: "+callerId);
		GroupMemberAbstractProcessor groupMemberProcessor = (GroupMemberAbstractProcessor) ConfigUtil.getBean(BeanConstant.GROUP_MEMBER_PROCESSOR);
		try{
			List<GroupMember> memberList = new ArrayList<GroupMember>();
			GroupMember  member = new GroupMember(String.valueOf(groupId), callerId, null, null);
			memberList.add(member);
			
			return groupMemberProcessor.removeMemberFromGroup(msisdn,mode,
					groupId, memberList );
		}catch(Exception e){
			ServiceUtil.throwCustomUserException(errorCodeMapping,e.getMessage(),MessageResource.GROUP_MEMBER_REMOVE_MESSAGE_FOR);
		}
		return null;
	}

	@Override
	public GroupMemberList getAllGroupMember(String msisdn, int groupId, int pagesize,
			int offset) throws UserException {
		logger.info("inside getAllGroupMember in GroupMemberRBTServiceImpl msisdn: "+msisdn +" , groupId :" +groupId +" , pagesize :"+pagesize +" , offset: "+offset);
		GroupMemberAbstractProcessor groupMemberProcessor = (GroupMemberAbstractProcessor) ConfigUtil.getBean(BeanConstant.GROUP_MEMBER_PROCESSOR);
		try{
			List<GroupMember> allMembersFromGroup = groupMemberProcessor.getAllMembersFromGroup(msisdn,groupId);
			int totalSize = allMembersFromGroup.size();

			allMembersFromGroup = ServiceUtil.paginatedSubList(allMembersFromGroup, pagesize, offset);
			
			GroupMemberList groupMemberList = new GroupMemberList(allMembersFromGroup);
			groupMemberList.setTotalSize(totalSize);
			groupMemberList.setOffset(offset == -1 ? 0 : offset);
			groupMemberList.setCount(allMembersFromGroup.size());
			
			return groupMemberList;
		}catch(Exception e){
			ServiceUtil.throwCustomUserException(errorCodeMapping,e.getMessage(),MessageResource.GROUP_MEMBER_GET_MESSAGE_FOR);
		}
		return null;
	}

	@Override
	public List<GroupMember> addMemberToGroup(String msisdn, String mode, int groupId,
			Long callerId, String callerName) throws UserException {
		logger.info("inside addMemberToGroup in GroupMemberRBTServiceImpl msisdn: "+msisdn +" , mode :" +mode +" , groupId :"+groupId +" , callerId: "+callerId+ " , callerName:"+callerName);
		GroupMemberAbstractProcessor groupMemberProcessor = (GroupMemberAbstractProcessor) ConfigUtil.getBean(BeanConstant.GROUP_MEMBER_PROCESSOR);
		try{
			List<GroupMember> memberList = new ArrayList<GroupMember>();
			GroupMember  member = new GroupMember(String.valueOf(groupId), String.valueOf(callerId), callerName, null);
			memberList.add(member);
			return groupMemberProcessor.addMemberToGroup(msisdn, mode, groupId, memberList);
		}catch(Exception e){
			ServiceUtil.throwCustomUserException(errorCodeMapping,e.getMessage(),MessageResource.GROUP_MEMBER_ADD_MESSAGE_FOR);
		}
		return null;
	
	}



	
}
