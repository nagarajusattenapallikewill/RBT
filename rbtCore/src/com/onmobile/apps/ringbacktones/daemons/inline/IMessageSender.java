package com.onmobile.apps.ringbacktones.daemons.inline;

import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public interface IMessageSender {
	public void send(Object obj, WebServiceContext parameters) throws Throwable;
}
