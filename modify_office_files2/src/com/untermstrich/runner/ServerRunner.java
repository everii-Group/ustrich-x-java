package com.untermstrich.runner;

import com.untermstrich.actions.Action;
import com.untermstrich.modofficefiles.ProcessException;
import com.untermstrich.modofficefiles.ProcessFactory;
import com.untermstrich.modofficefiles.logger.LoggerFactory;
import com.untermstrich.modofficefiles.logger.LoggerImplementation;

public class ServerRunner {
	
	private Action action;
	private Thread thread;
	protected StringBuilder messages = new StringBuilder();
	private static final LoggerImplementation logger = LoggerFactory.getLogger();
	
	public ServerRunner(String[] args) throws ProcessException {
		action = ProcessFactory.create(args);
		Thread.UncaughtExceptionHandler handler = new Thread.UncaughtExceptionHandler() {
		    public void uncaughtException(Thread th, Throwable ex) {
		        messages.append("Processor exception: "+ex+"\n");
		    }
		};
		thread = new Thread(action);
		thread.setUncaughtExceptionHandler(handler);
	}
	
	public synchronized void start() {
		logger.debug("Start runner thread: "+action.getClass());
		thread.start();
	}
	
	/**
	 * Get killtime
	 * 
	 * @return seconds
	 */
	public final long get_kill_time()
	{
		return System.nanoTime() + (action.max_runtime() * 1000 * 1000 * 1000L);
	}
	
	/**
	 * Kill process
	 */
	public void kill()
	{
		logger.error("Timeout error when running "+action.getClass().toString()+".");
		logger.error("Could not finish execution within "+action.max_runtime()+" seconds.");
		logger.error("Please ensure the file is not corrupted and/or upgrade your server hardware.");
		thread.interrupt();
		thread = null;
		action = null;
	}

    /**
     * Tests if this thread is alive.
     */
	public final boolean isAlive() {
		return thread.isAlive();
	}

	/**
	 * Get process reply
	 * 
	 * @return
	 */
	public final String getReply() {
		return messages.toString()+action.getReply();
	}

}
