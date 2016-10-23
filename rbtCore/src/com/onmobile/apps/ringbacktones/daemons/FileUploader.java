package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberStatus;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.HttpParameters;
import com.onmobile.apps.ringbacktones.webservice.common.HttpResponse;
import com.onmobile.apps.ringbacktones.webservice.common.RBTHttpClient;

public class FileUploader extends Thread implements iRBTConstant {
    private static Logger logger = Logger.getLogger(FileUploader.class);

    private ParametersCacheManager m_rbtParamCacheManager = null;
    private RBTDBManager m_rbtDbManager = null;

    private String feedURL = null;
    private Calendar calendar = Calendar.getInstance();
    private int sleepMins = 1;
    private List<String> m_circleIDToUploadCricketFiles = new ArrayList<String>();
    private static List<String> m_modesAllowedToUploadFiles = null;
    private HashMap<String, ArrayList<String>> circleIdToPlayerUrlMap = new HashMap<String, ArrayList<String>>();
    public boolean createdSuccessfully = false;

    public static void main(String[] args) {
        Tools.init("FileUploader", true);
        FileUploader instance = new FileUploader();
		logger.info("going to upload files");
		instance.start();
    }

    public FileUploader() {
        logger.info("inside FileUploader  constructor");

        m_rbtParamCacheManager = CacheManagerUtil.getParametersCacheManager();

        m_rbtDbManager = RBTDBManager.getInstance();
        
        String modesAllowed = getParamAsString("COMMON", "MODES_ALLOWED_TO_UPLOAD_FILES", null);
        if(modesAllowed!=null){
            m_modesAllowedToUploadFiles = Arrays.asList(modesAllowed.split(","));
        }
        m_circleIDToUploadCricketFiles = Arrays.asList(getParamAsString(
                "COMMON", "CIRCLE_ID_TO_UPLOAD_FILES", "").split(","));
        logger.info("m_circleIDToUploadCricketFiles : "
                + m_circleIDToUploadCricketFiles);
        boolean sitePrefixUrlNotUrl = getParamAsBoolean("COMMON",
                "SITE_PREFIX_URL_NOT_ALLOWED", "false");
        if (m_circleIDToUploadCricketFiles != null
                && m_circleIDToUploadCricketFiles.size() > 0) {
            if (sitePrefixUrlNotUrl) {
                feedURL = getFeedUrlByNoSitePrefixConfig(
                        m_circleIDToUploadCricketFiles, sitePrefixUrlNotUrl);
            } else {
                feedURL = getFeedUrl(m_circleIDToUploadCricketFiles);
            }
            logger.info("feedURL : " + feedURL);
        }

        if (getParamAsString("COMMON", "LOCAL_DIR", null) != null
                && this.feedURL != null
                && getParamAsString("COMMON", "LOCAL_UPLOADED_DIR", null) != null) {
            createdSuccessfully = true;
        } else {
            if (getParamAsString("COMMON", "LOCAL_DIR", null) == null) {
                logger.info("localDir is null");
            }
            if (this.feedURL == null) {
                logger.info("feedURL is null");
            }
        }
        logger.info("exiting FileUploader constructor");
    }

    public void run() {
        logger.info("inside start");
        while (createdSuccessfully) {
            try {
                logger.info("going inside uploadWavFilesToPlayers");
                convert3gpToWav();
                uploadWavFilesToPlayers();
                logger.info("came out of uploadWavFilesToPlayers");
                sleep();
            } catch (Throwable t) {
                logger.error("", t);

            }
        }

    }

    private void uploadWavFilesToPlayers() {
        logger.info("inside uploadWavFilesToPlayers");
        File localFile = new File(getParamAsString("COMMON", "LOCAL_DIR", null));
        File[] files = localFile.listFiles();
        logger.info("got the list from file");
        for (int count = 0; count < files.length; count++) {
            logger.info("inside for loop for file upload");
            try {
                ArrayList<String> playerURLList = null;
                if (!files[count].isDirectory()) {
                    String file = files[count].getName();
                    String fileName = file;
                    if (fileName != null && fileName.indexOf(".wav") != -1
                            || fileName.indexOf(".WAV") != -1
                            && fileName.length() > 10) {
                        logger.info("current filename : " + file
                                + " contains .wav/.WAV and has a length>20");
                        String subID = fileName.substring(0, 10);

                        if (fileName.indexOf("-") != -1) {
                            subID = fileName
                                    .substring(0, fileName.indexOf("-"));
                        }

                        SubscriberDetail subscriberDetail = RbtServicesMgr
                                .getSubscriberDetail(new MNPContext(subID));
                        String circleId = subscriberDetail.getCircleID();
                        subID = subscriberDetail.getSubscriberID();
                                                
                        logger.info("circleId : " + circleId);
                        if (circleId != null) {
                            playerURLList = circleIdToPlayerUrlMap.get(circleId
                                    .trim());
                            if (playerURLList != null
                                    && playerURLList.size() > 0) {
                                boolean uploadedToAllURL = false;
                                String absoluteFilePath = localFile
                                        .getAbsolutePath();
                                SubscriberStatus latestSubStatus = null;
                                if(m_modesAllowedToUploadFiles!=null){
                                      latestSubStatus = RBTDBManager.getInstance().getSubscriberLatestActiveSelection(subID);
                                }
                                for (int countVar = 0; countVar < playerURLList
                                        .size(); countVar++) {
                                    String playerURL = (String) playerURLList
                                            .get(countVar);
                                    logger.info("subscriber : " + subID
                                            + " wavfileName : " + files[count]
                                            + " playerURL : " + playerURL
                                            + " circleID : " + circleId);
                                    HttpResponse httpResponse = null;
                                    try {
                                        logger.info("Setting the parameters");
                                        // Setting HttpParameters
                                        HttpParameters httpParam = new HttpParameters();
                                        httpParam.setUrl(playerURL);
                                        httpParam.setConnectionTimeout(6000);
                                        httpParam.setSoTimeout(6000);

                                        // Setting request Params
                                        HashMap<String, String> params = new HashMap<String, String>();
                                        params.put(FEED, UGCFILE);
                                        // Setting File Params
                                        HashMap<String, File> fileParams = new HashMap<String, File>();
                                        fileParams.put(file, files[count]);
                                        httpResponse = RBTHttpClient
                                                .makeRequestByPost(httpParam,
                                                        params, fileParams);
                                        // fileResponse=RBTHTTPProcessing.postFile(httpParam,
                                        // params, arrfile);
                                    } catch (Exception e) {
                                        logger.error("", e);
                                        continue;
                                    }
                                    logger.info("RBT:: url -> " + playerURL
                                            + ", Response -> " + httpResponse);
                                    ;
                                    if (httpResponse != null
                                            && httpResponse.getResponse()
                                                    .indexOf("SUCCESS") != -1) {
                                        logger.info("going to move file "
                                                + file
                                                + " from "
                                                + absoluteFilePath
                                                + " to "
                                                + new File(getParamAsString(
                                                        "COMMON",
                                                        "LOCAL_UPLOADED_DIR",
                                                        null))
                                                        .getAbsolutePath());
                                        uploadedToAllURL = true;
                                    } else {
                                        uploadedToAllURL = false;
                                    }
                                	
                                	if(m_modesAllowedToUploadFiles!=null && latestSubStatus!=null &&
                                			m_modesAllowedToUploadFiles.contains(latestSubStatus.selectedBy())){
                                		   break;
                                	}
                                }

                                if (uploadedToAllURL) {
                                    logger.info(" uploadedToAllURL is true");
                                    boolean transferDone = false;
                                    String fileNameTemp = file.substring(file
                                            .lastIndexOf(File.separator) + 1);

                                    File fileTemp = new File(getParamAsString(
                                            "COMMON", "LOCAL_UPLOADED_DIR",
                                            null)
                                            + File.separator + fileNameTemp);
                                    if(fileTemp.exists())
                                    	fileTemp.delete();
                                    if (!fileTemp.exists()) {
                                        logger
                                                .info("fileNameTemp doesnt exist");
                                        File fileDest = new File(
                                                getParamAsString("COMMON",
                                                        "LOCAL_UPLOADED_DIR",
                                                        null));
                                        if (!fileDest.isDirectory()) {
                                            logger
                                                    .info("file "
                                                            + fileDest
                                                                    .getAbsolutePath()
                                                            + " doesnt exist. Ging to create this dir");
                                            fileDest.mkdir();
                                        }
                                        fileTemp = new File(getParamAsString(
                                                "COMMON", "LOCAL_UPLOADED_DIR",
                                                null)
                                                + File.separator + fileNameTemp);
                                        transferDone = files[count]
                                                .renameTo(fileTemp);
                                        if (!transferDone) {
                                            try {
                                                Tools
                                                        .copyFile(
                                                                files[count]
                                                                        .getAbsolutePath(),
                                                                fileTemp
                                                                        .getAbsolutePath());
                                                transferDone = true;
                                            } catch (IOException e) {
                                                logger
                                                        .info("copy of file "
                                                                + file
                                                                + " from  "
                                                                + absoluteFilePath
                                                                + " to "
                                                                + fileTemp
                                                                        .getAbsolutePath()
                                                                + " FAILED...caught exception");
                                            }
                                            if (transferDone) {
                                                logger
                                                        .info("transferDone is true");
                                                boolean delStatus = files[count]
                                                        .delete();
                                                if (delStatus)
                                                    logger
                                                            .info("transfer of file "
                                                                    + file
                                                                    + " from  "
                                                                    + absoluteFilePath
                                                                    + " to "
                                                                    + fileTemp
                                                                            .getAbsolutePath()
                                                                    + " SUCCESS");
                                                else
                                                    logger
                                                            .info("transfer of file "
                                                                    + file
                                                                    + " from  "
                                                                    + absoluteFilePath
                                                                    + " to "
                                                                    + fileTemp
                                                                            .getAbsolutePath()
                                                                    + " FAILED");
                                            } else {
                                                logger
                                                        .info("transferDone is false");
                                                logger
                                                        .info("transfer of file "
                                                                + file
                                                                + " from  "
                                                                + absoluteFilePath
                                                                + " to "
                                                                + new File(
                                                                        getParamAsString(
                                                                                "COMMON",
                                                                                "LOCAL_UPLOADED_DIR",
                                                                                null))
                                                                        .getAbsolutePath()
                                                                + " FAILED");
                                            }
                                        } else {

                                            logger
                                                    .info(" file "
                                                            + file
                                                            + " uploaded successfully ");
                                        }
                                    } else {
                                        try {
                                            files[count].delete();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                        logger.info(" file " + file
                                                + " already exist from ");
                                    }
                                    if (transferDone) {
                                        logger.info("transfer of file "
                                                + file
                                                + " from  "
                                                + absoluteFilePath
                                                + " to "
                                                + new File(getParamAsString(
                                                        "COMMON",
                                                        "LOCAL_UPLOADED_DIR",
                                                        null))
                                                        .getAbsolutePath()
                                                + " SUCCESSFUL");
                                    } else {
                                        logger.info("transfer of file "
                                                + file
                                                + " from  "
                                                + absoluteFilePath
                                                + " to "
                                                + new File(getParamAsString(
                                                        "COMMON",
                                                        "LOCAL_UPLOADED_DIR",
                                                        null))
                                                        .getAbsolutePath()
                                                + " FAILED");
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {
                logger.error("", t);
            }
        }
        logger.info("exiting uploadWavFilesToPlayers");
    }

    private void populateSiteFeedUrl(String temp, String circleId,
            StringBuffer feedUrlBuff) {
        if (circleId == null || circleId.length() == 0 || temp == null
                || temp.length() == 0) {
            return;
        }
        if (feedUrlBuff == null) {
            feedUrlBuff = new StringBuffer();
        }
        ArrayList<String> urlList = null;
        logger.info("RBT:: url is not null");
        temp = temp.trim();
        if (temp != null && temp.indexOf(",") != -1) {
            StringTokenizer st = new StringTokenizer(temp, ",");
            String tempStr = null;
            while (st.hasMoreElements()) {
                String tempUrl = st.nextToken();
                if (tempUrl != null) {
                    if (urlList == null) {
                        urlList = new ArrayList<String>();
                    }
                    tempUrl = tempUrl.substring(0, tempUrl.lastIndexOf("/"));
                    logger.info("RBT:: temp value is " + tempUrl);
                    tempUrl = tempUrl
                            + "/RecordOwnDownloader/rbt_downloadFile.jsp?";
                    logger.info("RBT:: temp value is " + tempUrl);
                    if (tempStr == null) {
                        tempStr = tempUrl;
                    } else {
                        tempStr = tempStr + ";" + tempUrl;
                    }
                    urlList.add(tempUrl);
                }
            }
            if (urlList != null && urlList.size() > 0) {
                if (circleId != null) {
                    circleIdToPlayerUrlMap.put(circleId.trim(), urlList);
                    if (!(feedUrlBuff.length() > 0)) {
                        feedUrlBuff.append(tempStr);
                        logger.info("RBT:: FeedUrl is "
                                + feedUrlBuff.toString());
                    } else {
                        feedUrlBuff.append("," + tempStr);
                        logger.info("RBT:: FeedUrl is "
                                + feedUrlBuff.toString());
                    }
                }
            }
        } else {
            temp = temp.substring(0, temp.lastIndexOf("/"));
            logger.info("RBT:: temp value is " + temp);
            temp = temp + "/RecordOwnDownloader/rbt_downloadFile.jsp?";
            logger.info("RBT:: temp value is " + temp);
            if (circleId != null && temp != null) {
                urlList = new ArrayList<String>();
                urlList.add(temp);
                circleIdToPlayerUrlMap.put(circleId.trim(), urlList);
                if (!(feedUrlBuff.length() > 0)) {
                    feedUrlBuff.append(temp);
                    logger.info("RBT:: FeedUrl is " + feedUrlBuff.toString());
                } else {
                    feedUrlBuff.append("," + temp);
                    logger.info("RBT:: FeedUrl is " + feedUrlBuff.toString());
                }
            }
        }
    }

    private String getFeedUrlByNoSitePrefixConfig(List<String> arrSites,
            boolean sitePrefixUrlNotUrl) {
        logger.info("RBT:: inside getFeedUrlByNoSitePrefixConfig");
        if (sitePrefixUrlNotUrl) {
            logger.info("RBT:: sitePrefixUrlNotUrl==" + sitePrefixUrlNotUrl);
        }
        List<SitePrefix> prefix = CacheManagerUtil.getSitePrefixCacheManager()
                .getAllSitePrefix();
        if (prefix == null || prefix.size() == 0) {
            return null;
        }
        StringBuffer siteFeedUrl = new StringBuffer();
        if (arrSites != null && arrSites.size() > 0) {
            for (int count = 0; count < prefix.size(); count++) {
                logger.info("RBT:: count " + count);
                logger
                        .info("RBT:: looking for circle id in site prefix table -> "
                                + prefix.get(count).getCircleID());
                if (prefix.get(count).getCircleID() != null
                        && arrSites != null
                        && arrSites.contains(prefix.get(count).getCircleID()
                                .trim())) {
                    logger.info("RBT:: circle id "
                            + prefix.get(count).getCircleID()
                            + "exist in sites array");
                    String circleIdToUpload = prefix.get(count).getCircleID();
                    if (circleIdToUpload != null
                            && circleIdToUpload.length() > 0) {
                        circleIdToUpload = circleIdToUpload.trim();
                        String siteUploadUrl = getParamAsString("COMMON",
                                "CIRCLE_ID_TO_UPLOAD_FILES_"
                                        + circleIdToUpload.toUpperCase(), "");
                        logger.info("RBT:: url==" + siteUploadUrl);
                        if (siteUploadUrl != null && siteUploadUrl.length() > 0) {

                            populateSiteFeedUrl(siteUploadUrl,
                                    circleIdToUpload, siteFeedUrl);
                            logger.info("RBT:: feedurl == "
                                    + siteFeedUrl.toString());
                        }
                    }
                }
            }
        }
        if (siteFeedUrl != null && siteFeedUrl.length() > 0) {
            return siteFeedUrl.toString();
        }
        return null;
    }

    private String getFeedUrl(List<String> arrSites) {
        logger.info("RBT:: inside getFeedUrl");
        List<SitePrefix> prefix = CacheManagerUtil.getSitePrefixCacheManager()
                .getAllSitePrefix();
        if (prefix == null || prefix.size() == 0) {
            return null;
        }
        String temp = null;
        String feedUrl = null;
        StringBuffer siteFeedUrl = new StringBuffer();
        for (int i = 0; i < prefix.size(); i++) {
            logger.info("RBT:: count " + i);
            logger.info("RBT:: looking for circle id in site prefix table -> "
                    + prefix.get(i).getCircleID());
            if (prefix.get(i).getCircleID() != null && arrSites != null
                    && arrSites.contains(prefix.get(i).getCircleID().trim())) {
                String circleIdToUpload = prefix.get(i).getCircleID();
                logger.info("RBT:: circle id " + circleIdToUpload
                        + "exist in sites array");
                temp = prefix.get(i).getPlayerUrl();
                logger.info("RBT:: url==" + temp);
                if (temp != null) {

                    populateSiteFeedUrl(temp, circleIdToUpload, siteFeedUrl);
                    logger.info("RBT:: feedurl == " + siteFeedUrl.toString());
                    if (siteFeedUrl != null && siteFeedUrl.length() > 0) {
                        feedUrl = siteFeedUrl.toString();
                    }
                }
            }
        }
        logger.info("RBT:: inside getFeedUrl with feedurl==" + feedUrl);
        return feedUrl;
    }

    private void sleep() {
        long nexttime = getnexttime(sleepMins);
        calendar = Calendar.getInstance();
        calendar.setTime(new java.util.Date(nexttime));
        logger.info("RBT::Sleeping till " + calendar.getTime()
                + " for next processing !!!!!");
        long diff = (calendar.getTime().getTime() - Calendar.getInstance()
                .getTime().getTime());
        try {
            if (diff > 0)
                Thread.sleep(diff);
            else
                Thread.sleep(sleepMins * 60 * 1000);
        } catch (InterruptedException e) {
            logger.error("", e);
        }
    }

    public long getnexttime(int sleep) {
        Calendar now = Calendar.getInstance();
        now.setTime(new java.util.Date(System.currentTimeMillis()));
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);

        long nexttime = now.getTime().getTime();
        while (nexttime < System.currentTimeMillis()) {
            nexttime = nexttime + (sleep * 60 * 1000);
        }

        logger.info("RBT::getnexttime" + new Date(nexttime));
        return nexttime;
    }

    private boolean getParamAsBoolean(String type, String param,
            String defualtVal) {
        boolean returnBoolean = false;
        if (defualtVal == null || defualtVal.length() == 0) {
            defualtVal = "false";
        } else {
            defualtVal = defualtVal.trim();
            if (!defualtVal.equalsIgnoreCase("true")
                    && !defualtVal.equalsIgnoreCase("false")) {
                defualtVal = "false";
            }
        }
        try {
            String tempVal = m_rbtParamCacheManager.getParameter(type, param,
                    defualtVal).getValue();
            if (tempVal != null && tempVal.length() > 0) {
                if (tempVal.equalsIgnoreCase("true")) {
                    returnBoolean = true;
                } else {
                    returnBoolean = false;
                }
            }
        } catch (Exception e) {
            logger.info("Unable to get param ->" + param + "  type ->" + type);
            return returnBoolean;
        }
        return returnBoolean;
    }

    private String getParamAsString(String type, String param, String defualtVal) {
        try {
            return m_rbtParamCacheManager.getParameter(type, param, defualtVal)
                    .getValue();
        } catch (Exception e) {
            logger.info("Unable to get param ->" + param + "  type ->" + type);
            return defualtVal;
        }
    }

    private void convert3gpToWav() {
        logger.info("inside convert3gpToWav()");
        File localFile = new File(getParamAsString("COMMON",
                "LOCAL_DIR_FOR_3GP", null));

        if (localFile == null)
            return;

        File[] files = localFile.listFiles();

        if (files == null || files.length == 0) {
            logger.info("There are no files in the source folder to convert "
                    + localFile.getAbsolutePath());
            return;
        }

        for (int i = 0; i < files.length; i++) {
            String fileName = files[i].getName();
            logger.info("File name is : " + files[i]);

            if (fileName.endsWith(".3gp")) {
                try {
                    processCommand(files[i]);
                    files[i].delete();
                } catch (IOException e) {
					logger.error("The exception message ", 
                             e);
                    logger.error("The file does not exist "
                            + files[i].getName());
                } catch (InterruptedException e) {
                    logger.error("InterruptedException " + e);
                }
            }
        }

    }

    private void processCommand(File inputFile) throws IOException,
            InterruptedException {

        int lastDotIndex = inputFile.getName().lastIndexOf(".");
        String outputFileName = inputFile.getName().substring(0, lastDotIndex)
                + ".wav";

        String ffmegPath = getParamAsString("COMMON", "DIR_FOR_FFMPEG", null);
        if(ffmegPath == null) {
        	ffmegPath = "";
        }
        logger.info("the ffmpeg path is " + ffmegPath);
        String command = ffmegPath+"ffmpeg -i " + inputFile.getAbsolutePath() + " -y "
                + inputFile.getParent() + File.separator + outputFileName;
        logger.info("the ffmpeg command is " + command);

        Process p = Runtime.getRuntime().exec(command);
		logger.info("process" + p);
        InputStream is = p.getErrorStream();
		logger.info("is InputStream" + is);
        byte[] buffer = new byte[1024];
        StringBuffer sb = new StringBuffer();
        int byteCount = 0;
        do {
            byteCount = is.read(buffer);
            sb.append(buffer);
            buffer = null;
            buffer = new byte[1024];
        } while (byteCount > 0);
        logger.info("read byte" + sb);
        File soxCommandInputFile = new File(inputFile.getParent(),
                outputFileName);
		 logger.info("soxCommandInputFile" + soxCommandInputFile);
        processSoxCommand(soxCommandInputFile);
        soxCommandInputFile.delete();
    }

    private void processSoxCommand(File inputFile) throws IOException,
            InterruptedException {

        int lastIndex = inputFile.getName().lastIndexOf(".");
        String wavOutFile = inputFile.getName().substring(0, lastIndex)
                + ".wav";

        File destFolder = new File(getParamAsString("COMMON",
                "DEST_FOR_CONVERTED_WAVFILES", null)
                + File.separator + wavOutFile);

        String soxPath = getParamAsString("COMMON", "SOX_PATH", null);
        String soxCommand = soxPath + "sox " + inputFile.getAbsolutePath()
                + " -1 -c1 -r 8000 -U " + destFolder;

        logger.info("The sox command is " + soxCommand);

        Process p = Runtime.getRuntime().exec(soxCommand);
        InputStream is = p.getErrorStream();
        byte[] buffer = new byte[1024];
        StringBuffer sb = new StringBuffer();
        int byteCount = 0;
        do {
            byteCount = is.read(buffer);
            sb.append(buffer);
            buffer = null;
            buffer = new byte[1024];
        } while (byteCount > 0);

        File copyingDestFile = new File(getParamAsString("COMMON", "LOCAL_DIR",
                null)
                + wavOutFile);
        logger.info("Copying .wav files-> Souce path is "
                + destFolder.getAbsolutePath() + " Destiantion path is "
                + copyingDestFile);
        Tools.copyFile(destFolder.getAbsolutePath(), copyingDestFile
                .getAbsolutePath());
        destFolder.delete();
    }

}