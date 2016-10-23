package com.onmobile.apps.ringbacktones.v2.dao;

import java.util.List;

import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPBean;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPContentMap;
import com.onmobile.apps.ringbacktones.v2.dao.bean.UDPResponseBeanDO;
import com.onmobile.apps.ringbacktones.v2.dao.impl.UDPDaoImpl.UDPType;

public interface IUDPDao {
	
	public UDPBean createUDP(UDPBean udpBean) throws DataAccessException;
	public UDPBean updateUDP(UDPBean udpBean) throws DataAccessException;
	public boolean deleteUDP(int udpId) throws DataAccessException;
	public List<UDPBean> getAllUDP(String msisdn,int pageNum,int pageSize) throws DataAccessException;
	public UDPBean getUDPById(int udpId) throws DataAccessException;
	public UDPResponseBeanDO getUDPById(int udpId,boolean isContentRequired) throws DataAccessException;
	public boolean addContentToUDP(UDPContentMap udpContentMap,String msisdn,boolean isUDPActive) throws DataAccessException;
	public boolean removeContentUDP(int udpId,int toneId) throws DataAccessException;
	public boolean removeContentUDP(String subscriberId, long toneId, UDPType type) throws DataAccessException;
	public boolean isValidUDPId(int udpId,String msisdn) throws DataAccessException;
	public boolean isUDPActive(int udpId) throws DataAccessException;

}
