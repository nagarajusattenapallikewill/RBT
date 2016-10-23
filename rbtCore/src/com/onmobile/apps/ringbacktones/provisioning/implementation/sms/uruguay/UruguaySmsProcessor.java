package com.onmobile.apps.ringbacktones.provisioning.implementation.sms.uruguay;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.sms.SmsProcessor;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Group;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Setting;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GroupRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

public class UruguaySmsProcessor extends SmsProcessor {
	
	public UruguaySmsProcessor() throws RBTException
	{
		
	}

	public void processManage(Task task)
	{
		//Groups group = GroupsImpl.getGroup(conn, groupID);
		task.setObject(param_mode, "CCC");
		Subscriber subscriber = getSubscriber(task);
		String subscriberID = subscriber.getSubscriberID();

		if (!isUserActive(subscriber.getStatus()))
		{
			task.setObject(param_responseSms, getSMSTextForID(task,MANAGE_INACTIVE_USER, getSMSTextForID(task,HELP_SMS_TEXT, m_helpDefault, subscriber.getLanguage()), subscriber.getLanguage()));
			return;
		}
		HashMap<String, String> hashMap = new HashMap<String, String>();

		if (param(SMS, SMS_MANAGE_ACT_DATE, true))
		{
			Date actDate = subscriber.getStartDate();
			String actDateStr = new SimpleDateFormat("dd/MM/yyyy")
			.format(actDate);
			hashMap.put("ACT_DATE", actDateStr);
		}

		if (param(SMS, SMS_MANAGE_SEL_DISPLAY, true))
		{
			StringBuilder smsBuilder = new StringBuilder();
			int songCount = 1;

			Setting[] settings = getSettings(task).getSettings();
			String selectionCountRefIdMap = "";

			if (settings != null)
			{
				for (Setting setting : settings)
				{
					if (!setting.getSelectionStatus().equals(WebServiceConstants.ACTIVE))
						continue;

					String contentName = "-";
					String callerId = null;
					String groupId = null;

					if (setting.getToneType().equals(WebServiceConstants.CATEGORY_SHUFFLE))
					{
						Category category = rbtCacheManager.getCategory(
								setting.getCategoryID(),
								subscriber.getLanguage());
						if (category == null)
							continue;

						contentName = (category.getCategoryName() == null || category.getCategoryName().equalsIgnoreCase("null")) ? "NA" : category.getCategoryName();
					}
					else
					{
						Clip clip = rbtCacheManager.getClip(
								setting.getToneID(),
								subscriber.getLanguage());
						if (clip == null)
							continue;

						contentName = (clip.getClipName() == null || clip.getClipName().equalsIgnoreCase("null")) ? "NA" : clip.getClipName();
					
					}
					
					if(setting.getCallerID() != null && !setting.getCallerID().equalsIgnoreCase("null") && !setting.getCallerID().equalsIgnoreCase("all"))
					{
						callerId = setting.getCallerID();
						if(callerId.startsWith("G") || callerId.startsWith("g"))
						{
							groupId = callerId;
							logger.info("Group id is " + groupId);
						}
						if(groupId != null)
						{	GroupRequest groupRequest = new GroupRequest(subscriber.getSubscriberID(),groupId);
							Group group = RBTClient.getInstance().getGroup(groupRequest);	
							if(group != null && group.getGroupName() != null )
								callerId = group.getGroupName();
						}

					}		
					
					selectionCountRefIdMap = selectionCountRefIdMap + ","+songCount+":"+setting.getRefID().substring(setting.getRefID().length()-5);
					smsBuilder.append(", ").append(songCount)
					.append(".").append(contentName);
					if(callerId != null && !( groupId == null && (callerId.startsWith("G") || callerId.startsWith("g"))))
					smsBuilder.append(" ").append(callerId);
					songCount++;
				}
			}
			if (smsBuilder.length() > 2)
				hashMap.put("SELECTIONS", smsBuilder.substring(2));
			else
			{
				if(getSMSTextForID(task,MANAGE_NO_SELECTION, null, subscriber.getLanguage())!= null){
					task.setObject(param_responseSms, getSMSTextForID(task,MANAGE_NO_SELECTION, null , subscriber.getLanguage()));
					return;
				}
				hashMap.put("SELECTIONS", "0");
				
			}
			if(selectionCountRefIdMap.length() > 1)
			{
				selectionCountRefIdMap = selectionCountRefIdMap.substring(1);
				
				task.setObject(param_SMSTYPE, "MANAGE");
				//remove the entry if it already exists 
				removeViraldata(task);
				//insert into viral sms table
				addViraldata(subscriberID,null,"MANAGE",selectionCountRefIdMap,"SMS",1, null);
			}
				
		}

		if (param(SMS, SMS_MANAGE_DOWNLOADS_DISPLAY, true))
		{
			StringBuilder smsBuilder = new StringBuilder();
			int songCount = 1;

			Download[] downloads = null;
			Downloads downloadsObj = getDownloads(task);
			if (downloadsObj != null)
				downloads =  downloadsObj.getDownloads();
			if (downloads != null)
			{
				for (Download download : downloads)
				{
					if (download.getEndTime().getTime() < System.currentTimeMillis())
						continue;

					String contentName = "-";
					String promoCode = "-";
					if (download.getToneType().equals(WebServiceConstants.CATEGORY_SHUFFLE))
					{
						Category category = rbtCacheManager.getCategory(
								download.getCategoryID(),
								subscriber.getLanguage());
						if (category == null)
							continue;

						contentName = (category.getCategoryName() == null || category.getCategoryName().equalsIgnoreCase("null")) ? "NA" : category.getCategoryName();
						promoCode = (category.getCategoryPromoId() == null || category.getCategoryPromoId().equalsIgnoreCase("null")) ? "NA" : category.getCategoryPromoId();
					}
					else
					{
						Clip clip = rbtCacheManager.getClip(
								download.getToneID(),
								subscriber.getLanguage());
						if (clip == null)
							continue;

						contentName = (clip.getClipName() == null || clip.getClipName().equalsIgnoreCase("null")) ? "NA" : clip.getClipName();
						promoCode = (clip.getClipPromoId() == null || clip.getClipPromoId().equalsIgnoreCase("null")) ? "NA" : clip.getClipPromoId();
					
					}

					smsBuilder.append(", ").append(songCount++).append(". ")
					.append(contentName).append("-").append(promoCode);
				}
			}

			if (smsBuilder.length() > 2)
				hashMap.put("DOWNLOADS", smsBuilder.substring(2));
			else
			{
				if(getSMSTextForID(task,MANAGE_NO_SELECTION, null, subscriber.getLanguage())!= null){
					task.setObject(param_responseSms, getSMSTextForID(task,MANAGE_NO_SELECTION, null , subscriber.getLanguage()));
					return;
				}
				hashMap.put("DOWNLOADS", "0");
			}

		}
		
		hashMap.put(
				"SMS_TEXT",
				getSMSTextForID(task,MANAGE_SUCCESS, m_manageSuccessDefault,
						subscriber.getLanguage()));

		task.setObject(param_responseSms, finalizeSmsText(hashMap));
		logger.info("SmsProcessor-removeCallerIDSel RBT::managed selection successfull  "
				+ " of the subscriber " + subscriberID);
	}
	
}
