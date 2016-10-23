package com.onmobile.apps.ringbacktones.servlets;

import java.util.Calendar;
import java.util.Date;

public class Junk {
public static void main(String[] args){
	
	Calendar cal=Calendar.getInstance();
	Date dat=cal.getTime();
	long datlong=dat.parse("2008-04-22 17:45:10.250000");
	System.out.println(datlong);
}
}

	

//StringBuffer temp=new StringBuffer();
//temp.append("sub_active=true;sub_id=99162173515;last_bill_date=02-feb;lang=eng;sub_type=default");
//temp.append(";selCount=2;type=gift,caller=9912358627,song=tere naam,clipID=12314,catId=345,artist=hahah;type=gift,caller=9912358627,song=tere naam,clipID=10,catId=345,artist=abhsjfhjinav;giftCount=2;type=gift,caller=9912358627,song=tere naam,clipID=11,catId=355,artist=anand;type=gift,caller=9912358627,song=tere naam,clipID=10,catId=347528975,artist=abhinav");
//SubscriberDetails subDet=login.populateSubInfo(temp,"9916273515");
//System.out.println(subDet.subActive);
//System.out.println(subDet.subId);
//if (subDet.subActive==true) {
//	System.out.println(subDet.lastBillDate);
//	System.out.println(subDet.lang);
//	System.out.println(subDet.subType);
//}	
//System.out.println("no of sel=="+subDet.selCount);
//
//System.out.println("no of gift=="+subDet.giftCount);
//if (subDet.selCount>0) {
//	System.out.println((1) + "st selection "
//			+ subDet.defaultSong.caller + "  "
//			+ subDet.defaultSong.artist + "  "
//			+ subDet.defaultSong.catId + "  "
//			+ subDet.defaultSong.clipId + " "
//			+ subDet.defaultSong.songName);
//	for (int i = 1; i < subDet.selCount; i++) {
//			System.out
//					.println((i)
//							+ "th specialCaller selection "
//							+ ((DemoClip) (subDet.specialCallerSongs.get(i-1))).caller
//							+ "  "
//							+ ((DemoClip) (subDet.specialCallerSongs.get(i-1))).artist
//							+ "  "
//							+ ((DemoClip) (subDet.specialCallerSongs.get(i-1))).catId
//							+ "  "
//							+ ((DemoClip) (subDet.specialCallerSongs.get(i-1))).clipId
//							+ " "
//							+ ((DemoClip) (subDet.specialCallerSongs.get(i-1))).songName);
//
//	
//	}
//}	
//System.out.println("no of gift=="+subDet.giftCount);
//if (subDet.giftCount>0) {
//	for (int i = 0; i < subDet.giftCount; i++) {
//		
//		
//			System.out
//					.println((i+1)
//							+ "th gift selection "
//							+ ((DemoClip) (subDet.giftSongs.get(i))).caller
//							+ "  "
//							+ ((DemoClip) (subDet.giftSongs.get(i))).artist
//							+ "  "
//							+ ((DemoClip) (subDet.giftSongs.get(i))).catId
//							+ "  "
//							+ ((DemoClip) (subDet.giftSongs.get(i))).clipId
//							+ " "
//							+ ((DemoClip) (subDet.giftSongs.get(i))).songName);
//
//		
//	}
//}	