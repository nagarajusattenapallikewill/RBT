package com.onmobile.apps.ringbacktones.lucene.generic.msearch.beans;

import java.util.Arrays;
import java.util.List;

import org.apache.solr.client.solrj.beans.Field;

public class RBTMSearchResponse {

	@Field("DOWNLOADCOUNT")
	private int count;
	@Field("CONTENT_TYPE")
	private String title;
	@Field("LANGUAGE")
	private String language;
	@Field("ALBUM")
	private String album;
	@Field("IDEARBTID") 
	private String ideaRBTId; //TODO check id field name can be dynamic
	@Field("SINGER")
	private List<String> singers;

	public RBTMSearchResponse() {
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	public String getIdeaRBTId() {
		return ideaRBTId;
	}

	public void setIdeaRBTId(String telRBTId) {
		this.ideaRBTId = telRBTId;
	}

	public List<String> getSingers() {
		return singers;
	}

	public void setSingers(List<String> singers) {
		this.singers = singers;
	}

	@Override
	public String toString() {
		return "RBTMSearchResponse [count=" + count + ", title=" + title
				+ ", language=" + language + ", album=" + album + ", IDEARBTId="
				+ ideaRBTId + ", singers=" + singers + "]";
	}

}
