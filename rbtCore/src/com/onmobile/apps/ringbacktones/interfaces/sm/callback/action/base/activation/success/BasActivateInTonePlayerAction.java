package com.onmobile.apps.ringbacktones.interfaces.sm.callback.action.base.activation.success;

import com.onmobile.apps.ringbacktones.common.workunit.CallbackWorkUnit;
import com.onmobile.apps.ringbacktones.interfaces.sm.callback.action.CallbackAction;
import com.onmobile.apps.ringbacktones.service.dblayer.bean.RbtSubscriber;

public class BasActivateInTonePlayerAction extends CallbackAction
{
	@Override
	public void execute(CallbackWorkUnit callbackWorkUnit)
	{
		RbtSubscriber rbtSubscriber = callbackWorkUnit.getRbtSubscriber();
		rbtSubscriber.setPlayerStatus('A');
	}
}
