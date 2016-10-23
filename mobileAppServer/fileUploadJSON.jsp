<%@page import="com.onmobile.android.configuration.PropertyConfigurator"
language="java" import="com.jspsmart.upload.*, java.util.*, com.oreilly.servlet.multipart.* "
import="com.onmobile.apps.ringbacktones.webservice.client.requests.RBTDownloadFileRequest"
import="com.onmobile.apps.ringbacktones.webservice.client.RBTClient"
import="org.apache.log4j.Logger"
import="com.onmobile.android.utils.AESUtils" %><jsp:useBean 
id="mySmartUpload" scope="page" class="com.jspsmart.upload.SmartUpload" 
/><%

	Logger logger = Logger.getLogger(this.getClass());
	// Variables
	int count=0;
	Boolean success = false;
	
	// Initialization
	mySmartUpload.initialize(pageContext);

	mySmartUpload.setTotalMaxFileSize(10000000);

	// Upload
	mySmartUpload.upload();
	
	logger.debug("ContentType: " + 	request.getContentType());
	
	String fileName = request.getParameter("fileName");
	String origFileName = fileName;
	String subscriberId = request.getParameter("subscriberId");
	logger.debug("subscriberId: " + subscriberId);
	logger.debug("key: " + 	PropertyConfigurator.getRequestSubscriberIdEncryptionKey());

	if (PropertyConfigurator.isEncryptionEnabled()) {
		String encryptedSubscriberId = AESUtils.encrypt(subscriberId, PropertyConfigurator.getRequestSubscriberIdEncryptionKey());
		String trimFileName = null;
		if (fileName.indexOf(encryptedSubscriberId) != -1) {
			trimFileName = fileName.substring(encryptedSubscriberId.length());
			logger.debug("trimFileName: " + 	trimFileName);
			fileName = subscriberId + trimFileName;
		}
	}
	
	String filePath = null;
	try {
		
		Files files = mySmartUpload.getFiles();
		for(int i=0;i<files.getCount();i++){
			filePath = System.getProperty("java.io.tmpdir") + java.io.File.separator+fileName;
			File file = files.getFile(i);
			file.saveAs(filePath);

			RBTDownloadFileRequest downloadRequest = new RBTDownloadFileRequest(subscriberId);
			downloadRequest.setFileName(fileName);
			downloadRequest.setBulkTaskFile(filePath);
			downloadRequest.setType("MOBILEAPP");
			RBTClient.getInstance().downloadFile(downloadRequest);

			if(downloadRequest.getResponse().equalsIgnoreCase("SUCCESS")) {
				//out.print(origFileName+" uploaded sucessfully");
				success = true;
			} 
			new java.io.File(filePath).delete();
		}

	} catch (Exception e) {
		out.print("{failure}");
		logger.error(e.getMessage(), e);
	} 

	if (success) {
		out.print("{success}");
	} else {
		out.print("{failure}");
	}
	
%>