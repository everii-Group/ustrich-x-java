package com.untermstrich.communication;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.codec.digest.HmacUtils;

import ModOfficeFiles.LoggerFactory;
import ModOfficeFiles.LoggerImplementation;

import com.untermstrich.modofficefiles.ProcessException;
import com.untermstrich.runner.ServerRunner;

public class ServerProcesserThread extends Thread {
	
	public static String err1 = "Modify office files server processer";
	private static final LoggerImplementation logger = LoggerFactory.getLogger();
	
	private boolean startupMode;
	
	private BufferedReader in;
	private DataOutputStream out;
	private Socket client;
	private String token;
	private int age;
	
	private String jar_file_path;
	private String json_file_path;

	public ServerProcesserThread(String token, int age, BufferedReader in, DataOutputStream out, Socket client) {
		this.startupMode = false;
		this.in = in;
		this.out = out;
		this.client = client;
		this.token = token;
		this.age = age;
	}
	
	public ServerProcesserThread(String jar_file_path, String json_file_path) {
		this.startupMode = true;
		this.jar_file_path = jar_file_path;
		this.json_file_path = json_file_path;
	}
	
	/**
	 * Process the request
	 */
	public void run()
	{
		String reply = "Prepare process failed without message";
		try {
			ServerRunner runner;
			
			if (startupMode) {
				String[] args = {
					"xlsxextract",
					jar_file_path+"/test.xlsx",
					json_file_path+"/test.txt",
					null,
					null
				};
				
				runner = new ServerRunner(args);
			} else {
				String first = in.readLine();
				
				logger.debug("Comm: "+first);
		
				// Check for request method
				if (!first.toUpperCase().startsWith("GET")) {
					logger.error("Error - Have to send 501, GET missing.");
					writeOutput(create_status(501));
					return;
				}
				
				// Split into 3 parts (when we have a space)
				String[] firsts = first.split("\\s+");
				if (firsts.length != 3) {
					logger.error("Error - Have to send 400, did not get 3 uri parts.");
					writeOutput(create_status(400));
					return;
				}
				
				//Take part 2 as URI
				URI url = new URI(firsts[1]);
				String arg_action = url.getPath();
				Map<String, String> query_parts = splitQuery(url);
				
				//We need an action
				String[] arg_actions = arg_action.replaceFirst("^/", "").split("/", -1);
				if (arg_actions.length<1) {
					logger.error("Error - Have to send 404, action missing in uri.");
					writeOutput(create_status(404));
					return;
				}
				
				// GET /xlsxextract?file_from=%2FLibrary%2FWebServer%2FDocuments%2Fdata%2Fprojekte%2F_KeineNr_37%2Fdoku%2F11.testpay%2Ft2_998.xlsx&file_to=%2FLibrary%2FWebServer%2FDocuments%2Fversionx1%2Fwriteable%2Ftemp%2FctxEZ8PcK&time=1454413873&hmac=15b126b049ed9093d0ae38458d0533830154604d4b7aa601c4fd25d6dac0dafe HTTP/1.1
				//Create args array
				String[] args = {
					arg_actions[0],
					query_parts.get("file_from"),
					query_parts.get("file_to"),
					query_parts.get("password"),
					query_parts.get("xml_file")
				};
				String time = query_parts.get("time");
				
				logger.debug("Comm args: "+Arrays.toString(args));
				
				//Create HMAC
				StringBuilder forHmac = new StringBuilder();
				for (String arg : args) {
					if (arg!=null) {
						forHmac.append(arg);
					}
					forHmac.append("|");
				}
				forHmac.append(time);
				logger.trace("Comm forHmac: "+forHmac.toString());
				String myHmac = HmacUtils.hmacSha256Hex(token, forHmac.toString());
				
				//Validate HMAC
				String clientHmac = query_parts.get("hmac");
				if (!myHmac.equals(clientHmac)) {
					logger.error("Error - Have to send 403, Hmac mismatch: "+myHmac+" != "+clientHmac);
					writeOutput(create_status(403));
					return;
				}
				
				//Validate time 
				Date timeDate = new Date(Long.parseLong(time)*1000L);
				Date nowDate = new Date();
				long diff = (nowDate.getTime() - timeDate.getTime()) / (60 * 1000) % 60;
				
				if (Math.abs(diff)>age) {
					logger.error(String.format("Error - Have to send 403, Hmac time mismatch:  %1$te.%1$tm.%1$tY at %1$tH:%1$tM:%1$tS > "+age+" %n", timeDate));
					writeOutput(create_status(403));
					return;
				}
				
				runner = new ServerRunner(args);
			}
			
			//Process
			runner.start();
			
			//Ensure max runtime
			final long kill = runner.get_kill_time();
			
			while (runner.isAlive()) {
				if (System.nanoTime() > kill) {
					//Fail - timeout
					runner.kill();
					logger.error("Error - Have to send 500, the running action has timed out.");
					writeOutput(create_status(500));
					return;
				}
				try {
					Thread.sleep(200); //0.2 seconds
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			
			//Done
			reply = runner.getReply(); 
			logger.info("Full startup status");
			logger.info(reply);
			
			if (startupMode) {
				System.out.println("DONE Full startup");
			} else {
				writeOutput(create_status(200)+"\r\n"+reply);
			}
			return;
		} catch (SocketException e) {
			reply = "Process Socket Error";
			logger.error(reply+": ", e);
			try {
				writeOutput(create_status(500)+"\r\n"+reply);
			} catch (IOException e1) {
				logger.error("Error on sending error: ", e1);
			}
		} catch (IOException e) {
			reply = "Process IO Error";
			logger.error(reply+": ", e);
			try {
				writeOutput(create_status(500)+"\r\n"+reply);
			} catch (IOException e1) {
				logger.error("Error on sending error: ", e1);
			}
		} catch (URISyntaxException e) {
			reply = "Process URI Error";
			logger.error(reply+": ", e);
			try {
				writeOutput(create_status(500)+"\r\n"+reply);
			} catch (IOException e1) {
				logger.error("Error on sending error: ", e1);
			}
		} catch (ProcessException e) {
			reply = "Process Error: ";
			logger.error(reply+": ", e);
			try {
				writeOutput(create_status(500)+"\r\n"+reply);
			} catch (IOException e1) {
				logger.error("Error on sending error: ", e1);
			}
		} finally {
			try {
				if (client!=null && !client.isClosed()) {
					logger.error("Error - Have to send 500, the connection should have already been closed.");
					writeOutput(create_status(500)+"\r\n"+reply);
				}
			} catch (IOException e) {
				logger.error("Error:", e);
			} finally {
				try {
					if (client!=null && !client.isClosed()) {
						client.close();
					}
				} catch (IOException e) {
					logger.error("Error:", e);
				}
			}
		}
	}
	
	private void writeOutput(String message) throws IOException {
		if (out!=null && client!=null) {
			out.writeBytes(message);
			client.close();
		} else {
			logger.error(message);
		}
	}
	
	private static Map<String, String> splitQuery(URI url) throws UnsupportedEncodingException {
		Map<String, String> query_pairs = new LinkedHashMap<String, String>();
		String query = url.getRawQuery();
		System.out.println("query: "+query);
		String[] pairs = query.split("&");
		for (String pair : pairs) {
			int idx = pair.indexOf("=");
			System.out.println(pair);
			query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
		}
		return query_pairs;
	}

	
	/**
	 * Create the HTTP status header
	 * 
	 * @param code
	 * @return
	 */
	protected static String create_status(int code)
	{
		String s = "HTTP/1.0 ";
		// Well known codes
		switch (code)
			{
			case 200:
				s += "200 OK";
				break;
			case 400:
				s += "400 Bad request";
				break;
			case 403:
				s += "403 Authorization failed - Forbidden";
				break;
			case 404:
				s += "404 Not Found";
				break;
			case 500:
				s += "500 Internal Server Error";
				break;
			case 501:
				s += "501 Not Implemented";
				break;
			}
		s += "\r\n";
		s += "Connection: close\r\n"; // No persistent communication
		s += "Server: "+err1+"\r\n"; // Server
		// name
		return s;
	}
}
