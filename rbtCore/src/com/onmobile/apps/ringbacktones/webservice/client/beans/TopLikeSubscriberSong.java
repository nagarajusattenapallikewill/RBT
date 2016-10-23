package com.onmobile.apps.ringbacktones.webservice.client.beans;

public class TopLikeSubscriberSong {
	private int clipId;
	private long count;
	private String subscriberId;
	private int catId;

	public TopLikeSubscriberSong(int clipId, long count, String subscriberId,
			int catId) {
		this.clipId = clipId;
		this.count = count;
		this.subscriberId = subscriberId;
		this.catId = catId;
	}

	public TopLikeSubscriberSong(int clipId, long count) {
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

	public String getSubscriberId() {
		return subscriberId;
	}

	public void setSubscriberId(String subscriberId) {
		this.subscriberId = subscriberId;
	}

	public int getCatId() {
		return catId;
	}

	public void setCatId(int catId) {
		this.catId = catId;
	}

}
