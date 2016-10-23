package com.onmobile.apps.ringbacktones.webservice.implementation.vodafoneqatar;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;

/**
 * @author sahidul.karim
 *
 */
public class DownloadSetTimeComparator implements Comparator<SubscriberDownloads>
{
	private static DateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
	private static Logger logger = Logger.getLogger(DownloadSetTimeComparator.class);
	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(SubscriberDownloads download1,
			SubscriberDownloads download2)
	{
			logger.info("download1 "+download1.setTime());
			logger.info("download2 "+download2.setTime());
			if (download1.setTime().before(download2.setTime())) {
				logger.info(" if block");
				return -1;
			} 
			else if (download1.setTime().after(download2.setTime())) {
				logger.info("else block ");
				return 1;
			}
		return 0;
	}

}
