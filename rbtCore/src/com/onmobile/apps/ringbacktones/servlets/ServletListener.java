package com.onmobile.apps.ringbacktones.servlets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.subscriptions.ClipCacher;
import com.onmobile.apps.ringbacktones.subscriptions.GuiSearch;



public class ServletListener implements ServletContextListener {
	
	private static Logger logger = Logger.getLogger(ServletListener.class);
	
	private static boolean canRefreshContextAttributes=true;
	private static Object m_lock = new Object();
	public void contextInitialized(ServletContextEvent event){

		ServletContext sc=event.getServletContext();
		Tools.init("ccc",  true);
		logger.info("starting contextAttributeReinitialization from listener and started="+sc);
		contextAttributeReinitialization(sc);
		logger.info("FINISHED contextAttributeReinitialization from listener =>"+sc);
		GUIDaemon guidaemon=new GUIDaemon(sc);
		Thread thrd=new Thread(guidaemon);
		thrd.start();
		
		

	}
	public void contextDestroyed(ServletContextEvent event){
		try{
			logger.info("Destroyed this context=>"+event.getServletContext());
		}catch(Exception exe){
			logger.error("", exe);
		}
	}
	public static void initializeDisplaymenu(ServletContext sc){
		String[] displaymenuTemp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DISPLAY_MENU", "").split(",");
		HashMap displaymenumap=new HashMap();
		for(int i=0;i<displaymenuTemp.length;i++){
		 	String key=displaymenuTemp[i].substring(0, displaymenuTemp[i].indexOf("#"));
		 	String value=displaymenuTemp[i].substring(displaymenuTemp[i].indexOf("#")+1);
		 	displaymenumap.put(key, value);
		}
		sc.setAttribute("DISPLY_MENU_MAP", displaymenumap);
	}
	public static void contextAttributeReinitialization(ServletContext sc){
		logger.info("CCC::entering contextAttributeReinitialization" ); 
		logger.info("CCC:: initializing ClipCacher" ); 
		ClipCacher tempClipCacher=ClipCacher.init();
		logger.info("CCC:: initializing ClipCacher done" );
		 
		logger.info("CCC:: initializing GuiSearch" ); 
		try {
			logger.info("printing sys property"+System.getProperty("java.io.tmpdir") );
			GuiSearch.init();
		} catch (Throwable e) {
			// TODO Auto-generated catch block
			logger.error("", e);
		}
		logger.info("CCC:: initializing GuiSearch done" );
		
		String testStatus = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "TEST_STATUS", null);
		sc.setAttribute("TEST_STATUS", testStatus);
		
		logger.info("CCC:: TEST_STATUS=="+testStatus );
		List testNumbers = Arrays.asList(RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "TEST_NUMBERS", "").split(","));
		sc.setAttribute("TEST_NUMBERS", testNumbers);
		if(testNumbers!=null){
		logger.info("CCC:: TEST_NUMBERS size=="+testNumbers.size() );
		}else{
			logger.info("CCC:: TEST_NUMBERS size==null" );
		}
		String testCircleId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "TEST_CIRCLE_ID", null);
		sc.setAttribute("TEST_CIRCLE_ID", testCircleId);
		logger.info("CCC:: TEST_CIRCLE_ID =="+testCircleId );
		
		String userName = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DEFAULT_USER", null);
		sc.setAttribute("DEFAULT_USER", userName);
		String password = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DEFAULT_PASSWORD", null);
		sc.setAttribute("DEFAULT_PASSWORD", password);
		String northCircleId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "NORTH_CIRCLE_ID", null);
		sc.setAttribute("NORTH_CIRCLE_ID", northCircleId);
		String northEastCircleId = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "NORTH_CIRCLE_ID", null);
		sc.setAttribute("NORTH_WEST_CIRCLE_ID", northEastCircleId);
		String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_GUI", null);
		String hrlTick = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "HLR_TICK", null);
		String[] Prompt_Path = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "PROMPT_PATH", "").split(",");
		String[] clipFolders = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "CLIP_FOLDER", "").split(",");
		ArrayList promptpaths=new ArrayList();
		if (clipFolders!=null) {
			for (int i = 0; i < clipFolders.length; i++) {
				promptpaths.add(Prompt_Path[0]+"\\"+clipFolders[i]);
				
			}
		}	
		else{
			promptpaths.add(Prompt_Path);
		}
		synchronized (sc) {
			sc.setAttribute("PROMPT_PATH_LIST",Prompt_Path);
			sc.setAttribute("PROMPT_PATH", promptpaths);
		}
		
		String prompt_location = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "PROMPT_LOCATION", null);
		sc.setAttribute("PROMPT_LOCATION",prompt_location); 
		
		String sdrWorkingDir = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SDR_WORKING_DIR", "e:\\\\CCC\\\\report");
         int sdrSize = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "SDR_SIZE", 1000);
         long sdrInterval = RBTParametersUtils.getParamAsLong(iRBTConstant.COMMON, "SDR_INTERVAL", 24);
         String sdrRotation = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SDR_ROTATION", "size");
         boolean sdrBillingOn = RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "SDR_BILLING_ON", "TRUE");
         logger.info("CCC:: sdrSize =="+sdrSize );
         logger.info("CCC:: sdrWorkingDir =="+sdrWorkingDir );
         logger.info("CCC:: sdrInterval =="+sdrInterval );
         logger.info("CCC:: sdrRotation =="+sdrRotation );
         logger.info("CCC:: sdrBillingOn =="+sdrBillingOn );
         logger.info("CCC:: going to create ConfigForAccounting Object");
         CCCAccountingManager.configurations=new ConfigForAccounting(sdrWorkingDir,sdrSize,sdrInterval,sdrRotation,sdrBillingOn);
		int n = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "NUM_CONN", 4);

		RBTDBManager rbtdbManager=rbtdbManager=RBTDBManager.init(dbURL, n);
		Parameters haftaToAdvanceAllowed = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "HAFTA_TO_ADVANCE_ALLOWED"); 
		if(haftaToAdvanceAllowed!=null && haftaToAdvanceAllowed.getValue()!=null){ 
        	String strHaftaToAdvanceAllowed=haftaToAdvanceAllowed.getValue().trim(); 
        	sc.setAttribute("HAFTA_TO_ADVANCE", strHaftaToAdvanceAllowed); 
        } 
		Parameters defaultCatIdTemp = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_CCC_GUI_CAT_ID");
		boolean chekCatId=false;
		String catId=null;
		if(defaultCatIdTemp!=null){
			String catIdTemp=defaultCatIdTemp.getValue();
			
			if(catIdTemp!=null){
				catId=catIdTemp;
			}else{
				catId="3";
			}
		}else{
			catId="3";
		}
		sc.setAttribute("DEFAULT_CAT_ID", catId);
		
		Parameters clipIdTemp = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_CLIP");
		int clipId=0;
		boolean check=false;
		if(clipIdTemp!=null){
			String clipIdVaue=clipIdTemp.getValue();
			if(clipIdVaue!=null){
				clipId=new Integer(clipIdVaue).intValue();
				check=true;
			}
		}
		if(check){
		Clips defaultClip=rbtdbManager.getClip(clipId);
		if(defaultClip!=null){
			
		sc.setAttribute("DEFAULT_CLIP", defaultClip);
		}
		}
		Parameters param = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "SUBMGR_URL_FOR_DEACT_DAYS_LEFT");
		if(param!=null && param.getValue()!=null ){
			String subMgrUrlForDeactivationDaysLeft=param.getValue();
			sc.setAttribute("SUB_MGR_URL_UNSUB_DAYS_LEFT", subMgrUrlForDeactivationDaysLeft);
			logger.info("Parameter table Param subMgrUrlForDeactivationDaysLeft=="+subMgrUrlForDeactivationDaysLeft);
		}else{
			logger.info("subMgrUrlForDeactivationDaysLeft is not configured in parameters table");
		}
//		String defaultClipId = rbtdbManager.getDefaultClipId();
//		int clipId=new Integer(defaultClipId).intValue();
//		Clips defaultClip=rbtdbManager.getClip(clipId);
//		sc.setAttribute("DEFAULT_CLIP", defaultClip);
//		sc.setAttribute("DEFAULT_CLIP_ID", new Integer(clipId));
		//SubscriberDetails subDet=new SubscriberDetails(defaultClip);
		
		initializeDisplaymenu(sc);

		String siteURLDetails = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SITE_URL_DETAILS", null);
		initializeSiteURLMap(sc,siteURLDetails);
		String localURLDetails = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "LOCAL_URL_DETAILS", null);
		SiteURLDetails localurldetails=initializeLocalURLMap(sc,localURLDetails);
		synchronized (sc) {
			sc.setAttribute("LOCAL_URL", localurldetails);
		}		
		initializePromotions(sc,rbtdbManager);
		initializeSubscriptionPacks(sc,rbtdbManager);
		SiteURLDetails hrlTickURLDetails=initializeLocalURLMap(sc,hrlTick);
		synchronized (sc) {
			sc.setAttribute("HLR_TICK", hrlTickURLDetails);
		}		
//		String[] lang = {"eng","hin","kan","tam","tel","pun","guj","mar","mal","ori","ben","ass","boj"};
		String[] lang = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "LANG", "").split(",");
//		String[] langValues = {"English","Hindi","Kannada","Tamil","Telugu","Pujabi","Gujarathi","Marathi","Malayalam","Oriya","Bengali","Assamese","Bhojpuri"};
		String[] langValues = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "LANG_VALUES", "").split(",");	
		synchronized (sc) {
			sc.setAttribute("LANG", lang);
			sc.setAttribute("LANG_VALUE", langValues);
		}		
	}
	private static void initializeSubscriptionPacks(ServletContext sc,RBTDBManager rbtDBManager){
		logger.info("CCC:: entering initializeSubscriptionPacks" ); 
		String[] advanceRentalValues = null;
		ArrayList subPackDisplay=new ArrayList();
		ArrayList subPackTag=new ArrayList();
		subPackTag.add("DEFAULT");
		subPackDisplay.add("normal");
		int defaultCost = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DEFAULT_COST", 0);
		logger.info("CCC:: going to check for advanceRentals in DB" ); 
		advanceRentalValues = rbtDBManager.getAdvancedRentalValuesDB();
		
		if((advanceRentalValues != null)){
			logger.info("CCC:: advanceRentals in DB is valid " );
			String subAmount=null;
			String subPeriod=null;
			int subAmt=0;
			double subPer=0;
			double percentSaving=0.0;
			for(int i=0;i<advanceRentalValues.length;i++){
				
				String key=advanceRentalValues[i];
				logger.info("CCC:: checking for advanceRentals in subscription class=="+ key);
				SubscriptionClass subClass = CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(key);
				if(!subClass.showOnGui()){
					continue;
				}
				subAmount=subClass.getSubscriptionAmount();
				subPeriod=getDisplayForSubPeriod(subClass.getSubscriptionPeriod());
				if(subPeriod.equalsIgnoreCase("1 Week")){
					continue;
				}
				subAmt=(new Integer(subAmount).intValue());
				subPer=getIntSubPer(subClass.getSubscriptionPeriod());
				percentSaving=calculatePercSave(defaultCost,subAmt,subPer);
				int temp=(int)(percentSaving*100);
				percentSaving=((double)temp)/100;
				String display=subPeriod+" @"+subAmt+"("+percentSaving+"% Discount)";
				subPackTag.add(key);
				subPackDisplay.add(display);
				logger.info("CCC:: *****************************************advance pack initialized");
				logger.info("*****************************************advance pack initialized");
//System.out.println("*****************************************advance pack initialized");
			}
			sc.setAttribute("DISPLAY_SUB_PACK", subPackDisplay);
			sc.setAttribute("SUB_PACK_TAG", subPackTag);

		}else{
			sc.setAttribute("DISPLAY_SUB_PACK", subPackDisplay);
			sc.setAttribute("SUB_PACK_TAG", subPackTag);
		}
		

	}
	private static double getIntSubPer(String subPeriod){
		if(subPeriod.equalsIgnoreCase("M3")){
			return (3.0);
		}
		else if(subPeriod.equalsIgnoreCase("M4")){
			return (4.0);
		}
		else if(subPeriod.equalsIgnoreCase("M6")){
			return (6.0);
		}
		else if(subPeriod.equalsIgnoreCase("M12")){
			return (12.0);
		}
		else if(subPeriod.equalsIgnoreCase("D8")){
			return (0.25 );
		}

		else{
			return (1.0);
		}
	}
	private static double calculatePercSave(int defaultCost,int subAmt,double subPer){

		double temp=defaultCost*subPer;
		logger.info("total cost by default=="+temp);
		//System.out.println("total cost by default=="+temp);
		double temp1=(temp)-subAmt;
		logger.info("total cost in this pack=="+subAmt);
		//System.out.println("total cost in this pack=="+subAmt);
		logger.info("Total saving=="+temp1);
		//System.out.println("Total saving=="+temp1);

		double discount=((temp1)/(temp))*100;
		return (discount);
	}
	private static String getDisplayForSubPeriod(String subPeriod){
		
		if(subPeriod.equalsIgnoreCase("M3")){
			return ("3 Months");
		}
		else if(subPeriod.equalsIgnoreCase("M4")){
			return ("4 Months");
		}
		else if(subPeriod.equalsIgnoreCase("M6")){
			return ("6 Months");
		}
		else if(subPeriod.equalsIgnoreCase("M12")){
			return ("12 Months");
		}
		else if(subPeriod.equalsIgnoreCase("D7")){
			return ("1 Week");
		}

		else{
			return ("30 days");
		}
	}
	private static SiteURLDetails initializeLocalURLMap(ServletContext sc,String localURLDetails){
		StringTokenizer st2=new StringTokenizer(localURLDetails,",");
		SiteURLDetails localurldetails=null;
		int i=0;
		String site_url=null;
		boolean use_proxy=false;
		String proxy_host=null;
		int proxy_port=0;
		int time_out=5000;
		int conn_time_out=3000;
		String circle_id=null;
		while(st2.hasMoreElements()){
			if(i==0){
				site_url=st2.nextToken();
			}
			else if(i==1){
				String check=st2.nextToken();
				if(check.equalsIgnoreCase("true"))
					use_proxy=true;
				else
					use_proxy=false;
			}
			else if(i==2){
				proxy_host=st2.nextToken();
			}
			else if(i==3){
				proxy_port=(new Integer(st2.nextToken())).intValue();
			}
			else if(i==4){
				time_out=(new Integer(st2.nextToken())).intValue();
			}
			else if(i==5){
				conn_time_out=(new Integer(st2.nextToken())).intValue();
			}
			else{
				circle_id=st2.nextToken();
			}
			i++;
		}
		localurldetails=new SiteURLDetails(site_url,use_proxy,proxy_host,proxy_port,time_out,conn_time_out,circle_id);


		return localurldetails;
	}
	private static int initializeSiteURLMap(ServletContext sc,String siteURLDetails){
		StringTokenizer st1=new StringTokenizer(siteURLDetails,";");
		HashMap site_url_map=new HashMap();
		int count=0;
		while(st1.hasMoreTokens()){
			String temp=st1.nextToken();
			StringTokenizer st2=new StringTokenizer(temp,",");
			int i=0;
			String site_url=null;
			boolean use_proxy=false;
			String proxy_host=null;
			int proxy_port=0;
			int time_out=5000;
			int conn_time_out=3000;
			String circle_id=null;
			while(st2.hasMoreElements()){
				if(i==0){
					site_url=st2.nextToken();
				}
				else if(i==1){
					String check=st2.nextToken();
					if(check.equalsIgnoreCase("true"))
						use_proxy=true;
					else
						use_proxy=false;
				}
				else if(i==2){
					proxy_host=st2.nextToken();
				}
				else if(i==3){
					proxy_port=(new Integer(st2.nextToken())).intValue();
				}
				else if(i==4){
					time_out=(new Integer(st2.nextToken())).intValue();
				}
				else if(i==5){
					conn_time_out=(new Integer(st2.nextToken())).intValue();
				}
				else{
					circle_id=st2.nextToken();
				}
				i++;
			}

			SiteURLDetails siteurldetails=new SiteURLDetails(site_url,use_proxy,proxy_host,proxy_port,time_out,conn_time_out,circle_id);
			site_url_map.put(circle_id, siteurldetails);
			count++;
		}
		synchronized (sc) {
			sc.setAttribute("SITE_URL_MAP", site_url_map);
		}		
		return count;
	}

	public static void initializePrefixes(ServletContext sc)
	{
		if(sc.getAttribute("LOCAL_URL") != null && sc.getAttribute("SITE_URL_MAP") != null)
			return;

		synchronized(m_lock)
		{
			if(sc.getAttribute("LOCAL_URL") != null && sc.getAttribute("SITE_URL_MAP") != null)
			return;
			logger.info("sc Attributes LOCAL_URL or SITE_URL_MAP found null. So ccalling rest of initializePrefixes()" );			

			String localURLDetails = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "LOCAL_URL_DETAILS", null);
			SiteURLDetails localurldetails=initializeLocalURLMap(sc,localURLDetails);
			synchronized (sc) 
			{
					sc.setAttribute("LOCAL_URL", localurldetails);
			}
			String siteURLDetails = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "SITE_URL_DETAILS", null);
			initializeSiteURLMap(sc,siteURLDetails);
			ClipCacher.updateOperatorPrefixes();
			ClipCacher.updateCircleID();
		}
	}
	private static void initializePromotions(ServletContext sc,RBTDBManager rbtdbManager){ 
        HashMap siteURLDetailsMap=(HashMap)sc.getAttribute("SITE_URL_MAP"); 
        Set keyset=siteURLDetailsMap.keySet(); 
        Iterator iter=keyset.iterator(); 
        while(iter.hasNext()){ 
                String tempCircleId=(String)iter.next(); 
                logger.info("CCC:: CIRCLE_ID =="+tempCircleId ); 
                SiteURLDetails siteurldetails=(SiteURLDetails)(siteURLDetailsMap.get(tempCircleId)); 
                if(siteurldetails!=null){ 

                String circleId=siteurldetails.circle_id; 
                logger.info("CCC:: siteurldetails is not null with circle id =="+circleId ); 
                circleId=circleId.trim(); 
                circleId=circleId.toUpperCase(); 
                logger.info("CCC:: siteurldetails is not null with circle id =="+circleId ); 
                String strPromo=circleId+"_PROMOTIONS"; 
                logger.info("CCC:: siteurldetails is not null with promotion string =="+strPromo ); 

                Parameters promotions = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", strPromo); 
                if(promotions!=null && promotions.getValue()!=null && promotions.getValue().length()>0){ 
                        ArrayList arrPromoList=new ArrayList(); 
                        String temppromo=promotions.getValue().trim(); 
                        StringTokenizer st=new StringTokenizer(temppromo,","); 
                        while(st.hasMoreTokens()){ 
                                String temp=st.nextToken(); 
                                arrPromoList.add(temp); 
                        } 
                        if(arrPromoList!=null && arrPromoList.size()>0){ 
                                sc.setAttribute(strPromo, arrPromoList); 
                        } 
                } 
                } 
        } 
        //LOCAL_URL 
        SiteURLDetails localurldetails=(SiteURLDetails)sc.getAttribute("LOCAL_URL"); 
        if(localurldetails!=null){ 


                String circleId=localurldetails.circle_id; 
                logger.info("CCC:: siteurldetails is not null with circle id =="+circleId ); 
                circleId=circleId.trim(); 
                circleId=circleId.toUpperCase(); 
                logger.info("CCC:: siteurldetails is not null with circle id =="+circleId ); 
                String strPromo=circleId+"_PROMOTIONS"; 
                logger.info("CCC:: siteurldetails is not null with promotion string =="+strPromo ); 

                Parameters promotions = CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", strPromo); 
                if(promotions!=null && promotions.getValue()!=null && promotions.getValue().length()>0){ 
                        ArrayList arrPromoList=new ArrayList(); 
                        String temppromo=promotions.getValue().trim(); 
                        StringTokenizer st=new StringTokenizer(temppromo,","); 
                        while(st.hasMoreTokens()){ 
                                String temp=st.nextToken(); 
                                arrPromoList.add(temp); 
                        } 
                        if(arrPromoList!=null && arrPromoList.size()>0){ 
                                sc.setAttribute(strPromo, arrPromoList); 
                        } 
                } 

        } 
} 

	
}
