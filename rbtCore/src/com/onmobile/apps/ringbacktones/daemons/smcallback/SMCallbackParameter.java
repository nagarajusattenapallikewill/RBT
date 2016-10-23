/**
 * 
 */
package com.onmobile.apps.ringbacktones.daemons.smcallback;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author vinayasimha.patil
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface SMCallbackParameter
{
	String parameterName();
}
