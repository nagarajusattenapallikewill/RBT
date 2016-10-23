package com.onmobile.apps.ringbacktones.rbt2.common;

import java.util.HashMap;
import java.util.Map;

public enum SelectionStatus {
	
	ACTIVE("active"), DEACTIVATED("deactive"), ACTIVATIONPENDING("act_pending"),DEACTIVATIONPENDING("deact_pending"), EXPIRED("suspended"),GRACE("grace"),SYSTEM_DEACTIVATED("systemdeactive");
	
	private final String selectionStatus;
	
	private static Map<String, SelectionStatus> map =
            null;

    private SelectionStatus(final String selectionStatus) {
    	this.selectionStatus = selectionStatus;
    }
    
    static {
    	map = new HashMap<String, SelectionStatus>();
    	for(SelectionStatus selectionStatus : SelectionStatus.values()) {
    		map.put(selectionStatus.selectionStatus, selectionStatus);
    	}
    }

    public static String getSelectionStatus(String selStatus) {
    	SelectionStatus  selectionStatus = map.get(selStatus);
    	String tempValue = selectionStatus.toString();
    	if(tempValue.indexOf('$') != -1) {
    		tempValue = tempValue.replace("$", "");
    	}
    	return tempValue.toLowerCase();
    }
    

}


