package com.clc3.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.faces.bean.ManagedProperty;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import com.clc3.persistence.EMF;
import com.clc3.persistence.model.Quote;
import com.clc3.persistence.model.Stock;
import com.clc3.persistence.model.api.DatabaseEntity;

public class BaseBean {	
	@ManagedProperty(value="#{metadataBean}")
    private MetadataBean metadataBean;
	private Boolean hasEntries = false;

	public MetadataBean getMetadataBean() {
	//if (metadataBean == null)
	//		metadataBean = MetadataBean.getInstance();
		return metadataBean;
	//	return MetadataBean.getInstance();
	}
	public void setMetadataBean(MetadataBean metadataBean) {
		this.metadataBean = metadataBean;
	}

	public DatabaseEntity addToDb(DatabaseEntity entity) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			em.getTransaction().begin();
			em.merge(entity);
			em.flush();
			em.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
			throw new RuntimeException(e.getMessage());
		} finally {
			em.close();
		}
		return entity;
	}

	public void addToDbIfNotExists(DatabaseEntity entity) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			if (em.find(entity.getClass(), entity.getPrimaryKey()) == null)
				addToDb(entity);
		} finally {
			em.close();
		}
	}

	protected Stock addStockToDbIfNotExists(Stock stock) {
		EntityManager em = EMF.get().createEntityManager();
		try {
			Query query = em
					.createQuery("SELECT s FROM Stock s WHERE s.symbol = :symbol AND s.name = :name AND s.currency = :currency");
			query.setParameter("symbol", stock.getSymbol());
			query.setParameter("name", stock.getName());
			query.setParameter("currency", stock.getCurrency());
			stock = (Stock) query.getSingleResult();
		} catch (NoResultException e) {
			stock  = (Stock) addToDb(stock);
		} catch (Exception e) {
			// do nothing
		} finally {
			em.close();
		}
		return stock;
	}
	
	@SuppressWarnings("unchecked")
	protected Collection<Object> findAll(String tableName) {
		EntityManager em = EMF.get().createEntityManager();
		Collection<Object> result;
		try {
			result = new ArrayList<Object>(em.createQuery("SELECT x FROM " + tableName + " x").getResultList());
			if (result != null)
				result.size();
		} finally {
			em.close();
		}
		return result;
	}

	
	@SuppressWarnings("unchecked")
	public List<Stock> loadAllStocks () {
		System.out.println("LoadAllStocks called..");
		EntityManager em = EMF.get().createEntityManager();
		List<Stock> result = new ArrayList<Stock> ();
		try {
			result = new ArrayList<Stock>(em.createQuery("SELECT s FROM Stock s").getResultList());
			System.out.println("stockList.size() = " + result.size());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			em.close();
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public List<Quote> loadAllQuotes () {
		System.out.println("LoadAllQuotes called..");
		EntityManager em = EMF.get().createEntityManager();
		List<Quote> result = new ArrayList<Quote> ();
		try {
			result = new ArrayList<Quote>(em.createQuery("SELECT q FROM Quote q").getResultList());
			System.out.println("quotesList.size() = " + result.size());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			em.close();
		}
		return result;
	}
	@SuppressWarnings("deprecation")
	public void createInitialDataBase () {
		List<Stock> tempStocks = new ArrayList<Stock>();
		
		//Create Stocks
		if (loadAllStocks().isEmpty()) {
			tempStocks = initStocks();
		}
		
		//Create Quotes
		if (loadAllQuotes().isEmpty()) {
			System.out.println("Adding quotes...");
			Random r = new Random (7);
			List<Quote> tempQuotes = new ArrayList<Quote>();
			for (Stock s: tempStocks) {
				double currentPrice = r.nextInt(200) + 80;
				for (int y = 108; y <= 112; y++) {
					for (int m = 0; m < 12; m++) {
						for (int d = 1; d <= 28; d++) {
							for (int t = 0; t < 24; t += 24) {
								Date currentDate = new Date (y, m, d, t, 0);
								currentPrice += (r.nextInt(10) - 5 + r.nextDouble());
								Quote newQuote = new Quote (currentPrice, currentDate, s.getId());
								tempQuotes.add(newQuote);
							}
						}
					}
				}
			}
			//addQuotes(tempQuotes);
			for (Quote q: tempQuotes) 
				addQuote (q);
			System.out.println("Added " + tempQuotes.size() + " new quotes.");
		}
	}
	private Quote addQuote(Quote q) {
		EntityManager em = EMF.get().createEntityManager();
		Quote result = null;
		try {
			em.getTransaction().begin();
			result = em.merge(q);
			em.flush();
			em.getTransaction().commit();
		}
		catch (Exception e) {
			e.printStackTrace();
			em.getTransaction().rollback();
			throw new RuntimeException (e.getMessage());
		}
		finally {
			em.close();
		}
		return result;
	}
	@SuppressWarnings("unchecked")
	public Boolean getDbHasEntries () {
		System.out.println("Checking db for entries..");
		if (!hasEntries) {
			List<Stock> stocks = new ArrayList<Stock> ();
			List<Quote> quotes = new ArrayList<Quote> ();
			EntityManager em = EMF.get().createEntityManager();
			try {
				stocks = em.createQuery("SELECT s FROM Stock s").setMaxResults(1).getResultList();
				if (stocks != null)
					stocks.size();
				quotes = em.createQuery("SELECT q FROM Quote q").setMaxResults(1).getResultList();
				if (quotes != null)
					quotes.size();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				em.close();
			}
			hasEntries = !(stocks.isEmpty() || quotes.isEmpty());
		}
		else
			System.out.println("Already loaded..");
		return hasEntries;		
	}

	private List<Stock> initStocks() {
		List<Stock> stocks = new ArrayList<Stock>();
		stocks.add(addStockToDbIfNotExists(new Stock("BAS.F", "MBASF N", "USD")));
		stocks.add(addStockToDbIfNotExists(new Stock("FORD",
				"Forward Industries, Inc.", "USD")));
		stocks.add(addStockToDbIfNotExists(new Stock("IBM",
				"International Business Machines Corp.", "USD")));
		stocks.add(addStockToDbIfNotExists(new Stock("MSFT", "Microsoft Inc.",
				"USD")));
		stocks.add(addStockToDbIfNotExists(new Stock("MXX", "MXX Corp.", "EUR")));
		stocks.add(addStockToDbIfNotExists(new Stock("ORCL",
				"Oracle Corporation", "USD")));
		stocks.add(addStockToDbIfNotExists(new Stock("VOE.VI", "VOESTALPINE",
				"EUR")));
		stocks.add(addStockToDbIfNotExists(new Stock("VOW3.SG",
				"VOLKSWAGEN VZ", "EUR")));
		return stocks;
	}
}
