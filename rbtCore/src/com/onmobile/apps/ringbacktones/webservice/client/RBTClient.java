/**
 * OnMobile Ring Back Tone 
 *  
 * $Author: koyel.mahata $
 * $Id: RBTClient.java,v 1.111 2015/03/10 06:03:38 koyel.mahata Exp $
 * $Revision: 1.111 $
 * $Date: 2015/03/10 06:03:38 $
 */
package com.onmobile.apps.ringbacktones.webservice.client;

import java.io.File;
import java.lang.reflect.Constructor;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.genericcache.beans.RbtSupport;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Category;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.ClipRating;
import com.onmobile.apps.ringbacktones.v2.bean.UDPResponseBean;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ApplicationDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Bookmarks;
import com.onmobile.apps.ringbacktones.webservice.client.beans.BulkTask;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CallDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ChargeSms;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Consent;
import com.onmobile.apps.ringbacktones.webservice.client.beans.CopyDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Cos;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Download;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Downloads;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Feed;
import com.onmobile.apps.ringbacktones.webservice.client.beans.FeedStatus;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GiftInbox;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GiftOutbox;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Group;
import com.onmobile.apps.ringbacktones.webservice.client.beans.GroupDetails;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Library;
import com.onmobile.apps.ringbacktones.webservice.client.beans.NextServiceCharge;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Offer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Parameter;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PendingConfirmationsRemainder;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PickOfTheDay;
import com.onmobile.apps.ringbacktones.webservice.client.beans.PredefinedGroup;
import com.onmobile.apps.ringbacktones.webservice.client.beans.RBTLoginUser;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Rbt;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Retailer;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SMSHistory;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SMSText;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Settings;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Site;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPack;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriberPromo;
import com.onmobile.apps.ringbacktones.webservice.client.beans.SubscriptionClass;
import com.onmobile.apps.ringbacktones.webservice.client.beans.TransData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Transaction;
import com.onmobile.apps.ringbacktones.webservice.client.beans.ViralData;
import com.onmobile.apps.ringbacktones.webservice.client.beans.WCHistory;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ApplicationDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.BookmarkRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.BulkSelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.BulkSubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.BulkUpdateDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ConsentRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ContentRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.CopyRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.DataRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GiftRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.GroupRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.MemcacheContentRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RBTDownloadFileRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.RbtDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.Request;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SelectionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SngRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.SubscriptionRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UpdateDetailsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.UtilsRequest;
import com.onmobile.apps.ringbacktones.webservice.client.requests.ValidateNumberRequest;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;
import com.onmobile.apps.ringbacktones.webservice.common.WebServiceConstants;
import com.onmobile.apps.ringbacktones.webservice.features.getCurrSong.CurrentPlayingSongWSResponseBean;
import com.onmobile.apps.ringbacktones.webservice.implementation.util.RBTProtocolBean;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 */
public class RBTClient implements WebServiceConstants {
    private static RBTClient rbtClient = null;

    private Configurations configurations = null;

    private Connector connector = null;
    
  
    private Connector tempConnector = null;
    
    private RBTClient(Configurations configurations) {
        try {
            this.configurations = configurations;
            if (configurations == null)
                this.configurations = new Configurations();
            this.configurations.getLogger().info(
                    "configurations: " + this.configurations);

            String basePackage = this.getClass().getPackage().getName();
            String connectorClassName = "HttpConnector";
            if (this.configurations.getConnectorClass() != null)
                connectorClassName = this.configurations.getConnectorClass();
               
            
            @SuppressWarnings("unchecked")
            Class<Connector> connectorClass = (Class<Connector>) Class
                    .forName(basePackage + "." + connectorClassName);
           
           

            Constructor<Connector> connectorConstructor = connectorClass
                    .getConstructor(this.configurations.getClass());
            
            
            connector = connectorConstructor.newInstance(this.configurations);
            
          
        } catch (Exception e) {
        	 this.configurations.getLogger().error(e.getMessage(), e);
            this.configurations.getLogger().info("RBTClient : " + e);
        }
    }

    private Connector getConnector(Request request) {
        @SuppressWarnings("unchecked")
        Class<Connector> connectorClass = (Class<Connector>) request
                .getConnectorClass();
        if (connectorClass != null
                && !connectorClass.getName().equals(
                        connector.getClass().getName())) {
            if (tempConnector == null) {
                synchronized (RBTClient.class) {
                    if (tempConnector == null) {
                        try {
                            Constructor<Connector> connectorConstructor = connectorClass
                                    .getConstructor(this.configurations
                                            .getClass());
                            tempConnector = connectorConstructor
                                    .newInstance(this.configurations);
                        } catch (Exception e) {
                            this.configurations.getLogger().info(
                                    "RBTClient : " + e);
                        }
                    }
                }
            }

            return tempConnector;
        }

        return connector;
    }

    public static RBTClient getInstance() {
        return getInstance(null);
    }

    public static RBTClient getInstance(Configurations configurations) {
        if (rbtClient == null) {
            synchronized (RBTClient.class) {
                if (rbtClient == null)
                    rbtClient = new RBTClient(configurations);
            }
        }

        return rbtClient;
    }

    public Rbt getRBTUserInformation(RbtDetailsRequest rbtDetailsRequest) {
        return rbt(rbtDetailsRequest, api_Rbt, null);
    }

    public Subscriber getSubscriber(RbtDetailsRequest rbtDetailsRequest) {
        Subscriber subscriber = null;
        try {
            rbtDetailsRequest.setInfo(SUBSCRIBER);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                subscriber = rbt.getSubscriber();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return subscriber;
    }

    public GiftInbox getGiftInbox(RbtDetailsRequest rbtDetailsRequest) {
        GiftInbox giftInbox = null;

        try {
            rbtDetailsRequest.setInfo(GIFT_INBOX);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                giftInbox = rbt.getGiftInbox();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return giftInbox;
    }

    public GiftOutbox getGiftOutbox(RbtDetailsRequest rbtDetailsRequest) {
        GiftOutbox giftOutbox = null;

        try {
            rbtDetailsRequest.setInfo(GIFT_OUTBOX);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                giftOutbox = rbt.getGiftOutbox();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return giftOutbox;
    }

    public Library getLibrary(RbtDetailsRequest rbtDetailsRequest) {
        Library library = null;

        try {
            rbtDetailsRequest.setInfo(LIBRARY);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                library = rbt.getLibrary();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return library;
    }
    
    public Download[] getMiPlayList(RbtDetailsRequest rbtDetailsRequest) {
    	Download[] miPlaylist = null;

        try {
           rbtDetailsRequest.setInfo(MI_PLAYLIST);
           Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
           if (rbt != null && rbt.getmiPlaylist()!=null && rbt.getmiPlaylist().getDownloads().length > 0)
            	miPlaylist = rbt.getmiPlaylist().getDownloads();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return miPlaylist;
    }

    public SubscriberPack[] getSubscriberPacks(
            RbtDetailsRequest rbtDetailsRequest) {
        SubscriberPack[] subscriberPacks = null;

        try {
            rbtDetailsRequest.setInfo(SUBSCRIBER_PACKS);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                subscriberPacks = rbt.getSubscriberPacks();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return subscriberPacks;
    }

    public Settings getSettings(RbtDetailsRequest rbtDetailsRequest) {
        Settings settings = null;

        try {
            rbtDetailsRequest.setInfo(SETTINGS);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null && rbt.getLibrary() != null)
                settings = rbt.getLibrary().getSettings();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return settings;
    }

    public Downloads getDownloads(RbtDetailsRequest rbtDetailsRequest) {
        Downloads downloads = null;

        try {
            rbtDetailsRequest.setInfo(DOWNLOADS);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null && rbt.getLibrary() != null)
                downloads = rbt.getLibrary().getDownloads();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return downloads;
    }

    public Library getLibraryHistory(RbtDetailsRequest rbtDetailsRequest) {
        Library library = null;

        try {
            rbtDetailsRequest.setInfo(LIBRARY_HISTORY);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                library = rbt.getLibrary();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return library;
    }

    public Library getRefundableSelections(RbtDetailsRequest rbtDetailsRequest) {
        Library library = null;

        try {
            rbtDetailsRequest.setInfo(REFUNDABLE_SELECTIONS);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                library = rbt.getLibrary();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return library;
    }

    public Bookmarks getBookmarks(RbtDetailsRequest rbtDetailsRequest) {
        Bookmarks bookamarks = null;

        try {
            rbtDetailsRequest.setInfo(BOOKMARKS);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                bookamarks = rbt.getBookmarks();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return bookamarks;
    }

    public GroupDetails getGroupDetails(RbtDetailsRequest rbtDetailsRequest) {
        GroupDetails groupDetails = null;

        try {
            rbtDetailsRequest.setInfo(GROUP_DETAILS);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                groupDetails = rbt.getGroupDetails();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return groupDetails;
    }

    public SMSHistory[] getSMSHistory(RbtDetailsRequest rbtDetailsRequest) {
        SMSHistory[] smsHistory = null;

        try {
            rbtDetailsRequest.setInfo(SMS_HISTORY);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                smsHistory = rbt.getSmsHistory();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return smsHistory;
    }
    
    // RBT-8199:Need to include all SMS logs in CCC GUI as part of SMS logs
    public SMSHistory[] getSMSHistoryFromUMP(RbtDetailsRequest rbtDetailsRequest) {
        SMSHistory[] smsHistoryFromUMP = null;

        try {
            rbtDetailsRequest.setInfo(SMS_HISTORY_FROM_UMP);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                smsHistoryFromUMP = rbt.getSmsHistoryFromUMP();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return smsHistoryFromUMP;
    }

    public WCHistory[] getWCHistory(RbtDetailsRequest rbtDetailsRequest) {
        WCHistory[] wcHistory = null;

        try {
            rbtDetailsRequest.setInfo(WC_HISTORY);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
            	wcHistory = rbt.getWcHistory();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return wcHistory;
    }

    public Transaction[] getTransactionHistory(
            RbtDetailsRequest rbtDetailsRequest) {
        Transaction[] transactions = null;

        try {
            rbtDetailsRequest.setInfo(TRANSACTION_HISTORY);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                transactions = rbt.getTransactions();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return transactions;
    }

    public CallDetails getCallDetails(RbtDetailsRequest rbtDetailsRequest) {
        CallDetails callDetails = null;

        try {
            rbtDetailsRequest.setInfo(CALL_DETAILS);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                callDetails = rbt.getCallDetails();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return callDetails;
    }

    public Offer[] getOffers(RbtDetailsRequest rbtDetailsRequest) {
        Offer[] allOffers = null;

        try {
            Rbt rbt = rbt(rbtDetailsRequest, api_Offer, null);
            if (rbt != null)
                allOffers = rbt.getOffers();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return allOffers;
    }

    public Offer[] getOfferFromBI(RbtDetailsRequest rbtDetailsRequest){
        Offer[] allOffers = null;

        try {
            Rbt rbt = rbt(rbtDetailsRequest, api_Offer, action_offerFromBI);
            if (rbt != null)
                allOffers = rbt.getOffers();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return allOffers;
    	
    }
    
    public Offer[] getPackageOffer(RbtDetailsRequest rbtDetailsRequest){
        Offer[] allOffers = null;

        try {
            Rbt rbt = rbt(rbtDetailsRequest, api_Offer, action_getPackageOffer);
            if (rbt != null)
                allOffers = rbt.getOffers();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return allOffers;
    	
    }
    
    public Offer getOfferByID(RbtDetailsRequest rbtDetailsRequest) {
        Offer offer = null;
        try {
            Rbt rbt = rbt(rbtDetailsRequest, api_Offer, null);
            if (rbt != null)
                offer = rbt.getOffers()[0];
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
        return offer;
    }

    public SubscriberPromo getSubscriberPromo(
            RbtDetailsRequest rbtDetailsRequest) {
        SubscriberPromo subscriberPromo = null;

        try {
            rbtDetailsRequest.setInfo(SUBSCRIBER_PROMO);
            Rbt rbt = rbt(rbtDetailsRequest, api_Rbt, null);
            if (rbt != null)
                subscriberPromo = rbt.getSubscriberPromo();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return subscriberPromo;
    }

    public SubscriberPromo addSubscriberPromo(
            SubscriptionRequest subscriptionRequest) {
        SubscriberPromo subscriberPromo = null;
        Rbt rbt = rbt(subscriptionRequest, api_Subscription,
                action_addSubscriberPromo);
        if (rbt != null)
            subscriberPromo = rbt.getSubscriberPromo();

        return subscriberPromo;
    }

    public void removeSubscriberPromo(SubscriptionRequest subscriptionRequest) {
        rbt(subscriptionRequest, api_Subscription, action_removeSubscriberPromo);
    }

    public Subscriber activateSubscriber(SubscriptionRequest subscriptionRequest) {
        Subscriber subscriber = null;
        Rbt rbt = rbt(subscriptionRequest, api_Subscription, action_activate);
        if (rbt != null)
            subscriber = rbt.getSubscriber();

        return subscriber;
    }

    public Rbt  activateRbtSubscriber(SubscriptionRequest subscriptionRequest) {
        Rbt rbt = rbt(subscriptionRequest, api_Subscription, action_activate);
        return rbt;
    }

    public Subscriber upgradeSubscriber(SubscriptionRequest subscriptionRequest) {
        Subscriber subscriber = null;
        Rbt rbt = rbt(subscriptionRequest, api_Subscription, action_upgrade);
        if (rbt != null)
            subscriber = rbt.getSubscriber();

        return subscriber;
    }

    //This api is for Idea Consent
    public Rbt activateSubscriberPreConsent(SubscriptionRequest subscriptionRequest) {
        Rbt rbt = rbt(subscriptionRequest, api_SubscriptionPreConsent, action_activate);
        return rbt;
    }
    
    public void activateAnnouncement(SelectionRequest selectionRequest) {
        rbt(selectionRequest, api_Selection, action_activateAnnouncement);
    }

    public void deactivateAnnouncement(SelectionRequest selectionRequest) {
        rbt(selectionRequest, api_Selection, action_deactivateAnnouncement);
    }

    public Rbt acceptGiftService(SubscriptionRequest subscriptionRequest) {
        Rbt rbt = rbt(subscriptionRequest, api_Subscription, action_acceptGift);
        return rbt;
    }

    public Subscriber updateSubscription(SubscriptionRequest subscriptionRequest) {
        Subscriber subscriber = null;
        Rbt rbt = rbt(subscriptionRequest, api_Subscription, action_update);
        if (rbt != null)
            subscriber = rbt.getSubscriber();

        return subscriber;
    }

    /**
     * This API added for ACWM and Idea T&B
     * 
     * @author Sreekar
     * @param subscriptionRequest
     * @return
     */
    public Subscriber confirmSubscription(
            SubscriptionRequest subscriptionRequest) {
        Subscriber subscriber = null;
        subscriptionRequest.setInfo(WebServiceConstants.CONFIRM_CHARGE);
        Rbt rbt = rbt(subscriptionRequest, api_Subscription, action_update);
        if (rbt != null)
            subscriber = rbt.getSubscriber();

        return subscriber;
    }

    public Subscriber deactivateSubscriber(
            SubscriptionRequest subscriptionRequest) {
        Subscriber subscriber = null;
        Rbt rbt = rbt(subscriptionRequest, api_Subscription, action_deactivate);
        if (rbt != null)
            subscriber = rbt.getSubscriber();

        return subscriber;
    }

    public Subscriber deactivatePack(SubscriptionRequest subscriptionRequest) {
        Subscriber subscriber = null;
        Rbt rbt = rbt(subscriptionRequest, api_Subscription,
                action_deactivatePack);
        if (rbt != null)
            subscriber = rbt.getSubscriber();

        return subscriber;
    }

    public Subscriber subscribeUser(SubscriptionRequest subscriptionRequest) {
        Subscriber subscriber = null;
        Rbt rbt = rbt(subscriptionRequest, api_Subscription,
                action_subscribeUser);
        if (rbt != null)
            subscriber = rbt.getSubscriber();

        return subscriber;
    }

    public Rbt addSubscriberSelection(SelectionRequest selectionRequest) {
    	if (configurations.isOverwriteDownload())
    		return overwriteSubscriberSelection(selectionRequest);

        Rbt rbt = rbt(selectionRequest, api_Selection, action_set);
        return rbt;
    }

    public Rbt addSubscriberConsentSelection(SelectionRequest selectionRequest) {
    	 Rbt rbt = rbt(selectionRequest, api_SelectionPreConsent, action_set);
       return rbt;
    }

    public Rbt addSubscriberConsentSelectionIntegration(SelectionRequest selectionRequest) {
   	   Rbt rbt = rbt(selectionRequest, api_SelectionConsentIntegration, action_set);
       return rbt;
    }
    
    public Rbt addSubscriberConsentSelectionInt(SelectionRequest selectionRequest) {
    	   Rbt rbt = rbt(selectionRequest, api_SelectionPreConsentInt, action_set);
        return rbt;
     }

    public Library reset(SelectionRequest selectionRequest) {
        Library library = null;
        Rbt rbt = rbt(selectionRequest, api_Selection,
                action_reset);
        if(rbt!=null)
        	library = rbt.getLibrary();
        return library;
    }
        
    	
    public Rbt overwriteSubscriberSelection(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection, action_overwrite);
        return rbt;
    }

    public Rbt acceptGift(SelectionRequest selectionRequest) {
    	if (configurations.isOverwriteDownload())
    		return overwriteGift(selectionRequest);

        Rbt rbt = rbt(selectionRequest, api_Selection, action_acceptGift);
        return rbt;
    }

    public Rbt overwriteGift(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection, action_overwriteGift);
        return rbt;
    }

    public Library updateSubscriberSelection(SelectionRequest selectionRequest) {
        Library library = null;
        Rbt rbt = rbt(selectionRequest, api_Selection, action_update);
        if (rbt != null)
            library = rbt.getLibrary();

        return library;
    }

    public Library deleteSubscriberSelection(SelectionRequest selectionRequest) {
        Library library = null;
        Rbt rbt = rbt(selectionRequest, api_Selection, action_deleteSetting);
        if (rbt != null)
            library = rbt.getLibrary();

        return library;
    }
    
    public Rbt addSubscriberDownload(SelectionRequest selectionRequest) {
    	if (configurations.isOverwriteDownload())
    		return overwriteRbtSubscriberDownload(selectionRequest);

        Rbt rbt = rbt(selectionRequest, api_Selection, action_downloadTone);
        return rbt;
    }

    public Rbt overwriteRbtSubscriberDownload(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection, action_overwriteDownload);
        return rbt;
    }

    public Library overwriteSubscriberDownload(SelectionRequest selectionRequest) {
        Library library = null;
        Rbt rbt = rbt(selectionRequest, api_Selection, action_overwriteDownload);
        if (rbt != null)
            library = rbt.getLibrary();

        return library;
    }

    public Rbt downloadGift(SelectionRequest selectionRequest) {
    	if (configurations.isOverwriteDownload())
    		return overwriteDownloadGift(selectionRequest);

        Rbt rbt = rbt(selectionRequest, api_Selection, action_downloadGift);
        return rbt;
    }

    public Rbt overwriteDownloadGift(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection,
                action_overwriteDownloadGift);
        return rbt;
    }

    public Library deleteSubscriberDownload(SelectionRequest selectionRequest) {
        Library library = null;
        Rbt rbt = rbt(selectionRequest, api_Selection, action_deleteTone);
        if (rbt != null)
            library = rbt.getLibrary();

        return library;
    }

    public Consent[] getSelConsentUnprocessedRecords(ConsentRequest consentRequest){
        Consent []consent = null;
        Rbt rbt = rbt(consentRequest, api_Rbt, null);
        if (rbt != null &&  rbt.getConsents()!=null){
            consent = rbt.getConsents().getConsent();
        }
        return consent;
    }
    
    public Library shuffleDownloads(SelectionRequest selectionRequest) {
        Library library = null;
        Rbt rbt = rbt(selectionRequest, api_Selection, action_shuffle);
        if (rbt != null)
            library = rbt.getLibrary();

        return library;
    }
    
    public Library disableRandomization(SelectionRequest selectionRequest) {
        Library library = null;
        Rbt rbt = rbt(selectionRequest, api_Selection, action_unRandomize);
        if (rbt != null)
            library = rbt.getLibrary();

        return library;
    }

    public Rbt upgradeSpecialSelectionPack(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection, action_upgradeSelection);
        return rbt;
    }

    public Rbt updateSelectionForDeactDelay(SelectionRequest selectionRequest) {
        selectionRequest.setInfo("deact_delay");
        Rbt rbt = rbt(selectionRequest, api_Selection, action_update);
        return rbt;
    }

    public Rbt upgradeSelectionPack(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection, action_upgrade);
        return rbt;
    }

    public Rbt upgradeAllSelections(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection,
                action_upgradeAllSelections);
        return rbt;
    }
    
    public Rbt upgradeAllDownloads(SelectionRequest selectionRequest){
        Rbt rbt = rbt(selectionRequest, api_Selection, action_upgradeAllDownloads);
        return rbt;
    	
    }
    
    public Rbt upgradeSelectionValidity(SelectionRequest selectionRequest) {
        selectionRequest.setInfo(UPGRADE_VALIDITY);
        Rbt rbt = rbt(selectionRequest, api_Selection, action_update);
        return rbt;
    }

    public Rbt deactivateOffer(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection, action_deactivateOffer);
        return rbt;
    }

    public Bookmarks addBookmark(BookmarkRequest bookmarkRequest) {
        Bookmarks bookamarks = null;
        Rbt rbt = rbt(bookmarkRequest, api_BookMark, action_add);
        if (rbt != null)
            bookamarks = rbt.getBookmarks();

        return bookamarks;
    }

    public Bookmarks overwriteBookmark(BookmarkRequest bookmarkRequest) {
        Bookmarks bookamarks = null;
        Rbt rbt = rbt(bookmarkRequest, api_BookMark, action_overwrite);
        if (rbt != null)
            bookamarks = rbt.getBookmarks();

        return bookamarks;
    }

    public Bookmarks removeBookmark(BookmarkRequest bookmarkRequest) {
        Bookmarks bookamarks = null;
        Rbt rbt = rbt(bookmarkRequest, api_BookMark, action_remove);
        if (rbt != null)
            bookamarks = rbt.getBookmarks();

        return bookamarks;
    }

    private Rbt rbt(Request request, String api, String action) {
        Rbt rbt = null;
        String circleId = request.getCircleID();
        String subscriberId = request.getSubscriberID();
      
       // IXMLParser parser = null;
        try {
        	//Made changes to redirect to B2B user to get the ip and port accordingly 
			ConnectorHandler connectorHandler = ComvivaConnectorFactory
					.getCVConnectorInstance(subscriberId, circleId, connector,
							configurations, api, action);
			Connector tempConnector = connectorHandler.getConnector();
			Map<String, String> b2bSubInfo = connectorHandler.getB2bUserInfo();
			if (null != b2bSubInfo && !b2bSubInfo.isEmpty()
					&& null != request.getRequestParamsMap()) {
				request.getRequestParamsMap().putAll(b2bSubInfo);
			}
			Parser parser =  tempConnector.makeWebServiceRequest(connectorHandler, request, api,
                    action);
            if(parser.getRequest() == null)
            	parser.setRequest(request);
            
            rbt = parser.getParser().getRBT(parser);

        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info("rbt: " + rbt);

        return rbt;
    }

    public HashMap<String, String> getUserInfoFromOperator(
            RbtDetailsRequest rbtDetailsRequest) {
        HashMap<String, String> operatorUserInfo = null;

        try {
            rbtDetailsRequest.setInfo(OPERATOR_USER_INFO);
            Parser parser = connector.makeWebServiceRequest(
                    rbtDetailsRequest, api_Rbt, null);
            Document document = parser.getDocument();

            if (document != null) {
                Element responseElem = (Element) document.getElementsByTagName(
                        RESPONSE).item(0);
                Text responseText = (Text) responseElem.getFirstChild();
                String response = responseText.getNodeValue();
                rbtDetailsRequest.setResponse(response); // set response text in
                                                         // Request object

                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element operatorUserInfoElem = (Element) document
                            .getElementsByTagName(OPERATOR_USER_INFO).item(0);
                    operatorUserInfo = XMLParser
                            .getAttributesMap(operatorUserInfoElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "operatorUserInfo: " + operatorUserInfo);

        return operatorUserInfo;
    }

    public CopyDetails getCopyData(CopyRequest copyRequest) {
        CopyDetails copyDetails = null;

        try {
            Parser parser = connector.makeWebServiceRequest(copyRequest,
                    api_Copy, action_get);
            Document document = parser.getDocument();

            if (document != null) {
                Element responseElem = (Element) document.getElementsByTagName(
                        RESPONSE).item(0);
                Text responseText = (Text) responseElem.getFirstChild();
                String response = responseText.getNodeValue();
                copyRequest.setResponse(response); // set response text in
                                                   // Request object

                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element copyDetailsElem = (Element) document
                            .getElementsByTagName(COPY_DETAILS).item(0);
                    copyRequest.setResponse(copyDetailsElem
                            .getAttribute(RESULT));
                    copyDetails = XMLParser.getCopyDetails(copyDetailsElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info("copyDetails: " + copyDetails);

        return copyDetails;
    }

    public void copy(CopyRequest copyRequest) {
        try {
            Parser parser = connector.makeWebServiceRequest(copyRequest,
                    api_Copy, action_set);
            Document document = parser.getDocument();

            if (document != null) {
                Element responseElem = (Element) document.getElementsByTagName(
                        RESPONSE).item(0);
                Text responseText = (Text) responseElem.getFirstChild();
                String response = responseText.getNodeValue();
                copyRequest.setResponse(response); // set response text in
                                                   // Request object
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }

    public void directCopy(CopyRequest copyRequest) {
        try {
            Parser parser = connector.makeWebServiceRequest(copyRequest,
                    api_Copy, action_directCopy);

            Document document = parser.getDocument();
            
            if (document != null) {
                Element responseElem = (Element) document.getElementsByTagName(
                        RESPONSE).item(0);
                Text responseText = (Text) responseElem.getFirstChild();
                String response = responseText.getNodeValue();
                copyRequest.setResponse(response); // set response text in
                                                   // Request object
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }

    public void sendGift(GiftRequest giftRequest) {
        try {
            ValidateNumberRequest validateNumberRequest = new ValidateNumberRequest(
                    giftRequest.getGifterID(), giftRequest.getGifteeID(),
                    giftRequest.getToneID(), giftRequest.getCategoryID());
            if (giftRequest.getIsPostMethod() != null && giftRequest.getIsPostMethod()) {
            	validateNumberRequest.setIsPostMethod(true);
            }
            validateNumber(validateNumberRequest, action_gift);
            String response = validateNumberRequest.getResponse();
            if (!response.equalsIgnoreCase(VALID)) {
                giftRequest.setResponse(response);
                return;
            }
            String validNumbers = validateNumberRequest.getResponseValidNumbers();
            if (validNumbers != null) {
            	configurations.getLogger().info("After validation, gifteeId: " + validNumbers);
            	giftRequest.setGifteeID(validNumbers);
            }
            gift(giftRequest, action_sendGift);
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }

    public Rbt sendGiftWithoutGifteeValidation(GiftRequest giftRequest) {
        return gift(giftRequest, action_sendGift);
    }
    
    public void rejectGift(GiftRequest giftRequest) {
        gift(giftRequest, action_rejectGift);
    }

    private Rbt gift(GiftRequest giftRequest, String action) {
    	Rbt rbt = null;
        try {
            Parser parser = connector.makeWebServiceRequest(giftRequest,
                    api_Gift, action);
            
            Document document = parser.getDocument();

            if (document != null)
                getResponse(giftRequest, document);
            
            if(giftRequest.getIsConsentFlow() != null && giftRequest.getIsConsentFlow()) {
            	String response = giftRequest.getResponse();
            	if(response.equalsIgnoreCase(SUCCESS)) {
            		Element rbtElem = (Element) document.getElementsByTagName(
                            RBT).item(0);
                    rbt = XMLParser.getRBT(rbtElem, giftRequest);
            	}
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
        return rbt;
    }
    
    public void deleteConsent(ConsentRequest consentRequest, String action) {
        try {
            Parser parser = connector.makeWebServiceRequest(consentRequest,
            		api_SetSubscriberDetails, action);
            
            Document document = parser.getDocument();

            if (document != null)
                getResponse(consentRequest, document);
            
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }
    
    public void insertNameTune(SubscriptionRequest subRequest, String action) {
        try {
            Parser parser = connector.makeWebServiceRequest(subRequest,
            		api_SetSubscriberDetails, action);
            
            Document document = parser.getDocument();

            if (document != null)
                getResponse(subRequest, document);
            
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }

    public void validateGifteeNumber(ValidateNumberRequest validateNumberRequest) {
        validateNumber(validateNumberRequest, action_gift);
    }

    private void validateNumber(ValidateNumberRequest validateNumberRequest,
            String action) {
        try {
            Parser parser = connector.makeWebServiceRequest(
                    validateNumberRequest, api_ValidateNumber, action);
            
            Document document = parser.getDocument();

            if (document != null) {
                Element responseElem = (Element) document.getElementsByTagName(
                        RESPONSE).item(0);
                Text responseText = (Text) responseElem.getFirstChild();
                String response = responseText.getNodeValue();

                if (response.equalsIgnoreCase(NOT_ALLOWED))
                    response = responseElem.getAttribute(STATUS);

                validateNumberRequest.setResponse(response);
                
                if (responseElem.hasAttribute(VALID_NUMBERS)) {
                	String responseValidNumbers = responseElem.getAttribute(VALID_NUMBERS);
                	validateNumberRequest.setResponseValidNumbers(responseValidNumbers);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }

    public Group getGroup(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_get);
        Group[] groups = groupDetails.getGroups();
        if (groups != null && groups.length > 0)
            return groups[0];

        return null;
    }
    
    public Group[] getGroups(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_get);
        Group[] groups = groupDetails.getGroups();
        if (groups != null && groups.length > 0) {
            return groups;
        }
        return null;
    }

    public GroupDetails addGroup(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_add);
        return groupDetails;
    }

    public GroupDetails removeGroup(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_remove);
        return groupDetails;
    }

    public GroupDetails addGroupMember(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_addMember);
        return groupDetails;
    }
    
    public GroupDetails addGroupMultipleMember(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_addMultipleMember);
        return groupDetails;
    }

    public GroupDetails updateGroupMember(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_updateMember);
        return groupDetails;
    }

    public GroupDetails moveGroupMember(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_moveMember);
        return groupDetails;
    }

    public GroupDetails removeGroupMember(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_removeMember);
        return groupDetails;
    }
    
    public GroupDetails removeGroupMultipleMember(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_removeMultipleMember);
        return groupDetails;
    }

    private GroupDetails group(GroupRequest groupRequest, String action) {
        GroupDetails groupDetails = null;

        try {
            Parser parser = connector.makeWebServiceRequest(groupRequest,
                    api_Group, action);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(groupRequest, document);
                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element groupDetailsElem = (Element) document
                            .getElementsByTagName(GROUP_DETAILS).item(0);
                    groupDetails = XMLParser.getGroupDetails(groupDetailsElem,
                            groupRequest);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info("groupDetails: " + groupDetails);

        return groupDetails;
    }

    public Subscriber setSubscriberDetails(UpdateDetailsRequest updateRequest) {
        Subscriber subscriber = null;

        try {
            Parser parser = connector.makeWebServiceRequest(updateRequest,
                    api_SetSubscriberDetails, null);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(updateRequest, document);
                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element subscriberElem = (Element) document
                            .getElementsByTagName(SUBSCRIBER).item(0);
                    subscriber = XMLParser.getSubscriber(subscriberElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info("subscriber: " + subscriber);

        return subscriber;
    }

    public Parameter getParameter(
            ApplicationDetailsRequest applicationDetailsRequest) {
        Parameter[] parameters = getParameters(applicationDetailsRequest);
        if (parameters != null && parameters.length > 0)
            return parameters[0];

        return null;
    }

    public Parameter[] getParameters(
            ApplicationDetailsRequest applicationDetailsRequest) {
        Parameter[] parameters = parameters(applicationDetailsRequest,
                action_get);
        return parameters;
    }

    public Parameter setParameter(
            ApplicationDetailsRequest applicationDetailsRequest) {
        Parameter[] parameters = parameters(applicationDetailsRequest,
                action_set);
        if (parameters != null && parameters.length > 0)
            return parameters[0];

        return null;
    }

    public void removeParameter(
            ApplicationDetailsRequest applicationDetailsRequest) {
        parameters(applicationDetailsRequest, action_remove);
    }

    private Parameter[] parameters(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        Parameter[] parameters = null;

        try {
            applicationDetailsRequest.setInfo(PARAMETERS);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                parameters = applicationDetails.getParameters();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return parameters;
    }

    public SubscriptionClass getSubscriptionClass(
            ApplicationDetailsRequest applicationDetailsRequest) {
        SubscriptionClass[] subscriptionClasses = getSubscriptionClasses(applicationDetailsRequest);
        if (subscriptionClasses != null && subscriptionClasses.length > 0)
            return subscriptionClasses[0];

        return null;
    }

    public SubscriptionClass[] getSubscriptionClasses(
            ApplicationDetailsRequest applicationDetailsRequest) {
        SubscriptionClass[] subscriptionClasses = subscriptionClasses(
                applicationDetailsRequest, action_get);
        return subscriptionClasses;
    }

    private SubscriptionClass[] subscriptionClasses(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        SubscriptionClass[] subscriptionClasses = null;

        try {
            applicationDetailsRequest.setInfo(SUBSCRIPTION_CLASSES);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                subscriptionClasses = applicationDetails
                        .getSubscriptionClasses();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return subscriptionClasses;
    }

    public ChargeClass getChargeClass(
            ApplicationDetailsRequest applicationDetailsRequest) {
        ChargeClass[] chargeClasses = getChargeClasses(applicationDetailsRequest);
        if (chargeClasses != null && chargeClasses.length > 0)
            return chargeClasses[0];

        return null;
    }

    public ChargeClass[] getChargeClasses(
            ApplicationDetailsRequest applicationDetailsRequest) {
        ChargeClass[] chargeClasses = chargeClasses(applicationDetailsRequest,
                action_get);
        return chargeClasses;
    }

    private ChargeClass[] chargeClasses(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        ChargeClass[] chargeClasses = null;

        try {
            applicationDetailsRequest.setInfo(CHARGE_CLASSES);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                chargeClasses = applicationDetails.getChargeClasses();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return chargeClasses;
    }

    public SMSText getSMSText(
            ApplicationDetailsRequest applicationDetailsRequest) {
        SMSText[] smsTexts = smsTexts(applicationDetailsRequest, action_get);
        if (smsTexts != null && smsTexts.length > 0)
            return smsTexts[0];

        return null;
    }

    public SMSText[] getSMSTexts(
            ApplicationDetailsRequest applicationDetailsRequest) {
        SMSText[] smsTexts = smsTexts(applicationDetailsRequest, action_get);
        return smsTexts;
    }

    public SMSText setSMSText(
            ApplicationDetailsRequest applicationDetailsRequest) {
        SMSText[] smsTexts = smsTexts(applicationDetailsRequest, action_set);
        if (smsTexts != null && smsTexts.length > 0)
            return smsTexts[0];

        return null;
    }

    private SMSText[] smsTexts(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        SMSText[] smsTexts = null;

        try {
            applicationDetailsRequest.setInfo(SMS_TEXTS);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                smsTexts = applicationDetails.getSmsTexts();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return smsTexts;
    }

    public PickOfTheDay[] getPickOfTheDays(
            ApplicationDetailsRequest applicationDetailsRequest) {
        PickOfTheDay[] pickOfTheDays = pickOfTheDays(applicationDetailsRequest,
                action_get);
        return pickOfTheDays;
    }

    public PickOfTheDay[] setPickOfTheDay(
            ApplicationDetailsRequest applicationDetailsRequest) {
        PickOfTheDay[] pickOfTheDays = pickOfTheDays(applicationDetailsRequest,
                action_set);
        return pickOfTheDays;
    }

    public PickOfTheDay[] removePickOfTheDay(
            ApplicationDetailsRequest applicationDetailsRequest) {
        PickOfTheDay[] pickOfTheDays = pickOfTheDays(applicationDetailsRequest,
                action_remove);
        return pickOfTheDays;
    }

    private PickOfTheDay[] pickOfTheDays(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        PickOfTheDay[] pickOfTheDays = null;

        try {
            applicationDetailsRequest.setInfo(PICK_OF_THE_DAYS);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                pickOfTheDays = applicationDetails.getPickOfTheDays();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return pickOfTheDays;
    }

    public RBTLoginUser getRBTLoginUser(
            ApplicationDetailsRequest applicationDetailsRequest) {
        RBTLoginUser rbtLoginUser = rbtLoginUser(applicationDetailsRequest,
                action_get);
        return rbtLoginUser;
    }
    
    
    public List<Clip> getClipFromWebservice(MemcacheContentRequest memcacheContentRequest) {
    	memcacheContentRequest.setType(CLIP);
    	String response = memcacheContentDetails(memcacheContentRequest, action_memcache);
    	List<Clip> clipList = new ArrayList<Clip>();
    	if(response.equalsIgnoreCase("ERROR")) {
    		configurations.getLogger().info("Webservice returns no clips");
    		return null;
    	}
    	try{
			JSONArray jsonArray = new JSONArray(response);
			int jsonArrayLength = jsonArray.length();
			for(int i = 0; i < jsonArrayLength; i++) {
				String object = (String)jsonArray.get(i);
				ObjectMapper mapper = new ObjectMapper();
				clipList.add((Clip)mapper.readValue(object, Clip.class));
			}
		}
		catch(Exception e) {
			configurations.getLogger().error("Exception: ", e);
		}
    	return clipList;
    }
    
    public List<Category> getCategoryFromWebservice(MemcacheContentRequest memcacheContentRequest) {
    	memcacheContentRequest.setType(CATEGORY);
    	String response = memcacheContentDetails(memcacheContentRequest, action_memcache);
    	List<Category> categoryList = new ArrayList<Category>();
    	if(response.equalsIgnoreCase("ERROR")) {
    		configurations.getLogger().info("Webservice returns no categories");
    		return null;
    	}
    	try{
			JSONArray jsonArray = new JSONArray(response);
			int jsonArrayLength = jsonArray.length();
			for(int i = 0; i < jsonArrayLength; i++) {
				String object = (String)jsonArray.get(i);
				ObjectMapper mapper = new ObjectMapper();
				categoryList.add((Category)mapper.readValue(object, Category.class));
			}
		}
		catch(Exception e) {
			configurations.getLogger().error("Exception: ", e);
		}
    	return categoryList;
    }

    public RBTLoginUser[] getAllRBTLoginUsers(
            ApplicationDetailsRequest applicationDetailsRequest) {
        RBTLoginUser[] allRBTLoginUsers = rbtLoginUsers(
                applicationDetailsRequest, action_get);
        return allRBTLoginUsers;
    }

    public RBTLoginUser setRBTLoginUser(
            ApplicationDetailsRequest applicationDetailsRequest) {
        RBTLoginUser rbtLoginUser = rbtLoginUser(applicationDetailsRequest,
                action_set);
        return rbtLoginUser;
    }

    public void removeRBTLoginUser(
            ApplicationDetailsRequest applicationDetailsRequest) {
        rbtLoginUser(applicationDetailsRequest, action_remove);
    }

    private RBTLoginUser rbtLoginUser(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        RBTLoginUser rbtLoginUser = null;

        try {
            applicationDetailsRequest.setInfo(RBT_LOGIN_USER);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null) {
                RBTLoginUser[] rbtLoginUsers = applicationDetails
                        .getRbtLoginUsers();
                if (rbtLoginUsers != null && rbtLoginUsers.length > 0)
                    rbtLoginUser = rbtLoginUsers[0];
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return rbtLoginUser;
    }

    private RBTLoginUser[] rbtLoginUsers(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        RBTLoginUser[] rbtLoginUsers = null;

        try {
            applicationDetailsRequest.setInfo(RBT_LOGIN_USERS);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                rbtLoginUsers = applicationDetails.getRbtLoginUsers();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return rbtLoginUsers;
    }

    public Site getSite(ApplicationDetailsRequest applicationDetailsRequest) {
        Site[] sites = sites(applicationDetailsRequest, action_get);
        if (sites != null && sites.length > 0)
            return sites[0];

        return null;
    }

    public Site[] getSites(ApplicationDetailsRequest applicationDetailsRequest) {
        Site[] sites = sites(applicationDetailsRequest, action_get);
        return sites;
    }

    public Site setSite(ApplicationDetailsRequest applicationDetailsRequest) {
        Site[] sites = sites(applicationDetailsRequest, action_set);
        if (sites != null && sites.length > 0)
            return sites[0];

        return null;
    }

    public Site[] removeSite(ApplicationDetailsRequest applicationDetailsRequest) {
        Site[] sites = sites(applicationDetailsRequest, action_remove);
        return sites;
    }

    private Site[] sites(ApplicationDetailsRequest applicationDetailsRequest,
            String action) {
        Site[] sites = null;

        try {
            applicationDetailsRequest.setInfo(SITES);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                sites = applicationDetails.getSites();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return sites;
    }

    public ChargeSms getChargeSms(
            ApplicationDetailsRequest applicationDetailsRequest) {
        ChargeSms[] chargeSmses = chargeSmses(applicationDetailsRequest,
                action_get);
        if (chargeSmses != null && chargeSmses.length > 0)
            return chargeSmses[0];

        return null;
    }

    public ChargeSms[] getChargeSmses(
            ApplicationDetailsRequest applicationDetailsRequest) {
        ChargeSms[] chargeSmses = chargeSmses(applicationDetailsRequest,
                action_get);
        return chargeSmses;
    }

    public ChargeSms setChargeSms(
            ApplicationDetailsRequest applicationDetailsRequest) {
        ChargeSms[] chargeSmses = chargeSmses(applicationDetailsRequest,
                action_set);
        if (chargeSmses != null && chargeSmses.length > 0)
            return chargeSmses[0];

        return null;
    }

    private ChargeSms[] chargeSmses(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        ChargeSms[] chargeSmses = null;

        try {
            applicationDetailsRequest.setInfo(CHARGE_SMS);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                chargeSmses = applicationDetails.getChargeSmses();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return chargeSmses;
    }

    public Cos getCos(ApplicationDetailsRequest applicationDetailsRequest) {
        Cos[] coses = cos(applicationDetailsRequest, action_get);
        if (coses != null && coses.length > 0)
            return coses[0];

        return null;
    }

    public Cos[] getCoses(ApplicationDetailsRequest applicationDetailsRequest) {
        Cos[] coses = cos(applicationDetailsRequest, action_get);
        return coses;
    }

    private Cos[] cos(ApplicationDetailsRequest applicationDetailsRequest,
            String action) {
        Cos[] coses = null;

        try {
            applicationDetailsRequest.setInfo(COS_DETAILS);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                coses = applicationDetails.getCoses();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return coses;
    }

    public Retailer getRetailer(
            ApplicationDetailsRequest applicationDetailsRequest) {
        Retailer[] retailers = retailer(applicationDetailsRequest, action_get);
        if (retailers != null && retailers.length > 0)
            return retailers[0];

        return null;
    }

    private Retailer[] retailer(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        Retailer[] retailers = null;

        try {
            applicationDetailsRequest.setInfo(RETAILER);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                retailers = applicationDetails.getRetailers();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return retailers;
    }

    public FeedStatus getFeedStatus(
            ApplicationDetailsRequest applicationDetailsRequest) {
        FeedStatus[] feedStatuses = feedStatus(applicationDetailsRequest,
                action_get);
        if (feedStatuses != null && feedStatuses.length > 0)
            return feedStatuses[0];

        return null;
    }

    private FeedStatus[] feedStatus(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        FeedStatus[] feedStatuses = null;

        try {
            applicationDetailsRequest.setInfo(FEED_STATUS);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                feedStatuses = applicationDetails.getFeedStatuses();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return feedStatuses;
    }

    public Feed getFeed(ApplicationDetailsRequest applicationDetailsRequest) {
        Feed[] feeds = feed(applicationDetailsRequest, action_get);
        if (feeds != null && feeds.length > 0)
            return feeds[0];

        return null;
    }

    public Feed[] getFeeds(ApplicationDetailsRequest applicationDetailsRequest) {
        Feed[] feeds = feed(applicationDetailsRequest, action_get);
        return feeds;
    }

    private Feed[] feed(ApplicationDetailsRequest applicationDetailsRequest,
            String action) {
        Feed[] feeds = null;

        try {
            applicationDetailsRequest.setInfo(FEED_DETAILS);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                feeds = applicationDetails.getFeeds();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return feeds;
    }

    public PredefinedGroup[] getPredefinedGroups(
            ApplicationDetailsRequest applicationDetailsRequest) {
        PredefinedGroup[] predefinedGroups = predefinedGroup(
                applicationDetailsRequest, action_get);
        return predefinedGroups;
    }

    private PredefinedGroup[] predefinedGroup(
            ApplicationDetailsRequest applicationDetailsRequest, String action) {
        PredefinedGroup[] predefinedGroups = null;

        try {
            applicationDetailsRequest.setInfo(PREDEFINED_GROUPS);
            ApplicationDetails applicationDetails = applicationDetails(
                    applicationDetailsRequest, action);
            if (applicationDetails != null)
                predefinedGroups = applicationDetails.getPredefinedGroups();
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return predefinedGroups;
    }

    private ApplicationDetails applicationDetails(Request request, String action) {
        ApplicationDetails applicationDetails = null;
        String circleId = null ; 
        String subscriberId = request.getSubscriberID() ;
		ConnectorHandler connectorHandler = ComvivaConnectorFactory
				.getCVConnectorInstance(subscriberId, circleId, connector,
						configurations, api_ApplicationDetails, action);
		Connector tempConnector = connectorHandler.getConnector();
        try {
            Parser parser = tempConnector.makeWebServiceRequest(connectorHandler,request,
                    api_ApplicationDetails, action);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(request, document);
                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element applicationDetailsElem = (Element) document
                            .getElementsByTagName(APPLICATION_DETAILS).item(0);
                    applicationDetails = XMLParser
                            .getApplicationDetails(applicationDetailsElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "applicationDetails: " + applicationDetails);

        return applicationDetails;
    }

    
    private String memcacheContentDetails(Request request, String action) {
        String response = "ERROR";
        try {
           Parser parser = connector.makeWebServiceRequest(request,
                    api_Content, action);
           
           Document document = parser.getDocument();

            if (document != null) {
            	response = getResponse(request, document);
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "response: " + response);

        return response;
    }
    
    
    public BulkTask[] getBulkTasks(BulkSelectionRequest bulkSelectionRequest) {
        BulkTask[] bulkTasks = bulkTask(bulkSelectionRequest, action_get);
        return bulkTasks;
    }

    public File uploadBulkTask(BulkSelectionRequest bulkSelectionRequest) {
        File file = bulk(bulkSelectionRequest, action_upload);
        return file;
    }

    // JiraID-RBT-4187:Song upgradation through bulk process
    public File upgradeBulkSelections(BulkSelectionRequest bulkSelectionRequest) {
        File file = bulk(bulkSelectionRequest, action_upgradeAllSelections);
        return file;
    }

    public File uploadNprocessBulkTask(BulkSelectionRequest bulkSelectionRequest) {
        File resultFile = bulk(bulkSelectionRequest, action_uploadNprocess);
        return resultFile;
    }

    public File editBulkTaskForCorporate(
            BulkSelectionRequest bulkSelectionRequest) {
        File file = bulk(bulkSelectionRequest, action_editTask);
        return file;
    }

    public BulkTask editBulkTask(BulkSelectionRequest bulkSelectionRequest) {
        BulkTask[] bulkTasks = bulkTask(bulkSelectionRequest, action_editTask);
        if (bulkTasks != null && bulkTasks.length > 0)
            return bulkTasks[0];

        return null;
    }

    public void deleteBulkTask(BulkSelectionRequest bulkSelectionRequest) {
        bulkTask(bulkSelectionRequest, action_deleteTask);
    }

    public void removeBulkTask(BulkSelectionRequest bulkSelectionRequest) {
        bulkTask(bulkSelectionRequest, action_removeTask);
    }

    public File processBulkTask(BulkSelectionRequest bulkSelectionRequest) {
        File resultFile = bulk(bulkSelectionRequest, action_process);
        return resultFile;
    }

    public File processGetDownloadOfTheDayTask(
            BulkSelectionRequest bulkSelectionRequest) {
        File resultFile = bulk(bulkSelectionRequest, action_getdownloadOfDays);
        return resultFile;
    }

    public File processDownloadOfTheDayTask(
            BulkUpdateDetailsRequest bulkUpdateDetailsRequest) {
        File resultFile = bulk(bulkUpdateDetailsRequest, action_downloadOfDay);
        return resultFile;
    }

    public File bulkActivation(BulkSubscriptionRequest bulkSubscriptionRequest) {
        File resultFile = bulk(bulkSubscriptionRequest, action_activate);
        return resultFile;
    }

    public File bulkDeactivation(BulkSubscriptionRequest bulkSubscriptionRequest) {
        File resultFile = bulk(bulkSubscriptionRequest, action_deactivate);
        return resultFile;
    }

    public File bulkAnnouncementActivation(
            BulkSelectionRequest bulkSelectionRequest) {
        File resultFile = bulk(bulkSelectionRequest,
                action_activateAnnouncement);
        return resultFile;
    }

    public File bulkAnnouncementDeactivation(
            BulkSelectionRequest bulkSelectionRequest) {
        File resultFile = bulk(bulkSelectionRequest,
                action_deactivateAnnouncement);
        return resultFile;
    }

    public File bulkSelection(BulkSelectionRequest bulkSelectionRequest) {
        File resultFile = bulk(bulkSelectionRequest, action_set);
        return resultFile;
    }

    public File bulkDeleteSelection(BulkSelectionRequest bulkSelectionRequest) {
        File resultFile = bulk(bulkSelectionRequest, action_deleteSetting);
        return resultFile;
    }

    public File getSubscriberStatusFile(
            BulkUpdateDetailsRequest bulkUpdateDetailsRequest) {
        bulkUpdateDetailsRequest.setInfo(SUBSCRIBER_STATUS);
        File resultFile = bulk(bulkUpdateDetailsRequest, action_get);
        return resultFile;
    }

    public File bulkSetSubscriberDetails(
            BulkUpdateDetailsRequest bulkUpdateDetailsRequest) {
        File resultFile = bulk(bulkUpdateDetailsRequest, action_update);
        return resultFile;
    }

    public File getBlacklistFile(
            BulkUpdateDetailsRequest bulkUpdateDetailsRequest) {
        bulkUpdateDetailsRequest.setInfo(BLACKLIST);
        File resultFile = bulk(bulkUpdateDetailsRequest, action_get);
        return resultFile;
    }

    public File getLoginUsersFile(
            BulkUpdateDetailsRequest bulkUpdateDetailsRequest) {
        bulkUpdateDetailsRequest.setInfo(LOGIN_USER);
        File resultFile = bulk(bulkUpdateDetailsRequest, action_get);
        return resultFile;
    }

    public File getBulkSubscribersStatusFile(
            BulkUpdateDetailsRequest bulkUpdateDetailsRequest) {
        File resultFile = bulk(bulkUpdateDetailsRequest,
                action_checkBulkSubscribersStatus);
        return resultFile;
    }

    public File getBulkTaskSubscribersStatusFile(
            BulkSelectionRequest bulkSelectionRequest) {
        File resultFile = bulk(bulkSelectionRequest,
                action_getBulkTaskSubscriberDetails);
        return resultFile;
    }

    public File bulkUpdateSubscription(
            BulkSubscriptionRequest bulkSubscriptionRequest) {
        File resultFile = bulk(bulkSubscriptionRequest, action_updateValidity);
        return resultFile;
    }

    private File bulk(Request request, String action) {
        File file = null;
        request.setResponse(FAILED);

        try {
            file = getConnector(request).makeWebServiceBulkRequest(request,
                    api_BulkTask, action);
            if (file != null && file.exists())
                request.setResponse(SUCCESS);
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
            request.setResponse(ERROR);
        }

        configurations.getLogger().info("file: " + file);
        return file;
    }

    private BulkTask[] bulkTask(Request request, String action) {
        request.setResponse(FAILED);
        BulkTask[] bulkTasks = null;

        try {
            Parser parser = getConnector(request).makeWebServiceRequest(
                    request, api_BulkUploadTask, action);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(request, document);
                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element bulkTasksElem = (Element) document
                            .getElementsByTagName(BULK_TASKS).item(0);
                    bulkTasks = XMLParser.getBulkTasks(bulkTasksElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
            request.setResponse(ERROR);
        }

        return bulkTasks;
    }

    public void sendSMS(UtilsRequest utilsRequest) {
        utils(utilsRequest, action_sendSMS);
    }

    public void tickHLR(UtilsRequest utilsRequest) {
        utils(utilsRequest, action_tickHLR);
    }

    public void untickHLR(UtilsRequest utilsRequest) {
        utils(utilsRequest, action_untickHLR);
    }

    public void suspension(UtilsRequest utilsRequest) {
        utils(utilsRequest, action_suspension);
    }

    public void makeThirdPartyRequest(UtilsRequest utilsRequest) {
        utils(utilsRequest, action_thirdPartyRequest);
    }

    private void utils(UtilsRequest utilsRequest, String action) {
        try {
            Parser parser = connector.makeWebServiceRequest(utilsRequest,
                    api_Utils, action);

            Document document = parser.getDocument();
            
            if (document != null)
                getResponse(utilsRequest, document);
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }
    
    public PendingConfirmationsRemainder addPendingConfirmationsRemainder(DataRequest dataRequest) {
    	PendingConfirmationsRemainder[] pendingConfirmationsRemainders = processPendingConfirmationsRemainder(dataRequest, action_add);
        if (pendingConfirmationsRemainders != null && pendingConfirmationsRemainders.length > 0) {
            return pendingConfirmationsRemainders[0];
        }
        return null;
    }
    
    public PendingConfirmationsRemainder getPendingConfirmationsRemainder(DataRequest dataRequest) {
    	PendingConfirmationsRemainder[] pendingConfirmationsRemainders = processPendingConfirmationsRemainder(dataRequest, action_get);
        if (pendingConfirmationsRemainders != null && pendingConfirmationsRemainders.length > 0) {
            return pendingConfirmationsRemainders[0];
        }
        return null;
    }
    
    public PendingConfirmationsRemainder[] getPendingConfirmationsRemainders(DataRequest dataRequest) {
        return processPendingConfirmationsRemainder(dataRequest, action_get);
    }
    
    public void updatePendingConfirmationsRemainder(DataRequest dataRequest) {
    	processPendingConfirmationsRemainder(dataRequest, action_update);
    }
    
    public void deletePendingConfirmationsRemainder(DataRequest dataRequest) {
    	processPendingConfirmationsRemainder(dataRequest, action_remove);
    }

    public PendingConfirmationsRemainder[] processPendingConfirmationsRemainder(DataRequest dataRequest, String action) {
        PendingConfirmationsRemainder[] pendingConfirmationsRemainder = new PendingConfirmationsRemainder[] {};
        try {
            dataRequest.setInfo(PENDING_CONFIRMATIONS_REMINDER_DATA);
            Parser parser = connector.makeWebServiceRequest(dataRequest,
                    api_Data, action);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(dataRequest, document);
                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element remainderSmsDataEle = (Element) document
                            .getElementsByTagName(PENDING_CONFIRMATIONS_REMINDER_DATA).item(0);
                    pendingConfirmationsRemainder = XMLParser.getPendingConfirmationsRemainderData(remainderSmsDataEle);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "remainderSmsData: " + Arrays.toString(pendingConfirmationsRemainder));

        return pendingConfirmationsRemainder;
    }
    
    public ViralData[] getViralData(DataRequest dataRequest) {
        ViralData[] viralData = viralData(dataRequest, action_get);
        return viralData;
    }

    public ViralData addViralData(DataRequest dataRequest) {
        ViralData[] viralData = viralData(dataRequest, action_add);
        if (viralData != null && viralData.length > 0)
            return viralData[0];

        return null;
    }

    public ViralData[] processViralData(DataRequest dataRequest) {
        ViralData[] viralData = viralData(dataRequest, action_process);
        return viralData;
    }

    public ViralData[] updateViralData(DataRequest dataRequest) {
        ViralData[] viralData = viralData(dataRequest, action_update);
        return viralData;
    }

    public void removeViralData(DataRequest dataRequest) {
        viralData(dataRequest, action_remove);
    }

    private ViralData[] viralData(DataRequest dataRequest, String action) {
        ViralData[] viralData = null;
        try {
            dataRequest.setInfo(VIRAL_DATA);
            Parser parser = connector.makeWebServiceRequest(dataRequest,
                    api_Data, action);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(dataRequest, document);
                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element viralDataElem = (Element) document
                            .getElementsByTagName(VIRAL_DATA).item(0);
                    viralData = XMLParser.getViralData(viralDataElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "viralData: " + Arrays.toString(viralData));

        return viralData;
    }

    public TransData getTransData(DataRequest dataRequest) {
        TransData[] transData = transData(dataRequest, action_get);
        if (transData != null && transData.length > 0)
            return transData[0];

        return null;
    }

    public TransData[] getTransDatas(DataRequest dataRequest) {
        TransData[] transData = transData(dataRequest, action_get);
        return transData;
    }

    public TransData addTransData(DataRequest dataRequest) {
        TransData[] transData = transData(dataRequest, action_add);
        if (transData != null && transData.length > 0)
            return transData[0];

        return null;
    }

    public RbtSupport addRbtSupport(DataRequest dataRequest) {
        RbtSupport[] rbtSupportData = rbtSupportData(dataRequest, action_add);
        if (rbtSupportData != null && rbtSupportData.length > 0)
            return rbtSupportData[0];

        return null;
    }

    public void removeTransData(DataRequest dataRequest) {
        transData(dataRequest, action_remove);
    }

    private TransData[] transData(DataRequest dataRequest, String action) {
        TransData[] transData = null;
        try {
            dataRequest.setInfo(TRANS_DATA);
            Parser parser = connector.makeWebServiceRequest(dataRequest,
                    api_Data, action);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(dataRequest, document);
                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element transDataElem = (Element) document
                            .getElementsByTagName(TRANS_DATA).item(0);
                    transData = XMLParser.getTransData(transDataElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "transData: " + Arrays.toString(transData));

        return transData;
    }

    private RbtSupport[] rbtSupportData(DataRequest dataRequest, String action) {
        RbtSupport[] rbtSupportData = null;
        try {
            dataRequest.setInfo(RBTSUPPORT_DATA);
            Parser parser = connector.makeWebServiceRequest(dataRequest,
                    api_Data, action);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(dataRequest, document);
                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element transDataElem = (Element) document
                            .getElementsByTagName(RBTSUPPORT_DATA).item(0);
                    rbtSupportData = XMLParser.getRbtSupportData(transDataElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "transData: " + Arrays.toString(rbtSupportData));

        return rbtSupportData;
    }

    public void addContent(ContentRequest contentRequest) {
        try {
            Parser parser = connector.makeWebServiceRequest(contentRequest,
                    api_Content, action_add);
            
            Document document = parser.getDocument();

            if (document != null)
                getResponse(contentRequest, document);
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }

    public Subscriber changeMsisdn(UpdateDetailsRequest updateRequest) {
        Subscriber subscriber = null;
        Rbt rbt = rbt(updateRequest, api_SetSubscriberDetails,
                action_changeMsisdn);
        if (rbt != null)
            subscriber = rbt.getSubscriber();

        return subscriber;
    }

    public void sendChangeMsisdnRequest(UpdateDetailsRequest updateRequest) {
        rbt(updateRequest, api_SetSubscriberDetails,
                action_sendChangeMsisdnRequest);
    }

    public void addSNGUser(SngRequest sngRequest) {
        rbt(sngRequest, api_Sng, action_activate);
    }

    public void removeSNGUser(SngRequest sngRequest) {
        rbt(sngRequest, api_Sng, action_deactivate);
    }

    public void removeSNGUsers(SngRequest sngRequest) {
        rbt(sngRequest, api_Sng, deactivate_all);
    }

    public String redirectWebServiceRequest(
            HashMap<String, String> requestParams, String api) {
        String returnStr = null;
        try {
            returnStr = connector.redirectWebServiceRequest(requestParams, api);

        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        configurations.getLogger().info("document: " + returnStr);
        return returnStr;
    }

    public String[] getRecommendationMusic(ContentRequest contentRequest) {
        List<String> promoIdList = new ArrayList<String>();
        try {
            Parser parser = connector.makeWebServiceRequest(contentRequest,
                    api_Content, action_recommendation_music);
            
            Document document = parser.getDocument();
            
            if (document != null) {
                Element responseElem = (Element) document.getElementsByTagName(
                        BIRESPONSE).item(0);
                promoIdList = XMLParser.getBiRespose(responseElem);
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
        return promoIdList.toArray(new String[0]);
    }
    
    public String[] getRERecommendationMusic(ContentRequest contentRequest) {
        List<String> promoIdList = new ArrayList<String>();
        try {
            Parser parser = connector.makeWebServiceRequest(contentRequest,
                    api_Content, action_re_recommendation_music);
            
            Document document = parser.getDocument();
            
            if (document != null) {
                promoIdList = XMLParser.getBiReRespose(document);
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
        return promoIdList.toArray(new String[0]);
    }

    public String[] getTopTenMusic(ContentRequest contentRequest) {
        List<String> promoIdList = new ArrayList<String>();
        try {
            Parser parser = connector.makeWebServiceRequest(contentRequest,
                    api_Content, circle_top_ten);
            
            Document document = parser.getDocument();
            
            if (document != null) {
                Element responseElem = (Element) document.getElementsByTagName(
                        BIRESPONSE).item(0);
                promoIdList = XMLParser.getBiRespose(responseElem);
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
        return promoIdList.toArray(new String[0]);
    }

    public ClipRating getClipRating(ContentRequest contentRequest) {
        ClipRating clipRating = null;

        Map<Integer, ClipRating> clipRatings = getClipRatings(contentRequest);
        if (clipRatings != null && clipRatings.size() > 0) {
            Iterator<ClipRating> iterator = clipRatings.values().iterator();
            clipRating = iterator.next();
        }

        return clipRating;
    }

    public Map<Integer, ClipRating> getClipRatings(ContentRequest contentRequest) {
        Map<Integer, ClipRating> clipRatings = null;

        try {
            Parser parser = connector.makeWebServiceRequest(contentRequest,
                    api_WebService, action_getClipRating);
            
            Document document = parser.getDocument();
            
            if (document != null) {
                String response = getResponse(contentRequest, document);

                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element clipRatingsElem = (Element) document
                            .getElementsByTagName(CLIP_RATINGS).item(0);
                    clipRatings = XMLParser.getClipRatings(clipRatingsElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info("clipRatings: " + clipRatings);

        return clipRatings;
    }
    
    public Rbt getMobileAppRegistration(ApplicationDetailsRequest applicationRequest) {
    	Rbt rbt = null;
    	try {
            Parser parser = connector.makeWebServiceRequest(applicationRequest,
                    api_WebService, action_getMobileAppRegistration);
            
            Document document = parser.getDocument();
            
            if (document != null) {
                String response = getResponse(applicationRequest, document);

                if (response.equalsIgnoreCase(SUCCESS)) {
                	Element rbtElem = (Element) document.getElementsByTagName(
                            RBT).item(0);
                    rbt = XMLParser.getRBT(rbtElem, applicationRequest);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

    	return rbt;
    }

    public void rateClip(ContentRequest contentRequest) {
        clipRating(contentRequest, action_rateClip);
    }

    public void likeClip(ContentRequest contentRequest) {
        clipRating(contentRequest, action_likeClip);
    }

    public void dislikeClip(ContentRequest contentRequest) {
        clipRating(contentRequest, action_dislikeClip);
    }

    private void clipRating(ContentRequest contentRequest, String action) {
        try {
            Parser parser = connector.makeWebServiceRequest(contentRequest,
                    api_WebService, action);
            
            Document document = parser.getDocument();
            
            if (document != null)
                getResponse(contentRequest, document);
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }

    private String getResponse(Request request, Document document) {
        Element responseElem = (Element) document
                .getElementsByTagName(RESPONSE).item(0);
        Text responseText = (Text) responseElem.getFirstChild();
        String response = responseText.getNodeValue();
        request.setResponse(response); // set response text in Request object

        return response;
    }

    public RBTLoginUser getRBTUserPIN(
            ApplicationDetailsRequest applicationDetailsRequest) {
        return rbtUserPIN(applicationDetailsRequest, action_getUserPIN);
    }

    public RBTLoginUser setRBTUserPIN(
            ApplicationDetailsRequest applicationDetailsRequest) {
        return rbtUserPIN(applicationDetailsRequest, action_setUserPIN);
    }

    public void expireRBTUserPIN(
            ApplicationDetailsRequest applicationDetailsRequest) {
        rbtUserPIN(applicationDetailsRequest, action_expireUserPIN);
    }

    public String referUser(SelectionRequest selectionRequest) {

        try {
            Parser parser = connector.makeWebServiceRequest(
                    selectionRequest, api_WebService, action_refer);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(selectionRequest, document);
                return response;
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return null;

    }

    private RBTLoginUser rbtUserPIN(Request request, String action) {
        ApplicationDetails applicationDetails = null;

        try {
            Parser parser = connector.makeWebServiceRequest(request,
                    api_WebService, action);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(request, document);
                if (response.equalsIgnoreCase(SUCCESS)
                        || response.equalsIgnoreCase(ALREADY_REGISTERED)) {
                    Element applicationDetailsElem = (Element) document
                            .getElementsByTagName(APPLICATION_DETAILS).item(0);
                    applicationDetails = XMLParser
                            .getApplicationDetails(applicationDetailsElem);

                    if (applicationDetails != null
                            && applicationDetails.getRbtLoginUsers() != null
                            && applicationDetails.getRbtLoginUsers().length > 0
                            && applicationDetails.getRbtLoginUsers()[0] != null)
                        return applicationDetails.getRbtLoginUsers()[0];
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "applicationDetails: " + applicationDetails);

        return null;
    }

    public String addGCMRegistration(ApplicationDetailsRequest applicationDetailsRequest) {
        ApplicationDetails applicationDetails = null;
        String circleId = applicationDetailsRequest.getCircleID();
        String subscriberId = null ;
        try {
			ConnectorHandler connectorHandler = ComvivaConnectorFactory
					.getCVConnectorInstance(subscriberId, circleId, connector,
							configurations, api_WebService,
							action_addGCMRegistration);
			Connector tempConnector = connectorHandler.getConnector();
            Parser parser = tempConnector.makeWebServiceRequest(connectorHandler, applicationDetailsRequest,
                    api_WebService, action_addGCMRegistration);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(applicationDetailsRequest, document);
                return response;
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "applicationDetails: " + applicationDetails);

        return null;
    }

    public String removeGCMRegistration(ApplicationDetailsRequest applicationDetailsRequest) {
        ApplicationDetails applicationDetails = null;
        String circleId = applicationDetailsRequest.getCircleID();
        String subscriberId = null ;
         try {
			ConnectorHandler connectorHandler = ComvivaConnectorFactory
					.getCVConnectorInstance(subscriberId, circleId, connector,
							configurations, api_WebService,
							action_removeGCMRegistration);
			Connector tempConnector = connectorHandler.getConnector();
         	Parser parser = tempConnector.makeWebServiceRequest(connectorHandler, applicationDetailsRequest,
                    api_WebService, action_removeGCMRegistration);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(applicationDetailsRequest, document);
                return response;
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info(
                    "applicationDetails: " + applicationDetails);

        return null;
    }

    public Map<Integer, ChargeClass> getNextChargeClasses(
            SelectionRequest selectionRequest) {
        Map<Integer, ChargeClass> chargeClasses = null;

        try {
            Parser parser = connector
                    .makeWebServiceRequest(selectionRequest, api_WebService,
                            action_getNextChargeClass);
            
            Document document = parser.getDocument();
            
            if (document != null) {
                String response = getResponse(selectionRequest, document);

                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element chargeClassesElem = (Element) document
                            .getElementsByTagName(CHARGE_CLASSES).item(0);
                    chargeClasses = XMLParser
                            .getNextChargeClasses(chargeClassesElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info("chargeClasses: " + chargeClasses);

        return chargeClasses;
    }

    public ChargeClass getNextChargeClass(SelectionRequest selectionRequest) {
        ChargeClass chargeClass = null;

        Map<Integer, ChargeClass> chargeClasses = getNextChargeClasses(selectionRequest);
        if (chargeClasses != null && chargeClasses.size() > 0) {
            Iterator<ChargeClass> iterator = chargeClasses.values().iterator();
            chargeClass = iterator.next();
        }

        return chargeClass;
    }

    public ChargeClass getNextChargeClassForRMO(SelectionRequest selectionRequest) {
        ChargeClass chargeClass = null;

        Map<String, ChargeClass> chargeClasses = getNextChargeClassesForRMO(selectionRequest);
        if (chargeClasses != null && chargeClasses.size() > 0) {
            Iterator<ChargeClass> iterator = chargeClasses.values().iterator();
            chargeClass = iterator.next();
        }

        return chargeClass;
    }

    public Map<String, ChargeClass> getNextChargeClassesForRMO(
            SelectionRequest selectionRequest) {
        Map<String, ChargeClass> chargeClasses = null;

        try {
            Parser parser = connector
                    .makeWebServiceRequest(selectionRequest, api_WebService,
                            action_getNextChargeClass);
            
            Document document = parser.getDocument();
            
            if (document != null) {
                String response = getResponse(selectionRequest, document);

                if (response.equalsIgnoreCase(SUCCESS)) {
                    Element chargeClassesElem = (Element) document
                            .getElementsByTagName(CHARGE_CLASSES).item(0);
                    chargeClasses = XMLParser
                            .getNextChargeClassesForRMO(chargeClassesElem);
                }
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        if (configurations.getLogger().isInfoEnabled())
            configurations.getLogger().info("chargeClasses: " + chargeClasses);

        return chargeClasses;
    }

    public String bulkAddTransData(
            BulkUpdateDetailsRequest bulkUpdateDetailsRequest) {
        String response = ERROR;
        try {
            bulkUpdateDetailsRequest.setType(iRBTConstant.TYPE_ZOOMIN);
            Parser parser = connector.makeWebServiceRequest(
                    bulkUpdateDetailsRequest, api_WebService,
                    action_addTransData);
            
            Document document = parser.getDocument();
            
            if (document != null) {
                String uploadResponse = getResponse(bulkUpdateDetailsRequest,
                        document);

                if (uploadResponse != null)
                    response = uploadResponse;
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }

        return response;
    }

    public Rbt addMultipleSubscriberSelections(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection,
                action_addMultipleSettings);
        return rbt;
    }

    public Rbt addMultipleSubscriberDownloads(SelectionRequest selectionRequest) {
        Rbt rbt = rbt(selectionRequest, api_Selection,
                action_addMultipleDownloads);
        return rbt;
    }

    public Library deleteMultipleSubscriberSelections(
            SelectionRequest selectionRequest) {
        Library library = null;
        Rbt rbt = rbt(selectionRequest, api_Selection,
                action_deleteMultipleSettings);
        if (rbt != null)
            library = rbt.getLibrary();

        return library;
    }

    public Library deleteMultipleSubscriberDownloads(
            SelectionRequest selectionRequest) {
        Library library = null;
        Rbt rbt = rbt(selectionRequest, api_Selection,
                action_deleteMultipleTones);
        if (rbt != null)
            library = rbt.getLibrary();

        return library;
    }
    
    public GroupDetails editGroup(GroupRequest groupRequest) {
        GroupDetails groupDetails = group(groupRequest, action_update);
        return groupDetails;
    }
    
    
    public void downloadFile(RBTDownloadFileRequest downloadFileRequest) {
    	try {
            Parser parser = connector.makeWebServiceRequest(downloadFileRequest,
                    api_RBTDownloadFile, null);
            
            Document document = parser.getDocument();
            
            if (document != null)
                getResponse(downloadFileRequest, document);
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
    }
    
    public String[] getSearchContent(ContentRequest contentRequest) {
        List<String> clipIdList = new ArrayList<String>();
        try {
            Parser parser = connector.makeWebServiceRequest(contentRequest,
                    api_Search, null);
            
            Document document = parser.getDocument();
            
            if (document != null) {
                clipIdList = XMLParser.getSearchResponse(document);
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
        return clipIdList.toArray(new String[0]);
    }
    
    public String getOrSetNotificationStatus(ApplicationDetailsRequest applicationDetailsRequest) {

        try {
            Parser parser = connector.makeWebServiceRequest(applicationDetailsRequest,
                    api_WebService, action_getOrSetNotificationStatus);
            
            Document document = parser.getDocument();

            if (document != null) {
                String response = getResponse(applicationDetailsRequest, document);
                return response;
            }
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
        return null;
    }
    
	public Document groupAction(GroupRequest groupRequest, String action) {
		Document document = null;
		try {
			Parser parser = connector.makeWebServiceRequest(groupRequest, api_Group,
					action);
			document = parser.getDocument();

			if (document != null)
				getResponse(groupRequest, document);
		} catch (Exception e) {
			configurations.getLogger().error(e.getMessage(), e);
		}
		return document;
	}

	public Document searchAction(ContentRequest contentRequest) {

		Document document = null;
		try {
			Parser parser = connector.makeWebServiceRequest(contentRequest,
					api_Search, null);
			document = parser.getDocument();
			if (document != null) {
				XMLParser.getSearchResponse(document);
			}
		} catch (Exception e) {
			configurations.getLogger().error(e.getMessage(), e);
		}
		return document;
	}

	public Document rbtAction(RbtDetailsRequest detailsRequest) {
		Document document = null;
		try {
			Parser parser = connector.makeWebServiceRequest(detailsRequest, api_Rbt,
					null);
			
			document = parser.getDocument();

			if (document != null)
				getResponse(detailsRequest, document);
		} catch (Exception e) {
			configurations.getLogger().error(e.getMessage(), e);
		}
		return document;

	}
	public Document subscriberAction(SubscriptionRequest detailsRequest, String action) {
		Document document = null;
		try {
			Parser parser = connector.makeWebServiceRequest(detailsRequest, api_Subscription,
					action);
			document = parser.getDocument();
			if (document != null)
				getResponse(detailsRequest, document);
		} catch (Exception e) {
			configurations.getLogger().error(e.getMessage(), e);
		}
		return document;

	}

	public Document getContent(ContentRequest contentRequest) {
		Document document = null;
		try {
			Parser parser = connector.makeWebServiceRequest(contentRequest,
					api_Content, null);
			document = parser.getDocument();
		} catch (Exception e) {
			configurations.getLogger().error(e.getMessage(), e);
		}
		return document;

	}

	public Document selectionAction(SelectionRequest selectionRequest,
			String action) {
		Document document = null;
		try {
			Parser parser = connector.makeWebServiceRequest(selectionRequest,
					api_Selection, action);
			document = parser.getDocument();
			if (document != null)
				getResponse(selectionRequest, document);
		} catch (Exception e) {
			configurations.getLogger().error(e.getMessage(), e);
		}
		return document;

	}
	
	//RBT-13537 VF IN:: 2nd Consent Reporting for RBT
	public String loggingCDR(Request request) {
		   String response = null;
	        try {
	            Parser parser = connector.makeWebServiceRequest(request,
	                    api_WebService, action_Logging_CDR);
	            Document document = parser.getDocument();
	            if (document != null) {
	                response = getResponse(request, document);
	                configurations.getLogger().info("response in loggingCDR: "+response);
	            }
	        } catch (Exception e) {
	            configurations.getLogger().error(e.getMessage(), e);
	        }
	       return response;
	 }
	
	 //RBT-15403
	public List<RBTProtocolBean> getProtocolNumForBlockUnblock(RbtDetailsRequest rbtDetailsRequest){
	     try {
	            Parser parser = connector.makeWebServiceRequest(rbtDetailsRequest,
	                    api_WebService, action_Protocol_Numb);
	            Document document = parser.getDocument();
	            if (document != null) {
	            	return populateProtocolBeanList( document );
	            }
	        } catch (Exception e) {
	            configurations.getLogger().error(e.getMessage(), e);
	        }
	     return null;
	}
	
	/**
	 * Generate List of protocol bean from the xml document
	 * @param document
	 * @return
	 * @throws ParseException 
	 */
	private List<RBTProtocolBean> populateProtocolBeanList(Document document) throws ParseException {
		List<RBTProtocolBean> protocolNumList = new ArrayList<RBTProtocolBean>();
		NodeList nList = document.getElementsByTagName(PROTOCOL);
		String transactionType=null,subscriberId=null,protocolNum = null,requestTime = null;
		for (int temp = 0; temp < nList.getLength(); temp++) {
			 RBTProtocolBean bean = new RBTProtocolBean();
			 Node nNode = nList.item(temp);
			 if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				 
				 Element eElement = (Element) nNode;
				 transactionType = eElement.getElementsByTagName("transactionType").item(0).getTextContent();
				 subscriberId = eElement.getElementsByTagName("subscriberId").item(0).getTextContent();
				 protocolNum = eElement.getElementsByTagName("protocolNum").item(0).getTextContent();
				 requestTime = eElement.getElementsByTagName("requestTime").item(0).getTextContent();
				 
				 bean.setProtocolNum(Long.parseLong(protocolNum));
				 bean.setTransactionType(transactionType);
				 bean.setSubscriberId(subscriberId);
				 bean.setRequestTime(requestTime);
				 
				 protocolNumList.add(bean);
			 }
		}
		return protocolNumList;
	}

	public CurrentPlayingSongWSResponseBean getCurrentPlayingSong(
			ApplicationDetailsRequest applicationDetailsRequest) {
		CurrentPlayingSongWSResponseBean bean = new CurrentPlayingSongWSResponseBean();
		try {
			Parser parser = connector.makeWebServiceRequest(
					applicationDetailsRequest, api_WebService,
					action_getCurrentPlayingSong);
			Document document = parser.getDocument();
			if (document != null) {
				String response = getResponse(applicationDetailsRequest,
						document);
				if (response != null) {
					if (response.equals(SUCCESS)) {
						Element clipIdElem = (Element) document
								.getElementsByTagName(WAV_FILE).item(0);
						
						Element catIdElem = (Element) document
								.getElementsByTagName(CATEGORY_ID).item(0);
						
						bean.setWavFileName(clipIdElem.getTextContent());
						bean.setCategoryId(Integer.parseInt(catIdElem.getTextContent()));
						bean.setResponseStr(SUCCESS);
					} else if (response.equals(ERROR)) {
						bean.setResponseStr(ERROR);
						Element retryElem = (Element) document
								.getElementsByTagName("RETRY").item(0);
						bean.setResponseCode(retryElem.getTextContent());
					}
				}
			}
		} catch (Exception e) {
			configurations.getLogger().error(e.getMessage(), e);
		}
		return bean;
	}
	
	
	public String getCallLogHistory(Request  restrequest) {
		String jsonResponse =null;
        try {
             jsonResponse = rbtRest(restrequest, api_CallLog, null);
        } catch (Exception e) {
            configurations.getLogger().error(e.getMessage(), e);
        }
        return jsonResponse;
    }
	
	private String rbtRest(Request restrequest, String api, String action) throws Exception {
		String response = null ;
		try {
			 response = connector.makeRestRequest(restrequest, api, action);
		} catch (Exception e) {
			configurations.getLogger().error(e.getMessage(), e);
		}
		return response;
	}
	
	
	public UDPResponseBean createUDP(Request request){
		if(request !=null && request.getIsDtoCRequest().equals(YES)){
		}
		return null;
	}
	
	public UDPResponseBean updateUDP(Request request){
		if(request !=null && request.getIsDtoCRequest().equals(YES)){
		}
		return null;
	}
	
	public List<UDPResponseBean> getAllUDP(Request request){
		return null;
	}
	
	public String removeUDP(Request request){
		return null;
	}
	
	public String addContentToUDP(Request request){
		return null;
	}
	
	public String removeContentFromUDP(Request request){
		return null;
	}

	public UDPResponseBean getAllContentsFromUDP(Request request){
		return null;
	}
	
	public Rbt processUDPSelection(SelectionRequest selectionRequest) {
    	Rbt rbt = rbt(selectionRequest, api_Selection, ACTION_SET_UDP);
        return rbt;
    }
	
	public Rbt processUDPDeactivation(SelectionRequest selectionRequest) {
    	Rbt rbt = rbt(selectionRequest, api_Selection, ACTION_DEACT_UDP);
        return rbt;
    }
	
	public Settings getSettingsForPlayRule(RbtDetailsRequest rbtDetailsRequest) {
		Settings settings = getSettings(rbtDetailsRequest);
		return settings;
	}
	
	  public NextServiceCharge getNextServiceCharge(
			SelectionRequest selectionRequest) {
		NextServiceCharge nextServiceCharge = null;

		try {
			Parser parser = connector.makeWebServiceRequest(selectionRequest,
					api_WebService, action_getNextServiceCharge);

			Document document = parser.getDocument();

			if (document != null) {
				String response = getResponse(selectionRequest, document);

				if (response.equalsIgnoreCase(SUCCESS)) {
					Element chargeClassesElem = (Element) document
							.getElementsByTagName(NEXTSERVICE_CHARGE).item(0);
					nextServiceCharge = XMLParser
							.getNextServiceChargeClass(chargeClassesElem);
				}
			}
		} catch (Exception e) {
			configurations.getLogger().error(e.getMessage(), e);
		}

		if (configurations.getLogger().isInfoEnabled())
			configurations.getLogger().info(
					"chargeClasses: " + nextServiceCharge);

		return nextServiceCharge;
	}

	public Rbt processUpgradeDownload(RbtDetailsRequest rbtDetailsRequest) {
		Rbt rbt = rbt(rbtDetailsRequest, api_Selection, action_upgradeDownload);
		return rbt;
	}
	
}
