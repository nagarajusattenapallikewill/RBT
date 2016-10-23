package com.onmobile.apps.ringbacktones.content;

import java.sql.Connection;
import java.util.Date;

/*This class is related to Access*/

public interface Access
{
	/*Returns clip ID*/
	 public int clipID();

	/*Returns clip name*/
	public String clipName();

	/*Returns year*/
	public String year();

	/*Returns month*/
	public String month();

	/*Returns no of previews*/
    public int noOfPreviews();

	/*Increment no of previews*/
    public void incrementNoOfPreviews();
	
	/*Returns no of access*/
    public int noOfAccess();

	/*Increment no of access*/
    public void incrementNoOfAccess();
    
    /*Returns no of plays*/
	public int noOfPlays();

    /*Returns Access Date*/
	public Date accessDate();
	
	/*Increment no of plays*/
	public void incrementNoOfPlays();

	/*To update record in the database*/
	public void update(String dbUrl, int nConn);
	
	/*To update record in the database*/
	public void update(Connection conn);

}