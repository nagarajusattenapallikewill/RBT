package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.onmobile.apps.ringbacktones.v2.bean.AssetBean;

public interface IRBTAsset {
	
	public Asset buildAsset(AssetBean bean);
}
