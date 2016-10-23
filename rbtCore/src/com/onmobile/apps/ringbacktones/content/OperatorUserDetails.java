package com.onmobile.apps.ringbacktones.content;

import java.io.Serializable;

public interface OperatorUserDetails extends Serializable {

	public String subID();
	
	public String status();
	
	public String serviceKey();
	
	public String operatorName();
	
	public String circleId();
}
