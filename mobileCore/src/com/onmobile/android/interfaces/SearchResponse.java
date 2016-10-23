package com.onmobile.android.interfaces;

public interface SearchResponse {

	/**
	 * 
	 * @param searchString
	 * @param criteria
	 * @param pageNo
	 * @param language
	 * @param subscriberId
	 * @param maxResults
	 * @return
	 */
	public String searchClipsByNameTune(String searchString, String criteria, int pageNo, String language, String subscriberId, int maxResults);

	/**
	 * 
	 * @param searchString
	 * @param language
	 * @param subscriberId
	 * @return
	 */
	public String createNameTune(String searchString, String language, String subscriberId);
	
	/**
	 * 
	 * @param searchText
	 * @return
	 */
	public String searchSuggestions(String searchText);

	/**
	 * API for searching for Artists.
	 * @param search
	 * @param maxResults
	 * @return
	 */
	
	public String searchForArtists(String search, int maxResults);
	/**
	 * API for searching for playlists
	 * @param search
	 * @param maxResults
	 * @return
	 */
	public String searchForPlaylists(String search, int maxResults);

	/**
	 * 
	 * @param searchString
	 * @param criteria
	 * @param pageNo
	 * @param subId
	 * @param maxResults
	 * @return
	 */
	public String searchClips(String searchString, String criteria, int pageNo, String subId, int maxResults, String language);
	
}
