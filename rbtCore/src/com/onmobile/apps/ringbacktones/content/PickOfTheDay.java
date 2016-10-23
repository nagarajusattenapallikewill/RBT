package com.onmobile.apps.ringbacktones.content;

/*This class is related to PickOfTheDay*/

public interface PickOfTheDay
{
	/*Returns category ID*/
	public int categoryID();

	/*Returns clip ID*/
	public int clipID();

	/*Returns play date*/
	public String playDate();
	
	/*Return circle ID*/
	public String circleID();
	
	/*Returns prepaidYes value*/
	public char prepaidYes();
    
     /*Returns profile*/ 
    public String profile();
    
    /*Returns language*/ 
    public String language();
    
}