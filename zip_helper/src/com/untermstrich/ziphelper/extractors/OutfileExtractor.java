package com.untermstrich.ziphelper.extractors;

import java.io.File;
import java.io.IOException;






import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class OutfileExtractor extends Extractor {
	
	File out_file;

	public OutfileExtractor(File arg_file, ZipFile zip_file, FileHeader file_header) {
		super(zip_file, file_header);
		
		String out_file_name = arg_file.getAbsolutePath()+".out";
		out_file = new File(out_file_name);
	}

	@Override
	public void extract() throws ZipException, IOException {
		extract_out(out_file);
		System.out.println("ZIP FILE EXTRACTED");
	}

}
