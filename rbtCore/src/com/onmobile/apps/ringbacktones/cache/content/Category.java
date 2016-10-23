package com.onmobile.apps.ringbacktones.cache.content;

import java.util.Date;
import java.util.HashMap;
/**
 * @author Sreekar
 * @date 07/08/2008
 */
public class Category {
	private int _id;
	private String _name;
	private String _nameWavFile;
	private String _previewWavFile;
	private String _grammar;
	private int _type;
	private char _askMobileNumber;
	private String _greeting;
	private Date _startTime;
	private Date _endTime;
	private String _classType;
	private String _promoID;
	private String _smsAlias;
	private String _mmNumber;
	private HashMap<String, String> _languageGrammarMap;
	
	public Category(int id, String name, String nameWavFile,
			String previewWavFile, String grammar, int type,
			char askMobileNumber, String greeting, Date startTime,
			Date endTime, String classType, String promoID, String smsAlias,
			String mmNumber, HashMap<String, String> languageGrammarMap)
	{
		_id = id;
		_name = name;
		_nameWavFile = nameWavFile;
		_previewWavFile = previewWavFile;
		_grammar = grammar;
		_type = type;
		_askMobileNumber = askMobileNumber;
		_greeting = greeting;
		_startTime = startTime;
		_endTime = endTime;
		_classType = classType;
		_promoID = promoID;
		_smsAlias = smsAlias;
		_mmNumber = mmNumber;
		_languageGrammarMap = languageGrammarMap;
	}
	
	public int getID() {
		return _id;
	}
	
	public String getName() {
		return _name;
	}
	
	public String getNameWavFile() {
		return _nameWavFile;
	}
	
	public String getPreviewWavFile() {
		return _previewWavFile;
	}
	
	public String getGrammar() {
		return _grammar;
	}
	
	public int getType() {
		return _type;
	}
	
	public char getaskMobileNumber() {
		return _askMobileNumber;
	}
	
	public String getGreeting() {
		return _greeting;
	}
	
	public Date getStartTime() {
		return _startTime;
	}
	
	public Date getEndTime() {
		return _endTime;
	}
	
	public String getClassType() {
		return _classType;
	}
	
	public String getPromoID() {
		return _promoID;
	}
	
	public String getSMSAlias() {
		return _smsAlias;
	}
	
	public String getMMNumber() {
		return _mmNumber;
	}
	
	public HashMap<String, String> getLanguageGrammarMap() {
		return _languageGrammarMap;
	}
	
	public String toString() {
		return _name;
	}
}