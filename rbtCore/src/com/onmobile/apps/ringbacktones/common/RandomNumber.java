/**
 * 
 */
package com.onmobile.apps.ringbacktones.common;

import java.util.Random;

/**
 * @author vinayasimha.patil
 * 
 */
public class RandomNumber
{
	private Random random = null;
	private int noOfDigits;
	private int maxRandomNumber;

	/**
	 * @param noOfDigits
	 */
	public RandomNumber(int noOfDigits)
	{
		super();
		this.noOfDigits = noOfDigits;

		random = new Random();
		maxRandomNumber = (int) Math.pow(10, noOfDigits);
	}

	public int nextInt()
	{
		return Integer.parseInt(nextNumber());
	}

	public String nextNumber()
	{
		StringBuilder builder = new StringBuilder(noOfDigits);
		while (builder.length() < noOfDigits)
		{
			int randomNumber = random.nextInt(maxRandomNumber);
			builder.append(randomNumber);
		}

		return builder.substring(0, noOfDigits);
	}
}
