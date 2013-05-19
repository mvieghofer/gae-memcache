package com.clc3.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import javax.cache.Cache;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

import com.clc3.persistence.EMF;
import com.clc3.persistence.model.RequestData;
import com.clc3.persistence.model.SportData;

@ManagedBean
@SessionScoped
@SuppressWarnings("unchecked")
public class SportDataBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;
	private String method = "";
	private Collection<SportData> allResults;
	private int nrSimulatedRequests;
	private Long mainEventKey;
	private List<Long> keys;
	private Random rand;
	private int numberOfRequests = 500;
	private int numberDebugData = 500;
	
	private ChartSeries cachingCs;
	private ChartSeries runtimeNoCachingCs;
	private ChartSeries runtimeCachingCs;

	private double minRuntime = Double.MAX_VALUE;
	private double maxRuntime = 0;

	
	
	

	

	

	//ACTION METHODS
	public void extremeTest() {
		getMetadataBean().cleanupAndClearCache();
		getMetadataBean().setLastCalledMethod("extremeTest (" + getNumberOfRequests() + ")");
		Boolean tempBool = getMetadataBean().getCachingEnabled();

		setMinRuntime(Long.MAX_VALUE);
		setMaxRuntime(0);
		int iterations = 0;
		cachingCs = new ChartSeries();
		cachingCs.setLabel("Percent of elements cached");

		
		runtimeCachingCs = new ChartSeries();
		runtimeCachingCs.setLabel("With caching");
		getMetadataBean().setCachingEnabled(true);
		while (getMetadataBean().getPercentCached() < 95
				&& iterations < 100) {
			simulateRequestExtremeTest(iterations, cachingCs, runtimeCachingCs);
			iterations++;
		}

		
		runtimeNoCachingCs = new ChartSeries();
		runtimeNoCachingCs.setLabel("Without caching");
		getMetadataBean().setCachingEnabled(false);
		for (int i = 0; i < iterations; i++) {
			simulateRequestExtremeTest(i, null, runtimeNoCachingCs);
		}
		
		getMetadataBean().setCachingEnabled(tempBool);
		getMetadataBean().setTotalTimeElapsed(0);
	}
	public void normalTest() {
		getMetadataBean().cleanup();
		getMetadataBean().setLastCalledMethod("simulateRequest (" + getNumberOfRequests() + ")");
		
		simulateRequests ();
	}
	
	
	
	
	
	
	//PRIVATE DB METHODS
	private void simulateRequestExtremeTest(int iterations,
			ChartSeries cachingCs, ChartSeries runtimeCachingCs) {
		double totalTimeElapsed;
		simulateRequests();
		totalTimeElapsed = getMetadataBean().getTotalTimeElapsed();
		System.out.printf((getMetadataBean().getCachingEnabled() 
				? "caching: %.2f ms (cached: "+ getMetadataBean().getPercentCached() +")\n" 
				: "no caching: %.0f ms\n"), 
				totalTimeElapsed);
		if (cachingCs != null)
			cachingCs.set((iterations + 1) + "", getMetadataBean().getPercentCached());
		runtimeCachingCs.set((iterations + 1) + "", totalTimeElapsed);
		if (totalTimeElapsed < getMinRuntime())
			setMinRuntime(totalTimeElapsed);
		if (totalTimeElapsed > getMaxRuntime())
			setMaxRuntime(totalTimeElapsed);
	}
	private void simulateRequests() {
		if (allResults == null || allResults.size() == 0)
			loadFromDatabase();
		getResultKeys();
		getMetadataBean().startTiming();
		rand = new Random();
		mainEventKey = getRandomKey();
		Long key;
		findById(mainEventKey);
		for (int i = 0; i < numberOfRequests; i++) {
			nrSimulatedRequests = i + 1;
			if (rand.nextBoolean())
				key = mainEventKey;
			else {
				key = getRandomKey();
				while (key == mainEventKey)
					key = getRandomKey();
			}
			findById(key);
		}
		getMetadataBean().stopTiming();
	}
	private void loadFromDatabase() {
		allResults = findAll();
		if (allResults.size() != numberDebugData) {
			insertDebugData();
			allResults = findAll();
		}
	}
	private Collection<SportData> findAll() {
		EntityManager entityManager = EMF.get().createEntityManager();
		Collection<SportData> resultList;
		try {
			Query createQuery = entityManager
					.createQuery("SELECT sd FROM SportData sd");
			resultList = new ArrayList<SportData>(createQuery.getResultList());
		} finally {
			// entityManager.close();
		}
		return resultList;
	}
	private void insertDebugData() {
		Cache cache = getMetadataBean().getCache();
		Random rand = new Random();
		int currSize = findAll().size();
		if (currSize < numberDebugData) {
			EntityManager em = EMF.get().createEntityManager();
			for (int i = 0; i < numberDebugData - currSize; i++) {
				SportData sd = new SportData("team " + rand.nextInt(50),
						"team " + rand.nextInt(50), rand.nextInt(10) + ":"
								+ rand.nextInt(10));
				if (getMetadataBean().getCachingEnabled() && cache != null)
					cache.put(sd.getId(), sd);
				try {
					em.getTransaction().begin();
					em.merge(sd);
					em.flush();
					em.getTransaction().commit();
				} finally {
					// em.close();
				}
			}
		}
	}
	private SportData findById(Long key) {
		Cache cache = getMetadataBean().getCache();
		SportData p = null;
		EntityManager em = EMF.get().createEntityManager();
		Boolean isCached = false;
		try {
			p = (SportData) cache.get(key);
			if (getMetadataBean().getCachingEnabled() && p != null) {
				isCached = true;
			} else {
				p = em.find(SportData.class, key);

				//METADATA ACCUMULATION
				getMetadataBean().setTotalNrOfDSReads(getMetadataBean().getTotalNrOfDSReads() + 1);
				
				if (getMetadataBean().getCachingEnabled())
					cache.put(key, p);
			}
			RequestData requestData = new RequestData();
			requestData.setKey(key);
			requestData.setCached(isCached);
			getMetadataBean().getRequestData().add(requestData);
		} finally {
			// em.close();
		}
		return p;
	}

	
	
	
	
	//GETTERS UND SETTERS
	public int getNrSimulatedRequests() {
		return nrSimulatedRequests;
	}
	public void setNrSimulatedRequests(int simulatedRequests) {
		this.nrSimulatedRequests = simulatedRequests;
	}

	public int getNumberOfRequests() {
		return numberOfRequests;
	}
	public void setNumberOfRequests(int numberOfRequests) {
		this.numberOfRequests = numberOfRequests;
	}
	
	public int getNumberDebugData() {
		return findAll().size();
	}

	public CartesianChartModel getCachingModel() {
		CartesianChartModel cachingModel = new CartesianChartModel();
		cachingModel.addSeries(cachingCs);
		return cachingModel;
	}

	public CartesianChartModel getRuntimeModel() {
		CartesianChartModel runtimeModel = new CartesianChartModel();
		runtimeModel.addSeries(runtimeCachingCs);
		runtimeModel.addSeries(runtimeNoCachingCs);
		return runtimeModel;
	}

	public double getMaxRuntime() {
		if (maxRuntime % 100 != 0)
			maxRuntime += (100 - (maxRuntime % 100));
		return maxRuntime;
	}
	public void setMaxRuntime(double maxRuntime) {
		this.maxRuntime = maxRuntime;
	}

	public double getMinRuntime() {
		minRuntime -= minRuntime % 100;
		return minRuntime;
	}
	public void setMinRuntime(double minRuntime) {
		this.minRuntime = minRuntime;
	}

	public int getNrMainEventKey() {
		int nrMainEventKey = 0;
		for (RequestData rd : getMetadataBean().getRequestData()) {
			if (rd.getKey() == mainEventKey)
				nrMainEventKey++;
		}
		return nrMainEventKey;
	}
	
	private Long getRandomKey() {
		return keys.get(rand.nextInt(keys.size() - 1));
	}

	private void getResultKeys() {
		keys = new ArrayList<Long>();
		for (SportData sportData : allResults) {
			keys.add(sportData.getId());
		}
	}
	
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}

	public Collection<SportData> getAllResults() {
		loadFromDatabase();
		return allResults;
	}
	public ChartSeries getCachingCs() {
		return cachingCs;
	}
	public void setCachingCs(ChartSeries cachingCs) {
		this.cachingCs = cachingCs;
	}
	public ChartSeries getRuntimeNoCachingCs() {
		return runtimeNoCachingCs;
	}
	public void setRuntimeNoCachingCs(ChartSeries runtimeNoCachingCs) {
		this.runtimeNoCachingCs = runtimeNoCachingCs;
	}
	public ChartSeries getRuntimeCachingCs() {
		return runtimeCachingCs;
	}
	public void setRuntimeCachingCs(ChartSeries runtimeCachingCs) {
		this.runtimeCachingCs = runtimeCachingCs;
	}

}
