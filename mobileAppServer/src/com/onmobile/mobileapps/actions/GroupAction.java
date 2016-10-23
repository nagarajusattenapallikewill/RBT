package com.onmobile.mobileapps.actions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.factory.ResponseFactory;
import com.onmobile.android.interfaces.SubscriberResponse;
import com.onmobile.android.utils.StringConstants;

public class GroupAction extends DispatchAction{
	
	public static Logger logger = Logger.getLogger(GroupAction.class);
	
	public ActionForward getGroups(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		logger.info("Inside getGroups method");
	    String subscriberId = request.getParameter("subscriberId");
	
		String getGroupsResponse=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getGroups(subscriberId);
		request.setAttribute("response", getGroupsResponse);
		logger.info("getGroupResponse " + getGroupsResponse);
		return mapping.findForward("success");

	}
	
	public ActionForward getPredefinedGroups(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		logger.info("Inside getPredefinedGroups method");
	
		String getGroupsResponse=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getPredefinedGroups();
		request.setAttribute("response", getGroupsResponse);
		logger.info("getPredefinedGroupResponse " + getGroupsResponse);
		return mapping.findForward("success");

	}
	
	public ActionForward addContacts(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside addContacts");

		String subscriberId = request.getParameter("subscriberId");
		String callerId = request.getParameter("callerId");
		String callerName = request.getParameter("callerName");
		String predefinedGroupId = request.getParameter("predefinedGroupId");
		String groupId = request.getParameter("groupId");
		String mode = request.getParameter("mode");
		
		logger.info("addContacts. subscriberId: " + subscriberId + ", callerId: "
				+ callerId + ", callerName: " + callerName
				+ ", predefinedGroupId: " + predefinedGroupId + "groupId:" + groupId + "mode: " + mode);
		
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		
		String responseMsg = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode).addContactMemebers(
				subscriberId, callerId, callerName, predefinedGroupId, groupId, mode);

		request.setAttribute("response", responseMsg);

		logger.info("response"+responseMsg);
		return mapping.findForward("success"); 
	}
	
	public ActionForward addMultipleContacts(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside addMultipleContacts");

		String subscriberId = request.getParameter("subscriberId");
		String callerId = request.getParameter("callerId");
		String callerName = request.getParameter("callerName");
		String predefinedGroupId = request.getParameter("predefinedGroupId");
		String groupId = request.getParameter("groupId");		
		String mode = request.getParameter("mode");
		
		logger.info("addMultipleContacts. subscriberId: " + subscriberId + ", callerId: "
				+ callerId + ", callerName: " + callerName
				+ ", predefinedGroupId: " + predefinedGroupId + "groupId:" + groupId + "mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		
		String responseMsg = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode)
				.addMultipleContactMemebers(subscriberId, callerId, callerName,
						predefinedGroupId, groupId, mode);

		request.setAttribute("response", responseMsg);

		logger.info("response"+responseMsg);
		return mapping.findForward("success"); 
	}
	
	public ActionForward getAllContacts(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside getAllContacts");

		String subscriberId = request.getParameter("subscriberId");
		String predefinedGroupId = request.getParameter("predefinedGroupId");
		String mode = request.getParameter("mode");
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String responseMsg = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).getAllContactMemebers(subscriberId, predefinedGroupId, mode);

		request.setAttribute("response", responseMsg);

		logger.info("response"+responseMsg);
		return mapping.findForward("success"); 
	}

	/**
	 * To get all the contacts from all groups. The response list would be sorted in ascending order of contact names.
	 */
	public ActionForward getAllContactsFromAllGroups(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response) throws OMAndroidException {
		
		String subscriberId = request.getParameter("subscriberId");
		logger.info("Getting all contacts from all groups for subscriberId: " + subscriberId);
		String mode = request.getParameter("mode");
		logger.info("mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String responseMsg = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).getAllContactsFromAllGroups(subscriberId, mode);

		request.setAttribute("response", responseMsg);

		logger.info("response: " + responseMsg);
		return mapping.findForward("success"); 
	}
	
	public ActionForward removeContacts(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside removeContacts");

		String subscriberId = request.getParameter("subscriberId");
		String callerId = request.getParameter("callerId");
		String predefinedGroupId = request.getParameter("predefinedGroupId");
		String groupId = request.getParameter("groupId");
		String mode = request.getParameter("mode");
		logger.info("removeContacts. subscriberId: " + subscriberId
				+ ", callerId: " + callerId + ", predefinedGroupId: "
				+ predefinedGroupId + ", groupId:" + groupId + "mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String responseMsg = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).
				removeContactMemebers(subscriberId, callerId, predefinedGroupId, groupId, mode);

		request.setAttribute("response", responseMsg);

		logger.info("Response: "+responseMsg);
		return mapping.findForward("success"); 
	}
	
	public ActionForward removeMultipleContacts(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside removeMultipleContacts");

		String subscriberId = request.getParameter("subscriberId");
		String callerId = request.getParameter("callerId");
		String predefinedGroupId = request.getParameter("predefinedGroupId");
		String groupId = request.getParameter("groupId");
		String mode = request.getParameter("mode");
		logger.info("removeMultipleContacts. subscriberId: " + subscriberId + ", callerId: " + callerId
				+ ", predefinedGroupId: " + predefinedGroupId + ", groupId:"
				+ groupId + "mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String responseMsg = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode)
				.removeMultipleContactMemebers(subscriberId, callerId,
						predefinedGroupId, groupId, mode);

		request.setAttribute("response", responseMsg);

		logger.info("Response: "+responseMsg);
		return mapping.findForward("success"); 
	}
	
	public ActionForward moveContacts(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside moveContacts");
	
		String subscriberId = request.getParameter("subscriberId");
		String callerId = request.getParameter("callerId");
		String sourcePreGroupId = request.getParameter("sourcePreGroupId");
		String sourceGroupId = request.getParameter("sourceGroupId");
		String destPreGroupId = request.getParameter("destPreGroupId");
		String destGroupId = request.getParameter("destGroupId");
		String mode = request.getParameter("mode");
		logger.info("moveContacts. subscriberId: " + subscriberId
				+ ", callerId: " + callerId + ", sourcePreGroupId: "
				+ sourcePreGroupId + ", sourceGroupId: " + sourceGroupId
				+ ", destPreGroupId: " + destPreGroupId + ", destGroupId: "
				+ destGroupId + ", mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String responseMsg = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode).moveContactMemebers(
				subscriberId, callerId, sourcePreGroupId, sourceGroupId, destPreGroupId, destGroupId, mode);

		request.setAttribute("response", responseMsg);

		logger.info("response"+responseMsg);
		return mapping.findForward("success"); 
	}
	
	//RBT-14626	Signal app requirement - Mobile app server API enhancement (phase 2)
	public ActionForward removeGroup(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		logger.info("Inside removeGroup");

		String subscriberId = request.getParameter("subscriberId");
		String predefinedGroupId = request.getParameter("predefinedGroupId");
		String groupId = request.getParameter("groupId");
		String mode = request.getParameter("mode");
		logger.info("removeGroup. subscriberId: " + subscriberId
				+ ", predefinedGroupId: "
				+ predefinedGroupId + ", groupId:" + groupId + "mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String responseMsg = ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, mode).
				removeGroup(subscriberId,groupId, mode);

		request.setAttribute("response", responseMsg);

		logger.info("Response: "+responseMsg);
		return mapping.findForward("success"); 
	}
}
