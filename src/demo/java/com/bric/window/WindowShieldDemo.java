/*
 * @(#)WindowShieldDemo.java
 *
 * $Date: 2014-05-06 21:07:47 +0200 (Di, 06 Mai 2014) $
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
package com.bric.window;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.WindowConstants;

/** This is a simple demo app that tests the WindowShield class.
 * 
 */
public class WindowShieldDemo {
	public static void main(String[] args) {
		JFrame frame1 = createFrame("Not Shielded");
		JFrame frame2 = createFrame("Shielded");
		frame1.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		frame2.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		WindowShield.install(frame2);
		frame2.setLocation(frame1.getX()+frame1.getWidth()+10,frame2.getY());
	}
	
	protected static JFrame createFrame(String title) {
		JFrame f = new JFrame(title);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(3,3,3,3);
		c.anchor = GridBagConstraints.WEST;
		f.getContentPane().add(new JLabel("Steps:"),c);
		c.gridy++;
		f.getContentPane().add(new JLabel("1.  Select the other window."),c);
		c.gridy++;
		f.getContentPane().add(new JLabel("2.  Try to click the button below."),c);
		c.gridy++; c.anchor = GridBagConstraints.CENTER;
		JButton button = new JButton("Button");
		f.getContentPane().add(button,c);
		
		f.pack();
		f.setVisible(true);
		return f;
		
	}
}
