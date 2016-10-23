package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip.ClipInfoKeys;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfo;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;

public class ClipThread extends GenericCacheThread {

	private static final Logger logger = Logger.getLogger(ClipThread.class);
	
	public static final String support_album_alphabet_index = "support_album_alphabet_index"; 

	public ClipThread(String name, List clips) {
		super(name, clips);
	}
	
	@Override
	public void processRecord(Object obj) throws Exception {
		String defaultLanguage = RBTContentJarParameters.getInstance().getParameter("default_language");
		String[] supportedLanguages = null;
		if(RBTContentJarParameters.getInstance().getParameter("supported_languages")!=null && !(RBTContentJarParameters.getInstance().getParameter("supported_languages").equals(""))){
			supportedLanguages = RBTContentJarParameters.getInstance().getParameter("supported_languages").split(",");
		}
		Clip clip = (Clip) obj;
		
		if( clip.getClipStartTime().getTime() > System.currentTimeMillis()) {
			return;
		}
		
		ArrayList<String> keysList = new ArrayList<String>();
		String origClipName = clip.getClipName();
		String origGrammar = clip.getClipGrammar();
		String origSMSAlias = clip.getClipSmsAlias();
		String origArtist = clip.getArtist();
		String origAlbum = clip.getAlbum();
		String origLanguage = clip.getLanguage();
		String origClipInfo = clip.getClipInfo();
		String origImgPath = null;
		String origKey = null;
		ClipInfoKeys[] keys = ClipInfoKeys.values();
		for(int i=0;i<keys.length;i++)
		{
			keysList.add(keys[i].toString());
			
		}
			
		if(null == clip) {
			throw new IllegalArgumentException("The parameter clip can't be null");
		}
		/* Set for all languages as well as particular language
		*  For all languages the key will be clipId_<CLIP_ID>_ALL, in this case the set will be null
		*  For particular language the key will be clipId_<CLIP_ID>_LANG, in this case the map will be null
		*/
		//-------- Set a temp variable for clipInfoSet
		Set<ClipInfo> tempClipInfoSet = clip.getClipInfoSet();
		clip.setClipInfoSet(null);
		clip.setClipLanguage(defaultLanguage);
		//-----------Set the cache for default language
		mc.set(RBTCacheKey.getClipIdLanguageCacheKey(clip.getClipId(), defaultLanguage), clip);
		clip.setClipInfoSet(tempClipInfoSet);
		//------Populate the ClipInfoMap to get all language info
		if(clip.getClipInfoSet()!=null && clip.getClipInfoSet().size()>0){
			Iterator<ClipInfo> itr = clip.getClipInfoSet().iterator();
			while(itr.hasNext()){
				ClipInfo clipInfo = itr.next();
				Map<String, String> clipInfoMap = clip.getClipInfoMap();
				if(clipInfoMap==null)
					clipInfoMap = new HashMap<String, String>();
				clipInfoMap.put(clipInfo.getName(), clipInfo.getValue());
				clip.setClipInfoMap(clipInfoMap);
			}
			Map<String, String> clipInfoMap = clip.getClipInfoMap();
			Map<String, String> clipMap = new HashMap<String,String>();
			if(clipInfoMap!=null){
			for(int j=0;j<keysList.size();j++)
			{
				if(clipInfoMap.containsKey(keysList.get(j))){
					origImgPath = clipInfoMap.get(keysList.get(j));
					origKey = keysList.get(j);
					if(origKey!=null&&origImgPath!=null){
						logger.info("adding into the map " + origKey+" " + origImgPath );
						clipMap.put(origKey, origImgPath);					
				}
			}
			}
			}

			
			/* Set for all languages
			*  ClipInfoset is not required in case of all languages
			*/
			clip.setClipInfoSet(null);
			mc.set(RBTCacheKey.getClipIdLanguageCacheKey(clip.getClipId(),"ALL"), clip);
			//-------- Again set the clipInfoSet
			clip.setClipInfoSet(tempClipInfoSet);
			//--------- ClipInfoMap is not required in case of specific languages
			
			
			clip.setClipInfoMap(clipMap);
			
			//-----------Set the cache for default language
			mc.set(RBTCacheKey.getClipIdLanguageCacheKey(clip.getClipId(), defaultLanguage), clip);
			
			//------- Set for supported languages
			if(supportedLanguages!=null && supportedLanguages.length>0){
				for(int i=0; i<supportedLanguages.length; i++){
					Iterator<ClipInfo> clipInfoitr = clip.getClipInfoSet().iterator();
					//-------- Set information on the clip bean for the passed language from the clipInfoSet
					clip.setClipName(null);
					clip.setClipGrammar(null);
					clip.setArtist(null);
					clip.setAlbum(null);
					clip.setClipSmsAlias(null);
					clip.setClipInfo(null);
					clip.setLanguage(null);
					clip.setClipLanguage(defaultLanguage);
					while(clipInfoitr.hasNext()){
						ClipInfo clipInfo = clipInfoitr.next();
//						if(!clipInfo.getName().endsWith(supportedLanguages[i].trim().toUpperCase())){
//							continue;
//						}
						if(clipInfo.getName().equalsIgnoreCase(RBTCacheKey.getClipNameLanguageKey(supportedLanguages[i].toUpperCase()))){
							clip.setClipName(clipInfo.getValue());
						}
						if(clipInfo.getName().equalsIgnoreCase(RBTCacheKey.getClipGrammarLanguageKey(supportedLanguages[i].toUpperCase()))){
							clip.setClipGrammar(clipInfo.getValue());
						}
						if(clipInfo.getName().equalsIgnoreCase(RBTCacheKey.getClipArtistLanguageKey(supportedLanguages[i].toUpperCase()))){
							clip.setArtist(clipInfo.getValue());
						}
						if(clipInfo.getName().equalsIgnoreCase(RBTCacheKey.getClipAlbumLanguageKey(supportedLanguages[i].toUpperCase()))){
							clip.setAlbum(clipInfo.getValue());
						}
						if(clipInfo.getName().equalsIgnoreCase(RBTCacheKey.getClipSMSAliasLanguageKey(supportedLanguages[i].toUpperCase()))){
							clip.setClipSmsAlias(clipInfo.getValue());
						}
						if(clipInfo.getName().equalsIgnoreCase(RBTCacheKey.getClipInfoLanguageKey(supportedLanguages[i].toUpperCase()))){
							clip.setClipInfo(clipInfo.getValue());
						}
						if(clipInfo.getName().equalsIgnoreCase(RBTCacheKey.getClipLanguageLanguageKey(supportedLanguages[i].toUpperCase()))){
							clip.setLanguage(clipInfo.getValue());
						}
					}
					
					boolean isSupportedLangAvail = false;
					
					if(clip.getClipName()==null || clip.getClipName().equals("")) { 
						clip.setClipName(origClipName);
					}
					else {
						isSupportedLangAvail = true;
					}

					if(clip.getArtist()==null || clip.getArtist().equals("")) { 
						clip.setArtist(origArtist);
					}
					else {
						isSupportedLangAvail = true;
					}

					if(clip.getAlbum()==null || clip.getAlbum().equals("")) { 
						clip.setAlbum(origAlbum);
					}
					else {
						isSupportedLangAvail = true;
					}

					if(clip.getLanguage()==null || clip.getLanguage().equals("")) clip.setLanguage(origLanguage);
					if(clip.getClipInfo()==null || clip.getClipInfo().equals("")) clip.setClipInfo(origClipInfo);
					if(clip.getClipGrammar()==null || clip.getClipGrammar().equals("")) clip.setClipGrammar(origGrammar);
					if(clip.getClipSmsAlias()==null || clip.getClipSmsAlias().equals("")) clip.setClipSmsAlias(origSMSAlias);
					
					//If not supported, we don't add to the memcache.
					if(isSupportedLangAvail) {
						clip.setClipLanguage(supportedLanguages[i]);
						mc.set(RBTCacheKey.getClipIdLanguageCacheKey(clip.getClipId(), supportedLanguages[i]), clip);
					}
					
				}
			}
		}/*else{
			//-------- Populate in all languages for the default language value
			if(supportedLanguages!=null && supportedLanguages.length>0){
				for(int i=0; i<supportedLanguages.length; i++){
					mc.set(RBTCacheKey.getClipIdLanguageCacheKey(clip.getClipId(), supportedLanguages[i]), clip);
				}
			}
		}*/
		
		if(null != clip.getClipPromoId() && clip.getClipPromoId().length() > 0) {			
			String[] clipPromoCode = clip.getClipPromoId().split(",");
			for (int index = 0; index < clipPromoCode.length; index++) {
				mc.set(RBTCacheKey.getPromoIdCacheKey(clipPromoCode[index].trim()), "" + clip.getClipId());
			}
//			mc.set(RBTCacheKey.getPromoIdCacheKey(clip.getClipPromoId()), "" + clip.getClipId());
		}
		if(null != clip.getClipRbtWavFile() && clip.getClipRbtWavFile().length() > 0) {
			mc.set(RBTCacheKey.getRbtWavFileCacheKey(clip.getClipRbtWavFile()), "" + clip.getClipId());
		}
		
		if(null != clip.getClipVcode() && clip.getClipVcode().length() > 0) {			
			String[] clipVcode = clip.getClipVcode().split(",");
			for (int index = 0; index < clipVcode.length; index++) {
				mc.set(RBTCacheKey.getVcodeCacheKey(clipVcode[index].trim()), "" + clip.getClipId());
			}
		}
		
		if(null != clip.getClipSmsAlias() && clip.getClipSmsAlias().length() > 0) {
			String[] smsAlias = RBTCache.getMultipleSmsAlias(clip.getClipSmsAlias());
			for(int i=0; i<smsAlias.length; i++){
				mc.set(RBTCacheKey.getSmsAliasCacheKey(smsAlias[i]), "" + clip.getClipId());
			}
//			mc.set(RBTCacheKey.getSmsAliasCacheKey(clip.getClipSmsAlias()), "" + clip.getClipId());
		}
		// clip-album cache will be handled by CacheClipAlbum class 

		if (RBTContentJarParameters.getInstance().getParameter(
				support_album_alphabet_index) != null
				&& RBTContentJarParameters.getInstance().getParameter(
						support_album_alphabet_index).equalsIgnoreCase("TRUE")) {
			putClipInAlbumCache(clip.getAlbum(), clip.getClipId());
			Date currentDate = new Date();
			if (clip.getAlbum() != null 
					&& !clip.getAlbum().trim().isEmpty() 
					&& clip.getLanguage() != null 
					&& !clip.getLanguage().trim().isEmpty()
					&& !clip.getClipStartTime().after(currentDate)
					&& !clip.getClipEndTime().before(currentDate)
					) {
				Character albumStartingLetter = clip.getAlbum().charAt(0);
				if (!Character.isWhitespace(albumStartingLetter)) {
					String key = RBTCacheKey.getAlbumNameByLanguageKey(clip.getLanguage(), albumStartingLetter);
					@SuppressWarnings("unchecked")
					Set<String> set = (Set<String>) mc.get(key);
					if (set == null) {
						set = new HashSet<String>();
						set.add(clip.getAlbum());
						mc.add(key, set);
					} else {
						set.add(clip.getAlbum());
						mc.replace(key, set);
					}
				}
			}
		}
	}

	@Override
	public void finalProcess() throws Exception {
		// nothing to do
	}

	private void putClipInAlbumCache(String album, int clipId) {
		String albumKey = RBTCacheKey.getAlbumCacheKey(album);
		String clipIds = (String) mc.get(albumKey);
		if (clipIds == null) {
			mc.set(albumKey, RBTCacheKey.getClipIdCacheKey(clipId));
		} else {
			String sClipId = RBTCacheKey.getClipIdCacheKey(clipId);
			mc.set(albumKey, clipIds + "," + sClipId);
		}
	}
}