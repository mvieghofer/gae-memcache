package com.clc3.persistence.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.clc3.persistence.model.api.DatabaseEntity;


@Entity
public class Quote implements Serializable,DatabaseEntity {
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private Double price;
//	private Double ask;
//	private Double bid;
	private Date time;
	private Long stockId;
	
	
	
	
	public Quote(Double price, /* Double ask, Double bid, */ Date time, Long stockId) {
		super();
		this.price = price;
//		this.ask = ask;
//		this.bid = bid;
		this.time = time;
		this.stockId = stockId;
	}
	
	public Quote() {
		// TODO Auto-generated constructor stub
	}





	public Double getPrice() {
		return price;
	}
	public void setPrice(Double price) {
		this.price = price;
	}
//	public Double getAsk() {
//		return ask;
//	}
//	public void setAsk(Double ask) {
//		this.ask = ask;
//	}
//	public Double getBid() {
//		return bid;
//	}
//	public void setBid(Double bid) {
//		this.bid = bid;
//	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public Long getId() {
		return id;
	}

	public Long getStockId() {
		return stockId;
	}

	public void setStockId(Long stockId) {
		this.stockId = stockId;
	}

	@Override
	public Object getPrimaryKey() {
		return id;
	}
	

}