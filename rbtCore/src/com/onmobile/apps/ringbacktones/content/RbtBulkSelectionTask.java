package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/* This class is related to the Rbt Bulk Selection Task  added by eswar krishna*/
public interface RbtBulkSelectionTask 
{
	/* returns file id */
	public int fileID();
	
	/* returns the file name*/
	public String fileName();

	/* returns the activated By */
	public String act_By();

	/* returns the activation Class (Subscription Class)*/
	public String activation_Class();

	/* returns the Selection Class (Selection Class)*/
	public String selection_Class();

	/* returns the Status of the file*/
	public String file_Status();

	/* returns the uploaded date*/
	public Date uploaded_Date();

	/* returns the processed date */
	public Date processed_Date();

	/* returns the activation info */
	public String activation_Info();

}
