package com.onmobile.apps.ringbacktones.content;

/*This class is related to OnVoxUser*/

public interface RBTLogin
{
	/*Returns name*/
	public String user();

	/*Returns prefix*/
	public String pwd();

	/*Returns url*/
	public String userType();
	
	/*Returns menu order in GUI*/
	public String[] menuOrder();
	
	/*Returns askPassword in GUI*/
	public String askPassword();
}