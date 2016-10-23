package com.onmobile.apps.ringbacktones.daemons;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.LineNumberReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.Gatherer.FTPConfig;
import com.onmobile.apps.ringbacktones.Gatherer.FTPHandler;
import com.onmobile.apps.ringbacktones.Gatherer.SFTPConfig;
import com.onmobile.apps.ringbacktones.Gatherer.SFTPHandler;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class VodaPrismDaemon extends Thread implements Constants
{
	Logger logger = null;
	String folderName = null;
	String destinationFolderName = null;
	private static HashMap<String, String> modeSrvClassSubClassMap = null;
	private static HashMap<String, String> srvClassCosIdMap = null;
	HashSet<String> circleList = null;
	HashSet<String> circleListForAcquisitionOptin = null;
	int sleepInterval = 30000;
	
	HashSet<String> hsbModes = null;
	HashSet<String> rechargeModes = null;
	HashSet<String> acquisitionModes = null;
	Map<String,List<String>> sericeIdServiceClassMap = null;
	Map<String,String> upgradeSubscriptionClassMap = null;
	
	
	public void init() throws Exception
	{
		logger = Logger.getLogger(VodaPrismDaemon.class);
		setName("VodaPrismDaemon");
		folderName = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODA_PRISM_INPUT_FOLDER" , null);
		if(folderName == null)
		{
			logger.info("Input folder for incoming circle level files not configured as COMMON, VODA_PRISM_INPUT_FOLDER. Process can't start.");
			throw new Exception("Input folder for incoming circle level files not configured as COMMON, VODA_PRISM_INPUT_FOLDER. Process can't start.");
		}
		
		destinationFolderName = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODA_PRISM_DESTINATION_FOLDER" , null);
		if(destinationFolderName == null)
		{
			logger.info("Destination folder for incoming circle level files not configured as COMMON, VODA_PRISM_DESTINATION_FOLDER. Process can't start.");
			throw new Exception("Input folder for incoming circle level files not configured as COMMON, VODA_PRISM_DESTINATION_FOLDER. Process can't start.");
		}		
		File file = new File(destinationFolderName);
		if(!file.exists())
			file.mkdirs();
		String circleListStr = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODA_PRISM_LOCAL_CIRCLE_NAMES" , null);
		if(circleListStr == null)
		{
			logger.info("Local circle names parameter is not configured as COMMON, VODA_PRISM_LOCAL_CIRCLE_NAMES. Process can't start.");
			throw new Exception("Local circle names parameter is not configured as COMMON, VODA_PRISM_LOCAL_CIRCLE_NAMES. Process can't start.");
		}	
		circleList = new HashSet<String>();
		StringTokenizer stkParent = new StringTokenizer(circleListStr, ",");
		while(stkParent.hasMoreTokens())
		{
			circleList.add(stkParent.nextToken());
		}
		logger.info("circleList=" + circleList);
		
		String circleListAcqOptinStr = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODA_PRISM_ACQUISITION_OPTIN_CIRCLES" , null);
		if(circleListAcqOptinStr == null)
		{
			logger.info("Optin circles for acquisition is not configured as COMMON, VODA_PRISM_ACQUISITION_OPTIN_CIRCLES. All circles will be tteated as optout.");
		}	
		circleListForAcquisitionOptin = new HashSet<String>();
		StringTokenizer stkParentOptin = new StringTokenizer(circleListAcqOptinStr, ",");
		while(stkParentOptin.hasMoreTokens())
		{
			circleListForAcquisitionOptin.add(stkParentOptin.nextToken());
		}
		logger.info("circleListForAcquisitionOptin=" + circleListForAcquisitionOptin);
		
		String subClassMapStr = CacheManagerUtil.getParametersCacheManager().getParameterValue("SMS", "VODACT_SERVICE_MODE_SRVCLASS_SUBCLASS_MAP", null);
		if(subClassMapStr != null)
		{
			modeSrvClassSubClassMap = new HashMap<String, String>();
			StringTokenizer stkParent1 = new StringTokenizer(subClassMapStr, ";");
			while(stkParent1.hasMoreTokens())
			{
				StringTokenizer stkChild = new StringTokenizer(stkParent1.nextToken(), ",");
				if(stkChild.countTokens() == 2)
				{
					String token1 = stkChild.nextToken().trim();
					String token2 = stkChild.nextToken().trim();
					SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(token2);
					if(subClass != null)
						modeSrvClassSubClassMap.put(token1, token2);	
				}
			}
		}	
		logger.info("modeSrvClassSubClassMap=" + modeSrvClassSubClassMap);
		String cosIdMapStr = CacheManagerUtil.getParametersCacheManager().getParameterValue("SMS", "VODACT_SERVICE_SRVCLASS_COSID_MAP", null);
		if(cosIdMapStr != null)
		{
			srvClassCosIdMap = new HashMap<String, String>();
			StringTokenizer stkParent2 = new StringTokenizer(cosIdMapStr, ";");
			while(stkParent2.hasMoreTokens())
			{
				StringTokenizer stkChild = new StringTokenizer(stkParent2.nextToken(), ",");
				if(stkChild.countTokens() == 2)
				{
					String token1 = stkChild.nextToken().trim();
					String token2 = stkChild.nextToken().trim();
					CosDetails cosDetail = CacheManagerUtil.getCosDetailsCacheManager().getCosDetail(token2);
					if(cosDetail != null)
						srvClassCosIdMap.put(token1, token2);	
				}
			}
		}
		logger.info("srvClassCosIdMap=" + srvClassCosIdMap);
		
		String sleepTimeStr = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODACT_SERVICE_SLEEP_INTERVAL_SECONDS", null);
		try
		{
			sleepInterval = Integer.parseInt(sleepTimeStr);	
		}
		catch(Exception e)
		{
			sleepInterval = 30;
		}
		logger.info("Sleep time="+sleepInterval);
		
		String paramValue = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODACT_SERVICE_HSB_MODES", "HSB");
		hsbModes = new HashSet<String>();
		addValueInCollection(hsbModes, paramValue, ",");
		
		paramValue = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODACT_SERVICE_RECHARGE_MODES", "RECHARGE");
		rechargeModes = new HashSet<String>();
		addValueInCollection(rechargeModes, paramValue, ",");
		
		paramValue = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODACT_SERVICE_ACQUISITION_MODES", "ACQUISITION");
		acquisitionModes = new HashSet<String>();
		addValueInCollection(acquisitionModes, paramValue, ",");
		
		logger.info("hsbModes: "+hsbModes + " , rechargeModes: " + rechargeModes + " , acquisitionModes: " + acquisitionModes);
		
		paramValue = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODACT_SERVICE_SRVID_SRVCLASS_MAP", null);
		sericeIdServiceClassMap = new HashMap<String, List<String>>();
		if(paramValue != null) {
			StringTokenizer tokenizer = new StringTokenizer(paramValue, ";");
			while(tokenizer.hasMoreTokens())
			{
				StringTokenizer childTokenizer = new StringTokenizer(tokenizer.nextToken(), ":");
				if(childTokenizer.countTokens() ==  2) {
					List<String> serviceClass = new ArrayList<String>();
					String serviceId = childTokenizer.nextToken();
					StringTokenizer servClass = new StringTokenizer(childTokenizer.nextToken(),",");
					while(servClass.hasMoreTokens()) {
						serviceClass.add(servClass.nextToken());
					}
					sericeIdServiceClassMap.put(serviceId, serviceClass);
				}
			}
		}
		
		paramValue = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "VODACT_SERVICE_UPGRADE_SUB_CLASS", null);
		upgradeSubscriptionClassMap = new HashMap<String, String>();
		if(paramValue != null) {
			
			StringTokenizer tokenizer = new StringTokenizer(paramValue, ";");
			while(tokenizer.hasMoreTokens())
			{
				StringTokenizer childTokenizer = new StringTokenizer(tokenizer.nextToken(), ",");
				if(childTokenizer.countTokens() == 2)
				{
					String token1 = childTokenizer.nextToken().trim();
					String token2 = childTokenizer.nextToken().trim();
					upgradeSubscriptionClassMap.put(token1, token2);	
				}
			}
		}
		
		logger.info("sericeIdServiceClassMap: "+sericeIdServiceClassMap + " , upgradeSubscriptionClassMap: " + upgradeSubscriptionClassMap);
		
	}
	
	@Override
	public void run()
	{
		try
		{
			init();
		}
		catch (Exception e)
		{
			
		}
		while(true)
		{
			try
			{
				File[] fileList = null;
				
				File f = new File(folderName);
				if(f.exists())
					fileList = f.listFiles();
				
				if(fileList != null && fileList.length != 0)
				{				
					logger.info("Count of files found in "+folderName+ " = "+fileList.length);
					for(int i = 0; i < fileList.length; i++)
					{
						BufferedWriter responseWriter = null;
						LineNumberReader lnr = null;
						File file = null;
						File responseFile = null;
						boolean processed = false;
						try
						{
							file = fileList[i];
							String fileName = file.getName(); 
							logger.info("Current file name="+fileName);
							if (!fileName.toLowerCase().endsWith(".dat"))
							{
								logger.info("File name does not end in suffix .dat , hence moving to next file.");
								continue;
							}

							String circleName = fileName.substring(0, fileName.indexOf("_"));
							logger.info("Circle name = "+circleName);
							if(!circleList.contains(circleName))
							{
								logger.info("Url for circle "+circleName + " not configured. Moving to next file.");
								continue;	
							}

							String responseFileName = fileName + ".response";
							responseFile = new File(folderName + File.separator + responseFileName);
							if(responseFile.exists())
							{
								logger.info("Response file with name "+responseFileName + " exists. Moving to next file.");
								continue;
							}
							responseWriter = new BufferedWriter(new FileWriter(responseFile)); 
							
							lnr = new LineNumberReader(new FileReader(file));
							String str = null;
							int count=0;
							String response = response_VODACT_INVALID;
							while((str = lnr.readLine()) != null)
							{
								try
								{
									response = response_VODACT_INVALID;
									count++;
									logger.info("Processing request number "+count+", input is "+str);
									StringTokenizer stk = new StringTokenizer(str, ",");
									// 919727431557,110131100813,act,HSB,CRBT,CRBT,prepaid,churn,-69965771,NONE,NONE,NONE
									if(stk.countTokens() != 13)
									{
										logger.info("Token count != 13. Its "+stk.countTokens()+", moving to next line");
										continue;
									}	
									String msisdn = stk.nextToken();
									String time = stk.nextToken();
									String requestType = stk.nextToken();
									String mode = stk.nextToken();
									String srvClass2 = stk.nextToken();
									String srvClass = stk.nextToken();
									String subType = stk.nextToken();
									String reason = stk.nextToken();
									String tokenX2 = stk.nextToken();
									String smsText = stk.nextToken();
									String categoryId = stk.nextToken();
									
									
									//Validating mode
									if(!hsbModes.contains(mode) && !rechargeModes.contains(mode) && !acquisitionModes.contains(mode)) {
										logger.info("Mode is invalid. It is "+requestType+", so moving to next request");
										writeResponse(responseWriter,response_VODACT_INVALID,str);
										continue;
									}
									
									//Validation service id and service class map
									List<String> srvClsList = sericeIdServiceClassMap.get(srvClass2);
									if(requestType.equalsIgnoreCase("act") && (srvClsList == null || !srvClsList.contains(srvClass))) {
										logger.info("service id and service class having invalid map. It is serviceId: "+srvClass2+" serviceClass: " + srvClass + " , so moving to next request");
										writeResponse(responseWriter,response_VODACT_INVALID,str);
										continue;
									}
									
									if(categoryId.equalsIgnoreCase("none"))
										categoryId = "3";
									String tokenX3 = stk.nextToken();

									// Validating request type
									if(!requestType.equalsIgnoreCase("act") && !requestType.equalsIgnoreCase("dct") && !requestType.equalsIgnoreCase("can"))
									{
										logger.info("Request Type is not act. It is "+requestType+", so moving to next request");
										writeResponse(responseWriter, response_VODACT_INVALID, str);
										continue;
									}
									
									
									Subscriber subscriber = getSubscriber(msisdn);
									msisdn = subscriber.getSubscriberID();
									boolean isUserActive = isUserActive(subscriber.getStatus());
									String upgrade = stk.nextToken().toUpperCase();
									
									if(!subscriber.isValidPrefix()) {
										logger.info("subscriber not valid prefix msisdn: " + msisdn);
										writeResponse(responseWriter, response_VODACT_INVALID_PREFIX, str);
										continue;
									}
									
									//Validation Request type and sms text
									//if request type ACT then SMS text should not contains DEC or CAN
									//if request type is DCT or CAN, then SMS text should not contains ACT
									
									List<String> smsTextList = Arrays.asList(smsText.toUpperCase().split("\\ "));
									if(((requestType.equalsIgnoreCase("DCT") || requestType.equalsIgnoreCase("CAN")) && (!smsTextList.contains("CAN") && !smsTextList.contains("DCT")))
											|| ((requestType.equalsIgnoreCase("ACT")) && (smsTextList.contains("DCT") || smsTextList.contains("CAN"))))
									{
										logger.info("Mode is "+mode+", and smstext does not contain ACT/DCT/CAN. Hence not processing this request.");
										writeResponse(responseWriter, response_VODACT_INVALID, str);
										continue;
									}
									
									if(hsbModes.contains(mode) && isUserActive && !upgrade.equals("AU") && smsText.toUpperCase().contains("ACT"))
									{
										logger.info("User is active and mode is HSB. So moving to next request.");
										writeResponse(responseWriter, response_VODACT_ALREADY_ACTIVE, str);
										continue;
									}
//									if(acquisitionModes.contains(mode) || rechargeModes.contains(mode))
//									{
//										if(!smsText.toUpperCase().contains("ACT") && !smsText.toUpperCase().contains("DCT") && !smsText.toUpperCase().contains("CAN"))
//										{
//											logger.info("Mode is "+mode+", and smstext does not contain ACT/DCT/CAN. Hence not processing this request.");
//											writeResponse(responseWriter, response_VODACT_INVALID, str);
//											continue;
//										}
//									}

									//validating subscriber type
									if(!(subType.equalsIgnoreCase("PREPAID") || subType.equalsIgnoreCase("POSTPAID") || subType.equalsIgnoreCase("UNKNOWN"))) {
										logger.info("Invalid subtype, subscriber type should be either PREPAID/POSTPAID/UNKNOWN");
										writeResponse(responseWriter, response_VODACT_INVALID, str);
										continue;
									}
									
									boolean isUpgradeEnabled = false;
									if(isUserActive && upgrade.equalsIgnoreCase("AU")) {
										isUpgradeEnabled = true;
									}

									String circleId = subscriber.getCircleID();
									if(hsbModes.contains(mode))
									{
										String clipId = CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "HSB_CLIP_ID_"+circleId.toUpperCase(), null);
										Clip clip = RBTCacheManager.getInstance().getClip(clipId);
										String subClass = "HSB";
										if(smsText.toUpperCase().contains("DCT") || smsText.toUpperCase().contains("CAN")) {
											response = processDeactivation(subscriber, mode);
										}
										else {
											if(isUpgradeEnabled && isUserActive) {
												String tempSubClass = upgradeSubscriptionClassMap.get(subscriber.getSubscriptionClass());
												if(!subClass.equals(tempSubClass)) {
													logger.info("Subscription class Mapping not found. Hence not processing this request.");
													writeResponse(responseWriter, response_VODACT_SERVICE_NOT_CONFIGURED, str);
													continue;
												}
											}
											if(clip == null || clip.getClipEndTime().getTime() < System.currentTimeMillis())
											{											
												response = processActivation(subscriber , mode, subClass, null, subType, isUserActive, isUpgradeEnabled, reason );
											}
											else
											{
												response = processSelection( subscriber, mode, subClass, null, subType, isUserActive, clip, 
														CacheManagerUtil.getParametersCacheManager().getParameterValue("COMMON", "HSB_CHARGE_CLASS", "FREE")
														, categoryId, isUpgradeEnabled, reason);
											}
										}
									}
									else
									{	
										if(smsText.toUpperCase().contains("DCT") || smsText.toUpperCase().contains("CAN")) {
											response = processDeactivation(subscriber, mode);
										}
										else {
											String subClass = Utility
													.getVodaCTSubClass(mode, srvClass,circleId);
											if(subClass==null){
												logger.info("Subscription class Mapping not found. Hence not processing this request.");
												writeResponse(responseWriter, response_VODACT_SERVICE_NOT_CONFIGURED, str);
												continue;
											}
											if(isUpgradeEnabled && isUserActive) {
												String tempSubClass = upgradeSubscriptionClassMap.get(subscriber.getSubscriptionClass());
												if(!subClass.equals(tempSubClass)) {
													logger.info("Subscription class Mapping not found. Hence not processing this request.");
													writeResponse(responseWriter, response_VODACT_SERVICE_NOT_CONFIGURED, str);
													continue;
												}
											}
											String cosId = Utility.getVodaCTCosId(mode, srvClass, circleId);
											
											smsText = smsText.replaceAll("ACT", "");
											smsText = smsText.replaceAll("CT", "");
											smsText = smsText.trim();
											
											Clip clip = null;
											if (smsText.length() != 0)
												clip = RBTCacheManager.getInstance().getClipByPromoId(smsText);

											Category category = null;
											if (clip == null)
											{
												category = RBTCacheManager.getInstance().getCategoryByPromoId(smsText);
												if (category != null && com.onmobile.apps.ringbacktones.webservice.common.Utility.isShuffleCategory(category.getCategoryTpe()))
													categoryId = String.valueOf(category.getCategoryId());
											}

											if ((category != null && com.onmobile.apps.ringbacktones.webservice.common.Utility.isShuffleCategory(category.getCategoryTpe()))
													|| (clip != null && clip.getClipEndTime().getTime() > System.currentTimeMillis()))
											{
												response = processSelection( subscriber, mode, subClass, cosId, subType, isUserActive, clip, null, categoryId, isUpgradeEnabled, reason);
											}
											else
											{
												response = processActivation(subscriber , mode, subClass, cosId, subType, isUserActive, isUpgradeEnabled, reason );
											}
										}
									}
									logger.info("Response for request "+msisdn+","+mode+" = "+response);
									writeResponse(responseWriter, response.toUpperCase(), str);
								}
								catch(Exception e)
								{
									logger.error("Exception while processing "+str, e);
								}
							}
							
							processed = true;
						}
						catch(Exception e)
						{
							logger.error("Exception while processing file.", e);							
						}
						finally
						{
							try
							{
								if (responseWriter != null)
									responseWriter.close();
							}
							catch (Exception e1)
							{
							}
							try
							{
								if (lnr != null)
									lnr.close();
							}
							catch (Exception e2)
							{
							}
							
							if (processed)
							{
								uploadFileToFTP(responseFile);

								copyFile(file);
								deleteInputFileFromFTPFolder(file);
								copyFile(responseFile);
								deleteInputFileFromFTPFolder(responseFile);
							}
						}
					}
				}

				Thread.sleep(sleepInterval * 1000);
			}
			catch (Throwable e)
			{
				logger.info("Caught exception in infinite loop of VodaPrismDaemon", e);
			}
		}
	}
		
	private void deleteInputFileFromFTPFolder(File file)
	{
		try
		{
			file.delete();
		}
		catch(Throwable e)
		{
			logger.info("Exception while deleting file "+file.getName());
		}
		
	}

	private void copyFile(File file)
	{
		try
		{
			FileChannel sourceFileChannel = null;
			FileChannel destinationFileChannel = null;
			Calendar newCalendar = Calendar.getInstance();
			String yearString = String.valueOf(newCalendar.get(Calendar.YEAR));
			String monthString = String.valueOf(newCalendar.get(Calendar.MONTH));
			String dateString = String.valueOf(newCalendar.get(Calendar.DATE));
			String destinationFileName = destinationFolderName + File.separator + yearString + File.separator + monthString +
					File.separator + dateString;
			File folder = new File(destinationFileName);
			if(!folder.exists())
				folder.mkdirs();
			destinationFileName =  destinationFileName + File.separator + file.getName();
			File destination = new File(destinationFileName);
			
			try
			{
				sourceFileChannel = (new FileInputStream(file)).getChannel();
				destinationFileChannel = (new FileOutputStream(destination)).getChannel();
				sourceFileChannel.transferTo(0, file.length(), destinationFileChannel);
			}
			catch (IOException e)
			{
				logger.error("Exception while copying file "+file.getName(), e);
			}
			finally
			{
				try
				{
					if (sourceFileChannel != null)
						sourceFileChannel.close();
					if (destinationFileChannel != null)
						destinationFileChannel.close();
				}
				catch (IOException e)
				{
					logger.error("Exception while copying file "+file.getName(), e);
				}
			}
			
		}
		catch(Exception e)
		{
			logger.error("Exception while copying file "+file.getName(), e);
		}
	}

public String processActivation(Subscriber subscriber, String mode, String subscriptionClass, String cosId, 
			String subType, boolean isUserActive, boolean isUpgradeEnabled, String reason)
	{
		logger.info("*********2, subid="+subscriber.getSubscriberID()+", mode="+mode+", subClass="+subscriptionClass+", isUserActive="+isUserActive);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());
		subscriptionRequest.setMode(mode);
		subscriptionRequest.setIsPrepaid(subType == null || subType.equalsIgnoreCase("UNKNOWN")? subscriber.isPrepaid() : subType.equalsIgnoreCase("prepaid"));
		subscriptionRequest.setModeInfo(mode + "VODA_CT_REASON=" + reason);
		subscriptionRequest.setCircleID(subscriber.getCircleID());		
		if(cosId != null)
			subscriptionRequest.setCosID(Integer.valueOf(cosId));
		if ((!isUserActive || isUpgradeEnabled)
				&& acquisitionModes.contains(mode)
				&& circleListForAcquisitionOptin != null
				&& subscriber.getCircleID() != null
				&& circleListForAcquisitionOptin.contains(subscriber.getCircleID().toUpperCase()))
		{
			subscriptionClass = subscriptionClass + "_OPTIN";  
		}
		if(rechargeModes.contains(mode) && isUserActive && !isUpgradeEnabled)
		{			
			subscriptionRequest.setSubscriptionClass(subscriptionClass);
			subscriptionRequest.setInfo(WebServiceConstants.UPGRADE_VALIDITY);
			subscriber = RBTClient.getInstance().updateSubscription(subscriptionRequest);
		}	
		else
		{	
			if(isUserActive && isUpgradeEnabled) {
				subscriptionRequest.setRentalPack(subscriptionClass);
			}
			else {
				subscriptionRequest.setSubscriptionClass(subscriptionClass);
			}
			subscriber = RBTClient.getInstance().activateSubscriber(subscriptionRequest);
		}
		String response = subscriptionRequest.getResponse();
		return response.toUpperCase();
	}
	
	public String processSelection(Subscriber subscriber, String mode, String subscriptionClass, String cosId, String subType, 
			boolean isUserActive, Clip clip, String chargeClass, String categoryId, boolean isUpgradeEnabled, String reason)
	{
		SelectionRequest selectionRequest = new SelectionRequest(subscriber.getSubscriberID());
		if(cosId != null)
			selectionRequest.setCosID(Integer.valueOf(cosId));
		selectionRequest.setMode(mode);
		selectionRequest.setModeInfo(mode + "VODA_CT_REASON=" + reason);
		selectionRequest.setCircleID(subscriber.getCircleID());
		//added
		if ((!isUserActive || isUpgradeEnabled)
				&& acquisitionModes.contains(mode)
				&& circleListForAcquisitionOptin != null
				&& subscriber.getCircleID() != null
				&& circleListForAcquisitionOptin.contains(subscriber.getCircleID().toUpperCase()))
		{
			subscriptionClass = subscriptionClass + "_OPTIN";
		}
		
		if(rechargeModes.contains(mode) && isUserActive && !isUpgradeEnabled)
		{
			selectionRequest.setSubscriptionClass(subscriptionClass);
			selectionRequest.setInfo(WebServiceConstants.UPGRADE_VALIDITY);
			subscriber = RBTClient.getInstance().updateSubscription(selectionRequest);
		}	
		//end
		
		if(chargeClass != null)
		{
			selectionRequest.setChargeClass(chargeClass);
			selectionRequest.setUseUIChargeClass(true);
		}

		if(isUserActive && isUpgradeEnabled) {
			selectionRequest.setRentalPack(subscriptionClass);
		}
		else{
			selectionRequest.setSubscriptionClass(subscriptionClass);
		}
		selectionRequest.setCategoryID(categoryId);
		if (clip != null)
			selectionRequest.setClipID(""+clip.getClipId());

		selectionRequest.setIsPrepaid(subType == null || subType.equalsIgnoreCase("UNKNOWN")? subscriber.isPrepaid() : subType.equalsIgnoreCase("prepaid"));
		selectionRequest.setInLoop(CacheManagerUtil.getParametersCacheManager().
				getParameterValue("COMMON", "VODACT_SERVICE_FILE_REQUEST_IN_LOOP", "FALSE").equalsIgnoreCase("TRUE") ? true : false );
		RBTClient.getInstance().addSubscriberSelection(selectionRequest);
		String response = selectionRequest.getResponse();
		return response.toUpperCase();
	}
	
	private void writeResponse(BufferedWriter responseWriter, String msisdn, String requestType, String mode, String srvClass,
			String smsText, String response)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(msisdn);sb.append(",");
		sb.append(requestType);sb.append(",");
		sb.append(mode);sb.append(",");
		sb.append(srvClass);sb.append(",");
		sb.append(smsText);sb.append(",");
		sb.append(response);
		try
		{
			responseWriter.append(sb.toString());responseWriter.newLine();responseWriter.flush();
		}
		catch(Exception e)
		{
			logger.error("Exception while writing to response file.", e);
		}
	}
	
	private void writeResponse(BufferedWriter responseWriter, String response, String line) {
		StringBuffer sb = new StringBuffer();
		sb.append(response);sb.append(",");
		sb.append(line);
		try
		{
			responseWriter.append(sb.toString());responseWriter.newLine();responseWriter.flush();
		}
		catch(Exception e)
		{
			logger.error("Exception while writing to response file.", e);
		}
	}


	public static Subscriber getSubscriber(String subscriberID)
	{
		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID); 
		Subscriber subscriber = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
		return subscriber;
	}
	
	public static boolean isUserActive(String subscriberStatus)
    {
          if (subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.ACTIVE)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.LOCKED)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.RENEWAL_PENDING)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.GRACE)
                      || subscriberStatus.equalsIgnoreCase(WebServiceConstants.SUSPENDED))
                return true;

          return false;
    }
	
	public static String getVodaCTSubClassFromModeNsrvclass(String mode, String srvClass)
	{
		Logger.getLogger(Utility.class).info("mode=" + mode+", srvClass="+srvClass);
		String modeSrvClass = mode + "_" + srvClass;
		if(modeSrvClassSubClassMap != null && modeSrvClassSubClassMap.containsKey(modeSrvClass))
			return modeSrvClassSubClassMap.get(modeSrvClass);
		return null;
	}
	
	public static String getVodaCTCosidFromsrvclass(String srvClass)
	{
		Logger.getLogger(Utility.class).info("srvClass=" + srvClass);
		if(srvClassCosIdMap != null && srvClassCosIdMap.containsKey(srvClass))
			return srvClassCosIdMap.get(srvClass);
		return null;
	}
	
	public static boolean isEmpty(String str)
	{
		if(str == null || str.trim().length() == 0 || str.trim().equalsIgnoreCase("null"))
			return true;
		return false;
	}
	
	private void uploadFileToFTP(File file)
	{
		if (file == null || !file.exists())
			return;
		
		String ftpServerIP = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "VODA_PRISM_FTP_SERVER_IP", null);
		if (ftpServerIP == null)
			return;

		int ftpServerPort = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "VODA_PRISM_FTP_SERVER_PORT", 21);
		String ftpServerUserName = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "VODA_PRISM_FTP_SERVER_USERNAME", null);
		String ftpServerPassword = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "VODA_PRISM_FTP_SERVER_PASSWORD", null);
		String ftpUploadDir = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "VODA_PRISM_FTP_UPLOAD_DIR", null);
		
		int ftpWaitTime = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "VODA_PRISM_FTP_WAIT_TIME", 1800000);
		int ftpRetries = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "VODA_PRISM_FTP_RETRIES", 3);
		int ftpTimeout = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "VODA_PRISM_FTP_TIMEOUT", 7200000);
		
		//RBT-12820 Protocol change to upload the recharge response file
		boolean sftpUploadEnabled = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "IS_SFTP_UPLOAD_ENABLED", "false");
		
		logger.info("sftpUploadEnabled value : " + sftpUploadEnabled);
		if (sftpUploadEnabled) {
			SFTPConfig sftpConfig = new SFTPConfig(ftpServerIP, ftpServerPort,
					ftpServerUserName, ftpServerPassword, ftpUploadDir,
					ftpWaitTime, ftpRetries, ftpTimeout);
			SFTPHandler sftpHandler = new SFTPHandler(sftpConfig);
			sftpHandler.uploadFileBySFTP(file.getAbsolutePath());

		} else {
			FTPConfig ftpConfig = new FTPConfig(ftpServerIP, ftpServerPort,
					ftpServerUserName, ftpServerPassword, ftpUploadDir,
					ftpWaitTime, ftpRetries, ftpTimeout);
			FTPHandler ftpHandler = new FTPHandler(ftpConfig);
			ftpHandler.upload(file.getAbsolutePath());
		}
	}
	
	private void addValueInCollection(Collection<String> collection, String value, String delimiter) {
		if(value == null) {
			return;
		}
		StringTokenizer tokenizer = new StringTokenizer(value, delimiter);
		while(tokenizer.hasMoreTokens()) {
			collection.add(tokenizer.nextToken());
		}
	}
	
	private String processDeactivation(Subscriber subscriber, String mode) {
		logger.info("*********2, subid="+subscriber.getSubscriberID()+", mode="+mode);
		SubscriptionRequest subscriptionRequest = new SubscriptionRequest(subscriber.getSubscriberID());
		subscriptionRequest.setMode(mode);
		RBTClient.getInstance().deactivateSubscriber(subscriptionRequest);
		String response = subscriptionRequest.getResponse();
		return response.toUpperCase();
	}
}
