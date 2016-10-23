package com.onmobile.apps.ringbacktones.rbtcontents.utils;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.MissingResourceException;
import java.util.Set;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.SimpleHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.CategoryClipMap;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipInfo;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.v2.utils.ITPHitUtils;
import com.onmobile.apps.ringbacktones.rbtcontents.v2.utils.UtilsFactory;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class TPHitUtils {
	private static Logger basicLogger = Logger.getLogger(TPHitUtils.class);

	private static List<String> azaanCopticDoaaCategoriesList = new ArrayList<String>();
	private static boolean isODACategoriesSupported = false;
	private static boolean isRadioCategoriesSupported = false;
	private static boolean isFestivalNametuneCategoriesSupported = false;
	private static boolean isPlaylistCategoriesSupported = false;

	private static List<String> odaCategoryTypes = null;

	static {
		String azaanCopticDoaaCategories = null;
		azaanCopticDoaaCategories = RBTContentJarParameters.getInstance().getParameter(
				"AZAAN_COPTIC_DOAA_CATEGORIES");
		if (azaanCopticDoaaCategories != null) {
			azaanCopticDoaaCategoriesList = Arrays.asList(azaanCopticDoaaCategories.split(","));
		}
		basicLogger.info("azaanCopticDoaaCategoriesList:" + azaanCopticDoaaCategoriesList);
		
		isODACategoriesSupported = Boolean.valueOf(RBTContentJarParameters.getInstance().getParameter(
				"IS_ODA_CATEGORIES_SUPPORTED"));
		basicLogger.info("isODACategoriesSupported:" + isODACategoriesSupported);
		isRadioCategoriesSupported = Boolean.valueOf(RBTContentJarParameters.getInstance().getParameter(
				"IS_RADIO_CATEGORIES_SUPPORTED"));
		basicLogger.info("isRadioCategoriesSupported:" + isRadioCategoriesSupported);
		isFestivalNametuneCategoriesSupported = Boolean.valueOf(RBTContentJarParameters.getInstance().getParameter(
				"IS_FESTIVAL_NAMETUNE_CATEGORIES_SUPPORTED"));
		basicLogger.info("isFestivalNametuneCategoriesSupported:" + isFestivalNametuneCategoriesSupported);
		isPlaylistCategoriesSupported = Boolean.valueOf(RBTContentJarParameters.getInstance().getParameter(
				"IS_PLAYLIST_SUPPORTED"));
		basicLogger.info("isPlaylistCategoriesSupported:" + isPlaylistCategoriesSupported);
		
		String odaCategoryTypesString = RBTContentJarParameters.getInstance().getParameter(
				"ODA_CATEGORY_TYPES");
		basicLogger.info("odaCategoryTypesString:" + odaCategoryTypesString);
		if (odaCategoryTypesString != null && !odaCategoryTypesString.trim().isEmpty()) {
			odaCategoryTypes = Arrays.asList(odaCategoryTypesString.split(","));
		}
		if (odaCategoryTypes == null || odaCategoryTypes.isEmpty()){
			odaCategoryTypes = new ArrayList<String>();
			odaCategoryTypes.add("16");
			odaCategoryTypes.add("20");
		}
		basicLogger.info("odaCategoryTypes:" + odaCategoryTypes);
	}

	public static void updateCorrespondingCategorySetForTPHit(CategoryClipMap categoryClipMap) {
		if (categoryClipMap == null) {
			return;
		}
		Category category = RBTCacheManager.getInstance().getCategory(categoryClipMap.getCategoryId());
		if(category == null) {
			return;
		}
		if (isODACategoriesSupported
				&& odaCategoryTypes != null
				&& odaCategoryTypes.contains(String.valueOf(category
						.getCategoryTpe()))) {
			RBTCache.odaCategoryIdsForTPHit.add(category.getCategoryId()+"");
		} else if (category.getCategoryTpe() == 23) {
			if (azaanCopticDoaaCategoriesList.contains(category.getCategoryId() + "")) {	
				RBTCache.azaanCopticDoaaCategoryClipMapsForTPHit.add(categoryClipMap);
			} else if (isRadioCategoriesSupported) {
				RBTCache.radioCategoryClipMapsForTPHit.add(categoryClipMap);
			}
		} else if (isFestivalNametuneCategoriesSupported && category.getCategoryTpe() == 27) {
			RBTCache.festivalNameTuneCategoryClipMapsForTPHit.add(categoryClipMap);
		} else if(isPlaylistCategoriesSupported && category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
			RBTCache.playListCategoryIdsForTPHit.add(categoryClipMap.getCategoryId());
		}
	}

	public static void updateODACategoryInTP() throws Exception{
		String allowUpdateTP = "FALSE";
		try{
			allowUpdateTP = RBTContentJarParameters.getInstance().getParameter("UPDATE_TP");
		}
		catch(MissingResourceException mse){
			allowUpdateTP = "FALSE";

		}
		basicLogger.info("allowUpdateTP: " + allowUpdateTP);
		if(!"TRUE".equalsIgnoreCase(allowUpdateTP)){			
			return;
		}
		basicLogger.info("************************************************************************************************************************");
		basicLogger.info("updateCategoryInTP started...");
		basicLogger.info("");
		makeHitToTPODAUrl(RBTCache.odaCategoryIdsForTPHit);		
	}

	public static void updateRadioCategoryInTP() throws Exception{
		String allowRadioUpdateTP = "FALSE";
		try{
			allowRadioUpdateTP = RBTContentJarParameters.getInstance().getParameter("RADIO_UPDATE_TP");
		}
		catch(MissingResourceException mse){
			allowRadioUpdateTP = "FALSE";

		}
		basicLogger.info("allowRadioUpdateTP: " + allowRadioUpdateTP);
		if(!"TRUE".equalsIgnoreCase(allowRadioUpdateTP)){
			return;
		}
		basicLogger.info("************************************************************************************************************************");
		basicLogger.info("updateRadioCategoryInTP started...");
		basicLogger.info("");
		makeHitToRadioTPUrl(RBTCache.radioCategoryClipMapsForTPHit);		
	}

	public static void updateAzaanCopticDoaaCategoryInTP() throws Exception{
		String azaanCopticDoaaCategories = null;
		try{
			azaanCopticDoaaCategories = RBTContentJarParameters.getInstance().getParameter("AZAAN_COPTIC_DOAA_CATEGORIES");
		}
		catch(MissingResourceException mse){
			azaanCopticDoaaCategories = null;
		}
		if(azaanCopticDoaaCategories == null){ 
			return;
		}
		basicLogger.info("************************************************************************************************************************");
		basicLogger.info("updateAzaanCopticDoaaCategoryInTP started...");
		basicLogger.info("");
		makeHitToAzaanCopticDoaaTPUrl(RBTCache.azaanCopticDoaaCategoryClipMapsForTPHit);		
	}

	public static void updateFestivalNameTuneCategoryInTP() throws Exception{
		String allowFestivalNameTuneUpdateTP = "FALSE";
		try{
			allowFestivalNameTuneUpdateTP = RBTContentJarParameters.getInstance().getParameter("FESTIVAL_NAMETUNE_UPDATE_TP");
		}
		catch(MissingResourceException mse){
			allowFestivalNameTuneUpdateTP = "FALSE";

		}
		basicLogger.info("allowFestivalNameTuneUpdateTP: " + allowFestivalNameTuneUpdateTP);
		if(!"TRUE".equalsIgnoreCase(allowFestivalNameTuneUpdateTP)){
			return;
		}
		basicLogger.info("************************************************************************************************************************");
		basicLogger.info("updateFestivalNmaeTuneCategoryInTP started...");
		basicLogger.info("");
		makeHitToFestivalNameTuneTPUrl(RBTCache.festivalNameTuneCategoryClipMapsForTPHit);		
	}

	public static void makeHitToTPODAUrl(Set<String> oDACategoryIdSet) throws Exception{
		basicLogger.info("makeHitToTPODAUrl call. Size: " + oDACategoryIdSet.size());
//		String url = RBTContentJarParameters.getInstance().getParameter("HTTP_URL");
		String operators = RBTContentJarParameters.getInstance().getParameter("operator_ids");
		String rbt_deployment_type = RBTContentJarParameters.getInstance().getParameter("rbt_deployment_type");
		String tp_hit_impl_class = RBTContentJarParameters.getInstance().getParameter("tp_hit_impl_class");
		String operator[] = null;
//		if(url == null || (url = url.trim()).equals("")) {
//			basicLogger.info("HTTP_URL is not configured in rbtcontentjar.properties, so we not update the clip indexing in TP");
//			System.out.println("HTTP_URL is not configured in rbtcontentjar.properties, so we not update the clip indexing in TP");
//			return;
//		}

		if (oDACategoryIdSet.size() == 0) {
			basicLogger.info("Automatic shuffle category list size is 0, because this release doesn't contains automatic shuffle category release. So we are not hit the TP url");
			System.out.println("Automatic shuffle category list size is 0, because this release doesn't contains automatic shuffle category release. So we are not hit the TP url");
			return;
		}

		StringBuilder sb = new StringBuilder();
		try
		{
			Iterator<String> iterator = oDACategoryIdSet.iterator();
			sb.append("<update>");
			while (iterator.hasNext())
			{
				String categoryId = iterator.next();
				basicLogger.info("categoryId: " + categoryId);
				String topSongCount = RBTContentJarParameters.getInstance().getParameter("NUMBER_OF_TOP_SONGS_" + categoryId);
				if (topSongCount == null) {
					Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(categoryId));
					if (category != null) {
						topSongCount = RBTContentJarParameters.getInstance().getParameter("NUMBER_OF_TOP_SONGS_" + category.getCategoryTpe());	
					}
				}
				int itopSongCount = Integer.parseInt(topSongCount);

				//List<CategoryClipMap> clipmaps = CategoryClipMapDAO.getClipsInCategory(Integer.parseInt(categoryId));
				Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(Integer.parseInt(categoryId));
				int clipMapSize = clips.length;
				StringBuilder wavFileBuilder = new StringBuilder("");
				for (int clipCount = 0; clipCount < clipMapSize
						&& clipCount < itopSongCount; clipCount++)
				{
					Clip clip = clips[clipCount];
					if(clip != null){
						wavFileBuilder.append(clip.getClipRbtWavFile() + ",");
					}
				}
				String wavFile = wavFileBuilder.toString();
				if (wavFile.length() > 0) {
					wavFile = wavFile.substring(0, wavFile.length() -1);
				}
				if(wavFile != null && !(wavFile = wavFile.trim()).equals("")){
					sb.append("<feed name = \"");
					sb.append(categoryId);
					sb.append("\" value = \"");
					sb.append(wavFile);
					sb.append("\"/>");
				}

			}
			sb.append("</update>");
			String xml = sb.toString();
			basicLogger.info("XML Request : " + xml);
			if(null != operators){
				operator = operators.split(",");
			}
			
			if(null != rbt_deployment_type && rbt_deployment_type.equalsIgnoreCase("RBT2.0") && operator != null && operator.length > 0){
				UtilsFactory factory = new UtilsFactory();
				ITPHitUtils tPHitUtils = factory.getTPHitUtils(tp_hit_impl_class);
				if (tPHitUtils != null) {
					basicLogger.info("Going for TP hit for multiple oprtator");
					for (int i = 0; i < operator.length; i++) {
						tPHitUtils.hitUrl(xml, operator[i]);
					}
				}
				basicLogger.info("Successfully hit toneplayer ODA");
			}else{
				hitUrl(xml);
			}
			//			if(response != null){
			//				String responseString = new String(response).trim();
			//				if(responseString.equalsIgnoreCase("SUCCESS")){
			//					//write into log file
			//					basicLogger.info("We are getting successfull response from Tone Player [Response : " + responseString + " ] ");
			//					System.out.println("We are getting successfull response from Tone Player [Response : " + responseString + " ] ");
			//				}
			//				else{
			//					//write into log file
			//					basicLogger.info("Please hit the url by execute following commands. Because we are not getting successfull response from Tone Player [Response : " + responseString + " ] ");
			//					System.out.println("Please hit the url by execute following commands. Because we are not getting successfull response from Tone Player [Response : " + responseString + " ] ");
			//				}
			//			}
			//			else{
			//				//get bad request and inform to ops team and re-execute the process.
			//				basicLogger.info("We are getting no response from Tone Player. Please hit the url by executig following commands");
			//				System.out.println("We are getting no response from Tone Player. Please hit the url by executig following commands");
			//			}
		}  catch (Exception e){
			basicLogger.error("Exception while sending request to Tone Player", e);
			throw e;
		}
	}

	public static byte[] hitUrl(String xml){
		HttpClient client = null;
		GetMethod getMethod = null;
		ByteArrayOutputStream bos = null;
		//        HostConfiguration config = new HostConfiguration();
		int statuscode;
		InputStream in = null;

		String urls = RBTContentJarParameters.getInstance().getParameter("HTTP_URL");
		String connectionTimeOut = RBTContentJarParameters.getInstance().getParameter("CONNECTION_TIMEOUT");
		String soTimeOut = RBTContentJarParameters.getInstance().getParameter("SO_TIMEOUT");
		String totalConnections = RBTContentJarParameters.getInstance().getParameter("TOTAL_CONNECTION");;
		String maxHostConnection = RBTContentJarParameters.getInstance().getParameter("MAX_HOST_CONNECTION");
		String proxyHost = RBTContentJarParameters.getInstance().getParameter("PROXY_HOST");
		String proxyPort = RBTContentJarParameters.getInstance().getParameter("PROXY_PORT");

		SimpleHttpConnectionManager multiThreadedHttpConnectionManager = new SimpleHttpConnectionManager();

		HttpConnectionManagerParams httpConnectionManagerParams = multiThreadedHttpConnectionManager.getParams();
		httpConnectionManagerParams.setConnectionTimeout(getParameterASInt(connectionTimeOut));
		httpConnectionManagerParams.setSoTimeout(getParameterASInt(soTimeOut));
		httpConnectionManagerParams.setMaxTotalConnections(getParameterASInt(totalConnections));
		httpConnectionManagerParams.setDefaultMaxConnectionsPerHost(getParameterASInt(maxHostConnection));

		client = new HttpClient(multiThreadedHttpConnectionManager);
		DefaultHttpMethodRetryHandler defaultHttpMethodRetryHandler = new DefaultHttpMethodRetryHandler(0, false);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, defaultHttpMethodRetryHandler);

		String[] arrUrl = urls.split("\\,");

		for(int j = 0; j < arrUrl.length; j++){
			String url = arrUrl[j].trim();
			getMethod = new GetMethod(url);

			List<NameValuePair> list = new ArrayList<NameValuePair>();
			list.add(new NameValuePair("XML", xml));
			getMethod.setQueryString(list.toArray(new NameValuePair[0]));

			URL httpurl = null;
			try{
				httpurl = new URL(url);
				HostConfiguration hostConfiguration = client.getHostConfiguration();
				hostConfiguration.setHost(httpurl.getHost(), httpurl.getPort());
				if (null != proxyHost && !(proxyHost = proxyHost.trim()).equals("") && null != proxyPort && !(proxyPort = proxyPort.trim()).equals("")){
					hostConfiguration.setProxy(proxyHost,Integer.parseInt(proxyPort));
				}
				//				statuscode = client.executeMethod(config,getMethod);
				statuscode = client.executeMethod(getMethod);
				if(statuscode == 200) {
					bos = new ByteArrayOutputStream();
					in = getMethod.getResponseBodyAsStream();
					byte[] bytes = new byte[2024];
					int size = 2024;
					int i = 0;
					while((i = in.read(bytes, 0, size)) != -1){
						bos.write(bytes, 0, i);
					}
					byte[] response =  bos.toByteArray();
					if(response != null){
						String responseString = new String(response).trim();
						if(responseString.equalsIgnoreCase("SUCCESS")){
							//write into log file
							basicLogger.info("We are getting successfull response from Tone Player [Response : " + responseString + " ] Url : "+ url);
							System.out.println("We are getting successfull response from Tone Player [Response : " + responseString + " ] Url : "+ url);
						}
						else{
							//write into log file
							basicLogger.info("Please hit the url by execute following commands. Because we are not getting successfull response from Tone Player [Response : " + responseString + " ] Url : "+ url);
							System.out.println("Please hit the url by execute following commands. Because we are not getting successfull response from Tone Player [Response : " + responseString + " ] Url : "+ url);
						}
					}
					else{
						//get bad request and inform to ops team and re-execute the process.
						basicLogger.info("We are getting no response from Tone Player. Url : "+ url);
						System.out.println("We are getting no response from Tone Player. Url : "+ url);
					}
				} else {
					basicLogger.info("Failure: Url : "+ url + " Response code " + statuscode);
					return null;
				}
			} catch (HttpException e) {
				basicLogger.error("Http Failure : URL " + url, e);
				e.printStackTrace();
			} catch (IOException e) {
				basicLogger.error("IO Failue : URL " + url , e);
				e.printStackTrace();
			} finally {
				if(null != getMethod) {
					getMethod.releaseConnection();
				}
			}

		}
		return null;
	}

	private static int getParameterASInt(String parameter){
		try{
			return Integer.parseInt(parameter);
		}
		catch(Exception e){
			return 0;
		}
	}

	public static void makeHitToRadioTPUrl(Set<CategoryClipMap> radioCategoryClipMapsForTPHit) {
		basicLogger.info("makeHitToRadioTPUrl call. Size: " + radioCategoryClipMapsForTPHit.size());
		if (radioCategoryClipMapsForTPHit == null || radioCategoryClipMapsForTPHit.isEmpty()) {
			basicLogger.info("radioCategoryClipMapsForTPHit empty. Returning without updating.");
			return;
		}
		Map<Integer, List<String>> radioCategoriesMap = new HashMap<Integer, List<String>>();
		for (CategoryClipMap categoryClipMap : radioCategoryClipMapsForTPHit) {
			basicLogger.info("categoryClipMap: " + categoryClipMap);
			Clip clip = RBTCacheManager.getInstance().getClip(categoryClipMap.getClipId());
			if (clip == null) {
				continue;
			}
			String wavFileName = clip.getClipRbtWavFile();								
			String fromDate = RBTContentUtils.parseAndGetDate(categoryClipMap.getFromTime());
			String fromTime = RBTContentUtils.parseAndGetTime(categoryClipMap.getFromTime());
			String toTime = RBTContentUtils.parseAndGetTime(categoryClipMap.getToTime());
			StringBuilder sb = new StringBuilder();
			sb.append(fromDate).append(",");
			sb.append(fromTime).append(",");
			sb.append(toTime).append(",");
			sb.append(wavFileName).append(",");
			sb.append(categoryClipMap.getCategoryId());
			List<String> radioDetailsList = radioCategoriesMap.get(categoryClipMap.getCategoryId());
			if(radioDetailsList == null) {
				radioDetailsList = new ArrayList<String>();
				radioCategoriesMap.put(categoryClipMap.getCategoryId(), radioDetailsList);
			}
			radioDetailsList.add(sb.toString());
			basicLogger
			.info("adding radioCategoriesMap: " + radioCategoriesMap);
		}
		String tpRadioUrl = null;
		try {
			tpRadioUrl = RBTContentJarParameters.getInstance().getParameter(
					"TP_RADIO_URL");
			if (null != tpRadioUrl) {

				if (0 == radioCategoriesMap.size()) {
					basicLogger
					.warn("No categories are of RADIO category type, not"
							+ " making any hit to TP_RADIO_URL");
				}

				// Setting HttpParameters
				HttpParameters httpParam = new HttpParameters();
				httpParam.setUrl(tpRadioUrl);
				httpParam.setConnectionTimeout(6000);
				httpParam.setSoTimeout(6000);

				// Setting request Params
				HashMap<String, String> requestParams = new HashMap<String, String>();
				// requestParams.put("RADIO", "RADIOFILE");

				System.out.println("entry: " + radioCategoriesMap);
				basicLogger.info(radioCategoriesMap);

				HashMap<String, File> fileParams = new HashMap<String, File>();
				try {
					File file = createAndGetFile(radioCategoriesMap);
					fileParams.put("RADIO", file);
					HttpResponse httpResponse = RBTHttpClient
							.makeRequestByPost(httpParam, requestParams,
									fileParams);

					String response = "Successfully hit TP_RADIO_URL, "
							+ "response: " + httpResponse.getResponse();
					System.out.println(response);
					basicLogger.info(response);

					if(file.exists()) {
						String msg1 = "Deleting the file, file: "+file.getName();
						System.out.println(msg1);
						basicLogger.info(msg1);
						file.delete();
					}
				} catch (HttpException e) {
					basicLogger.error("Exception caught" + e , e);
				} catch (IOException e) {
					basicLogger.error("Exception caught" + e , e);
				} catch (Throwable e) {
					basicLogger.error("Exception caught" + e , e);
				}
			}
		} catch (Exception e) {
			String error = "Not hitting TP_RADIO_URL, not configured";
			System.out.println(error);
			basicLogger.info(error);
		}
	}

	public static void makeHitToAzaanCopticDoaaTPUrl(Set<CategoryClipMap> azaanCopticDoaaCategoryClipMapsForTPHit) {
		basicLogger.info("makeHitToAzaanCopticDoaaTPUrl call. Size: " + azaanCopticDoaaCategoryClipMapsForTPHit.size());
		if (azaanCopticDoaaCategoryClipMapsForTPHit == null || azaanCopticDoaaCategoryClipMapsForTPHit.isEmpty()) {
			basicLogger.info("azaanCopticDoaaCategoryClipMapsForTPHit empty. Returning without updating.");
			return;
		}
		Map<Integer, List<String>> azaanCopticDoaaCategoriesMap = new HashMap<Integer, List<String>>();
		for (CategoryClipMap categoryClipMap : azaanCopticDoaaCategoryClipMapsForTPHit) {
			basicLogger.info("categoryClipMap: " + categoryClipMap);
			Clip clip = RBTCacheManager.getInstance().getClip(categoryClipMap.getClipId());
			if (clip == null) {
				continue;
			}
			String wavFileName = clip.getClipRbtWavFile();								
			String fromDate = RBTContentUtils.parseAndGetDate(categoryClipMap.getFromTime());
			String fromTime = RBTContentUtils.parseAndGetTimeIn24HrFormat(categoryClipMap.getFromTime());
			String toTime = RBTContentUtils.parseAndGetTimeIn24HrFormat(categoryClipMap.getToTime());
			StringBuilder sb = new StringBuilder();
			sb.append(fromDate).append(",");
			sb.append(fromTime).append(",");
			sb.append(toTime).append(",");
			sb.append(wavFileName);
			List<String> radioDetailsList = azaanCopticDoaaCategoriesMap.get(categoryClipMap.getCategoryId());
			if(radioDetailsList == null) {
				radioDetailsList = new ArrayList<String>();
				azaanCopticDoaaCategoriesMap.put(categoryClipMap.getCategoryId(), radioDetailsList);
			}
			radioDetailsList.add(sb.toString());
			basicLogger
			.info("adding azaanCopticDoaaCategoriesMap: " + azaanCopticDoaaCategoriesMap);
		}

		String tpAzaanCopticDoaaUrl = null;

		try {
			tpAzaanCopticDoaaUrl = RBTContentJarParameters.getInstance().getParameter(
					"TP_AZAAN_COPTIC_DOAA_URL");
			if (null != tpAzaanCopticDoaaUrl) {

				if (0 == azaanCopticDoaaCategoriesMap.size()) {
					System.out.println("Size of the azaanCopticDoaaCategories Map  is zero");
					basicLogger.warn("No categories are of RADIO category type, not"
							+ " making any hit to TP_AZAAN_COPTIC_DOAA_URL");
					return;
				}
				Map<String, String> catIdCatNameMap = new HashMap<String, String>();
				//	Map<String,List<String>> catNameCatIdMap = new HashMap<String,List<String>>();
				String catIdCatNameMappingStr = RBTContentJarParameters.getInstance().getParameter(
						"CATID_NAME_MAPPING_FOR_AZAAN_COPTIC_DOAA");
				//1,2,3:AZAAN;12,13,14:COPTIC;
				if (catIdCatNameMappingStr != null) {
					String[] catIdCatName = catIdCatNameMappingStr.split(";");
					for (String str : catIdCatName) {
						String[] split = str.split(":");
						if (split.length == 2) {
							String catIds[] = split[0].split(",");
							for(String catId : catIds)
								catIdCatNameMap.put(catId, split[1]);
							//			catNameCatIdMap.put(split[1], Arrays.asList(catIds));
						}
					} 
				}
				// Setting HttpParameters
				HttpParameters httpParam = new HttpParameters();
				httpParam.setConnectionTimeout(6000);
				httpParam.setSoTimeout(6000);

				// Setting request Params
				System.out.println("entry: " + azaanCopticDoaaCategoriesMap);
				basicLogger.info(azaanCopticDoaaCategoriesMap);
				String categories = RBTContentJarParameters.getInstance().getParameter(
						"AZAAN_COPTIC_DOAA_CATEGORIES");
				List<String> list = Arrays.asList(categories.split(","));
				for (Entry<Integer, List<String>> entry : azaanCopticDoaaCategoriesMap.entrySet()) {
					try {
						Integer catId = entry.getKey();
						HashMap<String, String> requestParams = new HashMap<String, String>();
						File file = createAndGetFile(catId+"",entry.getValue(), list);
						HashMap<String, File> fileParams = new HashMap<String, File>();
						//String url = tpAzaanCopticDoaaUrl;
						fileParams.put(entry.getKey() + "", file);

						requestParams.put("TYPE", catIdCatNameMap.get(entry.getKey() + ""));
						//url = url.replaceAll("%TYPE%", catIdCatNameMap.get(entry.getKey() + ""));
						httpParam.setUrl(tpAzaanCopticDoaaUrl);
						basicLogger.info("httpParam = " + httpParam + " , fileParams = "
								+ fileParams +",requestParams = "+requestParams);
						HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParam,
								requestParams, fileParams);

						String response = "Successfully hit TP_AZAAN_COPTIC_DOAA_URL, "
								+ "response: " + httpResponse.getResponse();
						System.out.println(response);
						System.out.println("Response code ==="+httpResponse.getResponseCode());
						basicLogger.info(response);

						if (file.exists()) {
							//	File fileProcessed = new File(file.getAbsolutePath()+"_"+System.currentTimeMillis());
							//	file.renameTo(fileProcessed);
							String msg1 = "Deleting the file, file: " + file.getName();
							System.out.println(msg1);
							basicLogger.info(msg1);
							file.delete();
						}
					} catch (HttpException e) {
						basicLogger.error("Exception caught" + e , e);
					} catch (IOException e) {
						basicLogger.error("Exception caught" + e , e);
					} catch (Throwable e) {
						basicLogger.error("Exception caught" + e , e);
					}
				}
			}
		} catch (Exception e) {
			String error = "Not hitting TP_AZAAN_COPTIC_DOAA_URL, not configured";
			System.out.println(error);
			basicLogger.info(error);
		}
	}

	public static void makeHitToFestivalNameTuneTPUrl(Set<CategoryClipMap> festivalNameTuneCategoryClipMapsForTPHit) {
		basicLogger.info("makeHitToFestivalNameTuneTPUrl call. Size: " + festivalNameTuneCategoryClipMapsForTPHit.size());
		if (festivalNameTuneCategoryClipMapsForTPHit == null || festivalNameTuneCategoryClipMapsForTPHit.isEmpty()) {
			basicLogger.info("festivalNameTuneCategoryClipMapsForTPHit empty. Returning without updating.");
			return;
		}
		Map<Integer, List<String>> festivalNameTuneCategoriesMap = new HashMap<Integer, List<String>>();
		for (CategoryClipMap categoryClipMap : festivalNameTuneCategoryClipMapsForTPHit) {
			basicLogger.info("categoryClipMap: " + categoryClipMap);
			Clip clip = RBTCacheManager.getInstance().getClip(categoryClipMap.getClipId());
			if (clip == null) {
				continue;
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
			String wavFileName = clip.getClipRbtWavFile();	
			String startDate = null;
			String endDate = null;
			boolean isDefaultNameTune = false;
			if (categoryClipMap.getFromTime() == null || categoryClipMap.getToTime() == null) {
				isDefaultNameTune = true;
				startDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
				endDate =  "20371231000000";
			} else {
				startDate = sdf.format(categoryClipMap.getFromTime());
				endDate = sdf.format(categoryClipMap.getToTime());
			}
			StringBuilder sb = new StringBuilder();
			sb.append(categoryClipMap.getCategoryId()).append(",");
			sb.append(startDate).append(",");
			sb.append(endDate).append(","); 
			sb.append(wavFileName);
			if (isDefaultNameTune) { 
				ClipInfo clipInfo = new ClipInfo();
				clipInfo.setClipId(clip.getClipId());
				clipInfo.setName("default_festival_nametune");
				clipInfo.setValue("true");
				List<Clip> clipList = new ArrayList<Clip>();
				Set<ClipInfo> clipInfoSet = new HashSet<ClipInfo>();
				clipInfoSet.add(clipInfo);
				clip.setClipInfoSet(clipInfoSet);
				clipList.add(clip);
				Clip clips[] = clipList.toArray(new Clip[0]);
				try {
					ClipsDAO.saveOrUpdateClip(clips);
				} catch (Exception e) {
					String error = "Error while save/update operation for clip";
					basicLogger.info(error + " " + e, e);
				}
			} else {
				List<String> festivalNameTunesDetailsList = festivalNameTuneCategoriesMap.get(categoryClipMap.getCategoryId());
				if (festivalNameTunesDetailsList == null) {
					festivalNameTunesDetailsList = new ArrayList<String>();
					festivalNameTuneCategoriesMap.put(categoryClipMap.getCategoryId(), festivalNameTunesDetailsList);
				}
				festivalNameTunesDetailsList.add(sb.toString());
				basicLogger.info("adding festivalNameTuneCategoriesMap: "
						+ festivalNameTuneCategoriesMap);
			}
		}
		String festivalNameTuneUrl = null;
		try {
			festivalNameTuneUrl = RBTContentJarParameters.getInstance().getParameter(
					"TP_FESTIVAL_NAMETUNE_URL");
			if (null != festivalNameTuneUrl) {
				if (0 == festivalNameTuneCategoriesMap.size()) {
					basicLogger.warn("No categories are of Festival NameTune category type, not"
							+ " making any hit to TP_FESTIVAL_NAMETUNE_URL");
				}
				// Setting HttpParameters
				HttpParameters httpParam = new HttpParameters();
				httpParam.setUrl(festivalNameTuneUrl);
				httpParam.setConnectionTimeout(6000);
				httpParam.setSoTimeout(6000);

				// Setting request Params
				HashMap<String, String> requestParams = new HashMap<String, String>();
				System.out.println("entry: " + festivalNameTuneCategoriesMap);
				basicLogger.info(festivalNameTuneCategoriesMap);
				HashMap<String, File> fileParams = new HashMap<String, File>();
				try {
					File file = createAndGetFile(festivalNameTuneCategoriesMap);
					fileParams.put("FESTIVAL_NAMETUNE", file);
					HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParam,
							requestParams, fileParams);
					String response = "Successfully hit TP_FESTIVAL_NAMETUNE_URL, " + "response: "
							+ httpResponse.getResponse();
					System.out.println(response);
					basicLogger.info(response);
					if (file.exists()) {
						String msg1 = "Deleting the file, file: " + file.getName();
						System.out.println(msg1);
						basicLogger.info(msg1);
						file.delete();
					}
				} catch (HttpException e) {
					basicLogger.error("Exception caught" + e , e);
				} catch (IOException e) {
					basicLogger.error("Exception caught" + e , e);
				} catch (Throwable e) {
					basicLogger.error("Exception caught" + e , e);
				}
			}
		} catch (Exception e) {
			String error = "Not hitting TP_FESTIVAL_NAMETUNE_URL, not configured";
			System.out.println(error);
			basicLogger.info(error);
		}
	}

	private static File createAndGetFile(Map<Integer, List<String>> map) {
		File file = null;
		BufferedWriter bw = null;
		FileOutputStream fos = null;
		try {
			file = new File("nametune.csv");

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);

			for (List<String> list : map.values()) {
				for (String entry : list) {
					basicLogger.info("Writing entry into file: " + entry);
					System.out.println("Writing entry into file: " + entry);
					bw.write(entry);
					bw.newLine();
				}
			}

			bw.close();
			basicLogger.info("Successfully written file. ");
			System.out.println("Successfully written file. ");
		} catch (FileNotFoundException e) {
			basicLogger.error("Exception caught" + e , e);
		} catch (IOException e) {
			basicLogger.error("Exception caught" + e , e);
		} catch (Exception e) {
			basicLogger.error("Exception caught" + e , e);
		} finally {
			if (null != fos) {
				try {
					fos.close();
					bw.close();
				} catch (IOException e) {
					basicLogger.error("Exception caught" + e , e);
				}
			}
		}
		String msg = "Successfully created temporary file. File: " + file;
		System.out.println(msg);
		basicLogger.info(msg);
		return file;
	}

	private static File createAndGetFile(String categoryId,List<String> list,List<String> categoryList) {
		File file = null;
		BufferedWriter bw = null;
		FileOutputStream fos = null;
		try {
			file = new File("nametune.csv");

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);

			if (categoryList != null && categoryList.contains(categoryId)) {
				for (String record : list) {
					basicLogger.info("Writing entry into file: " + record);
					System.out.println("Writing entry into file: " + record);
					bw.write(record);
					bw.newLine();
				}
			}

			bw.close();
			basicLogger.info("Successfully written file. ");
			System.out.println("Successfully written file. ");
		} catch (FileNotFoundException e) {
			basicLogger.error("Exception caught" + e , e);
		} catch (IOException e) {
			basicLogger.error("Exception caught" + e , e);
		} catch (Exception e) {
			basicLogger.error("Exception caught" + e , e);
		} finally {
			if (null != fos) {
				try {
					fos.close();
					bw.close();
				} catch (IOException e) {
					basicLogger.error("Exception caught" + e , e);
				}
			}
		}
		String msg = "Successfully created temporary file. File: " + file;
		System.out.println(msg);
		basicLogger.info(msg);
		return file;
	}

	public static void updateProvisioningRequestsForODA() {
		basicLogger.info("Starting updateProvisioningRequestsForODA. Number of categories to be updated: " + RBTCache.playListCategoryIdsForTPHit.size());
		for (Integer categoryId : RBTCache.playListCategoryIdsForTPHit) {
			basicLogger.info("categoryId: " + categoryId);
			RBTContentUtils.updateProvisioningRequestsForODA(categoryId);
		}
		basicLogger.info("updateProvisioningRequestsForODA finished.");
	}
}
