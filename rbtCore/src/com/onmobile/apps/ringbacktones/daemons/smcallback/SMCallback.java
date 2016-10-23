/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.smcallback;

/**
 * Represents the callback
 * 
 * @author vinayasimha.patil
 */
public interface SMCallback
{
	public SMCallbackResponse processCallback(
			SMCallbackContext smCallbackContext);
}
