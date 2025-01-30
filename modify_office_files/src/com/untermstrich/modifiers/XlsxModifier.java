package com.untermstrich.modifiers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.SpreadsheetVersion;
import org.apache.poi.ss.formula.FormulaParser;
import org.apache.poi.ss.formula.FormulaRenderer;
import org.apache.poi.ss.formula.FormulaType;
import org.apache.poi.ss.formula.ptg.Ptg;
import org.apache.poi.ss.formula.ptg.RefPtgBase;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.util.AreaReference;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFEvaluationWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import ModOfficeFiles.LoggerFactory;
import ModOfficeFiles.LoggerImplementation;

import com.untermstrich.exceptions.MalformedPresetException;

public class XlsxModifier {

	//Debug - Show details
	private static final LoggerImplementation logger = LoggerFactory.getLogger();
	
	XSSFWorkbook workbook = null;
	XSSFEvaluationWorkbook fworkbook = null;
	
	XSSFSheet found_worksheet = null;
	int found_row = 0;
	int current_row = 0;
	Map<String, Integer> found_cells = new HashMap<String, Integer>();
	
	boolean current_row_was_used = false;
	
	Date min_date = null;
	Date max_date = null;
	
	/**
	 * Init 
	 * 
	 * @param workbook
	 */
	public XlsxModifier(XSSFWorkbook workbook) {
		this.workbook = workbook;
		this.fworkbook = XSSFEvaluationWorkbook.create(workbook);
	}
	
	/**
	 * Replace field (name) with value
	 * 
	 * @param 	name		Field name
	 * @param 	value		Value
	 * @param 	type		Field type
	 * @param 	real 		Real value
	 * @return	Success
	 * @throws ParseException 
	 */
	public boolean replace(String name, String value, String real, String type) throws ParseException {
		Name named_cell = workbook.getName(name);
		
		if (named_cell == null || named_cell.isDeleted()) {
			return false;
		}
		
		// Retrieve the cell at the named range
		boolean changed_cell = false;
		AreaReference named_aref = new AreaReference(named_cell.getRefersToFormula(), SpreadsheetVersion.EXCEL97);
	    CellReference[] named_crefs = named_aref.getAllReferencedCells();
	    
	    if (named_crefs.length>=1) {
	    	changed_cell = true;
	    	XSSFSheet sheet = workbook.getSheet(named_crefs[0].getSheetName());
	    	
	    	//Catch empty field
	    	if (sheet==null) {
	    		return false;
	    	}
	    	
	    	//Get the row
	    	Row row = sheet.getRow(named_crefs[0].getRow());
	    	if (row==null) {
	    		//If there is no row, create it first
	    		sheet.createRow(named_crefs[0].getRow());
	    		
	    		row = sheet.getRow(named_crefs[0].getRow());
	    		if (row==null) {
	    			//Just in case
	    			throw new IndexOutOfBoundsException("Cannot get cell(row) for "+name+". Please add any kind of content (eg a #) to the cell in the template.");
	    		}
	    	}
	    	
	    	//Get the cell. If there is none, create it as blank
	    	Cell cell = row.getCell(named_crefs[0].getCol(), MissingCellPolicy.CREATE_NULL_AS_BLANK);
	    	if (cell==null) {
	    		//Just in case
	    		throw new IndexOutOfBoundsException("Cannot get cell for "+name+". Please add any kind of content (eg a #) to the cell in the template.");
	    	}
	    	replace_in_cell(cell, value, real, type, false);
	    }
		return changed_cell;
	}
	
	/**
	 * Replace field (name) with value
	 * 
	 * @param 	name		Field name
	 * @param 	value		Value
	 * @param 	type		Field type
	 * @param 	real 		Real value
	 * @return	Success
	 * @throws ParseException 
	 */
	public boolean replace_in_active_row_date(String name, String value, String real, String type) throws ParseException {
		return replace_in_active_row(name, value, real, type, true);
	}
	
	/**
	 * Replace field (name) with value
	 * 
	 * @param 	name		Field name
	 * @param 	value		Value
	 * @param 	type		Field type
	 * @param 	real 		Real value
	 * @return	Success
	 * @throws ParseException 
	 */
	public boolean replace_in_active_row(String name, String value, String real, String type) throws ParseException {
		return replace_in_active_row(name, value, real, type, false);
	}

	
	/**
	 * Replace field (name) with value
	 * 
	 * @param 	name		Field name
	 * @param 	value		Value
	 * @param 	type		Field type
	 * @param 	real 		Real value
	 * @param	boolean		Is date
	 * @return	Success
	 * @throws ParseException 
	 */
	private boolean replace_in_active_row(String name, String value, String real, String type, boolean is_date) throws ParseException {
		if (!found_cells.containsKey(name)) {
			return false;
		}
		
		current_row_was_used = true;
		
    	Row row = found_worksheet.getRow(current_row);
    	Cell cell = row.getCell(found_cells.get(name));
		
    	if (cell==null) {
    		//Just in case
    		throw new IndexOutOfBoundsException("Cannot get cell for "+name+". Please add any kind of content (eg a #) to the cell in the template.");
    	}
		replace_in_cell(cell, value, real, type, is_date);
		
		return true;
	}
	
	/**
	 * Replace value in cell
	 * 
	 * @param cell
	 * @param value
	 * @param real
	 * @param type
	 * @param is_date
	 * @throws ParseException
	 */
	private void replace_in_cell(Cell cell, String value, String real, String type, boolean is_date) throws ParseException {
    	CellStyle cell_style = cell.getCellStyle();
    	String cell_format = cell_style.getDataFormatString().toUpperCase();
    	
    	String cell_type = "string";
    	if (cell_format.equalsIgnoreCase(("GENERAL"))) {
    		cell_type = "string";
    	} else if (cell_format.contains("DD") || cell_format.contains("MM") || cell_format.contains("TT") || cell_format.contains("YY") || cell_format.contains("JJ")) {
    		cell_type = "date";
    	} else if (cell_format.contains("0")) {
    		cell_type = "number";
    	}
    	
    	Date real_value_date = null;
    	if (is_date || cell_type.equals("date")) {
    		SimpleDateFormat sdfToDate = null;
    		if (type.equals("xlsdate")) {
    			sdfToDate = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );
    		} else if (type.equals("javadate")) {
        			sdfToDate = new SimpleDateFormat( "yyyy-MM-dd" );
    		} else if (type.equals("date")) {
    			sdfToDate = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );
    		}
    		
    		if (sdfToDate!=null && !real.isEmpty()) {
    			//If we have a date formatter and have a non empty string, get real value date
        		real_value_date = sdfToDate.parse(real);
        		
        		if (is_date) {
        			if (min_date==null || min_date.after(real_value_date)) {
        				min_date = real_value_date;
        			}
        			if (max_date==null || max_date.before(real_value_date)) {
        				max_date = real_value_date;
        			}
        		}
    		}
    	}
    	
    	if (real_value_date!=null && (type.equals("date") || type.equals("javadate") || type.equals("xlsdate"))) {
    		cell.setCellValue(real_value_date);
    	} else if (type.equals("number") && cell_type.equals("number")) {
    		Double real_value_double = new Double(0);
    		try {
    			real_value_double = Double.parseDouble(real);
			} catch (Exception e) {
				//Ignore
			}
    		cell.setCellValue(real_value_double);	    		
    	} else {
    		cell.setCellValue(value);
    	}		
	}
	
	/**
	 * Get active sheet and row based on name
	 * 
	 * @param 	name		Field name
	 * @return	Found
	 * @throws Exception 
	 */
	public boolean find_active_sheet_and_row(String name) throws MalformedPresetException {
		//Find named cell
		Name named_cell = workbook.getName(name);
		
		if (named_cell == null || named_cell.isDeleted()) {
			return false;
		}
		
		//Retrieve the cell at the named range
		AreaReference named_aref = new AreaReference(named_cell.getRefersToFormula(), SpreadsheetVersion.EXCEL97);
	    CellReference[] named_crefs = named_aref.getAllReferencedCells();
	    
	    if (named_crefs.length<1) {
	    	return false;
	    }
	    
	    //Get sheet and row
	    XSSFSheet this_found_worksheet = workbook.getSheet(named_crefs[0].getSheetName());
	    int this_found_row = named_crefs[0].getRow();
	    
	    //Catch empty field
	    if (this_found_worksheet==null) {
	    	return false;
	    }
	    
	    found_cells.put(name, Integer.valueOf(named_crefs[0].getCol()) );
	    
	    if (found_row==0 && found_worksheet==null) {
	    	//First finding
	    	found_worksheet = this_found_worksheet;
	    	found_row = this_found_row;
	    } else {
	    	//Validate further findings
	    	if (!found_worksheet.getSheetName().equals(this_found_worksheet.getSheetName())) {
	    		throw new MalformedPresetException(
    				"The field '"+name+"' is not in the same sheet as the other fields. \n"+
    				"Ensure that all fields are in the same row an the same sheet. \n"+
    				"("+found_worksheet.getSheetName()+" != "+this_found_worksheet.getSheetName()+")"
				);
	    	}
	    	if (found_row!=this_found_row) {
	    		throw new MalformedPresetException(
    				"The field '"+name+"' is not in the same row as the other fields. \n"+
    				"Ensure that all fields are in the same row an the same sheet. \n"+
    				"("+found_row+" != "+this_found_row+")"
				);
	    	}
	    }
	    
		return true;
	}
	
	/**
	 * Increase size of active cells to all rows
	 */
	public void increase_active_cells() {
		//Process found fields
		logger.trace("increase_active_cells");
		for ( String found_cell_name : found_cells.keySet()) {
			
			CellReference new_from_ref = new CellReference(found_worksheet.getSheetName(), found_row, found_cells.get(found_cell_name), true, true);
			CellReference new_to_ref = new CellReference(found_worksheet.getSheetName(), current_row, found_cells.get(found_cell_name), true, true);
			AreaReference new_ref = new AreaReference(new_from_ref, new_to_ref);
			
			Name named_cell = workbook.getName(found_cell_name);
			named_cell.setRefersToFormula(new_ref.formatAsString());
			
			logger.trace(found_cell_name+": "+new_ref.formatAsString());
		}
		
		//Process other fields in row
		int names_count = workbook.getNumberOfNames();
		for (int i = 0; i<names_count; i++) {
			@SuppressWarnings("deprecation")
			Name named_cell = workbook.getNameAt(i);
			logger.trace(named_cell.getNameName()+": (named_cell) ");
			
			//Faulty refernces
			if (named_cell == null || named_cell.isDeleted()) {
				continue;
			}
			
			//Only process single cells
		    @SuppressWarnings("deprecation")
			AreaReference named_aref = new AreaReference(named_cell.getRefersToFormula());
		    if (!named_aref.isSingleCell()) {
		    	logger.trace("Not single");
		    	continue;
		    }
		    
		    CellReference named_cref = named_aref.getFirstCell();
		    
		    //Faulty cell
		    if (named_cref == null) {
		    	logger.trace("No reference");
		    	continue;
		    }
		    
		    //Only same sheet as found
		    if (!found_worksheet.getSheetName().equals(named_cref.getSheetName())) {
		    	continue;
		    }
		    
		    //Only same row as found
		    if (named_cref.getRow() != found_row) {
		    	logger.trace("Not same row");
		    	continue;
		    }
		    
			CellReference new_from_ref = new CellReference(found_worksheet.getSheetName(), found_row, named_cref.getCol(), true, true);
			CellReference new_to_ref = new CellReference(found_worksheet.getSheetName(), current_row, named_cref.getCol(), true, true);
			AreaReference new_ref = new AreaReference(new_from_ref, new_to_ref);
		    named_cell.setRefersToFormula(new_ref.formatAsString());
		    
		    logger.trace(new_ref.formatAsString());
		}
	}
	
	/**
	 * Prepare next row (According to current_row)
	 */
	@SuppressWarnings("deprecation")
	public void prepare_next_row() {
		logger.trace("Prepare next row: ---------------------------");
		logger.trace(found_worksheet.getSheetName()+" -- ");
		if (current_row==0) {
			current_row = found_row;
			logger.trace("Row 0");
			return;
		}
		
		if (!current_row_was_used) {
			logger.trace("Stay in same row");
			return;
		} else {
			current_row_was_used = false;
		}
		
		if (current_row!=0) {
			current_row++;
		}
		
		
		//Copy the last row
		XSSFRow from_row = found_worksheet.getRow(current_row-1);
		XSSFRow to_row = found_worksheet.getRow(current_row);
		
		//Shift rows down
		if (to_row!=null) {
			found_worksheet.shiftRows(current_row, found_worksheet.getLastRowNum(), 1);
		}
		
		//Ensure we have a to row
		logger.trace("Row "+current_row+" ---------------------------");
		to_row = found_worksheet.createRow(current_row);
		
		for (int i = 0; i < from_row.getLastCellNum(); i++) {
			logger.trace("to "+current_row+":"+i+", ");
			
			Cell from_cell = from_row.getCell(i);
			Cell to_cell = to_row.getCell(i);
			
			//If no from cell, no to cell as well
			if (from_cell==null) {
				to_cell = null;
				continue;
			}
			
			//Ensure we have a to cell
			if (to_cell==null) {
				to_cell = to_row.createCell(i);
			}
			
			//Copy style
			CellStyle from_style = from_cell.getCellStyle();
			if (from_style!=null) {
				to_cell.setCellStyle(from_style);
			}
			
			//Copy type
			to_cell.setCellType(from_cell.getCellType());
			
            //Set the value
			switch (from_cell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                	to_cell.setCellValue(from_cell.getStringCellValue());
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                	to_cell.setCellValue(from_cell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_FORMULA:
                	String formula = from_cell.getCellFormula();
                	
                	//Pares formula
                	Ptg[] ptgs = FormulaParser.parse(formula, fworkbook, FormulaType.CELL, 0);
                	
                	//Re-calculate cell references
                	for( Ptg ptg  : ptgs ) {
                		if(ptg instanceof RefPtgBase) {
                			//base class for cell reference "things"
                			RefPtgBase ref = (RefPtgBase)ptg;
                			if(ref.isColRelative()) {
                				ref.setColumn( ref.getColumn() + 0 );
                			}
                			if( ref.isRowRelative() ) {
                				ref.setRow( ref.getRow() + 1 );
                			}
                				
                		}
                	}
                	
                	//Parse to string
                	formula = FormulaRenderer.toFormulaString(fworkbook, ptgs);
                	
                	to_cell.setCellFormula(formula);
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                	to_cell.setCellValue(from_cell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_STRING:
                	to_cell.setCellValue(from_cell.getRichStringCellValue());
                    break;
            }
		}
		
		logger.trace("Done");
	}

	/**
	 * Remove the last row, if it was not used
	 */
	public void remove_last_row_if_not_used() {
		if (current_row_was_used) {
			return;
		}
		
		if (found_row==current_row) {
			return;
		}
		
		Row row = found_worksheet.getRow(current_row);
		found_worksheet.removeRow(row);
		
		found_worksheet.shiftRows(current_row+1, found_worksheet.getLastRowNum(), -1);
		
		current_row--;
	}

	/**
	 * @return The min date
	 */
	public Date getMin_date() {
		return min_date;
	}

	/**
	 * @return The max date
	 */
	public Date getMax_date() {
		return max_date;
	}
	
	/**
	 * @return The found worksheet or null
	 */
	public XSSFSheet getFound_worksheet() {
		return found_worksheet;
	}
	
}
