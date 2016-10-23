package com.onmobile.apps.ringbacktones.lucene.solr;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.apache.solr.common.SolrInputDocument;

import com.onmobile.apps.ringbacktones.lucene.LuceneCategory;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipBoundary;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;

/**
 * @author senthil.raja
 *
 */
public class IndexManager extends BaseClient implements SearchConstants{
	
	public Logger log = Logger.getLogger(IndexManager.class);
	
	private HashMap<String,String> paramMaps;
	private HashMap<String,String> indexMap;
	private int numOfDoc;
	private int dummyCategoryId;
	private int noOfClipsPerIteration;
	
	private TreeSet<ClipBoundary> clipBoundaries = null;
	
	private Site sites[] = null;
	
	public IndexManager() throws Exception {

		log.info("Indexer Started...");
		//--------- Get the default and the supported languages
		
		paramMaps = ConfigReader.getInstance().getParameters();
		String[] indexFields = paramMaps.get(PARAM_INDEX_FIELDS).split("\\,");
		indexMap = getIndexMap(indexFields);
		String noOfDocs = paramMaps.get(PARAM_NUM_OF_DOCS);
		try {
			numOfDoc = Integer.parseInt(noOfDocs);
		}
		catch (NumberFormatException e) {
			//log the error
			numOfDoc = NUM_OF_DOC_DEFAULT_VALUE;
		}
		String dummyCatId = paramMaps.get(PARAM_DUMMY_CATEGORY_ID);
		try {
			dummyCategoryId = Integer.parseInt(dummyCatId);
		}
		catch(NumberFormatException e){
			log.error("Dummy categoryId value should be nmber in config file", e);
			throw new Exception("Dummy categoryId value should be nmber in config file");
		}		
		
		try{
			noOfClipsPerIteration = Integer.parseInt(paramMaps.get(PARAM_NO_OF_CLIP_PER_ITERATION));
		}
		catch(Exception e){
			noOfClipsPerIteration = NO_OF_CLIPS_PER_ITERATION;
		}
		
		ApplicationDetailsRequest applicationRequest = new ApplicationDetailsRequest();
		sites = rbtClient.getSites(applicationRequest);

		clipBoundaries = ClipsDAO.getClipBoundariesUsingBinaryAlg(noOfClipsPerIteration);
		
	}

	public void init()  throws Exception{

		boolean isPorcessedDefaultLanguage = false;
		
		//-----------Creating the writer
		log.info("Creating the writers..");
		try {
			if(supportedLanguages!=null && supportedLanguages.size()>0){
				for(String supportedLanguage:supportedLanguages){
					this.prepareData(supportedLanguage);
					if(supportedLanguage.equalsIgnoreCase(defaultLanguage)) {
						isPorcessedDefaultLanguage = true;
					}
				}
			}
			if(!isPorcessedDefaultLanguage)
				this.prepareData(defaultLanguage);
		}
		catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		}
		finally{
			RBTCache.shutDown();
			Set<String> keyLanguageSet = serverMap.keySet();
			for(String languageKey : keyLanguageSet) {
				try {
					IndexCreator.getInstance().optimize(languageKey);
				}
				catch(Exception e) {}
				log.info("Successfully optimized");
				System.out.println("Successfully optimized");
				try{
					IndexCreator.getInstance().commit(languageKey);
				}
				catch(Exception e) {}
				log.info("Successfully commited");
				System.out.println("Successfully commited");
			}
		}
	}
	
	private void prepareData(String language) throws Exception {
		
		log.info("Language is "+language);

		// should be a key of clipId_categoryId_subCatID against the clipId
		Map<String, String> categoryClipMap=new HashMap<String, String>();
		Set<Integer> clipHashSet = new HashSet<Integer>();
		Set<SolrInputDocument> docSet = new HashSet<SolrInputDocument>(numOfDoc);
		try {
			//--------------- Set the index factors			
			
			if (sites == null) {
				log.warn("No sites are configured in this site");
				throw new Exception("No sites are configured in this site");
			}
			
			log.info("Site count "+sites.length);
			for (int siteCount = 0; siteCount < sites.length; siteCount++) {
				String circleId = sites[siteCount].getCircleID();
				
				getCategoriesAndSubCategoriesInLoop(PARENT_CATEGORY_ZERO, circleId, 'b', language, categoryClipMap, docSet, clipHashSet);
			}
			
			//Create index for Corporate Category and clips
			Category category = cacheManager.getCategory(1, language);
			if(category != null) {
				LuceneCategory luceneCategory = new LuceneCategory(category,category.getCategoryId(),category.getCategoryName());
				getClipsAndIndex(luceneCategory, categoryClipMap, true, luceneCategory, language, docSet, clipHashSet);
			}
			//Create the index for unmapped clips			
			createIndexForUnMappedClip(clipHashSet, docSet, language);
		} catch (Exception e) {
			throw e;
		}
	}

	public void getCategoriesAndSubCategoriesInLoop(int parentCategoryId,
			String circleId, char prepaidYes, String language,
			Map<String, String> categoryClipMap, Set<SolrInputDocument> docSet,
			Set<Integer> clipHashSet) throws Exception {
		
		log.debug("Checking for parentCatId " + parentCategoryId);
		
		Category[] categories = null;
		Category parentCategory = cacheManager.getCategory(parentCategoryId, language);
		if (parentCategoryId == 0) {
			categories = cacheManager.getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language);
			if (categories == null) {
				prepaidYes = 'y';
				categories = cacheManager.getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language);				
			}
			if (categories == null) {
				log.warn("The active Categories are null for circle:"+circleId + " parentCatId:" + parentCategoryId + " prepaid:" + prepaidYes + " lang:" +language);
				return;
			}
		} else {
			categories = cacheManager.getActiveCategoriesInCircle(circleId, parentCategoryId, prepaidYes, language);
		}	
		
		if (categories == null || categories.length <= 0) {
			return;
		}
		int categorySize = categories.length;
		for (int i = 0; i < categorySize; i++) {
			
			//get the cilps from Category and create the index
			if (categories[i] != null) {
				if (parentCategory == null) {
					getClipsAndIndex(categories[i], categoryClipMap, true, categories[i], language, docSet, clipHashSet);
				} else {
					getClipsAndIndex(categories[i], categoryClipMap, false, parentCategory, language, docSet, clipHashSet);
				}				
				getCategoriesAndSubCategoriesInLoop(categories[i].getCategoryId(), circleId, prepaidYes, language, categoryClipMap, docSet, clipHashSet);
			}
		}		
	}
	
	private void createIndexForUnMappedClip(Set<Integer> clipHashSet, Set<SolrInputDocument> docSet, String language)
			throws Exception {
		Date now = new Date();
		log.info("Now Indexing Unmapped Clips and dummy category");
		Category category = cacheManager.getCategory(dummyCategoryId, language);
		if (category == null) {
			log.warn("In createIndexForUnMappedClip(): The dummy category is null for category id "+dummyCategoryId);
			IndexCreator.getInstance().commit(language);
			docSet.clear();
			throw new Exception("The dummy category is null for category id "+dummyCategoryId);
		}		
		int parentCategoryId = category.getCategoryId();
		String parentCategoryName = category.getCategoryName();
		LuceneCategory categoryObj = new LuceneCategory(category,parentCategoryId,parentCategoryName);
		SolrInputDocument doc = indexConfiguredFieldsInObject(categoryObj, language);
		doc.addField("id", parentCategoryId+"_" + language.toUpperCase());
		if(categoryObj.getCategoryLanguage().equals(language))
			docSet.add(doc);
		List<Clip> clipList = null;
		for(ClipBoundary clipBoundary : clipBoundaries){
			clipList = ClipsDAO.getClipsInBetween(clipBoundary.getStartIndex(), clipBoundary.getEndIndex(),language);
			if(clipList == null || clipList.size() == 0){
				continue;
			}
			int clipSize = clipList.size();
			for(int i = 0; i < clipSize; i++){
				Clip clip = clipList.get(i);
				
				if (!clip.getClipStartTime().before(now)) {
					//ignore index for future date clip
					continue;
				}
				
				//Ignore UGC clips
				boolean ugcCheck =checkUgcTrialClips(paramMaps,clip);
				if(ugcCheck)
					continue;
				if (!clipHashSet.contains(clip.getClipId()) && clip.getClipLanguage().equalsIgnoreCase(language)) {
					docSet.add(getSolrInputDocumentForClip(clip, category, category, true, language));
				}
				if (docSet.size() >= numOfDoc || clipSize == (i + 1)) {
					IndexCreator.getInstance().createIndex(docSet, language);
					docSet.clear();
				}				
			}
		}
		
		if (docSet.size() > 0) {
			IndexCreator.getInstance().createIndex(docSet, language);
			docSet.clear();
		}

	}
	
	boolean checkUgcTrialClips(HashMap<String,String> paramMaps,Clip clip)
	{
		boolean tempCheck = (("TRUE".equalsIgnoreCase(paramMaps.get(AVOID_UGC_TRIAL_CLIPS)))                        
                && ((clip.getClipGrammar() != null && clip.getClipGrammar().startsWith("UGC")) 
                            || (clip.getClassType() != null && clip.getClassType().startsWith("TRIAL"))));
		return tempCheck;

	}
	
	
	private String getMethodName(String methodType, String fieldName) {
		String str = fieldName;
		char c = str.charAt(0);
		// converting to upper case 
		c = (char)((int)c - 32);
		str = methodType + c + str.substring(1);
		return str;
	}
	
	public SolrInputDocument indexConfiguredFieldsInObject(Object object, String language)
			throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String classCanonicalName = object.getClass().getCanonicalName();		
		String[] fields = paramMaps.get(classCanonicalName).split("\\,");				
		SolrInputDocument doc = new SolrInputDocument();
		boolean isRequiredPhoneticGramms = true;
		Class cls = object.getClass();
		for (int i = 0; i < fields.length; i++) {			
			Object value = null;
			try {
				String methodName = getMethodName("get" , fields[i]);				
				value = cls.getMethod(methodName).invoke(object);
				if (value instanceof Date) {
					value = new Long(((Date)value).getTime());
				}
			} catch (NoSuchMethodException e) {
				log.error(fields[i] + " not declared ", e);
				throw e;
			} catch (IllegalAccessException ille) {
				log.error(fields[i] + " has not access ", ille);
				throw ille;
			} catch (InvocationTargetException ille) {
				log.error(fields[i] + " has not access ", ille);
				throw ille;
			}
			String fieldName = indexMap.get(fields[i]);
			if (fieldName == null) {
				continue;
			}
			fieldName = getFieldName(fieldName);
			//if DB has null, indexed as empty string
			if (value == null || value.toString().trim().length() <=0 ) {
				continue;
			}
			
			doc.addField(fieldName, value.toString());
			if (isGramsEnabled) {
				String temp = getGrams(value.toString());
				if(null != temp) {
					doc.addField(getGramFieldName(fieldName), temp);
				}
			}
			if (isPhoneticsEnabled) {
				String temp = getPhonetics(value.toString());
				if (null != temp) {
					doc.addField(getPhoneticFieldName(fieldName), temp);
				}
			}
		}
		doc.addField(FIELD_SEARCH_TYPE,cls.getSimpleName());
		
//		if("TRUE".equalsIgnoreCase(isSupportLanguage))
//			doc.addField(FIELD_LANGUAGE, language.toUpperCase());
		
		return doc;
	}
	
	private HashMap<String, String> getIndexMap(String[] indexFields) {
		HashMap<String,String> indexMap = new HashMap<String,String>();
		int size = indexFields.length;
		for (int i = 0; i < size; i++) {
			String[] arr = indexFields[i].split("\\:");
			indexMap.put(arr[0], arr[1]);
		}
		return indexMap;
	}
	
	private void getClipsAndIndex(Category category, Map<String, String> categoryClipMap,
			boolean parentYes, Category parentCategory, String language,
			Set<SolrInputDocument> docSet, Set<Integer> clipHashSet) throws Exception {
		//if we are to get the clips inside the parent cat then the key would be clipId_parentCatId_null
		// If we are to get the clips inside the sub cat then the key would be clipId_parentCatId_subCatId
		int subCatId=category.getCategoryId();
		String key=null;
		if (parentYes) {
			key = "_" + parentCategory.getCategoryId() + "_null_" + language.toUpperCase();
		} else {
			key = "_" + parentCategory.getCategoryId() + "_" + subCatId + "_" + language.toUpperCase();
		}
		log.info("Parent Category (" + parentCategory.getCategoryId() + " ) ----  Sub Category (" + subCatId + " )");
		
		//Create Index for Category
		LuceneCategory categoryObj = null;
		int parentCategoryId =  parentCategory.getCategoryId();
		String parentCategoryName = parentCategory.getCategoryName() ;
		if (parentYes) {
			categoryObj = new LuceneCategory(parentCategory, parentCategoryId, parentCategoryName);
		} else {
			categoryObj = new LuceneCategory(category, parentCategoryId, parentCategoryName);
		}
		SolrInputDocument categoryDoc = indexConfiguredFieldsInObject(categoryObj, language);
		categoryDoc.addField("id", categoryObj.getParentCategoryId() + "" + categoryObj.getCategoryId()+""+language.toUpperCase());
		if(categoryObj.getCategoryLanguage().equalsIgnoreCase(language))
			docSet.add(categoryDoc);
		
		if(Utility.isShuffleCategory(category.getCategoryTpe())){
			return;
		}
		
		Clip[] clipsInCat = cacheManager.getClipsInCategory(subCatId, language);
		
		if (clipsInCat != null && clipsInCat.length > 0) {
			int clipsInCateLength = clipsInCat.length;
			log.info("Category (" + subCatId + " ) has " + clipsInCat.length + " clips");
			for (int clipIndex = 0; clipIndex < clipsInCateLength; clipIndex++) {
				if (clipsInCat[clipIndex] != null) {
					//Ignore UGC Clips
					boolean ugcCheck =checkUgcTrialClips(paramMaps,clipsInCat[clipIndex]);
					if(ugcCheck)
						continue;
					
					clipHashSet.add(clipsInCat[clipIndex].getClipId());
					if (categoryClipMap.get(clipsInCat[clipIndex].getClipId()+ key) == null) {
						categoryClipMap.put(clipsInCat[clipIndex].getClipId()+key,Integer.toString(clipsInCat[clipIndex].getClipId()));
						if(clipsInCat[clipIndex].getClipLanguage().equalsIgnoreCase(language))
							docSet.add(getSolrInputDocumentForClip(clipsInCat[clipIndex], parentCategory, categoryObj, parentYes, language));
					}					
				}
				if (docSet.size() >= numOfDoc || clipsInCateLength == (clipIndex + 1)) {
					IndexCreator.getInstance().createIndex(docSet, language);
					docSet.clear();
				}
			}
		}
		
		if (docSet.size() > 0) {
			IndexCreator.getInstance().createIndex(docSet, language);
			docSet.clear();
		}
	}
	
	private SolrInputDocument getSolrInputDocumentForClip(Clip clip, Category parentCategory,
			Category category, boolean parentYes, String language) throws Exception {
		LuceneClip clipObj = null;						
		int parentCatId = parentCategory.getCategoryId();
		String parentCatName = parentCategory.getCategoryName();
		if (parentYes) {
			clipObj = new LuceneClip(clip, parentCatId, parentCatId, parentCatName, parentCatName);
		} else {
			int subCatId = category.getCategoryId();
			String subCatName = category.getCategoryName();
			clipObj = new LuceneClip(clip, parentCatId, subCatId, parentCatName, subCatName);
		}
		SolrInputDocument doc = indexConfiguredFieldsInObject(clipObj, language);
		doc.addField("id", clipObj.getClipId() + "" + clipObj.getParentCategoryId() + "" +clipObj.getSubCategoryId() + language.toUpperCase());
		//changes for corp and normal		
		if(category.getCategoryId() == 1) {
			doc.addField(FILTER_CLIP_TYPE, CLIP_TYPE_CORP);
		}
		else
			doc.addField(FILTER_CLIP_TYPE, CLIP_TYPE_NORMAL);
	
		return doc;
	}
	

	public static boolean initialize() {
		try {
			main(null);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static void main(String args[]) throws Exception{
		try {
			IndexManager manager = new IndexManager();
			manager.init();
		} catch (Exception e) {
			throw e;
		}
		finally {
			System.exit(0);
		}
	}
}
