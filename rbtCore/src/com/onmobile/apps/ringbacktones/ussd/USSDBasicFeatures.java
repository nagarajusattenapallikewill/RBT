package com.onmobile.apps.ringbacktones.ussd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Categories;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.ussd.vodafoneRomania.PropertyReader;

public class USSDBasicFeatures implements FeatureInterface{
	/*author@Abhinav Anand
	 */
	protected PropertyReader pr=new PropertyReader();

	private static Logger logger = Logger.getLogger(USSDBasicFeatures.class);
	public   HashMap USSDMenu =new HashMap();
	private  int currentParentProcessId=0;
	private  int currentProcessId=1;
	public   USSDConstants info=null;
	private  static RBTDBManager rbtDBManager =null; 
	private static String m_class="USSDBasicFeatures";
	public USSDConstants getInfo() {
		return info;
	}
	public void setInfo() {
		info=new USSDConstants();
	}
	public HashMap getUSSDMenu() {
		return USSDMenu;
	}
	public void setUSSDMenu(HashMap menu) {
		USSDMenu = menu;
	}
	public void init(){
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

				if(tempPrefix!=null && tempPrefix.getCircleID() !=null && tempPrefix.getCircleID().length()>0){
					logger.info("site is valid");
					logger.info("site circle ID=="+tempPrefix.getCircleID());
					HashMap circleBasedMap=new HashMap();
					logger.info("going to populate mainMenuHashMap");
					populateMainMenuHashMap(circleBasedMap,currentParentProcessId,currentProcessId,tempPrefix.getCircleID().trim(),'b');
					if(circleBasedMap!=null && !circleBasedMap.isEmpty() && circleBasedMap.size()>0){
						logger.info("successfully populated mainMenuHashMap");
						USSDMenu.put(tempPrefix.getCircleID().trim(), circleBasedMap);
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

		return USSDMenu;
	}

	public int populateMainMenuHashMap(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId,char prepaidYes){
		String method="populateMainMenuHashMap";
		logger.info("entering");
		int tempChildProcessId=currentProcessId;
		int tempCurrentProcessId=tempChildProcessId;
		logger.info("tempChildProcessId=="+tempChildProcessId+" ,tempCurrentProcessId"+tempCurrentProcessId);
		USSDInfo ussdInfoMainMenu=initializeUSSDInfo(-1,0,"mainMenu",null,info.defaultURL+"&request_type=mainMenu");
		logger.info("menuOrder=="+info.menuOrder);
		StringTokenizer st=new StringTokenizer(info.menuOrder,",");
		int count=0;
		while(st.hasMoreTokens()){
			logger.info("count=="+count);
			String token=st.nextToken();
			if (token!=null && token.length()>0) {
				tempChildProcessId = tempCurrentProcessId;
				logger.info("tempChildProcessId=="+tempChildProcessId);
				if (token.equalsIgnoreCase("MSearch") && info.mSearchURL != null) {
					logger.info("token.equalsIgnoreCase(\"MSearch\") && info.mSearchURL != null");

					logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
					populateChildInfo(ussdInfoMainMenu, "M-Search", count + 1,
							info.defaultURL+"&request_type=MSearch&ans=$answer$&processId="+tempCurrentProcessId+"&pageNo=0", tempChildProcessId);
					tempCurrentProcessId++;
					count++;
				} else if (token.equalsIgnoreCase("SubUnsub")) {
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
								circleId,prepaidYes);
						logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
					}else if (token.equalsIgnoreCase("MyFavorite")) {
						logger.info("token.equalsIgnoreCase(\"MyFavorite\")");
						tempCurrentProcessId = populateMenuHashMapForMyFavorite(
								ussdMenu, parentProcessId, tempCurrentProcessId,
								circleId);
						logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
					}  else if (token.equalsIgnoreCase("WhatsHot")) {
						logger.info("token.equalsIgnoreCase(\"WhatsHot\")");
						tempCurrentProcessId = populateMenuHashMapForWhatsHot(
								ussdMenu, parentProcessId, tempCurrentProcessId,
								circleId);
						logger.info("tempCurrentProcessId=="+tempCurrentProcessId);
					} else if (token.equalsIgnoreCase("Browse")) {
						logger.info("token.equalsIgnoreCase(\"Browse\")");
						tempCurrentProcessId = populateMenuHashMapForBrowseHT(
								ussdMenu, parentProcessId, tempCurrentProcessId,
								circleId,prepaidYes);
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
	public String getBackNMainMenuOptionInResponse(String backNMainMenuStr,boolean isBackNMainMenuallowedinResponse){
		String strMainMenuBackOption="";
		if(isBackNMainMenuallowedinResponse){
			strMainMenuBackOption=backNMainMenuStr;
		}
		return strMainMenuBackOption;
	}
	public int populateMenuHashMapForWhatsHot(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		String method="populateMenuHashMapForWhatsHot";
		logger.info("entering with currentProcessId=="+currentProcessId);
		USSDInfo ussdInfoWhatsHot=initializeUSSDInfo(parentProcessId,currentProcessId,"what's Hot","&processId="+currentProcessId+"&pageNo=0"+"&request_type=whatshot",info.defaultURL);
		ussdInfoWhatsHot.responseString=info.whatsHotMsg;
		logger.info("ussdInfoWhatsHot.responseString=="+ussdInfoWhatsHot.responseString);
		String tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0",info.toGiveBackNMainMenuOptionInResponse);
		ussdInfoWhatsHot.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar;
		logger.info("ussdInfoWhatsHot.dynamicURLResponseString=="+ussdInfoWhatsHot.dynamicURLResponseString);
		logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoWhatsHot  with processId"+ussdInfoWhatsHot.processId);
		ussdMenu.put(""+currentProcessId, ussdInfoWhatsHot);
		currentProcessId++;
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public int populateMenuHashMapForMSearch(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		String method="populateMenuHashMapForMSearch";
		logger.info("populateMenuHashMapForMSearch");
		logger.info("entering with currentProcessId=="+currentProcessId);
//		int tempCurrentProcessId=-1;
//		USSDInfo ussdInfoMSearch=initializeUSSDInfo(parentProcessId,currentProcessId,"M-Search",null);
//		ussdInfoWhatsHot;
//		ussdMenu.put(""+currentProcessId, ussdInfoWhatsHot);
//		currentProcessId++;
		logger.info("exiting with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public int populateMenuHashMapForBrowseHT(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId,char prepaidYes){
		String method="populateMenuHashMapForBrowseHT";
		logger.info("populateMenuHashMapForMSearch");
		logger.info("entering with currentProcessId=="+currentProcessId);
		logger.info("entering with info.ussdParentCat=="+info.ussdParentCat);
		logger.info("entering with info.ussdCatsNotInBrowse=="+info.ussdCatsNotInBrowse);
		int catId=-1;
		if(info.ussdParentCat!=null && info.ussdParentCat.length()>0){
			try {
				catId=Integer.parseInt(info.ussdParentCat);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return currentProcessId;
			}
		}else{
			return currentProcessId;
		}

		Categories[] baseCats=rbtDBManager.getSubCategories(catId, circleId, prepaidYes);
		if(baseCats!=null && baseCats.length>0){
			logger.info("baseCats[]!=null && baseCats[].length>0");
		}
		StringTokenizer st=new StringTokenizer(info.ussdCatsNotInBrowse,",");
		ArrayList ussdCatNotinBrowseMenu=new ArrayList(); 
		while(st.hasMoreElements()){
			String tempCat=st.nextToken();
			if(tempCat!=null && tempCat.length()>0){
				ussdCatNotinBrowseMenu.add(tempCat);
			}
		}
		if(baseCats!=null && baseCats.length>0){
			logger.info("baseCats[]!=null && baseCats[].length>0");
			USSDInfo ussdInfoBrowse=initializeUSSDInfo(parentProcessId,currentProcessId,info.browseMenuStr,"&processId="+ currentProcessId+ "&pageNo=0&request_type=catSearch",info.defaultURL);
			int browseCurrentProcessId=currentProcessId;
			currentProcessId++;
			int parentCountTemp=1;
			for(int count=0;count<baseCats.length;count++){
				logger.info("catId=="+baseCats[count].id());
				if(!ussdCatNotinBrowseMenu.contains(""+baseCats[count].id())){
					logger.info("catId=="+baseCats[count].id()+" is in browse menu");
					String catName=baseCats[count].name();
					if(catName==null || catName.length()==0){
						catName="parentCat";
					}else{
						catName=catName.trim();
					}
					logger.info("catname=="+baseCats[count].name());
					USSDInfo ussdInfoParentCat=initializeUSSDInfo(browseCurrentProcessId,currentProcessId,catName,"&processId="+ currentProcessId+ "&pageNo=0&request_type=childCatSearch",info.defaultURL);
					int currentParentProcessId=currentProcessId;
					currentProcessId++;
					populateChildInfo(ussdInfoBrowse,catName,parentCountTemp,ussdInfoParentCat.URL,currentParentProcessId);
					parentCountTemp++;
					Categories[] childCats=rbtDBManager.getSubCategories(baseCats[count].id(), circleId, prepaidYes);
					if(childCats!=null && childCats.length>0){
						logger.info("childCat[]!=null && childCat[].length>0");
						int childCountTemp=1;
						for(int childCount=0;childCount<childCats.length;childCount++){
							logger.info("childCatId=="+childCats[childCount].id());
							if (!ussdCatNotinBrowseMenu.contains(""+childCats[childCount].id())) {
								logger.info("childCatId=="+childCats[childCount].id()+" is in browse menu");
								String childCatName = childCats[childCount].name();
								if (childCatName == null || childCatName.length() == 0) {
									childCatName = "childCat";
								} else {
									childCatName = childCatName.trim();
								}
								logger.info("childCatname=="+childCats[childCount].name());
								USSDInfo ussdInfoChildCat = initializeUSSDInfo(currentParentProcessId,currentProcessId,childCatName,"&processId="+ currentProcessId+ "&pageNo=0&request_type=songSearch&catId="+childCats[childCount].id()+"&giftAllowed="+info.giftAllowedInBrowse+"&specialCaller="+info.specialCallerAllowedInBrowse+"&preListen="+info.preListenAllowedInBrowse,info.defaultURL);
								populateChildInfo(ussdInfoParentCat,childCatName,childCountTemp, ussdInfoChildCat.URL,currentProcessId);
								childCountTemp++;
								ussdInfoChildCat.catId = ""+ childCats[childCount].id();
								logger.info("ussdInfoChildCat.catId=="+ussdInfoChildCat.catId);
								/*if(ussdInfoChildCat.dynamicURLResponseString==null || ussdInfoChildCat.dynamicURLResponseString.equalsIgnoreCase("")){
								ussdInfoChildCat.dynamicURLResponseString=info.mainMenuURL+"`"+info.seperatorChar+"*1`"+ussdInfoParentCat.URL;
								logger.info("ussdInfoChildCat.dynamicURLResponseString=="+ussdInfoChildCat.dynamicURLResponseString);
							}else{
								ussdInfoChildCat.dynamicURLResponseString=ussdInfoChildCat.dynamicURLResponseString+info.mainMenuURL+"`"+info.seperatorChar+"*1`"+ussdInfoParentCat.URL;
								logger.info("ussdInfoChildCat.dynamicURLResponseString=="+ussdInfoChildCat.dynamicURLResponseString);
							}*/
								logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoChildCat with processId"+ussdInfoChildCat.processId);
								ussdMenu.put("" + currentProcessId,ussdInfoChildCat);
								logger.info("currentProcessId=="+currentProcessId);
								currentProcessId++;
							}
						}
					}
					if(ussdInfoParentCat!=null){
						String tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+ussdInfoBrowse.URL,info.toGiveBackNMainMenuOptionInResponse);
						if(ussdInfoParentCat.dynamicURLResponseString==null || ussdInfoParentCat.dynamicURLResponseString.equalsIgnoreCase("")){
							ussdInfoParentCat.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar;
						}else{
							ussdInfoParentCat.dynamicURLResponseString=ussdInfoParentCat.dynamicURLResponseString+tempStrMainMenuBackOption+info.endURLChar;
						}
						logger.info("ussdInfoParentCat.dynamicURLResponseString=="+ussdInfoParentCat.dynamicURLResponseString);
					}
					logger.info("populating ussdMenu Map with key=="+currentParentProcessId+" and value==ussdInfoParentCat with processId"+ussdInfoParentCat.processId);
					ussdMenu.put(""+currentParentProcessId, ussdInfoParentCat);
				}
			}
			if(ussdInfoBrowse!=null){
				String tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0",info.toGiveBackNMainMenuOptionInResponse);
				if(ussdInfoBrowse.dynamicURLResponseString==null || ussdInfoBrowse.dynamicURLResponseString.equalsIgnoreCase("")){
					ussdInfoBrowse.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar;
				}else{
					ussdInfoBrowse.dynamicURLResponseString=ussdInfoBrowse.dynamicURLResponseString+tempStrMainMenuBackOption+info.endURLChar;
				}
				logger.info("ussdInfoBrowse.dynamicURLResponseString=="+ussdInfoBrowse.dynamicURLResponseString);
			}
			logger.info("populating ussdMenu Map with key=="+browseCurrentProcessId+" and value==ussdInfoBrowse with processId"+ussdInfoBrowse.processId);
			ussdMenu.put(""+browseCurrentProcessId, ussdInfoBrowse);
		}
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public int populateMenuHashMapForFreeZone(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId,char prepaidYes){
		String method="populateMenuHashMapForFreeZone";
		logger.info("populateMenuHashMapForFreeZone");
		logger.info("entering with currentProcessId=="+currentProcessId);
		logger.info("ussdCatsFreeZone=="+info.ussdCatsFreeZone);
		String catId=info.ussdCatsFreeZone;
		if (catId!=null && catId.length()>0) {

			String childCatName = "Free Zone";
			USSDInfo ussdInfoChildCat = initializeUSSDInfo(
					parentProcessId, currentProcessId, childCatName,
					"&processId=" + currentProcessId+ "&pageNo=0&request_type=songSearch&giftAllowed=false&specialCaller=true&preListen=false&catId="+catId+"&status=free",info.defaultURL);
			logger.info("ussdInfoChildCat.processName=="+ussdInfoChildCat.processName);
			ussdInfoChildCat.catId = catId;
			logger.info("ussdInfoChildCat.catId=="+ussdInfoChildCat.catId);
			//ussdInfoChildCat.dynamicURLResponseString=info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0";
			logger.info("ussdInfoChildCat.dynamicURLResponseString=="+ussdInfoChildCat.dynamicURLResponseString);
			logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoChildCat with processId"+ussdInfoChildCat.processId);
			ussdMenu.put("" + currentProcessId, ussdInfoChildCat);
			currentProcessId++;
		}	
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}

	//added for GiftInbox
	public int populateMenuHashMapForGiftInbox(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId,char prepaidYes){
		String method="populateMenuHashMapForGiftInbox";
		logger.info("populateMenuHashMapForGiftInbox");
		logger.info("entering with currentProcessId=="+currentProcessId);
		String childCatName =info.giftInbox;
		USSDInfo ussdInfoChildCat = initializeUSSDInfo(
				parentProcessId, currentProcessId, childCatName,
				"&processId=" + currentProcessId+ "&pageNo=0&request_type=giftinbox",info.defaultURL);
		
		logger.info("ussdInfoChildCat.dynamicURLResponseString=="+ussdInfoChildCat.dynamicURLResponseString);
		logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoChildCat with processId"+ussdInfoChildCat.processId);
		ussdMenu.put("" + currentProcessId, ussdInfoChildCat);
		currentProcessId++;
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public int populateMenuHashMapForCricket(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		String method="populateMenuHashMapForCricket";
		logger.info("populateMenuHashMapForCricket");
		logger.info("entering with currentProcessId=="+currentProcessId);
		USSDInfo ussdInfoCricket=initializeUSSDInfo(parentProcessId,currentProcessId,"Hello Tunes Cricket Pack","&processId="+currentProcessId+"&pageNo=0"+"&request_type=cricket",info.defaultURL);
		ussdInfoCricket.responseString=info.cricketMsg;
		logger.info("ussdInfoCricket.responseString=="+ussdInfoCricket.responseString);
		String tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0",info.toGiveBackNMainMenuOptionInResponse);
		ussdInfoCricket.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar;
		logger.info("ussdInfoCricket.dynamicURLResponseString=="+ussdInfoCricket.dynamicURLResponseString);
		logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoCricket with processId"+ussdInfoCricket.processId);
		ussdMenu.put(""+currentProcessId, ussdInfoCricket);
		currentProcessId++;
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public int populateMenuHashMapForCopy(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		String method="populateMenuHashMapForCopy";
		logger.info("populateMenuHashMapForCopy");
		logger.info("entering with currentProcessId=="+currentProcessId);
		USSDInfo ussdInfoCopy=initializeUSSDInfo(parentProcessId,currentProcessId,"Copy",null,info.defaultURL+info.copyURL+"&request_value=copyNumber");
		ussdInfoCopy.responseString=info.copyMsg;
		logger.info("ussdInfoCopy.responseString=="+ussdInfoCopy.responseString);
		int count=1;
		String tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0",info.toGiveBackNMainMenuOptionInResponse);
		ussdInfoCopy.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar;
		logger.info("ussdInfoCopy.dynamicURLResponseString=="+ussdInfoCopy.dynamicURLResponseString);
		int currentCopyParentProcessId=currentProcessId;
		currentProcessId++;
		USSDInfo ussdInfoCopyReconfirmation=initializeUSSDInfo(currentCopyParentProcessId,currentProcessId,"CopyReconfirmation",null,info.defaultURL+info.copyURL+"&request_value=copyReconfirmation");
		populateChildInfo(ussdInfoCopy,null,-1,null,currentProcessId);
//		ussdInfoCopy.URL=ussdInfoCopy.URL+"&childProcessId="+currentProcessId;
		logger.info("populating ussdMenu Map with key=="+currentCopyParentProcessId+" and value==ussdInfoCopy with processId"+ussdInfoCopy.processId);

		ussdMenu.put(""+currentCopyParentProcessId,ussdInfoCopy);
		currentCopyParentProcessId=currentProcessId;
		currentProcessId++;
		ussdInfoCopyReconfirmation.responseString=info.copyReconfirmMsg;
		tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+ussdInfoCopy.URL,info.toGiveBackNMainMenuOptionInResponse);
		ussdInfoCopyReconfirmation.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar;
		logger.info("ussdInfoCopyReconfirmation.responseString=="+ussdInfoCopyReconfirmation.responseString);
		logger.info("ussdInfoCopyReconfirmation.dynamicURLResponseString=="+ussdInfoCopyReconfirmation.dynamicURLResponseString);
		if(info.specialCallerForCopyAllowed){
			USSDInfo ussdInfoCopyCallerNoSelection=initializeUSSDInfo(currentCopyParentProcessId,currentProcessId,"CopyCallerNoSelection",null,info.defaultURL+info.copyURL+"&request_value=copyCallerNoSelection");
			populateChildInfo(ussdInfoCopyReconfirmation,null,-1,null,currentProcessId);
			ussdInfoCopyCallerNoSelection.responseString=info.seperatorChar+"1.Select for all callers"+info.newLineCharString+info.seperatorChar+"2.Select for one caller"+info.newLineCharString+info.enterChoiceStr;
			tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+ussdInfoCopyReconfirmation.URL,info.toGiveBackNMainMenuOptionInResponse);
			ussdInfoCopyCallerNoSelection.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar+"`"+info.seperatorChar+"1`"+info.defaultURL+info.copyURL+"&processId="+ussdInfoCopyReconfirmation.processId+"&pageNo=0&request_value=copySel&callerNo=specialCaller&parentProcessId="+currentProcessId+info.endURLChar+"`"+info.seperatorChar+"2`"+info.defaultURL+info.copyURL+"&processId="+ussdInfoCopyReconfirmation.processId+"&pageNo=0&request_value=copySel&callerNo=specialCaller";
			logger.info("ussdInfoCopyCallerNoSelection.responseString=="+ussdInfoCopyCallerNoSelection.responseString);
			logger.info("ussdInfoCopyCallerNoSelection.dynamicURLResponseString=="+ussdInfoCopyCallerNoSelection.dynamicURLResponseString);

			logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoCopyCallerNoSelection with processId"+ussdInfoCopyCallerNoSelection.processId);
			ussdMenu.put(""+currentProcessId,ussdInfoCopyCallerNoSelection);
			currentProcessId++;
			ussdInfoCopyReconfirmation.dynamicURLResponseString=ussdInfoCopyReconfirmation.dynamicURLResponseString+"`"+info.seperatorChar+"1`"+ussdInfoCopyCallerNoSelection.URL+info.endURLChar;
			logger.info("populating ussdMenu Map with key=="+currentCopyParentProcessId+" and value==ussdInfoCopyReconfirmation with processId"+ussdInfoCopyReconfirmation.processId);
			logger.info("populating ussdMenu Map with key=="+currentCopyParentProcessId+" and value==ussdInfoCopyReconfirmation with processId"+ussdInfoCopyReconfirmation.processId);
			ussdMenu.put(""+currentCopyParentProcessId,ussdInfoCopyReconfirmation);
		}else{
			ussdInfoCopyReconfirmation.dynamicURLResponseString=ussdInfoCopyReconfirmation.dynamicURLResponseString+"`"+info.seperatorChar+"1`"+info.defaultURL+"&request_type=copy&processId="+ussdInfoCopyReconfirmation.processId+"&pageNo=0&request_value=copySel&callerNo=all"+info.endURLChar;
			logger.info("ussdInfoCopyReconfirmation.dynamicURLResponseString=="+ussdInfoCopyReconfirmation.dynamicURLResponseString);

			logger.info("populating ussdMenu Map with key=="+currentCopyParentProcessId+" and value==ussdInfoCopyReconfirmation with processId"+ussdInfoCopyReconfirmation.processId);
			ussdMenu.put(""+currentCopyParentProcessId,ussdInfoCopyReconfirmation);
		}
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public int populateMenuHashMapForManage(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		String method="populateMenuHashMapForManage";
		logger.info("populateMenuHashMapForManage");
		logger.info("entering with currentProcessId=="+currentProcessId);
		USSDInfo ussdInfoManage=initializeUSSDInfo(parentProcessId,currentProcessId,"Manage",null,info.defaultURL+"&processId="+currentProcessId+"&pageNo=0&request_type=manage");
//		ussdInfoManage.dynamicURLResponseString=info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0";
		logger.info("ussdInfoManage.dynamicURLResponseString=="+ussdInfoManage.dynamicURLResponseString);
		logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoManage with processId"+ussdInfoManage.processId);
		ussdMenu.put(""+currentProcessId, ussdInfoManage); 
		currentProcessId++;
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public int populateMenuHashMapForMyFavorite(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		String method="populateMenuHashMapForMyFavorite";
		logger.info("populateMenuHashMapForMyFavorite");
		logger.info("entering with currentProcessId=="+currentProcessId);
		USSDInfo ussdInfoMyFavorite=initializeUSSDInfo(parentProcessId,currentProcessId,pr.getPropertyValue("mainmenu.str"),null,info.defaultURL+"&processId="+currentProcessId+"&pageNo=0&request_type=myfavorite");
//		populateChildInfo(ussdInfoMyFavorite,null,-1,null,currentProcessId);
		ussdInfoMyFavorite.responseString=info.seperatorChar+"1.Tonurile mele de asteptare"+info.newLineCharString+info.seperatorChar+"2.Schimba tonul de asteptare"+info.newLineCharString+info.enterChoiceStr;
		String tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0",info.toGiveBackNMainMenuOptionInResponse);
		ussdInfoMyFavorite.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar+"`"+info.seperatorChar+"1`"+info.defaultURL+"&processId="+currentProcessId+"&pageNo=0&request_type=myfavorite&request_value=viewfavorite"+info.endURLChar+"`"+info.seperatorChar+"2`"+info.defaultURL+"&processId="+currentProcessId+"&pageNo=0&request_type=myfavorite&request_value=addfavorite"+info.endURLChar;
		logger.info("ussdInfoCopyCallerNoSelection.responseString=="+ussdInfoMyFavorite.responseString);
		logger.info("ussdInfoCopyCallerNoSelection.dynamicURLResponseString=="+ussdInfoMyFavorite.dynamicURLResponseString);

		logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoCopyCallerNoSelection with processId"+ussdInfoMyFavorite.processId);
		ussdMenu.put(""+currentProcessId,ussdInfoMyFavorite);
		currentProcessId++;
		//		ussdInfoManage.dynamicURLResponseString=info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0";
		logger.info("ussdInfoMyFavorite.dynamicURLResponseString=="+ussdInfoMyFavorite.dynamicURLResponseString);
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public int populateMenuHashMapForTariff(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		String method="populateMenuHashMapForTariff";
		logger.info("populateMenuHashMapForTariff");
		logger.info("entering with currentProcessId=="+currentProcessId);
		USSDInfo ussdInfoTariff=initializeUSSDInfo(parentProcessId,currentProcessId,pr.getPropertyValue("tariff.str"),"&processId="+currentProcessId+"&pageNo=0"+"&request_type=tariff",info.defaultURL);
		ussdInfoTariff.responseString=info.tariffMsg;
		logger.info("ussdInfoTariff.responseString=="+ussdInfoTariff.responseString);
		String tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0",info.toGiveBackNMainMenuOptionInResponse);
		ussdInfoTariff.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar;
		logger.info("ussdInfoTariff.dynamicURLResponseString=="+ussdInfoTariff.dynamicURLResponseString);
		logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoCricket with processId"+ussdInfoTariff.processId);
		ussdMenu.put(""+currentProcessId, ussdInfoTariff);
		currentProcessId++;
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public int populateMenuHashMapForHelp(HashMap ussdMenu,int parentProcessId, int currentProcessId,String circleId){
		String method="populateMenuHashMapForHelp";
		logger.info("populateMenuHashMapForHelp");
		logger.info("entering with currentProcessId=="+currentProcessId);
		USSDInfo ussdInfoHelp=initializeUSSDInfo(parentProcessId,currentProcessId,"Help",null,info.defaultURL+"&processId="+currentProcessId+"&pageNo=0&request_type=help");
		ussdInfoHelp.responseString=info.helpMsg+info.enterChoiceStr;
		String tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+info.defaultURL+"&processId=0&pageNo=0",info.toGiveBackNMainMenuOptionInResponse);
		ussdInfoHelp.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar;
		logger.info("ussdInfoHelp.responseString=="+ussdInfoHelp.responseString);
		logger.info("ussdInfoHelp.dynamicURLResponseString=="+ussdInfoHelp.dynamicURLResponseString);
		logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoHelp with processId"+ussdInfoHelp.processId);
		ussdMenu.put(""+currentProcessId, ussdInfoHelp);
		int currentParentProcessId=currentProcessId;
		currentProcessId++;
		USSDInfo ussdInfoHelloTunesHelp=initializeUSSDInfo(currentParentProcessId,currentProcessId,"Hello Tunes Help",null,info.defaultURL+"&processId="+currentProcessId+"&pageNo=0&request_type=helpHelloTunes");
		ussdInfoHelloTunesHelp.responseString=info.helloTunesHelpMsg;
		tempStrMainMenuBackOption=getBackNMainMenuOptionInResponse(info.mainMenuURL+"`"+info.seperatorChar+"*1`"+ussdInfoHelp.URL,info.toGiveBackNMainMenuOptionInResponse);
		ussdInfoHelloTunesHelp.dynamicURLResponseString=tempStrMainMenuBackOption+info.endURLChar;
		logger.info("ussdInfoHelloTunesHelp.responseString=="+ussdInfoHelloTunesHelp.responseString);
		logger.info("ussdInfoHelloTunesHelp.dynamicURLResponseString=="+ussdInfoHelloTunesHelp.dynamicURLResponseString);
		logger.info("populating ussdMenu Map with key=="+currentProcessId+" and value==ussdInfoHelloTunesHelp with processId"+ussdInfoHelloTunesHelp.processId);
		ussdMenu.put(""+currentProcessId, ussdInfoHelloTunesHelp);
		populateChildInfo(ussdInfoHelp,null,1,ussdInfoHelloTunesHelp.URL,currentProcessId);
		currentProcessId++;
		logger.info("returning with currentProcessId=="+currentProcessId);
		return currentProcessId;
	}
	public  USSDInfo initializeUSSDInfo(int parentProcessId,int currentProcessId,String processName,String URL,
			String defaultURL){
		String method="initializeUSSDInfo";
		logger.info("entering");
		USSDInfo ussdInfo=new USSDInfo();
		ussdInfo.processId=""+currentProcessId;
		logger.info("ussdInfo.processId=="+ussdInfo.processId);
		if(URL==null){
			logger.info("url==null");
			ussdInfo.URL=defaultURL+"&processId="+currentProcessId+"&pageNo=0"+info.endURLChar;
			logger.info("ussdInfo.URL=="+ussdInfo.URL);
		}else{
			logger.info("url!=null");
			ussdInfo.URL=defaultURL+URL+info.endURLChar;
			logger.info("ussdInfo.URL=="+ussdInfo.URL);
		}
		ussdInfo.responseString="";
		ussdInfo.dynamicURLResponseString="";
		ussdInfo.processName=processName;
		logger.info("ussdInfo.processName=="+ussdInfo.processName);
		ussdInfo.childProcessId="";
		ussdInfo.parentProcessId=""+parentProcessId;
		logger.info("ussdInfo.parentProcessId=="+ussdInfo.parentProcessId);
		logger.info("exiting");
		return ussdInfo;
	}
	public  void populateChildInfo(USSDInfo ussdInfo,String childName,int count,String childUrl,int currentChildProcessId){
		String method="populateChildInfo";
		logger.info("entering");
		if (currentChildProcessId!=-1) {
			logger.info("currentChildProcessId!=-1");
			if (ussdInfo.childProcessId == null || ussdInfo.childProcessId.equalsIgnoreCase("")) {
				logger.info("ussdInfo.childProcessId == null || ussdInfo.childProcessId.equalsIgnoreCase(\"\")");
				ussdInfo.childProcessId = "" + currentChildProcessId;
				logger.info("ussdInfo.childProcessId== "+ussdInfo.childProcessId );
			} else {
				logger.info("ussdInfo.childProcessId != null && !ussdInfo.childProcessId.equalsIgnoreCase(\"\")");
				ussdInfo.childProcessId = ussdInfo.childProcessId + ","+ currentChildProcessId;
				logger.info("ussdInfo.childProcessId== "+ussdInfo.childProcessId );
			}
		}		
		if (childName!=null) {
			logger.info("childName!=null");
			if (ussdInfo.responseString == null || ussdInfo.responseString.equalsIgnoreCase("")) {
				if (count>0) {
					logger.info("count>0");
					ussdInfo.responseString = info.seperatorChar+(count) + "." + childName;
					logger.info("ussdInfo.responseString== "+ussdInfo.responseString );
				}else{
					logger.info("count=<0");
					ussdInfo.responseString =childName;
					logger.info("ussdInfo.responseString== "+ussdInfo.responseString );
				}				
			} else {
				if (count>0) {
					logger.info("count>0");
					ussdInfo.responseString = ussdInfo.responseString +info.newLineCharString+ info.seperatorChar+(count)+ "." + childName;
					logger.info("ussdInfo.responseString== "+ussdInfo.responseString );
				}else{
					logger.info("count>0");
					ussdInfo.responseString = ussdInfo.responseString + childName;
					logger.info("ussdInfo.responseString== "+ussdInfo.responseString );
				}				
			}
		}		
		if (childUrl != null && count>0) {
			logger.info("childUrl != null && count>0");
			if ( ussdInfo.dynamicURLResponseString == null || ussdInfo.dynamicURLResponseString.equalsIgnoreCase("")) {
				logger.info(" ussdInfo.dynamicURLResponseString == null || ussdInfo.dynamicURLResponseString.equalsIgnoreCase(\"\")" );
				ussdInfo.dynamicURLResponseString = "`" +info.seperatorChar+ (count) + "`"+ childUrl+info.endURLChar;
				logger.info("ussdInfo.dynamicURLResponseString== "+ussdInfo.dynamicURLResponseString );
			} else {
				logger.info(" ussdInfo.dynamicURLResponseString != null && !ussdInfo.dynamicURLResponseString.equalsIgnoreCase(\"\")" );
				ussdInfo.dynamicURLResponseString = ussdInfo.dynamicURLResponseString+ "`" +info.seperatorChar+ (count) + "`" + childUrl+info.endURLChar;
				logger.info("ussdInfo.dynamicURLResponseString== "+ussdInfo.dynamicURLResponseString );
			}
		}		
	}
}
