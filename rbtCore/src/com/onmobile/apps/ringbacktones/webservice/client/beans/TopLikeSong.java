package com.onmobile.apps.ringbacktones.webservice.client.beans;

public class TopLikeSong {
	private int clipId;
	private long count;

	public TopLikeSong(int clipId, long count) {
		this.clipId = clipId;
		this.count = count;
	}

	public int getClipId() {
		return clipId;
	}

	public void setClipId(int clipId) {
		this.clipId = clipId;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

}
