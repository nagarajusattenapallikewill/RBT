package com.onmobile.apps.ringbacktones.content;

/**
 * @author sridhar.sindiri
 *
 */
public interface GCMRegistration
{
	public String registrationID();

	public String subscriberID();
	
	public String os_type();
	
	public String notificationEnabled();
}
