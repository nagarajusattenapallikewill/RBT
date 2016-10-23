package com.onmobile.apps.ringbacktones.v2.resolver.request.handler;

import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import com.livewiremobile.store.storefront.dto.payment.PurchaseCombo;
import com.livewiremobile.store.storefront.dto.rbt.ThirdPartyConsent;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.rbt2.builder.AbstractAssetUtilBuilder;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.logger.BasicCDRLogger;
import com.onmobile.apps.ringbacktones.rbt2.logger.dto.LoggerDTO;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ConsentPropertyConfigurator;
import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.v2.util.AbstractOperatorUtility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

/**
 * 
 * @author md.alam
 *
 */
 
@Service(value = BeanConstant.PURCHASE_SET_COMBO_REQUEST_HANDLER)
public class PurchaseSetComboReqHandler extends AbstractComboReqHandler {
	
	@Autowired
	private ApplicationContext applicationContext;
	private static Logger logger = Logger.getLogger(PurchaseSetComboReqHandler.class);

	@Override
	public PurchaseCombo applyRequest(Subscriber subscriber, PurchaseCombo purchaseCombo,
			SelectionRequest selectionRequest) throws UserException {

		logger.info("Applying Combo Request for Purchase and Selection");
		if (purchaseCombo.getPurchase() != null && purchaseCombo.getPlayrule() != null) {

			String operatorName = ServiceUtil.getOperatorName(subscriber);

			AbstractOperatorUtility operatorUtility = (AbstractOperatorUtility) getOperatorUtilityObject(operatorName);
			Rbt rbt = makeSelection(operatorUtility, selectionRequest);
			responseValidation(selectionRequest.getResponse());

			String cgUrl = makeConsentCGUrl(operatorUtility, rbt, selectionRequest, operatorName, subscriber);
			String rUrl = makeRUrl(operatorUtility);

			ThirdPartyConsent thirdPartyConsent = buildThirdPartyConsent(operatorUtility, cgUrl, rUrl);
			if (thirdPartyConsent != null) {
				purchaseCombo.setThirdpartyconsent(thirdPartyConsent);
			} else {
				// Added for VOL-1377 Incorrect ref_id is returned by rbt while setting a playrule
				AbstractAssetUtilBuilder builder = (AbstractAssetUtilBuilder) getAssetBuilder(purchaseCombo.getAsset().getType(),purchaseCombo.getAsset().getSubtype());
				Map<String, String> whereClasueMap = builder.getWhereClauseForGettingLatestActiveSelection(purchaseCombo, selectionRequest.getCallerID() , selectionRequest.getClipID());
				Setting latestSelection = null;
				if(rbt.getLibrary() != null) {
					latestSelection = ServiceUtil.getSubscriberLatestSelectionFromSetting(rbt.getLibrary().getSettings(), whereClasueMap);
				}
				if(latestSelection!=null){
				    purchaseCombo.getPlayrule().setId(latestSelection.getRefID());
				 
				}else{
					purchaseCombo.getPlayrule().setStatus("Consent_Pending");
				}				
			}
		} else {
			logger.info("Invalid Request");
			ServiceUtil.throwCustomUserException(errorCodeMapping, null, null);
		}
		return purchaseCombo;

	}

	
}
