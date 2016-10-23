package com.onmobile.mobileapps.actions;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.impl.ContentJSONResponseImpl;
import com.onmobile.android.interfaces.ContentResponse;

public class DislikeAction  extends Action{
	
	
	public static Logger logger = Logger.getLogger(DislikeAction.class);
	
	public ActionForward execute(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
	    ContentResponse c1=new ContentJSONResponseImpl();
		String s2=null;
		String clipId = request.getParameter("clipId");
		int clipIdInt = Integer.parseInt(clipId);
		Integer ii=new Integer(clipIdInt);
		s2=c1.dislike(ii);
		request.setAttribute("response", s2);
		logger.info("final string:"+s2);
		return mapping.findForward("success"); 
		
	}


}