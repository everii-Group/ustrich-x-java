package com.untermstrich.modofficefiles;

import java.util.Arrays;

import org.apache.poi.openxml4j.util.ZipSecureFile;

import com.untermstrich.enums.MOD_ACTIONS;
import com.untermstrich.modofficefiles.logger.LoggerFactory;
import com.untermstrich.modofficefiles.logger.LoggerImplementation;
import com.untermstrich.runner.ServerRunner;

public class ModOfficeFiles {
	
	public static String err1 = "Modify office files error";
	
	private static LoggerImplementation logger;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		//Define logger using -Dlogger=... command
		String loggerModeString = System.getProperty("logger");
		if (loggerModeString!=null && loggerModeString.equalsIgnoreCase("Log4j")) {
			LoggerFactory.setMode(LoggerFactory.Modes.Log4j);
		} else {
			LoggerFactory.setMode(LoggerFactory.Modes.Basic);
		}
		logger = LoggerFactory.getLogger();
		
		//Check input parameters
		if (args.length<3) {
			logger.error(err1);
			logger.error("Arguments missing:");
			logger.error(" 1: action (xlsxdoc, xlsxfill, xlsxfillcsv, xlsxextract, decfile, decfilehf, server, serverold)");
			logger.error(" 2: file name from | server config");
			logger.error(" 3: file name to | server log");
			logger.error(" 4: password");
			logger.error(" 5: xml file for doc/fill");
			logger.error(" Additionally you can define the logger using the java system property -Dlogger=");
			logger.error(" Possible logger: Log4j or Basic (Default)");
			logger.error(" Use –Dlog4j2.formatMsgNoLookups=True when using Log4j.");
			return;
		}
		
		logger.debug("Startup with parameters: "+Arrays.toString(args));
		try {
			//Get mode and set default logger
			MOD_ACTIONS arg_action;
			try {
				arg_action = MOD_ACTIONS.valueOf(args[0]);
				if (arg_action.equals(MOD_ACTIONS.server)) {
					ModOfficeFilesConfig.DEFAULT_LOGGER = "org.apache.logging.log4j.untermstrich.server";
				}
			} catch (IllegalArgumentException e2) {
				throw new ProcessException("Invalid argument action");
			}
			
			//Avoid Zip bomb detected error
			ZipSecureFile.setMinInflateRatio(0.001);
			
			//Create and start runner
			ServerRunner runner = new ServerRunner(args);
			
			logger.trace( (System.nanoTime()/(1000 * 1000 * 1000L))+" START");
			runner.start();
			logger.trace( (System.nanoTime()/(1000 * 1000 * 1000L))+" END START");

			//Ensure max runtime
			final long kill = runner.get_kill_time();
			
			logger.trace( (kill/(1000 * 1000 * 1000L))+" KILL TIME");
			
			while (runner.isAlive()) {
				if (System.nanoTime() > kill) {
					//Fail - timeout
					runner.kill();
					System.exit(1);
					return;
				}
				try {
					Thread.sleep(200); //0.2 seconds
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
				logger.trace( (System.nanoTime()/(1000 * 1000 * 1000L))+" RUNNING" );
			}
			
			String reply = runner.getReply();
			System.out.println(reply); //Needed to output DONE reply
			logger.debug("Runner reply: "+reply);
			
		} catch (ProcessException e) {
			logger.error(err1);
			logger.error(e.getMessage());
		}
	}

}
