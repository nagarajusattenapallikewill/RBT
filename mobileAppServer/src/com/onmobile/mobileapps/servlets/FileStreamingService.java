package com.onmobile.mobileapps.servlets;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.onmobile.android.configuration.PropertyConfigurator;
import com.onmobile.android.utils.Utility;
/**
 *FileStreamingService takes absolute file path of the clip file/preview file
 * as a parameter("filePath") and returns the image/preview as a response to the browser.
 */
public class FileStreamingService extends HttpServlet {
	private static final long serialVersionUID = 456789056789L;
	private static Logger logger=Logger.getLogger(FileStreamingService.class);
	private MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

	private static final String ALLOWED_EXTENSIONS_DEFAULT = "wav,mp3,png,jpg,gif";
	private static final String CLIP = "CLIP";
	private static final String CATEGORY = "CATEGORY";
	private static Map<Long, String> clipResolutionsMap = null;
	private static Map<Long, String> categoryResolutionsMap = null;
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	protected void doPost(HttpServletRequest request , HttpServletResponse response) throws IOException, ServletException {
		String filePath = request.getParameter("filePath");
		String isPreviewStr = request.getParameter("isPreview");
		String categoryId = request.getParameter("categoryId");
		String type = request.getParameter("type");	//clip or category
		String resolution  = request.getParameter("resolution");
		logger.info("filePath = " + filePath + ", isPreviewStr = "+ isPreviewStr 
				+", categoryId = " + categoryId + ", type = " + type + ", resolution = " + resolution);
		if(filePath.contains("../")){
			logger.info("File path contains ../, Hence request rejected");
			response.sendError(HttpServletResponse.SC_BAD_REQUEST); 
			return; 
		}
		if(categoryId != null && categoryId.length()>0) {
			String categoryImagePath = PropertyConfigurator.getCategoryImagePath(categoryId);
			logger.info("categoryImagePath :="+categoryImagePath);
			processStreaming(categoryImagePath, response);
		} else {	
			
		   boolean isAllowed = isAllowed(filePath);
		
		if (!isAllowed) {
			logger.info("Resource is not allowed to be served");
			response.sendError(HttpServletResponse.SC_NOT_FOUND); 
			return;
		}
	   
		String isImageUrl = PropertyConfigurator.isImageUrl(); 
		String isRelativePath = PropertyConfigurator.isRelativePath();
		isRelativePath = (!Utility.isStringValid(isRelativePath) ? request
				.getParameter("isRelativePath") : isRelativePath);
		if(!Boolean.parseBoolean(isRelativePath) && Boolean.parseBoolean(isPreviewStr)) {
			File mp3File = new File(filePath);
			if(!mp3File.exists()) {
				return;
			}	
			response.setContentType("audio/mpeg");
			response.addHeader("Content-Disposition","attachment; filename=" + mp3File.getName());
			response.setContentLength((int) mp3File.length());
			processStreaming(filePath,response);
		}else if(Boolean.parseBoolean(isImageUrl)) {
			
			logger.info("path url is "+ filePath);   
			HttpClient httpClient = new HttpClient(connectionManager);
			GetMethod get = new GetMethod(filePath);
			get.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(3, false));
			httpClient.executeMethod(get);
			InputStream is = null;
			byte[] bytes = null;
			try{
				is = get.getResponseBodyAsStream();
				bytes = streamToBytes(is);
			} finally {
				if (null != is) {
					is.close();
				}
			}
			if (null == bytes) {
				logger.info("The file is empty , read file has returned null");
				return;
			}
			OutputStream outStream = response.getOutputStream();
			outStream.write(bytes);
			outStream.close();
		} else {
			String waveFilePath = PropertyConfigurator.getPreviewPath();
			String imagePath = PropertyConfigurator.getImagePath();
			logger.info("waveFilePath :="+waveFilePath);
			logger.info("imagePath :="+imagePath);
			logger.info("isRelativePath :="+isRelativePath);
			String absolutePath = filePath;
			if(Boolean.parseBoolean(isRelativePath)) {
				//checking whether the filePath is for image file path or preview file path
				if(Boolean.parseBoolean(isPreviewStr)) {
					absolutePath = new File(waveFilePath, filePath).getAbsolutePath();
					File mp3File = new File(absolutePath);
					if (!mp3File.exists())
						return;

					response.setContentType("audio/mpeg");
					response.addHeader("Content-Disposition",
			                "attachment; filename=" + mp3File.getName());
					response.setContentLength((int) mp3File.length());
				} else {
					absolutePath = calculateAbsolutePathForClipOrCategory(
							filePath, type, resolution);
					if (absolutePath == null) {
						logger.error("absolutePath is null. Returning now.");
						return;
					}
					logger.info("Final path for image: " + absolutePath);
				}
			}
			File absolutePathObj = new File(absolutePath);
			if (absolutePathObj.exists()) {
			   response.addHeader("Content-Disposition","attachment; filename=" + absolutePathObj.getName());
			}
			processStreaming(absolutePath, response);
		}
	  } 
	}

	private String calculateAbsolutePathForClipOrCategory(String filePath,
			String type, String resolution) {
		String absolutePath = null;
		if (type == null || type.trim().isEmpty()) {
			logger.debug("parameter, type was not present. Set to default value as: " + CLIP);
			type = CLIP;
		} else if (!type.equalsIgnoreCase(CLIP) && !type.equalsIgnoreCase(CATEGORY)) {
			logger.error("Invalid value for parameter, type: " + type + ". Returning now!");
			return null;
		}
		String path = null;
		if (type.equalsIgnoreCase(CLIP)) {
			path = PropertyConfigurator.getImageWithResolutionsPath();
		} else {
			path = PropertyConfigurator.getCategoryImageWithResolutionsPath();
		}
		String finalResolution = getResolution(type, path, resolution);
		if (finalResolution == null) {
			logger.error("No resolution found. Returning now.");
			return null;
		}
		path = new File(path, finalResolution).getAbsolutePath();
		
		File tempFile = new File(path, filePath);
		if (tempFile.exists()) {
			absolutePath = new File(path, filePath).getAbsolutePath();
		} else {
			logger.error("Required image doesn't exist in the path: " + tempFile.getAbsolutePath() + ". Returning null!");
		}
		return absolutePath;
	}
	
	/**
	 * Calculates and returns the required resolution to be used.
	 * If exact match (String matching) is found, the same is returned.
	 * Else numeric matching is checked.
	 * Else the next greatest resolution is returned.
	 * Else if no resolution is found to be greater than the reqResolution, the greatest available resolution is returned.
	 * @author rony.gregory
	 * @param imagePath
	 * @param reqResolution
	 * @return Resolution string
	 */
	private String getResolution(String type, String imagePath, String reqResolution) {
		logger.info("imagePath: " + imagePath + ", reqResolution: " +  reqResolution);
		File file = new File(imagePath);
		Map<Long, String> localResolutionsMap = null;
		if (type.equalsIgnoreCase(CLIP)) {
			if (clipResolutionsMap == null) {
				clipResolutionsMap = fillResolutionsMap(clipResolutionsMap, file);
				logger.info("Filled clipResolutionsMap: " + clipResolutionsMap);
			}
			localResolutionsMap = clipResolutionsMap;		
		} else {
			if (categoryResolutionsMap == null) {
				categoryResolutionsMap = fillResolutionsMap(categoryResolutionsMap, file);
				logger.info("Filled categoryResolutionsMap: " + categoryResolutionsMap);
			}
			localResolutionsMap = categoryResolutionsMap;
		}
		
		if (localResolutionsMap == null || localResolutionsMap.isEmpty()) {
			logger.error("No suitable directories were found in the path: " + imagePath + ". Returning now.") ;
			return null;
		}
		logger.debug("localResolutionsMap: " + localResolutionsMap);
		Collection<String> resolutionsInMap = localResolutionsMap.values();
		for (String fileName : resolutionsInMap) {
			if (fileName.equalsIgnoreCase(reqResolution)) {
				logger.info("Direct match found for resolution. Calculated resolution: " + fileName);
				return fileName;
			}
		}
		logger.info("Resolutions map (the resolutions present in the file system): " + localResolutionsMap);
		Long longReqResolution = getLongValueOfResolution(reqResolution);
		if (longReqResolution == -1) {
			logger.error("Request parameter, resolution = " + reqResolution + " not in proper format. Returning now.");
		}
		Long finalResolution = 0L;
		if (localResolutionsMap.containsKey(longReqResolution)) {
			logger.info("Numeric match found for resolution. Calculated resolution: " + localResolutionsMap.get(longReqResolution));
			return localResolutionsMap.get(longReqResolution);
		} else {
			Set<Long> longResolutions = localResolutionsMap.keySet();
			Long largestResolution = 0L;
			for (Long longResolution : longResolutions) {
				if (longReqResolution < longResolution) {
					finalResolution = longResolution;
					logger.info("Next greatest resolution returned.");
					break;
				}
				largestResolution = longResolution;
			}
			if (finalResolution == 0L) {
				finalResolution = largestResolution;
				logger.info("Greatest available resolution returned.");
			}
		}
		logger.info("Calculated resolution: " + localResolutionsMap.get(finalResolution));
		return localResolutionsMap.get(finalResolution);
	}

	private Map<Long, String> fillResolutionsMap(Map<Long, String> localResolutionsMap, File file) {
		if (localResolutionsMap == null) {
			localResolutionsMap = new TreeMap<Long, String>();
		}
		if (file.isDirectory()) {
			String[] fileList = file.list();
			if (fileList != null) {
				for (String fileName : fileList) {
					File f = new File(file.getAbsoluteFile(),fileName);
					if (f.isDirectory()) {
						Long multipliedValue = getLongValueOfResolution(fileName);
						if (multipliedValue != -1L) {
							localResolutionsMap.put(multipliedValue, fileName);
						} else {
							logger.info("Directory not in the required format - <width>x<height>. Ignoring directory: " + fileName);
						}
					}
				}
			}
		}
		return localResolutionsMap;
	}

	private Long getLongValueOfResolution(String resolution) {
		Long longValue = -1L;
		try {
			if (resolution.toUpperCase().indexOf('X') != -1 ) {	//Only the width is used.
				longValue = Long.valueOf(resolution.substring(0, resolution.toUpperCase().indexOf('X')));
			}
		} catch(NumberFormatException e) {
			logger.warn("Resolution: " + resolution + " not in proper format.");
		} catch(IndexOutOfBoundsException e) {
			logger.warn("Resolution: " + resolution + " not in proper format.");
		}
		return longValue;
	}
	
	private void processStreaming(String filePath, HttpServletResponse response) throws IOException {
		logger.info("path is "+ filePath);
		byte[] bytes = readFile(filePath);
		if (null == bytes) {
			logger.info("The file is empty , read file has returned null");
			return;
		}
		OutputStream outStream = response.getOutputStream();
		outStream.write(bytes);
		outStream.close();
	}
	
	private byte[] readFile(String path) throws IOException {
		String absoluteFilePath = path;
		File file = new File(absoluteFilePath);
		if (!file.exists()) {
			logger.info("The file does not exist at " + absoluteFilePath);
			return null;
		}
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			return streamToBytes(fis);
		} finally {
			if (null != fis) {
				fis.close();
			}
		}
	}

	public static byte[] streamToBytes(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int len = -1;
		while ((len = is.read(buffer)) != -1) {
			// write the contents into the stream
			baos.write(buffer, 0, len);
		}
		return baos.toByteArray();
	}
	
	public boolean isAllowed(String filePath) {  
		String allowedExtensions = PropertyConfigurator.allowedFileExtension();
		if(allowedExtensions == null || allowedExtensions.equals("") || allowedExtensions.equalsIgnoreCase("null"))  
			allowedExtensions = ALLOWED_EXTENSIONS_DEFAULT;
		
		logger.info("allowedExtensions :="+allowedExtensions);
		File file = new File(filePath);
		//no extension
		if(file.getName().indexOf(".") == -1)
			return false;

		String fileExtension = file.getName().substring(file.getName().lastIndexOf(".")+1);
		
		List<String> extesnsionList = Arrays.asList(allowedExtensions.split(","));  
		return extesnsionList.contains(fileExtension);
	}	
}
