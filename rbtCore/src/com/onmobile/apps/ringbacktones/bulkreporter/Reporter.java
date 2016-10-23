/**
 * 
 */
package com.onmobile.apps.ringbacktones.bulkreporter;

import com.onmobile.apps.ringbacktones.daemons.Ozonized;

/**
 * @author vinayasimha.patil
 *
 */
public class Reporter extends Ozonized
{
	private static final String COMPONENT_NAME = "TATA_RBT_BULK_REPORTER";
	private BulkActivationReporter bulkActivationReporter = null;

	@Override
	public String getComponentName()
	{
		return COMPONENT_NAME;
	}

	@Override
	public int startComponent()
	{
		bulkActivationReporter = new BulkActivationReporter();
		return JAVA_COMPONENT_SUCCESS;
	}

	@Override
	public void stopComponent()
	{
		bulkActivationReporter.stop();
	}

	@SuppressWarnings("unused")
	public static void main(String[] args)
	{
		new BulkActivationReporter();
	}
}
