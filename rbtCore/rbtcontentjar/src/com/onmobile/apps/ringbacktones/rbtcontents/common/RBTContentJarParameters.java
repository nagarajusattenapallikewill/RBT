package com.onmobile.apps.ringbacktones.rbtcontents.common;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class RBTContentJarParameters {

	private static RBTContentJarParameters instance = new RBTContentJarParameters();
	
	private ResourceBundle bundle = null;
	
	private RBTContentJarParameters() {
		bundle = ResourceBundle.getBundle("rbtcontentjar");
	}
	
	public static RBTContentJarParameters getInstance() {
		return instance;
	}
	
	public String dbURL() {
		return bundle.getString("DB_URL");
	}
	
	public String defaultReportPath() {
		return bundle.getString("INDEX_PATH");
	}
	
	public String getParameter(String paramName) {
		String value = null;
		try{
			value = bundle.getString(paramName);
		}
		catch(MissingResourceException mr){
			// do not do anything
		}
		catch(Exception e){
			//do not do anything
		}
		return value;
	}
}
