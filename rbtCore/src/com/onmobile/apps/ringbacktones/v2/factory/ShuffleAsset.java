package com.onmobile.apps.ringbacktones.v2.factory;

import java.util.ArrayList;
import java.util.List;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.Pager;
import com.livewiremobile.store.storefront.dto.rbt.RBTUGC;
import com.livewiremobile.store.storefront.dto.rbt.Shuffle;
import com.livewiremobile.store.storefront.dto.rbt.Song;
import com.onmobile.apps.ringbacktones.v2.bean.AssetBean;

public class ShuffleAsset implements IRBTAsset{

	private Shuffle  shuffle;
	
	public ShuffleAsset(){
		shuffle = new Shuffle();
	}
	
	public Asset buildAsset(AssetBean assetBean) {
		shuffle = new Shuffle();
		shuffle.setId(assetBean.getCategoryId());
		shuffle.setReferenceId(assetBean.getRefrenceId());
		shuffle.setName(assetBean.getUdpName());
		return shuffle;
	}
	
	public ShuffleAsset setSongs(List<Song> songs) {
		this.shuffle.setSongs(songs);
		return this;
	}

	public ShuffleAsset setCount(int count) {
		this.shuffle.setCount(count);
		return this;
	}

	public ShuffleAsset setName(String name) {
		this.shuffle.setName(name);
		return this;
	}

	public ShuffleAsset setExtraInfo(String extraInfo) {
		this.shuffle.setExtraInfo(extraInfo);
		return this;
	}

	public ShuffleAsset setPager(long offSet, long pageSize, long totalResult) {
		this.shuffle.setPager(new Pager(offSet, pageSize));
		return this;
	}

	public ShuffleAsset setId(long id) {
		this.shuffle.setId(id);
		return this;
	}

	public ShuffleAsset setReferenceId(String referenceId) {
		this.shuffle.setReferenceId(referenceId);
		return this;
	}
	
	public ShuffleAsset setSong(Song song){
		List<Song> songList = this.shuffle.getSongs();
		
		if(songList == null) {
			songList = new ArrayList<Song>();
		}
		songList.add(song);
		this.setSongs(songList);
		return this;
	}
	
	
	public ShuffleAsset setUGCSong(RBTUGC rbtugc){
		return this;
	}
	
	public Shuffle buildShuffle(){
		return shuffle;
	}
	
	
	
}
