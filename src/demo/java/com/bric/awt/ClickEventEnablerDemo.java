/*
 * @(#)ClickEventEnablerDemo.java
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
package com.bric.awt;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EtchedBorder;

import com.bric.blog.Blurb;
import com.bric.swing.BricApplet;

/** A simple demo program showing off the {@link ClickEventEnabler}. 
 *
 **/
@Blurb (
filename = "ClickEventEnabler",
title = "Mouse Click Events: Adding Wiggle Room",
releaseDate = "February 2011",
summary = "This triggers mouseClicked events even if the mouse moves a few pixels between click and release.",
instructions = "This applet demonstrates how the <code>ClickEventEnabler.html</code> redefines "+
"mouse click events to allow for a little bit of mouse movement.\n"+
"<p>When you click a label: if a MouseEvent.MOUSE_CLICKED event was received then it will "+
"pulse with a color. So if you click either label and don't move the mouse at all: they will pulse.\n"+
"<p>However if you press the mouse button, move the cursor a pixel or two, and then release: only "+
"the second label will pulse.\n"+
"<p>The tolerance spinner controls the number of pixels you can move the mouse and still receive "+
"a <code>mouseClicked()</code> notification.",
link = "http://javagraphics.blogspot.com/2011/02/mouse-click-events-adding-wiggle-room.html",
sandboxDemo = true
)
public class ClickEventEnablerDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().setLayout(new GridBagLayout());
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
				c.fill = GridBagConstraints.NONE;
				frame.getContentPane().add(new ClickEventEnablerDemo(), c);
				frame.pack();
				frame.setVisible(true);
			}
		});
	}
	
	class TargetLabel extends JLabel {
		private static final long serialVersionUID = 1L;

		Timer timer = new Timer(40, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color c = getForeground();
				int red = Math.max(0, c.getRed()-3);
				int green = Math.max(0, c.getGreen()-3);
				int blue = Math.max(0, c.getBlue()-3);
				setForeground(new Color(red,green,blue));
				if(red==0 && green==0 && blue==0) 
					timer.stop();
			}
		});
		
		MouseListener mouseListener = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pulse();
			}
		};
		
		public TargetLabel(String s) {
			super(s);
			addMouseListener(mouseListener);
			setForeground(Color.black);
		}
		
		protected void pulse() {
			Color c = getForeground();
			int green = c.getGreen();
			int red = green;
			setForeground(new Color(red, 255, 0));
			timer.start();
		}
	}
	
	JSpinner spinner = new JSpinner(new SpinnerNumberModel( ClickEventEnabler.CLICK_EVENT_TOLERANCE, 0, 20, 1 ));
	
	TargetLabel target1 = new TargetLabel("Click Me (original)");
	TargetLabel target2 = new TargetLabel("Click Me (improved)");
	
	public ClickEventEnablerDemo() {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(6,6,6,6);
		c.gridy++; c.gridx = 0; c.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(target1, c);
		c.gridy++;
		getContentPane().add(target2, c);
		
		c.gridy++; c.gridwidth = 1;
		c.anchor = GridBagConstraints.EAST;
		getContentPane().add(new JLabel("Tolerance:"), c);
		c.gridx++; 
		c.anchor = GridBagConstraints.WEST;
		getContentPane().add(spinner, c);
		
		ClickEventEnabler.ClickMonitor fixer = new ClickEventEnabler.ClickMonitor();
		target2.addMouseListener(fixer);
		target2.addMouseMotionListener(fixer);
		
		target1.setToolTipText("Click this label, but move the mouse just a pixel or two.");
		target2.setToolTipText("Now click THIS label and move the mouse just a pixel or two.");
		spinner.setToolTipText("This adjusts the number of pixels you can drag the mouse and still get a mouseClicked event.");
	
		((JComponent)getContentPane()).setBorder(new EtchedBorder());
	}
}
