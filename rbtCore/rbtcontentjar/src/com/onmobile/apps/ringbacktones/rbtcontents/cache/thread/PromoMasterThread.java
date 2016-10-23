package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.PromoMaster;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;

public class PromoMasterThread extends GenericCacheThread {

	private static final Logger log = Logger.getLogger(PromoMasterThread.class);

	public PromoMasterThread(String name, List records) {
		super(name, records);
	}
	
	public void processRecord(Object obj) {
		PromoMaster promoMaster = (PromoMaster) obj;
		mc.set(RBTCacheKey.getPromoMasterCacheKey(promoMaster.getPromoCode(), promoMaster.getPromoType()), "" + promoMaster.getClipId());
		if(log.isDebugEnabled()) {
			log.debug("Initializing the promo master cache: " + RBTCacheKey.getPromoMasterCacheKey(promoMaster.getPromoCode(), promoMaster.getPromoType()));
		}
		mc.set(RBTCacheKey.getPromoCodeCacheKey(promoMaster.getPromoCode()), "" + promoMaster.getClipId());
		if(log.isDebugEnabled()) {
			log.debug("Initializing the promo master cache: " + RBTCacheKey.getPromoCodeCacheKey(promoMaster.getPromoCode()));
		}
	}

	@Override
	public void finalProcess() throws Exception {
		//nothing to do
	}

}
