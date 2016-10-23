/**
 * 
 */
package com.onmobile.apps.ringbacktones.dncto.rules;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Used to indicate that the field is RBT DNCTO rule attribute.
 * 
 * @author vinayasimha.patil
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RuleAttribute
{
	/**
	 * @return <tt>true</tt> if annotated field is mandatory in RBT DNCTO rule,
	 *         otherwise <tt>false</tt>
	 */
	boolean mandatory() default false;
}
