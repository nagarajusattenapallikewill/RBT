package com.onmobile.apps.ringbacktones.v2.common;

public abstract interface MessageResource {

	//Library
	public static final String LIBRARY_DELETE_MESSAGE = "library.delete.message.";
	public static final String LIBRARY_DELETE_CODE = "library.delete.code.";	
	public static final String LIKE_CONTENT_MESSAGE = "like.content.message.";
	public static final String LIBRARY_GET_MESSAGE = "library.get.message.";
	public static final String LIBRARY_UPDATE_MESSAGE = "library.update.message.";
	
	//Settings
	public static final String DELETE_SETTING_MESSAGE = "setting.delete.message.";
	public static final String ACT_SETTING_MESSAGE = "setting.act.message.";
	public static final String SUB_DONT_EXIST_MESSAGE = "sub.dont.exist.message.";
	public static final String LIST_PLAY_RULE_MESSAGE = "playrule.list.message.for.";
	
	//Profile
	public static final String PROFILE_MESSAGE = "profile.message.";
	
	//General
	public static final String GENERAL_MESSAGE = "general.message";
	
	public static final String INVALID_PARAMETER_MESSAGE = "invalid.parameter.message";
	public static final String INVALID_CONTENT_TYPE_MESSAGE = "invalid.content.type.message";
	//Added for subtype
	public static final String INVALID_CONTENT_SUB_TYPE_MESSAGE = "invalid.content.sub.type.message";
	
	//UDP
	public static final String UDP_CREATE_MESSAGE_FOR = "udp.create.message.for.";
	public static final String UDP_DELETE_MESSAGE_FOR = "udp.delete.message.for.";
	public static final String UDP_UPDATE_MESSAGE_FOR = "udp.update.message.for.";
	public static final String UDP_CONTENT_ADD_MESSAGE_FOR ="udp.content.add.message.for.";
	public static final String UDP_CONTENT_DELETE_MESSAGE_FOR ="udp.content.delete.message.for.";
	public static final String GET_ALL_UDP_MESSAGE_FOR ="get.all.udp.message.for.";
	public static final String GET_CONTENT_OF_UDP_MESSAGE_FOR ="get.content.of.udp.message.for.";
	
	
	//Group Member
	public static final String GROUP_MEMBER_REMOVE_MESSAGE_FOR = "group.member.remove.message.for.";
	public static final String GROUP_MEMBER_ADD_MESSAGE_FOR = "group.member.add.message.for.";
	public static final String GROUP_MEMBER_GET_MESSAGE_FOR = "group.member.get.message.for.";
	
	//Spring Bean Configuration
	public static final String BEAN_CONFIGURATION_ERROR_MESSAGE = "bean.not.configured.for.";
	
	//Subscription
	public static final String CREATE_SUBSCRIPTION_MESSAGE_FOR = "create.subscription.message.for.";
	public static final String UPDATE_SUBSCRIPTION_MESSAGE_FOR = "update.subscription.message.for.";
	public static final String GET_ALLOWED_SUBSCRIPTION_MESSAGE_FOR = "allowed.subscription.message.for.";
	
	public static final String CALLLOG_MESSAGE_FOR = "calllog.exception.message.";
	
	//NextChargeClass
	public static final String NEXT_CHARGE_CLASS_MESSAGE_FOR = "next.charge.class.message.for.";
	
	//UGC Wav File
	public static final String UGC_DOWNLOAD_SERVICE = "ugc.download.service.";
	public static final String UGC_DOWNLOAD_SERVICE_ERROR = "ugc.download.service.error.";
	
	//Combo Request
	public static final String COMBO_REQUEST_MESSAGE_FOR = "combo.request.message.for.";
	
	
	public static final String THIRDPARTY_SERVER_DOWN = "third.party.server.down";
	
	public static final String AWS_ERROR = "aws.error";
}
