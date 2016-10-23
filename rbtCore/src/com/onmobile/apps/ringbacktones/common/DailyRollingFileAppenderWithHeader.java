/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

import java.io.File;

import org.apache.log4j.DailyRollingFileAppender;

/**
 * @author vinayasimha.patil
 * 
 */
public class DailyRollingFileAppenderWithHeader extends
		DailyRollingFileAppender
{
	/* (non-Javadoc)
	 * @see org.apache.log4j.WriterAppender#writeHeader()
	 */
	@Override
	protected void writeHeader()
	{
		File file = new File(fileName);
		if (file.exists() && file.length() == 0)
			super.writeHeader();
	}
}
