/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.implementation.tatagsm;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

/**
 * @author vinayasimha.patil
 *
 */
public class TataGSMRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(TataGSMRBTProcessor.class);

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

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getUserInfoMap(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected HashMap<String, String> getUserInfoMap(WebServiceContext task)
	{
		HashMap<String, String> userInfoMap = super.getUserInfoMap(task);

		if (task.containsKey(param_operatorUserInfo))
			userInfoMap.put(iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT, task.getString(param_operatorUserInfo));

		logger.info("RBT:: response: " + userInfoMap);
		return userInfoMap;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor#getSelectionInfoMap(com.onmobile.apps.ringbacktones.webservice.common.Task)
	 */
	@Override
	protected HashMap<String, String> getSelectionInfoMap(WebServiceContext task)
	{
		HashMap<String, String> selectionInfoMap = super.getSelectionInfoMap(task);

		if (task.containsKey(param_operatorUserInfo))
			selectionInfoMap.put(iRBTConstant.EXTRA_INFO_WDS_QUERY_RESULT, task.getString(param_operatorUserInfo));

		logger.info("RBT:: response: " + selectionInfoMap);
		return selectionInfoMap;
	}
}
