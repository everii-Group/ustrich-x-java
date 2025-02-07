package com.untermstrich.simples;

public class ParsedFilterRule {

	String title;
	String value;
	
	public ParsedFilterRule(String title, String value) {
		super();
		this.title = title;
		this.value = value;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	
}
