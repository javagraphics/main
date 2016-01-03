/*
 * @(#)ClassLabelList.java
 *
 * $Date: 2015-02-24 08:05:43 +0100 (Di, 24 Feb 2015) $
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
package com.bric.jar;

import java.io.File;
import java.io.FileFilter;

import com.bric.swing.FileLabelList;

public class ClassLabelList extends FileLabelList {
	private static final long serialVersionUID = 1L;

	public ClassLabelList(boolean includeMainMethod) {
		this(null, includeMainMethod);
	}

	public ClassLabelList(File[] directories,boolean includeMainMethod) {
		this(directories, includeMainMethod ? ClassCheckList.includeMainFilter : ClassCheckList.excludeMainFilter );
	}
	
	public ClassLabelList(File[] directories,FileFilter primaryFilter) {
		super(directories, primaryFilter, ClassCheckList.basicComparatorConstraints);
	}

	@Override
	protected String getText(File file) {
		String s = file.getName();
		if(s.toLowerCase().endsWith(".java"))
			s = s.substring(0,s.length()-5);
		String packageName = JarWriter.getPackage(file);
		s+=" ("+packageName+")";
		return s;
	}
}
