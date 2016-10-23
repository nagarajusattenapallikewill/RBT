package com.onmobile.apps.ringbacktones.daemons;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTEventLogger;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.RBTLotteryEntries;
import com.onmobile.apps.ringbacktones.content.RBTLotteryNumber;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;

/**
 * @author sridhar.sindiri
 *
 */
public class RBTLotteryEntriesDaemon extends Thread
{
	private static final Logger logger = Logger.getLogger(RBTLotteryEntriesDaemon.class);
	private RBTDaemonManager mainDaemonThread = null;

	/**
	 * @param mainDaemonThread
	 */
	public RBTLotteryEntriesDaemon(RBTDaemonManager mainDaemonThread)
	{
		try
		{
			setName("RBTLotteryEntriesDaemon");
			this.mainDaemonThread = mainDaemonThread;
			init();
		}
		catch (Exception e)
		{
			logger.error("Issue in creating RBTLotteryEntriesDaemon", e);
		}
	}

	/**
	 * 
	 */
	public void init()
	{
		
	}

	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run()
	{
		while(mainDaemonThread != null && mainDaemonThread.isAlive()) 
		{
			processLotteryEntries();
			try
			{
				logger.info("RBTLotteryEntriesDaemon Thread Sleeping for 5 minutes............");
				Thread.sleep(RBTParametersUtils.getParamAsInt("DAEMON", "SLEEP_INTERVAL_MINUTES", 5) * 60 * 1000);
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
	}

	/**
	 * 
	 */
	private void processLotteryEntries()
	{
		RBTLotteryEntries[] rbtLotteryEntries = RBTDBManager.getInstance().getUnProcessedLotteryEntries();
		if (rbtLotteryEntries == null || rbtLotteryEntries.length == 0)
		{
			logger.info("No lottery entries to process");
			return;
		}

		for (RBTLotteryEntries rbtLotteryEntry : rbtLotteryEntries)
		{
			String subscriberID = rbtLotteryEntry.subscriberID();
			int clipID = rbtLotteryEntry.clipID();
			try
			{
				String lotteryIdsStr = RBTParametersUtils.getParamAsString("DAEMON", "LOTTERY_IDS_FOR_EACH_DOWNLOAD", null);
				String[] lotteryIds = lotteryIdsStr.split(",");
				int count = 0;
				for (String eachLotteryId : lotteryIds)
				{
					RBTLotteryNumber rbtLotteryNumber = null;
					if (eachLotteryId.startsWith("!"))
					{
						eachLotteryId = eachLotteryId.substring(1);
						rbtLotteryNumber = RBTDBManager.getInstance().getOldestLotteryNumberNotUnderLotteryID(Integer.parseInt(eachLotteryId));
					}
					else
					{
						rbtLotteryNumber = RBTDBManager.getInstance().getOldestLotteryNumberUnderLotteryID(Integer.parseInt(eachLotteryId));
					}

					if (rbtLotteryNumber == null)
					{
						logger.info("No entry is there in RBT_LOTTERY_NUMBER, so not updating RBT_LOTTERY_ENTRIES");
						continue;
					}

					boolean updateSuccess = false;
					try
					{
						count++;
						// update RBT_LOTTERY_ENTRIES with the lottery Id and lottery number for the entry
						if (count == 1)
						{
							updateSuccess = RBTDBManager.getInstance()
								.updateRBTLotteryEntryDetails(
										rbtLotteryNumber.lotteryID(),
										rbtLotteryNumber.lotteryNumber(),
										rbtLotteryEntry.sequenceID());
						}
						else
						{
							updateSuccess = RBTDBManager.getInstance()
									.insertRbtLotteryEntry(
											rbtLotteryNumber.lotteryID(),
											rbtLotteryEntry.subscriberID(),
											rbtLotteryEntry.entryTime(),
											rbtLotteryNumber.lotteryNumber(),
											rbtLotteryEntry.clipID());
						}

						if (!updateSuccess)
						{
							logger.warn("Lottery entry not updated successfully");
							continue;
						}

						String smsText = RBTParametersUtils.getParamAsString("DAEMON", "LOTTERY_ENTRY_SMS_TEXT_" + rbtLotteryNumber.lotteryID(), null);
						if (smsText == null)
							smsText = RBTParametersUtils.getParamAsString("DAEMON", "LOTTERY_ENTRY_SMS_TEXT", null);

						Clip clip = RBTCacheManager.getInstance().getClip(clipID);
						String clipName = clip.getClipName();
						smsText = smsText.replaceAll("%SONG_NAME", clipName);
						smsText = smsText.replaceAll("%LOTTERY_NUMBER", rbtLotteryNumber.lotteryNumber());
						smsText = smsText.replaceAll("%LOTTERY_ID", String.valueOf(rbtLotteryNumber.lotteryID()));

						Tools.sendSMS(RBTParametersUtils
								.getParamAsString("DAEMON",
										"LOTTERY_SMS_SENDER_NO", "12345"),
										subscriberID, smsText, false);

						StringBuilder logBuilder = new StringBuilder();
						logBuilder.append(subscriberID).append(", ")
								.append(rbtLotteryNumber.lotteryNumber()).append(", ").append(rbtLotteryNumber.lotteryID())
								.append(", ").append(clipID).append(", ")
								.append(smsText);

						RBTEventLogger.logEvent(RBTEventLogger.Event.LOTTERY_TRANS,
								logBuilder.toString());
					}
					finally
					{
						if (updateSuccess)
							RBTDBManager.getInstance().deleteRBTLotteryNumberBySequenceID(rbtLotteryNumber.sequenceID());
						else
						{
							RBTDBManager.getInstance().updateRBTLotteryNumberAccessCount(rbtLotteryNumber.sequenceID(), 0);

							RBTDBManager.getInstance()
									.updateRBTLotteryEntryDetails(-1, null, rbtLotteryEntry.sequenceID());
						}
					}
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
			}
		}
	}
}
