package com.onmobile.apps.ringbacktones.provisioning.implementation.promo.bsnl;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Task;
import com.onmobile.apps.ringbacktones.provisioning.implementation.promo.PromoProcessor;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * The 3rd party request processor class for BSNL.
 * 
 * @author Sreekar
 *
 */
public class BsnlPromoProcessor extends PromoProcessor implements Constants {
	public BsnlPromoProcessor() throws RBTException {
		super();
		logger = Logger.getLogger(this.getClass());
	}
	
	@Override
	public Task getTask(HashMap<String, String> requestParams) {
		Task task = super.getTask(requestParams);
		/*if(task.containsKey(param_BSNL_adRBT_sender))
			task.setObject(param_mode, task.getString(param_BSNL_adRBT_sender));*/
		if(isAdRBTRequest(task))
			task.setTaskAction(task.getString(param_BSNL_adRBT_command));
		return task;
	}

	@Override
	public String validateParameters(Task task) {
		if(isAdRBTRequest(task)) {
			task.setObject(param_MSISDN, task.getString(param_BSNL_adRBT_msisdn));
			String transID = task.getString(param_BSNL_adRBT_trx_id);
			if(transID == null)
				task.setObject(param_BSNL_adRBT_trx_id, "noTrxID");
		}
		return super.validateParameters(task);
	}
	
	@Override
	/**
	 * Processes the Ad RBT Request with UTPL integration
	 */
	public void processAdRBTRequest(Task task) {
		String command = task.getString(param_BSNL_adRBT_command);
		if(command == null) {
			task.setObject(param_response, Resp_Err);
			return;
		}
		if(command.equalsIgnoreCase(param_BSNL_adRBT_command_SUBINFO))
			processAdRBTSubsriberInfoRequest(task);
		else if(command.equalsIgnoreCase(param_BSNL_adRBT_command_ACT))
				processAdRBTActivationRequest(task);
		else if(command.equalsIgnoreCase(param_BSNL_adRBT_command_DCT))
			processAdRBTDeactivationRequest(task);
	}
	
	private boolean isAdRBTRequest(Task task) {
		return task.containsKey(param_BSNL_adRBT_command);
	}

	/**
	 * Populates the subscriber object to the task. The response encoder will have to get the
	 * relavent details from the subscriber object and send the response
	 * 
	 * @param task
	 */
	private void processAdRBTSubsriberInfoRequest(Task task) {
		if(!task.containsKey(param_subscriber))
			getSubscriber(task);
	}

	/**
	 * Processes the Ad-Rbt activation request.
	 * <table border=true>
	 * <tr>
	 * <td>New User</td>
	 * <td>User will be activated as Ad-RBT User and request will be sent to SM</td>
	 * </tr>
	 * <tr>
	 * <td>Already Active User in RBT</td>
	 * <td>User will be updated to Ad RBT user to inform player and send activation request to 3rd
	 * party if needed</td>
	 * </tr>
	 * <tr>
	 * <td>Activation pending/Grace user</td>
	 * <td>User type will be updated, on successful activation call back we will update 3rd party</td>
	 * </tr>
	 * <td>Deactivation pending</td>
	 * <td>User extra_info column will be updated so that on successful deactivation call back again
	 * activate the user in Ad-RBT</td> </tr>
	 * </table>
	 * 
	 * @param task
	 */
	private void processAdRBTActivationRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);
		if (subscriber == null) {
			task.setObject(param_response, Resp_Err);
			return;
		}
		String subscriberID = subscriber.getSubscriberID();
		String status = subscriber.getStatus();
		String rbtType = subscriber.getUserType();
		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				|| status.equalsIgnoreCase(WebServiceConstants.DEACTIVE))
			activateAdRBTSubscriber(task);
		else if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE)
				|| status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
				|| status.equalsIgnoreCase(WebServiceConstants.GRACE)) {
			if (rbtType.equalsIgnoreCase(WebServiceConstants.AD_RBT))
				logger.info("User " + subscriberID + " already adRbt user and status is " + status);
			else {
				if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE))
					task.setObject("updatePlayer", "true");
				updateRbtToAdRbt(task);
			}
		}
		else if (status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING))
			updateDeacPendingSubToAdRbt(task);

		if (!task.containsKey(param_response))
			task.setObject(param_response, Resp_Err);
	}

	/**
	 * Processes the Ad-Rbt deactivation request.
	 * <table border=true>
	 * <tr>
	 * <td>New User/Activation pending/Grace/Already Active User in RBT</td>
	 * <td>Request will be rejected</td>
	 * </tr>
	 * <tr>
	 * <td>Active user in Ad-RBT</td>
	 * <td>Request will be accepted, and on successful call back we will need to update 3rd party</td>
	 * </tr>
	 * <tr>
	 * <td>Activation pending/Grace user in Ad-RBT</td>
	 * <td>User extra_info column will be updated so that on successful activation call back we will
	 * start the deactivation process</td>
	 * </tr>
	 * <td>Deactivation pending</td>
	 * <td>Reject request</td> </tr>
	 * </table>
	 * 
	 * @param task
	 */
	private void processAdRBTDeactivationRequest(Task task) {
		Subscriber subscriber = (Subscriber) task.getObject(param_subscriber);

		String subscriberID = subscriber.getSubscriberID();
		String status = subscriber.getStatus();
		String rbtType = subscriber.getUserType();

		if (status.equalsIgnoreCase(WebServiceConstants.NEW_USER)
				|| status.equalsIgnoreCase(WebServiceConstants.DEACTIVE)
				|| status.equalsIgnoreCase(WebServiceConstants.DEACT_PENDING))
			logger.info("RBT::User " + subscriberID + " status is " + status
					+ ". Cannot deactivate from ad rbt");
		else if (status.equalsIgnoreCase(WebServiceConstants.ACT_PENDING)
				|| status.equalsIgnoreCase(WebServiceConstants.GRACE)
				|| status.equalsIgnoreCase(WebServiceConstants.ACTIVE)) {
			if (rbtType.equalsIgnoreCase(WebServiceConstants.AD_RBT)) {
				if (status.equalsIgnoreCase(WebServiceConstants.ACTIVE))
					updateAdRbtToRbt(task);
				else
					deactivateAdRbtFromActPendingSub(task);
			}
			else
				logger.info("RBT::User " + subscriberID + " with type " + rbtType + " status is "
						+ status + ". Cannot deactivate from ad rbt");
		}

		if (!task.containsKey(param_response))
			task.setObject(param_response, Resp_Err);
	}
	
	private void updateAdRbtToRbt(Task task) {
		task.setObject(param_rbttype, new Integer(0));
		task.setObject(param_playerStatus, "A");
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_DEACTIVATION, "true");
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_MODE, task
				.getString(param_BSNL_adRBT_sender)
				+ ":" + task.getString(param_BSNL_adRBT_trx_id));
		updateSubscription(task);
	}

	/**
	 * Updates the extra info of subscriber so that after receiving activation success call back the
	 * subscriber will be deactivated from RBT
	 * 
	 * @param task
	 */
	private void deactivateAdRbtFromActPendingSub(Task task) {
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_DEACTIVATION, "true");
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_MODE, task
				.getString(param_BSNL_adRBT_sender)
				+ ":" + task.getString(param_BSNL_adRBT_trx_id));
		updateSubscription(task);
	}
	
	/**
	 * Activates new Ad-RBT subscriber
	 * @param task
	 */
	private void activateAdRBTSubscriber(Task task) {
		task.setObject(param_TRANSID, task.getString(param_BSNL_adRBT_trx_id));
		task.setObject(param_rbttype, new Integer(1));
		task.setObject(param_ACTIVATED_BY, task.getString(param_BSNL_adRBT_sender));
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_TRANS_ID, task.getString(param_BSNL_adRBT_transactionKey));
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_ACTIVATION, "true");
		processActivation(task);
	}
	
	/**
	 * Updates normal RBT subscriber to Ad RBT subscriber
	 * @param task
	 */
	private void updateRbtToAdRbt(Task task) {
		task.setObject(param_ACTIVATED_BY, task.getString(param_BSNL_adRBT_sender));
		task.setObject(param_TRANSID, task.getString(param_BSNL_adRBT_trx_id));
		task.setObject(param_rbttype, new Integer(1));
		if(task.containsKey("updatePlayer") && "true".equalsIgnoreCase(task.getString("updatePlayer")))
			task.setObject(param_playerStatus, "A");
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_TRANS_ID, task.getString(param_BSNL_adRBT_transactionKey));
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_ACTIVATION, "true");
		updateSubscription(task);
	}
	
	/**
	 * Updates deactivation pending subscriber, so that once his deactivation completes he will be
	 * again activated for Ad Rbt. Add ad_rbt_activation=true in extra info column.
	 * 
	 * @param task
	 */
	private void updateDeacPendingSubToAdRbt(Task task) {
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_ACTIVATION, "true");
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_TRANS_ID, task.getString(param_BSNL_adRBT_transactionKey));
		addToSubscriberExtraInfo(task, EXTRA_INFO_ADRBT_MODE, task
				.getString(param_BSNL_adRBT_sender)
				+ ":" + task.getString(param_BSNL_adRBT_trx_id));
		updateSubscription(task);
	}
}