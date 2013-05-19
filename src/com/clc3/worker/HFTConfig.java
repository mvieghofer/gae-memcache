package com.clc3.worker;


public class HFTConfig {
	private static HFTConfig instance = new HFTConfig();
	private Boolean addQuoteEnabled = true;

	public static HFTConfig getInstance() {
		return instance;
	}
	private HFTConfig() {}

	
	public void clear() {
		instance = new HFTConfig();
	}


	public Boolean getAddQuoteEnabled() {
		return addQuoteEnabled;
	}
	public void setAddQuoteEnabled(Boolean addQuoteEnabled) {
		this.addQuoteEnabled = addQuoteEnabled;
	}

}
