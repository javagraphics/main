/*
 * @(#)SourceHeaderJob.java
 *
 * $Date: 2015-02-28 05:28:18 +0100 (Sa, 28 Feb 2015) $
 *
 * Copyright (c) 2011 by Jeremy Wood.
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
import java.util.Iterator;

import com.bric.io.FileTreeIterator;
import com.bric.jar.JarWriter;

/** This abstract class helps update source code files.
 * <p>If changes are made then you'll need to refresh the files in Eclipse.
 * (This will not change source code files if they already meet
 * all the expected requirements.)
 */
public abstract class FormatSourceCodeJob extends BlogUpdaterJob {
	
	public FormatSourceCodeJob(WorkspaceContext context) {
		super(context);
		setDescription("Formatting Source Code...");
		setName("Formatting Source Code");
	}

	@Override
	protected void runJob() throws IOException {
		JarWriter j = context.getJarWriter();
		for(File sourcepath : j.getSourcepaths()) {
			Iterator<File> inputFiles = new FileTreeIterator(sourcepath, "java");
			while(inputFiles.hasNext()) {
				File file = inputFiles.next();
				setNote(file.getName());
				formatFile(file);
			}
		}
	}
	
	/** Apply some level of new formatting to a java source code file.
	 * 
	 * @param javaFile .java the file to modify.
	 * @return true if the file was rewritten, false if no changes were necessary.
	 */
	protected abstract boolean formatFile(File javaFile) throws IOException;
}
