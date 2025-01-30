package com.untermstrich.modifiers;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.untermstrich.enums.REP_FIELD_TYPES;
import com.untermstrich.enums.REP_PARSE_SPECIAL;
import com.untermstrich.exceptions.MalformedPresetException;
import com.untermstrich.exceptions.NoDataRowInPresetException;
import com.untermstrich.modofficefiles.logger.LoggerFactory;
import com.untermstrich.modofficefiles.logger.LoggerImplementation;
import com.untermstrich.simples.ParsedFilterRule;

public class ReportXmlParser extends DefaultHandler implements ReportParser {
	
	//Debug - Show details
	private static final LoggerImplementation logger = LoggerFactory.getLogger();
	
	boolean within_columns = false;
	boolean within_filters = false;
	boolean within_row = false;
	boolean within_textelement = false;
	String within_type = "";
	
	boolean found_row = false;
	
	String report_id = "";
	Map<String, ParsedFilterRule> report_filters = new HashMap<String, ParsedFilterRule>();
	
	StringBuffer element_content = null;
	Map<String, String> element_atts = new HashMap<String, String>();
	
	XlsxModifier modifier;
	REP_PARSE_SPECIAL special_mode = null;
	
	public ReportXmlParser(XlsxModifier modifier, XSSFWorkbook workbook) {
		this.modifier = modifier;
	}
	
	public ReportXmlParser(XlsxModifier modifier, XSSFWorkbook workbook, REP_PARSE_SPECIAL special_mode) {
		this.modifier = modifier;
		this.special_mode = special_mode;
	}
	
	public ReportXmlParser(XSSFWorkbook workbook) {
		this.modifier = new XlsxModifier(workbook);
	}
	
	public ReportXmlParser(XSSFWorkbook workbook, REP_PARSE_SPECIAL special_mode) {
		this.modifier = new XlsxModifier(workbook);
		this.special_mode = special_mode;
	}
	
	/**
	 * React on start of element 
	 * 
     * @param uri 		The Namespace URI
     * @param localName The local name
     * @param qName 	The qualified name
	 */
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		element_content = new StringBuffer();
		
		element_atts.put("id", atts.getValue("id"));
		element_atts.put("name", atts.getValue("name"));
		element_atts.put("title", atts.getValue("title"));
		element_atts.put("type", atts.getValue("type"));
		element_atts.put("usnumber", atts.getValue("usnumber"));
		element_atts.put("usdate", atts.getValue("usdate"));
		element_atts.put("xlsdate", atts.getValue("xlsdate"));
		
		//Start of title
		if (qName.equalsIgnoreCase("title")) {
			report_id = element_atts.get("id");
			return;
		}
		
		//Start of columns
		if (qName.equalsIgnoreCase("columns")) {
			within_columns = true;
			return;
		}
		
		//Start of filters
		if (qName.equalsIgnoreCase("filters")) {
			within_filters = true;
			return;
		}
		
		//Start of row
		if (qName.equalsIgnoreCase("row")) {
			logger.trace("-----------------------------------------------------");
			within_row = true;
			within_type = "";
			
			//Modifier - add next row
			modifier.prepare_next_row();
			
			return;
		}
		
		if (within_columns) {
			// Process columns header
			if (qName.equalsIgnoreCase("field") || qName.equalsIgnoreCase("addi:field")) {
				// Find data row
				String aname = element_atts.get("name");
				if (aname != null) {
					aname = prefix_for_special(aname);
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
						this.fatalError(new SAXParseException(e.getMessage(), null));
					}

				}
			}
		}

		if (within_row) {
			//Start of cell
			if (qName.equalsIgnoreCase("cell") || qName.equalsIgnoreCase("addi:cell")) {
				within_textelement = true;
			}
		}
		
		if (within_filters) {
			//Start of filter
			if (qName.equalsIgnoreCase("filter")) {
				within_textelement = true;
			}
		}
	}
	
	/**
	 * Get element content
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
	 */
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (within_textelement) {
			element_content.append(ch, start, length);			
		}
	}

	/**
	 * React on end of element 
	 * 
     * @param uri 		The Namespace URI
     * @param localName The local name
     * @param qName 	The qualified name
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		within_textelement = false;
		
		//End of columns
		if (qName.equalsIgnoreCase("columns")) {
			if (!found_row) {
				this.fatalError(new NoDataRowInPresetException());
			}
			
			within_columns = false;
			return;
		}
		
		//End of filters
		if (qName.equalsIgnoreCase("filters")) {
			try {
				String all_filters = new String();
				
				for ( String report_filter_name : report_filters.keySet()) {
					ParsedFilterRule report_filter = report_filters.get(report_filter_name);
					
					if (!report_filter_name.startsWith("print_")) {
						//Save to all filters list
						if (all_filters.length()!=0) {
							all_filters += "\n";
						}
						all_filters += report_filter.getTitle()+": "+report_filter.getValue();
					}
					
					//Set for certain filter
					modifier.replace("report_filter__"+report_filter_name, report_filter.getValue(), report_filter.getValue(), "string");
				}
			
				modifier.replace("report_filter", all_filters, all_filters, "string");
			} catch (ParseException e) {
				logger.error("Error:", e);
				this.fatalError(new SAXParseException(e.getMessage(), null));
			}
			return;
		}
		
		//End of filter
		if (qName.equalsIgnoreCase("filter")) {
			String aname = element_atts.get("name");
			String atitle = element_atts.get("title");
			
			report_filters.put(aname, new ParsedFilterRule(atitle, element_content.toString()));
			return;
		}
		
		//End of row
		if (qName.equalsIgnoreCase("row")) {
			within_row = false;
			return;
		}
		
		if (within_row) {
			if (qName.equalsIgnoreCase("cell") || qName.equalsIgnoreCase("addi:cell")) {
				//Set cell value
				String aname = element_atts.get("name");
				String atype = element_atts.get("type");
				
				logger.trace(aname+" = "+atype);
				
				//Get cell type
				REP_FIELD_TYPES ftype = null;
				try {
					ftype = REP_FIELD_TYPES.valueOf(atype);
				} catch (IllegalArgumentException e1) {
					//Ignore - Fallback to string
					ftype = REP_FIELD_TYPES.string;
				}
				
				//Create value and real value
				String value = element_content.toString();
				String real = value;
				String type = "text";
				switch (ftype) {
					case number:
					case number2:
					case currency:
						value = element_atts.get("usnumber");
						type = "number";
						break;
					case bool:
						break;
					case date:
						value = element_atts.get("usdate");
						real = element_atts.get("xlsdate");
						type = "xlsdate";
						break;
					case time:
						value = element_atts.get("usdate");
						real = element_atts.get("xlsdate");
						type = "xlsdate";
						break;
					case timespan:
						break;
					case string:
						type = "text";
						break;
				}
				
				if (this.special_mode!=null) {
					//Find type for special
					switch (this.special_mode) {
						case calcexp_externalexpenses:
						case calcexp_extra:
						case calcexp_times:
						case calcexp_travel:
							logger.trace(aname+": "+value);
							
							//Find type
							if (aname.equals("type_id")) {
								within_type = value;
							}
							if (within_type=="" && aname.equals("type_title")) {
								within_type = value;
							}
							logger.trace(within_type);
							break;
					}
					
					
					//Ensure type for special
					switch (this.special_mode) {
						case calcexp_externalexpenses:
							if (!within_type.equalsIgnoreCase("Kosten") && !within_type.contains("externalexpenses")) {
								return;
							}
							break;
						case calcexp_extra:
							if (!within_type.equalsIgnoreCase("Nebenkosten") && !within_type.contains("xtra")) {
								return;
							}
							break;
						case calcexp_times:
							if (!within_type.equalsIgnoreCase("Zeiten") && !within_type.contains("times")) {
								return;
							}
							break;
						case calcexp_travel:
							if (!within_type.equalsIgnoreCase("Reisekosten") && !within_type.contains("travel")) {
								return;
							}
							break;
					}
				}
				logger.trace("  - Found: "+aname+" = "+value);

				//Modifier - set for cell
				try {
					if (aname.equalsIgnoreCase("date")) {
						aname = prefix_for_special(aname);
						modifier.replace_in_active_row_date(aname, value, real, type);
					} else {
						aname = prefix_for_special(aname);
						logger.trace(aname+", "+value+", "+real+", "+type);
						modifier.replace_in_active_row(aname, value, real, type);
					}
				} catch (ParseException e) {
					this.fatalError(new SAXParseException(e.getMessage(), null));
				}

			}
		}
	}

	/**
	 * React on end of element
	 * 
	 * @see org.xml.sax.helpers.DefaultHandler#endDocument()
	 */
	@Override
	public void endDocument() throws SAXException {
		modifier.remove_last_row_if_not_used();
		modifier.increase_active_cells();
	}
	
	/**
	 * Prefix for special
	 * 
	 * @param 	aname
	 * @return
	 */
	private String prefix_for_special(String aname) {
		if (this.special_mode==null) {
			return aname;
		}
		
		switch (this.special_mode) {
			case calcexp_externalexpenses:
				return "externalexpenses__"+aname;
			case calcexp_extra:
				return "extra__"+aname;
			case calcexp_times:
				return "times__"+aname;
			case calcexp_travel:
				return "travel__"+aname;
		}
		return aname;
	}

	/* (non-Javadoc)
	 * @see com.untermstrich.modifiers.ReportParser#getReport_id()
	 */
	public String getReport_id() {
		return report_id;
	}
	
	/* (non-Javadoc)
	 * @see com.untermstrich.modifiers.ReportParser#getMin_date()
	 */
	public Date getMin_date() {
		return modifier.getMin_date();
	}

	/* (non-Javadoc)
	 * @see com.untermstrich.modifiers.ReportParser#getMax_date()
	 */
	public Date getMax_date() {
		return modifier.getMax_date();
	}

	/* (non-Javadoc)
	 * @see com.untermstrich.modifiers.ReportParser#getModifier()
	 */
	public XlsxModifier getModifier() {
		return modifier;
	}
	
}
