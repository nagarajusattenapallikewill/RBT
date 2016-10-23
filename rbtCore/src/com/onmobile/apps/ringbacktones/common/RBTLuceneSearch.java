package com.onmobile.apps.ringbacktones.common;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.BooleanClause;

import com.onmobile.apps.ringbacktones.cache.content.ClipMinimal;
import com.tangentum.phonetix.DoubleMetaphone;

public class RBTLuceneSearch
{
    private static Logger logger = Logger.getLogger(RBTLuceneSearch.class);

    private DoubleMetaphone m_metaphone = null;
    private IndexWriter m_indexWriter1 = null;
    private IndexWriter m_indexWriter2 = null;
    private String m_indexPath = null;
//    private double m_threshold = 1.0;
    private int m_max_results = 5;
    private int m_web_results = 5;
    private String m_keyWord = null;

    private static Hashtable<Integer, IndexWriter> m_writer = null;

	public boolean init()
    {
		return (init(false));
	}

    public boolean init(boolean isTata)
    {
        try
        {
            m_metaphone = new DoubleMetaphone(6);
            m_indexPath = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);

			if(isTata && m_indexPath != null)
				m_indexPath += File.separator + "TataLucene";

            File index = new File(m_indexPath + File.separator + "2");
            if (index.exists())
                index.delete();
            try
            {
                m_indexWriter2 = new IndexWriter(index.getAbsolutePath(),
                        new StandardAnalyzer(), true);
            }
            catch (Exception e)
            {
            	logger.error("", e);
            }

            m_indexWriter2.optimize();
            m_indexWriter2.close();

            setWriter(m_indexWriter2);

            index = new File(m_indexPath + File.separator + "1");
            if (index.exists())
                index.delete();
            try
            {
                m_indexWriter1 = new IndexWriter(index.getAbsolutePath(),
                        new StandardAnalyzer(), true);
            }
            catch (Exception e)
            {
            	logger.error("", e);
            }
            m_indexWriter1.optimize();
            m_indexWriter1.close();

//            if (RBTSMSConfig.getInstance().luceneThreshold() > 0.0)
//                m_threshold = RBTSMSConfig.getInstance().luceneThreshold();
            if (m_indexPath == null)
                return false;
            m_max_results = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "LUCENE_MAX_RESULTS", 5);
            m_web_results = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "WEB_RESULTS", 5);

        }
        catch (Exception e)
        {
        	logger.error("", e);
            return false;
        }

        return true;
    }

    public void createWriter(String[] clips)
    {
        IndexWriter iw = getWriter();
        String dir_name = null;
        if (m_writer.containsKey(new Integer("1")))
        {
            dir_name = "2";
            iw = m_indexWriter2;
        }
        else
        {
            dir_name = "1";
            iw = m_indexWriter1;
        }
        File index = new File(m_indexPath + File.separator + dir_name);
        if (index.exists())
            index.delete();
        System.out.println("Index Created in " + index.getAbsolutePath() + " and clips length " + clips.length);
        try
        {
            iw = new IndexWriter(index.getAbsolutePath(),
                    new StandardAnalyzer(), true);
			iw.setMergeFactor(10000);
			iw.setMaxMergeDocs(9999999);
			iw.setMaxBufferedDocs(10000);

        }
        catch (Exception e)
        {
        	logger.error("", e);
        }
		StringTokenizer stk = null;
		String[] fields = { "CLIP_ID", "song", "movie", "singer"};
		String id = null;
		String name = null;
		String album = null;
		String singer = null;
        if (clips != null && clips.length > 0)
        {
            for (int i = 0; i < clips.length; i++)
            {
				id = null;
				name = null;
				album = null;
				singer = null;
			
				stk = new StringTokenizer(clips[i], ",");
				
				if (stk.hasMoreTokens())
					id = stk.nextToken();
				if (stk.hasMoreTokens())
				{
					name = stk.nextToken().toLowerCase();
					name = name.replaceAll("_", " ");
				}
				if (stk.hasMoreTokens())
					album = stk.nextToken().toLowerCase();
				if (stk.hasMoreTokens())
					singer = stk.nextToken().toLowerCase();
				String[] values = {id, name, album, singer};
                createModifyDocuments(iw, fields, values);
            }

        }

        optimizeIndexWriter(iw);
    }

    private synchronized IndexWriter getWriter()
    {
        if (m_writer == null)
            return null;
        if (m_writer != null && m_writer.containsKey(new Integer("1")))
            return (m_writer.get(new Integer("1")));
        else
            return (m_writer.get(new Integer("2")));
    }

    private synchronized void setWriter(IndexWriter writer)
    {
        IndexWriter iw = getWriter();

        if (iw == null || m_writer.containsKey(new Integer("1")))
        {
            if (iw == null)
                m_writer = new Hashtable<Integer, IndexWriter>();
            else
                m_writer.remove(new Integer("1"));
            m_writer.put(new Integer("2"), writer);
        }
        else
        {
            m_writer.remove(new Integer("2"));
            m_writer.put(new Integer("1"), writer);
        }

    }

    private boolean createModifyDocuments(IndexWriter iw, String[] fieldNames,
            String[] fieldValues)
    {
        String keyWord = null, keyValue = null;
        if (fieldNames == null || fieldNames.length <= 1 || fieldValues == null
                || fieldValues.length <= 1
                || fieldNames.length != fieldValues.length)
            return false;
        try
        {
            keyWord = fieldNames[0];
            Integer.parseInt(fieldValues[0].trim());
            keyValue = fieldValues[0];

            //Creating doc and adding unique id to Keyword of the doc
            Document doc = new Document();
//            Field f1=new Field(keyWord, keyValue, Field.Store.YES, Field.Index.UN_TOKENIZED); 
            Field f3=new Field(keyWord, keyValue, Field.Store.YES, Field.Index.UN_TOKENIZED);
            doc.add(f3);
            //			logger.info("Adding to Doc "+keyWord+" and
            // "+keyValue);
            m_keyWord = keyWord;
            //putting rest of the fields in SearchKeyword
            for (int i = 1; i < fieldNames.length; i++)
            {
                String SearchKeyword = " ";

                if (fieldValues[i] == null)
                    continue;

                SearchKeyword = SearchKeyword + " "
                        + getPhonetics(fieldValues[i], false) + " "
                        + getPhonetics(fieldValues[i], true);
                //				String[] s = fieldValues[i].split(" ");
                StringTokenizer stk = new StringTokenizer(fieldValues[i], " ");
                String wordByWord = "";
                while (stk.hasMoreTokens())
                //				for(int j=0; j<s.length; j++)
                {
                    String tkn = (String) stk.nextToken();
                    wordByWord = wordByWord + getPhonetics(tkn, true) + " ";
                    SearchKeyword = SearchKeyword + " "
                            + getPhonetics(tkn, true);
                }
                SearchKeyword = SearchKeyword + " " + wordByWord;
                SearchKeyword = SearchKeyword.trim() + " "
                        + getSubstring(fieldValues[i], 3);
                SearchKeyword = SearchKeyword.trim() + " "
                        + getSubstring(fieldValues[i], 4);
                Field f2=new Field(fieldNames[i], SearchKeyword.trim(),Field.Store.YES,Field.Index.TOKENIZED);
				doc.add(f2);
  
                //				System.out.println("Adding to Doc "+fieldNames[i] +"
                // SearchKeyword " +SearchKeyword+ " fieldValues
                // "+fieldValues[i]);

            }

            iw.addDocument(doc);

        }
        catch (Exception e)
        {
            logger.error("", e);
            return false;
        }

        return true;
    }

    public void optimizeIndexWriter(IndexWriter iw)
    {
        try
        {
            iw.optimize();
            iw.close();
            setWriter(iw);
        }
        catch (Exception e)
        {
            logger.error("", e);
        }
    }

    private String getSubstring(String clipName, int n)
    {
        String output = "";
        //			String[] s = clipName.split(" ");
        StringTokenizer stk = new StringTokenizer(clipName, " ");
        clipName = "";
        int i = 0;
        while (stk.hasMoreTokens())
        //for (;i<s.length; i++)
        {
            clipName = clipName + (String) stk.nextToken();
        }
        int size = clipName.length();
        i = 0;
        while (i < (size - n))
        {
            output = output + clipName.substring(i, (i + n)) + " ";
            i++;
        }

        output = output + clipName.substring(i);
        return output;
    }

    private String getPhonetics(String input, boolean trimSpace)
    {
//        String[] tokens;
        if (trimSpace)
        {
            //				tokens = input.split(" ");
            StringTokenizer stk = new StringTokenizer(input, " ");

            if (stk.hasMoreTokens())
            //				if(tokens != null && tokens.length >=1)
            {
                input = " ";
                while (stk.hasMoreTokens())
                //					for(int i=0; i<tokens.length; i++)
                {
                    input = input.trim() + (String) stk.nextToken();
                }
            }
        }

        String phonem = m_metaphone.generateKey(input);

        return phonem.trim();
    }

    public String[] search(String searchString, boolean isSMS, String searchOn)
    {
        ArrayList<String> hitList = new ArrayList<String>();
        Document tmpDoc = null;
        ScoreDoc[] scores = null;
        String dir = "1";
        IndexSearcher iSearcher = null;
        try
        {
            IndexWriter iw = getWriter();
            if (iw == null)
                return null;
            if (m_writer.containsKey(new Integer("2")))
                dir = "2";

            iSearcher = new IndexSearcher(m_indexPath + File.separator + dir);

            System.out.println("Searching On Index " + m_indexPath
                    + File.separator + dir + "on Key " + searchOn);

            String queryString = getQueryString(searchString);

            logger.info("Query String for " + searchString
                    + " is" + queryString);

            BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD};
            Query query= MultiFieldQueryParser.parse(queryString, new String[]{searchOn}, flags, new WhitespaceAnalyzer());
/*            String[] search = new String[1];
            String[] queryS= new String[1];
            search[0]=searchOn;
            queryS[0]= queryString;
            Query query = MultiFieldQueryParser.parse(queryS, search, new StandardAnalyzer());
*/
            int results = m_web_results;
            if (isSMS)
                results = m_max_results;

            scores = iSearcher.search(query, new QueryFilter(query), results).scoreDocs;

            logger.info("scores " + scores);

            if (scores == null || scores.length == 0)
                return null;

            for (int i = 0; i < scores.length; i++)
            {
                tmpDoc = iSearcher.doc(scores[i].doc);
                //						if(scores[i].score > m_threshold)
                hitList.add(tmpDoc.get(m_keyWord));
            }

        }
        catch (Exception e)
        {
            logger.error("", e);
        }

        finally
        {
            try
            {
                if (iSearcher != null)
                    iSearcher.close();
            }
            catch (Exception e)
            {
                logger.error("", e);
            }
        }
        logger.info("hitList " + hitList);

        if (hitList == null || scores == null)
            return null;
        logger.info("No. Hits for " + searchString
                + " is " + scores.length + " and matches obtained are "
                + hitList.toString());
        return hitList.toArray(new String[0]);
    }

    private String getQueryString(String clipName)
    {
        String query = "";
        //			String[] s = clipName.split(" ");
        StringTokenizer stk = new StringTokenizer(clipName, " ");
        String wordByWord = "";
        String tmp = "";
        while (stk.hasMoreTokens())
        //			for(int i=0; i<s.length; i++)
        {
            String tkn = (String) stk.nextToken();
            String phonem = getPhonetics(tkn, true);
            String word = "";
            wordByWord = wordByWord + phonem + " ";
            word = word + " OR " + phonem;
            tmp = tmp + word;
        }
        query = "(" + getPhonetics(clipName, false) + " OR "
                + getPhonetics(clipName, true) + " OR " + wordByWord.trim()
                + " " + tmp.trim() + " OR " + getSubstring(clipName, 3)
                + " OR " + getSubstring(clipName, 4) + ")";

        return query;
    }
    
    //FOR TATA
    public void createWriter(ClipMinimal[] prepaidClips, ClipMinimal[] postpaidClips)
    {
		IndexWriter iw = getWriter();
		String dir_name = null;
		if(m_writer.containsKey(new Integer("1"))) {
			dir_name = "2";
			iw = m_indexWriter2;
		}
		else {
			dir_name = "1";
			iw = m_indexWriter1;
		}
		File index = new File(m_indexPath + File.separator + dir_name);
		if(index.exists())
			index.delete();
		System.out.println("Index Created in " + index.getAbsolutePath());
		try {
			iw = new IndexWriter(index.getAbsolutePath(), new StandardAnalyzer(), true);
		}
		catch (Exception e) {
			logger.error("", e);
		}

		if(prepaidClips != null && prepaidClips.length > 0) {
			for(int i = 0; i < prepaidClips.length; i++) {
				String[] fields = { "CLIP_ID", "pre", "post" };
				String[] values = { String.valueOf(prepaidClips[i].getClipId()),
						prepaidClips[i].getClipName(), null };
				createModifyDocuments(iw, fields, values);
			}
		}

		if(postpaidClips != null && postpaidClips.length > 0) {
			for(int i = 0; i < postpaidClips.length; i++) {
				String[] fields = { "CLIP_ID", "pre", "post" };
				String[] values = { String.valueOf(postpaidClips[i].getClipId()), null,
						postpaidClips[i].getClipName() };
				createModifyDocuments(iw, fields, values);
			}
		}

		optimizeIndexWriter(iw);
	}

}