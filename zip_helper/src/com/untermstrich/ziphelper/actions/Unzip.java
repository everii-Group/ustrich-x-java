package com.untermstrich.ziphelper.actions;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.untermstrich.ziphelper.FILE_TYPES;
import com.untermstrich.ziphelper.extractors.Extractor;
import com.untermstrich.ziphelper.extractors.OutfileExtractor;
import com.untermstrich.ziphelper.extractors.OutputExtractor;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;


public class Unzip extends Action {

	static String err1 = "Zip helper error - Unzip";
	boolean into_outfile = false;

	/**
	 * Unzip file
	 * 
	 * @param 	into_outfile	Save into file, else print to System.out
	 */
	public Unzip(boolean into_outfile) {
		this.into_outfile = into_outfile;
	}

	@Override
	public void run(File arg_file, String arg_password, String arg_type) {
		FILE_TYPES arg_type_enum = FILE_TYPES.any;
		try {
			if (Action.typeContains(arg_type)) {
				arg_type_enum = FILE_TYPES.valueOf(arg_type);				
			}
		} catch (IllegalArgumentException e2) {
			System.out.println(err1);
			System.out.println("Invalid argument type");
			return;
		}
		
		//Open file
		ZipFile zip_file;
		try {
			zip_file = new ZipFile(arg_file);
			
			//Is a valid zip file
			if (!zip_file.isValidZipFile()) {
				System.out.println(err1);
				System.out.println("The zip file is not a valid zip file");
				return;
			}
		} catch (ZipException e3) {
			System.out.println(err1);
			System.out.print("Opening the zip file:");
			System.out.println(e3.getMessage());
			return;
		}
		
		try {
			//If the zip is encrypted - set the password
			if (zip_file.isEncrypted()) {
				if (arg_password.equals("-")) {
					System.out.println(err1);
					System.out.println("The zip file is password protected but you don't have the right to open it.");
					return;
				}
				zip_file.setPassword(arg_password);
			}
			
			//Try to find a valid file and extract it
			Extractor ex = null;
			@SuppressWarnings("unchecked")
			List<FileHeader> file_header_list = zip_file.getFileHeaders();
			for (int i = 0; i < file_header_list.size(); i++) {
				FileHeader file_header = file_header_list.get(i);
				
				//No directories
				if (file_header.isDirectory()) {
					continue;
				}
				
				String file_name = file_header.getFileName().toLowerCase();
				
				switch (arg_type_enum) {
					case msg:
					case eml:
						if (file_name.endsWith(".msg") || file_name.endsWith(".eml")) {
							ex = init_extractor(arg_file, zip_file, file_header);
						}
						break;
					case docx:
					case doc:
					case odt:
						if (file_name.endsWith(".docx") || file_name.endsWith(".doc") || file_name.endsWith(".odt")) {
							ex = init_extractor(arg_file, zip_file, file_header);
						}
						break;
					case xlsx:
					case xlsm:
					case xls:
					case ods:
						if (file_name.endsWith(".xlsx") || file_name.endsWith(".xlsm") || file_name.endsWith(".xls") || file_name.endsWith(".ods")) {
							ex = init_extractor(arg_file, zip_file, file_header);
						}
						break;
					case html:
						if (file_name.endsWith(".html") || file_name.endsWith(".htm")) {
							ex = init_extractor(arg_file, zip_file, file_header);
						}
						break;
					case any:
					default:
						ex = init_extractor(arg_file, zip_file, file_header);
						break;
				}
			}
			
			if (ex==null) {
				System.out.println(err1);
				System.out.print("Cannot find file of type "+arg_type.toString());
				return;

			}
			
			//Extract
			ex.extract();
			
		} catch (ZipException e) {
			System.out.println(err1+" ZipException");
			System.out.println(e.getMessage()+" ("+e.getCode()+")");
		} catch (IOException e) {
			System.out.println(err1+" IOException");
			System.out.println(e.getMessage()+" (IOErr)");
		}
	}
	
	/**
	 * Init extractor according to into_outfile
	 * 
	 * @param arg_file
	 * @param zip_file
	 * @param file_header
	 * @return
	 */
	private Extractor init_extractor(File arg_file, ZipFile zip_file, FileHeader file_header) {
		if (this.into_outfile) {
			return new OutfileExtractor(arg_file, zip_file, file_header);
		} else {
			return new OutputExtractor(arg_file, zip_file, file_header);
		}
	}
}
