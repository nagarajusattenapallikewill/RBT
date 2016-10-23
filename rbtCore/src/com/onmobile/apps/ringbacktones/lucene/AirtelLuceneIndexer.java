package com.onmobile.apps.ringbacktones.lucene;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryFilter;
import org.apache.lucene.search.ScoreDoc;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.tangentum.phonetix.DoubleMetaphone;


public class AirtelLuceneIndexer extends AbstractLuceneIndexer{
	
	private static HashMap<String, LuceneClip> m_detail = new HashMap<String, LuceneClip>();
	protected static String[] fields = {"clipId","vcode","PARENT_CAT_NAME","SUB_CAT_NAME","song","album","artist","parentCatId", "subCatId"};
	
	boolean init(){
		try{
			client=RBTClient.getInstance();
			m_indexPath=(client.getInstance().getParameter(new ApplicationDetailsRequest("ALL","DEFAULT_REPORT_PATH",(String)null))).getValue();
		}
		catch(Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
		}
		m_metaphone = new DoubleMetaphone(6);
		File index = new File(m_indexPath + File.separator +"2");
		if (index.exists())
			index.delete();
		try{
			m_indexWriter2 = new IndexWriter(index.getAbsolutePath(),new StandardAnalyzer(), true);
		}
		catch (Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
		}
		try{
			m_indexWriter2.optimize();
			m_indexWriter2.close();
		}
		catch(IOException ioe){
			log.error(ioe.getMessage());
			ioe.printStackTrace();
		}
		this.setWriter(m_indexWriter2);
		index = new File(m_indexPath + File.separator + "1");
		if (index.exists())
			index.delete();
		try{
			m_indexWriter1 = new IndexWriter(index.getAbsolutePath(),new StandardAnalyzer(), true);
		}
		catch (Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
		}
		try{
			m_indexWriter1.optimize();
			m_indexWriter1.close();
		}
		catch(IOException ioe){
			log.error(ioe.getMessage());
			ioe.printStackTrace();
		}
		this.setWriter(m_indexWriter1);
		status=false;
		this.initializeBeforeCaliingCreateWriter();
		try{
			rbtContentCacheMgr=RBTCacheManager.getInstance();
		}
		catch(Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
		}
		getContentAndIndex();
		return true;
	}
	
	protected void getContentAndIndex() {
		Map<String, String> categoryCircleMap=new HashMap<String, String>();
		try {
			IndexWriter iw=this.initializeBeforeCaliingCreateWriter();
			iw.setMergeFactor(10000);
			iw.setMaxMergeDocs(9999999);
			iw.setMaxBufferedDocs(10000);
			ApplicationDetailsRequest applicationRequest=new ApplicationDetailsRequest();
			com.onmobile.apps.ringbacktones.webservice.client.beans.Site sites[]=client.getSites(applicationRequest);
			for(int siteCount=0;siteCount<sites.length;siteCount++){
				log.info("In getContentAndIndex(): The site length is "+sites.length);
				String circleId=sites[siteCount].getCircleID();
				log.info("In getContentAndIndex(): The circle Id is  "+circleId);
				Category[] parentCategories=null;
				char prepaidYes='b';
				if(rbtContentCacheMgr.getCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, prepaidYes)!=null && rbtContentCacheMgr.getCategoriesInCircle(circleId,PARENT_CATEGORY_ZERO,prepaidYes).length>0){
					parentCategories=rbtContentCacheMgr.getCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, prepaidYes);
				}else if(rbtContentCacheMgr.getCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, 'y')!=null && rbtContentCacheMgr.getCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, 'y').length>0){
					prepaidYes='y';
					parentCategories=rbtContentCacheMgr.getCategoriesInCircle(circleId, PARENT_CATEGORY_ZERO, prepaidYes);
				}
				if(parentCategories == null || parentCategories.length <= 0){
					log.info("In getContentAndIndex(): The parentCategories are null");
					continue;
				}
				log.info("In getContentAndIndex():  The parent Categories length is "+parentCategories.length);
				for(int i=0; i< parentCategories.length; i++){
					
					//Check for clips in the parent categories
					getClipsAndIndex(parentCategories[i],categoryCircleMap,fields,iw,true,parentCategories[i]);
					
					//get the sub categories
					Category[] subCategories = rbtContentCacheMgr.getCategoriesInCircle(circleId, parentCategories[i].getCategoryId(), prepaidYes);
					if(subCategories == null || subCategories.length <= 0)
						continue;
					for(int j=0; j< subCategories.length; j++){
						getClipsAndIndex(subCategories[j],categoryCircleMap,fields,iw, false, parentCategories[i]);
						int subCatId=subCategories[j].getCategoryId();
						
						// Now try and get the categories under sub categories if any and index the clips in those categories.
						Category[] subSubCategories = rbtContentCacheMgr.getCategoriesInCircle(circleId, subCatId, prepaidYes);
						if(subSubCategories == null || subSubCategories.length <= 0)
							continue;
						for(int h=0; h< subSubCategories.length; h++){
							getClipsAndIndex(subSubCategories[h],categoryCircleMap,fields,iw, false, parentCategories[i]);
						}
					}
					log.info(_class+" GuiSearch::Imformation Indexing clips for category name "+parentCategories[i].getCategoryName());
				}
			}
			log.info("GuiSearch::Imformation  caching done Number of documents indexed = "+iw.docCount());
			optimizeIndexWriter(iw);
			Calendar cal1 = Calendar.getInstance();
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			log.info(_class+ "GuiSearch::Imformation b4 calling create index "+sdf1.format(cal1.getTime()));
			Calendar cal2 = Calendar.getInstance();
			SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			log.info(_class +"GuiSearch::Imformation after calling create index and before search "+sdf2.format(cal2.getTime()));
		} 
		catch (Exception e2){
			e2.printStackTrace();
			log.error(_class+ "GuiSearch::Exception caught "+ e2.getMessage());
		}
		log.info("Created index at path "+m_indexPath);
	}
	
	
		
	public ArrayList searchQuery(HashMap map,int pageNo, int maxResults){
		try{
			rbtContentCacheMgr=RBTCacheManager.getInstance();
		}
		catch(Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
		}
		m_metaphone = new DoubleMetaphone(6);
		m_web_results=maxResults;
		String dir="1";
		IndexSearcher iSearcher = null;
		IndexWriter iw =null;
		String _method = "searchQuery";
		ArrayList hitList = new ArrayList();
		org.apache.lucene.document.Document tmpDoc = null;
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
		System.out.println(_class+ " searchQuery got the page number and hence returning "+m_web_results+" reults");
		System.out.println("got the page number and hence returning "+m_web_results+" reults");


		int cou=0;
		for(int count=0;count<6;count++){
			try{
				String tem=(String)map.get(names[count]);
				if(tem!=null){
					tem=tem.toString();
				}
				if(tem != null && tem.length()>0){
					System.out.println(_class + " searchQuery tem is not null");
					searchOn[cou]=names[count];
					if(names[count].equalsIgnoreCase("song") || names[count].equalsIgnoreCase("album") || names[count].equalsIgnoreCase("artist")){
						//searchString[cou]=tem+"*";
						/*Avoided using * since its being used as a prefix query and results in more than 1024 results
						Lucene throws an exception if the BooleanCluses exceed more than 1024. 
						*/
						searchString[cou]=tem;
					}
					else{
						searchString[cou]=tem;
						if(names[count].equalsIgnoreCase("PARENT_CAT_NAME"))
							parentCat=searchString[cou];
						if(names[count].equalsIgnoreCase("SUB_CAT_NAME"))
							subCat=searchString[cou];
					}
					System.out.println("::"+searchOn[cou]+" "+searchString[cou]);
					System.out.println(_class +" searchQuery searchOn lastest entry value=="+searchOn[cou]+" searchString lastest entry value=="+searchString[cou]+" cou=="+cou);
					cou++;
				}
			}
			catch(Exception e){
				log.error(e.getMessage());
				System.out.println("argument not found");
			}
		}
		try
		{
			String[] sString=new String[cou];
			boolean status1 = false;
			StringBuffer searchBuffer=new StringBuffer();
			StringBuffer vcodeBuffer=new StringBuffer();
			int countOfParameter=0;
			String searchinOn=null;
			if(searchOn != null){
				for (int i=0;i<cou;i++){
					if (searchOn[i].equalsIgnoreCase("vcode")){
						sString = new String[1];
						sString[0] = searchString[i];
						status1 = true;
						break;
					}else{
						if(searchOn[i].equalsIgnoreCase("PARENT_CAT_NAME")||searchOn[i].equalsIgnoreCase("SUB_CAT_NAME")){
							searchBuffer.append("("+searchOn[i]+":"+searchString[i]+")");
							int searchLength=searchOn.length;
							if (i<(cou-1))
								searchBuffer.append("AND");
						}
						else{
							sString[i]=searchString[i]+" ";
							countOfParameter=i;
							searchinOn=searchOn[i];
						}
					}

				}

			}
			
			if (!status1){
				System.out.println("countOfParameter="+countOfParameter);
				if(sString[countOfParameter]!=null){
					String c=getQueryString(sString[countOfParameter]);
					c=c.substring(0,c.indexOf(")"))+" OR "+sString[countOfParameter]+")";
					System.out.println("::c is "+c+" sstring is "+sString[countOfParameter]);
					searchBuffer.append(c);
				}
			}
			else{
				searchOn = new String [1];
				searchOn [0] = "vcode";
				vcodeBuffer.append("("+sString[0]+")");
			}
			log.info(" :::: "+vcodeBuffer.toString());
			log.info(" ::::"+searchBuffer.toString());

			log.info(" searchQuery sString is "+sString);
			Query query;
			log.info("status is "+status1);
			if(status1)
			{
				BooleanClause.Occur[] flags = {BooleanClause.Occur.MUST};
				query = MultiFieldQueryParser.parse(vcodeBuffer.toString(),searchOn,flags,new StandardAnalyzer());
			}
			else
			{
				BooleanClause.Occur[] flags = {BooleanClause.Occur.SHOULD};
				String[] test=new String[1];
				test[0]=searchOn[countOfParameter];
				query= MultiFieldQueryParser.parse(searchBuffer.toString(), test, flags, new StandardAnalyzer());
			}
			log.info(_class +" searchQuery query=="+query.toString()+" and result=="+results);
			log.info("query=="+query.toString()+" and result=="+results);
			try{ 
				iSearcher = null;
				File index = new File(m_indexPath + File.separator +"2");
				if (index.exists())
					index.delete();
				try{
					m_indexWriter2 = new IndexWriter(index.getAbsolutePath(),new StandardAnalyzer(), false);
				}
				catch (Exception e){
					log.error(e.getMessage());
					e.printStackTrace();
				}
				try{
					m_indexWriter2.optimize();
					m_indexWriter2.close();
				}
				catch(IOException ioe){
					log.error(ioe.getMessage());
					ioe.printStackTrace();
				}
				iw=m_indexWriter2;
				if (iw == null)
					return null;
				
				try {
					iSearcher = new IndexSearcher(m_indexPath + File.separator + "2");
				} catch (IOException e1) {
					log.error(e1.getMessage());
					e1.printStackTrace();
				}
				scores= iSearcher.search(query, new QueryFilter(query), results).scoreDocs;
			}
			catch(Exception e){
				log.info("Caught an exception while searching");
				e.printStackTrace();
			}
			if (scores == null || scores.length == 0){
				System.out.println("Scores are null..");
				return null;
			}
			log.info("Scores Length "+scores.length);
			for (int i = 0; i < scores.length; i++){
				tmpDoc = iSearcher.doc(scores[i].doc);
				if(tmpDoc.get("parentCatId")!=null && !tmpDoc.get("parentCatId").equals("")){
					if(tmpDoc.get("subCatId")!=null && !tmpDoc.get("subCatId").equals("")){
						hitList.add(tmpDoc.get("clipId")+"_"+tmpDoc.get("parentCatId")+"_"+tmpDoc.get("subCatId"));
						log.info("parent = "+tmpDoc.get("parentCatId"));
						log.info("sub = "+tmpDoc.get("subCatId"));
					}else{
						hitList.add(tmpDoc.get("clipId")+"_"+tmpDoc.get("parentCatId")+"_null");
						log.info("parent = "+tmpDoc.get("parentCatId"));
					}
				}
				log.info("scores "+i+" "+tmpDoc);
			}


		}
		catch (Exception e){
			log.error(e.getMessage());
			e.printStackTrace();
		}

		finally{
			try{
				if (iSearcher != null)
					iSearcher.close();
			}
			catch (Exception e){
				log.error(e.getMessage());
				e.printStackTrace();
			}
		}

		if (hitList == null || scores == null)
			return null;
		String[] temp= (String[]) hitList.toArray(new String[0]);
		this.totalSearchSize=temp.length;
		ArrayList<LuceneClip> clipdetails=new ArrayList<LuceneClip>(temp.length);
		ArrayList<LuceneClip> clipRest=new ArrayList<LuceneClip>(temp.length);
		int i=results-m_web_results;
		log.info("scores.length= "+scores.length+"  i= "+i+" results ="+results+" m_web_results ="+m_web_results+" temp.length="+temp.length);
		boolean add=true;
		log.info("---------------parentCat is "+parentCat+"  and subCat is "+subCat);
		for(; i<temp.length && i<results; i++){
			Clip clip=rbtContentCacheMgr.getClip((temp[i].split("_"))[0]);
			LuceneClip lucClip=new LuceneClip();
			lucClip.setParentCategoryId(Integer.parseInt((temp[i].split("_"))[1]));
			lucClip.setParentCategoryName(rbtContentCacheMgr.getCategory(Integer.parseInt((temp[i].split("_"))[1])).getCategoryName());
			if(!(temp[i].split("_"))[2].equals("null")){
				lucClip.setSubCategoryId(Integer.parseInt((temp[i].split("_"))[2]));
				lucClip.setSubCategoryName(rbtContentCacheMgr.getCategory(Integer.parseInt((temp[i].split("_"))[2])).getCategoryName());
			}
			String vcode=clip.getClipRbtWavFile();
			vcode=vcode.replaceAll("rbt", "");
			vcode=vcode.replaceAll("_", "");
			lucClip.setVcode(vcode);
			lucClip.setClipId(clip.getClipId());
			lucClip.setClipName(clip.getClipName());
			lucClip.setAlbum(clip.getAlbum());
			lucClip.setArtist(clip.getArtist());
			lucClip.setClipSmsAlias(clip.getClipSmsAlias());
			lucClip.setLanguage(clip.getLanguage());
			lucClip.setClipGrammar(clip.getClipGrammar());
			lucClip.setClipInfo(clip.getClipInfo());
			lucClip.setClassType(clip.getClassType());
			lucClip.setClipStartTime(clip.getClipStartTime());
			lucClip.setClipEndTime(clip.getClipEndTime());
			lucClip.setClipPromoId(clip.getClipPromoId());
			lucClip.setClipPreviewWavFile(clip.getClipPreviewWavFile());
			lucClip.setClipRbtWavFile(clip.getClipRbtWavFile());
			lucClip.setSmsStartTime(clip.getSmsStartTime());
			lucClip.setAddToAccessTable(clip.getAddToAccessTable());
			lucClip.setClipDemoWavFile(clip.getClipDemoWavFile());
			
			String tempParCat=null;
			String tempSubCat=null;
			if(parentCat!=null){
				tempParCat=lucClip.getParentCategoryName();
				if(!tempParCat.equalsIgnoreCase(parentCat))
					add=false;
			}
			if(subCat!=null){
				tempSubCat=lucClip.getSubCategoryName();
				if(!tempSubCat.equalsIgnoreCase(subCat))
					add=false;
			}
			if(add)
				clipdetails.add(lucClip);
			else
				clipRest.add(lucClip);
		}
		clipdetails.addAll(clipRest);
		return clipdetails;
	}

	
	private void getClipsAndIndex(Category category, Map<String, String>categoryCircleMap, String[] fields, IndexWriter iw, boolean parentYes, Category parentCategory){
		//if we are to get the clips inside the parent cat then the key would be clipId_parentCatId_null
		// If we are to get the clips inside the sub cat then the key would be clipId_parentCatId_subCatId
		int subCatId=category.getCategoryId();
		String key=null;
		if(parentYes)
			key="_"+parentCategory.getCategoryId()+"_null";
		else
			key="_"+parentCategory.getCategoryId()+"_"+subCatId;
		String subCategoryName=category.getCategoryName();
		Clip[] clipsInCat=rbtContentCacheMgr.getClipsInCategory(subCatId);
		if(clipsInCat!=null && clipsInCat.length>0){
			for(int h=0;h<clipsInCat.length;h++){
				if(clipsInCat[h]!=null ){
					if(categoryCircleMap.get(clipsInCat[h].getClipId()+key)==null){
						categoryCircleMap.put(clipsInCat[h].getClipId()+key,Integer.toString(clipsInCat[h].getClipId()));
						String wavFile = clipsInCat[h].getClipRbtWavFile();
						String vcode = wavFile;
						vcode=vcode.replaceAll("rbt", "");
						vcode=vcode.replaceAll("_", "");
						String clipName = clipsInCat[h].getClipName();
						String album = clipsInCat[h].getAlbum();
						String singer = clipsInCat[h].getArtist();
						int clipID = clipsInCat[h].getClipId();
						String[] values=new String[9];
						values[0]=clipID+"";
						values[1]=vcode;
						values[2]=parentCategory.getCategoryName();
						values[4]=clipName;
						values[5]=album;
						values[6]=singer;
						values[7]=parentCategory.getCategoryId()+"";
						if(parentYes){
							values[3]=null;
							values[8]=null;
						}else{
							values[3]=subCategoryName;
							values[8]=subCatId+"";
						}
						LuceneWorkerThread worker=new LuceneWorkerThread(fields);
						worker.createModifyDocuments(iw, fields, values, "Main");
					}
					
				}
			}
		}
	}

	private static final String _class = "LuceneIndexer";

	@Override
	public int getTotalSearchSize() {
		return totalSearchSize;
		
	}
	
	

	
}
