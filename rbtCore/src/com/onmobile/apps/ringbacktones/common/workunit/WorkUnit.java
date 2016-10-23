package com.onmobile.apps.ringbacktones.common.workunit;

import java.util.UUID;

import com.onmobile.apps.ringbacktones.service.dblayer.bean.RbtSubscriber;

public class WorkUnit
{
	private String uuid;
	private long startTime;
	private long endTime;
	private int sqlQueryCount;
	private long sqlQueryCumulativeTime;
	private int httpUrlHitCount;
	private long httpUrlCumulativeTime;
	private String responseString;
	private boolean toBeTerminated;
	private String msisdn;
	private RbtSubscriber rbtSubscriber;
	
	public WorkUnit()
	{
		setStartTime(System.nanoTime());
		setUuid(UUID.randomUUID().toString());
	}
	
	/**
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * @return the startTime
	 */
	public long getStartTime() {
		return startTime;
	}
	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
	/**
	 * @return the endTime
	 */
	public long getEndTime() {
		return endTime;
	}
	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}
	/**
	 * @return the sqlQueryCount
	 */
	public int getSqlQueryCount() {
		return sqlQueryCount;
	}
	/**
	 * @param sqlQueryCount the sqlQueryCount to set
	 */
	public void setSqlQueryCount(int sqlQueryCount) {
		this.sqlQueryCount = sqlQueryCount;
	}
	/**
	 * @return the sqlQueryCumulativeTime
	 */
	public long getSqlQueryCumulativeTime() {
		return sqlQueryCumulativeTime;
	}
	/**
	 * @param sqlQueryCumulativeTime the sqlQueryCumulativeTime to set
	 */
	public void setSqlQueryCumulativeTime(long sqlQueryCumulativeTime) {
		this.sqlQueryCumulativeTime = sqlQueryCumulativeTime;
	}
	/**
	 * @return the httpUrlHitCount
	 */
	public int getHttpUrlHitCount() {
		return httpUrlHitCount;
	}
	/**
	 * @param httpUrlHitCount the httpUrlHitCount to set
	 */
	public void setHttpUrlHitCount(int httpUrlHitCount) {
		this.httpUrlHitCount = httpUrlHitCount;
	}
	/**
	 * @return the httpUrlCumulativeTime
	 */
	public long getHttpUrlCumulativeTime() {
		return httpUrlCumulativeTime;
	}
	/**
	 * @param httpUrlCumulativeTime the httpUrlCumulativeTime to set
	 */
	public void setHttpUrlCumulativeTime(long httpUrlCumulativeTime) {
		this.httpUrlCumulativeTime = httpUrlCumulativeTime;
	}

	/**
	 * @param responseString the responseString to set
	 */
	public void setResponseString(String responseString) {
		this.responseString = responseString;
	}

	/**
	 * @return the responseString
	 */
	public String getResponseString() {
		return responseString;
	}
	
	public void log()
	{
		setEndTime(System.nanoTime());
	}

	/**
	 * @param toBeTerminated the toBeTerminated to set
	 */
	public void setToBeTerminated(boolean toBeTerminated) {
		this.toBeTerminated = toBeTerminated;
	}

	/**
	 * @return the toBeTerminated
	 */
	public boolean isToBeTerminated() {
		return toBeTerminated;
	}

	/**
	 * @return the msisdn
	 */
	public String getMsisdn() {
		return msisdn;
	}

	/**
	 * @param msisdn the msisdn to set
	 */
	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	/**
	 * @return the rbtSubscriber
	 */
	public RbtSubscriber getRbtSubscriber() {
		return rbtSubscriber;
	}

	/**
	 * @param rbtSubscriber the rbtSubscriber to set
	 */
	public void setRbtSubscriber(RbtSubscriber rbtSubscriber) {
		this.rbtSubscriber = rbtSubscriber;
	}
	
	
}
