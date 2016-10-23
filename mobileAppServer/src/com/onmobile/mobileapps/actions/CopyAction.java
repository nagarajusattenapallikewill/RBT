package com.onmobile.mobileapps.actions;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.onmobile.android.factory.ResponseFactory;
import com.onmobile.android.interfaces.SubscriberResponse;

public class CopyAction extends DispatchAction{
public static Logger logger = Logger.getLogger(GiftAction.class);
	
	public ActionForward getCopySelections(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) throws ServletException {
		logger.info("Inside getCopySelections method");
	    String copyMobileNumber = request.getParameter("copyMobileNumber");
	    String subscriberId = request.getParameter("subscriberId");
		String getGroupsResponse=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getCopySelections(copyMobileNumber,subscriberId);
		request.setAttribute("response", getGroupsResponse);
		logger.info("copyResponse " + getGroupsResponse);
		return mapping.findForward("success");

	}
}
