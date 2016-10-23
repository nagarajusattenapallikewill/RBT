package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.SitePrefixCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.UgcClip;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.UgcClipDAO;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ContentRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class UGCContentPopulateDaemon extends Thread implements iRBTConstant{

	private static Logger logger = Logger.getLogger(UGCContentPopulateDaemon.class);
	
	private ParametersCacheManager _paramCacheManager = null;
	private int _noOfRowsPopulateDaemon;
	private static RBTClient rbtClient = null;
	private int sleepMins = 1;
	private Calendar calendar = Calendar.getInstance();
	private List<SitePrefix> siteprefixes = null;
	private String defaultUGCClassType = "";
	private String ugcSelectionEnabled = "FALSE";
	private String reportingUrl = "";
	public UGCContentPopulateDaemon(){
		Tools.init("UGCContentPopulateDaemon", true);

		_paramCacheManager = CacheManagerUtil.getParametersCacheManager();
		_noOfRowsPopulateDaemon = Integer.parseInt(_paramCacheManager.getParameter("COMMON", "NO_OF_ROWS").getValue());
		sleepMins = Integer.parseInt(_paramCacheManager.getParameter("COMMON", "UGCDAEMON_SLEEPMINS").getValue());
		defaultUGCClassType = _paramCacheManager.getParameter("COMMON", "DEFAULT_UGC_CLASSTYPE").getValue();
		ugcSelectionEnabled = _paramCacheManager.getParameter("COMMON", "UGC_SELECTION_ENABLED").getValue();
		reportingUrl = _paramCacheManager.getParameter("COMMON", "UGC_REPORTING_URL").getValue();

		try {
			rbtClient = RBTClient.getInstance();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * process a selection for the ugc clip to caller id "ALL"
	 * @param ugcClip
	 * @param channel
	 * @param consentLog
	 * @param populated
	 * @return
	 */
	public boolean processSelectionForUgcClip(UgcClip ugcClip,String channel,String consentLog,boolean populated){
		String tempClipExtraInfo = ugcClip.getClipExtraInfo();
			if(ugcSelectionEnabled!=null && !ugcSelectionEnabled.equalsIgnoreCase("FALSE")){
				RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(ugcClip.getSubscriberId());
				com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
				if(subscriber!=null && subscriber.getCircleID()!=null && !subscriber.getCircleID().equals("")){
					String actMode=UGC_MODE;
					if(channel!=null && channel.length()>0){
						actMode=channel+":"+actMode;
					}
					SelectionRequest selectionRequest = new SelectionRequest(ugcClip.getSubscriberId(),null,"ALL",UGC_MODE,
							ugcClip.getCategoryId()+"",ugcClip.getClipId()+"",ugcClip.getLanguage(),null,0,23,1,null);
					selectionRequest.setCircleID(subscriber.getCircleID());
					HashMap<String,String> extraInfoMap=new HashMap<String,String>();
					if (consentLog!=null && consentLog.length()>0) {
						extraInfoMap.put("CONSENT_LOG", consentLog);
						selectionRequest.setSelectionInfoMap(extraInfoMap);
					}
					rbtClient.addSubscriberSelection(selectionRequest);
					String selResponse = selectionRequest.getResponse();
					if(selResponse!=null && selResponse.equalsIgnoreCase(WebServiceConstants.SUCCESS)){
						tempClipExtraInfo = tempClipExtraInfo.replaceAll("SelResponse=n", "SelResponse=y");
					}else{
						populated=false;
					}
				}
			}
		ugcClip.setClipExtraInfo(tempClipExtraInfo);
		return populated;
	}
	
	/**
	 * process a content request
	 * @param ugcClip
	 * @param circleID
	 * @param info
	 * @return
	 */
	public String processContentRequest(UgcClip ugcClip,String circleID,String info){ 
		ContentRequest contentRequest = new ContentRequest(ugcClip.getLanguage(),ugcClip.getClipId(),
				ugcClip.getClipName(),null,null,"rbt_"+ugcClip.getClipRbtWavFile()+"_rbt",null,null,ugcClip.getClipPromoId(),
				defaultUGCClassType,ugcClip.getClipStartTime(),ugcClip.getClipEndTime(),ugcClip.getAlbum(),null,null,info);
		contentRequest.setContentType("UGCCLIP");
		contentRequest.setCircleID( circleID);
		rbtClient.addContent(contentRequest);
		String clipAddResponse = contentRequest.getResponse();
		return clipAddResponse;
	}
	
	/**
	 * Process content request in all sites
	 * @param ugcClip
	 * @param sitePrefixes
	 * @param info
	 * @param populated
	 * @return
	 */
	public boolean processAllContentRequests(UgcClip ugcClip,List<SitePrefix> sitePrefixes,String info,boolean populated){
		StringBuffer clipExtraInfo = new StringBuffer("");
		for(int i=0;i<sitePrefixes.size();i++){
			String circleID = sitePrefixes.get(i).getCircleID();
			//clip Extra Info column to be populated.
			String clipAddResponse = processContentRequest(ugcClip,circleID,info);
			if(clipAddResponse!=null && (clipAddResponse.equalsIgnoreCase(WebServiceConstants.SUCCESS) || clipAddResponse.equalsIgnoreCase(WebServiceConstants.ALREADY_EXISTS))){
				clipExtraInfo.append(circleID+"=y,");
			}else{
				clipExtraInfo.append(circleID+"=n,");
				populated = false;
			}
			logger.info("RbtClient ->"+rbtClient+" Sites ->"+" clip add Response ->"+clipAddResponse);
		}
		ugcClip.setClipExtraInfo(clipExtraInfo.toString());
		return populated;
	}

	
	/**
	 * task to be done on the start of the thread.
	 */
	public void run(){
		while(true){
			try {
				logger.info("UGCContentPopulateDaemon is in its run method");
				sleep();
				//process clips with status 'n' & extra_info 'FILE_DOWNLOADED'
				updateUGCClip();
				//process clips with status 'r' & extra_info '<CID1>=y,<CID2>=y,SelResponse=n'
				updateUGCClipsRetry();
			} catch(Exception e) {
				logger.info("UGCContentPopulateDaemon Thread has got exception");
			}
		}
	}

	/**
	 * Add the UGC clip to the rbt_clips table and make selections if it is configured.
	 * @param ugcClip
	 */
	public void updateUGCClip(){
		logger.info("invoking updateUGCClip");
		UgcClip[] ugcClips = getUGCClips('n', null, _noOfRowsPopulateDaemon);

		if(ugcClips == null) {
			logger.info("no clip found with status 'n'");
			return;
		}
		logger.info("Processing UGC clips with status 'n' " + ugcClips.length);
		for(int abc=0;abc<ugcClips.length;abc++) {
			UgcClip ugcClip = ugcClips[abc];
			if(ugcClip == null) {
				continue;
			}
			logger.info("Processing UGC clip-" + abc + " " + ugcClip);
			if(ugcClip.getClipExtraInfo()!=null)
			{
				logger.info("cannot be processed as the extra info column is not null value= "+ugcClip.getClipExtraInfo());
			}else{
				boolean populated = true;
				StringBuffer clipExtraInfo = new StringBuffer("");
				SitePrefixCacheManager sitePrefixCacheMgr = CacheManagerUtil.getSitePrefixCacheManager();
				List<SitePrefix> sitePrefixes = sitePrefixCacheMgr.getAllSitePrefix();
				logger.info("UGC Clip ->"+ugcClip);
				
				String info=null;
				
				//process the content request in all clips table
				populated = processAllContentRequests(ugcClip,sitePrefixes,info,populated);
				logger.info("value of populated from 'processAllContentRequests'" + populated);
				//making a selection
				populated = processSelectionForUgcClip(ugcClip, "UGC", "", populated);
				logger.info("value of populated from 'processSelectionForUgcClip'" + populated);
				//if successfully entered into all the circles then update the same to the UGC_CLIPS table
				clipExtraInfo = new StringBuffer(ugcClip.getClipExtraInfo());
				logger.info("clipExtraInfo" + clipExtraInfo);
				if(populated){
					ugcClip.setClipStatus('y');
					ugcClip.setClipExtraInfo("");
				}else{
					//need to retry this record
					ugcClip.setClipStatus('r');
					ugcClip.setClipExtraInfo(clipExtraInfo.toString());
				}
				try {
					UgcClip ugcClipRet = UgcClipDAO.updateClip(ugcClip);
					if(ugcClipRet!=null){
						reportUrlSuccess(ugcClipRet);
						logger.info("reported url success");
					}
				} catch (DataAccessException e) {
					e.printStackTrace();
					logger.info("exception in update UGC clip");
				}
				
			}
		}
	}

	/**
	 * Get the clips with status 'retry'
	 * check if there is n in the key=value pair in the extra_info column. 
	 * The possible keys are circle_id & SelResponse and values are y & n
	 * if n process that record
	 */
	private void updateUGCClipsRetry() {
		logger.info("invoking updateUGCClipsRetry");
		UgcClip[] ugcClips = getUGCClips('r', null, _noOfRowsPopulateDaemon);
		if(ugcClips == null) {
			logger.info("no UGC clip to be re-tried.");
			return;
		}
		logger.info("Processing UGC clips with status 'r' " + ugcClips.length);
		for(int i=0;i<ugcClips.length;i++) {
			UgcClip ugcClip = ugcClips[i];
			if(ugcClip == null) {
				continue;
			}
			logger.info("Processing UGC clip-" + i + " " + ugcClip);
			String clipExtraInf = null;
			boolean populated = true;
			String consentLog=null;
			String channel=null;
			clipExtraInf = ugcClip.getClipExtraInfo();
			String tempClipExtraInfo = clipExtraInf;
			if(clipExtraInf == null) {
				clipExtraInf = "";
			}
			String[] status = clipExtraInf.split(",");
			logger.info("UGC Clip ->"+ugcClip);
			logger.info("Selection Response ->"+clipExtraInf);
			for(int k=0;k<status.length;k++){
				String keyValuePair = status[k];
				String[] keyValues = keyValuePair.split("=");
				String key = keyValues[0];
				if(key.equals("SelResponse")){
					//action to be done for the  selection response
					if(keyValues.length >1 && keyValues[1].equalsIgnoreCase("n")){
						populated = processSelectionForUgcClip(ugcClip,channel,consentLog,populated);
						logger.info("processSelectionForUgcClip returns" + populated);
					}					
				}else {
					//processing the circle ids
					if(keyValues.length > 1 &&  keyValues[1].equalsIgnoreCase("n")){
						String info=null;
						
						String clipAddResponse = processContentRequest(ugcClip,key,info);
						if(clipAddResponse!=null && clipAddResponse.equalsIgnoreCase("success")){
							tempClipExtraInfo = tempClipExtraInfo.replaceAll(key+"=n", key+"=y");
						}else if(clipAddResponse!=null && clipAddResponse.equalsIgnoreCase("already_exists")){
							tempClipExtraInfo = tempClipExtraInfo.replaceAll(key+"=n", key+"=y");
						}else{
							populated = false;
						}
						logger.info(" clip add response -> "+clipAddResponse);
					}
					logger.info("Clip Extra Info ->"+tempClipExtraInfo);
				}
			}
			logger.info("Clip Extra Info ->"+tempClipExtraInfo);
			try {
				UgcClip ugcClipRet = null;
				if(populated){
					ugcClips[i].setClipStatus('y');
					ugcClips[i].setClipExtraInfo("");
					ugcClipRet = UgcClipDAO.updateClip(ugcClips[i]);
					if(ugcClipRet!=null){
						reportUrlSuccess(ugcClipRet);
						logger.info("kasjhdfskjhfksjhfksdjj");
					}
				} else {
					ugcClips[i].setClipExtraInfo(tempClipExtraInfo);
					ugcClipRet = UgcClipDAO.updateClip(ugcClips[i]);
				}
			} catch (DataAccessException e) {
				// TODO Auto-generated catch block
				logger.info("Exception in UGCClipRetry"+e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the "count no of clips" with the given status & extrainfo value
	 * if the status is 'r',the clipextrainfo can be anything.
	 * @param status
	 * @param extraInfo
	 * @param count
	 * @return
	 */
	private UgcClip[] getUGCClips(char status, String extraInfo, int count) {
		List<UgcClip> result = new ArrayList<UgcClip>(count);
		UgcClip[] ugcClips = null;
		try {
			ugcClips = UgcClipDAO.getUgcClipByStatus(status, 0, count);
			if(ugcClips == null) {
				logger.info("getUGCClips has null value.");
				return result.toArray(new UgcClip[0]);
			}
			for(int i=0; i<ugcClips.length; i++) {
				if(status!='r')
				{
					result.add(ugcClips[i]);
				}else if(ugcClips[i] != null && ugcClips[i].getClipExtraInfo().equalsIgnoreCase(extraInfo)) {
					result.add(ugcClips[i]);
				}
			}
		} catch (DataAccessException e) {
			logger.error("", e);
			e.printStackTrace();
		}
		return result.toArray(new UgcClip[0]);
	}
	
	private void reportUrlSuccess(UgcClip ugcClipRet) {
		RBTHttpClient rbtHttpClient = new RBTHttpClient(new HttpParameters());
		HashMap<String,String> requestParams = new HashMap<String,String>();
		requestParams.put("SINGER", ugcClipRet.getSubscriberId()+"");
		requestParams.put("CLIP_NAME", ugcClipRet.getClipName()+"");
		requestParams.put("CATEGORY_ID",  ugcClipRet.getParentCategoryId()+"");
		requestParams.put("SUBCATEGORY_ID",ugcClipRet.getCategoryId()+"");
		requestParams.put("EXPIRE_DATE", ugcClipRet.getClipEndTime()+"");
		requestParams.put("COPYRIGHT_ID", ugcClipRet.getRightsBody()+"");
		requestParams.put("ALBUM_MOVIE", ugcClipRet.getAlbum()+"");
		requestParams.put("LANG_ID", ugcClipRet.getLanguage()+"");
		requestParams.put("PUBLISHER_ID", ugcClipRet.getPublisher()+""); 
		requestParams.put("VCODE", ugcClipRet.getClipRbtWavFile()+"");
		HttpResponse response = null;
		try {
			if(reportingUrl!=null && !reportingUrl.equals("")){
				response	= rbtHttpClient.makeRequestByPost(reportingUrl, requestParams, null);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("Response from the report Request ->"+reportingUrl+" Request Params -> "+requestParams+" Response ->"+response);
	}

	private void sleep() {
		long nexttime = getnexttime(sleepMins);
		calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date(nexttime));
		logger.info("RBT::Sleeping till " + calendar.getTime()
				+ " for next processing !!!!!");
		long diff = (calendar.getTime().getTime() - Calendar.getInstance().getTime().getTime());
		try {
			if (diff > 0)
				Thread.sleep(diff);
			else
				Thread.sleep(sleepMins * 60 * 1000);
		}
		catch (InterruptedException e) {
			logger.error("", e);
		}
	}

	public long getnexttime(int sleep) {
		Calendar now = Calendar.getInstance();
		now.setTime(new java.util.Date(System.currentTimeMillis()));
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);

		long nexttime = now.getTime().getTime();
		while (nexttime < System.currentTimeMillis()) {
			nexttime = nexttime + (sleep * 60 * 1000);
		}

		logger.info("RBT::getnexttime" + new Date(nexttime));
		return nexttime;
	}
}
