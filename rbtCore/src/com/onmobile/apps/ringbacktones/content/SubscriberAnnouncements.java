package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/**
 * 
 * @author senthil.raja
 *
 */

/*
 * 
 * This class is related to SubscriberAnnouncements
 * Table name - RBT_SUBSCRIBER_ANNOUNCEMENTS
 */

public interface SubscriberAnnouncements {
	
	public long sequenceId();
	
	public String subscriberId();
	
	public int clipId();
	
	public Date activationDate();
	
	public Date deActivationDate();
	
	public int status();
	
	public String timeInterval();
	
	public String frequency();
	
	public void setDeactivationDate(Date deactivationDate);
	
	public void setActivationDate(Date activationDate);
	
	public void setStatus(int status);
	
	public String toString();

}
