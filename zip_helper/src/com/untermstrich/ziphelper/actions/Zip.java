package com.untermstrich.ziphelper.actions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.untermstrich.ziphelper.FILE_TYPES;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class Zip extends Action {
	
	static String err1 = "Zip helper error - Zip";

	@Override
	public void run(File arg_file, String arg_password, String arg_type) {
		String zip_file_name = arg_file.getAbsolutePath()+".zip";
		String file_name = arg_file.getName();
		
		FILE_TYPES arg_type_enum = FILE_TYPES.any;
		try {
			if (Action.typeContains(arg_type)) {
				arg_type_enum = FILE_TYPES.valueOf(arg_type);				
			}
		} catch (IllegalArgumentException e2) {
			if (!file_name.endsWith("."+arg_type)) {
				file_name = file_name + "."+arg_type;
			}
		}

		switch (arg_type_enum) {
				//------------------------------------
			case msg:
				if (!file_name.endsWith(".msg")) {
					file_name = file_name + ".msg";
				}
				break;
			case eml:
				if (!file_name.endsWith(".eml")) {
					file_name = file_name + ".eml";
				}
				break;
				//------------------------------------
			case docx:
				if (!file_name.endsWith(".docx")) {
					file_name = file_name + ".docx";
				}
				break;
			case doc:
				if (!file_name.endsWith(".doc")) {
					file_name = file_name + ".doc";
				}
				break;
			case odt:
				if (!file_name.endsWith(".odt")) {
					file_name = file_name + ".odt";
				}
				break;
				//------------------------------------
			case xlsx:
				if (!file_name.endsWith(".xlsx")) {
					file_name = file_name + ".xlsx";
				}
				break;
			case xlsm:
				if (!file_name.endsWith(".xlsm")) {
					file_name = file_name + ".xlsm";
				}
				break;
			case xls:
				if (!file_name.endsWith(".xls")) {
					file_name = file_name + ".xls";
				}
				break;
			case ods:
				if (!file_name.endsWith(".ods")) {
					file_name = file_name + ".ods";
				}
				break;
				//------------------------------------
			case html:
				if (!file_name.endsWith(".html") && !file_name.endsWith(".htm")) {
					file_name = file_name + ".html";					
				}
				break;
			case any:
			default:
				//Do not change file_name
				break;
		}
		
		//Create file
		ZipFile zip_file = null;
		InputStream inputStream = null;
		try {
			zip_file = new ZipFile(zip_file_name);
			
			ZipParameters zip_parameters = new ZipParameters();
			//Set compression method to deflate compression
			zip_parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
			//Set compression level
			zip_parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
			
			if (!arg_password.equals("-")) {
				zip_parameters.setEncryptFiles(true);
				zip_parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
				zip_parameters.setPassword(arg_password);
			}
			
			zip_parameters.setFileNameInZip(file_name);
			zip_parameters.setSourceExternalStream(true);
			
			//Read file
			inputStream = new FileInputStream(arg_file);
			zip_file.addStream(inputStream, zip_parameters);
			
			System.out.println("ZIP FILE CREATED");
			
		} catch (ZipException e) {
			System.out.println(err1+" ZipException");
			System.out.println(e.getMessage()+" ("+e.getCode()+")");
			e.printStackTrace(System.err);
		} catch (FileNotFoundException e) {
			System.out.println(err1+" FileNotFoundException");
			System.out.println(e.getMessage()+" ("+e.getCause()+")");
			e.printStackTrace(System.err);
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
			}			
		}
		
	}

}
