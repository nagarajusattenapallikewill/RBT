package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;

public class ProfileSelectionD2CMigration extends RBTDBManager implements ID2CMigration {

	private static Logger logger = Logger.getLogger(ProfileSelectionD2CMigration.class);

	public SubscriberStatus[] getSelection(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {
			SubscriberStatus[] profileSel = SubscriberStatusImpl.getActiveSelectionsByStatus(conn, subscriberID, 99);
			List<SubscriberStatus> activeStatus = new ArrayList<SubscriberStatus>();
			if(profileSel == null || profileSel.length < 1){
				return null;
			}
			for (SubscriberStatus subscriberStatus : profileSel) {
				String selSatus = subscriberStatus.selStatus();
				String callerId = subscriberStatus.callerID();
				if (!(callerId != null && callerId.startsWith("G")) && !(selSatus.equalsIgnoreCase("X"))) {
					activeStatus.add(subscriberStatus);
				}

			}

			return activeStatus.toArray(new SubscriberStatus[activeStatus.size()]);

		} catch (Throwable e) {
			logger.error("Exception before release connection", e);
		} finally {
			releaseConnection(conn);
		}
		return null;
	}

}
