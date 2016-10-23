package com.onmobile.apps.ringbacktones.rbt2.service;

import com.onmobile.apps.ringbacktones.content.Subscriber;

public interface RBTDaemonService {
	
	
	public boolean updateSubscribersInPlayer(Subscriber subscribersToUpdate, boolean suspend);
	public boolean deactivateUsersInPlayerDB(Subscriber subscriber);

}
