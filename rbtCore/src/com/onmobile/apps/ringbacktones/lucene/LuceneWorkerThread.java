package com.onmobile.apps.ringbacktones.lucene;


import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import com.tangentum.phonetix.DoubleMetaphone;

public class LuceneWorkerThread {
	
	private static Logger log=Logger.getLogger(LuceneWorkerThread.class);
	private static String _class="LuceneWorkerThread";
	private static DoubleMetaphone m_metaphone =  new DoubleMetaphone(6);
	private int offset=0;
	private int maxProcessValue=10;
	private String threadName=null;
	private String[] fields = {"vcode","PARENT_CAT_NAME","SUB_CAT_NAME","song","album","artist","parentCatId","subCatId"};
	private List[] contentQueue=null;
	
	public LuceneWorkerThread(String[] fields){
		this.fields=fields;
	}
	
	public LuceneWorkerThread(List[] contentQueue, int offset, String threadName, int maxProcessValue){
		this.offset=offset;
		this.contentQueue=contentQueue;
		this.threadName=threadName;
		this.maxProcessValue=maxProcessValue;
	}
	
	public void processContentQueue(){
		int maxValue=offset+maxProcessValue;
		log.info("LuceneWorkerThread:: "+"processContentQueue MaxValue for "+threadName+"="+maxValue);
		log.info("LuceneWorkerThread:: processContentQueue  "+threadName+"="+offset);
		for(int j=offset;j<maxValue;j++){
			if(contentQueue[j]!=null){
				for(int i=0;i<contentQueue[j].size();i++){
					QueueContent queueContent=(QueueContent)contentQueue[j].get(i);
					createModifyDocuments(queueContent.getIndexWriter(),fields,queueContent.getFieldValues(),Thread.currentThread().getName());
				}
			}
		}
	}
	
	public boolean createModifyDocuments(IndexWriter iw, String[] fieldNames,
			String[] fieldValues, String threadName){
		String keyWord = null, keyValue = null;
		if (fieldNames == null || fieldNames.length <= 1 || fieldValues == null
				|| fieldValues.length <= 1
				|| fieldNames.length != fieldValues.length)
			return false;
		try{
			keyWord = fieldNames[0];
			keyValue = fieldValues[0];
			//Creating doc and adding unique id to Keyword of the doc
			Document doc = new Document();
			Field f3=new Field(keyWord, keyValue, Field.Store.YES, Field.Index.UN_TOKENIZED); 
			doc.add(f3);
			//putting rest of the fields in SearchKeyword
			for (int i = 1; i < fieldNames.length; i++){
				String SearchKeyword = " ";
				if (fieldValues[i] == null)
					continue;
				try{
					Integer.parseInt(fieldValues[i]);
					//continue;
				}
				catch(Exception e){
					//Tools.logDetail(_class, "GuiSearch::Exception caught ", e.getMessage());
				}
				if (fieldNames[i].equalsIgnoreCase("vcode")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("clipId")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("clipRbtWavFile")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("PARENT_CAT_NAME")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("SUB_CAT_NAME")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("parentCatId")){
					//log.info("Inside parentCatId and indexing "+fieldValues[i]);
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("subCatId")){
					//log.info("Inside subCatId and indexing "+fieldValues[i]);
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					continue;
				}
				SearchKeyword = SearchKeyword + " "+ getPhonetics(fieldValues[i], false) + " "
					+ getPhonetics(fieldValues[i], true);
				StringTokenizer stk = new StringTokenizer(fieldValues[i], " ");
				String wordByWord = "";
				while (stk.hasMoreTokens()){
					String tkn = (String) stk.nextToken();
					wordByWord = wordByWord + getPhonetics(tkn, true) + " ";
					SearchKeyword = SearchKeyword + " "
					+ getPhonetics(tkn, true);
				}
				SearchKeyword = SearchKeyword + " " + wordByWord;
				SearchKeyword = SearchKeyword.trim() + " "+ getSubstring(fieldValues[i], 3);
				SearchKeyword = SearchKeyword.trim() + " "+ getSubstring(fieldValues[i], 4);
				Field f2=new Field(fieldNames[i], SearchKeyword.trim(),Field.Store.YES,Field.Index.TOKENIZED);
				doc.add(f2);
				
			}
			try{
				iw.addDocument(doc);
			}
			catch(Exception e){
				log.error(e.getMessage());
				e.printStackTrace();
				return false;
			}
		}
		catch (Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private static String getPhonetics(String input, boolean trimSpace){
		if (input == null)
			return null;
		try{
			Integer.parseInt(input);
			return null;
		}
		catch(Exception e){
			//log.error(e.getMessage());
		}
		if (trimSpace){
			StringTokenizer stk = new StringTokenizer(input, " ");
			if (stk.hasMoreTokens()){
				input = " ";
				while (stk.hasMoreTokens()){
					input = input.trim() + (String) stk.nextToken();
				}
			}
		}
		String phonem=null;
		if(input != null)
			phonem = m_metaphone.generateKey(input);
		if(phonem != null)
			phonem=phonem.trim();
		return phonem;
	}
	
	private static String getSubstring(String clipName, int n){
		String output = "";
		StringTokenizer stk = new StringTokenizer(clipName, " ");
		clipName = "";
		int i = 0;
		while (stk.hasMoreTokens()){
			clipName = clipName + (String) stk.nextToken();
		}
		int size = clipName.length();
		i = 0;
		while (i < (size - n)){
			output = output + clipName.substring(i, (i + n)) + " ";
			i++;
		}
		output = output + clipName.substring(i);
		return output;
	}

}
