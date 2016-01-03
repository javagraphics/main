/*
 * @(#)QTAudioDemo.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
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
package com.bric.qt;

import java.awt.FileDialog;
import java.io.File;

import javax.swing.JFrame;

import quicktime.QTSession;

import com.bric.blog.Blurb;
import com.bric.io.SuffixFilenameFilter;

/** A demo app for {@link QTAudio}.
 * 
 * @deprecated
 */
@Blurb (
filename = "QTAudio",
title = "Audio: Using QTJ With Audio",
releaseDate = "",
summary = "This includes static methods to convert mp3 files (and other modern" +
		 " audio formats) to wav files, and creating a waveform graphic to represent "+
		 " a sound.",
scrapped = "QuickTime for Java is deprecated and unsupported.",
sandboxDemo = false
)
public class QTAudioDemo {

	
	public static void main(String[] args) {
		demoExtract();
	}
	
	private static void demoExtract() {
		try {
			QTSession.open();
			JFrame frame = new JFrame();
			FileDialog fd = new FileDialog(frame);
			fd.setFilenameFilter(new SuffixFilenameFilter("mov", "mp3", "m4a"));
			fd.pack();
			fd.setVisible(true);
			if(fd.getFile()==null) return;
			File movFile = new File(fd.getDirectory()+fd.getFile());
			File[] wavFiles = QTAudio.extractAudioAsWaveFiles(movFile);
			for(int a = 0; a<wavFiles.length; a++) {
				System.out.println(wavFiles[a].getAbsolutePath());
			}
			System.out.println("Finished");
			System.exit(0);
		} catch(Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
}
