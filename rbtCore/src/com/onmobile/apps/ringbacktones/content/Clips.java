package com.onmobile.apps.ringbacktones.content;

import java.util.Date;

/*This class is related to Clips*/

public interface Clips
{
	/*Returns clip ID*/
	 public int id();

	/*Returns clip name*/
	public String name();

	/*Returns clip name wav file*/
	public String nameFile();

	/*Returns clip preview file*/
	public String previewFile();

	/*Returns complete clip file*/
	public String wavFile();

	/*Returns clip grammar*/
	public String grammar();
	
	/*Return clip alias*/
	public String alias();

	/*Returns whether to add an entry to access table*/
    public boolean  addAccess();
	
    /*Returns promotion ID*/
    public String promoID();
    
    /*Returns start time*/
    public Date startTime();
    
    /*Returns end time*/
    public Date endTime();
    
    /*Returns sms time*/
    public Date smsTime();
    
    /*Returns class type*/
    public String classType();
    
	/*Returns album*/
	public String album();
	
	/*Returns language*/
	public String lang();
	
	/*Returns whether the clip is active*/
	public boolean clipInList();
	
	/*Return play time*/
	public String playTime();
	
	/*Returns demo file*/
	public String demoFile();
    
    /*Return clip demo wave file*/ 
    //public String clipDemoWavFile();
	
	/*Returns artist*/
	public String artist();
    
	/* Return clipInfo*/
	public String clipInfo(); 

}