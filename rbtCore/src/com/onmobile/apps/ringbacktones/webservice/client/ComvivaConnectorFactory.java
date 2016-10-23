package com.onmobile.apps.ringbacktones.webservice.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.OperatorUserDetails;
import com.onmobile.apps.ringbacktones.rbt2.common.BeanConstant;
import com.onmobile.apps.ringbacktones.rbt2.common.ConfigUtil;
import com.onmobile.apps.ringbacktones.rbt2.service.IUserDetailsService;
import com.onmobile.apps.ringbacktones.v2.dao.constants.OperatorUserTypes;
import com.onmobile.apps.ringbacktones.webservice.common.Configurations;

public class ComvivaConnectorFactory {

	private static Connector directConnector = null;
	private static ComVivaHttpConnector comVivaHttpConnector = null;
	private static Logger logger = Logger
			.getLogger(ComvivaConnectorFactory.class);

	public static Connector getCVConnectorInstance(String circleId,
			Connector connector) {
		return connector;
	}

	private synchronized static Connector getDirectConnector(
			Configurations configurations) {
		if (directConnector == null) {
			try {
				directConnector = new DirectConnector(configurations);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return directConnector;
	}

	public static ConnectorHandler getCVConnectorInstance(String subscriberId,
			String circleId, Connector connector,
			Configurations configurations, String api, String action) {
		logger.info("getCVConnectorInstance invoked");

		ConnectorHandler connectorHandler = new ConnectorHandler();
		if (null != subscriberId && !subscriberId.isEmpty()) {
			IUserDetailsService operatorUserDetailsService = null;
			try {
				operatorUserDetailsService = (IUserDetailsService) ConfigUtil.getBean(BeanConstant.USER_DETAIL_BEAN);
				if ((!RBTParametersUtils.getParamAsBoolean(iRBTConstant.COMMON, "DTOC_DEPLOYED", "FALSE")
						&& operatorUserDetailsService != null)) {
					OperatorUserDetails operatorDetails = (OperatorUserDetails) operatorUserDetailsService
							.getUserDetails(subscriberId);

					if ((operatorDetails != null)
							&& (operatorDetails.serviceKey()
									.equalsIgnoreCase(OperatorUserTypes.LEGACY.getDefaultValue())
									|| operatorDetails.serviceKey().equalsIgnoreCase(
											OperatorUserTypes.LEGACY_FREE_TRIAL.getDefaultValue()))) {
						connector = new HttpConnector(configurations);
						connectorHandler.setConnector(connector);
						Map<String, String> b2bUserInfo = new HashMap<String, String>();
						b2bUserInfo.put("B2B_OPERATORNAME", operatorDetails.operatorName());
						b2bUserInfo.put("B2B_CIRCLEID", operatorDetails.circleId());
						connectorHandler.setB2bUserInfo(b2bUserInfo);
						return connectorHandler;
					}
				}
			} catch (Exception e) {
				configurations.getLogger().error("RBT:: " + e.getMessage(), e);
			}
		}

		if (configurations.isUse_direct_connector_cv()) {
			Map<String, List<String>> apiMap = configurations
					.getComvivaApiMap();
			if (apiMap != null && apiMap.containsKey(api)) {
				if (apiMap.get(api).contains(action)) {
					if (directConnector == null) {
						connectorHandler.setConnector(connector);
						return connectorHandler;
					}
					logger.info("Returning DirectConnector instance");
					connectorHandler.setConnector(connector);
					return connectorHandler;
				}
			}
		}

		boolean isCVCircleId = isCVCircleID(circleId, configurations);
		logger.info(circleId + " is Comviva circle Id: " + isCVCircleId);
		if (isCVCircleId) {
			try {
				connector = getCVHttpConnectorInstance(configurations);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		logger.info("Returning ComvivaHttpConnector instance");
		connectorHandler.setConnector(connector);
		return connectorHandler;
	}

	private static boolean isCVCircleID(String circleId,
			Configurations configurations) {
		if (circleId != null && configurations.getCvCircleId() != null)
			return configurations.getCvCircleId().contains(
					circleId.toUpperCase());
		return false;

	}

	public static ComVivaHttpConnector getCVHttpConnectorInstance(
			Configurations configurations) {
		if (comVivaHttpConnector == null) {
			synchronized (ComVivaHttpConnector.class) {
				if (comVivaHttpConnector == null)
					try {
						comVivaHttpConnector = new ComVivaHttpConnector(
								configurations);
					} catch (Exception e) {
						e.printStackTrace();
					}
			}
		}
		return comVivaHttpConnector;
	}

}
