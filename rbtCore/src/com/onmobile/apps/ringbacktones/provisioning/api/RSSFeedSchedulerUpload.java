package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;

public class RSSFeedSchedulerUpload extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletConfig servletConfig = null;
	private static final String api_rssFeed = "rssFeed";
	private static final RBTDBManager rbtDBManager = RBTDBManager.getInstance();
    private static Logger logger = Logger.getLogger(RSSFeedSchedulerUpload.class);
    private static List<String> linkTypeModuleIdList = null;
    private static List<String> contentTypemoduleIdList = null;
    private static ResourceBundle resourceBundle = null;
    
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.servletConfig = config;
		resourceBundle = ResourceBundle.getBundle("rssFeed");
		linkTypeModuleIdList = Arrays.asList(resourceBundle.getString("link.type.moduleId").split(","));
		contentTypemoduleIdList = Arrays.asList(resourceBundle.getString("content.type.moduleId").split(","));
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		logger.info("Going to Upload RSS Feed file");
		PrintWriter out = response.getWriter();
		response.setContentType("text/html");
		out.print("<br><h3>Upload RSS Feed File<h3><br>");
		out.print("<form action='rssFeedUpload.do?upload=true' method='post' enctype='multipart/form-data'>"
				+ "RSS Feed File:  <input type='file' name='feedFile'>"
				+ "<br/><br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<input type='submit' value='upload'></form>'");

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		HashMap<String, String> requestParams = Utility.getRequestParamsMap(servletConfig, request,
				response, api_rssFeed);
		String upload = request.getParameter("upload");
		logger.info("upload = "+upload+"request = "+request+ "requestParams = "+requestParams);
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:ss a");
		if (upload != null && upload.equalsIgnoreCase("true")) {
			String filePath = requestParams.get(Constants.ugc_param_WAVFILE);
			File file = new File(filePath);
			if (filePath == null || !file.exists()) {
              logger.info("No File found in the path = "+filePath);
              return;
			}
			FileReader fileReader = new FileReader(file);
			BufferedReader bufferedReader = new BufferedReader(fileReader);
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				String record[] = line.split(",");
				if (record.length == 0){
					logger.info("Data is not proper for the record = "+line);
					continue;
				}
				String feedDay = record.length>0?record[0]:null;
				String feedWeekId = record.length>1?record[1]:null;
				String feedCircleGroup = record.length>2?record[2]:null;
				String feedGroupId = record.length>3?record[3]:null;
				String feedModule = record.length>4?record[4]:null;
				String feedModuleId = record.length>5?record[5]:null;
				String feedCategory = record.length>6?record[6]:null;
				String feedCategoryId = record.length>7?record[7]:null;
				String feedCPName = record.length>8?record[8]:null;
				String feedCPId = record.length>9?record[9]:null;
				String feedPosition = record.length>10?record[10]:null;
				String feedTimeSlot = record.length>11?record[11]:null;
				String feedTimeSlotId = record.length>12?record[12]:null;
				String feedOMCategoryId = record.length>13?record[13]:null;
				String feedOMContentName = record.length>14?record[14]:null;
				String feedPubDate = dateFormat.format(new Date());
				String feedType = null;
				String feedReleaseDate = getReleaseDate(feedWeekId,feedDay);
				if(linkTypeModuleIdList!=null && linkTypeModuleIdList.contains(feedModuleId)){
					feedType = "LINK";
				}else if(contentTypemoduleIdList!=null && contentTypemoduleIdList.contains(feedModuleId)){
					feedType = "CONTENT";
				}
				boolean success = rbtDBManager.insertRSSFeedSchedulerRecord(feedDay, feedWeekId,
						feedCircleGroup, feedGroupId, feedModule, feedModuleId, feedCategory,
						feedCategoryId, feedCPName, feedCPId, feedPosition, feedTimeSlot,
						feedTimeSlotId, feedOMCategoryId, feedOMContentName,feedType,feedPubDate,feedReleaseDate);
				logger.info("Record Inserted .... = "+success);
			}

		}
		
			PrintWriter out = response.getWriter();
   			response.setContentType("text/html");
            out.println("Completed.........");

	}
	
	public static void main(String[] args) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:ss");
		System.out.println(dateFormat.format(new Date()));
	}
	
	private String getReleaseDate(String weekId,String day){
		String weekid_day = resourceBundle.getString("weekid_day");
		String dateStr = resourceBundle.getString(weekid_day);
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
		if(weekid_day!=null){
			String str[] = weekid_day.split("_");
		    String confWeekId = str[0];
		    String confDay = str[1];
		    String dayStr = resourceBundle.getString(day);
		    String confDayStr = resourceBundle.getString(str[1]);
			int noOfDaysToBeAdded = (Integer.parseInt(weekId) - Integer.parseInt(confWeekId)) * 7
					+ (Integer.parseInt(dayStr) - Integer.parseInt(confDayStr));
			try {
				Date date = sdf.parse(dateStr);
				long timeInMillis = date.getTime()+noOfDaysToBeAdded*24*60*60*1000l;
				Date finalDate = new Date(timeInMillis);
                return sdf.format(finalDate);				
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		return null;
	}
}
