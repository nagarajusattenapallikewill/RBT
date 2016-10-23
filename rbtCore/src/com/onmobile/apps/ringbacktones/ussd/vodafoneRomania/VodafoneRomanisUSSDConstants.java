package com.onmobile.apps.ringbacktones.ussd.vodafoneRomania;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.ussd.USSDConstants;

public class VodafoneRomanisUSSDConstants extends USSDConstants{
	/*author@Abhinav Anand
	 */
	//Added for giftinbox
	private static Logger logger = Logger.getLogger(VodafoneRomanisUSSDConstants.class);
	private PropertyReader pr=new PropertyReader();
	public String giftInbox=pr.getPropertyValue("giftinbox.str");
	public String urlForSubDetails="Rbt.do?subscriberID=%subId%&info=subscriber&calledNo=%ussdNo%&mode=USSD";
	public  String urlForGiftInbox="Rbt.do?subscriberID=%subId%&info=gift_inbox&calledNo=%ussdNo%&mode=USSD";
	public  String urlForLibraryDetails="Rbt.do?subscriberID=%subId%&info=library&calledNo=%ussdNo%&mode=USSD";
	public  String urlForCallDetails="Rbt.do?subscriberID=%subId%&info=call_details&calledNo=%ussdNo%&mode=USSD";
	//for default caller, caller_id="all"
	
	
	public  String urlForCopySelection="Copy.do?action=set&subscriberID=%subId%&fromSubscriber=%copyNo%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&status=1&calledNo=%ussdNo%&mode=USSD";
	public  String urlForSendingGift="Gift.do?action=send_gift&gifterID=%subId%&gifteeID=%gifteeNo%&toneID=%clipId%&categoryID=%catId%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForDeactivation="Subscription.do?action=deactivate&subscriberID=%subId%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForMonthlyActivation="Subscription.do?action=activate&subscriberID=%subId%&isPrepaid=%isPrepaid%&calledNo=%ussdNo%&mode=USSD";
	public  String urlForAdvancePackActivation="Subscription.do?action=activate&subscriberID=%subId%&isPrepaid=%isPrepaid%&calledNo=%ussdNo%&mode=USSD&rentalPack=%advancePack%";
	public  String urlForSelection="Selection.do?action=set&subscriberID=%subId%&isPrepaid=%isPrepaid%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&calledNo=%ussdNo%&mode=USSD";
	public String urlForSelectionWithTimeOfTheDay="Selection.do?action=set&subscriberID=%subId%&isPrepaid=%isPrepaid%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&fromTime=%startTime%&toTime=%endTime%&status=80&calledNo=%ussdNo%&mode=USSD";
	public String urlForSelectionWithDayOfTheWeek="Selection.do?action=set&subscriberID=%subId%&isPrepaid=%isPrepaid%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&interval=%day%&status=75&calledNo=%ussdNo%&mode=USSD";
	public String urlForSelectionWithFutureDate="Selection.do?action=set&subscriberID=%subId%&isPrepaid=%isPrepaid%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&interval=%futureDate%&status=95&calledNo=%ussdNo%&mode=USSD";
	public String urlForSelectionInLoop="Selection.do?action=set&subscriberID=%subId%&isPrepaid=%isPrepaid%&callerID=all&categoryID=%catId%&clipID=%clipId%&inLoop=y&calledNo=%ussdNo%&mode=USSD";
	public  String urlForValidatingGiftee="ValidateNumber.do?action=gift&subscriberID=%subId%&number=%giftee%&toneID=%clipId%&categoryID=%catId%";
	public  String mainMenuURL="`#`http://localhost:9910/ussd/ussd.do?processId=0&pageNo=0";
	//mainMenuURL="`#`"+defaultURL+"&processId=0&pageNo=0";
	public  String defaultURL=pr.getPropertyValue("url.string");
//	Please enter the mobile number
	public  String askForSpecialCallerMsg=pr.getPropertyValue("ask.for.special.caller");
	//Please enter the name of the song or the movie/album you are looking for
	public String msearchMsg=pr.getPropertyValue("msearch.msg.string");
	public  String calledNo=pr.getPropertyValue("called.number");
	public String maxSetHours="24";
	//please Try later
	public  String strTechnicalDifficultyMsg=pr.getPropertyValue("technical.difficulties");
	//Invalid request
	public  String strInvalidRequestMsg=pr.getPropertyValue("invalid.request");
	//Selection already exist.
	public  String strInvalidSelRequestMsg=pr.getPropertyValue("invalid.selrequest");
	//OK.Success!!!
	public  String strSuccessMsg=pr.getPropertyValue("success.message");
	public String contentType="text/html";
	public String contentTypeNotification="text/html";
	public String contentTypeAnswer="text/html";
	//Try later
	public  String backEndInvalidReponseMsg="error";
	//Success
	public  String backEndSuccessMsg="success";
	//error
	public  String backEndTechnicalDifficultyMsg=pr.getPropertyValue("msg.try.later");
	public String backEndSongAlreadyExistMsg="already_exists";
	public  boolean unsubscriptionAllowed=true;
	public  boolean defaultSubscritpionAllowed=true;
	public  boolean advanceSubscriptionAllowed=false;
	public int maxClipsNo=200;
	public  String newLineCharString="<br>";
	public String previous = pr.getPropertyValue("string.previous");
	//Browse Songs
	//public String browseMenuStr="Cauta cantece";
	public String browseMenuStr=pr.getPropertyValue("browse.menu.string");
	//@@0.More
	public  String moreStr=pr.getPropertyValue("more.string");
	public  String enterChoiceStr="";
	public  int maxStrlengthlimit=140;
	public String seperatorChar="@@";
	public String endURLChar="&";
	public boolean timeOfTheDayAllowed=true;
	public boolean dayOfTheWeekAllowed=true;
	public boolean futureDayAllowed=true;
	//Do you wish to set this song for a particular time of the day?
	public  String timeOfTheDayOptionMsg=pr.getPropertyValue("set.timeoftheday.permission");
	//@@1.Gift to your friend<br>@@2.Set as RBT
	public  String listRBTOptionMsg="@@1."+pr.getPropertyValue("list.rbt.option.gift")+"<br>@@2."+pr.getPropertyValue("list.rbt.option.set");
	//Please enter the time(HH HH):e.g. 11 12
	public  String timeOfTheDayValueMsg=pr.getPropertyValue("set.timeoftheday.value");
	//Please enter day of the week.e.g:<br>1-Sun<br>2-Mon<br>3-Tue<br>4-Wed<br>5-Thu<br>6-Fri<br>7-Sat
	public  String dayOfTheWeekValueMsg=pr.getPropertyValue("set.dayinweek.value");
	//Please enter the future date(ddMMyyyy)e.g:29122009
	public  String futureDateValueMsg=pr.getPropertyValue("set.futuredate.value");
	//ANSWER
	public String answerStr=pr.getPropertyValue("msg.yes");
	//No
	public String rejectStr=pr.getPropertyValue("msg.no");
	//set for all callers
	public  String defaultSelOption=pr.getPropertyValue("set.normal.allcaller");
	//Do you wish to set this song for a particular number?
	public  String specialCallerSelOption=pr.getPropertyValue("set.number.permission");
	
	//Gift to your friends
	public  String giftOption=pr.getPropertyValue("list.rbt.option.gift");
	//Prelisten
	public  String prelistenOption=pr.getPropertyValue("msg.listen");
	public String defaultSubCost="0,25";
	public String defaultSelCost="0,90";
	public String giftAllowedInBrowse="false";
	public String specialCallerAllowedInBrowse="true";
	public String preListenAllowedInBrowse="false";
	//Thank you for confirming the RBT service.Your request will be processed and you will be activated to the service if you have sufficient balance.You will be intimated shortly
	public String subSuccessPrepaidMsg= pr.getPropertyValue("sub.success.prepaid.msg");//"Iti multumesc pentru confirmarea activarii serviciului.Cererea ta va fi procesata si vei fi activat la serviciu daca ai credit suficient. Vei fi informat in curand.";
	//Thank you for confirming the RBT service.Your request will be processed and you will be activated to the service.You will be intimated shortly
	//Iti multumesc pentru confirmarea activarii serviciului. Serviciul va fi activat in cateva minute. Vei primi un mesaj de confirmare
	public String subSuccessPostpaidMsg= pr.getPropertyValue("sub.success.postpaid.msg"); //"Iti multumesc pentru confirmarea activarii serviciului.Cerearea ta va fi procesata si vei fi activat la. Vei fi informat in curand.";
	//Thank you
	public String unSubSuccessMsg=pr.getPropertyValue("unsub.success.msg");
	
	//You will be unsubscribed from the RBT service. Please select what you wish to do<br>@@1.Confirm@@2.Exit
	public String confirmUnsubcriptionMsg=pr.getPropertyValue("unsub.confirm.msg");
	//Activarea serviciului costa 0,25 euro/luna, TVA inclus
	//Subscription charges for RBT service $%amount%.<br>@@1.Confirm@@2.Exit
	
//	You have selected Clip 1. The subscription charge for the service is $ 30/month,
//	and songs are charged at $ 0.07 for normal and S1.07 for premium songs. VAT charges extra.
	public String confirmSubMsg=pr.getPropertyValue("sub.confirm.msg");
	//You will be charged $%selCharge% for this song and $%subCharge% as monthly subscription,if you are not already an user<br>@@1.Confirm@@2.Exit
	public  String selChargingMsg=pr.getPropertyValue("sub.charging.msg");
	//You will be Charged $%selCharge% for song.<br>@@1.Confirm@@2.Exit
	public  String giftChargingMsgForSubGiftee=pr.getPropertyValue("giftsel.charging.subgiftee.msg");
	//You will be Charged $%selCharge% for song and additional $%subCharge% for monthly subscription as giftee is not an existing user.<br>@@1.Confirm@@2.Exit
	public  String giftChargingMsgForUnsubGiftee=pr.getPropertyValue("giftsel.charging.unsubgiftee.msg");
	//You will be Charged $%selCharge% for song.Additional $%subCharge% for monthly subscription if giftee is not an active user.Press 1 to continue.Press # to go to main menu
	public  String giftChargingMsg=pr.getPropertyValue("giftsel.charging.msg");
	//To prelisten the song please exit this menu and Dial %nameWavFile% at $3/minute.
	public  String prelistenMsg=pr.getPropertyValue("msg.prelisten");
	//OK.Press *1 to Go one level up
	public  String songSelSuccessmsg=pr.getPropertyValue("set.normal.sucess.msg");
	//Thank you for gifting song/service
	public  String giftSelSuccessmsg=pr.getPropertyValue("giftsel.sucess.msg");
	//Thank you for selecting the clip.You will be subscribed to the RBT service and your clip will be set as the RBT within %maxSetHours% hours.
	public  String songSelSuccessmsgActive=pr.getPropertyValue("songsel.success.msg.active");
	public  String songSelSuccessmsgDeactive=pr.getPropertyValue("songsel.success.msg.deactive");
	//Thank you for request.The same would be activated within 24 hours.
/*/*/public  String freeSongSelSuccessmsg=pr.getPropertyValue("freesongsel.success.msg");
	//Thank you for selecting the clip.The clip will be set in loop within 24 hours.
	public  String favoriteSelSuccessmsg=pr.getPropertyValue("favoritesel.success.msg");
	public   String ussdParentCat="2";
	public   String ussdCatsNotInBrowse="3,4";
	public   String ussdCatsFreeZone="3";
	//You can now copy an Airtel Subscriber's Hello Tune by pressing * when you call him(before the is call picked up)Chrgs:HT subscription $.30/month,$20/song
	public   String whatsHotMsg=pr.getPropertyValue("msg.whatshot");
	//Please enter the Airtel numbe,you want to copy Hello Tune from eg 9945981517
	public   String copyMsg=pr.getPropertyValue("copy.number.enter.msg");
	//Vei fi tarifat cu 0,70 euro pentru acest ton si 0,25 euro/luna pt serviciu, TVA inclus. Daca ai deja serviciul activat nu vei fi taxat pentru activare.
	//You will be charged $20 to download this song and additional $30 as monthly subscription if you are not a Hello Tune customer.@@1.Continue
	public   String copyReconfirmMsg=pr.getPropertyValue("copy.reconfirm.msg");
	public   String cricketMsg=pr.getPropertyValue("msg.cricket");
	//@@1.Browsing the Hello Tunes Menu
	public   String helpMsg=pr.getPropertyValue("msg.help");
	//Press * to go back to the previous menu.Press # to go back to the Hello Tunes Menu.Press * to Go one level up."
	public   String helloTunesHelpMsg=pr.getPropertyValue("msg.help.hellotunes");
	public String tariffMsg=pr.getPropertyValue("msg.tariff");
	public   String copyURL="&request_type=copy";
	public   String mSearchURL=null;
	public   String menuOrder=pr.getPropertyValue("menu.order");
	public   boolean specialCallerForCopyAllowed=false;
	public   boolean toGiveBackNMainMenuOptionInResponse=true;
	//
	public String msearchConfirmMsg=pr.getPropertyValue("msearch.confirm.string");
	public String countryPrefix="4";
	public String responseKeyWithMissingSeperator=pr.getPropertyValue("responsekey.missing.seperator");
	
	private VodafoneRomanisUSSDConstants(){
		
	}
	public  VodafoneRomanisUSSDConstants(USSDConstants info){
		
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
		//Added for giftinbox
		info.setGiftInbox(giftInbox);
		info.setTariffMsg(tariffMsg);
		info.setStrInvalidSelRequestMsg(strInvalidSelRequestMsg);
		info.setBackEndSongAlreadyExistMsg(backEndSongAlreadyExistMsg);
		info.setResponseKeyWithMissingSeperator(responseKeyWithMissingSeperator);
		info.setCountryPrefix(countryPrefix);
		info.setEndURLChar(endURLChar);
		info.setDayOfTheWeekValueMsg(dayOfTheWeekValueMsg);
		info.setFutureDateValueMsg(futureDateValueMsg);
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
		info.setDefaultSelCost(defaultSelCost);
		info.setTimeOfTheDayVaueMsg(timeOfTheDayValueMsg);
		info.setRejectStr(rejectStr);
		info.setAnswerStr(answerStr);
		info.setTimeOfTheDayOptionMsg(timeOfTheDayOptionMsg);
		info.setContentType(contentType);
		info.setBrowseMenuStr(browseMenuStr);
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
	@Override
	public String getCountryPrefix() {
		// TODO Auto-generated method stub
		return this.countryPrefix;
	}
	public String getResponseKeysWithMissingSeperator(){
	
		return this.responseKeyWithMissingSeperator;
		
	}
}

