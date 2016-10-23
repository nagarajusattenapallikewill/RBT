package com.onmobile.apps.ringbacktones.provisioning.api;


import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.ResponseEncoder;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;


/**
 * Servlet implementation class Promotion
 */
public class Promotion extends HttpServlet implements Constants
{
	private static final long serialVersionUID = 1L;
	
	private ServletConfig servletConfig = null;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public Promotion()
	{
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException
	{
		super.init(servletConfig);

		this.servletConfig = servletConfig;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request, response, api_Promotion);

		Utility.sqlInjectionInRequestParam(requestParams);
		ResponseEncoder responseEncoder = AdminFacade.getResponseEncoderObject(api_Promotion);
		String responseText = responseEncoder.getGenericErrorResponse(requestParams);
		String contentType = responseEncoder.getContentType(requestParams);
		try
		{
			Parameters modeIPParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.CONSENT, "MODE_IP_MAPPING_FOR_CONSENT", null);
			Parameters requestParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.CONSENT, "PROMOTION_REQUESTS_FOR_CONSENT", null);
			String promotionRequest = requestParams.get(param_REQUEST);
			List<String> promotionRequestLists = null;
			if(requestParam != null) {
				promotionRequestLists = Arrays.asList(requestParam.getValue().toUpperCase().split(","));
			}
			if(modeIPParam != null && promotionRequestLists != null && promotionRequestLists.contains(promotionRequest.toUpperCase())) {
				String ipAddress = request.getRemoteAddr();
				requestParams.put(WebServiceConstants.param_ipAddressConsent, ipAddress);
				String mode = null;
				if((promotionRequest.equalsIgnoreCase(action_activate) || promotionRequest.equalsIgnoreCase(action_topup) 
						|| promotionRequest.equalsIgnoreCase(request_TNB) || promotionRequest.equalsIgnoreCase(request_upgrade_base)) 
						&& requestParams.get(param_ACTIVATED_BY) != null) {
					mode = requestParams.get(param_ACTIVATED_BY);
				} 				
				else if((promotionRequest.equalsIgnoreCase(action_selection) || promotionRequest.equalsIgnoreCase(action_cricket) 
						|| promotionRequest.equalsIgnoreCase(request_TNB)) && requestParams.get(param_SELECTED_BY) != null) { 
					mode = requestParams.get(param_SELECTED_BY);
				}
				else if((promotionRequest.equalsIgnoreCase(action_up_validity)) && requestParams.get(param_MODE) != null) {
					mode = requestParams.get(param_MODE);
				}
				else if((promotionRequest.equalsIgnoreCase(action_deactivate_tone)) && requestParams.get(param_CATEGORY_ID) != null) {
					mode = requestParams.get(param_CATEGORY_ID);
				}
				requestParams.put(WebServiceConstants.param_modeConsent, mode);
			}
			
			responseText = AdminFacade.processPromotionRequest(requestParams);
			responseText = getModifiedResponse(responseText, requestParams);
		}
		catch (Exception e)
		{
			Logger.getLogger(Sms.class).error("RBT:: " + e.getMessage(), e);
		}

		if (!requestParams.containsKey("ENCODING") || requestParams.get("ENCODING") == null)
		{
			if (contentType != null)
				response.setContentType(contentType);
		}
		else
		{
			response.setContentType ("text/html;charset="+requestParams.get("ENCODING"));
		}
		Logger.getLogger(Sms.class).info("RBT:: responseText: " + responseText);
		response.getWriter().write(responseText);
	}

	private String getModifiedResponse(String responseText,
			HashMap<String, String> requestParams)
	{
		
		String taskAction = requestParams.get("TASK_ACTION");
		String alteredResponseReq = requestParams.get("ALTER_RESPONSE");
		String modifiedResponseRequired = requestParams.get("ALTER_STATUS_RES");
		String pack = null;
		if(taskAction != null && taskAction.equalsIgnoreCase("status") && modifiedResponseRequired != null)
		{
			if(responseText == null)
				responseText = "ERROR#OTHER";
			else if (responseText.equalsIgnoreCase(Resp_Inactive))
				responseText = "NONACTIVE";
			else if (responseText.indexOf("ACTIVE_") != -1)
			{
				pack = requestParams.get("PACK_STATUS");
				responseText = "ACTIVE";
			}
			else
				responseText = "ERROR#OTHER";
			if(requestParams.get("STATUS_DATE") != null && responseText.indexOf("ERROR") == -1)
				responseText = responseText + "#"+requestParams.get("STATUS_DATE");
			if(pack != null)
				responseText = responseText + "#" + pack;
		}else if(taskAction!=null && taskAction.equalsIgnoreCase(action_activate) && alteredResponseReq!=null&&alteredResponseReq.equalsIgnoreCase("true")){
				responseText = Utility.getConfiguredResponse(responseText,action_activate);
		}else if(taskAction!=null && taskAction.equalsIgnoreCase(action_deactivate) && alteredResponseReq!=null&&alteredResponseReq.equalsIgnoreCase("true")){
				responseText = Utility.getConfiguredResponse(responseText,action_deactivate);
		}
		return responseText;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		doGet(request, response);
	}
}
