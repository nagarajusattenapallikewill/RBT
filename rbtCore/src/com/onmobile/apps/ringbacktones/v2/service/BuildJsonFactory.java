package com.onmobile.apps.ringbacktones.v2.service;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.livewiremobile.store.storefront.dto.RuntimeTypeAdapterFactory;
import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.livewiremobile.store.storefront.dto.rbt.Caller;
import com.livewiremobile.store.storefront.dto.rbt.CallingParty;
import com.livewiremobile.store.storefront.dto.rbt.CallingParty.CallingPartyType;
import com.livewiremobile.store.storefront.dto.rbt.RBTPlaylist;
import com.livewiremobile.store.storefront.dto.rbt.RBTStation;
import com.livewiremobile.store.storefront.dto.rbt.Shuffle;
import com.livewiremobile.store.storefront.dto.rbt.Song;
import com.livewiremobile.store.storefront.dto.rbt.SystemTone;

public class BuildJsonFactory {

	private Gson gson = null;
	private static BuildJsonFactory buildJsonFactory = new BuildJsonFactory(); 
	
	protected BuildJsonFactory(){
		initJson();
	}
	
	private void setGson(Gson gson) {
		this.gson = gson;
	}
	
	public Gson getGson() {
		return gson;
	}
	
	public static BuildJsonFactory getJson(){
		return buildJsonFactory;		
	}
	
	private void initJson() {
        GsonBuilder gsonBuilder = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
        RuntimeTypeAdapterFactory<Asset> assetAdapter = RuntimeTypeAdapterFactory.of(Asset.class , "type");
        assetAdapter.registerSubtype(Song.class , AssetType.SONG.toString());
        assetAdapter.registerSubtype(Shuffle.class , AssetType.SHUFFLELIST.toString());
        assetAdapter.registerSubtype(SystemTone.class ,  AssetType.SYSTEMTONE.toString());
        assetAdapter.registerSubtype(RBTPlaylist.class ,  AssetType.RBTPLAYLIST.toString());
        assetAdapter.registerSubtype(RBTStation.class ,  AssetType.RBTSTATION.toString());

        RuntimeTypeAdapterFactory<CallingParty> callingPartyassetAdapter = RuntimeTypeAdapterFactory.of(CallingParty.class, "type");
        callingPartyassetAdapter.registerSubtype(Caller.class , CallingPartyType.CALLER.toString());

        gsonBuilder.registerTypeAdapterFactory(assetAdapter);
        gsonBuilder.registerTypeAdapterFactory(callingPartyassetAdapter);
        gsonBuilder.serializeNulls();
        setGson(gsonBuilder.create());

	}
}
