package com.untermstrich.modofficefiles.logger;


public abstract class LoggerImplementation {
	
	public static enum LoggerLevels {
		FATAL, ERROR, WARN, INFO, DEBUG, TRACE
	}
	
	/**
	 * Logs a fatal message
	 * 
	 * @param message
	 * @param t
	 */
    public void fatal(String message, Throwable t) {
    	log(LoggerLevels.FATAL, message, t);
    }

	/**
	 * Logs a error message
	 * 
	 * @param message
	 * @param t
	 */
    public void error(String message, Throwable t) {
    	log(LoggerLevels.ERROR, message, t);
    }
    
	/**
	 * Logs a error message
	 * 
	 * @param message
	 */
    public void error(String message) {
    	log(LoggerLevels.ERROR, message);
    }

	/**
	 * Logs a warn message
	 * 
	 * @param message
	 * @param t
	 */
    public void warn(String message, Throwable t) {
    	log(LoggerLevels.WARN, message, t);
    }

	/**
	 * Logs a info message
	 * 
	 * @param message
	 */
    public void info(String message) {
    	log(LoggerLevels.INFO, message);
    }

	/**
	 * Logs a debug message
	 * 
	 * @param message
	 */
    public void debug(String message) {
    	log(LoggerLevels.DEBUG, message);
    }

	/**
	 * Logs a trace message
	 * 
	 * @param message
	 */
    public void trace(String message) {
    	log(LoggerLevels.TRACE, message);
    }

    /**
     * Logs a message with level
     * 
     * @param level
     * @param message
     * @param t
     */
    abstract protected void log(LoggerLevels level, String message, Throwable t);

    /**
     * Logs a message with level
     * 
     * @param level
     * @param message
     */
    abstract protected void log(LoggerLevels level, String message);
    
    
}
