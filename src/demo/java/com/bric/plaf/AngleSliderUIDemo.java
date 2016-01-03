/*
 * @(#)AngleSliderUIDemo.java
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
package com.bric.plaf;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.bric.blog.Blurb;
import com.bric.blog.ResourceSample;
import com.bric.swing.BricApplet;

/** A demo class for the <code>AngleSliderUI</code> class.
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/AngleSliderUIDemo/sample.png" alt="new&#160;com.bric.plaf.AngleSliderUIDemo()">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 */
@Blurb (
filename = "AngleSliderUI",
title = "Angles: need GUI widget for angles?",
releaseDate = "May 2008",
summary = "Take a <a href=\"http://java.sun.com/javase/6/docs/api/index.html?javax/swing/JSlider.html\"><code>JSlider</code></a>. "+
"Add a <code><a href=\"https://javagraphics.java.net/doc/com/bric/plaf/AngleSliderUI.html\">AngleSliderUI</a></code>. "+
"Voila!  You have a GUI widget for angles.\n"+
"<P>Comes in Aqua and non-Aqua flavors.",
instructions = "This applet shows two simple <code>AngleSliderUIs</code>. Click and drag on each to adjust "+
"the angle.\n"+
"<p>This is a new type of <code>SliderUI</code>, so the internal data model and listeners are "+
"the same as what you're used to when working with a <code>JSlider</code>.",
link = "http://javagraphics.blogspot.com/2008/05/angles-need-gui-widget-for-angles.html",
sandboxDemo = true
)
@ResourceSample( sample = { "new com.bric.plaf.AngleSliderUIDemo()" } )
public class AngleSliderUIDemo extends BricApplet implements ActionListener {
	private static final long serialVersionUID = 1L;

	/** A simple demo program for the AngleSliderUIs. 
	 * @param args the program arguments.
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame();
		
		AngleSliderUIDemo demo = new AngleSliderUIDemo();
		f.getContentPane().add(demo);
		f.pack();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		f.setVisible(true);
	}
	
	JLabel label1 = new JLabel("AngleSliderUI:");
	JLabel label2 = new JLabel("AquaAngleSliderUI:");
	JSlider slider1 = new JSlider();
	JSlider slider2 = new JSlider();
	JCheckBox enabled1 = new JCheckBox("Enabled",true);
	JCheckBox enabled2 = new JCheckBox("Enabled",true);
	
	public AngleSliderUIDemo() {
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 0;
		c.insets = new Insets(4,4,4,4);
		JPanel contents = new JPanel(new GridBagLayout());
		contents.setOpaque(true);
		contents.setBackground(Color.white);
		getContentPane().add(contents);
		
		contents.add(label1,c);
		c.gridx++;
		contents.add(label2,c);
		c.gridy++;
		c.gridx = 0;
		contents.add(slider1,c);
		c.gridx++;
		contents.add(slider2,c);
		c.gridx = 0; c.gridy++;
		contents.add(enabled1,c);
		c.gridx++;
		contents.add(enabled2,c);
		
		slider1.setUI(new AngleSliderUI());
		slider2.setUI(new AquaAngleSliderUI());
		
		enabled1.addActionListener(this);
		enabled2.addActionListener(this);
		
		enabled1.setOpaque(false);
		enabled2.setOpaque(false);
		
		contents.setOpaque(true);
		contents.setBackground(Color.white);
	}
	
	public void actionPerformed(ActionEvent e) {
		slider1.setEnabled(enabled1.isSelected());
		slider2.setEnabled(enabled2.isSelected());
	}
}
