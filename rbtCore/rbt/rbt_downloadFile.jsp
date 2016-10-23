<%@ page
	import="java.util.*,java.io.*,java.io.File,org.apache.log4j.Logger"%>
<%@ page language="java" import="com.jspsmart.upload.*"%>
<form name="feed" ENCTYPE="multipart/form-data">
	<%
		Logger DOWNLOAD_FILE_TXN_LOG = Logger
				.getLogger("DownloadFileTxnLogger");
		Logger LOG = Logger.getLogger(this.getClass());

		Properties m_bundle = new Properties();
		String pathDir = null;
		String resourceFilePath = "e:" + File.separator + "onmobile"
				+ File.separator + "ivm_2_2" + File.separator + "config"
				+ File.separator + "resource.properties";
		if (m_bundle != null) {
			try {
				if (new File(resourceFilePath).exists())
					m_bundle.load(new FileInputStream(resourceFilePath));
				else
					m_bundle.load(new FileInputStream(File.separator
							+ "opt" + File.separator + "rbtconfig"
							+ File.separator + "resource.properties"));
			} catch (Exception e) {
				LOG.error(
						"Unable to load bundle. Exception: "
								+ e.getMessage(), e);
			}
		}
		String strFeed = null;
		String strStatus = null;
		String strFile = null;
		String strResponse = "FAILURE";
		int saved = 0;
		int fileCount = 0;
		try {
			pathDir = m_bundle.getProperty("DEFAULT_DOWNLOAD_PATH");

			long maxfilesize = 20000000;
			SmartUpload mySmartUpload = new SmartUpload();
			mySmartUpload.initialize(pageContext);
			mySmartUpload.setTotalMaxFileSize(maxfilesize);
			mySmartUpload.upload();

			strFeed = mySmartUpload.getRequest().getParameter("FEED");
			fileCount = (null != mySmartUpload.getFiles()) ? mySmartUpload
					.getFiles().getCount() : 0;
			//strStatus = mySmartUpload.getRequest().getParameter("STATUS");

			if (strFeed != null && strFeed.equalsIgnoreCase("UGCFILE")
					&& mySmartUpload.getFiles().getCount() > 0) {
				if (mySmartUpload.getFiles().getFile(0).getSize() > 0
						&& pathDir != null) {
					strFile = mySmartUpload.getFiles().getFile(0)
							.getFileName();
					saved = mySmartUpload.save(pathDir);
					if (saved > 0) {
						LOG.info("Successfully saved. pathDir: " + pathDir
								+ ", saved: " + saved);
						strResponse = "SUCCESS";
					} else {
						LOG.error("Failed to save. feed: " + strFeed
								+ ", file: " + strFile + ", pathDir: "
								+ pathDir + ", saved: " + saved
								+ ", fileCount: " + fileCount);
					}
				}
			}

		} catch (Exception e) {
			LOG.error("Failed to upload. feed: " + strFeed + ", file: "
					+ strFile + ", pathDir: " + pathDir + ", saved: "
					+ saved + ", fileCount: " + fileCount + " Exception: "
					+ e.getMessage(), e);
		}

		try {
			LOG.info("Returning response: " + strResponse);
			out.write(strResponse);
			if (strResponse.equals("SUCCESS")) {
				DOWNLOAD_FILE_TXN_LOG.info("Status: SUCCESS, feed: "
						+ strFeed + ", file: " + strFile + ", pathDir: "
						+ pathDir + ", saved: " + saved + ", fileCount: "
						+ fileCount);
			} else {
				DOWNLOAD_FILE_TXN_LOG.info("Status: FAILURE, feed: "
						+ strFeed + ", file: " + strFile + ", pathDir: "
						+ pathDir + ", saved: " + saved + ", fileCount: "
						+ fileCount);
			}

			//out.close();
		} catch (Exception e) {
			LOG.error(
					"Unable to write response. Exception " + e.getMessage(),
					e);
			DOWNLOAD_FILE_TXN_LOG.info("Status: FAILURE, feed: " + strFeed
					+ ", file: " + strFile + ", pathDir: " + pathDir
					+ ", saved: " + saved + ", fileCount: " + fileCount);

		}
	%>
</form>