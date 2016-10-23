/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.Category;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

/**
 * @author vinayasimha.patil
 *
 */
public class CategoryRefreshTask implements Runnable
{
	private static Logger logger = Logger.getLogger(CategoryRefreshTask.class);

	private int[] categories = null;
	private int accessDays = 7;
	private static int m_nConn=4;

	private boolean runThread;
	private Thread categoryRefreshThread = null;

	public CategoryRefreshTask(int[] categories, int accessDays)
	{
		this.categories = categories;
		this.accessDays = accessDays;

		runThread = true;
		categoryRefreshThread = new Thread(this);
		categoryRefreshThread.start();
	}
	
	public final void stop()
	{
		runThread = false;
		categoryRefreshThread.interrupt();
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	public void run()
	{
		RBTDBManager rbtDBManager = RBTDBManager.getInstance();
		while (runThread) {
			Parameters catRfshParameter = CacheManagerUtil.getParametersCacheManager().getParameter(
					"TATA_RBT_DAEMON", "CATEGORY_REFRESH_DATE");
			SimpleDateFormat dateFormat = new SimpleDateFormat(
					"yyyy-MM-dd HH:mm:ss");
			Date refreshDate;
			try {
				refreshDate = dateFormat.parse(catRfshParameter.getValue());
				if (refreshDate.getTime() <= System.currentTimeMillis()) {
					for (int i = 0; i < categories.length; i++) {
						Category category = rbtDBManager
								.getCategory(categories[i]);
						if (category != null) {
							int[] clipIDs = rbtDBManager
									.getClipIDsInCategory(category.getID());
							if (clipIDs != null && clipIDs.length > 0) {

								Integer[] accesses = rbtDBManager
										.getClipsInMostAccessOrder(clipIDs,
												accessDays);
								if (accesses != null && accesses.length > 0) {
									logger.info("RBT:: Found " + accesses.length
													+ " songs");
									rbtDBManager.removeCategoryClips(category
											.getID());

									ArrayList accessList = new ArrayList();
									int clipIndex = 1;
									for (int j = 0; j < accesses.length; j++) {
										accessList.add(accesses[j].intValue()
												+ "");
										Clips clip = rbtDBManager
												.getClip(accesses[j].intValue());
										rbtDBManager.createCategoryClip(
												category, clip, "y", clipIndex,
												null);
										clipIndex++;
									}
									for (int j = 0; j < clipIDs.length; j++) {
										if (!accessList.contains(clipIDs[j]
												+ "")) {
											Clips clip = rbtDBManager
													.getClip(clipIDs[j]);
											rbtDBManager.createCategoryClip(
													category, clip, "y",
													clipIndex, null);
											clipIndex++;
										}
									}
								}
							} else {
								logger.info("RBT:: No mapping for Category '"
												+ categories[i] + "'");
							}
						} else {

							logger.info("RBT:: Category '"
									+ categories[i] + "' does not exist");
						}
					}

					Calendar calendar = Calendar.getInstance();
					calendar.setTime(refreshDate);
					calendar.add(Calendar.DAY_OF_YEAR, accessDays);
					refreshDate = calendar.getTime();
					CacheManagerUtil.getParametersCacheManager().updateParameter("TATA_RBT_DAEMON",
							"CATEGORY_REFRESH_DATE", dateFormat
									.format(refreshDate));
				}

				Thread.sleep(RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "SLEEP_MINUTES", 0) * 1000 * 60);
			} catch (ParseException e) {
				logger.error("", e);
			} catch (NumberFormatException e) {
				logger.error("", e);
			} catch (InterruptedException e) {
				logger.error("", e);
			}
		}
	}
}
