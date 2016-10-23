package com.onmobile.apps.ringbacktones.hunterFramework;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;

/**
 * This class represents a progressive publisher that works on database as its 
 * data source.
 * 
 * @author nandakishore
 *
 */
public abstract class ProgressiveSqlQueryPublisher extends ProgressivePublisher
{
	private static Logger logger = Logger.getLogger(ProgressiveSqlQueryPublisher.class);
    /**
     * The db connection object used to execute the present query.
     * The db connection is procured on every query execution the publisher 
     * goes to wait/sleep state on various scenarios during when the connection 
     * object will starve. 
     */
    private Connection connection = null;
    /**
     * The statement for the present executing query
     */
    private Statement statement = null;
    
    /**
     * The resultset of the present executing query.
     */
    private ResultSet rset = null;

    /**
     * This method gets a connection, creates a statement and executes the select
     * query. The result set is preserved, through which queue components are 
     * retrived. Normally each and every row in the resultset gets converted
     * to a queue component.
     */
    @Override
    public void executeQuery(int count) throws QueryException
    {
        String sqlQuery = getSqlQuery(count);
        logger.info(getUniqueName()+" sql="+sqlQuery);
        connection = getConnection();
        try
        {
            statement = connection.createStatement();
//            if(!RBTCommonConfig.getInstance().getDbSelectionString().equals(DBHandler.DB_SAPDB))
//            {
//                statement = connection.createStatement(java.sql.ResultSet.TYPE_FORWARD_ONLY,
//                  java.sql.ResultSet.CONCUR_READ_ONLY);
//                statement.setFetchSize(Integer.MIN_VALUE);
//            }
//            else
//                statement = connection.createStatement();
            rset = statement.executeQuery(sqlQuery);
        }
        catch (SQLException e)
        {
            logger.error("", e);
            throw new QueryException(e);
        }
    }

    /**
     * Close rset, statement and release connection.
     */
    @Override
    public void finaliseQuery()
    {
        try
        {
            rset.close();
        }
        catch (Exception e)
        {
        }
        try
        {
            statement.close();
        }
        catch (Exception e)
        {
        }
        try
        {
            releaseConnection(connection);
        }
        catch (Exception e)
        {
        }
    }

    /**
     * @return Get the connection from the connection pool.
     * @throws QueryException
     */
    abstract protected Connection getConnection() throws QueryException;

    public ResultSet getRset()
    {
        return rset;
    }

    abstract protected String getSqlQuery(int count);

    public Statement getStatement()
    {
        return statement;
    }

    /**
     * Check with the result set if there are any more results to lookup.
     */
    @Override
    public boolean hasMoreQueueComponents() throws QueryException
    {
        try
        {
            return rset != null && rset.next();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new QueryException(e);
        }
    }

    /**
     * @param connection - Connection to be released back to the connection pool
     * @throws RBTException
     */
    abstract protected void releaseConnection(Connection connection) throws RBTException;

    public void setRset(ResultSet rset)
    {
        this.rset = rset;
    }

    public void setStatement(Statement statement)
    {
        this.statement = statement;
    }

}
