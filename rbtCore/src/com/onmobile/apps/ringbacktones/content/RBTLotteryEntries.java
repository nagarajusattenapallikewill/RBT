package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/**
 * @author sridhar.sindiri
 *
 */
public interface RBTLotteryEntries {

	public long sequenceID();

	public int lotteryID();

	public String subscriberID();

	public Date entryTime();

	public String lotteryNumber();

	public int clipID();
}
