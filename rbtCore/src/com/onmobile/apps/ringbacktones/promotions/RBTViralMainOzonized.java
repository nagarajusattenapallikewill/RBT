/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;

/**
 * @author mohsin
 * 
 */
public class RBTViralMainOzonized extends Ozonized
{
	private static Logger logger = Logger.getLogger(RBTViralMainOzonized.class);
	private static final String COMPONENT_NAME = "RBTViral";

	private RBTViralMain rbtViral = null;

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent()
	{
		try
		{
			rbtViral = RBTViralMain.getInstance(false);
			rbtViral.setName("VIRAL_MAIN_OZONIFIED");
			rbtViral.start();

		}
		catch (Exception e)
		{
			logger.error("", e);
		}

		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{
		rbtViral.stopThread();
	}
}
