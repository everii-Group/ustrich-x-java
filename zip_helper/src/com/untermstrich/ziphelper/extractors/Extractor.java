package com.untermstrich.ziphelper.extractors;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.io.ZipInputStream;
import net.lingala.zip4j.model.FileHeader;

public abstract class Extractor {
	
	ZipFile zip_file;
	FileHeader file_header;
	
	public Extractor(ZipFile zip_file, FileHeader file_header) {
		this.zip_file = zip_file;
		this.file_header = file_header;
	}
	
	/**
	 * Extract to system out
	 * 
	 * @throws ZipException
	 * @throws IOException
	 */
	protected void extract_out() throws ZipException, IOException {
		//Open input stream
		ZipInputStream is = zip_file.getInputStream(file_header);
		
		int readLen = -1;
		byte[] buff = new byte[4096];
		while ((readLen = is.read(buff)) != -1) {
			System.out.write(buff, 0, readLen);
		}
		
		//Close input stream
		if (is != null) {
			is.close();
			is = null;
		}
	}
	
	/**
	 * Extract to out file
	 * 
	 * @param out_file
	 * @throws ZipException
	 * @throws IOException
	 */
	protected void extract_out(File out_file) throws ZipException, IOException {
		//Open input stream
		ZipInputStream is = zip_file.getInputStream(file_header);
		
		//Open output stream
		FileOutputStream os = new FileOutputStream(out_file);
		
		int readLen = -1;
		byte[] buff = new byte[4096];
		while ((readLen = is.read(buff)) != -1) {
			os.write(buff, 0, readLen);
		}
		
		//Close output stream
		if (os != null) {
			os.close();
			os = null;
		}
		
		//Close input stream
		if (is != null) {
			is.close();
			is = null;
		}
	}

	abstract public void extract() throws ZipException, IOException;

}
