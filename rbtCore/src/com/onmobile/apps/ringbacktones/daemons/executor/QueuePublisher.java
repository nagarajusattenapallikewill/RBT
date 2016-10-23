/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.executor;

import java.util.LinkedList;
import java.util.Map;

/**
 * @author vinayasimha.patil
 * 
 */
public interface QueuePublisher
{
	public void start();

	public void stop();

	public Map<String, LinkedList<Command>> getDuplicateRequestMap();

	public RbtThreadPoolExecutor getExecutor();

	public void setExecutor(RbtThreadPoolExecutor executor);
}
