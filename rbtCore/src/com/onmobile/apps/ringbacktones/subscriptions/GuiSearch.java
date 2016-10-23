package com.onmobile.apps.ringbacktones.subscriptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;
import org.apache.lucene.search.ScoreDoc;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.tangentum.phonetix.DoubleMetaphone;


public class GuiSearch {

	private static Logger logger = Logger.getLogger(GuiSearch.class);
	
	private static Hashtable m_writer = null;
	private static DoubleMetaphone m_metaphone = null;
	private static IndexWriter m_indexWriter1 = null;
	private static IndexWriter m_indexWriter2 = null;
	private static String m_indexPath = "c:/rbt/index";
	private static String m_keyWord = null;
	private static int m_web_results = 100;
	private static HashMap m_detail = new HashMap();
	public static boolean m_usePool = true;
	public static String m_countryPrefix = "91";
	public static RBTDBManager m_rbtDBManager = null;
	public static String dPipe = "##";
	public static String dPipeNA = "##NA";
	public static String dNA = "NA";
	/**
	 * @param k
	 */

	public static void getContentAndIndex() {

		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		m_metaphone = new DoubleMetaphone(6);
		try {
			IndexWriter iw = initializeBeforeCaliingCreateWriter();
			String[] fields = {"vcode","PARENT_CAT_NAME","SUB_CAT_NAME","song","album","artist"};
			//Categories[] parentCategories = m_rbtDBManager.getActiveCategories();
			Categories[] parentCategories = m_rbtDBManager.getGUIActiveCategories(null, 'b');
			if(parentCategories == null || parentCategories.length <= 0)
				return;
			for(int i=0; i< parentCategories.length; i++)
			{
				int parentCatId=parentCategories[i].id();
				String parentCategoryName=parentCategories[i].name();
				//Categories[] subCategories = m_rbtDBManager.getSubCategories(parentCatId);
				Categories[] subCategories = m_rbtDBManager.getGUISubCategories(parentCatId, parentCategories[i].circleID(), parentCategories[i].prepaidYes());
				if(subCategories == null || subCategories.length <= 0)
					continue;
				for(int j=0; j< subCategories.length; j++){

					int subCatId=subCategories[j].id();
					String subCategoryName=subCategories[j].name();
//					if(!subCategoryName.equalsIgnoreCase("New Arrivals") && !(subCategoryName.startsWith("Pop")))
//						continue;
					Clips[] clips = m_rbtDBManager.getAllClipsCCC(subCatId,null);
					if(clips == null || clips.length <= 0)
						continue;

					for(int k = 0; k < clips.length; k++){

						StringBuffer clipDetail=new StringBuffer();
						String wavFile = clips[k].wavFile();
						String clipName = clips[k].name();
						String album = clips[k].album();
						String singer = clips[k].artist();
						int clipID = clips[k].id();
						Date endTime=clips[k].endTime();
						String classType = clips[k].classType();
						String vcode = wavFile;
						vcode=vcode.replaceAll("rbt", "");
						vcode=vcode.replaceAll("_", "");

						if(vcode != null && vcode.length()>0 )clipDetail.append(vcode);
						else clipDetail.append(dNA);
						if(clipName != null && clipName.length()>0)clipDetail.append(dPipe+clipName);
						else clipDetail.append(dPipeNA);
						if(album != null && album.length()>0)clipDetail.append(dPipe+album);
						else clipDetail.append(dPipeNA);
						if(singer != null && singer.length()>0)clipDetail.append(dPipe+singer);
						else clipDetail.append(dPipeNA);
						if(parentCategoryName != null && parentCategoryName.length()>0)clipDetail.append(dPipe+parentCategoryName);
						else clipDetail.append(dPipeNA);
						if(subCategoryName != null && subCategoryName.length()>0)clipDetail.append(dPipe+subCategoryName);
						else clipDetail.append(dPipeNA);
						clipDetail.append(dPipe+clipID);
						if(classType != null && classType.length()>0)clipDetail.append(dPipe+classType);
						else clipDetail.append(dPipeNA);
						clipDetail.append(dPipe+subCatId);
						m_detail.put(vcode,clipDetail.toString());
						//System.out.println("writing the index "+vcode+ " "+clipDetail.toString());
						String[] values = {vcode ,parentCategoryName,subCategoryName,clipName,album,singer};
						Calendar cal1 = Calendar.getInstance();
						Date sysdate=cal1.getTime();
						if(endTime!=null && endTime.after(sysdate)){
//							logger.info(" clips end date is not null and its after sysdate");
						createModifyDocuments(iw, fields, values);
						}else{
//							/logger.info(" clips end date is null or its before sysdate");
						}
					}

				}
				logger.info("clips got for category name "+parentCategories[i].name());
			}
			logger.info("cacheing done");
			optimizeIndexWriter(iw);
			Calendar cal1 = Calendar.getInstance();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			logger.info("b4 calling create index "+sdf1.format(cal1.getTime()));
			Calendar cal2 = Calendar.getInstance();
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			logger.info("after calling create index and before search "+sdf2.format(cal2.getTime()));
		} 
		catch (Exception e2){
			e2.printStackTrace();
			logger.info(e2.getMessage());
		}

	}



	private synchronized static IndexWriter getWriter()
	{
		if (m_writer == null)
			return null;
		if (m_writer != null && m_writer.containsKey(new Integer("1")))
			return ((IndexWriter) m_writer.get(new Integer("1")));
		else
			return ((IndexWriter) m_writer.get(new Integer("2")));
	}

	private static boolean createModifyDocuments(IndexWriter iw, String[] fieldNames,
			String[] fieldValues)
	{
		String _method = "createModifyDocuments";
		String keyWord = null, keyValue = null;
		if (fieldNames == null || fieldNames.length <= 1 || fieldValues == null
				|| fieldValues.length <= 1
				|| fieldNames.length != fieldValues.length)
			return false;
		try
		{
			keyWord = fieldNames[0];
			//Integer.parseInt(fieldValues[0].trim());
			keyValue = fieldValues[0];

			//Creating doc and adding unique id to Keyword of the doc
			Document doc = new Document();
			Field f3=new Field(keyWord, keyValue, Field.Store.YES, Field.Index.UN_TOKENIZED); 
			doc.add(f3);
			//doc.add(Field.Keyword(keyWord, keyValue));
			//			logger.info("Adding to Doc "+keyWord+" and
			// "+keyValue);
			m_keyWord = keyWord;
			//putting rest of the fields in SearchKeyword
			for (int i = 1; i < fieldNames.length; i++)
			{
				String SearchKeyword = " ";

				if (fieldValues[i] == null)
					continue;
				try{
					Integer.parseInt(fieldValues[i]);
					continue;
				}
				catch(Exception e)
				{
					//e.printStackTrace();
//					Tools.logDetail(_class, "GuiSearch::Exception caught ", e.getMessage());
				}
				if (fieldNames[i].equalsIgnoreCase("vcode")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					//doc.add(Field.Text(fieldNames[i], fieldValues[i]));
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("PARENT_CAT_NAME")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					//doc.add(Field.Text(fieldNames[i], fieldValues[i]));
					continue;
				}
				if (fieldNames[i].equalsIgnoreCase("SUB_CAT_NAME")){
					Field f1=new Field(fieldNames[i],fieldValues[i],Field.Store.YES,Field.Index.TOKENIZED);
					doc.add(f1);
					//doc.add(Field.Text(fieldNames[i], fieldValues[i]));
					continue;
				}
				// //System.out.println(fieldValues[i]);
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
				//doc.add(Field.Text(fieldNames[i], SearchKeyword.trim()));

				//				//System.out.println("Adding to Doc "+fieldNames[i] +"
				// SearchKeyword " +SearchKeyword+ " fieldValues
				// "+fieldValues[i]);

			}
			try{
				iw.addDocument(doc);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				logger.info(e.getMessage());
				return false;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.info(e.getMessage());
			return false;
		}

		return true;
	}

	public static void optimizeIndexWriter(IndexWriter iw)
	{
		String _method = "optimizeIndexWriter";
		try
		{
			iw.optimize();
			iw.close();
			setWriter(iw);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.info(e.getMessage());
		}
	}

	private static String getPhonetics(String input, boolean trimSpace)
	{
//		String[] tokens;
		if (input == null)
			return null;
		try{
			Integer.parseInt(input);
			return null;

		}
		catch(Exception e)
		{
			//e.printStackTrace();
			//Tools.logDetail(_class, "GuiSearch::Exception caught ", e.getMessage());
		}
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
		String phonem=null;
		if(input != null)
			phonem = m_metaphone.generateKey(input);
		if(phonem != null)
			phonem=phonem.trim();

		return phonem;
	}

	private static String getSubstring(String clipName, int n)
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

	private synchronized static void setWriter(IndexWriter writer){

		IndexWriter iw = getWriter();
		if (iw == null || m_writer.containsKey(new Integer("1"))){
			if (iw == null)
				m_writer = new Hashtable();
			else
				m_writer.remove(new Integer("1"));
			m_writer.put(new Integer("2"), writer);
		}
		else{
			m_writer.remove(new Integer("2"));
			m_writer.put(new Integer("1"), writer);
		}

	}

	private static String getQueryString(String clipName)
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
	public static ArrayList searchQuery(HashMap map,int pageNo)
	{
		ArrayList arr=searchQueryInner(map,pageNo);
		//if(arr != null && arr.size()>0)
		return arr;
		//HashMap mapTemp=new HashMap();
		//mapTemp.put("SUB_CAT_NAME", "BOLLYWOOD");
		//arr=searchQueryInner(mapTemp);
		//return arr;
	}

   // PARENT=English Songs;SUB_CAT=New Arrivals;song=meim
	public static ArrayList searchQueryInner(HashMap map,int pageNo)
	{
		String dir="1";
		IndexSearcher iSearcher = null;
		IndexWriter iw = getWriter();
		if (iw == null)
			return null;
		if (m_writer.containsKey(new Integer("2")))
			dir = "2";

		try {
			iSearcher = new IndexSearcher(m_indexPath + File.separator + dir);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		String _method = "searchQuery";
		ArrayList hitList = new ArrayList();
		Document tmpDoc = null;
		ScoreDoc[] scores = null;
		String parentCat=null;
		String subCat=null;
		String[] names={"vcode","PARENT_CAT_NAME","SUB_CAT_NAME","song","album","artist"};
		String[] searchString = new String[map.size()];
		String[] searchOn = new String[map.size()];
		int results=m_web_results;
		
			if(pageNo<0){
				pageNo=0;
			}
			results=m_web_results*(pageNo+1);
			logger.info("got the page number and hence returning "+m_web_results+" reults");
			System.out.println("got the page number and hence returning "+m_web_results+" reults");
			
	
		int cou=0;
		for(int count=0;count<6;count++)
		{
			try{
				String tem=(String)map.get(names[count]);
				if(tem!=null){
					tem=tem.toString();
				}
				//logger.info("tem value=="+tem+" with names count== "+count+" value=="+names[count]);
				//System.out.println("tem value=="+tem+" with names count== "+count+" value=="+names[count]);
				if(tem != null && tem.length()>0)
				{
					logger.info("tem is not null");
					//System.out.println("tem is not null");
					searchOn[cou]=names[count];
					if(names[count].equalsIgnoreCase("song") || names[count].equalsIgnoreCase("album") || names[count].equalsIgnoreCase("artist"))
					{
						searchString[cou]=tem+"*";
					}
					else
					{
						searchString[cou]=tem;
						if(names[count].equalsIgnoreCase("PARENT_CAT_NAME"))
							parentCat=searchString[cou];
						if(names[count].equalsIgnoreCase("SUB_CAT_NAME"))
							subCat=searchString[cou];
					}
					System.out.println("::"+searchOn[cou]+" "+searchString[cou]);
					logger.info("searchOn lastest entry value=="+searchOn[cou]+" searchString lastest entry value=="+searchString[cou]+" cou=="+cou);
					//System.out.println("The argument found as expected ============searchOn lastest entry name=="+searchOn[cou]+" searchString lastest entry value=="+searchString[cou]+" cou=="+cou);
					cou++;

				}
			}
			catch(Exception e){
				//System.out.println("argument not found");
			}
		}
		try
		{
			String[] sString=new String[cou];
			//System.out.println("Here");
			boolean status1 = false;
			StringBuffer searchBuffer=new StringBuffer();
			StringBuffer vcodeBuffer=new StringBuffer();
			int countOfParameter=0;
			String searchinOn=null;
			if(searchOn != null)
			{
				//System.out.println("   seach on is not null==============="+cou);
				for (int i=0;i<cou;i++){
					//System.out.println("   seach on is ==================="  +searchOn[i]);
					if (searchOn[i].equalsIgnoreCase("vcode")){
						sString = new String[1];
						//System.out.println("in for "+searchString[i]+"  i ="+i);
						sString[0] = searchString[i];
						//sString = searchString[i];
						status1 = true;
						break;
					}else{
						if(searchOn[i].equalsIgnoreCase("PARENT_CAT_NAME")||searchOn[i].equalsIgnoreCase("SUB_CAT_NAME"))
						{//sString=searchString[i]+" ";
						//System.out.println("in else "+searchString[i]+" i="+i);
							searchBuffer.append("("+searchOn[i]+":"+searchString[i]+")");
							int searchLength=searchOn.length;
							if (i<(cou-1))
								searchBuffer.append("AND");
						}
						else
						{
							sString[i]=searchString[i]+" ";
							countOfParameter=i;
							searchinOn=searchOn[i];
						}
					}
					
				}
//				//System.out.println("outside for "+sString);
			}
			else{
//				//System.out.println("search on is null");
			}
			//System.out.println("sString length is "+sString.length);
			if (!status1){
//				for(int i=0;i<sString.length;i++)
//				{
//					//System.out.println("sString is "+sString[i]+"  and the value of i is "+i);
//					sString[i] = getQueryString(sString[i]);
//					System.out.println(" :::: "+searchOn[i]);
//					System.out.println(" ::::"+sString[i]);
//					
//				}
				System.out.println(countOfParameter);
				if(sString[countOfParameter]!=null)
				{
				String c=getQueryString(sString[countOfParameter]);
				//System.out.println("hshakskjdhks  "+sString[countOfParameter]);
				//System.out.println("    kdjkd "+c.endsWith(")"));
				//if((!c.equals("*"))&&(c.endsWith(")")))
				//c=c.replace(")"," OR "+sString[countOfParameter]+")");
				c=c.substring(0,c.indexOf(")"))+" OR "+sString[countOfParameter]+")";
				System.out.println("::c is "+c+" sstring is "+sString[countOfParameter]);
				searchBuffer.append(c);
				}
			}
			else{
				searchOn = new String [1];
				searchOn [0] = "vcode";
				//sString[0] = "("+sString[0]+")";
				vcodeBuffer.append("("+sString[0]+")");
			}
			System.out.println(" :::: "+vcodeBuffer.toString());
			System.out.println(" ::::"+searchBuffer.toString());
			
			logger.info("sString is "+sString);
//			//System.out.println("sString is "+sString);
			//sString=sString.trim();
			//Query query = QueryParser.parse(sString, sSearchOn, new StandardAnalyzer());
			//  sString, searchOn,new StandardAnalyzer()
//			query = MultiFieldQueryParser.parse(sString,searchOn,new StandardAnalyzer());
			Query query;
			System.out.println("status is "+status1);
			if(status1)
				{
				 BooleanClause.Occur[] flags = {BooleanClause.Occur.MUST};
				 query = MultiFieldQueryParser.parse(vcodeBuffer.toString(),searchOn,flags,new StandardAnalyzer());
				}
			else
			{
				 BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD};
				 //String[] test={searchOn[countOfParameter]};
				 String[] test=new String[1];
				test[0]=searchOn[countOfParameter];
				 query= MultiFieldQueryParser.parse(searchBuffer.toString(), test, flags, new StandardAnalyzer());
			}
//			Query query = MultiFieldQueryParser.parse(searchOn,searchOn,new StandardAnalyzer());

			logger.info("query=="+query.toString()+" and result=="+results);
			System.out.println("query=="+query.toString()+" and result=="+results);
			try{ 
				scores= iSearcher.search(query, new QueryFilter(query), results).scoreDocs;
			}
			catch(Exception e){
				//System.out.println("exception is "+e.getMessage());
				e.printStackTrace();
			}
			if (scores == null || scores.length == 0)
				return null;

			for (int i = 0; i < scores.length; i++)
			{
				tmpDoc = iSearcher.doc(scores[i].doc);
				//						if(scores[i].score > m_threshold)
				hitList.add(tmpDoc.get("vcode"));
				System.out.println("scores "+i+" "+tmpDoc);
			}
			

		}
		catch (Exception e)
		{
			e.printStackTrace();
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
				e.printStackTrace();
			}
		}

		if (hitList == null || scores == null)
			return null;
		String[] temp= (String[]) hitList.toArray(new String[0]);
		ArrayList clipdetails=new ArrayList(temp.length);
		ArrayList clipRest=new ArrayList(temp.length);
		int i=results-m_web_results;
		System.out.println("scores.length= "+scores.length+"  i= "+i+" results ="+results+" m_web_results ="+m_web_results+" temp.length="+temp.length);
		boolean add=true;
		System.out.println("---------------parentCat is "+parentCat+"  and subCat is "+subCat);
		for(; i<temp.length && i<results; i++)
		{
			String temporaryString=(String)m_detail.get(temp[i]);
			String tempParCat=null;
			String tempSubCat=null;
			if(parentCat!=null)
			{
				StringTokenizer SToken=new StringTokenizer(temporaryString,dPipe);
				SToken.nextToken();
				tempParCat=SToken.nextToken();
				if(!tempParCat.equalsIgnoreCase(parentCat))
					add=false;
			}
			if(subCat!=null)
			{
				StringTokenizer SToken=new StringTokenizer(temporaryString,dPipe);
				SToken.nextToken();
				SToken.nextToken();
				tempSubCat=SToken.nextToken();
				if(!tempSubCat.equalsIgnoreCase(subCat))
					add=false;
			}
			if(add)
				clipdetails.add(temporaryString);
			else
				clipRest.add(temporaryString);
		}
		clipdetails.addAll(clipRest);
		return clipdetails;
	}


	public static void init1() throws Exception{

		Tools.init("RBT_WAR", false);
		m_metaphone = new DoubleMetaphone(6);
		m_rbtDBManager = RBTDBManager.getInstance();
	}

	public static IndexWriter initializeBeforeCaliingCreateWriter(){

		IndexWriter iw = getWriter();
		String dir_name = null;
		if (m_writer.containsKey(new Integer("1"))){
			dir_name = "2";
			iw = m_indexWriter2;
		}
		else{
			dir_name = "1";
			iw = m_indexWriter1;
		}
		File index = new File(m_indexPath + File.separator + dir_name);
		if (index.exists())
			index.delete();
		//System.out.println("Index Created in " + index.getAbsolutePath());
		try{
			iw = new IndexWriter(index.getAbsolutePath(),new StandardAnalyzer(), true);
		}
		catch (Exception e){
			e.printStackTrace();
			logger.info(e.getMessage());
			return iw;
		}
		return iw;
	}

	private static final String _class = "GuiSearch";
	private static boolean status = true;
	public static boolean init(){

		String _method = "init";
		try{
			if(status){
				init1();
				m_indexPath = RBTParametersUtils.getParamAsString(iRBTConstant.SMS, "DEFAULT_REPORT_PATH", null);
				File index = new File(m_indexPath + File.separator + "2");
				if (index.exists())
					index.delete();
				try{
					logger.info("Path of index2 file is : "+index.getAbsolutePath());
					//System.out.println("Path of index2 file is : "+index.getAbsolutePath());
					logger.info("tmpdir : "+System.getProperty("java.io.tmpdir"));
					//System.out.println("tmpdir is : "+System.getProperty("java.io.tmpdir"));
					m_indexWriter2 = new IndexWriter(index.getAbsolutePath(),
							new StandardAnalyzer(), true);
				}
				catch (Exception e){
					e.printStackTrace();
					logger.info(e.getMessage());
				}
				m_indexWriter2.optimize();
				m_indexWriter2.close();
				setWriter(m_indexWriter2);
				index = new File(m_indexPath + File.separator + "1");
				if (index.exists())
					index.delete();
				try{
					m_indexWriter1 = new IndexWriter(index.getAbsolutePath(),
							new StandardAnalyzer(), true);
				}
				catch (Exception e){
					e.printStackTrace();
					logger.info(e.getMessage());
				}
				m_indexWriter1.optimize();
				m_indexWriter1.close();            
				status=false;
			}
			getContentAndIndex();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			logger.info(e.getMessage());
			return false;
		}
		return true;
	}

	public static void main(String[] args)
	{
		try {
			init();
			//init1();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		FileOutputStream out=null;
//		PrintStream p=null;
//		try {
//			out = new FileOutputStream("c:/rbt/log.txt");
//			p = new PrintStream( out );
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		String[] searchString={"english songs"};
		String[] searchOn={"PARENT_CAT_NAME"};
//		String[] searchString={"a"};
//		String[] searchOn={"song"};
		
		HashMap map=new HashMap();
		map.put(searchOn[0],searchString[0]);
//		map.put(searchOn[1],searchString[1]);
//		map.put(searchOn[2],searchString[2]);
		
		ArrayList search=searchQuery(map,0);
		
		if(search != null){
			for(int i=0;i<search.size();i++)
			{
				System.out.println(search.get(i));
			}
		}
		else{
			////System.out.println("search is null in main ");
		}
		System.out.println("YA 2ND !====================================");
		
		String[] searchString1={"english songs","New Arrivals","Can You"};
		String[] searchOn1 ={"PARENT_CAT_NAME","SUB_CAT_NAME","song"};
		HashMap map1=new HashMap();
		map1.put(searchOn1[0],searchString1[0]);
		map1.put(searchOn1[1],searchString1[1]);
		map1.put(searchOn1[2],searchString1[2]);
		ArrayList search1=searchQuery(map1,0);
		if(search1 != null){
			for(int i=0;i<search1.size();i++)
			{
				System.out.println(search1.get(i));
			}
		}
		else{
			////System.out.println("search is null in main ");
		}
		
		System.out.println("YA 3RD !====================================");
		String[] searchString2={"hindi songs","murder"};
		String[] searchOn2={"PARENT_CAT_NAME","song"};
		HashMap map2=new HashMap();
		map2.put(searchOn2[0],searchString2[0]);
		map2.put(searchOn2[1],searchString2[1]);
		ArrayList search2=searchQuery(map2,0);
		if(search2 != null){
			for(int i=0;i<search2.size();i++)
			{
				System.out.println(search2.get(i));
			}
		}
		else{
			////System.out.println("search is null in main ");
		}
System.out.println("YA 4TH !====================================");
		
		String[] searchString3={"english songs","New Arrivals"};
		String[] searchOn3 ={"PARENT_CAT_NAME","SUB_CAT_NAME"};
		HashMap map3=new HashMap();
		map3.put(searchOn3[0],searchString3[0]);
		map3.put(searchOn3[1],searchString3[1]);
		//map3.put(searchOn3[2],searchString3[2]);
		ArrayList search3=searchQuery(map3,0);
		if(search3 != null){
			for(int i=0;i<search3.size();i++)
			{
				System.out.println(search3.get(i));
			}
		}
		else{
			////System.out.println("search is null in main ");
		}
		
		
	}





}
