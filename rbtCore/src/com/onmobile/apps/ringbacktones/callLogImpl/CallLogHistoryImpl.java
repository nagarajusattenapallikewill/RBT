package com.onmobile.apps.ringbacktones.callLogImpl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import com.livewiremobile.store.storefront.dto.calllog.LogEntry;
import com.onmobile.apps.ringbacktones.callLog.CallLogHistory;
import com.onmobile.apps.ringbacktones.callLog.beans.CallLogHistoryBean;
import com.onmobile.apps.ringbacktones.callLog.beans.HelperCallLogBean;
import com.onmobile.apps.ringbacktones.callLog.utils.CallLogUtils;
import com.onmobile.apps.ringbacktones.content.database.dao.CallLogDAO;
import com.onmobile.apps.ringbacktones.v2.exception.UserException;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;

@Component
public class CallLogHistoryImpl implements CallLogHistory {
	private static Logger logger = Logger.getLogger(CallLogHistoryImpl.class);


	@Override
	public void save(CurrentPlayingSongBean currentPlayingSongBean, String callType ) {

		HelperCallLogBean helperCallLogBean = CallLogUtils.getHelperCallLogBean(currentPlayingSongBean,callType);
		logger.info("HelperCallLogBean: "+helperCallLogBean+" , type: "+callType);

		if (helperCallLogBean == null && (callType == null || callType.isEmpty()))
			return;

		saveCallLogHistory(helperCallLogBean,callType);

	}


	@Override
	public void saveCallLogHistory(HelperCallLogBean helperCallLogBean,String type) {
		CallLogDAO.save(helperCallLogBean, type);
	}


	@SuppressWarnings("unchecked")
	@Override
	public CallLogHistoryBean getCallLogHistory(String subscriberId,String callType,int pageNum,int pageSize) {

		List<Map<String,Object>> callLogHistory = CallLogDAO.get(subscriberId, callType);
		CallLogHistoryBean callLogHistoryBean = null;
		if (callLogHistory != null) {
			callLogHistoryBean  = CallLogUtils.getCallLogHistoryBean(callLogHistory, callType,pageNum,pageSize);
			System.out.println("CallLog:::: "+callLogHistoryBean);

		}

		return callLogHistoryBean;
	}





	public static void main(String[] args) {
		System.out.println("Starting Main");
		System.out.println("Starting Main");
		

	


		//CallLogHistoryBean historyBean = new CallLogHistoryBean(0, callLogs);
		CallLogHistoryImpl historyImpl = new CallLogHistoryImpl();
		
		HelperCallLogBean bean = new HelperCallLogBean(1, "8951317637", "7411604385", new Date(), "Test_meeting_rbt","Outgoing");
		historyImpl.saveCallLogHistory(bean, "callerId");
		CallLogHistoryBean callLogHistoryBean = historyImpl.getCallLogHistory("8951317637", "Outgoing", 0, 0);
		logger.info("CALL_LOG_HISTORY_BEAN::: "+callLogHistoryBean);
		//callLogHistoryBean = historyImpl.getCallLogHistory("7411604385", "Outgoing", 0, 0);
		//logger.info("CALL_LOG_HISTORY_BEAN::: "+callLogHistoryBean);
		/*AtomicInteger pageNum = new AtomicInteger(0);
		AtomicInteger pageSize = new AtomicInteger(0);
		System.out.println("Atomic:: "+pageNum+"  ,  "+pageSize);
		System.out.println(historyImpl.getCallLogHistory("8951317637", "ALL", 0, 0));
		System.out.println("Atomic:::: "+pageNum+"  ,  "+pageSize);*/
		//historyImpl.ge
		
		//CallLogDAO.deleteExpiredRecords(null, "");
		
		/*EntityBean bean = new EntityBean(1, "a", "b", "c", "d");

		Gson gson = new Gson();
		String json = gson.toJson(bean);

		List<String> list = new ArrayList<String>();
		list.add(json);
		System.out.println(list);
		EntityBean entityBean = gson.fromJson(list.get(0), EntityBean.class);
		System.out.println(entityBean);*/




	}


	@Override
	public LogEntry getCurrentPlayingSong(String subscriberID, String type) throws UserException {
		logger.warn("Method not implemented");
		return null;
	}

}
