package com.onmobile.apps.ringbacktones.lucene.msearch;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.onmobile.apps.ringbacktones.lucene.LuceneCategory;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;
import com.onmobile.apps.ringbacktones.lucene.solr.ConfigReader;
import com.onmobile.apps.ringbacktones.lucene.solr.SearchConstants;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.LanguageIndentifier;

public class MSearch {

	private static DocumentBuilder db = null;
	private static final Logger logger = Logger.getLogger(MSearch.class);
	private static HttpClient m_httpClient = null;
	private static MSearch msearchInstance = null;

	private int totalArtistSearchSize = 0;
	private int totalArtistSongSearchSize = 0;
	
	public int getTotalArtistSearchSize() {
		return totalArtistSearchSize;
	}
	
	public int getTotalArtistSongSearchSize() {
		return totalArtistSongSearchSize;
	}
	
    static {
    	 try {
			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		 } catch (Exception ex) {
            logger.info("Exception While Initializing DocumentBuilder");
		 }
    }
    
	public MSearch() {
	
	}
	
	public static MSearch getInstance(){
        if(msearchInstance == null){
        	synchronized (MSearch.class) {
				if(msearchInstance==null){
					 msearchInstance = new MSearch();
				}
			}
         }
        return msearchInstance;
	}

	private MSearchClip[] getParsedClipData(String clipXML) {
		MSearchClip clips[] = null;
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					clipXML.trim().getBytes("UTF-8"));
			logger.info("ClipXml in getParsedClipData=" + clipXML);
			Document doc = null;
			if (db != null) {
				doc = db.parse(byteArrayInputStream);
			}
			Element root = doc.getDocumentElement();
			NodeList nodes = root.getElementsByTagName("contents");
			Element element1 = (Element) nodes.item(0);
			NodeList node2 = element1.getElementsByTagName("content");
			clips = new MSearchClip[node2.getLength()];
			for (int i = 0; i < node2.getLength(); i++) {
				Element element2 = (Element) node2.item(i);
				String clipName = element2.getAttribute("name");
				clips[i] = new MSearchClip();
				clips[i].setClipName(clipName);
				NodeList list2 = element2.getElementsByTagName("property");
				for (int j = 0; j < list2.getLength(); j++) {
					Element element3 = (Element) list2.item(j);
					if (element3.getAttribute("name")
							.equalsIgnoreCase("calbum")) {
						String albumName = element3.getAttribute("value");
						clips[i].setAlbumName(albumName);

					} else if (element3.getAttribute("name").equalsIgnoreCase(
							"RBTCODE")) {
						String promoId = element3.getAttribute("value");
						clips[i].setPromoId(promoId);
					}
				}
			}

		} catch (Exception e) {
			logger.error("Exception while populating MSearch clips." + e.getMessage(), e);
		}
		return clips;
	}

	public ArrayList<LuceneClip> searchClip(HashMap<String, String> paramsMap,
			String language, String queryLanguage) {
		String strURL = null;
		RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
		ArrayList<LuceneClip> luceneClipList = new ArrayList<LuceneClip>();
		if (paramsMap == null || paramsMap.size() == 0) {
			logger.error("Input paramters map is null/empty");
			return luceneClipList;
		}

		String searchChannel = paramsMap.get("searchChannel");
		String songQuery = paramsMap.get("song");
		String artist = paramsMap.get("artist");
		String album = paramsMap.get("album");
		String mSearchUrl = ConfigReader.getInstance().getParameter(
				SearchConstants.MSEARCH_URL);
		if (searchChannel != null)
		{
			String mSearchNameTuneUrl = ConfigReader.getInstance().getParameter(
					SearchConstants.MSEARCH_NAMETUNE_URL);
			if (mSearchNameTuneUrl != null)
				mSearchUrl = mSearchNameTuneUrl;
		}

		String subID = paramsMap.get("SUBSCRIBER_ID");
		if(subID == null){
			subID = ConfigReader.getInstance().getParameter(
					SearchConstants.MSEARCH_DEFAULT_SUBID);
		}
		String parentCatId = paramsMap.get("parentCatId");
		String subCatId = paramsMap.get("subCatId");
		if (parentCatId == null)
			parentCatId = ConfigReader.getInstance().getParameter(
					SearchConstants.DEFAULT_PARENT_CAT_ID);
		if (subCatId == null)
			subCatId = ConfigReader.getInstance().getParameter(
					SearchConstants.DEFAULT_SUB_CAT_ID);
		String query=null;
		String type=null;
		if (paramsMap.containsKey("all")) {
			query = paramsMap.get("all");
			type = ConfigReader.getInstance().getParameter(
					SearchConstants.MSEARCH_CRITERIA_ALL);
			if (type == null || type.trim().isEmpty()) {
				type = "songName,artist,album,nametune";
			}

		} else if(songQuery!=null&&songQuery.length()>0){
			query=songQuery;
			type="songName";
		}else if(artist!=null && artist.length()>0){
			query=artist;
			type="artist";
		}else if(album!=null&&album.length()>0){
			query=album;
			type="album";
		}else if(searchChannel != null && searchChannel.length()>0){
			query=searchChannel;
			type="nametune";
		}
		
	
		
		try {
			/*strURL = mSearchUrl + "msisdn=" + subID + "&query="
					+ URLEncoder.encode(songQuery, "UTF-8") + "&language="
					+ language;*/
			
			if(queryLanguage == null) {
				queryLanguage = LanguageIndentifier.getLanguage(query, "UTF-8");
				logger.info("Language identified by the LanguageIdentifier for the query "+query+" is "+queryLanguage);
			}
			
			strURL = mSearchUrl.replace("%MSISDN%", subID);
			strURL = strURL.replace("%LANGUAGE%", URLEncoder.encode(queryLanguage,"UTF-8"));
			strURL = strURL.replace("%QUERY%", URLEncoder.encode(query,"UTF-8"));
			strURL = strURL.replace("%TYPE%", URLEncoder.encode(type,"UTF-8"));
		} catch (Exception ex) {
			logger.error("Exception in .......Encoding MSearchUrl");
			logger.error("Exception in .......Encoding MSearchUrl",ex);
		}
		
		logger.info("SongSearch URL=" + strURL);
		String response = null;
		String proxyHost = ConfigReader.getInstance().getParameter(SearchConstants.PROXY_HOST);
		String proxyPort = ConfigReader.getInstance().getParameter(SearchConstants.PROXY_PORT);
		int timeOut = 90000;
		int confProxyPort = 0;
		boolean useProxy = false;
		if(proxyHost!=null && proxyPort!=null){
			try{
			   confProxyPort = Integer.parseInt(proxyPort);
			   timeOut = Integer.parseInt(ConfigReader.getInstance().getParameter(SearchConstants.PARAM_CONNECTION_TIMEOUT));
			   useProxy = true;
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		String clipXML = callURL(strURL, -1, response, useProxy, proxyHost, confProxyPort, true,
				timeOut);
		MSearchClip clips[] = getParsedClipData(clipXML);
		if(clips == null)
			return null;
		String[] promoIds = new String[clips.length];
		if (clips != null) {
			for (int i = 0; i < clips.length; i++) {
				promoIds[i] = clips[i].getPromoId();
				Clip clip = null;
				String contentType = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_MSEARCH_RESULT_CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase("clipid"))
					clip = rbtCacheManager.getClip(promoIds[i],language);
				else
					clip = rbtCacheManager.getClipByPromoId(promoIds[i],language);

				if(clip!=null)
				{
				LuceneClip luceneClip1 = new LuceneClip(clip, Long
						.parseLong(parentCatId), Long.parseLong(subCatId),
						null, null);
				luceneClipList.add(luceneClip1);
				}
			}
		}

		return luceneClipList;

	}

	private String callURL(String strURL, Integer statusCode, String response,
			boolean useProxy, String proxyHost, int proxyPort, boolean toRetry,
			int timeOut) {
		
		GetMethod get = null;
		int m_timeOutInMilliSecond = timeOut;

		try {
			strURL = strURL.trim();
			URL oSrc = new URL(strURL);
			String strHostIp = oSrc.getHost();
			int iHostPort = oSrc.getPort();
			HostConfiguration ohcfg = new HostConfiguration();
			ohcfg.setHost(strHostIp, iHostPort);
			java.net.InetAddress ip = java.net.InetAddress.getLocalHost();
			String IPAddress = ip.getHostAddress();

			if (!strHostIp.equalsIgnoreCase("localhost")
					&& !strHostIp.equalsIgnoreCase(IPAddress))
				if (useProxy && proxyHost != null && proxyPort != -1)
					ohcfg.setProxy(proxyHost, proxyPort);
			if (m_httpClient == null) {
				MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
				connectionManager.getParams().setStaleCheckingEnabled(true);
				connectionManager.getParams().setMaxConnectionsPerHost(ohcfg,
						10);
				connectionManager.getParams().setMaxTotalConnections(20);
				connectionManager.getParams().setSoTimeout(
						m_timeOutInMilliSecond);
				connectionManager.getParams().setConnectionTimeout(
						m_timeOutInMilliSecond);
				m_httpClient = new HttpClient(connectionManager);
				DefaultHttpMethodRetryHandler retryhandler = null;
				if (toRetry) {
					retryhandler = new DefaultHttpMethodRetryHandler(3, false);
				} else {
					retryhandler = new DefaultHttpMethodRetryHandler(0, false);
				}
				m_httpClient.getParams().setParameter(
						HttpMethodParams.RETRY_HANDLER, retryhandler);
				m_httpClient.getParams().setSoTimeout(m_timeOutInMilliSecond);
				m_httpClient.setTimeout(m_timeOutInMilliSecond);
			}
			get = new GetMethod(strURL);
			statusCode = new Integer(m_httpClient.executeMethod(ohcfg, get));
			logger.info("Status_Code=" + statusCode);
			response = get.getResponseBodyAsString();
			// logger.info("Response from URL=="+response);
			return response;

		} catch (Throwable e) {
			logger.error("Message=" + e.getMessage(), e);
			return response;
		} finally {
			if (get != null)
				get.releaseConnection();
		}

	}
	
	//RBT-9871
	public ArrayList<LuceneClip> callURLformultiFeildmsearch(String multiFeildmSearchURL,HashMap map)
	{
		RBTCacheManager rbtCacheManager = RBTCacheManager.getInstance();
		ArrayList<LuceneClip> luceneClipList = new ArrayList<LuceneClip>();
		String proxyHost = ConfigReader.getInstance().getParameter(SearchConstants.PROXY_HOST);
		String proxyPort = ConfigReader.getInstance().getParameter(SearchConstants.PROXY_PORT);
		String response = null;
		int timeOut = 90000;
		int confProxyPort = 0;
		boolean useProxy = false;
		if(proxyHost!=null && proxyPort!=null){
			try{
			   confProxyPort = Integer.parseInt(proxyPort);
			   timeOut = Integer.parseInt(ConfigReader.getInstance().getParameter(SearchConstants.PARAM_CONNECTION_TIMEOUT));
			   useProxy = true;
			}catch(Exception ex){
				ex.printStackTrace();
			}
		}
		String clipXML=callURL(multiFeildmSearchURL, -1, response, useProxy, proxyHost, confProxyPort, true, timeOut);
		logger.info("msearch returning clipXML:"+clipXML);
		MSearchClip clips[] = getmultiFeildmsearchParsedClipData(clipXML);
		
		if(clips == null)
			return null;
		String[] promoIds = new String[clips.length];
		if (clips != null) {
			for (int i = 0; i < clips.length; i++) {
				promoIds[i] = clips[i].getPromoId();
				Clip clip = null;
				String contentType = ConfigReader.getInstance().getParameter(SearchConstants.PARAM_MSEARCH_RESULT_CONTENT_TYPE);
				if (contentType != null && contentType.equalsIgnoreCase("clipid"))
					clip = rbtCacheManager.getClip(promoIds[i]);
				else
					clip = rbtCacheManager.getClipByPromoId(promoIds[i]);

				if(clip!=null)
				{
					//RBT-11300
					Long dummyCatId=3L;
					try {
						dummyCatId = Long.parseLong(ConfigReader.getInstance().getParameter(SearchConstants.PARAM_DUMMY_CATEGORY_ID));
						logger.info("getting dummy_category_id: "+dummyCatId);
					}catch(Exception e) {
						logger.info("Exception while getting dummy_category_id."+e);
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
				luceneClipList.add(lucClip);
				}
				
			}
		}
		logger.info("luceneClipList size is: "+luceneClipList.size());	
		return luceneClipList;
	}
	//RBT-9871
	private MSearchClip[] getmultiFeildmsearchParsedClipData(String clipXML) {
		MSearchClip clips[] = null;
		try {
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(
					clipXML.trim().getBytes("UTF-8"));
			
			Document doc = null;
			if (db != null) {
				doc = db.parse(byteArrayInputStream);
			}
			Element root = doc.getDocumentElement();
			NodeList nodes = root.getElementsByTagName("result");
			Element element1 = (Element) nodes.item(0);
			NodeList node1 = element1.getElementsByTagName("doc");
			clips = new MSearchClip[node1.getLength()];
			for (int i = 0; i < node1.getLength(); i++) {
				Element element2 = (Element) node1.item(i);
				NodeList node2 = element2.getElementsByTagName("str");
				clips[i] = new MSearchClip();
				for (int j = 0; j < node2.getLength(); j++) {
					
					Element element3 = (Element) node2.item(j);
					
					if (element3.getAttribute("name")
							.endsWith("RBTID")) {
						String promoId = element3.getTextContent();
						clips[i].setPromoId(promoId);

					} 
				}
			}

		} catch (Exception e) {
			logger.error("Exception while populating MSearch clips." + e.getMessage(), e);
		}
		logger.info("clips size retuned after parsing: "+clips.length);
		return clips;
	}

	private SearchResponse parseXml(String xmlResponse, int rows) {

		int categoryId;
		String totalCategoryResults = null;
		ArrayList<LuceneCategory> list = new ArrayList<LuceneCategory>();

		try {
			logger.debug("XML RECORDS == " + xmlResponse);
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlResponse));

			Document doc = db.parse(is);

			logger.info("Parsing Done!");

			NodeList nodeList = doc.getElementsByTagName("doc");
			if (nodeList != null) {
				for (int x = 0; x < nodeList.getLength(); x++) {
					Node node = nodeList.item(x);
					// if (node.getNodeType() == Node.ELEMENT_NODE) {
					Element element = (Element) node;
					NodeList nodelist1 = element.getElementsByTagName("str");
					LuceneCategory luceneCategory = null;
					for (int j = 0; j < nodelist1.getLength(); j++) {

						Element element1 = (Element) nodelist1.item(j);
						if (element1.getAttribute("name").endsWith(
								"CATEGORYID")) {
							categoryId = Integer.parseInt(element1
									.getTextContent());
							Category cat = RBTCacheManager.getInstance().getCategory(categoryId);
							if (null != cat) {
								luceneCategory = new LuceneCategory(cat, -1, null);
							} else {
								logger.info("No category found in cache/DB for catId: " + categoryId);
							}
						}
					}
					if (null != luceneCategory) {
						list.add(luceneCategory);
					} else {
						logger.info("Got null object so ignoring at index " + x);
					}
					if (rows == list.size()) {
						break;
					}
				}
				NodeList nodeList3 = doc.getElementsByTagName("result");
				NamedNodeMap attributes = nodeList3.item(0).getAttributes();
				/* we should not take total results from the xml response as there might be deviations 
				 * in some cases like category expired, category does not exist in cache/DB, so reading 
				 * the total search result size from valid converted categories from our cache/DB */
				totalCategoryResults = list.size()+"";
//				totalCategoryResults = attributes.getNamedItem("numFound").getNodeValue();
			} else {
				logger.error("Improper XML response");
			}

		} catch (ParserConfigurationException e) {
			logger.error("Unable to parse xml: " + xmlResponse, e);
		} catch (SAXException e) {
			logger.error("Unable to parse xml: " + xmlResponse, e);
		} catch (IOException e) {
			logger.error("Unable to parse xml: " + xmlResponse, e);
		}
		SearchResponse searchResponse = new SearchResponse(
				totalCategoryResults, list);
		return searchResponse;

	}

	public SearchResponse searchCategory(String searchText,
			int noOfRows) {

		logger.info("In searchCategory with values search: " + searchText
				+ " noOfRows:" + noOfRows);
		String mSearchUrl = ConfigReader.getInstance().getParameter(
				SearchConstants.CATEGORY_MSEARCH_URL);
		logger.debug("Msearch url got for "
				+ SearchConstants.CATEGORY_MSEARCH_URL + " as " + mSearchUrl);
		String strURL = mSearchUrl;
		try {
			strURL = strURL.replace("%QUERY%", URLEncoder.encode(searchText, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			logger.error("Exception while encoding the search string: " + ex.getMessage(), ex);
		}
		strURL = strURL.replace("%NUM_OF_RESULTS%", String.valueOf(noOfRows));
		
		// logger.info(strURL);
		String response = null;
		String proxyHost = ConfigReader.getInstance().getParameter(
				SearchConstants.PROXY_HOST);
		String proxyPort = ConfigReader.getInstance().getParameter(
				SearchConstants.PROXY_PORT);
		int timeOut = 90000;
		int confProxyPort = 0;
		boolean useProxy = false;
		try {
			if (proxyHost != null) {
				confProxyPort = Integer.parseInt(proxyPort);
				useProxy = true;

			}
		} catch (Exception ex) {
			logger.error(ex);
		}
		try {
			timeOut = Integer.parseInt(ConfigReader.getInstance().getParameter(
					SearchConstants.PARAM_CONNECTION_TIMEOUT));

		} catch (Exception ex) {
			logger.error(ex);
		}
		Integer statusCode = -1;
		boolean toRetry = true;
		String clipXML = callURL(strURL, statusCode, response, useProxy,
				proxyHost, confProxyPort, toRetry, timeOut);
		logger.info("URL:" + strURL + " clipXML:" + clipXML);
		SearchResponse list = parseXml(clipXML, noOfRows);
		return list;
	}

	public List<String> getSuggestions(String search, int rows) {

		ArrayList<String> list = new ArrayList<String>();

		String mSearchUrl = ConfigReader.getInstance().getParameter(
				SearchConstants.SUGGESTION_URL);
		String strURL;
		strURL = mSearchUrl.replace("%QUERY%", search);
		String noofRows = String.valueOf(rows);

		strURL = strURL.replace("%NUM_OF_RESULTS%", noofRows);
		logger.info("URL hit " + strURL);
		String response = null;
		String proxyHost = ConfigReader.getInstance().getParameter(
				SearchConstants.PROXY_HOST);
		String proxyPort = ConfigReader.getInstance().getParameter(
				SearchConstants.PROXY_PORT);
		int timeOut = 90000;
		int confProxyPort = 0;
		boolean useProxy = false;

		try {
			if (proxyHost != null) {
				confProxyPort = Integer.parseInt(proxyPort);
				useProxy = true;

			}
		} catch (Exception ex) {
			logger.error(ex);
		}
		try {
			timeOut = Integer.parseInt(ConfigReader.getInstance().getParameter(
					SearchConstants.PARAM_CONNECTION_TIMEOUT));

		} catch (Exception ex) {
			logger.error(ex);
		}
		Integer statusCode = -1;
		boolean toRetry = true;
		String jsonResp = callURL(strURL, statusCode, response, useProxy,
				proxyHost, confProxyPort, toRetry, timeOut);
		logger.info("jsonResp :" + jsonResp);
		JSONParser jsonParser = new JSONParser();
		JSONObject jsonObject = null;
		try {
			jsonObject = (JSONObject) jsonParser.parse(jsonResp);
		} catch (ParseException e) {
			logger.error(e);
			return null;
		}
		JSONObject structure = (JSONObject) jsonObject.get("terms");
		JSONArray jsonArray = (JSONArray) structure.get("suggestions");
		Iterator<String> i = jsonArray.iterator();
		int k = 0;
		while (i.hasNext()) {
			list.add(k, i.next());
			k++;
			i.next();
			if (k >= rows) {
				break;
			}
		}

		return list;

	}
	
	public ArrayList<String> searchForArtists(String artist, int noOfRows) {

		logger.info("In searchForArtists with values: " + artist
				+ " noOfRows:" + noOfRows);
		String mSearchUrl = ConfigReader.getInstance().getParameter(
				SearchConstants.ARTIST_MSEARCH_URL);
		logger.debug("Msearch artist url got for "
				+ SearchConstants.ARTIST_MSEARCH_URL + " as " + mSearchUrl);
		String strURL = mSearchUrl;
		
		try {
			strURL = strURL.replace("%QUERY%", URLEncoder.encode(artist, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			logger.error("Exception while encoding the artist search string: " + ex.getMessage(), ex);
		}
		strURL = strURL.replace("%NUM_OF_RESULTS%", String.valueOf(noOfRows));
		
		String response = null;
		String proxyHost = ConfigReader.getInstance().getParameter(
				SearchConstants.PROXY_HOST);
		String proxyPort = ConfigReader.getInstance().getParameter(
				SearchConstants.PROXY_PORT);
		int timeOut = 90000;
		int confProxyPort = 0;
		boolean useProxy = false;
		try {
			if (null != proxyHost && !"".equals(proxyHost)) {
				confProxyPort = Integer.parseInt(proxyPort);
				useProxy = true;
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
		try {
			timeOut = Integer.parseInt(ConfigReader.getInstance().getParameter(
					SearchConstants.PARAM_CONNECTION_TIMEOUT));
		} catch (Exception ex) {
			logger.error(ex);
		}
		Integer statusCode = -1;
		boolean toRetry = true;
		String clipXML = callURL(strURL, statusCode, response, useProxy,
				proxyHost, confProxyPort, toRetry, timeOut);
		
		logger.info("URL:" + strURL + " clipXML:" + clipXML);
		ArrayList<String> artistSearchList = parseArtistResponseXml(clipXML, noOfRows);
		
		return artistSearchList;
	}
	
	private ArrayList<String> parseArtistResponseXml(String xmlResponse, int rows) {

		String totalArtistResults = null;
		ArrayList<String> artistList = new ArrayList<String>();
		logger.debug("Artist XML Response: " + xmlResponse);

		try {
			DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlResponse));

			Document doc = db.parse(is);
			Element element = doc.getDocumentElement();
			
			NodeList list = element.getElementsByTagName("arr");
			String artist = "";
	        if (list != null && list.getLength() > 0) {
	        	for (int i = 0; i < list.getLength(); i++) {
	        		NodeList subList = list.item(i).getChildNodes();
		            if (subList != null && subList.getLength() > 0) {
		                artist = (subList.item(0).getTextContent());
		                artistList.add(artist);
		            }
	            }
	        } else {
				logger.error("Improper XML response");
			}

			NodeList nodeList3 = doc.getElementsByTagName("result");
			NamedNodeMap attributes = nodeList3.item(0).getAttributes();
			totalArtistResults = attributes.getNamedItem("numFound").getNodeValue();
			logger.info("Artist XML parsing Done!");

		} catch (ParserConfigurationException e) {
			logger.error("Unable to parse artist xml: " + xmlResponse, e);
		} catch (SAXException e) {
			logger.error("Unable to parse artist xml: " + xmlResponse, e);
		} catch (IOException e) {
			logger.error("Unable to parse artist xml: " + xmlResponse, e);
		}
		
		totalArtistSearchSize = Integer.valueOf(totalArtistResults);
		
		return artistList;
	}
	
	public ArrayList<LuceneClip> searchForArtistSongs(String artist, int noOfRows) {

		logger.info("In searchForArtistSongs with values: " + artist
				+ " noOfRows:" + noOfRows);
		String mSearchUrl = ConfigReader.getInstance().getParameter(
				SearchConstants.ARTIST_SONGS_MSEARCH_URL);
		logger.debug("Msearch artist songs url got for "
				+ SearchConstants.ARTIST_SONGS_MSEARCH_URL + " as " + mSearchUrl);
		String strURL = mSearchUrl;
		
		try {
			strURL = strURL.replace("%QUERY%", URLEncoder.encode(artist, "UTF-8"));
		} catch (UnsupportedEncodingException ex) {
			logger.error("Exception while encoding the artist search string: " + ex.getMessage(), ex);
		}
		strURL = strURL.replace("%NUM_OF_RESULTS%", String.valueOf(noOfRows));
		
		String response = null;
		String proxyHost = ConfigReader.getInstance().getParameter(
				SearchConstants.PROXY_HOST);
		String proxyPort = ConfigReader.getInstance().getParameter(
				SearchConstants.PROXY_PORT);
		int timeOut = 90000;
		int confProxyPort = 0;
		boolean useProxy = false;
		try {
			if (null != proxyHost && !"".equals(proxyHost)) {
				confProxyPort = Integer.parseInt(proxyPort);
				useProxy = true;
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
		try {
			timeOut = Integer.parseInt(ConfigReader.getInstance().getParameter(
					SearchConstants.PARAM_CONNECTION_TIMEOUT));
		} catch (Exception ex) {
			logger.error(ex);
		}
		Integer statusCode = -1;
		boolean toRetry = true;
		String clipXML = callURL(strURL, statusCode, response, useProxy,
				proxyHost, confProxyPort, toRetry, timeOut);
		
		logger.info("URL: " + strURL + ", clipXML: " + clipXML);
		ArrayList<LuceneClip> artistSongsSearchList = parseArtistSongsResponseXml(clipXML, noOfRows);
		
		return artistSongsSearchList;
	}
	
	private ArrayList<LuceneClip> parseArtistSongsResponseXml(String xmlResponse, int rows) {

		int clipId;
//		String totalCategoryResults = null;
		ArrayList<LuceneClip> clipList = new ArrayList<LuceneClip>();

		try {
			logger.debug("XML RECORDS == " + xmlResponse);
			DocumentBuilder db = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			InputSource is = new InputSource();
			is.setCharacterStream(new StringReader(xmlResponse));

			Document doc = db.parse(is);

			logger.info("Parsing Done!");

			NodeList nodeList = doc.getElementsByTagName("doc");
			if (nodeList != null) {
				for (int x = 0; x < nodeList.getLength(); x++) {
					Node node = nodeList.item(x);
					Element element = (Element) node;
					NodeList nodelist1 = element.getElementsByTagName("str");
					LuceneClip luceneClip = null;
					for (int j = 0; j < nodelist1.getLength(); j++) {

						Element element1 = (Element) nodelist1.item(j);
						if (element1.getAttribute("name").endsWith("RBTID")) {
							clipId = Integer.parseInt(element1.getTextContent());
							Clip clip = RBTCacheManager.getInstance().getClip(clipId);
							if (null != clip) {
								luceneClip = new LuceneClip(clip, -1, -1, null, null);
							} else {
								logger.info("No clip found in cache/DB for clipId: " + clipId);
							}
						}
					}
					if (null != luceneClip) {
						clipList.add(luceneClip);
					} else {
						logger.info("Got null object so ignoring at index " + x);
					}
					if (rows == clipList.size()) {
						break;
					}
				}
				NodeList nodeList3 = doc.getElementsByTagName("result");
				NamedNodeMap attributes = nodeList3.item(0).getAttributes();
				/* we should not take total results from the xml response as there might be deviations 
				 * in some cases like clip expired, clip does not exist in cache/DB, so reading 
				 * the total search result size from valid converted clips from our cache/DB */
//				totalCategoryResults = attributes.getNamedItem("numFound").getNodeValue();
				totalArtistSongSearchSize = clipList.size();
			} else {
				logger.error("Improper XML response");
			}

		} catch (ParserConfigurationException e) {
			logger.error("Unable to parse xml: " + xmlResponse, e);
		} catch (SAXException e) {
			logger.error("Unable to parse xml: " + xmlResponse, e);
		} catch (IOException e) {
			logger.error("Unable to parse xml: " + xmlResponse, e);
		}

//		totalArtistSongSearchSize = Integer.valueOf(totalCategoryResults);
		
		return clipList;

	}
	
}
