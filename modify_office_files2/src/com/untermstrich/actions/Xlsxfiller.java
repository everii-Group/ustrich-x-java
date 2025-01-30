package com.untermstrich.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

import com.untermstrich.enums.REP_PARSE_SPECIAL;
import com.untermstrich.exceptions.NoDataRowInPresetException;
import com.untermstrich.modifiers.ReportCsvParser;
import com.untermstrich.modifiers.ReportParser;
import com.untermstrich.modifiers.ReportXmlParser;
import com.untermstrich.modifiers.XlsxModifier;

public class Xlsxfiller extends Action {
	
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
			boolean have_any_data_row = false;
			ReportParser handler = null;
			Date min_date = null;
			Date max_date = null;
			XlsxModifier modifier = new XlsxModifier(workbook);
			
			switch (this.file_mode) {
				case csv:
					handler = new ReportCsvParser(workbook);
					((ReportCsvParser) handler).process(arg_xml_file);
					have_any_data_row = true;
					
					logger.trace("CSV ID: "+handler.getReport_id());
					break;
				case xml:
					//Get xml
					SAXParserFactory factory = SAXParserFactory.newInstance();
					SAXParser saxParser = factory.newSAXParser();
					
					//Update basic fields
					modifier.reset_new_start_row();
					handler = new ReportXmlParser(modifier, workbook);
					try {
						saxParser.parse(arg_xml_file, (ReportXmlParser)handler);
						have_any_data_row = true;
					} catch (NoDataRowInPresetException e) {}
			
					min_date = handler.getMin_date();
					max_date = handler.getMax_date();
					
					//Update report specific versions
					String report_id = handler.getReport_id();
					ArrayList<REP_PARSE_SPECIAL> specific_process = new ArrayList<REP_PARSE_SPECIAL>();
					if (report_id.startsWith("calcexp_") && report_id.endsWith("_merged")) {
						specific_process.add(REP_PARSE_SPECIAL.calcexp_times);
						specific_process.add(REP_PARSE_SPECIAL.calcexp_travel);
						specific_process.add(REP_PARSE_SPECIAL.calcexp_extra);
						specific_process.add(REP_PARSE_SPECIAL.calcexp_externalexpenses);
					}
					
					for (REP_PARSE_SPECIAL specific_item : specific_process) {
						modifier.reset_new_start_row();
						handler = new ReportXmlParser(modifier, workbook, specific_item);
						try {
							saxParser.parse(arg_xml_file, (ReportXmlParser)handler);
							have_any_data_row = true;
							
							Date this_min_date = handler.getMin_date();
							Date this_max_date = handler.getMax_date();
							
							if (this_min_date!=null && this_max_date!=null) {
								if (min_date==null || min_date.after(this_min_date)) {
									min_date = this_min_date;
								}
								if (max_date==null || max_date.before(this_max_date)) {
									max_date = this_max_date;
								}
							}
							
						} catch (NoDataRowInPresetException e) {}
					}
					break;
			}
			
			if (!have_any_data_row) {
				throw new NoDataRowInPresetException();
			}
			
			//Add report_min_date and report_max_date
			if (min_date!=null && max_date!=null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				modifier.replace("report_min_date", dateFormat.format(min_date), dateFormat.format(min_date), "javadate");
				modifier.replace("report_max_date", dateFormat.format(max_date), dateFormat.format(max_date), "javadate");
			}
			
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
		} catch (ParseException e) {
			setReply("Error processing XLSX.");
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
