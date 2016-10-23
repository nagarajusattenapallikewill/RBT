package com.onmobile.apps.ringbacktones.common;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import com.onmobile.common.db.OnMobileDBServices;
import com.onmobile.common.debug.DebugManager;

public class RBTMultimodal
{
    private static final String _moduleName = "IVM-Multimodal";
    private static final String _className = "RBTMultimodal";
    private static RBTMultimodal _multimodal = null;
    private static Hashtable _phoneNos = null;
    private static boolean _bNumbersAvail = false;
    private static boolean _bInitializeCalled = false;
    private static String _connString = null;
    private static final String EXCEPTION_NOT_INITIALIZED = "Multimodal is not initialized! Return null.";
    private static final String EXCEPTION_NUMBER_POOL_EMPTY = "Multimodal number pool is empty! Return null.";
    private static final String MCM_APP_ID = "AppId";
    private static final String MCM_CONTEXT_KEY = "ContextKey";
    private static final String MCM_CONTEXT_DATA = "ContextData";
    private static final String DEFAULT_POOL_KEY = "General Pool";

    /**
     * Initialize and get an instance of Multimodal
     * 
     * @param connString Database connect string
     * 
     * @return Multimodal or null
     */
    static
    {
        _multimodal = new RBTMultimodal();
    }

    public static RBTMultimodal getInstance(String connString)
    {
        synchronized (_multimodal)
        {
            if (!_bNumbersAvail || !_bInitializeCalled)
            {
                RBTMultimodal.initialize(connString);
            }
        }
        return _multimodal;
    }

    private static RBTMultimodal initialize(String connString)
    {
        String strMethodName = "initialize";
        _phoneNos = new Hashtable();

        try
        {
            _connString = connString;
            DebugManager.trace(_moduleName, _className, strMethodName,
                               "_connString = " + _connString, Thread
                                       .currentThread().getName(), "");

            String strSQL = "SELECT NUMBER_POOL_KEY, PHONE_NO FROM MM_INCOMING_PHONE_NUMBERS WHERE PHONE_NO_FOR_MASS_PUSH != 1 ORDER BY PHONE_NO";
            Connection con = MultimodalDBServices.getDBConnection(_connString);
            ResultSet rs = MultimodalDBServices
                    .executeSingletonSQL(con, strSQL);
            String poolKey = null;
            String no = null;
            while (rs.next())
            {
                poolKey = rs.getString("NUMBER_POOL_KEY");
                no = rs.getString("PHONE_NO");
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "poolKey=" + poolKey + " and no=" + no,
                                   Thread.currentThread().getName(), "");
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "Call _phoneNos.get(" + poolKey + ")",
                                   Thread.currentThread().getName(), "");
                Vector v = (Vector) _phoneNos.get(poolKey);
                if (v == null)
                    v = new Vector();
                v.addElement(no);
                _phoneNos.put(poolKey, v);
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "For poolKey=" + poolKey
                                           + ", no of numbers=" + v.size(),
                                   Thread.currentThread().getName(), "");
            }
            rs.close();
            MultimodalDBServices.releaseConnection(con);
            if (_phoneNos.size() > 0)
            {
                _bNumbersAvail = true;
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "_bNumbersAvail set to true", Thread
                                           .currentThread().getName(), "");
            }
            _bInitializeCalled = true;
            return _multimodal;
        }
        catch (Exception e)
        {
            DebugManager.exception(_moduleName, _className, strMethodName, e,
                                   Thread.currentThread().getName(), "");
        }
        _bInitializeCalled = false;
        return null;
    }

    private static String getPoolKey(String strPhoneNo)
    {
        String strMethodName = "getPoolKey";
        String strPoolKey = null;
        try
        {
            if (_phoneNos != null)
            {
                Enumeration e = _phoneNos.keys();
                int ctr = 0;
                while (e.hasMoreElements())
                {
                    String s = (String) e.nextElement();
                    ctr++;
                    Vector v = (Vector) _phoneNos.get(s);
                    if (v.contains(strPhoneNo))
                    {
                        strPoolKey = s;
                        return strPoolKey;
                    }
                }
            }
        }
        catch (Exception ex)
        {
            DebugManager.exception(_moduleName, _className, strMethodName, ex,
                                   Thread.currentThread().getName(), "");
            return null;
        }
        return null;
    }

    /**
     * Remove context from all tables for this number
     * 
     * @param number Phone number whose contexts are to be deleted
     * 
     * @return false if either Multimodal is not initialized; true otherwise
     */
    public boolean removeContextsForNumber(String number)
    {
        String strMethodName = "removeContextsForNumber";
        DebugManager.trace(_moduleName, _className, strMethodName,
                           "Entry with number = " + number, Thread
                                   .currentThread().getName(), "");

        try
        {
            Connection con = MultimodalDBServices.getDBConnection(_connString);
            String strSQL = "DELETE FROM MULTIMODAL_CONTEXT_NON_SUBS WHERE CONTEXT_MODE_ID = '"
                    + number + "'";
            int iAffectedRows = MultimodalDBServices.executeUpdate(con, strSQL);
            DebugManager.trace(_moduleName, _className, strMethodName,
                               "After Deleting, iAffectedRows = "
                                       + iAffectedRows, Thread.currentThread()
                                       .getName(), "");
            MultimodalDBServices.releaseConnection(con);
            return true;
        }
        catch (Exception e)
        {
            DebugManager.exception(_moduleName, _className, strMethodName, e,
                                   Thread.currentThread().getName(), "");
        }
        return false;
    }

    /**
     * Application registering a context with the MCM for later use
     * 
     * @param callerNumber Phone number for whom context is to be set
     * @param appId Application ID (Service_Id from Service table)
     * @param context key Context key
     * @param context data Context data
     * @param poolKey Pool key. If null, multimodal number is picked from the
     *            general pool
     * 
     * @return null if a failure occurs, otherwise, the multimodal number
     */
    public String getMultimodalNumber(String callerNumber, String appID,
            String contextKey, String contextData, String poolKey)
    {
        String strMethodName = "getMultimodalNumber";
        DebugManager.trace(_moduleName, _className, strMethodName,
                           "Entry with callerNumber=" + callerNumber
                                   + ",appID=" + appID + ",contextKey="
                                   + contextKey + ",contextData=" + contextData
                                   + ",poolKey=" + poolKey, Thread
                                   .currentThread().getName(), "");
        if (!_bInitializeCalled)
        {
            DebugManager.warning(_moduleName, _className, strMethodName,
                                 EXCEPTION_NOT_INITIALIZED, Thread
                                         .currentThread().getName(), "");
            return null;
        }
        if (!_bNumbersAvail)
        {
            DebugManager.warning(_moduleName, _className, strMethodName,
                                 EXCEPTION_NUMBER_POOL_EMPTY, Thread
                                         .currentThread().getName(), "");
            return null;
        }
        callerNumber = callerNumber.toUpperCase();
        appID = appID.toUpperCase();
        Vector vPhoneNos = new Vector();
        if (poolKey != null)
        {
            if (_phoneNos != null)
            {
                int ctr = 0;
                Enumeration e = _phoneNos.keys();
                while (e.hasMoreElements())
                {
                    String s = (String) e.nextElement();
                    DebugManager.trace(_moduleName, _className, strMethodName,
                                       "poolKey no. " + ctr + " = " + s, Thread
                                               .currentThread().getName(), "");
                    ctr++;
                    DebugManager
                            .trace(
                                   _moduleName,
                                   _className,
                                   strMethodName,
                                   "If "
                                           + poolKey
                                           + " (from API) equals "
                                           + s
                                           + " (from database), vPhoneNos will be populated",
                                   Thread.currentThread().getName(), "");
                    if (poolKey.equals(s))
                    {
                        DebugManager.trace(_moduleName, _className,
                                           strMethodName, "Call _phoneNos.get("
                                                   + s + ")", Thread
                                                   .currentThread().getName(),
                                           "");
                        vPhoneNos = (Vector) _phoneNos.get(s);
                        DebugManager.trace(_moduleName, _className,
                                           strMethodName, "vPhoneNos.size() = "
                                                   + vPhoneNos.size(), Thread
                                                   .currentThread().getName(),
                                           "");
                        break;
                    }
                }
            }
            else
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "_phoneNos is null", Thread.currentThread()
                                           .getName(), "");
            if (vPhoneNos.size() == 0)
            {
                DebugManager
                        .warning(
                                 _moduleName,
                                 _className,
                                 strMethodName,
                                 "Did not find number pool with this pool key ("
                                         + poolKey
                                         + "). Will now see if a general pool exists (with default pool key)",
                                 Thread.currentThread().getName(), "");
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "Call _phoneNos.get(DEFAULT_POOL_KEY)",
                                   Thread.currentThread().getName(), "");
                vPhoneNos = (Vector) _phoneNos.get(DEFAULT_POOL_KEY);
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "vPhoneNos.size() = " + vPhoneNos.size(),
                                   Thread.currentThread().getName(), "");
            }
        }
        else
        {
            DebugManager
                    .trace(
                           _moduleName,
                           _className,
                           strMethodName,
                           "poolKey is null. Look for numbers in the general pool",
                           Thread.currentThread().getName(), "");
            DebugManager.trace(_moduleName, _className, strMethodName,
                               "Call _phoneNos.get(DEFAULT_POOL_KEY)", Thread
                                       .currentThread().getName(), "");
            vPhoneNos = (Vector) _phoneNos.get(DEFAULT_POOL_KEY);
            DebugManager.trace(_moduleName, _className, strMethodName,
                               "vPhoneNos.size() = " + vPhoneNos.size(), Thread
                                       .currentThread().getName(), "");
        }

        if ((vPhoneNos == null) || (vPhoneNos.size() == 0))
        {
            DebugManager
                    .warning(
                             _moduleName,
                             _className,
                             strMethodName,
                             "Number pool empty for this particular key and General Pool is empty too! Return null.",
                             Thread.currentThread().getName(), "");
            return null;
        }

        int iAffectedRows = 0;
        Connection con = null;
        String incomingNo = null;
        ResultSet rs = null;
        String strSQL = null;
        String applicationID = null;
        String key = null;
        long lTS = 0;

        try
        {
            con = MultimodalDBServices.getDBConnection(_connString);
            strSQL = "SELECT CONTEXT_MODE_ID, APP_ID, CONTEXT_KEY, TS FROM MULTIMODAL_CONTEXT_NON_SUBS WHERE NON_SUBSCRIBER_PHONE_NUMBER = '"
                    + callerNumber + "' FOR UPDATE OF";

            //			System.out.println(strSQL);
            rs = MultimodalDBServices.executeSingletonSQL(con, strSQL);

            HashMap map = new HashMap();
            HashMap map1 = new HashMap();
            int i = 0;
            while (rs.next())
            {
                i++;
                incomingNo = rs.getString("CONTEXT_MODE_ID");
                applicationID = rs.getString("APP_ID");
                key = rs.getString("CONTEXT_KEY");
                lTS = rs.getLong("TS");

                boolean updateMap = false;
                String strPoolKeyName = getPoolKey(incomingNo);
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "updateMap : strPoolKeyName = "
                                           + strPoolKeyName + ", poolKey = "
                                           + poolKey + ", incomingNo = "
                                           + incomingNo, Thread.currentThread()
                                           .getName(), "");
                if (strPoolKeyName != null)
                {
                    if (poolKey != null && strPoolKeyName.equals(poolKey))
                    {
                        DebugManager
                                .trace(
                                       _moduleName,
                                       _className,
                                       strMethodName,
                                       "set updateMap to true because (poolKey != null && strPoolKeyName.equals(poolKey))",
                                       Thread.currentThread().getName(), "");
                        updateMap = true;
                    }

                    /*
                     * if (strPoolKeyName.equals(DEFAULT_POOL_KEY)) updateMap =
                     * true;
                     */
                    if (poolKey == null
                            && strPoolKeyName.equals(DEFAULT_POOL_KEY))
                    {
                        DebugManager
                                .trace(
                                       _moduleName,
                                       _className,
                                       strMethodName,
                                       "set updateMap to true because (poolKey == null && strPoolKeyName.equals(DEFAULT_POOL_KEY))",
                                       Thread.currentThread().getName(), "");
                        updateMap = true;
                    }
                }
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "updateMap = " + updateMap, Thread
                                           .currentThread().getName(), "");
                if (updateMap)
                {
                    DebugManager.trace(_moduleName, _className, strMethodName,
                                       "Put incomingNo = " + incomingNo
                                               + " into maps", Thread
                                               .currentThread().getName(), "");
                    map.put(applicationID + "\"" + key, new MCMContext(
                            incomingNo, lTS, i));
                    map1.put(applicationID + "\"" + key, incomingNo);
                }
            }
            rs.close();

            MCMContext context = (MCMContext) map
                    .get(appID + "\"" + contextKey);
            if (context != null)
            {
                incomingNo = context._number;
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "updating context data, number = "
                                           + incomingNo, Thread.currentThread()
                                           .getName(), "");
                strSQL = "UPDATE MULTIMODAL_CONTEXT_NON_SUBS SET CONTEXT_DATA = '"
                        + contextData
                        + "', TS = "
                        + System.currentTimeMillis()
                        + " WHERE NON_SUBSCRIBER_PHONE_NUMBER = '"
                        + callerNumber
                        + "' AND CONTEXT_MODE_ID = '"
                        + context._number + "'";
                iAffectedRows = MultimodalDBServices.executeUpdate(con, strSQL);
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "No of rows updated = " + iAffectedRows,
                                   Thread.currentThread().getName(), "");
            }
            else
            {
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "map.size() = " + map.size(), Thread
                                           .currentThread().getName(), "");
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "vPhoneNos.size() = " + vPhoneNos.size(),
                                   Thread.currentThread().getName(), "");
                if (map.size() < vPhoneNos.size())
                {
                    DebugManager
                            .trace(
                                   _moduleName,
                                   _className,
                                   strMethodName,
                                   "All the called numbers are NOT used up for this caller number",
                                   Thread.currentThread().getName(), "");
                    for (i = 0; i < vPhoneNos.size(); i++)
                    {
                        if (!map1.containsValue(vPhoneNos.elementAt(i)))
                            break;
                    }
                    incomingNo = (String) vPhoneNos.elementAt(i);
                    DebugManager.trace(_moduleName, _className, strMethodName,
                                       incomingNo + " available", Thread
                                               .currentThread().getName(), "");
                    strSQL = "INSERT INTO MULTIMODAL_CONTEXT_NON_SUBS VALUES ('V','"
                            + incomingNo
                            + "', '"
                            + callerNumber
                            + "', '"
                            + appID
                            + "', '"
                            + contextKey
                            + "', '"
                            + contextData
                            + "', '0', "
                            + System.currentTimeMillis() + ")";
                    iAffectedRows = MultimodalDBServices.executeUpdate(con,
                                                                       strSQL);
                    DebugManager
                            .trace(_moduleName, _className, strMethodName,
                                   "No of rows inserted = " + iAffectedRows,
                                   Thread.currentThread().getName(), "");
                }
                else
                {
                    DebugManager
                            .trace(
                                   _moduleName,
                                   _className,
                                   strMethodName,
                                   "All the called numbers are used up for this caller number. Will now replace oldest context",
                                   Thread.currentThread().getName(), "");
                    long minTS = -1;
                    String oldestNumber = "";
                    Collection col = map.values();
                    Iterator itr = col.iterator();
                    while (itr.hasNext())
                    {
                        MCMContext c = (MCMContext) itr.next();
                        if ((minTS == -1) || (c._lTS < minTS))
                        {
                            minTS = c._lTS;
                            oldestNumber = c._number;
                        }
                    }
                    DebugManager.trace(_moduleName, _className, strMethodName,
                                       "oldestNumber = " + oldestNumber
                                               + ", minTS = " + minTS, Thread
                                               .currentThread().getName(), "");
                    DebugManager.trace(_moduleName, _className, strMethodName,
                                       "updating context data, number = "
                                               + oldestNumber, Thread
                                               .currentThread().getName(), "");
                    strSQL = "UPDATE MULTIMODAL_CONTEXT_NON_SUBS SET CONTEXT_KEY = '"
                            + contextKey
                            + "', CONTEXT_DATA = '"
                            + contextData
                            + "', TS = "
                            + System.currentTimeMillis()
                            + " WHERE NON_SUBSCRIBER_PHONE_NUMBER = '"
                            + callerNumber
                            + "' AND CONTEXT_MODE_ID = '"
                            + oldestNumber + "'";
                    iAffectedRows = MultimodalDBServices.executeUpdate(con,
                                                                       strSQL);
                    DebugManager.trace(_moduleName, _className, strMethodName,
                                       "No of rows updated = " + iAffectedRows,
                                       Thread.currentThread().getName(), "");
                    incomingNo = oldestNumber;
                }
            }
            MultimodalDBServices.releaseConnection(con);
        }
        catch (Exception e)
        {
            DebugManager.exception(_moduleName, _className, strMethodName, e,
                                   Thread.currentThread().getName(), "");
            incomingNo = null;
        }
        DebugManager.trace(_moduleName, _className, strMethodName,
                           "Return with incomingNo = " + incomingNo, Thread
                                   .currentThread().getName(), "");
        return incomingNo;
    }

    /**
     * Voice platform retrieving the context for a call that has come in
     * 
     * @param callerNo caller number
     * @param calledNo called number
     * 
     * @return Nnull if there is no entry found for the combination. Otherwise a
     *         HashMap populated with appID, Context key and Context data
     */
    public HashMap retrieveContext(String callerNo, String calledNo)
    {
        String strMethodName = "retrieveContext";
        DebugManager.trace(_moduleName, _className, strMethodName,
                           "Entry with callerNo = " + callerNo
                                   + " and calledNo = " + calledNo, Thread
                                   .currentThread().getName(), "");
        if (!_bInitializeCalled)
        {
            DebugManager.warning(_moduleName, _className, strMethodName,
                                 EXCEPTION_NOT_INITIALIZED, Thread
                                         .currentThread().getName(), "");
            return null;
        }
        if (!_bNumbersAvail)
        {
            DebugManager.warning(_moduleName, _className, strMethodName,
                                 "Number pool empty", Thread.currentThread()
                                         .getName(), "");
            return null;
        }
        callerNo = callerNo.toUpperCase();
        try
        {
            Connection con = MultimodalDBServices.getDBConnection(_connString);
            String strSQL = "SELECT APP_ID, CONTEXT_KEY, CONTEXT_DATA FROM MULTIMODAL_CONTEXT_NON_SUBS WHERE NON_SUBSCRIBER_PHONE_NUMBER = '"
                    + callerNo + "' AND CONTEXT_MODE_ID='" + calledNo + "'";
            ResultSet rs = MultimodalDBServices
                    .executeSingletonSQL(con, strSQL);
            HashMap appContext = null;
            while (rs.next())
            {
                appContext = new HashMap();
                appContext.put(MCM_APP_ID, rs.getString("APP_ID"));
                appContext.put(MCM_CONTEXT_KEY, rs.getString("CONTEXT_KEY"));
                appContext.put(MCM_CONTEXT_DATA, rs.getString("CONTEXT_DATA"));
            }
            rs.close();
            MultimodalDBServices.releaseConnection(con);
            return appContext;
        }
        catch (Exception e)
        {
            DebugManager.exception(_moduleName, _className, strMethodName, e,
                                   Thread.currentThread().getName(), "");
        }
        return null;
    }

    /**
     * Application removing a voice context for a subcriber
     * 
     * @param callerNo caller number
     * @param appId Application Id
     * @param contextKey Context key
     * 
     * @return true if an entry is found for the combination and is successfully
     *         deleted; false in all other cases.
     */
    public boolean removeContext(String callerNo, String appID,
            String contextKey)
    {
        String strMethodName = "removeContext";
        if (!_bInitializeCalled)
        {
            DebugManager.warning(_moduleName, _className, strMethodName,
                                 EXCEPTION_NOT_INITIALIZED, Thread
                                         .currentThread().getName(), "");
            return false;
        }
        DebugManager.trace(_moduleName, _className, strMethodName,
                           "Entry with callerNo=" + callerNo + ",appID="
                                   + appID + ",contextKey=" + contextKey,
                           Thread.currentThread().getName(), "");
        callerNo = callerNo.toUpperCase();
        appID = appID.toUpperCase();
        try
        {
            Connection con = MultimodalDBServices.getDBConnection(_connString);
            String strSQL = "DELETE FROM MULTIMODAL_CONTEXT_NON_SUBS WHERE NON_SUBSCRIBER_PHONE_NUMBER = '"
                    + callerNo
                    + "' AND APP_ID = '"
                    + appID
                    + "' AND CONTEXT_KEY = '" + contextKey + "'";
            int iAffectedRows = MultimodalDBServices.executeUpdate(con, strSQL);
            DebugManager.trace(_moduleName, _className, strMethodName,
                               "No of rows deleted = " + iAffectedRows, Thread
                                       .currentThread().getName(), "");
            MultimodalDBServices.releaseConnection(con);
            if (iAffectedRows == 0)
            {
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "No entry found for callerNo - " + callerNo
                                           + ", appID - " + appID
                                           + ", contextKey - " + contextKey,
                                   Thread.currentThread().getName(), "");
                return false;
            }
            return true;
        }
        catch (Exception e)
        {
            DebugManager.exception(_moduleName, _className, strMethodName, e,
                                   Thread.currentThread().getName(), "");
        }
        return false;
    }
}

class MCMContext
{
    public String _number;
    public long _lTS;
    public int _iIndex;

    public MCMContext(String number, long lTS, int iIndex)
    {
        _number = number;
        _lTS = lTS;
        _iIndex = iIndex;
    }
}

class MultimodalDBServices
{
    private static final String _moduleName = "IVM-Multimodal";
    private static final String _className = "MultimodalDBServices";
    private static Connection _con = null;

    public static Connection getDBConnection(String connString)
    {
        String strMethodName = "getDBConnection";
        try
        {
            if (_con == null)
            {
                /*
                 * Class.forName("com.sap.dbtech.jdbc.DriverSapDB"); _con =
                 * DriverManager .getConnection("jdbc:sapdb://" + connString);
                 */
                _con = OnMobileDBServices.getDBConnection(connString);
                DebugManager.trace(_moduleName, _className, strMethodName,
                                   "Created connection " + _con, Thread
                                           .currentThread().getName(), "");
            }
            return _con;
        }
        catch (Exception e)
        {
            DebugManager.exception(_moduleName, _className, strMethodName, e,
                                   Thread.currentThread().getName(), "");
        }
        return null;
    }

    public static ResultSet executeSingletonSQL(Connection con, String sql)
    {
        String strMethodName = "executeSingletonSQL";
        DebugManager.trace(_moduleName, _className, strMethodName, sql, Thread
                .currentThread().getName(), "");
        try
        {
            Statement sm = null;
            ResultSet rs = null;
            sm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                                     ResultSet.CONCUR_READ_ONLY);
            rs = sm.executeQuery(sql);
            return new MultimodalResultSet(sm, rs);
        }
        catch (Exception e)
        {
            DebugManager.exception(_moduleName, _className, strMethodName, e,
                                   Thread.currentThread().getName(), "");
        }
        return null;
    }

    public static int executeUpdate(Connection con, String sql)
    {
        String strMethodName = "executeUpdate";
        DebugManager.trace(_moduleName, _className, strMethodName, sql, Thread
                .currentThread().getName(), "");
        try
        {
            Statement sm = null;
            int i = -2862;
            sm = con.createStatement();
            i = sm.executeUpdate(sql);
            sm.close();
            return i;
        }
        catch (Exception e)
        {
            DebugManager.exception(_moduleName, _className, strMethodName, e,
                                   Thread.currentThread().getName(), "");
        }
        return -2862;
    }

    public static void releaseConnection(Connection con)
    {
        try
        {
            OnMobileDBServices.releaseConnection(con);
            _con = null;
        }
        catch (Exception e)
        {
            DebugManager.exception(_moduleName, _className,
                                   "releaseConnection", e, Thread
                                           .currentThread().getName(), "");
        }
    }

}

class MultimodalResultSet implements ResultSet
{
    private Statement sm;
    private ResultSet rs;

    public MultimodalResultSet(Statement sm, ResultSet rs)
    {
        this.sm = sm;
        this.rs = rs;
    }

    @Override
	public void close() throws SQLException
    {
        rs.close();
        sm.close();
    }

    @Override
	public boolean next() throws SQLException
    {
        return rs.next();
    }

    @Override
	public boolean wasNull() throws SQLException
    {
        return rs.wasNull();
    }

    @Override
	public String getString(int columnIndex) throws SQLException
    {
        String str = rs.getString(columnIndex);
        return str;
    }

    @Override
	public boolean getBoolean(int columnIndex) throws SQLException
    {
        return rs.getBoolean(columnIndex);
    }

    @Override
	public byte getByte(int columnIndex) throws SQLException
    {
        return rs.getByte(columnIndex);
    }

    @Override
	public short getShort(int columnIndex) throws SQLException
    {
        return rs.getShort(columnIndex);
    }

    @Override
	public int getInt(int columnIndex) throws SQLException
    {
        return rs.getInt(columnIndex);
    }

    @Override
	public long getLong(int columnIndex) throws SQLException
    {
        return rs.getLong(columnIndex);
    }

    @Override
	public float getFloat(int columnIndex) throws SQLException
    {
        return rs.getFloat(columnIndex);
    }

    @Override
	public double getDouble(int columnIndex) throws SQLException
    {
        return rs.getDouble(columnIndex);
    }

    @Override
    @Deprecated
	public BigDecimal getBigDecimal(int columnIndex, int scale)
            throws SQLException
    {
        return rs.getBigDecimal(columnIndex, scale);
    }

    @Override
	public byte[] getBytes(int columnIndex) throws SQLException
    {
        return rs.getBytes(columnIndex);
    }

    @Override
	public java.sql.Date getDate(int columnIndex) throws SQLException
    {
        return rs.getDate(columnIndex);
    }

    @Override
	public java.sql.Time getTime(int columnIndex) throws SQLException
    {
        return rs.getTime(columnIndex);
    }

    @Override
	public java.sql.Timestamp getTimestamp(int columnIndex) throws SQLException
    {
        return rs.getTimestamp(columnIndex);
    }

    @Override
	public java.io.InputStream getAsciiStream(int columnIndex)
            throws SQLException
    {
        return rs.getAsciiStream(columnIndex);
    }

    @Override
    @Deprecated
	public java.io.InputStream getUnicodeStream(int columnIndex)
            throws SQLException
    {
        return rs.getUnicodeStream(columnIndex);
    }

    @Override
	public java.io.InputStream getBinaryStream(int columnIndex)
            throws SQLException
    {
        return rs.getBinaryStream(columnIndex);
    }

    @Override
	public String getString(String columnName) throws SQLException
    {
        String str = rs.getString(columnName);
        return str;
    }

    @Override
	public boolean getBoolean(String columnName) throws SQLException
    {
        return rs.getBoolean(columnName);
    }

    @Override
	public byte getByte(String columnName) throws SQLException
    {
        return rs.getByte(columnName);
    }

    @Override
	public short getShort(String columnName) throws SQLException
    {
        return rs.getShort(columnName);
    }

    @Override
	public int getInt(String columnName) throws SQLException
    {
        return rs.getInt(columnName);
    }

    @Override
	public long getLong(String columnName) throws SQLException
    {
        return rs.getLong(columnName);
    }

    @Override
	public float getFloat(String columnName) throws SQLException
    {
        return rs.getFloat(columnName);
    }

    @Override
	public double getDouble(String columnName) throws SQLException
    {
        return rs.getDouble(columnName);
    }

    @Override
    @Deprecated
	public BigDecimal getBigDecimal(String columnName, int scale)
            throws SQLException
    {
        return rs.getBigDecimal(columnName, scale);
    }

    @Override
	public byte[] getBytes(String columnName) throws SQLException
    {
        return rs.getBytes(columnName);
    }

    @Override
	public java.sql.Date getDate(String columnName) throws SQLException
    {
        return rs.getDate(columnName);
    }

    @Override
	public java.sql.Time getTime(String columnName) throws SQLException
    {
        return rs.getTime(columnName);
    }

    @Override
	public java.sql.Timestamp getTimestamp(String columnName)
            throws SQLException
    {
        return rs.getTimestamp(columnName);
    }

    @Override
	public java.io.InputStream getAsciiStream(String columnName)
            throws SQLException
    {
        return rs.getAsciiStream(columnName);
    }

    @Override
    @Deprecated
	public java.io.InputStream getUnicodeStream(String columnName)
            throws SQLException
    {
        return rs.getUnicodeStream(columnName);
    }

    @Override
	public java.io.InputStream getBinaryStream(String columnName)
            throws SQLException
    {
        return rs.getBinaryStream(columnName);
    }

    @Override
	public SQLWarning getWarnings() throws SQLException
    {
        return rs.getWarnings();
    }

    @Override
	public void clearWarnings() throws SQLException
    {
        rs.clearWarnings();
    }

    @Override
	public String getCursorName() throws SQLException
    {
        return rs.getCursorName();
    }

    @Override
	public ResultSetMetaData getMetaData() throws SQLException
    {
        return rs.getMetaData();
    }

    @Override
	public Object getObject(int columnIndex) throws SQLException
    {
        return rs.getObject(columnIndex);
    }

    @Override
	public Object getObject(String columnName) throws SQLException
    {
        return rs.getObject(columnName);
    }

    @Override
	public int findColumn(String columnName) throws SQLException
    {
        return rs.findColumn(columnName);
    }

    @Override
	public java.io.Reader getCharacterStream(int columnIndex)
            throws SQLException
    {
        return rs.getCharacterStream(columnIndex);
    }

    @Override
	public java.io.Reader getCharacterStream(String columnName)
            throws SQLException
    {
        return rs.getCharacterStream(columnName);
    }

    @Override
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException
    {
        return rs.getBigDecimal(columnIndex);
    }

    @Override
	public BigDecimal getBigDecimal(String columnName) throws SQLException
    {
        return rs.getBigDecimal(columnName);
    }

    @Override
	public boolean isBeforeFirst() throws SQLException
    {
        return rs.isBeforeFirst();
    }

    @Override
	public boolean isAfterLast() throws SQLException
    {
        return rs.isAfterLast();
    }

    @Override
	public boolean isFirst() throws SQLException
    {
        return rs.isFirst();
    }

    @Override
	public boolean isLast() throws SQLException
    {
        return rs.isLast();
    }

    @Override
	public void beforeFirst() throws SQLException
    {
        rs.beforeFirst();
    }

    @Override
	public void afterLast() throws SQLException
    {
        rs.afterLast();
    }

    @Override
	public boolean first() throws SQLException
    {
        return rs.first();
    }

    @Override
	public boolean last() throws SQLException
    {
        return rs.last();
    }

    @Override
	public int getRow() throws SQLException
    {
        return rs.getRow();
    }

    @Override
	public boolean absolute(int row) throws SQLException
    {
        return rs.absolute(row);
    }

    @Override
	public boolean relative(int rows) throws SQLException
    {
        return rs.relative(rows);
    }

    @Override
	public boolean previous() throws SQLException
    {
        return rs.previous();
    }

    @Override
	public void setFetchDirection(int direction) throws SQLException
    {
        rs.setFetchDirection(direction);
    }

    @Override
	public int getFetchDirection() throws SQLException
    {
        return rs.getFetchDirection();
    }

    @Override
	public void setFetchSize(int rows) throws SQLException
    {
        rs.setFetchSize(rows);
    }

    @Override
	public int getFetchSize() throws SQLException
    {
        return rs.getFetchSize();
    }

    @Override
	public int getType() throws SQLException
    {
        return rs.getType();
    }

    @Override
	public int getConcurrency() throws SQLException
    {
        return rs.getConcurrency();
    }

    @Override
	public boolean rowUpdated() throws SQLException
    {
        return rs.rowUpdated();
    }

    @Override
	public boolean rowInserted() throws SQLException
    {
        return rs.rowInserted();
    }

    @Override
	public boolean rowDeleted() throws SQLException
    {
        return rs.rowDeleted();
    }

    @Override
	public void updateNull(int columnIndex) throws SQLException
    {
        rs.updateNull(columnIndex);
    }

    @Override
	public void updateBoolean(int columnIndex, boolean x) throws SQLException
    {
        rs.updateBoolean(columnIndex, x);
    }

    @Override
	public void updateByte(int columnIndex, byte x) throws SQLException
    {
        rs.updateByte(columnIndex, x);
    }

    @Override
	public void updateShort(int columnIndex, short x) throws SQLException
    {
        rs.updateShort(columnIndex, x);
    }

    @Override
	public void updateInt(int columnIndex, int x) throws SQLException
    {
        rs.updateInt(columnIndex, x);
    }

    @Override
	public void updateLong(int columnIndex, long x) throws SQLException
    {
        rs.updateLong(columnIndex, x);
    }

    @Override
	public void updateFloat(int columnIndex, float x) throws SQLException
    {
        rs.updateFloat(columnIndex, x);
    }

    @Override
	public void updateDouble(int columnIndex, double x) throws SQLException
    {
        rs.updateDouble(columnIndex, x);
    }

    @Override
	public void updateBigDecimal(int columnIndex, BigDecimal x)
            throws SQLException
    {
        rs.updateBigDecimal(columnIndex, x);
    }

    @Override
	public void updateString(int columnIndex, String x) throws SQLException
    {
        rs.updateString(columnIndex, x);
    }

    @Override
	public void updateBytes(int columnIndex, byte x[]) throws SQLException
    {
        rs.updateBytes(columnIndex, x);
    }

    @Override
	public void updateDate(int columnIndex, java.sql.Date x)
            throws SQLException
    {
        rs.updateDate(columnIndex, x);
    }

    @Override
	public void updateTime(int columnIndex, java.sql.Time x)
            throws SQLException
    {
        rs.updateTime(columnIndex, x);
    }

    @Override
	public void updateTimestamp(int columnIndex, java.sql.Timestamp x)
            throws SQLException
    {
        rs.updateTimestamp(columnIndex, x);
    }

    @Override
	public void updateAsciiStream(int columnIndex, java.io.InputStream x,
            int length) throws SQLException
    {
        rs.updateAsciiStream(columnIndex, x, length);
    }

    @Override
	public void updateBinaryStream(int columnIndex, java.io.InputStream x,
            int length) throws SQLException
    {
        rs.updateBinaryStream(columnIndex, x, length);
    }

    @Override
	public void updateCharacterStream(int columnIndex, java.io.Reader x,
            int length) throws SQLException
    {
        rs.updateCharacterStream(columnIndex, x, length);
    }

    @Override
	public void updateObject(int columnIndex, Object x, int scale)
            throws SQLException
    {
        rs.updateObject(columnIndex, x, scale);
    }

    @Override
	public void updateObject(int columnIndex, Object x) throws SQLException
    {
        rs.updateObject(columnIndex, x);
    }

    @Override
	public void updateNull(String columnName) throws SQLException
    {
        rs.updateNull(columnName);
    }

    @Override
	public void updateBoolean(String columnName, boolean x) throws SQLException
    {
        rs.updateBoolean(columnName, x);
    }

    @Override
	public void updateByte(String columnName, byte x) throws SQLException
    {
        rs.updateByte(columnName, x);
    }

    @Override
	public void updateShort(String columnName, short x) throws SQLException
    {
        rs.updateShort(columnName, x);
    }

    @Override
	public void updateInt(String columnName, int x) throws SQLException
    {
        rs.updateInt(columnName, x);
    }

    @Override
	public void updateLong(String columnName, long x) throws SQLException
    {
        rs.updateLong(columnName, x);
    }

    @Override
	public void updateFloat(String columnName, float x) throws SQLException
    {
        rs.updateFloat(columnName, x);
    }

    @Override
	public void updateDouble(String columnName, double x) throws SQLException
    {
        rs.updateDouble(columnName, x);
    }

    @Override
	public void updateBigDecimal(String columnName, BigDecimal x)
            throws SQLException
    {
        rs.updateBigDecimal(columnName, x);
    }

    @Override
	public void updateString(String columnName, String x) throws SQLException
    {
        rs.updateString(columnName, x);
    }

    @Override
	public void updateBytes(String columnName, byte x[]) throws SQLException
    {
        rs.updateBytes(columnName, x);
    }

    @Override
	public void updateDate(String columnName, java.sql.Date x)
            throws SQLException
    {
        rs.updateDate(columnName, x);
    }

    @Override
	public void updateTime(String columnName, java.sql.Time x)
            throws SQLException
    {
        rs.updateTime(columnName, x);
    }

    @Override
	public void updateTimestamp(String columnName, java.sql.Timestamp x)
            throws SQLException
    {
        rs.updateTimestamp(columnName, x);
    }

    @Override
	public void updateAsciiStream(String columnName, java.io.InputStream x,
            int length) throws SQLException
    {
        rs.updateAsciiStream(columnName, x, length);
    }

    @Override
	public void updateBinaryStream(String columnName, java.io.InputStream x,
            int length) throws SQLException
    {
        rs.updateBinaryStream(columnName, x, length);
    }

    @Override
	public void updateCharacterStream(String columnName, java.io.Reader reader,
            int length) throws SQLException
    {
        rs.updateCharacterStream(columnName, reader, length);
    }

    @Override
	public void updateObject(String columnName, Object x, int scale)
            throws SQLException
    {
        rs.updateObject(columnName, x, scale);
    }

    @Override
	public void updateObject(String columnName, Object x) throws SQLException
    {
        rs.updateObject(columnName, x);
    }

    @Override
	public void insertRow() throws SQLException
    {
        rs.insertRow();
    }

    @Override
	public void updateRow() throws SQLException
    {
        rs.updateRow();
    }

    @Override
	public void deleteRow() throws SQLException
    {
        rs.deleteRow();
    }

    @Override
	public void refreshRow() throws SQLException
    {
        rs.refreshRow();
    }

    @Override
	public void cancelRowUpdates() throws SQLException
    {
        rs.cancelRowUpdates();
    }

    @Override
	public void moveToInsertRow() throws SQLException
    {
        rs.moveToInsertRow();
    }

    @Override
	public void moveToCurrentRow() throws SQLException
    {
        rs.moveToCurrentRow();
    }

    @Override
	public Statement getStatement() throws SQLException
    {
        return rs.getStatement();
    }

    @Override
	public Ref getRef(int i) throws SQLException
    {
        return rs.getRef(i);
    }

    @Override
	public Blob getBlob(int i) throws SQLException
    {
        return rs.getBlob(i);
    }

    @Override
	public Clob getClob(int i) throws SQLException
    {
        return rs.getClob(i);
    }

    @Override
	public Array getArray(int i) throws SQLException
    {
        return rs.getArray(i);
    }

    @Override
	public Ref getRef(String colName) throws SQLException
    {
        return rs.getRef(colName);
    }

    @Override
	public Blob getBlob(String colName) throws SQLException
    {
        return rs.getBlob(colName);
    }

    @Override
	public Clob getClob(String colName) throws SQLException
    {
        return rs.getClob(colName);
    }

    @Override
	public Array getArray(String colName) throws SQLException
    {
        return rs.getArray(colName);
    }

    @Override
	public java.sql.Date getDate(int columnIndex, Calendar cal)
            throws SQLException
    {
        return rs.getDate(columnIndex, cal);
    }

    @Override
	public java.sql.Date getDate(String columnName, Calendar cal)
            throws SQLException
    {
        return rs.getDate(columnName, cal);
    }

    @Override
	public java.sql.Time getTime(int columnIndex, Calendar cal)
            throws SQLException
    {
        return rs.getTime(columnIndex, cal);
    }

    @Override
	public java.sql.Time getTime(String columnName, Calendar cal)
            throws SQLException
    {
        return rs.getTime(columnName, cal);
    }

    @Override
	public java.sql.Timestamp getTimestamp(int columnIndex, Calendar cal)
            throws SQLException
    {
        return rs.getTimestamp(columnIndex, cal);
    }

    @Override
	public java.sql.Timestamp getTimestamp(String columnName, Calendar cal)
            throws SQLException
    {
        return rs.getTimestamp(columnName, cal);
    }

    @Override
	public URL getURL(int columnIndex) throws SQLException
    {
        return rs.getURL(columnIndex);
    }

    // JDBC 3.0 features.
    @Override
	public URL getURL(String columnName) throws SQLException
    {
        return rs.getURL(columnName);
    }

    @Override
	public void updateRef(int columnIndex, Ref x) throws SQLException
    {
        rs.updateRef(columnIndex, x);
    }

    @Override
	public void updateRef(String columnName, Ref x) throws SQLException
    {
        rs.updateRef(columnName, x);
    }

    @Override
	public void updateBlob(int columnIndex, Blob x) throws SQLException
    {
        rs.updateBlob(columnIndex, x);
    }

    @Override
	public void updateBlob(String columnName, Blob x) throws SQLException
    {
        rs.updateBlob(columnName, x);
    }

    @Override
	public void updateClob(int columnIndex, Clob x) throws SQLException
    {
        rs.updateClob(columnIndex, x);
    }

    @Override
	public void updateClob(String columnName, Clob x) throws SQLException
    {
        rs.updateClob(columnName, x);
    }

    @Override
	public void updateArray(int columnIndex, Array x) throws SQLException
    {
        rs.updateArray(columnIndex, x);
    }

    @Override
	public void updateArray(String columnName, Array x) throws SQLException
    {
        rs.updateArray(columnName, x);
    }

	/* (non-Javadoc)
	 * @see java.sql.Wrapper#unwrap(java.lang.Class)
	 */
	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException
	{
		return rs.unwrap(iface);
	}

	/* (non-Javadoc)
	 * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
	 */
	@Override
	public boolean isWrapperFor(Class<?> iface) throws SQLException
	{
		return rs.isWrapperFor(iface);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(int, java.util.Map)
	 */
	@Override
	public Object getObject(int columnIndex, Map<String, Class<?>> map)
			throws SQLException
	{
		return rs.getObject(columnIndex, map);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getObject(java.lang.String, java.util.Map)
	 */
	@Override
	public Object getObject(String columnLabel, Map<String, Class<?>> map)
			throws SQLException
	{
		return rs.getObject(columnLabel, map);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRowId(int)
	 */
	@Override
	public RowId getRowId(int columnIndex) throws SQLException
	{
		return rs.getRowId(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getRowId(java.lang.String)
	 */
	@Override
	public RowId getRowId(String columnLabel) throws SQLException
	{
		return rs.getRowId(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateRowId(int, java.sql.RowId)
	 */
	@Override
	public void updateRowId(int columnIndex, RowId x) throws SQLException
	{
		rs.updateRowId(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateRowId(java.lang.String, java.sql.RowId)
	 */
	@Override
	public void updateRowId(String columnLabel, RowId x) throws SQLException
	{
		rs.updateRowId(columnLabel, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getHoldability()
	 */
	@Override
	public int getHoldability() throws SQLException
	{
		return rs.getHoldability();
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#isClosed()
	 */
	@Override
	public boolean isClosed() throws SQLException
	{
		return rs.isClosed();
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNString(int, java.lang.String)
	 */
	@Override
	public void updateNString(int columnIndex, String nString)
			throws SQLException
	{
		rs.updateNString(columnIndex, nString);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNString(java.lang.String, java.lang.String)
	 */
	@Override
	public void updateNString(String columnLabel, String nString)
			throws SQLException
	{
		rs.updateNString(columnLabel, nString);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.sql.NClob)
	 */
	@Override
	public void updateNClob(int columnIndex, NClob nClob) throws SQLException
	{
		rs.updateNClob(columnIndex, nClob);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.sql.NClob)
	 */
	@Override
	public void updateNClob(String columnLabel, NClob nClob)
			throws SQLException
	{
		rs.updateNClob(columnLabel, nClob);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNClob(int)
	 */
	@Override
	public NClob getNClob(int columnIndex) throws SQLException
	{
		return rs.getNClob(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNClob(java.lang.String)
	 */
	@Override
	public NClob getNClob(String columnLabel) throws SQLException
	{
		return rs.getNClob(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getSQLXML(int)
	 */
	@Override
	public SQLXML getSQLXML(int columnIndex) throws SQLException
	{
		return rs.getSQLXML(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getSQLXML(java.lang.String)
	 */
	@Override
	public SQLXML getSQLXML(String columnLabel) throws SQLException
	{
		return rs.getSQLXML(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateSQLXML(int, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException
	{
		rs.updateSQLXML(columnIndex, xmlObject);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateSQLXML(java.lang.String, java.sql.SQLXML)
	 */
	@Override
	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException
	{
		rs.updateSQLXML(columnLabel, xmlObject);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNString(int)
	 */
	@Override
	public String getNString(int columnIndex) throws SQLException
	{
		return rs.getNString(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNString(java.lang.String)
	 */
	@Override
	public String getNString(String columnLabel) throws SQLException
	{
		return rs.getNString(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNCharacterStream(int)
	 */
	@Override
	public Reader getNCharacterStream(int columnIndex) throws SQLException
	{
		return rs.getCharacterStream(columnIndex);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#getNCharacterStream(java.lang.String)
	 */
	@Override
	public Reader getNCharacterStream(String columnLabel) throws SQLException
	{
		return rs.getNCharacterStream(columnLabel);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException
	{
		rs.updateNCharacterStream(columnIndex, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException
	{
		rs.updateNCharacterStream(columnLabel, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException
	{
		rs.updateAsciiStream(columnIndex, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException
	{
		rs.updateBinaryStream(columnIndex, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException
	{
		rs.updateCharacterStream(columnIndex, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException
	{
		rs.updateAsciiStream(columnLabel, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException
	{
		rs.updateBinaryStream(columnLabel, x, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException
	{
		rs.updateCharacterStream(columnLabel, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream, long)
	 */
	@Override
	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException
	{
		rs.updateBlob(columnIndex, inputStream, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream, long)
	 */
	@Override
	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException
	{
		rs.updateBlob(columnLabel, inputStream, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException
	{
		rs.updateClob(columnIndex, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException
	{
		rs.updateClob(columnLabel, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException
	{
		rs.updateNClob(columnIndex, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader, long)
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException
	{
		rs.updateNClob(columnLabel, reader, length);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException
	{
		rs.updateNCharacterStream(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException
	{
		rs.updateNCharacterStream(columnLabel, reader);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(int, java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException
	{
		rs.updateAsciiStream(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(int, java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException
	{
		rs.updateBinaryStream(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(int, java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException
	{
		rs.updateCharacterStream(columnIndex, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateAsciiStream(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException
	{
		rs.updateAsciiStream(columnLabel, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBinaryStream(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException
	{
		rs.updateBinaryStream(columnLabel, x);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateCharacterStream(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException
	{
		rs.updateCharacterStream(columnLabel, reader);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(int, java.io.InputStream)
	 */
	@Override
	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException
	{
		rs.updateBlob(columnIndex, inputStream);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateBlob(java.lang.String, java.io.InputStream)
	 */
	@Override
	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException
	{
		rs.updateBlob(columnLabel, inputStream);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(int, java.io.Reader)
	 */
	@Override
	public void updateClob(int columnIndex, Reader reader) throws SQLException
	{
		rs.updateClob(columnIndex, reader);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateClob(String columnLabel, Reader reader)
			throws SQLException
	{
		rs.updateClob(columnLabel, reader);
		
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(int, java.io.Reader)
	 */
	@Override
	public void updateNClob(int columnIndex, Reader reader) throws SQLException
	{
		rs.updateNClob(columnIndex, reader);
	}

	/* (non-Javadoc)
	 * @see java.sql.ResultSet#updateNClob(java.lang.String, java.io.Reader)
	 */
	@Override
	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException
	{
		rs.updateNClob(columnLabel, reader);
		
	}
}