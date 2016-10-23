/**
 * 
 */
package com.onmobile.apps.ringbacktones.airtelcgi;

/**
 * @author vinayasimha.patil
 *
 */
public interface DSProtocolConstants
{
	/*Request-Response Message Type*/
	public static final int DS_PING				= 1;
	public static final int DS_PONG				= 2;
	public static final int DS_CONNECT_REQ		= 3;
	public static final int DS_CONNECT_RES		= 4;
	public static final int DS_CONNECT_ERR		= 5;
	public static final int DS_DISCONNECT_REQ	= 6;

	public static final int DS_SUB_PROF_REQ		= 120;
	public static final int DS_SUB_PROF_RES		= 121;
	public static final int DS_SUB_PROF_ERR		= 122;
	public static final int DS_TONE_COPY_REQ	= 161;
	public static final int DS_TONE_COPY_RES	= 162;
	public static final int DS_TONE_COPY_ERR	= 163;
	public static final int DS_TONE_GIFT_REQ	= 164;
	public static final int DS_TONE_GIFT_RES	= 165;
	public static final int DS_TONE_GIFT_ERR	= 166;

	public static final int RBT_DATE_LEN			= 11;
	public static final int RBT_SUBSCRIBER_NO_LEN	= 16;
	public static final int RBT_TONE_ID_LEN			= 16;
	public static final int RBT_SONG_NAME_LEN		= 30;
	public static final int OM_INFO_LEN				= 20;

	/* Provisioning Interfaces*/
	public static final byte DS_IVR				= 'I';	/*Provisioning Request From IVR Interface*/
	public static final byte DS_WEB				= 'W';	/*Provisioning Request From WEB Interface*/
	public static final byte DS_WAP				= 'Q';	/*Provisioning Request From WAP Interface*/
	public static final byte DS_BULK			= 'B';	/*Provisioning Request From BULK Interface*/
	public static final byte DS_USSD			= 'U';	/*Provisioning Request From USSD Interface*/
	public static final byte DS_SMS				= 'M';	/*Provisioning Request From SMS  Interface*/
	public static final byte DS_BULK_CORP		= 'H';	/*Provisioning Request From CORP BULK  Interface*/
	public static final byte DS_MUSIC_SEARCH	= 'Z';	/*Provisioning Request From MUSIC_SEARCH Interface*/
	public static final byte DS_EASYCHARGING	= 'E';	/*Provisioning Request From EASYCHARGING Interface*/
	public static final byte DS_STAR2COPY		= 'T';	/*Provisioning Request From STAR2COPY Interface*/
	public static final byte DS_AUTODIAL		= 'D';	/*Provisioning Request From AUTODIAL Interface*/
	public static final byte DS_AUTODIAL3030	= 'L';	/*Provisioning Request From AUTODIAL3030 Interface*/
	public static final byte DS_ENVIO			= 'N';	/*Provisioning Request From ENVIO Interface*/
	public static final byte DS_KIOSK			= 'K';	/*Provisioning Request From KIOSK Interface*/
	public static final byte DS_ONMOBILE		= 'O';	/*Provisioning Request From ONMOBILE Interface*/
	public static final byte DS_WIRELESS		= 'R';	/*Provisioning Request From WIRELESS Interface*/
	public static final byte DS_SDP				= 'P';	/*Provisioning Request From SDP Interface*/
	public static final byte DS_CUSTOMER_CARE	= 'C';	/*Provisioning Request From CUSTOMER_CARE Interface*/
	public static final byte DS_MOD				= 'F';	/*Provisioning Request From MOD Interface*/
	public static final byte DS_HTAQ			= 'G';	/*Provisioning Request From HTAQ Interface*/
	public static final byte DS_DIT				= 'J';	/*Provisioning Request From DIT Interface*/

	/*ERROR CODES*/
	public static final short NO_ERROR					= 0;	/*Incase of Success*/
	public static final short SUSPEND_PROCESS_STATUS	= 0x9e;	/*Incase of Subscriber De-Provisioning fails or suspended*/
	public static final short SUSPEND_STATUS			= 0x01;	/*Incase of Insufficient Balance*/
	public static final short DATABASE_DOWN				= 0x92;	/*Incase of DataBase is down */
	public static final short POLY_DB_NOT_CONNECTED		= 171;	/*Incase of Polyhedra Database is not connected*/ 
	public static final short RECORD_NOT_FOUND			= 0x91;	/*Incase of subscriber record is not found*/
	public static final short RECORD_EXISTS_ALREADY		= 0x93;	/*Incase of a request made to add a entry , that already exit*/
	public static final short ILLIGAL_DATE_TIME			= 0x70;	/*Incase of Invalid date format*/
	public static final short SUBSCRIBER_DOES_NOT_EXIST	= 0x82;	/*Incase of request for subscriber that doesnot exist*/
	public static final short ILLIGAL_WEEKDAY			= 5;	/*Incase of Invalid WEEDAY*/
	public static final short INVALID_PACKET			= 0x01;	/*Incase of Content of the packet is malformed */
	public static final short PASSWORD_NOT_MATCHED		= 0x89;	/*Incase of Incorrect password*/
	public static final short DB_ERROR					= 0x9F;	/*Incase of any kind of error related to database*/
	public static final short SUBSCRIBER_GIFT_PENDING	= 55;	/*Incase of presenting a gift to a non-HT subscriber already having one gift*/
	public static final short GIFT_INBOX_FULL			= 56;	/*Incase of presenting one more gift to HT subscriber already having 5 gift in the inbox*/
	public static final short SYNTAX_ERROR				= 1001;	/*Incase of malformed request*/

	public static final int DS_CONNECT_LEN		= 24;
	public static final int DS_SUB_PROF_LEN		= 39;
	public static final int DS_TONE_COPY_LEN	= 54;
	public static final int DS_TONE_GIFT_LEN	= 85;

	public static final String OM_SITES		= "OM_SITES";
	public static final String BTSL_SITES	= "BTSL_SITES";
}
