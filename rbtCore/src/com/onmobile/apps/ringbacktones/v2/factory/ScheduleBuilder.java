package com.onmobile.apps.ringbacktones.v2.factory;

import com.livewiremobile.store.storefront.dto.rbt.DateRange;
import com.livewiremobile.store.storefront.dto.rbt.Schedule;
import com.livewiremobile.store.storefront.dto.rbt.TimeRange;
import com.livewiremobile.store.storefront.dto.rbt.Schedule.ScheduleType;

/**
 * 
 * @author md.alam
 *
 */
public class ScheduleBuilder {
	
	private Schedule schedule;
	
	public ScheduleBuilder() {
		schedule = new Schedule();
	}
	
	public ScheduleBuilder setType(ScheduleType scheduleType) {
		schedule.setType(scheduleType);
		return this;
	}
	
	public ScheduleBuilder setId(long id) {
		schedule.setId(id);
		return this;
	}
	
	public ScheduleBuilder setDescription(String description) {
		schedule.setDescription(description);
		return this;
	}
	
	public ScheduleBuilder setPlayedDuration(String playDuration) {
		schedule.setPlayDuration(playDuration);
		return this;
	}
	
	public ScheduleBuilder setDateRange(DateRange dateRange) {
		schedule.setDateRange(dateRange);
		return this;
	}
	
	public ScheduleBuilder setTimeRange(TimeRange timeRange) {
		schedule.setTimeRange(timeRange);
		return this;
	}
	
	public Schedule buildSchedule() {
		return this.schedule;
	}

}
