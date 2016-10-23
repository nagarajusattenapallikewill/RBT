/**
 * 
 */
package com.onmobile.apps.ringbacktones.webservice.client.requests;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author vinayasimha.patil
 *
 */
public class ContentRequest extends Request
{
	private Integer contentID = null;
	private String contentType = null;
	private Boolean isPrepaid = null;
	private String language = null;
	private Integer pageNo = null;
	private Integer startIndex = null;
	private Integer endIndex = null;

	private Integer clipID = null;
	private String name = null;
	private String nameWavFile = null;
	private String previewWavFile = null;
	private String rbtWavFile = null;
	private String grammar = null;
	private String smsAlias = null;
	private String promoID = null;
	private String classType = null;
	private Date startTime = null;
	private Date endTime = null;
	private String album = null;
	private String demoWavFile = null;
	private String artist = null;
	private String info = null;

	private List<Integer> clipIDs = null;
	private Integer rating = null;
	
	private String searchtype = null;
	private String type = null;
	private String searchText = null;
	private String  maxResult = null;
	
	/**
	 * 
	 */
	public ContentRequest()
	{
		super(null);
	}

	/**
	 * @param subscriberID
	 */
	public ContentRequest(String subscriberID)
	{
		super(subscriberID);
	}

	/**
	 * @param subscriberID
	 * @param contentID
	 * @param contentType
	 * @param circleID
	 * @param isPrepaid
	 * @param pageNo
	 */
	public ContentRequest(String subscriberID, Integer contentID,
			String contentType, String circleID, Boolean isPrepaid,
			Integer pageNo)
	{
		super(subscriberID, circleID);
		this.contentID = contentID;
		this.contentType = contentType;
		this.isPrepaid = isPrepaid;
		this.pageNo = pageNo;
	}

	/**
	 * @param subscriberID
	 * @param contentID
	 * @param contentType
	 * @param circleID
	 * @param isPrepaid
	 * @param language
	 * @param pageNo
	 */
	public ContentRequest(String subscriberID, Integer contentID,
			String contentType, String circleID, Boolean isPrepaid,
			String language, Integer pageNo)
	{
		super(subscriberID, circleID);
		this.contentID = contentID;
		this.contentType = contentType;
		this.isPrepaid = isPrepaid;
		this.language = language;
		this.pageNo = pageNo;
	}

	/**
	 * @param subscriberID
	 * @param contentID
	 * @param contentType
	 * @param circleID
	 * @param isPrepaid
	 * @param startIndex
	 * @param endIndex
	 */
	public ContentRequest(String subscriberID, Integer contentID,
			String contentType, String circleID, Boolean isPrepaid,
			Integer startIndex, Integer endIndex)
	{
		super(subscriberID, circleID);
		this.contentID = contentID;
		this.contentType = contentType;
		this.isPrepaid = isPrepaid;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	/**
	 * @param subscriberID
	 * @param contentID
	 * @param contentType
	 * @param circleID
	 * @param isPrepaid
	 * @param language
	 * @param pageNo
	 * @param startIndex
	 * @param endIndex
	 */
	public ContentRequest(String subscriberID, Integer contentID,
			String contentType, String circleID, Boolean isPrepaid,
			String language, Integer pageNo, Integer startIndex,
			Integer endIndex)
	{
		super(subscriberID, circleID);
		this.contentID = contentID;
		this.contentType = contentType;
		this.isPrepaid = isPrepaid;
		this.language = language;
		this.pageNo = pageNo;
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	}

	/**
	 * @param language
	 * @param clipID
	 * @param name
	 * @param nameWavFile
	 * @param previewWavFile
	 * @param rbtWavFile
	 * @param grammar
	 * @param smsAlias
	 * @param promoID
	 * @param classType
	 * @param startTime
	 * @param endTime
	 * @param album
	 * @param demoWavFile
	 * @param artist
	 * @param info
	 */
	public ContentRequest(String language, Integer clipID, String name,
			String nameWavFile, String previewWavFile, String rbtWavFile,
			String grammar, String smsAlias, String promoID, String classType,
			Date startTime, Date endTime, String album, String demoWavFile,
			String artist, String info)
	{
		super(null);
		this.language = language;
		this.clipID = clipID;
		this.name = name;
		this.nameWavFile = nameWavFile;
		this.previewWavFile = previewWavFile;
		this.rbtWavFile = rbtWavFile;
		this.grammar = grammar;
		this.smsAlias = smsAlias;
		this.promoID = promoID;
		this.classType = classType;
		this.startTime = startTime;
		this.endTime = endTime;
		this.album = album;
		this.demoWavFile = demoWavFile;
		this.artist = artist;
		this.info = info;
	}

	/**
	 * @return the contentID
	 */
	public Integer getContentID()
	{
		return contentID;
	}

	/**
	 * @param contentID the contentID to set
	 */
	public void setContentID(Integer contentID)
	{
		this.contentID = contentID;
	}

	/**
	 * @return the contentType
	 */
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType)
	{
		this.contentType = contentType;
	}

	/**
	 * @return the isPrepaid
	 */
	public Boolean getIsPrepaid()
	{
		return isPrepaid;
	}

	/**
	 * @param isPrepaid the isPrepaid to set
	 */
	public void setIsPrepaid(Boolean isPrepaid)
	{
		this.isPrepaid = isPrepaid;
	}

	/**
	 * @return the language
	 */
	public String getLanguage()
	{
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language)
	{
		this.language = language;
	}

	/**
	 * @return the pageNo
	 */
	public Integer getPageNo()
	{
		return pageNo;
	}

	/**
	 * @param pageNo the pageNo to set
	 */
	public void setPageNo(Integer pageNo)
	{
		this.pageNo = pageNo;
	}

	/**
	 * @return the startIndex
	 */
	public Integer getStartIndex()
	{
		return startIndex;
	}

	/**
	 * @param startIndex the startIndex to set
	 */
	public void setStartIndex(Integer startIndex)
	{
		this.startIndex = startIndex;
	}

	/**
	 * @return the endIndex
	 */
	public Integer getEndIndex()
	{
		return endIndex;
	}

	/**
	 * @param endIndex the endIndex to set
	 */
	public void setEndIndex(Integer endIndex)
	{
		this.endIndex = endIndex;
	}

	/**
	 * @return the clipID
	 */
	public Integer getClipID()
	{
		return clipID;
	}

	/**
	 * @param clipID the clipID to set
	 */
	public void setClipID(Integer clipID)
	{
		this.clipID = clipID;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the nameWavFile
	 */
	public String getNameWavFile()
	{
		return nameWavFile;
	}

	/**
	 * @param nameWavFile the nameWavFile to set
	 */
	public void setNameWavFile(String nameWavFile)
	{
		this.nameWavFile = nameWavFile;
	}

	/**
	 * @return the previewWavFile
	 */
	public String getPreviewWavFile()
	{
		return previewWavFile;
	}

	/**
	 * @param previewWavFile the previewWavFile to set
	 */
	public void setPreviewWavFile(String previewWavFile)
	{
		this.previewWavFile = previewWavFile;
	}

	/**
	 * @return the rbtWavFile
	 */
	public String getRbtWavFile()
	{
		return rbtWavFile;
	}

	/**
	 * @param rbtWavFile the rbtWavFile to set
	 */
	public void setRbtWavFile(String rbtWavFile)
	{
		this.rbtWavFile = rbtWavFile;
	}

	/**
	 * @return the grammar
	 */
	public String getGrammar()
	{
		return grammar;
	}

	/**
	 * @param grammar the grammar to set
	 */
	public void setGrammar(String grammar)
	{
		this.grammar = grammar;
	}

	/**
	 * @return the smsAlias
	 */
	public String getSmsAlias()
	{
		return smsAlias;
	}

	/**
	 * @param smsAlias the smsAlias to set
	 */
	public void setSmsAlias(String smsAlias)
	{
		this.smsAlias = smsAlias;
	}

	/**
	 * @return the promoID
	 */
	public String getPromoID()
	{
		return promoID;
	}

	/**
	 * @param promoID the promoID to set
	 */
	public void setPromoID(String promoID)
	{
		this.promoID = promoID;
	}

	/**
	 * @return the classType
	 */
	public String getClassType()
	{
		return classType;
	}

	/**
	 * @param classType the classType to set
	 */
	public void setClassType(String classType)
	{
		this.classType = classType;
	}

	/**
	 * @return the startTime
	 */
	public Date getStartTime()
	{
		return startTime;
	}

	/**
	 * @param startTime the startTime to set
	 */
	public void setStartTime(Date startTime)
	{
		this.startTime = startTime;
	}

	/**
	 * @return the endTime
	 */
	public Date getEndTime()
	{
		return endTime;
	}

	/**
	 * @param endTime the endTime to set
	 */
	public void setEndTime(Date endTime)
	{
		this.endTime = endTime;
	}

	/**
	 * @return the album
	 */
	public String getAlbum()
	{
		return album;
	}

	/**
	 * @param album the album to set
	 */
	public void setAlbum(String album)
	{
		this.album = album;
	}

	/**
	 * @return the demoWavFile
	 */
	public String getDemoWavFile()
	{
		return demoWavFile;
	}

	/**
	 * @param demoWavFile the demoWavFile to set
	 */
	public void setDemoWavFile(String demoWavFile)
	{
		this.demoWavFile = demoWavFile;
	}

	/**
	 * @return the artist
	 */
	public String getArtist()
	{
		return artist;
	}

	/**
	 * @param artist the artist to set
	 */
	public void setArtist(String artist)
	{
		this.artist = artist;
	}

	/**
	 * @return the info
	 */
	public String getInfo()
	{
		return info;
	}

	/**
	 * @param info the info to set
	 */
	public void setInfo(String info)
	{
		this.info = info;
	}

	/**
	 * @return the clipIDs
	 */
	public List<Integer> getClipIDs()
	{
		return clipIDs;
	}

	/**
	 * @param clipIDs
	 *            the clipIDs to set
	 */
	public void setClipIDs(List<Integer> clipIDs)
	{
		this.clipIDs = clipIDs;
	}

	/**
	 * @return the rating
	 */
	public Integer getRating()
	{
		return rating;
	}

	/**
	 * @param rating
	 *            the rating to set
	 */
	public void setRating(Integer rating)
	{
		this.rating = rating;
	}
	
	public String getSearchtype() {
		return searchtype;
	}

	public void setSearchtype(String searchtype) {
		this.searchtype = searchtype;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSearchText() {
		return searchText;
	}

	public void setSearchText(String searchText) {
		this.searchText = searchText;
	}

	public String getMaxResult() {
		return maxResult;
	}

	public void setMaxResult(String maxResult) {
		this.maxResult = maxResult;
	}

	/* (non-Javadoc)
	 * @see com.onmobile.apps.ringbacktones.webservice.client.requests.Request#getRequestParamsMap()
	 */
	@Override
	public HashMap<String, String> getRequestParamsMap()
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		HashMap<String, String> requestParams = super.getRequestParamsMap();

		if (contentID != null) requestParams.put(param_contentID, String.valueOf(contentID));
		if (contentType != null) requestParams.put(param_contentType, contentType);
		if (isPrepaid != null) requestParams.put(param_isPrepaid, (isPrepaid ? YES : NO));
		if (language != null) requestParams.put(param_language, language);
		if (pageNo != null) requestParams.put(param_pageNo, String.valueOf(pageNo));
		if (startIndex != null) requestParams.put(param_startIndex, String.valueOf(startIndex));
		if (endIndex != null) requestParams.put(param_endIndex, String.valueOf(endIndex));
		if (clipID != null) requestParams.put(param_clipID, String.valueOf(clipID));
		if (name != null) requestParams.put(param_name, name);
		if (nameWavFile != null) requestParams.put(param_nameWavFile, nameWavFile);
		if (previewWavFile != null) requestParams.put(param_previewWavFile, previewWavFile);
		if (rbtWavFile != null) requestParams.put(param_rbtWavFile, rbtWavFile);
		if (grammar != null) requestParams.put(param_grammar, grammar);
		if (smsAlias != null) requestParams.put(param_smsAlias, smsAlias);
		if (promoID != null) requestParams.put(param_promoID, promoID);
		if (classType != null) requestParams.put(param_classType, classType);
		if (startTime != null) requestParams.put(param_startTime, dateFormat.format(startTime));
		if (endTime != null) requestParams.put(param_endTime, dateFormat.format(endTime));
		if (album != null) requestParams.put(param_album, album);
		if (demoWavFile != null) requestParams.put(param_demoWavFile, demoWavFile);
		if (artist != null) requestParams.put(param_artist, artist);
		if (info != null) requestParams.put(param_info, info);
		if (rating != null) requestParams.put(param_rating, String.valueOf(rating));
		
		if (searchtype != null) requestParams.put(param_searchType, String.valueOf(searchtype));
		if (type != null) requestParams.put(param_type, String.valueOf(type));
		if (searchText != null) requestParams.put(param_searchText, String.valueOf(searchText));
		if (maxResult != null) requestParams.put(param_maxResults, String.valueOf(maxResult));
		
		if (clipIDs != null && clipIDs.size() > 0)
		{
			StringBuilder builder = new StringBuilder();
			for (Integer clipID : clipIDs)
			{
				builder.append(clipID).append(",");
			}

			requestParams.put(param_clipID, builder.substring(0, builder.length() - 1));
		}

		return requestParams;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("ContentRequest [browsingLanguage=");
		builder.append(browsingLanguage);
		builder.append(", circleID=");
		builder.append(circleID);
		builder.append(", mode=");
		builder.append(mode);
		builder.append(", modeInfo=");
		builder.append(modeInfo);
		builder.append(", onlyResponse=");
		builder.append(onlyResponse);
		builder.append(", operatroID=");
		builder.append(operatroID);
		builder.append(", subscriberID=");
		builder.append(subscriberID);
		builder.append(", album=");
		builder.append(album);
		builder.append(", artist=");
		builder.append(artist);
		builder.append(", classType=");
		builder.append(classType);
		builder.append(", clipID=");
		builder.append(clipID);
		builder.append(", clipIDs=");
		builder.append(clipIDs);
		builder.append(", contentID=");
		builder.append(contentID);
		builder.append(", contentType=");
		builder.append(contentType);
		builder.append(", demoWavFile=");
		builder.append(demoWavFile);
		builder.append(", endIndex=");
		builder.append(endIndex);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", grammar=");
		builder.append(grammar);
		builder.append(", info=");
		builder.append(info);
		builder.append(", isPrepaid=");
		builder.append(isPrepaid);
		builder.append(", language=");
		builder.append(language);
		builder.append(", name=");
		builder.append(name);
		builder.append(", nameWavFile=");
		builder.append(nameWavFile);
		builder.append(", pageNo=");
		builder.append(pageNo);
		builder.append(", previewWavFile=");
		builder.append(previewWavFile);
		builder.append(", promoID=");
		builder.append(promoID);
		builder.append(", rating=");
		builder.append(rating);
		builder.append(", rbtWavFile=");
		builder.append(rbtWavFile);
		builder.append(", smsAlias=");
		builder.append(smsAlias);
		builder.append(", startIndex=");
		builder.append(startIndex);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append("]");
		return builder.toString();
	}
}
