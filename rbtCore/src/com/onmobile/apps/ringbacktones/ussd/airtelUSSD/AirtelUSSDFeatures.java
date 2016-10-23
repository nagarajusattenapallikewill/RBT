package com.onmobile.apps.ringbacktones.ussd.airtelUSSD;

import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.ussd.FeatureInterface;
import com.onmobile.apps.ringbacktones.ussd.USSDBasicFeatures;
import com.onmobile.apps.ringbacktones.ussd.USSDConstants;
import com.onmobile.apps.ringbacktones.ussd.USSDInfo;

public class AirtelUSSDFeatures extends USSDBasicFeatures implements FeatureInterface{
	/*author@Abhinav Anand
	 */
	private static Logger logger = Logger.getLogger(AirtelUSSDFeatures.class);
	public  HashMap<String,HashMap> airtelUSSDMenu =new HashMap();
	private  int airtelCurrentParentProcessId=0;
	private  int airtelCurrentProcessId=1;
	private  static RBTDBManager rbtDBManager =null; 
	public   AirtelUSSDConstants airtelInfo=null;
	private static String m_class="AirtelUSSDFeatures";
	
	public USSDConstants getInfo() {
		return airtelInfo;
	}
	public void setInfo() {
		String method="setInfo";
		super.setInfo();
		airtelInfo=new AirtelUSSDConstants(super.getInfo());
		logger.info("info.ussdCatsFreeZone=="+info.ussdCatsFreeZone);
		
	}
	public HashMap getUSSDMenu() {
		return airtelUSSDMenu;
	}
	public void setUSSDMenu(HashMap menu) {
		super.setUSSDMenu(menu);
		airtelUSSDMenu = menu;
	}
	public void init(){
		super.init();
		rbtDBManager = RBTDBManager.getInstance();
	}
	public HashMap initializeMainMenu(){
		logger.info("entering initializeMainMenu");
		rbtDBManager = RBTDBManager.getInstance();
		logger.info("going to get AllSitePrefixes ");
		List<SitePrefix> sitePrefix=CacheManagerUtil.getSitePrefixCacheManager().getAllSitePrefix();
		if (sitePrefix!=null && sitePrefix.size()>0) {
			logger.info("getAllSitePrefixes return not null value");
			for (int count = 0; count < sitePrefix.size(); count++) {
				logger.info("count=="+count);
				SitePrefix tempPrefix=sitePrefix.get(count);
			
				if(tempPrefix!=null && tempPrefix.getCircleID()!=null && tempPrefix.getCircleID().length()>0){
					logger.info("site is valid");
					logger.info("site circle ID=="+tempPrefix.getCircleID());
					HashMap circleBasedMap=new HashMap();
					logger.info("going to populate mainMenuHashMap");
					populateMainMenuHashMap(circleBasedMap,airtelCurrentParentProcessId,airtelCurrentProcessId,tempPrefix.getCircleID().trim(),'b');
					if(circleBasedMap!=null && !circleBasedMap.isEmpty() && circleBasedMap.size()>0){
						logger.info("successfully populated mainMenuHashMap");
						airtelUSSDMenu.put(tempPrefix.getCircleID().trim(), circleBasedMap);
					}else{
						logger.info("failed to populate mainMenuHashMap");
					}
				}else{
					if(tempPrefix==null){
						logger.info("site==null");
						
					}else if(tempPrefix.getCircleID()==null){
						logger.info("site cirleId==null");
						
					}else{
						logger.info("site cirleId length=<0");
						
					}
					
				}
			}
		}else{
			logger.info("getAllSitePrefixes returned null");
		}		
		
		return airtelUSSDMenu;
	}
	public int populateMainMenuHashMap(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId,char perpaidYes){
		String method="populateMainMenuHashMap";
		logger.info("entering");
		int tempChildProcessId=currentProcessId;
		int tempCurrentProcessId=tempChildProcessId;
		logger.info("tempChildProcessId=="+tempChildProcessId+" ,tempCurrentProcessId"+tempCurrentProcessId);
		USSDInfo ussdInfoMainMenu=initializeUSSDInfo(-1,0,"mainMenu",null,airtelInfo.defaultURL+"&request_type=mainMenu");
		logger.info("menuOrder=="+airtelInfo.menuOrder);
		StringTokenizer st=new StringTokenizer(airtelInfo.menuOrder,",");
		int count=0;
		while(st.hasMoreTokens()){
			logger.info("count=="+count);
			String token=st.nextToken();
			logger.info("token=="+token);
			if (token!=null && token.length()>0) {
				tempChildProcessId = tempCurrentProcessId;
				logger.info("tempChildProcessId=="+tempChildProcessId);
				if (token.equalsIgnoreCase("MSearch") && airtelInfo.mSearchURL != null) {
					logger.info("token.equalsIgnoreCase(\"MSearch\") && info.mSearchURL != null");
					
					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
					populateChildInfo(ussdInfoMainMenu, "M-Search", count + 1,
							info.defaultURL+"&request_type=MSearch&ans=$answer$&processId="+tempCurrentProcessId+"&pageNo=0", tempChildProcessId);
					tempCurrentProcessId++;
					count++;
				}else if(token.equalsIgnoreCase("SubUnsub")) {
					logger.info("token.equalsIgnoreCase(\"SubUnsub\")");
					populateChildInfo(ussdInfoMainMenu, "Activate/Deactivate Services", count + 1,
							info.defaultURL+"&request_type=SubUnsub&processId="+tempCurrentProcessId+"&pageNo=0&request_value=monthlySub", tempChildProcessId);
					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
					USSDInfo ussdInfoSubUnsub=initializeUSSDInfo(parentProcessId,currentProcessId,"Activate/Deactivate Services","&request_type=SubUnsub&processId="+tempCurrentProcessId+"&pageNo=0&request_value=monthlySub",
							info.defaultURL);
					ussdMenu.put(""+tempCurrentProcessId,ussdInfoSubUnsub);
					tempCurrentProcessId++;
					count++;
				}else{
					if (token.equalsIgnoreCase("FreeZone")) {
					logger.info("token.equalsIgnoreCase(\"FreeZone\")");
					tempCurrentProcessId = populateMenuHashMapForFreeZone(
							ussdMenu, parentProcessId, tempCurrentProcessId,
							circleId,perpaidYes);
					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
				} else if (token.equalsIgnoreCase("WhatsHot")) {
					logger.info("token.equalsIgnoreCase(\"WhatsHot\")");
					tempCurrentProcessId = populateMenuHashMapForWhatsHot(
							ussdMenu, parentProcessId, tempCurrentProcessId,
							circleId);
					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
				} else if (token.equalsIgnoreCase("Browse")) {
					logger.info("token.equalsIgnoreCase(\"Browse\")");
					tempCurrentProcessId = populateMenuHashMapForBrowseHT(
							ussdMenu, parentProcessId, tempCurrentProcessId,
							circleId,perpaidYes);
					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
				} else if (token.equalsIgnoreCase("Cricket")) {
					logger.info("token.equalsIgnoreCase(\"Cricket\")");
					tempCurrentProcessId = populateMenuHashMapForCricket(
							ussdMenu, parentProcessId, tempCurrentProcessId,
							circleId);
					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
				} else if (token.equalsIgnoreCase("Copy")) {
					logger.info("token.equalsIgnoreCase(\"Copy\")");
					tempCurrentProcessId = populateMenuHashMapForCopy(
							ussdMenu, parentProcessId, tempCurrentProcessId,
							circleId);
					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
				}else if (token.equalsIgnoreCase("Manage")) {
					logger.info("token.equalsIgnoreCase(\"Manage\")");
					//"&request_type=copy&ans=$answer$"helpMsg
					tempCurrentProcessId = populateMenuHashMapForManage(
							ussdMenu, parentProcessId, tempCurrentProcessId,
							circleId);
					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
				} else if (token.equalsIgnoreCase("Help")) {
					logger.info("token.equalsIgnoreCase(\"Help\")");
					tempCurrentProcessId = populateMenuHashMapForHelp(ussdMenu,
							parentProcessId, tempCurrentProcessId, circleId);
					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
				}
					logger.info("tempChildProcessId=="+tempChildProcessId);
				USSDInfo tempInfo = (USSDInfo) ussdMenu.get(""
						+ tempChildProcessId);
				if(tempInfo!=null){
					logger.info("tempInfo!=");
					logger.info("tempInfo process name=="+tempInfo.processName);
				populateChildInfo(ussdInfoMainMenu, tempInfo.processName,
						count + 1, tempInfo.URL, tempChildProcessId);
				count++;
				}
			}
			}	
			if(ussdInfoMainMenu!=null){
				logger.info("populating ussdMenu Map with key=="+0+" and value==ussdInfoMainMenu with processId"+ussdInfoMainMenu.processId);
				ussdMenu.put("0", ussdInfoMainMenu);
				}	
		}
		return tempCurrentProcessId;
	}
	public int populateMenuHashMapForWhatsHot(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		return super.populateMenuHashMapForWhatsHot(ussdMenu, parentProcessId, currentProcessId, circleId);
	}
	public int populateMenuHashMapForMSearch(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
//		int tempCurrentProcessId=-1;
//		USSDInfo ussdInfoMSearch=initializeUSSDInfo(parentProcessId,currentProcessId,"M-Search",null);
//		ussdInfoWhatsHot;
//		ussdMenu.put(""+currentProcessId, ussdInfoWhatsHot);
//		currentProcessId++;
		return super.populateMenuHashMapForMSearch(ussdMenu, parentProcessId, currentProcessId, circleId);
	}
	public int populateMenuHashMapForBrowseHT(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId,char perpaidYes){
		return super.populateMenuHashMapForBrowseHT(ussdMenu, parentProcessId, currentProcessId, circleId,perpaidYes);
	}
	public int populateMenuHashMapForFreeZone(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId,char perpaidYes){
		return super.populateMenuHashMapForFreeZone(ussdMenu, parentProcessId, currentProcessId, circleId,perpaidYes);
	}
	public int populateMenuHashMapForCricket(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		return super.populateMenuHashMapForCricket(ussdMenu, parentProcessId, currentProcessId, circleId);
	}
	public int populateMenuHashMapForCopy(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		return super.populateMenuHashMapForCopy(ussdMenu, parentProcessId, currentProcessId, circleId);
	}
	
	public int populateMenuHashMapForManage(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		return super.populateMenuHashMapForManage(ussdMenu, parentProcessId, currentProcessId, circleId);
	}
	public int populateMenuHashMapForHelp(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		return super.populateMenuHashMapForHelp(ussdMenu, parentProcessId, currentProcessId, circleId);
	}
	public USSDInfo initializeUSSDInfo(int parentProcessId,int currentProcessId,String processName,String URL,String defaultURL){
		return super.initializeUSSDInfo(parentProcessId, currentProcessId, processName, URL, defaultURL);
	}
	public  void populateChildInfo(USSDInfo ussdInfo,String childName,int count,String childUrl,int currentChildProcessId){
		 super.populateChildInfo(ussdInfo, childName, count, childUrl, currentChildProcessId);
	}
}
