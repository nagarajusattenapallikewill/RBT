package com.onmobile.apps.ringbacktones.servlets;

public class ConfigForAccounting {
	private String sdrWorkingDir="e:\\CCC\\report";
	private int sdrSize= 1000;;
	private long sdrInterval= 24;;
	private String sdrRotation= "size";
	private boolean sdrBillingOn= true;
	
	public ConfigForAccounting(String sdrWorkingDir,int sdrSize,long sdrInterval,String sdrRotation,boolean sdrBillingOn){
		this.sdrSize=sdrSize;
		this.sdrBillingOn=sdrBillingOn;
		this.sdrRotation=sdrRotation;
		this.sdrWorkingDir=sdrWorkingDir;
		this.sdrInterval=sdrInterval;
	}
	
	/**
	 * @return the sdrWorkingDir
	 */
	public String getSdrWorkingDir() {
		return sdrWorkingDir;
	}

	/**
	 * @return the sdrSize
	 */
	public int getSdrSize() {
		return sdrSize;
	}

	/**
	 * @return the sdrInterval
	 */
	public long getSdrInterval() {
		return sdrInterval;
	}

	/**
	 * @return the sdrRotation
	 */
	public String getSdrRotation() {
		return sdrRotation;
	}

	/**
	 * @return the sdrBillingOn
	 */
	public boolean isSdrBillingOn() {
		return sdrBillingOn;
	}

}
