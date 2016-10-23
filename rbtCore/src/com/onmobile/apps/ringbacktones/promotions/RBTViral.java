package com.onmobile.apps.ringbacktones.promotions;

import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.LineNumberReader;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.FeedStatus;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;

public class RBTViral extends Thread
{
	private static Logger logger = Logger.getLogger(RBTViral.class);
	
    private static final String m_class = "RBTViral";
    private static final String PREFIX_IN_CIRCLE = "CIRCLE";
    private static final String PREFIX_OUT_CIRCLE = "OPERATOR";
    private static final String PREFIX_MOBILE = "MOBILE";

    private static final String m_cdrDirDefault = null;
    private static String m_cdrDir = m_cdrDirDefault;
    private static final String m_telServersDefault = null;
    private static String m_telServers = m_telServersDefault;
	private static String m_lastReadtelServer = null;
    private static final String m_circlePrefixesDefault = null;
    private static String m_circlePrefixes = m_circlePrefixesDefault;
    private static final int m_sleepIntervalDefault = 5;
    private static int m_sleepInterval = m_sleepIntervalDefault;
    private static final int m_nonSubClearIntervalDefault = 1;
    private static int m_nonSubClearInterval = m_nonSubClearIntervalDefault;
    private static final int m_nonCricSubClearIntervalDefault = 1;
    private static int m_nonCricSubClearInterval = m_nonCricSubClearIntervalDefault;
    private static final boolean m_nonSubSendSMSDefault = false;
    private static boolean m_nonSubSendSMS = m_nonSubSendSMSDefault;
    private static final boolean m_deactSubSendSMSDefault = false;
    private static boolean m_deactSubSendSMS = m_deactSubSendSMSDefault;
    private static final String m_basicViralNoDefault = null;
    private static String m_basicViralNo = m_basicViralNoDefault;
    private static final String m_cricViralNoDefault = null;
    private static String m_cricViralNo = m_cricViralNoDefault;
    private static final boolean m_basicViralOnDefault = false;
    private static boolean m_basicViralOn = m_basicViralOnDefault;
    private static final boolean m_cricViralOnDefault = false;
    private static boolean m_cricViralOn = m_cricViralOnDefault;
    private static final boolean m_testOnDefault = false;
    private static boolean m_testOn = m_testOnDefault;
    private static final boolean m_sendBasicToActiveDefault = false;
    private static boolean m_sendBasicToActive = m_sendBasicToActiveDefault;

    private static String m_calledPrefix = null;

	private static long m_startTime;
	File m_file = null;
    public ArrayList aThreads = null;
    private static final String m_blackOutPeriodDefault = null;
    private static String m_blackOutPeriod = m_blackOutPeriodDefault;
    //	private static final String m_vpDbURLDefault = null;
    //	private static String m_vpDbURL = m_vpDbURLDefault;
    private static final String m_rbtDbURLDefault = null;
    private static final String m_basicViralSMSDefault = "You have just heard a RingbackTone";
    private static String m_basicViralSMS = m_basicViralSMSDefault;
    private static final String m_cricViralSMSDefault = "You have just heard a Cricket RingbackTone";
    private static String m_cricViralSMS = m_cricViralSMSDefault;
    
    private static final String PARAMETER_TYPE = "VIRAL";

    private boolean m_Continue = true;

    private static final String _COMPONENT_NAME = "RBT_VIRAL";

    private static Hashtable m_weekDayTable;
    private Hashtable m_blackOutPeriodTable;

    private static List m_prefixList;
    private List m_telServersList;

    private long m_size = 0;

    private static long m_linesToRead = 1000;
    private long m_cdrLineSize = 128;

    public static int m_numThreads = 5;
    public static Parse[] parse = null;

    private int m_minDurDefault = 15;
    private static int m_minDur;

    private int m_processMaxSecsDefault = 0;
    private static int m_processMaxSecs;

	String m_fileName = null;

    public Vector m_vector;

    public RBTViral()
    {
    }

    public void init(Vector v)
    {
        m_vector = v;

        Tools.init(m_class, true);

        m_weekDayTable = new Hashtable();

        m_weekDayTable.put("SUN", new Integer("1"));
        m_weekDayTable.put("MON", new Integer("2"));
        m_weekDayTable.put("TUE", new Integer("3"));
        m_weekDayTable.put("WED", new Integer("4"));
        m_weekDayTable.put("THU", new Integer("5"));
        m_weekDayTable.put("FRI", new Integer("6"));
        m_weekDayTable.put("SAT", new Integer("7"));
    }

    private synchronized void init() throws Exception
    {
        try
        {
            RBTDBManager rbtDBManager = RBTDBManager.getInstance();
            if (rbtDBManager == null)
                throw new Exception("RBTViral::RBT DB Manager null");

            List<Parameters> parameters = CacheManagerUtil.getParametersCacheManager().getParameters(PARAMETER_TYPE);
            if (parameters == null)
                throw new Exception(
                        "RBTViral::no values in RBT_VIRAL_PARAMETERS table");

            HashMap m_viralParametersMap = new HashMap();
            for (int i = 0; i < parameters.size(); i++)
            {
                m_viralParametersMap.put(parameters.get(i).getParam(), parameters.get(i).getValue());
            }

            try
            {
                if (m_viralParametersMap.containsKey("CDR_DIR"))
                {
                    m_cdrDir = (String) m_viralParametersMap.get("CDR_DIR");
                    if (m_cdrDir == null)
                        throw new Exception(
                                "RBTViral::CDR directory value is null");
                }
                else
                {
                    throw new Exception(
                            "RBTViral::CDR_DIR not available in RBT_VIRAL_PARAMETERS table");
                }

            }
            catch (Exception e)
            {
                throw new Exception(
                        "RBTViral::CDR_DIR not available in RBT_VIRAL_PARAMETERS table");
            }
            logger.info("RBTViral::CDR directory "
                    + m_cdrDir);

            try
            {
                if (m_viralParametersMap.containsKey("TELEPHONY_SERVERS"))
                {
                    m_telServers = (String) m_viralParametersMap
                            .get("TELEPHONY_SERVERS");
                    if (m_telServers == null)
                        throw new Exception(
                                "RBTViral::Telephony servers value is null");
                }
                else
                {
                    throw new Exception(
                            "RBTViral::TELEPHONY_SERVERS not available in RBT_VIRAL_PARAMETERS table");
                }

            }
            catch (Exception e)
            {
                throw new Exception(
                        "RBTViral::TELEPHONY_SERVERS not available in RBT_VIRAL_PARAMETERS table");
            }
            logger.info("RBTViral::Telephony servers "
                    + m_telServers);

            try
            {
                if (m_viralParametersMap.containsKey("CIRCLE_PREFIXES"))
                {
                    m_circlePrefixes = (String) m_viralParametersMap
                            .get("CIRCLE_PREFIXES");
                    if (m_circlePrefixes == null)
                        throw new Exception(
                                "RBTViral::Circle prefixes value is null");
                }
                else
                {
                    throw new Exception(
                            "RBTViral::CIRCLE_PREFIXES not available in RBT_VIRAL_PARAMETERS table");
                }

            }
            catch (Exception e)
            {
                throw new Exception(
                        "RBTViral::CIRCLE_PREFIXES not available in RBT_VIRAL_PARAMETERS table");
            }
            logger.info("RBTViral::Circle prefixes "
                    + m_circlePrefixes);

            try
            {
                if (m_viralParametersMap.containsKey("SLEEP_INTERVAL"))
                {
                    String sleepInterval = (String) m_viralParametersMap
                            .get("SLEEP_INTERVAL");
                    m_sleepInterval = Integer.parseInt(sleepInterval);
                }
                else
                {
                    m_sleepInterval = m_sleepIntervalDefault;
                }
            }
            catch (Exception e)
            {
                m_sleepInterval = m_sleepIntervalDefault;
            }
            logger.info("RBTViral::Sleep interval "
                    + m_sleepInterval);

            try
            {
                if (m_viralParametersMap
                        .containsKey("NON_SUBSCRIBER_CLEAR_INTERVAL"))
                {
                    String sleepInterval = (String) m_viralParametersMap
                            .get("NON_SUBSCRIBER_CLEAR_INTERVAL");
                    m_nonSubClearInterval = Integer.parseInt(sleepInterval);
                }
                else
                {
                    m_nonSubClearInterval = m_nonSubClearIntervalDefault;
                }
            }
            catch (Exception e)
            {
                m_nonSubClearInterval = m_nonSubClearIntervalDefault;
            }
            logger.info("RBTViral::Non subscriber clear interval "
                                    + m_nonSubClearInterval);

            try
            {
                if (m_viralParametersMap
                        .containsKey("NON_CRICKET_SUBSCRIBER_CLEAR_INTERVAL"))
                {
                    String sleepInterval = (String) m_viralParametersMap
                            .get("NON_CRICKET_SUBSCRIBER_CLEAR_INTERVAL");
                    m_nonCricSubClearInterval = Integer.parseInt(sleepInterval);
                }
                else
                {
                    m_nonCricSubClearInterval = m_nonCricSubClearIntervalDefault;
                }
            }
            catch (Exception e)
            {
                m_nonCricSubClearInterval = m_nonCricSubClearIntervalDefault;
            }
            logger.info("RBTViral::Non cricket subscriber clear interval "
                                    + m_nonCricSubClearInterval);

            try
            {
                if (m_viralParametersMap.containsKey("NON_SUBSCRIBER_SEND_SMS"))
                {
                    String sendSMS = (String) m_viralParametersMap
                            .get("NON_SUBSCRIBER_SEND_SMS");
                    m_nonSubSendSMS = sendSMS.equalsIgnoreCase("TRUE");
                }
                else
                {
                    m_nonSubSendSMS = m_nonSubSendSMSDefault;
                }
            }
            catch (Exception e)
            {
                m_nonSubSendSMS = m_nonSubSendSMSDefault;
            }
            logger.info("RBTViral::Non subscriber send sms "
                                    + m_nonSubSendSMS);

            try
            {
                if (m_viralParametersMap
                        .containsKey("DEACT_SUBSCRIBER_SEND_SMS"))
                {
                    String sendSMS = (String) m_viralParametersMap
                            .get("DEACT_SUBSCRIBER_SEND_SMS");
                    m_deactSubSendSMS = sendSMS.equalsIgnoreCase("TRUE");
                }
                else
                {
                    m_deactSubSendSMS = m_deactSubSendSMSDefault;
                }
            }
            catch (Exception e)
            {
                m_deactSubSendSMS = m_deactSubSendSMSDefault;
            }
            logger.info("RBTViral::Deactive subscriber send sms "
                                    + m_deactSubSendSMS);

            try
            {
                if (m_viralParametersMap.containsKey("BASIC_VIRAL_SENDER_NO"))
                {
                    m_basicViralNo = (String) m_viralParametersMap
                            .get("BASIC_VIRAL_SENDER_NO");
                    if (m_basicViralNo == null)
                        throw new Exception(
                                "RBTViral::Basic viral sender number is null");
                }
                else
                {
                    throw new Exception(
                            "RBTViral::BASIC_VIRAL_SENDER_NO not available in RBT_VIRAL_PARAMETERS table");
                }

            }
            catch (Exception e)
            {
                throw new Exception(
                        "RBTViral::BASIC_VIRAL_SENDER_NO not available in RBT_VIRAL_PARAMETERS table");
            }
            logger.info("RBTViral::Basic viral sender number "
                                    + m_basicViralNo);

            try
            {
                if (m_viralParametersMap.containsKey("CRICKET_VIRAL_SENDER_NO"))
                {
                    m_cricViralNo = (String) m_viralParametersMap
                            .get("CRICKET_VIRAL_SENDER_NO");
                    if (m_cricViralNo == null)
                        throw new Exception(
                                "RBTViral::Cricket viral sender number is null");
                }
                else
                {
                    throw new Exception(
                            "RBTViral::CRICKET_VIRAL_SENDER_NO not available in RBT_VIRAL_PARAMETERS table");
                }

            }
            catch (Exception e)
            {
                throw new Exception(
                        "RBTViral::CRICKET_VIRAL_SENDER_NO not available in RBT_VIRAL_PARAMETERS table");
            }
            logger.info("RBTViral::Cricket viral sender number "
                                    + m_cricViralNo);

            try
            {
                if (m_viralParametersMap.containsKey("CALLED_PREFIX"))
                {
                    m_calledPrefix = (String) m_viralParametersMap
                            .get("CALLED_PREFIX");
                }

            }
            catch (Exception e)
            {
            }
            logger.info("RBTViral::Called Party Prefix "
                                    + m_calledPrefix);


            try
            {
                if (m_viralParametersMap.containsKey("BASIC_VIRAL_ON"))
                {
                    String viralOn = (String) m_viralParametersMap
                            .get("BASIC_VIRAL_ON");
                    m_basicViralOn = viralOn.equalsIgnoreCase("TRUE");
                }
                else
                {
                    m_basicViralOn = m_basicViralOnDefault;
                }
            }
            catch (Exception e)
            {
                m_basicViralOn = m_basicViralOnDefault;
            }
            logger.info("RBTViral::Basic viral on "
                    + m_basicViralOn);

            try
            {
                if (m_viralParametersMap.containsKey("CRICKET_VIRAL_ON"))
                {
                    String viralOn = (String) m_viralParametersMap
                            .get("CRICKET_VIRAL_ON");
                    m_cricViralOn = viralOn.equalsIgnoreCase("TRUE");
                }
                else
                {
                    m_cricViralOn = m_cricViralOnDefault;
                }
            }
            catch (Exception e)
            {
                m_cricViralOn = m_cricViralOnDefault;
            }
            logger.info("RBTViral::Cricket viral on "
                    + m_cricViralOn);

            try
            {
                if (m_viralParametersMap.containsKey("TESTING_ON"))
                {
                    String viralOn = (String) m_viralParametersMap
                            .get("TESTING_ON");
                    m_testOn = viralOn.equalsIgnoreCase("TRUE");
                }
                else
                {
                    m_testOn = m_testOnDefault;
                }
            }
            catch (Exception e)
            {
                m_testOn = m_testOnDefault;
            }
            logger.info("RBTViral::Testing on "
                            + m_testOn);

            try
            {
                if (m_viralParametersMap.containsKey("SEND_BASIC_TO_ACTIVE"))
                {
                    String sendBasicToActive = (String) m_viralParametersMap
                            .get("SEND_BASIC_TO_ACTIVE");
                    m_sendBasicToActive = sendBasicToActive
                            .equalsIgnoreCase("TRUE");
                }
                else
                {
                    m_sendBasicToActive = m_sendBasicToActiveDefault;
                }
            }
            catch (Exception e)
            {
                m_sendBasicToActive = m_sendBasicToActiveDefault;
            }
            logger.info("RBTViral::Send basic to active "
                    + m_sendBasicToActive);

            try
            {
                if (m_viralParametersMap.containsKey("BLACK_OUT_PERIOD"))
                {
                    m_blackOutPeriod = (String) m_viralParametersMap
                            .get("BLACK_OUT_PERIOD");
                }
                else
                {
                    m_blackOutPeriod = m_blackOutPeriodDefault;
                }

            }
            catch (Exception e)
            {
                m_blackOutPeriod = m_blackOutPeriodDefault;
            }
            logger.info("RBTViral::Blackout period "
                    + m_blackOutPeriod);

            try
            {
                if (m_viralParametersMap.containsKey("BASIC_VIRAL_SMS"))
                {
                    m_basicViralSMS = (String) m_viralParametersMap
                            .get("BASIC_VIRAL_SMS");
                }
                else
                {
                    m_basicViralSMS = m_basicViralSMSDefault;
                }

            }
            catch (Exception e)
            {
                m_basicViralSMS = m_basicViralSMSDefault;
            }
            logger.info("RBTViral::Basic viral sms text "
                    + m_basicViralSMS);

            try
            {
                if (m_viralParametersMap.containsKey("CRICKET_VIRAL_SMS"))
                {
                    m_cricViralSMS = (String) m_viralParametersMap
                            .get("CRICKET_VIRAL_SMS");
                }
                else
                {
                    m_cricViralSMS = m_cricViralSMSDefault;
                }

            }
            catch (Exception e)
            {
                m_cricViralSMS = m_cricViralSMSDefault;
            }
            logger.info("RBTViral::Cricket viral sms text "
                                    + m_cricViralSMS);

            try
            {
                if (m_viralParametersMap.containsKey("MIN_DURATION"))
                {
                    String dur = (String) m_viralParametersMap
                            .get("MIN_DURATION");
                    m_minDur = Integer.parseInt(dur);
                }
                else
                {
                    m_minDur = m_minDurDefault;
                }

            }
            catch (Exception e)
            {
                m_minDur = m_minDurDefault;
            }
            logger.info("RBTViral::Min duration "
                    + m_minDur);

            try
            {
                if (m_viralParametersMap.containsKey("MAX_SECS"))
                {
                    String dur = (String) m_viralParametersMap
                            .get("MAX_SECS");
                    m_processMaxSecs = Integer.parseInt(dur+"000");
                }
                else
                {
                    m_processMaxSecs = m_processMaxSecsDefault;
                }

            }
            catch (Exception e)
            {
                m_processMaxSecs = m_processMaxSecsDefault;
            }
            logger.info("RBTViral::Process Max Secs "
                    + m_processMaxSecs);


			
			try
            {
                if (m_viralParametersMap.containsKey("LINES_TO_READ"))
                {
                    String lines = (String) m_viralParametersMap
                            .get("LINES_TO_READ");
                    m_linesToRead = Integer.parseInt(lines);
                }
                else
                {
                    m_linesToRead = 1000;
                }

            }
            catch (Exception e)
            {
                m_linesToRead = 1000;
            }
            logger.info("RBTViral::Lines to read "
                    + m_linesToRead);
        }
        catch (Exception e)
        {
            throw e;
        }

        if (m_telServers != null)
        {
            m_telServersList = new ArrayList();

            StringTokenizer st = new StringTokenizer(m_telServers, ",");
            while (st.hasMoreTokens())
            {
                m_telServersList.add(st.nextToken());
            }
        }

        if (m_circlePrefixes != null)
        {
            m_prefixList = new ArrayList();
            StringTokenizer st = new StringTokenizer(m_circlePrefixes, ",");
            while (st.hasMoreTokens())
            {
                m_prefixList.add(st.nextToken());
            }
        }

        if (m_blackOutPeriod != null)
        {
            try
            {
                parseBlackOutPeriod();
            }
            catch (Exception e)
            {
                m_blackOutPeriod = null;
            }
        }
    }

    private void parseBlackOutPeriod() throws Exception
    {
        try
        {
            if (m_blackOutPeriod != null)
            {
                m_blackOutPeriodTable = new Hashtable();
                StringTokenizer st = new StringTokenizer(m_blackOutPeriod, ",");
                while (st.hasMoreTokens())
                {
                    String value = st.nextToken().toUpperCase();
                    if (m_weekDayTable.containsKey(value.substring(0, 3)))
                    {
                        int day1 = ((Integer) m_weekDayTable.get(value
                                .substring(0, 3))).intValue();
                        String startDay = value.substring(0, 3);
                        int day2 = -1;
                        String temp = value.substring(0, value.indexOf("["));
                        String hours = value.substring(value.indexOf("["));
                        if (temp.lastIndexOf("-") != -1)
                        {
                            if (m_weekDayTable.containsKey(temp.substring(temp
                                    .lastIndexOf("-") + 1)))
                            {
                                day2 = ((Integer) m_weekDayTable.get(temp
                                        .substring(temp.lastIndexOf("-") + 1)))
                                        .intValue();
                            }
                        }
                        StringTokenizer tokens = new StringTokenizer(hours, ";");
                        while (tokens.hasMoreTokens())
                        {
                            String hour = tokens.nextToken();
                            int startHour = -1;
                            int endHour = -1;

                            if (hour.lastIndexOf("-") != -1)
                            {
                                if (hour.lastIndexOf("[") != -1)
                                    startHour = Integer
                                            .parseInt(hour.substring(1, hour
                                                    .lastIndexOf("-")));
                                else
                                    startHour = Integer
                                            .parseInt(hour.substring(0, hour
                                                    .lastIndexOf("-")));

                                if (hour.lastIndexOf("]") != -1)
                                    endHour = Integer
                                            .parseInt(hour.substring(hour
                                                    .lastIndexOf("-") + 1, hour
                                                    .lastIndexOf("]")));
                                else
                                    endHour = Integer
                                            .parseInt(hour.substring(hour
                                                    .lastIndexOf("-") + 1));
                            }
                            else
                            {
                                int index = hour.lastIndexOf("]");
                                if (index == -1)
                                    index = hour.lastIndexOf("[");
                                if (index != -1)
                                {
                                    if (hour.lastIndexOf("[") != -1)
                                        startHour = Integer.parseInt(hour
                                                .substring(1));
                                    else
                                        startHour = Integer.parseInt(hour
                                                .substring(0, index));
                                }
                                else
                                {
                                    startHour = Integer.parseInt(hour);
                                }
                            }

                            String period = "";
                            if (endHour == -1)
                                period = period + startHour;
                            else if (endHour > startHour)
                            {
                                for (int i = startHour; i <= endHour; i++)
                                {
                                    if (!period.equals(""))
                                        period = period + "," + i;
                                    else
                                        period = period + i;
                                }
                            }
                            else
                            {
                                for (int i = 0; i <= endHour; i++)
                                {
                                    if (!period.equals(""))
                                        period = period + "," + i;
                                    else
                                        period = period + i;
                                }

                                for (int i = startHour; i <= 23; i++)
                                {
                                    if (!period.equals(""))
                                        period = period + "," + i;
                                    else
                                        period = period + i;
                                }
                            }

                            Iterator it = (Iterator) m_weekDayTable.keys();
                            ArrayList days = new ArrayList();
                            days.add("HI");
                            while (it.hasNext())
                                days.add(it.next());

                            if (day2 == -1)
                            {
                                String tmp = "";
                                if (m_blackOutPeriodTable.get(startDay) != null)
                                    tmp = (String) m_blackOutPeriodTable
                                            .get(startDay);

                                if (tmp.equals(""))
                                    tmp = period;
                                else
                                    tmp = tmp + "," + period;

                                m_blackOutPeriodTable.put(startDay, tmp);
                            }
                            else if (day2 > day1)
                            {
                                for (int i = day1; i <= day2; i++)
                                {
                                    if (m_weekDayTable
                                            .containsValue(new Integer(i)))
                                    {
                                        String tmp = "";
                                        if (m_blackOutPeriodTable.get(days
                                                .get(i)) != null)
                                            tmp = (String) m_blackOutPeriodTable
                                                    .get(days.get(i));

                                        if (tmp.equals(""))
                                            tmp = period;
                                        else
                                            tmp = tmp + "," + period;

                                        m_blackOutPeriodTable.put(days.get(i),
                                                                  tmp);
                                    }
                                }
                            }
                            else
                            {
                                for (int i = 1; i <= day2; i++)
                                {
                                    if (m_weekDayTable
                                            .containsValue(new Integer(i)))
                                    {
                                        String tmp = "";
                                        if (m_blackOutPeriodTable.get(days
                                                .get(i)) != null)
                                            tmp = (String) m_blackOutPeriodTable
                                                    .get(days.get(i));

                                        if (tmp.equals(""))
                                            tmp = period;
                                        else
                                            tmp = tmp + "," + period;

                                        m_blackOutPeriodTable.put(days.get(i),
                                                                  tmp);
                                    }
                                }

                                for (int i = day1; i <= 7; i++)
                                {
                                    if (m_weekDayTable
                                            .containsValue(new Integer(i)))
                                    {
                                        String tmp = "";

                                        if (m_blackOutPeriodTable.get(days
                                                .get(i)) != null)
                                            tmp = (String) m_blackOutPeriodTable
                                                    .get(days.get(i));

                                        if (tmp.equals(""))
                                            tmp = period;
                                        else
                                            tmp = tmp + "," + period;

                                        m_blackOutPeriodTable.put(days.get(i),
                                                                  tmp);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
            throw new Exception("RBTViral::Exception in parseBlackOutPeriod "
                    + e.getMessage());
        }
    }
    
    public void stopThread()
	{
    	for (int i = 0; parse != null && i < m_numThreads; i++)
        {
            parse[i].stopThread();
        }
    	
		m_Continue = false;
	}

	public void run()
	{
		parse = new Parse[m_numThreads];
		for (int i = 0; i < m_numThreads; i++)
        {
            Parse t = new Parse(m_vector);
            parse[i] = t;
            t.start();
        }
		
        boolean isBlackOutPeriod = false;

        while (m_Continue)
        {
            m_startTime = System.currentTimeMillis();
			try
            {
                init();
            }
            catch (Exception e)
            {
                logger.error("", e);
                return;
            }

            //check blackout period
            isBlackOutPeriod = false;
            if (m_blackOutPeriodTable != null
                    && m_blackOutPeriodTable.size() != 0)
            {
                Calendar calendar = Calendar.getInstance();
                String[] days = { "SUN", "MON", "TUE", "WED", "THU", "FRI",
                        "SAT" };
                int day = calendar.get(Calendar.DAY_OF_WEEK) - 1;
                String hour = new Integer(calendar.get(Calendar.HOUR_OF_DAY))
                        .toString();
                String blackOutHour = (String) m_blackOutPeriodTable
                        .get(days[day]);
                logger.info("RBTViral::blackOutHour "
                        + blackOutHour + " hour " + hour);
                if (blackOutHour != null && hour != null)
                {
                    StringTokenizer st = new StringTokenizer(blackOutHour, ",");
                    while (st.hasMoreTokens())
                    {
                        String temp = st.nextToken().trim();
                        if (temp.equalsIgnoreCase(hour))
                        {
                            isBlackOutPeriod = true;
                            break;
                        }
                    }
                }
            }

            logger.info("RBTViral::isBlackOutPeriod "
                    + isBlackOutPeriod);
            //if not blackout period
            if (!isBlackOutPeriod)
            {
                if (m_cricViralOn || m_basicViralOn)
                {
                    FeedStatus feedStatus = RBTDBManager
                            .getInstance()
                            .getFeedStatus("CRICKET");
                    if (feedStatus != null
                            && feedStatus.status().equalsIgnoreCase("OFF"))
                    {
                        m_cricViralOn = false;
                        logger.info("RBTViral::Feed status is off");
                    }

                    logger.info("RBTViral::CDR collection started");
                    collectCDRFiles();

                }
            }
            Date nextRunTime = roundToNearestInterVal(m_sleepInterval);
            long sleeptime = getSleepTime(nextRunTime);
            logger.info("RBTViral::Sleeping till "
                    + nextRunTime + "!!!!");
            try
            {
                Thread.sleep(sleeptime);
            }
            catch (Exception e)
            {
                logger.error("", e);
            }
        }
    }
	
    private Date roundToNearestInterVal(int interval)
    {
        Calendar calendar = Calendar.getInstance();
        int n = 60 / interval;
        for (int i = 1; i <= n; i++)
        {
            if (calendar.get(Calendar.MINUTE) < (interval * (i))
                    && calendar.get(Calendar.MINUTE) >= (interval * (i - 1)))
            {
                calendar.set(Calendar.SECOND, 0);
                if (i < n)
                    calendar.set(Calendar.MINUTE, (interval * (i)));
                else
                {
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.add(Calendar.HOUR_OF_DAY, 1);
                }
                break;
            }
        }
        return calendar.getTime();
    }

    private long getSleepTime(Date date)
    {
        Calendar calendar = Calendar.getInstance();
        Date currentTime = calendar.getTime();
        return (date.getTime() - currentTime.getTime());
    }

    private void collectCDRFiles()
    {
        Iterator it = m_telServersList.iterator();
        File latestFile = null;
        long lastSize = 0;
		String first = null;

        while (it.hasNext())
        {

            String ip = (String) it.next();
			if(first == null)
				first = ip;

			if(m_lastReadtelServer == null)
			{
				m_lastReadtelServer = ip;
				break;
			}
			else if(m_lastReadtelServer.equals(ip))
			{
				m_lastReadtelServer = null;
				continue;
			}
        }

		if(m_lastReadtelServer == null)
			m_lastReadtelServer = first;

			String cdrLocation = locateCDRFiles(m_lastReadtelServer);
            File cdrdir = new File(cdrLocation);
            if (!cdrdir.isDirectory())
            {
                logger.info("RBTViral::Could not locate CDR directory "
                                         + cdrLocation);
            }
            else
            {
                File file = getLatestFile(cdrdir);
                if (file != null)
                {
                    if (latestFile == null)
                        latestFile = file;
                    else
                    {
                        if (file.lastModified() > latestFile.lastModified())
                        {
                            latestFile = file;
                        }
                    }
                }
            }

		if (latestFile == null)
        {
            logger.info("RBTViral::No CDRs present!!!");
            return;
        }

        if (m_fileName != null)
        {
            File lastFile = new File(m_fileName);
            if (latestFile.equals(lastFile) && latestFile.length() <= m_size)
            {
                logger.info("RBTViral::No Updations in CDR files. Hence no processing!!!");
                return;
            }
            else if (latestFile.equals(lastFile))
            {
                m_fileName = latestFile.getAbsolutePath();
                lastSize = m_size;
            }
            else
            {
                m_fileName = latestFile.getAbsolutePath();
                lastSize = 0;
            }
        }
        else
            m_fileName = latestFile.getAbsolutePath();

        processCDRFile(lastSize);

        return;
    }

    private String subID(String subscriberID)
    {
        return (RBTDBManager.getInstance().subID(subscriberID));
    }

    private void processCDRFile(long lastRead)
    {
        logger.info("RBTViral::CDR file being processed is " + m_fileName + " lastRead "+lastRead);
        try
        {
            RandomAccessFile accessFile = new RandomAccessFile(m_fileName, "r");
            long cdrSize = m_size = accessFile.length();
			ArrayList subs = new ArrayList();
            System.out.println("Initial CDR File Size " + cdrSize);

            long lines = (cdrSize - lastRead) / m_cdrLineSize;
            if (lines <= m_linesToRead)
            {
                accessFile.seek(lastRead);
            }
            else
            {
                accessFile.seek(cdrSize - (m_linesToRead * m_cdrLineSize));
            }

            String str = null;
            int linesRead = 0;
            while (linesRead < m_linesToRead
                    && (str = accessFile.readLine()) != null)
            {
                if (!str.startsWith("10,"))
                    continue;
				String caller = null;

				StringTokenizer stk = new StringTokenizer(str,",");
				if(stk.hasMoreTokens())stk.nextToken();
				if(stk.hasMoreTokens())stk.nextToken();
				if(stk.hasMoreTokens())stk.nextToken();
				if(stk.hasMoreTokens())caller = stk.nextToken();

				if(caller == null || caller.length() < 7)
					continue;
                try
                {
                    synchronized (m_vector)
                    {
                        if(!subs.contains(subID(caller)))
						{
							m_vector.add(str);
	                        m_vector.notify();
							subs.add(subID(caller));
						}
                    }
                }
                catch (Exception e)
                {
                    System.out.println("Exception while notify " + e);
                }

                long size = accessFile.length();
                long diff = cdrSize - size;

                if (diff > m_cdrLineSize)
                {
                    cdrSize = m_size = size;
                    lastRead = lastRead + diff;
					logger.info("RBTViral::Seeking "+ lastRead +" in file " + m_fileName);
                    accessFile.seek(lastRead);
                }

                linesRead++;
            }

            accessFile.close();
            logger.info("RBTViral::Finished processing file " + m_fileName);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
    }

    private String locateCDRFiles(String serverIP)
    {
        char DOLLAR = '$';
        char COLON = ':';
        String cdrroot = m_cdrDir.replace(COLON, DOLLAR);
        String fullCDRPath = "\\\\" + serverIP + "\\" + cdrroot;
        return fullCDRPath;
    }

    private File getLatestFile(File cdrDir)
    {
        logger.info("RBTViral::CDR directory parsed "
                + cdrDir);
        File[] list = cdrDir.getAbsoluteFile().listFiles(new FilenameFilter()
        {

            long latest = 0;

            public boolean accept(File dir, String name)
            {
                if (name.endsWith(".txt"))
                {
                    if (name.startsWith("C"))
                    {
                        File f = new File(dir.getAbsolutePath()
                                + File.separator + name);
                        if (latest <= 0 || f.lastModified() > latest)
                        {
                            m_file = f;
                            latest = f.lastModified();
                        }
                        return false;
                    }
                    else
                        return false;
                }
                else
                    return false;
            }
        });

        if (m_file != null)
            logger.info("RBTViral::Latest CDR file returned is "
                                    + m_file.getAbsolutePath());

        return m_file;
    }

    public static synchronized boolean isTest()
    {
        return (m_testOn);
    }

    public static synchronized boolean sendBasicToActive()
    {
        return (m_sendBasicToActive);
    }

    public static synchronized boolean isDeactSendSMS()
    {
        return (m_deactSubSendSMS);
    }

    public static synchronized boolean isCricketViralOn()
    {
        return (m_cricViralOn);
    }

    public static synchronized boolean isNonSubSendSMS()
    {
        return (m_nonSubSendSMS);
    }

    public static synchronized long startTime()
    {
        return (m_startTime);
    }

    public static synchronized long getMaxSecs()
    {
        return (m_processMaxSecs);
    }

	public static synchronized int getNonSubClearInterval()
    {
        return (m_nonSubClearInterval);
    }

    public static synchronized int nonCricSubClearInterval()
    {
        return (m_nonCricSubClearInterval);
    }

    public static synchronized String cricViralSMS()
    {
        return (m_cricViralSMS);
    }

    public static synchronized String basicViralSMS()
    {
        return (m_basicViralSMS);
    }

    public static synchronized String basicViralNo()
    {
        return (m_basicViralNo);
    }
    
    public static synchronized String cricViralNo()
    {
        return (m_cricViralNo);
    }

    public static synchronized String calledPrefix()
    {
        return (m_calledPrefix);
    }

    public static synchronized boolean isBasicViralOn()
    {
        return (m_basicViralOn);
    }

    public static synchronized int getMinDur()
    {
        return m_minDur;
    }

    public static synchronized boolean checkInCircle(String subID)
    {
        String subscriber = subID;
        if (subscriber.length() >= 4)
        {
        	int prefixIndex = RBTDBManager.getInstance()
					.getPrefixIndex(); 
            String prefix = subscriber.substring(0, prefixIndex); 
            if (m_prefixList.contains(prefix))
                return true;
        }
        return false;
    }

    public static void main(String args[])
    {
        Vector vector;
        try
        {
            vector = new Vector();
            RBTViral rbtViral = new RBTViral();
            rbtViral.init(vector);
            rbtViral.start();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}

class Parse extends Thread
{
	private static Logger logger = Logger.getLogger(RBTViral.class);
	
    private static final int FIELD_INDEX_CALLER = 5;
    private static final int FIELD_INDEX_CALLED = 4;
    private static final int FIELD_INDEX_TIME = 8;
    private static final int FIELD_INDEX_DURATION = 9;
    private static final int FIELD_INDEX_HANGUP = 10;
    private static final int FIELD_INDEX_TOTAL_DURATION = 11;
    private final String m_filePath = "./test.txt";
    private String calledPrefix = null;
	private File m_testSubFile;
    private static final String m_class = "Parse";
    private boolean m_Continue = true;
    Vector m_vector;

    Parse(Vector v)
    {
        m_vector = v;
    }
    
    /**
	 * 
	 */
	public void stopThread()
	{
		m_Continue = false;
	}

    public void run()
    {
        while (m_Continue)
        {
            try
            {

                String str = null;
                synchronized (m_vector)
                {
                    
                	if (m_vector.isEmpty())
                        m_vector.wait();
                    
                	if(!m_vector.isEmpty()) {
	                    str = (String) m_vector.lastElement();
	                    m_vector.remove(str);
                    }
                }
                if(str == null)
                	continue;
                StringTokenizer tokens = new StringTokenizer(str, ",", true);
                int j = 0;
                String caller = null, called = null;
                while (j < FIELD_INDEX_CALLED)
                {
                    j++;
                    if (tokens.nextToken().equals(","))
                        continue;
                    tokens.nextToken();
                }
                String calledNo = tokens.nextToken();
                if (calledNo.equals(","))
                {
                    j++;
                }
                while (j < FIELD_INDEX_CALLER)
                {
                    j++;
                    if (tokens.nextToken().equals(","))
                    {
                        continue;
                    }
                    tokens.nextToken();
                }

                String subscriberString = tokens.nextToken();
                if (subscriberString.equals(","))
                {
                    j++;
                }
                //test is on and the subscriber is not among test numbers do
                // not send sms
                else if (RBTViral.isTest()
                        && !isTestSubscriber(subID(subscriberString)))
                {
                    continue;
                }
                if (!RBTViral.checkInCircle(subID(subscriberString)))
                {
                	logger.info("Rejecting caller -- "+subID(subscriberString)+ " as not a in circle number");
                    continue;
                }
                else
                {
                    caller = subID(subscriberString);
                    called = subID(calledNo);
                }

				calledPrefix =  RBTViral.calledPrefix();
				if(calledPrefix != null)
					called = called.substring(calledPrefix.length());

                while (j < FIELD_INDEX_TIME)
                {
                    j++;
                    if (tokens.nextToken().equals(","))
                        continue;
					tokens.nextToken();
                }

                String tmpString = tokens.nextToken();
                if (tmpString.equals(",") || tmpString.length() < 14)
                {
                    continue;
                }
				else
				{
					if(RBTViral.getMaxSecs() > 0)
					{
						String time = tmpString;
						long diff = RBTViral.startTime() - Tools.getdate(time.substring(0,14), "yyyyMMddHHmmss").getTime();
						if(diff > RBTViral.getMaxSecs())
						{
							logger.info(" Diff " + diff + " is greater than max allowed " + RBTViral.getMaxSecs() + " hence not processing caller "+caller + " called "+called);
							continue;
						}
					}
				}
                tokens.nextToken();
                String temp = tokens.nextToken();

                if (temp.equals(","))
                {
                    continue;
                }
                
				int dur = Integer.parseInt(temp.substring(0, temp.indexOf(".")));

                if (dur < RBTViral.getMinDur()){
                	logger.info("Rejecting caller -- "+caller+ " as duration "+dur+" not sufficient");
                	continue;
                }

                tokens.nextToken();
                String hangUp = tokens.nextToken();
                if (hangUp.equals(","))
                {
                    continue;
                }

                tokens.nextToken();
                temp = tokens.nextToken();
                if (temp.equals(","))
                {
                    continue;
                }
                int totalDur = Integer.parseInt(temp.substring(0, temp.indexOf(".")));

                logger.info("RBTViral::Caller " + caller
                        + " calledNo " + called);

                //				String vpDbURL = RBTViral.vpDbURL();

                if (RBTViral.isCricketViralOn())
                {
                    boolean active = false;

                    if (isCricketStartSubscriber(called)
                            && !isCorporateSubscriber(called))
                    {
                        if (RBTViral.isNonSubSendSMS())
                        {
                            logger.info("RBTViral::Cricket promotion sms to "
                                                    + caller);
                            if (isActiveSubscriber(caller))
                            {
                                active = true;
                            }
                            else
                            {
                                active = false;
                            }
                        }
                        else if (!isActiveSubscriber(caller))
                        {
                            active = false;
                        }

                        String smsActText = null;
                        String smsCtText = null;
                        String sms = RBTViral.cricViralSMS();
                        String cricViralNo = RBTViral.cricViralNo();
                        int criSmsIndex = sms.lastIndexOf("%L");
                        smsCtText = sms;

                        if (criSmsIndex != -1)
                            smsCtText = sms.substring(0, criSmsIndex)
                                    + sms.substring(criSmsIndex + 2);

                        smsActText = sms;
                        if (criSmsIndex != -1)
                            smsActText = sms.substring(0, criSmsIndex) + "ACT"
                                    + sms.substring(criSmsIndex + 2);

                        //if a caller is a blacklist number
                        if (isBlacklistSubscriber(caller))
                            continue;
                        //if corporate subscriber do not send sms
                        if (isCorporateSubscriber(caller))
                            continue;
                        //if a caller is a deactive subscriber and sms should
                        // not be sent to deactive subscriber
                        if (!RBTViral.isDeactSendSMS()
                                && isDeactiveSubscriber(caller))
                            continue;
                        //if a caller is an active cricket subscriber
                        if (isCricketSetSubscriber(caller)
                                || isCricketStartSubscriber(caller))
                            continue;

                        Date smsTime = Calendar.getInstance().getTime();

                        ViralSMSTable[] viralSMS = RBTDBManager
                                .getInstance()
                                .getViralSMS(caller);

                        if (viralSMS != null
                                && viralSMS[0].type()
                                        .equalsIgnoreCase("CRICKET"))
                        {
                            Date lastSMSDate = viralSMS[0].sentTime();
                            long difference = (Calendar.getInstance().getTime()
                                    .getTime() - lastSMSDate.getTime())
                                    / (3600 * 1000);
                            int diff = new Long(difference).intValue();
                            if (diff < RBTViral.nonCricSubClearInterval())
                                continue;

                            RBTDBManager.getInstance()
                                    .updateViralSMSTable(caller, smsTime,
                                                         "CRICKET", called,
                                                         null, 0, null, null,null);
                        }
                        else if (viralSMS != null
                                && viralSMS[0].type().equalsIgnoreCase("BASIC"))
                            continue;
                        else
                        {
                            RBTDBManager.getInstance()
                                    .insertViralSMSTableMap(caller, smsTime,
                                                         "CRICKET", called,
                                                         null, 0, null, null,null);
                        }

                        if (!active)
                        {
                            try
                            {
                                Tools.sendSMS(cricViralNo, caller, smsActText,
                                              false);
                            }
                            catch (Exception e)
                            {
                                logger.error("", e);
                            }
                        }
                        else
                        {
                            try
                            {
                                Tools.sendSMS(cricViralNo, caller, smsCtText,
                                              false);
                            }
                            catch (Exception e)
                            {
                                logger.error("", e);
                            }
                        }
                    }
                }
                if (RBTViral.isBasicViralOn())
                {
                	logger.info(caller+ " isActiveSubscriber "+isActiveSubscriber(caller));
                    if ((RBTViral.isTest() && RBTViral.sendBasicToActive() && !isCorporateSubscriber(called))
                            || (!isActiveSubscriber(caller) && !isCorporateSubscriber(called)))
                    {
                        //if a caller is a blacklist number
                        /*
                         * if(blackOutList.contains(caller)) continue;
                         */
                        if (isBlacklistSubscriber(caller))
                            continue;
                        //if corporate subscriber do not send sms
                        if (isCorporateSubscriber(caller))
                            continue;
                        //if a caller is a deactive subscriber and sms should
                        // not be sent to deactive subscriber
                        if (!RBTViral.isDeactSendSMS()
                                && isDeactiveSubscriber(caller))
                            continue;

                        //get the song name played to the caller
                        SubscriberStatus subscriberStatus = RBTDBManager
                                .getInstance()
                                .getViralSelection(called, caller);
                        if (subscriberStatus == null)
                            continue;

                        ClipMinimal clip = RBTDBManager
                                .getInstance()
                                .getClipRBT(subscriberStatus.subscriberFile());
                        if (clip == null)
                            continue;

                        Date smsTime = Calendar.getInstance().getTime();

                        String sms = RBTViral.basicViralSMS();
                        String basicViralNo = RBTViral.basicViralNo();

                        if (sms == null)
                            return;
                        //get the song name played to the caller
                        if (sms.indexOf("%S") != -1)
                        {
                            sms = sms.substring(0, sms.indexOf("%S"))
                                    + clip.getClipName()
                                    + sms.substring(sms.indexOf("%S") + 2);
                        }
                        if (sms.indexOf("%C") != -1)
                        {
                            sms = sms.substring(0, sms.indexOf("%C"))
                                    + called
                                    + sms.substring(sms.indexOf("%C") + 2);
                        }

                        ViralSMSTable[] viralSMS = RBTDBManager
                                .getInstance()
                                .getViralSMS(caller);
                        if (viralSMS != null
                                && viralSMS[0].type().equalsIgnoreCase("BASIC"))
                        {
                            Date lastSMSDate = viralSMS[0].sentTime();
                            long difference = (Calendar.getInstance().getTime()
                                    .getTime() - lastSMSDate.getTime())
                                    / (3600 * 1000);
                            int diff = new Long(difference).intValue();
                            if (diff < RBTViral.getNonSubClearInterval())
							{
								logger.info("caller "+caller + " has been already made viral in "+diff);
                                continue;
							}
                            RBTDBManager
                                    .getInstance()
                                    .updateViralSMSTable(
                                                         caller,
                                                         smsTime,
                                                         "BASIC",
                                                         called,
                                                         (new Integer(clip.getClipId()))
                                                                 .toString(),
                                                         0, null, null,null);
                        }
                        else if (viralSMS != null
                                && viralSMS[0].type()
                                        .equalsIgnoreCase("CRICKET")/* m_cricketViralTable.contains(caller) */)
                            continue;
                        else
                        {
                            RBTDBManager
                                    .getInstance()
                                    .insertViralSMSTableMap(
                                                         caller,
                                                         smsTime,
                                                         "BASIC",
                                                         called,
                                                         (new Integer(clip.getClipId()))
                                                                 .toString(),
                                                         0, null, null,null);
                        }

                        try
                        {
                            String[] smsText = splitSMSText(sms);
                            if (smsText != null)
                            {
                                for (int i = 0; i < smsText.length; i++)
                                {
                                    Tools.sendSMS(basicViralNo, caller,
                                                  smsText[i], false);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            logger.error("", e);
                        }

                    }
                }

            }
            catch (Exception e)
            {
                logger.error("", e);
                continue;
            }
        }

        logger.info("Thread is killed");
    }

    public boolean isTestSubscriber(String caller)
    {
        LineNumberReader fin = null;
        m_testSubFile = new File(m_filePath);

        if (!m_testSubFile.exists())
        {
            logger.info("RBTViral::Test file missing!!!");
            return false;
        }
        try
        {
            fin = new LineNumberReader(new FileReader(m_testSubFile));
            String str = null;
            while ((str = fin.readLine()) != null)
            {
                if (str.trim().equalsIgnoreCase(caller.trim()))
                    return true;
            }
        }
        catch (Exception e)
        {
            logger.error("", e);
        }

        try
        {
            fin.close();
        }
        catch (Exception e)
        {

        }

        return false;
    }

    private String subID(String subscriberID)
    {
        return (RBTDBManager.getInstance().subID(subscriberID));
    }

    private boolean isCorporateSubscriber(String subID)
    {
        if (RBTDBManager.getInstance()
                .getActiveSubscriberRecord(subID, null, 0, 0, 2359) != null)
            return true;
        return false;
    }

    private boolean isDeactiveSubscriber(String subID)
    {
        return (RBTDBManager.getInstance().isSubscriberDeActivated(subID));
    }

    private boolean isActiveSubscriber(String subID)
    {
        return (RBTDBManager.getInstance()
                .isSubscriberActivated(subID, true));
    }

    private boolean isCricketSetSubscriber(String subID)
    {
        if (RBTDBManager.getInstance()
                .getActiveStatus(subID, false) != null)
            return true;
        return false;
    }

    private boolean isCricketStartSubscriber(String subID)
    {
        if (RBTDBManager.getInstance()
                .getActiveStatus(subID, true) != null)
            return true;
        return false;
    }

    private boolean isBlacklistSubscriber(String subID)
    {
        return RBTDBManager.getInstance()
                .isViralBlackListSub(subID);
    }

    private String[] splitSMSText(String strSmsText)
    {
        if (strSmsText != null)
        {
            if (strSmsText.length() > 160)
            {
                int index = 0;
                int last = 155;
                List list = new ArrayList();
                String temp = null;
                while (index < strSmsText.length())
                {
                    temp = strSmsText.substring(index, (index + last));
                    if (last >= 155)
                    {
                        last = temp.lastIndexOf(" ");
                        temp = strSmsText.substring(index, last);
                    }
                    else
                    {
                        list.add(temp);
                        break;
                    }
                    index = last;
                    last = strSmsText.length() - index;

                    list.add(temp);
                }

                if (list.size() > 0)
                {
                    String[] smsTexts = (String[]) list.toArray(new String[0]);
                    return smsTexts;
                }
                else
                {
                    return null;
                }
            }
            else
            {
                String[] smsTexts = new String[1];
                smsTexts[0] = strSmsText;
                return smsTexts;
            }
        }
        return null;
    }
}

