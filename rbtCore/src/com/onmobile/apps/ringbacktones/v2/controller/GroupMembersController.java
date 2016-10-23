package com.onmobile.apps.ringbacktones.v2.controller;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.livewiremobile.store.storefront.dto.rbt.Caller;
import com.onmobile.apps.ringbacktones.v2.bean.ServiceResolver;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.rbt2.service.IGroupMemberService;

@RestController
@RequestMapping("/groups")
public class GroupMembersController {

	private static Logger logger = Logger.getLogger(GroupMembersController.class);
	
	@Autowired
	private ServiceResolver serviceResolver;

	public void setServiceResolver(ServiceResolver serviceResolver) {
		this.serviceResolver = serviceResolver;
	}
	
	@RequestMapping(method = RequestMethod.POST, value="{groupid}/callers/{implpath}")
	public Object addmembersToGroup(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestBody Caller caller,
			@PathVariable(value="groupid") int groupId,
			@RequestParam(value = "mode" , defaultValue="WAP" ,required=false) String mode ,
			@PathVariable(value="implpath") String implpath) throws UserException{
		
			logger.info("inside addmembersToGroup in GroupMembersController..");
		    IGroupMemberService groupMemberService = serviceResolver.getGroupMemberServiceImpl(implpath);
		    if(groupMemberService == null){
				throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
			}
		    return groupMemberService.addMemberToGroup(msisdn,mode,groupId, caller.getId(),caller.getName());
		
	}
	
	@RequestMapping(method = RequestMethod.DELETE, value="/{groupid}/callers/{callerid}/{implpath}")
	public Object removeMemberFromGroup(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@RequestParam(value = "mode" , defaultValue="WAP" ,required=false) String mode ,
			@PathVariable(value="groupid") int groupId,
			@PathVariable(value="callerid") String callerId,
			@PathVariable(value="implpath") String implpath) throws UserException{
		
			logger.info("inside addmembersToGroup in GroupMembersController..");
		
		    IGroupMemberService groupMemberService = serviceResolver.getGroupMemberServiceImpl(implpath);
		    if(groupMemberService == null){
				throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
			}
		    return groupMemberService.removeMemberFromGroup(msisdn, mode, groupId, callerId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value="{groupid}/callers/{implpath}")
	public Object getAllGroupMember(
			@RequestParam(value = "subscriberId", required = true) String msisdn,
			@PathVariable(value="groupid") int groupId,
			@RequestParam(value = "pagesize", required = false, defaultValue= "0") int pagesize,
			@RequestParam(value = "offset", required = false, defaultValue= "0") int offset,
			@PathVariable(value="implpath") String implpath) throws UserException{
		
			logger.info("inside addmembersToGroup in GroupMembersController..");
		    IGroupMemberService groupMemberService = serviceResolver.getGroupMemberServiceImpl(implpath);
		    if(groupMemberService == null){
				throw new UserException("INVALIDPARAMETER","SERVICE_NOT_AVAILABLE");
			}
			return groupMemberService.getAllGroupMember(msisdn, groupId, pagesize, offset);
		
	}
}
