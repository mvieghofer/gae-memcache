package com.clc3.worker;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class StopAddingQuotesTaskWorker extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		HFTConfig.getInstance().setAddQuoteEnabled(false);
		System.out.println("------------------------");
		System.out.println("--------- Stop ---------");
		System.out.println("------------------------");
	}
}
