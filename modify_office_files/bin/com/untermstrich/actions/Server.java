package com.untermstrich.actions;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.untermstrich.communication.ServerThread;
import com.untermstrich.communication.TokenSizeException;
import com.untermstrich.modofficefiles.ModOfficeFiles;

public class Server extends Action {
	private boolean old_mode;
	
	public Server(boolean old_mode) {
		this.old_mode = old_mode;
	}
	
	@Override
	public void run() {
		logger.info("Startup server");
		
		try {
			//Get json config
			BufferedReader buffered_reader = new BufferedReader(new FileReader(arg_file_from));
			JsonObject info_object = Json.parse(buffered_reader).asObject();
			
			ServerThread thread = new ServerThread(
					info_object.getBoolean("local", true), 
					info_object.getInt("port", 17332), 
					info_object.getString("token", ""),
					info_object.getInt("age", 0),
					old_mode,
					arg_file_from
			);
			
			thread.start();
			
			setReply("DONE Server start");
			
		} catch (FileNotFoundException e) {
			logger.error(ModOfficeFiles.err1);
			logger.error("Error file not found: ", e);
		} catch (IOException e) {
			logger.error(ModOfficeFiles.err1);
			logger.error("Error: ", e);
		} catch (TokenSizeException e) {
			logger.error(ModOfficeFiles.err1);
			logger.error("Error in config file: ", e);
		}
	}

}
