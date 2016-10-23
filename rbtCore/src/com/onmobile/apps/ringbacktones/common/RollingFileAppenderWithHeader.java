/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

import java.io.File;

import org.apache.log4j.RollingFileAppender;

/**
 * @author vinayasimha.patil
 * 
 */
public class RollingFileAppenderWithHeader extends
		RollingFileAppender
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
