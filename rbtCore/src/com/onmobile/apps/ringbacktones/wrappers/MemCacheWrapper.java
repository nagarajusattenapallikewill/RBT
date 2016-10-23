package com.onmobile.apps.ringbacktones.wrappers;



import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;



public class MemCacheWrapper {

	private  static RBTCacheManager memCache = null;
	static final org.apache.log4j.Logger c_logger = org.apache.log4j.Logger.getLogger(MemCacheWrapper.class);

	private MemCacheWrapper(){

	}
	private void init()
	{
		if(memCache==null){
			memCache = RBTCacheManager.getInstance();
		}
	}
	public static MemCacheWrapper getInstance(){
		MemCacheWrapper cacheController = new MemCacheWrapper();
		cacheController.init();

		return (cacheController);
	}
//	public ArrayList getProfileClips(String language){
//		ArrayList profileList=null;
//		Clip[] profileClipsTemp=cacheManager.getClipsInCategory(PROFILE_CAT_ID);
//
//		if(profileClipsTemp!=null && profileClipsTemp.length>0){
//
//
//			for(int index=0;index<profileClipsTemp.length;index++){
//				Clip tempClip=profileClipsTemp[index];
//				if(tempClip!=null ){
//
//					String tempNameWavFile=tempClip.getClipPreviewWavFile();
//					if(tempNameWavFile!=null && tempNameWavFile.indexOf("_"+language+"_")!=-1){
//						ProfileClip profileClip=new ProfileClip();
//						profileClip.setCatId(PROFILE_CAT_ID);
//						profileClip.setClipId(tempClip.getClipId());
//						profileClip.setEndDate(tempClip.getClipEndTime());
//						profileClip.setSongName(tempClip.getClipName());
//						profileClip.setStartDate(tempClip.getClipStartTime());
//						profileClip.setWavfile(tempClip.getClipPreviewWavFile());
//						if(profileList==null){
//							profileList=new ArrayList();
//						}
//						profileList.add(profileClip);
//					}
//				}
//			}
//		}
//		return profileList;
//	}
	public Clip getClipByRbtWavFileName(String rbtWavFile){
		
		
		Clip clip=memCache.getClipByRbtWavFileName(rbtWavFile);
		return clip;
	}
	public Clip[] getClips(int catId){
		Clip[] clips=memCache.getClipsInCategory(catId);
		return clips;
	}
	public Clip getClip(int clipId){
		Clip clip=null;
		clip=memCache.getClip(clipId);

		return clip;

	}
	public Clip getClip(int clipId, String language){
		Clip clip=null;
		clip=memCache.getClip(clipId, language);

		return clip;

	}
	public Clip getClip(String clipId){
		Clip clip=null;
		clip=memCache.getClip(clipId);

		return clip;

	}
	public Category getCategory(int catId){
		Category category=memCache.getCategory(catId);
		return category;
	}
	
	public Category[] getAllCategoriesForCircle(String circleID){
		Category[] categories =  memCache.getCategoriesInCircle(circleID, 0, 'b');
		if (categories == null || categories.length == 0){
			categories = memCache.getCategoriesInCircle(circleID, 0, 'y');
		}
		return categories;
	}

	public Category[] getAllSubCategoriesForCategory(int categoryID, String circleID){
		return memCache.getCategoriesInCircle(circleID, categoryID, 'b');
	}

	public Clip[] getAllClips(int categoryID){
		return memCache.getClipsInCategory(categoryID);
	}
	//	public ArrayList<Lu>search(HashMap map, int no){


}
