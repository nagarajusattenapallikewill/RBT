package com.onmobile.apps.ringbacktones.ussd;


public class USSDConstants {
	/*author@Abhinav Anand
	 */
	//Added for giftinbox
	public String giftInbox="Cadourile mele";
	public String urlForSubDetails="Rbt.do?subscriberID=%subId%&info=subscriber&calledNo=%ussdNo%&mode=USSD";
	public  String urlForGiftInbox="Rbt.do?subscriberID=%subId%&info=gift_inbox&calledNo=%ussdNo%&mode=USSD";
	public  String urlForLibraryDetails="Rbt.do?subscriberID=%subId%&info=library&calledNo=%ussdNo%&mode=USSD";
	public  String urlForCallDetails="Rbt.do?subscriberID=%subId%&info=call_details&calledNo=%ussdNo%&mode=USSD";
	//for default caller, caller_id="all"
	public  String urlForCopySelection="Copy.do?action=set&subscriberID=%subId%&fromSubscriber=%copyNo%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&status=1&calledNo=%ussdNo%&mode=USSD";
	public  String urlForSendingGift="Gift.do?action=send_gift&gifterID=%subId%&gifteeID=%gifteeNo%&toneID=%clipId%&categoryID=%catId%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForDeactivation="Subscription.do?action=deactivate&subscriberID=%subId%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForMonthlyActivation="Subscription.do?action=activate&subscriberID=%subId%&isPrepaid=%isPrepaid%&language=%lan%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForAdvancePackActivation="Subscription.do?action=activate&subscriberID=%subId%&isPrepaid=%isPrepaid%&language=%lan%&calledNo=%ussdNo%&mode=USSD&rentalPack=%advancePack%";
	public  String urlForSelection="Selection.do?action=set&subscriberID=%subId%&isPrepaid=%isPrepaid%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForValidatingGiftee="ValidateNumber.do?action=gift&subscriberID=%subId%&number=%giftee%&toneID=%clipId%&categoryID=%catId%";
	
	public  String mainMenuURL="`#`http://localhost:9910/ussd/ussd.do?msisdn=$subscriber$&processId=0&pageNo=0";
	//mainMenuURL="`#`"+defaultURL+"&processId=0&pageNo=0";
	public  String defaultURL="http://localhost:9910/ussd/ussd.do?msisdn=$subscriber$";
	public  String askForSpecialCallerMsg="Please enter the mobile number";
	public String msearchMsg="Please enter the name of the song or the movie/album you are looking for";
	public boolean timeOfTheDayAllowed=true;
	public boolean dayOfTheWeekAllowed=true;
	public boolean futureDayAllowed=true;
	public  String calledNo="*678#";
	public  String strTechnicalDifficultyMsg="Try later";
	public  String strInvalidRequestMsg="Invalid request";
	//Selection already exist.
	public  String strInvalidSelRequestMsg="Selection already exist";
	

	public  String strSuccessMsg="OK. Success !!!";
	public String contentType="application/X-USSD-request+ans";
	public String contentTypeNotification="plain/X-USSD-notification";
	public String contentTypeAnswer="plain/X-USSD-request";
	public  String backEndInvalidReponseMsg="Invalid Response";
	public  String backEndSuccessMsg="success";
	public  String backEndTechnicalDifficultyMsg="Try later";
	public String backEndSongAlreadyExistMsg="already_exist";
	

	public  boolean unsubscriptionAllowed=true;
	public  boolean defaultSubscritpionAllowed=false;
	public  boolean advanceSubscriptionAllowed=true;
	public int maxClipsNo=100;
	public  String newLineCharString="\n";
	public String browseMenuStr="Browse";
	public  String moreStr="0.More";
	public  String enterChoiceStr="\nEnter ur choice no.:*g for generic help";
	public  int maxStrlengthlimit=140;
	public String seperatorChar="";
	public String endURLChar="";
	public  String timeOfTheDayOptionMsg="Do you wish to set this song for a particular time of the day?";
	public  String timeOfTheDayValueMsg="Please enter the time(HH HH):e.g. 11 12";
	public  String dayOfTheWeekValueMsg="Please enter day of the week.e.g:<br>1-Sun<br>2-Mon<br>3-Tue<br>4-Wed<br>5-Thu<br>6-Fri<br>7-Sat";
	public  String futureDateValueMsg="Please enter the future date(ddMMyyyy)e.g:29122009";
	public String answerStr="ANSWER";
	public String rejectStr="NO";
	public  String defaultSelOption="Select for all callers";
	public  String specialCallerSelOption="Select for one caller";
	public  String giftOption="Gift it to friends";
	public  String prelistenOption="Prelisten";
	public String defaultSubCost="30";
	public String defaultSelCost="30";
	public String giftAllowedInBrowse="true";
	public String specialCallerAllowedInBrowse="true";
	public String preListenAllowedInBrowse="true";
	public String confirmUnsubcriptionMsg="Your HT profie and songs will be deleted if you choose to unsubscriber.Press 1 to confirm,*1 for previous menu,# for main menu";
	public String confirmSubMsg="You will be charged Rs%amount% as subscription for %period% pack.Press 1 to continue,*1 to previoous menu,# for main menu";
	public  String selChargingMsg="You will be charged Rs 20 for this song Rs 30 as monthly subscription.Press 1 to continue,*1 for previous menu,# for main menu";
	public  String giftChargingMsgForSubGiftee="You will be Charged Rs 20 for song.Press 1 to continue.Press # to go to main menu";
	public  String giftChargingMsg="You will be Charged Rs 20 for song.Additional Rs 30 for monthly subscription if giftee is not an active user.Press 1 to continue.Press # to go to main menu";
	public  String giftChargingMsgForUnsubGiftee="You will be Charged Rs 20 for song and additional Rs 30 for monthly subscription.Press 1 to continue.Press # to go to main menu";
	public  String prelistenMsg="To prelisten the song please exit this menu and Dial %nameWavFile% at Rs3/minute.Press *1 to go back to the previous menu.Press *1 Go one level up ";
	public  String songSelSuccessmsg="OK.Press *1 to Go one level up";
	//
	public  String giftSelSuccessmsg="Thank you for gifting song/service";
	public  String freeSongSelSuccessmsg="Thank you for request.The same would be activated within 24 hours.Press 1 to continue,*1 for previous menu,# for main menu";
	public  String favoriteSelSuccessmsg="Thank you for selecting the clip.The clip will be set in loop within 24 hours.";
	public  String listRBTOptionMsg="@@1.Gift to your friends<br>@@2.Set as RBT";
	public   String ussdParentCat="2";
	public   String ussdCatsNotInBrowse="3,4";
	public   String ussdCatsFreeZone="3";
	public   String whatsHotMsg="You can now copy an Airtel Subscriber's Hello Tune by pressing * when you call him(before the is call picked up)Chrgs:HT subscription Rs.30/month,Rs20/song Press *1 to Go one level up";
	public   String copyMsg="Please enter the Airtel numbe,you want to copy Hello Tune from. e.g.-9945981517";
	public   String copyReconfirmMsg="You will be charged Rs20 to download this song and additional Rs30 as monthly subscription if you are not a Hello Tune customer.Press 1 to continue or press *1 for previous menu";
	public   String cricketMsg="Play the latest scores to your caller with Cricket Hello Tunes.Call 543211678 to subscribe.Charges:Rs.30 for entire series.When no match is on,callers will hear your default tune Press *1 Go one level up";
	public String tariffMsg="Tarife: 0,70 sau 1,07 euro sa cumperi un ton si 0,30 euro/luna abonamantul la serviciu, TVA inclus.";
	public   String helpMsg="1.Browsing the Hello Tunes Menu";
	public   String helloTunesHelpMsg="Press *1 to go back to " +
			"the previous menu.Press # to go back to the Hello Tunes Menu.Press *1 to Go one level up.";
	public   String copyURL="&request_type=copy&ans=$answer$";
	public   String mSearchURL=null;
	public   String menuOrder="MSearch,FreeZone,WhatsHot,Browse,Cricket,Copy,Manage,Help";
	public   boolean specialCallerForCopyAllowed=false;
	public   boolean toGiveBackNMainMenuOptionInResponse=true;
	public String msearchConfirmMsg="We will confirm your selection";
	public String countryPrefix="91";
	public String responseKeyWithMissingSeperator="ussd,response,msisdn,service";
	
	public String getTariffMsg() {
		return tariffMsg;
	}
	public void setTariffMsg(String tariffMsg) {
		this.tariffMsg = tariffMsg;
	}
	public String getResponseKeyWithMissingSeperator() {
		return responseKeyWithMissingSeperator;
	}
	public String getCountryPrefix() {
		return countryPrefix;
	}
	public String getEndURLChar() {
		return endURLChar;
	}
	public String getDayOfTheWeekValueMsg() {
		return dayOfTheWeekValueMsg;
	}
	public String getFutureDateValueMsg() {
		return futureDateValueMsg;
	}
	public String getGiftChargingMsg() {
		return giftChargingMsg;
	}
	public String getGiftChargingMsgForSubGiftee() {
		return giftChargingMsgForSubGiftee;
	}
	public String getGiftChargingMsgForUnsubGiftee() {
		return giftChargingMsgForUnsubGiftee;
	}
	public String getGiftSelSuccessmsg() {
		return giftSelSuccessmsg;
	}
	public String getUrlForValidatingGiftee() {
		return urlForValidatingGiftee;
	}
public boolean isDayOfTheWeekAllowed() {
	return dayOfTheWeekAllowed;
}
public boolean isFutureDayAllowed() {
	return futureDayAllowed;
}
public boolean isTimeOfTheDayAllowed() {
	return timeOfTheDayAllowed;
}
	public String getListRBTOptionMsg() {
		return listRBTOptionMsg;
	}
	public String getFavoriteSelSuccessmsg() {
		return favoriteSelSuccessmsg;
	}
	public String getMsearchConfirmMsg() {
		return msearchConfirmMsg;
	}
	public String getContentTypeAnswer() {
		return contentTypeAnswer;
	}
	public String getContentTypeNotification() {
		return contentTypeNotification;
	}
	public String getTimeOfTheDayValueMsg() {
		return timeOfTheDayValueMsg;
	}
	public String getDefaultSelCost() {
		return defaultSelCost;
	}
	public String getRejectStr() {
		return rejectStr;
	}
	public String getAnswerStr() {
		return answerStr;
	}
	public String getTimeOfTheDayOptionMsg() {
		return timeOfTheDayOptionMsg;
	}
	public String getContentType() {
		return contentType;
	}
	public String getBrowseMenuStr() {
		return browseMenuStr;
	}
	public int getMaxClipsNo() {
		return maxClipsNo;
	}
	public String getGiftAllowedInBrowse() {
		return giftAllowedInBrowse;
	}
	public String getPreListenAllowedInBrowse() {
		return preListenAllowedInBrowse;
	}
	public String getSpecialCallerAllowedInBrowse() {
		return specialCallerAllowedInBrowse;
	}
	public boolean getToGiveBackNMainMenuOptionInResponse() {
		return toGiveBackNMainMenuOptionInResponse;
	}
	public String getSeperatorChar() {
		return seperatorChar;
	}
	public String getFreeSongSelSuccessmsg() {
		return freeSongSelSuccessmsg;
	}
	public String getMsearchMsg() {
		return msearchMsg;
	}
	public String getConfirmSubMsg() {
		return confirmSubMsg;
	}
	public String getConfirmUnsubcriptionMsg() {
		return confirmUnsubcriptionMsg;
	}
	public String getDefaultSubCost() {
		return defaultSubCost;
	}
	public  boolean isAdvanceSubscriptionAllowed() {
		return advanceSubscriptionAllowed;
	}

	public  String getAskForSpecialCallerMsg() {
		return askForSpecialCallerMsg;
	}

	public  String getBackEndInvalidReponseMsg() {
		return backEndInvalidReponseMsg;
	}

	public  String getBackEndSuccessMsg() {
		return backEndSuccessMsg;
	}

	public  String getBackEndTechnicalDifficultyMsg() {
		return backEndTechnicalDifficultyMsg;
	}

	public  String getCalledNo() {
		return calledNo;
	}

	public  String getCopyMsg() {
		return copyMsg;
	}

	public  String getCopyReconfirmMsg() {
		return copyReconfirmMsg;
	}

	public  String getCopyURL() {
		return copyURL;
	}

	public  String getCricketMsg() {
		return cricketMsg;
	}

	public  String getDefaultSelOption() {
		return defaultSelOption;
	}

	public  boolean isDefaultSubscritpionAllowed() {
		return defaultSubscritpionAllowed;
	}

	public  String getDefaultURL() {
		return defaultURL;
	}

	public  String getEnterChoiceStr() {
		return enterChoiceStr;
	}
	public  String getGiftOption() {
		return giftOption;
	}

	public  String getHelloTunesHelpMsg() {
		return helloTunesHelpMsg;
	}

	public  String getHelpMsg() {
		return helpMsg;
	}

	public  String getMainMenuURL() {
		return mainMenuURL;
	}

	public  int getMaxStrlengthlimit() {
		return maxStrlengthlimit;
	}

	public  String getMenuOrder() {
		return menuOrder;
	}

	public  String getMoreStr() {
		return moreStr;
	}

	public  String getMSearchURL() {
		return mSearchURL;
	}
	public String getBackEndSongAlreadyExistMsg() {
		return backEndSongAlreadyExistMsg;
	}
	public  String getNewLineCharString() {
		return newLineCharString;
	}

	public  String getPrelistenMsg() {
		return prelistenMsg;
	}

	public  String getPrelistenOption() {
		return prelistenOption;
	}
	public String getStrInvalidSelRequestMsg() {
		return strInvalidSelRequestMsg;
	}
	public  String getSelChargingMsg() {
		return selChargingMsg;
	}

	public  String getSongSelSuccessmsg() {
		return songSelSuccessmsg;
	}

	public  boolean isSpecialCallerForCopyAllowed() {
		return specialCallerForCopyAllowed;
	}

	public  String getSpecialCallerSelOption() {
		return specialCallerSelOption;
	}

	public  String getStrInvalidRequestMsg() {
		return strInvalidRequestMsg;
	}

	public  String getStrSuccessMsg() {
		return strSuccessMsg;
	}

	public  String getStrTechnicalDifficultyMsg() {
		return strTechnicalDifficultyMsg;
	}

	public  boolean isUnsubscriptionAllowed() {
		return unsubscriptionAllowed;
	}

	public  String getUrlForAdvancePackActivation() {
		return urlForAdvancePackActivation;
	}

	public  String getUrlForCallDetails() {
		return urlForCallDetails;
	}

	public  String getUrlForCopySelection() {
		return urlForCopySelection;
	}

	public  String getUrlForDeactivation() {
		return urlForDeactivation;
	}

	public  String getUrlForGiftInbox() {
		return urlForGiftInbox;
	}

	public  String getUrlForLibraryDetails() {
		return urlForLibraryDetails;
	}

	public  String getUrlForMonthlyActivation() {
		return urlForMonthlyActivation;
	}

	public  String getUrlForSelection() {
		return urlForSelection;
	}

	public  String getUrlForSendingGift() {
		return urlForSendingGift;
	}

	public  String getUrlForSubDetails() {
		return urlForSubDetails;
	}

	public  String getUssdCatsFreeZone() {
		return ussdCatsFreeZone;
	}

	public  String getUssdCatsNotInBrowse() {
		return ussdCatsNotInBrowse;
	}

	public  String getUssdParentCat() {
		return ussdParentCat;
	}

	public  String getWhatsHotMsg() {
		return whatsHotMsg;
	}
	//toGiveBackNMainMenuOptionInResponse
	public void setResponseKeyWithMissingSeperator(
			String responseKeyWithMissingSeperator) {
		this.responseKeyWithMissingSeperator = responseKeyWithMissingSeperator;
	}
	public void setCountryPrefix(String countryPrefix) {
		this.countryPrefix = countryPrefix;
	}
	public void setEndURLChar(String endURLChar) {
		this.endURLChar = endURLChar;
	}
	public void setDayOfTheWeekValueMsg(String dayOfTheWeekValueMsg) {
		this.dayOfTheWeekValueMsg = dayOfTheWeekValueMsg;
	}
	public void setFutureDateValueMsg(String futureDateValueMsg) {
		this.futureDateValueMsg = futureDateValueMsg;
	}
	public void setGiftChargingMsg(String giftChargingMsg) {
		this.giftChargingMsg = giftChargingMsg;
	}
	public void setGiftChargingMsgForSubGiftee(
			String giftChargingMsgForSubGiftee) {
		this.giftChargingMsgForSubGiftee = giftChargingMsgForSubGiftee;
	}
	public void setGiftChargingMsgForUnsubGiftee(
			String giftChargingMsgForUnsubGiftee) {
		this.giftChargingMsgForUnsubGiftee = giftChargingMsgForUnsubGiftee;
	}
	public void setGiftSelSuccessmsg(String giftSelSuccessmsg) {
		this.giftSelSuccessmsg = giftSelSuccessmsg;
	}
	public void setUrlForValidatingGiftee(String urlForValidatingGiftee) {
		this.urlForValidatingGiftee = urlForValidatingGiftee;
	}
	public void setDayOfTheWeekAllowed(boolean dayOfTheWeekAllowed) {
		this.dayOfTheWeekAllowed = dayOfTheWeekAllowed;
	}
	public void setFutureDayAllowed(boolean futureDayAllowed) {
		this.futureDayAllowed = futureDayAllowed;
	}
	public void setTimeOfTheDayAllowed(boolean timeOfTheDayAllowed) {
		this.timeOfTheDayAllowed = timeOfTheDayAllowed;
	}
	public void setListRBTOptionMsg(String listRBTOptionMsg) {
		this.listRBTOptionMsg = listRBTOptionMsg;
	}
	public void setFavoriteSelSuccessmsg(String favoriteSelSuccessmsg) {
		this.favoriteSelSuccessmsg = favoriteSelSuccessmsg;
	}
	public void setMsearchConfirmMsg(String msearchConfirmMsg) {
		this.msearchConfirmMsg = msearchConfirmMsg;
	}
	public void setContentTypeAnswer(String contentTypeAnswer) {
		this.contentTypeAnswer = contentTypeAnswer;
	}
	public void setContentTypeNotification(String contentTypeNotification) {
		this.contentTypeNotification = contentTypeNotification;
	}
	public void setTimeOfTheDayValueMsg(String timeOfTheDayValueMsg) {
		this.timeOfTheDayValueMsg = timeOfTheDayValueMsg;
	}
	public void setDefaultSelCost(String defaultSelCost) {
		this.defaultSelCost = defaultSelCost;
	}
	public void setRejectStr(String rejectStr) {
		this.rejectStr = rejectStr;
	}
	public void setAnswerStr(String answerStr) {
		this.answerStr = answerStr;
	}
	public void setTimeOfTheDayVaueMsg(String timeOfTheDayValueMsg) {
		this.timeOfTheDayValueMsg = timeOfTheDayValueMsg;
	}
	public void setTimeOfTheDayOptionMsg(String timeOfTheDayOptionMsg) {
		this.timeOfTheDayOptionMsg = timeOfTheDayOptionMsg;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public void setBrowseMenuStr(String browseMenuStr) {
		this.browseMenuStr = browseMenuStr;
	}
	public void setMaxClipsNo(int maxClipsNo) {
		this.maxClipsNo = maxClipsNo;
	}
	public void setGiftAllowedInBrowse(String giftAllowedInBrowse) {
		this.giftAllowedInBrowse = giftAllowedInBrowse;
	}
	public void setPreListenAllowedInBrowse(String preListenAllowedInBrowse) {
		this.preListenAllowedInBrowse = preListenAllowedInBrowse;
	}
	public void setSpecialCallerAllowedInBrowse(String specialCallerAllowedInBrowse) {
		this.specialCallerAllowedInBrowse = specialCallerAllowedInBrowse;
	}
	public void setToGiveBackNMainMenuOptionInResponse(boolean toGiveBackNMainMenuOptionInResponse) {
		this.toGiveBackNMainMenuOptionInResponse = toGiveBackNMainMenuOptionInResponse;
	}
	public void setSeperatorChar(String seperatorChar) {
		this.seperatorChar = seperatorChar;
	}
	public void setFreeSongSelSuccessmsg(String freeSongSelSuccessmsg) {
		this.freeSongSelSuccessmsg = freeSongSelSuccessmsg;
	}
	public void setConfirmSubMsg(String confirmSubMsg) {
		this.confirmSubMsg = confirmSubMsg;
	}
	public void setConfirmUnsubcriptionMsg(String confirmUnsubcriptionMsg) {
		this.confirmUnsubcriptionMsg = confirmUnsubcriptionMsg;
	}
	public void setDefaultSubCost(String defaultSubCost) {
		this.defaultSubCost = defaultSubCost;
	}
	public void setMsearchMsg(String msearchMsg) {
		this.msearchMsg = msearchMsg;
	}
	public  void setAdvanceSubscriptionAllowed(boolean advanceSubscriptionAllowed) {
		this.advanceSubscriptionAllowed = advanceSubscriptionAllowed;
	}

	public  void setAskForSpecialCallerMsg(String askForSpecialCallerMsg) {
		this.askForSpecialCallerMsg = askForSpecialCallerMsg;
	}

	public  void setBackEndInvalidReponseMsg(String backEndInvalidReponseMsg) {
		this.backEndInvalidReponseMsg = backEndInvalidReponseMsg;
	}
	
	public void setBackEndSongAlreadyExistMsg(String backEndSongAlreadyExistMsg) {
		this.backEndSongAlreadyExistMsg = backEndSongAlreadyExistMsg;
	}

	public  void setBackEndSuccessMsg(String backEndSuccessMsg) {
		this.backEndSuccessMsg = backEndSuccessMsg;
	}

	public  void setBackEndTechnicalDifficultyMsg(
			String backEndTechnicalDifficultyMsg) {
		this.backEndTechnicalDifficultyMsg = backEndTechnicalDifficultyMsg;
	}

	public  void setCalledNo(String calledNo) {
		this.calledNo = calledNo;
	}

	public  void setCopyMsg(String copyMsg) {
		this.copyMsg = copyMsg;
	}

	public  void setCopyReconfirmMsg(String copyReconfirmMsg) {
		this.copyReconfirmMsg = copyReconfirmMsg;
	}

	public  void setCopyURL(String copyURL) {
		this.copyURL = copyURL;
	}

	public  void setCricketMsg(String cricketMsg) {
		this.cricketMsg = cricketMsg;
	}

	public  void setDefaultSelOption(String defaultSelOption) {
		this.defaultSelOption = defaultSelOption;
	}

	public  void setDefaultSubscritpionAllowed(boolean defaultSubscritpionAllowed) {
		this.defaultSubscritpionAllowed = defaultSubscritpionAllowed;
	}

	public  void setDefaultURL(String defaultURL) {
		this.defaultURL = defaultURL;
	}

	public  void setEnterChoiceStr(String enterChoiceStr) {
		this.enterChoiceStr = enterChoiceStr;
	}

	

	public  void setGiftOption(String giftOption) {
		this.giftOption = giftOption;
	}

	public  void setHelloTunesHelpMsg(String helloTunesHelpMsg) {
		this.helloTunesHelpMsg = helloTunesHelpMsg;
	}

	public  void setHelpMsg(String helpMsg) {
		this.helpMsg = helpMsg;
	}

	public  void setMainMenuURL(String mainMenuURL) {
		this.mainMenuURL = mainMenuURL;
	}

	public  void setMaxStrlengthlimit(int maxStrlengthlimit) {
		this.maxStrlengthlimit = maxStrlengthlimit;
	}

	public  void setMenuOrder(String menuOrder) {
		this.menuOrder = menuOrder;
	}

	public  void setMoreStr(String moreStr) {
		this.moreStr = moreStr;
	}

	public  void setMSearchURL(String searchURL) {
		this.mSearchURL = searchURL;
	}

	public  void setNewLineCharString(String newLineCharString) {
		this.newLineCharString = newLineCharString;
	}

	public  void setPrelistenMsg(String prelistenMsg) {
		this.prelistenMsg = prelistenMsg;
	}

	public  void setPrelistenOption(String prelistenOption) {
		this.prelistenOption = prelistenOption;
	}

	public  void setSelChargingMsg(String selChargingMsg) {
		this.selChargingMsg = selChargingMsg;
	}

	public  void setSongSelSuccessmsg(String songSelSuccessmsg) {
		this.songSelSuccessmsg = songSelSuccessmsg;
	}

	public  void setSpecialCallerForCopyAllowed(boolean specialCallerForCopyAllowed) {
		this.specialCallerForCopyAllowed = specialCallerForCopyAllowed;
	}

	public  void setSpecialCallerSelOption(String specialCallerSelOption) {
		this.specialCallerSelOption = specialCallerSelOption;
	}

	public  void setStrInvalidRequestMsg(String strInvalidRequestMsg) {
		this.strInvalidRequestMsg = strInvalidRequestMsg;
	}

	public  void setStrSuccessMsg(String strSuccessMsg) {
		this.strSuccessMsg = strSuccessMsg;
	}

	public  void setStrTechnicalDifficultyMsg(String strTechnicalDifficultyMsg) {
		this.strTechnicalDifficultyMsg = strTechnicalDifficultyMsg;
	}

	public  void setUnsubscriptionAllowed(boolean unsubscriptionAllowed) {
		this.unsubscriptionAllowed = unsubscriptionAllowed;
	}

	public  void setUrlForAdvancePackActivation(String urlForAdvancePackActivation) {
		this.urlForAdvancePackActivation = urlForAdvancePackActivation;
	}

	public  void setUrlForCallDetails(String urlForCallDetails) {
		this.urlForCallDetails = urlForCallDetails;
	}

	public  void setUrlForCopySelection(String urlForCopySelection) {
		this.urlForCopySelection = urlForCopySelection;
	}

	public  void setUrlForDeactivation(String urlForDeactivation) {
		this.urlForDeactivation = urlForDeactivation;
	}

	public  void setUrlForGiftInbox(String urlForGiftInbox) {
		this.urlForGiftInbox = urlForGiftInbox;
	}

	public  void setUrlForLibraryDetails(String urlForLibraryDetails) {
		this.urlForLibraryDetails = urlForLibraryDetails;
	}

	public  void setUrlForMonthlyActivation(String urlForMonthlyActivation) {
		this.urlForMonthlyActivation = urlForMonthlyActivation;
	}

	public  void setUrlForSelection(String urlForSelection) {
		this.urlForSelection = urlForSelection;
	}

	public  void setUrlForSendingGift(String urlForSendingGift) {
		this.urlForSendingGift = urlForSendingGift;
	}

	public  void setUrlForSubDetails(String urlForSubDetails) {
		this.urlForSubDetails = urlForSubDetails;
	}

	public  void setUssdCatsFreeZone(String ussdCatsFreeZone) {
		this.ussdCatsFreeZone = ussdCatsFreeZone;
	}

	public  void setUssdCatsNotInBrowse(String ussdCatsNotInBrowse) {
		this.ussdCatsNotInBrowse = ussdCatsNotInBrowse;
	}
	
	public void setStrInvalidSelRequestMsg(String strInvalidSelRequestMsg) {
		this.strInvalidSelRequestMsg = strInvalidSelRequestMsg;
	}
	public  void setUssdParentCat(String ussdParentCat) {
		this.ussdParentCat = ussdParentCat;
	}

	public  void setWhatsHotMsg(String whatsHotMsg) {
		this.whatsHotMsg = whatsHotMsg;
	}
	public String getGiftInbox() {
		return giftInbox;
	}
	public void setGiftInbox(String giftInbox) {
		this.giftInbox = giftInbox;
	}

	
	
}
