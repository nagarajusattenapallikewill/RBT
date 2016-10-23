package com.onmobile.apps.ringbacktones.rbt2.command;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.IUDPDao;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPBean;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;

public class UDPFeatureRestrictCommand extends FeatureListRestrictionCommand implements iRBTConstant{

	private static Logger logger = Logger.getLogger(UDPFeatureRestrictCommand.class);
	
	@Override
	public void executeCalback(String msisdn) {

		logger.info("UDPFeatureRestrictCommand execute begins for subscriber: " + msisdn);
		
		//Remove all udp song selections
		
		SubscriberStatus[] subscriberSelections = RBTDBManager.getInstance().getAllActiveSubscriberSettings(msisdn);
		
		if(subscriberSelections != null && subscriberSelections.length != 0) {			
			for(SubscriberStatus subscriberSelection : subscriberSelections) {
				if(subscriberSelection.udpId() != null) {
					char oldLoopStatus = subscriberSelection.loopStatus();
					char newLoopStatus = LOOP_STATUS_EXPIRED_INIT;
					if (oldLoopStatus == LOOP_STATUS_EXPIRED)
						newLoopStatus = oldLoopStatus;
					else if (oldLoopStatus == LOOP_STATUS_OVERRIDE_INIT
							|| oldLoopStatus == LOOP_STATUS_LOOP_INIT)
						newLoopStatus = LOOP_STATUS_EXPIRED;
					
					RBTDBManager.getInstance().smSelectionActivationRenewalFailure(msisdn, subscriberSelection.refID(), 
							"DAEMON", null, subscriberSelection.classType(), newLoopStatus, subscriberSelection.selType(), 
							subscriberSelection.extraInfo(), subscriberSelection.circleId());
				}
			}
			
			logger.info("Directly deactivated all UDP song selecctions for the subscriber: " + msisdn);
		}

		//Remove UDP and UDP contents.
		IUDPDao udpDao = (IUDPDao) ConfigUtil.getBean(BeanConstant.UDP_DAO_IMPL);
		
		try {
			List<UDPBean> allUDP = udpDao.getAllUDP(msisdn, -1, -1);
			if(allUDP != null) {
				for(UDPBean udpBean : allUDP) {
					udpDao.deleteUDP(udpBean.getUdpId());
				}
			}
			
			logger.info("successfully removed all UDP for the subscriber: " + msisdn);
			
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			logger.error("Exception will remove all udp for subscriber: " + msisdn, e);
		}

		
		logger.info("UDPFeatureRestrictCommand execute ends for subscriber: " + msisdn);
	}

	@Override
	public String executeInlineCall(SelectionRequest selectionRequest, String clipID) {
		return null;
	}

}
