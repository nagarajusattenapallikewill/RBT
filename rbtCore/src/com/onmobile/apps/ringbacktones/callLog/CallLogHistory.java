package com.onmobile.apps.ringbacktones.callLog;

import com.livewiremobile.store.storefront.dto.calllog.LogEntry;
import com.onmobile.apps.ringbacktones.callLog.beans.HelperCallLogBean;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;

public interface CallLogHistory {

	public void save(CurrentPlayingSongBean currentPlayingSongBean, String type );
	public void saveCallLogHistory(HelperCallLogBean helperCallLogBean,String type);
	public <T extends Object> T getCallLogHistory(String subscriberId,String callType,int offSet,int pageSize);
	public LogEntry getCurrentPlayingSong(String subscriberID, String type) throws UserException;
}
