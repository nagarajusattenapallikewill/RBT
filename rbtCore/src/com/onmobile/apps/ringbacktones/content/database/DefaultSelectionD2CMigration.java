package com.onmobile.apps.ringbacktones.content.database;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;

public class DefaultSelectionD2CMigration extends RBTDBManager implements ID2CMigration {
	private static Logger logger = Logger.getLogger(DefaultSelectionD2CMigration.class);

	public SubscriberStatus[] getSelection(String subscriberID) {
		Connection conn = getConnection();
		if (conn == null)
			return null;
		try {

			SubscriberStatus[] status = SubscriberStatusImpl.getActiveSelectionsByStatus(conn, subscriberID, 1);
			List<SubscriberStatus> activeStatus = new ArrayList<SubscriberStatus>();
			if(status == null || status.length < 1 ){
				return null;
			}
			for (SubscriberStatus subscriberStatus : status) {
				String selSatus = subscriberStatus.selStatus();
				String callerId = subscriberStatus.callerID();
				int catType = subscriberStatus.categoryType();
				boolean isSuffleCat = false;
				String notAllowedCatagories = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON,
						"D2C_MIGRATION_NOT_ALLOWED_CATEGORY_TYPE", "");
				if(notAllowedCatagories != null){
					String notAllowedCat[] = notAllowedCatagories.split(",");
					if(notAllowedCat != null && notAllowedCat.length > 0){
						for (String categoryType : notAllowedCat) {
							if(categoryType.equals(catType)){
								isSuffleCat = true;
								break;
							}
						}
					}
				}
				if (!isSuffleCat && (callerId == null || callerId.equalsIgnoreCase("all"))
						&& !(selSatus.equalsIgnoreCase("X"))){
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
