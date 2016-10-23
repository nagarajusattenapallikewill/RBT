package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ContentRelease;

public class ContentReleaseImpl extends RBTPrimitive implements ContentRelease
{
	private static Logger logger = Logger.getLogger(ContentReleaseImpl.class);
	
    private static final String TABLE_NAME = "RBT_CONTENT_RELEASE";
	private static final String SITE_NAME_COL = "SITE_NAME";
    private static final String RELEASE_DATE_COL = "RELEASE_DATE";

    private String m_name;
    private Date m_releaseDate;

	private ContentReleaseImpl(String name, Date releaseDate)
	{
		m_name = name;
        m_releaseDate = releaseDate;
	}
	
	public String name()
    {
        return m_name;
    }
	
	public Date releaseDate()
	{
		return m_releaseDate;
	}

	static ContentRelease getLatestContentRelease(Connection conn)
    {
        logger.info("RBT::inside getLatestContentRelease");
        
      	String query = null;
		Statement stmt = null;
		ResultSet results = null;

		String name = null;
		Date releaseDate = null;
	
		ContentReleaseImpl contentRelease = null;

		query = "SELECT * FROM " + TABLE_NAME + " WHERE " + RELEASE_DATE_COL + " = ( SELECT MAX( " + RELEASE_DATE_COL + " ) FROM " + TABLE_NAME + " )";
		
		logger.info("RBT::query "+query);
		
        try
        {
            logger.info("RBT::inside try block");  
			stmt = conn.createStatement();
			results = stmt.executeQuery(query);
					while (results.next())
					{
						name = results.getString(SITE_NAME_COL);
						releaseDate = results.getTimestamp(RELEASE_DATE_COL);
		
						contentRelease = new ContentReleaseImpl(name, releaseDate);
					}
		}
        catch(SQLException se)
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
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
        return contentRelease;
    }
}