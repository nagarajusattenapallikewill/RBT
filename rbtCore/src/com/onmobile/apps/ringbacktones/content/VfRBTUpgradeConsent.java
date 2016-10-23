package com.onmobile.apps.ringbacktones.content;

public interface VfRBTUpgradeConsent {
	Subscriber consentUpgradeFlow(UpgradeObject object);
	boolean CheckUpgradeModeIsConfigured(String mode);
}
