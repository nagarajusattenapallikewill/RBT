package com.onmobile.apps.ringbacktones.v2.factory;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.livewiremobile.store.storefront.dto.rbt.Asset;
import com.livewiremobile.store.storefront.dto.rbt.Asset.AssetType;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.v2.bean.AssetBean;
import com.onmobile.apps.ringbacktones.v2.dao.IRbtUgcWavfileDao;
import com.onmobile.apps.ringbacktones.v2.dao.IUDPDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPBean;
import com.onmobile.apps.ringbacktones.v2.service.AssetTypeAdapter;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;

public class BuildAssetFactory {
	
	
	Logger logger = Logger.getLogger(BuildAssetFactory.class);

	private Map<String, Class<?>> registerMap = null;
	private static BuildAssetFactory buildAssetFactory = null;
	private final static Object object = new Object();
	
	private static AssetTypeAdapter assetTypeAdapter = null;
	
	public void setAssetTypeAdapter(AssetTypeAdapter assetTypeAdapter) {
		this.assetTypeAdapter = assetTypeAdapter;
	}

	private BuildAssetFactory() {
		registerBuildFactoryMap();
	}
	
	private void registerBuildFactoryMap() {
		registerMap = new HashMap<String, Class<?>>();
		registerMap.put(AssetType.SONG.toString(), SongAsset.class);
		registerMap.put(AssetType.RBTSTATION.toString(), RBTStationAsset.class);
		registerMap.put(AssetType.SHUFFLELIST.toString(), ShuffleAsset.class);
		registerMap.put(AssetType.RBTPLAYLIST.toString(), RbtPlaylistAsset.class);
		registerMap.put(AssetType.RBTUGC.toString(), UGCSongAsset.class);
		registerMap.put(AssetType.RBTPROFILETONE.toString(), RBTProfileToneAsset.class);
	}
	
	public static BuildAssetFactory createBuildAssetFactory(){
		if(buildAssetFactory == null) {
			synchronized (object) {
				if (buildAssetFactory == null) {
					buildAssetFactory = new BuildAssetFactory();
				}
			}
		}
		return buildAssetFactory;
		
	}
	
	public Asset buildAssetFactoryFromDownload(Download download) {
		AssetBean assetBean = new AssetBean(download.getToneID(), download.getRefID(), download.getToneName(), download.getCategoryID(), download.getChargeClass(),download.getNextBillingDate(),download.getChargeClass());
		try {
			return BuildAssetFactory.createBuildAssetFactory().buildAssetFactory(assetBean);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}	
	}
	
	public Asset buildAssetFactory(AssetBean assetBean) throws Exception{
		
		Category category = RBTCacheManager.getInstance().getCategory(assetBean.getCategoryId());
		
		if(category == null) {
			throw new Exception("Can't find assetType with null category object categoryId: " + assetBean.getCategoryId());
		}
		String assetType = null;
		if (assetBean.getUdpName() != null) {
			assetType = assetTypeAdapter.getAssetType(-1);
		}
		/*else if(category.getCategoryId() == 99) {
			assetType = "RBTPROFILETONE";
		}*/
		else
			assetType = assetTypeAdapter.getAssetType(category.getCategoryTpe());
		
		Asset asset = null;
		IRBTAsset obj = null;
		try {
			obj = (IRBTAsset) registerMap.get(assetType).newInstance();
			asset = obj.buildAsset(assetBean);
			asset.setChargeClass(assetBean.getChargeClass());
		} catch (InstantiationException e) {
			throw new Exception("Asset not registred with asset type " + assetType, e);
		} catch (IllegalAccessException e) {
			throw new Exception("Asset not registred with asset type " + assetType, e);
		}
		return asset;
	}
	
	public Asset buildAssetFactoryFromSetting(Setting setting) {
		try {
			String udpName = null;
			if (setting.getUdpId() != -1) {
				IUDPDao udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
				UDPBean udpBean = udpDao.getUDPById(setting.getUdpId());
				udpName = udpBean.getUdpName();
			}

			AssetBean assetBean = null;
			if (setting != null && setting.getCutRBTStartTime() != null) {
				String cutrbtduration = setting.getCutRBTStartTime();
				logger.info(":---> CutRbtDuration" + cutrbtduration);
				assetBean = new AssetBean(setting.getToneID(), setting.getRefID(), setting.getToneName(),
						setting.getCategoryID(), setting.getChargeClass(), setting.getUdpId() + "", udpName,
						setting.getStatus(), cutrbtduration);

			} else {
				assetBean = new AssetBean(setting.getToneID(), setting.getRefID(), setting.getToneName(),
						setting.getCategoryID(), setting.getChargeClass(), setting.getUdpId() + "", udpName,
						setting.getStatus());
			}
			return BuildAssetFactory.createBuildAssetFactory().buildAssetFactory(assetBean);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
	
	public Asset buildAssetFactoryFromSubscriberStatus(SubscriberStatus subscriberStatus) {
		try {
			String udpName = null;

			int udpId = -1;

			if (subscriberStatus.udpId() != null) {
				udpId = Integer.parseInt(subscriberStatus.udpId());
			}

			if (udpId != -1) {
				IUDPDao udpDao = (IUDPDao) ConfigUtil
						.getBean(BeanConstant.UDP_DAO_IMPL);
				UDPBean udpBean = udpDao.getUDPById(udpId);
				udpName = udpBean.getUdpName();
			}
			
			Clip clip = null;
			if(subscriberStatus.categoryType() == iRBTConstant.RECORD){
				IRbtUgcWavfileDao ugcWavfileDao = (IRbtUgcWavfileDao) ConfigUtil.getBean(BeanConstant.UGC_WAV_FILE_DAO);
				RBTUgcWavfile ugcWavFile = ugcWavfileDao.getUgcWavFile(Long.parseLong(subscriberStatus.subID()), subscriberStatus.subscriberFile());
				clip = new Clip();
				clip.setClipId((int)ugcWavFile.getUgcId());
			}else{
				 clip = RBTCacheManager.getInstance().getClipByRbtWavFileName(subscriberStatus.subscriberFile());
			}
			
			
			AssetBean assetBean = new AssetBean(clip.getClipId(),
					subscriberStatus.refID(), clip.getClipName(),
					subscriberStatus.categoryID(), subscriberStatus.classType(),
					udpId + "", udpName,subscriberStatus.status());

			return BuildAssetFactory.createBuildAssetFactory()
					.buildAssetFactory(assetBean);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}
