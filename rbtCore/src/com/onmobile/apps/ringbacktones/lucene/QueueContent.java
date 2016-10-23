package com.onmobile.apps.ringbacktones.lucene;
//package com.onmobile.apps.ringbacktones.subscriptions;

import org.apache.lucene.index.IndexWriter;

public class QueueContent {
	
	private IndexWriter indexWriter; 
	private String[] fieldValues;
	
	public QueueContent(IndexWriter indexWriter, String[] fieldValues){
		this.fieldValues=fieldValues;
		this.indexWriter=indexWriter;
	}
	public IndexWriter getIndexWriter() {
		return indexWriter;
	}
	public String[] getFieldValues() {
		return fieldValues;
	}
}
