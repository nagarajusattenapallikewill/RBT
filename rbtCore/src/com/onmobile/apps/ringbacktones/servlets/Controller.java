package com.onmobile.apps.ringbacktones.servlets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.StringTokenizer;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.RBTSMSConfig;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.Clips;
import com.onmobile.apps.ringbacktones.content.database.RBTDBManager;
import com.onmobile.apps.ringbacktones.subscriptions.ClipCacher;
import com.onmobile.apps.ringbacktones.subscriptions.ClipGui;


public class Controller extends HttpServlet{
	
	private static Logger logger = Logger.getLogger(Controller.class);
	
	public void doPost(HttpServletRequest request,HttpServletResponse response){
		doGet(request,response);
	}
	public void doGet(HttpServletRequest request,HttpServletResponse response){
		String subscriberID=null;
		HttpSession session=request.getSession();
		ServletContext context=getServletContext();
		String requestSource=((String)request.getParameter("source")); 
		logger.info("RBT::entering request coming for=="+requestSource); 
		if(requestSource.equalsIgnoreCase("signUp")){
			responseToReuqestStatusSignUp(request, response,session, context);

		}
		else if(requestSource.equalsIgnoreCase("unsubscribe")){
			responseToReuqestStatusUnSubscribe(request, response,session, context);

		}
		else if(requestSource.equalsIgnoreCase("update")){
			responseToReuqestStatusUpdate(request, response,session, context);
		}
		else if(requestSource.equalsIgnoreCase("delete")){
			responseToReuqestStatusDelete(request, response,session, context);
		}
		else if(requestSource.equalsIgnoreCase("gift")){
			responseToReuqestStatusGift(request, response,session, context);
		}
		else if(requestSource.equalsIgnoreCase("copy")){
			responseToReuqestStatusCopy(request, response,session, context);
		}
		else if(requestSource.equalsIgnoreCase("selection")){
			responseToReuqestStatusSelection(request, response,session, context);
		}

		else if(requestSource.equalsIgnoreCase("hlr_tick")){
			hlrProcess(request,response,session,context);
		}
		else if(requestSource.equalsIgnoreCase("editCaller")){
			responseToReuqestStatusChangeSelection(request, response,session, context);
		}


	}
	private static void responseToReuqestStatusChangeSelection(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		String queryString=(String)request.getParameter("queryString");
		String newCaller=(String)request.getParameter("newCaller");
		String newVcode=(String)request.getParameter("newVcode");
		if (newVcode!=null) {
			newVcode = newVcode.trim();
		}	
		if (newCaller!=null) {
			newCaller = newCaller.trim();
		}	
		String subId=null;
		String oldCaller=null;
		String clipId=null;
		String catID=null;
		String index=null;
		StringTokenizer st=new StringTokenizer(queryString,";");
		int i=0;
		while(st.hasMoreElements()){
			String temp=st.nextToken();
			StringTokenizer st1=new StringTokenizer(temp,":::");
			while(st1.hasMoreElements()){
				st1.nextToken();
				if(i==0){
					index=st1.nextToken();
				}
				else if(i==1){
					subId=st1.nextToken();
				}
				else if(i==2){
					oldCaller=st1.nextToken();
				}
				else if(i==3){
					clipId=st1.nextToken();
				}
				else if(i==4){
					catID=st1.nextToken();
				}
			}
			i++;
		}
		SubscriberDetails subDet=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
		String chargeClass=null;
		boolean isVcodeValid=ClipCacher.isValidVcode("rbt_"+newVcode+"_rbt");

//		StringTokenizer st2=new StringTokenizer(vcodeTemp,"_");
//		st2.nextToken();
//		String oldVcode=st2.nextToken();
		boolean alreadyNotSetAsDefault=true;
		String wavfile="rbt_"+newVcode+"_rbt";
		if (subDet.defaultSong!=null) {
			if ((!newCaller.equalsIgnoreCase("DEFAULT"))) {
			if(subDet.defaultSong.size()>0){
			if((wavfile.equalsIgnoreCase(((DemoClip)subDet.defaultSong.get(0)).wavfile))){
				alreadyNotSetAsDefault = false;
			}

			}
			}
			}
		boolean callerNoIsNotDefault=true;
		if(newCaller.equalsIgnoreCase("DEFAULT")){
			callerNoIsNotDefault=false;
		}
		boolean check=true;boolean numberAlreadyNotExist=true;
		if(subDet.subActive){
			numberAlreadyNotExist=callerNoAllowedForCopyNGIft(subDet,newCaller);
		}
		else{
			numberAlreadyNotExist=true;
		}
		if(isVcodeValid && numberAlreadyNotExist && callerNoIsNotDefault && alreadyNotSetAsDefault){
			
			
			String newClipId=ClipCacher.init().m_VcodeIDMap.get("rbt_"+newVcode+"_rbt").toString();
			ClipGui clipGui=ClipCacher.init().getClip(new Integer(newClipId).intValue());

			chargeClass=clipGui.getClassType();
			if(chargeClass==null){
				chargeClass="DEFAULT";
			}
			String oldVcode=null;
			DemoClip clip= null;
			if(oldCaller.equalsIgnoreCase("junk")){
				oldVcode="junk";

			}else{
				clip= (DemoClip)((ArrayList)subDet.specialCallerSongs.get(new Integer(index).intValue())).get(0);
				oldVcode=clip.wavfile;


			}
			clipId=""+clipGui.getClipId();
			String defaultCatId=(String)sc.getAttribute("DEFAULT_CAT_ID");

			catID=defaultCatId;
			if(oldVcode.equalsIgnoreCase(newVcode)&& oldCaller.equalsIgnoreCase(newCaller)){
				try {
					//response.encodeURL("/subscriber/subscriber_info.jsp?caller_tab=1");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/success.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				 UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
				SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");
				String freeSong=(String)session.getAttribute("FREE_SONG"); 
                String subs=(String)session.getAttribute("FREE_SONG_MSISDN"); 
                if(freeSong!=null && subs!=null && freeSong.length()>0 && subs.length()>0){ 
                    if(freeSong.trim().equalsIgnoreCase("true") && subs.trim().equalsIgnoreCase(subDet.subId)){ 
                            chargeClass="FREE"; 
                    } 
                } 
				String attachMsg="?request_value=change_selection&SUB_ID="+subDet.subId+"&oldCaller="+oldCaller+"&newCaller="+newCaller+"&catId="+catID+"&clipId="+clipId+"&chargeClass="+chargeClass;
				ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
				if(responseobj.responseStatus ==true){
					if(!responseobj.response.toString().equalsIgnoreCase("error")|| (responseobj.response.toString().indexOf("<rbt><setSelection success=\"false\"/></rbt>")>=0)){ 
						 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                              CCCAccountingManager.addToCCCGUIAccounting(userDetails,subDet.subId,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
                      }
						if (freeSong!=null) { 
                                session.removeAttribute("FREE_SONG"); 
                        } 
                        if (subs!=null) { 
                                session.removeAttribute("FREE_SONG_MSISDN"); 
                        } 
						if(oldCaller.equalsIgnoreCase("junk")){
							//int currentSlectionLength=((ArrayList)(subDet.specialCallerSongs)).size();
							ArrayList nextSelection=new ArrayList();
						
								nextSelection.add(new DemoClip( newCaller,clipGui.getClipName() ,clipGui.getClipId(),new Integer(catID).intValue(),clipGui.getArtist(),clipGui.getWavFile()));
								if((ArrayList)(subDet.specialCallerSongs)==null){ 
                                    subDet.specialCallerSongs=new ArrayList(); 
								} 
								((ArrayList)(subDet.specialCallerSongs)).add(nextSelection);
						
						}else{
							
								((ArrayList)(subDet.specialCallerSongs).get(new Integer(index).intValue())).clear();
								((ArrayList)(subDet.specialCallerSongs).get(new Integer(index).intValue())).add(0,new DemoClip( newCaller,clipGui.getClipName() ,clipGui.getClipId(),new Integer(catID).intValue(),clipGui.getArtist(),clipGui.getWavFile()) ) ;

						}


						try {
							//response.encodeURL("/subscriber/subscriber_info.jsp?caller_tab=1");
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/success.jsp?caller_tab=1");
							view.forward(request,response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                             CCCAccountingManager.addToCCCGUIAccounting(userDetails,subDet.subId,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
                     }
						logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
						try {
							//response.encodeURL("/subscriber/systemDown.jsp");
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
							view.forward(request,response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}
				}else{
					  if(userDetails!=null && attachMsg!=null){
                           CCCAccountingManager.addToCCCGUIAccounting(userDetails,subDet.subId,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
                   }
					 if(userDetails!=null && attachMsg!=null){
                          CCCAccountingManager.addToCCCGUIAccounting(userDetails,subDet.subId,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
                  }
					logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
					try {
						//response.encodeURL("/subscriber/systemDown.jsp");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}else{
			logger.info("RBT::check == invalid Vcode !!! Check it out Dude!!!)"); 
			try {
				//response.encodeURL("/subscriber/systemDown.jsp");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/invalidVcode.jsp?caller_tab=1");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}


	}

	private static void responseToReuqestStatusSelection(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		//<rbt><setSelection success=\"true\"/></rbt>

		String callerno=null;
		SubscriberDetails subDet=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
		if(subDet.subActive && !((String)subDet.subYes).equalsIgnoreCase("D") && !((String)subDet.subYes).equalsIgnoreCase("P")&& !((String)subDet.subYes).equalsIgnoreCase("Z")){
			String subID=((String)(subDet).subId);
			String queryString=(String)request.getParameter("queryString");
			String catId=null;
			String clipId=null;
			String chargeClass=null;
			String songName=null;
			String artist=null;
			String wavfile=null;
			StringTokenizer st1=new StringTokenizer(queryString,";");
			String temp=null;
			int i=0;
			while(st1.hasMoreElements()){
				temp=st1.nextToken();
				StringTokenizer st2=new StringTokenizer(temp,":::");
				st2.nextToken();
				String value=st2.nextToken();
				System.out.println("value==="+value);
				if(i==0){
					catId=value;
					System.out.println("catId=="+catId);

				}
				else if(i==1){
					clipId=value;
					System.out.println("clipId=="+clipId);
				}
				else if(i==2){
					artist=value;
					System.out.println("artist=="+artist);
				}
				else if(i==3){
					chargeClass=value;
					System.out.println("chargeClass=="+chargeClass);
				}
				else if(i==4){
					songName=value;
					System.out.println("songName=="+songName);
				}
				else if(i==5){
					wavfile=value;
					System.out.println("wavfile=="+wavfile);
				}
				else if(i==6){
					callerno=value;
					System.out.println("callerno=="+callerno);
				}
				i++;
			}
			String defaultCatId=(String)sc.getAttribute("DEFAULT_CAT_ID");
			if(catId==null || catId.equalsIgnoreCase("null")){
				catId=defaultCatId;
			}
			UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
			String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_GUI", null);
			int n = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "NUM_CONN", 4);
			RBTDBManager rbtdbManager=rbtdbManager=RBTDBManager.init(dbURL, n);
			Clips clip =rbtdbManager.getClip(new Integer(clipId).intValue());
			Date clipDate=clip.endTime();
			Calendar cal1=Calendar.getInstance();
			Date sysDate=cal1.getTime();
			boolean clipExpired=false;
			if(sysDate.after(clipDate)){
				clipExpired=true;
			}
			SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");

			logger.info("RBT::check == false and url is not null"); 
			if(!clipExpired){
				String freeSong=(String)session.getAttribute("FREE_SONG"); 
                String subs=(String)session.getAttribute("FREE_SONG_MSISDN"); 
                if(freeSong!=null && subs!=null && freeSong.length()>0 && subs.length()>0){ 
                        if(freeSong.trim().equalsIgnoreCase("true") && subs.trim().equalsIgnoreCase(subID)){ 
                                chargeClass="FREE"; 
                        } 
                } 
				String attachMsg="?request_value=selection&SUB_ID="+subID+"&callerno="+callerno+"&catId="+catId+"&clipId="+clipId+"&chargeClass="+chargeClass;
				ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
				if(responseobj.responseStatus ==true){
						if(responseobj.response.toString().equalsIgnoreCase("<rbt><setSelection success=\"true\"/></rbt>")){
							 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                                  CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
                          }
							if (freeSong!=null) { 
		                        session.removeAttribute("FREE_SONG"); 
		                } 
		                if (subs!=null) { 
		                        session.removeAttribute("FREE_SONG_MSISDN"); 
		                } 
						String strLastBillDate=null;
						if(!subDet.subActive){
							Calendar cal=Calendar.getInstance();
							Date date=cal.getTime();
							strLastBillDate=""+date.getDate()+"-";
							strLastBillDate=strLastBillDate+getMonth(date.getMonth());
							subDet.subActive=true;
							subDet.lang="eng";
							subDet.subYes="A";
							subDet.blackList=false;
							subDet.subType="default";
							subDet.lastBillDate=strLastBillDate;
							subDet.specialCallerSongs=new ArrayList();
						}
						if(subDet.specialCallerSongs==null){
							subDet.specialCallerSongs=new ArrayList();
						}
						int callerListSize=subDet.selCount;
						ArrayList tempArr=new ArrayList();
						if(!callerno.equalsIgnoreCase("DEFAULT")){

							tempArr.add(new DemoClip( callerno,songName ,new Integer(clipId).intValue(),new Integer(catId).intValue(),artist,wavfile));

							if(subDet.defaultSong!=null && subDet.defaultSong.size()>0){
								subDet.specialCallerSongs.add((callerListSize-1), (tempArr));
							}else{
								subDet.specialCallerSongs.add((callerListSize), (tempArr));
							}
							subDet.selCount++;

						}else{
							tempArr.add(new DemoClip( callerno,songName ,new Integer(clipId).intValue(),new Integer(catId).intValue(),artist,wavfile));
							subDet.defaultSong=tempArr;		
							subDet.selCount++;

						}
						try {
							//response.encodeURL("/subscriber/success.jsp?caller_tab=1");
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/success.jsp?caller_tab=1&source=close");
							view.forward(request,response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

					else{
						 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                              CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
                      }
						logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
						try {
							//response.encodeURL("/subscriber/systemDown.jsp");
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&source=close");
							
							view.forward(request,response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else{
					if(userDetails!=null && attachMsg!=null){
                         CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
                 }
					logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
					try {
						//response.encodeURL("/subscriber/systemDown.jsp");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&source=close");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else{
				try {
					//response.encodeURL("/subscriber/success.jsp?caller_tab=1");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/expiredClip.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			try {
				//response.encodeURL("/subscriber/success.jsp?caller_tab=1");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/unsubscribedUser.jsp?caller_tab=1");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}


	private static void responseToReuqestStatusCopy(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		String callerno=(String)request.getParameter("callerno");

		SubscriberDetails subDetCopy=(SubscriberDetails)session.getAttribute("COPY_SUB_DETAILS");
		String copymsisdn=(String)(subDetCopy.subId);
		SubscriberDetails subDet=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
		String wavfile=null;
		String catId=null;
		String subID=((String)(subDet).subId);
		Clips defaultClip=(Clips) sc.getAttribute("DEFAULT_CLIP");
		SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");
		 UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
		if(((((ArrayList)subDetCopy.defaultSong)!=null)&& (((ArrayList)subDetCopy.defaultSong).size()>0))){
			wavfile=""+((DemoClip)(subDetCopy.defaultSong.get(0))).wavfile;
			catId=""+((DemoClip)(subDetCopy.defaultSong.get(0))).catId;


		}else{
			if(defaultClip!=null){
				catId="26";
				wavfile=defaultClip.previewFile();
			}
		}
		logger.info("RBT::check == false and url is not null"); 
		boolean check=true;boolean numberAlreadyNotExist=true;
		if(subDet.subActive){
			numberAlreadyNotExist=callerNoAllowedForCopyNGIft(subDet,callerno);
		}
		else{
			numberAlreadyNotExist=true;
		}
		if (subDet.defaultSong!=null) {
		if ((!callerno.equalsIgnoreCase("DEFAULT"))) {
		if(subDet.defaultSong.size()>0){
		if((wavfile.equalsIgnoreCase(((DemoClip)subDet.defaultSong.get(0)).wavfile))){
		check = false;
		}

		}
		}
		}
	
		if(subDet.specialCallerSongs!=null && subDet.specialCallerSongs.size()==3 && !callerno.equalsIgnoreCase("DEFAULT")&& numberAlreadyNotExist){
			logger.info("invalid number");
			check=false;
		}
		if(check==true){
			int clipId=0;
			Clips clip =null;
			boolean clipExpired=false;
			String dbURL = RBTParametersUtils.getParamAsString(iRBTConstant.COMMON, "DB_URL_GUI", null);
			int n = RBTParametersUtils.getParamAsInt(iRBTConstant.SMS, "NUM_CONN", 4);
			RBTDBManager rbtdbManager=RBTDBManager.init(dbURL, n);
			if((((ArrayList)subDetCopy.defaultSong)!=null)&& (((ArrayList)subDetCopy.defaultSong).size()>0)){
				clipId=((DemoClip)(subDetCopy.defaultSong.get(0))).clipId;
				clip =rbtdbManager.getClip(new Integer(clipId).intValue());
				Date clipDate=clip.endTime();
				Calendar cal1=Calendar.getInstance();
				Date sysDate=cal1.getTime();

				if(sysDate.after(clipDate)){
					clipExpired=true;
				}
			}

			if(!clipExpired){
				String attachMsg="?request_value=copy&SUB_ID="+subID+"&callerno="+callerno+"&wavfile="+wavfile+"&catId="+catId+"&copymsisdn="+copymsisdn;
				ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
				if(responseobj.responseStatus ==true){
					if(!responseobj.response.toString().equalsIgnoreCase("error")){
						 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                              CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
                      }

						session.removeAttribute("COPY_SUB_DETAILS");
						try {
							//response.encodeURL("/subscriber/subscriber_info.jsp?caller_tab=1");
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp?caller_tab=1");
							view.forward(request,response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					else{
						 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                              CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
                      }
						logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
						try {
							//response.encodeURL("/subscriber/systemDown.jsp");
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
							view.forward(request,response);
						} catch (ServletException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else{
					if(userDetails!=null && attachMsg!=null){
                         CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
                 }
					logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
					try {
						//response.encodeURL("/subscriber/systemDown.jsp");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}else{

				//response.encodeURL("/subscriber/copycontroller.jsp?copynumber="+copynumber);
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/expiredClip.jsp?caller_tab=1");
				try {
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else{
			try {
				//response.encodeURL("/subscriber/invalidNumber.jsp?toPage=/ccc/subscriber/copycontroller.jsp&giftindex=copy");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/invalidNumber.jsp?toPage=/ccc/subscriber/copycontroller.jsp&giftindex=copy");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private static void responseToReuqestStatusGift(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		String callerno=(String)request.getParameter("callerno");
		String giftindex=(String)request.getParameter("giftindex");
		int gift=new Integer(giftindex).intValue();
		SubscriberDetails subDet=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
		String subID=((String)(subDet).subId);
		SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");
		logger.info("RBT::check == false and url is not null"); 
		boolean check=true;
		//callerNoAllowed(subDet,callerno);
		int clipId=((DemoClip)subDet.giftSongs.get(gift)).clipId;
		if (subDet.defaultSong!=null && subDet.defaultSong.size()>0) {
		if ((clipId == ((DemoClip)subDet.defaultSong.get(0)).clipId)
		&& (!callerno.equalsIgnoreCase("DEFAULT"))) {
		check = false;
		}
		}		
		boolean numberAlreadyNotExist=true;
		if(subDet.subActive){
			numberAlreadyNotExist=callerNoAllowedForCopyNGIft(subDet,callerno);
		}
		else{
			numberAlreadyNotExist=true;
		}
		if(subDet.specialCallerSongs!=null && subDet.specialCallerSongs.size()==3 && !callerno.equalsIgnoreCase("DEFAULT")&& numberAlreadyNotExist){
			logger.info("invalid number");
			check=false;
		}
		if(check==true){

			String gifter=((DemoClip)subDet.giftSongs.get(gift)).caller;
			String sentTime=((DemoClip)subDet.giftSongs.get(gift)).sentTime;
			StringTokenizer st1=new StringTokenizer(sentTime," ");
			String temp1=st1.nextToken();
			String temp2=st1.nextToken();
			sentTime=temp1+";;;"+temp2;
			UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
			String attachMsg="?request_value=gift&SUB_ID="+subID+"&callerno="+callerno+"&sentTime="+sentTime+"&clipId="+clipId+"&gifter="+gifter;
			ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
			System.out.println("responseStatus=="+responseobj.responseStatus+"and responseString=="+responseobj.response.toString());
			if(responseobj.responseStatus ==true){
				if(!responseobj.response.toString().equalsIgnoreCase("error")){
					 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                          CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
                  }
//					
						subDet.giftSongs.remove(gift);
						subDet.giftCount--;
					//}
					try {
						//response.encodeURL("/subscriber/success.jsp?caller_tab=1");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/success.jsp?caller_tab=1");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                          CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
                  }
					logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
					try {
						//response.encodeURL("/subscriber/systemDown.jsp");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			else{
				 if(userDetails!=null && attachMsg!=null){
                      CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
              }
				logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
				try {
					//response.encodeURL("/subscriber/systemDown.jsp");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else{
			try {
				//response.encodeURL("/subscriber/invalidNumber.jsp?toPage=/ccc/subscriber/gift.jsp&giftindex="+giftindex);
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/invalidNumber.jsp?toPage=/ccc/subscriber/gift.jsp&giftindex="+giftindex);
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	private static boolean callerNoAllowedForCopyNGIft(SubscriberDetails subDet,String callerno){

		if(subDet.subId.equalsIgnoreCase(callerno)){
			return false;
		}
		if(callerno.equalsIgnoreCase("DEFAULT")){
			if(subDet.defaultSong!=null && subDet.defaultSong.size()>0){
				System.out.println("returning "+false+"from callernoallowed");
				return false;
			}
			else{ 

				System.out.println("returning "+true+"from callernoallowed");
				return true;
			}
		}
		else{
			if (subDet.specialCallerSongs!=null) {
			
					if(subDet.specialCallerSongs.size()>0){
						for (int i = 0; i < subDet.specialCallerSongs
						.size(); i++) {

							if (((DemoClip) ((ArrayList)subDet.specialCallerSongs
									.get(i)).get(0)).caller
									.equalsIgnoreCase(callerno)) {
								System.out.println("returning " + false
										+ "from callernoallowed");
								return false;
							}
						}
					}
				
			}						
		}
		System.out.println("returning "+true+"from callernoallowed");
		return true;
	
	}
	private static boolean callerNoAllowed(SubscriberDetails subDet,String callerno){
		if(subDet.subId.equalsIgnoreCase(callerno)){
			return false;
		}
		if(callerno.equalsIgnoreCase("DEFAULT")){
			if(subDet.defaultSong!=null && subDet.defaultSong.size()>0){
				System.out.println("returning "+false+"from callernoallowed");
				return false;
			}
			else{ 

				System.out.println("returning "+true+"from callernoallowed");
				return true;
			}
		}
		else{
			if (subDet.specialCallerSongs!=null) {
				if (subDet.specialCallerSongs.size() == 3) {
					System.out.println("returning " + false
							+ "from callernoallowed");

					return false;
				} else {
					if(subDet.specialCallerSongs.size()>0){
						for (int i = 0; i < subDet.specialCallerSongs
						.size(); i++) {

							if (((DemoClip) ((ArrayList)subDet.specialCallerSongs
									.get(i)).get(0)).caller
									.equalsIgnoreCase(callerno)) {
								System.out.println("returning " + false
										+ "from callernoallowed");
								return false;
							}
						}
					}
				}
			}						
		}
		System.out.println("returning "+true+"from callernoallowed");
		return true;
	}
	private static void responseToReuqestStatusGiftDel(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		System.out.println("inside delete*****************Gift inbox");

		String queryString=(String)request.getParameter("queryString");

		System.out.println("queryString=="+queryString);
		StringTokenizer st=new StringTokenizer(queryString,";");
		String callerno=null;
		String groupno=null;
		String action=null;
		String giftindex=null;
		int i=0;
		while(st.hasMoreElements()){
			String chh=st.nextToken();
			System.out.println(chh);
			StringTokenizer st1=new StringTokenizer(chh,":::");


			System.out.println(st1.nextToken());


			String temp=st1.nextToken();
			if(i==0){
				callerno=temp;
				System.out.println("callerno=="+callerno);
			}
			else if(i==1){
				groupno=temp;
				System.out.println("groupno=="+groupno);
			}
			else if(i==2){
				action=temp;	
			}
			else if(i==3){
				giftindex=temp;
			}

			i++;
		}


		int gift=new Integer(giftindex).intValue();
		SubscriberDetails subDet=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
		String subID=((String)(subDet).subId);
		SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");
		logger.info("RBT::check == false and url is not null"); 

		int clipId=((DemoClip)subDet.giftSongs.get(gift)).clipId;



		String gifter=((DemoClip)subDet.giftSongs.get(gift)).caller;
		String sentTime=((DemoClip)subDet.giftSongs.get(gift)).sentTime;
		StringTokenizer st1=new StringTokenizer(sentTime," ");
		String temp1=st1.nextToken();
		String temp2=st1.nextToken();
		sentTime=temp1+";;;"+temp2;
		 UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
		String attachMsg="?request_value=gift&SUB_ID="+subID+"&callerno="+callerno+"&sentTime="+sentTime+"&clipId="+clipId+"&gifter="+gifter;
		ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
		System.out.println("responseStatus=="+responseobj.responseStatus+"and responseString=="+responseobj.response.toString());
		if(responseobj.responseStatus ==true){
			if(!responseobj.response.toString().equalsIgnoreCase("error")){
				if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                     CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
             }
				subDet.giftSongs.remove(gift);
				subDet.giftCount--;

				try {
					//response.encodeURL("/subscriber/success.jsp?caller_tab=1");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/success.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                      CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
              }
				logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
				try {
					//response.encodeURL("/subscriber/systemDown.jsp");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		else{
			 if(userDetails!=null && attachMsg!=null){
                  CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
          }
			logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
			try {
				//response.encodeURL("/subscriber/systemDown.jsp");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}



	}
	private static void responseToReuqestStatusDelete(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		System.out.println("inside delete*****************");

		String queryString=(String)request.getParameter("queryString");

		System.out.println("queryString=="+queryString);
		StringTokenizer st=new StringTokenizer(queryString,";");
		String callerno=null;
		String groupno=null;
		String action=null;
		int i=0;
		while(st.hasMoreElements()){
			String chh=st.nextToken();
			System.out.println(chh);
			StringTokenizer st1=new StringTokenizer(chh,":::");


			System.out.println(st1.nextToken());


			String temp=st1.nextToken();
			if(i==0){
				callerno=temp;
				System.out.println("callerno=="+callerno);
			}
			else if(i==1){
				groupno=temp;
				System.out.println("groupno=="+groupno);
			}
			else if(i==2){
				action=temp;
				if(action.equalsIgnoreCase("delGift")){
					responseToReuqestStatusGiftDel(request, response,session, sc);
					return;
				}

			}

			i++;
		}


		int callerindex=new Integer(groupno).intValue();
		String subID=((String)((SubscriberDetails)session.getAttribute("SUB_DETAILS")).subId);
		SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");
		logger.info("RBT::check == false and url is not null"); 
		String source=(String)request.getParameter("source");
		String attachMsg="?request_value="+source+"&SUB_ID="+subID+"&callerno="+callerno;
		UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
		ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
		if(responseobj.responseStatus ==true){
			if(!responseobj.response.toString().equalsIgnoreCase("error")){
				 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                      CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
              }
				SubscriberDetails subDetails=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
				if(callerindex==0){
					subDetails.defaultSong=null;
					(subDetails.selCount)--;
				}
				else{
					ArrayList d=new ArrayList();

					subDetails.specialCallerSongs.remove(callerindex-1);
					(subDetails.selCount)--;
				}
				try {
					//response.encodeURL("/subscriber/subscriber_info.jsp?caller_tab=1");
					RequestDispatcher view=null;
					if(action.equalsIgnoreCase("delSongSel")){
						 view=request.getRequestDispatcher("/subscriber/success.jsp?caller_tab=1&source=close");
					}else{
							view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp?caller_tab=1");
					}
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                      CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
              }
				logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
				try {
					//response.encodeURL("/subscriber/systemDown.jsp");
					RequestDispatcher view=null;
					if(action.equalsIgnoreCase("delSongSel")){
						 view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&source=close");
					}else{
						 view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
					}
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		else{
			if(userDetails!=null && attachMsg!=null ){
                 CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
         }
			logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
			try {
				//responsRL("/subscriber/systemDown.jsp");
				RequestDispatcher view=null;
				if(action.equalsIgnoreCase("delSongSel")){
					 view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1&source=close");
				}else{
					 view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
				}
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private void hlrProcess(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		{
			String dispMessage = null;
			String hlrsuccess =null;
			 UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
			if(request.getAttribute("success")!= null){
				hlrsuccess =request.getAttribute("success").toString();
			}
			else
			{
				hlrsuccess="false";
			}
			String requestSource=(String)request.getParameter("source"); 
			String subscriberID=null;

			logger.info("RBT::entering MSISDN "); 
			subscriberID=((String)request.getParameter("msisdn"));
			if(subscriberID!=null){
				subscriberID=subscriberID.trim();
				logger.info("RBT::inside MSISDN  subscriberID== "+ subscriberID); 
			}
			else{
				logger.info("RBT::inside MSISDN  subscriberID== "+ subscriberID); 
			}
			SiteURLDetails destUrlDetail=login.getDestURL(sc,subscriberID);
			if(destUrlDetail==null){
				logger.info("RBT::check == false and url is null"); 
				try {
					dispMessage= "This number is not a valid Airtel Number.";
					// response.encodeURL("/subscriber/hlr_tick.jsp?source=hllr&message="+dispMessage);
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/hlr_tick.jsp?source=hllr&message="+dispMessage);
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				logger.info("RBT::check == false and url is not null"); 

				String attachMsg="?request_value=hlr_tick&SUB_ID="+subscriberID;
				String northCircleID=(String)sc.getAttribute("NORTH_CIRCLE_ID");
				String northWestCircleId=(String)sc.getAttribute("NORTH_WEST_CIRCLE_ID");
				
				String testStatus=(String)sc.getAttribute("TEST_STATUS");
				ArrayList testNumbers=(ArrayList)sc.getAttribute("TEST_NUMBERS");
				String testCircleId=(String)sc.getAttribute("TEST_CIRCLE_ID");
				boolean isToMakeHttpHit=false;
				if(testStatus!=null && testStatus.equalsIgnoreCase("true")){

					if(testNumbers!=null && testNumbers.contains(subscriberID)){
						isToMakeHttpHit=false;
						HashMap site_url_details=(HashMap)(sc.getAttribute("SITE_URL_MAP"));
						if(testCircleId!=null){
						destUrlDetail=(SiteURLDetails)(site_url_details.get(testCircleId));
						}else{
							isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID) || destUrlDetail.circle_id.equalsIgnoreCase(northWestCircleId) ;
						}
					}else{
						isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID) || destUrlDetail.circle_id.equalsIgnoreCase(northWestCircleId) ;
					}
				
				}else{
					
					isToMakeHttpHit=destUrlDetail.circle_id.equalsIgnoreCase(northCircleID) || destUrlDetail.circle_id.equalsIgnoreCase(northWestCircleId) ;
				}
				//ResponseObj responseobj=new ResponseObj();
				if(!isToMakeHttpHit){
					String tohit= null;
					ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
					String responser = responseobj.response.toString();
					String[] strArray =responser.split(";");
					//StringTokenizer st=new StringTokenizer(responser);

					String subStatus=strArray[0];
					String subClass=null;
					if(strArray.length >1)
						subClass = strArray[1];

					if(responseobj.responseStatus ==true){
						if(!subStatus.equalsIgnoreCase("error")){
							if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                                 CCCAccountingManager.addToCCCGUIAccounting(userDetails,subscriberID,attachMsg.substring(1),hlrsuccess,responseobj.responseStatus);
                         }
							if (hlrsuccess.equalsIgnoreCase("false")) {
								if(subStatus.length()<=0 || subStatus.equalsIgnoreCase("X") || subStatus.equalsIgnoreCase("D") ||subStatus.equalsIgnoreCase("P") ){
									dispMessage="The number "+ subscriberID + " is UnSubscribed " ;
								}else if (subStatus.equalsIgnoreCase("G") ||subStatus.equalsIgnoreCase("A") || subStatus.equalsIgnoreCase("N") || subStatus.equalsIgnoreCase("E") || subStatus.equalsIgnoreCase("F")){
									dispMessage="Activation is under process for the number "+ subscriberID +".Please try after sometime.";
								}else if (subStatus.equalsIgnoreCase("Z")){
									dispMessage= subscriberID +" is currently suspended . Unable to process request.";
								}else if(subStatus.equalsIgnoreCase("B")){
									dispMessage="HLR request submitted successfully for the number "+ subscriberID;
									hlrsuccess="true";
								}
							}else{
								/*    
                                      login.makeHttpRequest(SiteURLDetails url,"/subscription/hlrHit.jsp?msisdn="+subscriberID);       
								 */
							}

						}     
						else{
							 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                                  CCCAccountingManager.addToCCCGUIAccounting(userDetails,subscriberID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
                          }
							/*
							 * Facing technical difficuties
							 * */
							dispMessage="We are facing technical difficulties please try after sometime.";
							// response.encodeURL("/subscriber/hlr_tick.jsp?source=hlr_tick&message="+dispMessage+"&success="+hlrsuccess+"&subId="+subscriberID);
							RequestDispatcher view=request.getRequestDispatcher("/subscriber/hlr_tick.jsp?source=hlr_tick&message="+dispMessage+"&success="+hlrsuccess+"&subId="+subscriberID);
							try {
								view.forward(request,response);
							} catch (ServletException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}                 
						}

					}
					if(dispMessage.length()<1)
						dispMessage= "This number is not a valid Airtel Number.";
					//  response.encodeURL("/subscriber/hlr_tick.jsp?source=hlr_tick&message="+dispMessage+"&success="+hlrsuccess+"&subId="+subscriberID);
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/hlr_tick.jsp?source=hlr_tick&message="+dispMessage+"&success="+hlrsuccess+"&subId="+subscriberID+"&subClass="+subClass);
					try {
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}else{

					logger.info("RBT::check == false and url is null"); 
					try {
						dispMessage= "This number is not a valid Airtel Number.";
						// response.encodeURL("/subscriber/hlr_tick.jsp?source=hllr&message="+dispMessage);
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/hlr_tick.jsp?source=hllr&message="+dispMessage);
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		}
	}

	private static void responseToReuqestStatusUpdate(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		String subID=((String)((SubscriberDetails)session.getAttribute("SUB_DETAILS")).subId);
		String inittype=((String)((SubscriberDetails)session.getAttribute("SUB_DETAILS")).subType);
		String finaltype=(String)request.getParameter("finaltype");
		String newlang=(String)request.getParameter("newlang");
		int lang=new Integer(newlang).intValue();
		String[] langarr=(String[])sc.getAttribute("LANG");
		newlang=langarr[lang-1];
		int type=new Integer(finaltype).intValue();
		ArrayList tagSubPack=(ArrayList)sc.getAttribute("SUB_PACK_TAG");
//		ArrayList tagSubPack=(ArrayList)sc.getAttribute("SUB_PACK_TAG"); 
        ArrayList tagSubPackTemp=(ArrayList)sc.getAttribute("SUB_PACK_TAG"); 
        if(tagSubPackTemp!=null && tagSubPackTemp.size()>0){ 
                for(int count=0;count<tagSubPackTemp.size();count++){ 
                        tagSubPack.add(tagSubPackTemp.get(count)); 
                } 
        } 
        int countHafta=1; 
        String strHaftaToAdvance=(String)sc.getAttribute("HAFTA_TO_ADVANCE"); 
        boolean haftaToAdvance=false; 
        if(strHaftaToAdvance!=null && strHaftaToAdvance.equalsIgnoreCase("true")){ 
                haftaToAdvance=true; 
        } 
        if(haftaToAdvance && inittype.equalsIgnoreCase("HAFTA")){ 
                if(tagSubPack!=null && tagSubPack.size()>0){ 
                        for(int t=1;t<tagSubPack.size();t++){ 
                                if(((String)tagSubPack.get(t)).trim().equalsIgnoreCase("HAFTA")){ 
                                        countHafta=t; 
                                } 
                        } 
                } 
                tagSubPack.remove(countHafta); 
        } 
		SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");
		logger.info("RBT::check == false and url is not null"); 
		String source=(String)request.getParameter("source");
		String attachMsg="?request_value="+source+"&SUB_ID="+subID+"&inittype="+inittype+"&finaltype="+finaltype+"&newlang="+newlang;
		 UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
		ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
		if(responseobj.responseStatus ==true){
			if(!responseobj.response.toString().equalsIgnoreCase("error")){
				if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                     CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
             }
				SubscriberDetails subDetails=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
				if(!finaltype.equalsIgnoreCase("Default")){
					subDetails.subType=finaltype;
					subDetails.subYes="C";
				}
				subDetails.lang=newlang;

				try {
					//response.encodeURL("/subscriber/subscriber_info.jsp?caller_tab=1");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                      CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
              }
				logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
				try {
					//	response.encodeURL("/subscriber/systemDown.jsp");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		else{
			if(userDetails!=null && attachMsg!=null ){
                 CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
         }
			logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
			try {
				//response.encodeURL("/subscriber/systemDown.jsp");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	private static void responseToReuqestStatusUnSubscribe(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		String subID=((String)((SubscriberDetails)session.getAttribute("SUB_DETAILS")).subId);
		SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");
		logger.info("RBT::check == false and url is not null"); 
		String source=(String)request.getParameter("source");
		 UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
	 	 
		String attachMsg="?request_value="+source+"&SUB_ID="+subID;
		ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
		if(responseobj.responseStatus ==true){
			if(!responseobj.response.toString().equalsIgnoreCase("error")){
				if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                     CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
             }
				SubscriberDetails subDetails=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
				subDetails.subActive=true;
				subDetails.subYes="D";
//				session.removeAttribute("MSISDN_ENTERED");
				try {
					//response.encodeURL("/subscriber/subscriber_info.jsp?caller_tab=1");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/subscriber_info.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else{
				if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                     CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
             }
				logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
				try {
					//response.encodeURL("/subscriber/systemDown.jsp");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}
		else{
			 if(userDetails!=null && attachMsg!=null ){
                  CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
          }
			logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
			try {
				//response.encodeURL("/subscriber/systemDown.jsp");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
	private static void responseToReuqestStatusSignUp(HttpServletRequest request,HttpServletResponse response,HttpSession session,ServletContext sc){
		String sub_typeTemp=(String)request.getParameter("subsType4Airtel");
		String pwd=(String)request.getParameter("pwd1");
		String lang_type=(String)request.getParameter("langType");
		String subID=((String)((SubscriberDetails)session.getAttribute("SUB_DETAILS")).subId);
		String pwdSet=(String)session.getAttribute("PWD");
		ArrayList tagSubPack=(ArrayList)sc.getAttribute("SUB_PACK_TAG");
		String sub_type=null;
		int lang=new Integer(lang_type).intValue();
		int temp=new Integer(sub_typeTemp).intValue();
		for(int i=0;i<tagSubPack.size();i++){
			System.out.println("choice count=="+i);
			if(i==temp){
				sub_type=(String)tagSubPack.get(i);
				System.out.println("choice final"+sub_type);
			}

		}
		String[] langs=null;
		if(lang!=0){
			langs=(String[])sc.getAttribute("LANG");
			lang_type=langs[lang-1];
		}
		else{
			lang_type="eng";
		}

		if(pwd.equals(pwdSet)){
			SiteURLDetails destUrlDetail=(SiteURLDetails)session.getAttribute("DEST_URL_DETAILS");


			logger.info("RBT::check == false and url is not null"); 
			   UserDetails userDetails=(UserDetails)session.getAttribute("USER_DETAILS");
			String attachMsg="?request_value=signUp&SUB_ID="+subID+"&sub_type="+sub_type+"&lang_type="+lang_type;
			ResponseObj responseobj=login.makeHttpRequest(destUrlDetail,attachMsg);
			if(responseobj.responseStatus ==true){
				if(!responseobj.response.toString().equalsIgnoreCase("error")){
					 if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                          CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"SUCCESS",responseobj.responseStatus);
                  }
					SubscriberDetails subDetails=(SubscriberDetails)session.getAttribute("SUB_DETAILS");
					subDetails.subActive=true;
					subDetails.lang=lang_type;
					subDetails.subType=sub_type;
					subDetails.blackList=false;
					subDetails.subYes="A";
					Calendar cal=Calendar.getInstance();
					Date dat=cal.getTime();
					String strLastBillDate=null;
					strLastBillDate=""+dat.getDate()+"-";
					strLastBillDate=strLastBillDate+getMonth(dat.getMonth())+"-";
					strLastBillDate=strLastBillDate+(dat.getYear()+1900);
					subDetails.lastBillDate=strLastBillDate;
					subDetails.selCount=0;
					subDetails.giftCount=0;
					try {
						//response.encodeURL("/subscriber/success.jsp?caller_tab=1");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/success.jsp?caller_tab=1");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else{
					if(userDetails!=null && attachMsg!=null && responseobj.response.length()>=0){
                         CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"ERROR",responseobj.responseStatus);
                 }
					logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
					try {
						//response.encodeURL("/subscriber/systemDown.jsp");
						RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
						view.forward(request,response);
					} catch (ServletException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
			else{
				 if(userDetails!=null && attachMsg!=null ){
                      CCCAccountingManager.addToCCCGUIAccounting(userDetails,subID,attachMsg.substring(1),"Oops!!!Technical Difficulties",responseobj.responseStatus);
              }
				logger.info("RBT::check == no subscriber Detail available....Oops !!!! technical Difficulties. Plz try after some time:)"); 
				try {
					//response.encodeURL("/subscriber/systemDown.jsp");
					RequestDispatcher view=request.getRequestDispatcher("/subscriber/systemDown.jsp?caller_tab=1");
					view.forward(request,response);
				} catch (ServletException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}else{
			try {
				//response.encodeURL("/subscriber/Reject.jsp");
				RequestDispatcher view=request.getRequestDispatcher("/subscriber/Reject.jsp");
				view.forward(request,response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static String getMonth(int month){
		if(month==0){
			return "JAN";
		}
		else if(month==1){
			return "FEB";
		}
		else if(month==2){
			return "MAR";
		}
		else if(month==3){
			return "APR";
		}
		else if(month==4){
			return "MAY";
		}
		else if(month==5){
			return "JUN";
		}
		else if(month==6){
			return "JUL";
		}
		else if(month==7){
			return "AUG";
		}
		else if(month==8){
			return "SEP";
		}
		else if(month==9){
			return "OCT";
		}
		else if(month==10){
			return "NOV";
		}
		else{
			return "DEC";
		}
	}
}
