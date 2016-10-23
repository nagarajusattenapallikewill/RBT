/**
 * 
 */
package com.onmobile.apps.ringbacktones.promotions;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;

/**
 * @author vinayasimha.patil
 * 
 */
public class RBTViralOzonized extends Ozonized
{
	private static Logger logger = Logger.getLogger(RBTViralOzonized.class);
	private static final String COMPONENT_NAME = "RBTViral";

	private RBTViral rbtViral = null;

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent()
	{
		Vector<?> vector;
		try
		{
			vector = new Vector<Object>();
			rbtViral = new RBTViral();
			rbtViral.init(vector);
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
