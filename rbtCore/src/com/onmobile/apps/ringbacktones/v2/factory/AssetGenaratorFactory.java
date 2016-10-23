package com.onmobile.apps.ringbacktones.v2.factory;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.livewiremobile.store.storefront.dto.rbt.RBTPlaylist;
import com.livewiremobile.store.storefront.dto.rbt.RBTStation;
import com.livewiremobile.store.storefront.dto.rbt.Shuffle;
import com.livewiremobile.store.storefront.dto.rbt.Song;

public class AssetGenaratorFactory {
		
	static Logger logger = Logger.getLogger(AssetGenaratorFactory.class);

	public static <T extends Asset> T getAssetObject(Asset asset,String type){
		
		if(AssetType.valueOf(type) !=null && AssetType.valueOf(type).equals(type)){
			AssetType assetype = AssetType.valueOf(type);
			
			switch (assetype){
			  case SONG : asset = (Song)asset;
			  break;
			  
			  case RBTSTATION: asset = (RBTStation)asset;
			  break;
			  
			  case SHUFFLELIST: asset = (Shuffle)asset;
			  break;
			  
			  case RBTPLAYLIST: asset = (RBTPlaylist)asset;
			  break;
			}
			
		}
		
		logger.info("returning class:"+asset.getClass()+"for type: "+type);
		return  (T) asset;
	}
	
}
