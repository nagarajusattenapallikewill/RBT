package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.bsnl;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoResponseEncoder;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class BsnlPromoResponseEncoder extends PromoResponseEncoder {
	//Ad RBT response constants
	private static final String ISEXISTS_EXISTS = "1";
	private static final String ISEXISTS_NOT_EXISTS = "0";
	private static final String ISACTIVE_NOT_ACTIVE = "0";
	private static final String ISACTIVE_ACTIVE = "1";
	private static final String ISACTIVE_ACTIVATION_PENDING = "2";
	private static final String ISACTIVE_DEACTIVATION_PENDING = "3";
	
	public BsnlPromoResponseEncoder() throws Exception {
		super();
		logger = Logger.getLogger(BsnlPromoResponseEncoder.class);
	}
	
	public String encode(Task task) {
		if(Utility.isPromotionRequest(task))
			return super.encode(task);
		String response = task.getString(param_response);
		logger.info("RBT::initial response -> " + response);
		response = prepareAdRBTResponse(task);
		logger.info("RBT::response -> " + response);
		return response;
	}
	
	/**
	 * Replies in the format &lt;response result ='1' trx_id='1234' isExists='1' isActive='1'/&gt;
	 * <br>Possible values for the above mentioned response are 
	 * <table border=true>
	 * <tr><td>isExists</td><td>Possible values: 0-Does not exists, 1-Exists</td></tr>
	 * <tr><td>isActive</td><td>Possible values: 0-Not active, 1-Active, 2-Activation Pending, 3-Deactivation Pending</td></tr>
	 * <tr><td>trx_id</td><td>The original transaction ID from the incoming request Ex:1234</td></tr>
	 * <tr><td>result</td><td>Possible values: 0-Not processed/failed, 1-Success</td></tr>
	 * </table>
	 * 
	 * @param task
	 * @return Returns the response xml string to be responded back
	 */
	private String prepareAdRBTResponse(Task task) {
		String response = task.getString(param_response);
		if(response != null && (response.equalsIgnoreCase(Resp_Err) || response.equalsIgnoreCase(Resp_Failure)))
			return null;
		
		String command = task.getString(param_BSNL_adRBT_command);
		if(command.equalsIgnoreCase(param_BSNL_adRBT_command_SUBINFO))
			response = prepareAdRBTSubInfoXML(task);
		else if(command.equalsIgnoreCase(param_BSNL_adRBT_command_ACT))
			response = prepareAdRBTActivationResponseXML(task);
		else if(command.equalsIgnoreCase(param_BSNL_adRBT_command_DCT))
			response = prepareAdRBTDeactivationResponseXML(task);
		return response;
	}
	
	/**
	 * Response format for both activation and deactivation is of the same format
	 * @param task
	 * @return
	 */
	private String prepareAdRBTDeactivationResponseXML(Task task) {
		return prepareAdRBTActivationResponseXML(task);
	}
	
	private String prepareAdRBTActivationResponseXML(Task task) {
		String response = task.getString(param_response);
		String responseXML = "<response result='%RESULT' trx_id='%TRANS_ID' />";
		if(response == null || response.equals(""))
			return getAdRbtErrorXML(task, responseXML);
		
		if(response.equalsIgnoreCase(Resp_Success)) {
			responseXML = responseXML.replaceAll("%RESULT", "1");
			responseXML = responseXML.replaceAll("%TRANS_ID", task.getString(param_BSNL_adRBT_trx_id));
		}
		else
			responseXML = getAdRbtErrorXML(task, responseXML);
		return responseXML;
	}
	
	private String prepareAdRBTSubInfoXML(Task task) {
		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String responseXML = "<response result ='%RESULT' trx_id='%TRANS_ID' isExists='%ISEXISTS' isActive='%ISACTIVE'/>";
		if(subscriber == null || !subscriber.isValidPrefix())
			return getAdRbtErrorXML(task, responseXML);
		
		responseXML = responseXML.replaceAll("%RESULT", "1");
		responseXML = responseXML.replaceAll("%TRANS_ID", task.getString(param_BSNL_adRBT_trx_id));
		
		String status = subscriber.getStatus();
		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				|| status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)) {
			responseXML = responseXML.replaceAll("%ISEXISTS", ISEXISTS_NOT_EXISTS);
			responseXML = responseXML.replaceAll("%ISACTIVE", ISACTIVE_NOT_ACTIVE);
		}
		else {
			responseXML = responseXML.replaceAll("%ISEXISTS", ISEXISTS_EXISTS);
			if(status.equalsIgnoreCase(WebServiceConstants.ACTIVE))
				responseXML = responseXML.replaceAll("%ISACTIVE", ISACTIVE_ACTIVE);
			else if(status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING))
				responseXML = responseXML.replaceAll("%ISACTIVE", ISACTIVE_ACTIVATION_PENDING);
			else if(status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING))
				responseXML = responseXML.replaceAll("%ISACTIVE", ISACTIVE_DEACTIVATION_PENDING);
			else
				responseXML = responseXML.replaceAll("%ISACTIVE", ISACTIVE_NOT_ACTIVE);
		}
		return responseXML;
	}
	
	private String getAdRbtErrorXML(Task task, String response) {
		if(response.indexOf("%RESULT") >= 0)
			response = response.replaceAll("%RESULT", "0");
		if(response.indexOf("%TRANS_ID") >= 0)
			response = response.replaceAll("%TRANS_ID", task.getString(param_BSNL_adRBT_trx_id));
		if(response.indexOf("%ISEXISTS") >= 0)
			response = response.replaceAll("%ISEXISTS", "0");
		if(response.indexOf("%ISACTIVE") >= 0)
			response = response.replaceAll("%ISACTIVE", "0");
		
		return response;
	}
	
	@Override
	public String getGenericErrorResponse(HashMap<String, String> requestParams) {
		if(requestParams.containsKey(param_BSNL_adRBT_command))
			return "<response result ='0'/>";
		return super.getGenericErrorResponse(requestParams);
	}
}