package com.onmobile.apps.ringbacktones.daemons;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.MissingResourceException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.onmobile.apps.ringbacktones.common.Log4jErrorConnector;
import com.onmobile.apps.ringbacktones.common.Log4jSysOutConnector;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.Tools;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.common.cjni.O3InterfaceHelper;
import com.onmobile.common.message.O3Message;

public class TATARBTDaemonOzonized extends Ozonized implements iRBTConstant
{
	private static Logger logger = Logger.getLogger(TATARBTDaemonOzonized.class);
	
	private static final String _module = "O3Daemons";
	private static final String _context = null;

	private static boolean m_Continue = true;
	O3InterfaceHelper m_InterfaceHelper = null;
	O3Message m_o3message = null;
	public Hashtable configValues = null;
	public boolean m_initedComponent = false;
	public boolean FROM_CMD_LINE =true;
	public boolean FROM_DFW_GUI =false;

	private static final String _COMPONENT_NAME = "TATA_RBT_Daemon";

	public static int sleepMinutes;

	private static String destinationComponent = "PromoTool";
	private static String refKey;

	protected static String promoSMSC;
	protected static String promoSMSStartTime;
	protected static String promoSMSEndTime;
	protected static int numOfPromSMSPerSec;
	protected static String m_smsNo;

	ArrayList clipsToBeAddedPost = new ArrayList();
	ArrayList clipsToBeAddedPre = new ArrayList();
	ArrayList subscribersToBeActivated = new ArrayList();
	ArrayList subscribersToBeDeactivated = new ArrayList();
	ArrayList selectionsToBeDeleted = new ArrayList();
	ArrayList activationPendingSubscribersPost = new ArrayList();
	ArrayList activationPendingSubscribersPre = new ArrayList();
	ArrayList deactivationPendingSubscribersPost = new ArrayList();
	ArrayList deactivationPendingSubscribersPre = new ArrayList();

	boolean sentCCBulkDeactivationSMS = false;

	TATARBTDaemonController controllerThread = null;
	  private TATAFTPDownloader ftpDownloader = null;
	private HotSongsCategoryUpdater hotSongsCategoryUpdater = null;

	public TATARBTDaemonOzonized()
	{
		getResourceValues();
	}

	private static void getResourceValues()
	{
		try
		{
			sleepMinutes = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "SLEEP_MINUTES", 0);
			destinationComponent = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SMS_PROMO_TOOL_COMPONENT_NAME", null);
			refKey = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SMS_PROMO_TOOL_REF_KEY", null);
			numOfPromSMSPerSec = RBTParametersUtils.getParamAsInt(iRBTConstant.TATADAEMON, "NUMBER_OF_PROMO_SMS_PER_SEC", 10);
			promoSMSStartTime = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "PROMO_SMS_START_TIME", null);
			promoSMSEndTime = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "PROMO_SMS_END_TIME", null);
			promoSMSC = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "PROMO_SMSC", null);
			m_smsNo = RBTParametersUtils.getParamAsString(iRBTConstant.TATADAEMON, "SMS_NUMBER", "");
		}
		catch(MissingResourceException mre)
		{
			logger.info("Missing Resource : "+mre.getMessage());	
		}
		catch(Exception e)
		{
			logger.info("Exception : "+e.getMessage());
		}
	}

	//to send ozone info message
	public synchronized boolean createInfoMessage(String msgId,String data)
	{        
		O3Message infoMsg= m_InterfaceHelper.getOzoneMessenger().createInfoMessage(msgId, destinationComponent, this.getComponentName(), refKey, data);
		infoMsg.setDestIP(m_InterfaceHelper.getO3OzoneInformation().getHostIP());
		infoMsg.setDestProc(m_InterfaceHelper.getO3OzoneInformation().getOzoneExeName());

		int numberOfMessagesSent = m_InterfaceHelper.getOzoneMessenger().broadcastOzoneMessage(infoMsg, destinationComponent, refKey, null);
		if(numberOfMessagesSent >= 0)
		{
			logger.info("RBT::numberOfMessagesSent = " + numberOfMessagesSent);
			logger.info("RBT::InfoMessage Created and broadcatsed successfully.[refKey = " + refKey + ",msgId = " + msgId + ",data = " + data + "].");
			return true;
		}
		else
		{
			logger.info("RBT::Unable to send O3InfoMessage.[refKey = " + refKey + ",msgId = " + msgId + ",data = " + data + "].");
			return false;
		}
	}

	@Override
	public String getComponentName()
	{
		return _COMPONENT_NAME;
	}

	@Override
	public int initComponent(O3InterfaceHelper o3interfacehelper)
	{
		if(m_initedComponent==false)
		{
			m_initedComponent=true;
			m_InterfaceHelper = o3interfacehelper;
			logger.info("m_initedComponent is" + m_initedComponent);

			return JAVA_COMPONENT_SUCCESS;
		}
		else
		{
			logger.info("Trying to init Again returning "  + JAVA_COMPONENT_CONFIG_REQUIRES_RESTART);

			return JAVA_COMPONENT_CONFIG_REQUIRES_RESTART;
		}
	}

	@Override
	public int configureComponent(Node arg0)
	{
		FROM_CMD_LINE = false;
		FROM_DFW_GUI = true;
		publishRuntimeInfo("inside configureComponent");

		return JAVA_COMPONENT_SUCCESS;

	}

	@Override
	public int startComponent()
	{
		m_Continue = true;
		publishRuntimeInfo("starting the component");
		Log4jErrorConnector r = new Log4jErrorConnector();
		Log4jSysOutConnector l = new Log4jSysOutConnector();
		controllerThread = new TATARBTDaemonController(this);
		controllerThread.start();
	
		hotSongsCategoryUpdater = new HotSongsCategoryUpdater();
		  ftpDownloader = TATAFTPDownloader.getInstance();
           ftpDownloader.init();
		return JAVA_COMPONENT_SUCCESS;

	}

	@Override
	public void stopComponent()
	{
		m_Continue = false;
		publishRuntimeInfo("stopping the component");
		controllerThread.interrupt();
		long ctime = System.currentTimeMillis();
		try
		{
			controllerThread.join();

			hotSongsCategoryUpdater.stop();
			 ftpDownloader.cancel();
		}
		catch(Exception ex)
		{
			logger.error("", ex);

		}
		ctime = System.currentTimeMillis() - ctime;

		publishRuntimeInfo("stopped the component");
		logger.info("Time Taken in Stoppping Thread : " + ctime + " milliSeconds. ");
	}

	@Override
	public void processMessage(O3Message o3message)
	{
		m_o3message = o3message;
	}

	public static boolean isOzoneThreadLive()
	{
		return m_Continue;
	}

	public void publishRuntimeInfo(String info)
	{
		if(FROM_DFW_GUI)
			m_InterfaceHelper.publishRuntimeInfo("<![CDATA[" + info + "]]>");
		if(FROM_CMD_LINE)
			System.out.println("publishRuntimeInfo : " + info);
	}
	public void setdaemonStatusTo(String s) 
	{
		publishRuntimeInfo(s);
	}

	public boolean mustContinue()
	{
		return m_Continue;
	}

	public static void main(String args[])
	{
		Tools.init("COM.ONMOBILE.APPS.RINGBACKTONES.VOICEIMPL.SORBTMANAGER", true);//String module, int log_level, boolean console
		TATARBTDaemonOzonized actualDaemon = new TATARBTDaemonOzonized();
		actualDaemon.startComponent();
	}
}