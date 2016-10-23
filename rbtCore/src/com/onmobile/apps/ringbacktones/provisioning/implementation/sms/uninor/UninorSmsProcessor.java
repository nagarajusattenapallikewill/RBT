/**
 * 
 */
package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.uninor;

import java.util.Arrays;
import java.util.List;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.provisioning.common.SmsKeywordsStore;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

/**
 * @author vinayasimha.patil
 *
 */
public class UninorSmsProcessor extends SmsProcessor
{

	/**
	 * @throws RBTException
	 */
	public UninorSmsProcessor() throws RBTException
	{
		super();
	}
	
	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor#processDeactivation(com.onmobile.apps.ringbacktones.provisioning.common.Task)
	 */
	@Override
	public String processDeactivation(Task task)
	{
		if (SmsKeywordsStore.comboPackKeywordsSet == null
				|| SmsKeywordsStore.comboPackKeywordsSet.size() == 0)
			return super.processDeactivation(task);

		boolean isComboPackDeactRequest = false;

		@SuppressWarnings("unchecked")
		List<String> smsTextTokens = (List<String>) task.getObject(param_smsText);
		if (smsTextTokens != null)
		{
			for (String smsToken : smsTextTokens)
			{
				if (SmsKeywordsStore.comboPackKeywordsSet.contains(smsToken))
				{
					isComboPackDeactRequest = true;
					break;
				}
			}
		}

		List<String> comboSubscriptionClasses = Arrays
				.asList(RBTParametersUtils.getParamAsString(SMS, "COMBO_PACK_SUBSCRIPTION_CLASSES", "")
						.trim().toUpperCase().split(","));

		Subscriber subscriber = (Subscriber)task.getObject(param_subscriber);
		String subscriptionClass = subscriber.getSubscriptionClass();
		
		if (subscriptionClass != null)
			subscriptionClass = subscriptionClass.toUpperCase();
		
		boolean comboPackUser = comboSubscriptionClasses.contains(subscriptionClass);
		if (isComboPackDeactRequest && !comboPackUser)
		{
			task.setObject(param_responseSms,
					getSMSTextForID(task, DEACTIVATION_FAILURE_NON_COMBO_USER,
							m_deactivationFailureNonComboUserDefault,
							subscriber.getLanguage()));
			return null;
		}
		else if (!isComboPackDeactRequest && comboPackUser)
		{
			task.setObject(param_responseSms,
					getSMSTextForID(task, DEACTIVATION_FAILURE_COMBO_USER,
							m_deactivationFailureComboUserDefault,
							subscriber.getLanguage()));
			return null;
		}

		return super.processDeactivation(task);
	}
}
