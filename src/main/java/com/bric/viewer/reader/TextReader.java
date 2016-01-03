/*
 * @(#)TextReader.java
 *
 * $Date: 2015-06-02 06:14:21 +0200 (Di, 02 Jun 2015) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
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
import java.net.URL;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.EditorKit;
import javax.swing.text.PlainDocument;

import com.bric.io.location.IOLocation;

public class TextReader extends FileReader {
	JComboBox encodingComboBox = new JComboBox();
	
	@Override
	public String getDescription() {
		return "Text";
	}

	@Override
	public String[] getExtensions() {
		return new String[] {"txt", "rtf", "xml", "properties", "mf", "js"};
	}
	
	@Override
	public JComponent[] getFooterControls() {
		return new JComponent[] { encodingComboBox };
	}

	@Override
	public JComponent getComponent(IOLocation loc,Dimension expectedSize) throws IOException {
		final JEditorPane[] dest = new JEditorPane[] { null };
		Runnable runnable = new Runnable() {
			public void run() {
				dest[0] = new JEditorPane();
			}
		};
		if(SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		final URL url = getURL(loc);
		
		if("rtf".equalsIgnoreCase(loc.getExtension())) {
			EditorKit kit = dest[0].getEditorKitForContentType("text/rtf");
			dest[0].setEditorKit(kit);
		}
		
		final IOException[] ex = new IOException[] { null };
		
		runnable = new Runnable() {
			public void run() {
				try {
					dest[0].setPage(url);
					dest[0].getDocument().putProperty(PlainDocument.tabSizeAttribute, 3);
				} catch (IOException e) {
					ex[0] = e;
					e.printStackTrace();
				}
			}
		};
		if(SwingUtilities.isEventDispatchThread()) {
			runnable.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(runnable);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		
		if(ex[0]!=null) throw ex[0];
		
		
		return dest[0];
	}

}
