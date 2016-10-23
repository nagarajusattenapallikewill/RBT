package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.List;

import org.apache.log4j.Logger;

import com.danga.MemCached.MemCachedClient;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCache;

public abstract class GenericCacheThread extends Thread {
	
	private static final Logger log = Logger.getLogger(GenericCacheThread.class);

	static boolean stop = false;

	MemCachedClient mc = null;

	private List records = null;

	private Exception exception = null;

	public GenericCacheThread(String name, List records) {
		super(name);
		this.records = records;
		this.mc = RBTCache.getMemCachedClient();
	}

	public static void stopThreads() {
		stop = true;
	}

	public Exception getException() {
		return exception;
	}
	
	protected void setException(Exception e) {
		exception = e;
	}

	public void run() {
		long l1 = System.currentTimeMillis();
		int size = 0;
		try {
			size = records.size();
			log.info(getName() + " processing number of records: " + size);
			for (int i = 0; i < size; i++) {
				if (stop) {
					log.error("Forcibly stopping " + getName() + " because some other thread got error.");
					return;
				}
				Object obj = records.get(i);
				processRecord(obj);
			}
			finalProcess();
		} catch (Exception e) {
			setException(e);
			stopThreads();
			log.error("Exception occurred in " + getName() + " while adding records to cache", e);
		}
		long l2 = System.currentTimeMillis();
		log.info(getName() + " is finished processing records: " + size + " TimeTaken: " + (l2 - l1) + "ms");
	}
	
	public abstract void processRecord(Object obj) throws Exception;

	public abstract void finalProcess() throws Exception;

}
