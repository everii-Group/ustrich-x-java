package com.untermstrich.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ooxml.POIXMLProperties.CoreProperties;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.untermstrich.modifiers.DocXmlParser;

public class Xlsxdocer extends Action {
	
	@SuppressWarnings("deprecation")
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
			
			//Set date/time
			GregorianCalendar date_greg = new GregorianCalendar();
			
			CoreProperties cp = workbook.getProperties().getCoreProperties();
			cp.setCreated(Optional.of(date_greg.getTime()));
			cp.setCreator("untermStrich software gmbh");
			
			/////////////////////////////////////////////////////////////////////////////////////
			
			//Get xml
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DocXmlParser handler = new DocXmlParser(workbook);
			
			/////////////////////////////////////////////////////////////////////////////////////
			
			//Process workbook
			saxParser.parse(arg_xml_file, handler);
			
			//Update formulas
			XSSFFormulaEvaluator eval = new XSSFFormulaEvaluator(workbook);
			String reply = "";
	        for(int i=0; i<workbook.getNumberOfSheets(); i++) {
	            Sheet sheet = workbook.getSheetAt(i);
	            for(Row r : sheet) {
	                for (Cell c : r) {
						try {
							eval.evaluateFormulaCellEnum(c);
						} catch (FormulaParseException e) {
							reply = "In '"+sheet.getSheetName()+"' cell "+c.getAddress();
							setReply(reply+": "+e.getLocalizedMessage());
							logger.warn(reply, e);
						} catch (RuntimeException e) {
							reply = "In '"+sheet.getSheetName()+"' cell "+c.getAddress();
							setReply(reply+": "+e.getLocalizedMessage());
							logger.warn(reply, e);
						}
					}
				}
			}

			//Close input file
			input_document.close();
			
			/////////////////////////////////////////////////////////////////////////////////////
			
			//Open output file
			FileOutputStream output_file = new FileOutputStream(arg_file_to);
			
			//Write output file
			workbook.write(output_file);
			
			//Close output file
			output_file.close();
			
			/////////////////////////////////////////////////////////////////////////////////////
			
			setReply("DONE");
			
//			//Open file
//			ProcessBuilder pb = new ProcessBuilder("open", arg_file_to.getAbsolutePath());
//			exec(pb);
			
		} catch (FileNotFoundException e) {
			setReply("Error file not found.");
			logger.error("Error:", e);
		} catch (IOException e) {
			setReply("IO Error.");
			logger.error("Error:", e);
		} catch (ParserConfigurationException e) {
			setReply("Error loading XML parser.");
			logger.error("Error:", e);
		} catch (SAXParseException e) {
			setReply("Error parsing and processing XML.");
			logger.error("Error:", e);
		} catch (SAXException e) {
			setReply("Error parsing XML.");
			logger.error("Error:", e);
		}
	}

//	/**
//	 * Execute a command
//	 * 
//	 * @param command
//	 * @return success?
//	 */
//	private static Boolean exec(ProcessBuilder pb)
//	{
//		try {
//			logger.error(pb.command());
//			Process p = pb.start();
//			p.waitFor();
//			if (p.exitValue()==0) {
//				return new Boolean(true);
//			} else {
//				return new Boolean(false);
//			}
//		} catch (Exception e) {
//			logger.error("Error:", e);
//			return new Boolean(false);
//		}
//	}
	
}
