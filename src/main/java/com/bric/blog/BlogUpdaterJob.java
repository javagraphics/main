/*
 * @(#)BlogUpdaterJob.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import com.bric.io.FileTreeIterator;
import com.bric.job.Job;

/** This includes some common helper methods used for
 * several {@link com.bric.job.Job} objects related to 
 * updating the javagraphics blog.
 */
abstract class BlogUpdaterJob extends Job {
	
	protected final WorkspaceContext context;
	
	BlogUpdaterJob(WorkspaceContext context) {
		this.context = context;
	}

	protected BufferedImage padImage(BufferedImage bi,int top,int left,int bottom,int right) {
		if(bi==null) return null;
		BufferedImage bi2 = new BufferedImage(bi.getWidth() + left + right, bi.getHeight() + top + bottom, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi2.createGraphics();
		g.drawImage(bi, left, top, null);
		g.dispose();
		return bi2;
	}

	/** A file in either the new build directory or the old (unmodified)
	 * build directory that matches these search parameters.
	 * 
	 * @param fileName the name of the file when the file extension is stripped away. 
	 * @param subDirectory the subdirectory in the "www" directory to search within.
	 * For example: this should be "blurbs", "jars", or "jnlp".
	 * @param fileExtensions the file extensions to search within.
	 * @return a file that matches this search, or null.
	 */
	protected File findFile(String fileName,String subDirectory,String[] fileExtensions,boolean throwExceptionIfNotFound) {

		//first check the newly generated files:
		File dir1 = new File(context.getDestinationDirectory(), subDirectory);
		File[] files = FileTreeIterator.findAll(
				new File[] { dir1 }, 
				fileName, 
				fileExtensions );
		if(files.length>1) {
			for(int a = 0; a<files.length; a++) {
				System.err.println(files[a].getAbsolutePath());
			}
			throw new RuntimeException("ambiguous results for \""+fileName+"\"");
		} else if(files.length==1) {
			return files[0];
		}

		//now check the old files:
		File dir2 = new File(new File(context.getWorkspaceDirectory(), "www"), subDirectory);
		files = FileTreeIterator.findAll(
				new File[] { dir2 },
				fileName, 
				fileExtensions );
		if(files.length>1) {
			for(int a = 0; a<files.length; a++) {
				System.err.println(files[a].getAbsolutePath());
			}
			throw new RuntimeException("ambiguous results for \""+fileName+"\"");
		} else if(files.length==1) {
			return files[0];
		}
		
		if(throwExceptionIfNotFound) {
			throw new RuntimeException("the file \""+fileName+"\" was not found in \""+dir1.getAbsolutePath()+"\" or \""+dir2.getAbsolutePath()+"\"");
		}
		
		return null;
	}
}
