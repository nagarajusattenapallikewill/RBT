package com.onmobile.apps.ringbacktones.ussd.airtel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.ussd.common.ClipBean;
import com.onmobile.apps.ringbacktones.ussd.common.StringUtils;
import com.onmobile.apps.ringbacktones.ussd.common.USSDCacheManager;
import com.onmobile.apps.ringbacktones.ussd.common.USSDConfigParameters;
import com.onmobile.apps.ringbacktones.ussd.common.USSDNode;

public class AirtelUSSDShowSongList {
	private static Logger basicLogger = Logger.getLogger(AirtelUSSDMainMenu.class);
	private List<USSDNode> menu = new ArrayList<USSDNode>();
	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;
	private String categoryType=null;

	public AirtelUSSDShowSongList(Map<String, String> input, HttpServletResponse response,String categoryType) {
		this.input = input;
		this.response = response;
		this.categoryType=categoryType;
	}

	public void process() throws IOException {
		System.out.println(" cattpe "+categoryType); 
		String catId=getCategoryId(categoryType);
		if(categoryType==null){
			basicLogger.error(" category id is null ");
			return;
		}
		ArrayList<ClipBean> songList=new USSDCacheManager().getSongList(catId);

		//for testing
		/*songList=new ArrayList<ClipBean>();
		String[] name={"aaaaa","bbbb","cccc","dddd"};
		int[] id={1,2,3,4};
		int[] catid={11,12,13,14};
		int[] status={0,0,0,0};

		for(int a=0;a<name.length;a++){
			ClipBean c=new ClipBean();
			c.setSongName(name[a]);
			c.setCatId(catid[a]);
			c.setClipId(id[a]);
			c.setStatus(status[a]);
			songList.add(c);
		}*/

		if(songList==null){
			basicLogger.error(" SongList is null for category ID = "+catId);
			System.out.println("song lis is null for catid "+catId);
			new AirtelUSSDMessageDisplay(input,response,"",USSDConfigParameters.getInstance().getParameter("NOSONG_AVAILABLE"),input.get("action"),"mainmenu",null).displayMessage();
			return;
		}else
			basicLogger.info(" songList size is "+songList.size());
		for(int i=0;i<songList.size();i++){
			ClipBean clip=songList.get(i);
			if(clip!=null){
				menu.add(new USSDNode(i, 0, clip.getSongName(), 
						USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=isuda"+"&clipname="+clip.getSongName()+"&clipid="+clip.getClipId()+"&status="+clip.getStatus()+"&catid="+clip.getCatId()+"&selType="+categoryType));
			}
		}

		/*response.setContentType(AirtelUSSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		response.getWriter().println(getResponse());*/
		new USSDResponse().sendResponse(response, getResponse());
	}

		public String getResponse() {
		String nextNodeId = input.get("next");
		boolean backOptionRequired=true;
		String backUrl=null;
		int startIndex = 0;
		if(null != nextNodeId && nextNodeId.length() > 0) {
			try {
				startIndex = Integer.parseInt(nextNodeId);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}

		if(startIndex<0)
			startIndex=0;

		
        
		if(startIndex==0){
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=mainmenu";
		}else{
			backUrl=USSDConfigParameters.getInstance().getUSSDHostURL() + "&action="+input.get("action");
		}


		List<USSDNode> output = new ArrayList<USSDNode>();
		for(int i=startIndex; i<menu.size(); i++) {
			output.add(menu.get(i));
		}
		String welcomeMessage = "";

		if(categoryType.equalsIgnoreCase("popularSong")){
			welcomeMessage =USSDConfigParameters.getInstance().getParameter("POPULARSONG_HEADING");
		}
		else if(categoryType.equalsIgnoreCase("top10")){
			welcomeMessage =USSDConfigParameters.getInstance().getParameter("TOP10_HEADING");
		}
		else if(categoryType.trim().equalsIgnoreCase("freeZone")){

			welcomeMessage =USSDConfigParameters.getInstance().getParameter("FREEZONE_HEADING");
		}
		if(StringUtils.isEmpty(welcomeMessage)) {
			welcomeMessage = "";
		}

		return AirtelUSSDResponseBuilder.convertToResponse(welcomeMessage, output, true, 
				USSDConfigParameters.getInstance().getUSSDHostURL() + "&action="+input.get("action"), backOptionRequired,backUrl,startIndex);
	}
	public String getCategoryId(String categoryType){
		USSDCacheManager cacheMngr=new USSDCacheManager();
		String catID=null;
		if(categoryType.equalsIgnoreCase("popularSong"))
			catID=cacheMngr.getParameters("USSD","POPULARSONG_CATID", "17");
		else if(categoryType.equalsIgnoreCase("top10"))
			catID=cacheMngr.getParameters("USSD", "TOP10_CATID", "18");
		else if(categoryType.equalsIgnoreCase("freeZone"))
			catID=cacheMngr.getParameters("USSD", "FREEZONE_CATID", "19");
		return catID;

	}
}
