package com.onmobile.apps.ringbacktones.rbt2.db;

import java.util.List;

import com.onmobile.apps.ringbacktones.v2.dao.bean.WavFileMapping;

public interface IWavFileMappingDAO {

	public WavFileMapping getWavFileVerOne(String wavFileVerTwo, String operator);

	public boolean saveWavFileMapping(WavFileMapping wavFileMapping);

	public int saveOrUpdateWavFileMapping(List<WavFileMapping> wavFileMappings);

	public WavFileMapping getWavFileVerTwo(String wavFileVerOne, String operator);

	public List<WavFileMapping> getWavFileVerTwoByBatch(int minLimit,
			int MaxLimit);
}
