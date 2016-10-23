package com.onmobile.apps.ringbacktones.content;

import java.util.Date;
import java.util.HashMap;

/*This class is related to Categories*/

public interface Categories
{
	/*Returns category ID*/
	 public int id();

	/*Returns category name*/
	public String name();

	/*Returns category wave file*/
	public String nameFile();
	
	/*Returns language*/
	public String language();

	/*Returns category preview file*/
	public String previewFile();

	/*Returns category grammar*/
	public String grammar();

	/*Returns category type*/
	public int type();

	/*Returns category index*/
	public int index();

	/*Returns whether to prompt the user for a mobile number*/
	public boolean askMobileNumber();	
	
	/*Returns category greeting*/
	public String greeting();
	
	/*Returns category start time*/
	public Date startTime();
	
	/*Returns category end time*/
	public Date endTime();
	
	/*Returns parent ID*/
	public int parentID();

	/*Returns class type*/
	public String classType();
	
	/*Returns promo ID*/
	public String promoID();
	
	/*Returns Circle ID*/
	public String circleID();
	
	/* Return whether allowed for prepaid or not*/
	public char prepaidYes();
	
	/*Returns alias*/
	public String alias();
	
	public String mmNumber();
	
	public HashMap<String, String> languageGrammarMap();
	
	/*Converts date to string*/
	public String date(Date date);
}