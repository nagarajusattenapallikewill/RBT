package com.onmobile.apps.ringbacktones.subscriptions;

import java.util.Date;

import com.onmobile.apps.ringbacktones.content.Clips;

public class ClipGui {
	 private int clipId;
	    private String clipName;
	    private String wavFile;
	    private String album;
	    private String classType;
	    private String artist;
	    public ClipGui(Clips clip)
	    {
	        this.clipId = clip.id();
	        if(clip.name()==null || clip.name().equalsIgnoreCase("null"))
	        	this.clipName="na";
	        else
	        	this.clipName = clip.name();
	        if(clip.wavFile()==null || clip.wavFile().equalsIgnoreCase("null"))
	        	this.wavFile="na";
	        else
	        	this.wavFile = clip.wavFile();
	        if(clip.classType()==null || clip.classType().equalsIgnoreCase("null"))
	        	this.classType="na";
	        else
	        	this.classType = clip.classType();
	        if(clip.artist()==null || clip.artist().equalsIgnoreCase("null"))
	        	this.artist="na";
	        else
	        	this.artist = clip.artist();
	        if(clip.album()==null || clip.album().equalsIgnoreCase("null"))
	        	this.album="na";
	        else
	        	this.album = clip.album();
	    }
	    
	    public ClipGui(int id, String name, String wavFile, 
					String classType, String album, String artist){ 
		   this.clipId = id; 
		   if(name==null||name.equalsIgnoreCase("null")){
			   this.clipName="na";
		   }else
			   this.clipName=name;
		   if(wavFile==null || wavFile.equalsIgnoreCase("null"))
	        	this.wavFile="na";
	        else
	        	this.wavFile = wavFile;
	        if(classType==null || classType.equalsIgnoreCase("null"))
	        	this.classType="na";
	        else
	        	this.classType = classType;
	        if(artist==null || artist.equalsIgnoreCase("null"))
	        	this.artist="na";
	        else
	        	this.artist = artist;
	        if(album==null || album.equalsIgnoreCase("null"))
	        	this.album="na";
	        else
	        	this.album = album;
	   }

		public String getAlbum() {
			return album;
		}

		public void setAlbum(String album) {
			this.album = album;
		}

		public String getArtist() {
			return artist;
		}

		public void setArtist(String artist) {
			this.artist = artist;
		}

		public String getClassType() {
			return classType;
		}

		public void setClassType(String classType) {
			this.classType = classType;
		}

		public int getClipId() {
			return clipId;
		}

		public void setClipId(int clipId) {
			this.clipId = clipId;
		}

		public String getClipName() {
			return clipName;
		}

		public void setClipName(String clipName) {
			this.clipName = clipName;
		}

		public String getWavFile() {
			return wavFile;
		}

		public void setWavFile(String wavFile) {
			this.wavFile = wavFile;
		} 

}
