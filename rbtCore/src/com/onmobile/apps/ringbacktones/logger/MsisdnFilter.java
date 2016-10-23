package com.onmobile.apps.ringbacktones.logger;

import org.apache.log4j.Level;
import org.apache.log4j.MDC;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.varia.StringMatchFilter;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;

public class MsisdnFilter extends StringMatchFilter implements iRBTConstant
{
	public int decide(LoggingEvent event)
	{
		if(event.getLevel() == Level.ERROR)
			return Filter.ACCEPT; 
		String msisdn = (String)MDC.get(mdc_msisdn);
		if(msisdn != null && getStringToMatch().indexOf(msisdn) != -1)
			return Filter.ACCEPT;
		return Filter.DENY;
	}
}
