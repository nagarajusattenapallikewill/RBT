/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.telefonica;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;
import com.onmobile.apps.ringbacktones.webservice.implementation.RBTProtocolGenerator;

/**
 * @author vinayasimha.patil
 *
 */
public class TelefonicaRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(TelefonicaRBTProcessor.class);

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
	
	protected String appendProtocolNumber(String subscriberId,
			String selectionInfo) {
		Boolean vivoProtocolGen =  RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "VIVO_PROTOCOL_GENERATOR", "false");
		
		if(vivoProtocolGen){
			try {
				selectionInfo = (null == selectionInfo) ? "" : selectionInfo;
				String id = RBTProtocolGenerator.getInstance()
						.generateUniqueProtocolNum();
				selectionInfo = selectionInfo + "|protocolnumber:" + id + "|";
			} catch (Exception e) {
				logger.error("Unable to create protocol number for subscriberId: "
						+ subscriberId + ", Exception: " + e.getMessage(), e);
			}
			logger.info("Successfully created protocol number. Returning: "
					+ selectionInfo + " for subscriberId: " + subscriberId);
		}else{
			selectionInfo =  super.appendProtocolNumber(subscriberId, selectionInfo);
		}
		
		return selectionInfo;
	}
}
