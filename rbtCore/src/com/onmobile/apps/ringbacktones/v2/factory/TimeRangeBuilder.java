package com.onmobile.apps.ringbacktones.v2.factory;

import java.util.Date;

import com.livewiremobile.store.storefront.dto.rbt.TimeRange;


/**
 * 
 * @author md.alam
 *
 */
public class TimeRangeBuilder {
	
	private TimeRange timeRange;
	
	public TimeRangeBuilder() {
		timeRange = new TimeRange();
	}
	
	public TimeRangeBuilder setFromTime(Date fromTime) {
		timeRange.setFromTime(fromTime);
		return this;
	}
	
	public TimeRangeBuilder setToTime(Date toTime) {
		timeRange.setToTime(toTime);
		return this;
	}
	
	public TimeRange buildTimeRange() {
		return this.timeRange;
	}

}
