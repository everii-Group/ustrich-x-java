package com.untermstrich.ziphelper.extractors;

import java.io.File;
import java.io.IOException;




import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

public class OutputExtractor extends Extractor {

	public OutputExtractor(File arg_file, ZipFile zip_file, FileHeader file_header) {
		super(zip_file, file_header);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void extract() throws ZipException, IOException {
		extract_out();
	}

}
