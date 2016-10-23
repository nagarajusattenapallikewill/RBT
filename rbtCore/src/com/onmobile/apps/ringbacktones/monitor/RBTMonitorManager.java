package com.onmobile.apps.ringbacktones.monitor;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.monitor.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;

public class RBTMonitorManager implements iRBTConstant, Constants {
	private static final Logger _logger = Logger.getLogger(RBTMonitorManager.class);
	private static final Object _syncObj = new Object();
	private static final String _defaultMonitor = "com.onmobile.apps.ringbacktones.monitor.implementation.BasicRBTMonitor";
	private iRBTMonitor _monitor;
	
	private static RBTMonitorManager _monitorManager = null;
	private static ParametersCacheManager _paramCache = CacheManagerUtil.getParametersCacheManager();
	
	public static RBTMonitorManager getInstance() {
		if(_monitorManager == null) {
			synchronized (_syncObj) {
				if(_monitorManager == null)
					_monitorManager = new RBTMonitorManager();
			}
		}
		return _monitorManager;
	}
	
	private RBTMonitorManager() {
		initMonitorClass();
	}
	
	private void initMonitorClass() {
		String monitorClassStr = _paramCache.getParameter(MONITOR, "MONITOR_IMPLEMENTATION", _defaultMonitor).getValue().trim();
		Class<?> monitorClass = null;
		try {
			monitorClass = Class.forName(monitorClassStr);
		}
		catch(ClassNotFoundException cnfe) {
			_logger.error("RBT:: class " + monitorClassStr
					+ "not found in the classpath. Proceesing with " + _defaultMonitor, cnfe);
			try {
				monitorClass = Class.forName(_defaultMonitor);
			}
			catch(Exception e) {
				_logger.error("RBT:: exception", e);
			}
		}
		try {
			_monitor = (iRBTMonitor)monitorClass.newInstance();
		}
		catch (Exception e) {
			_logger.error("RBT:: exception", e);
		}
	}
	
	private boolean isValidTraceType(String traceType) {
		if(traceType == null)
			return false;
		if (traceType.equals(TRACE_TYPE_IVR) || traceType.equals(TRACE_TYPE_SMS)
				|| traceType.equals(TRACE_TYPE_COPY) || traceType.equals(TRACE_TYPE_WEBSERVICE)
				|| traceType.equals(TRACE_TYPE_CCC) || traceType.equals(TRACE_TYPE_PROMOTION)
				|| _monitor.isThirdPartyRequest(traceType))
			return true;
		return false;
	}
	
	public String monitor(HashMap<String, String> params) {
		_logger.info("RBT::started with params->" + params);
		String traceType = params.get(param_tracetype);
		String msisdn = params.get(param_msisdn);
		String response = null;
		if(isValidTraceType(traceType)) {
			String startMonitorResult = _monitor.startMonitor(msisdn, traceType);
			if(!startMonitorResult.equals(RESPONSE_SUCCESS))
				return startMonitorResult;
		}
		else {
			_logger.error("RBT::invalid trace type->" + traceType);
			return _monitor.getGenericErrorResponse();
		}
		
		if(traceType.equals(TRACE_TYPE_COPY))
			response = _monitor.processCopyMonitor(params);
		else if(traceType.equals(TRACE_TYPE_SMS))
			response = _monitor.processSMSMonitor(params);
		else if(traceType.equals(TRACE_TYPE_IVR))
			response = _monitor.processIVRMonitor(params);
		else if(traceType.equals(TRACE_TYPE_WEBSERVICE))
			response = _monitor.processWebServiceMonitor(params);
		else if(traceType.equals(TRACE_TYPE_CCC))
			response = _monitor.processWebServiceMonitor(params);
		else if(_monitor.isThirdPartyRequest(traceType))
			response = _monitor.processThirdPartyMonitor(params);
		
		_monitor.endMonitor(msisdn);
		if(response == null) {
			response = _monitor.getGenericErrorResponse();
			_logger.info("RBT::no response populated, returning generic error->" + response);
		}
		_logger.info("returning response->" + response);
		return response;
	}
	
	public String endMonitor(HashMap<String, String> params) {
		String msisdn = params.get(param_msisdn);
		if(msisdn == null)
			return _monitor.getGenericErrorResponse();
		return _monitor.endMonitor(msisdn);
	}
	
	public RBTNode startCopyNode(HashMap<String, String> params) {
		String param = params.get(com.onmobile.apps.ringbacktones.provisioning.common.Constants.rbt_param_DETAILS);
		if(param == null)
			return RBTNode.getDummyNode();
		StringTokenizer stk = new StringTokenizer(param, ":");
		String msisdn = null;
		if(stk.hasMoreTokens())
			stk.nextToken();
		if(stk.hasMoreTokens())
			msisdn = stk.nextToken();
		if(msisdn != null)
			return startNode(msisdn, RBTNode.NODE_PLAYER_UPDATER);
		return RBTNode.getDummyNode();
	}
	
	public void endCopyNode(HashMap<String, String> params, RBTNode node, String response) {
		if(node.equals(RBTNode.getDummyNode()))
			return;
		String param = params.get(com.onmobile.apps.ringbacktones.provisioning.common.Constants.rbt_param_DETAILS);
		StringTokenizer stk = new StringTokenizer(param, ":");
		String msisdn = null;
		if(stk.hasMoreTokens())
			stk.nextToken();
		if(stk.hasMoreTokens())
			msisdn = stk.nextToken();
		if(msisdn != null)
			endNode(msisdn, node, response);
	}
	
	public RBTNode startNode(String msisdn, String nodeName) {
		RBTNode node = new RBTNode(nodeName);
		_monitor.startNode(msisdn, node);
		return node;
	}
	
	/**
	 * This method returns SUCCESS by comparing the response from rbt_sms.jsp with the list of
	 * configured bulk promo ids
	 * 
	 * @param response response from the rbt_sms.jsp
	 * @return SUCCESS/FAILURE
	 */
	private String isSuccessfulSMSResponse(String response) {
		try {
			SmsProcessor processor = (SmsProcessor) AdminFacade
					.getProcessorObject(com.onmobile.apps.ringbacktones.provisioning.common.Constants.param_sms);
			String allSMSToCompare = _paramCache.getParameter(MONITOR, "SUCCESS_SMS_TEXTS", "")
					.getValue();
			if (allSMSToCompare == null)
				return RBTNode.RESPONSE_FAILURE;
			StringTokenizer stk = new StringTokenizer(allSMSToCompare, ",");
			while (stk.hasMoreTokens()) {
				String config = processor.getSMSTextForID(null, stk.nextToken(), null, null);
				if (checkSMSResponse(response, config))
					return RBTNode.RESPONSE_SUCCESS;
			}
		}
		catch (Exception e) {
			_logger.error("RBT::Exception", e);
		}
		return RBTNode.RESPONSE_FAILURE;
	}


	/**
	 * This method compares the response from rbt_sms.jsp and the configured sms in
	 * RBT_BULK_PROMO_SMS for a particular bulk promo ID
	 * 
	 * @param response response from rbt_sms.jsp
	 * @param config configuration in the RBT_BULK_PROMO_SMS
	 * @return
	 */
	private boolean checkSMSResponse(String response, String config) {
		if (response == null || config == null)
			return false;
		if (config.indexOf("%") == -1)
			return response.equals(config);

		String regexStr = createRegexString(config);
		Pattern pattern = Pattern.compile(regexStr);
		Matcher matcher = pattern.matcher(response);
		return matcher.find() && matcher.end() == response.length();
	}
	
	private String createRegexString(String config) {
		while(config.indexOf("%") != -1) {
			int index = config.indexOf("%");
			int endIndex = config.indexOf(" ", index);
			int indexofStop = config.indexOf(".", index);
			if(indexofStop != -1 && indexofStop < endIndex)
				endIndex = indexofStop;

			if(endIndex == -1)
				endIndex = config.length();
			config = config.substring(0, index) + ".*" + config.substring(endIndex);
		}
		return config;
	}
	
	private String isSuccessfulAutodialResponse(String response) {
		if(response == null)
			return null;
		try {
			int i = Integer.parseInt(response.trim());
			if(i == 0)
				return RBTNode.RESPONSE_SUCCESS;
		}
		catch(Exception e) {
		}
		
		return RBTNode.RESPONSE_FAILURE;
	}
	
	private String isSuccessfulUSSDResponse(String response) {
		if(response != null && response.equalsIgnoreCase("ok"))
			return RBTNode.RESPONSE_SUCCESS;
		return RBTNode.RESPONSE_FAILURE;
	}
	
	private String isSuccessfulMODResponse(String response) {
		if(response == null)
			return null;
		try {
			int i = Integer.parseInt(response.trim());
			if(i == 0 || i == 1)
				return RBTNode.RESPONSE_SUCCESS;
		}
		catch(Exception e) {
		}
		
		return RBTNode.RESPONSE_FAILURE;
	}
	
	private String isSuccessfulECResponse(String response) {
		if(response != null && response.indexOf(">0<") != -1)
			return RBTNode.RESPONSE_SUCCESS;
		return RBTNode.RESPONSE_FAILURE;
	}
	
	private String isSuccessfulEnvioResponse(String response) {
		if (response != null
				&& (response.indexOf(">0<") != -1 || response.indexOf(">1<") != -1
						|| response.indexOf(">2<") != -1 || response.indexOf(">3<") != -1
						|| response.indexOf(">8<") != -1 || response.indexOf(">9<") != -1
						|| response.indexOf(">24<") != -1 || response.indexOf(">27<") != -1 || response
						.indexOf(">30<") != -1))
			return RBTNode.RESPONSE_SUCCESS;
		return RBTNode.RESPONSE_FAILURE;
	}
	
	private String populateSplNodesResponse(RBTNode node, String response) {
		if (node.getNodeName() == null)
			return response;

		if (node.getNodeName().equals(RBTNode.NODE_SMS))
			response = isSuccessfulSMSResponse(response);
		else if (node.getNodeName().equals(RBTNode.NODE_AUTODIAL))
			response = isSuccessfulAutodialResponse(response);
		else if (node.getNodeName().equals(RBTNode.NODE_USSD))
			response = isSuccessfulUSSDResponse(response);
		else if (node.getNodeName().equals(RBTNode.NODE_MOD))
			response = isSuccessfulMODResponse(response);
		else if (node.getNodeName().equals(RBTNode.NODE_EC))
			response = isSuccessfulECResponse(response);
		else if (node.getNodeName().equals(RBTNode.NODE_ENVIO))
			response = isSuccessfulEnvioResponse(response);
		
		return response;
	}
	
	public void endNode(String msisdn, RBTNode node, String response) {
		if(node == null)
			return;
		
		response = populateSplNodesResponse(node, response);

		if(response != null && (response.indexOf("success") != -1 || response.indexOf("SUCCESS") != -1))
			node.setNodeResponse(RBTNode.RESPONSE_SUCCESS);
		else
			node.setNodeResponse(RBTNode.RESPONSE_FAILURE);
		_monitor.endNode(msisdn, node);
	}
	
	public void endNode(String msisdn, RBTNode node, boolean response) {
		endNode(msisdn, node, response ? RBTNode.RESPONSE_SUCCESS : RBTNode.RESPONSE_FAILURE);
	}
	
	public boolean validWebServiceNode(String nodeName) {
		return _monitor.validWebServiceNode(nodeName);
	}
}