package com.onmobile.apps.ringbacktones.ussd.common;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfileBean;
import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfileUSSD;
import com.onmobile.apps.ringbacktones.ussd.airtelprofile.AirtelProfilesClip;

public class USSDCacheManager {

	private static int PROFILE_CAT_ID=99;
	private  RBTCacheManager cacheManager = null;
	private static Logger basicLogger = Logger.getLogger(USSDCacheManager.class);
	
	public USSDCacheManager(){
		cacheManager=RBTCacheManager.getInstance();
	}
	public  ArrayList<AirtelProfileBean> populateLanguageProfileMap(ArrayList languageList,ArrayList langValuesList){
		basicLogger.debug(" populateLanguageProfileMap "+" LanList : "+languageList+" LanValList "+langValuesList);
		if(languageList==null || languageList.size()==0 || langValuesList==null|| langValuesList.size()==0){
			basicLogger.debug(" populateLanguageProfileMap Arraylist is null ");
			return null;
		}
		ArrayList profileBeanList=null;
		for(int count=0;count<languageList.size();count++){
			String langTemp=(String)languageList.get(count);
			String langValueTemp=(String)langValuesList.get(count);
			if(langTemp!=null && langTemp.length()>0){
				ArrayList profileClipsList=getProfileClips(langTemp);
				if(profileClipsList!=null && profileClipsList.size()>0){
					if(profileBeanList==null){
						profileBeanList=new ArrayList();
					}
					AirtelProfileBean profileBean=new AirtelProfileBean();
					profileBean.setLang(langTemp);
					profileBean.setLangvalue(langValueTemp);
					profileBean.setProfileClips(profileClipsList);
					profileBeanList.add(profileBean);
				}
			}
		}
		return profileBeanList;
	}
	public ArrayList getProfileClips(String language){
		ArrayList profileList=null;
		basicLogger.debug(" getProfileClips Language "+language);
		Clip[] profileClipsTemp=cacheManager.getClipsInCategory(PROFILE_CAT_ID);

		if(profileClipsTemp!=null && profileClipsTemp.length>0){


			for(int index=0;index<profileClipsTemp.length;index++){
				Clip tempClip=profileClipsTemp[index];
				if(tempClip!=null ){

					String tempNameWavFile=tempClip.getClipPreviewWavFile();
					if(tempNameWavFile!=null && tempNameWavFile.indexOf("_"+language+"_")!=-1){
						AirtelProfilesClip profileClip=new AirtelProfilesClip();
						profileClip.setCatId(PROFILE_CAT_ID);
						profileClip.setClipId(tempClip.getClipId());
						profileClip.setEndDate(tempClip.getClipEndTime());
						profileClip.setSongName(tempClip.getClipName());
						profileClip.setStartDate(tempClip.getClipStartTime());
						profileClip.setWavfile(tempClip.getClipPreviewWavFile());
						profileClip.setSmsAlias(tempClip.getClipSmsAlias());
						if(profileList==null){
							profileList=new ArrayList();
						}
						profileList.add(profileClip);
					}
				}
			}
		}else
			basicLogger.debug(" getProfileClips clips is null ");
		return profileList;
	}
	/*
	 * Get List of Song under a categoryID
	 * @param categoryId
	 * 
	 */
	public ArrayList<ClipBean> getSongList(String categoryID){
		ArrayList<ClipBean> songList=null;
		basicLogger.debug(" getSongList Catid "+categoryID);
		int catID=-1;
		try{
			catID=Integer.parseInt(categoryID);
		}catch(Exception e){
			
		}
		Clip[] clipsTemp=cacheManager.getClipsInCategory(catID);

		if(clipsTemp!=null && clipsTemp.length>0){


			for(int index=0;index<clipsTemp.length;index++){
				Clip tempClip=clipsTemp[index];
				if(tempClip!=null ){
						ClipBean clipBean=new ClipBean();
						clipBean.setCatId(catID);
						clipBean.setClipId(tempClip.getClipId());
						clipBean.setEndDate(tempClip.getClipEndTime());
						clipBean.setSongName(tempClip.getClipName());
						clipBean.setStartDate(tempClip.getClipStartTime());
						clipBean.setWavfile(tempClip.getClipPreviewWavFile());
						clipBean.setSmsAlias(tempClip.getClipSmsAlias());
						clipBean.setAlbum(tempClip.getAlbum());
						clipBean.setArtist(tempClip.getArtist());
						clipBean.setPromoID(tempClip.getClipPromoId());
						if(songList==null){
							songList=new ArrayList<ClipBean>();
						}
						songList.add(clipBean);
					
				}
			}
		}else
			basicLogger.debug(" getSonglist clips is null ");
		if(songList!=null)
		      basicLogger.debug(" getSongList size "+songList.size());
		else
			basicLogger.debug(" getSonglist is null ");
		return songList;

	}
	//get charge Amount
	public String getchargeAmount(){
		String amount=null;
		Category cat=cacheManager.getCategory(99);
		String classType=cat.getClassType();
		if(classType!=null)
			amount=CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType).getAmount();
		return amount;
	}
	//get charge Amount
	public String getchargeAmount(String catID){
	   	basicLogger.debug(" getchargeAmount catid = "+catID);
	   int catId=-1;
		String amount=null;
		try{
			catId=Integer.parseInt(catID);
		}catch(NumberFormatException nfe){
			
		}
		Category cat=cacheManager.getCategory(catId);
		String classType=cat.getClassType();
		basicLogger.debug(" getchargeAmount classType = "+classType);
		if(classType!=null)
			amount=CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType).getAmount();
		basicLogger.debug(" getchargeAmount amount = "+amount);
		return amount;
		
	}
	//get subscription amount
	public String getSubclassAmount(String subPackType){
		String amount=null;
		SubscriptionClass subClass=CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subPackType);
		if(subClass!=null)
			return subClass.getSubscriptionAmount();
		return amount;
	}
	//Get parameters from rbt_parameters table
	public String getParameters(String type,String paramName,String defaultVal){
		 Parameters param=CacheManagerUtil.getParametersCacheManager().getParameter(type, paramName,defaultVal);
		 if(param!=null)
			 return param.getValue();
		return null;
	}
}
