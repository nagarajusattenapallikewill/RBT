package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.RBTPlaylist;
import com.onmobile.apps.ringbacktones.v2.bean.AssetBean;

public class RbtPlaylistAsset implements IRBTAsset{

	public Asset buildAsset(AssetBean assetBean) {
		RBTPlaylist asset = new RBTPlaylist();
		asset.setId(assetBean.getCategoryId());
		asset.setReferenceId(assetBean.getRefrenceId());
		return asset;
	}
}
