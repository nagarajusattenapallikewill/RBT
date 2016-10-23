package com.onmobile.apps.ringbacktones.daemons.reminder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberDownloads;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.content.database.TnbSubscriberImpl;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;


public class ChangeTNBOptInToNormal {

	private static Logger logger = Logger.getLogger(ChangeTNBOptInToNormal.class);
	private	List<String> m_tnbSubClassesList = new ArrayList<String>();
	private Map<String,String> m_tnbUpgradeSubClass = new HashMap<String, String>();
	private Connection connection = null;
	private Statement statement = null;
	private ResultSet resultSet = null;
	
	ParametersCacheManager m_rbtParamCacheManager = null;
	
	
	private ChangeTNBOptInToNormal() {
		m_rbtParamCacheManager =  CacheManagerUtil.getParametersCacheManager();
		
		String tnbSubscriptionClasses = getParamAsString("COMMON", "TNB_SUBSCRIPTION_CLASSES", "ZERO");
    	m_tnbSubClassesList = Arrays.asList(tnbSubscriptionClasses.toUpperCase().split(",")); 
    	
    	String tnbUpgradeSubClass = getParamAsString("COMMON", "TNB_UPGRADE_SUBSCRIPTION_CLASSES", "ZERO");
    	List<String> tnbSubUpgradeClassesList = Arrays.asList(tnbUpgradeSubClass.toUpperCase().split(","));
    	
    	for(String temp : tnbSubUpgradeClassesList) {
    		String[] split = temp.split("\\:");
			if(split == null || split.length != 2 ) {
				continue;
			}				
			m_tnbUpgradeSubClass.put(split[0],split[1]);
    	}    	
	}
	
	public static void main(String args[]) {
		ChangeTNBOptInToNormal obj = new ChangeTNBOptInToNormal();	
		obj.startProcess();
	}
	
	private void startProcess()
	{
		try
		{
			RBTDBManager rbtDBManager = RBTDBManager.getInstance();
			String sqlQuery = "SELECT * FROM RBT_TNB_SUBSCRIBER";
			connection = rbtDBManager.getConnection();
			logger.info("Connection successfully created");
			statement = connection.createStatement();
			logger.info("Query: " + sqlQuery);
			resultSet = statement.executeQuery(sqlQuery);
			
			while(resultSet.next())
			{
				try
				{
					TnbSubscriberImpl tnbSubscriber = TnbSubscriberImpl.getNextTnbSubscriber(resultSet); 
					changeTNBToNormal(tnbSubscriber);
					logger.info("Successfully processed TNB Subscriber: " + tnbSubscriber.seqID());
				}
				catch(Exception e)
				{
				}
			}
		}
		catch(Throwable e)
		{
			logger.error("Exception before release connection", e);
		}
		finally
		{
			releaseConnection(connection, statement, resultSet);
		}
	}
	
	private void changeTNBToNormal(TnbSubscriberImpl tnbSubscriberImpl) throws Exception{
		
		String subId = tnbSubscriberImpl.subID();
		Subscriber subscriber = DBConfigTools.rbtDBManager.getSubscriber(subId);
		if(subscriber == null) {
			return;
		}
		if(!m_tnbSubClassesList.contains(subscriber.subscriptionClass())) {
			logger.info("Subcriber " + subscriber.subID() + " not a TNB Subscriber");
			return;
		}
		
		boolean isNonFreeSelectionExists = false;
		if(DBConfigTools.getParameter(ConstantsTools.SMS, ConstantsTools.UPDATE_TNB_TO_NORMAL_ON_DEFAULT_SELECTION, false)) 
		{	
			if (DBConfigTools.getParameter(ConstantsTools.COMMON,ConstantsTools.ADD_TO_DOWNLOADS,false))
			{
				SubscriberDownloads[] subDownloads = DBConfigTools.rbtDBManager
						.getNonFreeDownloads(subId, RBTParametersUtils
								.getParamAsString("DAEMON",
										"TNB_FREE_CHARGE_CLASS",
										ConstantsTools.FREE));
				if (subDownloads != null && subDownloads.length > 0)
					isNonFreeSelectionExists = true;
			}
			else
			{
				SubscriberStatus[] subSel = DBConfigTools.rbtDBManager
						.getNonFreeSelections(subId, RBTParametersUtils
								.getParamAsString("DAEMON",
										"TNB_FREE_CHARGE_CLASS",
										ConstantsTools.FREE));
				if (subSel != null && subSel.length > 0)
					isNonFreeSelectionExists = true;
			}
		}
		if (isNonFreeSelectionExists)
		{
			logger.info("Subscriber " + subscriber.subID() + " has charge selection");
			String initSubClass = subscriber.subscriptionClass();
			String finalSubClass = m_tnbUpgradeSubClass.get(initSubClass);
			if(finalSubClass == null) {
				return;
			}
			
			if(DBConfigTools.rbtDBManager.convertSubscriptionType(subscriber.subID(), initSubClass, finalSubClass, subscriber)){			
				String unsubscriptionMsg = DBConfigTools.getSmsText(ConstantsTools.SMS, ConstantsTools.UPGRADE_TNB_MSG, subscriber.language());
				Tools.sendSMS(DBConfigTools.getParameter(ConstantsTools.SMS,ConstantsTools.SMS_NO,"123456"), subscriber.subID(), unsubscriptionMsg, false);
				DBConfigTools.rbtDBManager.deleteTNBSubscriber(subscriber.subID());
				logger.info("Subscriber " + subscriber.subID() + " following this successfully done, sub class updated, successfully send sms, removed from TNB Subscriber");
			}
		}
		Calendar calendar = Calendar.getInstance();
		calendar.set(2037, Calendar.JANUARY, 1, 0, 0, 0);
		Date endDate = calendar.getTime();
		if(DBConfigTools.rbtDBManager.updateEndDate(subscriber.subID(), endDate, null)) {
			logger.info("Subscriber " + subscriber.subID() + " end time successfully updated");
		}
	}
	
	private String getParamAsString(String type, String param, String defaultVal)
    {
    	try{
    		return m_rbtParamCacheManager.getParameter(type, param, defaultVal).getValue();
    	}catch(Exception e){
    		logger.warn("Unable to get param ->"+param +"  type ->"+type);
    		return defaultVal;
    	}
    }
	private static boolean releaseConnection(Connection conn, Statement stmt, ResultSet rs)
	{
		try
		{
			if(rs != null)
				rs.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing resultSet", e);
		}
		
		try
		{
			if(stmt != null)
				stmt.close();
		}
		catch(Throwable e)
		{
			logger.error("Exception in closing statement", e);
		}
		
		return RBTDBManager.getInstance().releaseConnection(conn);
	}
}
