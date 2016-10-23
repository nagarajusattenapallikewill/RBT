package com.onmobile.apps.ringbacktones.v2.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.rbt.Asset;

public class AssetTypeAdapter {
	
	Logger logger = Logger.getLogger(AssetTypeAdapter.class);
	
	public AssetTypeAdapter() {
		logger.info("AssetTypeAdapter Object Injected");
	}
	
	private final Map<Integer, Asset.AssetType> assetMap = new HashMap<Integer, Asset.AssetType>();
	

	public void setAssetMap(Map<Integer, Asset.AssetType> assetMap) {
		this.assetMap.putAll(assetMap);
	}

//	private static enum CategoryTypeEnum {
//		SONG_7(7), SONG_5(5), RBTSTATION(16), SHUFFLELIST(33), RBTPLAYLIST(0), TEST(5);
//
//	    private final int categoryType;
//
//	    private static Map<Integer, CategoryTypeEnum> map =
//	            null;
//
//	    private CategoryTypeEnum(final int leg) {
//	        categoryType = leg;
//	    }
//	    
//	    static {
//	    	map = new HashMap<Integer, CategoryTypeEnum>();
//	    	for(CategoryTypeEnum leg : CategoryTypeEnum.values()) {
//	    		map.put(leg.categoryType, leg);
//	    	}
//	    }
//
//	    public static CategoryTypeEnum valueOf(int categoryType) {
//	        return map.get(categoryType);
//	    }
//	}
	
	public String getAssetType(int categoryType) throws Exception{
    	String strAssetType = null;
    	Asset.AssetType assetType = assetMap.get(categoryType);
    	if(assetType != null) {
    		strAssetType = assetType.toString();    		
    	}
    	else {
    		throw new Exception("Exception asset type not mapped with category type");
    	}
    	return strAssetType;
    }
	
	
//    public static String getAssetTypeEnum(int categoryType) throws Exception{
//    	String strAssetType = null;
//    	CategoryTypeEnum assetType = CategoryTypeEnum.valueOf(categoryType);
//    	if(assetType != null) {
//    		strAssetType = assetType.toString();
//    		if(strAssetType.indexOf("_") != -1) {
//    			strAssetType = strAssetType.substring(0, strAssetType.indexOf("_"));
//    		}
//    	}
//    	else {
//    		throw new Exception("Exception asset type not mapped with category type");
//    	}
//    	return strAssetType;
//    }
}
