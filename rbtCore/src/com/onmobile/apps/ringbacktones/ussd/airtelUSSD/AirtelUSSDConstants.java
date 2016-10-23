package com.onmobile.apps.ringbacktones.ussd.airtelUSSD;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.ussd.USSDConstants;

public class AirtelUSSDConstants extends USSDConstants{
	/*author@Abhinav Anand
	 */
	//for default caller, caller_id="all"
	private static Logger logger = Logger.getLogger(AirtelUSSDConstants.class);
	public String urlForSubDetails="Rbt.do?subscriberID=%subId%&info=subscriber&calledNo=%ussdNo%&mode=USSD";
	public  String urlForGiftInbox="Rbt.do?subscriberID=%subId%&info=gift_inbox&calledNo=%ussdNo%&mode=USSD";
	public  String urlForLibraryDetails="Rbt.do?subscriberID=%subId%&info=library&calledNo=%ussdNo%&mode=USSD";
	public  String urlForCallDetails="Rbt.do?subscriberID=%subId%&info=call_details&calledNo=%ussdNo%&mode=USSD";

	public  String urlForCopySelection="Copy.do?action=set&subscriberID=%subId%&fromSubscriber=%copyNo%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&status=1&calledNo=%ussdNo%&mode=USSD";
	public  String urlForSendingGift="Gift.do?action=send_gift&gifterID=%subId%&gifteeID=%gifteeNo%&toneID=%clipId%&categoryID=%catId%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForDeactivation="Subscription.do?action=deactivate&subscriberID=%subId%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForMonthlyActivation="Subscription.do?action=activate&subscriberID=%subId%&isPrepaid=%isPrepaid%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForAdvancePackActivation="Subscription.do?action=activate&subscriberID=%subId%&isPrepaid=%isPrepaid%&calledNo=%ussdNo%&mode=USSD&rentalPack=%advancePack%";
	public  String urlForSelection="Selection.do?action=set&inLoop=y&subscriberID=%subId%&isPrepaid=%isPrepaid%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForValidatingGiftee="ValidateNumber.do?action=gift&subscriberID=%subId%&number=%giftee%&toneID=%clipId%&categoryID=%catId%";
	public  String mainMenuURL="`#`http://localhost:9910/ussd/ussd.do?msisdn=$subscriber$&processId=0&pageNo=0";
	//mainMenuURL="`#`"+defaultURL+"&processId=0&pageNo=0";
	public  String defaultURL="http://localhost:9910/ussd/ussd.do?msisdn=$subscriber$";
	public  String askForSpecialCallerMsg="Please enter the mobile number";
	public String msearchMsg="Please enter the name of the song or the movie/album you are looking for";
	public  String giftSelSuccessmsg="Thanks you for gifting song/service";
	public  String listRBTOptionMsg="@@1.Gift to your friends<br>@@2.Set as RBT";
	public boolean timeOfTheDayAllowed=true;
	public boolean dayOfTheWeekAllowed=true;
	public boolean futureDayAllowed=true;
	public  String calledNo="*678#";
	public  String strTechnicalDifficultyMsg="Try later";
	public  String strInvalidRequestMsg="Invalid request";
	public  String strSuccessMsg="OK. Success !!!";
	//Selection already exist.
	public  String strInvalidSelRequestMsg="Selection already exist";
	//"application/X-USSD-request+ans"
	public String contentType="application/X-USSD-request+ans";
	public String contentTypeNotification="plain/X-USSD-notification";
	public String contentTypeAnswer="plain/X-USSD-request";
	public  String backEndInvalidReponseMsg="Try later";
	public  String backEndSuccessMsg="success";
	public  String backEndTechnicalDifficultyMsg="error";
	public String backEndSongAlreadyExistMsg="already_exist";
	public  boolean unsubscriptionAllowed=true;
	public  boolean defaultSubscritpionAllowed=false;
	public  boolean advanceSubscriptionAllowed=true;
	public int maxClipsNo=100;
	public  String newLineCharString="\n";
	public  String moreStr="0.More";
	public  String enterChoiceStr="\nEnter ur choice no.:*g for generic help";
	public  int maxStrlengthlimit=140;
	public String seperatorChar="";
	public String endURLChar="";
	public  String defaultSelOption="Select for all callers";
	public  String specialCallerSelOption="Select for one caller";
	public  String giftOption="Gift it to friends";
	public  String prelistenOption="Prelisten";
	public String defaultSubCost="30";
	public String giftAllowedInBrowse="true";
	public String specialCallerAllowedInBrowse="true";
	public String preListenAllowedInBrowse="true";
	public String confirmUnsubcriptionMsg="Your HT profie and songs will be deleted if you choose to unsubscriber.Press 1 to confirm,*1 for previous menu,# for main menu";
	public String confirmSubMsg="You will be charged Rs%amount% as subscription for %period% pack.Press 1 to continue,*1 to previoous menu,# for main menu";
	public  String selChargingMsg="You will be charged Rs 20 for this song Rs 30 as monthly subscription.Press 1 to continue,*1 for previous menu,# for main menu";
	public  String giftChargingMsgForSubGiftee="You will be Charged Rs 20 for song.Press 1 to continue.Press # to go to main menu";
	public  String giftChargingMsgForUnsubGiftee="You will be Charged Rs 20 for song and additional Rs 30 for monthly subscription.Press 1 to continue.Press # to go to main menu";
	public  String giftChargingMsg="You will be Charged Rs 20 for song.Additional Rs 30 for monthly subscription if giftee is not an active user.Press 1 to continue.Press # to go to main menu";
	public  String prelistenMsg="To prelisten the song please exit this menu and Dial %nameWavFile% at Rs3/minute.Press *1 to go back to the previous menu.Press *1 Go one level up ";
	public  String songSelSuccessmsg="OK.Press *1 to Go one level up";
	public  String freeSongSelSuccessmsg="Thanks you for request.The same would be activated within 24 hours.Press 1 to continue,*1 for previous menu,# for main menu";
	public  String favoriteSelSuccessmsg="Thanks you for selecting the clip.The clip will be set in loop within 24 hours.";
	public   String ussdParentCat="2";
	public   String ussdCatsNotInBrowse="3,4";
	public   String ussdCatsFreeZone="3";
	public   String whatsHotMsg="You can now copy an Airtel Subscriber's Hello Tune by pressing * when you call him(before the is call picked up)Chrgs:HT subscription Rs.30/month,Rs20/song Press *1 to Go one level up";
	public   String copyMsg="Please enter the Airtel numbe,you want to copy Hello Tune from eg 9945981517";
	public   String copyReconfirmMsg="You will be charged Rs20 to download this song and additional Rs30 as monthly subscription if you are not a Hello Tune customer.Press 1 to continue or press *1 for previous menu";
	public   String cricketMsg="Play the latest scores to your caller with Cricket Hello Tunes.Call 543211678 to subscribe.Charges:Rs.30 for entire series.When no match is on,callers will hear your default tune Press *1 Go one level up";
	public   String helpMsg="1.Browsing the Hello Tunes Menu";
	public   String helloTunesHelpMsg="Press *1 to go back to " +
			"the previous menu.Press # to go back to the Hello Tunes Menu.Press *1 to Go one level up.";
	public String tariffMsg="Tarife: 0,70 sau 1,07 euro sa cumperi un ton si 0,30 euro/luna abonamantul la serviciu, TVA inclus.";
	public   String copyURL="&request_type=copy&ans=$answer$";
	public   String mSearchURL=null;
	public   String menuOrder="MSearch,FreeZone,WhatsHot,Browse,Cricket,Copy,Manage,Help";
	public   boolean specialCallerForCopyAllowed=false;
	public   boolean toGiveBackNMainMenuOptionInResponse=true;
	public String msearchConfirmMsg="We will confirm your selection";
	public String countryPrefix="0,+,91";
	public String responseKeyWithMissingSeperator="ussd,response,msisdn,service";
	private AirtelUSSDConstants(){
		
	}
	public  AirtelUSSDConstants(USSDConstants info){
		
		String temp=null;
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_DEFAULT_URL", null);
		if(temp!=null){
			this.defaultURL=temp;
			temp=null;
		}
		mainMenuURL="`"+this.seperatorChar+"#`"+this.defaultURL+"&processId=0&pageNo=0"+info.endURLChar;
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_CALLED_NUMBER", null);
		if(temp!=null){
			this.calledNo=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_PARENT_CAT", null);
		if(temp!=null){
			this.ussdParentCat=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_CATS_NOT_IN_BROWSE", null);
		if(temp!=null){
			this.ussdCatsNotInBrowse=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_CAT_FREE_ZONE", null);
		if(temp!=null){
			this.ussdCatsFreeZone=temp;
			logger.info("local info.ussdCatsFreeZone=="+this.ussdCatsFreeZone);
			logger.info("local info.ussdCatsFreeZone=="+ussdCatsFreeZone);
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_MENU_ORDER", null);
		if(temp!=null){
			this.menuOrder=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_MSEARCH_URL", null);
		if(temp!=null){
			this.mSearchURL=temp;
			temp=null;
		}
		info.setTariffMsg(tariffMsg);
		info.setStrInvalidSelRequestMsg(strInvalidSelRequestMsg);
		info.setBackEndSongAlreadyExistMsg(backEndSongAlreadyExistMsg);
		info.setResponseKeyWithMissingSeperator(responseKeyWithMissingSeperator);
		info.setCountryPrefix(countryPrefix);
		info.setEndURLChar(endURLChar);
		info.setGiftChargingMsg(giftChargingMsg);
		info.setUrlForValidatingGiftee(urlForValidatingGiftee);
		info.setGiftChargingMsgForSubGiftee(giftChargingMsgForSubGiftee);
		info.setGiftChargingMsgForUnsubGiftee(giftChargingMsgForUnsubGiftee);
		info.setGiftSelSuccessmsg(giftSelSuccessmsg);
		info.setTimeOfTheDayAllowed(timeOfTheDayAllowed);
		info.setDayOfTheWeekAllowed(dayOfTheWeekAllowed);
		info.setFutureDayAllowed(futureDayAllowed);
		info.setListRBTOptionMsg(listRBTOptionMsg);
		info.setFavoriteSelSuccessmsg(favoriteSelSuccessmsg);
		info.setMsearchConfirmMsg(msearchConfirmMsg);
		info.setContentTypeAnswer(contentTypeAnswer);
		info.setTimeOfTheDayValueMsg(timeOfTheDayValueMsg);
		info.setContentTypeNotification(contentTypeNotification);
		info.setContentType(contentType);
		info.setMaxClipsNo(maxClipsNo);
		info.setGiftAllowedInBrowse(giftAllowedInBrowse);
		info.setPreListenAllowedInBrowse(preListenAllowedInBrowse);
		info.setSpecialCallerAllowedInBrowse(specialCallerAllowedInBrowse);
		info.setSeperatorChar(seperatorChar);
		info.setToGiveBackNMainMenuOptionInResponse(toGiveBackNMainMenuOptionInResponse);
		info.setFreeSongSelSuccessmsg(freeSongSelSuccessmsg);
		info.setMsearchMsg(msearchMsg);
		info.setDefaultSubCost(defaultSubCost);
		info.setConfirmUnsubcriptionMsg(confirmUnsubcriptionMsg);
		info.setConfirmSubMsg(confirmSubMsg);
		 info.setAdvanceSubscriptionAllowed(advanceSubscriptionAllowed);
		 info.setAskForSpecialCallerMsg(askForSpecialCallerMsg); 

		info.setBackEndInvalidReponseMsg(backEndInvalidReponseMsg); 

		info.setBackEndSuccessMsg(backEndSuccessMsg);
		info.setBackEndTechnicalDifficultyMsg(backEndTechnicalDifficultyMsg); 
		info.setCalledNo(calledNo); 

		info.setCopyMsg(copyMsg);

		info.setCopyReconfirmMsg(copyReconfirmMsg);

		info.setCopyURL(copyURL); 

		info.setCricketMsg(cricketMsg);

		info.setDefaultSelOption(defaultSelOption); 
		info.setDefaultSubscritpionAllowed(defaultSubscritpionAllowed); 

		info.setDefaultURL(defaultURL); 
		info.setEnterChoiceStr(enterChoiceStr);
		info.setGiftOption(giftOption); 

		info.setHelloTunesHelpMsg(helloTunesHelpMsg);

		info.setHelpMsg(helpMsg); 

		info.setMainMenuURL(mainMenuURL); 

		info.setMaxStrlengthlimit(maxStrlengthlimit);

		info.setMenuOrder(menuOrder);
		info.setMoreStr(moreStr); 
		info.setMSearchURL(mSearchURL); 
		info.setNewLineCharString(newLineCharString); 
		info.setPrelistenMsg(prelistenMsg); 

		info.setPrelistenOption(prelistenOption); 

		info.setSelChargingMsg(selChargingMsg); 
		info.setSongSelSuccessmsg(songSelSuccessmsg);

		info.setSpecialCallerForCopyAllowed(specialCallerForCopyAllowed);
		info.setSpecialCallerSelOption(specialCallerSelOption); 

		info.setStrInvalidRequestMsg(strInvalidRequestMsg); 
		info.setStrSuccessMsg(strSuccessMsg); 
		info.setStrTechnicalDifficultyMsg(strTechnicalDifficultyMsg); 
		info.setUnsubscriptionAllowed(unsubscriptionAllowed); 
		info.setUrlForAdvancePackActivation(urlForAdvancePackActivation);
		info.setUrlForCallDetails(urlForCallDetails); 

		info.setUrlForCopySelection(urlForCopySelection);

		info.setUrlForDeactivation(urlForDeactivation);

		info.setUrlForGiftInbox(urlForGiftInbox);
		info.setUrlForLibraryDetails(urlForLibraryDetails);
		info.setUrlForMonthlyActivation(urlForMonthlyActivation); 
		info.setUrlForSelection(urlForSelection);
		info.setUrlForSendingGift(urlForSendingGift); 
		info.setUrlForSubDetails(urlForSubDetails);
		logger.info("before populatin info.ussdCatsFreeZone=="+info.ussdCatsFreeZone);
		info.setUssdCatsFreeZone(ussdCatsFreeZone); 
		
		logger.info("info.ussdCatsFreeZone=="+info.ussdCatsFreeZone);
		info.setUssdCatsNotInBrowse(ussdCatsNotInBrowse);
		info.setUssdParentCat(ussdParentCat);
		info.setWhatsHotMsg(whatsHotMsg);
	}
	public String getCountryPrefix() {
		// TODO Auto-generated method stub
		return this.countryPrefix;
	}
	public String getResponseKeyWithMissingSeperator(){
		return this.responseKeyWithMissingSeperator;
	}
}
