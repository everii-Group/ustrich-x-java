package com.untermstrich.modifiers;

import java.text.ParseException;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class DocXmlParser extends DefaultHandler {
	
	XlsxModifier modifier;
	
	public DocXmlParser(XSSFWorkbook workbook) {
		this.modifier = new XlsxModifier(workbook);
	}
	
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
		//If not item, nothing to do for me
		if (qName.indexOf("item")!=0) {
			return;
		}
		
//		private static final LoggerImplementation logger = LoggerFactory.getLogger();
//		logger.info("qName: " + qName);
//		for (int i = 0; i < atts.getLength(); i++) {
//			logger.printf(Level.INFO, "Attribut no. %d: %s = %s%n", i, atts.getQName(i), atts.getValue(i));
//		}
		
		String classname = atts.getValue("classname");
		String text = atts.getValue("text");
		String real = atts.getValue("real");
		String type = atts.getValue("type");
		
		if (classname==null) {
			this.fatalError(new SAXParseException("Attribute 'classname' missing for 'item' tag.", null));
		}
		if (text==null) {
			this.fatalError(new SAXParseException("Attribute 'text' missing for 'item' tag.", null));
		}
		if (real==null) {
			this.fatalError(new SAXParseException("Attribute 'real' missing for 'item' tag.", null));
		}
		if (type==null) {
			this.fatalError(new SAXParseException("Attribute 'type' missing for 'item' tag.", null));
		}
		
		try {
			modifier.replace(classname, text, real, type);
		} catch (IndexOutOfBoundsException e) {
			this.fatalError(new SAXParseException(e.getMessage(), null));
		} catch (ParseException e) {
			this.fatalError(new SAXParseException(e.getMessage(), null));
		}
	}
	
	/**
	 * @return The found worksheet or null
	 */
	public XSSFSheet getFound_worksheet() {
		return this.modifier.getFound_worksheet();
	}

}
