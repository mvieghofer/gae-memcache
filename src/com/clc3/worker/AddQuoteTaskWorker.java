package com.clc3.worker;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheManager;
import javax.persistence.EntityManager;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.clc3.bean.MetadataBean;
import com.clc3.persistence.EMF;
import com.clc3.persistence.model.RequestData;
import com.clc3.persistence.model.Quote;
import com.clc3.util.QuoteUtil;

public class AddQuoteTaskWorker extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger
			.getLogger(AddQuoteTaskWorker.class.getName());
	private Cache cache;


	@SuppressWarnings("unchecked")
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		MetadataBean bean = (MetadataBean) getServletContext().getAttribute("metadataBean");
		Long stockId = -1L;
		boolean isHft = false;
		String stockIdParam = "";
		String hftParam = "";
		try {
			stockIdParam = req.getParameter("stockId");
		} catch (Exception e) {
			logger.info("Error getting parameter 'stockId'");
		}
		try {
			hftParam = req.getParameter("hft");
		} catch (Exception e) {
			logger.info("Error getting parameter 'hft'");
		}

		if (HFTConfig.getInstance().getAddQuoteEnabled()) {
			try {
				if (!stockIdParam.equals(""))
					stockId = Long.parseLong(stockIdParam);
				if (hftParam != null && !hftParam.equals(""))
					isHft = Boolean.parseBoolean(hftParam);
				Random rand = new Random();
				if (stockId > -1) {
					Quote quote = new Quote(50 * rand.nextDouble(), new Date(
							System.currentTimeMillis()), stockId);
					if (isHft && bean.getCachingEnabled()) {
						if (cache == null)
							createCache();
						String key = UUID.randomUUID().toString();
						bean.getHftDataCaching()
								.add(new RequestData(key, true));
						cache.put(key, quote);
					} else {
						EntityManager em = EMF.get().createEntityManager();
						try {
							em.getTransaction().begin();
							em.merge(quote);
							em.flush();
							em.getTransaction().commit();
						} catch (Exception e) {
							em.getTransaction().rollback();
						} finally {
							em.close();
						}
						if (bean.getCachingEnabled()) {
							bean.getHftDataCaching()
								.add(new RequestData(quote.getId(), false));
						}
						else {
							bean.getHftDataNoCaching()
								.add(new RequestData(quote.getId(), false));
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (!isHft)
				QuoteUtil.sendAddQuoteRequestById(stockId);
			else
				QuoteUtil.sendAddQuoteRequestHFTById(stockId);
			if (isHft)
				System.out.print("------- HFT -------");
			System.out.println("Added Quote successfully: " + stockId);
		}
		resp.setContentType("applicaiton/text");
		resp.getWriter().write("Added Quote successfully");
	}

	private void createCache() {
		try {
			cache = CacheManager.getInstance().getCacheFactory()
					.createCache(Collections.emptyMap());

		} catch (CacheException e) {
			// e.printStackTrace();
		}
	}
}
