package com.onmobile.apps.ringbacktones.v2.processor;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.rbt2.thread.AddSelToTonePlayer;
import com.onmobile.apps.ringbacktones.rbt2.thread.ProcessingClipTransfer;
import com.onmobile.apps.ringbacktones.rbt2.thread.ThreadExecutor;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;


public class SelectionProcessorImpl implements ISelectionProcessor, WebServiceConstants {

		
	private ThreadExecutor executor;
	
	public SelectionProcessorImpl() {
		
	}
	
	@Override
	public void startProcessing(WebServiceContext task,Subscriber subscriber) {
		addSelectionsToTonePlayer(subscriber);
		task.put(param_subscriber, subscriber);
		clipTransfer(task);
	}
	
	public void addSelectionsToTonePlayer(Subscriber subscriber) {
		AddSelToTonePlayer addSelToTonePlayerThread = new AddSelToTonePlayer();
		addSelToTonePlayerThread.setSubscriber(subscriber);
		executor.getExecutor().execute(addSelToTonePlayerThread);
	}
	
	public void clipTransfer(WebServiceContext task) {
		ProcessingClipTransfer clipTransferThread = new ProcessingClipTransfer();
		clipTransferThread.setSubscriber((Subscriber)task.get(param_subscriber));
		clipTransferThread.setClipId(task.getString(param_clipID));
		clipTransferThread.setUdpId(task.getString(param_udpId));
		clipTransferThread.setCategoryId(task.getString(param_categoryID));
		executor.getExecutor().execute(clipTransferThread);
	}

	
	public void setExecutor(ThreadExecutor executor) {
		this.executor = executor;
	}
	
	
	
}
