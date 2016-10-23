package com.onmobile.apps.ringbacktones.daemons.reminder;

import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.content.database.TnbSubscriberImpl;
import com.onmobile.apps.ringbacktones.content.database.TrialSelectionImpl;
import com.onmobile.apps.ringbacktones.hunterFramework.HunterException;
import com.onmobile.apps.ringbacktones.hunterFramework.ProgressiveSqlQueryPublisher;
import com.onmobile.apps.ringbacktones.hunterFramework.QueryException;
import com.onmobile.apps.ringbacktones.hunterFramework.QueueComponent;
import com.onmobile.apps.ringbacktones.tools.ConstantsTools;
import com.onmobile.apps.ringbacktones.tools.DBConfigTools;
import com.onmobile.apps.ringbacktones.tools.XMLConfigTools;
import com.onmobile.common.db.OnMobileDBServices;
import com.onmobile.common.exception.OnMobileException;

public class ReminderPublisher extends ProgressiveSqlQueryPublisher implements ConstantsTools{
	
	private static Logger logger = Logger.getLogger(ReminderPublisher.class);

	public String chargePack = null;
	public String circleId = null;
	public int iterId = 0;
	public int smsDay = 0;
	public String reminderType = null;
	public int subscriptionDays = 0;
	static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	public ReminderPublisher(String reminderType, String chargePack, String circleId, int iterId, int smsDay, int subscriptionDays)
	{
		logger.info("Making ReminderPublisher with reminderType="+reminderType+", chargePack="+chargePack+", circleId="+circleId+"," +
				" smsDay="+smsDay);
		this.reminderType = reminderType;
		this.chargePack = chargePack;
		this.circleId = circleId;
		this.iterId = iterId;
		this.smsDay = smsDay;
		this.subscriptionDays = subscriptionDays;
	}

	@Override
	protected Connection getConnection() throws QueryException
	{
		try
        {
            return OnMobileDBServices.getDBConnection();
        }
        catch (OnMobileException e)
        {
        	logger.error("", e);
            throw new QueryException(e);
        }
    }

	@Override
	protected String getSqlQuery(int count)
	{
		makeThreadSleep();
		String query = null;
		Calendar cal = Calendar.getInstance();
		logger.info("cal="+cal);
		if(smsDay != -1)
			cal.add(Calendar.DAY_OF_YEAR, -smsDay);
		else
			cal.add(Calendar.DAY_OF_YEAR, -subscriptionDays);
		Date startDate = cal.getTime();
		String dateStr = sdf.format(startDate);
		logger.info("smsDay="+smsDay+", dateStr="+dateStr);
		
		if(!reminderType.equals(TRIAL))
		{
			if(smsDay == -1)
			{
				if(XMLConfigTools.getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
					query = "SELECT * FROM RBT_TNB_SUBSCRIBER WHERE SEQ_ID > " + getPresentSequenceId() + " AND " +
							"TO_CHAR(START_DATE,'YYYY-MM-DD') <= '"+dateStr+"'  AND CHARGE_PACK = '"+ chargePack +"' "
						+ " AND ITER_ID <= "+ iterId + " AND CIRCLE_ID = '" + circleId+"' AND ROWNUM < "+count;
				else
					query = "SELECT * FROM RBT_TNB_SUBSCRIBER WHERE SEQ_ID > " + getPresentSequenceId() + " AND " +
					"DATE(START_DATE) <= '"+ dateStr +"' AND CHARGE_PACK = '"+ chargePack +"' "
				+ " AND ITER_ID <= "+ iterId + " AND CIRCLE_ID = '" + circleId+"' LIMIT "+ count;
				
			}
			else
			{
				if(XMLConfigTools.getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
					query = "SELECT * FROM RBT_TNB_SUBSCRIBER WHERE SEQ_ID > " + getPresentSequenceId() + " AND " +
							"TO_CHAR(START_DATE,'YYYY-MM-DD') = '"+dateStr+"'  AND CHARGE_PACK = '"+ chargePack +"' "
						+ " AND ITER_ID = "+ iterId + " AND CIRCLE_ID = '" + circleId+"' AND ROWNUM < "+count;
				else
					query = "SELECT * FROM RBT_TNB_SUBSCRIBER WHERE SEQ_ID > " + getPresentSequenceId() + " AND " +
					"DATE(START_DATE) = '"+ dateStr +"' AND CHARGE_PACK = '"+ chargePack +"' "
				+ " AND ITER_ID = "+ iterId + " AND CIRCLE_ID = '" + circleId+"' LIMIT "+ count;
					
			}
			
		}
		else
		{
			if(smsDay == -1)
			{
				if(XMLConfigTools.getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
					query = "SELECT * FROM RBT_TRIAL_SELECTION WHERE SEQ_ID > " + getPresentSequenceId() + " AND " +
							"TO_CHAR(START_DATE,'YYYY-MM-DD') <= '"+dateStr+"'  AND CHARGE_PACK = '"+ chargePack +"' "
						+ " AND ITER_ID <= "+ iterId + " AND CIRCLE_ID = '" + circleId+"' AND ROWNUM < "+count;
				else
					query = "SELECT * FROM RBT_TRIAL_SELECTION WHERE SEQ_ID > " + getPresentSequenceId() + " AND " +
					"DATE(START_DATE) <= '"+ dateStr +"' AND CHARGE_PACK = '"+ chargePack +"' "
				+ " AND ITER_ID <= "+ iterId + " AND CIRCLE_ID = '" + circleId+"' LIMIT "+ count;
			
			}
			else
			{
				if(XMLConfigTools.getDBSelectionString().equalsIgnoreCase(DB_SAPDB))
					query = "SELECT * FROM RBT_TRIAL_SELECTION WHERE SEQ_ID > " + getPresentSequenceId() + " AND " +
							"TO_CHAR(START_DATE,'YYYY-MM-DD') = '"+dateStr+"'  AND CHARGE_PACK = '"+ chargePack +"' "
						+ " AND ITER_ID = "+ iterId + " AND CIRCLE_ID = '" + circleId+"' AND ROWNUM < "+count;
				else
					query = "SELECT * FROM RBT_TRIAL_SELECTION WHERE SEQ_ID > " + getPresentSequenceId() + " AND " +
					"DATE(START_DATE) = '"+ dateStr +"' AND CHARGE_PACK = '"+ chargePack +"' "
				+ " AND ITER_ID = "+ iterId + " AND CIRCLE_ID = '" + circleId+"' LIMIT "+ count;
				
			}	
		}	
		
		return query;
	}
	
	@Override
	protected void releaseConnection(Connection connection) throws RBTException
	{
		try
        {
            OnMobileDBServices.releaseConnection(connection);
        }
        catch (OnMobileException e)
        {
        	logger.error("", e);
            throw new RBTException(e.getMessage());
        }
	}

	@Override
	protected QueueComponent getNextQueueComponent() throws HunterException
	{
		try
        {
            ReminderQueueComponent result = null;
            if(reminderType.equals(TNB_OPTIN))
            {
            	TnbSubscriberImpl tnbSubscriberImpl = TnbSubscriberImpl.getNextTnbSubscriber(getRset());
                result = new TNBOptinQueueComponent(tnbSubscriberImpl);
            }
            else if(reminderType.equals(TNB_OPTOUT))
            {
            	TnbSubscriberImpl tnbSubscriberImpl = TnbSubscriberImpl.getNextTnbSubscriber(getRset());
                result = new TNBOptoutQueueComponent(tnbSubscriberImpl);
            }
            else
            {
            	TrialSelectionImpl trialSelectionImpl = TrialSelectionImpl.getNextTnbSubscriber(getRset());
                result = new TrialQueueComponent(trialSelectionImpl);
            }
            
            result.setSmsDay(smsDay);
            return result;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new QueryException(e);
        }
    }

	@Override
	protected void setPresentQueryCount(int addCount, int count) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getWorkerThreadPriority() {
		// TODO Auto-generated method stub
		return 0;
	}
	private void makeThreadSleep()
	{
		int sleepSecs = DBConfigTools.getParameter(DAEMON, REM_PUB_SLEEP_SEC, 10);
		try
		{
			Thread.sleep(sleepSecs*1000);
		}
		catch(InterruptedException i)
		{
			logger.error("", i);
		}
		
	}

}
