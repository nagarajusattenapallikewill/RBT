package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipBoundary;
import com.onmobile.apps.ringbacktones.rbtcontents.common.RBTContentJarParameters;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.ClipsDAO;
import com.onmobile.apps.ringbacktones.rbtcontents.dao.DataAccessException;

public class ClipThreadManager {

	private static final Logger log = Logger.getLogger(ClipThreadManager.class);
	
	int noOfThreads = -1;
	
	ClipBoundary clipBoundary = null;
	
	public ClipThreadManager(int noOfThreads) {
		this.noOfThreads = noOfThreads;
	}
	
	public void setClipBoundary(ClipBoundary clipBoundary) {
		this.clipBoundary = clipBoundary;
	}

	public void startThreads() throws InterruptedException, MultiThreadCacheInitException, DataAccessException {
		log.info("Starting clip and clipAlbum threads. clipBoundary: "
				+ clipBoundary);
		long l1 = System.currentTimeMillis();
		List<Clip> clips = ClipsDAO.getClipsInBetween(clipBoundary.getStartIndex(), clipBoundary.getEndIndex());
		long l2 = System.currentTimeMillis();
		log.info("Fetched clips from database. number of clips found: "
				+ clips.size() + ", time taken to fetch in ms: " + (l2 - l1)
				+ ", clipBoundary: " + clipBoundary);
		ArrayList<GenericCacheThread> threadsList = new ArrayList<GenericCacheThread>(noOfThreads); 
		int batchSize = clips.size() / noOfThreads;
		int it = 0;
		for (it = 0; it < noOfThreads; it++) {
			List<Clip> subList = null;
			if (it == noOfThreads - 1) {
				// in last iteration consider remaining all records
				subList = clips.subList(it * batchSize, clips.size());
			} else {
				subList = clips.subList(it * batchSize, (it + 1) * batchSize);
			}
			ClipThread ccThread = new ClipThread("ClipsThread" + (it+1), subList);
			ccThread.start();
			log.info(ccThread.getName() + " is started with clips: "
					+ subList.size() + ", clipBoundary: " + clipBoundary);
			threadsList.add(ccThread);
		}

		long l3 = System.currentTimeMillis();
		log.info("Total time taken to Cache Clips is:" + (l3 - l2)
				+ ", clipBoundary: " + clipBoundary);
		
		if (RBTContentJarParameters.getInstance().getParameter(
				ClipThread.support_album_alphabet_index) == null
				|| !RBTContentJarParameters.getInstance().getParameter(
						ClipThread.support_album_alphabet_index).equalsIgnoreCase("TRUE")) {
			
			int j = 0;
			for (j = 0; j < noOfThreads; j++) {
				List<Clip> subList = null;
				if (j == noOfThreads - 1) {
					// in last iteration consider remaining all records
					subList = clips.subList(j * batchSize, clips.size());
				} else {
					subList = clips.subList(j * batchSize, (j + 1) * batchSize);
				}
				
				ClipAlbumThread cacThread = new ClipAlbumThread("AlbumClipsThread"+(j + 1), subList);
				cacThread.start();
				log.info(cacThread.getName() + " is started with clips: "
						+ subList.size() + ", clipBoundary: " + clipBoundary);
				threadsList.add(cacThread);
			}
		}

		log.info("Waiting to finish clip and albumClip threads. clipBoundary: "
				+ clipBoundary);

		ThreadManagerUtils.joinAndCheckThreadsStatus(threadsList);
		long l4 = System.currentTimeMillis();
		log.info("Total time taken to Cache ClipAlbum is:" + (l4 - l3)
				+ ", clipBoundary: " + clipBoundary);
		
		log.info("Finished cacheing clipBoundary: " + clipBoundary
				+ ", Total time taken to Cache Clip and ClipAlbum is:"
				+ (l4 - l1));
	}
	
}
