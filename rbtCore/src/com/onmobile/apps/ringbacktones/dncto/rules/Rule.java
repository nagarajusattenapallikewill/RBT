/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto.rules;

import com.onmobile.apps.ringbacktones.dncto.DNCTOContext;
import com.onmobile.dnctoservice.exception.DNCTOException;

/**
 * Interface for representing the RBT DNCTO rule.
 * 
 * @author vinayasimha.patil
 */
public interface Rule
{
	/**
	 * Returns <tt>true</tt> if it is allowed to send promotions to the
	 * subscriber.
	 * 
	 * @param dnctoContext
	 *            the {@link DNCTOContext} containing data required for applying
	 *            rule
	 * @return returns <tt>true</tt> if it is allowed to send promotions to the
	 *         subscriber
	 * @throws DNCTOException
	 *             if unable to read the DNCTO data
	 */
	public boolean applyRule(DNCTOContext dnctoContext) throws DNCTOException;
}
