/*
 * @(#)PlaybackDemo.java
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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import quicktime.QTException;
import quicktime.QTSession;

import com.bric.blog.Blurb;

/** A simple demo for the {@link Playback} class.
 */
@Deprecated
@Blurb (
filename = "Playback",
title = "QTJ: Video Capture in Swing Components",
releaseDate = "April 2007",
summary = "This demonstrates how to interface with QuickTime for Java and " +
"grab pixel data from a <code>QDGraphics</code> "+
"and render that in a <code>JComponent</code>",
instructions = "",
link = "http://javagraphics.blogspot.com/2007/04/qtj-video-capture-in-swing-components.html",
scrapped = "QuickTime for Java is deprecated and unsupported.",
sandboxDemo = false
)
public class PlaybackDemo {
	
	public static void main(String[] args) {
		try {
			QTSession.open();
			JFrame frame = new JFrame("Playback");
			final JCheckBox flipHorizontal = new JCheckBox("Flip Horizontal");
			final JSlider repaintSlider = new JSlider(0,1000,1000);
			final Playback playbackPanel = new Playback(new Dimension(640,480));
			frame.getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 0;
			c.insets = new Insets(4,4,4,4);
			c.anchor = GridBagConstraints.WEST;
			frame.getContentPane().add(flipHorizontal,c);
			c.gridy++;
			frame.getContentPane().add(new JLabel("Repaint Delay:"),c);
			c.gridy++; c.fill = GridBagConstraints.HORIZONTAL;
			frame.getContentPane().add(repaintSlider,c);
			c.gridy++;
			c.weighty = 1;
			c.fill = GridBagConstraints.NONE; c.insets = new Insets(0,0,0,0);
			
			repaintSlider.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					int i = repaintSlider.getValue();
					playbackPanel.msRepaintDelay = i;
					System.out.println("Set repaint delay to "+i+" ms");
				}
			});
			flipHorizontal.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					playbackPanel.qtImage.setFlipHorizontal(flipHorizontal.isSelected());
				}
			});
			repaintSlider.setValue(100);
			
			frame.getContentPane().add(playbackPanel, c);
			frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
			
			frame.pack();
			frame.setLocationRelativeTo(null);
			frame.setVisible(true);
		} catch(QTException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

}
