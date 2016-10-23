package com.onmobile.apps.ringbacktones.webservice;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

/**
 * @author vinayasimha.patil
 */
public interface RBTProcessor {
	public String processActivation(WebServiceContext task);

	public String updateSubscription(WebServiceContext task);

	public String processDeactivation(WebServiceContext task);

	public String processRRBTConsentDeactivation(WebServiceContext task);

	public String processSubscriberPromoRequest(WebServiceContext task);

	public String processNormalScratchCard(WebServiceContext task);

	public String processSelection(WebServiceContext task);

	public String deleteSetting(WebServiceContext task);

	public String updateSelection(WebServiceContext task);

	public String downloadTone(WebServiceContext task);

	public String deleteTone(WebServiceContext task);

	public String shuffleDownloads(WebServiceContext task);

	public String upgradeSelectionPack(WebServiceContext task);

	public String upgradeSpecialSelectionPack(WebServiceContext task);

	public String deactivateOffer(WebServiceContext task);

	public String deactivatePack(WebServiceContext task);

	public String subscribeUser(WebServiceContext task);

	public String updateCopyContestInfo(WebServiceContext task);

	public String addBookMark(WebServiceContext task);

	public String removeBookMark(WebServiceContext task);

	public String processGroupRequest(WebServiceContext task);

	public String processGroupMemberRequest(WebServiceContext task);

	public String processGroupMultipleMemberRequest(WebServiceContext task);

	public String processAffiliateGroupRequest(WebServiceContext task);

	public String processAffiliateGroupMemberRequest(WebServiceContext task);

	public String processCopyRequest(WebServiceContext task);

	public String processDirectCopyRequest(WebServiceContext task);

	public String processGiftRequest(WebServiceContext task);

	public String processGiftRejectRequest(WebServiceContext task);

	public String setSubscriberDetails(WebServiceContext task);

	public String setApplicationDetails(WebServiceContext task);

	public String removeApplicationDetails(WebServiceContext task);

	public String getApplicationDetails(WebServiceContext task);

	public String processBulkActivation(WebServiceContext task);

	public String getDownloadOfTheDayEntries(WebServiceContext task);

	public String processDownloadOfDayInsertion(WebServiceContext task);

	public String processBulkDeactivation(WebServiceContext task);

	public String processBulkSelection(WebServiceContext task);

	public String processBulkDeleteSelection(WebServiceContext task);

	public String processBulkSetSubscriberDetails(WebServiceContext task);

	public String processBulkGetSubscriberDetails(WebServiceContext task);

	public String processBulkGetCorporateDetails(WebServiceContext task);

	public String processBulkGetTaskDetails(WebServiceContext task);

	public String processBulkUpload(WebServiceContext task);

	public String processBulkTask(WebServiceContext task);

	public String uploadNprocessBulkTask(WebServiceContext task);

	public String editBulkTask(WebServiceContext task);

	public String editBulkTaskForCorporate(WebServiceContext task);

	public String checkBulkSubscribersStatus(WebServiceContext task);

	public String removeBulkTask(WebServiceContext task);

	public String sendSMS(WebServiceContext task);

	public String processHLRRequest(WebServiceContext task);

	public String processSuspension(WebServiceContext task);

	public String processThirdPartyRequest(WebServiceContext task);

	public String addData(WebServiceContext task);

	public String processData(WebServiceContext task);

	public String updateData(WebServiceContext task);

	public String removeData(WebServiceContext task);

	public String processUSSD(WebServiceContext task);

	public String processAnnouncementActivation(WebServiceContext task);

	public String processAnnouncementDeactivation(WebServiceContext task);

	public String processBulkAnnouncementActivation(WebServiceContext task);

	public String processBulkAnnouncementDeactivation(WebServiceContext task);

	public String processBulkUpdateSubscription(WebServiceContext task);

	public String processSngActivation(WebServiceContext task);

	public String processSngAllDeativation(WebServiceContext task);

	public String processSngUserUpdate(WebServiceContext task);

	public String processSngDeactivation(WebServiceContext task);

	public String processChangeMsisdn(WebServiceContext task);

	public String processSendChangeMsisdnRequestToSM(WebServiceContext task);

	public String processDeleteConsentRecords(WebServiceContext task);

	public String processUpgradeAllSelections(WebServiceContext task);

	// JiraID-RBT-4187:Song upgradation through bulk process
	public String processBulkSelectionUpgradation(WebServiceContext task);

	public String processAddMultipleSelections(WebServiceContext task);

	public String processAddMultipleDownloads(WebServiceContext task);

	public String processDeleteMultipleSelections(WebServiceContext task);

	public String processDeleteMultipleDownloads(WebServiceContext task);

	public String disableRandomization(WebServiceContext task);

	public String reset(WebServiceContext task);

	public String processUpgradeAllDownloads(WebServiceContext task);

	// RBT-13415 - Nicaragua Churn Management.
	public String processRejectDelayDeactivation(WebServiceContext webServiceContext);

	public String processUDPSelections(WebServiceContext task);

	public String processUDPDeactivation(WebServiceContext task);

	String processUpgradeDownload(WebServiceContext webServiceContext);
}