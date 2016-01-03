/*
 * @(#)GradientTexturePaintDemo.java
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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import com.bric.awt.GradientTexturePaint.Cycle;
import com.bric.blog.Blurb;
import com.bric.swing.BricApplet;
import com.bric.swing.RenderingHintsContextMenu;

/** A simple app demoing the <code>GradientTexturePaint</code>.
 * <P>The nature of this demo requires bundling the <code>GradientSlider</code>
 * and therefore the <code>ColorPicker</code> classes, so this
 * jar could be considerably reduced in size if you only want the
 * <code>GradientTexturePaint</code> itself.
 * 
 */
@Blurb (
filename = "GradientTexturePaint",
title = "Gradients: Using TexturePaint for Gradients",
releaseDate = "November 2009",
summary = "This class uses a <code>java.awt.TexturePaint</code> to render a multiple-color linear gradient.\n"+
"<p>This was mostly an experiment I put together for <a href=\"http://javagraphics.blogspot.com/2009/11/gradients-boring-discussion.html\">this article</a>. "+
"As of this writing I'd only recommend using this if you can't use the <a href=\"http://java.sun.com/javase/6/docs/api/index.html?java/awt/MultipleGradientPaint.html\">MultipleGradientPaint</a> in Java 1.6 or its earlier incarnations in the <a href=\"http://xmlgraphics.apache.org/batik/\">batik</a> project.\n"+
"<p>This is functional, though.  And if you're stuck in an earlier Java version: it's a very small class to add if you need it.",
instructions = "This applet is part of an analysis of different gradients <a href=\"http://javagraphics.blogspot.com/2009/11/gradients-boring-discussion.html\">how they perform.</a>. This displays a <code>GradientTexturePaint</code> (which is a gradient rendered via a <code>TexturePaint</code>).",
sandboxDemo = true
)
public class GradientTexturePaintDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame();
				GradientTexturePaintDemo demo = new GradientTexturePaintDemo();
		        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.getContentPane().add(demo);
				f.pack();
				f.setVisible(true);
			}
		});
	}

	JRadioButton noCycle = new JRadioButton("No Cycle");
	JRadioButton tile = new JRadioButton("Loop");
	JRadioButton loop = new JRadioButton("Tile");
	PreviewPanel preview = new PreviewPanel();
	Point2D p1 = new Point2D.Float(20,20);
	Point2D p2 = new Point2D.Float(100,100);
	Color[] colors = new Color[] {new Color(0xDDFFFF), new Color(0x2D5BE6),new Color(0x000000) };
	float[] positions = new float[] {0, .6f, 1f};
	
	RenderingHintsContextMenu contextMenu = new RenderingHintsContextMenu(preview, 
			new RenderingHints.Key[] { RenderingHints.KEY_ANTIALIASING, RenderingHints.KEY_INTERPOLATION, RenderingHints.KEY_COLOR_RENDERING });
	
	public GradientTexturePaintDemo() {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weighty = 0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(3,3,3,3);
		c.gridwidth = 1; c.weightx = 0;
		c.anchor = GridBagConstraints.WEST;
		getContentPane().add(noCycle,c);
		c.gridx++;
		getContentPane().add(tile,c);
		c.gridx++;
		getContentPane().add(loop,c);
		c.gridy++; c.gridx = 0; c.weightx = 1;
		c.weighty = 1; c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(preview,c);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				preview.repaint();
			}
		};
		loop.addActionListener(actionListener);
		noCycle.addActionListener(actionListener);
		tile.addActionListener(actionListener);
		
		getContentPane().setBackground(Color.white);
		loop.setOpaque(false);
		noCycle.setOpaque(false);
		tile.setOpaque(false);
		
		ButtonGroup bg = new ButtonGroup();
		bg.add(loop);
		bg.add(tile);
		bg.add(noCycle);
		noCycle.doClick();
	}
	
	class PreviewPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		MouseInputAdapter mouseListener = new MouseInputAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(e.isPopupTrigger())
					return;
				
				Point p = e.getPoint();
				if(p.distance(p1)<p.distance(p2)) {
					p1.setLocation(p);
				} else {
					p2.setLocation(p);
				}
				preview.repaint();
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				mousePressed(e);
			}
		};
		
		public PreviewPanel() {
			setPreferredSize(new Dimension(500,500));
			addMouseListener(mouseListener);
			addMouseMotionListener(mouseListener);
		}
		
		Ellipse2D dot = new Ellipse2D.Float();
		@Override
		protected void paintComponent(Graphics g0) {
			super.paintComponent(g0);
			Graphics2D g = (Graphics2D)g0;
			
			g.setRenderingHints(contextMenu.getRenderingHints());
			
			try {
				Cycle cycle = Cycle.LOOP;
				if(noCycle.isSelected())
					cycle = Cycle.NONE;
				if(tile.isSelected())
					cycle = Cycle.TILE;
				GradientTexturePaint p = new GradientTexturePaint(colors,
						positions,
						p1, p2,
						cycle);
				
				g.setPaint(p);
				g.fillRect(0,0,getWidth(),getHeight());
				
				dot.setFrame(p1.getX()-5,p1.getY()-5,10,10);
				g.setColor(Color.white);
				g.fill(dot);
				g.setColor(Color.black);
				g.draw(dot);

				dot.setFrame(p2.getX()-5,p2.getY()-5,10,10);
				g.setColor(Color.white);
				g.fill(dot);
				g.setColor(Color.black);
				g.draw(dot);
			} catch(Throwable t) {
				t.printStackTrace();
			}
		}
	}
}
