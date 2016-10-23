package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.ArrayList;

public class ThreadManagerUtils {

	protected static void joinAndCheckThreadsStatus(ArrayList<GenericCacheThread> threadsList)
			throws InterruptedException, MultiThreadCacheInitException {
		for (int it = 0; it < threadsList.size(); it++) {
			GenericCacheThread st = threadsList.get(it);
			// join the main thread
			st.join();
			if (st.getException() != null) {
				throw new MultiThreadCacheInitException(st.getException());
			}
		}
	}

}
