package com.untermstrich.communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.untermstrich.modofficefiles.logger.LoggerFactory;
import com.untermstrich.modofficefiles.logger.LoggerImplementation;

public class ServerThread extends Thread {
	
	public static String err1 = "Modify office files server";
	private static final LoggerImplementation logger = LoggerFactory.getLogger();
	
	/**
	 * The managed server socket
	 */
	private ServerSocket serversocket;
	
	private boolean local;
	private int port;
	private String token;
	private int age;
	private boolean old_mode;
	private File json_file_from;
	
	public ServerThread(boolean local, int port, String token, int age, boolean old_mode, File json_file_from) throws TokenSizeException {
		if (token.length()<10) {
			throw new TokenSizeException("Invalid token size");
		}
		if (age<10) {
			age = 10;
		}
		
		logger.info("Init server on port "+port);
		if (local) {
			logger.info("Localhost only");
		}
		
		this.local = local;
		this.port = port;
		this.token = token;
		this.age = age;
		this.old_mode = old_mode;
		this.json_file_from = json_file_from;
	}

	/**
	 * Wait for the request
	 */
	public void run()
	{
		try {
			logger.info("ServerThread running");
			
			if (local) {
				serversocket = new ServerSocket(port, 10, InetAddress.getByName("localhost"));
			} else {
				serversocket = new ServerSocket(port, 10);
			}
			
			logger.info("ServerSocket open");
			
			if (!old_mode) {
				logger.info("Full startup");
				
				//Get startup from file path
				File jar_file = new File(ClassLoader.getSystemClassLoader().getResource(".").getPath());
				Path json_file_path = Paths.get(json_file_from.getAbsolutePath());

				ServerProcesserThread startupProcesser = new ServerProcesserThread(jar_file.getAbsolutePath(), json_file_path.getParent().toString());
				startupProcesser.start();
				
				logger.info("Full startup called");
			}
			
			logger.info("ServerSocket listening");
			
			while (!this.isInterrupted()) {
				// Wait for requests
				Socket client = serversocket.accept();
				
				logger.info("ServerThread starting communication");

				// Get request
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));

				// Get reply
				DataOutputStream out = new DataOutputStream(client.getOutputStream());

				ServerProcesserThread processer = new ServerProcesserThread(token, age, in, out, client);
				processer.start();
			}
			
			logger.info("ServerThread died");

		} catch (BindException e) {
			logger.fatal("Server Error when binding "+port+": ", e);
		} catch (UnknownHostException e) {
			logger.fatal("Server Error unknown Host: ", e);
		} catch (IOException e) {
			logger.error("Server Error: ", e);
		}
	}
	
}
