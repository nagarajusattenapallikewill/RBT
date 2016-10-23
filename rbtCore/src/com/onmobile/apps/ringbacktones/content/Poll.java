package com.onmobile.apps.ringbacktones.content;

import java.sql.Connection;
import java.util.Date;

public interface Poll 
{
		/* Returns Poll ID*/
	 	public String pollID();

		
		/*Returns no of yes for inCircle*/
	    public int noOfYes_Incircle();

		/*Increment no of yes for inCircle*/
	    public int incrementNoOfYes_Incircle();
	    
	    /*Returns no of no for inCircle*/
	    public int noOfNo_Incircle();

		/*Increment no of no for inCircle*/
	    public int incrementNoOfNo_Incircle();
		
		
	    /*Returns no of yes for outCircle*/
	    public int noOfYes_Outcircle();

		/*Increment no of yes for outCircle*/
	    public int incrementNoOfYes_outcircle();

	    /*Returns no of no for outCircle*/
	    public int noOfNo_Outcircle();

		/*Increment no of no for outCircle*/
	    public int incrementNoOfNo_outcircle();

	    /*Returns no of yes for OtherOperator*/
	    public int noOfYes_OtherOperator();

		/*Increment no of yes for OtherOperator*/
	    public int incrementNoOfYes_OtherOperator();

	    /*Returns no of no for OtherOperator*/
	    public int noOfNo_OtherOperator();

		/*Increment no of no for OtherOperator*/
	    public int incrementNoOfNo_OtherOperator();

	    public int totalYesCount();
	    
	    public int totalNoCount();
}
