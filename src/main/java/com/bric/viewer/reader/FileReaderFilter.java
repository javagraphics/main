/*
 * @(#)FileReaderFilter.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
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
package com.bric.viewer.reader;

import com.bric.io.location.IOLocation;
import com.bric.io.location.IOLocationFilter;

public class FileReaderFilter extends IOLocationFilter {
	FileReader reader;
	boolean acceptDirectory, acceptNavigable, acceptAlias;
	
	public FileReaderFilter(FileReader r,boolean acceptDirectory,boolean acceptNavigable,boolean acceptAlias) {
		if(r==null) throw new NullPointerException();
		
		reader = r;
		this.acceptDirectory = acceptDirectory;
		this.acceptNavigable = acceptNavigable;
		this.acceptAlias = acceptAlias;
	}

	@Override
	public IOLocation filter(IOLocation loc) {
		if(loc.isAlias() && (!acceptAlias))
			return null;
		
		if(loc.isDirectory() || loc.isNavigable()) {
			return (acceptDirectory || acceptNavigable) ? loc : null;
		}
		
		if(reader.accepts(loc)) {
			return loc;
		}
		return null;
	}
}
