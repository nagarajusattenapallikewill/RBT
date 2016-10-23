/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto;

import com.onmobile.apps.ringbacktones.dncto.rules.Condition;

/**
 * Contains DNCTO Constants.
 * 
 * @author vinayasimha.patil
 */
public interface DNCTOConstants
{
	/**
	 * Represents the subscriber status. Subscriber can be in one of the
	 * following status:
	 * <ul>
	 * <li>{@link #NEW}<br>
	 * Represents user is not a RBT subscriber.</li>
	 * <li>{@link #ACT}<br>
	 * Represents the subscriber is active.</li>
	 * <li>{@link #DCT}<br>
	 * Represents the subscriber is deactive.</li>
	 * <li>{@link #SUS}<br>
	 * Represents the subscriber is suspended.</li>
	 * <li>{@link #GRC}<br>
	 * Represents the subscriber is in grace.</li>
	 * <li>{@link #ERR}<br>
	 * Represents the subscriber is in error state.</li>
	 * </ul>
	 * 
	 * @author vinayasimha.patil
	 */
	public enum SubscriberStatus
	{
		/**
		 * Represents user is not a RBT subscriber.
		 */
		NEW,

		/**
		 * Represents the subscriber is active.
		 */
		ACT,

		/**
		 * Represents the subscriber is deactive.
		 */
		DCT,

		/**
		 * Represents the subscriber is suspended.
		 */
		SUS,

		/**
		 * Represents the subscriber is in grace.
		 */
		GRC,

		/**
		 * Represents the subscriber is in error state.
		 */
		ERR
	}

	/**
	 * Represents the condition type supported in RBT DNCTO Rules.
	 * {@link Condition} can have one of the
	 * following types:
	 * <ul>
	 * <li>{@link #AND}<br>
	 * Represents the AND operation.</li>
	 * <li>{@link #OR}<br>
	 * Represents the OR operation.</li>
	 * </ul>
	 * 
	 * @author vinayasimha.patil
	 */
	public enum ConditionType
	{
		/**
		 * Represents the AND operation.
		 */
		AND,

		/**
		 * Represents the OR operation.
		 */
		OR
	}
}
