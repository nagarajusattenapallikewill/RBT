package com.onmobile.apps.ringbacktones.rbtCommon;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface XmlType {
	public String elementPath() default "";

	public String childMethodName() default ""; 
}
