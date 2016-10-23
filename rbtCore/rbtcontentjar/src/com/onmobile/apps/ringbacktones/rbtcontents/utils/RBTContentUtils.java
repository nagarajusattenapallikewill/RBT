package com.onmobile.apps.ringbacktones.rbtcontents.utils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import sun.util.logging.resources.logging;

import com.onmobile.apps.ringbacktones.common.hibernate.HibernateUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.bi.BIInterface;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class RBTContentUtils {

	private static Logger basicLogger = Logger.getLogger(RBTContentUtils.class);
	private static final String strLanguageMaps = RBTContentJarParameters.getInstance().getParameter("LANGUAGE_SHORT_LANGUAGE_MAP");
	private static String clipIdLength  = RBTContentJarParameters.getInstance().getParameter("CLIP_ID_LEN_FOR_PREVIEW_FILE_PATH");
	
	public static String ignoreJunkCharacters(String value) {
		if(value == null) {
			return null;
		}
		try{
			value = new String(value.getBytes("UTF-8"),"UTF-8");
		}
		catch(UnsupportedEncodingException e) {}
		//		StringBuilder builder = new StringBuilder();
		//		for(int i = 0; i < value.length(); i++) {
		//			builder.append(value.charAt(i) + " : " + (int)value.charAt(i) + ", ");
		//		}
		//		basicLogger.debug(builder.toString());

		return value.replaceAll("["+(char)127+"-"+(char)191+(char)8224+"-"+(char)8482+"]", "");
	}

	public static String getBIResponse(int parentCatId, int categoryType, String subscriberId, boolean isFromCategory) {

		String url = RBTContentJarParameters.getInstance().getParameter("BI_URL_" + categoryType);
		String biUrlGenereStr = RBTContentJarParameters.getInstance().getParameter("CONFIG_BI_URL_GENERE");
		String biUrlCategoryStr = RBTContentJarParameters.getInstance().getParameter("CONFIG_BI_URL_CATEGORY");
		String biUrlClipStr = RBTContentJarParameters.getInstance().getParameter("CONFIG_BI_URL_CLIP");
		String clipCatId = String.valueOf(parentCatId);

		if(null != subscriberId){
			url = url.replaceAll("<subscriberid>", subscriberId);
			basicLogger.info("RBT:: URL: " + url);
		}
		if(isFromCategory) {
			url = url.replace("<contentId>", (biUrlGenereStr!=null? biUrlGenereStr:BIInterface.BI_URL_GENERE));
			url = url.replaceAll("<contentType>", (biUrlCategoryStr!=null? biUrlCategoryStr:BIInterface.BI_URL_CATEGORY));
		} else {
				url = url.replaceAll("<contentId>", (clipCatId!=null ? clipCatId:BIInterface.BI_URL_CLIP_CAT_ID));
				url = url.replaceAll("<contentType>", (biUrlClipStr!=null? biUrlClipStr:BIInterface.BI_URL_CLIP));
			}

		String response = "URL_FAILURE";

		HttpParameters httpParam = new HttpParameters();
		httpParam.setUrl(url);
		httpParam.setConnectionTimeout(6000);

		String userName = RBTContentJarParameters.getInstance().getParameter("BI_USERNAME");
		String password = RBTContentJarParameters.getInstance().getParameter("PASSWORD");;

		httpParam.setUsernamePasswordCredentials(new UsernamePasswordCredentials(userName, password));


		try {
			HttpResponse httpResponse = RBTHttpClient.makeRequestByGet(httpParam, null);
			response = httpResponse.getResponse();
			if(httpResponse.getResponseCode() != 200) {
				response = "URL_FAILURE";
			}
		}
		catch(Exception e) {
			basicLogger.error("Exception while hitting url: " +  url, e);			
		}
		basicLogger.debug("URL: " + url + " Response: " + response);
		return response;
	}
	
	//Get BI sub categories types of parent category id from configuration
	public static List<String> getBICategoryTypeList(String catTypesFromConfig) {
		//Get BI category types from configuration
		if( null != catTypesFromConfig && !catTypesFromConfig.isEmpty()) {
			String strCategoriesTypes = RBTContentJarParameters.getInstance().getParameter("BI_CATEGORIES_CATEGORY_TYPES");
			if(strCategoriesTypes == null || (strCategoriesTypes = strCategoriesTypes.trim()).length() == 0) {
				return new ArrayList<String>();
			}
			List<String> categoriesTypesList = (List<String>) Arrays.asList(strCategoriesTypes.split("\\,"));		
			return categoriesTypesList;
		} else {
			String strCategoryTypes = RBTContentJarParameters.getInstance().getParameter("BI_CATEGORY_TYPES");
			if(strCategoryTypes == null || (strCategoryTypes = strCategoryTypes.trim()).length() == 0) {
				return new ArrayList<String>();
			}
			List<String> categoryTypesList = (List<String>) Arrays.asList(strCategoryTypes.split("\\,"));		
			return categoryTypesList;
		}
	}

	public static String getShortCodeLanguage(String langauge){
		if(strLanguageMaps == null  || langauge == null) {
			return null;
		}
		String[] arrLanguageMap = strLanguageMaps.split("\\,");
		Map<String,String> languageMap = new HashMap<String, String>();
		for(int i = 0; i < arrLanguageMap.length; i++) {
			String[] tempArr = arrLanguageMap[i].split("\\:");
			if(tempArr.length != 2) {
				continue;
			}
			languageMap.put(tempArr[0].trim().toLowerCase(), tempArr[1]);
		}
		return languageMap.get(langauge.trim().toLowerCase());		
	}

	/**
	 * Returns the folder path based on the given clip id. for ex: If the input
	 * clipId is 2131459999. And configured clip id length is 4, then it trims
	 * last 4 digits i.e. 9999 and returns 2/1/3/4/5/ or 2\1\3\4\5\ based on the
	 * operating system.
	 * 
	 * @param clipId
	 * @return String
	 */
	public static String getClipPreviewFolderPath(String clipId) {
		StringBuilder path = null;
		if(null != clipIdLength) {
			path = new StringBuilder();
			int confClipIdLen = Integer.parseInt(clipIdLength);
			int folderPath = clipId.length() - confClipIdLen;
			if(confClipIdLen < clipId.length()) {
				char[] pathChars = clipId.substring(0,folderPath).toCharArray();
				for(char c : pathChars) {
					path.append(c).append(File.separator); 
				}
			}
			basicLogger.debug("Clip Preview Folder path: "+path.toString());
			//		} else {
			//			basicLogger.warn("CLIP_ID_LEN_FOR_PREVIEW_FILE_PATH is NOT configured.");
		}

		return (path != null) ? path.toString() : null;
	}

	//RBT-11752
	public static void updateProvisioningRequestsForODA(int categoryID){
		long start = System.currentTimeMillis();
		Session session = HibernateUtil.getSession(); 
		ResultSet rs = null;
		try{
			@SuppressWarnings("deprecation")
			Connection connection = session.connection();
			Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);
			String selectQuery = "SELECT * FROM RBT_PROVISIONING_REQUESTS WHERE TYPE="+categoryID;
			rs = statement.executeQuery(selectQuery);
			while(rs.next()) {
				int status=rs.getInt("STATUS");
				String extraInfo=rs.getString("EXTRA_INFO");
				if(status==30 || status==31 || status==32) {

					if(extraInfo!=null && !extraInfo.equals("")) {
						//<?xml VERSION="1.0" encoding="UTF-8" standalone="no"?><r CAT_ID="646" INDEX="1" MAX_ALLOWED="5"  ODA_REFRESH="TRUE"/>
						//RBT-14900	Base Activation failure is not updating provisioning request table
						if(!extraInfo.contains("ODA_REFRESH")) {
							String extraInfoSub=extraInfo.substring(0, extraInfo.length()-2);
							extraInfo=extraInfoSub+" ODA_REFRESH=\"TRUE\"/>";
						}
					}else {
						extraInfo="<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><r ODA_REFRESH=\"TRUE\"/>";
					}

					rs.updateString("EXTRA_INFO", extraInfo);
				}else if(status==33) {
					rs.updateInt("STATUS", 50);
				}				
				rs.updateRow();
				connection.commit();
			}

			basicLogger.info("Updated Provisioning table for categoryID: " + categoryID + " in " + (System.currentTimeMillis() - start));

		} catch(Exception e) {
			basicLogger.info("Exception occurs while updating provisioning table: "+e);
			e.printStackTrace();
		}finally {
			if(session!=null)
				session.close();
		}
	}
	
	
	public static String parseAndGetDate(Date d) {
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(d);
			StringBuilder sb = new StringBuilder();
			sb.append(c.get(Calendar.YEAR));
			sb.append("-");
			if(c.get(Calendar.MONTH) < 10) {
				sb.append("0");	
			}
			sb.append(c.get(Calendar.MONTH) + 1);
			sb.append("-");
			if(c.get(Calendar.DATE) < 10) {
				sb.append("0");	
			}
			sb.append(c.get(Calendar.DATE));
			
			System.out.println(" date: "+sb.toString());
			basicLogger.info("Converted fromTime: "+ d  + "to date: "+sb.toString());
			
			return sb.toString();
		} catch (Exception e) {
			basicLogger.info("Unable to parse the given date. d: " + d
					+ ", ParseException: " + e.getMessage(), e);
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String parseAndGetTime(Date d) {
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(d);
			StringBuilder sb = new StringBuilder();
			if(c.get(Calendar.HOUR) < 10) {
				sb.append("0");
			}
			sb.append(c.get(Calendar.HOUR));
			sb.append(":");
			if(c.get(Calendar.MINUTE) < 10) {
				sb.append("0");
			}
			sb.append(c.get(Calendar.MINUTE));
			
			System.out.println(" date: "+sb.toString());
			basicLogger.info("Converted fromTime: "+ d  + "to time: "+sb.toString());
			
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static String parseAndGetTimeIn24HrFormat(Date d) {
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(d);
			StringBuilder sb = new StringBuilder();
			if(c.get(Calendar.HOUR_OF_DAY) < 10) {
				sb.append("0");
			}
			sb.append(c.get(Calendar.HOUR_OF_DAY));
			sb.append(":");
			if(c.get(Calendar.MINUTE) < 10) {
				sb.append("0");
			}
			sb.append(c.get(Calendar.MINUTE));
			
			System.out.println(" date: "+sb.toString());
			basicLogger.info("Converted fromTime: "+ d  + "to time: "+sb.toString());
			
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

	public static String appNameDefaultLanguageKey(String appName) {
		return appName.toLowerCase() + ".default_language";
	}
	
	
	public static boolean isParentToBeUpdated() {
		boolean updateParent = false;
		String updateParentReqd = RBTContentJarParameters.getInstance().getParameter("PARENT_CAT_UPDATE_REQD");
		if (updateParentReqd != null && updateParentReqd.equalsIgnoreCase("true"))
			updateParent = true;
		return updateParent;

	}
	
	// API to return paginated categories
	public static Category[] getPagination(List<Category> categoryList, int offset, int rowCount) {
		
		Category[] categories = null;
		
		if(null == categoryList || categoryList.size() == 0) {
			basicLogger.info("List size is empty so returning null :" + categoryList.size());
			return null;
		}
		if(offset >= categoryList.size()) {
			basicLogger.warn("Index out of range. StartIndex value should be less than  " + categoryList.size());				
			return null;
		}
		
		if(rowCount == -1) {
			rowCount = categoryList.size();
		}
		
		int endIndex = offset + rowCount;
		if(endIndex > categoryList.size()) {
			endIndex = categoryList.size();
		}
		categories = new Category[endIndex - offset];
		for(int i=offset, j = 0; i<endIndex; i++, j++) {
			categories[j] = categoryList.get(i);
		}
		
		return categories;
		
	}
	public static void main(String[] args) {
		Clip clip = new Clip();
		clip.setClipId(2131459999);
		clip.setClipPreviewWavFile("test.wav");
		RBTContentUtils.getClipPreviewFolderPath("2131459999");
		System.out.println(" ClipPreviewWavFilePath.. "+clip.getClipPreviewWavFilePath());
		String categoryTypesFromConfig = RBTContentJarParameters.getInstance().getParameter("BI_CATEGORIES_CATEGORY_TYPES");
		System.out.println("configured category type :: " + categoryTypesFromConfig);
		RBTContentUtils.getBICategoryTypeList(categoryTypesFromConfig);
	}
}
