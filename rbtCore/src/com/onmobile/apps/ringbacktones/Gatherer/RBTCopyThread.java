package com.onmobile.apps.ringbacktones.Gatherer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import com.onmobile.apps.ringbacktones.Gatherer.threadMonitor.ThreadInfo;
import com.onmobile.apps.ringbacktones.common.iRBTConstant;
import com.onmobile.apps.ringbacktones.content.ViralSMSTable;
import com.onmobile.apps.ringbacktones.webservice.client.beans.Subscriber;

public class RBTCopyThread extends Thread implements ThreadInfo, iRBTConstant {
	private static Logger logger = Logger.getLogger(RBTCopyThread.class);

	RBTGatherer m_parentThread = null;
	Vector<ViralSMSTable> m_pendingCopy;
	static int nativeCount = 0;
	static int exoticCount = 0;
	boolean isWaiting = false;
	String threadName = null;
	//RBT-14671 - # like
	private RBTCopyLikeUtils m_rbtCopyLikeUtils = new RBTCopyLikeUtils();
	
	public RBTCopyThread(RBTGatherer rbtGatherer,
			Vector<ViralSMSTable> pendingList) {
		m_parentThread = rbtGatherer;
		m_pendingCopy = pendingList;
	}

	public void run() {
		while (m_parentThread != null && m_parentThread.isAlive()) {
			// Tools.logDetail(_class, method, "entered while");
			try {
				ViralSMSTable vst = null;
				boolean recordFound = false;

				synchronized (m_pendingCopy) {
					if (m_pendingCopy.size() > 0) {
						for (int i = 0; i < m_pendingCopy.size(); i++) {
							ViralSMSTable viral = m_pendingCopy.get(i);
							if (viral.isTaken())
								continue;
							vst = viral;
							vst.setTaken(true);
							recordFound = true;
							nativeCount++;
							break;
						}
						if (recordFound)
							m_parentThread.rbtCopyProcessor
									.writeCopyStats("Worker Thread "
											+ this.getThreadName()
											+ ". Found native record "
											+ vst.toString());
						else
							m_parentThread.rbtCopyProcessor
									.writeCopyStats("Worker Thread "
											+ this.getThreadName()
											+ ". No native record found.");
					} else
						m_parentThread.rbtCopyProcessor
								.writeCopyStats("Worker Thread "
										+ this.getThreadName()
										+ ". Base queue size 0");
				}

				Vector<ViralSMSTable> pendingList = null;

				if (!recordFound) {
					Set<String> keySet = RBTCopyProcessor.m_ViralSMSRecordsListMap
							.keySet();
					boolean foundExotic = false;
					for (String key : keySet) {
						pendingList = RBTCopyProcessor.m_ViralSMSRecordsListMap
								.get(key);

						synchronized (pendingList) {
							if (pendingList.size() > 0) {
								for (int i = 0; i < pendingList.size(); i++) {
									ViralSMSTable viral = pendingList.get(i);
									if (viral.isTaken())
										continue;
									vst = viral;
									vst.setTaken(true);
									foundExotic = true;
									exoticCount++;
									break;
								}

							}
						}
						if (foundExotic) {
							m_parentThread.rbtCopyProcessor
									.writeCopyStats("Worker Thread "
											+ this.getThreadName()
											+ ". Found exotic record "
											+ vst.toString());
							break;
						} else
							m_parentThread.rbtCopyProcessor
									.writeCopyStats("Worker Thread "
											+ this.getThreadName()
											+ ". No exotic record found.");

					}

					if (vst == null) {
						synchronized (m_pendingCopy) {
							m_parentThread.rbtCopyProcessor
									.writeCopyStats("Worker Thread "
											+ this.getThreadName()
											+ ". No record at all found. Going to wait state.");
							logger.info("Worker Thread "
									+ this.getThreadName()
									+ ". No record at all found. Going to wait state.");
							isWaiting = true;
							m_pendingCopy.wait();
							isWaiting = false;
						}

					}
				}
				// Added COPYSTAR
				if (vst != null) {
					try {
						MDC.put(mdc_msisdn, vst.callerID());
						m_parentThread.rbtCopyProcessor
								.writeCopyStats("Worker Thread "
										+ this.getThreadName()
										+ ". Working om " + vst.toString());

						Subscriber subscriber = vst.getSubscriber();
						if (subscriber == null)//RBT-14671 - # like
							subscriber = m_rbtCopyLikeUtils
									.getSubscriber(vst.callerID());
						if (vst.type().equals("RRBT_COPY")) {
							m_parentThread.rbtCopyProcessor.processRRBTCopy(
									vst, subscriber);

						} else if ((((m_parentThread.getParamAsBoolean(
								"PRESS_STAR_DOUBLE_CONFIRMATION", "FALSE") || (m_parentThread.getParamAsBoolean(
										"PRESS_STAR_DOUBLE_CONFIRMATION_INACTIVE_USER", "FALSE") && !RBTCopyProcessor.isSubActive(subscriber))) || m_parentThread
								.getParamAsBoolean("IS_OPT_IN", "FALSE")) && (vst
								.type().equals("COPY")))
								|| (m_parentThread.getParamAsBoolean(
										"IS_STAR_OPT_IN_ALLOWED", "FALSE") && vst
										.type().equals("COPYSTAR"))) {
							if ((vst.selectedBy() == null
									|| vst.selectedBy().indexOf("RETRY") != -1 || vst
									.selectedBy().indexOf("XCOPY") != -1)
									&& subscriber.isValidPrefix()) {
								if (m_parentThread.getParamAsBoolean(
										"IS_LOCAL_COPY_TEST_ON", "FALSE")
										&& !Arrays
												.asList(m_parentThread
														.getParamAsString(
																"GATHERER",
																"LOCAL_COPY_TEST_NUMBERS",
																"").split(","))
												.contains(vst.callerID()))
									m_parentThread.rbtCopyProcessor
											.copyTestFailed(
													vst,
													m_parentThread.rbtCopyProcessor.m_localType);
								else
									m_parentThread.rbtCopyProcessor
											.processLocalCopyRequest(vst,
													false, subscriber);
							} else
								m_parentThread.rbtCopyProcessor.processCopy(
										vst, subscriber);
						} else
							m_parentThread.rbtCopyProcessor.processCopy(vst,
									subscriber);
					} catch (Exception e) {
						logger.error("", e);
						m_parentThread.writeCopyCaller(vst.toString());
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						String stacktrace = sw.toString();
						sw.append(vst.toString());
						m_parentThread.writeCopyException(sw.toString());
						sw = null;
						stacktrace = null;
						m_parentThread.rbtCopyProcessor
								.writeCopyStats("Worker Thread "
										+ this.getThreadName()
										+ ". Updating to reconcile "
										+ vst.callerID());
						m_parentThread.rbtCopyProcessor.updateViralPromotion(
								vst.subID(), vst.callerID(), vst.sentTime(),
								vst.type(), vst.type() + "RECON", null);
					} catch (OutOfMemoryError oome) {
						logger.error("", oome);
					} catch (Throwable t) {
						logger.error("", t);
						m_parentThread.writeCopyCaller(vst.toString());
						m_parentThread.rbtCopyProcessor
								.writeCopyStats("Worker Thread "
										+ this.getThreadName()
										+ ". Updating to reconcile "
										+ vst.callerID());
						m_parentThread.rbtCopyProcessor.updateViralPromotion(
								vst.subID(), vst.callerID(), vst.sentTime(),
								vst.type(), vst.type() + "RECON", null);
					} finally {
						MDC.remove(mdc_msisdn);
					}
					boolean removedList = false;
					String removedFromMap = null;
					if (recordFound) {
						synchronized (m_pendingCopy) {
							removedList = m_pendingCopy.remove(vst);
						}
					} else {
						synchronized (pendingList) {
							removedList = pendingList.remove(vst);
						}
					}
					m_parentThread.rbtCopyProcessor
							.writeCopyStats("Worker Thread "
									+ this.getThreadName()
									+ ". Removed from list : " + removedList
									+ ", Removed from map :" + removedFromMap);
					m_parentThread.rbtCopyProcessor
							.writeCopyStats("Worker Thread "
									+ this.getThreadName()
									+ ". Finished working on " + vst.toString());
				}
			}
			/*
			 * catch(InterruptedException ie) { ie.printStackTrace();
			 * logger.error("", ie);
			 * 
			 * }
			 */
			catch (Exception e) {
				e.printStackTrace();
				logger.error("", e);

			} catch (Throwable t) {
				t.printStackTrace();
				logger.error("", t);
			}
		}
	}

	public boolean amIAlive() {
		return this.isAlive();
	}

	public String getActivity() {
		String activityString = nativeCount
				+ " native copyrequests processed , " + exoticCount
				+ " copyRequests from other queue processed.";
		return activityString;
	}

	public String getLoad() {
		int pendingCopyRequests = m_pendingCopy.size();
		String loadString = pendingCopyRequests + " are pending copy requests.";
		return loadString;
	}

	public String getStatus() {
		String statusString = "Is Waiting : " + isWaiting;
		return statusString;
	}

	public String getThreadName() {
		return threadName;
	}

	public void setThreadName(String name) {
		threadName = name;
	}

	public String getThreadDetail() {
		StringBuffer sbf = new StringBuffer();
		sbf.append(getThreadName());
		sbf.append(": ");
		sbf.append(getActivity());
		sbf.append(", ");
		sbf.append(getLoad());
		sbf.append(", ");
		sbf.append(getStatus());
		return sbf.toString();
	}
}
