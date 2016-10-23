package com.onmobile.apps.ringbacktones.hunterFramework.debugger;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Indicates that the field is CLI Command parameter.
 * 
 * @author vinayasimha.patil
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface CLIField
{
	/**
	 * @return <tt>true</tt> if annotated field is mandatory in the CLI Command,
	 *         otherwise <tt>false</tt>
	 */
	boolean mandatory() default false;
}
