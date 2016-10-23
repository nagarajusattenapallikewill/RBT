package com.onmobile.apps.ringbacktones.lucene;


import java.util.List;

import org.apache.log4j.Logger;

public class LuceneIndexerThread implements Runnable {
	private static final Logger log=Logger.getLogger(LuceneIndexerThread.class);
	private static final String _class="LuceneIndexerThread";
	private int _offset=0;
	private int _maxValue=5;
	private Object lock=new Object();
	private int _offsetInterval;
	private List[] _contentQueue = null;
	private List _luceneThreadPool = null;
	
	public LuceneIndexerThread(List luceneThreadPool, int offsetInterval, List[] contentQueue, int maxValue)
	{
		_luceneThreadPool = luceneThreadPool;
		_offsetInterval = offsetInterval;
		_contentQueue = contentQueue;
		_maxValue=maxValue;
	}
	
	public void run()
	{
		log.info("run: Entering run in LuceneIndexerThread");
		LuceneWorkerThread worker=null;
		synchronized(lock){
			worker=new LuceneWorkerThread(_contentQueue,_offset,Thread.currentThread().getName(), _maxValue);
			_offset=_offset+_offsetInterval;
		}
		worker.processContentQueue();
		_offset=0;
		synchronized(_contentQueue)
		{
			System.out.println(_class+ "LuceneIndexerThread: Removed Thread "+Thread.currentThread().getName());
			_luceneThreadPool.remove(Thread.currentThread());
			if(_luceneThreadPool.size() == 0){
				log.info("LuceneIndexerThread: Content Queue notifying...");
				_contentQueue.notify();
			}
		}
		log.info("LuceneIndexerThread: Stopping Thread "+Thread.currentThread().getName()+".. ");
	}
	
}
