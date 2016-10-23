package com.onmobile.apps.ringbacktones.ussd;

import java.util.ArrayList;
import java.util.HashMap;
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
import com.onmobile.apps.ringbacktones.servlets.SiteURLDetails;

public class USSDServletListner implements ServletContextListener {
	/*author@Abhinav Anand
	 */
	private static Logger logger = Logger.getLogger(USSDServletListner.class);
	private static boolean canRefreshContextAttributes=true;
	private static Object m_lock = new Object();
	public  static USSDBasicFeatures ussdFeatures=null;
	public static USSDController ussdController=null;
	private static RBTDBManager rbtDBManager=null;
	private static String USSD="USSD";
	private static String fileSeparator="/";

	public void contextInitialized(ServletContextEvent event){

		ServletContext sc=event.getServletContext();
		Tools.init("USSD",  true);
		logger.info("starting contextAttributeReinitialization from listener and started="+sc);
		System.out.println("starting contextAttributeReinitialization from listener and started=");
		contextAttributeInitialization(sc);
		logger.info("FINISHED contextAttributeReinitialization from listener =>"+sc);
		System.out.println("FINISHED contextAttributeReinitialization from listener =>");
		USSDCacheRefresher ussdCacheRefresher=new USSDCacheRefresher(sc);
		Thread thrd=new Thread(ussdCacheRefresher);
		thrd.start();

	}
	public static void contextAttributeInitialization(ServletContext sc){
		String method="contextAttributeInitialization";
		
		String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_USSD", null);
		if(rbtDBManager==null){
			rbtDBManager = RBTDBManager.init(dbURL,4);
		}
		if (ussdFeatures==null){
			logger.info("going to initialize initializeUSSDFeatureClass");

			initializeUSSDFeatureClass(sc);
		}
		if(ussdFeatures!=null && ussdController!=null){
			logger.info("Already initialized initializeUSSDFeatureClass");

			HashMap ussdMenu=new HashMap();
			logger.info("going to  initialized initializeMainMenu");
			ussdMenu=ussdFeatures.initializeMainMenu();

			if(ussdMenu!=null && !ussdMenu.isEmpty() && ussdMenu.size()>0){
				sc.setAttribute("USSD_MENU_MAP", ussdMenu);
				logger.info("HashMap is not null...successfully initialized context attribut USSD_MENU_MAP");
			}else{
				logger.info("HashMap null...failed to initialize context attribut USSD_MENU_MAP");
			}
			String siteURLDetails = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_USSD", null);
			logger.info("siteURLDetails=="+siteURLDetails);
			initializeSiteURLMap(sc,siteURLDetails);
			String localURLDetails = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "LOCAL_URL_DETAILS_USSD", null);
			logger.info("localURLDetails=="+localURLDetails);
			SiteURLDetails localurldetails=initializeLocalURLMap(sc,localURLDetails);
			synchronized (sc) {
				if(localurldetails!=null){
					logger.info("populating context Attribute \"LOCAL_URL\" with site_url_map");
					sc.setAttribute("LOCAL_URL", localurldetails);
				}
			}		
			initializeSubscriptionPacks(sc,rbtDBManager);
			Parameters defaultCatIdTemp=CacheManagerUtil.getParametersCacheManager().getParameter(USSD, "DEFAULT_USSD_CAT_ID");
			String catId="3";
			if(defaultCatIdTemp!=null){
				String catIdTemp=defaultCatIdTemp.getValue();
				if(catIdTemp!=null){
					logger.info("defaultCatId=="+catIdTemp);
					catId=catIdTemp;
				}
			}
			logger.info("defaultCatId=="+catId);
			sc.setAttribute("DEFAULT_CAT_ID", catId);
			//Added
            String chargeClassMSG = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSDCHARGECLASS_MESSAGE", null);
            logger.info("chargeclassmsgs == "+chargeClassMSG);
            if(chargeClassMSG!=null){
            	HashMap map=new HashMap();
            	ArrayList list=tokenizeArrayList(chargeClassMSG, ":");
            	ArrayList temp=null;
            	for(int i=0;i<list.size();i++){
            		temp=tokenizeArrayList((String)list.get(i), "=");
            		map.put((String)temp.get(0), (String)temp.get(1));
            	}
            	 sc.setAttribute("USSDCHARGECLASS_MESSAGE", map);
				 logger.info("chargeclassmsgs1 == "+map);
            }
           
            
			Parameters clipIdTemp=CacheManagerUtil.getParametersCacheManager().getParameter("COMMON", "DEFAULT_CLIP");
			int clipId=0;
			boolean check=false;
			if(clipIdTemp!=null){
				String clipIdValue=clipIdTemp.getValue();
				if(clipIdValue!=null){

					clipId=new Integer(clipIdValue).intValue();
					check=true;
					logger.info("defaultClipId=="+clipIdValue+" and check==true");
				}
			}
			if(check){
				logger.info("checl==true");
				Clips defaultClip=rbtDBManager.getClip(clipId);
				if(defaultClip!=null){
					logger.info("setting defaultClip as context Attribute with clipId=="+defaultClip.id());
					sc.setAttribute("DEFAULT_CLIP", defaultClip);
				}
			}
		}else{
			logger.info("initialize of USSDFeatureClass failed");
		}
	}
	private static void initializeUSSDFeatureClass(ServletContext sc){

		try {

			String ussdfeatureClass = "com.onmobile.apps.ringbacktones.ussd.USSDBasicFeatures";
			String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_USSD", null);
			int n = RBTParametersUtils.getParamAsInt(iRBTConstant.COMMON, "DB_CONN_USSD", 30);
			if(rbtDBManager==null){
				rbtDBManager = RBTDBManager.init(dbURL,n);
			}
			if(rbtDBManager!=null){
				Parameters ussdfeatureParam = CacheManagerUtil.getParametersCacheManager().getParameter(
						iRBTConstant.USSD, "USSD_FEATURE_CLASS");
				if (ussdfeatureParam != null){
					System.out.println("DB entry for \"USSD_FEATURE_CLASS\" is not null");
					ussdfeatureClass = ussdfeatureParam.getValue().trim();
				}

				logger.info("RBT:: informationClass: " + ussdfeatureClass);
				System.out.println("RBT:: informationClass: " + ussdfeatureClass);
				Class rbtUSSDfeatureClass = Class.forName(ussdfeatureClass);
				ussdFeatures = (USSDBasicFeatures) rbtUSSDfeatureClass.newInstance();

				String ussdcontrollerClass = "com.onmobile.apps.ringbacktones.ussd.USSDController";
				Parameters ussdcontrollerParam = CacheManagerUtil.getParametersCacheManager().getParameter(
						iRBTConstant.USSD, "USSD_CONTROLLER_CLASS");
				if (ussdcontrollerParam != null){
					System.out.println("DB entry for \"USSD_FEATURE_CLASS\" is not null");
					ussdcontrollerClass = ussdcontrollerParam.getValue().trim();
				}
				logger.info("RBT:: informationClass: " + ussdcontrollerClass);
				System.out.println("RBT:: informationClass: " + ussdcontrollerClass);
				Class rbtUSSDcontrollerClass = Class.forName(ussdcontrollerClass);
				ussdController = (USSDController) rbtUSSDcontrollerClass.newInstance();
			}else{
				logger.info("rbtDBManager==null");
			}
		} catch (ClassNotFoundException e) {
			logger.error("", e);
		} catch (InstantiationException e) {
			logger.error("", e);
		} catch (IllegalAccessException e) {
			logger.error("", e);
		}
		if(ussdFeatures!=null && ussdController!=null){
			logger.info("ussdFeatures!=null && ussdController!=null");
			ussdController.setUssdFeatures(ussdFeatures);
			ussdController.setInfo();
			ussdFeatures.setInfo();
			ussdController.init();
			ussdFeatures.init();
		}else{
			logger.info("Either ussdController is null or ussdFeatures is null");
		}

	}
	private static void initializeSubscriptionPacks(ServletContext sc,RBTDBManager rbtDBManager){
		String method="initializeSubscriptionPacks";
		logger.info("USSD:: entering initializeSubscriptionPacks" ); 

		String[] advanceRentalValues = null;
		ArrayList subPackDisplay=new ArrayList();
		ArrayList subPackTag=new ArrayList();
		subPackTag.add("DEFAULT");
		subPackDisplay.add("normal");

		logger.info("USSD:: going to check for advanceRentals in DB" ); 
		advanceRentalValues = rbtDBManager.getAdvancedRentalPacksForUSSD();

		if((advanceRentalValues != null)){
			logger.info("USSD:: advanceRentals in DB is valid " );
			String subPeriod=null;
			for(int i=0;i<advanceRentalValues.length;i++){

				String key=advanceRentalValues[i];
				logger.info("USSD:: checking for advanceRentals in subscription class=="+ key);
				SubscriptionClass subClass=CacheManagerUtil.getSubscriptionClassCacheManager().getSubscriptionClass(key);
				if(subClass!=null && !subClass.showOnGui()){
					continue;
				}
				if(subClass==null || subClass.getSubscriptionPeriod()==null){
					continue;
				}
//				subAmount=subClass.subscriptionAmount();
				subPeriod=getDisplayForSubPeriod(subClass.getSubscriptionPeriod());
				if(subPeriod.equalsIgnoreCase("1 Week")){
					continue;
				}
//				subAmt=(new Integer(subAmount).intValue());
//				subPer=getIntSubPer(subClass.subscriptionPeriod());
//				percentSaving=calculatePercSave(defaultCost,subAmt,subPer);
//				int temp=(int)(percentSaving*100);
//				percentSaving=((double)temp)/100;
//				String display=subPeriod+" @"+subAmt+"("+percentSaving+"% Discount)";
				String display=subPeriod;
				subPackTag.add(key);
				subPackDisplay.add(display);
				logger.info("USSD:: *****************************************advance pack initialized-------display=="+display);
				logger.info("*****************************************advance pack initialized-------------key=="+key);
//				System.out.println("*****************************************advance pack initialized");
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
//	private static double calculatePercSave(int defaultCost,int subAmt,double subPer){

//	double temp=defaultCost*subPer;
//	logger.info("total cost by default=="+temp);
//	//System.out.println("total cost by default=="+temp);
//	double temp1=(temp)-subAmt;
//	logger.info("total cost in this pack=="+subAmt);
//	//System.out.println("total cost in this pack=="+subAmt);
//	logger.info("Total saving=="+temp1);
//	//System.out.println("Total saving=="+temp1);

//	double discount=((temp1)/(temp))*100;
//	return (discount);
//	}
	private static String getDisplayForSubPeriod(String subPeriod){

		if(subPeriod.equalsIgnoreCase("M3")){
			return ("3 month advance rental pack");
		}
		else if(subPeriod.equalsIgnoreCase("M4")){
			return ("4 month advance rental pack");
		}
		else if(subPeriod.equalsIgnoreCase("M6")){
			return ("6 month advance rental pack");
		}
		else if(subPeriod.equalsIgnoreCase("M12")){
			return ("One year advance rental pack");
		}
		else if(subPeriod.equalsIgnoreCase("D7")){
			return ("1 Week");
		}

		else{
			return ("30 days normal pack");
		}
	}
	public  static String getDisplayForSubPeriodInMonth(String subPeriod){

		if(subPeriod.equalsIgnoreCase("M3")){
			return ("3 month");
		}
		else if(subPeriod.equalsIgnoreCase("M4")){
			return ("4 month");
		}
		else if(subPeriod.equalsIgnoreCase("M6")){
			return ("6 month");
		}
		else if(subPeriod.equalsIgnoreCase("M12")){
			return ("One year");
		}
		else if(subPeriod.equalsIgnoreCase("D7")){
			return ("1 Week");
		}

		else{
			return ("30 days normal pack");
		}
	}

	private static SiteURLDetails initializeLocalURLMap(ServletContext sc,String localURLDetails){
		String method="initializeLocalURLMap";
		logger.info("inside initializeLocalURLMap");
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
				if(site_url!=null){
					logger.info("site_url=="+site_url);
				}
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
				logger.info("circle_id=="+circle_id);
			}
			i++;
		}
		localurldetails=new SiteURLDetails(site_url,use_proxy,proxy_host,proxy_port,time_out,conn_time_out,circle_id);
		logger.info("exiting");

		return localurldetails;
	}
	private static int initializeSiteURLMap(ServletContext sc,String siteURLDetails){
		String method="initializeSiteURLMap";
		logger.info("inside initializeSiteURLMap");
		StringTokenizer st1=new StringTokenizer(siteURLDetails,";");
		HashMap site_url_map=new HashMap();
		int count=0;
		while(st1.hasMoreTokens()){
			logger.info("count=="+count);
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
					if(site_url!=null){
						logger.info("site_url=="+site_url);
					}
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
					logger.info("circle_id=="+circle_id);
				}
				i++;
			}

			SiteURLDetails siteurldetails=new SiteURLDetails(site_url,use_proxy,proxy_host,proxy_port,time_out,conn_time_out,circle_id);
			site_url_map.put(circle_id, siteurldetails);
			count++;
		}
		synchronized (sc) {
			if(site_url_map!=null && site_url_map.size()>0){
				logger.info("populating context Attribute \"SITE_URL_MAP\" with site_url_map of size=="+site_url_map.size());
				sc.setAttribute("SITE_URL_MAP", site_url_map);
			}
		}		
		logger.info("exiting");
		return count;
	}
	public void contextDestroyed(ServletContextEvent event){
		try{
			logger.info("Destroyed this context=>"+event.getServletContext());
		}catch(Exception exe){
			logger.error("", exe);
		}
	}
	 public static ArrayList tokenizeArrayList(String stringToTokenize,
				String delimiter)
		{

			if (stringToTokenize == null)
				return null;
			String delimiterUsed = ",";
			
			if (delimiter != null)
				delimiterUsed = delimiter;

			ArrayList result = new ArrayList();
			StringTokenizer tokens = new StringTokenizer(stringToTokenize,
					delimiterUsed);
			while (tokens.hasMoreTokens())
				result.add(tokens.nextToken());

			return result;
		}
}
