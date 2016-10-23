package com.onmobile.apps.ringbacktones.rbtCommon;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface XmlField {
	String xmlAttrib() default ""; 
}
