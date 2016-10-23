/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.executor;

import java.util.concurrent.ArrayBlockingQueue;

/**
 * @author vinayasimha.patil
 * 
 */
public class RbtBlockingQueue extends ArrayBlockingQueue<Runnable>
{
	private final int capacity;
	/**
	 * 
	 */
	private static final long serialVersionUID = 6512109613560086015L;

	/**
	 * @param capacity
	 */
	public RbtBlockingQueue(int capacity)
	{
		super(capacity, true);

		this.capacity = capacity;
	}

	/**
	 * @return the capacity
	 */
	public int getCapacity()
	{
		return capacity;
	}
}
