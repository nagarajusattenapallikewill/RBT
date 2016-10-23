package com.onmobile.apps.ringbacktones.daemons;

public class GNOCRaiseTicketThread extends Thread {

	@Override
	public void run() {
		GNOCAlarmAggregatorSM gnocRaiseTicket = new GNOCAlarmAggregatorSM();
		gnocRaiseTicket.writeRaiseTickets();
	}
   
	
}
