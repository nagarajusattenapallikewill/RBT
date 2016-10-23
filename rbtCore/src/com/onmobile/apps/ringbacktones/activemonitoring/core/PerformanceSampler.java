package com.onmobile.apps.ringbacktones.activemonitoring.core;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

import org.javasimon.Sample;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.StopwatchSample;

import com.onmobile.snmp.agentx.client.ManagedObjectCallback;
import com.onmobile.snmp.agentx.client.OID;

/**
 * @author vasipalli.sreenadh
 *
 */
public class PerformanceSampler extends ManagedObjectCallback 
{
	protected AtomicReference<StopwatchSample> lastSample = new AtomicReference<StopwatchSample>();
	protected ThreadLocal<Split> threadLocal = new ThreadLocal<Split>();
	protected static final Timer timer = new Timer();
	protected int lowestBucketDurationInMins = -1;
	protected ObjectName objectName;
	
	public PerformanceSampler(ObjectName name, int lowestBucketDurationInMins, OID oid)
	{
		super(oid);
		this.lowestBucketDurationInMins = lowestBucketDurationInMins;
		startSampling();
		objectName = name;
	}
	
	
	/**
	 * Add a single perf sample in ms.
	 * @param sample
	 */
	public void addSample(long sampleInMS) 
	{
		SimonManager.getStopwatch(objectName.getSimonName()).addTime(sampleInMS * 1000 * 1000);
	}
		
	/**
	 * Call this method to start a sampling. Should be preceded by markEndOfSmapling called by the same thread. 
	 */
	public void markStartOfSampling() 
	{
		markEndOfSampling(); //Clear any existing split which is not ended.
		
		Split start = SimonManager.getStopwatch(objectName.getSimonName()).start();
		threadLocal.set(start);
	}
	
	public void markEndOfSampling() 
	{
		Split existingSplit = threadLocal.get();
		if (existingSplit != null) 
		{
			existingSplit.stop();
			threadLocal.remove();
		}
	}
	

	protected Sample getSamplesToSave() 
	{
		return SimonManager.getStopwatch(objectName.getSimonName()).sampleAndReset();
	}
	
	protected void saveSamples(Sample sample) 
	{
		//		if (isPersistenceEnabled())
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
	
	/* (non-Javadoc)
	 * @see com.onmobile.snmp.agentx.ManagedObjectCallback#getValue()
	 */
	@Override
	public Object getValue() 
	{
		return getMean();
	}
	
	public long getMax() 
	{
		StopwatchSample sample = lastSample.get();
		return (sample != null) ? sample.getMax()/(1000 * 1000) : 0;
	}

	
	public Date getMaxTimestamp() 
	{
		StopwatchSample sample = lastSample.get();
		return (sample != null) ? new Date(sample.getMaxTimestamp()) : null;
	}

	public double getMean() 
	{
		StopwatchSample sample = lastSample.get();
		return (sample != null) ? sample.getMean()/(1000 * 1000) : 0;
	}

	public long getMin() 
	{
		StopwatchSample sample = lastSample.get();
		return (sample != null) ? sample.getMin()/(1000 * 1000) : 0;
	}

	public Date getMinTimestamp() 
	{
		StopwatchSample sample = lastSample.get();
		return (sample != null) ? new Date(sample.getMinTimestamp()) : null;
	}

	public double getStandardDeviation() 
	{
		StopwatchSample sample = lastSample.get();
		return (sample != null) ? sample.getStandardDeviation() : 0;
	}

	public double getVariance() 
	{
		StopwatchSample sample = lastSample.get();
		return (sample != null) ? sample.getVariance() : 0;
	}

	public double getTPS() 
	{
		StopwatchSample sample = lastSample.get();
		return (sample != null) ? sample.getCounter()*1.0/(lowestBucketDurationInMins * 60) : 0;
	}

    public double getTAT() 
    {
		StopwatchSample sample = lastSample.get();
		return (sample != null) ? sample.getMean()/1000 : 0;
	}

    public long getCurrentCount() 
    {
        StopwatchSample sample = lastSample.get();
		return (sample != null) ? sample.getCounter(): 0;
    }
    
    protected class SamplerTimerTask extends TimerTask 
	{
		@Override
		public void run() 
		{
			Sample sample = getSamplesToSave();
			saveSamples(sample);
			lastSample.set((StopwatchSample) sample);
		}
	}

}
