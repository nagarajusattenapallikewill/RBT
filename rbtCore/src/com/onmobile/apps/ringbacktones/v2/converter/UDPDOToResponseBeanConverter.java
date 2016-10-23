package com.onmobile.apps.ringbacktones.v2.converter;

import java.util.List;

import com.onmobile.apps.ringbacktones.rbt2.service.util.ServiceUtil;
import com.onmobile.apps.ringbacktones.rbtcontents.beans.Clip;
import com.onmobile.apps.ringbacktones.v2.bean.UDPResponseBean;
import com.onmobile.apps.ringbacktones.v2.dao.DataAccessException;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPBean;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPContentMap;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPResponseBeanDO;

public class UDPDOToResponseBeanConverter {
	
	public static UDPResponseBean getUDPResponseBeanFromUDPBean(UDPBean udpBean){
		UDPResponseBean responseBean = null;
		if(udpBean != null){
		responseBean= new UDPResponseBean();
		responseBean.setId(udpBean.getUdpId());
		responseBean.setName(udpBean.getUdpName());
		responseBean.setSubscriberId(udpBean.getSubscriberId());
		responseBean.setCreationTime(udpBean.getCreationTime());
		responseBean.setUpdationTime(udpBean.getUpdationTime());
		responseBean.setExtraInfo(udpBean.getExtraInfo());
		responseBean.setMode(udpBean.getMode());
		responseBean.setSelActivated(udpBean.isSelActivated());
		}
		return responseBean;
	}
	
	
	public static UDPResponseBean getUDPResponseBeanFromUDPResponseBeanDO(UDPResponseBeanDO beanDO) throws DataAccessException{
		UDPResponseBean responseBean = null;
		if(beanDO != null){
			responseBean = getUDPResponseBeanFromUDPBean(beanDO.getUdpBean());
			List<Clip> clipList = null;
			List<UDPContentMap> udpContentMaps = beanDO.getUdpContentMaps();
			if(udpContentMaps != null && udpContentMaps.size() > 0){
				clipList = ServiceUtil.getClipsFromUDPMap(udpContentMaps);
			}
			responseBean.setClips(clipList);
		}
		return responseBean;
	}
	
	public static UDPBean getUDPBeanFromUDPResponseBean(UDPResponseBean udpResponseBean) {
		UDPBean udpBean = null;
		if (udpResponseBean != null) {
			udpBean = new UDPBean();
			udpBean.setUdpId(udpResponseBean.getId());
			udpBean.setExtraInfo(udpResponseBean.getExtraInfo());
			udpBean.setUdpName(udpResponseBean.getName());
			udpBean.setSelActivated(udpResponseBean.isSelActivated());
			udpBean.setSubscriberId(udpResponseBean.getSubscriberId());
			udpBean.setCreationTime(udpResponseBean.getCreationTime());
		}
		return udpBean;
	}

}
