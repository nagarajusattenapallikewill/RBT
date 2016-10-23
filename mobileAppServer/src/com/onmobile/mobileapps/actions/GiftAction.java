package com.onmobile.mobileapps.actions;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;

import com.jspsmart.upload.SRequest;
import com.jspsmart.upload.SmartUpload;
import com.jspsmart.upload.SmartUploadException;
import com.onmobile.android.exceptions.OMAndroidException;
import com.onmobile.android.factory.ResponseFactory;
import com.onmobile.android.interfaces.SubscriberResponse;
import com.onmobile.android.utils.StringConstants;
import com.onmobile.android.utils.Utility;

public class GiftAction extends DispatchAction{
	
	public static Logger logger = Logger.getLogger(GiftAction.class);
	
	public ActionForward getGiftInbox(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.info("Inside getGiftInbox method");
	    String subId = request.getParameter("subscriberId");
	
		String giftInboxResponse=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getGiftInbox(subId);
		request.setAttribute("response", giftInboxResponse);
		logger.info("giftInboxResponse " + giftInboxResponse);
		return mapping.findForward("success");
	}
	
	public ActionForward gift(ActionMapping mapping, ActionForm form, 
			HttpServletRequest request, HttpServletResponse response)throws OMAndroidException{
		
		logger.info("Inside gift");
	    String subId = request.getParameter("subscriberId");
		String contentId=request.getParameter("contentId");
		String giftee=request.getParameter("giftee");
		String categoryId = request.getParameter("categoryId");
		boolean isPost = request.getMethod().equals("POST");
		String contentType = request.getContentType();
		if (!Utility.isStringValid(giftee) && isPost && contentType.startsWith("multipart/")) {
			logger.debug("Mulipart group request for subscriberId: " + subId);
			try {
				SmartUpload smartUpload = new SmartUpload();
				smartUpload.initialize(getServlet().getServletConfig(), request, response);
				smartUpload.setTotalMaxFileSize(20000000);
				smartUpload.upload();
				SRequest smartUploadRequest = smartUpload.getRequest();				
				giftee = smartUploadRequest.getParameter("giftee");
			}  catch (SmartUploadException e) {
				logger.error("Exception caught: " + e, e);
			} catch (IOException e) {
				logger.error("Exception caught: " + e, e);
			} catch (ServletException e) {
				logger.error("Exception caught: " + e, e);
			}
		}
		StringBuffer strbuf=null;
		String chargeClass="Default";
		String mode = request.getParameter("mode");
		logger.info("subscriberId: " + subId + ", contentId: " + contentId + ", giftee" + giftee + ", categoryId: " + categoryId + ", mode: " + mode);
		if (mode == null || mode.trim().isEmpty()) {
			mode = StringConstants.CHANNEL;
			logger.info("mode was null. Hence set as mode = " + StringConstants.CHANNEL);
		}
		String giftResponse = ResponseFactory.getSubscriberResponse(
				ResponseFactory.RESPONSE_TYPE_JSON, mode).giftSong(subId,
				contentId, giftee, mode, strbuf, chargeClass, categoryId);
		request.setAttribute("response", giftResponse);
		logger.info("giftResponse " + giftResponse);
		return mapping.findForward("success"); 
	}
	
	public ActionForward getGiftInboxCount(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.info("Inside getGiftInboxCount method");
	    String subId = request.getParameter("subscriberId");
	
		String giftInboxCountResponse=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).getGiftInboxCount(subId);
		request.setAttribute("response", giftInboxCountResponse);
		logger.info("giftInboxResponse " + giftInboxCountResponse);
		return mapping.findForward("success");
	}
	

	public ActionForward rejectGift(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		logger.info("Inside rejectGift method");
	    String gifterId = request.getParameter("gifterId");
	    String gifteeId = request.getParameter("gifteeId");
	    String giftSentTime = request.getParameter("giftSentTime");
		
	    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
	    try {
	    	Date newdate = sdf.parse(giftSentTime);
	    	SimpleDateFormat tosdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	    	String finalDate = tosdf.format(newdate);
	    	String giftInboxCountResponse=ResponseFactory.getSubscriberResponse(ResponseFactory.RESPONSE_TYPE_JSON, SubscriberResponse.CHANNEL).rejectGift(gifterId, gifteeId, finalDate);
	    	request.setAttribute("response", giftInboxCountResponse);
	    	logger.info("giftInboxResponse " + giftInboxCountResponse);
	    	return mapping.findForward("success");

	    } catch (ParseException e) {
				logger.error("",e);
		}
	    return mapping.findForward("failure");
	}
	
}
