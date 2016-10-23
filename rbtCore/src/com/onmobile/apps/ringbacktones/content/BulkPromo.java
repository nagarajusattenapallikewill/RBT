package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

public interface BulkPromo
{

	//Returns the Bulk Promo Id
	public String bulkPromoId();
	
	//Returns Promo Start Date
	public Date promoStartDate();
	
	//Returns Promo End Date
	public Date promoEndDate();
	
	//Returns processedDeactivation
	public String processedDeactivation();
	
	//Returns the COS Id
	public String cosID();
}