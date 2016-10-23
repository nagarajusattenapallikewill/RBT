package com.onmobile.apps.ringbacktones.common;

public class RBTDeploymentFinder {
	
	private static boolean isRRBTSystem = RBTParametersUtils.getParamAsBoolean("COMMON", "RRBT_SYSTEM", "FALSE");
	
	private static boolean isPRECALLSystem = RBTParametersUtils.getParamAsBoolean("COMMON", "PRECALL_SYSTEM", "FALSE");
	
	private static boolean isBGMSystem = RBTParametersUtils.getParamAsBoolean("COMMON", "BGM_SYSTEM", "FALSE");
	
	private RBTDeploymentFinder() {
		
	}
	
	public static boolean isRRBTSystem() {
		return isRRBTSystem;
	}

	public static boolean isPRECALLSystem() {
		return isPRECALLSystem;
	}
	
	public static boolean isBGMSystem() {
		return isBGMSystem;
	}
}
