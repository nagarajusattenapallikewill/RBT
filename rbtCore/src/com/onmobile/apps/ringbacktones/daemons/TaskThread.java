package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
/**
 *	Task thread performs a task for e.g. ACT_Task 
 */
public class TaskThread extends Thread implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(TaskThread.class);

	private Subscriber m_subscriber;
	private SubscriberStatus m_addSetting;
	//private ToBeDeletedSelections m_tobeDeleteSeletion;
	private SubscriberDownloads m_subscriberDownloads;
	private SubscriberStatus m_tobeDeleteSetting;

	private TATARBTDaemonController m_mainThread = null;

	private boolean isStarted = false;

	private static TATARBTDaemonMain tataRBTDaemonMain;

	public TaskThread (TATARBTDaemonController ozoneMainThread)
	{
		m_mainThread = ozoneMainThread;

		tataRBTDaemonMain = TATARBTDaemonMain.getInstance();
		logger.info("RBT::leaving");
	}

	public void run()
	{
		logger.info("RBT::Inside task thread run");
		try
		{
			isStarted = true;
			while(m_mainThread.isAlive())
			{
				try {
					DaemonTask task = m_mainThread.getTask();// Gets the task to be performed

					if (task == null) // We have already waited to be notified, pick up the next available task.
					{
						logger.info("RBT::task is null");
						continue; 
					}

					int m_task = task.getTaskType();
					Object m_obj = task.getObject();

					if(m_task == ACTIVATION_TASK || m_task == ACTIVATION_GRACE_TASK)
					{
						m_subscriber = (Subscriber)m_obj;
						String result = null;
						if (m_subscriber.activationInfo().indexOf("GROUPADD") != -1
								&& TATARBTDaemonMain.parameterCacheManager.getParameter(
										iRBTConstant.TATADAEMON, "USE_GROUP_ADD_URL", "false")
										.getValue().equalsIgnoreCase("true")) {
							result = tataRBTDaemonMain.bulkActivateSubscriber(m_subscriber);
						}
						else{
							result = tataRBTDaemonMain.activateSubscriber(m_subscriber);
						}
						m_mainThread.removeHashmap(m_subscriber.subID());

						logger.info("RBT::activation result for subscriber " + m_subscriber.subID() + " is - " + result);
					}
					else if(m_task == ACTIVATION_POLLING_TASK_POSTPAID || m_task == ACTIVATION_POLLING_TASK_PREPAID)
					{
						m_subscriber = (Subscriber)m_obj;

						String result = tataRBTDaemonMain.checkActivationPendingSubscriber(m_subscriber);
						m_mainThread.removeHashmap(m_subscriber.subID());

						logger.info("RBT::activationPolling result for subscriber " + m_subscriber.subID() + " is - " + result);
					}
					else if(m_task == DEACTIVATION_TASK)
					{
						m_subscriber = (Subscriber)m_obj;

						String result = tataRBTDaemonMain.deactivateSubscriber(m_subscriber);
						m_mainThread.removeHashmap(m_subscriber.subID());

						logger.info("RBT::deactivation result for subscriber " + m_subscriber.subID() + " is - " + result);
					}
					else if(m_task == DEACTIVATION_POLLING_TASK_POSTPAID || m_task == DEACTIVATION_POLLING_TASK_PREPAID)
					{
						m_subscriber = (Subscriber)m_obj;

						String result = tataRBTDaemonMain.checkDeactivationPendingSubscriber(m_subscriber);
						m_mainThread.removeHashmap(m_subscriber.subID());

						logger.info("RBT::deactivationPolling result for subscriber " + m_subscriber.subID() + " is - " + result);
					}
					else if(m_task == UPDATE_TO_DEACTIVATE)
					{
						m_subscriber = (Subscriber)m_obj;
						String result = tataRBTDaemonMain.updateToDeactivate(m_subscriber);
						m_mainThread.removeHashmap(m_subscriber.subID());
						
						logger.info("RBT::updateToDeactivate task result for " + m_subscriber.subID() + " is - " + result);
					}
					else if(m_task == DELETE_SETTING_TASK)
					{
						m_tobeDeleteSetting = (SubscriberStatus)m_obj;
						

						String result = tataRBTDaemonMain.deleteSettingForSubscriber(m_tobeDeleteSetting);
						m_mainThread.removeHashmap(m_tobeDeleteSetting.subID());

						logger.info("RBT::delete setting result for subscriber " + m_tobeDeleteSetting.subID() + " and clip " + m_tobeDeleteSetting.subscriberFile() + " is - " + result);
					}
					else if(m_task == DELETE_SELECTION_TASK)
					{
						//m_tobeDeleteSeletion = (ToBeDeletedSelections)m_obj;
						m_subscriberDownloads = (SubscriberDownloads) m_obj;

						String result = tataRBTDaemonMain.deleteSelectionForSubscriber(m_subscriberDownloads);
						m_mainThread.removeHashmap(m_subscriberDownloads.subscriberId());

						logger.info("RBT::delete selection result for subscriber " + m_subscriberDownloads.subscriberId() + " and clip " + m_subscriberDownloads.promoId() + " is - " + result);
					}
					else if(m_task == ADD_SETTING_TASK_POSTPAID || m_task == ADD_SETTING_TASK_PREPAID || m_task == SELECTION_GRACE_TASK)
					{
						m_addSetting = (SubscriberStatus)m_obj;

						String result = tataRBTDaemonMain.addClipForSubscriber(m_addSetting);
						m_mainThread.removeHashmap(m_addSetting.subID());

						logger.info("RBT::add setting result for subscriber " + m_addSetting.subID() + " and clip " + m_addSetting.subscriberFile() + " is - " + result);
					}
					else if(m_task == UPDATE_TO_TO_BE_DELETED) {
						m_subscriberDownloads = (SubscriberDownloads)m_obj;
						String result = tataRBTDaemonMain.updateSongToToBeDeleted(m_subscriberDownloads);
						m_mainThread.removeHashmap(m_subscriberDownloads.subscriberId());
						logger.info("RBT:: update song to-to be deleted result for subscriber " + m_subscriberDownloads.subscriberId() + " and promo id " + m_subscriberDownloads.promoId() + " is " + result);
					}
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}
		catch(Exception e)
		{
			logger.error("", e);
		}
		logger.info("RBT::task thread exiting");
	}

	public boolean didStart()
	{
		return isStarted;
	}
}
