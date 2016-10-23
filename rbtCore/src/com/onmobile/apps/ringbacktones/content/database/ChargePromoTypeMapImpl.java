package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ChargePromoTypeMap;

public class ChargePromoTypeMapImpl extends RBTPrimitive implements
        ChargePromoTypeMap
{
    private static Logger logger = Logger.getLogger(ChargePromoTypeMapImpl.class);

    private static final String TABLE_NAME = "RBT_CHARGE_PROMOTYPE_MAP";
    private static final String SMS_KEYWORD_COL = "SMS_KEYWORD";
    private static final String CHARGE_TYPE_COL = "CHARGE_TYPE";
    private static final String PROMO_TYPE_COL = "PROMO_TYPE";
    private static final String VOICE_GRAMMER_COL = "VOICE_GRAMMER";
    private static final String LEVEL_NO_COL = "LEVEL_NO";
    private static final String VOICE_ORDER_COL = "VOICE_ORDER";

    private String m_smsKeyword;
    private String m_chargeType;
    private String m_promoType;
    private String m_voiceGrammer;
    private int m_level;
    private int m_voiceOrder;

    private ChargePromoTypeMapImpl(String smsKeyword, String chargeType,
            String promoType, String voiceGrammer, int level, int voiceOrder)
    {
        m_smsKeyword = smsKeyword;
        m_chargeType = chargeType;
        m_promoType = promoType;
        m_voiceGrammer = voiceGrammer;
        m_level = level;
        m_voiceOrder = voiceOrder;
    }

    public String smsKeyword()
    {
        return m_smsKeyword;
    }

    public String chargeType()
    {
        return m_chargeType;
    }

    public String promoType()
    {
        return m_promoType;
    }

    public String voiceGrammer()
    {
        return m_voiceGrammer;
    }

    public int level()
    {
        return m_level;
    }

    public int voiceOrder()
    {
        return m_voiceOrder;
    }

    static ChargePromoTypeMap[] getChargePromoTypeMaps(Connection conn)
    {
        logger.info("RBT::inside getChargePromoTypeMaps");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String smsKeyword = null;
        String chargeType = null;
        String promoType = null;
        String voiceGrammer = null;
        int level = 0;
        int voiceOrder = 0;

        ChargePromoTypeMapImpl chargePromoTypeMapImpl = null;
        List chargePromoTypeMapList = new ArrayList();

        query = "SELECT * FROM " + TABLE_NAME;

        logger.info("RBT::query "
                + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                smsKeyword = results.getString(SMS_KEYWORD_COL);
                chargeType = results.getString(CHARGE_TYPE_COL);
                promoType = results.getString(PROMO_TYPE_COL);
                voiceGrammer = results.getString(VOICE_GRAMMER_COL);
                level = results.getInt(LEVEL_NO_COL);
                voiceOrder = results.getInt(VOICE_ORDER_COL);
                chargePromoTypeMapImpl = new ChargePromoTypeMapImpl(smsKeyword,
                        chargeType, promoType, voiceGrammer, level, voiceOrder);
                chargePromoTypeMapList.add(chargePromoTypeMapImpl);
            }
        }
        catch (SQLException se)
        {
        	logger.error("", se);
            return null;
        }
        finally
        {
            try
            {
                stmt.close();
            }
            catch (Exception e)
            {
            	logger.error("", e);
            }
        }
        if (chargePromoTypeMapList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_CHARGE_PROMOTYPE_MAP successful");
            return (ChargePromoTypeMap[]) chargePromoTypeMapList
                    .toArray(new ChargePromoTypeMap[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_CHARGE_PROMOTYPE_MAP");
            return null;
        }
    }

    static ChargePromoTypeMap[] getChargePromoTypeMapsByLevel(Connection conn,
            int level, String chargeType)
    {
        logger.info("RBT::inside getChargePromoTypeMapsByLevel");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String smsKeyword = null;
        String charge_type = null;
        String promoType = null;
        String voiceGrammer = null;
        int level_no = 0;
        int voiceOrder = 0;

        ChargePromoTypeMapImpl chargePromoTypeMapImpl = null;
        List chargePromoTypeMapList = new ArrayList();

        query = "SELECT * FROM " + TABLE_NAME + " WHERE " + LEVEL_NO_COL
                + " = '" + level + "' AND " + CHARGE_TYPE_COL + " = "
                + sqlString(chargeType);

        logger.info("RBT::query "
                + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                smsKeyword = results.getString(SMS_KEYWORD_COL);
                charge_type = results.getString(CHARGE_TYPE_COL);
                promoType = results.getString(PROMO_TYPE_COL);
                voiceGrammer = results.getString(VOICE_GRAMMER_COL);
                level_no = results.getInt(LEVEL_NO_COL);
                voiceOrder = results.getInt(VOICE_ORDER_COL);
                chargePromoTypeMapImpl = new ChargePromoTypeMapImpl(smsKeyword,
                        charge_type, promoType, voiceGrammer, level_no,
                        voiceOrder);
                chargePromoTypeMapList.add(chargePromoTypeMapImpl);
            }
        }
        catch (SQLException se)
        {
        	logger.error("", se);
            return null;
        }
        finally
        {
            try
            {
                stmt.close();
            }
            catch (Exception e)
            {
            	logger.error("", e);
            }
        }
        if (chargePromoTypeMapList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_CHARGE_PROMOTYPE_MAP successful");
            return (ChargePromoTypeMap[]) chargePromoTypeMapList
                    .toArray(new ChargePromoTypeMap[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_CHARGE_PROMOTYPE_MAP");
            return null;
        }
    }

    static ChargePromoTypeMap[] getChargePromoTypeMapsForType(Connection conn,
            String promoType, String chargeType, String accessedFrom)
    {
        logger.info("RBT::inside getChargePromoTypeMapsForType");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String smsKeyword = null;
        String charge_type = null;
        String promo_type = null;
        String voiceGrammer = null;
        int level_no = 0;
        int voiceOrder = 0;

        ChargePromoTypeMapImpl chargePromoTypeMapImpl = null;
        List chargePromoTypeMapList = new ArrayList();

        query = "SELECT * FROM " + TABLE_NAME + " WHERE " + PROMO_TYPE_COL
                + " = '" + promoType + "' AND " + CHARGE_TYPE_COL + " = "
                + sqlString(chargeType);

        if ("VUI".equalsIgnoreCase(accessedFrom))
            query += " AND " + VOICE_GRAMMER_COL + " IS NOT NULL";

        logger.info("RBT::query " + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                smsKeyword = results.getString(SMS_KEYWORD_COL);
                charge_type = results.getString(CHARGE_TYPE_COL);
                promo_type = results.getString(PROMO_TYPE_COL);
                voiceGrammer = results.getString(VOICE_GRAMMER_COL);
                level_no = results.getInt(LEVEL_NO_COL);
                voiceOrder = results.getInt(VOICE_ORDER_COL);
                chargePromoTypeMapImpl = new ChargePromoTypeMapImpl(smsKeyword,
                        charge_type, promo_type, voiceGrammer, level_no,
                        voiceOrder);
                chargePromoTypeMapList.add(chargePromoTypeMapImpl);
            }
        }
        catch (SQLException se)
        {
        	logger.error("", se);
            return null;
        }
        finally
        {
            try
            {
                stmt.close();
            }
            catch (Exception e)
            {
            	logger.error("", e);
            }
        }
        if (chargePromoTypeMapList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_CHARGE_PROMOTYPE_MAP successful");
            return (ChargePromoTypeMap[]) chargePromoTypeMapList
                    .toArray(new ChargePromoTypeMap[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_CHARGE_PROMOTYPE_MAP");
            return null;
        }
    }

    static ChargePromoTypeMap[] getChargePromoTypeMapsForLevelAndType(
            Connection conn, String accessedFrom, int level, String type)
    {
        logger.info("RBT::inside getChargePromoTypeMapsForLevelAndType");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String smsKeyword = null;
        String charge_type = null;
        String promoType = null;
        String voiceGrammer = null;
        int level_no = 0;
        int voiceOrder = 0;

        ChargePromoTypeMapImpl chargePromoTypeMapImpl = null;
        List chargePromoTypeMapList = new ArrayList();

        query = "SELECT * FROM " + TABLE_NAME + " WHERE " + LEVEL_NO_COL + " = "
                + level + " AND " + CHARGE_TYPE_COL + " = " + sqlString(type);

        if ("VUI".equalsIgnoreCase(accessedFrom))
            query += " AND " + VOICE_GRAMMER_COL + " IS NOT NULL";

        logger.info("RBT::query " + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                smsKeyword = results.getString(SMS_KEYWORD_COL);
                charge_type = results.getString(CHARGE_TYPE_COL);
                promoType = results.getString(PROMO_TYPE_COL);
                voiceGrammer = results.getString(VOICE_GRAMMER_COL);
                level_no = results.getInt(LEVEL_NO_COL);
                voiceOrder = results.getInt(VOICE_ORDER_COL);
                chargePromoTypeMapImpl = new ChargePromoTypeMapImpl(smsKeyword,
                        charge_type, promoType, voiceGrammer, level_no,
                        voiceOrder);
                chargePromoTypeMapList.add(chargePromoTypeMapImpl);
            }
        }
        catch (SQLException se)
        {
        	logger.error("", se);
            return null;
        }
        finally
        {
            try
            {
                stmt.close();
            }
            catch (Exception e)
            {
            	logger.error("", e);
            }
        }
        if (chargePromoTypeMapList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_CHARGE_PROMOTYPE_MAP successful");
            return (ChargePromoTypeMap[]) chargePromoTypeMapList
                    .toArray(new ChargePromoTypeMap[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_CHARGE_PROMOTYPE_MAP");
            return null;
        }
    }
}