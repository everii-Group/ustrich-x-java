package ModOfficeFiles;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoggerLog4j extends LoggerImplementation {
	
	private static final Logger logger = LogManager.getLogger(ModOfficeFilesConfig.DEFAULT_LOGGER);

	@Override
	protected void log(LoggerLevels level, String message, Throwable t) {
		switch (level) {
		case DEBUG:
			logger.debug(message, t);
			break;
		case ERROR:
			logger.error(message, t);
			break;
		case FATAL:
			logger.fatal(message, t);
			break;
		case INFO:
			logger.info(message, t);
			break;
		case TRACE:
			logger.trace(message, t);
			break;
		case WARN:
			logger.warn(message, t);
			break;
		}
	}

	@Override
	protected void log(LoggerLevels level, String message) {
		switch (level) {
		case DEBUG:
			logger.debug(message);
			break;
		case ERROR:
			logger.error(message);
			break;
		case FATAL:
			logger.fatal(message);
			break;
		case INFO:
			logger.info(message);
			break;
		case TRACE:
			logger.trace(message);
			break;
		case WARN:
			logger.warn(message);
			break;
		}
	}

}
