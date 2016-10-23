/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.romania;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class RomaniaRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(RomaniaRBTProcessor.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getCos(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber)
	{
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}
}
