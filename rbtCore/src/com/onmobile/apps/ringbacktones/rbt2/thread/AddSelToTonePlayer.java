package com.onmobile.apps.ringbacktones.rbt2.thread;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.rbt2.daemon.RBTPlayerUpdateDaemonWrapper;


public class AddSelToTonePlayer implements Runnable {

	private Subscriber subscriber;
	
	@Override
	public void run() {
		if(subscriber != null)
			RBTPlayerUpdateDaemonWrapper.getInstance().addSelectionsToTonePlayer(subscriber);
	}
	
	public void setSubscriber(Subscriber subscriber) {
		this.subscriber = subscriber;
	}
	

}
