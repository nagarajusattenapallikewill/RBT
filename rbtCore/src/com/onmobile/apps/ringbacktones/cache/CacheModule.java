package com.onmobile.apps.ringbacktones.cache;

/**
 * @author Sreekar
 * @date 25/06/2008
 * @description All Individual Cache modules must implement this class
 */

public abstract class CacheModule {
	/**
	* Refreshes the cache of module
	*
	* @param checkNeeded checks for the conditiond for which cache to be refreshed. If false refreshes cache without checking for the conditions
	*/
	protected abstract boolean refreshCache(boolean checkNeeded);
}