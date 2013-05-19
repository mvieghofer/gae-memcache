package com.clc3.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

import org.primefaces.model.chart.CartesianChartModel;
import org.primefaces.model.chart.ChartSeries;

import com.clc3.persistence.model.RequestData;
import com.clc3.util.QuoteUtil;
import com.clc3.worker.HFTConfig;

@ManagedBean
@SessionScoped
public class HftBean extends BaseBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private CartesianChartModel noCacheModel = new CartesianChartModel();
	private CartesianChartModel cacheModel = new CartesianChartModel();
	private Boolean chartPolling = false;
	private boolean showAppInfo;
	

	//ACTION
	public void startTrading() {
		chartPolling = true;
		HFTConfig.getInstance().setAddQuoteEnabled(true);
		getMetadataBean().cleanup();
		QuoteUtil.fillQueueWithAddQuoteRequests();
		QuoteUtil.fillQueueWithAddQuoteRequestsHFT();
	}
	public void stopTrading() {
		chartPolling = false;
		QuoteUtil.sendStopAddQuoteRequest();
	}
	
	public void showAppInfo() {
		setShowAppInfo(true);
	}
	
	//PRIVATE METHODS
	private ChartSeries createCacheModel(List<RequestData> requestData) {
		System.out.println("Creating cache model for <" + (getMetadataBean().getCachingEnabled() ? "CACHING" : "NOCACHING") + ">");
		
		ChartSeries cs = new ChartSeries();
		cs.setLabel("Quotes");
		Integer i = 1;
		for (RequestData rd : new ArrayList<RequestData> (requestData)) {
			if (rd.isCached()) {
				cs.set(i, 0);
			} else {
				cs.set(i, 1);
			}
			i++;
		}
		return cs;
	}
	
	
	
	//GETTER UND SETTER
	public CartesianChartModel getCacheModel() {
		ChartSeries cs = createCacheModel(getMetadataBean().getHftDataCaching());
		
		cacheModel = new CartesianChartModel();
		cacheModel.addSeries(cs);
		
		System.out.println("ChartSeries size: " + cs.getData().size());
		
		return cacheModel;
	}
	public CartesianChartModel getNoCacheModel() {
		ChartSeries cs = createCacheModel(getMetadataBean().getHftDataNoCaching()); 
		
		noCacheModel = new CartesianChartModel();
		noCacheModel.addSeries(cs);
		
		System.out.println("ChartSeries size: " + cs.getData().size());
		
		return noCacheModel;
	}
	
	
	public Boolean getCacheModelRendered () {
		CartesianChartModel model = getCacheModel();
		return !(model == null) && !(model.getSeries().isEmpty());
	}
	public Boolean getNoCacheModelRendered () {
		CartesianChartModel model = getNoCacheModel();
		return !(model == null) && !(model.getSeries().isEmpty());
	}
	public Boolean getChartPolling() {
		return chartPolling;
	}
	public void setChartPolling(Boolean chartPolling) {
		this.chartPolling = chartPolling;
	}
	public boolean getShowAppInfo() {
		return showAppInfo;
	}
	public void setShowAppInfo(boolean showAppInfo) {
		this.showAppInfo = showAppInfo;
	}
	
	
}
