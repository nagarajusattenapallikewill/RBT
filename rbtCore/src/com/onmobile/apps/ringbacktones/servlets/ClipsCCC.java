package com.onmobile.apps.ringbacktones.servlets;

import java.util.Comparator;

import com.onmobile.apps.ringbacktones.subscriptions.ClipGui;

public class ClipsCCC implements Comparable{

	private int clipId;
    private String clipName;
    private String wavFile;
    private String album;
    private String classType;
    private String artist;
    private String genre;
    private String subGenre;
    private String catId;
	public String getAlbum() {
		return album;
	}
	
	
	public static final Comparator ARTIST_COMPARATOR = new Comparator(){
		public int compare(Object a1, Object a2) {
			try{
			return (((ClipsCCC)a1).getArtist().compareToIgnoreCase(((ClipsCCC)a2).getArtist()));	
			}catch(Exception ex){
				throw new IllegalArgumentException(ex.getMessage());
			}
		}	
	};
	public static final Comparator ALBUM_COMPARATOR = new Comparator(){
		public int compare(Object a1, Object a2) {
			try{
			return (((ClipsCCC)a1).getAlbum().compareToIgnoreCase(((ClipsCCC)a2).getAlbum()));	
			}catch(Exception ex){
				throw new IllegalArgumentException(ex.getMessage());
			}
		}	
	};
	public static final Comparator VCODE_COMPARATOR = new Comparator(){
		public int compare(Object a1, Object a2) {
			try{
			return (((ClipsCCC)a1).getWavFile().compareToIgnoreCase(((ClipsCCC)a2).getWavFile()));	
			}catch(Exception ex){
				throw new IllegalArgumentException(ex.getMessage());
			}
		}	
	};
	public static final Comparator SONG_COMPARATOR = new Comparator(){
		public int compare(Object a1, Object a2) {
			try{
			return (((ClipsCCC)a1).getClipName().compareToIgnoreCase(((ClipsCCC)a2).getClipName()));	
			}catch(Exception ex){
				throw (new IllegalArgumentException(ex.getMessage()));
			}
		}	
	};
	
	public ClipsCCC(ClipGui cg , String catId,String parent,String child){
		this.album=cg.getAlbum();
		this.artist = cg.getArtist();
		this.catId = catId;
		this.classType = cg.getClassType();
		this.clipId = cg.getClipId();
		this.clipName = cg.getClipName();
		this.genre = parent;
		this.subGenre=child;
		this.wavFile=cg.getWavFile().substring(4, cg.getWavFile().length()-4);
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
	public String getCatId() {
		return catId;
	}
	public void setCatId(String catId) {
		this.catId = catId;
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
	public String getGenre() {
		return genre;
	}
	public void setGenre(String genre) {
		this.genre = genre;
	}
	public String getSubGenre() {
		return subGenre;
	}
	public void setSubGenre(String subGenre) {
		this.subGenre = subGenre;
	}
	public String getWavFile() {
		return wavFile;
	}
	public void setWavFile(String wavFile) {
		this.wavFile = wavFile;
	}
	public int compareTo(Object arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
}
