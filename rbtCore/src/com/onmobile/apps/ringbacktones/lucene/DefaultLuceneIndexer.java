package com.onmobile.apps.ringbacktones.lucene;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.jboss.mx.logging.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.lucene.msearch.MSearch;
import com.onmobile.apps.ringbacktones.lucene.msearch.SearchResponse;
import com.onmobile.apps.ringbacktones.lucene.solr.ConfigReader;
import com.onmobile.apps.ringbacktones.lucene.solr.SearchClientFactory;
import com.onmobile.apps.ringbacktones.lucene.solr.SearchConstants;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;

public class DefaultLuceneIndexer extends AbstractLuceneIndexer{
	protected static String[] fields = {"clipId","vcode","PARENT_CAT_NAME","SUB_CAT_NAME","song","album","artist", "parentCatId", "subCatId", "SUB_CAT_TYPE", "CAT_PROMO_ID"};
	
	private static String filterCategoryIds = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_FILTER_CAT_IDS);
	private static String filterClipIds = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_FILTER_CLIP_IDS);
	private static List<String> filterCategoryIdsList = new ArrayList<String>();
	private static List<String> filterClipIdsList = new ArrayList<String>();
	
	static {
		if(filterCategoryIds != null && !filterCategoryIds.trim().equals(""))
			filterCategoryIdsList = Arrays.asList(filterCategoryIds.split(","));
		
		if(filterClipIds != null && !filterClipIds.trim().equals(""))
			filterClipIdsList = Arrays.asList(filterClipIds.split(","));
	}
	
	public DefaultLuceneIndexer() throws Exception{}
	
	@Override
	protected void getContentAndIndex(IndexWriter iw, String language) {
		log.info("getContentAndIndex:: Language is "+language);
		// should be a key of clipId_categoryId_subCatID against the clipId
		Map<String, String> categoryCircleMap=new HashMap<String, String>();
		try{
			//--------------- Set the index factors
			iw.setMergeFactor(10000);
			iw.setMaxMergeDocs(9999999);
			iw.setMaxBufferedDocs(10000);
			ApplicationDetailsRequest applicationRequest=new ApplicationDetailsRequest();
			Site sites[]=rbtClient.getSites(applicationRequest);
			log.info("Site count "+sites.length);
			for(int siteCount=0;siteCount<sites.length;siteCount++){
				String circleId=sites[siteCount].getCircleID();
				Category[] parentCategories=null;
				char prepaidYes='b';
				if(cacheManager.getActiveCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, prepaidYes)!=null && cacheManager.getActiveCategoriesInCircle(circleId,PARENT_CATEGORY_ZERO,prepaidYes).length>0){
					parentCategories=cacheManager.getActiveCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, prepaidYes, null, language);
				}else if(cacheManager.getActiveCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, 'y')!=null && cacheManager.getActiveCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, 'y').length>0){
					prepaidYes='y';
					parentCategories=cacheManager.getActiveCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, prepaidYes, null, language);
				}
				if(parentCategories == null || parentCategories.length <= 0){
					log.info("In getContentAndIndex(): The parentCategories are null for circle "+circleId);
					continue;
				}
				for(int i=0; i< parentCategories.length; i++){
					log.info("Looping parentCategories "+parentCategories[i]);
					//Check for clips in the parent categories
					getClipsAndIndex(parentCategories[i],categoryCircleMap,fields,iw,true,parentCategories[i], language);
					//get the sub categories
					Category[] subCategories = cacheManager.getActiveCategoriesInCircle(circleId, parentCategories[i].getCategoryId(), prepaidYes, null, language);
					if(subCategories == null || subCategories.length <= 0){
						log.info("No sub categories found under category "+parentCategories[i].getCategoryId()+" circleId "+circleId+" " );
						continue;
					}
					for(int j=0; j< subCategories.length; j++){
						if(subCategories[j]!=null){
							getClipsAndIndex(subCategories[j],categoryCircleMap,fields,iw, false, parentCategories[i], language);
							int subCatId=subCategories[j].getCategoryId();
							
							// Now try and get the categories under sub categories if any and index the clips in those categories.
							Category[] subSubCategories = cacheManager.getActiveCategoriesInCircle(circleId, subCatId, prepaidYes, null, language);
							if(subSubCategories == null || subSubCategories.length <= 0){
								log.info("No sub categories found under sub category "+subCatId+" circleId "+circleId+" " );
								continue;
							}
							for(int h=0; h< subSubCategories.length; h++){
								if(subSubCategories[h]!=null)
									getClipsAndIndex(subSubCategories[h],categoryCircleMap,fields,iw, false, subCategories[j], language);
							}
						}
					}
				}
			}
			log.info("GuiSearch::Imformation  caching done Number of documents indexed = "+iw.docCount());
			//------------ Optimize the index after creating the index. 
//			optimizeIndexWriter(iw);
		}
		catch (Exception e2){
			log.error(e2.getMessage());
			e2.printStackTrace();
		}
		finally {
			//------------ Optimize the index after creating the index. 
			optimizeIndexWriter(iw);
		}
		
	}
    
	@Override
	public int getTotalSearchSize() {
		
		return totalResults;
	}

	@Override
	public int getTotalArtistResults(){
		return totalArtistResults;
	}
	
	@Override
	public int getTotalArtistSongResults() {
		return totalArtistSongResults;
	}
	
	
	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map, int pageNo, int maxResults, String language) {
		return searchCategoryQuery(map, pageNo, maxResults, language, true);
	}
	
	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map, int pageNo, int maxResults, String language, boolean isSupportPhonetic) {
		return searchCategoryQuery(map, pageNo, maxResults, language, isSupportPhonetic, null);
	}
	
	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map, int pageNo, int maxResults, String language, boolean isSupportPhonetic, String queryLanguage) {
		
		String solrSearch = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_SEARCH);
		String operator = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_OPERATOR);
		String mSearch = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_MSEARCH_SEARCH);
		if(mSearch==null || !Boolean.parseBoolean(mSearch)){
			  map.remove("SUBSCRIBER_ID");
		}
		if(map!=null && map.size()<=2 && map.get(SearchConstants.FIELD_CAT_SMS_ALIAS)!=null && !map.get(SearchConstants.FIELD_CAT_SMS_ALIAS).equals("")){
			boolean parentCatPresent = false;
			String parentCatName = null;
			String catSmsAlias = (String)map.get(SearchConstants.FIELD_CAT_SMS_ALIAS);
			Category albumCategory = cacheManager.getCategoryBySMSAlias(catSmsAlias);
			String categoryName = "";
			if (albumCategory != null) {
				categoryName = albumCategory.getCategoryName();			
			}
			else {
				log.info("Category is null, search by smsAlias: " + catSmsAlias);
				return null;
			}
			if(map.get(SearchConstants.FIELD_PARENT_CAT_NAME)!=null && !map.get(SearchConstants.FIELD_PARENT_CAT_NAME).equals("")){
				parentCatPresent = true;
				parentCatName = (String)map.get(SearchConstants.FIELD_PARENT_CAT_NAME);
			}
			map = new HashMap();
			map.put(SearchConstants.FIELD_SUB_CAT_NAME, categoryName);
			if(parentCatPresent)
				map.put(SearchConstants.FIELD_PARENT_CAT_NAME, parentCatName);
		}
		if (Boolean.parseBoolean(solrSearch)) {
			log.info("Going to search in solr.");
			// get search results
			ArrayList<LuceneCategory> luceneCatList = SearchClientFactory.getSearchClient(operator).searchCategory(map, language, queryLanguage);
			totalResults = luceneCatList.size();
			log.info("Solr result Size : " + totalResults);
			log.info("Page No : " + pageNo + " MaxResults : " + totalResults);
			// pagenation logic
			if (luceneCatList.size() > pageNo * maxResults) {
				int next = (pageNo + 1) * maxResults;
				int max = (luceneCatList.size() >= next) ? next : luceneCatList.size();
				luceneCatList = new ArrayList<LuceneCategory>(luceneCatList.subList(pageNo * maxResults, max));
			} else {
				luceneCatList = new ArrayList<LuceneCategory>();
			}
			log.info("LuceneCatList size : " + luceneCatList.size());
			return luceneCatList;
		}
		
		String promoId =  null;
		if(language==null || language.equals(""))
			language = defaultLanguage;
		if(map==null && map.size()==0)
			return null;
		//------------------------- If only cat promo id is passed get it from memcached
		if(map!=null && map.size()<=2 && map.get("CAT_PROMO_ID")!=null && !map.get("CAT_PROMO_ID").equals("")){
			boolean parentCatPresent = false;
			String parentCatName = null;
			promoId = (String)map.get("CAT_PROMO_ID");
			Category albumCategory = cacheManager.getCategoryByPromoId(promoId);
			String categoryName = albumCategory.getCategoryName();
			if(map.get("PARENT_CAT_NAME")!=null && !map.get("PARENT_CAT_NAME").equals("")){
				parentCatPresent = true;
				parentCatName = (String)map.get("PARENT_CAT_NAME");
			}
			map = new HashMap();
			map.put("SUB_CAT_NAME", categoryName);
			if(parentCatPresent)
				map.put("PARENT_CAT_NAME", parentCatName);
		}
		
		int offset=(pageNo*maxResults)+1;
		if(pageNo==0)
			offset = 0;
		ArrayList<LuceneCategory> luceneCategoryList = null;
		StringBuffer queryBuffer = new StringBuffer(); 
		Iterator itr = map.entrySet().iterator();
		while(itr.hasNext()){
			final Entry entry = (Entry) itr.next();
	        final String key = entry.getKey().toString();
	        String value = (String) entry.getValue();
	        
	        if(key.equalsIgnoreCase("SUB_CAT_NAME")){
	        	if(value.length()==1){
	        		value=value.trim()+"*";
	        	}else{
	        		value = getQueryString(value, isSupportPhonetic);
	        	}
	        	if(queryLanguage == null)
	        		queryLanguage = identifyLanguage(value);
        	}
	        
	        for(int i=0; i<fields.length; i++){
	        	if(key.equalsIgnoreCase(fields[i])){
	        		queryBuffer.append(fields[i]+":"+value+" AND ");
		        }
	        }
		}
		if (queryLanguage == null || queryLanguage.equals("")) {
			queryLanguage = defaultLanguage;
		}
	        String query = queryBuffer.toString();
	        query = query.trim();
	        if(query.endsWith("AND")){
	        	int indexOfAnd = query.lastIndexOf("AND");
	        	query = query.substring(0, indexOfAnd);
	        }
	        IndexSearcher searcher = null;
	        try{
	        	log.info("The query for search category is "+query);
	        	if(query.contains("OR OR"))
	        		query = query.replaceAll("OR OR", "OR");
	        	String indexPath = getIndexPath(queryLanguage);
	        	searcher = new IndexSearcher(indexPath);
	        	Analyzer analyzer = null;
				try{
					analyzer = getAnalyzer(queryLanguage);
				}
				catch(RBTLuceneException rbtle){
					log.error(rbtle.getMessage());
					return null;
				}
				QueryParser qParser = new QueryParser("SUB_CAT_NAME",analyzer);
				Query lucQuery = qParser.parse(query);
				BooleanQuery.setMaxClauseCount(5000);
				TopDocs topDocs = searcher.search(lucQuery, new QueryFilter(lucQuery), 200);
				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
				if(scoreDocs!=null && scoreDocs.length>0){
					log.info("Score docs are "+scoreDocs.length);
					luceneCategoryList = new ArrayList<LuceneCategory>();
					for(int j=0;j<scoreDocs.length;j++){

						Document document = searcher.doc(scoreDocs[j].doc);
						String parentcatId = document.get("parentCatId");
						String subCatId = document.get("subCatId");
						
						//Filtering clipid and category id				
						if(filterCategoryIdsList.contains(subCatId)) {
							continue;
						}
						
						if(subCatId!=null){
							Category subCategory = cacheManager.getCategory(Integer.parseInt(subCatId));
							//--------------- Show only the shuffle categories
							if (subCategory.getCategoryTpe() == iRBTConstant.SHUFFLE || subCategory.getCategoryTpe() == iRBTConstant.ODA_SHUFFLE || 
									subCategory.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
								Category parentCat =  null;
								//------------- If the parent category does not match the search criteria for parent cat passed do not display those categories
								if(parentcatId!=null){
									parentCat = cacheManager.getCategory(Integer.parseInt(parentcatId));
									if(map.get("PARENT_CAT_NAME")!=null && !(map.get("PARENT_CAT_NAME").equals("")) && !(parentCat.getCategoryName().equalsIgnoreCase((String)map.get("PARENT_CAT_NAME")))){
										continue;
									}
								}
								//---- If the promo id does not match the passed promo id do not show those categories
								if(promoId!=null && !(subCategory.getCategoryPromoId().equals(promoId))){
									continue;
								}
								LuceneCategory lucCategory = new LuceneCategory();
								lucCategory.setCategoryId(subCategory.getCategoryId());
								lucCategory.setCategoryName(subCategory.getCategoryName());
								lucCategory.setCategoryAskMobileNumber(subCategory.getCategoryAskMobileNumber());
								lucCategory.setCategoryEndTime(subCategory.getCategoryEndTime());
								lucCategory.setClassType(subCategory.getClassType());
								lucCategory.setCategoryGrammar(subCategory.getCategoryGrammar());
								lucCategory.setCategoryGreeting(subCategory.getCategoryGreeting());
								lucCategory.setCategoryNameWavFile(subCategory.getCategoryNameWavFile());
								lucCategory.setCategoryPreviewWavFile(subCategory.getCategoryPreviewWavFile());
								lucCategory.setCategoryPromoId(subCategory.getCategoryPromoId());
								lucCategory.setCategorySmsAlias(subCategory.getCategorySmsAlias());
								lucCategory.setCategoryStartTime(subCategory.getCategoryStartTime());
								lucCategory.setCategoryTpe(subCategory.getCategoryTpe());
								lucCategory.setMmNumber(subCategory.getMmNumber());
								if(parentcatId!=null){
									lucCategory.setParentCategoryId(Integer.parseInt(parentcatId));
									lucCategory.setParentCategoryName(parentCat.getCategoryName());
								}
								
								//------------- Check for duplicate entries.
								if(!luceneCategoryList.contains(lucCategory)){
								luceneCategoryList.add(lucCategory);
								}
							}
							
						}
					}
				}
//				searcher.close();
	        }
	        catch(ParseException pe){
	        	pe.printStackTrace();
	        }
		    catch(IOException ioe){
		    	ioe.printStackTrace();
		    }
		    finally{
		    	try{
		    		if(searcher != null)
		    			searcher.close();
		    	}
		    	catch(Throwable t) {}
		    }
	        
		//-------- Create a subset from the passed page number
		if(luceneCategoryList!=null && luceneCategoryList.size()>0){
			ArrayList<LuceneCategory> origList = new ArrayList<LuceneCategory>(luceneCategoryList);
			ArrayList<LuceneCategory> subList= new ArrayList<LuceneCategory>(maxResults);
			for(int i=offset;i<origList.size() && i<(maxResults+offset); i++) {
				subList.add(origList.get(i));
			}
			return subList;
		}
			
		else{
			return null;
		}
	}

	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map, int pageNo, int maxResults, boolean isSupportPhonetic) {
		return searchCategoryQuery(map, pageNo, maxResults, null, isSupportPhonetic);
	}

	@Override
	public ArrayList<LuceneCategory> searchCategoryQuery(HashMap map, int pageNo, int maxResults) {
		return searchCategoryQuery(map, pageNo, maxResults, true);
	}
	
	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, String language) {
		return searchQuery(map, pageNo, maxResults, language, true);
	}

	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, String language, boolean isSupportPhonetic) {
		return searchQuery(map, pageNo, maxResults, language, isSupportPhonetic, false);
	}
	
	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, String language,
			boolean isSupportPhonetic, boolean isUnionSearch) {
		return searchQuery(map, pageNo, maxResults, language, isSupportPhonetic, isUnionSearch, null);
		
	}

	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, String language,
			boolean isSupportPhonetic, boolean isUnionSearch, String queryLanguage) {
		
		
		String tempLanguage = getLanguage(language);
//		String tempQueryLanguage = identifyLanguage(queryLanguage);
		String tempQueryLanguage = queryLanguage;
		
		String mSearch = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_MSEARCH_SEARCH);
		String solrSearch = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_SOLR_SEARCH);
		String operator = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_OPERATOR);
		
		removeSpecialCharsFromSearchQuery(map);
		
		if(map!=null && ((map.get(SearchConstants.FIELD_CLIP_SMS_ALIAS)!=null && !map.get(SearchConstants.FIELD_CLIP_SMS_ALIAS).equals("")) 
				|| (map.get(SearchConstants.FIELD_CLIP_PROMO_ID)!=null && !map.get(SearchConstants.FIELD_CLIP_PROMO_ID).equals("")) 
				||  (map.get(SearchConstants.FIELD_CLIP_ID)!=null && !map.get(SearchConstants.FIELD_CLIP_ID).equals("")) )){
			boolean parentCatPresent = false;
			String parentCatName = null;			
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
			}
		}
		if(mSearch==null || !Boolean.parseBoolean(mSearch)){
			  map.remove("SUBSCRIBER_ID");
		}
		if (Boolean.parseBoolean(solrSearch)) {
			log.info("Going to search in solr.");
			// get search results
			ArrayList<LuceneClip> luceneClipList = SearchClientFactory.getSearchClient(operator).searchClip(map, language, queryLanguage);
			totalResults = luceneClipList.size();
			// pagenation logic 
			if (luceneClipList.size() > pageNo * maxResults) {
				int next = (pageNo + 1) * maxResults;
				int max = (luceneClipList.size() >= next) ? next : luceneClipList.size();
				luceneClipList = new ArrayList<LuceneClip>(luceneClipList.subList(pageNo * maxResults, max));
			} else {
				luceneClipList = new ArrayList<LuceneClip>();
			}
			return luceneClipList;
		}else if(mSearch!=null && Boolean.parseBoolean(mSearch)){
			log.info("Going to search in solr.");
			MSearch ms = MSearch.getInstance();
			ArrayList<LuceneClip> luceneClipList = ms.searchClip(map, tempLanguage, tempQueryLanguage);
			totalResults = luceneClipList.size();
			// pagenation logic 
			if (luceneClipList.size() > pageNo * maxResults) {
				int next = (pageNo + 1) * maxResults;
				int max = (luceneClipList.size() >= next) ? next : luceneClipList.size();
				luceneClipList = new ArrayList<LuceneClip>(luceneClipList.subList(pageNo * maxResults, max));
			} else {
				luceneClipList = new ArrayList<LuceneClip>();
			}
			return luceneClipList;
		}
		
		return searchInDefaultLucene(map, pageNo, maxResults, language, isSupportPhonetic, isUnionSearch, queryLanguage);
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
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults) {
		return searchQuery(map, pageNo, maxResults, true);
	}
	
	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, String language, String queryLanguage) {
		return searchQuery(map, pageNo, maxResults, language, true, false, queryLanguage);
	}
	
	@Override
	public ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, boolean isSupportPhonetic) {
		return searchQuery(map, pageNo, maxResults, null, isSupportPhonetic);
	}
	
	private void getClipsAndIndex(Category category, Map<String, String>categoryCircleMap, 
		String[] fields, IndexWriter iw, boolean parentYes, Category parentCategory, String language){
		//if we are to get the clips inside the parent cat then the key would be clipId_parentCatId_null
		// If we are to get the clips inside the sub cat then the key would be clipId_parentCatId_subCatId
		int subCatId=category.getCategoryId();
		String key=null;
		if(parentYes){
			key="_"+parentCategory.getCategoryId()+"_null";
		}
		else{
			key="_"+parentCategory.getCategoryId()+"_"+subCatId;
		}
		String subCategoryName=category.getCategoryName();
		Clip[] clipsInCat=cacheManager.getActiveClipsInCategory(subCatId, language);
		if(clipsInCat==null)
			log.info("No clips in  category "+subCatId+" lang "+language);
		else
			log.info("Got the clips in  category "+subCatId+" lang "+language+"  and the count is "+clipsInCat.length);	
		if(clipsInCat!=null && clipsInCat.length>0){
			for(int h=0;h<clipsInCat.length;h++){
				if(clipsInCat[h]!=null ){
					if(categoryCircleMap.get(clipsInCat[h].getClipId()+key)==null){
						categoryCircleMap.put(clipsInCat[h].getClipId()+key,Integer.toString(clipsInCat[h].getClipId()));
						String wavFile = clipsInCat[h].getClipRbtWavFile();
						String clipName = clipsInCat[h].getClipName();
						String album = clipsInCat[h].getAlbum();
						String singer = clipsInCat[h].getArtist();
						int clipID = clipsInCat[h].getClipId();
						String[] values=new String[11];
						values[0]=clipID+"";
						String vcode = wavFile;
						if(wavFile.contains("rbt")){
							vcode=vcode.replaceAll("rbt", "");
							vcode=vcode.replaceAll("_", "");
						}
						values[1]=vcode;
						values[2]=parentCategory.getCategoryName();
						values[4]=clipName;
						values[5]=album;
						values[6]=singer;
						values[7]=parentCategory.getCategoryId()+"";
						if(parentYes){
							values[3]=parentCategory.getCategoryName();
							values[8]=parentCategory.getCategoryId()+"";
							values[9] = parentCategory.getCategoryTpe()+"";
							values[10] = parentCategory.getCategoryPromoId();
						}else{
							values[3]=subCategoryName;
							values[8]=subCatId+"";
							values[9] = category.getCategoryTpe()+"";
							values[10] = category.getCategoryPromoId();
						}
						createModifyDocuments(iw, fields, values);
					}
					
				}else{
					log.info("Clips null for category "+subCatId+" lang "+language+"  and the count is "+clipsInCat.length);
				}
			}
		}
	}
	
	private Map<String, Category> getLocalCategoryMap(Category categories[]){
		Map<String, Category> categoryMap = null;
		if(categories!=null && categories.length>0){
			categoryMap = new HashMap<String, Category>();
			for(int i=0;i<categories.length;i++){
				categoryMap.put(categories[i].getCategoryId()+"",categories[i]);
			}
		}
		return categoryMap;
	}


	public ArrayList<Category> searchCategoryAlphabetically(String alphabet, int pageNo, 
			int maxResults, String circle, String language, int parentCatId){
		ArrayList<Category> categoryList = null;
		if(alphabet.length()==0 || alphabet.length()>1)
			return null;
		//----------Converting to upper case since map created with upper case letters
		alphabet = alphabet.trim().toUpperCase();
		int passedAlphabetUpper = (int)alphabet.charAt(0);
		int passedAlphabetLower = (int)alphabet.trim().toLowerCase().charAt(0);
		log.info("IN searchCategoryAlphabetically:: uppper ascii "+passedAlphabetUpper+" Lower ascii "+passedAlphabetLower);
		//------------- If there are no parent categories inside the alpabetMap
		if(alphabetMap.get(passedAlphabetUpper)==null){
			Map<String, ArrayList<Category>> parentMap = new HashMap<String, ArrayList<Category>>();
			//----------- Getting the categories in the language passed
			Category[] wholeSubCategories = cacheManager.getActiveCategoriesInCircle(circle, parentCatId,'b',language);
			if(wholeSubCategories!=null && wholeSubCategories.length>0)
				log.info("IN searchCategoryAlphabetically:: Got active categories for parentCat"+parentCatId+" the size is "+wholeSubCategories.length);
			ArrayList<Category> alphabetCategoryList = new ArrayList<Category>();
			//----------------- Iterating the whole sub categories and segragating the alphabet specific categories, lower as well as upper cases
			for(int i=0;i<wholeSubCategories.length;i++){
				if(wholeSubCategories[i]!=null){
					int firstLetterOfSubCategory = (int)wholeSubCategories[i].getCategoryName().charAt(0);
					
					//Filtering clipid and category id				
					if(filterCategoryIdsList.contains(wholeSubCategories[i].getCategoryId() + "")) {
						continue;
					}
					
					if(passedAlphabetLower==firstLetterOfSubCategory || passedAlphabetUpper==firstLetterOfSubCategory){
						alphabetCategoryList.add(wholeSubCategories[i]);
						parentMap.put(parentCatId+"_"+language, alphabetCategoryList);
					}
				}
			}
			alphabetMap.put(passedAlphabetUpper, parentMap);
			categoryList = alphabetCategoryList;
		}
		//---------------if there are parentCategories inside the alphabet map
		else{
			//----------- If the parent cat passed is present inside the map
			if((alphabetMap.get(passedAlphabetUpper)!=null && alphabetMap.get(passedAlphabetUpper).get(parentCatId+"_"+language)!=null) 
					|| (alphabetMap.get(passedAlphabetLower)!=null && alphabetMap.get(passedAlphabetLower).get(parentCatId+"_"+language)!=null)){
				if(alphabetMap.get(passedAlphabetUpper)!=null && alphabetMap.get(passedAlphabetUpper).get(parentCatId+"_"+language)!=null)
					categoryList = alphabetMap.get(passedAlphabetUpper).get(parentCatId+"_"+language);
				else
					categoryList = alphabetMap.get(passedAlphabetLower).get(parentCatId+"_"+language);
			}
			//----------If the parentCat is not present inside the alphabet map
			else{
				Map<String, ArrayList<Category>> parentMap = alphabetMap.get(passedAlphabetUpper);
				Category[] wholeSubCategories = cacheManager.getActiveCategoriesInCircle(circle, parentCatId, 'b', language);
				ArrayList<Category> alphabetCategoryList = new ArrayList<Category>();
				for(int i=0;i<wholeSubCategories.length;i++){
					if(wholeSubCategories[i]!=null){
						
						//Filtering clipid and category id				
						if(filterCategoryIdsList.contains(wholeSubCategories[i].getCategoryId() + "")) {
							continue;
						}
						
						int firstLetterOfSubCategory = (int)wholeSubCategories[i].getCategoryName().charAt(0);
						if(passedAlphabetLower==firstLetterOfSubCategory || passedAlphabetUpper==firstLetterOfSubCategory){
							alphabetCategoryList.add(wholeSubCategories[i]);
							parentMap.put(parentCatId+"_"+language, alphabetCategoryList);
						}
					}
				}
				alphabetMap.put(passedAlphabetUpper, parentMap);
				categoryList = alphabetCategoryList;
			}
		}
		//------------------------ creating a sub set of the categories for page number passed and the max results passed
		// pagination value gets by multiplying page number and max results.
		int offset=(pageNo*maxResults);
		if(pageNo==0)
			offset = 0;
		//-------- Create a subset from the passed page number

		if(categoryList!=null && categoryList.size()>0){
			this.totalResults = categoryList.size();
			ArrayList<Category> origList = new ArrayList<Category>(categoryList);
			ArrayList<Category> subList= new ArrayList<Category>(maxResults);
			for(int i=offset;i<origList.size() && i<(maxResults+offset); i++) {
				subList.add(origList.get(i));
			}
			return subList;
		}
		
		return categoryList;
	}
	
	
	private ArrayList<LuceneClip> searchInDefaultLucene(HashMap map, int pageNo, int maxResults, String language,
			boolean isSupportPhonetic, boolean isUnionSearch, String queryLanguage) {
		log.info("Inside search query ");
		if(language==null || language.equals(""))
			language = defaultLanguage;
		if(map==null && map.size()==0)
			return null;
		int offset = pageNo * maxResults;
		ArrayList<LuceneClip> luceneClipList = null;
		StringBuffer queryBuffer = new StringBuffer(); 
		Iterator itr = map.entrySet().iterator();
		String[] luceneFields = new String[map.size()];
		int countParameters = 0;
		while(itr.hasNext()){
			final Entry entry = (Entry) itr.next();
	        final String key = entry.getKey().toString();
			luceneFields[countParameters] = key;
	        String value = (String) entry.getValue();
	        //---------- insert a tilde for terms like the input passed
	        if(key.equalsIgnoreCase("clipId") || key.equalsIgnoreCase("song") || key.equalsIgnoreCase("artist") || key.equalsIgnoreCase("album")){
        		//value = value+"~";
				if(value.contains("&"))
					value = value.replaceAll("&"," ");
	        	value = getQueryString(value, isSupportPhonetic);
	        	if(queryLanguage == null)
	        		queryLanguage = identifyLanguage(value);
        	}
	        
	        for(int i=0; i<fields.length; i++){
	        	if(key.equalsIgnoreCase(fields[i])){
	        		if (isUnionSearch) {
	        			queryBuffer.append("("+fields[i]+":"+value+") OR ");
	        		} else {
	        			queryBuffer.append("("+fields[i]+":"+value+") AND ");
	        		}
		        }
	        }
			countParameters++;
		}
		if (queryLanguage == null || queryLanguage.equals("")) {
			queryLanguage = defaultLanguage;
		}
	        String query = queryBuffer.toString();
	        query = query.trim();
	        if(query.endsWith("AND")){
	        	int indexOfAnd = query.lastIndexOf("AND");
	        	query = query.substring(0, indexOfAnd);
	        }
	        if(query.endsWith("OR")){
	        	int indexOfOr = query.lastIndexOf("OR");
	        	query = query.substring(0, indexOfOr);
	        }
	        IndexSearcher searcher = null;
	        try{
	        	log.info("The query is "+query);
	        	BooleanClause.Occur[] flags = new BooleanClause.Occur[map.size()];
	        	BooleanQuery.setMaxClauseCount(5000);
				for(int i=0;i<map.size();i++){

				if(luceneFields[i].equalsIgnoreCase("vcode")){
		        		flags[i] = BooleanClause.Occur.MUST;
				}else if(luceneFields[i].equalsIgnoreCase("song")){
						flags[i] = BooleanClause.Occur.SHOULD;
					}
					else if(luceneFields[i].equalsIgnoreCase("PARENT_CAT_NAME")){
						flags[i] = BooleanClause.Occur.MUST;
					}
					else if(luceneFields[i].equalsIgnoreCase("SUB_CAT_NAME")){
						flags[i] = BooleanClause.Occur.SHOULD;
					}
					else if(luceneFields[i].equalsIgnoreCase("album")){
						flags[i] = BooleanClause.Occur.SHOULD;
					}
					else if(luceneFields[i].equalsIgnoreCase("artist")){
						flags[i] = BooleanClause.Occur.SHOULD;
					}
					else if(luceneFields[i].equalsIgnoreCase("clipId")){
						flags[i] = BooleanClause.Occur.MUST;
					}
					else if(luceneFields[i].equalsIgnoreCase("parentCatId")){
						flags[i] = BooleanClause.Occur.MUST;
					}
					else if(luceneFields[i].equalsIgnoreCase("subCatId")){
						flags[i] = BooleanClause.Occur.MUST;
					}
					else if(luceneFields[i].equalsIgnoreCase("SUB_CAT_TYPE")){
						flags[i] = BooleanClause.Occur.MUST;
					}
					else if(luceneFields[i].equalsIgnoreCase("CAT_PROMO_ID")){
						flags[i] = BooleanClause.Occur.MUST;
					}
	        	}
				String indexPath = getIndexPath(queryLanguage);
				log.info("The index search path is "+indexPath);
				searcher = new IndexSearcher(indexPath);
				Analyzer analyzer = null;
				try{
					analyzer = getAnalyzer(queryLanguage);
					log.info("Got the analyzer for "+queryLanguage+" as "+analyzer);
				}
				catch(RBTLuceneException rbtle){
					log.error(rbtle.getMessage());
					return null;
				}
				Query lucQuery= MultiFieldQueryParser.parse(query, luceneFields, flags, analyzer);
				TopDocs topDocs = searcher.search(lucQuery, new QueryFilter(lucQuery), 100);
				ScoreDoc[] scoreDocs = topDocs.scoreDocs;
				if(scoreDocs!=null)
					log.info("Score docs "+scoreDocs.length);
				List<String> localClipCategoryCache = null;
				List<String> localCategoryCache = null;
				List<String> localClipCache = null;
				if(scoreDocs!=null && scoreDocs.length>0){
					localClipCategoryCache = new ArrayList<String>();
					localClipCache = new ArrayList<String>();
					localCategoryCache = new ArrayList<String>();
					luceneClipList = new ArrayList<LuceneClip>();
					for(int j=0;j<scoreDocs.length;j++){
						Document document = searcher.doc(scoreDocs[j].doc);
						String clipId = document.get("clipId");
						String parentcatId = document.get("parentCatId");
						String subCatId = document.get("subCatId");
						String vcode = document.get("vcode");
						StringBuilder clipCatMap = new StringBuilder();
						
						//Filtering clipid and category id				
						if(filterClipIdsList.contains(clipId)) {
							continue;
						}
						
						if(filterCategoryIdsList.contains(subCatId)) {
							continue;
						}						
						
						clipCatMap.append(clipId).append(":");
						clipCatMap.append(parentcatId).append(":");
						clipCatMap.append(subCatId);
						if(!localClipCategoryCache.contains(clipCatMap.toString())){
							localClipCategoryCache.add(clipCatMap.toString());
						}
						if(!localClipCache.contains(clipId)){
							localClipCache.add(clipId);
						}
						if(parentcatId!=null && !parentcatId.equalsIgnoreCase("null")){
							if(!localCategoryCache.contains(parentcatId)){
								localCategoryCache.add(parentcatId);
							}
						}
						if(subCatId!=null && !subCatId.equalsIgnoreCase("null")){
							if(!localCategoryCache.contains(subCatId)){
								localCategoryCache.add(subCatId);
							}
						}
						
					}
						
					//--------------- Get the clips at once from the memcache
					String[] clipIds = (String[])localClipCache.toArray(new String[0]);
					String[] categoryIds = (String[])localCategoryCache.toArray(new String[0]);
					Clip[] clips = cacheManager.getClips(clipIds, language);
					Category[] categories = null;
					if(categoryIds!=null && categoryIds.length>0){
						log.debug("The category length is "+categoryIds.length);
						categories = cacheManager.getCategories(categoryIds, language);
					}
					//--------------- Get local category map categoryId Vs Category object
					Map<String, Category> localCategoryMap = getLocalCategoryMap(categories);

					for(int i=0;i<clips.length;i++){
						if(clips[i]!=null){
							Clip clip = clips[i];
							String[] clipParentAndSubCat = localClipCategoryCache.get(i).split(":");
							String parentCategoryId  = clipParentAndSubCat[1];
							String subCategoryId  = clipParentAndSubCat[2];
							String parentCatName = null;
							String subCatName = null;
							if(parentCategoryId!=null && !parentCategoryId.equalsIgnoreCase("null")){
								parentCatName =  localCategoryMap.get(parentCategoryId).getCategoryName();
							}else{
								parentCategoryId = null;
							}
							if(subCategoryId!=null && !subCategoryId.equalsIgnoreCase("null")){
								subCatName = localCategoryMap.get(subCategoryId).getCategoryName();
							}else{
								subCategoryId = null;
							}
							LuceneClip lucClip = null;
							if(parentCategoryId==null && subCategoryId==null){
								lucClip = new LuceneClip(clip, 0 ,0,null, null);
							}else if(parentCategoryId==null){
								lucClip = new LuceneClip(clip, 0 ,Long.parseLong(subCategoryId),null, null);
							}else if(subCategoryId==null){
								lucClip = new LuceneClip(clip, Long.parseLong(parentCategoryId) ,0,null, null);
							}else{
								lucClip = new LuceneClip(clip, Long.parseLong(parentCategoryId) ,Long.parseLong(subCategoryId),parentCatName, subCatName);
							}
							luceneClipList.add(lucClip);
						}
						
					}
				}
//				searcher.close();
			}
	        catch(ParseException pe){
	        	pe.printStackTrace();
	        }
	        catch(IOException ioe){
	        	ioe.printStackTrace();
	        }
	        finally{
		    	try{
		    		if(searcher != null)
		    			searcher.close();
		    	}
		    	catch(Throwable t) {}
		    }
	        
		
		if(luceneClipList!=null && luceneClipList.size()>0){
			this.totalResults=luceneClipList.size();
			ArrayList<LuceneClip> origList = new ArrayList<LuceneClip>(luceneClipList);
			ArrayList<LuceneClip> subList= new ArrayList<LuceneClip>(maxResults);

			for(int i=offset;i<origList.size() && i<(maxResults+offset); i++) {
				subList.add(origList.get(i));
			}
			return subList;
		}
			
		else{
			return null;
		}

	}
	
	private String getLanguage(String language) {
		if(language == null) {
			language = defaultLanguage;
		}
		return language;
	}
	
	@Override
	public ArrayList<LuceneClip> searchByNametune(HashMap map,int pageNo, int maxResults,String language,String queryLanguage){
		MSearch msearch = MSearch.getInstance();
		ArrayList<LuceneClip> luceneClipList = msearch.searchClip(map, language, queryLanguage);
		if(luceneClipList==null){
			if(log.isDebugEnabled()){
		       log.debug("SearchByNameTune:ClipList is Null map= "+map + "pageNo = "+pageNo+"maxResults="
				   + maxResults + "language="+language+"queryLanguage="+queryLanguage);
			 }
			return luceneClipList;
		}
		totalResults = luceneClipList.size();
		luceneClipList = getLuceneClipByPageNo(luceneClipList,pageNo,maxResults);
		return luceneClipList;
	}
	
	private ArrayList<LuceneClip> getLuceneClipByPageNo(ArrayList<LuceneClip> luceneClipList,int pageNo,int maxResults){
		if(luceneClipList==null || luceneClipList.size()==0 || maxResults == -1)
			return luceneClipList;
		if (luceneClipList.size() > pageNo * maxResults) {
			int next = (pageNo + 1) * maxResults;
			int max = (luceneClipList.size() >= next) ? next : luceneClipList.size();
			luceneClipList = new ArrayList<LuceneClip>(luceneClipList.subList(pageNo * maxResults, max));
		} else {
			luceneClipList = new ArrayList<LuceneClip>();
		}

		return luceneClipList;
	}

	//RBT-9871
	@Override
	public ArrayList<LuceneClip> multiFeildmsearch(HashMap map, int pageNo,
			int maxResults, String language) {
		log.info("request map in multiFeildmsearch :"+map);
		ArrayList<LuceneClip> luceneClips=new ArrayList<LuceneClip>();
		String multiFeildmSearchURL = ConfigReader.getInstance().getParameter(SearchConstants.MULTI_FEILD_MSEARCH_URL);
		String multiFeildmSearchFeilds = ConfigReader.getInstance().getParameter(SearchConstants.MULTI_FEILD_MSEARCH_URL_FL);
		 
		if(map!=null && ((map.get(SearchConstants.FIELD_CLIP_PROMO_ID)!=null && !map.get(SearchConstants.FIELD_CLIP_PROMO_ID).equals("")))){
			log.info("searching by clipPromoId...");			
			Clip clip = null;
			if(map.containsKey(SearchConstants.FIELD_CLIP_PROMO_ID)){
				String clipPromoId = (String)map.get(SearchConstants.FIELD_CLIP_PROMO_ID);
				clip = cacheManager.getClipByPromoId(clipPromoId, language);
				if (clip == null) {
					log.info("Clip is null search by clipPromoId: " + clipPromoId);
					return null;
				}
				map.remove(SearchConstants.FIELD_CLIP_PROMO_ID);
				if(clip!=null)
				{
					//RBT-11300
					Long dummyCatId=3L;
					try {
						dummyCatId = Long.parseLong(ConfigReader.getInstance().getParameter(SearchConstants.PARAM_DUMMY_CATEGORY_ID));
						log.info("getting dummy_category_id: "+dummyCatId);
					}catch(Exception e) {
						log.info("Exception while getting dummy_category_id."+e);
					}
					
					LuceneClip lucClip=null;
					String parentCategoryId=(String) map.get("parentCatId");
					String subCategoryId=(String) map.get("subCatId");
					if((parentCategoryId==null || parentCategoryId.equals(""))&& (subCategoryId==null || subCategoryId.equals(""))){
						lucClip = new LuceneClip(clip, dummyCatId ,dummyCatId,null, null);
					}else if(parentCategoryId==null || parentCategoryId.equals("")){
						lucClip = new LuceneClip(clip, dummyCatId ,Long.parseLong(subCategoryId),null, null);
					}else if(subCategoryId==null || subCategoryId.equals("")){
						lucClip = new LuceneClip(clip, Long.parseLong(parentCategoryId) ,dummyCatId,null, null);
					}else{
						lucClip = new LuceneClip(clip, Long.parseLong(parentCategoryId) ,Long.parseLong(subCategoryId),null, null);
					}
				 luceneClips.add(lucClip);
				}			
			}

		}else if(multiFeildmSearchURL!=null && !multiFeildmSearchURL.trim().equals("")){
			log.info("multi feild msearch starting....");
			String query="";
			if(map.get(SearchConstants.DIALER_TONE_NAME)!=null && !((String) map.get(SearchConstants.DIALER_TONE_NAME)).trim().equals(""))
			{
				query += "title:"+map.get(SearchConstants.DIALER_TONE_NAME)+"|";
			}
			if(map.get(SearchConstants.ALBUM)!=null && !((String) map.get(SearchConstants.ALBUM)).trim().equals(""))
			{
				query += "album:"+map.get(SearchConstants.ALBUM)+"|";
			}
			if(map.get(SearchConstants.ARTIST)!=null && !((String) map.get(SearchConstants.ARTIST)).trim().equals(""))
			{
				query += "singer:"+map.get(SearchConstants.ARTIST)+"|";
			}
			query = query.substring(0,query.lastIndexOf("|")); 

			log.info("query: "+query +" and multiFeildmSearchFeilds: "+multiFeildmSearchFeilds);
			
			//RBT-9871 RBT-11260
			try {
				query=URLEncoder.encode(query,"UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.info("Exception in encoding: "+e);
				e.printStackTrace(); 
			}
			multiFeildmSearchURL = multiFeildmSearchURL.replace("%QUERY%", query);
			
			String celebrity = (String) map.get(SearchConstants.CELEBRITY);
			if (celebrity != null && !celebrity.trim().equals("")) {
				multiFeildmSearchURL = multiFeildmSearchURL.replace("%GENERENAME%", celebrity);
			}
			
			if(multiFeildmSearchFeilds!=null && !multiFeildmSearchFeilds.equals(""))
			{
				multiFeildmSearchURL=multiFeildmSearchURL.replace("%FEILDS%", multiFeildmSearchFeilds);
			}
			if(language!=null && !language.equalsIgnoreCase("ALL"))
			{
				multiFeildmSearchURL += "&fq=LANGUAGE:"+language;
			}
			
			log.info("updated encoded multiFeildmSearchURL: "+multiFeildmSearchURL);
						
			luceneClips = MSearch.getInstance().callURLformultiFeildmsearch(multiFeildmSearchURL,map);
			
			totalResults = luceneClips.size();
			
			// pagenation logic 
			if (luceneClips.size() > pageNo * maxResults) {
				int next = (pageNo + 1) * maxResults;
				int max = (luceneClips.size() >= next) ? next : luceneClips.size();
				luceneClips = new ArrayList<LuceneClip>(luceneClips.subList(pageNo * maxResults, max));
			} else {
				luceneClips = new ArrayList<LuceneClip>();
			}
			
			log.info("msearch returning luceneClips: " + luceneClips);
			
			}else {
				luceneClips = searchQuery(map, pageNo, maxResults, language);
			}
		return luceneClips;
		
	}
		
	@Override
	public SearchResponse searchCategory(String searchText, int noOfRows) {
		MSearch msearch = MSearch.getInstance();
		SearchResponse searchResponse = msearch.searchCategory(searchText, noOfRows);
		return searchResponse;
	}
	
	
	public List<String> getSuggestions(String search, int rows){
		MSearch msearch = MSearch.getInstance();
				List<String> list=msearch.getSuggestions(search, rows);
				return list;	
	}
	
	@Override
	public ArrayList<String> searchForArtists(String artist, int noOfRows)  {
		MSearch msearch = MSearch.getInstance();
		ArrayList<String> artistSearchResults = msearch.searchForArtists(artist, noOfRows);
		totalArtistResults = msearch.getTotalArtistSearchSize();
		return artistSearchResults;
	}
	
	@Override
	public ArrayList<LuceneClip> searchForArtistSongs(String artist, int noOfRows)  {
		MSearch msearch = MSearch.getInstance();
		ArrayList<LuceneClip> artistSongsSearchResults = msearch.searchForArtistSongs(artist, noOfRows);
		totalArtistSongResults = msearch.getTotalArtistSongSearchSize();
		return artistSongsSearchResults;
	}
}
