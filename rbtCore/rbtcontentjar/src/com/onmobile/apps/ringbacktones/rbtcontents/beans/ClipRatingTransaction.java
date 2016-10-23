package com.onmobile.apps.ringbacktones.rbtcontents.beans;

import java.io.Serializable;
import java.util.Date;

public class ClipRatingTransaction implements Serializable
{
	private static final long serialVersionUID = 8888258918299913616L;

	private int clipId;
	private Date ratingDate;
	private int noOfVotes;
	private int sumOfRatings;
	private int likeVotes;
	private int dislikeVotes;

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
	 * @return the ratingDate
	 */
	public Date getRatingDate()
	{
		return ratingDate;
	}

	/**
	 * @param ratingDate
	 *            the ratingDate to set
	 */
	public void setRatingDate(Date ratingDate)
	{
		this.ratingDate = ratingDate;
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

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ClipRatingTransaction [clipId=");
		builder.append(clipId);
		builder.append(", dislikeVotes=");
		builder.append(dislikeVotes);
		builder.append(", likeVotes=");
		builder.append(likeVotes);
		builder.append(", noOfVotes=");
		builder.append(noOfVotes);
		builder.append(", ratingDate=");
		builder.append(ratingDate);
		builder.append(", sumOfRatings=");
		builder.append(sumOfRatings);
		builder.append("]");
		return builder.toString();
	}
}
