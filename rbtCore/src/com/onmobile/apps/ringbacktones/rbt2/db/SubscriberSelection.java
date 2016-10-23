package com.onmobile.apps.ringbacktones.rbt2.db;

import java.util.List;

import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.rbt2.bean.ExtendedSubStatus;

public interface SubscriberSelection {
	
	public int getSubSelectionCountByUDPId(String udpId);
	public SubscriberStatus getSelectionByUdpIdAndClipId(String udpId, int clipId, String msisdn, String cType);
	public boolean deleteSubSelectionByUdpId(SubscriberStatus subscriberStatus, boolean isDirectDeact);
	public List<ExtendedSubStatus> getSelections(ExtendedSubStatus extendedSubStatus);
	public List<ExtendedSubStatus> getAllSelectionsByRestrictions(ExtendedSubStatus extendedSubStatus);
	public boolean deactivateSubSelection(ExtendedSubStatus extendedSubStatus, boolean isDirectDeact);

}
