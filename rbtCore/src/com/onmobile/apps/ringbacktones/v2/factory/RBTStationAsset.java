package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.RBTStation;
import com.onmobile.apps.ringbacktones.v2.bean.AssetBean;

public class RBTStationAsset implements IRBTAsset{

	public Asset buildAsset(AssetBean assetBean) {
		RBTStation asset = new RBTStation();
		asset.setId(assetBean.getCategoryId());
		asset.setReferenceId(assetBean.getRefrenceId());
		return asset;
	}
}
