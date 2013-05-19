package com.clc3.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.cache.Cache;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.persistence.EntityManager;

import org.primefaces.model.chart.OhlcChartModel;
import org.primefaces.model.chart.OhlcChartSeries;

import com.clc3.persistence.EMF;
import com.clc3.persistence.model.Quote;
import com.clc3.persistence.model.Stock;

@ManagedBean
@SessionScoped
public class QuoteStatisticBean extends BaseBean implements Serializable {

	private static final long serialVersionUID = 1L;

	private Quote tempQuote;
	private List<Quote> quoteList;

	private List<Stock> stocks;
	private String selectedStockSymbol;

	private Integer selectedYear;
	private Integer selectedYearForAll;

	public QuoteStatisticBean() {
		EMF.get().createEntityManager();
		selectedStockSymbol = "MSFT";
		selectedYear = 2008;
		selectedYearForAll = 2008;
		stocks = new ArrayList<Stock>();
	}

	// ACTION METHODS
	public void refreshStockTable() {
		System.out.println("Refreshing stock table..");
		stocks = loadAllStocks();
	}

	// PRIVATE METHODS
	@SuppressWarnings({ "unchecked", "deprecation" })
	private List<Quote> loadQuotesForStockAndYear(String stockSymbol, int year) {
		System.out.println("LoadQuotesForStocks called for Stock "
				+ stockSymbol + "..");
		EntityManager em = EMF.get().createEntityManager();

		List<Quote> result = new ArrayList<Quote>();
		try {
			Stock s = (Stock) em
					.createQuery(
							"SELECT s FROM Stock s WHERE s.symbol = :symbol")
					.setParameter("symbol", stockSymbol).getSingleResult();
			result = new ArrayList<Quote>(em
					.createQuery(
							"SELECT q FROM Quote q "
									+ "WHERE q.stockId = :stockId "
									+ "AND q.time >= :from "
									+ "AND q.time <= :to "
									+ "ORDER BY q.time ASC")
					.setParameter("stockId", s.getId())
					.setParameter("from", new Date(year - 1900, 0, 1))
					.setParameter("to", new Date(year - 1900, 11, 31))
					.getResultList());
			System.out.println("quoteList.size() = " + result.size());

			// METADATA ACCUMULATION
			getMetadataBean().setTotalNrOfDSReads(
					getMetadataBean().getTotalNrOfDSReads() + 1
							+ (result == null ? 1 : result.size()));

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			em.close();
		}
		return result;
	}

	@SuppressWarnings({ "deprecation", "unchecked" })
	private OhlcChartModel createOhlcChartModel(String stockSymbol, int year) {

		String key = "ohlc" + stockSymbol + year;
		System.out.println("Entering chart init method..");

		Cache cache = getMetadataBean().getCache();
		OhlcChartModel model = (OhlcChartModel) cache.get(key);
		if (getMetadataBean().getCachingEnabled() && model != null) {
			System.out.println("Using cache...");
			return model;
		} else {
			System.out.println("No cache entry...");
			OhlcChartModel ohlcModel = new OhlcChartModel();
			List<Quote> quotes = loadQuotesForStockAndYear(stockSymbol, year);

			if (quotes == null || quotes.size() == 0) {
				ohlcModel.add(new OhlcChartSeries(1, 1, 0, 3, 2));
				ohlcModel.add(new OhlcChartSeries(2, 1, 0, 3, 2));
				return ohlcModel;
			}

			List<OhlcChartSeries> ohlcData = new ArrayList<OhlcChartSeries>();

			int[] count = new int[12];
			double[] high = new double[12];
			double[] low = new double[12];
			double[] open = new double[12];
			double[] close = new double[12];

			for (int i = 0; i < 12; i++) {
				low[i] = Double.MAX_VALUE;
			}

			for (int i = 0; i < 12; i++) {
				high[i] = Double.MAX_VALUE * -1;
			}

			int m = -1;

			double lastQuotePrice = 0;

			for (Quote q : quotes) {
				if (m != q.getTime().getMonth()) {
					if (m != -1)
						close[m] = lastQuotePrice;

					m = q.getTime().getMonth();

					open[m] = q.getPrice();
				}

				count[m]++;

				if (high[m] < q.getPrice())
					high[m] = q.getPrice();

				if (low[m] > q.getPrice())
					low[m] = q.getPrice();

				lastQuotePrice = q.getPrice();
			}

			close[11] = quotes.get(quotes.size() - 1).getPrice();

			for (int i = 0; i < 12; i++) {
				ohlcData.add(new OhlcChartSeries(i + 1, open[i], high[i],
						low[i], close[i]));
			}

			ohlcModel.setData(ohlcData);

			if (getMetadataBean().getCachingEnabled())
				cache.put(key, ohlcModel);

			return ohlcModel;
		}
	}

	@SuppressWarnings("unchecked")
	private OhlcChartModel createAllStocksOhlcChartModel(int year) {

		String key = "ohlcAll" + year;
		System.out.println("Entering chartAll init method..");

		Cache cache = getMetadataBean().getCache();
		OhlcChartModel model = (OhlcChartModel) cache.get(key);
		if (getMetadataBean().getCachingEnabled() && model != null) {
			System.out.println("Using cache...");
			return model;
		} else {
			System.out.println("No cache entry...");
			OhlcChartModel ohlcModel = new OhlcChartModel();

			List<Stock> allStocks = loadAllStocks();

			if (allStocks == null || allStocks.size() == 0) {
				ohlcModel.add(new OhlcChartSeries(1, 1, 0, 3, 2));
				ohlcModel.add(new OhlcChartSeries(2, 1, 0, 3, 2));
				return ohlcModel;
			}

			int nr = 0;
			for (Stock s : allStocks) {
				List<Quote> quotes = loadQuotesForStockAndYear(s.getSymbol(),
						year);
				double high = Double.MAX_VALUE * -1;
				double low = Double.MAX_VALUE;
				double open = 0;
				double close = 0;
				nr++;

				if (quotes == null || quotes.isEmpty()) {
					ohlcModel.add(new OhlcChartSeries(nr, 1, 0, 3, 2));
				} else {
					open = quotes.get(0).getPrice();
					close = quotes.get(quotes.size() - 1).getPrice();

					for (Quote q : quotes) {
						if (q.getPrice() > high)
							high = q.getPrice();
						if (q.getPrice() < low)
							low = q.getPrice();
					}

					ohlcModel.add(new OhlcChartSeries(nr, open, high, low,
							close));
				}
			}

			if (getMetadataBean().getCachingEnabled())
				cache.put(key, ohlcModel);

			return ohlcModel;
		}
	}

	// GETTER AND SETTER
	public Quote getTempQuote() {
		return tempQuote;
	}

	public void setTempQuote(Quote tempQuote) {
		this.tempQuote = tempQuote;
	}

	public List<Stock> getStocks() {
		if (stocks == null || stocks.isEmpty())
			refreshStockTable();
		return stocks;
	}

	public void setStocks(List<Stock> stocks) {
		this.stocks = stocks;
	}

	public List<Quote> getQuoteList() {
		return quoteList;
	}

	public void setQuoteList(List<Quote> quoteList) {
		this.quoteList = quoteList;
	}

	public OhlcChartModel getOhlcModel() {
		getMetadataBean().setLastCalledMethod(
				"Get Ohlc Model for " + getSelectedStockSymbol() + " - "
						+ getSelectedYear());
		getMetadataBean().startTiming();
		OhlcChartModel model = createOhlcChartModel(selectedStockSymbol,
				selectedYear);
		getMetadataBean().stopTiming();
		return model;
	}

	public OhlcChartModel getAllStocksOhlcModel() {
		getMetadataBean().setLastCalledMethod(
				"Get Ohlc Model for all stocks - " + getSelectedYearForAll());
		getMetadataBean().startTiming();
		OhlcChartModel model = createAllStocksOhlcChartModel(selectedYearForAll);
		getMetadataBean().stopTiming();
		return model;
	}

	public String getSelectedStockSymbol() {
		if (selectedStockSymbol == null || selectedStockSymbol == "")
			return "MSFT";
		return selectedStockSymbol;
	}

	public void setSelectedStockSymbol(String selectedStockSymbol) {
		this.selectedStockSymbol = selectedStockSymbol;
	}

	public List<Integer> getPossibleYears() {
		List<Integer> years = new ArrayList<Integer>();
		years.add(2008);
		years.add(2009);
		years.add(2010);
		years.add(2011);
		years.add(2012);

		// System.out.println("Nr. of years: " + years.size());

		return years;
	}

	public List<String> getStocksForLegend() {
		List<String> result = new ArrayList<String>();
		int count = 1;
		for (Stock s : getStocks()) {
			result.add(count + " = (" + s.getSymbol() + ")");
			count++;
		}
		return result;
	}

	public Integer getSelectedYear() {
		return selectedYear;
	}

	public void setSelectedYear(Integer selectedYear) {
		this.selectedYear = selectedYear;
	}

	public Integer getSelectedYearForAll() {
		return selectedYearForAll;
	}

	public void setSelectedYearForAll(Integer selectedYearForAll) {
		this.selectedYearForAll = selectedYearForAll;
	}

}
