/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.egypt;

import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * Promotion Implementation class for Egypt Vodafone.
 * 
 * @author vinayasimha.patil
 */
public class EgyptPromoProcessor extends PromoProcessor
{

	/**
	 * @throws RBTException
	 */
	public EgyptPromoProcessor() throws RBTException
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.promo.
	 * PromoProcessor#getTask(java.util.HashMap)
	 */
	/**
	 * Overridden to support the Egypt Vodafone specific feature: if promoID
	 * sent as '000000' then treat the request as deactivation request.
	 */
	@Override
	public Task getTask(HashMap<String, String> requestParams)
	{
		Task task = super.getTask(requestParams);

		String taskAction = task.getTaskAction();
		String promoID = task.getString(param_PROMO_ID);
	    String confActPromoId =  getParameter(PROMOTION, "PROMO_CODE_FOR_RANDOMIZATION");
	    String confDctPromoId =  getParameter(PROMOTION, "PROMO_CODE_FOR_UNRANDOMIZATION");
		if (taskAction != null
				&& taskAction.equalsIgnoreCase(request_selection)
				&& promoID != null && promoID.equals("000000"))
		{
			task.setTaskAction(request_deactivate);
			String deactivatedBy = task.getString(param_SELECTED_BY);
			task.setObject(param_DEACTIVATED_BY, deactivatedBy);
			
		}else if(taskAction!=null && taskAction.equalsIgnoreCase(request_selection)
				&& promoID!=null && promoID.equals(confActPromoId)) 
		{
			task.setTaskAction(request_SHUFFLE);
			task.setObject("ACTION", "ACT");
			
		}else if(taskAction!=null && taskAction.equalsIgnoreCase(request_selection)
				&& promoID!=null && promoID.equals(confDctPromoId))
		{
			task.setTaskAction(request_SHUFFLE);
			task.setObject("ACTION", "DCT");
			
		}

		return task;
	}

	/*
	 * (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.promo.
	 * PromoProcessor
	 * #processSelection(com.onmobile.apps.ringbacktones.provisioning
	 * .common.Task)
	 */
	/**
	 * Overridden to support the Egypt Vodafone specific feature: if channel is
	 * USSD we need to use FIRST_USSD charge class for new user and DEFAILE_USSD
	 * for active users.
	 */
	@Override
	public void processSelection(Task task)
	{
		String selectedBy = task.getString(param_SELECTED_BY);
		if (selectedBy.equalsIgnoreCase("USSD"))
		{
			Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
			String status = subscriber.getStatus();
			if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
					|| status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
			{
				task.setObject(param_CHARGE_CLASS, "FIRST_USSD");
			}
			else
				task.setObject(param_CHARGE_CLASS, "DEFAULT_USSD");

			task.setObject(param_USE_UI_CHARGE_CLASS, "TRUE");
		}

		super.processSelection(task);
	}
}
