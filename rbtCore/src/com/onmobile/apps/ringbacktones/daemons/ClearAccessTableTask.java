/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

/**
 * @author vinayasimha.patil
 *
 */
public class ClearAccessTableTask extends TimerTask
{
	private static Logger logger = Logger.getLogger(ClearAccessTableTask.class);
	
	private int backupDays = 0;

	public ClearAccessTableTask(int backupDays)
	{
		this.backupDays = backupDays;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	public void run()
	{

		logger.info("RBT::Removing records before "+ backupDays + "days..!");

		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		boolean success = rbtDBManager.clearAccessTable(backupDays);

		if(success == true)
		{
			logger.info("RBT::Records succesfully deleted older than "+backupDays+" days.");
		}
		else
		{
			logger.info("RBT::No records to delete older than "+backupDays+" days.");
		}
	}

}
