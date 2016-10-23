package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;

public class RBTWebHelper implements iRBTConstant
{
    private static Logger logger = Logger.getLogger(RBTWebHelper.class);
    
    String m_messagePathDefault = null;
    String m_messagePath = m_messagePathDefault;
    int m_activationPeriodDefault = 0;
    int m_activationPeriod = m_activationPeriodDefault;
    boolean m_allowReactivationDefault = false;
    boolean m_allowReactivation = m_allowReactivationDefault;
    boolean m_delSelectionsDefault = true;
    boolean m_delSelections = m_delSelectionsDefault;
    String m_webSenderNoDefault = null;
    String m_webSenderNo = m_webSenderNoDefault;
    ArrayList m_subActiveDeactivatedBy = null;
    String m_webSMSTextDefault = "The password for accessing Web page is %L";
    String m_webSMSText = m_webSMSTextDefault;
    String m_countryPrefix = "91";

    boolean m_useSubscriptionManager = false;

    public RBTWebHelper() throws Exception
    {
        Tools.init("RBT_WEB", false);

        m_activationPeriod = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ACTIVATION_PERIOD", 0);

        m_allowReactivation = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "ALLOW_REACTIVATIONS", "FALSE");

        m_messagePath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "MESSAGE_PATH", null);

        m_useSubscriptionManager = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "USE_SUBSCRIPTION_MANAGER", "TRUE");

        m_delSelections = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DEL_SELECTIONS", "TRUE");

        m_subActiveDeactivatedBy = new ArrayList();

        String deactBy = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SUB_ACTIVE_DEACTIVATED_BY", null);
        if (deactBy == null)
        {
        	m_subActiveDeactivatedBy.add("AUX");
        	m_subActiveDeactivatedBy.add("NEFX");
        }
        else
        {
        	StringTokenizer stk = new StringTokenizer(deactBy, ",");
        	while (stk.hasMoreTokens())
        	{
        		m_subActiveDeactivatedBy.add(stk.nextToken());
        	}
        }

        m_webSenderNo = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "WEB_SENDER_NO", null);

        m_webSMSText = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "WEB_SMS_TEXT", null);
        if (m_webSMSText == null)
        {
        	m_webSMSText = m_webSMSTextDefault;
        }

        m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");
    }

    private boolean isSubActive(Subscriber subscriber)
    {
        if (subscriber != null)
        {
            if (subscriber.deactivatedBy() == null)
            {
                return true;
            }
            else
            {
                String deact = subscriber.deactivatedBy();
                if (m_subActiveDeactivatedBy.contains(deact))
                {
                    return true;
                }
            }
        }
        return false;
    }

    public StringBuffer getSubscriber(String strSubID) throws Exception
    {
        Subscriber subscriber = RBTDBManager.getInstance()
                .getSubscriber(strSubID);
        StringBuffer strXML = new StringBuffer();

        if (!isSubActive(subscriber))
        {
            strXML.append("<rbt></rbt>");
        }
        else
        {
            SubscriberStatus[] subscriberStatus = RBTDBManager
                    .getInstance()
                    .getSubscriberRecords(strSubID, "VUI",
                                          m_useSubscriptionManager);
            if (subscriberStatus == null)
            {
                strXML.append("<rbt><selections count=\"0\"/></rbt>");
            }
            else
            {
                strXML.append("<rbt><selections count=\""
                        + subscriberStatus.length + "\" >");

                String circleID = RBTDBManager.getInstance().getCircleId(strSubID);
                char ipPrepaid = subscriberStatus[0].prepaidYes() ? 'y' : 'n';
                for (int i = 0; i < subscriberStatus.length; i++)
                {
                    String clipName = null;
                    String callerID = null;

                    Categories category = (Categories) RBTDBManager.getInstance().getCategory(
							subscriberStatus[i].categoryID(), circleID,
							ipPrepaid);
                    
                    if (category != null)
                    {
                        if (category.type() == SHUFFLE || category.type() == RECORD)
                        {
                            clipName = category.name().trim();
                        }
                        else
                        {
                            ClipMinimal clip = RBTDBManager.getInstance()
                                    .getClipRBT(
                                                subscriberStatus[i]
                                                        .subscriberFile());
                            if (clip != null)
                                clipName = clip.getClipName().trim();
                        }

                        if (subscriberStatus[i].callerID() == null)
                        {
                            callerID = "ALL";
                        }
                        else
                        {
                            callerID = subscriberStatus[i].callerID().trim();
                        }

                        if (clipName != null)
                        {
                            strXML.append("<selection caller=\"" + callerID
                                    + "\" song=\"" + clipName
                                    + "\" fromTime=\""
                                    + subscriberStatus[i].fromTime()
                                    + "\" toTime=\""
                                    + subscriberStatus[i].toTime() + "\" />");
                        }
                    }

                }
                strXML.append("</selections></rbt>");
            }
        }
        return strXML;
    }

    public StringBuffer getActiveCategories(String circleID, char prepaidYes) throws Exception
    {
        Categories[] categories = RBTDBManager.getInstance()
                .getActiveCategories(circleID, prepaidYes);
        StringBuffer strXML = new StringBuffer();

        if (categories == null)
        {
            strXML.append("<rbt><categories></categories></rbt>");
        }
        else
        {
            strXML.append("<rbt><categories>");
            for (int i = 0; i < categories.length; i++)
            {
                if (categories[i].type() == RECORD)
                {
                    continue;
                }
                else
                {
                    strXML.append("<category id=\"" + categories[i].id()
                            + "\" name=\"" + categories[i].name()
                            + "\" parentCategory=\"" + categories[i].parentID()
                            + "\">");
                    if (categories[i].type() == BOUQUET || categories[i].type() == PARENT)
                    {
                        strXML.append(getSubCategories(categories[i].id(), categories[i].type(),
								circleID, prepaidYes));
                    }
                    else
                    {
                        strXML.append(getClips(categories[i].id()));
                    }

                    strXML.append("</category>");
                }
            }
            strXML.append("</categories></rbt>");
        }
        return strXML;
    }

    private String getClips(int categoryID) throws Exception
    {
        String buf = new String();

        Clips[] clips = RBTDBManager.getInstance()
                .getAllClips(categoryID);
        if (clips == null)
        {
        }
        else
        {
            for (int j = 0; j < clips.length; j++)
            {
                buf = buf
                        + ("<clip name=\"" + clips[j].name() + "\" wavFile=\""
                                + clips[j].wavFile() + "\" promotionID=\""
                                + clips[j].promoID() + "\"/>");
            }
        }
        return buf;
    }

    private String getSubCategories(int categoryID, int type, String circleID, char prepaidYes)
			throws Exception
    {
        String buf = new String();
        Categories[] subCategories = null;
        if (type == BOUQUET)
            subCategories = RBTDBManager.getInstance()
                    .getActiveBouquet(categoryID, circleID, prepaidYes);
        else
            subCategories = RBTDBManager.getInstance()
                    .getSubCategories(categoryID, circleID, prepaidYes);
        if (subCategories == null)
        {
            return "";
        }
        for (int i = 0; i < subCategories.length; i++)
        {
            buf = buf + "<subCategory id=\"" + subCategories[i].id()
                    + "\" name=\"" + subCategories[i].name()
                    + "\" parentCategory=\"" + subCategories[i].parentID()
                    + "\">";
            buf = buf + getClips(subCategories[i].id());
            buf = buf + "</subCategory>";
        }
        return buf;
    }

    public StringBuffer activateSubscriber(String strSubID, boolean isPrepaid)
            throws Exception
    {
        StringBuffer strXML = new StringBuffer();

        Subscriber subscriber = RBTDBManager.getInstance()
                .getSubscriber(strSubID);

        if (subscriber != null)
        {
            if (m_activationPeriod != 0
                    && RBTDBManager.getInstance().isSubDeactive(subscriber))
            {
                Date activationPeriod = subscriber.endDate();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(activationPeriod);
                long difference = (System.currentTimeMillis() - calendar
                        .getTime().getTime())
                        / (1000 * 60 * 60);
                long timePeriod = (new Integer(m_activationPeriod)).longValue();
                if (difference < timePeriod)
                {
                    strXML.append("<rbt><subscriber activate=\""
                            + m_activationPeriod + "\" /></rbt>");
                    return strXML;
                }
            }

        }

        subscriber = RBTDBManager.getInstance()
                .activateSubscriber(strSubID, "WEB", null, null, isPrepaid,
                                    m_activationPeriod, 0, "WEB", "WEB",
                                    m_useSubscriptionManager, null, false, 0, null);
        
        if (subscriber == null)
        {
            strXML.append("<rbt><subscriber activate=\"false\"/></rbt>");
        }
        else
        {
            strXML.append("<rbt><subscriber activate=\"true\"/></rbt>");
        }
        return strXML;
    }

    public StringBuffer deactivateSubscriber(String strSubID) throws Exception
    {
        String dct = RBTDBManager
                .getInstance()
                .deactivateSubscriber(strSubID, "WEB", null, m_delSelections,
                                      true, m_useSubscriptionManager, true);
        StringBuffer strXML = new StringBuffer();
        boolean success = false;
        if(dct != null && dct.equals("SUCCESS")) 
            success = true; 
        if (!success)
        {
            strXML.append("<rbt><subscriber deactivate=\"false\"/></rbt>");
        }
        else
        {
            strXML.append("<rbt><subscriber deactivate=\"true\"/></rbt>");
        }
        return strXML;
    }

    public StringBuffer removeSelection(String strSubID, String strCallerID)
            throws Exception
    {
        RBTDBManager.getInstance()
                .deactivateSubscriberRecords(strSubID, strCallerID, 1, 0, 2359,
                                             m_useSubscriptionManager, "WEB");
        StringBuffer strXML = new StringBuffer();

        strXML.append("<rbt><remove success=\"true\"/></rbt>");

        return strXML;
    }

    public StringBuffer addSelections(String strSubID, String strCallerID,
            int categoryID, String strRBT, int fromTime, int toTime)
            throws Exception
    {
        StringBuffer strXML = new StringBuffer();

        Subscriber subscriber = RBTDBManager.getInstance()
                .getSubscriber(strSubID);

        if (subscriber == null)
        {
            strXML.append("<rbt><setSelection success=\"false\"/></rbt>");
        }
        else
        {
            String subYes = null;
            if (subscriber != null)
                subYes = subscriber.subYes();

			SubscriptionClass sub = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(subscriber.subscriptionClass());
			
			String chargeClass = null;
			
			if(subscriber != null)
			{
				if(sub != null && sub.getFreeSelections() > 0 && subscriber.maxSelections() < sub.getFreeSelections())
					chargeClass = "FREE";
				
				boolean OptIn = false; 
                if(subscriber.activationInfo() != null && subscriber.activationInfo().indexOf(":optin:") != -1) 
                        OptIn = true; 

                RBTDBManager dbManager = RBTDBManager.getInstance();
                boolean inLoop = dbManager.allowLooping() && dbManager.isDefaultLoopOn();
                if(!inLoop || dbManager.moreSelectionsAllowed(strSubID, strCallerID))
                	dbManager.addSubscriberSelections(strSubID, strCallerID, categoryID,
								 strRBT, null, null, null, 1,
								 "WEB", "WEB", 0,
								 subscriber.prepaidYes(), false,
								 m_messagePath, fromTime, toTime,
								 chargeClass, m_useSubscriptionManager,
								 true, null, null, subYes,null, true,
								 OptIn, inLoop,sub.getSubscriptionClass(), subscriber,null);

            }
			strXML.append("<rbt><setSelection success=\"true\"/></rbt>");
        }
        return strXML;
    }

    public StringBuffer getChargeClass() throws Exception
    {
        List<ChargeClass> chargeClass = CacheManagerUtil.getChargeClassCacheManager().getAllChargeClass();
        StringBuffer strXML = new StringBuffer();

        if (chargeClass == null)
        {
            strXML.append("<rbt><chargeClasses></chargeClasses></rbt>");
        }
        else
        {
            strXML.append("<rbt><chargeClasses>");
            for (int i = 0; i < chargeClass.size(); i++)
            {
                strXML.append("<chargeClass type=\""
                        + chargeClass.get(i).getChargeClass() + "\" amount=\""
                        + chargeClass.get(i).getAmount() + "\">");
                strXML.append("</chargeClass>");
            }
            strXML.append("</chargeClasses></rbt>");
        }
        return strXML;
    }

    public StringBuffer searchSong(String strSong) throws Exception
    {
        StringBuffer strXML = new StringBuffer();
        if (RBTMOHelper.rbtClipsLucene == null)
        {
            strXML.append("<rbt><searchSongs></searchSongs></rbt>");
        }
        else
        {
            String[] songs = RBTMOHelper.rbtClipsLucene.search(strSong, false,
                                                               "song");
            if (songs == null)
            {
                strXML.append("<rbt><searchSongs></searchSongs></rbt>");
            }
            else
            {
                strXML.append("<rbt><searchSongs>");
                for (int i = 0; i < songs.length; i++)
                {
                    int clipID = Integer.parseInt(songs[i].trim());
                    Clips clip = RBTDBManager.getInstance()
                            .getClip(clipID);
                    if (clip != null)
                    {
                        strXML
                                .append("<searchSong name=\"" + clip.name()
                                        + "\" wavFile=\"" + clip.wavFile()
                                        + "\" promotionID=\"" + clip.promoID()
                                        + "\"/>");
                    }
                }
                strXML.append("</searchSongs></rbt>");
            }
        }
        return strXML;
    }

    public StringBuffer sendSMS(String strSubID, String strPwd)
            throws Exception
    {
        StringBuffer strXML = new StringBuffer();
        if (strSubID != null && m_webSenderNo != null)
        {

            String smsText = m_webSMSText;
            if (smsText.lastIndexOf("%L") != -1)
            {
                int index = smsText.lastIndexOf("%L");
                smsText = smsText.substring(0, index) + strPwd
                        + smsText.substring(index + 2);
            }

            boolean success = Tools.sendSMS(m_webSenderNo, strSubID, smsText,
                                            false);

            if (!success)
            {
                strXML.append("<rbt><sms success=\"false\"/></rbt>");
            }
            else
            {
                strXML.append("<rbt><sms success=\"true\"/></rbt>");
            }
        }
        else
        {
            strXML.append("<rbt><sms success=\"false\"/></rbt>");
        }
        return strXML;
    }
}