package com.onmobile.apps.ringbacktones.webservice.features.getCurrSong;

public class RBTLoginUserFactory {

	public LoadRBTLoginUser getRBTLoginUser(String source) {
		if (source == null) {
			return null;
		}
		if (source.equalsIgnoreCase("CACHE")) {
			return new RBTLoginUserCacheImpl();
		} else if (source.equalsIgnoreCase("DB")) {
			return new RBTLoginUserQueryImpl();
		}
		return null;
	}
}
