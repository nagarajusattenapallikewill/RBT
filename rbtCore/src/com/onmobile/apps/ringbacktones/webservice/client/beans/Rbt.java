 package com.onmobile.apps.ringbacktones.webservice.client.beans;

import java.util.Arrays;

/**
 * @author vasipalli.sreenadh
 * @author vinayasimha.patil
 *
 */
public class Rbt
{
	private Subscriber subscriber = null;
	private GiftInbox giftInbox = null;
	private GiftOutbox giftOutbox = null;
	private Library library = null;
	private Bookmarks bookmarks = null;
	private GroupDetails groupDetails = null;
	private SMSHistory[] smsHistory = null;
	private Transaction[] transactions = null;
	private CallDetails callDetails = null;
	private SubscriberPromo subscriberPromo = null;
	private Offer[] allOffers = null;
	private BiDownloadHistory[] biDownloadHistories = null;
	private SubscriberPack[] subscriberPacks = null;
	private WCHistory[] wcHistory = null;
	private Consent consent = null;
	private MobileAppRegistration mobileAppRegistration = null;
	private SMSHistory[] smsHistoryFromUMP = null;
	private Consents consents = null;
	private ViralData[] viralData = null;
	private Downloads miPlaylist = null;

	/**
	 * @return the miPlaylist
	 */
	public Downloads getmiPlaylist() {
		return miPlaylist;
	}

	/**
	 * @param miPlaylist the miPlaylist to set
	 */
	public void setmiPlaylist(Downloads miPlaylist) {
		this.miPlaylist = miPlaylist;
	}

	/**
	 * 
	 */
	public Rbt()
	{

	}

	/**
	 * @param subscriber
	 * @param giftInbox
	 * @param giftOutbox
	 * @param library
	 * @param bookmarks
	 * @param groupDetails
	 * @param smsHistory
	 * @param transactions
	 * @param callDetails
	 * @param subscriberPromo
	 */
	public Rbt(Subscriber subscriber, GiftInbox giftInbox,
			GiftOutbox giftOutbox, Library library, Bookmarks bookmarks,
			GroupDetails groupDetails, SMSHistory[] smsHistory,
			Transaction[] transactions, CallDetails callDetails,
			SubscriberPromo subscriberPromo, Offer[] allOffers)
	{
		this.subscriber = subscriber;
		this.giftInbox = giftInbox;
		this.giftOutbox = giftOutbox;
		this.library = library;
		this.bookmarks = bookmarks;
		this.groupDetails = groupDetails;
		this.smsHistory = smsHistory;
		this.transactions = transactions;
		this.callDetails = callDetails;
		this.subscriberPromo = subscriberPromo;
		this.allOffers = allOffers;
	}

	/**
	 * @return the subscriber
	 */
	public Subscriber getSubscriber()
	{
		return subscriber;
	}

	/**
	 * @return the giftInbox
	 */
	public GiftInbox getGiftInbox()
	{
		return giftInbox;
	}

	/**
	 * @return the giftOutbox
	 */
	public GiftOutbox getGiftOutbox()
	{
		return giftOutbox;
	}

	/**
	 * @return the library
	 */
	public Library getLibrary()
	{
		return library;
	}

	/**
	 * @return the bookmarks
	 */
	public Bookmarks getBookmarks()
	{
		return bookmarks;
	}

	/**
	 * @return the groupDetails
	 */
	public GroupDetails getGroupDetails()
	{
		return groupDetails;
	}

	/**
	 * @return the smsHistory
	 */
	public SMSHistory[] getSmsHistory()
	{
		return smsHistory;
	}

	/**
	 * @return the transactions
	 */
	public Transaction[] getTransactions()
	{
		return transactions;
	}

	/**
	 * @return the callDetails
	 */
	public CallDetails getCallDetails()
	{
		return callDetails;
	}

	/**
	 * @return the subscriberPromo
	 */
	public SubscriberPromo getSubscriberPromo()
	{
		return subscriberPromo;
	}
	
	public Offer[] getOffers() {
		return allOffers;
	}

	/**
	 * 
	 * @return the biDownloadHistories
	 */
	public BiDownloadHistory[] getBiDownloadHistories() {
		return biDownloadHistories;
	}
	
	/**
	 * @param subscriber the subscriber to set
	 */
	public void setSubscriber(Subscriber subscriber)
	{
		this.subscriber = subscriber;
	}

	/**
	 * @param giftInbox the giftInbox to set
	 */
	public void setGiftInbox(GiftInbox giftInbox)
	{
		this.giftInbox = giftInbox;
	}

	/**
	 * @param giftOutbox the giftOutbox to set
	 */
	public void setGiftOutbox(GiftOutbox giftOutbox)
	{
		this.giftOutbox = giftOutbox;
	}

	/**
	 * @param library the library to set
	 */
	public void setLibrary(Library library)
	{
		this.library = library;
	}

	/**
	 * @param bookmarks the bookmarks to set
	 */
	public void setBookmarks(Bookmarks bookmarks)
	{
		this.bookmarks = bookmarks;
	}

	/**
	 * @param groupDetails the groupDetails to set
	 */
	public void setGroupDetails(GroupDetails groupDetails)
	{
		this.groupDetails = groupDetails;
	}

	/**
	 * @param smsHistory the smsHistory to set
	 */
	public void setSmsHistory(SMSHistory[] smsHistory)
	{
		this.smsHistory = smsHistory;
	}

	/**
	 * @param transactions the transactions to set
	 */
	public void setTransactions(Transaction[] transactions)
	{
		this.transactions = transactions;
	}

	/**
	 * @param callDetails the callDetails to set
	 */
	public void setCallDetails(CallDetails callDetails)
	{
		this.callDetails = callDetails;
	}

	/**
	 * @param subscriberPromo the subscriberPromo to set
	 */
	public void setSubscriberPromo(SubscriberPromo subscriberPromo)
	{
		this.subscriberPromo = subscriberPromo;
	}
	
	public void setOffers(Offer[] allOffers) {
		this.allOffers = allOffers;
	}

	/**
	 * @param biDownloadHistories the biDownloadHistories to set
	 */
	public void setBiDownloadHistories(BiDownloadHistory[] biDownloadHistories) {
		this.biDownloadHistories = biDownloadHistories;
	}
	
	/**
	 * 
	 * @return the subscriberPacks
	 */
	public SubscriberPack[] getSubscriberPacks() {
		return subscriberPacks;
	}
	/**
	 * @param subscriberPacks the subscriberPacks to set
	 */
	public void setSubscriberPacks(SubscriberPack[] subscriberPacks) {
		this.subscriberPacks = subscriberPacks;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bookmarks == null) ? 0 : bookmarks.hashCode());
		result = prime * result + ((callDetails == null) ? 0 : callDetails.hashCode());
		result = prime * result + ((giftInbox == null) ? 0 : giftInbox.hashCode());
		result = prime * result + ((giftOutbox == null) ? 0 : giftOutbox.hashCode());
		result = prime * result + ((groupDetails == null) ? 0 : groupDetails.hashCode());
		result = prime * result + ((library == null) ? 0 : library.hashCode());
		result = prime * result + Arrays.hashCode(smsHistory);
		result = prime * result + Arrays.hashCode(wcHistory);
		result = prime * result + ((subscriber == null) ? 0 : subscriber.hashCode());
		result = prime * result + ((subscriberPromo == null) ? 0 : subscriberPromo.hashCode());
		result = prime * result + Arrays.hashCode(transactions);
		result = prime * result + Arrays.hashCode(biDownloadHistories);
		result = prime * result + Arrays.hashCode(subscriberPacks);
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Rbt))
			return false;
		Rbt other = (Rbt) obj;
		if (bookmarks == null)
		{
			if (other.bookmarks != null)
				return false;
		}
		else if (!bookmarks.equals(other.bookmarks))
			return false;
		if (callDetails == null)
		{
			if (other.callDetails != null)
				return false;
		}
		else if (!callDetails.equals(other.callDetails))
			return false;
		if (giftInbox == null)
		{
			if (other.giftInbox != null)
				return false;
		}
		else if (!giftInbox.equals(other.giftInbox))
			return false;
		if (giftOutbox == null)
		{
			if (other.giftOutbox != null)
				return false;
		}
		else if (!giftOutbox.equals(other.giftOutbox))
			return false;
		if (groupDetails == null)
		{
			if (other.groupDetails != null)
				return false;
		}
		else if (!groupDetails.equals(other.groupDetails))
			return false;
		if (library == null)
		{
			if (other.library != null)
				return false;
		}
		else if (!library.equals(other.library))
			return false;
		if (!Arrays.equals(smsHistory, other.smsHistory))
			return false;

		if (!Arrays.equals(wcHistory, other.wcHistory))
			return false;

		if (subscriber == null)
		{
			if (other.subscriber != null)
				return false;
		}
		else if (!subscriber.equals(other.subscriber))
			return false;
		if (subscriberPromo == null)
		{
			if (other.subscriberPromo != null)
				return false;
		}
		else if (!subscriberPromo.equals(other.subscriberPromo))
			return false;
		if (!Arrays.equals(transactions, other.transactions))
			return false;
		if (!Arrays.equals(biDownloadHistories, other.biDownloadHistories))
			return false;
		if (!Arrays.equals(subscriberPacks, other.subscriberPacks))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("Rbt[bookmarks = ");
		builder.append(bookmarks);
		builder.append(", callDetails = ");
		builder.append(callDetails);
		builder.append(", giftInbox = ");
		builder.append(giftInbox);
		builder.append(", giftOutbox = ");
		builder.append(giftOutbox);
		builder.append(", groupDetails = ");
		builder.append(groupDetails);
		builder.append(", library = ");
		builder.append(library);
		builder.append(", smsHistory = ");
		builder.append(Arrays.toString(smsHistory));
		builder.append(", wcHistory = ");
		builder.append(Arrays.toString(wcHistory));
		builder.append(", subscriber = ");
		builder.append(subscriber);
		builder.append(", subscriberPromo = ");
		builder.append(subscriberPromo);
		builder.append(", transactions = ");
		builder.append(Arrays.toString(transactions));
		builder.append(", offers = ");
		builder.append(Arrays.toString(allOffers));
		builder.append(", biDownloadHistory = ");
		builder.append(Arrays.toString(biDownloadHistories));
		builder.append(", subscriberPacks = ");
		builder.append(Arrays.toString(subscriberPacks));
		builder.append(", consent = ");
		builder.append(consent);
		builder.append(", mobileAppRegistration = ");
		builder.append(mobileAppRegistration);
		builder.append(", smsHistoryFromUMP = ");
		builder.append(smsHistoryFromUMP);
		builder.append(", viralData = ");
		builder.append(viralData);
		builder.append(", miPlaylist = ");
		builder.append(miPlaylist);
		builder.append("]");
		return builder.toString();
	}

	public WCHistory[] getWcHistory() {
		return wcHistory;
	}

	public void setWcHistory(WCHistory[] wcHistory) {
		this.wcHistory = wcHistory;
	}

	public Consent getConsent() {
		return consent;
	}

	public void setConsent(Consent consent) {
		this.consent = consent;
	}
	
	public void setMobileAppRegistration(MobileAppRegistration mobileAppRegistration) {
		this.mobileAppRegistration = mobileAppRegistration;
	}
	
	public MobileAppRegistration getMobileAppRegistration() {
		return mobileAppRegistration;
	}

	public SMSHistory[] getSmsHistoryFromUMP() {
		return smsHistoryFromUMP;
	}

	public void setSmsHistoryFromUMP(SMSHistory[] smsHistoryFromUMP) {
		this.smsHistoryFromUMP = smsHistoryFromUMP;
	}
	public Consents getConsents() {
		return consents;
	}

	public void setConsents(Consents consents) {
		this.consents = consents;
	}

	public ViralData[] getViralData() {
		return viralData;
	}

	public void setViralData(ViralData[] viralData) {
		this.viralData = viralData;
	}
}
