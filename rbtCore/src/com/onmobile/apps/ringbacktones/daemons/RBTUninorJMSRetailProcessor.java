package com.onmobile.apps.ringbacktones.daemons;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueReceiver;
import javax.jms.QueueSession;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTUninorJMSRetailProcessor extends Thread
{
	private static final Logger logger = Logger.getLogger(RBTUninorJMSRetailProcessor.class);
	private RBTDaemonManager rbtDaemonManager = null;

	private QueueConnection queueConnection = null;
	private QueueReceiver queueReceiver = null;

	/**
	 * @param rbtDaemonManager
	 * @throws JMSException 
	 * @throws NamingException 
	 */
	public RBTUninorJMSRetailProcessor(RBTDaemonManager rbtDaemonManager) throws NamingException, JMSException
	{
		initialize();
		this.rbtDaemonManager = rbtDaemonManager;
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		try
		{
			while (rbtDaemonManager != null && rbtDaemonManager.isAlive())
			{
				try {
					readAndProcessJMSMessage();

				} catch (Throwable e) {
					logger.error(e.getMessage(), e);
				}
			}
			logger.debug("RBTUninorRetailProcessor Thread is Stopped working..");
		}
		finally
		{
			try {
				if (queueReceiver != null)
					queueReceiver.close();

				if (queueConnection != null)
					queueConnection.close();
			} catch (JMSException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Stores the mapping for JMS server circle id to OM circle ID
	 */
	private Map<String, String> circleMap = new HashMap<String, String>();
	
	private void initCircleMap(String circleMapStr) {
		String[] commaSplit = circleMapStr.split(",");
		for(String mapStr : commaSplit) {
			String[] finalSplit = mapStr.split(":");
			this.circleMap.put(finalSplit[0], finalSplit[1]);
		}
	}

	/**
	 * @throws NamingException
	 * @throws JMSException
	 */
	private void initialize() throws NamingException, JMSException
	{
		try
		{
			String contextFactoryPath = RBTParametersUtils.getParamAsString("DAEMON", "JMS_JNDI_FACTORY_INITIAL", null);
			String providerUrl = RBTParametersUtils.getParamAsString("DAEMON", "JMS_PROVIDER_URL", null);
			String userName = RBTParametersUtils.getParamAsString("DAEMON", "JMS_USERNAME", null);
			String password = RBTParametersUtils.getParamAsString("DAEMON", "JMS_PASSWORD", null);
			String connectionFactoryName = RBTParametersUtils.getParamAsString("DAEMON", "JMS_CONNECTION_FACTORY_NAME", null);
			String queueName = RBTParametersUtils.getParamAsString("DAEMON", "JMS_QUEUE_NAME", null);
			String circleMap = RBTParametersUtils.getParamAsString("DAEMON", "JMS_CIRCLE_MAP", null);
			if(circleMap != null) {
				initCircleMap(circleMap);
			}

			if (contextFactoryPath == null || providerUrl == null
					|| userName == null || password == null
					|| connectionFactoryName == null || queueName == null)
			{
				logger.warn("JMS configurations are incomplete, so initialization has been failed.");
				throw new ExceptionInInitializerError("JMS configurations are incomplete, so initialization has been failed.");
			}

			Hashtable<String, String> env = new Hashtable<String, String>();
			env.put("java.naming.factory.initial", contextFactoryPath);
			env.put("java.naming.provider.url", providerUrl);
			env.put("java.naming.security.principal", userName);
			env.put("java.naming.security.credentials", password);

			Context ctx = new InitialContext(env);
			logger.info("Context created : " + ctx.toString());

			QueueConnectionFactory queueConnectionFactory = (QueueConnectionFactory) ctx.lookup(connectionFactoryName);
			logger.info("ConnectionFactory created : " + queueConnectionFactory);

			Queue myQueue = (Queue) ctx.lookup(queueName);
			logger.info("Got the queue object : " + myQueue);

			queueConnection = queueConnectionFactory.createQueueConnection();
			logger.info("Got the queue connection : " + queueConnection);

			QueueSession queueSession = queueConnection.createQueueSession(false, QueueSession.AUTO_ACKNOWLEDGE);
			logger.info("Created session....");

			this.queueReceiver = queueSession.createReceiver(myQueue);
			queueConnection.start();
		}
		catch (NamingException e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
		catch (JMSException e)
		{
			logger.error(e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 
	 */
	private void readAndProcessJMSMessage()
	{
		try
		{
			Message m = queueReceiver.receive();
			if (m instanceof TextMessage)
			{
				TextMessage textMessage = (TextMessage) m;
				logger.info("Reading message: " + textMessage.getText());
				String jmsXmlResponse = textMessage.getText();

				processJmsXMLResponse(jmsXmlResponse);
			}
		}
		catch (JMSException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * @param xml
	 */
	private void processJmsXMLResponse(String xml)
	{
		if (xml == null)
			return;

		Document document = XMLUtils.getDocumentFromString(xml);
		NodeList nodesList = document.getElementsByTagName("VendorNotification");
		if (nodesList == null || nodesList.getLength() == 0)
		{
			logger.info("Invalid xml message : " + xml);
			writeTrans("NA", "NA", "NA", "INVALID_XML");
			return;
		}

		Element vendorNotificationElement = (Element) nodesList.item(0);

		String subscriberID = null;
		Element subIDElem = (Element) vendorNotificationElement.getElementsByTagName("SubscriberID").item(0);
		if (subIDElem != null && subIDElem.getChildNodes().getLength() > 0)
		{
			Text subIDText = (Text) subIDElem.getFirstChild();
			subscriberID = subIDText.getNodeValue();
		}

		String circleID = null;
		Element circleIDElem = (Element) vendorNotificationElement.getElementsByTagName("ServProv").item(0);
		if (circleIDElem != null && circleIDElem.getChildNodes().getLength() > 0)
		{
			Text circleIDText = (Text) circleIDElem.getFirstChild();
			circleID = circleIDText.getNodeValue();
			
			circleID = getOMCircleID(circleID);
		}

		String rechargeType = null;
		Element rechargeTypeElem = (Element) vendorNotificationElement.getElementsByTagName("RechargeType").item(0);
		if (rechargeTypeElem != null && rechargeTypeElem.getChildNodes().getLength() > 0)
		{
			Text rechargeTypeText = (Text) rechargeTypeElem.getFirstChild();
			rechargeType = rechargeTypeText.getNodeValue();
		}

		if (subscriberID.trim().length() == 0 || circleID.trim().length() == 0 || rechargeType.trim().length() == 0)
		{
			logger.info("Invalid subscriberID or circleID or rechargeType in the message, subscriberID : " + subscriberID + ", circleID : "
					+ circleID + ", rechargeType : " + rechargeType);
			writeTrans(subscriberID, circleID, rechargeType, "INVALID_PARAMETER");
			return;
		}

		String subClass = getSubscriptionClassByCircleIDAndRechargeType(circleID, rechargeType);
		if (subClass == null)
		{
			logger.info("Could not find matching subscription class for circleId and rechargeType combination, circleID : "
					+ circleID + ", rechargeType : " + rechargeType);
			writeTrans(subscriberID, circleID, rechargeType, "INVALID_RC_TYPE");
			return;
		}

		String response = activateOrUpdateSubscription(subscriberID, subClass, RBTParametersUtils.getParamAsString("DAEMON", "MODE_FOR_JMS_RETAIL", "VP"));
		writeTrans(subscriberID, circleID, rechargeType, response);
	}

	private String getOMCircleID(String circleID) {
		//If a map is defined then return the mapped circle
		if(circleMap.containsKey(circleID)) {
			return circleMap.get(circleID);
		}
		//otherwise return the same circle
		return circleID;
	}

	/**
	 * 
	 * Sample format of the below config:
	 * 
	 * "DAEMON", "RECHARGE_TYPE_SUBCLASS_CONFIG_AP", "13001:DEFAULT;13002,13003:DEFAULT_10"
	 * "DAEMON", "RECHARGE_TYPE_SUBCLASS_CONFIG_KK", "14001:DEFAULT;14002,14003:DEFAULT_10"
	 * .
	 * .
	 * 
	 * @param circleID
	 * @param rechargeType
	 * @return
	 */
	private String getSubscriptionClassByCircleIDAndRechargeType(String circleID, String rechargeType)
	{
		String[] rechargeTypesSubClassPairs = RBTParametersUtils
				.getParamAsString("DAEMON","RECHARGE_TYPE_SUBCLASS_CONFIG_"+ circleID.toUpperCase(), "").split(";");

		for (String eachPair : rechargeTypesSubClassPairs)
		{
			String[] tokens = eachPair.split(":");
			Set<String> rechargeTypesSet = new HashSet<String>(Arrays.asList(tokens[0].trim().split(",")));
			if (rechargeTypesSet.contains(rechargeType))
			{
				String subClass = tokens[1].trim();
				return subClass;
			}
		}

		return null;
	}

	/**
	 * Activate or update base subscription. Subscribe to retail pack if the 
	 * subscriber is not active or upgrade to rental pack if the subscriber
	 * is already active. 
	 * 
	 * @param msisdn
	 */
	private String activateOrUpdateSubscription(String msisdn, String subClass, String mode)
	{
		String response = "error";
		try {
			if (logger.isDebugEnabled()) {
				logger.debug(" Activating subscriber: " + msisdn + ", subClass: "
						+ subClass + ", mode: " + mode);
			}

			SubscriptionRequest subscriptionRequest = new SubscriptionRequest(
					msisdn);
			subscriptionRequest.setRentalPack(subClass);
			subscriptionRequest.setMode(mode);
			subscriptionRequest.setModeInfo(mode);
			subscriptionRequest.setUpgradeGraceAndSuspended(true);
			subscriptionRequest.setSuspendedUsersAllowed(true);

			// activate or update subscription
			RBTClient.getInstance().activateSubscriber(subscriptionRequest);
			response = subscriptionRequest.getResponse();

			if (logger.isDebugEnabled()) {
				logger.debug(" Tried to activate/update subscriber: " + msisdn
						+ ", response: " + response);
			}

			return response;
		}
		catch (Throwable t)
		{
			logger.error(t.getMessage(), t);
		}

		return response;
	}

	/**
	 * @param response
	 */
	private void writeTrans(String subscriberID, String circleID, String rechargeType, String response)
	{
		StringBuilder logBuilder = new StringBuilder();
		logBuilder.append(subscriberID).append(", ")
				.append(circleID).append(", ").append(rechargeType)
				.append(" - ").append(response);
		RBTEventLogger.logEvent(RBTEventLogger.Event.JMS_RETAIL,
				logBuilder.toString());
	}
}
