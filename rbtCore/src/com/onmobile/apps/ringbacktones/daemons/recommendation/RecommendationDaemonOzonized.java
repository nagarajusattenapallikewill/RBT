/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.recommendation;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.daemons.Ozonized;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.common.cjni.O3InterfaceHelper;
import com.onmobile.common.message.O3InfoMessage;

/**
 * @author vinayasimha.patil
 *
 */
public class RecommendationDaemonOzonized extends Ozonized
{
	private static Logger logger = Logger.getLogger(RecommendationDaemonOzonized.class);

	private static final String COMPONENT_NAME = "RBTRecommendationDaemon";

	private static O3InterfaceHelper o3InterfaceHelper = null;

	List<DaemonThread> taskList = null;

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int initComponent(O3InterfaceHelper o3InterfaceHelper)
	{
		RecommendationDaemonOzonized.o3InterfaceHelper = o3InterfaceHelper;

		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public int startComponent()
	{
		RBTDBManager.getInstance();

		taskList = new ArrayList<DaemonThread>();

		String[] taskListStr = { "ContentRecommendationByCategory", "ContentRecommendationByArtist",
		"TopDownloadsCategoryPopulator" };
		Parameters taskListParam = CacheManagerUtil.getParametersCacheManager().getParameter(iRBTConstant.DAEMON, "RECOMMENDATION_TASK_LIST");
		if(taskListParam != null)
			taskListStr = taskListParam.getValue().trim().split(",");

		String basePackage = this.getClass().getPackage().getName();
		for (int i = 0; i < taskListStr.length; i++)
		{
			try
			{
				Class<?> daemonThreadClass = Class.forName(basePackage +"."+ taskListStr[i]);
				DaemonThread daemonThread = (DaemonThread) daemonThreadClass.newInstance();
				taskList.add(daemonThread);
			}
			catch (ClassNotFoundException e)
			{
				logger.error("", e);
			}
			catch (InstantiationException e)
			{
				logger.error("", e);
			}
			catch (IllegalAccessException e)
			{
				logger.error("", e);
			}
		}

		for (DaemonThread daemonThread : taskList)
		{
			logger.info("The name of the class being loaded : "+daemonThread.getClass().getName());
			daemonThread.start();
		}

		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{
		for (DaemonThread daemonThread : taskList)
		{
			daemonThread.stop();
		}
	}

	public synchronized static boolean broadcastInfoMessage(String messageID, String dstComponent, String refKey, String messageData)
	{
		boolean response = false;

		if(o3InterfaceHelper == null)
		{
			logger.info("RBT:: O3InterfaceHelper not initialized");
			return response;
		}

		try
		{
			logger.info("RBT:: messageID = "+ messageID);
			logger.info("RBT:: dstComponent = "+ dstComponent);
			logger.info("RBT:: refKey = "+ refKey);
			logger.info("RBT:: messageData = "+ messageData);

			O3InfoMessage o3InfoMessage = (O3InfoMessage) o3InterfaceHelper.getOzoneMessenger().createInfoMessage(messageID, dstComponent, COMPONENT_NAME, refKey, messageData);
			int noOfMsgSent = o3InterfaceHelper.getOzoneMessenger().broadcastOzoneMessage(o3InfoMessage, dstComponent, refKey, null);

			logger.info("RBT:: noOfMsgSent = "+ noOfMsgSent);
			if(noOfMsgSent > 0)
				response = true;
		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		logger.info("RBT:: response = "+ response);
		return response;
	}
}
