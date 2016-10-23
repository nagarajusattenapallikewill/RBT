package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;

public class ClipRating implements Serializable
{
	private static final long serialVersionUID = -5626214737566232650L;

	private int clipId;
	private int noOfVotes;
	private int sumOfRatings;
	private int likeVotes;
	private int dislikeVotes;
	private int noOfDownloads;

	/**
	 * 
	 */
	public ClipRating()
	{

	}

	/**
	 * @param clipId
	 * @param noOfVotes
	 * @param sumOfRatings
	 * @param likeVotes
	 * @param dislikeVotes
	 * @param noOfDownloads
	 */
	public ClipRating(int clipId, int noOfVotes, int sumOfRatings,
			int likeVotes, int dislikeVotes, int noOfDownloads)
	{
		super();
		this.clipId = clipId;
		this.noOfVotes = noOfVotes;
		this.sumOfRatings = sumOfRatings;
		this.likeVotes = likeVotes;
		this.dislikeVotes = dislikeVotes;
		this.noOfDownloads = noOfDownloads;
	}

	/**
	 * @return the clipId
	 */
	public int getClipId()
	{
		return clipId;
	}

	/**
	 * @param clipId
	 *            the clipId to set
	 */
	public void setClipId(int clipId)
	{
		this.clipId = clipId;
	}

	/**
	 * @return the noOfVotes
	 */
	public int getNoOfVotes()
	{
		return noOfVotes;
	}

	/**
	 * @param noOfVotes
	 *            the noOfVotes to set
	 */
	public void setNoOfVotes(int noOfVotes)
	{
		this.noOfVotes = noOfVotes;
	}

	/**
	 * @return the sumOfRatings
	 */
	public int getSumOfRatings()
	{
		return sumOfRatings;
	}

	/**
	 * @param sumOfRatings
	 *            the sumOfRatings to set
	 */
	public void setSumOfRatings(int sumOfRatings)
	{
		this.sumOfRatings = sumOfRatings;
	}

	/**
	 * @return the likeVotes
	 */
	public int getLikeVotes()
	{
		return likeVotes;
	}

	/**
	 * @param likeVotes
	 *            the likeVotes to set
	 */
	public void setLikeVotes(int likeVotes)
	{
		this.likeVotes = likeVotes;
	}

	/**
	 * @return the dislikeVotes
	 */
	public int getDislikeVotes()
	{
		return dislikeVotes;
	}

	/**
	 * @param dislikeVotes
	 *            the dislikeVotes to set
	 */
	public void setDislikeVotes(int dislikeVotes)
	{
		this.dislikeVotes = dislikeVotes;
	}

	/**
	 * @return the noOfDownloads
	 */
	public int getNoOfDownloads()
	{
		return noOfDownloads;
	}

	/**
	 * @param noOfDownloads
	 *            the noOfDownloads to set
	 */
	public void setNoOfDownloads(int noOfDownloads)
	{
		this.noOfDownloads = noOfDownloads;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ClipRating [clipId=");
		builder.append(clipId);
		builder.append(", dislikeVotes=");
		builder.append(dislikeVotes);
		builder.append(", likeVotes=");
		builder.append(likeVotes);
		builder.append(", noOfDownloads=");
		builder.append(noOfDownloads);
		builder.append(", noOfVotes=");
		builder.append(noOfVotes);
		builder.append(", sumOfRatings=");
		builder.append(sumOfRatings);
		builder.append("]");
		return builder.toString();
	}
}
