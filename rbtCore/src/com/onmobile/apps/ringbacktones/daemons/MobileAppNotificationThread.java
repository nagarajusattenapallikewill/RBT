package com.onmobile.apps.ringbacktones.daemons;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javapns.devices.Device;
import javapns.devices.Devices;
import javapns.devices.exceptions.InvalidDeviceTokenFormatException;
import javapns.devices.implementations.basic.BasicDevice;
import javapns.notification.AppleNotificationServer;
import javapns.notification.AppleNotificationServerBasicImpl;
import javapns.notification.Payload;
import javapns.notification.PushNotificationManager;
import javapns.notification.PushNotificationPayload;
import javapns.notification.PushedNotification;
import javapns.notification.PushedNotifications;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;
import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.beans.Parameters;
import com.onmobile.apps.ringbacktones.webservice.client.RBTClient;
import com.onmobile.apps.ringbacktones.webservice.client.beans.MobileAppRegistration;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceContext;

public class MobileAppNotificationThread extends Thread implements WebServiceConstants{

	private static AtomicInteger atomicInteger = new AtomicInteger(1);
	private String smsText = null;
	private String smsTextForIOS = null;
	private Sender sender = null;
	private WebServiceContext webServiceContext = null;
	private static Executor threadPool = Executors.newFixedThreadPool(5);
	private static final Logger logger = Logger.getLogger(MobileAppNotificationThread.class);
	volatile boolean isConnectionInitialize = false;
	private List<String> validSubscriberIds;

	public MobileAppNotificationThread(String name, String smsText, String smsTextForIOS, Sender sender, WebServiceContext webServiceContext, List<String> validSubscriberIds) {
		super(name);
		this.smsText = smsText;
		this.smsTextForIOS = smsTextForIOS;
		this.sender = sender;
		this.webServiceContext = webServiceContext;
		this.validSubscriberIds = validSubscriberIds;
	}

	public static void setAtomicInteger(int value) {
		atomicInteger.set(value);
	}

	public void run() {

		try {
			while(true) {

				int pageNo = atomicInteger.getAndIncrement();
				ApplicationDetailsRequest applicationRequest = new ApplicationDetailsRequest();
				applicationRequest.setPageNo(pageNo + "");
				applicationRequest.setTittle(webServiceContext.getString(param_title));
				applicationRequest.setLanguage(webServiceContext.getString(param_language));
				applicationRequest.setType(webServiceContext.getString(param_type));
				applicationRequest.setClipID(webServiceContext.getString(param_clipIds));
				applicationRequest.setOsType(webServiceContext.getString(param_os_Type));
				String catId = webServiceContext.getString(param_catId);
				if(catId != null && (catId = catId.trim()).length() > 0) {
					applicationRequest.setCategoryID(Integer.parseInt(webServiceContext.getString(param_catId)));
				}

				Rbt rbt = RBTClient.getInstance().getMobileAppRegistration(applicationRequest);

				if(!applicationRequest.getResponse().equalsIgnoreCase("success")) {
					logger.info("Thread shutdown because " + applicationRequest.getResponse());
					break;
				}

				MobileAppRegistration mobileAppRegistration = rbt.getMobileAppRegistration();
				Map<String, String> registerIdSubscriberIdMap = mobileAppRegistration.getRegisterIdSubscriberIdMap();
				Set<String> registerIds = new HashSet<String>();
				if (validSubscriberIds != null) {
					for (Entry<String,String> registerIdSubscriberIdPair : registerIdSubscriberIdMap.entrySet()) {
						if (validSubscriberIds.contains(registerIdSubscriberIdPair.getValue())) {
							registerIds.add(registerIdSubscriberIdPair.getKey());
						}
					}
				} else {
					registerIds = registerIdSubscriberIdMap.keySet();
				}

				if(webServiceContext.getString(param_os_Type) != null &&
						webServiceContext.getString(param_os_Type).equalsIgnoreCase("IOS")) {
					String title =  webServiceContext.getString(param_title);
					String clipIds = webServiceContext.getString(param_clipIds);
					String type = webServiceContext.getString(param_type);
					processNotificationForIOS(registerIds, "eng", title, clipIds, catId, type);
				} else {
					processNotificationForAndriod(registerIds);
				}
			}	
		}
		catch(Throwable t) {
			logger.error(t.getMessage(), t);
		}
	}

	/* PushNotificationManager pushManager = PushNotificationManager.getInstance();  
	   String certificatePath = CacheManagerUtil.getParametersCacheManager().getParameterValue("MOBILEAPP", "IOS_CERTIFICATION_PATH", null);
	   String passKey = CacheManagerUtil.getParametersCacheManager().getParameterValue("MOBILEAPP", "IOS_CERTIFICATION_PATH", null);
	   String ipPort = CacheManagerUtil.getParametersCacheManager().getParameterValue("MOBILEAPP", "IOS_CERTIFICATION_PATH", null);
    PayLoad payLoad = new PayLoad();  
    PayLoadCustomAlert custompayload = new PayLoadCustomAlert();

    logger.info("Actual Message: "+ smsText);
    if(lang != null && lang.toLowerCase().equalsIgnoreCase("fr")){        	   
 	   custompayload.addActionLocKey("Voir");
    } else {
 	   custompayload.addActionLocKey("View");
    }
    Charset charset = Charset.forName("ISO-8859-1"); 
    CharsetEncoder encoder = charset.newEncoder();
    smsText = new String(encoder.encode(CharBuffer.wrap(smsText)).array());

    custompayload.addLocKey(smsText);

    logger.info("Encoded Message: "+ smsText);
    payLoad.addCustomAlert(custompayload);
    payLoad.addBadge(1);  
    payLoad.addSound("default"); 

    logger.info("Sending Message for device token: "+ deviceToken);
    pushManager.addDevice("iPhone", deviceToken);
    if(!isConnectionInitialize) {
 	   pushManager.initializeConnection(ipPort.split(":")[0],
 	     Integer.parseInt(ipPort.split(":")[1]), certificatePath, passKey, SSLConnectionHelper.KEYSTORE_TYPE_PKCS12);
 	   isConnectionInitialize = true;
    }
    Device client = pushManager.getDevice("iPhone");  
    pushManager.sendNotification(client,payLoad);*/

	private void processNotificationForIOS(Collection<String> registerIds, String lang, 
			String title, String clipIds, String catId, String type) throws Exception {
		String [] registerIDsArr = registerIds.toArray(new String[0]);
		logger.info("smsText:" + smsText);
		boolean production = false;
		String certificatePath = CacheManagerUtil.getParametersCacheManager().getParameterValue("MOBILEAPP", "IOS_KEY_STORE_PATH", null);
		String passKey = CacheManagerUtil.getParametersCacheManager().getParameterValue("MOBILEAPP", "IOS_CERTIFICATION_PASSWORD", null);
		Parameters isProdServerParam = CacheManagerUtil.getParametersCacheManager().getParameter("MOBILEAPP", "USE_IOS_PRODUCTION_SERVER");
		if(isProdServerParam != null && isProdServerParam.getValue().equalsIgnoreCase("true")) {
			production = true;
		}
		PushedNotifications notifications = new PushedNotifications();
		//Gson gson = new Gson();
		//Type listOfTestObject = new TypeToken<List<NotificationBean>>(){}.getType();
		// logger.info("listOfTestObject:" + listOfTestObject + "smsText:" + smsText);
		// NotificationBean[] arrayLst = (NotificationBean[])java.lang.reflect.Array.newInstance(NotificationBean.class, 0);

		// NotificationBean[] notificationBeanObj = gson.fromJson(smsText, listOfTestObject);
		// List <NotificationBean> notificationBeanObj = gson.fromJson(smsText, listOfTestObject);
		// logger.info("notificationBeanObj:" + notificationBeanObj.size());
		PushNotificationManager pushManager = new PushNotificationManager();
		Payload payload = createComplexPayload(title, clipIds, catId, type, smsTextForIOS);
		logger.info("payload:" + payload);
		try {  
			if (payload != null) {
				logger.info("certificatePath:" + certificatePath +"passKey:" + passKey+"production:" + production);
				AppleNotificationServer server = new AppleNotificationServerBasicImpl(certificatePath, passKey, production);

				pushManager.initializeConnection(server);
				logger.info("pushManager:" + pushManager);
				List<Device> deviceList = Devices.asDevices(registerIDsArr);
				logger.info("deviceList:" + deviceList);
				notifications.setMaxRetained(deviceList.size());
				for (Device device : deviceList) {
					try {
						BasicDevice.validateTokenFormat(device.getToken());
						PushedNotification notification = pushManager.sendNotification(device, payload, false);
						logger.info("notification:" + notification);
						notifications.add(notification);
					} catch (InvalidDeviceTokenFormatException e) {
						logger.info("InvalidDeviceTokenFormatException:"+e);
						notifications.add(new PushedNotification(device, payload, e));
					} 
				}
			}
		}  catch (Exception e) {  
			logger.info("notification exception:" + e);
			logger.info("notification msg:" + e.getMessage());
			e.printStackTrace();  
		} finally {
			try {
				pushManager.stopConnection();
			} catch (Exception e) {

			}
		}	
	}  

	private static Payload createComplexPayload(String title, String clipIds, 
			String catId, String type, String smsTextForIOS) {
		PushNotificationPayload complexPayload = PushNotificationPayload.complex();
		try {
			complexPayload.addAlert(title);
			//complexPayload.addBadge(45);
			complexPayload.addSound("default");
			complexPayload.addCustomDictionary("notif_type", type);
			complexPayload.addCustomDictionary("notif_title", smsTextForIOS);
			complexPayload.addCustomDictionary("notif_cat_id", catId);
			complexPayload.addCustomDictionary("notif_clip_id", clipIds);
		} catch (JSONException e) {
			System.out.println("Error creating complex payload:");
			e.printStackTrace();
		}
		return complexPayload;
	}
	private void processNotificationForAndriod(Collection<String> registerIds) {
		if(registerIds.size() == 1) {
			// send a single message using plain post
			String registrationId = (String) registerIds.toArray()[0];
			Message message = new Message.Builder().addData("message", smsText).build();
			Result result;
			try {
				result = sender.send(message, registrationId, 5);
				logger.info("Sent message to one device: " + result);
			} catch (IOException e) {
				logger.info("Sent message to one device: Exception happend" + e, e);
				e.printStackTrace();
			}

		}
		else {
			List<String> partialDevices = new ArrayList<String>();
			int counter = 0;
			int total = registerIds.size();
			for (String registerId : registerIds) {
				counter++;
				partialDevices.add(registerId);
				int partialSize = partialDevices.size();
				if (partialSize == 1000 || counter == total) {
					asyncSend(partialDevices, smsText);
					partialDevices.clear();
				}
			}
		}
	}

	private void asyncSend(List<String> partialDevices, final String smsText) {

		logger.info("smsText:" + smsText);
		try {
			// make a copy
			final List<String> devices = new ArrayList<String>(partialDevices);
			threadPool.execute(new Runnable() {

				public void run() {
					Message message = new Message.Builder().addData("message", smsText).build();
					MulticastResult multicastResult;
					try {
						multicastResult = sender.send(message, devices, 5);
					} catch (IOException e) {
						logger.error(e.getMessage(), e);
						return;
					}

					logger.debug("MulticastResult : " + multicastResult);

				}
			});
		}
		catch (Throwable t) {
			logger.error(t.getMessage(), t);
		}
	}
}
