package com.onmobile.apps.ringbacktones.subscriptionsImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.RBTHTTPProcessing;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.WriteSDR;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.BulkPromo;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.PickOfTheDay;
import com.onmobile.apps.ringbacktones.content.Retailer;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberPromo;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.UserRights;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.CosDetails;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.tangentum.phonetix.DoubleMetaphone;

public class RBTSubUnsub implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(RBTSubUnsub.class);
	
    ResourceBundle m_bundle = null;
    private static String _class = "RBTSubUnsub";
    String[] m_validIP = null;
    String[] m_validPrefix = null;
    String m_daysDefault = "45";
    String m_days = m_daysDefault;
    int m_activationPeriodDefault = 0;
    int m_activationPeriod = m_activationPeriodDefault;
    int m_smsPromotionCategoryIDDefault = 2;
    int m_smsPromotionCategoryID = m_smsPromotionCategoryIDDefault;
    boolean m_allowReactivationDefault = false;
    boolean m_allowReactivation = m_allowReactivationDefault;
    boolean m_delSelectionsDefault = true;
    boolean m_delSelections = m_delSelectionsDefault;
    boolean m_isPrepaidDefault = false;
    boolean m_isPrepaid = m_isPrepaidDefault;
    ArrayList m_subActiveDeactivatedBy = null;
    String m_messagePathDefault = null;
    String m_messagePath = m_messagePathDefault;
    String m_filePathDefault = null;
    String m_filePath = m_filePathDefault;
    String m_invalidPrefixDefault = "You are not authorized to use this service. We apologize the inconvenience";
    String m_invalidPrefix = m_invalidPrefixDefault;

    int m_bulkSelectionSuccessCount = 0;
    int m_bulkSelectionFailureCount = 0;

    String FAILURE = "FAILURE";
    String SUCCESS = "SUCCESS";
    String CC = "Customer Care";
    String SMS = "SMS Request";
    String VP = "Voice Portal";
    String AU = "Auto";
    String OP = "Operator";
    String NA = "Not Activated";
    String NEF = "Not Enough Fund";
    String FRE = "Free";
    String VPO = "Auto Dialer";

    String m_profileCorporateCategories = "99";

    boolean m_corpChangeSelectionBlock = false;

//    private boolean m_queryWDS = true;
    private String wdsHTTPLink = null;

    private int m_clipsLimit = 15;

    static int STATUS_SUCCESS = 1;
    static int STATUS_ALREADY_ACTIVE = 2;
    static int STATUS_ALREADY_CANCELLED = 3;
    static int STATUS_NOT_AUTHORIZED = 4;
    static int STATUS_TECHNICAL_FAILURE = 5;

    private static RBTSubUnsub rbtSubUnsub = null;

    public static RBTSubUnsub init()
    {
        if (rbtSubUnsub == null)
        {
            try
            {
                rbtSubUnsub = new RBTSubUnsub();
            }
            catch (Exception e)
            {
                logger.error("", e);
                rbtSubUnsub = null;
            }
        }

        return rbtSubUnsub;

    }

    public RBTSubUnsub() throws Exception
    {
        String validIP = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "VALID_IP", null);

        StringTokenizer tokens = new StringTokenizer(validIP, ",");
        List ipList = new ArrayList();
        while (tokens.hasMoreTokens())
        {
        	ipList.add(tokens.nextToken());
        }
        if (ipList.size() > 0)
        {
        	m_validIP = (String[]) ipList.toArray(new String[0]);
        }

        String validPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "VALID_PREFIX", null);
        if (validPrefix == null)
        	throw new Exception();

        StringTokenizer token = new StringTokenizer(validPrefix, ",");
        List prefixList = new ArrayList();
        while (token.hasMoreTokens())
        {
        	prefixList.add(token.nextToken());
        }
        if (prefixList.size() > 0)
        {
        	m_validPrefix = (String[]) prefixList.toArray(new String[0]);
        }

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

        m_days = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "AUTO_DEACTIVATION_PERIOD", null);
        if (m_days == null)
        	m_days = m_daysDefault;

        m_activationPeriod = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ACTIVATION_PERIOD", 0);

        m_allowReactivation = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "ALLOW_REACTIVATIONS", "FALSE");

        m_messagePath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "MESSAGE_PATH", null);

        if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_PROMOTION_CATEGORY_ID", null) != null)
        	m_smsPromotionCategoryID = Integer.parseInt(RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "SMS_PROMOTION_CATEGORY_ID", null));

        m_delSelections = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DEL_SELECTIONS", "TRUE");

        if (RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_SUB_TYPE", "POSTPAID").equalsIgnoreCase("pre"))
        	m_isPrepaid = true;

        m_filePath = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);

        m_profileCorporateCategories = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "PROFILE_CORPORATE_CATEGORIES", "99");

        m_invalidPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "INVALID_PREFIX_TEXT", null);
        if (m_invalidPrefix == null)
        	m_invalidPrefix = m_invalidPrefixDefault;

        m_corpChangeSelectionBlock = RBTParametersUtils.getParamAsBoolean(iRBTConstant.SMS, "CORP_CHANGE_SELECTION_ALL_BLOCK", "FALSE");

        /*String queryWDSStr = rbtCommonConfig.shouldQueryWDS();
            if (queryWDSStr != null && queryWDSStr.equalsIgnoreCase("true"))
                m_queryWDS = true;*/

        wdsHTTPLink = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "WDS_HTTP_LINK", null);

        m_clipsLimit = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "CLIPS_LIMIT", 15);
    }

    public void initTools()
    {
        Tools.init("RBT_WAR", false);
    }

    public boolean isValidIP(String strIP)
    {
        for (int i = 0; i < m_validIP.length; i++)
        {
            if (strIP.trim().equalsIgnoreCase(m_validIP[i].trim()))
                return true;
        }
        return false;
    }

    public boolean isSubActive(String strSubID)
    {
        return (isSubActive(getSubscriber(strSubID)));
    }

    public boolean isValidSub(String subscriber){
        return RBTDBManager.getInstance().isValidPrefix( subscriber);
    }

    public String actSubscriber(String strSubID, String strActby,
            boolean bPrepaid, boolean bSendToHLR, String strActInfo,
            CosDetails cos)
    {
        Subscriber subscriber;
        String sub;

        int period = 0;

        if (!bSendToHLR)
        {
            period = 30;
        }

        subscriber = getSubscriber(strSubID);
        Calendar endCal = Calendar.getInstance();
        endCal.set(2037, 0, 1);
        if (subscriber != null)
        {
            if (!isSubActive(subscriber))
            {
                sub = activate(strSubID, bPrepaid, strActby, subscriber
                        .endDate(), period, strActInfo, cos);

                if (sub != null)
                    return sub;
                else
                    return SUCCESS;
            }
            else
            {
                //				if(m_allowReactivation)
                //				{
                //					reactivateSubscriber(strSubID, bPrepaid, true, strActby,
                // strActInfo);
                //					return SUCCESS;
                //				}
                return "Subscriber already exists";
            }
        }
        if (subscriber == null)
        {
            if (strSubID.length() > 10)
                strSubID = strSubID.substring(0, 10);
            sub = activate(strSubID, bPrepaid, strActby, null, period,
                           strActInfo, cos);

            if (sub != null)
                return "Unable to activate subscriber due to some internal reasons";
        }

        return SUCCESS;
    }
    
    private String activate(String strSubID, boolean bPrepaid, String strActby,Date endDate, int trialPeriod, String strActInfo, CosDetails cos) 
    {    
        return activate(strSubID, bPrepaid, strActby, endDate, trialPeriod, strActInfo, cos, 0); 
    } 
    
    private String activate(String strSubID, boolean bPrepaid, String strActby,
            Date endDate, int trialPeriod, String strActInfo, CosDetails cos, int rbtType){

        Subscriber subscriber = activateSubscriber(strSubID, strActby, null,
                                                   bPrepaid, trialPeriod,
                                                   strActInfo, cos, rbtType);
        if (subscriber == null)
            return null;
        else
            return "activated";
    }
    
    public Subscriber activateSubscriber(String strSubID, String strActBy,
            Date startDate, boolean bPrepaid, int days, String strActInfo,
            CosDetails cos, int rbtType){
        return (RBTDBManager.getInstance().activateSubscriber(strSubID, strActBy, startDate, bPrepaid, 0,
                                    days, strActInfo, cos.getSubscriptionClass(),
                                    true, cos, rbtType,null));
    }

    public Clips getClipPromoID(String clipPromoID)
    {
        return (RBTDBManager.getInstance()
                .getClipByPromoID(clipPromoID));
    }

    public String reActSubscriber(String strSubID, String strActby,
            boolean bPrepaid, String strActInfo)
    {
        Subscriber subscriber = getSubscriber(strSubID);

        if (subscriber == null)
        {
            return "Subscriber does not exist";
        }
        else
        {
            //			if(isSubActive(subscriber))
            //				reactivateSubscriber(strSubID, bPrepaid, false, strActby,
            // strActInfo);
            //			else
            //				return "Subscriber is deactive";

            return SUCCESS;

        }
    }

    public boolean isSubActive(Subscriber subscriber)
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

    public String deactSubscriber(String strSubID, String strDeactby,
            boolean bSendToHLR)
    {
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");

        Subscriber subscriber = getSubscriber(strSubID);

        if (subscriber == null)
            return "Subscriber does not exist";

        else if (!isSubActive(subscriber))
            return "Subscriber already deactivated by "
                    + subscriber.deactivatedBy() + " at "
                    + df.format(subscriber.endDate());

        else
            deactivateSubscriber(strSubID, strDeactby, null, m_delSelections,
                                 bSendToHLR);

        return SUCCESS;
    }

    public String subscriberStatus(String strSubID)
    {
        Subscriber subscriber = getSubscriber(strSubID);

        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
        if (subscriber == null)
            return "Subscriber does not exists: ";

        String startDate = df.format(subscriber.startDate());
        String endDate = df.format(subscriber.endDate());
        String actBy = subscriber.activatedBy();
        boolean prepaid = subscriber.prepaidYes();
        String subType = null;
        if (actBy != null)
        {
            if (actBy.equalsIgnoreCase("CC"))
                actBy = CC;
            if (actBy.equalsIgnoreCase("SMS"))
                actBy = SMS;
            if (actBy.equalsIgnoreCase("VP"))
                actBy = VP;
            if (actBy.equalsIgnoreCase("OP"))
                actBy = OP;
        }

        String deactBy = subscriber.deactivatedBy();
        if (deactBy != null)
        {
            if (deactBy.equalsIgnoreCase("CC"))
                deactBy = CC;
            if (deactBy.equalsIgnoreCase("SMS"))
                deactBy = SMS;
            if (deactBy.equalsIgnoreCase("VP"))
                deactBy = VP;
            if (deactBy.equalsIgnoreCase("AU"))
                deactBy = AU;
            if (deactBy.equalsIgnoreCase("OP"))
                deactBy = OP;
            if (deactBy.equalsIgnoreCase("NA"))
                deactBy = NA;
            if (deactBy.equalsIgnoreCase("NEF"))
                deactBy = NEF;
        }

        if (prepaid == true)
            subType = "Prepaid";
        else
            subType = "Postpaid";

        if (isSubActive(subscriber))
        {
            return (startDate + "," + actBy + ",Active," + subType);
        }

        if ((deactBy != null) && (deactBy.equalsIgnoreCase("AU")))
            return startDate + "," + actBy + ",Deactivated automatically on "
                    + endDate + ". No access for " + m_days + "," + subType;

        return (startDate + "," + actBy + ",Deactivated on  " + endDate + "("
                + deactBy + ")" + "," + subType);
    }

    public String[] getSubscriberSelections(String strSubID)
    {
        String circleId = RBTDBManager.getInstance()
                .getCircleId(strSubID);
        Categories category = null;
        Clips clip = null;
        String callerid, status;
        List statusList = new ArrayList();
        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");

        SubscriberStatus[] subscriberStatus = getSubscriberRecords(strSubID);
        if (subscriberStatus == null
                || (subscriberStatus.length == 1 && subscriberStatus[0]
                        .status() == 90))
            return null;
        for (int i = 0; i < subscriberStatus.length; i++)
        {
            int st = subscriberStatus[i].status();
            if (st == 90)
                continue;
            callerid = subscriberStatus[i].callerID();
            category = getCategory(subscriberStatus[i].categoryID(), circleId,
                                   (subscriberStatus[i].prepaidYes() ? 'y'
                                           : 'n'));
            clip = (Clips) getClipRBT(subscriberStatus[i].subscriberFile());
            String temp = null;
            if (category != null)
            {
                if (category.type() == SHUFFLE || category.type() == RECORD)
                {
                    temp = category.name() + "," + " ";
                }
                else
                {
                    temp = category.name() + "," + " ";
                    if (clip != null)
                        temp = category.name() + "," + clip.name();
                }
                if (callerid == null && st == 0)
                    status = "CORPORATE" + "," + temp + ","
                            + category.classType() + ","
                            + df.format(subscriberStatus[i].startTime()) + ","
                            + st;
                else if (callerid == null)
                    status = "ALL" + "," + temp + "," + category.classType()
                            + "," + df.format(subscriberStatus[i].startTime())
                            + "," + st;
                else
                    status = callerid + "," + temp + "," + category.classType()
                            + "," + df.format(subscriberStatus[i].startTime())
                            + "," + st;

                statusList.add(status);
            }
        }
        return (String[]) statusList.toArray(new String[0]);
    }

    public String addCorporateSelection(String strSubID, String song,
            boolean bPrepaid, boolean bReact, boolean bRemSel, String strActBy,
            String strActInfo, CosDetails cos)
    {
        String circleId = RBTDBManager.getInstance()
                .getCircleId(strSubID);
        logger.info("RBT::subscriber "
                + strSubID + " selected song " + song + " whether prepaid "
                + bPrepaid + " activated by " + strActBy);

        Subscriber subscriber = getSubscriber(strSubID);
        if (!isSubActive(subscriber))
        {
            logger.info("RBT::activating subscriber " + strSubID);
            Date endDate = null;

            if (subscriber != null)
                endDate = subscriber.endDate();

            String sub = activate(strSubID, bPrepaid, strActBy, endDate, 0,
                                  strActInfo, cos);
            if (sub != null)
                return sub;
        }
        //		else
        //		{
        //			if(bReact)
        //				reactivateSubscriber(strSubID, bPrepaid, false, strActBy,
        // strActInfo);
        //		}

        subscriber = getSubscriber(strSubID);

        boolean changeSubType = false;
        if (subscriber != null && bPrepaid != subscriber.prepaidYes())
            changeSubType = true;

        if (bRemSel)
            deactivateSubscriberRecords(strSubID, null, 1);
        addSelections(strSubID, null, bPrepaid, changeSubType, 99, song, null,
                      0, strActBy, strActInfo, circleId, 1, subscriber.subYes(),null);
        //		(strSubID, null, bPrepaid, changeSubType, song, null, 0, 0, strActBy,
        // strActInfo, circleId, 1);

        return null;
    }

    public File processSelections(String strFile, ArrayList preSubs,
            String strActBy, String strActInfo, CosDetails cos)
    {
        FileReader fr = null;
        FileWriter fw = null;
        BufferedReader br = null;
        File statusFile = null;

        m_bulkSelectionSuccessCount = m_bulkSelectionFailureCount = 0;

        StringBuffer success = null;
        StringBuffer failure = null;

        try
        {
            fr = new FileReader(m_filePath + File.separator + strFile);
            br = new BufferedReader(fr);

            String strSubID;
            String song;
            String subType;
            String period;
            success = new StringBuffer();
            failure = new StringBuffer();

            String line = br.readLine();

            success.append("Started... \n\n");

            while (line != null)
            {
                strSubID = song = subType = period = null;
                line = line.trim();
                StringTokenizer tokens = new StringTokenizer(line, ",");
                if (tokens.hasMoreTokens())
                    strSubID = tokens.nextToken().trim();
                if (tokens.hasMoreTokens())
                    song = tokens.nextToken().trim();
                if (tokens.hasMoreTokens())
                    subType = tokens.nextToken().trim();
                if (tokens.hasMoreTokens())
                    period = tokens.nextToken().trim();

                if (preSubs != null)
                {
                    if (preSubs.contains(strSubID))
                        subType = "prepaid";
                    else
                        subType = "postpaid";
                }

                if (strSubID != null && song != null && subType != null)
                {
                    if (isValidSub(strSubID))
                        addBulkSelections(strSubID, song, subType, period,
                                          strActBy, strActInfo, success,
                                          failure, cos);
                    else
                    {
                        failure.append(strSubID
                                + " is not a valid subscriber.\n");
                        m_bulkSelectionFailureCount++;
                    }
                }
                else
                {
                    if (strSubID == null)
                    {

                    }
                    else if (song == null || subType == null)
                        failure.append("Song or subcriber type missing for "
                                + strSubID + ".\n");

                    m_bulkSelectionFailureCount++;
                }
                line = br.readLine();
            }
            success.append("\n\n");

            failure.append("\nEnded... \n\n");

            failure.append("Processing Statistics - Success : "
                    + m_bulkSelectionSuccessCount + "   Failure : "
                    + m_bulkSelectionFailureCount);

            statusFile = new File(m_filePath
                    + File.separator
                    + "BulkSelection-"
                    + new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar
                            .getInstance().getTime()) + ".txt");

            fw = new FileWriter(statusFile);
            fw.write(success.toString());
            fw.write(failure.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                br.close();
                fr.close();
                fw.close();
            }
            catch (Exception e)
            {

            }
        }

        return statusFile;
    }

    private void addBulkSelections(String strSubID, String strSong,
            String strPrepaid, String strPeriod, String strActBy,
            String strActInfo, StringBuffer success, StringBuffer failure,
            CosDetails cos)
    {
        String circleId = RBTDBManager.getInstance()
                .getCircleId(strSubID);
        boolean isPrepaid = m_isPrepaid;
        if (strPrepaid != null)
        {
            if (strPrepaid.toLowerCase().startsWith("pre"))
                isPrepaid = true;
            else if (strPrepaid.toLowerCase().startsWith("post"))
                isPrepaid = false;
        }

        int trialPeriod = 0;
        try
        {
            trialPeriod = Integer.parseInt(strPeriod);
        }
        catch (Exception e)
        {
            trialPeriod = 0;
        }

        logger.info("RBT::subscriber "
                + strSubID + " selected song " + strSong + " " + strPrepaid
                + " activated by " + strActBy);

        Subscriber subscriber = getSubscriber(strSubID);
        if (!isSubActive(subscriber))
        {
            logger.info("RBT::activating subscriber " + strSubID);
            Date endDate = null;

            if (subscriber != null)
                endDate = subscriber.endDate();

            String sub = activate(strSubID, isPrepaid, strActBy, endDate,
                                  trialPeriod, strActInfo, cos);

            if (sub != null)
            {
                failure.append(strSubID + " activation failed.\n");
                m_bulkSelectionFailureCount++;
                return;
            }
        }

        subscriber = getSubscriber(strSubID);

        boolean changeSubType = false;

        if (isPrepaid != subscriber.prepaidYes())
            changeSubType = true;

        String wavFile = null;
        try
        {
            int clipID = Integer.parseInt(strSong);
            Clips clip = getClip(clipID);

            if (clip == null)
            {
                failure.append("Invalid clip id " + strSong
                        + " requested by subscriber " + strSubID + ". \n");
                m_bulkSelectionFailureCount++;
                return;
            }

            wavFile = clip.wavFile();
        }
        catch (Exception e)
        {
            wavFile = null;
        }

        if (wavFile == null)
        {
            HashMap phonemes = getClipsByPhonemes();

            if (phonemes == null || phonemes.size() == 0)
            {
                failure.append("No clips available. \n");
                m_bulkSelectionFailureCount++;
                return;
            }

            String encoded = getEncoding(strSong);

            wavFile = (String) phonemes.get(encoded);
        }

        if (wavFile == null)
        {
            failure.append(strSong + " requested by subscriber " + strSubID
                    + " not available in database. \n");
            m_bulkSelectionFailureCount++;
            return;
        }
        else if (wavFile.startsWith("Duplicate Entry"))
        {
            String name = wavFile.substring(wavFile.indexOf("-"));
            failure.append("Phoneme for " + strSong
                    + " requested by subscriber " + strSubID
                    + " matches with song whose wav file " + name + "\n");
            m_bulkSelectionFailureCount++;
            return;
        }

        addSelections(strSubID, null, isPrepaid, changeSubType,
                      m_smsPromotionCategoryID, wavFile, null, trialPeriod,
                      strActBy, strActInfo, circleId, 1, subscriber.subYes(),null);

        success.append("Activated " + wavFile + " for " + strPrepaid
                + " number " + strSubID + ".\n");
        m_bulkSelectionSuccessCount++;

        return;
    }

    public String subID(String subscriberID){
    	return (RBTDBManager.getInstance().subID(subscriberID));
    }

    public int isValidSub(String strSubID, Hashtable reason)
    {
        String subscriber = subID(strSubID);

        for (int i = 0; i < m_validPrefix.length; i++)
        {
            if (subscriber.substring(0, 4).equalsIgnoreCase(m_validPrefix[i]))
                return STATUS_SUCCESS;
        }
        reason.put("Reason", m_invalidPrefix);
        return STATUS_NOT_AUTHORIZED;
    }

    private HashMap getClipsByPhonemes()
    {
        ClipMinimal[] clips = getClips(null);
        HashMap hMap = null;

        if (clips != null)
        {
            hMap = new HashMap();
            for (int i = 0; i < clips.length; i++)
            {
                String encoded = getEncoding(clips[i].getClipName());
                if (hMap.containsKey(encoded))
                {
                    String name = (String) hMap.get(encoded) + ","
                            + clips[i].getWavFile();
                    hMap.put(encoded, "Duplicate Entry-" + name);
                }
                else
                {
                    hMap.put(encoded, clips[i].getWavFile());
                }
            }
        }
        return hMap;
    }

    private String getEncoding(String name)
    {
        DoubleMetaphone metaphone = new DoubleMetaphone();
        StringTokenizer st = new StringTokenizer(name.trim());
        String encoded = "";
        while (st.hasMoreTokens())
        {
            encoded = encoded + metaphone.generateKey(st.nextToken().trim());
        }
        encoded = encoded.trim();
        return encoded;
    }

    public ClipMinimal[] getClips(String start)
    {
        return (RBTDBManager.getInstance().getClipsByName(start));
    }

    public Hashtable getSMSPromoClips(String circleId, char prepaidYes)
    {
        Categories[] categories = RBTDBManager.getInstance()
                .getBouquet(circleId, prepaidYes);
        String category = m_profileCorporateCategories + ",";
        if (categories != null && categories.length > 0)
        {
            for (int i = 0; i < categories.length; i++)
            {
                category = category
                        + new Integer(categories[i].id()).toString();
                if (i < (categories.length - 1))
                {
                    category = category + ",";
                }
            }
        }
        ClipMinimal[] clips = RBTDBManager.getInstance()
                .getClipsNotInCategories1(category);
        Hashtable clipTable = new Hashtable();
        for (int j = 0; j < clips.length; j++)
        {
            clipTable.put(clips[j].getClipName(), clips[j].getWavFile());
        }

        return clipTable;
    }

    public Clips[] getAllClips(String categoryID)
    {
        return (RBTDBManager.getInstance()
                .getClipsInCategory(categoryID));
    }

    public Clips getClip(String name)
    {
        return (RBTDBManager.getInstance().getClip(name));
    }

    public Clips getClip(int clipID)
    {
        return (RBTDBManager.getInstance().getClip(clipID));
    }

    public PickOfTheDay insertPickOfTheDay(int categoryID, int clipID,
            String date, String circleId, char prepaidYes, String profile)
    {
        return (RBTDBManager.getInstance()
                .insertPickOfTheDay(4, clipID, date, circleId, prepaidYes, profile));
    }

    public PickOfTheDay[] getPickOfTheDays(String range, String circleId)
    {
        return (RBTDBManager.getInstance()
                .getPickOfTheDays(range, circleId));
    }

    public UserRights insertUserRights(String user, String rights)
    {
        return (RBTDBManager.getInstance().insertUserRights(user,
                                                                       rights));
    }

    public UserRights getUserRights(String userType)
    {
        return (RBTDBManager.getInstance().getUserRights(userType));
    }

    public Categories getCategory(int categoryID, String circleId,
            char prepaidYes)
    {
        return (RBTDBManager.getInstance().getCategory(categoryID,
                                                                  circleId,
                                                                  prepaidYes));
    }

    public void deactivateSubscriberRecords(String strSubID,
            String strCallerID, int status)
    {
        RBTDBManager.getInstance()
                .deactivateSubscriberRecords(strSubID, strCallerID, status, 0,
                                             23, true, "GUI");
        //		(strSubID, strCallerID, status);
    }

    public Categories[] getAllCategories(String circleId, char prepaidYes)
    {
        return (RBTDBManager.getInstance()
                .getAllCategories(circleId, prepaidYes));
    }

    public Subscriber getSubscriber(String strSubID)
    {
        return (RBTDBManager.getInstance().getSubscriber(strSubID));
    }

    public Categories[] getActiveBouquet(int categoryID, String circleId,
            char prepaidYes)
    {
        return (RBTDBManager.getInstance()
                .getActiveBouquet(categoryID, circleId, prepaidYes));
    }

    public void setPrepaidYes(String strSubID, boolean bPrepaid)
    {
        RBTDBManager.getInstance().setPrepaidYes(strSubID, bPrepaid);
    }

    public Categories[] getSubCategories(int categoryID, String circleId,
            char prepaidYes)
    {
        return (RBTDBManager.getInstance()
                .getSubCategories(categoryID, circleId, prepaidYes));
    }

    /*
     * public String getClassType(int categoryID) { return
     * (RBTDBManager.init(m_dbURL,
     * m_usePool).getCategory(categoryID).classType()); }
     */
    public ClipMinimal getClipRBT(String wavFile)
    {
        return (RBTDBManager.getInstance().getClipRBT(wavFile));
    }

    public String getChargeAmount(String classType)
    {
        return (CacheManagerUtil.getChargeClassCacheManager().getChargeClass(classType).getAmount());
    }

    public Categories[] getActiveCategories(String circleId, char prepaidYes)
    {
        return (RBTDBManager.getInstance()
                .getActiveCategories(circleId, prepaidYes));
    }

    public SubscriberPromo addSubscriberPromo(String strSubID, int freedays,
            boolean bPrepaid, String strActBy, String type)
    {
        return (RBTDBManager.getInstance()
                .createSubscriberPromo(strSubID, freedays, bPrepaid, strActBy,
                                       type));
        //				(strSubID, freedays, bPrepaid, strActBy));
    }

    //	private void reactivateSubscriber(String strSubID, boolean bPrepaid,
    // boolean bchargeSelections,String strActBy, String strActInfo)
    //	{
    //		RBTDBManager.init(m_dbURL, m_nConn).reactivateSubscriber(strSubID,
    // bPrepaid, bchargeSelections, "CC");
    //	}

//    public Subscriber activateSubscriber(String strSubID, String strActBy,
//            Date startDate, boolean bPrepaid, int days, String strActInfo,
//            CosDetail cos)
//    {
//        return (RBTDBManager.init(m_dbURL, m_nConn)
//                .activateSubscriber(strSubID, strActBy, startDate, bPrepaid, 0,
//                                    days, strActInfo, cos.subscriptionClass(),
//                                    true, cos));
//        //				(strSubID, strActBy, startDate, bPrepaid, m_activationPeriod, days,
//        // strActInfo, cos));
//    }

    //	public Subscriber activateSubscriber(String strSubID, String strActBy,
    // Date startDate, Date endDate, boolean bPrepaid, int days, String
    // strActInfo, CosDetail cos)
    //	{
    //		return (RBTDBManager.init(m_dbURL,
    // m_usePool).activateSubscriber(strSubID, strActBy, startDate, endDate,
    // bPrepaid, m_activationPeriod, days, strActInfo, cos));
    //	}

    //	public Subscriber activateSubscriber(String strSubID, String strActBy,
    // Date startDate, boolean bPrepaid, int days, String strActInfo, String
    // classType, CosDetail cos)
    //	{
    //		return (RBTDBManager.init(m_dbURL,
    // m_usePool).activateSubscriber(strSubID, strActBy, startDate, null,
    // bPrepaid, m_activationPeriod, days, strActInfo, classType, cos));
    //	}

    public boolean deactivateSubscriber(String strSubID, String deactivate,
            Date date, boolean delSelections, boolean bSendHLR)
    {
        return (RBTDBManager.getInstance()
                .deactivateSubscriber(strSubID, deactivate, date,
                                      delSelections, bSendHLR, true));
        //				(strSubID, deactivate, date, delSelections, bSendHLR));
    }

    private SubscriberStatus[] getSubscriberRecords(String strSubID)
    {
        return (RBTDBManager.getInstance()
                .getSubscriberRecords(strSubID, "GUI", true));
    }

    private SubscriberStatus[] getSubscriberDeactiveRecords(String strSubID)
    {
        return (RBTDBManager.getInstance()
                .getSubscriberDeactiveRecords(strSubID, "GUI"));
    }

    public String addSMSSelection(String strSubID, String strSong,
            boolean bPrepaid, String strActBy, boolean bIgnoreAct,
            boolean bFreeAct, boolean bFreeSel, boolean bReact,
            int trialPeriod, String strActInfo, CosDetails cos)
    {
        logger.info("RBT::subscriber "
                + strSubID + " selected song " + strSong + " whether prepaid "
                + bPrepaid + " activated by " + strActBy);

        Subscriber subscriber = getSubscriber(strSubID);
        String circleId = RBTDBManager.getInstance()
                .getCircleId(strSubID);
        if (!isSubActive(subscriber))
        {
            logger.info("RBT::activating subscriber " + strSubID);
            Date endDate = null;

            if (subscriber != null)
                endDate = subscriber.endDate();

            String sub = null;

            if (bFreeAct)
                sub = activate(strSubID, bPrepaid, strActBy, endDate,
                               trialPeriod, strActInfo, cos);
            else
                sub = activate(strSubID, bPrepaid, strActBy, endDate, 0,
                               strActInfo, cos);

            if (sub != null)
                return sub;
        }
        else if (bIgnoreAct)
        {
            logger.info("RBT::subscriber "
                    + strSubID + " already active");
            return "Subscriber already exists-" + strSubID;
        }
        //		else
        //		{
        //			if(bReact && !bFreeAct)
        //			{
        //				reactivateSubscriber(strSubID, bPrepaid, false, strActBy,
        // strActInfo);
        //			}
        //		}

        subscriber = getSubscriber(strSubID);

        boolean changeSubType = false;
        if (bPrepaid != subscriber.prepaidYes())
            changeSubType = true;

        if (bFreeSel)
            return (addSelections(strSubID, null, bPrepaid, changeSubType,
                                  m_smsPromotionCategoryID, strSong, null,
                                  trialPeriod, strActBy, strActInfo, circleId,
                                  1, subscriber.subYes(),null));
        else
            return (addSelections(strSubID, null, bPrepaid, changeSubType,
                                  m_smsPromotionCategoryID, strSong, null, 0,
                                  strActBy, strActInfo, circleId, 1, subscriber
                                          .subYes(),null));

    }

    private boolean isCorpSub(String strSubID)
    {
        return (isSubAlreadyActiveOnStatus(strSubID, null, 0));
    }

    private boolean isSubAlreadyActiveOnStatus(String strSubID,
            String callerID, int status)
    {
        SubscriberStatus subStatus = RBTDBManager.getInstance()
                .getActiveSubscriberRecord(strSubID, callerID, status, 0, 23);

        if (subStatus != null)
            return true;

        return false;
    }

    public String addSelections(String strSubID, String strCallerID,
            boolean bPrepaid, boolean changeSubType, int categoryID,
            String songName, Date endDate, int trialPeriod,
            String strSelectedBy, String strSelectionInfo, String circleId,
            int loopSetting, String subYes,String selInterval)
    {
        if (endDate == null)
        {
            Calendar endCal = Calendar.getInstance();
            endCal.set(2037, 0, 1);
            endDate = endCal.getTime();
        }

        if (strCallerID == null && m_corpChangeSelectionBlock
                && isCorpSub(strSubID))
        {
            return ("corp");
        }
        else
        {
            /*
             * RBTDBManager.init(m_dbURL,
             * m_usePool).addSubscriberSelections(strSubID, strCallerID,
             * categoryID, songName, null, null, endDate, loopSetting,
             * strSelectedBy, strSelectionInfo, trialPeriod, bPrepaid,
             * changeSubType, m_messagePath, 0, 23, null, true, false, null,
             * null, subYes, null, circleId, false);
             */

            //			(strSubID, strCallerID, categoryID, songName, null, null,
            // endDate, status, strSelectedBy,
            //			strSelectionInfo, trialPeriod, bPrepaid, changeSubType,
            // m_messagePath, circleId, loopSetting);
            RBTDBManager rbtDBManager = RBTDBManager.getInstance();

            boolean clipYes = false;
            Clips clip = rbtDBManager.getClipByPromoID(songName);
            if (clip != null)
                clipYes = true;

            SubscriberDownloads download = rbtDBManager
                    .getActiveSubscriberDownload(strSubID, songName);
            Subscriber subscriber = rbtDBManager.getSubscriber(strSubID);

            if (download == null)
            {
                rbtDBManager.addSubscriberDownload(strSubID, songName,
                                                   categoryID, false,
                                                   (clipYes ? DTMF_CATEGORY
                                                           : BOUQUET));

                int maxSelections = subscriber.maxSelections();

                maxSelections++;
                rbtDBManager.updateNumMaxSelections(strSubID, maxSelections);
            }

            rbtDBManager.addSubscriberSelections(strSubID, strCallerID,
                                                 categoryID, songName, null,
                                                 null, endDate, loopSetting,
                                                 strSelectedBy,
                                                 strSelectionInfo, trialPeriod,
                                                 bPrepaid, changeSubType,
                                                 m_messagePath, 0, 23, null,
                                                 true, false, null, null,
                                                 subYes, null, circleId, false, true, (loopSetting == 2), subscriber,selInterval);
        }

        return null;
    }

    public String[] getSubscriberDeactivatedRecords(String strSubID)
    {
        SubscriberStatus[] subscriberStatus = null;
        Categories category = null;
        Clips clip = null;
        String callerid, status;
        List statusList = new ArrayList();

        String circleId = RBTDBManager.getInstance()
                .getCircleId(strSubID);

        SimpleDateFormat df = new SimpleDateFormat("EEE MMM d yyyy  h:mm a");
        subscriberStatus = getSubscriberDeactiveRecords(strSubID);
        if (subscriberStatus == null
                || (subscriberStatus.length == 1 && subscriberStatus[0]
                        .status() == 90))
            return null;
        for (int i = 0; i < subscriberStatus.length; i++)
        {
            int st = subscriberStatus[i].status();
            if (st == 90)
                continue;
            callerid = subscriberStatus[i].callerID();
            category = getCategory(subscriberStatus[i].categoryID(), circleId,
                                   (subscriberStatus[i].prepaidYes() ? 'y'
                                           : 'n'));
            clip = (Clips) getClipRBT(subscriberStatus[i].subscriberFile());

            String temp = null;
            if (category != null)
            {
                if (category.type() == SHUFFLE || category.type() == RECORD)
                {
                    temp = category.name() + "," + " ";
                }
                else
                {
                    temp = category.name() + "," + " ";
                    if (clip != null)
                        temp = category.name() + "," + clip.name();
                }
                if (callerid == null && st == 0)
                    status = "CORPORATE" + "," + temp + ","
                            + category.classType() + ","
                            + df.format(subscriberStatus[i].endTime());

                else if (callerid == null)
                    status = "ALL" + "," + temp + "," + category.classType()
                            + "," + df.format(subscriberStatus[i].endTime());
                else
                    status = callerid + "," + temp + "," + category.classType()
                            + "," + df.format(subscriberStatus[i].endTime());
                statusList.add(status);
            }
        }
        return (String[]) statusList.toArray(new String[0]);
    }

    //	public ViralBlackListTable insertViralBlackList(String subscriberID)
    //	{
    //		Calendar endCal = Calendar.getInstance();
    //		endCal.set(2037, 0, 1);
    //		Date endDate = endCal.getTime();
    //
    //		return (RBTDBManager.init(m_dbURL,
    // m_usePool).insertViralBlackList(subscriberID,null,endDate));
    //	}
    //	public boolean removeViralBlackList(String subscriberID)
    //	{
    //		return (RBTDBManager.init(m_dbURL,
    // m_usePool).removeViralBlackList(subscriberID));
    //	}
    //	public ViralBlackListTable getViralBlackList(String subscriberID)
    //	{
    //		return (RBTDBManager.init(m_dbURL,
    // m_usePool).getViralBlackList(subscriberID));
    //	}
    //
    //	public boolean addBlackListFile(String strFile)
    //	{
    //		FileReader fr = null;
    //		BufferedReader br = null;
    //
    //		try
    //		{
    //			fr = new FileReader(m_filePath + File.separator + strFile);
    //			br = new BufferedReader(fr);
    //
    //			String strSubID;
    //			String line = br.readLine();
    //			while(line != null)
    //			{
    //				line = line.trim();
    //				strSubID = line;
    //				if(strSubID != null)
    //				{
    //					if(isValidSub(strSubID))
    //						insertViralBlackList(strSubID);
    //				}
    //				line = br.readLine();
    //			}
    //		}
    //		catch(Exception e)
    //		{
    //			return false;
    //		}
    //		finally
    //		{
    //			try
    //			{
    //				br.close();
    //				fr.close();
    //			}
    //			catch(Exception e)
    //			{
    //			}
    //
    //		}
    //		return true;
    //	}
    //
    //	public boolean removeBlackListFile(String strFile)
    //	{
    //		FileReader fr = null;
    //		BufferedReader br = null;
    //
    //		try
    //		{
    //			fr = new FileReader(m_filePath + File.separator + strFile);
    //			br = new BufferedReader(fr);
    //
    //			String strSubID;
    //			String line = br.readLine();
    //			while(line != null)
    //			{
    //				line = line.trim();
    //				strSubID = line;
    //				if(strSubID != null)
    //				{
    //					if(isValidSub(strSubID))
    //						removeViralBlackList(strSubID);
    //				}
    //				line = br.readLine();
    //			}
    //		}
    //		catch(Exception e)
    //		{
    //			return false;
    //		}
    //		finally
    //		{
    //			try
    //			{
    //				br.close();
    //				fr.close();
    //			}
    //			catch(Exception e)
    //			{
    //
    //			}
    //
    //		}
    //		return true;
    //	}
    //
    //	public File ViewBlackListFile(String strFile)
    //	{
    //		FileReader fr = null;
    //		FileWriter fw = null;
    //		BufferedReader br = null;
    //		File statusFile = null;
    //
    //		StringBuffer success = null;
    //		StringBuffer failure = null;
    //
    //
    //		try
    //		{
    //			fr = new FileReader(m_filePath + File.separator + strFile);
    //			br = new BufferedReader(fr);
    //
    //			String strSubID;
    //			success = new StringBuffer();
    //			failure = new StringBuffer();
    //
    //			String line = br.readLine();
    //			success.append("Started... \n\n");
    //
    //			while(line != null)
    //			{
    //				line = line.trim();
    //				strSubID = line;
    //				if(strSubID != null)
    //				{
    //					if(isValidSub(strSubID))
    //					{
    //						ViralBlackListTable viralBlackList = getViralBlackList(strSubID);
    //						if(viralBlackList == null)
    //							failure.append(strSubID + " is not a blacklist subscriber.\n");
    //						else
    //						{
    //							success.append(strSubID);
    //							success.append(",");
    //							success.append(new SimpleDateFormat("yyyy/MM/dd
    // HH:mm:ss").format(viralBlackList.startTime()));
    //							success.append("\n");
    //						}
    //					}
    //					else
    //					{
    //						failure.append(strSubID + " is not a valid subscriber.\n");
    //
    //					}
    //				}
    //				line = br.readLine();
    //			}
    //			statusFile = new File(m_filePath + File.separator + "BlackList-" + new
    // SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime())
    // + ".txt");
    //			fw = new FileWriter(statusFile);
    //			fw.write(success.toString());
    //			fw.write(failure.toString());
    //
    //		}
    //		catch(Exception e)
    //		{
    //			return null;
    //		}
    //		finally
    //		{
    //			try
    //			{
    //				br.close();
    //				fr.close();
    //				fw.close();
    //			}
    //			catch(Exception e)
    //			{
    //
    //			}
    //		}
    //		return statusFile;
    //	}

    public String getCircleId(List<SitePrefix> circles, String subscriberID)
    {
        String circleID = "Default";

        for (SitePrefix sitePrefix : circles)
        {
            String prefix = sitePrefix.getSitePrefix();

            StringTokenizer st = new StringTokenizer(prefix, ",");

            while (st.hasMoreTokens())
            {
                String testToken = st.nextToken();

                if (subscriberID.startsWith(testToken.trim()))
                {
                    circleID = sitePrefix.getCircleID();
                    logger.info("got circleId as "
                            + circleID);
                    return circleID;
                }
            }
        }

        return circleID;
    }

    public boolean activateSubscriberByBulkPromo(String subscriberID,
            String activatedBy, boolean prePaidYes, CosDetails cos)
    {
    	 return activateSubscriberByBulkPromo(subscriberID, activatedBy, prePaidYes, cos, false);
     }
 
     public boolean activateSubscriberByBulkPromo(String subscriberID,
             String activatedBy, boolean prePaidYes, CosDetails cos, boolean isFTPActivation)
     {
        boolean status = false;
        logger.info("RBT::entered");
        RBTDBManager rbtDBManager = RBTDBManager.getInstance();
        String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null);

        String workingDir = pathDir + File.separator + activatedBy
                + "_act_insert_failure";
        int rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);
        String eventType = "BULK_ACTIVATION";
        String subscriberType = (prePaidYes) ? "PRE_PAID" : "POST_PAID";
        String request = "activation";
        String response = "initial_failure";
        String requestedTimeStamp = (new Date()).toString();
        String responseTimeInMillis = "NA";
        String referenceID = activatedBy;
        String requestDetail = "insert_into_RBT_SUBSCRIBER";
        String responseDetail = "not_inserted";
        String activationInfo = activatedBy;
         if(isFTPActivation)
        	 activationInfo = activatedBy + ":GROUPADD";

        BulkPromo bulkPromo = rbtDBManager.getBulkPromo(activatedBy);

        Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
        if (subscriber == null && bulkPromo != null)
        {
            //			Calendar cal = Calendar.getInstance();
            //			Date endDate = bulkPromo.promoEndDate();
            //			cal.setTime(endDate);
            //			cal.add(Calendar.DATE, -1);
            //
            //			Calendar cal2 = Calendar.getInstance();
            //			cal.set(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
            //
            //			endDate = cal.getTime();

            Date startDate = bulkPromo.promoStartDate();

            subscriber = activateSubscriber(subscriberID, activatedBy,
                                            startDate, prePaidYes, 0,
                                            activationInfo, cos, 0);
            //			(subscriberID, activatedBy, startDate, endDate, prePaidYes, 0,
            // activatedBy, cos);
            if (subscriber != null)
            {
                status = true;

                workingDir = pathDir + File.separator + activatedBy
                        + "_act_insert_success";
                response = "initial_success";
                responseDetail = "successfully_inserted";
            }
        }
        logger.info("RBT::workingDir = " + workingDir);
        WriteSDR.addToAccounting(workingDir, rotationSize, eventType,
                                 subscriberID, subscriberType, request,
                                 response, requestedTimeStamp,
                                 responseTimeInMillis, referenceID,
                                 requestDetail, responseDetail);

        return status;
    }

    public boolean deactivateSubscriberByBulkPromo(String strSubID,
            String deactivate, Date date, boolean delSelections,
            boolean bSendHLR)
    {
        RBTDBManager rbtDBManager = RBTDBManager.getInstance();

        Subscriber subcriber = rbtDBManager.getSubscriber(strSubID);

        String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null);
        String workingDir = pathDir + File.separator + deactivate
                + "_deact_update_failure";
        int rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);
        String eventType = "BULK_DEACTIVATION";
        String subscriberType = (subcriber.prepaidYes()) ? "PRE_PAID"
                : "POST_PAID";
        String request = "deactivation";
        String response = "no_record";
        String requestedTimeStamp = (new Date()).toString();
        String responseTimeInMillis = "NA";
        String referenceID = deactivate;
        String requestDetail = "update_RBT_SUBSCRIBER";
        String responseDetail = "not_updated";
        if (rbtDBManager.checkCanAddSetting(strSubID))
        {
            boolean value = deactivateSubscriber(strSubID, deactivate, date,
                                                 delSelections, bSendHLR);
            if (value)
            {
                workingDir = pathDir + File.separator + deactivate
                        + "_deact_update_success";
                response = "updated";
                responseDetail = "updated";
            }
            else
            {
                response = "cannot_update";
            }
            logger.info("RBT:: WorkingDir=" + workingDir);
            WriteSDR.addToAccounting(workingDir, rotationSize, eventType,
                                     strSubID, subscriberType, request,
                                     response, requestedTimeStamp,
                                     responseTimeInMillis, referenceID,
                                     requestDetail, responseDetail);
            return (value);
        }
        else
        {
            if (subcriber != null)
                response = "cannot_update";
            logger.info("RBT:: WorkingDir=" + workingDir);
            WriteSDR.addToAccounting(workingDir, rotationSize, eventType,
                                     strSubID, subscriberType, request,
                                     response, requestedTimeStamp,
                                     responseTimeInMillis, referenceID,
                                     requestDetail, responseDetail);
            return false;
        }
    }

    public boolean addSelectionsforBulkPromo(String subscriberID,
            String wavFile, String activatedBy, boolean isClipWavFile,
            boolean firstSelection)
    {
        boolean status = false;

        RBTDBManager rbtDBManager = RBTDBManager.getInstance();
        String pathDir = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "REPORT_PATH", null);

        String workingDir = pathDir + File.separator + activatedBy
                + "_sel_insert_failure";
        int rotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);
        String eventType = "BULK_SELECTION";
        String subscriberType = "unknown";
        String request = "selection";
        String response = "subscriber_not_found";
        String requestedTimeStamp = (new Date()).toString();
        String responseTimeInMillis = "NA";
        String referenceID = activatedBy;
        String requestDetail = "insert_into_RBT_SUBSCRIBER_SELECTIONS";
        String responseDetail = "not_inserted";

        Subscriber subscriber = rbtDBManager.getSubscriber(subscriberID);
        if (subscriber != null)
        {
            /*
             * char prepaidYes = 'n'; if(subscriber.prepaidYes()) prepaidYes =
             * 'y';
             */

            if (activatedBy.equals(subscriber.activatedBy()))
            {
                String circleId = rbtDBManager.getCircleId(subscriberID);

                int categoryID = 4;
                if (isClipWavFile)
                {
                    categoryID = 3;
                }

                int loopSetting = 1;
                if (!firstSelection)
                    loopSetting = 2;

                Calendar calendar = Calendar.getInstance();
                calendar.set(2037, Calendar.DECEMBER, 31);
                Date endDate = calendar.getTime();

                int maxSelections = subscriber.maxSelections();
                SubscriberDownloads download = rbtDBManager
                        .getActiveSubscriberDownload(subscriberID, wavFile);
                if (download == null)
                {
                    //Categories category =
                    // rbtDBManager.getCategory(categoryID,circleId,prepaidYes);
                    //rbtDBManager.addSubscriberDownload(subscriberID, wavFile,
                    // categoryID, (isClipWavFile ? 'y' : 'n'), false,
                    // category.type());
                    rbtDBManager
                            .addSubscriberDownload(
                                                   subscriberID,
                                                   wavFile,
                                                   categoryID,
                                                   false,
                                                   (isClipWavFile ? DTMF_CATEGORY
                                                           : BOUQUET));
                    if (isClipWavFile)
                    {
                        maxSelections++;
                        rbtDBManager.updateNumMaxSelections(subscriberID,
                                                            maxSelections);
                    }
                }

                addSelections(subscriberID, null, subscriber.prepaidYes(),
                              false, categoryID, wavFile, endDate, 0,
                              activatedBy, activatedBy + "|" + maxSelections,
                              circleId, loopSetting, subscriber.subYes(),null);
                status = true;

                workingDir = pathDir + File.separator + activatedBy
                        + "_sel_insert_success";
                response = "initial_success";
                responseDetail = "inserted_successfully";
            }
            else
            {
                response = "subscriber_not_activated_by_" + activatedBy;
            }
        }
        logger.info("RBT:: WorkingDir=" + workingDir);
        WriteSDR.addToAccounting(workingDir, rotationSize, eventType,
                                 subscriberID, subscriberType, request,
                                 response, requestedTimeStamp,
                                 responseTimeInMillis, referenceID,
                                 requestDetail, responseDetail);

        return status;
    }

    public boolean deletedSelection(String subscriberID, String promoID)
    {
        return (RBTDBManager.getInstance()
                .updateDownloadStatus(subscriberID, promoID, 'd'));
    }

    public boolean isSubscriberBulkActivated(String subscriberId)
    {
        Subscriber subscriber = RBTDBManager.getInstance()
                .getSubscriber(subscriberId);
        if (subscriber != null)
        {
            String activateBy = subscriber.activatedBy();

            if (activateBy.equals("VP") || activateBy.equals("SMS")
                    || activateBy.equals("VP-PROMO")
                    || activateBy.equals("SMS_PROMO"))
                return false;

            BulkPromo bulkPromo = RBTDBManager.getInstance()
                    .getActiveBulkPromo(activateBy);
            return (bulkPromo != null);
        }
        else
            return false;
    }

    public boolean createSubscriberPromo(String subscriberID, CosDetails cos)
    {
        SubscriberPromo subscriberPromo = RBTDBManager.getInstance()
                .createSubscriberPromo(subscriberID, cos.getValidDays(),
                                       cos.prepaidYes(), null, cos.getCosId());

        return (subscriberPromo != null);
    }

    public boolean createRetailer(String retailerID, String retailerName)
    {
        Retailer retailer = RBTDBManager.getInstance()
                .insertRetailer(retailerID, "RET", retailerName);

        return (retailer != null);
    }

    public static long deltaDays(Date start, Date end)
    {
        long MILLIS_PER_DAY = 1000 * 60 * 60 * 24;
        long deltaMillis = end.getTime() - start.getTime();

        return deltaMillis / MILLIS_PER_DAY;
    }

    public String activateAndAddSelection(String strSubID, String callerID,
    		String strActBy, String actInfo, String subWavFile, CosDetails cos, int rbtType)
    {
        String returnValue = null;

        RBTDBManager rbtDBManager = RBTDBManager.getInstance();

        Subscriber subscriber = rbtDBManager.getSubscriber(strSubID);

        if (subscriber != null)
        {
            if (rbtDBManager.isSubscriberActivationPending(subscriber))
                returnValue = "USER_ACTIVATION_PENDING";
            if (rbtDBManager.isSubscriberDeactivationPending(subscriber))
                returnValue = "USER_DEACTIVATION_PENDING";
        }
        else
        {
            activate(strSubID, cos.prepaidYes(), strActBy, null, 0, actInfo,
                     cos,rbtType);
            subscriber = rbtDBManager.getSubscriber(strSubID);
        }

        if (checkSettingActive(strSubID, null, subWavFile))
            returnValue = "SONG_EXISTS";
        else if (reachedClipLimit(strSubID))
            returnValue = "USER_LIBRARY_FULL";

        if (returnValue == null)
        {
            subscriber = getSubscriber(strSubID);

            String selectedBy = strActBy;
            if (!cos.isDefaultCos()
                    && subscriber.maxSelections() < cos.getFreeSongs())
                selectedBy = selectedBy + "-COS" + cos.getCosId();

            addSelections(strSubID, callerID, cos.prepaidYes(), false, 3,
                          subWavFile, null, 0, selectedBy, actInfo, cos
                                  .getCircleId(), 2, subscriber.subYes(),null);
            returnValue = "SUCCESS";
        }

        return returnValue;
    }

    private boolean checkSettingActive(String subscriberID, String callerID,
            String subscriverFile)
    {
        SubscriberStatus selection = RBTDBManager.getInstance()
                .getSubWavFileForCaller(subscriberID, callerID, subscriverFile);
        if (selection != null
                && (selection.selStatus().equals(STATE_ACTIVATED) || selection
                        .selStatus().equals(STATE_ACTIVATION_PENDING) || selection
                        .selStatus().equals(STATE_TO_BE_ACTIVATED)))
            return true;
        else
            return false;
    }

    private boolean reachedClipLimit(String subscriberID)
    {
        //SubscriberDownloads[] subDownloads = RBTDBManager.init(m_dbURL,
        // m_usePool)
        //                                        .getActiveSubscriberDownloads(subscriberID);

        int count = 0;

        /*
         * for(int i = 0; subDownloads != null && i < subDownloads.length; i++) {
         * if(subDownloads[i].clipYes()) count++; }
         */

        SubscriberDownloads[] subDownloads = RBTDBManager.getInstance()
                .getSubscriberAllActiveDownloads(subscriberID, DTMF_CATEGORY);
        if (subDownloads != null)
            count = subDownloads.length;
        return (count >= m_clipsLimit);
    }

    public HashMap getPreOrPost(String subscriberId, char prepaidYes, String circleID, int rbtType,String strChannel)
    {
		String returnValue = null;
		String subscriberStatus = null;

//		char prepaidYes = 's';
		CosDetails cos = null;

		try {
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			String smsLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_SMS_LOG_PATH", null);
			int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 8000);

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);

			String urlstr = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "HTTP_LINK", "");
			urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SUBSCRIBER_STATUS_PAGE", "");
			urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_ACCOUNT", "") + "&";
			urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR_PASSWORD", "") + "&";
			urlstr += "phonenumber=" + subscriberId + "&";
			urlstr += RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "OPERATOR", "");

			RBTHTTPProcessing rbtHTTPProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String result = rbtHTTPProcessing.makeRequest1(urlstr, subscriberId, "RBT_WAR");
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String requestedTimeString = formatter.format(requestedTimeStamp);

//			logger.info("RBT::result = " + result);
//			logger.info("RBT::m_queryWDS = " + m_queryWDS);

			if (result != null) {
				result = result.trim();
				if (result.length() <= 2) {
					String prepaidStatus = "unknown";
					if(prepaidYes == 'y')
						prepaidStatus = "PRE_PAID";
					else if(prepaidYes == 'y')
						prepaidStatus = "POST_PAID";
					
					if (result.equals("3")) {
						WriteSDR.addToAccounting(smsLogPath, logRotationSize,
								"RBT_SUBSCRIBER_STATUS", subscriberId, prepaidStatus,
								"subscriber_status", "invalid_user", requestedTimeString,
								differenceTime + "", "RBT_WAR", urlstr, result);
						logger.info("RBT:: user" + subscriberId
								+ " in invalid");
						returnValue = "invalid-user";
					}
					if (result.equals("9")) {
						WriteSDR.addToAccounting(smsLogPath, logRotationSize,
								"RBT_SUBSCRIBER_STATUS", subscriberId, prepaidStatus,
								"subscriber_status", "user_is_suspended", requestedTimeString,
								differenceTime + "", "RBT_WAR", urlstr, result);
						logger.info("RBT:: user" + subscriberId
								+ " in suspended state");
						returnValue = "suspended";
					}
					else if (result.equals("6")) {
						WriteSDR
								.addToAccounting(smsLogPath, logRotationSize,
										"RBT_SUBSCRIBER_STATUS", subscriberId, prepaidStatus,
										"subscriber_status", "gifting_under_processing",
										requestedTimeString, differenceTime + "", "RBT_WAR",
										urlstr, result);
						logger.info("RBT::gifting under processing");
						returnValue = "activation-pending";
					}
					else if (result.equals("5")) {
						WriteSDR.addToAccounting(smsLogPath, logRotationSize,
								"RBT_SUBSCRIBER_STATUS", subscriberId, prepaidStatus,
								"subscriber_status", "user_is_blacklisted", requestedTimeString,
								differenceTime + "", "RBT_WAR", urlstr, result);
						logger.info("RBT::" + subscriberId
								+ " is a blacklisted user");
						returnValue = "black-listed";
					}
					// quering WDS as querSubscriberStatus returned unkown user
					else if (result.equals("7")) {
						WriteSDR.addToAccounting(smsLogPath, logRotationSize,
								"RBT_SUBSCRIBER_STATUS", subscriberId, prepaidStatus,
								"subscriber_status", "user_is_new_user", requestedTimeString,
								differenceTime + "", "RBT_WAR", urlstr, result);
						if (subscriber != null
								&& !rbtDBManager.isSubscriberActivationPending(subscriber))
							rbtDBManager.deactivateSubscriberForTATA(subscriberId);
						
						cos = rbtDBManager.getSubscriberCos(subscriberId, circleID, String
								  .valueOf(prepaidYes), strChannel, false);
						 returnValue = "new-user";
						/*if (m_queryWDS) {
							prepaidYes = queryWDS(subscriberId);
							if (prepaidYes != 's') {
								cos = rbtDBManager.getSubscriberCos(subscriberId, circleID,
										new Character(prepaidYes).toString(), "SMS");
								returnValue = "new-user";
							}
							else
								returnValue = "invalid-user";
						}*/
					}
					else if (result.equals("10")) {
						WriteSDR.addToAccounting(smsLogPath, logRotationSize,
								"RBT_SUBSCRIBER_STATUS", subscriberId, prepaidStatus,
								"subscriber_status", "user_express_copy_pending", requestedTimeString,
								differenceTime + "", "RBT_WAR", urlstr, result);
						if(subscriber != null
								&& !rbtDBManager.isSubscriberActivationPending(subscriber))
							rbtDBManager.deactivateSubscriberForTATA(subscriberId);
						returnValue = "express-copy-pending";
					}
				}
				else if (result.length() > 2 && result.length() < 20) {
					String prepaidStatus;
					StringTokenizer st = new StringTokenizer(result, "|");
					if (st.hasMoreTokens())
						subscriberStatus = st.nextToken();
					if (st.hasMoreTokens()) {
						prepaidStatus = st.nextToken();
						if (prepaidStatus.equals("1")) {
							if (subscriber != null && !subscriber.prepaidYes())
								rbtDBManager.changeSubscriberType(subscriberId, true);
							prepaidYes = 'y';
						}
						else {
							if (subscriber != null && subscriber.prepaidYes())
								rbtDBManager.changeSubscriberType(subscriberId, false);
							prepaidYes = 'n';
						}
					}

					String userType = "";
					if (prepaidYes == 'y')
						userType = "PRE_PAID";
					else
						userType = "POST_PAID";

					cos = rbtDBManager.getSubscriberCos(subscriberId, circleID, new Character(
							 prepaidYes).toString(), strChannel, false);

					if (subscriberStatus.equals("1") || subscriberStatus.equals("6")) {
						WriteSDR
								.addToAccounting(smsLogPath, logRotationSize,
										"RBT_SUBSCRIBER_STATUS", subscriberId, userType,
										"subscriber_status", "users_activation_pending",
										requestedTimeString, differenceTime + "", "RBT_WAR",
										urlstr, result);
						logger.info("RBT::before open state");

						boolean isPrepaid = false;
						if (prepaidYes == 'y')
							isPrepaid = true;
						if (subscriber == null) {
							logger.info("RBT::no details with us, making pending entry into the database");
							activateSubscriber(subscriberId, "WAR", null, isPrepaid, 0, "WAR", cos,rbtType);
							//rbtDBManager.updateActivationPendingSubscriberTATA(subscriberId);
							rbtDBManager.smURLSubscription(subscriberId, true, false, null);
						}
						returnValue = "activation-pending";
					}
					else if (subscriberStatus.equals("5") || subscriberStatus.equals("7")) {
						WriteSDR
								.addToAccounting(smsLogPath, logRotationSize,
										"RBT_SUBSCRIBER_STATUS", subscriberId, userType,
										"subscriber_status", "users_deactivation_pending",
										requestedTimeString, differenceTime + "", "RBT_WAR",
										urlstr, result);
						logger.info("RBT::deactivation pending");
						returnValue = "deactivation-pending";
					}
					else if (subscriberStatus.equals("2")) {
						WriteSDR.addToAccounting(smsLogPath, logRotationSize,
								"RBT_SUBSCRIBER_STATUS", subscriberId, userType,
								"subscriber_status", "activative_user", requestedTimeString,
								differenceTime + "", "RBT_WAR", urlstr, result);
						if (!rbtDBManager.checkCanAddSetting(subscriber)) {
							if (subscriber == null) {
								logger.info("RBT::subscriber is registered, but no detail in our database");
								logger.info("RBT::subscribing in our database");

								boolean isPrepaid = false;
								if (prepaidYes == 'y')
									isPrepaid = true;
								cos = CacheManagerUtil.getCosDetailsCacheManager().getDefaultCosDetail(circleID, new Character(
										prepaidYes).toString());
								activateSubscriber(subscriberId, "WAR", null, isPrepaid, 0, "WAR",
										cos,rbtType);

								Calendar nextChargingDate = Calendar.getInstance();
								nextChargingDate.set(2035, 11, 31, 0, 0, 0);

								String type = "B";
								if (isPrepaid)
									type = "P";

								/*boolean acceptRenewal = false;
								if (cos != null)
									acceptRenewal = cos.renewalAllowed() && cos.acceptRenewal();

								rbtDBManager.updateActivatedSubscriberTATA(subscriberId, acceptRenewal);
								subscriber = rbtDBManager.getSubscriber(subscriberId);*/
								rbtDBManager.smSubscriptionSuccess(subscriberId, nextChargingDate
										.getTime(), new Date(), type, subscriber
										 .subscriptionClass(), true, cos, subscriber.rbtType());
							}
						}

						returnValue = "activated";
					}
					else if (subscriberStatus.equals("4")) {
						WriteSDR.addToAccounting(smsLogPath, logRotationSize,
								"RBT_SUBSCRIBER_STATUS", subscriberId, userType,
								"subscriber_status", "user_is_deactived", requestedTimeString,
								differenceTime + "", "RBT_WAR", urlstr, result);
						logger.info("RBT::subscriber is unregistered subscriber, checking for promo");

						if ((subscriber != null)
								&& (rbtDBManager.checkCanAddSetting(subscriber) || rbtDBManager
										.isSubscriberDeactivationPending(subscriber))) {
							rbtDBManager.deactivateSubscriberForTATA(subscriberId);
							if (!cos.isDefaultCos())
								rbtDBManager.addSubscriberToDeactivatedSubscribersTable(
										subscriberId, "SMS", subscriber.activatedCosID());
						}
						//cos = null;
						cos = rbtDBManager.getSubscriberCos(subscriberId, circleID, new Character(
								 prepaidYes).toString(), strChannel, false);
						returnValue = "deactivated";
					}
				}
				else {
					if (subscriber == null) {
						WriteSDR.addToAccounting(smsLogPath, logRotationSize,
								"RBT_SUBSCRIBER_STATUS", subscriberId, "unknown",
								"subscriber_status", "error_response", requestedTimeString,
								differenceTime + "", "RBT_WAR", urlstr, result);
						cos = rbtDBManager.getSubscriberCos(subscriberId, circleID, String
								.valueOf(prepaidYes), strChannel, false);
						// quering WDS as we donn have the subscriber data and
						// querySubscriberStatus returned null
						/*if (m_queryWDS) {
							logger.info("RBT::subscriberStatus query resulted unusual response, quering WDS");
							prepaidYes = queryWDS(subscriberId);
							if (prepaidYes != 's')
								cos = rbtDBManager.getSubscriberCos(subscriberId, circleID,
										new Character(prepaidYes).toString(), "SMS");
						}
						else
							logger.info("RBT::subscriberStatus query resulted unusual response");*/
					}
					else {
						logger.info("RBT::unusual response, but we have the details continuing");
						if (subscriber.prepaidYes())
							prepaidYes = 'y';
						else
							prepaidYes = 'n';

						String userType = "";
						if (prepaidYes == 'y')
							userType = "PRE_PAID";
						else
							userType = "POST_PAID";

						cos = rbtDBManager.getSubscriberCos(subscriberId, circleID, new Character(
								 prepaidYes).toString(), strChannel, false);
						WriteSDR.addToAccounting(smsLogPath, logRotationSize,
								"RBT_SUBSCRIBER_STATUS", subscriberId, userType,
								"subscriber_status", "error_response", requestedTimeString,
								differenceTime + "", "RBT_WAR", urlstr, result);
					}
				}
			}
			else {
				if (subscriber == null) {
					WriteSDR.addToAccounting(smsLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS",
							subscriberId, "unknown", "subscriber_status", "null_error_response",
							requestedTimeString, differenceTime + "", "RBT_WAR", urlstr, result);
					cos = rbtDBManager.getSubscriberCos(subscriberId, circleID, String
							.valueOf(prepaidYes), strChannel, false);
					// quering WDS as we donn have the subscriber data and
					// querySubscriberStatus returned null
					/*if (m_queryWDS) {
						logger.info("RBT::subscriberStatus query resulted null, quering WDS");
						prepaidYes = queryWDS(subscriberId);
						if (prepaidYes != 's')
							cos = rbtDBManager.getSubscriberCos(subscriberId, circleID,
									new Character(prepaidYes).toString(), "SMS");
					}
					else
						logger.info("RBT::subscriberStatus query resulted null1");*/
				}
				else {
					logger.info("RBT::unexpected response, but we have the details continuing");
					if (subscriber.prepaidYes())
						prepaidYes = 'y';
					else
						prepaidYes = 'n';

					String userType = "";
					if (prepaidYes == 'y')
						userType = "PRE_PAID";
					else
						userType = "POST_PAID";

					WriteSDR.addToAccounting(smsLogPath, logRotationSize, "RBT_SUBSCRIBER_STATUS",
							subscriberId, userType, "subscriber_status", "null_error_response",
							requestedTimeString, differenceTime + "", "RBT_WAR", urlstr, result);
				}
			}
		}
		catch (Exception e) {
			logger.error("", e);
			Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId);
			if (subscriber == null) {
				logger.info("RBT::unexpected response, returning null");
				returnValue = null;
			}
			else {
				logger.info("RBT::unexpected response, but we have subscriberdetails, continuing");
				if (subscriber.prepaidYes())
					prepaidYes = 'y';
				else
					prepaidYes = 'n';
			}
		}

		HashMap map = new HashMap();
		map.put("RETURN_VALUE", returnValue);
		map.put("COS", cos);

		return map;
	}

    public HashMap queryWDS(String subscriberId)
    {
		char retVal = 's';
		String circleID = null;
		String rbtType = "0";
		try {
			String smsLogPath = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "QUERIED_INTERFACES_SMS_LOG_PATH", null);
			int logRotationSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "ROTATION_SIZE", 0);
			String wdsHttpQuery = wdsHTTPLink + "&mdn=" + subscriberId;

			RBTDBManager dbManager = RBTDBManager.getInstance();
			circleID = dbManager.getCircleId(subscriberId);
			RBTHTTPProcessing rbtHTTPProcessing = RBTHTTPProcessing.getInstance();

			Date requestedTimeStamp = new Date();
			String wdsResult = rbtHTTPProcessing.makeRequest1(wdsHttpQuery, subscriberId, "RBT_WAR");
			Date responseTimeStamp = new Date();

			long differenceTime = (responseTimeStamp.getTime() - requestedTimeStamp.getTime());

			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
			String requestedTimeString = formatter.format(requestedTimeStamp);

			if (wdsResult != null) {
				wdsResult = wdsResult.trim();
				StringTokenizer wdsST = new StringTokenizer(wdsResult, "|");
				if (wdsST.countTokens() >= 9) {
					String tempString = null;
					for (int tokenCount = 1; wdsST.hasMoreTokens(); tokenCount++) {
						tempString = wdsST.nextToken();
						if (tokenCount == 3) {
							if (tempString.startsWith("Pre")) {
								logger.info("RBT::User is prepaid as from WDS");
								retVal = 'y';
							}
							else {
								logger.info("RBT::User is postpaid as from WDS");
								retVal = 'n';
							}
						}
						else if (tokenCount == 5) {
							if (tempString.equals("1")) {
								logger.info("RBT::can allow subscriber as from WDS");
							}
							else {
								logger.info("RBT::cannot allow subscriber as from WDS");
								retVal = 's';
							}
						}
						else if(tokenCount == 8) {
							String allowedSubClass = dbManager.getAllowedSubscriberClass();
							if (allowedSubClass.indexOf(tempString) == -1) {
								logger.info("RBT::subscriber class from WDS->" + tempString
										+ ". Not allowing as allowed cases are " + allowedSubClass);
								retVal = 's';
							}
							if(tempString.equalsIgnoreCase("wl")) { 
                                rbtType = "10"; 
                                Subscriber subscriber = dbManager.getSubscriber(subscriberId); 
                                if(subscriber != null && subscriber.rbtType() != 10) 
                                        dbManager.updateRBTType(subscriberId, 10); 
							} 
						}
						else if(tokenCount == 9) {
							String circleIDTemp = tempString;
							if(circleIDTemp != null) {
								String mappedCircleID = dbManager.getMappedCircleID(circleIDTemp);
								if(!circleIDTemp.equalsIgnoreCase(mappedCircleID))
									logger.info("RBT:: mismatch in circle id's mappedCircleID->"
													+ mappedCircleID + ", m_circleId->" + circleIDTemp);
								circleID = mappedCircleID;
								if(circleID.equalsIgnoreCase("Default"))
									retVal = 's';
							}
							break;
						}
					}
					String userType = "unknown";
					if (retVal == 'y')
						userType = "PRE_PAID";
					else if(retVal == 'n')
						userType = "POST_PAID";
					WriteSDR.addToAccounting(smsLogPath, logRotationSize, "RBT_QUERY_WDS", subscriberId, userType,
							"query_wds", "success", requestedTimeString, differenceTime + "", "RBT_WAR", wdsHttpQuery,
							wdsResult);
				}
				else {
					WriteSDR.addToAccounting(smsLogPath, logRotationSize, "RBT_QUERY_WDS", subscriberId, "unknown",
							"query_wds", "error_response", requestedTimeString, differenceTime + "", "RBT_WAR",
							wdsHttpQuery, wdsResult);
					logger.info("RBT::didn't get proper response from WDS even!!");
					retVal = 's';
				}
			}
			else {
				WriteSDR.addToAccounting(smsLogPath, logRotationSize, "RBT_QUERY_WDS", subscriberId, "unknown",
						"query_wds", "null_error_response", requestedTimeString, differenceTime + "", "RBT_WAR",
						wdsHttpQuery, wdsResult);
				logger.info("RBT::didn't get response from WDS even!!");
				retVal = 's';
			}
		}
		catch (Exception e) {
			logger.error("", e);
			retVal = 's';
		}
		HashMap returnMap = new HashMap();
		returnMap.put("SUB_TYPE", String.valueOf(retVal));
		returnMap.put("CIRCLE_ID", circleID);
		returnMap.put("RBT_TYPE", rbtType);
		return returnMap;
	}

}