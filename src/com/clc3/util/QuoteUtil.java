package com.clc3.util;

import java.util.Collection;

import javax.persistence.EntityManager;

import com.clc3.persistence.EMF;
import com.clc3.persistence.model.Stock;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.taskqueue.TaskOptions.Method;

public class QuoteUtil {

	public static final long STANDARD_DELAY = 1000;
	public static final long STOP_ADDING_DELAY = 10000;
	public static final long HFT_DELAY = 20;

	@SuppressWarnings("unchecked")
	private static Collection<Stock> findAllStocks() {
		EntityManager em = EMF.get().createEntityManager();
		Collection<Stock> result;
		try {
			result = em.createQuery("SELECT x FROM Stock x").getResultList();
			if (result != null)
				result.size();
		} finally {
			em.close();
		}
		return result;
	}
	
	//CALLED BY HFT-BEAN
	public static void fillQueueWithAddQuoteRequests() {
		Queue queue = QueueFactory.getQueue("standardQueue");
		fillQueue(queue, STANDARD_DELAY);
	}
	public static void fillQueueWithAddQuoteRequestsHFT() {
		Queue queue = QueueFactory.getQueue("hftQueue");
		fillQueue(queue, HFT_DELAY);
	}
	private static void fillQueue(Queue queue, long delay) {
		for (Stock stock : findAllStocks()) {
			addQuoteTaskToQueue(queue, delay, stock.getId());
		}
	}
	
	
	//CALLED BY WORKER
	public static void sendAddQuoteRequestById(long id) {
		addQuoteTaskToQueue(QueueFactory.getQueue("standardQueue"), STANDARD_DELAY, id);
	}
	public static void sendAddQuoteRequestHFTById(Long stockId) {
		addQuoteTaskToQueue(QueueFactory.getQueue("hftQueue"), HFT_DELAY, stockId);
	}
	
	
	private static void addQuoteTaskToQueue(Queue queue, long delay, Long stockId) {
		TaskOptions taskOptions = TaskOptions.Builder
				.withCountdownMillis(delay);
		taskOptions.url("/addQuote");
		taskOptions.method(Method.GET);
		taskOptions.param("stockId", stockId.toString());
		if (delay == HFT_DELAY)
			taskOptions.param("hft", "true");
		queue.add(taskOptions);
	}
	

	public static void sendStopAddQuoteRequest() {
		Queue queue = QueueFactory.getQueue("stopAddingQueue");
		TaskOptions taskOptions = TaskOptions.Builder
				.withDefaults();
		taskOptions.url("/stopAddingQuotes");
		taskOptions.method(Method.GET);
		queue.add(taskOptions);
	}

	
}
