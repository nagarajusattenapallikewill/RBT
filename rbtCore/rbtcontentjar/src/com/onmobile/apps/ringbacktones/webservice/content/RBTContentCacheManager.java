/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.content;

import org.w3c.dom.Node;

import com.onmobile.apps.ringbacktones.webservice.common.RBTLogger;
import com.onmobile.common.cjni.IJavaComponent;
import com.onmobile.common.cjni.O3InterfaceHelper;
import com.onmobile.common.cjni.O3OzoneInformation;
import com.onmobile.common.message.O3InfoMessage;
import com.onmobile.common.message.O3Message;

/**
 * @author vinayasimha.patil
 *
 */
public class RBTContentCacheManager implements IJavaComponent
{
	private static final String CLASSNAME = "RBTContentCacheManager";
	public static final String COMPONENT_NAME = "RBTContentCacheManager";

	private O3InterfaceHelper o3InterfaceHelper = null;

	private ContentRefreshTCPServer contentRefreshTCPServer = null;

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#configureComponent(org.w3c.dom.Node)
	 */
	public int configureComponent(Node arg0)
	{
		return JAVA_COMPONENT_SUCCESS;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getBuildComment()
	 */
	public String getBuildComment()
	{
		return "";
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getBuildDate()
	 */
	public String getBuildDate()
	{
		return "";
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getBuildTime()
	 */
	public String getBuildTime()
	{
		return "";
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getBuildVersion()
	 */
	public String getBuildVersion()
	{
		return "";
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getComponentName()
	 */
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getKey()
	 */
	public String getKey()
	{
		return "";
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#getSubKey()
	 */
	public String getSubKey()
	{
		return "";
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#initComponent(com.onmobile.common.cjni.O3InterfaceHelper)
	 */
	public int initComponent(O3InterfaceHelper o3InterfaceHelper)
	{
		this.o3InterfaceHelper = o3InterfaceHelper;
		return JAVA_COMPONENT_SUCCESS;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#isSuspendCompleted()
	 */
	public int isSuspendCompleted()
	{
		return -2;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#processMessage(com.onmobile.common.message.O3Message)
	 */
	public void processMessage(O3Message o3Message)
	{
		try
		{
			String messageID = o3Message.getMessageID();
			String messageData = o3Message.getData();
			RBTLogger.logDetail(CLASSNAME, "processMessage", "RBT:: messagedata = " + messageData);

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
			RBTLogger.logException(CLASSNAME, "processMessage", e);
		}

		o3Message.close();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#resumeComponent()
	 */
	public int resumeComponent()
	{
		return -2;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#startComponent()
	 */
	public int startComponent()
	{
		RBTLogger.logDetail(CLASSNAME, "startComponent", "RBT:: Starting " + COMPONENT_NAME);

		RBTContent rbtContent = RBTContent.createRBTContent();
		broadcastInfoMessage("RBT_CACHE_REFRESH", COMPONENT_NAME, null, "RESTART");


		String tcpServerHost = rbtContent.getRBTServiceParameter("TCP_SERVER_HOST"); 
		if (tcpServerHost != null)
		{
			RBTLogger.logDetail(CLASSNAME, "startComponent", "RBT:: tcpServerHost: " + tcpServerHost);
			tcpServerHost = tcpServerHost.trim();
			String ipAddress = tcpServerHost.substring(0, tcpServerHost.indexOf(':'));
			String exeName = tcpServerHost.substring(tcpServerHost.indexOf(':') + 1);

			O3OzoneInformation o3OzoneInformation = o3InterfaceHelper.getO3OzoneInformation();
			String thisIPAddress = o3OzoneInformation.getHostIP();
			String thisExeName = o3OzoneInformation.getOzoneExeName();
			RBTLogger.logDetail(CLASSNAME, "startComponent", "RBT:: thisServerHost: " + thisIPAddress + ":" + thisExeName);

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

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#stopComponent()
	 */
	public void stopComponent()
	{
		if (contentRefreshTCPServer != null)
			contentRefreshTCPServer.stop();
	}

	/* (non-Javadoc)
	 * @see com.onmobile.common.cjni.IJavaComponent#suspendComponent()
	 */
	public int suspendComponent()
	{
		return -2;
	}

	public synchronized boolean broadcastInfoMessage(String messageID, String dstComponent, String refKey, String messageData)
	{
		boolean response = false;

		if (o3InterfaceHelper == null)
		{
			RBTLogger.logDetail(CLASSNAME, "broadcastInfoMessage", "RBT:: O3InterfaceHelper not initialized");
			return response;
		}

		try
		{
			StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append("RBT:: messageID = ").append(messageID);
			stringBuilder.append(", dstComponent = ").append(dstComponent);
			stringBuilder.append(", refKey = ").append(refKey);
			stringBuilder.append(", messageData = ").append(messageData);
			RBTLogger.logDetail(CLASSNAME, "broadcastInfoMessage", stringBuilder.toString());

			O3InfoMessage o3InfoMessage = (O3InfoMessage) o3InterfaceHelper.getOzoneMessenger().createInfoMessage(messageID, dstComponent, COMPONENT_NAME, refKey, messageData);
			int noOfMsgSent = o3InterfaceHelper.getOzoneMessenger().broadcastOzoneMessage(o3InfoMessage, dstComponent, refKey, null);

			RBTLogger.logDetail(CLASSNAME, "broadcastInfoMessage", "RBT:: noOfMsgSent = " + noOfMsgSent);
			if (noOfMsgSent > 0)
				response = true;
		}
		catch (Exception e)
		{
			RBTLogger.logException(CLASSNAME, "broadcastInfoMessage", e);
		}

		RBTLogger.logDetail(CLASSNAME, "broadcastInfoMessage", "RBT:: response = " + response);
		return response;
	}

}
