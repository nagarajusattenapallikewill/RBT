package com.onmobile.apps.ringbacktones.webservice.implementation.tefspain;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.common.DataUtils;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.implementation.BasicRBTProcessor;

public class TefSpainRBTProcessor extends BasicRBTProcessor
{
	private static Logger logger = Logger.getLogger(TefSpainRBTProcessor.class);

	@Override
	protected CosDetails getCos(WebServiceContext task, Subscriber subscriber) 
	{
		CosDetails cos = DataUtils.getCos(task, subscriber);
		logger.info("RBT:: response: " + cos.getCosId());
		return cos;
	}
	
	@Override
	public String deleteSetting(WebServiceContext task) {
		
		String response = ERROR;
		String categoryID = task.getString(param_categoryID);
		if (task.containsKey(param_clipID) && categoryID != null) {
			Category category = rbtCacheManager.getCategory(Integer.parseInt(categoryID));
			if (category != null && category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
				logger.info("Clip Based Deleting Setting not allowed for ODA_SHUFFLE");
				return NOT_ALLOWED;
			}
		} else if (categoryID != null
				&& rbtCacheManager.getCategory(Integer.parseInt(categoryID)).getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
			response = deactivateODAPack(task);
			return response;
		}
		response = super.deleteSetting(task);
		return response;

	}
	
	//RBT-12419
		@Override
		public String processSelection(WebServiceContext task) {
			
			String processSelectionResult = super.processSelection(task);
			String  subId = (String) task.get(param_subscriberID);
			String clipId=(String)task.get(param_clipID);
			String  catId = (String) task.get(param_categoryID);
			HashMap<String, String> whereClauseMap=new HashMap<String, String>();
			Clip clip=null;
			if(clipId!=null) {
				clip=RBTCacheManager.getInstance().getClip(Integer.parseInt(clipId));
			}
			logger.info("in processselection subId= "+subId+" , clipId= "+clipId+ " catId= "+catId);
			if(RBTParametersUtils.getParamAsBoolean("COMMON", "SELECTION_MODEL_PARAMETER", "FALSE")){
				if(processSelectionResult!=null && processSelectionResult.equalsIgnoreCase(CLIP_EXPIRED) && clip!=null && catId!=null) {
					processSelectionResult = CLIP_EXPIRED;
					whereClauseMap.put("SUBSCRIBER_WAV_FILE", clip.getClipRbtWavFile());
					SubscriberStatus subSatus = rbtDBManager.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subId, whereClauseMap);
					
					if(subSatus==null) {
						boolean removeFromDownload = rbtDBManager.removeSubscriberDownloadBySubIdAndWavFileAndCatId(subId, clip.getClipRbtWavFile(), Integer.parseInt(catId));
						if(removeFromDownload) {
							processSelectionResult = CLIP_EXPIRED_DOWNLOAD_DELETED;
						}
					}
					
				}else if(processSelectionResult!=null && processSelectionResult.equalsIgnoreCase(CATEGORY_EXPIRED) && catId!=null) {
					processSelectionResult = CATEGORY_EXPIRED;
					whereClauseMap.put("CATEGORY_ID", catId);
					SubscriberStatus subSatus =rbtDBManager.getSubscriberActiveSelectionsBySubIdAndCatIdAndWavFileName(subId, whereClauseMap);
					if(subSatus==null) {
						boolean removeFromDownload=rbtDBManager.removeSubscriberDownloadBySubIdAndWavFileAndCatId(subId, null, Integer.parseInt(catId));
						if(removeFromDownload) {
							processSelectionResult = CATEGORY_EXPIRED_DOWNLOAD_DELETED;
						}
					}
					
				  }	
			}
			return processSelectionResult;
		}
		
	@Override
	public String deleteTone(WebServiceContext task) {
		String response = ERROR;
		String categoryID = task.getString(param_categoryID);
		if (task.containsKey(param_clipID) && categoryID != null) {
			Category category = rbtCacheManager.getCategory(Integer
					.parseInt(categoryID));
			if (category != null
					&& category.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
				logger.info("Clip Based Deleting Setting not allowed for ODA_SHUFFLE");
				return NOT_ALLOWED;
			}
		} else if (categoryID != null
				&& rbtCacheManager.getCategory(Integer.parseInt(categoryID))
						.getCategoryTpe() == iRBTConstant.PLAYLIST_ODA_SHUFFLE) {
			response = deactivateODAPack(task);
			return response;
		}
		response = super.deleteTone(task);
		return response;
	}
}
