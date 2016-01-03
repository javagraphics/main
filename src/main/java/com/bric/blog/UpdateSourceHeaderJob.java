/*
 * @(#)UpdateSourceHeaderJob.java
 *
 * $Date: 2014-11-27 07:50:51 +0100 (Do, 27 Nov 2014) $
 *
 * Copyright (c) 2014 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.blog;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.bric.io.IOUtils;

public class UpdateSourceHeaderJob extends FormatSourceCodeJob {

	public UpdateSourceHeaderJob(WorkspaceContext context) {
		super(context);
		setName("Update Headers");
		setDescription("Updating Headers...");
	}
	
	@Override
	public boolean formatFile(File javaFile) throws IOException {
		if(javaFile.isDirectory()) {
			throw new IllegalArgumentException(javaFile.getAbsolutePath());
		}
		String name = javaFile.getName().toLowerCase();
		if(!name.endsWith("java"))
			throw new IllegalArgumentException(javaFile.getAbsolutePath());
		
		String[] lines = IOUtils.readLines(javaFile, -1);
		
		int firstLine = -1;
		for(int a = 0; firstLine==-1 && a<lines.length; a++) {
			String trimmed = lines[a].trim();
			if(trimmed.startsWith("package com.bric") || trimmed.startsWith("//")) {
				firstLine = a;
			} else if(trimmed.startsWith("package ")) {
				//did it start with a package other than "com.bric"? Then it's
				//not one of ours and we shouldn't modify it here:
				return false;
			}
		}
			
		if(firstLine==-1) {
			//not sure what this means, but abort:
			return false;
		}

		boolean changed = false;

		//check the source code header:
		String dateLine = getDateLine(lines);
		if(dateLine==null) {
			String[] newHeader = getHeader(javaFile.getName());
			String[] newArray = new String[newHeader.length + lines.length - firstLine];
			System.arraycopy(newHeader, 0, newArray, 0, newHeader.length);
			System.arraycopy(lines, firstLine, newArray, newHeader.length, lines.length - firstLine);
			
			IOUtils.writeLines(javaFile, newArray, true);
			
			changed = true;
		}
		return changed;
	}
	
	/** Returns the line in a file that begins with " * $Date" */
	private static String getDateLine(String[] lines) throws IOException {
		for(String line : lines) {
			String trim = line.trim();
			if(trim.startsWith("* $Date")) {
				return line;
			}
		}
		return null;
	}
	
	static int year = (new GregorianCalendar()).get(Calendar.YEAR);
	private static String[] getHeader(String fileName) {
		return new String[] {
				"/*",
				" * @(#)"+fileName,
				" *",
				" * $Da"+"te$",
				" *",
				" * Copyright (c) "+year+" by Jeremy Wood.",
				" * All rights reserved.",
				" *",
				" * The copyright of this software is owned by Jeremy Wood. ",
				" * You may not use, copy or modify this software, except in  ",
				" * accordance with the license agreement you entered into with  ",
				" * Jeremy Wood. For details see accompanying license terms.",
				" * ",
				" * This software is probably, but not necessarily, discussed here:",
				" * https://javagraphics.java.net/",
				" * ",
				" * That site should also contain the most recent official version",
				" * of this software.  (See the SVN repository for more details.)",
				" */" };
	}
	
}
