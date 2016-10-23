package com.onmobile.apps.ringbacktones.lucene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import com.onmobile.apps.ringbacktones.lucene.msearch.SearchResponse;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.LanguageIndentifier;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.tangentum.phonetix.DoubleMetaphone;

public abstract class AbstractLuceneIndexer {
	
	static Logger log = Logger.getLogger(AbstractLuceneIndexer.class); 

	//------------------ Search criterion

	public static final String SEARCH_ON_CLIP_ID="clipId";
	public static final String SEARCH_ON_CLIP_RBT_WAV_FILE="clipRbtWavFile";
	public static final String SEARCH_ON_PARENT_CAT_NAME="PARENT_CAT_NAME";
	public static final String SEARCH_ON_SUB_CAT_NAME="SUB_CAT_NAME";
	public static final String SEARCH_ON_SONG_NAME="song";
	public static final String SEARCH_ON_ALBUM="album";
	public static final String SEARCH_ON_ARTIST="artist";
	public static final String SEARCH_ON_CAT_PROMO_ID="CAT_PROMO_ID";
	private static final String DEFAULT_LANGUAGE_VALUE="eng";
	private static final String CHAR_SET_UTF8="UTF-8";
	protected static Map<String, String> languageAnalyzerMap = new HashMap<String, String>();
	protected static Map<Integer, Map<String, ArrayList<Category>>> alphabetMap = new HashMap<Integer, Map<String, ArrayList<Category>>>();
	
	static{
		log.info("Loading the Analyzers...");
//		languageAnalyzerMap.put("ara", "gpl.pierrick.brihaye.aramorph.lucene.ArabicStemAnalyzer");
		languageAnalyzerMap.put("ara", "org.apache.lucene.analysis.ar.ArabicAnalyzer");
		languageAnalyzerMap.put("eng", "org.apache.lucene.analysis.standard.StandardAnalyzer");
		languageAnalyzerMap.put("default", "org.apache.lucene.analysis.standard.StandardAnalyzer");
		log.info("Loaded the Analyzers...");
		
		/*log.info("Loading the alphabet Map");
		for(int i=65;i<=90;i++){
			String alphabet = new Character((char)i).toString();
			alphabetMap.put(alphabet, null);
		}
		*/
	}
	
	
	//---------------- Path to update/ recreate the indexes
	protected static String tempIndexPath = null;
	
	//---------------RBTClient instance
	protected static RBTClient rbtClient = null;
	
	//---------------RBTCacheManager instance
	protected static RBTCacheManager cacheManager = null;
	
	protected static DoubleMetaphone m_metaphone = new DoubleMetaphone(6);
	
	protected IndexWriter tempIndexWriter = null;
	
	//----------------- All Parent category ID's 
	protected final static int PARENT_CATEGORY_ZERO=0;
	
	//------------- Base index path 
	protected static String indexPath= null;
	
	//------------- Actual index creation path 
	protected static String actualIndexPath= null;
	
	protected int totalResults = 0 ;
	
	protected int totalArtistResults = 0 ;

	protected int totalArtistSongResults = 0 ;

	//--------------- For multi language support
	protected static String[] supportedLanguages = null;
	
	//--------------- Defualt language
	protected static String defaultLanguage = null;
	
	//RBT-9871
	public abstract ArrayList<LuceneClip> multiFeildmsearch(HashMap map,int pageNo, int maxResults, String language);
	
	//------------------------ Abstract methods, for any implementation. 
	
	/**
	 *  SearchQuery API to search songs based on criteria. The page number starts from 0,1, and so on. 
	 *  Following are the search criterion
	 *  SEARCH_ON_CLIP_ID
	 *	SEARCH_ON_CLIP_RBT_WAV_FILE
	 *	SEARCH_ON_PARENT_CAT_NAME
	 *	SEARCH_ON_SUB_CAT_NAME
	 *	SEARCH_ON_SONG_NAME
	 *	SEARCH_ON_ALBUM
	 *	SEARCH_ON_ARTIST
	 * 
	 */
	public abstract ArrayList<LuceneClip> searchQuery(HashMap map,int pageNo, int maxResults);
	
	/**
	 * @Deepak kumar
	 * Searching by NameTune through MSearch based on criteria Song or Artist or Album
	 */
	public abstract ArrayList<LuceneClip> searchByNametune(HashMap map,int pageNo, int maxResults,String language,String queryLanguage);
	
	public abstract ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, String language, String queryLanguage) ;

	/**
	 *  SearchQuery API to search songs based on criteria. The page number starts from 0,1, and so on. 
	 *  Following are the search criterion
	 *  SEARCH_ON_CLIP_ID
	 *	SEARCH_ON_CLIP_RBT_WAV_FILE
	 *	SEARCH_ON_PARENT_CAT_NAME
	 *	SEARCH_ON_SUB_CAT_NAME
	 *	SEARCH_ON_SONG_NAME
	 *	SEARCH_ON_ALBUM
	 *	SEARCH_ON_ARTIST
	 * 
	 */
	public abstract ArrayList<LuceneClip> searchQuery(HashMap map,int pageNo, int maxResults, boolean isSupportPhonetic);
	
	/**
	 *  SearchQuery API to search songs based on criteria. The page number starts from 0,1, and so on. 
	 *  Following are the search criterion
	 *  SEARCH_ON_CLIP_ID
	 *	SEARCH_ON_CLIP_RBT_WAV_FILE
	 *	SEARCH_ON_PARENT_CAT_NAME
	 *	SEARCH_ON_SUB_CAT_NAME
	 *	SEARCH_ON_SONG_NAME
	 *	SEARCH_ON_ALBUM
	 *	SEARCH_ON_ARTIST
	 * 
	 */
	public abstract ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, String language, boolean isSupportPhonetic);
	
	/**
	 *  SearchQuery API to search songs based on criteria. The page number starts from 0,1, and so on. 
	 *  Following are the search criterion
	 *  SEARCH_ON_CLIP_ID
	 *	SEARCH_ON_CLIP_RBT_WAV_FILE
	 *	SEARCH_ON_PARENT_CAT_NAME
	 *	SEARCH_ON_SUB_CAT_NAME
	 *	SEARCH_ON_SONG_NAME
	 *	SEARCH_ON_ALBUM
	 *	SEARCH_ON_ARTIST
	 *
	 * @param map Contains field name and search string as key value pairs
	 * @param pageNo 
	 * @param maxResults 
	 * @param language
	 * @param isSupportPhonetic
	 * @param isUnionSearch Search on multiple fields using OR operator
	 * @return
	 */
	public abstract ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, String language,
			boolean isSupportPhonetic, boolean isUnionSearch);
	
	
	public abstract ArrayList<LuceneClip> searchQuery(HashMap map, int pageNo, int maxResults, String language,
			boolean isSupportPhonetic, boolean isUnionSearch, String queryLanguage);

	/**
	 *  SearchQuery API to search songs based on criteria. The page number starts from 0,1, and so on. According to the language passed
	 *  Following are the search criterion
	 *  SEARCH_ON_CLIP_ID
	 *	SEARCH_ON_CLIP_RBT_WAV_FILE
	 *	SEARCH_ON_PARENT_CAT_NAME
	 *	SEARCH_ON_SUB_CAT_NAME
	 *	SEARCH_ON_SONG_NAME
	 *	SEARCH_ON_ALBUM
	 *	SEARCH_ON_ARTIST
	 * 
	 */
	public abstract ArrayList<LuceneClip> searchQuery(HashMap map,int pageNo, int maxResults, String language);
	
	/**
	 *  SearchCategoryQuery API to search categories based on criteria. The page number starts from 0,1, and so on.
	 *  The API searches only the shuffle categories.  
	 *  Following are the search criterion
	 *	SEARCH_ON_PARENT_CAT_NAME
	 *  SEARCH_ON_CAT_PROMO_ID
	 *	SEARCH_ON_SUB_CAT_NAME
	 */
	public abstract ArrayList<LuceneCategory> searchCategoryQuery(HashMap map,int pageNo, int maxResults);
	
	
	/**
	 *  SearchCategoryQuery API to search categories based on criteria. The page number starts from 0,1, and so on.
	 *  The API searches only the shuffle categories.  
	 *  Following are the search criterion
	 *	SEARCH_ON_PARENT_CAT_NAME
	 *  SEARCH_ON_CAT_PROMO_ID
	 *	SEARCH_ON_SUB_CAT_NAME
	 */
	public abstract ArrayList<LuceneCategory> searchCategoryQuery(HashMap map,int pageNo, int maxResults, boolean isSupportPhonetic);

	/**
	 *  SearchCategoryQuery API to search categories based on criteria. The page number starts from 0,1, and so on according to language passed
	 *  The API searches only the shuffle categories.  
	 *  Following are the search criterion
	 *	SEARCH_ON_PARENT_CAT_NAME
	 *  SEARCH_ON_CAT_PROMO_ID
	 *	SEARCH_ON_SUB_CAT_NAME
	 */
	public abstract ArrayList<LuceneCategory> searchCategoryQuery(HashMap map,int pageNo, int maxResults, String language);

	/**
	 *  SearchCategoryQuery API to search categories based on criteria. The page number starts from 0,1, and so on according to language passed
	 *  The API searches only the shuffle categories.  
	 *  Following are the search criterion
	 *	SEARCH_ON_PARENT_CAT_NAME
	 *  SEARCH_ON_CAT_PROMO_ID
	 *	SEARCH_ON_SUB_CAT_NAME
	 */
	public abstract ArrayList<LuceneCategory> searchCategoryQuery(HashMap map,int pageNo, int maxResults, String language, boolean isSupportPhonetic);
	
	public abstract ArrayList<LuceneCategory> searchCategoryQuery(HashMap map, int pageNo, int maxResults, String language, boolean isSupportPhonetic, String queryLanguage);

	/**
	 * 
	 * @param map
	 * @param pageNo
	 * @param maxResults
	 * @param circle
	 * @return ArrayList
	 */
	public abstract ArrayList<Category> searchCategoryAlphabetically(String alphabet, int pageNo, int maxResults, String circle, String language, int categoryId);
	
	/**
	 * This API gets the content from the cache and indexes.
	 */
	protected abstract void getContentAndIndex(IndexWriter iw, String language);
	
	/**
	 * To check the total results in a search operation
	 * @return int
	 */
	public abstract int getTotalSearchSize();
	
	/**
	 * Constructor 
	 * Gets the ACTUAL_INDEX_PATH and DEFAULT_REPORT_PATH from the RBTClient
	 * @throws Exception
	 */
	
	public AbstractLuceneIndexer(){
		log.info("AbstractLucneIndexer:Constr:: ");
		rbtClient = RBTClient.getInstance();
		cacheManager = RBTCacheManager.getInstance();
		//----------- get the index path from rbtClient
		log.info("Getting the DEFAULT_REPORT_PATH");
		indexPath = rbtClient.getParameter(new ApplicationDetailsRequest("ALL","DEFAULT_REPORT_PATH",(String)null)).getValue();
		log.info("Got the DEFAULT_REPORT_PATH as "+indexPath);
		log.info("Getting the ACTUAL_INDEX_PATH");
		actualIndexPath = rbtClient.getParameter(new ApplicationDetailsRequest("ALL","ACTUAL_INDEX_PATH",(String)null)).getValue();
		log.info("Got the ACTUAL_INDEX_PATH as "+actualIndexPath);
		if(indexPath==null || actualIndexPath==null){
			log.error("The DEFAULT_REPORT_PATH or ACTUAL_INDEX_PATH value is not set");
			//---------- Since a standalone program we exit from the process if paths are not set
			System.exit(1);
		}else{
			//--------- Get the default and the supported languages
			String supportedLangs = null;
			if(rbtClient.getParameter(new ApplicationDetailsRequest("ALL","SUPPORTED_LANGUAGES",(String)null))!=null)
				supportedLangs = rbtClient.getParameter(new ApplicationDetailsRequest("ALL","SUPPORTED_LANGUAGES",(String)null)).getValue();
			log.info("SUPPORTED_LANGUAGES are "+supportedLangs);
			if(supportedLangs!=null){
				supportedLanguages = supportedLangs.split(",");
				log.info("Supported languages length "+supportedLanguages.length);
			}
			else{
				log.info("Supported Languages not present in the DB, so creating indexes for Default language");
			}
			defaultLanguage = rbtClient.getParameter(new ApplicationDetailsRequest("ALL","DEFAULT_LANGUAGE",(String)null)).getValue();
			log.info("DEFAULT_LANGUAGE is "+defaultLanguage);
			if(defaultLanguage==null)
				defaultLanguage = DEFAULT_LANGUAGE_VALUE;
			//---------- Always create/update the index path in directory actualIndexPath
			tempIndexPath = actualIndexPath+File.separator+"newIndexes";
			log.info("tempIndexPath is "+tempIndexPath);
		}
		 
	}
	
	
	/**
	 * This method should only be called by the LuceneInitializer class. To initialize the indexes. 
	 * Thats the reason its been put into a default scope, so that only the initializer calls it, when the
	 * script for it is run. 
	 * 1. Create the index folders for all the supported languages, if supported languages exist. The folder names would be RBT short codes of the languages.
	 * 2. Always create default language folder to store the default language indexes.
	 * 3. Create the indexes accordingly.
	 * 4. Copy all the folders to DEFAULT_REPORT_PATH
	 */
	void init(){
		File index = null;
		
		//-----------Creating the writer
		log.info("Creating the writers..");
		try{
			Analyzer analyzer = null;
			if(supportedLanguages!=null && supportedLanguages.length>0){
				for(String supportedLanguage:supportedLanguages){
					//------------ Create the ACTUAL_INDEX_PATH
				index = new File(tempIndexPath+File.separator+supportedLanguage);
				if (index.exists())
					index.delete();
					//------------- Initialize the Analyzer for a specific language
					analyzer = getAnalyzer(supportedLanguage);
					log.info("Got the Analyzer instance for default language");
					tempIndexWriter = new IndexWriter(index.getAbsolutePath(),analyzer, true);
					//-------------Now get the content from cache and index it for the specific language
					this.getContentAndIndex(tempIndexWriter,supportedLanguage);
//					try{
//						tempIndexWriter.optimize();
//						tempIndexWriter.close();
//					}
//					catch(IOException ioe){
//						log.error(ioe.getMessage());
//						ioe.printStackTrace();
//					}
					tempIndexWriter = null;
				}
				//-----------Create index for default language
				analyzer = getAnalyzer("default");
				log.info("Got the Analyzer instance for default language");
				index = new File(tempIndexPath+File.separator+defaultLanguage);
				if (index.exists())
					index.delete();
				tempIndexWriter = new IndexWriter(index.getAbsolutePath(),analyzer, true);
				//-------------Now get the content from cache and index it for the specific language
				this.getContentAndIndex(tempIndexWriter,defaultLanguage);
//				try{
//					tempIndexWriter.optimize();
//					tempIndexWriter.close();
//				}
//				catch(IOException ioe){
//					log.error(ioe.getMessage());
//					ioe.printStackTrace();
//				}
			}else{
				index = new File(tempIndexPath+File.separator+defaultLanguage);
				log.info("Supported languages are null so creating index for default language");
				analyzer = getAnalyzer("default");
				log.info("Got the Analyzer instance for default language");
				tempIndexWriter = new IndexWriter(index.getAbsolutePath(),analyzer, true);
				//-------------Now get the content from cache and index it for the specific language
				this.getContentAndIndex(tempIndexWriter,defaultLanguage);
//				try{
//					tempIndexWriter.optimize();
//					tempIndexWriter.close();
//				}
//				catch(IOException ioe){
//					log.error(ioe.getMessage());
//					ioe.printStackTrace();
//				}
				
			}
		}
		catch (Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
		}
		
		/*
		 * Create newIndexes folder in DEFAULT_REPORT_PATH
		 * Copy the indexes in ACTUAL_INDEX_PATH to newIndexes folder in DEFAULT_REPORT_PATH
		 * Rename the newIndexes folder to timeInMillis after copying all the indexes.
		 */
		File newIndexesFolder = new File(indexPath+File.separator+"newIndexes");
		if(supportedLanguages!=null && supportedLanguages.length>0){
			for(String supportedLanguage:supportedLanguages){
				File destination = new File(indexPath+File.separator+"newIndexes"+File.separator+supportedLanguage);
				if(!destination.exists())
					destination.mkdirs();
				File source = new File(tempIndexPath+File.separator+supportedLanguage);
				copyFiles(source, destination);
			}  // end supportedlanguage for
			//----------- Copy the default language folder
			File destination = new File(indexPath+File.separator+"newIndexes"+File.separator+defaultLanguage);
			if(!destination.exists())
				destination.mkdirs();
			File source = new File(tempIndexPath+File.separator+defaultLanguage);
			copyFiles(source, destination);
		}else{
			File destination = new File(indexPath+File.separator+"newIndexes"+File.separator+defaultLanguage);
			if(!destination.exists())
				destination.mkdirs();
			
			File source = new File(tempIndexPath+File.separator+defaultLanguage);
			copyFiles(source, destination); 
		}//end else
		//----------- Rename the Indexes folder copied to timestamp in millis
		newIndexesFolder.renameTo(new File (indexPath+File.separator+System.currentTimeMillis()));
	}
	
	
	public boolean createModifyDocuments(IndexWriter iw, String[] fieldNames,
			String[] fieldValues){
		String keyWord = null, keyValue = null;
		if (fieldNames == null || fieldNames.length <= 1 || fieldValues == null
				|| fieldValues.length <= 1
				|| fieldNames.length != fieldValues.length)
			return false;
		try{
			keyWord = fieldNames[0];
			keyValue = fieldValues[0];
			//Creating doc and adding unique id to Keyword of the doc
			Document doc = new Document();
			Field f3=new Field(keyWord, keyValue, Field.Store.YES, Field.Index.UN_TOKENIZED); 
			doc.add(f3);
			//putting rest of the fields in SearchKeyword
			for (int i = 1; i < fieldNames.length; i++){
				String SearchKeyword = " ";
				if (fieldValues[i] == null)
					continue;
				try{
					Integer.parseInt(fieldValues[i]);
					//continue;
				}
				catch(Exception e){
					//Tools.logDetail(_class, "GuiSearch::Exception caught ", e.getMessage());
				}
				if (fieldNames[i].equalsIgnoreCase("vcode")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("clipId")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("clipRbtWavFile")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("PARENT_CAT_NAME")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				
				if (fieldNames[i].equalsIgnoreCase("SUB_CAT_TYPE")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				
				if (fieldNames[i].equalsIgnoreCase("CAT_PROMO_ID")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				
				
				if (fieldNames[i].equalsIgnoreCase("parentCatId")){
					//log.info("Inside parentCatId and indexing "+fieldValues[i]);
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("subCatId")){
					//log.info("Inside subCatId and indexing "+fieldValues[i]);
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				SearchKeyword = SearchKeyword + " "+ getPhonetics(fieldValues[i], false, true) + " "
					+ getPhonetics(fieldValues[i], true, true);
				StringTokenizer stk = new StringTokenizer(fieldValues[i], " ");
				String wordByWord = "";
				while (stk.hasMoreTokens()){
					String tkn = (String) stk.nextToken();
					wordByWord = wordByWord + getPhonetics(tkn, true, true) + " ";
					SearchKeyword = SearchKeyword + " "
					+ getPhonetics(tkn, true, true);
				}
				SearchKeyword = SearchKeyword + " " + wordByWord + fieldValues[i];
				SearchKeyword = SearchKeyword.trim() + " "+ getSubstring(fieldValues[i], 3);
				SearchKeyword = SearchKeyword.trim() + " "+ getSubstring(fieldValues[i], 4);
				Field f2=new Field(fieldNames[i], SearchKeyword.trim()+fieldValues[i].trim(),Field.Store.YES,Field.Index.TOKENIZED);
				doc.add(f2);
			}
			try{
				iw.addDocument(doc);
			}
			catch(Exception e){
				log.error(e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
		catch (Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	protected void optimizeIndexWriter(IndexWriter iw){
		try{
			iw.optimize();
			iw.close();
		}
		catch (Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static String getPhonetics(String input, boolean trimSpace, boolean isSupportPhonetic){
		if (!isSupportPhonetic || input == null)
			return null;
		try{
			// to check whether the input is a string or a number
			Integer.parseInt(input);
			return null;
		}
		catch(Exception e){
			//Phonetics not required for clip id, or vcode, or promo code.
			//log.error(e.getMessage());
		}
		if (trimSpace){
			StringTokenizer stk = new StringTokenizer(input, " ");
			if (stk.hasMoreTokens()){
				input = " ";
				while (stk.hasMoreTokens()){
					input = input.trim() + (String) stk.nextToken();
				}
			}
		}
		String phonem=null;
		if(input != null)
			phonem = m_metaphone.generateKey(input);
		if(phonem != null)
			phonem=phonem.trim();
		return phonem;
	}
	
	private static String getSubstring(String clipName, int n){
		String output = "";
		StringTokenizer stk = new StringTokenizer(clipName, " ");
		clipName = "";
		int i = 0;
		while (stk.hasMoreTokens()){
			clipName = clipName + (String) stk.nextToken();
		}
		int size = clipName.length();
		i = 0;
		while (i < (size - n)){
			output = output + clipName.substring(i, (i + n)) + " ";
			i++;
		}
		output = output + clipName.substring(i);
		return output;
	}
	
	
	/**
	 * Used to get the search folder. Deletes all the remaining folders except for the recent one.
	 * @return String
	 */
	protected String getIndexPath(String language){
		long largest = 0;
		File baseIndexDir = new File(indexPath);
		if(baseIndexDir.isDirectory()){
			File[] files = baseIndexDir.listFiles();
			//--------------- Get the largest digit folder for search
			for(int i=0;i<files.length;i++){
				if(i==0)
					largest = Long.parseLong(files[i].getName());
				long folderName = Long.parseLong(files[i].getName());
				largest = folderName>largest?folderName:largest;
			}
			//--------------- Delete any previous stale index folders
			String currentIndexFileName = String.valueOf(largest);
			for (int i = 0; i < files.length; i++)
			{
				if (!files[i].getName().equalsIgnoreCase(currentIndexFileName))
				{
					boolean deleteStatus = deleteFileRecursively(files[i]);
					log.info("Deleted the " + files[i].getName() + " with status: " + deleteStatus);
				}
			}
		}else{
			log.info("REPORT PATH IS NOT A DIRECTORY");
		}
		log.info("Folder Name for Search is "+largest);
		return indexPath+File.separator+largest+File.separator+language;
	}
	
	private boolean deleteFileRecursively(File file)
	{
		if (file == null || !file.exists())
			return false;

		if (file.isFile())
			return file.delete();

		File[] childFiles = file.listFiles();
		if (childFiles != null)
		{
			for (File childFile : childFiles)
			{
				deleteFileRecursively(childFile);
			}
		}

		return file.delete();
	}
	
	protected String identifyLanguage(String searchQuery){
		String language = null;
		language = LanguageIndentifier.getLanguage(searchQuery, CHAR_SET_UTF8);
		log.info("Language identified by the LanguageIdentifier for the query "+searchQuery+" is "+language);
		return language;
	}
	
	protected static String getQueryString(String clipName, boolean isSupportPhonetic){
		String query = "";
		StringTokenizer stk = new StringTokenizer(clipName, " ");
		String wordByWord = "";
		String tmp = "";
		while (stk.hasMoreTokens()){
			String tkn = (String) stk.nextToken();
			String phonem = getPhonetics(tkn, true, isSupportPhonetic);
			String word = "";
			if(phonem != null && phonem.trim().length() > 0){
				wordByWord = wordByWord + phonem + " ";
				word = word + " OR " + phonem;
			}			
			if(word.trim().length() > 0){
				tmp = tmp + word;
			}
		}
//		query = "("+clipName+" OR "+ getPhonetics(clipName, false) + " OR "
//		+ getPhonetics(clipName, true) + " OR " + wordByWord.trim()
//		+ " " + tmp.trim() + " OR " + getSubstring(clipName, 3)
//		+ " OR " + getSubstring(clipName, 4) + ")";
		StringBuilder buffer = new StringBuilder();
		buffer.append("(" + clipName);
		String temp = getPhonetics(clipName, false, isSupportPhonetic);
		if(temp != null && temp.trim().length() > 0 ){
			buffer.append( " OR "  + temp);
		}
		temp = getPhonetics(clipName, true, isSupportPhonetic);
		if(temp != null && temp.trim().length() > 0 ){
			buffer.append( " OR " + temp);
		}
		if(wordByWord.trim().length() > 0 || tmp.trim().length() > 0){
			buffer.append( " OR " );
		}
		if(wordByWord.trim().length() > 0){
			buffer.append(wordByWord.trim() + " ");
		}
		if(tmp.trim().length() > 0){
			buffer.append(tmp.trim());
		}
		if(isSupportPhonetic){
			buffer.append(" OR " + getSubstring(clipName, 3) + " OR " + getSubstring(clipName, 4));
		}
		buffer.append(")");
		//return query;
		return buffer.toString();
	}
	
	protected Analyzer getAnalyzer(String language) throws RBTLuceneException{
		Analyzer analyzer = null;
		try{
			if(languageAnalyzerMap.get(language)!=null){
				Class<?> analyzerClass = Class.forName(languageAnalyzerMap.get(language));
				analyzer = (Analyzer)analyzerClass.newInstance();
			}else{
				Class<?> analyzerClass = Class.forName(languageAnalyzerMap.get("default"));
				analyzer = (Analyzer)analyzerClass.newInstance();
			}
		}
		catch(Exception e){
			e.printStackTrace();
			log.error("Exception: Could not instantiate the class for Analyzer");
			log.error(e.getMessage());
			throw new RBTLuceneException(e.getMessage());
		}
		return analyzer;
	}
	
	private void copyFiles(File source, File destination){
		String [] children = source.list();
		log.info("Children list size "+children.length);
		 
		for(int i = 0; i < children.length; i++) {
			try {
				log.info("Children "+children[i]);
				InputStream in = new FileInputStream(new File(source, children[i]));
				OutputStream out = new FileOutputStream(new File(destination + File.separator + children [i]));
				byte [] buf = new byte[1024];
				int len;
				while((len = in.read (buf)) > 0) { 
					out.write(buf, 0, len);
				}
				in.close();
				out.close();
			} catch(IOException ioe) {
				ioe.printStackTrace();
			}
		}  //end for
	}

	public abstract SearchResponse searchCategory(String searchText, int noOfRows);

	public abstract List<String>  getSuggestions(String search, int rows);

	public abstract ArrayList<String> searchForArtists(String artist, int noOfRows);
	
	public abstract ArrayList<LuceneClip> searchForArtistSongs(String artist, int noOfRows);

	public abstract int getTotalArtistResults();
	
	public abstract int getTotalArtistSongResults();
	
}
