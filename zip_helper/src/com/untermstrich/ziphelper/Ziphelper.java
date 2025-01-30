package com.untermstrich.ziphelper;

//import java.io.FileOutputStream;
import java.io.File;

import com.untermstrich.ziphelper.actions.Action;
import com.untermstrich.ziphelper.actions.Unzip;
import com.untermstrich.ziphelper.actions.Zip;

public class Ziphelper {
	
	static String err1 = "Zip helper error";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Check input parameters
		if (args.length<4) {
			System.out.println(err1);
			System.out.println("Arguments missing:");
			System.out.println(" 1: action (zip, unzip, unzipfile)");
			System.out.println(" 2: file name");
			System.out.println(" 3: password");
			System.out.println(" 4: type (msg, eml, doc, docx, odt, xlsx, xlsm, xls, ods, html)");
			return;
		}
		
		//Get mode
		FILE_ACTIONS arg_action;
		try {
			arg_action = FILE_ACTIONS.valueOf(args[0]);
		} catch (IllegalArgumentException e2) {
			System.out.println(err1);
			System.out.println("Invalid argument action");
			return;
		}
		
		//Create file handle
		File arg_file;
		try {
			arg_file = new File(args[1]);
			if (!arg_file.isFile()) {
				System.out.println(err1);
				System.out.println("The zip file is not a file");
				System.out.println(arg_file.getName());
				return;
			}
			if (!arg_file.canRead()) {
				System.out.println(err1);
				System.out.println("Cannot read the zip file");
				System.out.println(arg_file.getName());
				return;
			}
		} catch (Exception e1) {
			System.out.println(err1);
			System.out.print("Opening the file:");
			System.out.println(e1.getMessage());
			return;
		}
		
		//Get password
		String arg_password = args[2];
		
		//Get type
		String arg_type = args[3].trim();
		
		//Call action
		Action action = null;
		switch (arg_action) {
			case unzipfile:
				action = new Unzip(true);
				break;
			case unzip:
				action = new Unzip(false);
				break;
			case zip:
				action = new Zip();
				break;
		}
		action.run(arg_file, arg_password, arg_type);
			
	}

}
