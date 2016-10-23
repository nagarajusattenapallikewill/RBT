package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.Song;
import com.onmobile.apps.ringbacktones.v2.bean.AssetBean;

public class SongAsset implements IRBTAsset{

	private Song song;
	
	public SongAsset(){
		song = new Song();
	}	
	
	public SongAsset setId(long id) {
		this.song.setId(id);
		return this;
	}

	public SongAsset setReferenceId(String referenceId) {
		this.song.setReferenceId(referenceId);
		return this;
	}

	public SongAsset setTitle(String title) {
		this.song.setTitle(title);
		return this;
	}
	
	public Song  buildSong(){
		return song;
	}

	public Asset buildAsset(AssetBean bean) {
		Song song = new Song();
		song.setTitle(bean.getToneName());
		song.setLang("en");
		song.setAutoRenew(true);
		song.setRenewable(false);
		song.setId(bean.getId());
		song.setStatus(Song.Status.AVAILABLE);
		song.setReferenceId(bean.getRefrenceId());
		song.setCutStartDuration(bean.getCutStartDuration());
		if(bean.getValidDate()!=null) {
			song.setValiddate(bean.getValidDate().toString());
		}
		return song;
	}
}
