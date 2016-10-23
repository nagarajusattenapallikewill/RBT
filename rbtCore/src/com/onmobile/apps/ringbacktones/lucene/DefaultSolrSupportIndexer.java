package com.onmobile.apps.ringbacktones.lucene;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.index.IndexWriter;
import org.springframework.context.ApplicationContext;

import com.onmobile.apps.ringbacktones.lucene.generic.msearch.RBTMSearch;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchParams;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans.RBTMSearchResponse;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.searchimpl.RBTMSearchImpl;
import com.onmobile.apps.ringbacktones.lucene.generic.msearch.utility.SpringUtility;
import com.onmobile.apps.ringbacktones.lucene.msearch.SearchResponse;
import com.onmobile.apps.ringbacktones.lucene.solr.ConfigReader;
import com.onmobile.apps.ringbacktones.lucene.solr.SearchConstants;
import com.onmobile.apps.ringbacktones.lucene.solr.SearchResult;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;

/**
 * 
 * @author ajay.kanwal
 *	New Indexer To Support Generic Msearch from CCC IDEA
 */
public class DefaultSolrSupportIndexer extends AbstractLuceneIndexer {

	//legacy support for filter on clip/cat ids
	private static String filterCategoryIds = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_FILTER_CAT_IDS);
	private static String filterClipIds = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_FILTER_CLIP_IDS);
	
	private static String solrParamCategoryName = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_QUERY_CATEGORY);
	private static String solrParamSubCategoryName = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_QUERY_SUBCATEGORY);
	private static String solrPhonetincSupportSymbol = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_SYMBOL_PHONETIC);
	private static String useSolrPhonetincSupportSymbol = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_SYMBOL_PHONETIC_ISREQUIRED);
	private static String solrParamLanguage = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_QUERY_LANGUGAGE);
	private static String solrParamTitle = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_QUERY_TITLE);
	private static String solrParamArtist = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_QUERY_ARTIST);
	private static String solrParamalbum = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_QUERY_ALBUM);
	private static String solrParamRequestHandler = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_QUERY_REQUEST_HANDLER);
	private static String solrParamAndSymbol = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_QUERY_AND_SYMBOL);
	private static String solrParamOrSymbol = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_QUERY_OR_SYMBOL);
	private static String solrFlQueryParam = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_FL_QUERY);
	
	private static List<String> filterCategoryIdsList = new ArrayList<String>();
	private static List<String> filterClipIdsList = new ArrayList<String>();
	private static String[] searchByOptionsAvailable = {"song","artist", "album"};
	//, "vcode", "CLIP_PROMO_ID","CLIP_SMS_ALIAS"};
	static {
		if(filterCategoryIds != null && !filterCategoryIds.trim().equals(""))
			filterCategoryIdsList = Arrays.asList(filterCategoryIds.split(","));
		
		if(filterClipIds != null && !filterClipIds.trim().equals(""))
			filterClipIdsList = Arrays.asList(filterClipIds.split(","));
	
	}
	
	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo,
			int maxResults, String language, boolean isSupportPhonetic,
			boolean isUnionSearch, String queryLanguage) {
		log.info("Inside searchQuery with Params ::" +map);
		removeSpecialCharsFromSearchQuery(map);
		ArrayList<LuceneClip> luceneClipList =  new ArrayList<LuceneClip>();
		String solrSearch = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_SEARCH);
		
		log.info("Input keywords after cleaning: " + map); 
		
		if (map == null || map.size() == 0) {
			log.error("Input paramters map is null/empty");
			return luceneClipList;
		}
		
		/*
		 * orginal handling for sms alias, promo id , clip ids search
		 */
		
		if(map!=null && ((map.get(SearchConstants.FIELD_CLIP_SMS_ALIAS)!=null && !map.get(SearchConstants.FIELD_CLIP_SMS_ALIAS).equals("")) 
				|| (map.get(SearchConstants.FIELD_CLIP_PROMO_ID)!=null && !map.get(SearchConstants.FIELD_CLIP_PROMO_ID).equals("")) 
				||  (map.get(SearchConstants.FIELD_CLIP_ID)!=null && !map.get(SearchConstants.FIELD_CLIP_ID).equals("")) )){
			//boolean parentCatPresent = false;
			//String parentCatName = null;			
			Clip clip = null;
			if(map.containsKey(SearchConstants.FIELD_CLIP_SMS_ALIAS)){
				String clipSmsAlias = (String)map.get(SearchConstants.FIELD_CLIP_SMS_ALIAS);
				clip = cacheManager.getClipBySMSAlias(clipSmsAlias, language);
				if(clip == null) {
					log.info("Clip is null search by smsAlias: " + clipSmsAlias);
					return null;
				}
				map.remove(SearchConstants.FIELD_CLIP_SMS_ALIAS);
			}
			else if(map.containsKey(SearchConstants.FIELD_CLIP_PROMO_ID)){
				String clipPromoId = (String)map.get(SearchConstants.FIELD_CLIP_PROMO_ID);
				clip = cacheManager.getClipByPromoId(clipPromoId, language);
				if (clip == null) {
					log.info("Clip is null search by clipPromoId: " + clipPromoId);
					return null;
				}
				map.remove(SearchConstants.FIELD_CLIP_PROMO_ID);
			}
			else {
				String clipid = (String)map.get(SearchConstants.FIELD_CLIP_ID);
				clip = cacheManager.getClip(clipid, language);
				if (clip == null) {
					log.info("Clip is null search by clipid: " + clipid);
					return null;
				}
				map.remove(SearchConstants.FIELD_CLIP_ID);
			}
			if(clip != null){				
				if (Boolean.parseBoolean(solrSearch)) {
					map.put("clipid", clip.getClipId()+"");
				}
				else{
					map.put("clipId", clip.getClipId()+"");
				}
			
				LuceneClip luceneClip = convertToLuceneClip(clip);
				luceneClipList.add(luceneClip);
				log.info("Clip found is :: "+luceneClip);
				return luceneClipList;
			}
		}
		
		/*
		 * for NT and songs url creation
		 */
		String clipTypeFilter = (String)map.get("CLIP_TYPE_FILTER");
		String solrSearchWithCore = null;
		
		
		//for query param
		ApplicationContext context = SpringUtility.getApplicationContext();
		RBTMSearchParams rbtmSearchParams = context.getBean("rbtMSearchParams", RBTMSearchParams.class);
		String[] searchByOptions = searchByOptionsAvailable;
		String searchFilter = null;
		String searchQuery = null;
		String solrQueryField = "";
		boolean parentCategoyPresent = false;
		boolean subCatPresent = false;
		String parentCatName = (String)map.get("PARENT_CAT_NAME");
		String subCatName = (String)map.get("SUB_CAT_NAME");
		String flParamValue = "IDEARBTID";
		
		if(solrFlQueryParam!=null && !solrFlQueryParam.isEmpty()){
			flParamValue = solrFlQueryParam;
		}
		if(null != parentCatName && !parentCatName.equals("")){
			if(!"all".equalsIgnoreCase(parentCatName)){
				parentCategoyPresent = true;	
			}
		}
		if(null !=subCatName && !subCatName.equals("")){
			subCatPresent = true;
		}
		
		if(parentCategoyPresent == true){
				solrQueryField = solrParamCategoryName+":"+parentCatName;
				solrQueryField ="("+solrQueryField+")";
		}
		
		if(subCatPresent == true){
			solrQueryField = solrQueryField+ solrParamAndSymbol+ "("+solrParamSubCategoryName+":"+subCatName+")";
		}
		
		if(parentCategoyPresent == true){
			solrQueryField =  solrQueryField +solrParamAndSymbol;
		}
		//query part after AND
		for (int i = 0; i < searchByOptions.length; i++) {
			if(map.containsKey(searchByOptions[i])){
				searchFilter = searchByOptions[i];
				searchQuery = (String)map.get(searchByOptions[i]);
			break;
			}
		}
		
		if(null != searchFilter && searchFilter.equalsIgnoreCase(searchByOptionsAvailable[0])){
			if("true".equalsIgnoreCase(useSolrPhonetincSupportSymbol)){
				solrQueryField = "("+solrQueryField+ "("+solrParamTitle+":"+searchQuery + solrPhonetincSupportSymbol+"))";
			}else{
				solrQueryField = "("+solrQueryField+ "("+solrParamTitle+":"+searchQuery +"))";
			}
		}else if(null != searchFilter && searchFilter.equalsIgnoreCase(searchByOptionsAvailable[1])){
			if("true".equalsIgnoreCase(useSolrPhonetincSupportSymbol)){
				solrQueryField ="("+solrQueryField+ "("+solrParamArtist+":"+searchQuery+ solrPhonetincSupportSymbol+"))";
			}else{
				solrQueryField ="("+solrQueryField+ "("+solrParamArtist+":"+searchQuery+"))";
			}
		}else if(null != searchFilter && searchFilter.equalsIgnoreCase(searchByOptionsAvailable[2])){
			if("true".equalsIgnoreCase(useSolrPhonetincSupportSymbol)){
				solrQueryField ="("+solrQueryField+ "("+solrParamalbum+":"+searchQuery+ solrPhonetincSupportSymbol+"))";
			}else{
				solrQueryField = "("+solrQueryField+"("+solrParamalbum+":"+searchQuery+"))";
			}
		}
		log.info("query param formed :: " +solrQueryField);
		
		//for language field filter
		if(null != map.get("LANGUAGE_FILTER") && !"".equalsIgnoreCase((String)map.get("LANGUAGE_FILTER"))){
			rbtmSearchParams.setFq(solrParamLanguage+":"+map.get("LANGUAGE_FILTER"));
			log.info("language filter:: "+rbtmSearchParams.getFq());
		}
		
		//for clip type filter core api will change
				if(null!=clipTypeFilter && clipTypeFilter.equalsIgnoreCase(SearchConstants.GENERIC_MSEARCH_CLIP_FILTER_SONG)){
					if(parentCategoyPresent == true){
						solrSearchWithCore = ConfigReader.getInstance().getParameter(SearchConstants.GENERIC_MSEARCH_CORE_SONG_WITH_CAT);
					}else{
						solrSearchWithCore = ConfigReader.getInstance().getParameter(SearchConstants.GENERIC_MSEARCH_CORE_SONG);
					}
				}else if(null!=clipTypeFilter && clipTypeFilter.equalsIgnoreCase(SearchConstants.GENERIC_MSEARCH_CLIP_FILTER_NAME_TUNES)){
					if(parentCategoyPresent == true){
						solrSearchWithCore = ConfigReader.getInstance().getParameter(SearchConstants.GENERIC_MSEARCH_CORE_NAME_TUNES_WITH_CAT);
					}else{
						solrSearchWithCore = ConfigReader.getInstance().getParameter(SearchConstants.GENERIC_MSEARCH_CORE_NAME_TUNES);
					}
				}
				String url = solrSearchWithCore;
				log.info("solr url with core ::" +url);
				

		rbtmSearchParams.setRows(""+maxResults);
		rbtmSearchParams.setQ(solrQueryField);
		//solrFlQueryParam="IDEARBTID"
		rbtmSearchParams.setFl(flParamValue);//is in sync with the bean in responseHandlerImpl. will remain same for idea
		rbtmSearchParams.setWt("xml");
		rbtmSearchParams.setSort("DOWNLOADCOUNT desc");
		rbtmSearchParams.setQt(solrParamRequestHandler);
		
		RBTMSearch rbtmSearch = context.getBean("rbtMSearchImpl",RBTMSearchImpl.class);
		List<RBTMSearchResponse> rbtmSearchResponses = rbtmSearch.searchClip(rbtmSearchParams, url);
		log.info("size for result set :: "+rbtmSearchResponses.size());
		for (Iterator iterator = rbtmSearchResponses.iterator(); iterator
				.hasNext();) {
			RBTMSearchResponse rbtmSearchResponse = (RBTMSearchResponse) iterator
					.next();
			log.info("List for result set :: "+rbtmSearchResponse);
			
		}
		// convert to luceneClips
		if (rbtmSearchResponses != null && rbtmSearchResponses.size() > 0) {
	    	
	    	log.info("Got search results of size " + rbtmSearchResponses.size());
	    	LuceneClip tempLuceneClip=null;
	    	for(RBTMSearchResponse record : rbtmSearchResponses){
				String clipId = record.getIdeaRBTId();
				Clip clip = cacheManager.getClip(clipId);
				//String parentCatId = clip.tgetParentCategoryId();
				//String subCatId = clip.getSubCategoryId();
				
				//Filtering clipid				
				if(filterClipIdsList.contains(clipId)) {
					log.info("As clipid ::"+clipId+" is blocked, not adding to list" );
					continue;
				}
				 tempLuceneClip = convertToLuceneClip(clip);
				if(null!=tempLuceneClip){
					log.info("PATENT CAT ID :: "+ tempLuceneClip.getParentCategoryId() +", CAT NAME ::  "+ tempLuceneClip.getParentCategoryName() +" SUBCATID :: "+tempLuceneClip.getSubCategoryId() + " SUBNAME :: "+tempLuceneClip.getSubCategoryName());
					luceneClipList.add(tempLuceneClip);
				}else{
					log.error("No Mapping for clipid:: "+clipId);
				}
				
	    	}
			
		}else{
			log.error("No results found");
		}	
		
		if (luceneClipList.size() > pageNo * maxResults) {
			int next = (pageNo + 1) * maxResults;
			int max = (luceneClipList.size() >= next) ? next : luceneClipList.size();
			luceneClipList = new ArrayList<LuceneClip>(luceneClipList.subList(pageNo * maxResults, max));
		}
		
		return luceneClipList;
	}

	private LuceneClip convertToLuceneClip(Clip clip) {
		
		LuceneClip tempLuceneClip = new LuceneClip();
		if(null == clip){
			return null;
		}
		Long dummyCatId=3L;
		try {
			dummyCatId = Long.parseLong(ConfigReader.getInstance().getParameter(SearchConstants.PARAM_DUMMY_CATEGORY_ID));
			log.info("getting dummy_category_id: "+dummyCatId);
			
		}catch(Exception e) {
			log.info("Exception while getting dummy_category_id."+e);
		}
		String vcode=clip.getClipRbtWavFile();
		vcode=vcode.replaceAll("rbt", "");
		vcode=vcode.replaceAll("_", "");
		tempLuceneClip.setVcode(vcode);
		tempLuceneClip.setClipId(clip.getClipId());
		tempLuceneClip.setClipName(clip.getClipName());
		tempLuceneClip.setAlbum(clip.getAlbum());
		tempLuceneClip.setArtist(clip.getArtist());
		tempLuceneClip.setClipSmsAlias(clip.getClipSmsAlias());
		tempLuceneClip.setLanguage(clip.getLanguage());
		tempLuceneClip.setClipGrammar(clip.getClipGrammar());
		tempLuceneClip.setClipInfo(clip.getClipInfo());
		tempLuceneClip.setClassType(clip.getClassType());
		tempLuceneClip.setClipStartTime(clip.getClipStartTime());
		tempLuceneClip.setClipEndTime(clip.getClipEndTime());
		tempLuceneClip.setClipPromoId(clip.getClipPromoId());
		tempLuceneClip.setClipPreviewWavFile(clip.getClipPreviewWavFile());
		tempLuceneClip.setClipRbtWavFile(clip.getClipRbtWavFile());
		tempLuceneClip.setSmsStartTime(clip.getSmsStartTime());
		tempLuceneClip.setAddToAccessTable(clip.getAddToAccessTable());
		tempLuceneClip.setClipDemoWavFile(clip.getClipDemoWavFile());
		tempLuceneClip.setParentCategoryId(dummyCatId);
		return tempLuceneClip;
	}

	@Override
	public ArrayList<LuceneClip> multiFeildmsearch(HashMap map, int pageNo,
			int maxResults, String language) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo,
			int maxResults) {
		return searchQuery(map, pageNo, maxResults, true);
	}

	@Override
	public ArrayList<LuceneClip> searchByNametune(HashMap map, int pageNo,
			int maxResults, String language, String queryLanguage) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo,
			int maxResults, String language, String queryLanguage) {
		return searchQuery(map, pageNo, maxResults, language, true, false, queryLanguage);
	}

	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo,
			int maxResults, boolean isSupportPhonetic) {
		return searchQuery(map, pageNo, maxResults, null, isSupportPhonetic);
	}

	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo,
			int maxResults, String language, boolean isSupportPhonetic) {
		return searchQuery(map, pageNo, maxResults, language, isSupportPhonetic, false);
	}

	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo,
			int maxResults, String language, boolean isSupportPhonetic,
			boolean isUnionSearch) {
		return searchQuery(map, pageNo, maxResults, language, isSupportPhonetic, isUnionSearch, null);
	}

	private static void removeSpecialCharsFromSearchQuery(Map<String, String> map) {
		Map<String, String> paramHashMap = ConfigReader.getInstance()
				.getParameters();
		if (!paramHashMap
				.containsKey(SearchConstants.REMOVE_SPECIAL_CHARS_FROM_SEARCH_QUERY)) {
			log.info("Param not configure in solr config:"
					+ SearchConstants.REMOVE_SPECIAL_CHARS_FROM_SEARCH_QUERY);
			return;
		}
		String specialChars = paramHashMap
				.get(SearchConstants.REMOVE_SPECIAL_CHARS_FROM_SEARCH_QUERY);
		Iterator<Entry<String, String>> itr = map.entrySet().iterator();
		while (itr.hasNext()) {
			Entry<String, String> entry = itr.next();
			String key = entry.getKey();
			String val = entry.getValue();
			if (null == val) {
				continue;
			}
			for (int i = 0; i < specialChars.length(); i++) {
				char charAt = specialChars.charAt(i);
				while (val.indexOf(charAt) != -1) {
					String one = val.substring(0, val.indexOf(charAt));
					String two = val.substring(val.indexOf(charAt) + 1);
					val = one + two;
				}
			}
			map.put(key, val);
		}
	}

	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo,
			int maxResults, String language) {
		return searchQuery(map, pageNo, maxResults, language, true);
	}

	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map,
			int pageNo, int maxResults) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map,
			int pageNo, int maxResults, boolean isSupportPhonetic) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map,
			int pageNo, int maxResults, String language) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map,
			int pageNo, int maxResults, String language,
			boolean isSupportPhonetic) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map,
			int pageNo, int maxResults, String language,
			boolean isSupportPhonetic, String queryLanguage) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public ArrayList<Category> searchCategoryAlphabetically(String alphabet,
			int pageNo, int maxResults, String circle, String language,
			int categoryId) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public int getTotalSearchSize() {
		return 0;
	}

	@Override
	public SearchResponse searchCategory(String searchText, int noOfRows) {
		 throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public List<String> getSuggestions(String search, int rows) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public ArrayList<String> searchForArtists(String artist, int noOfRows) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public ArrayList<LuceneClip> searchForArtistSongs(String artist,
			int noOfRows) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public int getTotalArtistResults() {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	public int getTotalArtistSongResults() {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
	}

	@Override
	protected void getContentAndIndex(IndexWriter iw, String language) {
		throw new UnsupportedOperationException("THIS METHOD IS NOT IMPLEMENTED.");
		
	}
}
