package com.clc3.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import com.clc3.persistence.model.RequestData;

@ManagedBean
@ApplicationScoped
public class MetadataBean implements Serializable {

	private static final long serialVersionUID = 1L;
	protected Cache cache;
	private long totalTimeElapsed;
	private List<RequestData> requestData = new ArrayList<RequestData>();
	private List<RequestData> hftDataCaching = new ArrayList<RequestData>();
	private List<RequestData> hftDataNoCaching = new ArrayList<RequestData>();
	private Boolean cachingEnabled;
	private String lastCalledMethod;
	private long totalNrOfDSReads;
	private long totalNrOfDSWrites;

	private long start;
	private long stop;
	private long elapsed;

	//private static MetadataBean instance = new MetadataBean();

	public MetadataBean() {
		cleanup();
		cachingEnabled = false;
	}

	//public static MetadataBean getInstance() {
	//	return instance;
	//}

	public void cleanup() {
		totalTimeElapsed = 0;
		requestData = new ArrayList<RequestData>();
		hftDataCaching = new ArrayList<RequestData>();
		hftDataNoCaching = new ArrayList<RequestData>();
		lastCalledMethod = "NoMethodCalled()";
		totalNrOfDSReads = 0;
		totalNrOfDSWrites = 0;
	}

	public void cleanupAndClearCache() {
		cleanup();
		clearCache();
	}

	public float getPercentCached() {
		int nrCached = 0;
		for (RequestData rd : requestData) {
			if (rd.isCached())
				nrCached++;
		}
		float f = 0;
		if (requestData.size() > 0)
			f = 100.0f / requestData.size();
		return f * nrCached;
	}

	public void startTiming() {
		start = System.currentTimeMillis();
	}

	public void stopTiming() {
		stop = System.currentTimeMillis();
		elapsed = stop - start;
		setTotalTimeElapsed(elapsed);
	}

	public long getTotalTimeElapsed() {
		return totalTimeElapsed;
	}

	public void setTotalTimeElapsed(long totalTimeElapsed) {
		this.totalTimeElapsed = totalTimeElapsed;
	}

	public List<RequestData> getRequestData() {
		return requestData;
	}

	public void setRequestData(List<RequestData> requestData) {
		this.requestData = requestData;
	}

	public int getRequestDataSize() {
		return requestData.size();
	}

	public Boolean getCachingEnabled() {
		return cachingEnabled;
	}

	public void setCachingEnabled(Boolean cachingEnabled) {
		this.cachingEnabled = cachingEnabled;
	}

	protected Cache getCache() {
		try {
			cache = CacheManager.getInstance().getCacheFactory()
					.createCache(Collections.emptyMap());
			return cache;
		} catch (CacheException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void clearCache() {
		if (cache == null)
			cache = getCache();
		cache.clear();

	}

	public Integer getCacheAccuracy() {
		return 0;// getCache().getCacheStatistics().getStatisticsAccuracy();
	}

	public Integer getNrOfCachedItems() {
		return getCache().getCacheStatistics().getObjectCount();
	}

	public Integer getCacheHits() {
		return getCache().getCacheStatistics().getCacheHits();
	}

	public Integer getCacheMisses() {
		return getCache().getCacheStatistics().getCacheMisses();
	}

	public String getLastCalledMethod() {
		return lastCalledMethod;
	}

	public void setLastCalledMethod(String lastCalledMethod) {
		this.lastCalledMethod = lastCalledMethod;
	}

	public List<RequestData> getHftDataCaching() {
		return hftDataCaching;
	}

	public void setHftDataCaching(List<RequestData> hftDataCaching) {
		this.hftDataCaching = hftDataCaching;
	}

	public int getHftDataCachingSize() {
		return hftDataCaching.size();
	}

	public List<RequestData> getHftDataNoCaching() {
		return hftDataNoCaching;
	}

	public void setHftDataNoCaching(List<RequestData> hftDataNoCaching) {
		this.hftDataNoCaching = hftDataNoCaching;
	}

	public int getHftDataNoCachingSize() {
		return hftDataNoCaching.size();
	}

	public long getTotalNrOfDSReads() {
		return totalNrOfDSReads;
	}

	public void setTotalNrOfDSReads(long totalNrOfDSReads) {
		this.totalNrOfDSReads = totalNrOfDSReads;
	}

	public long getTotalNrOfDSWrites() {
		return totalNrOfDSWrites;
	}

	public void setTotalNrOfDSWrites(long totalNrOfDSWrites) {
		this.totalNrOfDSWrites = totalNrOfDSWrites;
	}
}