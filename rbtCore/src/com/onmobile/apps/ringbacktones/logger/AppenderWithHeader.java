package com.onmobile.apps.ringbacktones.logger;

import java.io.File;

import org.apache.log4j.DailyRollingFileAppender;

public class AppenderWithHeader extends DailyRollingFileAppender
{

	protected void writeHeader()
	{
		File f = new File(getFile());
		if (f.length() == 0)
		{
			qw.write(getLayout().getHeader());
		}
	}
}
