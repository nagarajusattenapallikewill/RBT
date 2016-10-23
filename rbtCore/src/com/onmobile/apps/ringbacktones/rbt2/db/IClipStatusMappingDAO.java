package com.onmobile.apps.ringbacktones.rbt2.db;

import java.util.List;

import com.onmobile.apps.ringbacktones.v2.dao.bean.ClipStatusMapping;

public interface IClipStatusMappingDAO {
	
	public ClipStatusMapping getClipStatusMappingByOperatorId(int operatorId, int clipId);
	public boolean saveClipStatusMapping(ClipStatusMapping clipStatusMapping);
	public boolean updateClipStatusMapping(ClipStatusMapping clipStatusMapping);
	public List<ClipStatusMapping> getClipStatusMappingByStatus(int status);

}
