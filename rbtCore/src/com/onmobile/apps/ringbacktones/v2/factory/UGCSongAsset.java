package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.livewiremobile.store.storefront.dto.rbt.RBTUGC;
import com.livewiremobile.store.storefront.dto.rbt.Song;
import com.onmobile.apps.ringbacktones.v2.bean.AssetBean;

public class UGCSongAsset implements IRBTAsset {
	
	private RBTUGC rbtugc = null;
	
	public UGCSongAsset() {
		rbtugc = new RBTUGC();
		rbtugc.setType(AssetType.RBTUGC);
	}
	
	public UGCSongAsset setId(String id) {
		rbtugc.setId(Long.parseLong(id));
		return this;
	}
	
	public UGCSongAsset setRefId(String refId) {
		rbtugc.setReferenceId(refId);
		return this;
	}
	
	/*public UGCSongAsset setSubType(int status) {
		if(status == 99) {
			rbtugc.setSubtype(Song.SubType.PROFILE_RBT);
		}
		else {
			rbtugc.setSubtype(Song.SubType.REGULAR_RBT);
		}
		return this;
	}*/

	@Override
	public Asset buildAsset(AssetBean bean) {
		
		rbtugc.setId(bean.getId());
		rbtugc.setReferenceId(bean.getRefrenceId());
		rbtugc.setLang("en");
		rbtugc.setAutoRenew(true);
		rbtugc.setRenewable(false);
		rbtugc.setStatus(Song.Status.AVAILABLE);
		/*if(bean.getStatus() == 99) {
			rbtugc.setSubtype(Song.SubType.PROFILE_RBT);
		}
		else {
			rbtugc.setSubtype(Song.SubType.REGULAR_RBT);
		}*/
		return rbtugc;
	}
	
	public Asset buildAsset() {
		rbtugc.setLang("en");
		rbtugc.setAutoRenew(true);
		rbtugc.setRenewable(false);
		rbtugc.setStatus(Song.Status.AVAILABLE);
		return rbtugc;
	}

}
