/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.content;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;
import com.onmobile.common.cjni.O3InterfaceHelper;
import com.onmobile.common.cjni.O3OzoneInformation;
import com.onmobile.common.message.O3InfoMessage;
import com.onmobile.common.message.O3Message;

/**
 * @author vinayasimha.patil
 *
 */
public class RBTContentCacheManager extends Ozonized
{
	private static Logger logger = Logger.getLogger(RBTContentCacheManager.class);
	
	public static final String COMPONENT_NAME = "RBTContentCacheManager";

	private O3InterfaceHelper o3InterfaceHelper = null;

	private ContentRefreshTCPServer contentRefreshTCPServer = null;

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int initComponent(O3InterfaceHelper o3InterfaceHelper)
	{
		this.o3InterfaceHelper = o3InterfaceHelper;
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void processMessage(O3Message o3Message)
	{
		try
		{
			String messageID = o3Message.getMessageID();
			String messageData = o3Message.getData();
			logger.info("RBT:: messagedata = " + messageData);

			if (messageID.equalsIgnoreCase("RBT_CACHE_REFRESH"))
			{
				if (messageData.equalsIgnoreCase("RESTART"))
				{
					RBTContent.createRBTContent();
				}
				else if (messageData.equalsIgnoreCase("ALL"))
				{
					RBTContent.getInstance().clearContentMap();
				}
				else
				{
					String[] datas = messageData.split(",");
					for (String data : datas)
					{
						int contentID = Integer.parseInt(data);
						RBTContent.getInstance().clearContentMap(contentID);
					}
				}
			}
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		o3Message.close();
	}

	@Override
	public int startComponent()
	{
		logger.info("RBT:: Starting " + COMPONENT_NAME);

		RBTContent rbtContent = RBTContent.createRBTContent();
		broadcastInfoMessage("RBT_CACHE_REFRESH", COMPONENT_NAME, null, "RESTART");


		String tcpServerHost = rbtContent.getRBTServiceParameter("TCP_SERVER_HOST"); 
		if (tcpServerHost != null)
		{
			logger.info("RBT:: tcpServerHost: " + tcpServerHost);
			tcpServerHost = tcpServerHost.trim();
			String ipAddress = tcpServerHost.substring(0, tcpServerHost.indexOf(':'));
			String exeName = tcpServerHost.substring(tcpServerHost.indexOf(':') + 1);

			O3OzoneInformation o3OzoneInformation = o3InterfaceHelper.getO3OzoneInformation();
			String thisIPAddress = o3OzoneInformation.getHostIP();
			String thisExeName = o3OzoneInformation.getOzoneExeName();
			logger.info("RBT:: thisServerHost: " + thisIPAddress + ":" + thisExeName);

			if (thisIPAddress.equalsIgnoreCase(ipAddress) && thisExeName.equalsIgnoreCase(exeName))
			{			
				int tcpServerPort = 8000;
				String tcpServerPortStr = rbtContent.getRBTServiceParameter("TCP_SERVER_PORT"); 
				if (tcpServerPortStr != null)
					tcpServerPort = Integer.parseInt(tcpServerPortStr.trim());

				contentRefreshTCPServer = new ContentRefreshTCPServer(this, tcpServerPort, 1000);
			}
		}

		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{
		if (contentRefreshTCPServer != null)
			contentRefreshTCPServer.stop();
	}

	public synchronized boolean broadcastInfoMessage(String messageID, String dstComponent, String refKey, String messageData)
	{
		boolean response = false;

		if (o3InterfaceHelper == null)
		{
			logger.info("RBT:: O3InterfaceHelper not initialized");
			return response;
		}

		try
		{
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: messageID = ").append(messageID);
			stringBuilder.append(", dstComponent = ").append(dstComponent);
			stringBuilder.append(", refKey = ").append(refKey);
			stringBuilder.append(", messageData = ").append(messageData);
			logger.info(stringBuilder.toString());

			O3InfoMessage o3InfoMessage = (O3InfoMessage) o3InterfaceHelper.getOzoneMessenger().createInfoMessage(messageID, dstComponent, COMPONENT_NAME, refKey, messageData);
			int noOfMsgSent = o3InterfaceHelper.getOzoneMessenger().broadcastOzoneMessage(o3InfoMessage, dstComponent, refKey, null);

			logger.info("RBT:: noOfMsgSent = " + noOfMsgSent);
			if (noOfMsgSent > 0)
				response = true;
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		logger.info("RBT:: response = " + response);
		return response;
	}
}
