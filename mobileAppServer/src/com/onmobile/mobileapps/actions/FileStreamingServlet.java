package com.onmobile.mobileapps.actions;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

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
 *FileStreamingServlet takes absolute file path of the clip file/preview file
 * as a parameter("filePath") and returns the image/preview as a response to the browser.
 */
public class FileStreamingServlet extends HttpServlet {
	private static final long serialVersionUID = 456789056789L;
	private static Logger logger=Logger.getLogger(FileStreamingServlet.class);
	private MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();

	private static final String ALLOWED_EXTENSIONS_DEFAULT = "wav,mp3,png,jpg,gif";  
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	
	protected void doPost(HttpServletRequest request , HttpServletResponse response) throws IOException, ServletException {
		String filePath = request.getParameter("filePath");
		String isPreviewStr = request.getParameter("isPreview");
		String categoryId = request.getParameter("categoryId");
		logger.info("filePath :="+filePath +"isPreviewStr:="+isPreviewStr+"categoryId:="+categoryId);
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
					absolutePath = new File(imagePath, filePath).getAbsolutePath();
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
