/*
 * @(#)MultiFileReader.java
 *
 * $Date: 2015-06-02 06:14:21 +0200 (Di, 02 Jun 2015) $
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

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import javax.swing.JComponent;

import com.bric.io.location.IOLocation;

/** A <code>FileReader</code> composed of other <Code>FileReaders</code>.
 */
public class MultiFileReader extends FileReader {
	public static final MultiFileReader ALL_FILES = new MultiFileReader(new FileReader[] { new ImageReader(), new JavaFileReader(), new TextReader() }, "Supported Files");;
	
	static {
		//this might fail if JFX isn't available
		try {
			ALL_FILES.add(new JFXWebViewReader());
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	List<FileReader> readers;
	String description;
	
	public MultiFileReader(FileReader[] readers,String description) {
		this.readers = new ArrayList<>();
		this.description = description;
		for(int a = 0; a<readers.length; a++) {
			add(readers[a]);
		}
	}

	public void add(FileReader newReader) {
		if(newReader==null)
			throw new NullPointerException();
		this.readers.add(newReader);
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public String[] getExtensions() {
		TreeSet<String> set = new TreeSet<String>();
		for(FileReader r : readers) {
			String[] s = r.getExtensions();
			for(String t : s) {
				set.add(t);
			}
		}
		return set.toArray(new String[set.size()]);
	}

	@Override
	public JComponent getComponent(IOLocation loc,Dimension expectedSize) throws IOException {
		for(FileReader r : readers) {
			if(r.accepts(loc)) {
				JComponent jc = r.getComponent(loc, expectedSize);
				if(jc!=null) return jc;
			}
		}
		return null;
	}
}
