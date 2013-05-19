package com.clc3.persistence.model;

public class RequestData {

	private Object key;
	private boolean cached;

	public RequestData(Object key, boolean cached) {
		this.key = key;
		this.cached = cached;
	}
	
	public RequestData() {}

	public boolean isCached() {
		return cached;
	}

	public void setCached(boolean cached) {
		this.cached = cached;
	}

	public Object getKey() {
		return key;
	}

	public void setKey(Object key) {
		this.key = key;
	}
}
