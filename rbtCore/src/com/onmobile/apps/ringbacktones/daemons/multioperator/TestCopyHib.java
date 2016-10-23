package com.onmobile.apps.ringbacktones.daemons.multioperator;

import java.util.Date;


public class TestCopyHib {

	public static void main(String[] args) {

		RBTMultiOpCopyRequest rbtMultiOpCopyRequest = new RBTMultiOpCopyRequest();
		
//		rbtMultiOpCopyRequest.setCopyId(4L);
		rbtMultiOpCopyRequest.setKeyPressed("*9");
		rbtMultiOpCopyRequest.setRequestTime(new Date());
		rbtMultiOpCopyRequest.setStatus(4);
		
		
		RBTMultiOpCopyHibernateDao.getInstance().save(rbtMultiOpCopyRequest);

		System.out.println(" done.. ");
	}

}
