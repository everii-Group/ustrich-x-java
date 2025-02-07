package com.untermstrich.actions;

import java.io.File;

import com.untermstrich.enums.FILE_MODE;
import com.untermstrich.modofficefiles.logger.LoggerFactory;
import com.untermstrich.modofficefiles.logger.LoggerImplementation;

public abstract class Action implements Runnable {
	
	protected static final LoggerImplementation logger = LoggerFactory.getLogger();
	protected File arg_file_from; 
	protected File arg_file_to;
	protected String arg_password;
	protected File arg_xml_file;
	protected FILE_MODE file_mode;
	protected StringBuilder reply = new StringBuilder();

	/**
	 * Setup - Call before start
	 * 
	 * @param arg_file_from
	 * @param arg_file_to
	 * @param arg_password
	 * @param arg_xml_file
	 */
	public void setup(File arg_file_from, File arg_file_to, String arg_password, File arg_xml_file, FILE_MODE file_mode) {
		this.arg_file_from = arg_file_from;
		this.arg_file_to = arg_file_to;
		this.arg_password = arg_password;
		this.arg_xml_file = arg_xml_file;
		this.file_mode = file_mode;
	}
	
	/**
	 * Define max runtime
	 * 
	 * @return seconds
	 */
	public long max_runtime()
	{
		return 1800;
	}

	/**
	 * Get process reply
	 * 
	 * @return
	 */
	public final String getReply() {
		return reply.toString();
	}

	/**
	 * Set process reply
	 * 
	 * @param reply
	 */
	protected void setReply(String reply) {
		if (this.reply.length()>0) {
			this.reply.append("\n");			
		}
		this.reply.append(reply);
	}

}
