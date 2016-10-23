
/**
 * Program Name    : RBTSyncDatabases.java    
 * Purpose         : To Sync the information from the master database to the local RBTTata database.  
 * Author          : Rajeev Joseph<br>
 * Date            : 17/07/2007
 */ 

package com.onmobile.apps.ringbacktones.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;

/**
 * @author rajeev.joseph
 *
 */
public class RBTSyncDatabases implements iRBTConstant {

	private static Logger logger = Logger.getLogger(RBTSyncDatabases.class);
	
	private HashMap response = null;
	private String urlstrToGetSetting = null;
	
	/**
	 * Purpose : To initiate the process of Syncing the databases.
	 * 
	 * @param sSubscriberId
	 * @param sSubscriberType
	 * @param sCircleId
	 * @param sCalledNumber
	 * @return true if all process success else false
	 * @throws Exception
	 */
	public static boolean syncUserLibrary(String sSubscriberId,String sSubscriberType, String sCircleId, String sCalledNumber)
	{
		try{
			RBTSyncDatabases rbtDatabaseSync = new RBTSyncDatabases();
			
			detailLog("syncUserLibrary", "RBT::getting subscriber clips");
			rbtDatabaseSync.querySubscriberClipsFromLibrary(sSubscriberId, sSubscriberType);
			
			detailLog("syncUserLibrary", "RBT::getting subscriber musicboxes");
			rbtDatabaseSync.querySubscriberMusicboxesFromLibrary(sSubscriberId, sSubscriberType);
			
			detailLog("syncUserLibrary", "RBT::Syncing databases");
			rbtDatabaseSync.syncDatabases(sSubscriberId, sSubscriberType, sCircleId, sCalledNumber);
			
			return true;
		} catch(Exception e) {
			detailLog("syncUserLibrary", "RBT::Exception " + e.getMessage());
			return false;
		}

	}
	
	/**
	 * Purpose : To get all the clip information for a Subscriber.
	 * 
	 * @param sSubscriberId
	 * @param sSubscriberType
	 * @return String array of all clips
	 */
	private String[] querySubscriberClipsFromLibrary(String sSubscriberId,String sSubscriberType)
	{
		ArrayList clipsList = new ArrayList();

		try
		{
			detailLog("querySubscriberClips", "RBT::inside try.....");

			String vuiLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_VUI_LOG_PATH", null);
			int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

			String urlstr = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
			urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "QUERY_SONGS_PAGE", "");

			urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "") + "&";
			urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "") + "&";
			urlstr+= "phonenumber="+sSubscriberId+"&";
			urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");
			
			detailLog("querySubscriberClips", "RBT::hitting the url : "+urlstr);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String result = rbthttpProcessing.makeRequest1(urlstr, sSubscriberId, "RBT_VUI");
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result != null)
			{
				result = result.trim();

				detailLog("querySubscriberClips", "RBT:: result = " + result);

				if(result.length()>1)
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SONGS", sSubscriberId, sSubscriberType, "query_songs", "success", requestedTimeString, differenceTime+"", "RBT_VUI", urlstr, result);
					response.put("CLIPS_RESULT_FROM_BAK_END_KEY", result);
					StringTokenizer st = new StringTokenizer(result, "&");
					while(st.hasMoreTokens())
					{					
						String newString = st.nextToken();
						StringTokenizer tempStringTokenizer = new StringTokenizer(newString, "|");
						if(tempStringTokenizer.hasMoreTokens())
						{
							String tempString = tempStringTokenizer.nextToken().trim();
							clipsList.add(tempString);
						}					
					}				
					String[] returnResult = (String[])clipsList.toArray(new String[0]);

					if(returnResult.length>0)
						return returnResult;
				}
				else if(result.equals("4"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SONGS", sSubscriberId, sSubscriberType, "query_songs", "no-clips", requestedTimeString, differenceTime+"", "RBT_VUI", urlstr, result);
				}
				else
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SONGS", sSubscriberId, sSubscriberType, "query_songs", "error_response", requestedTimeString, differenceTime+"", "RBT_VUI", urlstr, result);
				}
			}
			else
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SONGS", sSubscriberId, sSubscriberType, "query_songs", "null_error_response", requestedTimeString, differenceTime+"", "RBT_VUI", urlstr, result);
				detailLog("querySubscriberClips", "RBT::null response, going to nexttransition");
			}
		}
		catch (Exception e)
		{
			warnLog("querySubscriberClips", "RBT::Exception " + e);
		}		
		return null;
	}

	
	/**
	 * Purpose : To query all the music boxes for a Subscriber
	 * 
	 * @param sSubscriberId
	 * @param sSubscriberType
	 * @return
	 */
	private String[] querySubscriberMusicboxesFromLibrary(String sSubscriberId, String sSubscriberType)
	{
		ArrayList clipsList = new ArrayList();

		try
		{
			String vuiLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_VUI_LOG_PATH", null);
			int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

			detailLog("querySubscriberMusicboxes", "RBT::inside try.....");
			
			String urlstr = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
			urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "QUERY_MUSICBOXES_PAGE", null);

			urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "") + "&";
			urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "") + "&";
			urlstr+= "phonenumber="+sSubscriberId+"&";
			urlstr+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");
			
			detailLog("querySubscriberClips", "RBT::hitting the url : "+urlstr);

			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String result = rbthttpProcessing.makeRequest1(urlstr, sSubscriberId, "RBT_VUI");
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String requestedTimeString = formatter.format(requestedTimeStamp);

			if(result != null)
			{
				result = result.trim();

				detailLog("querySubscriberMusicboxes", "RBT:: result = " + result);
				if(result.length()>1)
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_MUSICBOXES", sSubscriberId, sSubscriberType, "query_musicboxes", "success", requestedTimeString, differenceTime+"", "RBT_VUI", urlstr, result);
					response.put("MUSICBOXES_RESULT_FROM_BAK_END_KEY", result);
					StringTokenizer st = new StringTokenizer(result, "&");
					while(st.hasMoreTokens())
					{
						StringTokenizer tempStringTokenizer = new StringTokenizer(st.nextToken(), "|");
						clipsList.add(tempStringTokenizer.nextToken());
					}

					String[] returnResult = (String[])clipsList.toArray(new String[0]);

					if(returnResult.length>0)
						return returnResult;
				}
				else if(result.equals("4"))
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_MUSICBOXES", sSubscriberId, sSubscriberType, "query_musicboxes", "no-musicboxes", requestedTimeString, differenceTime+"", "RBT_VUI", urlstr, result);
				}
				else
				{
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_MUSICBOXES", sSubscriberId, sSubscriberType, "query_musicboxes", "error_response", requestedTimeString, differenceTime+"", "RBT_VUI", urlstr, result);
				}
			}
			else
			{
				WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_MUSICBOXES", sSubscriberId, sSubscriberType, "query_musicboxes", "null_error_response", requestedTimeString, differenceTime+"", "RBT_VUI", urlstr, result);
			}

		}
		catch (Exception e)
		{
			warnLog("querySubscriberMusicboxes", "RBT::Exception "+e);
		}
		return null;
	}
	
	
	/**
	 * Purpose : To syncing the databases.
	 * 
	 * @param sSubscriberId
	 * @param sSubscriberType
	 * @param sCircleId
	 * @param sCalledNumber
	 * @throws Exception
	 */
	private void syncDatabases(String sSubscriberId,String sSubscriberType, String sCircleId, String sCalledNumber) throws Exception
	{
		try
		{
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();

			Subscriber sub = rbtDBManager.getSubscriber(sSubscriberId);
			char prepaidYes = 'n';
			if(sub.prepaidYes())
				prepaidYes = 'y';
			
			String clipsResult = (String)response.get("CLIPS_RESULT_FROM_BAK_END_KEY"); 
			//String musicboxesResult = (String)response.get("MUSICBOXES_RESULT_FROM_BAK_END_KEY");

//			if(clipsResult == null || clipsResult == "")
//			{
//				detailLog("syncDatabases", "RBT::got null from library, not syncing");
//				return;
//			}

			String settingResult = null;

			

			String vuiLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_VUI_LOG_PATH", null);
			int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);

			for(int setTypeCounter = 1;setTypeCounter<3;setTypeCounter++)
			{
				Date requestedTimeStamp = new Date();
				settingResult = querySongOrMBSetting(sSubscriberId,setTypeCounter);
				Date responseTimeStamp = new Date();

				long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());
				SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
				String requestedTimeString = formatter.format(requestedTimeStamp);

				if(settingResult == null)
				{
					detailLog("syncDatabases", "RBT::got null setting result, leaving for loop");
					WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", sSubscriberId, sSubscriberType, "query_setting", "error_got-null", requestedTimeString, differenceTime+"", "RBT_VUI", urlstrToGetSetting, settingResult);
					break;
				}
				else
				{
					boolean breakFlag = false;
					if(settingResult.length() <= 1)
					{
						if(settingResult.equals("4"))
							WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", sSubscriberId, sSubscriberType, "query_setting", "no-setting", requestedTimeString, differenceTime+"", "RBT_VUI", urlstrToGetSetting, settingResult);
						else
							WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", sSubscriberId, sSubscriberType, "query_setting", "error_got-null", requestedTimeString, differenceTime+"", "RBT_VUI", urlstrToGetSetting, settingResult);
					}
					else
					{

						StringTokenizer settingResultST = new StringTokenizer(settingResult, "&");
						String token = null;
						while(settingResultST.hasMoreTokens())
						{
							token = settingResultST.nextToken();
							StringTokenizer tokenST = new StringTokenizer(token, "|");
							if(tokenST.countTokens() < 9)
							{
								detailLog("syncDatabases", "RBT::unexpected result leaving first while");
								breakFlag = true;
								break;
							}
							else
							{
								int tokenKaTokenCounter = 0;
								String tokenKaToken = null;

								boolean clipYes = false;
								tokenKaToken = tokenST.nextToken();
								tokenKaTokenCounter++;
								if(tokenKaToken.equals("1"))
									clipYes = true;

								String toneCode = null;
								String callerId = null;
								String startDate = null;
								int loopNumber = 1;

								while(tokenST.hasMoreTokens())
								{
									tokenKaToken = tokenST.nextToken();
									tokenKaTokenCounter++;
									if(tokenKaTokenCounter == 3)
									{
										callerId = tokenKaToken;
										detailLog("syncDatabases", "RBT::callerId = " + callerId);
										if(callerId != null)
										{
											if(callerId.length() >= 10)
												continue;
											else
											{
												callerId = null;
												tokenKaTokenCounter++;
											}
										}
									}
									else if(tokenKaTokenCounter == 7)
									{
										toneCode = tokenKaToken;
										detailLog("syncDatabases", "RBT::toneCode = " + toneCode);
									}
									else if(tokenKaTokenCounter == 8 && clipYes)
									{
										try
										{
											loopNumber = Integer.parseInt(tokenKaToken);
										}
										catch(Exception e)
										{

										}
										if(loopNumber > 1)
											loopNumber = 2;
									}
								}
								Calendar cal = Calendar.getInstance();
//								cal.set(2004,0,1,0,0,0);
								if(clipYes)
								{
									startDate = getClipStartDate(sSubscriberId, toneCode, clipsResult);
									if(startDate != null)
									{
										String yearString = startDate.substring(0,4);
										String monthString = startDate.substring(5,7);
										String dayString = startDate.substring(8,10);

										int year = Integer.parseInt(yearString);
										int month = Integer.parseInt(monthString);
										int day = Integer.parseInt(dayString);

										cal.set(year, month-1, day);
									}
								}

								Date startTime = cal.getTime();
								int categoryId = -1;

								String classType = null;
								
								//Categories category = null;								
								if(clipYes)
								{
									Clips clip = rbtDBManager.getClipPromoID(toneCode);
									try
									{
										classType = clip.classType();
										categoryId = rbtDBManager.getClipCategoryId(clip.id());
										Categories category = rbtDBManager.getCategory(categoryId, sCircleId, prepaidYes);
										if(category == null)
										{
											categoryId = 3;
											//category = rbtDBManager.getCategory(categoryId, sCircleId, prepaidYes);
										}
									}
									catch(Exception e)
									{

									}
								}
								else
								{
									Categories musicbox = rbtDBManager.getCategoryPromoID(toneCode, sCircleId, prepaidYes);
									classType = musicbox.classType();
									categoryId = musicbox.parentID();
									Categories category = rbtDBManager.getCategory(categoryId, sCircleId, prepaidYes);
									if(category == null)
									{
										categoryId = 4;
										//category = rbtDBManager.getCategory(categoryId, sCircleId, prepaidYes);
									}
								}
								if(classType == null)
									classType = "DEFAULT";

								//if we dont have the (clip in rbt_category_clip_map)/(musicbox in rbt_musicboxes)
								if(categoryId <= 0)
								{
									if(clipYes)
										categoryId = 3;
									else
										categoryId = 4;
								}

								int maxSelections = sub.maxSelections();								
								SubscriberDownloads download = rbtDBManager.getActiveSubscriberDownload(sSubscriberId, toneCode);
								if(download == null)
								{  
									rbtDBManager.addSubscriberDownload(sSubscriberId, toneCode, categoryId, true, (clipYes? DTMF_CATEGORY : BOUQUET));
									if(clipYes)
									{
										maxSelections++;
										rbtDBManager.updateNumMaxSelections(sSubscriberId, maxSelections);
									}
								}
								
								int validityInt = 365;
								ChargeClass chargeClass = CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType);
								if(chargeClass != null)
								{
									String validity = chargeClass.getSelectionPeriod();
									
									validityInt = (validity != null) ? Integer.parseInt(validity.substring(1)) : 365;
									
									if(validity.startsWith("M"))
										validityInt = validityInt * 30;
									else if(validity.startsWith("Y"))
										validityInt = validityInt * 365;
								}
								
								Calendar nextChgDtCal = Calendar.getInstance();
								nextChgDtCal.set(2037, 0, 1, 0, 0, 0);
								
								SubscriberStatus sebSel = rbtDBManager.getActiveSubscriberRecord(sSubscriberId, callerId, -1, 0, 2359);
								//adding active record ro selections table
								if(sebSel != null && !sebSel.selStatus().equalsIgnoreCase("D"))
								{
									rbtDBManager.addActiveSubSelections(sSubscriberId, callerId,
											categoryId, toneCode, startTime, startTime,
											download.endTime(), loopNumber, "VP",
											sCalledNumber +"|"+ maxSelections,
											prepaidYes=='y', 0, 2359, nextChgDtCal.getTime(), classType, true, clipYes?DTMF_CATEGORY:BOUQUET);
								}
//								if(!rbtDBManager.checkSettingActive(sSubscriberId, callerId, toneCode) && !rbtDBManager.checkSettingToBeDeleted(sSubscriberId, callerId, toneCode))
//								{
//									Calendar calendar = Calendar.getInstance();
//									calendar.setTime(startTime);
//									calendar.add(Calendar.DAY_OF_YEAR, (validityInt -1));
//									Date endTime = calendar.getTime();
//									rbtDBManager.addActiveSubscriberSelectionForTATA(sSubscriberId, callerId, categoryId, toneCode, startTime, startTime, endTime, 1, classType, "VP", sCalledNumber +"|"+ maxSelections, sub.prepaidYes(), 0, 23, loopNumber);
//								}
								
								rbtDBManager.updateDownloadStatusToDownloaded(sSubscriberId, toneCode, startTime, validityInt);
							}
						}
						if(breakFlag)
						{
							WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", sSubscriberId, sSubscriberType, "query_setting", "error_wrong-code", requestedTimeString, differenceTime+"", "RBT_VUI", urlstrToGetSetting, settingResult);
						}
						else
						{
							WriteSDR.addToAccounting(vuiLogPath, logRotationSize, "RBT_QUERY_SETTING", sSubscriberId, sSubscriberType, "query_setting", "success", requestedTimeString, differenceTime+"", "RBT_VUI", urlstrToGetSetting, settingResult);
						}
					}
				}
			}
			
			String[] allSubSelectionsFromBakEnd = (String[])querySubscriberClipsFromLibrary(sSubscriberId,sSubscriberType);
			for(int firstCounter = 0; firstCounter < allSubSelectionsFromBakEnd.length; firstCounter++)
			{
				SubscriberDownloads download = rbtDBManager.getActiveSubscriberDownload(sSubscriberId, allSubSelectionsFromBakEnd[firstCounter]);
				if(download == null)
				{
					int categoryId = 3;
					int noOfClips = allSubSelectionsFromBakEnd.length;
					if(firstCounter >= noOfClips)
						categoryId = 4;
					//Categories category = rbtDBManager.getCategory(categoryId, sCircleId, prepaidYes);
					
					rbtDBManager.addSubscriberDownload(sSubscriberId, allSubSelectionsFromBakEnd[firstCounter],
							categoryId, true, (categoryId==3?DTMF_CATEGORY:BOUQUET));
				}
			}
		}
		catch(Exception e)
		{
			warnLog("syncDatabases", "RBT::Exception " + getStackTrace(e));
		}
	}
	
	/**
	 * Purpose : To retrive the song settings.
	 * 
	 * @param subscriberId
	 * @param setType
	 * @return String
	 */
	private String querySongOrMBSetting(String subscriberId, int setType)
	{
		String returnValue = null;
		try
		{
			detailLog("querySongOrMBSetting", "RBT::Inside try...");

			String httpLink = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
			String operatorAccount = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "");
			String operatorPassword = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "");
			String operator = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");

			urlstrToGetSetting  = httpLink;
			urlstrToGetSetting+= RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "QUERY_SETTING_PAGE", "");

			urlstrToGetSetting+= operatorAccount+"&";
			urlstrToGetSetting+= operatorPassword+"&";
			urlstrToGetSetting+= "phonenumber="+subscriberId+"&";
			urlstrToGetSetting+= "settype="+setType+"&";
			urlstrToGetSetting+= operator;
		
			detailLog("querySongOrMBSetting", "RBT::hitting the url : "+urlstrToGetSetting);
		
			RBTHTTPProcessing rbthttpProcessing = RBTHTTPProcessing.getInstance();			
			returnValue = rbthttpProcessing.makeRequest1(urlstrToGetSetting, subscriberId, "RBT_VUI");

			if(returnValue != null)
				returnValue = returnValue.trim();
			detailLog("querySongOrMBSetting", "RBT::leaving");
		}
		catch(Exception e)
		{
			warnLog("querySongOrMBSetting", "RBT::Exception " + getStackTrace(e));
		}
		return returnValue;
	}
	
	/**
	 * Purpose : To get the Stack Trace of any exception.
	 * 
	 * @param ex
	 * @return
	 */
	private static String getStackTrace(Throwable ex)
	{
		StringWriter stringWriter = new StringWriter();
		String trace = "";
		if(ex instanceof Exception)
		{
			Exception exception = (Exception)ex;
			exception.printStackTrace(new PrintWriter(stringWriter));
			trace = stringWriter.toString();
			trace = trace.substring(0,trace.length()-2);
			trace = System.getProperty("line.separator")+" \t" + trace;
		}
		return trace;
	}

	/**
	 * Purpose : To retrive the clip start date.
	 * 
	 * @param sSubscriberId
	 * @param toneCode
	 * @param result
	 * @return
	 */
	private String getClipStartDate(String sSubscriberId, String toneCode, String result)
	{
		try
		{
			detailLog("getClipStartDateFromLibrary", "RBT::inside try.....");

			if(result == null)
			{
				detailLog("getClipStartDateFromLibrary", "RBT::got null result");
				return null;
			}
			else
			{
				result = result.trim();
				StringTokenizer clipTokenizer = new StringTokenizer(result, "&");
				while(clipTokenizer.hasMoreTokens())
				{
					String token = clipTokenizer.nextToken();
					if(token.startsWith(toneCode))
					{
						StringTokenizer tokenKaTokenizer = new StringTokenizer(token, "|");
						for(int counter=1; counter < 4; counter++)
						{
							String tempResult = tokenKaTokenizer.nextToken();
							if(counter == 3)
								return tempResult;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			detailLog("getClipStartDateFromLibrary", "RBT::Exception "+ getStackTrace(e));
		}
		return null;
	}
	
	/**
	 * Purpose : To log the general details.
	 * 
	 * @param function
	 * @param message
	 */
	private static void detailLog(String function, String message) 
	{
		logger.info(message);
	}

	/**
	 * Purpose : To log the error details.
	 * 
	 * @param function
	 * @param message
	 */
	private static void warnLog(String function, String message) 
	{
		logger.info(message);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			RBTSyncDatabases.syncUserLibrary("9900112294","POST-PAID", "361","332");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
