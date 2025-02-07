package com.untermstrich.actions;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

public class VersionInfo extends Action {
	
	@Override
	public void run() {
		try {
			String version = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("version.txt"));
			setReply(version);
		} catch (IOException e) {
			setReply("IO error.");
			logger.error("Error:", e);
		}
	}
	
}
