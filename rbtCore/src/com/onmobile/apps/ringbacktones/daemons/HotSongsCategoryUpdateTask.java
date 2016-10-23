/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;

/**
 * @author vinayasimha.patil
 *
 */
public class HotSongsCategoryUpdateTask extends TimerTask
{
	private static Logger logger = Logger.getLogger(HotSongsCategoryUpdateTask.class);

	private int hotSongsCategoryID;
	private int noOfhotSongsInCategory = 0;
	private int noOfhotSongsDays = 0;

	public HotSongsCategoryUpdateTask(int hotSongsCategoryID, int noOfhotSongsInCategory, int noOfhotSongsDays)
	{
		this.hotSongsCategoryID = hotSongsCategoryID;
		this.noOfhotSongsInCategory = noOfhotSongsInCategory;
		this.noOfhotSongsDays = noOfhotSongsDays;
	}

	/* (non-Javadoc)
	 * @see java.util.TimerTask#run()
	 */
	public void run()
	{
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		Category hotSongsCategory = rbtDBManager.getCategory(hotSongsCategoryID); 
		if(hotSongsCategory != null)
		{
			logger.info("RBT:: Hot song category id = "+ hotSongsCategory.getID());
			Integer[] accesses = null;
			try
			{
				accesses = rbtDBManager.getMostAccesses(noOfhotSongsInCategory,noOfhotSongsDays);
			}
			catch(Throwable th)
			{
				logger.error("", th);
			}
			if(accesses != null && accesses.length > 0)
			{
				logger.info("RBT:: Found "+ accesses.length + " hot songs");

				ArrayList accessList = new ArrayList();

				int[] clipIDs = new int[0];
				if(accesses.length < noOfhotSongsInCategory)
				{
					clipIDs = rbtDBManager.getClipIDsInCategory(hotSongsCategory.getID());
				}
				rbtDBManager.removeCategoryClips(hotSongsCategory.getID());

				int clipIndex = 1;
				for(int i = 0; i < accesses.length; i++)
				{
					accessList.add(accesses[i].intValue()+"");
					Clips clip = rbtDBManager.getClip(accesses[i].intValue());
					rbtDBManager.createCategoryClip(hotSongsCategory, clip, "y", clipIndex, null);
					clipIndex++;
				}
				for(int i = 0; clipIndex <= noOfhotSongsInCategory && i < clipIDs.length; i++)
				{
					if(!accessList.contains(clipIDs[i]+""))
					{
						Clips clip = rbtDBManager.getClip(clipIDs[i]);
						rbtDBManager.createCategoryClip(hotSongsCategory, clip, "y", clipIndex, null);
						clipIndex++;
					}
				}
			}
			else
			{
				logger.info("RBT:: Not found any entry in Access Table");
			}
		}
		else
		{
			logger.info("RBT:: Hot song category '"+ hotSongsCategoryID +"'does not exist");
		}
	}
}

