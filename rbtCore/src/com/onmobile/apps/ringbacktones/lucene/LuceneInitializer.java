package com.onmobile.apps.ringbacktones.lucene;

import com.tangentum.phonetix.DoubleMetaphone;

public class LuceneInitializer {
	protected static DoubleMetaphone m_metaphone = null;
	
	
	public static void main(String[] args) {
		AbstractLuceneIndexer indexer=LuceneIndexerFactory.getInstance();
		indexer.init();
	}
}

