package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ChargeClassMap;

public class ChargeClassMapImpl extends RBTPrimitive implements ChargeClassMap
{
    private static Logger logger = Logger.getLogger(ChargeClassMapImpl.class);

    private static final String TABLE_NAME = "RBT_CHARGE_CLASS_MAP";
    private static final String CLASS_TYPE_COL = "CLASS_TYPE";
    private static final String REGEX_SMS_OR_VOICE_COL = "REGEX_SMS_OR_VOICE";
    private static final String ACCESS_MODE_COL = "ACCESS_MODE";
    private static final String FINAL_CLASS_TYPE_COL = "FINAL_CLASS_TYPE";

    private String m_classType;
    private String m_regexClass;
    private String m_mode;
    private String m_finalClassType;

    private ChargeClassMapImpl(String classType, String regexClass,
            String mode, String finalClassType)
    {
        m_classType = classType;
        m_regexClass = regexClass;
        m_mode = mode;
        m_finalClassType = finalClassType;
    }

    public String classType()
    {
        return m_classType;
    }

    public String mode()
    {
        return m_mode;
    }

    public String regexClass()
    {
        return m_regexClass;
    }

    public String finalClassType()
    {
        return m_finalClassType;
    }

    static ChargeClassMap[] getChargeClassMaps(Connection conn)
    {
        logger.info("RBT::inside getChargeClassMaps");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String classType = null;
        String mode = null;
        String regexClass = null;
        String finalClassType = null;

        ChargeClassMapImpl chargeClassMap = null;
        List chargeClassMapList = new ArrayList();

        query = "SELECT * FROM " + TABLE_NAME;

        logger.info("RBT::query " + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                classType = results.getString(CLASS_TYPE_COL);
                mode = results.getString(ACCESS_MODE_COL);
                regexClass = results.getString(REGEX_SMS_OR_VOICE_COL);
                finalClassType = results.getString(FINAL_CLASS_TYPE_COL);
                chargeClassMap = new ChargeClassMapImpl(classType, regexClass,
                        mode, finalClassType);
                chargeClassMapList.add(chargeClassMap);
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
        if (chargeClassMapList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_CHARGE_CLASS_MAP successful");
            return (ChargeClassMap[]) chargeClassMapList
                    .toArray(new ChargeClassMap[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_CHARGE_CLASS_MAP");
            return null;
        }
    }

    static ChargeClassMap[] getChargeClassMapsForType(Connection conn,
            String types)
    {
    	logger.info("RBT::inside getChargeClassMapsForType");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String classType = null;
        String mode = null;
        String regexClass = null;
        String finalClassType = null;

        ChargeClassMapImpl chargeClassMap = null;
        List chargeClassMapList = new ArrayList();

        query = "SELECT * FROM " + TABLE_NAME + " WHERE "
                + REGEX_SMS_OR_VOICE_COL + " LIKE '%" + types + "%'";

        logger.info("RBT::query "
                + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                classType = results.getString(CLASS_TYPE_COL);
                mode = results.getString(ACCESS_MODE_COL);
                regexClass = results.getString(REGEX_SMS_OR_VOICE_COL);
                finalClassType = results.getString(FINAL_CLASS_TYPE_COL);

                chargeClassMap = new ChargeClassMapImpl(classType, regexClass,
                        mode, finalClassType);
                chargeClassMapList.add(chargeClassMap);
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
        if (chargeClassMapList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_CHARGE_CLASS_MAP successful");
            return (ChargeClassMap[]) chargeClassMapList
                    .toArray(new ChargeClassMap[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_CHARGE_CLASS_MAP");
            return null;
        }
    }

    static ChargeClassMap[] getChargeClassMapsForModeType(Connection conn,
            String modes, String types)
    {
        logger.info("RBT::inside getChargeClassMapsForModeType");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String classType = null;
        String mode = null;
        String regexClass = null;
        String finalClassType = null;

        ChargeClassMapImpl chargeClassMap = null;
        List chargeClassMapList = new ArrayList();

        query = "SELECT * FROM " + TABLE_NAME + " WHERE "
                + REGEX_SMS_OR_VOICE_COL + " like '%" + types + "%'";

        if ("VUI".equalsIgnoreCase(modes))
            query += " AND " + ACCESS_MODE_COL + " = '" + modes + "'";
        else if (modes != null)
            query += " AND " + ACCESS_MODE_COL + " IN ('" + modes + "', 'ALL')";

        logger.info("RBT::query "
                + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                classType = results.getString(CLASS_TYPE_COL);
                mode = results.getString(ACCESS_MODE_COL);
                regexClass = results.getString(REGEX_SMS_OR_VOICE_COL);
                finalClassType = results.getString(FINAL_CLASS_TYPE_COL);

                chargeClassMap = new ChargeClassMapImpl(classType, regexClass,
                        mode, finalClassType);
                chargeClassMapList.add(chargeClassMap);
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
        if (chargeClassMapList.size() > 0)
        {
           logger.info("RBT::retrieving records from RBT_CHARGE_CLASS_MAP successful");
            return (ChargeClassMap[]) chargeClassMapList
                    .toArray(new ChargeClassMap[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_CHARGE_CLASS_MAP");
            return null;
        }
    }

    static ChargeClassMap[] getChargeClassMapsForFinalClassType(
            Connection conn, String finalClassType, String accessedMode)
    {
        logger.info("RBT::inside getChargeClassMapsForFinalClassType");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String classType = null;
        String mode = null;
        String regexClass = null;
        String finalClsType = null;

        ChargeClassMapImpl chargeClassMap = null;
        List chargeClassMapList = new ArrayList();

        query = "SELECT * FROM " + TABLE_NAME + " WHERE "
                + FINAL_CLASS_TYPE_COL + " = '" + finalClassType + "'";

        // For VUI cases, access mode will be checked only for VUI as requested
        // by Geo. For rest other apps like sms,web and wap etc. accessed mode
        // will be checked for ALL also.
        if ("VUI".equalsIgnoreCase(accessedMode))
            query += " AND " + ACCESS_MODE_COL + " = '" + accessedMode + "'";
        else if (accessedMode != null)
            query += " AND " + ACCESS_MODE_COL + " IN ('" + accessedMode
                    + "', 'ALL')";

        logger.info("RBT::query " + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                classType = results.getString(CLASS_TYPE_COL);
                mode = results.getString(ACCESS_MODE_COL);
                regexClass = results.getString(REGEX_SMS_OR_VOICE_COL);
                finalClsType = results.getString(FINAL_CLASS_TYPE_COL);

                chargeClassMap = new ChargeClassMapImpl(classType, regexClass,
                        mode, finalClsType);
                chargeClassMapList.add(chargeClassMap);
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
        if (chargeClassMapList.size() > 0)
        {
            logger.info("RBT::retrieving records from RBT_CHARGE_CLASS_MAP successful");
            return (ChargeClassMap[]) chargeClassMapList
                    .toArray(new ChargeClassMap[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_CHARGE_CLASS_MAP");
            return null;
        }
    }

    static ChargeClassMap[] getChargeClassMapsForClassTypeType(Connection conn,
            String classTypes, String types)
    {
       logger.info("RBT::inside getChargeClassMapsForClassTypeType");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String classType = null;
        String mode = null;
        String regexClass = null;
        String finalClassType = null;

        ChargeClassMapImpl chargeClassMap = null;
        List chargeClassMapList = new ArrayList();

        query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLASS_TYPE_COL
                + " = '" + classTypes + "' AND " + REGEX_SMS_OR_VOICE_COL
                + " LIKE '" + types + "'";

        logger.info("RBT::query " + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                classType = results.getString(CLASS_TYPE_COL);
                mode = results.getString(ACCESS_MODE_COL);
                regexClass = results.getString(REGEX_SMS_OR_VOICE_COL);
                finalClassType = results.getString(FINAL_CLASS_TYPE_COL);

                chargeClassMap = new ChargeClassMapImpl(classType, regexClass,
                        mode, finalClassType);
                chargeClassMapList.add(chargeClassMap);
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
        if (chargeClassMapList.size() > 0)
        {
           logger.info("RBT::retrieving records from RBT_CHARGE_CLASS_MAP successful");
            return (ChargeClassMap[]) chargeClassMapList
                    .toArray(new ChargeClassMap[0]);
        }
        else
        {
            logger.info("RBT::no records in RBT_CHARGE_CLASS_MAP");
            return null;
        }
    }

    static ChargeClassMap getChargeClassMapsForModeRegexTypeAndClassType(
            Connection conn, String mode, String regexType, String classType)
    {
        logger.info("RBT::inside getChargeClassMapsForModeRegexTypeAndClassType");

        String query = null;
        Statement stmt = null;
        RBTResultSet results = null;

        String class_type = null;
        String accessed_mode = null;
        String regex_type = null;
        String final_class_type = null;

        ChargeClassMapImpl chargeClassMap = null;

        query = "SELECT * FROM " + TABLE_NAME + " WHERE " + CLASS_TYPE_COL
                + " = '" + classType + "' AND " + REGEX_SMS_OR_VOICE_COL
                + " = '" + regexType + "'";

        if ("VUI".equalsIgnoreCase(mode))
            query += " AND " + ACCESS_MODE_COL + " = '" + mode + "'";
        else if (mode != null)
            query += " AND " + ACCESS_MODE_COL + " IN ('" + mode + "', 'ALL')";

        logger.info("RBT::query " + query);

        try
        {
            logger.info("RBT::inside try block");
            stmt = conn.createStatement();
            results = new RBTResultSet(stmt.executeQuery(query));
            while (results.next())
            {
                class_type = results.getString(CLASS_TYPE_COL);
                accessed_mode = results.getString(ACCESS_MODE_COL);
                regex_type = results.getString(REGEX_SMS_OR_VOICE_COL);
                final_class_type = results.getString(FINAL_CLASS_TYPE_COL);

                chargeClassMap = new ChargeClassMapImpl(class_type, regex_type,
                        accessed_mode, final_class_type);
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
        return chargeClassMap;
    }
}