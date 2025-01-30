package com.untermstrich.modifiers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.SAXParseException;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.untermstrich.exceptions.MalformedPresetException;
import com.untermstrich.exceptions.NoDataRowInPresetException;
import com.untermstrich.modofficefiles.logger.LoggerFactory;
import com.untermstrich.modofficefiles.logger.LoggerImplementation;

public class ReportCsvParser implements ReportParser {

	private static final LoggerImplementation logger = LoggerFactory.getLogger();
	
	private XlsxModifier modifier;
	private boolean found_row = false;
	private String report_id = "";

	public ReportCsvParser(XSSFWorkbook workbook) {
		this.modifier = new XlsxModifier(workbook);
	}
	
	public void process(File arg_xml_file) throws IOException, SAXParseException {
		//Get info from json file
		BufferedReader buffered_reader = new BufferedReader(new FileReader(arg_xml_file.getAbsolutePath()+".info"));
		JsonObject info_object = Json.parse(buffered_reader).asObject();
		report_id = info_object.getString("report_id", null);
		JsonObject info_field_types = info_object.get("field_types").asObject();
		JsonArray info_filter_infos = info_object.get("filter_infos").asArray();
		buffered_reader.close();
		
		
		//Get CSV
		CSVParser parser = CSVParser.parse(arg_xml_file, Charset.forName("UTF-8"), CSVFormat.TDF.withHeader());
		Map<String, Integer> header = parser.getHeaderMap();
		
		//Find by header
		for (Entry<String, Integer> entry : header.entrySet()) {
			String aname = entry.getKey();
			if (aname.isEmpty()) {
				continue;
			}
			
			try {
				// Search for name
				if (!found_row) {
					// Modifier - find data row, use first field found
					found_row = modifier.find_active_sheet_and_row(aname);
				} else {
					// Modifier - find data row, validate same row
					modifier.find_active_sheet_and_row(aname);
				}
			} catch (MalformedPresetException e) {
				throw new SAXParseException(e.getMessage(), null);
			}
		}
		
		if (!found_row) {
			throw new NoDataRowInPresetException();
		}
		logger.trace("---------------------------------------------------");
		
		for (CSVRecord csvRecord : parser) {
			//Modifier - add next row
			modifier.prepare_next_row();
			
			for (Entry<String, String> entry : csvRecord.toMap().entrySet()) {
				String aname = entry.getKey();
				if (aname.isEmpty()) {
					continue;
				}
				String value = entry.getValue();
				String real = value;
				String type = info_field_types.getString(aname, "number");
				try {
					logger.trace(aname);
					modifier.replace_in_active_row(aname, value, real, type);
				} catch (ParseException e) {
					throw new SAXParseException(e.getMessage(), null);
				}
			}
		}
		
		try {
			String all_filters = new String();
			for ( JsonValue info_filter_info_value : info_filter_infos) {
				JsonObject info_filter_info = info_filter_info_value.asObject();
				String value = info_filter_info.getString("value", null);
				String title = info_filter_info.getString("title", null);
				String info_filter_id = info_filter_info.getString("id", null);
				
				if (!info_filter_id.startsWith("print_")) {
					//Save to all filters list
					if (all_filters.length()!=0) {
						all_filters += "\n";
					}
					
					all_filters += title+": "+value;
				}
				
				//Set for certain filter
				modifier.replace("report_filter__"+info_filter_info.getString("id", null), value, value, "string");
			}
			modifier.replace("report_filter", all_filters, all_filters, "string");
		} catch (ParseException e) {
			throw new SAXParseException(e.getMessage(), null);
		}

		modifier.remove_last_row_if_not_used();
		modifier.increase_active_cells();
	}
	
	public String getReport_id() {
		return report_id;
	}

	public Date getMin_date() {
		return null;
	}

	public Date getMax_date() {
		return null;
	}

	public XlsxModifier getModifier() {
		return modifier;
	}

}
