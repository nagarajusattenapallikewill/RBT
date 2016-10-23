package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.servlets.ClipsCCC;
public class ClipCacher{
	private static Logger logger = Logger.getLogger(ClipCacher.class);
	
	public static HashMap m_SubscriptionTypeMap = null;
	public static final String SUBSCRIPTION_MAP = "SUBSCRIPTION_MAP";

	private static ClipCacher cache;
	public static Hashtable m_clips = null;
	public static Hashtable m_clipIDPromoID = null;
	public static HashMap m_parentCategories = null;
	public static HashMap m_categoryClipsMap = null;//Integer (catID), ArrayList (int clipsIDs)

	public static HashMap m_categoryIdMap = null;
	public static RBTDBManager m_rbtDBManager = null;
	private static Categories[] categories=null;
	private static boolean bUpdateOperatorPrefixSite = false;
	public static SitePrefix localSitePrefix = null;
	private static HashMap m_operatorPrefix = new HashMap();
	private static HashMap m_circle_id = new HashMap();
	static String m_countryPrefix = "91";
	static Object lock = new Object();
	private static String strBuyMappings=null;
	private static String CCC = "CCC";
	private static String BUY_MAP = "BUY_MAP";

	public static  ClipCacher init(){

		long initTime =System.currentTimeMillis();
		if (cache != null)
			return cache;
		synchronized (lock)
		{
			if (cache != null)
				return cache;
			try
			{
				cache = new ClipCacher();
			}
			catch (Exception e)
			{
				logger.error("", e);
				cache = null;
			}
		}
		long finalTime=System.currentTimeMillis();
		logger.info("TIME TAKEN FOR INIT="+(finalTime-initTime));
		return cache;
	}

	public  static ClipCacher init (int i){
		if (cache != null)
			return cache;
		synchronized (lock)
		{
			if (cache != null)
				return cache;
			try
			{
				cache = new ClipCacher(i);
			}
			catch (Exception e)
			{
				logger.error("", e);
				cache = null;
			}
		}
		return cache;
	}

	private	ClipCacher(int i) throws Exception {
		m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
		m_rbtDBManager = RBTDBManager.getInstance();
		//doInitializeCache();
		//initialiseCategoryMap();
		//initialiseClipMap();
		bUpdateOperatorPrefixSite = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "UPDATE_OPERATOR_PREFIX", "FALSE"); 
		if(bUpdateOperatorPrefixSite) {
			updateOperatorPrefixes();
			updateCircleID();
		}
		// categories=RBTSubUnsub.init().getActiveCategories();
	}


	private ClipCacher() throws Exception{
		long a=0,b=0;

		m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
		m_rbtDBManager = RBTDBManager.getInstance();
		a = System.currentTimeMillis();
//		categories=RBTSubUnsub.init().getGUIActiveCategories();
		b = System.currentTimeMillis();
		logger.info("Time taken for getting parentCategories="+(b-a));
		a = System.currentTimeMillis();
		doInitializeCache();
		b = System.currentTimeMillis();
		logger.info("Time taken for caching clips="+(b-a));
		a=System.currentTimeMillis();
		initialiseCategoryMap();
		b=System.currentTimeMillis();
		logger.info("Time taken for caching Categories ="+(b-a));

		initialiseClipMap();
		a=System.currentTimeMillis();
		logger.info("Time taken for caching CategoryClipMap ="+(a-b));
		bUpdateOperatorPrefixSite = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "UPDATE_OPERATOR_PREFIX", "FALSE");
		if(bUpdateOperatorPrefixSite) {
			updateOperatorPrefixes();
			updateCircleID();
		}

	}

	public Categories[] getActiveCategories(String circleID){
		if(categories!=null){
			return categories;
		}else
			categories=RBTSubUnsub.init().getGUIActiveCategories(circleID, 'b');
		return categories;
	}

	public static void updateOperatorPrefixes()
	{
		List<SitePrefix> prefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (prefixes == null || prefixes.size() <= 0)
			return;
		HashMap temp = new HashMap();
		ArrayList numbers = null;
		for (int i = 0; i < prefixes.size(); i++)
		{
			numbers = Tools.tokenizeArrayList(prefixes.get(i).getSitePrefix(), null);
			if (prefixes.get(i).getSiteUrl() == null || prefixes.get(i).getSiteUrl().length() <= 0 )
			{	
				localSitePrefix = prefixes.get(i);
				continue;                           
			}
			if( numbers == null || numbers.size() <= 0)
				continue;
			for (int j = 0; j < numbers.size(); j++)
				temp.put((String) numbers.get(j), prefixes.get(i).getSiteUrl());
		}
		m_operatorPrefix = temp;
	}

	private void getClips()
	{
		//String _method = "getClips()";
		////logger.info("****** no parameters.");
		//return (m_rbtDBManager.getClipsByName(null));
		m_rbtDBManager.getAllClipsForCachingGui(m_clips,m_VcodeIDMap);
	}
	public ClipGui getClip(int clipID)
	{
		//String _method = "getClip()";
		////logger.info("****** parameters are --
		// "+clipID);
//		return (m_rbtDBManager.getClip(clipID));

		return ((ClipGui) m_clips.get(""+clipID));
	}

	public static ArrayList getSubCategories(String parentId){
		return (ArrayList)m_parentCategories.get(new Integer(Integer.parseInt(parentId)));

	}
	public static  String getSubCategoryName(String subCatId){
		return (String)m_categoryIdMap.get(subCatId);
	}
	/*
	private synchronized ClipGui getClip(String clipId){

		return (ClipGui)m_clips.get(clipId);

	}
	private synchronized ArrayList getClipArray(String subCatId){
		return (ArrayList)m_categoryClipsMap.get(new Integer(subCatId));
	}

	 */

	private void doInitializeCache()
	{
		//rbtClipsLucene.createWriter(getRequestRBTClips());
		m_clips = new Hashtable();
		getClips();
	}

	/**
	 * gives name:id map
	 * 
	 */
	//public HashMap getCategoryNameIDMap(){
	//	if(m_categoryNameIDMap==null)
	//		initialiseCategoryMap();
	///	return m_categoryNameIDMap;
	//  }

	public HashMap getCategoryIdMap(){
		if(m_categoryIdMap==null){
			initialiseCategoryMap();
		}
		return m_categoryClipsMap;
	}
	public HashMap getParentCategoriesMap(){
		if(m_parentCategories==null){
			initialiseClipMap();
		}
		return m_parentCategories;
	}
	public HashMap getcategoryClipMap(){
		if(m_categoryClipsMap==null){
			initialiseClipMap();
		}
		return m_categoryClipsMap;
	}
	private void initialiseCategoryMap(){ 
		ClipCacher.m_categoryIdMap=RBTDBManager.getInstance()
		.initialiseCategoriesMap();	
		long a= System.currentTimeMillis();
		initialiseClipMap();
		long b= System.currentTimeMillis();
		logger.info("time taken for caching cat-clip and cat-subcat is"+(b-a));
	}

	private void initialiseClipMap(){
		System.out.println("Initialising Cat Maps+++");

		m_parentCategories=new HashMap();
		m_categoryClipsMap=new HashMap();
		Categories[] parents = categories;
		if(parents!=null){
			for(int i=0;i<parents.length;i++){
				ArrayList cats = new ArrayList();
				Categories[] subc = RBTDBManager.getInstance()
				.getSubCategories(parents[i].id(), parents[i].circleID(), parents[i].prepaidYes());
				if(subc!=null){
					for(int j=0;j<subc.length;j++){
						ArrayList catClips = new ArrayList();
						cats.add(new Integer(subc[j].id()));
						Clips[] clips = RBTDBManager.getInstance()
						.getClipsInCategory(Integer.toString(subc[j].id()));
						if(clips!=null){
							for(int k=0;k<clips.length;k++){
								Integer id = new Integer(clips[k].id());
								catClips.add(id);
								m_ClipCategoryMap.put(clips[k].id()+"",subc[j].id()+"");
							}
							m_categoryClipsMap.put(new Integer(subc[j].id()), catClips);

						}
					}
					m_parentCategories.put(new Integer(parents[i].id()), cats);
				}
			}
		}
	}

	private String getChildCategoryId(String clipId){
		Iterator it = m_categoryClipsMap.keySet().iterator();
		while(it.hasNext()){
			Integer subCat = (Integer)it.next();
			ArrayList clips;
			synchronized (lock){
				clips= (ArrayList)m_categoryClipsMap.get(subCat);
			}
			if(clips!=null){
				if(clips.contains(new Integer(clipId))){
					return subCat.toString();
				}				
			}
		}
		return null;
	}

	private String getParentCategoryId(String clipId){
		String subCat = getChildCategoryId(clipId);
		Iterator it = m_parentCategories.keySet().iterator();
		while(it.hasNext()){
			Integer pcatid = (Integer)it.next();
			ArrayList subCats = (ArrayList)m_parentCategories.get(pcatid);
			if(subCats!=null&&subCat!=null){
				if(subCats.contains(new Integer(subCat))){
					return pcatid.toString();

				}
			}
		}
		return null;
	}

	private ArrayList selectSearchedClips(ArrayList clips,String searchText,String searchOption){
		ArrayList clipDets = clips;
		if(searchOption!=null&&searchText!=null&&searchText.length()>0){
			if(searchOption.equalsIgnoreCase("vcode")){
				clipDets = new ArrayList();
				for(int i=0;i<clips.size();i++){
					if(((ClipsCCC)clips.get(i)).getWavFile().toLowerCase().indexOf(searchText.toLowerCase())>0){
						clipDets.add(clips.get(i));
					}
				}
			}if(searchOption.equalsIgnoreCase("song")){
				clipDets = new ArrayList();
				for(int i=0;i<clips.size();i++){
					if(((ClipsCCC)clips.get(i)).getClipName().toLowerCase().indexOf(searchText.toLowerCase())>0){
						clipDets.add(clips.get(i));
					}
				}
			}if(searchOption.equalsIgnoreCase("album")){
				clipDets = new ArrayList();
				for(int i=0;i<clips.size();i++){
					if(((ClipsCCC)clips.get(i)).getAlbum().toLowerCase().indexOf(searchText.toLowerCase())>0){
						clipDets.add(clips.get(i));
					}
				}
			}if(searchOption.equalsIgnoreCase("artist")){
				clipDets = new ArrayList();
				for(int i=0;i<clips.size();i++){
					if(((ClipsCCC)clips.get(i)).getArtist().toLowerCase().indexOf(searchText.toLowerCase())>0){
						clipDets.add(clips.get(i));
					}
				}
			}
		}else{
			return clips;
		}
		return clipDets;
	}

	public List getClipDetails(int from,int to,String parentId,String subCat,String searchOption,String searchText,String sorter){
		//	ClipCacher rbtHelper = ClipCacher.init();
		ArrayList clipDets= new ArrayList();
		if(parentId!=null&&subCat!=null){
			if(parentId.equalsIgnoreCase("ALL")){
				ArrayList clipDetails = (ArrayList) RBTDBManager.getInstance().getClipDetails(from,to,parentId,subCat,searchOption,searchText,sorter);
				if(clipDetails!=null){
					for(int j =0;j<clipDetails.size();j++){
						ClipGui cm = (ClipGui)clipDetails.get(j);
						String subCatId =getChildCategoryId(""+cm.getClipId());
						String genreId = getParentCategoryId(""+cm.getClipId());
						String subCatName=null;
						String parent=null;
						if(subCatId!=null&&genreId!=null){
							subCatName=(String) m_categoryIdMap.get(subCatId);
							parent = (String) m_categoryIdMap.get(genreId);
						}
						clipDets.add(new ClipsCCC(cm,subCatId,subCatName,parent));
					}
					return clipDets;
				}
				return null;
			}
			if(subCat.equalsIgnoreCase("All")){
				ArrayList subCats = (ArrayList)m_parentCategories.get(new Integer(Integer.parseInt(parentId)));
				if(subCats!=null){
					for(int b=0;b<subCats.size();b++){
						ArrayList clips = (ArrayList)m_categoryClipsMap.get(subCats.get(b));
						for(int a =0;clips!=null&& a<clips.size();a++){
							ClipGui cm;

							cm = (ClipGui)m_clips.get(""+clips.get(a));							
							if(cm!=null){
								String parName = getParentCategoryId(""+cm.getClipId());	
								String subCatId = getChildCategoryId(""+cm.getClipId());
								ClipsCCC guiClip = new ClipsCCC(cm,getChildCategoryId(""+cm.getClipId()),(String)m_categoryIdMap.get(parName),(String)m_categoryIdMap.get(subCatId));					
								clipDets.add(guiClip);
							}
						}
					}
				}
				clipDets = selectSearchedClips(clipDets, searchText, searchOption);
			}else{
				ArrayList clips = (ArrayList)m_categoryClipsMap.get(new Integer(subCat));
				for(int a =0; a<clips.size();a++){
					ClipGui cm;

					cm = (ClipGui)m_clips.get(""+clips.get(a));							

					if(cm!=null){
						String parentName = getParentCategoryId(""+cm.getClipId());
						String subCatName = getChildCategoryId(""+cm.getClipId());
						ClipsCCC guiClip = new ClipsCCC(cm,getChildCategoryId(""+cm.getClipId()),(String)m_categoryIdMap.get(parentName),(String)m_categoryIdMap.get(subCatName));					
						clipDets.add(guiClip);
					}
				}
				clipDets = selectSearchedClips(clipDets, searchText, searchOption);
			}

		}
		if(sorter!=null){
			if(sorter!=null&&sorter.equalsIgnoreCase("vcode")){
				Collections.sort(clipDets, ClipsCCC.VCODE_COMPARATOR);
				if(to<clipDets.size())				 
					return clipDets.subList(from, to);
				else
					return clipDets.subList(from,clipDets.size());
			}
			if(sorter!=null&&sorter.equalsIgnoreCase("song")){
				Collections.sort(clipDets, ClipsCCC.SONG_COMPARATOR);
				if(to<clipDets.size())				 
					return clipDets.subList(from, to);
				else
					return clipDets.subList(from,clipDets.size());			}
			if(sorter!=null&&sorter.equalsIgnoreCase("album")){
				Collections.sort(clipDets, ClipsCCC.ALBUM_COMPARATOR);
				if(to<clipDets.size())				 
					return clipDets.subList(from, to);
				else
					return clipDets.subList(from,clipDets.size());			}
			if(sorter!=null&&sorter.equalsIgnoreCase("artist")){
				Collections.sort(clipDets, ClipsCCC.ARTIST_COMPARATOR);
				if(to<clipDets.size())				 
					return clipDets.subList(from, to);
				else
					return clipDets.subList(from,clipDets.size());			}
		}
		if(to<clipDets.size())				 
			return clipDets.subList(from, to);
		else 
			return clipDets.subList(from,clipDets.size());

	}

	public static String getModifiedSubscriptionType(String strSubscriptionType){
		String temp= null;
		try{
			if(m_SubscriptionTypeMap == null || !(m_SubscriptionTypeMap.size()>0)){
				String strMap =  CacheManagerUtil.getParametersCacheManager().getParameter(CCC,SUBSCRIPTION_MAP).getValue();
				if (strMap!=null){
					m_SubscriptionTypeMap = new HashMap();
					StringTokenizer tokens = new StringTokenizer (strMap,";");
					while(tokens.hasMoreTokens()){
						String map = tokens.nextToken();
						StringTokenizer elements = new StringTokenizer (map,"=");
						String key = elements.nextToken();
						String value = elements.nextToken();
						m_SubscriptionTypeMap.put(key, value);
					} 
				}
			}
			temp=(String)m_SubscriptionTypeMap.get(strSubscriptionType.trim().toUpperCase());
			if(temp!=null){
				return temp;
			}else{
				return strSubscriptionType;
			}
		}catch(Exception exe){
			return strSubscriptionType;
		}
	}

	public String[] getClipDetails(int from,int to,String parentId,String subId,String searchOption,String searchText){

		List clipDetails = new ArrayList();
		ClipCacher rbtHelper = ClipCacher.init();
		int count=0;
		if(parentId!=null&&parentId.length()>0&&!parentId.equalsIgnoreCase("all")){
			String parent = (String) m_categoryIdMap.get(parentId);
			if(subId!=null&&subId.length()>0&&!subId.equalsIgnoreCase("all")){
				String genre = (String) m_categoryIdMap.get(subId);
				ArrayList clips = (ArrayList) m_categoryClipsMap.get(new Integer(Integer.parseInt(subId)));
				if(clips!=null){
					if(searchOption!=null&&searchOption.length()>0&&searchText!=null&&searchText.length()>0){
						for(int l=0;l<clips.size();l++){
							ClipGui cm = rbtHelper.getClip(((Integer)clips.get(l)).intValue());
							boolean isAdd=false;
							if(searchOption.equalsIgnoreCase("artist")){
								if(cm.getArtist().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
									isAdd=true;
								}		
							}else if(searchOption.equalsIgnoreCase("album")){
								if(cm.getAlbum().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
									isAdd=true;
								}
							}else if(searchOption.equalsIgnoreCase("song")){
								if(cm.getClipName().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
									isAdd=true;
								}
							}else if(searchOption.equalsIgnoreCase("vcode")){
								if(cm.getWavFile().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
									isAdd=true;
								}
							}
							if(isAdd){
								count++;
								if(count>=from&&count<=to){
									String str = parent+";"+genre+";"+subId+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
									clipDetails.add(str);			
								}
							}
							if(clipDetails.size()>=to-from){
								return  (String[])clipDetails.toArray(new String[0]);
							}
						}
					}else{
						if(count+clips.size()<from){
							count=count+clips.size();
						}
						else if(count+clips.size()>=from){
							for(int l=0;l<clips.size();l++){
								count++;
								if(count>=from&&count<=to){
									ClipGui cm =rbtHelper.getClip(((Integer)clips.get(l)).intValue());
									String str = parent+";"+genre+";"+subId+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
									clipDetails.add(str);
								}

								if(clipDetails.size()>=to-from){
									return  (String[])clipDetails.toArray(new String[0]);
								}
							}
						}
					}
				}
				return (String[])clipDetails.toArray(new String[0]);

			}else if(subId!=null&&subId.length()>0&&subId.equalsIgnoreCase("all")){
				ArrayList subCats = (ArrayList) m_parentCategories.get(new Integer(Integer.parseInt(parentId)));
				if(subCats!=null){

					for(int k=0;k<subCats.size();k++){
						int	subid = ((Integer)subCats.get(k)).intValue();
						String genre = (String) m_categoryIdMap.get(""+subid);
						ArrayList clips = (ArrayList) m_categoryClipsMap.get(new Integer(subid));
						if(clips!=null){
							if(searchOption!=null&&searchOption.length()>0&&searchText!=null&&searchText.length()>0){
								for(int l=0;l<clips.size();l++){
									ClipGui cm = rbtHelper.getClip(((Integer)clips.get(l)).intValue());
									boolean isAdd=false;
									if(searchOption.equalsIgnoreCase("artist")){
										if(cm.getArtist().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}		
									}else if(searchOption.equalsIgnoreCase("album")){
										if(cm.getAlbum().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}else if(searchOption.equalsIgnoreCase("song")){
										if(cm.getClipName().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}else if(searchOption.equalsIgnoreCase("vcode")){
										if(cm.getWavFile().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}
									if(isAdd){
										count++;
										if(count>=from&&count<=to){
											String str = parent+";"+genre+";"+subid+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
											clipDetails.add(str);			
										}
									}
									if(clipDetails.size()>=to-from){
										return  (String[])clipDetails.toArray(new String[0]);
									}
								}
							}else{
								if(count+clips.size()<from){
									count=count+clips.size();
								}
								else if(count+clips.size()>=from){
									for(int l=0;l<clips.size();l++){
										count++;
										if(count>=from&&count<=to){
											ClipGui cm =rbtHelper.getClip(((Integer)clips.get(l)).intValue());
											String str = parent+";"+genre+";"+subid+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
											clipDetails.add(str);
										}

										if(clipDetails.size()>=to-from){
											return  (String[])clipDetails.toArray(new String[0]);
										}
									}
								}
							}
						}
					}
				}
				return  (String[])clipDetails.toArray(new String[0]);
			}
		}else if(parentId!=null&&parentId.length()>0&&parentId.equalsIgnoreCase("all")){

			Iterator iter= m_parentCategories.keySet().iterator();
			while(iter.hasNext()){
				int parId =((Integer)iter.next()).intValue();
				String parent = (String) m_categoryIdMap.get(Integer.toString(parId));
				ArrayList subCats = (ArrayList) m_parentCategories.get(new Integer(parId));
				if(subCats!=null){
					for(int k=0;k<subCats.size();k++){
						int	subid = ((Integer)subCats.get(k)).intValue();
						String genre = (String) m_categoryIdMap.get(""+subid);
						ArrayList clips = (ArrayList) m_categoryClipsMap.get(new Integer(subid));
						if(clips!=null){
							if(searchOption!=null&&searchOption.length()>0&&searchText!=null&&searchText.length()>0){

								for(int l=0;l<clips.size();l++){
									ClipGui cm = rbtHelper.getClip(((Integer)clips.get(l)).intValue());
									boolean isAdd=false;
									if(searchOption.equalsIgnoreCase("artist")){
										if(cm.getArtist().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}		
									}else if(searchOption.equalsIgnoreCase("album")){
										if(cm.getAlbum().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}else if(searchOption.equalsIgnoreCase("song")){
										if(cm.getClipName().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}else if(searchOption.equalsIgnoreCase("vcode")){
										if(cm.getWavFile().toLowerCase().indexOf(searchText.toLowerCase())!=-1){
											isAdd=true;
										}
									}
									if(isAdd){
										count++;
										if(count>=from&&count<=to){
											String str = parent+";"+genre+";"+subid+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
											clipDetails.add(str);			
										}
									}
									if(clipDetails.size()>=to-from){
										return  (String[])clipDetails.toArray(new String[0]);
									}
								}
							}else{
								if(count+clips.size()<from){
									count=count+clips.size();
								}
								else if(count+clips.size()>=from){
									for(int l=0;l<clips.size();l++){
										count++;
										if(count>=from&&count<=to){
											ClipGui cm =rbtHelper.getClip(((Integer)clips.get(l)).intValue());

											if(cm!=null){
												String str = parent+";"+genre+";"+subid+";"+cm.getClipId()+";"+cm.getWavFile().substring(4, cm.getWavFile().length()-4)+";"+cm.getClipName()+";"+cm.getArtist()+";"+cm.getAlbum()+";"+cm.getClassType();
												clipDetails.add(str);
											}
										}

										if(clipDetails.size()>=to-from){
											return  (String[])clipDetails.toArray(new String[0]);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return (String[])clipDetails.toArray(new String[0]);
	}

	public static String getCircleID(String strSub)
	{
		logger.info("RBT::entering getCircleID with subid== "+strSub);
		if(strSub == null || strSub.length() <= 0)
			return null;
		for(int i = 1; i <=strSub.length(); i++)
		{
			if(m_circle_id.containsKey(strSub.substring(0,i))){ 
				String temp=(String)m_circle_id.get(strSub.substring(0,i));
				logger.info("RBT::exiting getCircleID with value == "+temp); 
				return temp;
			}
		}	
		logger.info("RBT::exiting getCircleID with value == null"); 
		return null; 
	}
	public static void updateCircleID()
	{
		String _method = "updateCircleID()";
		List<SitePrefix> prefixes = CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (prefixes == null || prefixes.size() <= 0)
			return;
		HashMap temp = new HashMap();
		ArrayList numbers = null;
		for (int i = 0; i < prefixes.size(); i++)
		{
			numbers = Tools.tokenizeArrayList(prefixes.get(i).getSitePrefix(), null);
			if (prefixes.get(i).getSiteUrl() == null || prefixes.get(i).getSiteUrl().length() <= 0 )
			{	
				localSitePrefix = prefixes.get(i);
				continue;                           
			}
			if( numbers == null || numbers.size() <= 0)
				continue;
			for (int j = 0; j < numbers.size(); j++){
				logger.info("****** inserting"+(String) numbers.get(j)+"with circleId"+ prefixes.get(i).getCircleID()+" into m_circle_id hashMap");
				temp.put(((String) numbers.get(j)).trim(), (prefixes.get(i).getCircleID().trim()));
			}
		}
		m_circle_id = temp;
	}

	public static String getURL(String strSub)
	{
		//String _method = "getURL()";
		////logger.info("****** parameters are --
		// "+strPrefix);
		if(strSub == null || strSub.length() <= 0)
			return null;
		for(int i = 1; i <=strSub.length(); i++)
		{
			if(m_operatorPrefix.containsKey(strSub.substring(0,i)))
				return (String)m_operatorPrefix.get(strSub.substring(0,i));
		}	
		return null;
	}

	private static final String COMMON = "COMMON";
	private static final String SPECIAL_CATEGORIES = "SPECIAL_CATEGORIES";
	public static HashMap m_VcodeIDMap = new HashMap();
	private static HashMap m_ClipCategoryMap = new HashMap();
	private static String m_SpecialCategories = null;
//	private static String CLASSNAME = "ClipCacher";

	public static String checkForNull(String a){
		if(a==null||a.length()==0){
			return UNKNOWN;
		}else{
			a.trim();
			if(a.equalsIgnoreCase("null")){
				return UNKNOWN;
			}
			return a;
		}
	}

	private static String UNKNOWN = "Unknown";

	public static String getCategoryName(String strVCode, String strCategoryName){
		try{
			if(m_SpecialCategories == null)
				m_SpecialCategories =  CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,SPECIAL_CATEGORIES).getValue();
			if( m_SpecialCategories.toLowerCase().indexOf(strCategoryName.toLowerCase())>-1){
				String strID = m_VcodeIDMap.get("rbt_"+strVCode+"_rbt").toString();

				//System.out.println("strid= "+Integer.getInteger(strID));
				String strCatID = m_ClipCategoryMap.get(strID).toString();
				strCategoryName = m_categoryIdMap.get(strCatID).toString();
			}
		}catch(Exception exe){

			strCategoryName=UNKNOWN;
		}
		return strCategoryName;
	}

	public static String getBuyOption(String strCategoryName){

		String strBuyValue = "buy";
		try{
			if(strBuyMappings == null)
				strBuyMappings =  (CacheManagerUtil.getParametersCacheManager().getParameter(CCC,BUY_MAP).getValue()).toLowerCase();

			if(!(strBuyMappings.indexOf(strCategoryName.toLowerCase())>-1)){
				return strBuyValue;
			}else{
				StringTokenizer tokens = new StringTokenizer (strBuyMappings,";");
				while(tokens.hasMoreTokens()){
					String map = tokens.nextToken();
					StringTokenizer elements = new StringTokenizer (map,"=");
					String key = elements.nextToken();
					String value = elements.nextToken();
					if(key.equalsIgnoreCase(strCategoryName.toLowerCase())){
						return value;
					}
				}
			}
		}catch(Exception exe){
			//can ignore this
		}
		return strBuyValue;

	}
	public static boolean isSpecialCategory(String strCategoryName){
		try{
			if(m_SpecialCategories == null)
				m_SpecialCategories =  CacheManagerUtil.getParametersCacheManager().getParameter(COMMON,SPECIAL_CATEGORIES).getValue();
			if( m_SpecialCategories.toLowerCase().indexOf(strCategoryName.toLowerCase())>-1)
				return true;
		}catch(Exception exe){}
		return false;
	}



	public static boolean isValidVcode(String vCode){
		if(vCode!=null)
			return m_VcodeIDMap.containsKey(vCode);
		return false;
	}

}

