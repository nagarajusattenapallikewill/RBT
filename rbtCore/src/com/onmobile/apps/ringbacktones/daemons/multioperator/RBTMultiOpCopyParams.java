package com.onmobile.apps.ringbacktones.daemons.multioperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.onmobile.apps.ringbacktones.genericcache.CacheManagerUtil;
import com.onmobile.apps.ringbacktones.genericcache.ParametersCacheManager;
import com.onmobile.apps.ringbacktones.utils.MapUtils;

public class RBTMultiOpCopyParams {

	private static final String OPERATOR_ID_OPERATOR_RBT_NAME_MAP = "OPERATOR_ID_OPERATOR_RBT_NAME_MAP";
	private static Map<String, String> operatorIdOperatorRBTNameMap = new HashMap<String, String>();
	public static HashMap<String, String> operatorNameUrlMap = new HashMap<String, String>();

	private static final String OPERATOR_MAP_SITE_URL = "OPERATOR_MAP_SITE_URL";
	private static Map<String, String> operatorNameToUrlMap = null;

	private static final String RDC = "RDC";

	private RBTMultiOpCopyParams() {
	}

	static {

		String operatorIdOperatorRBTNameMapStr = CacheManagerUtil
				.getParametersCacheManager().getParameterValue(RDC,
						OPERATOR_ID_OPERATOR_RBT_NAME_MAP, null);
		if (null != operatorIdOperatorRBTNameMapStr) {
			operatorIdOperatorRBTNameMap = MapUtils.convertToMap(
					operatorIdOperatorRBTNameMapStr, ";", ",", null);
		}

		ParametersCacheManager parametersCacheManager = CacheManagerUtil
				.getParametersCacheManager();
		String operatorNameToUrlMapStr = parametersCacheManager
				.getParameterValue("DAEMON",
						OPERATOR_MAP_SITE_URL, null);
		operatorNameToUrlMap = MapUtils.convertToMap(operatorNameToUrlMapStr, ";",
				"=", null);

	}

	public static Map<String, String> getOperatorIdOperatorRBTNameMap() {
		return operatorIdOperatorRBTNameMap;
	}

	public static Map<String, String> getOperatorNameToUrlMap() {
		return operatorNameToUrlMap;
	}

}
