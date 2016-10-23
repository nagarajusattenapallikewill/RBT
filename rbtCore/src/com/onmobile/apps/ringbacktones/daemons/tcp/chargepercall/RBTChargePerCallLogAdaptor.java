package com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall;

import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCall;
import com.onmobile.apps.ringbacktones.daemons.tcp.chargepercall.hibernate.beans.RBTChargePerCallLog;

public class RBTChargePerCallLogAdaptor {

	public static RBTChargePerCallLog convert(RBTChargePerCall rbtChargePerCall) {
		RBTChargePerCallLog rbtChargePerCallLog = new RBTChargePerCallLog(
				rbtChargePerCall.getCallerId(), rbtChargePerCall.getCalledId(),
				rbtChargePerCall.getCalledTime());
		return rbtChargePerCallLog;
	}

}
