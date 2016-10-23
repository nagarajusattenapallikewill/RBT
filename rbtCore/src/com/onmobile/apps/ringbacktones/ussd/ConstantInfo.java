package com.onmobile.apps.ringbacktones.ussd;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;

public class ConstantInfo {
	/*author@Abhinav Anand
	 */
	public String urlForSubDetails="Rbt.do?subscriberID=%subId%&info=subscriber&calledNo=%ussdNo%&mode=USSD";
	public String urlForLibraryDetails="Rbt.do?subscriberID=%subId%&info=gift_inbox&calledNo=%ussdNo%&mode=USSD";
	public String urlForCallDetails="rbt/Rbt.do?subscriberID=%subId%&info=library&calledNo=%ussdNo%&mode=USSD";
	public String urlForGiftInbox="Rbt.do?subscriberID=%subId%&info=call_details&calledNo=%ussdNo%&mode=USSD";
	//for default caller, caller_id="all"
	public String urlForCopySelection="Copy.do?action=set&subscriberID=%subId%&fromSubscriber=%copyNo%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&status=1&calledNo=%ussdNo%&mode=USSD";
	public String urlForSendingGift="Gift.do?action=send_gift&gifterID=%subId%&gifteeID=%gifteeNo%&toneID=%clipId%&categoryID=%catId%&calledNo=%ussdNo%&mode=USSD";
	public String urlForDeactivation="Subscription.do?action=deactivate&subscriberID=%subId%&calledNo=%ussdNo%&mode=USSD";
	public String urlForMonthlyActivation="Subscription.do?action=activate&subscriberID=%subId%&isPrepaid=%isPrepaid%&language=%lan%&calledNo=%ussdNo%&mode=USSD";
	public String urlForAdvancePackActivation="Subscription.do?action=activate&subscriberID=%subId%&isPrepaid=%isPrepaid%&language=%lan%&calledNo=%ussdNo%&mode=USSD&rentalPack=%advancePack%";
	public String urlForSelection="Selection.do?action=set&subscriberID=%subId%&isPrepaid=%isPrepaid%&callerID=%callerId%&categoryID=%catId%&clipID=%clipId%&calledNo=%ussdNo%&mode=USSD";
	
	public String mainMenuURL="`#`http://localhost:9910/ussd/ussd.do?msisdn=$subscriber$&processId=0&pageNo=0";
	//mainMenuURL="`#`"+defaultURL+"&processId=0&pageNo=0";
	public String defaultURL="http://localhost:9910/ussd/ussd.do?msisdn=$subscriber$";
	public String askForSpecialCallerMsg="Please enter the mobile number";
	public String calledNo="*678#";
	public String strTechnicalDifficultyMsg="Try later";
	public String strInvalidRequestMsg="Invalid request";
	public String strSuccessMsg="OK. Success !!!";
	public String backEndInvalidReponseMsg="Try later";
	public String backEndSuccessMsg="success";
	public String backEndTechnicalDifficultyMsg="error";
	public boolean unsubscriptionAllowed=true;
	public boolean defaultSubscritpionAllowed=false;
	public boolean advanceSubscriptionAllowed=true;
	public String newLineCharString="\n";
	public String moreStr="0.More";
	public String enterChoiceStr="\nEnter ur choice no.:*g for generic help";
	public int maxStrlengthlimit=140;
	public String defaultSelOption="Select for all callers";
	public String specialCallerSelOption="Select for one caller";
	public String giftOption="Gift it to friends";
	public String prelistenOption="Prelisten";
	public String selChargingMsg="You will be charged Rs 20 for this song Rs 30 as monthly subscription.Press 1 to continue,*1 for previous menu,# for main menu";
	public String giftChargingMsg="Charges Rs 20 if giftee is an existing subscriber.Additional Rs 30 for monthly subscription if giftee is a new subscriber.Press 1 to continue.Press # to go to main menu";
	public String prelistenMsg="To prelisten the song please exit this menu and Dial %nameWavFile% at Rs3/minute.Press *1 to go back to the previous menu.Press *1 Go one level up ";
	public String songSelSuccessmsg="OK.Press 1 to Go one level up";
	public  String ussdParentCat="2";
	public  String ussdCatsNotInBrowse="3,4";
	public  String ussdCatsFreeZone="3";
	public  String whatsHotMsg="There is no free feature:)";
	public  String copyMsg="Please enter the Airtel numbe,you want to copy Hello Tune from eg 9945981517";
	public  String copyReconfirmMsg="You will be charged Rs20 to download this song and additional Rs30 as monthly subscription if you are not a Hello Tune customer.Press 1 to continue or press *1 for previous menu";
	public  String cricketMsg="Cricket is paid feature:)";
	public  String helpMsg="1.Browsing the Hello Tunes Menu";
	public  String helloTunesHelpMsg="Press *1 to go back to the previous menu.Press # to go back to the Hello Tunes Menu.Press *1 to Go one level up.";
	public  String copyURL="&request_type=copy&ans=$answer$";
	public  String mSearchURL=null;
	public  String menuOrder="MSearch,FreeZone,WhatsHot,Browse,Cricket,Copy,Manage,Help";
	public  boolean specialCallerForCopyAllowed=false;
	
	public ConstantInfo(String appName){
		mainMenuURL="`#`"+defaultURL+"&processId=0&pageNo=0";
		String temp=null;
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_DEFAULT_URL", null);
		if(temp!=null){
			defaultURL=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_CALLED_NUMBER", null);
		if(temp!=null){
			calledNo=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_PARENT_CAT", null);
		if(temp!=null){
			ussdParentCat=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_CATS_NOT_IN_BROWSE", null);
		if(temp!=null){
			ussdCatsNotInBrowse=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_CAT_FREE_ZONE", null);
		if(temp!=null){
			ussdCatsFreeZone=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_MENU_ORDER", null);
		if(temp!=null){
			menuOrder=temp;
			temp=null;
		}
		temp = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "USSD_MSEARCH_URL", null);
		if(temp!=null){
			mSearchURL=temp;
			temp=null;
		}
	}
	
}
