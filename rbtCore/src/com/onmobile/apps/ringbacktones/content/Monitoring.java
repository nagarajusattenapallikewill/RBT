package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/**
 * Interface representation for RBT_MONITORING table
 * 
 * @author Sreekar
 * @since 2010-01-11
 */
public interface Monitoring {
	public String msisdn();
	public Date createTime();
	public String traceType();
	public char status();
	public String traceResult();
}