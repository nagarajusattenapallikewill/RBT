package com.onmobile.apps.ringbacktones.ussd.tatagsm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;

public class USSDBrowseContents {

	private static Logger basicLogger = Logger.getLogger(USSDBrowseContents.class);

	private Map<String, String> input = new HashMap<String, String>();
	private HttpServletResponse response = null;

	public USSDBrowseContents(Map<String, String> input, HttpServletResponse response) {
		this.input = input;
		this.response = response;
	}

	public void process() throws IOException {
		response.setContentType(USSDServlet.CONTENT_TYPE_REQUEST_ANSWER);
		String resp=getResponse();
		basicLogger.info(" response is " + resp);
		response.getWriter().println(resp);
	}

	public String getResponse() {
		//parent category id pcatid=
		//category id catid=
		//clip id cid=
		//next next=
		//mobile number subscriber=
		RBTClient rbtClient = null;
		try {
			rbtClient = RBTClient.getInstance();
		} catch(Exception e) {
			e.printStackTrace();
			return "";
		}
		String subscriberId = input.get("subscriber");
		String parentCategoryId = input.get("pcatid");
		String categoryId = input.get("catid");
		String nextNodeId = input.get("next");

		int startIndex = 0;
		if(null != nextNodeId && nextNodeId.length() > 0) {
			try {
				startIndex = Integer.parseInt(nextNodeId);
			} catch(NumberFormatException nfe) {
				//ignore
			}
		}

		RbtDetailsRequest rbtDetailsRequest = new RbtDetailsRequest(subscriberId);
		Subscriber subscriber = rbtClient.getSubscriber(rbtDetailsRequest);
		if(null == subscriber) {
			basicLogger.error("Subscriber object is null. subscriberId: " + subscriberId);
			return USSDConfigParameters.getInstance().getParameter("MESSAGE_TECHNICAL_DIFFICULTIES");
		}
//		subscriber.setCircleID("HYD");
		
		if(StringUtils.isEmpty(parentCategoryId) && StringUtils.isEmpty(categoryId)) {
			//get the parent categories mapped under the configured parameter USSD_PARENT_CATEGORY_ID
			String ussdParentCategoryId = USSDConfigParameters.getInstance().getParameter("USSD_PARENT_CATEGORY_ID");
			int ussdParentCategoryIdInt = 0;
			if(StringUtils.isNotEmpty(ussdParentCategoryId)) {
				try {
					ussdParentCategoryIdInt = Integer.parseInt(ussdParentCategoryId);
				} catch(NumberFormatException nfe) {
					//ignore
				}
			}
			Category[] categories = getCategories(subscriber.getCircleID(), ussdParentCategoryIdInt);
			String subCategoryBrowseURL = USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=catbrowse";
			List<USSDNode> output = new ArrayList<USSDNode>();
			for(int i=startIndex; i<categories.length && i<startIndex+USSDNode.MAX_NODES; i++) {
				basicLogger.info(" categories[i].getCategoryName()"+categories[i].getCategoryName()); 
				USSDNode node = new USSDNode(categories[i].getCategoryId(), 0, 
												categories[i].getCategoryName(), 
												subCategoryBrowseURL + "&pcatid=" + categories[i].getCategoryId());
				output.add(node);
			}
			String welcomeMessage = USSDConfigParameters.getInstance().getParameter("MESSAGE_BROWSE_TOP_CATEGORIES");
			if(StringUtils.isEmpty(welcomeMessage)) {
				welcomeMessage = "";
			}
			return USSDResponseBuilder.convertToResponse(welcomeMessage, output, true, subCategoryBrowseURL, startIndex);
		}
		
		if(StringUtils.isNotEmpty(parentCategoryId) && StringUtils.isEmpty(categoryId)) {
			//get the sub categories mapped under this parent category
			Category[] categories = getCategories(subscriber.getCircleID(), Integer.parseInt(parentCategoryId));
			String subCategoryBrowseURL = USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=catbrowse&pcatid=" + parentCategoryId;
			List<USSDNode> output = new ArrayList<USSDNode>();
			for(int i=startIndex; i<categories.length && i<startIndex+USSDNode.MAX_NODES; i++) {
				USSDNode node = new USSDNode(categories[i].getCategoryId(), 0, 
											categories[i].getCategoryName(), 
											subCategoryBrowseURL + "&catid=" + categories[i].getCategoryId());
				output.add(node);
			}
			String welcomeMessage = USSDConfigParameters.getInstance().getParameter("MESSAGE_BROWSE_SUB_CATEGORIES");
			if(StringUtils.isEmpty(welcomeMessage)) {
				welcomeMessage = "";
			}

			return USSDResponseBuilder.convertToResponse(welcomeMessage, output, true, subCategoryBrowseURL, startIndex);
		}
		
		if(StringUtils.isNotEmpty(parentCategoryId) && StringUtils.isNotEmpty(categoryId)) {
			//get the clips mapped under the sub category category id
			Clip[] clips = RBTCacheManager.getInstance().getActiveClipsInCategory(Integer.parseInt(categoryId));
			String clipSelectionURL = USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=selectclip&pcatid=" + parentCategoryId + "&catid=" + categoryId;
			List<USSDNode> output = new ArrayList<USSDNode>();
			for(int i=startIndex; i<clips.length && i<startIndex+USSDNode.MAX_NODES; i++) {
				USSDNode node = new USSDNode(clips[i].getClipId(), 0, 
											clips[i].getClipName(), 
											clipSelectionURL + "&cid=" + clips[i].getClipId());
				output.add(node);
			}
			String welcomeMessage = USSDConfigParameters.getInstance().getParameter("MESSAGE_BROWSE_CLIPS");
			if(StringUtils.isEmpty(welcomeMessage)) {
				welcomeMessage = "";
			}
			return USSDResponseBuilder.convertToResponse(welcomeMessage, 
															output, 
															true, 
															USSDConfigParameters.getInstance().getUSSDHostURL() + "&action=catbrowse&pcatid=" + parentCategoryId + "&catid=" + categoryId, 
															startIndex);
		}
		basicLogger.error("Invalid browsing option. " + " subscriberId: " + subscriberId + " parentCategoryId: " + parentCategoryId
							+ " categoryId: " + categoryId + " startIndex: " + startIndex);
		return "";
	}
	
	private Category[] getCategories(String circleId, int parentCategoryId) {
		if(basicLogger.isInfoEnabled()) {
			basicLogger.info("Getting categories under circleId: " + circleId + " parentCategoryId: " + parentCategoryId);
		}
		RBTCacheManager cacheManager = RBTCacheManager.getInstance();
		Category[] categories = cacheManager.getActiveCategoriesInCircle(circleId, parentCategoryId, 'b');
		if(null == categories || categories.length <= 0) {
			categories = cacheManager.getActiveCategoriesInCircle(circleId, parentCategoryId, 'y');
			if(null == categories || categories.length <= 0) {
				categories = cacheManager.getActiveCategoriesInCircle(circleId, parentCategoryId, 'n');
			}
		}
		if(categories==null)
			basicLogger.info(" categories is null " );
		else
		   basicLogger.info(" categories " + categories.length);
		return categories;
	}
	
	public static void main(String[] args) {
		Map<String, String> input = new HashMap<String, String>();
//		input.put("pcatid", "2466");
//		input.put("catid", "5076");
//		input.put("next", "22");
//		action=catbrowse&pcatid=2466&catid=5049&next=8
		input.put("subscriber", "9886753756");
		USSDBrowseContents browseContents = new USSDBrowseContents(input, null);
		System.out.println(browseContents.getResponse());
	}
}
