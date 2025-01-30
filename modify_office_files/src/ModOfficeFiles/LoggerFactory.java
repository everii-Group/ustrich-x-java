package ModOfficeFiles;

public class LoggerFactory {

	public static enum Modes {
		Basic, Log4j
	}

	private static LoggerImplementation instance;
	private static Modes mode = Modes.Basic;

	private LoggerFactory() {
	}

	public static synchronized LoggerImplementation getLogger() {
		if (instance == null) {
			switch (mode) {
			case Basic:
				instance = new LoggerLogBasic();
				break;
			case Log4j:
				instance = new LoggerLog4j();
				break;
			}
			instance.debug("Init logger "+mode.name());
		}
		return instance;
	}

	public static synchronized void setMode(Modes mode) {
		LoggerFactory.mode = mode;
	}

}
