package com.onmobile.apps.ringbacktones.lucene.solr;

import com.onmobile.apps.ringbacktones.lucene.LuceneCategory;
import com.onmobile.apps.ringbacktones.lucene.LuceneClip;

/**
 * @author laxmankumar
 * @author senthil.raja
 *
 */
public interface SearchConstants {

	// configuration parameters
	String PROXY_HOST = "proxy_host";
	
	String PROXY_PORT = "proxy_port";
	
	String PARAM_SERVER_URL = "server_url";

	String PARAM_SOCKET_TIMEOUT = "socket_timeout";

	String PARAM_CONNECTION_TIMEOUT = "connection_timeout";

	String PARAM_GRAMS = "grams";
	
	String MSEARCH_DEFAULT_SUBID = "msearch_default_subID";
	
	String DEFAULT_PARENT_CAT_ID = "default_parent_cat_Id";
	
	String DEFAULT_SUB_CAT_ID = "default_sub_cat_Id";
	
	String MSEARCH_URL = "msearch_url";
	
	String CATEGORY_MSEARCH_URL = "category_msearch_url";

	String ARTIST_MSEARCH_URL = "artist_msearch_url";
	
	String ARTIST_SONGS_MSEARCH_URL = "artist_songs_msearch_url";
	
	String SUGGESTION_URL = "suggestion_url";

	String MSEARCH_NAMETUNE_URL= "msearch_nametune_url";
	
	String PARAM_MIN_GRAM_SIZE = "min_gram_size";

	String PARAM_MAX_GRAM_SIZE = "max_gram_size";

	String PARAM_PHONETICS = "phonetics";

	String PARAM_INCLUDE_EXPIRED = "include_expired";

	String PARAM_SOLR_SEARCH = "solr_search";
	
	String PARAM_MSEARCH_SEARCH = "msearch_search";
	
	String PARAM_DEFAULT_LANGUAGE = "DEFAULT_LANGUAGE";
	
	String PARAM_SUPPORTED_LANGUAGES = "SUPPORTED_LANGUAGES";

	String PARAM_NUM_OF_DOCS = "number_of_documents";
	
	String PARAM_DUMMY_CATEGORY_ID = "dummy_category_id";
	
	String PARAM_INDEX_FIELDS = "index_fields";
	
	String PARAM_REGEX = "regex";
	
	String PARAM_PHONETIC_MAX_LENGTH = "phonetic_max_length";
	
	String PARAM_NO_OF_CLIP_PER_ITERATION   = "no_of_clips_per_iteration";

	String PARAM_OPERATOR = "operator";
	
	String PARAM_USE_LANGUAGE_IDENTIFIER = "use_language_identifier";
	
	String PARAM_FILTER_CAT_IDS = "filter_cat_ids";
	
	String PARAM_FILTER_CLIP_IDS = "filter_clip_ids";
	
	String FIELD_SEARCH_TYPE = "searchtype";
	
	String FIELD_DATE_EXPIRED = "dateexpired";

	String FIELD_PARENT_CAT_NAME = "PARENT_CAT_NAME";

	String FIELD_PARENT_CAT_ID = "parentCatId";

	String FIELD_SUB_CAT_NAME = "SUB_CAT_NAME";
	
	String FIELD_SUB_CAT_ID = "subCatId";
	
	String FIELD_CAT_SMS_ALIAS = "CAT_SMS_ALIAS";
	
	String FIELD_CLIP_SMS_ALIAS = "CLIP_SMS_ALIAS";
	
	String FIELD_CLIP_PROMO_ID = "CLIP_PROMO_ID";
	
	String FIELD_CLIP_ID = "CLIP_ID";
	
	String FIELD_LANGUAGE = "language";

	String SEARCH_TYPE_CATEGORY = LuceneCategory.class.getSimpleName();
	
	String SEARCH_TYPE_CLIP = LuceneClip.class.getSimpleName();

	String DEFAULT_LANGUAGE_VALUE = "eng";
	
	int NUM_OF_DOC_DEFAULT_VALUE = 100;
	
	int PARENT_CATEGORY_ZERO = 0;

	int NO_OF_CLIPS_PER_ITERATION = 5000;
	
	String FILTER_CLIP_TYPE = "cliptype";
	
	String CLIP_TYPE_CORP = "CORP";
	
	String CLIP_TYPE_NORMAL = "NORMAL";
	
	String CLIP_TYPE="CLIP_TYPE";
	
	String AVOID_UGC_TRIAL_CLIPS = "AVOID_UGC_TRIAL_CLIPS";
	
	String SUPPORT_LANGUAGE = "support_language";
	
	String PARAM_MSEARCH_RESULT_CONTENT_TYPE = "msearch_result_content_type";
	
	String PARAM_CAT_TYPES_NOT_ALLOWED = "cat_types_not_allowed";
	
	String REMOVE_SPECIAL_CHARS_FROM_SEARCH_QUERY = "remove_special_chars_from_search_query";
	
	//RBT-9871
	String MULTI_FEILD_MSEARCH_URL="multifeild_msearch_url";
	String MULTI_FEILD_MSEARCH_URL_FL="multifeild_msearch_url_fl";
	String DIALER_TONE_NAME="song";
	String ALBUM="album";
	String ARTIST="artist";
	String CELEBRITY = "celebrity";
	
	String MSEARCH_CRITERIA_ALL = "msearch_criteria_all";
	
	//RBT -14926
	String GENERIC_MSEARCH_IMPL="use_generic_msearch";
	String GENERIC_MSEARCH_CORE_SONG="msearch_core_song";
	String GENERIC_MSEARCH_CORE_SONG_WITH_CAT="msearch_core_song_categorized";
	String GENERIC_MSEARCH_CORE_NAME_TUNES="msearch_core_name_tunes";
	String GENERIC_MSEARCH_CORE_NAME_TUNES_WITH_CAT="msearch_core_name_tunes_categorized";
	String GENERIC_MSEARCH_CLIP_FILTER_NAME_TUNES="nametune";
	String GENERIC_MSEARCH_CLIP_FILTER_SONG="song";
	String GENERIC_MSEARCH_SOLR_URL="solr_msearch_url";
	String PARAM_SOLR_QUERY_CATEGORY="solr_msearch_query_param_category";
	String PARAM_SOLR_QUERY_SUBCATEGORY="solr_msearch_query_param_subcategory";
	String PARAM_SOLR_SYMBOL_PHONETIC="solr_msearch_query_param_phonetic";
	String PARAM_SOLR_SYMBOL_PHONETIC_ISREQUIRED="use_solr_msearch_query_param_phonetic";
	String PARAM_SOLR_QUERY_LANGUGAGE="solr_msearch_query_param_language";
	String PARAM_SOLR_QUERY_TITLE="solr_msearch_query_param_title";
	String PARAM_SOLR_QUERY_ARTIST="solr_msearch_query_param_artist";
	String PARAM_SOLR_QUERY_ALBUM="solr_msearch_query_param_album";
	String PARAM_SOLR_QUERY_REQUEST_HANDLER="solr_msearch_query_request_handler";
	String PARAM_SOLR_QUERY_AND_SYMBOL="solr_msearch_query_and_symbol";
	String PARAM_SOLR_QUERY_OR_SYMBOL="solr_msearch_query_or_symbol";
	String PARAM_SOLR_FL_QUERY="solr_msearch_fl_query_param";
	
	
}
