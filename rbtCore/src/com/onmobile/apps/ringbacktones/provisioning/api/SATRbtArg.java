/**
 * OnMobile Ring Back Tone 
 * 
 * $Author: rajesh.karavadi $
 * $Id: SATRbtArg.java,v 1.8 2012/06/15 11:39:08 rajesh.karavadi Exp $
 * $Revision: 1.8 $
 * $Date: 2012/06/15 11:39:08 $
 */
package com.onmobile.apps.ringbacktones.provisioning.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.Servlet;
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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.provisioning.common.Constants;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;


/**
 * Servlet implementation class SatRbt
 */
public class SATRbtArg extends HttpServlet implements Constants
{
	private static final long serialVersionUID = -1124141214161089912L;
	private static Logger logger = Logger.getLogger(SATRbtArg.class);
	private Properties prop;
	private static Map<String,List<Clip>> songMap = new HashMap<String,List<Clip>>();
	private static Map<String, String> categoryIdNameMap = new HashMap<String, String>();
	private String baseUrl;
	private RBTCacheManager cacheManager;
	private RBTClient client;

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public SATRbtArg() {
		super();
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	public void init(ServletConfig servletConfig) throws ServletException {
		super.init(servletConfig);
		try {
			cacheManager = RBTCacheManager.getInstance();
			client = RBTClient.getInstance();
			prop = new Properties();
			InputStream in = getClass().getResourceAsStream(propertyFileName);
			prop.load(in);
			populateSongMap();
		} catch (IOException ioe) {
			logger.error("Failed to initialize SATRbtArg servlet. IOException: "
					+ ioe.getMessage(), ioe);
		} catch (Exception e) {
			logger.error("Failed to initialize SATRbtArg servlet. Exception: "
					+ e.getMessage(), e);
		}
		logger.info(" SATRbtArg intialized successfully ");
	}

	private void populateSongMap() throws Exception {
		songMap.clear();
		categoryIdNameMap.clear();
		String categoryIdsString = prop.getProperty(KEY_CATEGORY).trim();
		logger.info(" Categories string: " + categoryIdsString);
		String[] categoryIdsArray = null;
		if (categoryIdsString != null && !"".equals(categoryIdsString)) {
			categoryIdsArray = categoryIdsString.split(",");
			logger.info(" Categories array length " + categoryIdsArray.length);
		} else {
			logger.error("Unable to get the value for property 'Category' ");
			throw new ServletException("No category configured");
		}

		for (int i = 0; categoryIdsArray != null && i < categoryIdsArray.length; i++) {
			String categoryIdStr = categoryIdsArray[i];
			Integer categoryId = Integer.parseInt(categoryIdsArray[i]);
			Category category = cacheManager.getCategory(categoryId);
			Clip[] clips = cacheManager.getActiveClipsInCategory(categoryId);
			List<Clip> songList = new ArrayList<Clip>();
			if (clips != null && clips.length > 0) {
				for (int j = 0; j < clips.length; j++) {
					if (clips[j] != null)
						songList.add(clips[j]);
				}
			}
			songMap.put(categoryIdStr, songList);
			if (null != category) {
				categoryIdNameMap
						.put(categoryIdStr, category.getCategoryName());
			} else {
				throw new Exception("Category not found");
			}
		}
		logger.info(" categoryIdNameMap: " + categoryIdNameMap);
		logger.info(" songMap: " + songMap);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/xml; charset=utf-8");
		PrintWriter out = response.getWriter();
		String msisdn = request.getParameter("msisdn");
		String categoryId = request.getParameter("categoryId");
		String clipId = request.getParameter("clipId");
		String confirm = request.getParameter("confirm");
		String responseMessage = null;
		
		baseUrl = "http://" + request.getServerName() + ":"
				+ request.getServerPort() + request.getRequestURI();
		logger.info("Request parameters are msisdn: " + msisdn + ", clipId: " + clipId + ", categoryId: "
				+ categoryId + ", confirm: "+confirm+", baseUrl :" + baseUrl);

		/*
		 * First time, it has to display list of songs under a category. So, the
		 * request contains only categoryId. Second time, categoryId and clipId
		 * exists in the request to proceed further. At last to make selection,
		 * along with categoryId and clipId the request contains confirm=y
		 * parameter.
		 */
		if (null != msisdn && null != categoryId && null != clipId
				&& null != confirm && "y".equalsIgnoreCase(confirm)) {
			logger.info("Enterered into final step. msisdn: " + msisdn
					+ ", clipId: " + clipId + ", categoryId: " + categoryId
					+ ", confirm: " + confirm);
			if (!isValidSubscriber(msisdn)) {
				String message = prop.getProperty(KEY_ERROR_INVALID_MSISDN);
				responseMessage = convertStringAsResponse(message);
			} else if (!isValidCategory(categoryId)) {
				String message = prop.getProperty(KEY_ERROR_INVALID_CATEGORY);
				responseMessage = convertStringAsResponse(message);
			} else if (!isValidClip(categoryId, clipId)) {
				String message = prop.getProperty(KEY_ERROR_INVALID_CLIP);
				responseMessage = convertStringAsResponse(message);
			} else {
				String selectionResponse = selectSong(msisdn, clipId,
						categoryId);
				if (selectionResponse != null) {
					String selResponse = prop
							.getProperty(getKeyFromSelectionResponse(selectionResponse));
					responseMessage = convertStringAsResponse(selResponse);
				}
			}
		} else if (null != msisdn && null != categoryId && null != clipId
				&& null == confirm) {
			logger.info("Enterered into second step. msisdn: " + msisdn
					+ ", clipId: " + clipId + ", categoryId: " + categoryId);
			if (!isValidSubscriber(msisdn)) {
				String message = prop.getProperty(KEY_ERROR_INVALID_MSISDN);
				responseMessage = convertStringAsResponse(message);
			} else if (!isValidCategory(categoryId)) {
				String message = prop.getProperty(KEY_ERROR_INVALID_CATEGORY);
				responseMessage = convertStringAsResponse(message);
			} else if (!isValidClip(categoryId, clipId)) {
				String message = prop.getProperty(KEY_ERROR_INVALID_CLIP);
				responseMessage = convertStringAsResponse(message);
			} else {
				responseMessage = createConfirmationResponse(msisdn, clipId,
						categoryId);
			}
		} else if (null != msisdn && null != categoryId && null == clipId
				|| (null != confirm && !"y".equalsIgnoreCase(confirm))) {
			logger.info("Enterered into first step. msisdn: " + msisdn
					+ ", categoryId: " + categoryId);
			if (!isValidSubscriber(msisdn)) {
				String message = prop.getProperty(KEY_ERROR_INVALID_MSISDN);
				responseMessage = convertStringAsResponse(message);
			} else if (!isValidCategory(categoryId)) {
				String message = prop.getProperty(KEY_ERROR_INVALID_CATEGORY);
				responseMessage = convertStringAsResponse(message);
			} else {
				List<Clip> clipList = songMap.get(categoryId);
				logger.info("clipList:" + clipList);
				if (clipList != null) {
					String categoryName = categoryIdNameMap.get(categoryId);
					responseMessage = convertSongListAsResponse(msisdn,
							clipList, categoryId, categoryName);
				}
			}
		} else if(null == categoryId) {
			String message = prop.getProperty(KEY_ERROR_NO_CATEGORY_SELECTED);
			responseMessage = convertStringAsResponse(message);
		} else if(null == msisdn) {
			String message = prop.getProperty(KEY_ERROR_INVALID_MSISDN);
			responseMessage = convertStringAsResponse(message);
		}

		if (null == responseMessage) {
			String errorMessage = prop.getProperty(KEY_ERROR_INVALID_REQUEST);
			responseMessage = convertStringAsResponse(errorMessage);
		}
		
		logger.info(" Response: "+responseMessage);
		out.write(responseMessage);
		out.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	/**
	 * Selecting song with clipId, categoryId for msisdn.
	 * @param msisdn
	 * @param clipId
	 * @param categoryId
	 * @return
	 */
	protected String selectSong(String msisdn, String clipId, String categoryId) {
		String response = null;
		logger.info(" Making selection for msisdn: " + msisdn
				+ ", categoryId: " + categoryId + ", clipId: " + clipId);
		SelectionRequest selectionRequest = new SelectionRequest(msisdn,
				categoryId, clipId);
		selectionRequest.setMode("SAT");
		client.addSubscriberSelection(selectionRequest);
		response = selectionRequest.getResponse();
		logger.info("Selection response " + response);
		return response;
	}

	protected String convertStringAsResponse(String message) {
		Document document = createDocument();
		NodeList card = document.getElementsByTagName(NODE_CARD);
		Element cardElement = (Element) card.item(0);
		// Append text to node <p>.
		Element pElement =  document.createElement(NODE_P);
		cardElement.appendChild(pElement);
		Text text = document.createTextNode(message);
		pElement.appendChild(text);
		return convertToString(document);
	}

	/**
	 * @param msisdn
	 * @param clipList
	 * @param categoryId
	 * @param categoryName
	 * @return
	 */
	protected String convertSongListAsResponse(String msisdn,
			List<Clip> clipList, String categoryId, String categoryName) {
		Document document = createDocument();
		// display list of clips as options
		
		NodeList card = document.getElementsByTagName(NODE_CARD);
		Element cardElement = (Element) card.item(0);
		// Append text to node <p>.
		Element pElement =  document.createElement(NODE_P);
		cardElement.appendChild(pElement);
		
		Element selectElement = document.createElement(NODE_SELECT);
		// select node has an attribute title. The value will be the category name
		selectElement.setAttribute(TITLE, categoryName);
		pElement.appendChild(selectElement);
		for (Clip song : clipList) {
			String url = baseUrl + "?msisdn=" + msisdn + "&clipId="
					+ song.getClipId() + "&categoryId=" + categoryId;
			logger.info("<option onpick=\"" + url + "\">" + song.getClipName()
					+ "</option>");
			Element optionElement = document.createElement(NODE_OPTION);
			optionElement.setAttribute(ONPICK, url);
			Text songName = document.createTextNode(song.getClipName());
			optionElement.appendChild(songName);
			selectElement.appendChild(optionElement);
		}
		return convertToString(document);
	}
	
	/**
	 * Create SATML response.
	 * 
	 * <satml>
	 *  <card> 
	 *   <p>Rbt selection 30/month
	 *   <a href="http://localhost:8080/rbt/SATRbtArg.do?msisdn=9986030880&clipId=2452486&categoryId=500038&confirm=y" />
	 *   </p>
	 *   </card>
	 * </satml>
	 * 
	 * @param msisdn
	 * @param clipId
	 * @param categoryId
	 * 
	 * @return SATML format type XML response.
	 */
	public String createConfirmationResponse(String msisdn, String clipId,
			String categoryId) {
		Document document = createDocument();

		NodeList cardNodeList = document.getElementsByTagName(NODE_CARD);
		Element card = (Element) cardNodeList.item(0);

		String message = prop.getProperty(KEY_SEL_PRICE);
		
		Element p = document.createElement(NODE_P);
		card.appendChild(p);

		Text text = document.createTextNode(message);
		p.appendChild(text);
		
		String confirmUrl = baseUrl + "?msisdn=" + msisdn + "&clipId="
		+ clipId + "&categoryId=" + categoryId + "&confirm=y";

		Element anchor =  document.createElement(ANCHOR);
		anchor.setAttribute(HREF, confirmUrl);
		p.appendChild(anchor);
		
		return convertToString(document);
	}
	
	
	/**
	 * Create empty XML document for response. 
	 * 
	 * @return
	 */
	private Document createDocument() {
		DocumentBuilder documentBuilder;
		Document document = null;
		try {
			documentBuilder = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder();
			document = documentBuilder.newDocument();
			Element rootElement = document.createElement(NODE_SATML);
			document.appendChild(rootElement);
			Element cardElement = document.createElement(NODE_CARD);
			rootElement.appendChild(cardElement);
		} catch (ParserConfigurationException pce) {
			logger.error("Unable to create document builder. Exception: "
					+ pce.getMessage(), pce);
		}
		return document;
	}
	
	public String convertToString(Document document) {
		try {
			DOMSource domSource = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			TransformerFactory transformerFactory = TransformerFactory
					.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException te) {
			logger.error("Unable to convert Document to String"
					+ te.getMessage(), te);
		}
		return null;
	}
	
	private boolean isValidSubscriber(String msisdn) {
		if(!"".equals(msisdn)) {
			return true;
		}
		return false;
	}
	
	private boolean isValidCategory(String categoryId) {
		if (!"".equals(categoryId)
				&& null != categoryIdNameMap.get(categoryId)) {
			logger.info("Category: " + categoryId+" is valid");
			return true;
		}
		logger.info("Invalid category: " + categoryId);
		return false;
	}
	
	private boolean isValidClip(String categoryId, String clipId) {
		List<Clip> clips = songMap.get(categoryId);
		if (!"".equals(clipId) && null != clips) {
			for (Clip clip : clips) {
				String clipIdFromMap = String.valueOf(clip.getClipId());
				if (clipIdFromMap.equals(clipId)) {
					logger.info("Valid clip: " + categoryId + ", clip: "
							+ categoryId);
					return true;
				}
			}
		}
		logger.info("Invalid clip: " + clipId);
		return false;
	}
	
	private String getKeyFromSelectionResponse(String selectionResponse) {
		String key = null;
		if (selectionResponse.trim().equalsIgnoreCase(SUCCESS)) {
			key = KEY_SEL_RESP_SUCCESS;
		} else {
			key = KEY_SEL_RESP_ERROR;
		}
		return key;
	}
}
