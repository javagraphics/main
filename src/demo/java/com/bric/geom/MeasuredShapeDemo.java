/*
 * @(#)MeasuredShapeDemo.java
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
package com.bric.geom;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.blog.Blurb;
import com.bric.swing.BricApplet;

/** A simple demo for the <code>MeasuredShape</code> class.
 * 
 */
@Blurb (
filename = "MeasuredShape",
title = "Shapes: Measuring Length",
releaseDate = "February 2009",
summary = "An uninteresting but very useful class that deals with measuring shapes.",
instructions = "This modest demo shows the <code>MeasuredShape</code> in action.\n"+
"<p>It doesn't look like much, but this is an essential building block in several more complex projects "+
"(like strokes, animations, brushed metal, etc.)",
link = "http://javagraphics.blogspot.com/2009/02/shapes-measuring-length.html",
sandboxDemo = true
)
public class MeasuredShapeDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	/** A simple demo program. 
     * @param args the application's arguments. (This is unused.)
     */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				f.getContentPane().add(new MeasuredShapeDemo());
				f.pack();
		        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setVisible(true);
			}
		});
	}

	protected JSlider offsetSlider = new JSlider(0,100,0);
	protected JSlider percentSlider = new JSlider(0,100,100);
	protected JLabel offsetLabel = new JLabel("Offset:");
	protected JLabel percentLabel = new JLabel("Percent:");

	JPanel drawing = new JPanel() {
		private static final long serialVersionUID = 1L;

		@Override
		public Dimension getPreferredSize() {
			return new Dimension(200,200);
		}
		
		@Override
		protected void paintComponent(Graphics g0) {
			super.paintComponent(g0);
			
			Graphics2D g = (Graphics2D)g0;
			Ellipse2D e = new Ellipse2D.Float(5,5,getWidth()-10,getHeight()-10);
			MeasuredShape s = new MeasuredShape(e);
			float v1 = offsetSlider.getValue();
			float v2 = percentSlider.getValue();
			v1 = v1/100f;
			v2 = -v2/100f;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			
			g.draw(s.getShape(v1, v2));
			
		}
	};
	
	ChangeListener repaintListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			drawing.repaint();
		}
	};
	
	public MeasuredShapeDemo() {

		JPanel panel = new JPanel(new GridBagLayout());
		offsetSlider.addChangeListener(repaintListener);
		percentSlider.addChangeListener(repaintListener);
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 0;
		c.anchor = GridBagConstraints.EAST;
		c.insets = new Insets(4,4,4,4);
		panel.add(offsetLabel,c);
		c.gridy++;
		panel.add(percentLabel,c);
		c.gridy = 0; c.weightx = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx++;
		panel.add(offsetSlider,c);
		c.gridy++;
		panel.add(percentSlider,c);
		c.gridy++; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
		c.gridx = 0; c.gridwidth = GridBagConstraints.REMAINDER;
		panel.add(drawing,c);
		
		panel.setBackground(Color.white);
		drawing.setBackground(Color.white);
		drawing.setOpaque(true);
		getContentPane().add(panel);
	}
}
