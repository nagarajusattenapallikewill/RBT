package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;

import a.a.a.b.n;

public class EphemeralSelectionD2CMigration extends RBTDBManager implements ID2CMigration{
	

	private static Logger logger = Logger.getLogger(TimeBasedSelectionD2CMigration.class);

	public SubscriberStatus[] getSelection(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			SubscriberStatus[] dayOfTheWeekStatus = SubscriberStatusImpl.getActiveSelectionsByStatus(conn, subscriberID, 200);
			List<SubscriberStatus> activeStatus = new ArrayList<SubscriberStatus>();
			if(dayOfTheWeekStatus == null || dayOfTheWeekStatus.length < 1){
				return null;
			}
			for (SubscriberStatus subscriberStatus : dayOfTheWeekStatus) {
				String selSatus = subscriberStatus.selStatus();
				String callerId = subscriberStatus.callerID();
				if(!(callerId != null && callerId.startsWith("G")) && !(selSatus.equalsIgnoreCase("X"))){
					activeStatus.add(subscriberStatus);
				}
					
			}
			
			return  activeStatus.toArray(new SubscriberStatus[activeStatus.size()]);
			
		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}



}
