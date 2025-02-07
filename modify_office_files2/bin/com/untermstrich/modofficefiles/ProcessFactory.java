package com.untermstrich.modofficefiles;

import java.io.File;

import com.untermstrich.actions.Action;
import com.untermstrich.actions.Decfiler;
import com.untermstrich.actions.DecfilerHF;
import com.untermstrich.actions.Server;
import com.untermstrich.actions.VersionInfo;
import com.untermstrich.actions.Xlsxdocer;
import com.untermstrich.actions.Xlsxextractor;
import com.untermstrich.actions.Xlsxfiller;
import com.untermstrich.enums.FILE_MODE;
import com.untermstrich.enums.MOD_ACTIONS;

public class ProcessFactory {
	
	public static Action create(String[] args) throws ProcessException
	{
		//Get mode
		MOD_ACTIONS arg_action;
		try {
			arg_action = MOD_ACTIONS.valueOf(args[0]);
		} catch (IllegalArgumentException e2) {
			throw new ProcessException("Invalid argument action");
		}
		
		//Create file handle for from
		File arg_file_from;
		try {
			arg_file_from = new File(args[1]);
			if (!arg_file_from.isFile()) {
				throw new ProcessException("The from file is not a file: "+arg_file_from.getName());
			}
			if (!arg_file_from.canRead()) {
				throw new ProcessException("Cannot read the from file: "+arg_file_from.getName());
			}
		} catch (Exception e1) {
			throw new ProcessException("Opening the from file: "+e1.getMessage());
		}
		
		//Create file handle for to
		File arg_file_to;
		try {
			arg_file_to = new File(args[2]);
			if (arg_file_to.exists() && !arg_file_to.canWrite()) {
				throw new ProcessException("Cannot write the to file: "+arg_file_to.getName());
			}
		} catch (Exception e1) {
			throw new ProcessException("Opening the to file: "+e1.getMessage());
		}
		
		//Get password
		String arg_password = null;
		if (args.length>3 && args[3]!=null) {
			arg_password = args[3];
		}
		
		//Get xml file
		File arg_xml_file = null;
		if (args.length>4 && args[4]!=null) {
			try {
				arg_xml_file = new File(args[4]);
				if (!arg_xml_file.isFile()) {
					throw new ProcessException("The xml file is not a file: "+arg_xml_file.getName());
				}
				if (!arg_xml_file.canRead()) {
					throw new ProcessException("Cannot read the xml file: "+arg_xml_file.getName());
				}
			} catch (Exception e1) {
				throw new ProcessException("Opening the xml file: "+e1.getMessage());
			}
		}
		
		//Call action
		Action action = null;
		FILE_MODE file_mode = FILE_MODE.xml;
		switch (arg_action) {
			case xlsxdoc:
				action = new Xlsxdocer();
				break;
			case xlsxfill:
				action = new Xlsxfiller();
				break;
			case xlsxfillcsv:
				action = new Xlsxfiller();
				file_mode = FILE_MODE.csv;
				break;
			case xlsxextract:
				action = new Xlsxextractor();
				break;
			case decfile:
				action = new Decfiler();
				break;
			case decfilehf:
				action = new DecfilerHF();
				break;
			case server:
				action = new Server(false);
				break;
			case serverold:
				action = new Server(true);
				break;
			case version:
				action = new VersionInfo();
				break;
		}
		action.setup(arg_file_from, arg_file_to, arg_password, arg_xml_file, file_mode);
		return action;
	}

}
