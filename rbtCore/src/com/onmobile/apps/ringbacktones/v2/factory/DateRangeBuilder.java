package com.onmobile.apps.ringbacktones.v2.factory;

import java.util.Date;

import com.livewiremobile.store.storefront.dto.rbt.DateRange;

/**
 * 
 * @author md.alam
 *
 */
public class DateRangeBuilder {
	
	private DateRange dateRange;
	
	public DateRangeBuilder() {
		dateRange = new DateRange();
	}
	
	public DateRangeBuilder setStartDate(Date startDate) {
		dateRange.setStartDate(startDate);
		return this;
	}
	
	public DateRangeBuilder setEndDate(Date endDate) {
		dateRange.setEndDate(endDate);
		return this;
	}
	
	public DateRange buildDateRange() {
		return this.dateRange;
	}

}
