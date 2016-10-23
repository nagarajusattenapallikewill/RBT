package com.onmobile.apps.ringbacktones.v2.dao.constants;

public enum OperatorUserTypes {
	FREE_APP_USER("FREE"), FREE_TRIAL_APP_USER("FREE_TRIAL"), PAID_APP_USER("PAID"), PAID_APP_USER_LOW_BALANCE("FREE_SUSPENDED"),
	LEGACY("LEGACY"),
	LEGACY_FREE_TRIAL("LEGACY_FREE_TRIAL"),
	TRADITIONAL("TRADITIONAL"),
	TRADITIONAL_FREE_TRIAL("TRADITIONAL_FREE_TRIAL"),NEW_USER("NEW_USER");
	
	
	private String defaultValue;

	OperatorUserTypes(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}
}
