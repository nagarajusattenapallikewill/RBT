package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

import java.util.Set;

public interface LoadRBTLoginUser {
	public Set<String> getRBTLoginUserData(String callerId, String calledId,
			String userId);
}
