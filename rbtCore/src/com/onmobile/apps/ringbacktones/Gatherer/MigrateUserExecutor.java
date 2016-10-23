package com.onmobile.apps.ringbacktones.Gatherer;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.ProvisioningRequests;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.ID2CMigration;
import com.onmobile.apps.ringbacktones.content.database.ProvisioningRequestsDao;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.migration.B2BMigrationList;
import com.onmobile.apps.ringbacktones.rbt2.service.RBTOperatorUserDetailsMappingBean;
import com.onmobile.apps.ringbacktones.rbt2.service.impl.OperatorUserDetailsCallbackServiceImpl;
import com.onmobile.apps.ringbacktones.v2.dao.IMigrateUser;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTProvisioningRequests;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriber;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriberDetails;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTSubscriberSelection;

public class MigrateUserExecutor implements Runnable {
	private RBTDBManager rbtDBManager = RBTDBManager.getInstance();
	private static Logger logger = Logger.getLogger(MigrateUserExecutor.class);
	private String subscriberId = null;

	public MigrateUserExecutor(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	@Override
	public void run() {
		try {
			logger.info("Inside the executor");
			B2BMigrationList b2bList = (B2BMigrationList) ConfigUtil.getBean(BeanConstant.B2B_MIGRATION_LIST);
			if (b2bList == null) {
				return;
			}
			List<RBTSubscriberSelection> subscriberStatus = new ArrayList<RBTSubscriberSelection>();
			List<RBTProvisioningRequests> provisioningRequests = new ArrayList<RBTProvisioningRequests>();
			List<ID2CMigration> migrationsTypes = b2bList.getMigrationList();
			if (migrationsTypes == null || migrationsTypes.isEmpty()) {
				return;
			}
			for (ID2CMigration id2cMigration : migrationsTypes) {
				SubscriberStatus[] status = id2cMigration.getSelection(subscriberId);
				if (status != null && status.length > 0) {
					for (SubscriberStatus sel : status) {
						subscriberStatus.add(convertSubscriberSelection(sel));
					}
				}
			}
			List<ProvisioningRequests> provList = ProvisioningRequestsDao.getBySubscriberId(subscriberId);
			if (provList != null && !provList.isEmpty()) {
				for (ProvisioningRequests provisioningRequest : provList) {
					int status = provisioningRequest.getStatus();
					if (status == 30 || status == 31 || status == 32 || status == 33 || status == 34 || status == 35
							|| status == 36 || status == 41 || status == 42) {
						provisioningRequests.add(convertProvRequest(provisioningRequest));
					}
				}
			}

			Subscriber subscriber = rbtDBManager.getSubscriber(subscriberId);
			
			RBTSubscriberDetails rbtSubscriberDetails = new RBTSubscriberDetails(convertSubscriber(subscriber),
					subscriberStatus, provisioningRequests);
			IMigrateUser migrateUser = (IMigrateUser) ConfigUtil.getBean(BeanConstant.B2B_MIGRATION_IMPL);
			String response = migrateUser.migrateUser(rbtSubscriberDetails);
			if(response.equalsIgnoreCase("SUCCESS")){
				removingOperatorUserInfo(subscriberId);
			}
			
		} catch (Exception e) {
			logger.info("Exception Occured in MigrateUserExecutor" + e.getMessage());
		}
	}

	private void removingOperatorUserInfo(String subscriberId) {
		if (subscriberId == null || subscriberId.isEmpty()) {
			logger.error("subscriber cannot be null");
			return;
		}
		try {
			RBTOperatorUserDetailsMappingBean mappingBean = (RBTOperatorUserDetailsMappingBean) ConfigUtil
					.getBean(BeanConstant.RBT_OPERATOR_USER_DETAILS_MAPPING_BEAN);

			if (mappingBean != null) {
				OperatorUserDetailsCallbackServiceImpl callbackServiceImpl = (OperatorUserDetailsCallbackServiceImpl) mappingBean
						.getCallbackService();
				if (callbackServiceImpl != null) {
					callbackServiceImpl.removeOperatorUserInfo(subscriberId);
				}
			}
		} catch (Throwable e) {
			logger.error("Bean is not configured: " + e, e);
		}
	}

	private RBTSubscriber convertSubscriber(Subscriber subscriber) {

		RBTSubscriber rbtSubscriber = new RBTSubscriber(subscriberId, subscriber.activatedBy(),
				subscriber.deactivatedBy(), subscriber.startDate(), subscriber.endDate(),
				subscriber.prepaidYes() ? 'y' : 'n', subscriber.lastDeactivationDate(), subscriber.nextChargingDate(),
				subscriber.noOfAccess(), subscriber.activationInfo(), subscriber.subscriptionClass(),
				subscriber.subYes(), subscriber.lastDeactivationInfo(), subscriber.lastDeactivationDate(),
				subscriber.activationDate(), subscriber.oldClassType(), subscriber.maxSelections(), subscriber.cosID(),
				subscriber.activatedCosID(), subscriber.rbtType(), 'A', subscriber.language(), 0, "",
				subscriber.extraInfo(), subscriber.circleID(), subscriber.refID(), subscriber.retryCount(),
				subscriber.nextRetryTime(), subscriber.prismNextBillingDate());

		return rbtSubscriber;

	}

	private RBTSubscriberSelection convertSubscriberSelection(SubscriberStatus subscriberSelection) {
		RBTSubscriberSelection rbtSubscriberSelection = new RBTSubscriberSelection(subscriberId,
				subscriberSelection.callerID(), subscriberSelection.categoryID(), subscriberSelection.subscriberFile(),
				subscriberSelection.setTime(), subscriberSelection.endTime(), subscriberSelection.status(),
				subscriberSelection.classType(), subscriberSelection.selectedBy(), subscriberSelection.selectionInfo(),
				subscriberSelection.nextChargingDate(), subscriberSelection.prepaidYes() ? 'y' : 'n',
				subscriberSelection.fromTime(), subscriberSelection.toTime(),
				subscriberSelection.selStatus().toCharArray()[0], subscriberSelection.deSelectedBy(),
				subscriberSelection.oldClassType(), subscriberSelection.categoryType(),
				subscriberSelection.loopStatus(), subscriberSelection.selInterval(), subscriberSelection.refID(),
				subscriberSelection.extraInfo(), subscriberSelection.selType(), subscriberSelection.circleId(),
				subscriberSelection.retryCount(), subscriberSelection.nextRetryTime(), subscriberSelection.startTime());

		return rbtSubscriberSelection;
	}
	
	private RBTProvisioningRequests convertProvRequest(ProvisioningRequests provisioningRequests) {
		return new RBTProvisioningRequests(provisioningRequests.getRequestId(), provisioningRequests.getSubscriberId(),
				provisioningRequests.getType(), provisioningRequests.getStatus(), provisioningRequests.getExtraInfo(),
				provisioningRequests.getMode(), provisioningRequests.getModeInfo(), provisioningRequests.getTransId(),
				provisioningRequests.getChargingClass(), provisioningRequests.getCreationTime(),
				provisioningRequests.getNextRetryTime(), provisioningRequests.getRetryCount(),
				provisioningRequests.getNumMaxSelections(), provisioningRequests.getSmStatus());
	}

}
