package com.onmobile.apps.ringbacktones.daemons;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTDeploymentFinder;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.HttpParameters;
import com.onmobile.apps.ringbacktones.common.RBTException;
import com.onmobile.apps.ringbacktones.common.WriteDailyTrans;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Subscriber;
import com.onmobile.apps.ringbacktones.content.SubscriberAnnouncements;
import com.onmobile.apps.ringbacktones.content.database.DBUtility;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.genericcache.beans.SitePrefix;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.cache.RBTCacheManager;
import com.onmobile.apps.ringbacktones.services.mgr.RbtServicesMgr;
import com.onmobile.apps.ringbacktones.services.msisdninfo.MNPContext;
import com.onmobile.apps.ringbacktones.services.msisdninfo.SubscriberDetail;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;

/**
 * This is a Daemon Class and process all the recored from
 * RBT_SUBSCRIBER_ANNOUNCEMENTS table
 * 
 * @author Sreekar
 * @version 1.0
 */
public class RBTAnnouncementDaemon extends Thread implements iRBTConstant,
		WebServiceConstants {
	private static Logger logger = Logger
			.getLogger(RBTAnnouncementDaemon.class);

	protected static final String THREAD_TYPE_HLR_ACT = "ANN-HLR-ACT";
	protected static final String THREAD_TYPE_HLR_DCT = "ANN-HLR-DCT";
	protected static final String THREAD_TYPE_RBT_DCT = "ANN-RBT-DCT";
	protected static final String THREAD_TYPE_PLAYER_ACT = "ANN-PLR-ACT";
	protected static final String THREAD_TYPE_PLAYER_DCT = "ANN-PLR-DCT";

	private RBTDaemonManager _mainDaemonThread;
	private RBTDBManager _rbtDBManager = null;
	private ParametersCacheManager _paramCacheManager = null;
	private int _fetchSize = 5000;

	private HttpClient m_smHTTPClient = null;
	private HttpClient m_playerHTTPClient = null;
	URLCodec m_urlEncoder = new URLCodec();
	private WriteDailyTrans m_writeTrans = null;
	private static final SimpleDateFormat logDateFormatter = new SimpleDateFormat("yyyyMMddHHmmss");
	private static final SimpleDateFormat playerDateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private HashMap<String, RBTAnnouncementDaemonThread> _actTP = new HashMap<String, RBTAnnouncementDaemonThread>();
	private HashMap<String, RBTAnnouncementDaemonThread> _actPlayerTP = new HashMap<String, RBTAnnouncementDaemonThread>();
	private HashMap<String, RBTAnnouncementDaemonThread> _dctRBT = new HashMap<String, RBTAnnouncementDaemonThread>();
	private HashMap<String, RBTAnnouncementDaemonThread> _dctTP = new HashMap<String, RBTAnnouncementDaemonThread>();
	private HashMap<String, RBTAnnouncementDaemonThread> _dctPlayerTP = new HashMap<String, RBTAnnouncementDaemonThread>();

	private AnnouncementProcessList _actList = new AnnouncementProcessList("HLR-ACT");
	private AnnouncementProcessList _actPlayerList = new AnnouncementProcessList("PLR-ACT");
	private AnnouncementProcessList _dctList = new AnnouncementProcessList("HLR-DCT");
	private AnnouncementProcessList _dctPlayerList = new AnnouncementProcessList("pLR-DCT");
	private AnnouncementProcessList _dctRBTList = new AnnouncementProcessList("RBT-DCT");


	private String _sdrWorkingDir = null;

	private static final DateFormat _annoucementLoggerFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");
	private static final String ACTION_ANNOUNCEMENT_ACTIVATION = "ACT";
	private static final String ACTION_ANNOUNCEMENT_DEACTIVATION = "DCT";

	protected RBTAnnouncementDaemon(RBTDaemonManager mainDaemonThread) 
	{
		if (initParams())
			_mainDaemonThread = mainDaemonThread;
	}

	private boolean initParams() {
		logger.info("RBTAnn:: inside....");
		try {
			setName("RBTAnnouncementDaemon");
			_paramCacheManager = CacheManagerUtil.getParametersCacheManager();
			_rbtDBManager = RBTDBManager.getInstance();

			if (!RBTDeploymentFinder.isRRBTSystem()) {
				logger.info("RBTAnn::This is not RRBT system. Need not start Announcement Daemon");
				return false;
			}

			_sdrWorkingDir = _paramCacheManager.getParameterValue(DAEMON,"SUBMGR_SDR_WORKING_DIR", ".")+ File.separator + "AnnoucementDaemon";
			ArrayList<String> headers = new ArrayList<String>();
			headers.add("EVENT_TYPE");
			headers.add("SUBSCRIBERID");
			headers.add("REQUESTED_TIMESTAMP");
			headers.add("RESPONSE_DELAYINMS");
			headers.add("REQUEST_DETAIL");
			headers.add("RESPONSE_DETAIL");
			m_writeTrans = new WriteDailyTrans(_sdrWorkingDir,"ANNOUNCEMENT_REQUEST", headers);
			_fetchSize = getParamAsInt(DAEMON, "FETCH_SIZE", "5000");

			MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
			connectionManager.getParams().setStaleCheckingEnabled(true);
			connectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
			connectionManager.getParams().setMaxTotalConnections(20);
			connectionManager.getParams().setConnectionTimeout(getParamAsInt(DAEMON, "SMDAEMON_TIMEOUT", "6") * 1000);

			m_smHTTPClient = new HttpClient(connectionManager);
			m_smHTTPClient.getParams().setSoTimeout(10 * 1000);

			DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(0, false);
			m_smHTTPClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);

			MultiThreadedHttpConnectionManager playerConnectionManager = new MultiThreadedHttpConnectionManager();
			playerConnectionManager.getParams().setStaleCheckingEnabled(true);
			playerConnectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
			playerConnectionManager.getParams().setMaxTotalConnections(20);
			playerConnectionManager.getParams().setConnectionTimeout(getParamAsInt(DAEMON, "PLAYER_TIMEOUT", "6") * 1000);

			m_playerHTTPClient = new HttpClient(playerConnectionManager);
			m_playerHTTPClient.getParams().setSoTimeout(10 * 1000);

			DefaultHttpMethodRetryHandler playerRetryhandler = new DefaultHttpMethodRetryHandler(0, false);
			m_playerHTTPClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, playerRetryhandler);
		} catch (Exception e) {
			logger.error("", e);
		}
		logger.info("RBTAnn::successfully inited all params");
		return true;
	}

	private boolean makeThreads() {
		try {
			logger.info("RBTAnn::creating hlr activation threads");
			int actTPCount = getParamAsInt(DAEMON,"ANNOUNCEMENT_THREAD_POOL_SIZE_HLR_ACTIVATION", "1");
			for (int i = 1; i <= actTPCount; i++) {
				RBTAnnouncementDaemonThread thread = new RBTAnnouncementDaemonThread(THREAD_TYPE_HLR_ACT, this);
				thread.setName(THREAD_TYPE_HLR_ACT + "-" + i);
				thread.start();
				_actTP.put(thread.getName(), thread);
			}

			logger.info("RBTAnn::creating player activation threads");
			int actPlayerTPCount = getParamAsInt(DAEMON,"ANNOUNCEMENT_THREAD_POOL_SIZE_PLAYER_ACTIVATION", "1");
			for (int i = 1; i <= actPlayerTPCount; i++) {
				RBTAnnouncementDaemonThread thread = new RBTAnnouncementDaemonThread(THREAD_TYPE_PLAYER_ACT, this);
				thread.setName(THREAD_TYPE_PLAYER_ACT + "-" + i);
				thread.start();
				_actPlayerTP.put(thread.getName(), thread);
			}

			logger.info("RBTAnn::creating announcement deactivation threads");
			int dctAnnCount = getParamAsInt(DAEMON,"ANNOUNCEMENT_THREAD_POOL_SIZE_RBT_DEACTIVATION", "1");
			for (int i = 1; i <= dctAnnCount; i++) {
				RBTAnnouncementDaemonThread thread = new RBTAnnouncementDaemonThread(THREAD_TYPE_RBT_DCT, this);
				thread.setName(THREAD_TYPE_RBT_DCT + "-" + i);
				thread.start();
				_dctRBT.put(thread.getName(), thread);
			}

			logger.info("RBTAnn::creating hlr deactivation threads");
			int dctTPCount = getParamAsInt(DAEMON,"ANNOUNCEMENT_THREAD_POOL_SIZE_HLR_DEACTIVATION", "1");
			for (int i = 1; i <= dctTPCount; i++) {
				RBTAnnouncementDaemonThread thread = new RBTAnnouncementDaemonThread(THREAD_TYPE_HLR_DCT, this);
				thread.setName(THREAD_TYPE_HLR_DCT + "-" + i);
				thread.start();
				_dctTP.put(thread.getName(), thread);
			}

			logger.info("RBTAnn::creating player deactivation threads");
			int dctPlayerTPCount = getParamAsInt(DAEMON,"ANNOUNCEMENT_THREAD_POOL_SIZE_PLAYER_DEACTIVATION", "1");
			for (int i = 1; i <= dctPlayerTPCount; i++) {
				RBTAnnouncementDaemonThread thread = new RBTAnnouncementDaemonThread(THREAD_TYPE_PLAYER_DCT, this);
				thread.setName(THREAD_TYPE_PLAYER_DCT + "-" + i);
				thread.start();
				_dctPlayerTP.put(thread.getName(), thread);
			}
		} catch (Exception e) {
			logger.error("", e);
			return false;
		}

		return true;
	}

	private void checkThreads() {
		Iterator<String> itr = _actTP.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			RBTAnnouncementDaemonThread thread = _actTP.get(key);
			if (thread == null || !thread.isAlive())
				logger.info("RBTAnn::thread->" + key + " is not alive");
		}

		itr = _actPlayerTP.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			RBTAnnouncementDaemonThread thread = _actPlayerTP.get(key);
			if (thread == null || !thread.isAlive())
				logger.info("RBTAnn::thread->" + key + " is not alive");
		}

		itr = _dctTP.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			RBTAnnouncementDaemonThread thread = _dctTP.get(key);
			if (thread == null || !thread.isAlive())
				logger.info("RBTAnn::thread->" + key + " is not alive");
		}
		
		
		itr = _dctRBT.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			RBTAnnouncementDaemonThread thread = _dctRBT.get(key);
			if (thread == null || !thread.isAlive())
				logger.info("RBTAnn::thread->" + key + " is not alive");
		}
		
		itr = _dctPlayerTP.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			RBTAnnouncementDaemonThread thread = _dctPlayerTP.get(key);
			if (thread == null || !thread.isAlive())
				logger.info("RBTAnn::thread->" + key + " is not alive");
		}
	}

	private void updateLists() {
		if (_actList.size() > (1.5) * _fetchSize)
			logger.info("RBTAnn::hlr activation list is large not trying to add to the list. Current size->"
							+ _actList.size());
		else {
			
			if(_actList.size() == 0) {
				_actList.updateSequenceId(1l);
			}
			
			List<SubscriberAnnouncements> listToAdd = _rbtDBManager.getAnnouncementSubscribers(ANNOUNCEMENT_TO_BE_ACTIVED,_actList.sequenceID);
			_actList.addToList(listToAdd);
		}

		if (_actPlayerList.size() > (1.5) * _fetchSize)
			logger.info("RBTAnn::player activation list is large not trying to add to the list. Current size->"+ _actPlayerList.size());
		else {
			
			if(_actPlayerList.size() == 0) {
				_actPlayerList.updateSequenceId(1l);
			}
			
			List<SubscriberAnnouncements> listToAdd = _rbtDBManager.getAnnouncementSubscribers(ANNOUNCEMENT_TO_BE_ACTIVED_PLAYER,_actPlayerList.sequenceID);
			_actPlayerList.addToList(listToAdd);
		}
		
		if (_dctRBTList.size() ==0 )
		{
			List<SubscriberAnnouncements> listToAdd = _rbtDBManager.getExpiredAnnouncementSubscribers(_fetchSize+"");
			_dctRBTList.addToList(listToAdd);
		}

		if (_dctList.size() > (1.5) * _fetchSize)
			logger.info("RBTAnn::hlr deactivation list is large not trying to add to the list. Current size->"+ _dctList.size());
		else {
			
			if(_dctList.size() == 0) {
				_dctList.updateSequenceId(1l);
			}
			
			List<SubscriberAnnouncements> listToAdd = _rbtDBManager.getAnnouncementSubscribers(ANNOUNCEMENT_TO_BE_DEACTIVED,_dctList.sequenceID);
			_dctList.addToList(listToAdd);
		}

		if (_dctPlayerList.size() > (1.5) * _fetchSize)
			logger.info("RBTAnn::player deactivation list is large not trying to add to the list. Current size->"+ _dctPlayerList.size());
		else {
			
			if(_dctPlayerList.size() == 0) {
				_dctPlayerList.updateSequenceId(1l);
			}
			
			List<SubscriberAnnouncements> listToAdd = _rbtDBManager.getAnnouncementSubscribers(ANNOUNCEMENT_TO_BE_DEACTIVED_PLAYER,_dctPlayerList.sequenceID);
			_dctPlayerList.addToList(listToAdd);
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

	private void sleep() {
		long nexttime = getnexttime(getParamAsInt(DAEMON,"ANNOUNCEMENT_DAEMON_SLEEP_MINS", "5"));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new java.util.Date(nexttime));
		logger.info("RBTAnn::Sleeping till " + calendar.getTime()+ " for next processing !!!!!");
		long diff = (calendar.getTime().getTime() - Calendar.getInstance()
				.getTime().getTime());
		try {
			if (diff > 0)
				Thread.sleep(diff);
			else
				Thread.sleep(getParamAsInt(DAEMON,"ANNOUNCEMENT_DAEMON_SLEEP_MINS", "5") * 60 * 1000);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}

	public void run() {
		logger.info("RBTAnn::announcement daemon starting processing......");
		if (!makeThreads()) {
			logger.info("RBTAnn::stopping Announcement daemon as not able to create child threads");
			return;
		}

		while (_mainDaemonThread != null && _mainDaemonThread.isAlive()) {
			checkThreads();
			updateLists();
			logger.info("calling update lists ");
			sleep();
		}
	}

	private int getParamAsInt(String type, String paramName, String defaultVal) {
		return Integer.parseInt(_paramCacheManager.getParameterValue(type,
				paramName, defaultVal));
	}

	class AnnouncementProcessList {
		private String _listType;
		List<SubscriberAnnouncements> _list = new ArrayList<SubscriberAnnouncements>();
		long sequenceID = -1;

		AnnouncementProcessList(String listType) {
			_listType = listType;
		}

		void updateSequenceId(long sequenceID) {
			if (sequenceID > 0 && sequenceID < this.sequenceID)
				this.sequenceID = sequenceID - 1;
		}

		boolean addToList(List<SubscriberAnnouncements> listToAdd) {
			if (listToAdd == null || listToAdd.size() <= 0)
				return false;

			if ((_list.size() + listToAdd.size()) > 2 * _fetchSize) {
				logger.info("RBTAnn::still much more to process not adding to the list");
				return false;
			}

			synchronized (this) {
				_list.addAll(listToAdd);
				sequenceID = _list.get(_list.size() - 1).sequenceId();
			}

			synchronized (this) {
				logger.info("RBTAnn::notifying......");
				this.notifyAll();
				logger.info("RBTAnn::notified :)");
			}

			logger.info("RBTAnn::updated " + _listType+ " list total records to process->" + _list.size());
			return true;
		}

		synchronized SubscriberAnnouncements getRecordToProcess()
				throws InterruptedException {
			if (_list.size() <= 0) {
				synchronized (this) {
					logger.info("RBTAnn::going to wait....");
					this.wait();
					logger.info("RBTAnn::after wait......");
				}
				return null;
			}
			SubscriberAnnouncements record = _list.get(0);
			_list.remove(0);
			return record;
		}

		int size() {
			return _list.size();
		}
	}

	private boolean smHLRActivation(String subscriberID ,int clipId) {
		boolean response = false;
		String url = _paramCacheManager.getParameterValue(DAEMON,"SM_HLR_ACTIVATION_URL", "");
		if (url == null || url.equals("")) {
			logger.info("RBTAnn::hlr activation url not configured");
			return false;
		}

		url = url.replaceAll("%SUBSCRIBER_ID%", subscriberID);
		url = url.replaceAll("%CLIP_ID%", clipId+"");
		String responseStr = makeHttpRequest(url, subscriberID,THREAD_TYPE_HLR_ACT, m_smHTTPClient);
		StringTokenizer stk = new StringTokenizer(responseStr, "|");
		if (stk.hasMoreTokens() && stk.nextToken().equalsIgnoreCase("success"))
			response = true;

		return response;
	}

	private boolean smHLRDeactivation(String subscriberID) {
		boolean response = false;
		String url = _paramCacheManager.getParameterValue(DAEMON,"SM_HLR_DEACTIVATION_URL", "");
		if (url == null || url.equals("")) {
			logger.info("RBTAnn::hlr deactivation url not configured");
			return false;
		}

		url = url.replaceAll("%SUBSCRIBER_ID%", subscriberID);
		String responseStr = makeHttpRequest(url, subscriberID,THREAD_TYPE_HLR_DCT, m_smHTTPClient);

		StringTokenizer stk = new StringTokenizer(responseStr, "|");
		if (stk.hasMoreTokens() && stk.nextToken().equalsIgnoreCase("success"))
			response = true;

		return response;
	}

	private void logAnnouncement(SubscriberAnnouncements announcement,
			String action) {
		Logger annLogger = Logger.getLogger("announcement");
		if (annLogger == null) {
			logger.error("RBTAnn::couldn't find announcement logger");
			return;
		}
		int clipId = announcement.clipId();
		Clip clip = RBTCacheManager.getInstance().getClip(clipId);

		String subscriberId = announcement.subscriberId();

		String circleId = null;
		Subscriber subscriber = RBTDBManager.getInstance().getSubscriber(subscriberId);
		if (subscriber != null) {
			circleId = subscriber.circleID();
		} else {
			MNPContext context = new MNPContext(subscriberId, "Announcement");
			SubscriberDetail subDetail = RbtServicesMgr.getSubscriberDetail(context);
			circleId = subDetail.getCircleID();
		}
		logger.info("RBTAnn::Logging with subscriber Id=" + subscriberId
				+ ", circleId=" + circleId + ", clipName=" + clip.getClipName()
				+ "and  frequency=" + announcement.frequency() + " for "
				+ action + " action at  timestamp="
				+ _annoucementLoggerFormat.format(new Date()));
		annLogger.info(subscriberId + "," + circleId + "," + clip.getClipName()+ "," + announcement.frequency() + "," + action + ","+ _annoucementLoggerFormat.format(new Date()));
	}

	private void processHLRActivation(SubscriberAnnouncements announcement) {
		String subscriberID = announcement.subscriberId();
		Subscriber subscriber = _rbtDBManager.getSubscriber(subscriberID);
		if (subscriber == null || subscriber.subYes().equals(STATE_DEACTIVATED)|| subscriber.subYes().equals(STATE_DEACTIVATED_INIT)|| subscriber.subYes().equals(STATE_SUSPENDED)) {
			if (smHLRActivation(subscriberID,announcement.clipId())) {
				Parameters pcaHLRTickparam = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON,"PCA_HLR_TICK_ASYNC", "TRUE");
				if (pcaHLRTickparam != null&& pcaHLRTickparam.getValue().equalsIgnoreCase("TRUE")) {
					announcement.setStatus(ANNOUNCEMENT_ACTIVATION_PENDING);
				} else {
					announcement.setStatus(ANNOUNCEMENT_TO_BE_ACTIVED_PLAYER);
					_actPlayerList.updateSequenceId(announcement.sequenceId());
				}

				_rbtDBManager.updateAnnouncement(announcement);
				logAnnouncement(announcement, ACTION_ANNOUNCEMENT_ACTIVATION);
			} else {
				logger.info("RBTAnn::couldn't hlr activate subscriber "+ subscriberID + ", will try in next loop");
				_actList.updateSequenceId(announcement.sequenceId());
			}
		} else if (subscriber.subYes().equals(STATE_TO_BE_DEACTIVATED)|| subscriber.subYes().equals(STATE_DEACTIVATION_PENDING)|| subscriber.subYes().equals(STATE_DEACTIVATION_ERROR)) {
			logger.info("RBTAnn::updating status in announcement table to base deactivation pending");
			_rbtDBManager.updateAnnouncementsBaseCallbackPending(subscriberID,announcement.clipId());
		} else {
			logger.info("RBTAnn:: updating subscriber table with PCA in extra info");
			_rbtDBManager.updateExtraInfoAndPlayerStatus(subscriber,EXTRA_INFO_PCA_FLAG, "TRUE", "A");
			_rbtDBManager.updateAnnouncementToActive(announcement.subscriberId(), announcement.clipId());
			logAnnouncement(announcement, ACTION_ANNOUNCEMENT_ACTIVATION);
		}
	}

	private String createPlayerXML(SubscriberAnnouncements announcement) {
		
		
		Clip clip = RBTCacheManager.getInstance().getClip(announcement.clipId());
		
		// appending subscriber tag
		StringBuilder sb = new StringBuilder("<sub_selections><selections rbt_type=\"");
		sb.append(USER_TYPE_RRBT);
		sb.append("\"");
		sb.append(" pca_frequency=\"");
		sb.append(announcement.frequency());
		sb.append("\"");
		sb.append(" pca_file=\"");
		sb.append(clip.getClipRbtWavFile());
		sb.append("\"");
		sb.append(" >");

		// appending selection tag
//		sb.append("<selection ");
//		sb.append(" callerID=\"ALL\" status=\"1\" wav_file=\"");
//		Clip clip = RBTCacheManager.getInstance().getClip(announcement.clipId());
//		sb.append(clip.getClipRbtWavFile());
//		sb.append("|1\"");
//		sb.append(" start_date=\""+ playerDateFormatter.format(announcement.activationDate()));
//		sb.append("\"");
//		sb.append(" end_date=\""+ playerDateFormatter.format(announcement.deActivationDate()));
//		sb.append("\" type=\"C\" fromTime=\"0000\" toTime=\"2359\"");
//		sb.append(" />");
		// end tag
		sb.append("</selections></sub_selections>");

		String playerXml = sb.toString();
		logger.info("Player XML: " + playerXml + " subscriberId: " + announcement.subscriberId());
		
		return playerXml;
	}

	private boolean sendPlayerXML(String subscriberID, String xml) {
		SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(subscriberID));
		if (subscriberDetail == null) {
			logger.info("RBTAnn::couldn't determine circle for->"+ subscriberID);
			return false;
		}

		SitePrefix userPrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(subscriberDetail.getCircleID());
		if (userPrefix == null) {
			logger.info("RBT::user prefix null for user " + subscriberID);
			return false;
		}

		String playerURLs = userPrefix.getPlayerUrl();
		StringTokenizer stk = new StringTokenizer(playerURLs, ",");
		boolean response = true;
		while (stk.hasMoreTokens() && response) {
			HttpParameters httpParams = Tools.getHttpParamsForURL(stk.nextToken(), _paramCacheManager.getParameterValue(COMMON,"PLAYER_UPDATE_PAGE","rbtplayer/rbt_memcache_invalidation.jsp?"));
			StringBuilder sb = new StringBuilder(httpParams.getUrl());
			sb.append("SUB_ID=" + subscriberID);
			sb.append("&ACTION=UPDATE&TYPE=RRBT&XML=");
			sb.append(getEncodedUrlString(xml));
			String responseStr = makeHttpRequest(sb.toString(), subscriberID,THREAD_TYPE_PLAYER_ACT, m_playerHTTPClient);
			response = response && responseStr.equalsIgnoreCase("success");
		}
		return response;
	}

	private boolean sendPlayerDeactivation(String subscriberID) {
		SubscriberDetail subscriberDetail = RbtServicesMgr.getSubscriberDetail(new MNPContext(subscriberID));
		if (subscriberDetail == null) {
			logger.info("RBTAnn::couldn't determine circle for->"+ subscriberID);
			return false;
		}

		SitePrefix userPrefix = CacheManagerUtil.getSitePrefixCacheManager().getSitePrefixes(subscriberDetail.getCircleID());
		if (userPrefix == null) {
			logger.info("RBT::user prefix null for user " + subscriberID);
			return false;
		}

		String playerURLs = userPrefix.getPlayerUrl();
		StringTokenizer stk = new StringTokenizer(playerURLs, ",");
		boolean response = true;
		while (stk.hasMoreTokens() && response) {
			HttpParameters httpParams = Tools.getHttpParamsForURL(stk.nextToken(), _paramCacheManager.getParameterValue(COMMON,"PLAYER_UPDATE_PAGE","rbtplayer/rbt_memcache_invalidation.jsp?"));
			StringBuilder sb = new StringBuilder(httpParams.getUrl());
			sb.append("SUB_ID=" + subscriberID);
			sb.append("&ACTION=DEL&TYPE=RRBT");
			String responseStr = makeHttpRequest(sb.toString(), subscriberID,THREAD_TYPE_PLAYER_DCT, m_playerHTTPClient);
			response = response && responseStr.equalsIgnoreCase("success");
		}
		return response;
	}

	private void processPlayerActivation(SubscriberAnnouncements announcement) {
		String playerXML = createPlayerXML(announcement);
		if (sendPlayerXML(announcement.subscriberId(), playerXML))
			_rbtDBManager.updateAnnouncementToActive(announcement.subscriberId(), announcement.clipId());
		else {
			logger.info("RBTAnn::couldn't activate subscriber "+ announcement.subscriberId()+ " at player, will try in next loop");
			_actPlayerList.updateSequenceId(announcement.sequenceId());
		}
	}

	private void processHLRDeactivation(SubscriberAnnouncements announcement) {
		String subscriberID = announcement.subscriberId();
		Subscriber subscriber = _rbtDBManager.getSubscriber(subscriberID);
		if (subscriber == null || subscriber.subYes().equals(STATE_DEACTIVATED)) {
			if (smHLRDeactivation(subscriberID)) {
				Parameters pcaHLRTickparam = CacheManagerUtil.getParametersCacheManager().getParameter(DAEMON,"PCA_HLR_TICK_ASYNC", "TRUE");
				if (pcaHLRTickparam != null&& pcaHLRTickparam.getValue().equalsIgnoreCase("TRUE")) {
					announcement.setStatus(ANNOUNCEMENT_DEACTIVATION_PENDING);
				} else {
					announcement.setStatus(ANNOUNCEMENT_TO_BE_DEACTIVED_PLAYER);
					_dctPlayerList.updateSequenceId(announcement.sequenceId());
				}

				_rbtDBManager.updateAnnouncement(announcement);
				logAnnouncement(announcement, ACTION_ANNOUNCEMENT_DEACTIVATION);
			} else {
				logger.info("RBTAnn::couldn't hlr deactivate subscriber "+ subscriberID + ", will try in next loop");
				_dctList.updateSequenceId(announcement.sequenceId());
			}
		} else {
			logger.info("RBTAnn:: removing PCA in extra info of subscriber table");
			_rbtDBManager.updateExtraInfo(subscriberID, DBUtility.removeXMLAttribute(subscriber.extraInfo(),EXTRA_INFO_PCA_FLAG));
			_rbtDBManager.updatePlayerStatus(subscriberID, "A");
			_rbtDBManager.updateAnnouncementToDeactive(subscriberID,announcement.clipId());
			logAnnouncement(announcement, ACTION_ANNOUNCEMENT_DEACTIVATION);
		}
	}
	
	private void processRBTDeactivation(SubscriberAnnouncements announcement) {
		String subscriberID = announcement.subscriberId();
		int clipId = announcement.clipId();
		if (_rbtDBManager.deactivateAnnouncement(subscriberID,clipId)) {
			logger.info("RBTAnn::deactivated subscriber "+ announcement.subscriberId()+ " at RBT");
		} else {
			logger.info("RBTAnn::couldn't deactivate subscriber "+ announcement.subscriberId()+ " at RBT, will try in next loop");
		}
	}

	private void processPlayerDeactivation(SubscriberAnnouncements announcement) {
		if (sendPlayerDeactivation(announcement.subscriberId()))
			_rbtDBManager.updateAnnouncementToDeactive(announcement.subscriberId(), announcement.clipId());
		else {
			logger.info("RBTAnn::couldn't deactivate subscriber "+ announcement.subscriberId()+ " at player, will try in next loop");
			_dctPlayerList.updateSequenceId(announcement.sequenceId());
		}
	}

	protected void processAnnouncement(SubscriberAnnouncements announcement,String threadType) {
		if (threadType.equals(THREAD_TYPE_HLR_ACT))
			processHLRActivation(announcement);
		else if (threadType.equals(THREAD_TYPE_PLAYER_ACT))
			processPlayerActivation(announcement);
		else if (threadType.equals(THREAD_TYPE_HLR_DCT))
			processHLRDeactivation(announcement);
		else if (threadType.equals(THREAD_TYPE_PLAYER_DCT))
			processPlayerDeactivation(announcement);
		else if (threadType.equals(THREAD_TYPE_RBT_DCT))
			processRBTDeactivation(announcement);
	}

	protected SubscriberAnnouncements getAnnouncement(String threadType)throws InterruptedException {
		SubscriberAnnouncements announcement = null;
		if (threadType.equals(THREAD_TYPE_HLR_ACT))
			announcement = _actList.getRecordToProcess();
		else if (threadType.equals(THREAD_TYPE_PLAYER_ACT))
			announcement = _actPlayerList.getRecordToProcess();
		else if (threadType.equals(THREAD_TYPE_HLR_DCT))
			announcement = _dctList.getRecordToProcess();
		else if (threadType.equals(THREAD_TYPE_PLAYER_DCT))
			announcement = _dctPlayerList.getRecordToProcess();
		else if (threadType.equals(THREAD_TYPE_RBT_DCT))
			announcement = _dctRBTList.getRecordToProcess();
		return announcement;
	}

	public boolean writeTrans(String type, String subId, String requestTs,String responseTs, String requestDetail, String respDetail) {
		HashMap<String, String> h = new HashMap<String, String>();
		h.put("EVENT_TYPE", type);
		h.put("SUBSCRIBERID", subId);
		h.put("REQUESTED_TIMESTAMP", requestTs);
		h.put("RESPONSE_DELAYINMS", responseTs);
		h.put("REQUEST_DETAIL", requestDetail);
		h.put("RESPONSE_DETAIL", respDetail);

		if (m_writeTrans != null) {
			m_writeTrans.writeTrans(h);
			return true;
		}

		return false;
	}

	private String makeHttpRequest(String url, String subscriberID,String threadType, HttpClient httpClient) {
		String response = null;
		int statusCode = 0;
		HostConfiguration hcfg = new HostConfiguration();
		PostMethod postMethod = null;

		long startTime = System.currentTimeMillis();
		try {
			HttpURL httpURL = new HttpURL(url);
			hcfg.setHost(httpURL);
			postMethod = new PostMethod(url);

			statusCode = httpClient.executeMethod(hcfg, postMethod);

			response = postMethod.getResponseBodyAsString();
			if (response != null)
				response = response.trim();

			logger.info("RBT:: response " + response);
			logger.info("RBT:: statusCode recieved " + statusCode);
		}

		catch (Throwable t) {
			logger.error("", t);
			logger.info(" " + t.getMessage());
		} finally {
			if (postMethod != null)
				postMethod.releaseConnection();
			long endTime = System.currentTimeMillis();
			synchronized (logDateFormatter) {
				writeTrans(threadType, subscriberID, logDateFormatter.format(startTime), (endTime - startTime) + "", url,response);
			}
		}
		return (response);
	}

	private String getEncodedUrlString(String param) {
		String ret = null;
		try {
			ret = m_urlEncoder.encode(param, "UTF-8");
		} catch (Throwable t) {
			ret = null;
		}
		return ret;
	}
}