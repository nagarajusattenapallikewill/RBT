package com.onmobile.apps.ringbacktones.rbtcontents.cache.thread;

import java.util.List;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheKey;

public class ClipAlbumThread extends GenericCacheThread {

	private static final Logger log = Logger.getLogger(ClipAlbumThread.class);

	public ClipAlbumThread(String name, List records) {
		super(name, records);
	}

	@Override
	public void processRecord(Object obj) throws Exception {
		Clip clip = (Clip) obj;
		if (null == clip) {
			throw new IllegalArgumentException("The parameter clip can't be null");
		}
		String clipAlbum = clip.getAlbum();

		// Only for Esia (UGC Clips) subscriber id will be stored in album column
		if (null != clipAlbum && clipAlbum.length() > 0) {
			try {
				Long.parseLong(clipAlbum);
				putClipInAlbumCache(clipAlbum, clip.getClipId());
			} catch (NumberFormatException ne) {
				// ignore
			}
		}
	}

	@Override
	public void finalProcess() throws Exception {
		// nothing to do
	}

	private void putClipInAlbumCache(String album, int clipId) {
		long l1 = System.currentTimeMillis();
		String albumKey = RBTCacheKey.getAlbumCacheKey(album);
		String clipIds = (String) mc.get(albumKey);
		if (clipIds == null) {
			mc.set(albumKey, RBTCacheKey.getClipIdCacheKey(clipId));
		} else {
			String sClipId = RBTCacheKey.getClipIdCacheKey(clipId);
			mc.set(albumKey, clipIds + "," + sClipId);
		}
		long l2 = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.info(getName() + " is finished Album Cache. clipId: " + clipId
					+ ", album: " + album + ", TimeTaken: " + (l2 - l1) + " ms");
		}

	}

}
