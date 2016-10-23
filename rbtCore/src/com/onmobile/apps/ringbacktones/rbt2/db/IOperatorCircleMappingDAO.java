package com.onmobile.apps.ringbacktones.rbt2.db;

import java.util.List;
import java.util.Set;

import com.onmobile.apps.ringbacktones.v2.dao.bean.OperatorCircleMapping;

public interface IOperatorCircleMappingDAO {
	
	public OperatorCircleMapping getOprtrCircleMappingByOperatorAndCircle(String operatorName, String circle);
	public boolean saveOperatorCircleMapping(OperatorCircleMapping operatorCircleMapping);
	public List<OperatorCircleMapping> getOperatorCircleMappingList(Set<Integer> ids);
	public List<OperatorCircleMapping> getOperatorCircleMapping();

}
