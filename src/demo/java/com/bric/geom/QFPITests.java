/*
 * @(#)QFPITests.java
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
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;
import java.util.Vector;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.blog.Blurb;

/** This demonstrates the {@link QuadraticFlattenedPathIterator}.
 *
 **/
@Blurb (
filename = "QFPITests",
title = "Shapes: Flattening Cubic Curves",
releaseDate = "June 2007",
summary = "This breaks a path with cubic curves into several small quadratic curves.\n"+
"<p>You lose a little precision: but if (for whatever reason) you want quadratic curves: here you go!",
link = "http://javagraphics.blogspot.com/2007/06/shapes-flattened-cubic-curves.html",
scrapped = "The QuadraticFlattenedPathIterator isn't actually used in any live projects now.",
sandboxDemo = true
)
public class QFPITests extends JFrame {
	private static final long serialVersionUID = 1L;
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				QFPITests f = new QFPITests();
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	JLabel randomLabel = new JLabel("Random Seed");
	JSpinner randomSpinner = new JSpinner(new SpinnerNumberModel(1,1,100000,1));
	JLabel divisionsLabel = new JLabel("Max Divisions");
	JSpinner divisionsSpinner = new JSpinner(new SpinnerNumberModel(1,1,100000,1));
	JLabel segmentsLabel = new JLabel("Segments");
	JSpinner segmentsSpinner = new JSpinner(new SpinnerNumberModel(1,1,1000,1));
	Shape shape;
	JCheckBox showContrast = new JCheckBox("Show Contrast");
	Shape shape2;
	JCheckBox amplifyContrast = new JCheckBox("Amplify Contrast");
	
	JCheckBox showE = new JCheckBox("Show Extrema");
	JCheckBox showD = new JCheckBox("Show Divisions");
	
	public QFPITests() {
		super("Tests");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel controls1 = new JPanel(new GridBagLayout());
		JPanel controls2 = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.insets = new Insets(4,4,4,4);
		c.weightx = 1; c.weighty = 0;
		c.anchor = GridBagConstraints.EAST;
		controls1.add(divisionsLabel,c);
		c.gridx++; c.anchor = GridBagConstraints.WEST;
		controls1.add(divisionsSpinner,c);
		c.gridy++;
		c.gridx = 0; c.anchor = GridBagConstraints.EAST;
		controls1.add(segmentsLabel,c);
		c.gridx++; c.anchor = GridBagConstraints.WEST;
		controls1.add(segmentsSpinner,c);
		c.gridy++;
		c.gridx = 0; c.anchor = GridBagConstraints.EAST;
		controls1.add(randomLabel,c);
		c.gridx++; c.anchor = GridBagConstraints.WEST;
		controls1.add(randomSpinner,c);
		c.gridy = 0;
		c.gridx = 0; c.gridwidth = GridBagConstraints.REMAINDER;
		controls2.add(showD,c);
		c.gridy++;
		controls2.add(showE,c);
		c.gridy++;
		controls2.add(showContrast,c);
		c.gridy++;
		controls2.add(amplifyContrast,c);
		
		JPanel controls = new JPanel(new GridBagLayout());
		c.gridx = 0; c.gridy = 0;  c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.weightx = 1; c.weighty = 0;
		controls.add(controls1,c);
		c.gridx++;
		controls.add(controls2,c);
		
		getContentPane().setLayout(new GridBagLayout());
		c.gridx = 0; c.gridy = 0; c.weightx = 0; c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		getContentPane().add(controls,c);
		c.gridy++; c.fill = GridBagConstraints.BOTH; c.weightx = 1; c.weighty = 1;
		getContentPane().add(new ShapePanel(),c);
		
		javax.swing.Timer timer = new javax.swing.Timer(500, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(showContrast.isSelected()==false)
					repaint();
			}
		} );
		timer.start();
	}
	
	class ShapePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		public ShapePanel() {
			setPreferredSize(new Dimension(500,500));
			segmentsSpinner.setValue(new Integer(1));
			divisionsSpinner.setValue(new Integer(QuadraticFlattenedPathIterator.MAX_DIVISIONS));
			ChangeListener changeListener = new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					setup();
				}
			};
			showContrast.addChangeListener(new ChangeListener() {
				public void stateChanged(ChangeEvent e) {
					repaint();
					amplifyContrast.setEnabled(showContrast.isSelected());
				}
			});
			amplifyContrast.addChangeListener(changeListener);
			amplifyContrast.setEnabled(false);
			
			showE.addChangeListener(changeListener);
			showE.addChangeListener(changeListener);
			divisionsSpinner.addChangeListener(changeListener);
			randomSpinner.addChangeListener(changeListener);
			segmentsSpinner.addChangeListener(changeListener);
			QuadraticFlattenedPathIterator.extremaVector = new Vector<Point2D>();
			QuadraticFlattenedPathIterator.pointsVector = new Vector<Point2D>();
			setup();
		}
		
		BufferedImage bi1 = new BufferedImage(500,500,BufferedImage.TYPE_INT_RGB);
		BufferedImage bi2 = new BufferedImage(500,500,BufferedImage.TYPE_INT_RGB);
		BufferedImage bi3 = new BufferedImage(500,500,BufferedImage.TYPE_INT_RGB);
		
		protected void setup() {
			
			QuadraticFlattenedPathIterator.MAX_DIVISIONS = ((Number)divisionsSpinner.getValue()).intValue();
			Random r = new Random( ((Number)randomSpinner.getValue()).intValue() );
			shape = createShape(r, 
					((Number)segmentsSpinner.getValue()).intValue() );
			shape2 = new GeneralPath();
			((GeneralPath)shape2).append(new QuadraticFlattenedPathIterator(shape),true);
		
			Graphics2D g;
			g = bi1.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0,500,500);
			g.setColor(Color.black);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g.draw(shape);
			g.dispose();

			g = bi2.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0,500,500);
			g.setColor(Color.black);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g.draw(shape2);
			g.dispose();
			
			int[] i1 = new int[500];
			int[] i2 = new int[500];
			int[] i3 = new int[500];
			
			boolean amplify = amplifyContrast.isSelected();
			for(int y = 0; y<500; y++) {
				bi1.getRaster().getDataElements(0,y,500,1,i1);
				bi2.getRaster().getDataElements(0,y,500,1,i2);
				if(amplify) {
					for(int x = 0; x<500; x++) {
						int d2 = Math.abs((i1[x] & 0xFF)-(i2[x] & 0xFF));
						i3[x] = Math.min(d2*d2,255);
					}
				} else {
					for(int x = 0; x<500; x++) {
						int d2 = Math.abs((i1[x] & 0xFF)-(i2[x] & 0xFF));
						i3[x] = Math.min(d2,255);
					}
				}
				bi3.getRaster().setDataElements(0,y,500,1,i3);
			}
			
			repaint();
		}
		
		private Shape createShape(Random r,int segments) {
			float w = 500;
			float h = 500;
			GeneralPath p = new GeneralPath();
			p.moveTo(w*r.nextFloat(),h*r.nextFloat());
			for(int a = 0; a<segments; a++) {
				p.curveTo(w*r.nextFloat(), h*r.nextFloat(),
						w*r.nextFloat(), h*r.nextFloat(),
						w*r.nextFloat(), h*r.nextFloat() );
			}
			return p;
		}
	
		@Override
		protected void paintComponent(Graphics g) {
			long t = System.currentTimeMillis();
			long k = (t%1000)/500;
			super.paintComponent(g);
			g.setColor(Color.white);
			g.fillRect(0,0,getWidth(),getHeight());
			Graphics2D g2 = (Graphics2D)g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setColor(Color.black);
			if(showContrast.isSelected()) {
				g2.drawImage(bi3,0,0,null);	
			} else if(k==0) {
				g2.draw(shape);
			} else if(k==1) {
				g2.draw(shape2);
			}
			if(showD.isSelected()) {
				Vector<Point2D> v = QuadraticFlattenedPathIterator.pointsVector;
				g2.setColor(Color.red);
				draw(v,g2,1);
			}
			if(showE.isSelected()) {
				Vector<Point2D> v = QuadraticFlattenedPathIterator.extremaVector;
				g2.setColor(Color.green);
				draw(v,g2,2);
			}
		}
		
		protected void draw(Vector<Point2D> v,Graphics2D g,int radius) {
			Ellipse2D e = new Ellipse2D.Float();
			for(int a = 0; a<v.size(); a++) {
				Point2D p = v.get(a);
				e.setFrame(p.getX()-radius,p.getY()-radius,radius*2,radius*2);
				g.draw(e);
			}
		}
	}
}
