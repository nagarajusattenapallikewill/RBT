package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;

public class RBTUGCHelper
{
	private static Logger logger = Logger.getLogger(RBTUGCHelper.class);
	
    String m_countryPrefix = "0";
    String m_clipDIR = null;
    HashMap m_prefixCategoryMap = new HashMap();
    static RBTUGCHelper rbtUGCHelper = null;
    HashMap m_RegionMap = new HashMap();
    String UGC_REGION_CATEGORY_MAP = "UGC_REGION_CATEGORY_MAP";
    String m_classType = "DEFAULT";
    String m_filePath = null;
	Calendar ugcCal = Calendar.getInstance();
	Date ugcClipDate = null;

    RBTDBManager rbtDBmanager = null;
	RBTMOHelper rbtMO = null;

    String FAILURE = "FAILURE";
    String MISSING_PARAMETERS = "MISSING_PARAMETERS";
    String ERROR = "TECHNICAL_DIFFICULTY_ERROR";

    private static final String PARAMETER_TYPE_GATHERER = "GATHERER";

    public static RBTUGCHelper init()
    {
        try
        {
            if (rbtUGCHelper == null)
            {
                rbtUGCHelper = new RBTUGCHelper();
            }
        }
        catch (Exception e)
        {
            rbtUGCHelper = null;
            logger.error("", e);
        }
        return rbtUGCHelper;
    }

    public RBTUGCHelper() throws Exception
    {
        Tools.init("RBT_WAR", false);

        m_countryPrefix = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "COUNTRY_PREFIX", "91");

        rbtDBmanager = RBTDBManager.getInstance();
        rbtMO = RBTMOHelper.init(); 
        ugcCal.add(Calendar.YEAR, 50);
        ugcClipDate = ugcCal.getTime();
        initializePrefixes();

        m_classType = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "UGC_CLASSTYPE", "DEFAULT");
        m_filePath = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
    }

    public String addUGC(String strSubID, String strFile)
    {
        try
        {
            //File ugcFile = new File(m_filePath + File.separator + strFile);
            if (strSubID == null || strFile == null)
                return MISSING_PARAMETERS;
            if (m_filePath == null)
                return ERROR;
            File ugcFile = new File(m_filePath + File.separator + strFile);
            if (!ugcFile.exists() || ugcFile.length() <= 0)
            {
                logger.info("RBT::file missing or file size is zero "
                                        + strFile);
                return ERROR;
            }

            //String regionName = getRegionName(strSubID);
            String categoryID = getCategoryId(strSubID);
            if (categoryID == null)
                return ERROR;
            int length = strFile.length();
            strFile = strFile.substring(0, length - 4);
            Clips ugcClip = rbtDBmanager.addUGC(strSubID, categoryID,
                                                m_classType, strFile);

			if (ugcClip == null)
                return ERROR;
            rbtDBmanager.fillCategoryClipMaps(categoryID, ugcClip.id(), "y", 0,
                                              null);
            /*ClipMinimal clipMinimal = new ClipMinimal(ugcClip);
			clipMinimal.setEndTime(ugcClipDate);
			clipMinimal.setSmsTime(Calendar.getInstance().getTime());
            if (ugcClip.promoID() != null)
                    rbtMO.m_clips.put(ugcClip.promoID().toLowerCase(), clipMinimal); */

			/*
             * String fileName = ugcClip.wavFile() + ".wav"; StringTokenizer
             * tokens = new StringTokenizer(m_clipDIR, ","); while
             * (tokens.hasMoreTokens()) { String directory = tokens.nextToken();
             * Tools.logDetail(_class, "addUGC", "RBT::copying " +
             * ugcFile.getName() + " to " + directory + " as " + fileName);
             * Tools.moveFile(directory, ugcFile); new File(directory +
             * File.separator + ugcFile.getName()) .renameTo(new File(directory +
             * File.separator + fileName)); } ugcFile.delete();
             * Tools.logDetail(_class, "updateFile", "RBT::deleting " +
             * ugcFile.getName());
             */
            return ugcClip.promoID();
        }
        catch (Exception e)
        {
            logger.error("RBT::Exception caught " + e.getMessage(), e);
            return ERROR;
        }

    }

    public void initializePrefixes()
    {
        ArrayList aList = new ArrayList();
        Parameters gp = CacheManagerUtil.getParametersCacheManager().getParameter(PARAMETER_TYPE_GATHERER,
                                                  UGC_REGION_CATEGORY_MAP);
        if (gp != null && gp.getValue() != null)
        {
            StringTokenizer stParent = new StringTokenizer(gp.getValue(), ";");
            while (stParent.hasMoreTokens())
            {
                StringTokenizer stChild = new StringTokenizer(stParent
                        .nextToken(), ",");
                String siteName = null;
                String categoryId = null;
                if (stChild.hasMoreTokens())
                    siteName = stChild.nextToken().trim().toUpperCase();
                if (stChild.hasMoreTokens())
                    categoryId = stChild.nextToken().trim();
                if (siteName != null && categoryId != null)
                    m_RegionMap.put(siteName, categoryId);
                if (siteName != null)
                    aList.add(siteName);
            }
        }

        List<SitePrefix> prefix = CacheManagerUtil.getSitePrefixCacheManager().getLocalSitePrefixes();
        if (prefix != null && prefix.size() > 0)
        {
            for (int i = 0; i < prefix.size(); i++)
            {
                String name = prefix.get(i).getSiteName();
                if (name == null)
                    continue;
                else
                    name = name.toUpperCase();
                name = name.trim();
                if (!aList.contains(name))
                    continue;
                StringTokenizer stk = new StringTokenizer(prefix.get(i).getSitePrefix(),
                        ",");
                while (stk.hasMoreTokens())
                {
                    //m_prefixMap.put(stk.nextToken(), prefix[i].name());
                    m_prefixCategoryMap.put(stk.nextToken(),
                                            (String) m_RegionMap.get(name));
                }
            }
        }
    }

    private String getCategoryId(String strSubID)
    {
        if (strSubID == null || m_prefixCategoryMap == null
                || m_prefixCategoryMap.size() <= 0)
            return null;
        int prefixIndex = RBTDBManager.getInstance().getPrefixIndex(); 
        String strSubPrefix = strSubID.substring(0, prefixIndex); 
        if (m_prefixCategoryMap.containsKey(strSubPrefix))
            return (String) m_prefixCategoryMap.get(strSubPrefix);
        return null;
    }

    public boolean expireUGCClipsForPromoIDs(String promoIDsList)
    {
        return rbtDBmanager.expireUGCClipsForPromoIDs(promoIDsList);
    }

    public boolean expireUGCClipsOfCreator(String subID)
    {
        return rbtDBmanager.expireUGCClipsOfCreator(subID);
    }

}