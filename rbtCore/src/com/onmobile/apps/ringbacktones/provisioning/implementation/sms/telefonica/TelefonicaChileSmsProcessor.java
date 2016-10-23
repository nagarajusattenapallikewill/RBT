package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.telefonica;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;

public class TelefonicaChileSmsProcessor extends TelefonicaSmsProcessor {
	private static final Logger logger = Logger.getLogger(TelefonicaChileSmsProcessor.class);

	public TelefonicaChileSmsProcessor() throws RBTException {
		super();
	}

	@Override
	public void processDoubleConfirmation(Task task) {
		logger.debug("Inside processDoubleConfirmation: ");
		DataRequest dataRequest = new DataRequest(null, null);
		dataRequest.setSubscriberID(task.getString(param_subID));
		String type = null;
		type = RBTParametersUtils.getParamAsString(COMMON,
				"SMS_DOUBLE_CONFIRMATION_TYPES", SMSCONFPENDING + ","
						+ VIRAL_OPTIN);
		dataRequest.setType(type);
		ViralData[] viralDatas = RBTClient.getInstance().getViralData(dataRequest);
		ViralData viralData = null;
		if(viralDatas != null && viralDatas.length > 0 ){
			viralData = viralDatas[0];
			task.setObject(param_viral_data, viralDatas);
			String smsType = viralData.getType();
			logger.info("viralData:"+viralData+", smsType:"+smsType);
			if (smsType.equalsIgnoreCase(SMSCONFPENDING)) {
				super.confirmActNSel(task, viralData);
			}else if(smsType.equalsIgnoreCase(COPYCONFPENDING)) {
				super.processConfirmCopyRequest(task);
			}else if(smsType.equalsIgnoreCase(VIRAL_OPTIN)) {
				super.processViralAccept(task);
			}
		}else{
			logger.info(" no viralData found:");
			task.setObject(param_responseSms, getSMSTextForID(task,
					SMS_FAILURE, m_smsFailureTextDefault, "eng"));
			return;
		}
		
	}
}
