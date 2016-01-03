/*
 * @(#)GradientSliderDemo.java
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
package com.bric.swing;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import com.bric.blog.Blurb;

/** A simple demo of the {@link GradientSlider} class.
 * 
 * 
 */
@Blurb (
filename = "GradientSlider",
title = "Gradients: a GUI Widget to Design Them",
releaseDate = "May 2008",
summary = "This is a GUI component to arrange the "+
"distribution of colors for multicolor gradients. It is "+
"similar to a <code>JSlider</code>, but it supports "+
"multiple thumbs and maps each thumb to a <code>Color</code>.\n"+
"<p>(By default it also leans heavily on some existing color GUI "+
"widgets I wrote, but you can replace those with your own components if you want to.)",
instructions = "This applet demonstrates the <code>GradientSlider</code>.\n"+
"<p>There are several ways to interact with this component:\n"+
"<li>Click and drag on a thumb.\n"+
"<li>Double-click a thumb to invoke a color dialog.\n"+
"<li>Click away from a thumb to create a new thumb.\n"+
"<li>Right-click a thumb to trigger a color palette popup.\n"+
"<li>Drag a thumb off the component to delete it.\n"+
"<li>When a thumb is selected, use the arrow keys to nudge it or the delete key to remove it.</li>",
link = "http://javagraphics.blogspot.com/2008/05/gradients-gui-widget-to-design-them.html",
sandboxDemo = true
)
public class GradientSliderDemo extends BricApplet {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame frame = new JFrame();
		        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				frame.getContentPane().add(new GradientSliderDemo());
				
				frame.pack();
				frame.setLocationRelativeTo(null);
				frame.setVisible(true);
			}
		});
	}
	
	public GradientSliderDemo() {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 1; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH; c.insets = new Insets(10,10,10,10);
		GradientSlider slider = new GradientSlider();
		slider.setValues(new float[] {0, 1}, new Color[] { Color.red, Color.black});
		slider.putClientProperty("MultiThumbSlider.indicateComponent", "true");
		slider.setPaintTicks(true);
		getContentPane().add(slider,c);

		c.gridx = 0; c.gridy = 1;
		slider = new GradientSlider(MultiThumbSlider.VERTICAL);
		slider.setValues(new float[] {0, 1}, new Color[] { new Color(255,0,255), Color.black});
		slider.setPaintTicks(true);
		slider.putClientProperty("GradientSlider.useBevel", "true");
		getContentPane().add(slider,c);

		c.gridx = 2; c.gridy = 1;
		slider = new GradientSlider(MultiThumbSlider.VERTICAL);
		slider.setValues(new float[] {0, 1}, new Color[] { Color.yellow, Color.white});
		slider.setInverted(true);
		getContentPane().add(slider,c);

		c.gridx = 1; c.gridy = 2;
		slider = new GradientSlider();
		slider.setValues(new float[] {0, 1}, new Color[] { Color.green, Color.white});
		slider.setInverted(true);
		slider.putClientProperty("MultiThumbSlider.indicateComponent", "true");
		slider.putClientProperty("GradientSlider.useBevel", "true");
		getContentPane().add(slider,c);
		
		getContentPane().setBackground(Color.white);
		if(getContentPane() instanceof JComponent)
			((JComponent)getContentPane()).setOpaque(true);
	}
}
