package com.onmobile.apps.ringbacktones.activemonitoring.core;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;
import org.javasimon.Sample;

/**
 * @author vasipalli.sreenadh
 *
 */
public abstract class PerformaceSamplerBase 
{
	protected static final Logger log  = Logger.getLogger(PerformaceSamplerBase.class.getName());
	protected static final Timer timer = new Timer();
	protected int lowestBucketDurationInMins = -1;
	protected ObjectName objectName;
	

	protected abstract Sample getSamplesToSave();
	
	protected class SamplerTimerTask extends TimerTask 
	{
		@Override
		public void run() 
		{
			saveSamples(getSamplesToSave());
		}
	}
	
	protected void saveSamples(Sample sample) 
	{
	//	if (isPersistenceEnabled())
	//		_persistence.save(sample, _objectName);
	}
	
	protected void startSampling() 
	{
		if (lowestBucketDurationInMins < 1) 
			return;
		//Aligning the start time with clock.  _lowestBucketDurationInMins of 5 will be aligned to clock time minute, which is a multiple of 5
		Calendar cal = Calendar.getInstance();
		int mins= cal.get(Calendar.MINUTE)%lowestBucketDurationInMins;
		cal.add(Calendar.MINUTE, lowestBucketDurationInMins-mins);
		cal.set(Calendar.SECOND, 0);
	
		timer.scheduleAtFixedRate(new SamplerTimerTask(), cal.getTime(), lowestBucketDurationInMins * 60 * 1000);
	}
	
}
