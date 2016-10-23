package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall;

import java.util.Calendar;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.daemons.tcp.RbtTCPServer;
import com.onmobile.apps.ringbacktones.webservice.actions.GetCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongBean;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.MemcacheClientForCurrentPlayingSong;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.SendHandler;

public class RBTRealTimeSongInfoService {
	private static final Logger logger = Logger
			.getLogger(RBTChargePerCallService.class);

	public void process(CurrentPlayingSongBean bean) {
		if (bean != null) {
			logger.info("called: " + bean.getCalledId() + ", caller: "
					+ bean.getCallerId() + ", wavefile: " + bean.getWavFileName() + ", categoryId: " + bean.getCategoryId()); 
			try {
				Calendar cal = Calendar.getInstance();
				cal.add(Calendar.SECOND, RbtTCPServer.expiryInSeconds);
				 SendHandler.addToMemcache(bean, cal);
			} catch (Exception e) {
				logger.debug("Exeception occured whil uploading into memcahe");
			} catch (Throwable e) {
				logger.debug("Exeception:" + e);
			}
		} else {
			logger.info("calledId is null. Not inserted into memcache.");
		}
	}
}
