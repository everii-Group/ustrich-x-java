package com.untermstrich.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.io.FileUtils;
import org.apache.poi.xssf.extractor.XSSFExcelExtractor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class Xlsxextractor extends Action {
	
	public void run() {
		try {
			if (!arg_file_from.getCanonicalPath().equalsIgnoreCase(arg_file_to.getCanonicalPath())) {
				FileUtils.copyFile(arg_file_from, arg_file_to);
			}
			
			//Read input file
			FileInputStream input_document = new FileInputStream(arg_file_to);
			
			//Get POI representation
			XSSFWorkbook workbook = new XSSFWorkbook(input_document);
			
			/////////////////////////////////////////////////////////////////////////////////////
			
			XSSFExcelExtractor extractor = new XSSFExcelExtractor(workbook);
			extractor.setFormulasNotResults(false);
			extractor.setIncludeSheetNames(true);
			extractor.setIncludeCellComments(false);
			extractor.setIncludeHeadersFooters(false);
			
			/////////////////////////////////////////////////////////////////////////////////////
			
			//Open output file
			PrintWriter output_file = new PrintWriter(arg_file_to);
			
			//Write text to file
			output_file.print(extractor.getText().replace("null\t", ""));
			
			output_file.close();
			
			/////////////////////////////////////////////////////////////////////////////////////
			
			extractor.close();
			
			/////////////////////////////////////////////////////////////////////////////////////
			
			//Close input file
			input_document.close();
			
			setReply("DONE");
			
		} catch (FileNotFoundException e) {
			setReply("Error file not found.");
			logger.error("Error:", e);
		} catch (IOException e) {
			setReply("IO Error.");
			logger.error("Error:", e);
		}
	}
	
	/**
	 * Define max runtime
	 * 
	 * @return seconds
	 */
	public final long max_runtime()
	{
		return 30;
	}

}
