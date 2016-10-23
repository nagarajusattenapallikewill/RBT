package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.onmobile.apps.ringbacktones.common.XMLUtils;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.provisioning.common.Utility;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RSSFeedScheduler;

public class RSSFeedSchedulerResponse extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private ServletConfig servletConfig = null;
	private static final String api_rssFeed = "rssFeed";
	private static final RBTDBManager rbtDBManager = RBTDBManager.getInstance();
    private static ResourceBundle rssFeedPropertiesBundle = null;
    private static Logger logger = Logger.getLogger(RSSFeedSchedulerResponse.class);
    private static String linkFeedType = "LINK";
    private static String contentFeedType = "CONTENT";
    
    
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		this.servletConfig = servletConfig;
		try {
			rssFeedPropertiesBundle = ResourceBundle.getBundle("rssFeed");
		} catch (Exception ex) {
             logger.info("rssFeed is not there in the classpath");
             ex.printStackTrace();
		}
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String feedType = request.getParameter("cmd");
		String responseStr = null;
		if (feedType != null) {
			if (feedType.equalsIgnoreCase("link")) {
				responseStr = linkResponseInXML();
			} else if (feedType.equalsIgnoreCase("content")) {
				responseStr = contentResponseInXML();
			}else{
				responseStr = "ERROR";
			}
		}else{
			responseStr = "Feed Type should be either link or content";
		}
		PrintWriter out = response.getWriter();
		out.println(responseStr);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	private String contentResponseInXML() {
		String response = null;
		try {
			
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			// root elements
			Document doc = docBuilder.newDocument();
			Element header = doc.createElement("rss");
			doc.appendChild(header);
			header.setAttribute("version", "2.0");
			header.setAttribute("xmlns:media", "http://m.imimobile.com/mrss/");

			Element rootElement = doc.createElement("channel");
			header.appendChild(rootElement);
			rootElement.setAttribute("vendor", "onmobile");
			Date date = new Date();
			rootElement.setAttribute("lastpublishdate", date.toString());

			Element mainTitle = doc.createElement("title1");
			mainTitle.appendChild(doc.createTextNode("Callertunes"));
			rootElement.appendChild(mainTitle);
			
            RBTDBManager rbtDBManager = RBTDBManager.getInstance();
            List<RSSFeedScheduler> rssFeedRecordList = rbtDBManager.getRSSFeedRecord(contentFeedType);
            if(rssFeedRecordList!=null)
                logger.info("No of records found Content= "+rssFeedRecordList.size());
            else
            	logger.info("No records found Content");

            int i=0;
			for (RSSFeedScheduler rssFeed : rssFeedRecordList) {
				Element item = doc.createElement("item");
				item.setAttribute("id", (++i)+"");
				item.setAttribute("circle", 
						getProperties(rssFeed.getFeedCircleGroup(),rssFeed.getFeedCircleGroup()));
				item.setAttribute("weekid", rssFeed.getFeedWeekId());
				rootElement.appendChild(item);

				Element title = doc.createElement("title");
				title.appendChild(doc.createTextNode(getVal(rssFeed.getFeedOMContentName())));
				item.appendChild(title);

				Element contentType = doc.createElement("contenttype");
				String contentTypeStr = getProperties("content.contenttype.text","RBT");
				contentType.appendChild(doc.createTextNode(contentTypeStr));
				item.appendChild(contentType);

				Element category = doc.createElement("category");
				category.appendChild(doc.createTextNode(getVal(rssFeed.getFeedCategoryId())));
				item.appendChild(category);

				Element link = doc.createElement("link");
				String contentLink = getProperties("content.link.url","");
				contentLink = contentLink.replaceAll("%omCatId%", getVal(rssFeed.getFeedOMCategoryId()));
				link.appendChild(doc.createTextNode(contentLink)); 
				item.appendChild(link);

				Element moreLink = doc.createElement("morelink");
				moreLink.appendChild(doc.createTextNode(getProperties("content.morelink.url","")));
				item.appendChild(moreLink);

				Element moretitle = doc.createElement("moretitle");
				String moreTitleStr = getProperties("content.moretitle.text","Callertune");
				moretitle.appendChild(doc.createTextNode(moreTitleStr));
				item.appendChild(moretitle);

				Element pubdate = doc.createElement("pubdate"); 
				pubdate.appendChild(doc.createTextNode(getVal(rssFeed.getFeedPublishDate())));
				item.appendChild(pubdate);

				Element releasedate = doc.createElement("releasedate");
				releasedate.appendChild(doc.createTextNode(getVal(rssFeed.getFeedReleaseDate())));
				item.appendChild(releasedate);

				Element media = doc.createElement("media:thumbnail");
				media.setAttribute("width", "300");
				media.setAttribute("height", "300");
				String mediaUrl = getProperties("content.media.url","");
				mediaUrl = mediaUrl.replaceAll("%omCatId%", getVal(rssFeed.getFeedOMCategoryId()));
				media.setAttribute("url", mediaUrl);
				item.appendChild(media);

				Element position = doc.createElement("position");
				position.appendChild(doc.createTextNode(getVal(rssFeed.getFeedModuleId())));
				item.appendChild(position);

				Element timeslot = doc.createElement("timeslot");
				timeslot.appendChild(doc.createTextNode(getVal(rssFeed.getFeedTimeSlotId())));
				item.appendChild(timeslot);
				
			}

			  response =  XMLUtils.getStringFromDocument(doc);
			  logger.info("Response for Content = "+response);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (Exception tfe) {
			tfe.printStackTrace();
		}
		return response;
	}

	private String linkResponseInXML() {
		String response = null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			Document document = docBuilder.newDocument();

			Element header = document.createElement("rss");
			header.setAttribute("version", "2.0");
			header.setAttribute("xmlns:media", "http://m.imimobile.com/mrss/");

			Element rootElement = document.createElement("channel");
			header.appendChild(rootElement);
			rootElement.setAttribute("vendor", "onmobile");
			Date date = new Date();
			rootElement.setAttribute("lastpublishdate", date.toString());

			Element mainTitle = document.createElement("title");
			mainTitle.appendChild(document.createTextNode("Callertunes"));
			rootElement.appendChild(mainTitle);
            RBTDBManager rbtDBManager = RBTDBManager.getInstance();
            List<RSSFeedScheduler> rssFeedRecord = rbtDBManager.getRSSFeedRecord(linkFeedType);
            if(rssFeedRecord!=null)
                logger.info("No of records found Link= "+rssFeedRecord.size());
            else
            	logger.info("No records found Link");
            int i = 0;
			for (RSSFeedScheduler rssFeed : rssFeedRecord) {
				Element item = document.createElement("item");
				item.setAttribute("id", (++i)+""); 
				item.setAttribute("circle",
						getProperties(rssFeed.getFeedCircleGroup(),rssFeed.getFeedCircleGroup()));
				item.setAttribute("weekid", rssFeed.getFeedWeekId());
				rootElement.appendChild(item);

				Element title = document.createElement("title");
				String titleStr =getProperties("link.title.text","Callertunes"); 
				title.appendChild(document.createTextNode(titleStr));
				item.appendChild(title);

				Element category = document.createElement("category");
				category.appendChild(document.createTextNode(getVal(rssFeed.getFeedCategoryId())));
				item.appendChild(category);

				Element link = document.createElement("link");
				String linkUrl = getProperties("link.link.url" , "Not Configured");
				link.appendChild(document.createTextNode(linkUrl)); 
				item.appendChild(link);

				Element pubdate = document.createElement("pubdate");
				pubdate.appendChild(document.createTextNode(getVal(rssFeed.getFeedPublishDate())));
				item.appendChild(pubdate);

				Element releasedate = document.createElement("releasedate");
				releasedate.appendChild(document.createTextNode(getVal(rssFeed.getFeedReleaseDate())));
				item.appendChild(releasedate);

				Element position = document.createElement("position");
				position.appendChild(document.createTextNode(getVal(rssFeed.getFeedModuleId())));
				item.appendChild(position);

				Element timeslot = document.createElement("timeslot");
				timeslot.appendChild(document.createTextNode(getVal(rssFeed.getFeedTimeSlotId())));
				item.appendChild(timeslot);
				
			}
				document.appendChild(header);
            logger.info("Link Document ="+document);
			response =  XMLUtils.getStringFromDocument(document);
			logger.info("Link Document Str="+response);

		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (Exception tfe) {
			tfe.printStackTrace();
		}
		return response;
	}
	
	private String getProperties(String key, String defaultValue) {
		String value = null;
		try {
			value = rssFeedPropertiesBundle.getString(key);
		} catch (Exception ex) {
			value = defaultValue;
			ex.printStackTrace();
		}
		return value;
	}

	private String getVal(String value){
		if(value == null)
			value="";
		
		return value;
	}
}
