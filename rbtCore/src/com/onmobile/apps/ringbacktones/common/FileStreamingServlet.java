package com.onmobile.apps.ringbacktones.common;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

/**
 *FileStreamingServlet takes absolute file path of the clip file/preview file
 * as a parameter("filePath") and returns the image/preview as a response to the browser.
 */
public class FileStreamingServlet extends HttpServlet implements iRBTConstant{
	private static final long serialVersionUID = 456789056789L;
	private static Logger logger=Logger.getLogger(FileStreamingServlet.class);
	private MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
	
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}
	@Override
	public void init() throws ServletException {
		
	}
	
	protected void doPost(HttpServletRequest request , HttpServletResponse response) throws IOException, ServletException {
		String filePath = request.getParameter("filePath");
		String isPreviewStr = request.getParameter("isPreview");
		boolean isPreview = false;
		//checking whether the filePath is for image file path or preview file path
		if(isPreviewStr!=null && isPreviewStr.equalsIgnoreCase("TRUE"))
			isPreview = true;

		File mp3File = new File(filePath);
		if (!mp3File.exists())
			return;

		response.setContentType("audio/mpeg");
		response.addHeader("Content-Disposition",
                "attachment; filename=" + mp3File.getName());

		response.setContentLength((int) mp3File.length());
		//checking if the filePath is a preview   or   image with http url as source of image   or  image with system path as source of image. 
		if(isPreview){
			processStreaming(filePath,response);
		}else if(RBTParametersUtils.getParamAsString("COMMON", IS_IMAGE_URL, "false").equalsIgnoreCase("TRUE")){
			logger.info("path url is "+ filePath);
			if(filePath!=null && !filePath.trim().startsWith("http")){
				filePath="http://"+request.getServerName()+":"+request.getServerPort()+filePath;
				logger.info("full http path url is "+ filePath);
			}
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
		}else{
			processStreaming(filePath,response);
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
}
