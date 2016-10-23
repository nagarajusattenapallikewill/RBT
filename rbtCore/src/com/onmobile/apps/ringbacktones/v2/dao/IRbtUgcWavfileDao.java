package com.onmobile.apps.ringbacktones.v2.dao;

import java.util.List;

import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile;
import com.onmobile.apps.ringbacktones.v2.dao.bean.RBTUgcWavfile.UgcFileUploadStatus;

public interface IRbtUgcWavfileDao {

	public RBTUgcWavfile saveUgcWavfile(RBTUgcWavfile rbtUgcWavfile) throws DataAccessException;
	public RBTUgcWavfile updateUgcWavfile(RBTUgcWavfile rbtUgcWavfile) throws DataAccessException;
	public boolean deleteUgcWavFile(RBTUgcWavfile rbtUgcWavfile) throws DataAccessException;
	public RBTUgcWavfile getUgcWavFile(long ugcId) throws DataAccessException;
	public List<RBTUgcWavfile> getUgcWavFiles(long subscriberId) throws DataAccessException;
	public RBTUgcWavfile getUgcWavFile(long subscriberId, String wavFile) throws DataAccessException;
	public boolean deleteUgcWavfiles(long subscriberId) throws DataAccessException;
	public boolean deleteUgcWavfiles(long subscriberId, String wavFile) throws DataAccessException;
	public List<RBTUgcWavfile> getUgcWavFilesToTransfer(UgcFileUploadStatus status) throws DataAccessException;
	
}
