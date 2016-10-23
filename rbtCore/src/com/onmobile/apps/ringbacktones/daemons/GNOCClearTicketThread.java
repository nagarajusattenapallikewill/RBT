package com.onmobile.apps.ringbacktones.daemons;

public class GNOCClearTicketThread extends Thread {

	@Override
	public void run() {
		GNOCAlarmAggregatorSM gnocClearTicket = new GNOCAlarmAggregatorSM();
		gnocClearTicket.writeClearTickets();
	}

}
