/*
 * @(#)AudioPlayerComponentDemo.java
 *
 * $Date: 2015-02-28 21:59:45 +0100 (Sa, 28 Feb 2015) $
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
package com.bric.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.bric.blog.Blurb;

/** A demo for the AudioPlayerComponent
 * 
 */
@Blurb (
filename = "AudioPlayerComponent",
title = "Audio: UIs for Audio Playback",
releaseDate = "TBA",
summary = "This demonstrates the <code>AudioPlayerComponent</code>: a <code>JComponent</code> "+
"and accompanying UI for playback sound files.",
scrapped = "This is unfinished.",
sandboxDemo = false
)
public class AudioPlayerComponentDemo extends JPanel {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				f.getContentPane().add(new AudioPlayerComponentDemo());
				f.pack();
		        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setVisible(true);
			}
		});
	}

	Preferences prefs = Preferences.userNodeForPackage(AudioPlayerComponentDemo.class);
	AudioPlayerComponent apc = new AudioPlayerComponent();
	
	public AudioPlayerComponentDemo() {
		super();
		apc.addPropertyChangeListener(AudioPlayerComponent.SOURCE_KEY, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				prefs.put("lastSource", apc.getSource().toString());
			}
		});
		String lastSource = prefs.get("lastSource", null);
		if(lastSource!=null) {
			try {
				apc.setSource(new URL(lastSource));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		add(apc);
	}
}
