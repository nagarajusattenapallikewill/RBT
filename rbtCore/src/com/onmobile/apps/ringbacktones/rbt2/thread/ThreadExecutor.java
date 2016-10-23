package com.onmobile.apps.ringbacktones.rbt2.thread;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class ThreadExecutor {

	private ExecutorService ex = null;
	private int poolSize;


	public ExecutorService getExecutor() {
		if (ex == null) {
			synchronized (ThreadExecutor.class) {
				if (ex == null)
					ex = Executors.newFixedThreadPool(poolSize);
			}
		}
		return ex;
	}


	public void setPoolSize(int poolSize) {
		this.poolSize = poolSize;
	}

}
