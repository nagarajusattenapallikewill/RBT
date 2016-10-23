package com.onmobile.apps.ringbacktones.content;

public interface BulkPromoSMS
{
	//Returns the Bulk Promo Id
	public String bulkPromoId();
	
	//Returns SMS date
	public String smsDate();
	
	//Returns SMS text
	public String smsText();
	
	//Returns SMS sent value
	public boolean smsSent();
}