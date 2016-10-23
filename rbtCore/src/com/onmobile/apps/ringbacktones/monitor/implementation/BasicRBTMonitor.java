package com.onmobile.apps.ringbacktones.monitor.implementation;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Monitoring;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.monitor.RBTNode;
import com.onmobile.apps.ringbacktones.monitor.iRBTMonitor;
import com.onmobile.apps.ringbacktones.monitor.common.Constants;
import com.onmobile.apps.ringbacktones.monitor.common.Utility;
import com.onmobile.apps.ringbacktones.provisioning.AdminFacade;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

/**
 * Basic monitoring implementation for RBT. This is a singleton class
 * 
 * @author Sreekar
 * @since 2010-01-07
 */
public class BasicRBTMonitor implements iRBTMonitor, iRBTConstant, Constants {
	private static Logger _logger = Logger.getLogger(BasicRBTMonitor.class);
	private static Map<String, RBTNode> _monitorNodeMap = new HashMap<String, RBTNode>();
	private static Map<String, String> _monitorSubMap = new HashMap<String, String>();

	private static Map<String, Thread> _monitorWaitMap = new HashMap<String, Thread>();

	private RBTDBManager _dbManager;
	private DateFormat _formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
	
	private String thisMonitorNodeResponse = "<%N-STATUS >%R</%N-STATUS><%N-LATENCY>%D</%N-LATENCY>";

	public BasicRBTMonitor() throws RBTException {
		initDBManager();
	}

	private void initDBManager() {
		_dbManager = RBTDBManager.getInstance();
	}

	/**
	 * This method makes an initial entry into RBT_MONITORING table with trace result as null
	 * 
	 * @param msisdn subscriber number for which the trace has to be started
	 */
	public String startMonitor(String msisdn, String traceType) {
		synchronized (_monitorSubMap) {
			StringBuffer reason = new StringBuffer();
			if (!canMonitor(msisdn, traceType, reason))
				return reason.toString();
			_monitorSubMap.put(msisdn, traceType);
			_dbManager.insertMonitor(msisdn, null, traceType, MONITOR_STATE_STARTED, null);
		}
		return RESPONSE_SUCCESS;
	}

	/**
	 * This method updates the trace entry in RBT_MONITORING to trace completed state
	 */
	public String endMonitor(String msisdn) {
		synchronized (_monitorSubMap) {
			if (_monitorSubMap.containsKey(msisdn)) {
				notifyWaitingThread(msisdn);
				return RESPONSE_SUCCESS;
			}
			else
				return RESPONSE_NO_ACTIVE_MONITOR;
		}
	}

	private boolean ignoreFailureResponse(String nodeName) {
		if (nodeName.equals(RBTNode.NODE_PLAYER_DAEMON_SEL)
				|| nodeName.equals(RBTNode.NODE_PLAYER_DAEMON_SUB))
			return true;
		return false;
	}

	/**
	 * This method makes/updates entry in RBT_MONITORING table
	 * 
	 * @param msisdn number for which the monitoring is to be updated
	 * @param node node @ which monitoring is happening
	 */
	public void endNode(String msisdn, RBTNode node) {
		if (_monitorNodeMap.containsKey(msisdn)) {
			updateMonitor(msisdn, node);
			deregisterSubNode(msisdn);
			boolean endMonitor = false;
			boolean successResponse = node.getNodeRespone().equals(RBTNode.RESPONSE_SUCCESS);
			if (node.getNodeRespone() != null && !successResponse
					&& !ignoreFailureResponse(node.getNodeName())) {
				_logger.warn("RBT::ending trace for subscriber->" + msisdn
						+ " as we have failure @ node->" + node.getNodeName());
				endMonitor = true;
			}
			if (successResponse) {
				Monitoring pendingMonitor = _dbManager.getPendingSubscriberMonitor(msisdn);
				if (pendingMonitor == null)
					return;
				String nodeName = node.getNodeName();
				String traceResult = pendingMonitor.traceResult();
				// if node is player selection and if already got SM callback then end monitor
				if (nodeName.equals(RBTNode.NODE_PLAYER_DAEMON_SEL)
						&& checkNodeSuccess(RBTNode.NODE_SM_CALLBACK_SEL, traceResult))
					endMonitor = true;
				// if node is SM callback selection and if already updated player then end monitor
				else if (nodeName.equals(RBTNode.NODE_SM_CALLBACK_SEL)
						&& checkNodeSuccess(RBTNode.NODE_PLAYER_DAEMON_SEL, traceResult))
					endMonitor = true;
				// if node is player subscription and not selection request and if already got SM
				// callback then end monitor
				else if (nodeName.equals(RBTNode.NODE_PLAYER_DAEMON_SUB)
						&& checkNodeSuccess(RBTNode.NODE_SM_CALLBACK_SUB, traceResult)
						&& !isSelectionPending(msisdn))
					endMonitor = true;
				// if node is SM callback subscription and not selection request and if already
				// updated player then end monitor
				else if (nodeName.equals(RBTNode.NODE_SM_CALLBACK_SUB)
						&& checkNodeSuccess(RBTNode.NODE_PLAYER_DAEMON_SUB, traceResult)
						&& !isSelectionPending(msisdn))
					endMonitor = true;
			}

			if (endMonitor)
				httpEndMonitor(msisdn);
		}
	}

	private boolean isSelectionPendingToPlayer(SubscriberStatus[] allSelections) {
		if (allSelections == null || allSelections.length == 0)
			return false;
		for (int i = 0; i < allSelections.length; i++) {
			char loopStatus = allSelections[i].loopStatus();
			if (loopStatus == LOOP_STATUS_LOOP || loopStatus == LOOP_STATUS_LOOP_INIT
					|| loopStatus == LOOP_STATUS_OVERRIDE
					|| loopStatus == LOOP_STATUS_OVERRIDE_INIT)
				return true;
		}
		return false;
	}

	private boolean isSelectionPending(String msisdn) {
		if (msisdn == null)
			return false;
		SubscriberStatus[] allSelections = _dbManager.getAllActiveSelToUpdatePlayer(msisdn);
		return isSelectionPendingFromSM(allSelections) || isSelectionPendingToPlayer(allSelections);
	}

	/*
	 * private boolean isSelectionPendingToPlayer(String msisdn) { if(msisdn == null) return false;
	 * SubscriberStatus[] allSelections = _dbManager.getAllActiveSelToUpdatePlayer(msisdn); return
	 * isSelectionPendingToPlayer(allSelections); }
	 * 
	 * private boolean isSelectionPendingFromSM(String msisdn) { if(msisdn == null) return false;
	 * SubscriberStatus[] allSelections = _dbManager.getAllActiveSelToUpdatePlayer(msisdn); return
	 * isSelectionPendingFromSM(allSelections); }
	 */

	private boolean isSelectionPendingFromSM(SubscriberStatus[] allSelections) {
		if (allSelections == null || allSelections.length == 0)
			return false;
		for (int i = 0; i < allSelections.length; i++) {
			String selStatus = allSelections[i].selStatus();
			if (selStatus.equals(STATE_ACTIVATION_PENDING)
					|| selStatus.equals(STATE_BASE_ACTIVATION_PENDING)
					|| selStatus.equals(STATE_TO_BE_ACTIVATED))
				return true;
		}
		return false;
	}

	private boolean checkNodeSuccess(String nodeName, String traceResult) {
		if (nodeName == null || traceResult == null)
			return false;
		int lastIndex = traceResult.lastIndexOf(nodeName);
		if (lastIndex == -1)
			return false;
		int nextNodeIndex = traceResult.indexOf("<Node", lastIndex);
		String subStr = traceResult.substring(lastIndex, nextNodeIndex);
		if (subStr.indexOf(RBTNode.RESPONSE_SUCCESS) == -1)
			return false;
		return true;
	}

	private void httpEndMonitor(String msisdn) {
		String url = Utility.getBaseURLForSubscriber(msisdn);
		url += "/rbt_end_monitor.jsp?msisdn=" + msisdn;
		HttpParameters httpParameters = new HttpParameters(url);
		String response = null;
		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParameters, null);
			Logger.getLogger(AdminFacade.class).info("RBT:: httpResponse: " + httpResponse);
			response = httpResponse.getResponse();
			_logger.info("RBT::response for url->" + url + " is " + response);
		}
		catch (Exception e) {
			_logger.error("RBT:: " + e.getMessage(), e);
		}
	}

	private void updateMonitor(String msisdn, RBTNode node) {
		_dbManager.concatToActiveMonitor(msisdn, getXMLForNode(node));
	}

	private String getXMLForNode(RBTNode node) {
		long delay = new Date().getTime() - node.getStartTime().getTime();
		return "<Node name=\"" + node.getNodeName() + "\"  response=\"" + node.getNodeRespone()
				+ "\" startTime=\"" + _formatter.format(node.getStartTime()) + "\" delay=\""
				+ delay + "\" />";
	}

	public void startNode(String msisdn, RBTNode node) {
		if (_monitorNodeMap.containsKey(msisdn))
			return;
		Monitoring pendingMonitor = _dbManager.getPendingSubscriberMonitor(msisdn);
		if (pendingMonitor == null)
			return;
		registerSubNode(msisdn, node);
	}

	/**
	 * This method checks of monitoring can happen for this number/node
	 */
	public boolean canMonitor(String msisdn, String traceType, StringBuffer reason) {
		// checking the configuration if can monitor for this number
		String numbersList = CacheManagerUtil.getParametersCacheManager().getParameter(MONITOR,
				"MONITOR_NUMBERS", "").getValue();
		StringTokenizer stk = new StringTokenizer(numbersList, ",");
		boolean numberPresent = false;
		while (stk.hasMoreTokens())
			if (stk.nextToken().equals(msisdn))
				numberPresent = true;
		if (!numberPresent) {
			reason.append(RESPONSE_NOT_TRACE_NUMBER);
			return false;
		}
		if (_monitorSubMap.containsKey(msisdn)) {
			_logger.warn("RBT::cannot monitor " + msisdn + " as monitor is on for with type->"
					+ _monitorSubMap.get(msisdn));
			reason.append(RESPONSE_TRACE_IN_PROCESS);
			return false;
		}
		// checking for any live nodes for the subscriber
		if (_monitorNodeMap.containsKey(msisdn)) {
			_logger.warn("RBT::cannot monitor " + msisdn + " as monitor is on for the same @ node "
					+ _monitorNodeMap.get(msisdn));
			reason.append(RESPONSE_TRACE_IN_PROCESS);
			return false;
		}
		// check in the monitoring table for any entry for subscriber or monitor_type
		Monitoring pendingMonitor = _dbManager.getPendingSubscriberOrTypeMonitor(msisdn, traceType);
		if (pendingMonitor != null) {
			_logger.warn("RBT::monitoring already on->" + pendingMonitor);
			reason.append(RESPONSE_TRACE_IN_PROCESS);
			return false;
		}
		return true;
	}

	/**
	 * This method stores the subscriber as key & node as value in hashmap indicating trace is on
	 * for the sub
	 * 
	 * @param msisdn MSISDN for which the monitoring is on
	 * @param node RBTNode @ which subscriber trace is on now
	 */
	protected void registerSubNode(String msisdn, RBTNode node) {
		synchronized (_monitorNodeMap) {
			if (!_monitorNodeMap.containsKey(msisdn)) {
				node.setNodeStartTime(new Date());
				_monitorNodeMap.put(msisdn, node);
			}
		}
	}

	protected void deregisterSubNode(String msisdn) {
		synchronized (_monitorNodeMap) {
			if (_monitorNodeMap.containsKey(msisdn))
				_monitorNodeMap.remove(msisdn);
		}
	}

	/**
	 * Initiates the copy monitoring request
	 * 
	 * @param contains all the request parameters
	 */
	public String processCopyMonitor(HashMap<String, String> map) {
		String cCode = map.get(param_ccode);
		String vCode = map.get(param_vcode);
		Clip clip = null;
		if (cCode != null)
			clip = getClipByPromoID(cCode);
		else if (vCode != null)
			clip = getClipByVCode(vCode);
		if (clip == null) {
			_logger.warn("RBT::clip not found for the request");
			return null;
		}
		String msisdn = map.get(param_msisdn);
		String called = map.get(param_called);
		String keyPressed = map.get(param_keypressed);
		if (keyPressed == null)
			keyPressed = "s";
		String url = Utility.getBaseURLForSubscriber(msisdn);
		url += "/rbt_player_updater.jsp?ACTION=COPY&DETAILS=" + called + ":" + msisdn + ":"
				+ clip.getClipRbtWavFile() + ":26:1&KEYPRESSED=" + keyPressed + "&EXTRA_PARAMS="
				+ keyPressed;
		return hitIntialURLAndRespond(msisdn, url);
	}

	private String holdRequestAndRespond(String msisdn) {
		Thread currentThread = Thread.currentThread();
		if (_monitorWaitMap.containsKey(msisdn)) {
			synchronized (currentThread) {
				try {
					currentThread.wait();
				}
				catch (InterruptedException e) {
					_logger.error("RBT::Exception while trying to wait on the current thread", e);
				}
			}
		}
		else {
			_logger.info("RBT::no need to wait. I guess got failure from initial node");
		}
		populateEndNode(msisdn);
		Monitoring monitor = _dbManager.getPendingSubscriberMonitor(msisdn);
		if (monitor != null) {
			_dbManager.endActiveMonitor(msisdn, _monitorSubMap.get(msisdn));
			_monitorSubMap.remove(msisdn);
			_monitorNodeMap.remove(msisdn);
			String traceResult = monitor.traceResult();
			try {
				return formatTraceResult(traceResult);
			}
			catch(Exception e) {
				_logger.error("RBT::Exception while formatting result. returning DB result", e);
				return traceResult;
			}
		}
		return null;
	}
	
	private String formatTraceResult(String traceResult) throws ParseException {
		StringBuilder sb = new StringBuilder("<Response><serviceName>CRBT</serviceName>");
		int nodeStartIndex = traceResult.indexOf("<Node");
		long lastNodeEndTime = 0;
		while (nodeStartIndex != -1) {
			int nodeEndIndex = traceResult.indexOf(" />", nodeStartIndex);
			lastNodeEndTime = appendNodeDetailToResult(traceResult.substring(nodeStartIndex,
					nodeEndIndex), sb, lastNodeEndTime);
			nodeStartIndex = traceResult.indexOf("<Node", nodeStartIndex + 5);
			if(nodeStartIndex != -1 && nodeStartIndex != nodeEndIndex+3) {
				System.out.println("nodeStartIndex->" + nodeStartIndex + ", nodeEndIndex->" + nodeEndIndex);
				sb.append(traceResult.substring(nodeEndIndex+3, nodeStartIndex));
			}
		}
		sb.append("</Response>");
		return sb.toString();
	}
	
	private long appendNodeDetailToResult(String node, StringBuilder sb, long lastNodeEndTime) throws ParseException {
		node = node.replace("\"", "");
		int nameIndex = node.indexOf("name=");
		int responseIndex = node.indexOf("response=");
		int startTimeIndex = node.indexOf("startTime=");
		int delayIndex = node.indexOf("delay=");
		String nodeName = node.substring(nameIndex + 5, responseIndex).trim();
		String nodeResponse = node.substring(responseIndex + 9, startTimeIndex)
				.trim();
		String nodeStartTime = node.substring(startTimeIndex + 10, delayIndex).trim();
		String nodeDelay = node.substring(delayIndex + 6);

		long startTime = _formatter.parse(nodeStartTime).getTime();
		long delay = Long.parseLong(nodeDelay);
		long completeDelay = delay;
		if (lastNodeEndTime == 0)
			lastNodeEndTime = startTime;
		else
			completeDelay = (startTime - lastNodeEndTime) + delay;

		String thisNodeResponse = thisMonitorNodeResponse.replaceAll("%N", nodeName);
		thisNodeResponse = thisNodeResponse.replaceAll("%R", nodeResponse);
		thisNodeResponse = thisNodeResponse.replaceAll("%D", completeDelay + "");

		sb.append(thisNodeResponse);

		return lastNodeEndTime + completeDelay;
	}

	private void populateEndNode(String msisdn) {
		RBTNode endNode = new RBTNode(RBTNode.NODE_END);
		startNode(msisdn, endNode);
		endNode.setNodeResponse(RBTNode.RESPONSE_SUCCESS);
		endNode(msisdn, endNode);
	}

	private void notifyWaitingThread(String msisdn) {
		if (!_monitorWaitMap.containsKey(msisdn)) {
			_logger.warn("RBT::Cannot notify for subscriber->" + msisdn + ", no thread waiting");
			return;
		}
		Thread waitingThread = _monitorWaitMap.get(msisdn);
		synchronized (waitingThread) {
			if (_monitorWaitMap.containsKey(msisdn)) {
				waitingThread.notify();
				_logger.info("RBT::Notified on thread->" + waitingThread.getName() + " for sub->"
						+ msisdn);
				_monitorWaitMap.remove(msisdn);
			}
		}
	}

	public String processIVRMonitor(HashMap<String, String> map) {
		map.put(param_tracetype, TRACE_TYPE_IVR);
		return processWebServiceMonitor(map);
	}

	public String processSMSMonitor(HashMap<String, String> map) {
		String msisdn = map.get(param_msisdn);
		String url = Utility.getBaseURLForSubscriber(msisdn);
		url += "/rbt_sms.jsp?SUB_ID=" + msisdn + "&SMS_TEXT=" + map.get(param_smstext);
		return hitIntialURLAndRespond(msisdn, url);
	}

	public String processThirdPartyMonitor(HashMap<String, String> map) {
		// TODO Auto-generated method stub
		String traceType = map.get(param_tracetype);
		String msisdn = map.get(param_msisdn);
		String url = Utility.getBaseURLForSubscriber(msisdn);
		if (traceType.equals(TRACE_TYPE_AUTODIAL))
			url += "/autodial.jsp?";
		else if (traceType.equals(TRACE_TYPE_EC))
			url += "/easycharge.jsp?";
		else if (traceType.equals(TRACE_TYPE_USSD))
			url += "/ussd.jsp?";
		else if (traceType.equals(TRACE_TYPE_ENVIO))
			url += "/envio.jsp?";
		else if (traceType.equals(TRACE_TYPE_MOD))
			url += "/mod.jsp?";
		else if (traceType.equals(TRACE_TYPE_PROMOTION))
			url += "/rbt_promotion.jsp?";

		Iterator<String> itr = map.keySet().iterator();
		StringBuffer sb = new StringBuffer(url);
		while (itr.hasNext()) {
			String param = itr.next();
			sb.append(param + "=" + map.get(param) + "&");
		}
		url = sb.toString();
		_logger.info("RBT::BAC start url->" + url);
		return hitIntialURLAndRespond(msisdn, url);
	}

	public boolean validWebServiceNode(String nodeName) {
		if (nodeName == null)
			return false;
		if (nodeName.equals(RBTNode.NODE_IVR) || nodeName.equals(RBTNode.NODE_WEBSERVICE)
				|| nodeName.equals(RBTNode.NODE_CCC))
			return true;
		return false;
	}

	public String processWebServiceMonitor(HashMap<String, String> map) {
		if (!map.containsKey(param_tracetype))
			map.put(param_tracetype, TRACE_TYPE_WEBSERVICE);
		String action = map.get(param_action);
		String response = null;
		if (action == null)
			return null;
		if (action.equals(ACTION_ACTIVATE))
			response = processWebServiceActivation(map);
		else if (action.equals(ACTION_SELECTION))
			response = processWebServiceSelection(map);
		else if (action.equals(ACTION_DEACTIVATE))
			response = processWebServiceDeactivation(map);
		else {
			_logger.warn("RBT::invalid action for webservice request->" + action);
		}

		return response;
	}

	private String processWebServiceDeactivation(HashMap<String, String> map) {
		String msisdn = map.get(param_msisdn);
		String url = Utility.getBaseURLForSubscriber(msisdn);
		url += "/Subscription.do?action=deactivate&subscriberID=" + msisdn + "&mode="
				+ map.get(param_tracetype);
		return hitIntialURLAndRespond(msisdn, url);
	}

	private String processWebServiceSelection(HashMap<String, String> map) {
		HashMap<String, String> contentMap = getClipCategory(map);
		if (contentMap == null)
			return null;
		String categoryID = "6";
		String clipID = "-1";
		if (contentMap.containsKey("category"))
			categoryID = contentMap.get("category");
		if (contentMap.containsKey("clip"))
			clipID = contentMap.get("clip");

		String msisdn = map.get(param_msisdn);
		String url = Utility.getBaseURLForSubscriber(msisdn);
		url += "/Selection.do?action=set&subscriberID=" + msisdn + "&mode="
				+ map.get(param_tracetype) + "&callerID=all&categoryID=" + categoryID
				+ "&isPrepaid=y&clipID=" + clipID;
		return hitIntialURLAndRespond(msisdn, url);
	}

	private String processWebServiceActivation(HashMap<String, String> map) {
		String msisdn = map.get(param_msisdn);
		String url = Utility.getBaseURLForSubscriber(msisdn);
		url += "/Subscription.do?action=activate&subscriberID=" + msisdn + "&mode="
				+ map.get(param_tracetype) + "&isPrepaid=y";
		return hitIntialURLAndRespond(msisdn, url);
	}

	public String getGenericErrorResponse() {
		return RESPONSE_GENERIC_FAILURE;
	}

	public Clip getClipByPromoID(String promoID) {
		return RBTCacheManager.getInstance().getClipByPromoId(promoID);
	}

	public Clip getClipByVCode(String vCode) {
		return getClipByWavFIle("rbt_" + vCode + "_rbt");
	}

	public Category getCategoryByPromoID(String promoID) {
		return RBTCacheManager.getInstance().getCategoryByPromoId(promoID);
	}

	public Clip getClipByWavFIle(String wavFile) {
		return RBTCacheManager.getInstance().getClipByRbtWavFileName(wavFile);
	}

	private String hitIntialURLAndRespond(String msisdn, String url) {
		synchronized (url) {
			_monitorWaitMap.put(msisdn, Thread.currentThread());
		}
		HttpParameters httpParameters = new HttpParameters(url);
		String response = null;
		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParameters, null, null);
			_logger.info("RBT:: httpResponse: " + httpResponse);
			response = httpResponse.getResponse();
			_logger.info("RBT::response for url->" + url + " is " + response);
		}
		catch (Exception e) {
			_logger.error("RBT:: " + e.getMessage(), e);
		}
		if (response != null)
			return holdRequestAndRespond(msisdn);
		return null;
	}

	private HashMap<String, String> getClipCategory(HashMap<String, String> map) {
		String cCode = map.get(param_ccode);
		String vCode = map.get(param_vcode);
		String albumCode = map.get(param_albumcode);
		Clip clip = null;
		Category category = null;
		if (cCode != null)
			clip = getClipByPromoID(cCode);
		else if (vCode != null)
			clip = getClipByVCode(vCode);
		if (albumCode != null)
			category = getCategoryByPromoID(albumCode);
		if (((cCode != null || vCode != null) && clip == null)
				|| (albumCode != null && category == null)) {
			_logger.warn("RBT::clip/album not found for the request");
			return null;
		}
		HashMap<String, String> returnMap = new HashMap<String, String>();
		if (category != null)
			returnMap.put("category", category.getCategoryId() + "");
		if (clip != null)
			returnMap.put("clip", clip.getClipId() + "");

		return returnMap;
	}

	public boolean isThirdPartyRequest(String traceType) {
		if (traceType.equals(TRACE_TYPE_ENVIO) || traceType.equals(TRACE_TYPE_EC)
				|| traceType.equals(TRACE_TYPE_USSD) || traceType.equals(TRACE_TYPE_MOD)
				|| traceType.equals(TRACE_TYPE_AUTODIAL))
			return true;
		return false;
	}
}