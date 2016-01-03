/*
 * @(#)JavaFileReader.java
 *
 * $Date: 2015-12-21 05:22:16 +0100 (Mo, 21 Dez 2015) $
 *
 * Copyright (c) 2015 by Jeremy Wood.
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
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.PlainDocument;

import com.bric.io.IOUtils;
import com.bric.io.location.IOLocation;
import com.bric.swing.JavaTextComponentHighlighter;

public class JavaFileReader extends FileReader {
	
	JavaFileReader() {
	}
	
	@Override
	public String getDescription() {
		return "Java Files";
	}

	@Override
	public String[] getExtensions() {
		return new String[] { "java" };
	}

	@Override
	public JComponent getComponent(final IOLocation loc, Dimension expectedSize)
			throws IOException {
		final JTextPane[] dest = new JTextPane[] { null };
		final IOException[] ex = new IOException[] { null };
		
		Runnable runnable = new Runnable() {
			public void run() {
				try {
					dest[0] = new JTextPane();
					try(InputStream in = loc.createInputStream()) {
						String text = IOUtils.read(in);
						dest[0].setText(text);
					}
					dest[0].setEditable(false);
					dest[0].getDocument().putProperty(PlainDocument.tabSizeAttribute, 3);
	
					new JavaTextComponentHighlighter(dest[0]);
				} catch(IOException e) {
					e.printStackTrace();
					ex[0] = e;
				}
				
			}
		};
		if(SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch (InvocationTargetException e) {
				throw new RuntimeException(e);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		
		if(ex[0]!=null) throw ex[0];
		
		return dest[0];
	}

}
