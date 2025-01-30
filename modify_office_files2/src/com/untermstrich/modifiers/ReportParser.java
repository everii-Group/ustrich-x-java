package com.untermstrich.modifiers;

import java.util.Date;

public interface ReportParser {

	/**
	 * Get Report ID
	 * 
	 * @return the report_id
	 */
	public abstract String getReport_id();

	/**
	 * @return The min date of modifier
	 */
	public abstract Date getMin_date();

	/**
	 * @return The max date of modifier
	 */
	public abstract Date getMax_date();

	/**
	 * @return The modifier
	 */
	public abstract XlsxModifier getModifier();

}