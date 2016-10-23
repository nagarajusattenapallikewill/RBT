package com.onmobile.apps.ringbacktones.callLog.utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import voldemort.client.ClientConfig;
import voldemort.client.SocketStoreClientFactory;
import voldemort.client.StoreClient;
import voldemort.client.StoreClientFactory;
import voldemort.cluster.failuredetector.ThresholdFailureDetector;

import com.onmobile.apps.ringbacktones.callLog.CallLogConstants;
import com.onmobile.apps.ringbacktones.common.RBTParametersUtils;

public class StoreClientFactoryInstance {

	private static StoreClientFactory storeClientFactory = null;
	private static String bootStrapUrl = null;
	private static ClientConfig clientConfig = null;
	private static int maxThreads = 0;
	private static StoreClient<String, List<Map<String, Object>>> storeClient = null;

	static {
		bootStrapUrl = RBTParametersUtils.getParamAsString(
				CallLogConstants.PARAM_TYPE, CallLogConstants.BOOTSTRAP_URL,
				null);
		maxThreads = RBTParametersUtils.getParamAsInt(
				CallLogConstants.PARAM_TYPE, CallLogConstants.MAX_THREAD, 300);
	}

	private static StoreClientFactory createStoreClientFactoryInstance() {
		storeClientFactory = new SocketStoreClientFactory(clientConfig);
		return storeClientFactory;
	}

	private static StoreClientFactory getStoreClientFactoryInstance() {
		clientConfig = initializeClientConfig();
		if (clientConfig != null)
			return createStoreClientFactoryInstance();
		return null;
	}

	private static ClientConfig initializeClientConfig() {
		if (CallLogUtils.isValidString(bootStrapUrl)) {
			clientConfig = new ClientConfig();
			clientConfig.setMaxConnectionsPerNode(maxThreads);
			/* Changes are done for handling the voldemort issues.
			 * clientConfig.setMaxThreads(maxThreads);
			 * clientConfig.setMaxConnectionsPerNode(maxThreads);
			 * clientConfig.setConnectionTimeout(300000, TimeUnit.MILLISECONDS);
			 * clientConfig.setSocketTimeout(300000, TimeUnit.MILLISECONDS);
			 * clientConfig.setRoutingTimeout(300000, TimeUnit.MILLISECONDS);
			 */
			clientConfig.setBootstrapUrls(bootStrapUrl);
			clientConfig.setMaxQueuedRequests(50000);
			clientConfig.setFailureDetectorBannagePeriod(5000);
			clientConfig.setSelectors(500);
			clientConfig
					.setFailureDetectorImplementation(ThresholdFailureDetector.class
							.getName());
		}
		return clientConfig;
	}

	/**
	 * This method used to create the singleton StoreClient which will be used
	 * for DB operation in voldemort. No need to create a different StoreClient
	 * instance for same store.
	 * 
	 * @return
	 */
	public static StoreClient<String, List<Map<String, Object>>> getStoreClientInstance() {
		if (storeClient == null) {
			synchronized (StoreClientFactoryInstance.class) {
				if (storeClient == null) {
					storeClientFactory = getStoreClientFactoryInstance();
					storeClient = storeClientFactory
							.getStoreClient(CallLogConstants.STORE_NAME);
				}
			}
		}

		return storeClient;
	}

}
