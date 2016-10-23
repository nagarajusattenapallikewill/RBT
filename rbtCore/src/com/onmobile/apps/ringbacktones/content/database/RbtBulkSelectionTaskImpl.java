package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.RbtBulkSelectionTask;

/* Class added by the Eswar 29 oct 2008 */
public class RbtBulkSelectionTaskImpl extends RBTPrimitive implements RbtBulkSelectionTask
{
	private static Logger logger = Logger.getLogger(RbtBulkSelectionTaskImpl.class);
	
	private static final String TABLE_NAME = "RBT_BULK_SELECTION_TASKS";
	private static final String FILE_ID_COL = "FILEID";
	private static final String FILE_NAME_COL = "FILENAME";
	private static final String ACTIVATED_BY_COL = "ACTIVATED_BY";
	private static final String ACTIVATION_CLASS_COL = "ACTIVATION_CLASS";
	private static final String SELECTION_CLASS_COL = "SELECTION_CLASS";
	private static final String FILE_STATUS_COL = "FILE_STATUS";
	private static final String UPLOADED_DATE_COL = "UPLOADED_DATE";
	private static final String PROCESSED_DATE_COL = "PROCESSEDDATE";
	private static final String ACTIVATION_INFO_COL = "ACTINFO";
	private static String m_databaseType=getDBSelectionString();
	private int fileID;
	private String fileName;
	private String activatedBy;
	private String activationClass;
	private String selectionClass;
	private String fileStatus;
	private Date uploadedDate;
	private Date processedDate;
	private String activationInfo;

	//private static String m_databaseType = getDBSelectionString();

	private RbtBulkSelectionTaskImpl(String filename,String act_By,
		String activ_Class,String selec_Class)
	{
		fileName = filename;
		act_By = activatedBy;
		activationClass = activ_Class;
		selectionClass = selec_Class;
	}

	private RbtBulkSelectionTaskImpl(int fileId,String filename,
		String actBy,String activClass,String selecClass,String filestatus,Date uploadDate,Date processeddate,String actInfo)
	{
		fileID = fileId;
		fileName = filename;
		activatedBy = actBy;
		activationClass = activClass;
		selectionClass = selecClass;
		fileStatus = filestatus;
		uploadedDate = uploadDate;
		processedDate = processeddate;
		activationInfo = actInfo;
	}

	public int fileID()
	{
		return fileID;
	}

	public String fileName()
	{
		return fileName;
	}

	public String act_By()
	{
		return activatedBy;
	}
	
	public String activation_Class()
	{
		return activationClass;
	}
	
	public String selection_Class()
	{
		return selectionClass;
	}	

	public String file_Status()
	{
		return fileStatus;
	}
	
	public Date uploaded_Date()
	{
		return uploadedDate;
	}

	public Date processed_Date()
	{
		return processedDate;
	}
	
	public String activation_Info()
	{
		return activationInfo;
	}

	static String insert(Connection con,String fileName,String actBy,String subStrClass,String selStrClass,String actInfo)
	{
		System.out.println("Entered actual Insert::RbtBulkSelectionTaskImpl");
		logger.info("RBT::: inside Insert");

		StringBuffer query = new StringBuffer("");
		Statement stmt = null;
		boolean success = false;
	Date uploadTime=new Date(System.currentTimeMillis());
		String uplaodDate=sqlTime(uploadTime);
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			uplaodDate = "SYSDATE()";
			if(uploadTime != null)
				uplaodDate = mySQLDateTime(uploadTime);
			
		}
		
		StringBuffer result = new StringBuffer("RESULT:");

		query.append("INSERT INTO "+TABLE_NAME + " ("+ FILE_NAME_COL);
		query.append(","+ ACTIVATED_BY_COL);
		query.append(","+ ACTIVATION_CLASS_COL);
		query.append(","+ SELECTION_CLASS_COL);
		query.append(","+ FILE_STATUS_COL);
		query.append(","+ UPLOADED_DATE_COL);
		query.append(","+ ACTIVATION_INFO_COL);
		query.append(")");
		
		query.append(" VALUES ("+ sqlString(fileName));
		query.append(","+ sqlString(actBy));
		query.append(","+ sqlString(subStrClass));
		query.append(","+ sqlString(selStrClass));
		query.append(","+ sqlString("A"));

		query.append(","+uplaodDate);
		query.append(","+sqlString(actInfo));
		query.append(")");
		
		logger.info("RBT:Query "+ query.toString());

		try{
			logger.info(  "RBT::inside try block");
			System.out.println(query);
			stmt = con.createStatement();
			if(stmt.executeUpdate(query.toString())>0)
			{
				result.append("SUCCESS");
				success = true;
			}
			else
			{
				result.append("FAILURE");
			}
		}
		catch(SQLException sqle)
		{
			logger.error("", sqle);
			return null;
		}
		finally{
			try
			{
				stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(!success)
		{
			return result.toString();
		}
		return result.toString();
	}

	static String delete(Connection con,int fileID)
	{
		logger.info("RBT::inside deleteBulkSelectionTask");

		String query = null;
		Statement stmt = null;
		
		StringBuffer result=new StringBuffer("RESULT:");

		query="DELETE FROM "+TABLE_NAME+" WHERE ";
		query+="FILEID="+sqlInt(fileID);

		logger.info("RBT::query"+query);

		try{
			logger.info("RBT:: Inside try Block");
			stmt = con.createStatement();
			int rowsupdated = stmt.executeUpdate(query);
			if(rowsupdated==1)
			{
				result.append("SUCCESS");
			}
			else
			{
				result.append("FAILURE");
			}
		} catch(SQLException sqle) {
			logger.error("", sqle);
		}
		finally {
			try{
			
				stmt.close();

			} catch(SQLException sqle) {
				
					logger.error("", sqle);
			}
		}
		return result.toString();
	}

	static String updateProcessTime(Connection con,String filename)
	{
		logger.info("RBT:: inside updateStatus of file");

		String query = null;
		Statement stmt = null;
		StringBuffer result = new StringBuffer("RESULT:");
		Date uploadTime=new Date(System.currentTimeMillis());
		String uplaodDate=sqlTime(uploadTime);
		if(!m_databaseType.equalsIgnoreCase(DB_SAPDB))
		{
			uplaodDate = "SYSDATE()";
			if(uploadTime != null)
				uplaodDate = mySQLDateTime(uploadTime);
			
		}
		query = "UPDATE "+TABLE_NAME+" SET ";
		query+= PROCESSED_DATE_COL+"="+uplaodDate;
		query+= " WHERE "+FILE_NAME_COL+"="+sqlString(filename);

		logger.info("RBT::query"+query);

		try
		{		
			logger.info("RBT:: Inside Try Block");
			stmt = con.createStatement();
			int rowsupdated = stmt.executeUpdate(query);
			if(rowsupdated == 1 )
			{
				result.append("SUCCESS");
			}
			else
			{
				result.append("FAILURE");
			}
		}catch(SQLException sqle){
			logger.error("", sqle);
		}
		finally {
			try{
				stmt.close();
			} catch(SQLException sqle) {
				logger.error("", sqle);
			}
		}

		return result.toString();
	}

	static String updateStatus(Connection con,String filename,String status)
	{
		logger.info("RBT:: inside updateStatus of file");

		String query = null;
		Statement stmt = null;
		StringBuffer result = new StringBuffer("RESULT:");

		query = "UPDATE "+TABLE_NAME+" SET ";
		query+= FILE_STATUS_COL+"="+sqlString(status); 
		query+= " WHERE "+FILE_NAME_COL+"="+sqlString(filename);
		
		logger.info("RBT::query"+query);
		
		try{
			logger.info("RBT:: Inside try Block");
			stmt = con.createStatement();
			int rowsupdated = stmt.executeUpdate(query);
			if(rowsupdated== 1)
			{
				result.append("SUCCESS");
			}
			else
			{
				result.append("FAILURE");
			}
		}catch(SQLException sqle) {
			logger.error("", sqle);
		}
		finally {
			try{
			
				stmt.close();

			} catch(SQLException sqle) {
				logger.error("", sqle);
			}
		}
		return result.toString();
	}
	
	static String updateActInfo(Connection con,String filename,String actInfo)
	{
		logger.info("RBT:: inside updateStatus of file");

		String query = null;
		Statement stmt = null;
		StringBuffer result = new StringBuffer("RESULT:");

		query = "UPDATE "+TABLE_NAME+" SET ";
		query+= ACTIVATION_INFO_COL+"="+sqlString(actInfo); 
		query+= " WHERE "+FILE_NAME_COL+"="+sqlString(filename);
		
		logger.info("RBT::query"+query);
		
		try{
			logger.info("RBT:: Inside try Block");
			stmt = con.createStatement();
			int rowsupdated = stmt.executeUpdate(query);
			if(rowsupdated== 1)
			{
				result.append("SUCCESS");
			}
			else
			{
				result.append("FAILURE");
			}
		}catch(SQLException sqle) {
			logger.error("", sqle);
		}
		finally {
			try{
			
				stmt.close();

			} catch(SQLException sqle) {
				logger.error("", sqle);
			}
		}
		return result.toString();
	}


	static RbtBulkSelectionTask[] getBulkSelectionTasks(Connection con)
	{
		logger.info("RBT::inside getBulkSelectionTasks");

		String query = null;
		Statement stmt = null;
		ResultSet results = null;

		int fileId;
		String fileName;
		String actBy;
		String activ_Class;
		String selec_Class;
		String file_Status;
		Date uploadDate;
		Date processedDate;
		String actInfo;
		
		List bulkSelTaskList = new ArrayList();

		RbtBulkSelectionTaskImpl rbtBulkSelTask = null;
		
		query = "SELECT * FROM " + TABLE_NAME + " ORDER BY "+UPLOADED_DATE_COL+ " DESC";

		logger.info("RBT::query "+query);
		
		try{
			logger.info("RBT:: Inside Try Block");
			stmt = con.createStatement();
			results = stmt.executeQuery(query);

			while(results.next())
			{
				fileId = results.getInt(FILE_ID_COL);
				fileName = results.getString(FILE_NAME_COL);
				actBy = results.getString(ACTIVATED_BY_COL);
				activ_Class = results.getString(ACTIVATION_CLASS_COL);
				selec_Class = results.getString(SELECTION_CLASS_COL);
				file_Status = results.getString(FILE_STATUS_COL);
				uploadDate = results.getTimestamp(UPLOADED_DATE_COL);
				processedDate = results.getTimestamp(PROCESSED_DATE_COL);
				actInfo = results.getString(ACTIVATION_INFO_COL);
				//Date setTime = results.getTimestamp(SET_TIME_COL);
				rbtBulkSelTask = new RbtBulkSelectionTaskImpl(fileId,fileName,actBy,activ_Class,selec_Class,file_Status,uploadDate,processedDate,actInfo);
				bulkSelTaskList.add(rbtBulkSelTask);
			}
		} catch(SQLException sqle)
		{
			logger.error("", sqle);
		}
		finally {
			try
			{
				stmt.close();
			}
			catch(Exception e)
			{
				logger.error("", e);
			}
		}
		if(bulkSelTaskList.size() > 0)
		{
			logger.info("RBT::retrieving records from RBT_BULK_SELECTION_TASKS successful");
			return (RbtBulkSelectionTask[])bulkSelTaskList.toArray(new RbtBulkSelectionTask[0]);
		} 
		else
		{
			logger.info("RBT::no records in RBT_BULK_SELECTION_TASKS");
			return null;
		}
	}

	static String getStatus(Connection con,String filename)
	{
		logger.info("RBT::inside getStatus");

		String query = null;
		Statement stmt = null;
		ResultSet result = null;
		String status = new String();

		query = "SELECT FILE_STATUS FROM "+TABLE_NAME;
		query+= " WHERE FILENAME="+sqlString(filename);

		logger.info("RBT::Query"+query);
		try{
			logger.info("RBT::inside try Block");
			stmt = con.createStatement();
			result = stmt.executeQuery(query);
			if(result.first())
			{
				status = result.getString(FILE_STATUS_COL);
			}
			System.out.println("File Status"+status);
		} catch(SQLException sqle) {
			logger.error("", sqle);
		}
		finally {
			try{
				stmt.close();
			}
			catch(SQLException sqe)
			{
				logger.error("", sqe);
			}
		}
		return status;
	}

	static String getFilename(Connection con,int fileid)
	{
		logger.info("RBT::inside getFilename");

		String query = null;
		Statement stmt = null;
		ResultSet result = null;
		StringBuffer resFileName = new StringBuffer("");

		query = "SELECT FILENAME FROM "+TABLE_NAME;
		query+= " WHERE FILEID="+sqlInt(fileid);

		logger.info("RBT::query"+query);
		try{
			logger.info("RBT::inside try block");
			stmt = con.createStatement();
			result = stmt.executeQuery(query);
			if(result.first())
			{
			resFileName.append(result.getString(FILE_NAME_COL));
			}
			System.out.println("Filename::"+resFileName);
		} catch(SQLException sqle) {
			logger.error("", sqle);
		}	
		finally {
			try{
				stmt.close();
			}
			catch(SQLException sqe)
			{
				logger.error("", sqe);
			}
		}
		return resFileName.toString();
	}

	static String getActInfo(Connection con,String filename)
	{
		logger.info("RBT::inside getActInfo");

		String query = null;
		Statement stmt = null;
		ResultSet result = null;
		String actInfo=null;

		query = "SELECT "+ACTIVATION_INFO_COL+" FROM "+TABLE_NAME;
		query+= " WHERE FILENAME="+sqlString(filename);

		logger.info("RBT::query"+query);
		try{
			logger.info("RBT::inside try block");
			stmt = con.createStatement();
			result = stmt.executeQuery(query);
			if(result.first())
			{
				actInfo =result.getString(ACTIVATION_INFO_COL);
				System.out.println("Activation Info::"+actInfo);
			}
				
		} catch(SQLException sqle) {
			logger.error("", sqle);
		}	
		finally {
			try{
				stmt.close();
			}
			catch(SQLException sqe)
			{
				logger.error("", sqe);
			}
		}
		return actInfo;
	}

}
