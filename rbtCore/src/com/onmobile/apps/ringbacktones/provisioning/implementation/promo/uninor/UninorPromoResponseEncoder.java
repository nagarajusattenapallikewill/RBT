package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.uninor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoResponseEncoder;

/**
 * @author sridhar.sindiri
 *
 */
public class UninorPromoResponseEncoder  extends PromoResponseEncoder
{

	/**
	 * @throws Exception
	 */
	public UninorPromoResponseEncoder() throws Exception
	{
		super();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoResponseEncoder#encode(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	public String encode(Task task)
	{
		if (task.containsKey(param_REQUEST))
			return super.encode(task);

		String response = "";
		String taskAction = task.getTaskAction();
		if (!task.containsKey(param_response))
		{
			logger.error("RBT::no response populated taskAction-" + taskAction
					+ ", taskSession-" + task.getTaskSession());
			task.setObject(param_response, Resp_Err);
		}
		else
			logger.info("RBT::response-" + task.getString(param_response));

		if (taskAction.equalsIgnoreCase("Add") || taskAction.equalsIgnoreCase("Delete"))
			response = getResponseXml(task);

		return response;
	}

	/**
	 * @param task
	 * @return
	 */
	private String getResponseXml(Task task)
	{
		String response = task.getString(param_response);
		String responseXml = null;
		String agentID = task.getString(param_AgentID);
		String vendorName = task.getString(param_VendorName);
		int upLimit = 3;
		if (vendorName != null && vendorName.length() < 3)
			upLimit = vendorName.length();
		
		String transID = (vendorName != null) ? vendorName.substring(0, upLimit).toUpperCase() : "null";
		transID += "_" + System.currentTimeMillis() + "000";

		List<String> responseList = new ArrayList<String>();
		Parameters parameter = CacheManagerUtil.getParametersCacheManager().getParameter("UNINOR", "THIRD_PARTY_SUCCESS_RESPONSE_STATES", null);
		if(parameter != null) {
			responseList = Arrays.asList(parameter.getValue().split("\\,"));
		}
		
		if (response.equalsIgnoreCase("SUCCESS") || responseList.contains(response))
		{
			responseXml = "<performSubscriptionResponse><Status>Success</Status><VendorName>"
					+ vendorName
					+ "</VendorName><AgentID>"
					+ agentID
					+ "</AgentID><TransactionID>"
					+ transID
					+ "</TransactionID></performSubscriptionResponse>";
		}
		else
		{
			responseXml = "<performSubscriptionResponse><Status>Failure</Status><Reason>"
					+ response
					+ "</Reason><VendorName>"
					+ vendorName
					+ "</VendorName><AgentID>"
					+ agentID
					+ "</AgentID><TransactionID>"
					+ transID
					+ "</TransactionID></performSubscriptionResponse>";

		}

		return responseXml;
	}
}
