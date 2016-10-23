package com.onmobile.apps.ringbacktones.rbt2.daemon;

import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.daemons.RBTDaemonManager;
import com.onmobile.apps.ringbacktones.daemons.RBTPlayerUpdateDaemon;

public class RBTPlayerUpdateDaemonWrapper{
	
	
	private class RBTPlayerUpdateDaemonWrapperInner extends RBTPlayerUpdateDaemon{
		private RBTPlayerUpdateDaemonWrapperInner(RBTDaemonManager mainDaemonThread) {
			super(mainDaemonThread);
		}
	}
	
	private static RBTPlayerUpdateDaemonWrapper obj = new RBTPlayerUpdateDaemonWrapper();
	private RBTPlayerUpdateDaemonWrapperInner innerClass = null;;

	private RBTPlayerUpdateDaemonWrapper() {
		innerClass = new RBTPlayerUpdateDaemonWrapperInner(null);
	}
	
	
	public static RBTPlayerUpdateDaemonWrapper getInstance() {
		return obj;
	}
	
	public boolean addSelectionsToTonePlayer(Subscriber subscriber) {
		return innerClass.addSelectionsToplayer(subscriber);
	}

	public boolean removeSelectionsFromTonePlayer(Subscriber subscriber) {
		return innerClass.removeSelectionsFromplayer(subscriber);
	}
	
	public boolean updateSubscribersInTonePlayer(Subscriber subscriber, boolean suspend){
		return innerClass.updateSubscribersInPlayer(subscriber, suspend);
	}
	
	public boolean deactivateUsersInTonePlayerDB(Subscriber subscriber){
		return innerClass.deactivateUsersInPlayerDB(subscriber);
	}


}
