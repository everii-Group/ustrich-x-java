package com.untermstrich.actions;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.GeneralSecurityException;

import org.apache.poi.poifs.crypt.Decryptor;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class Decfiler extends Action {
	
	public void run() {
		// Check for password
		if (arg_password == null || arg_password.length() < 1) {
			setReply("Argument - password missing.");
			return;
		}
		
		//The documents
		InputStream input_document = null;
		OutputStream output_document = null;

		try {
			//Open file and decrypt
			FileInputStream input_file = new FileInputStream(arg_file_from);

			POIFSFileSystem input_poifs_file = new POIFSFileSystem(input_file);
			EncryptionInfo info = new EncryptionInfo(input_poifs_file);

			Decryptor d = Decryptor.getInstance(info);
			if (!d.verifyPassword(arg_password)) {
				setReply("Unable to process: document is encrypted with other password");
				return;
			}

			// Get input stream
			input_document = d.getDataStream(input_poifs_file);

			// Get output stream
			output_document = new FileOutputStream(arg_file_to);

			// Copy to output
			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = input_document.read(bytes)) != -1) {
				output_document.write(bytes, 0, read);
			}
			
			setReply("DONE");

		} catch (FileNotFoundException e) {
			setReply("File not found.");
			logger.error("Error:", e);
		} catch (IOException e) {
			setReply("IO error.");
			logger.error("Error:", e);
		} catch (GeneralSecurityException e) {
			setReply("Decryption error.");
			logger.error("Error:", e);
		} finally {
			//Close files
			if (input_document != null) {
				try {
					input_document.close();
				} catch (IOException e) {
					logger.error("Error:", e);
				}
			}
			if (output_document != null) {
				try {
					output_document.close();
				} catch (IOException e) {
					logger.error("Error:", e);
				}

			}
		}
	}

}
