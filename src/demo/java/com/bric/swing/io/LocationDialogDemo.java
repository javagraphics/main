/*
 * @(#)LocationDialogDemo.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
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
package com.bric.swing.io;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.blog.Blurb;
import com.bric.debug.AWTMonitor;

/** This is a demo app for the {@link com.bric.swing.io.OpenLocationPane} and {@link com.bric.swing.io.SaveLocationPane}.
 * 
 */
@Blurb (
filename = "LocationDialog",
title = "Navigation: Navigating File Systems",
releaseDate = "TBA",
summary = "This presents an architecture and UI for navigating file systems (or FTP servers, "+
		"or zip archives, or any tree-like structure.",
scrapped= "While incredibly useful, this is always in a state of incompletion.",
sandboxDemo = true
)
public class LocationDialogDemo {

	public static void main(String[] args) {
		//use a very short delay to make sure we stay out of the AWT thread
		AWTMonitor.installAWTListener("LocationDialogDemo",250,15000,true);
		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();
				frame.setVisible(true);
				
				OpenLocationPane.showDialog(frame, null, true, "Open File");
				
				SaveLocationPane.showDialog(frame, "jpg", "Save as JPG");
				
				System.exit(0);
			}
		});
	}
}
