package com.untermstrich.exceptions;

import org.xml.sax.SAXParseException;

public class NoDataRowInPresetException extends SAXParseException {

	private static final long serialVersionUID = -7891614158851511417L;

	public NoDataRowInPresetException() {
		super("Cannot find named row in file", null);
	}

}
