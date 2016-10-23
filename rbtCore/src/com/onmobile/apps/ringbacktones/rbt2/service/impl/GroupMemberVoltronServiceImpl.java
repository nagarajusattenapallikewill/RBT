package com.onmobile.apps.ringbacktones.rbt2.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.i18n.LocaleContextHolder;

import com.livewiremobile.store.storefront.dto.rbt.Caller;
import com.livewiremobile.store.storefront.dto.rbt.CallerList;
import com.livewiremobile.store.storefront.dto.rbt.Pager;
import com.onmobile.apps.ringbacktones.v2.bean.ResponseErrorCodeMapping;
import com.onmobile.apps.ringbacktones.v2.common.Constants;
import com.onmobile.apps.ringbacktones.v2.common.MessageResource;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.factory.BuildCaller;
import com.onmobile.apps.ringbacktones.v2.factory.BuildCallerList;
import com.onmobile.apps.ringbacktones.rbt2.service.IGroupMemberService;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMember;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupMemberList;

public class GroupMemberVoltronServiceImpl implements IGroupMemberService, Constants{
	
	
	
	private Logger logger = Logger.getLogger(GroupMemberVoltronServiceImpl.class);
	private Locale locale = LocaleContextHolder.getLocale();
	
	@Autowired
	private ApplicationContext applicationContext;
	private ResponseErrorCodeMapping errorCodeMapping;
	
	@Autowired
	private IGroupMemberService groupMemberService;
	
	public void setGroupMemberService(IGroupMemberService groupMemberService) {
		this.groupMemberService = groupMemberService;
	}

	public ResponseErrorCodeMapping getErrorCodeMapping() {
		return errorCodeMapping;
	}

	public void setErrorCodeMapping(ResponseErrorCodeMapping errorCodeMapping) {
		this.errorCodeMapping = errorCodeMapping;
	}

	
	private Map<String, String> constructSuccessMap(String responseMessage, String response){
		Map<String,String> returnMap = new HashMap<String, String>(2);
		String defaultMessage = responseMessage + response.toLowerCase() +" not configured.";
		returnMap.put("message",applicationContext.getMessage(responseMessage + response.toLowerCase(), null, defaultMessage, locale));		
		returnMap.put("code",errorCodeMapping.getErrorCode(response.toLowerCase()).getCode());
		return returnMap;
		
	}

	@Override
	public Map<String, String> removeMemberFromGroup(String msisdn, String mode,
			int groupId, String callerId) throws UserException {
		logger.info("inside removeMemberFromGroup in GroupMemberVoltronServiceImpl msisdn: "+msisdn +" , mode :" +mode +" , groupId :"+groupId +" , callerId: "+callerId);
		
		try{
		
			List<GroupMember> removedMembersFromGroup = (List<GroupMember>)groupMemberService.removeMemberFromGroup(msisdn, mode, groupId, callerId);
			
			if(removedMembersFromGroup == null || removedMembersFromGroup.size() == 0){
				String errorCode = errorCodeMapping.getErrorCode("").getCode();
				String message = applicationContext.getMessage(MessageResource.GROUP_MEMBER_REMOVE_MESSAGE_FOR + "", null, locale);
				throw new UserException(errorCode, message);
			}
			
		}catch(UserException ue){
			throw ue;
		}
		return constructSuccessMap(MessageResource.GROUP_MEMBER_REMOVE_MESSAGE_FOR,SUCCESS);
	}

	@Override
	public CallerList getAllGroupMember(String msisdn, int groupId, int pagesize,
			int offset) throws UserException {
		logger.info("inside getAllGroupMember in GroupMemberVoltronServiceImpl msisdn: "+msisdn +" , groupId :" +groupId +" , pagesize :"+pagesize +" , offset: "+offset);
		
		try{
			
			GroupMemberList groupMemberList = (GroupMemberList)groupMemberService.getAllGroupMember(msisdn, groupId, pagesize, offset);
			
			List<GroupMember> membersFromGroup = groupMemberList.getGroupMembers();
			int totalSize = groupMemberList.getTotalSize();
			
			if(membersFromGroup == null || membersFromGroup.size() == 0){
				return new BuildCallerList().buildCallerList();						
			}
			
			List<Caller> callerList = new ArrayList<Caller>();
			for(GroupMember groupMember : membersFromGroup) {
				callerList.add(new BuildCaller().setId(Long.parseLong(groupMember.getMemberID())).
					setName(groupMember.getMemberName()).setPhonenumber(groupMember.getMemberID()).buildCaller());
			}
			
			int size = membersFromGroup.size();
			
			Pager pager = new Pager(offset, size);
			pager.setTotalresults(totalSize);
			
			BuildCallerList callerListBuilder = new BuildCallerList().setCallers(callerList).setPager(pager).setCount(size);
			
			return callerListBuilder.buildCallerList();
			
			
			
		}catch(UserException ue){
			throw ue;
		}
	}


	@Override
	public Caller addMemberToGroup(String msisdn, String mode, int groupId,
			Long callerId, String callerName) throws UserException {
		logger.info("inside addMemberToGroup in GroupMemberVoltronServiceImpl msisdn: "+msisdn +" , mode :" +mode +" , groupId :"+groupId +" , callerId: "+callerId+ " , callerName:"+callerName);

		try{
		
			List<GroupMember> addedMembersFromGroup = (List<GroupMember>)groupMemberService.addMemberToGroup(msisdn, mode, groupId, callerId, callerName);
			
			if(addedMembersFromGroup == null || addedMembersFromGroup.size() == 0){
				String errorCode = errorCodeMapping.getErrorCode("").getCode();
				String message = applicationContext.getMessage(MessageResource.GROUP_MEMBER_ADD_MESSAGE_FOR + "", null, locale);
				throw new UserException(errorCode, message);
			}
			
			GroupMember groupMember = addedMembersFromGroup.get(0);
			BuildCaller callerBuilder = new BuildCaller().setId(Long.parseLong(groupMember.getMemberID())).
					setName(groupMember.getMemberName()).setPhonenumber(groupMember.getMemberID());
			
			return callerBuilder.buildCaller();
			
		}catch(UserException ue){
				throw ue;
		}
		
	}

}
