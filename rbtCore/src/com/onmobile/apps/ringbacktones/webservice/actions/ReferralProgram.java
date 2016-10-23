package com.onmobile.apps.ringbacktones.webservice.actions;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;
import com.onmobile.apps.ringbacktones.webservice.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceResponse;

/**
 * @author roshan.david
 *
 */
public class ReferralProgram implements WebServiceAction, WebServiceConstants
{
	private static Logger logger = Logger.getLogger(ReferralProgram.class);

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.actions.WebServiceAction#processAction(com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext)
	 */
	public WebServiceResponse processAction(WebServiceContext webServiceContext)
	{
		String response = FAILURE;
		try
		{

			String promoID = webServiceContext.getString(param_promoID);
			String subscriberID = webServiceContext.getString(param_subscriberID);
			String packName = webServiceContext.getString(param_subscriptionClass);
			String mode = webServiceContext.getString(param_mode);
			String clipId = webServiceContext.getString(param_clipID);
			String categoryId = webServiceContext.getString(param_categoryID);
			boolean isValidPack = false;
			boolean promoIdAvailable = false; 
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd:HH:mm:ss");
			Subscriber sub = null;
			Clip clip = null;
			Date creationTime = new Date();
			String referUrl = CacheManagerUtil.getParametersCacheManager().getParameterValue("WEBSERVICE","REFER_URL", null);
			String referXml = CacheManagerUtil.getParametersCacheManager().getParameterValue("WEBSERVICE","REFER_XML", null);

			if(subscriberID == null)
			{
				return getWebServiceResponse(response);
			}
			
			
			referXml = referXml.replaceAll("%msisdn%",subscriberID);
			referXml = referXml.replaceAll("%datetime%",dateFormat.format(new Date()));
			RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberID);
			sub = RBTClient.getInstance().getSubscriber(rbtDetailsRequest);
			logger.info("Subcriber is " + sub);
			
			// Need to build xml 
			// Need to hit configured url with xml
		//	buildReferralXml(clip,subClass,isPackReferral,isClipReferral);

			// hit the configured url and respond accordingly 

			if(clipId != null || categoryId != null)
			{
				logger.info("clip id is : " + clipId + " and categoryId is " + categoryId);
				promoIdAvailable = true;
				if(clipId != null)
					clip = RBTCacheManager.getInstance().getClip(clipId);
				if (clip == null && categoryId != null)
				{
					Category category = RBTCacheManager.getInstance().getCategory(Integer.parseInt(categoryId));
					if (category != null && category.getCategoryEndTime()!= null && category.getCategoryEndTime().getTime() > System.currentTimeMillis())
					{
						Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(category.getCategoryId(),sub.getLanguage());
						clip = clips[0];
					}
				}
				logger.info("clip value is : " + clip);

				if(clip != null)
				{
					referXml = referXml.replaceAll("%content%",clip.getClipRbtWavFile() != null ? clip.getClipRbtWavFile().substring(4, clip.getClipRbtWavFile().length()-4) : "");
					referXml = referXml.replaceAll("%contentname%",clip.getClipName() != null ? clip.getClipName() : "");
					if(clip.getClipEndTime() != null)
					{
						referXml = referXml.replaceAll("%contentexpiry%",dateFormat.format(clip.getClipEndTime()));
					}
					//expiry 
					//datetime
				}
				else
				{
					return getWebServiceResponse(response);
				}
			}
			
			if(promoID != null && clip == null)
			{
				logger.info("promo id is : " + promoID);
				promoIdAvailable = true;
				clip = RBTCacheManager.getInstance().getClipByPromoId(promoID,sub.getLanguage());
				if (clip == null)
				{
					Category category = RBTCacheManager.getInstance().getCategoryByPromoId(promoID,sub.getLanguage());
					if (category != null && category.getCategoryEndTime()!= null && category.getCategoryEndTime().getTime() > System.currentTimeMillis())
					{
						Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(category.getCategoryId(),sub.getLanguage());
						clip = clips[0];
					}
				}
				logger.info("clip value is : " + clip);

				if(clip != null)
				{
					referXml = referXml.replaceAll("%content%",clip.getClipRbtWavFile() != null ? clip.getClipRbtWavFile().substring(4, clip.getClipRbtWavFile().length()-4) : "");
					referXml = referXml.replaceAll("%contentname%",clip.getClipName() != null ? clip.getClipName() : "");
					if(clip.getClipEndTime() != null)
					{
						referXml = referXml.replaceAll("%contentexpiry%",dateFormat.format(clip.getClipEndTime()));
					}
					//expiry 
					//datetime
				}
				else
				{
					return getWebServiceResponse(response);
				}
			}
			

			
			if(packName != null)
			{
				logger.info("pack name is not null");
				List<SubscriptionClass> subclassList = CacheManagerUtil.getSubscriptionClassCacheManager().getAllSubscriptionClasses();
				if(subclassList != null)
				{
					for(int i=0;i<subclassList.size();i++)
					{
						if(packName.equalsIgnoreCase(subclassList.get(i).getOperatorCode4()))
						{
							isValidPack = true;
							logger.info("Pack name is " +subclassList.get(i).getOperatorCode4());
							referXml = referXml.replaceAll("%productid%",subclassList.get(i).getOperatorCode1() != null? subclassList.get(i).getOperatorCode1() : "");
							referXml = referXml.replaceAll("%productname%",subclassList.get(i).getOperatorCode2() != null? subclassList.get(i).getOperatorCode2() : "");
							referXml = referXml.replaceAll("%packid%",subclassList.get(i).getOperatorCode3() != null? subclassList.get(i).getOperatorCode3() : "");
							referXml = referXml.replaceAll("%packname%",subclassList.get(i).getOperatorCode4() != null? subclassList.get(i).getOperatorCode4() : "");
							referXml = referXml.replaceAll("%price%",subclassList.get(i).getSubscriptionAmount() != null ? subclassList.get(i).getSubscriptionAmount() : "");

						}
					}
					
					if(!isValidPack)
						return getWebServiceResponse(response);
				}
				else
				{
					return getWebServiceResponse(response);
				}
			}
			else if(!promoIdAvailable)
			{
				String defaultSubClass = CacheManagerUtil.getParametersCacheManager().getParameterValue("WEBSERVICE","REFER_DEFAULT_SUBCLASS", null);
				if(defaultSubClass != null)
				{
					logger.info("Using default sub class " + defaultSubClass);
					SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(defaultSubClass);
					referXml = referXml.replaceAll("%productid%",subClass.getOperatorCode1() != null? subClass.getOperatorCode1() : "");
					referXml = referXml.replaceAll("%productname%",subClass.getOperatorCode2() != null? subClass.getOperatorCode2() : "");
					referXml = referXml.replaceAll("%packid%",subClass.getOperatorCode3() != null? subClass.getOperatorCode3() : "");
					referXml = referXml.replaceAll("%packname%",subClass.getOperatorCode4() != null? subClass.getOperatorCode4() : "");
					referXml = referXml.replaceAll("%price%",subClass.getSubscriptionAmount() != null ? subClass.getSubscriptionAmount() : "");

				}
			}
			
			if(mode != null)
				referXml = referXml.replaceAll("%interface%",mode);
			
						referXml = referXml.replaceAll("%[a-z]*\\%", "");
			// HIT url 
			// Setting HttpParameters 
			HttpParameters httpParam = new HttpParameters();
			httpParam.setUrl(referUrl);
			httpParam.setConnectionTimeout(6000);

			// Setting request Params
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("xml",referXml );

			logger.info("ReferUrl: " + referUrl + ". Parameters: "+ params.toString());
			HttpResponse httpResponse = RBTHttpClient.makeRequestByPost(httpParam, params, null);
			response = httpResponse.getResponse().trim();
			logger.info("Response is "+ response);
			if(response.toUpperCase().contains("OK") || response.toUpperCase().contains("SUCCESS"))
			{
				response = SUCCESS;
				
			}
		}
		catch (Exception e)
		{
			response = FAILURE;
			logger.error(e.getMessage(), e);
		}

		return getWebServiceResponse(response);
	}


	/**
	 * @param response
	 * @return
	 */
	private WebServiceResponse getWebServiceResponse(String response)
	{
		Document document = Utility.getResponseDocument(response);
		WebServiceResponse webServiceResponse = Utility.getWebServiceResponseXML(document);
		logger.info("webServiceResponse: " + webServiceResponse);

		return webServiceResponse;
	}
}

