package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.RBTProfileTone;
import com.livewiremobile.store.storefront.dto.rbt.Song;
import com.onmobile.apps.ringbacktones.v2.bean.AssetBean;

public class RBTProfileToneAsset implements IRBTAsset{
	
	private RBTProfileTone rbtProfileTone;
	
	public  RBTProfileToneAsset() {
		rbtProfileTone = new RBTProfileTone();
	}
	
	@Override
	public Asset buildAsset(AssetBean bean) {
		rbtProfileTone.setTitle(bean.getToneName());
		rbtProfileTone.setLang("en");
		rbtProfileTone.setAutoRenew(true);
		rbtProfileTone.setRenewable(false);
		rbtProfileTone.setId(bean.getId());
		rbtProfileTone.setStatus(Song.Status.AVAILABLE);
		rbtProfileTone.setReferenceId(bean.getRefrenceId());
		return rbtProfileTone;
	}


	
}
