package com.untermstrich.ziphelper.actions;

import java.io.File;
import com.untermstrich.ziphelper.FILE_TYPES;

public abstract class Action {
	
	public abstract void run(File arg_file, String arg_password, String arg_type);

	public static boolean typeContains(String test) {
	    for (FILE_TYPES c : FILE_TYPES.values()) {
	        if (c.name().equalsIgnoreCase(test)) {
	            return true;
	        }
	    }
	    return false;
	}
	
}
