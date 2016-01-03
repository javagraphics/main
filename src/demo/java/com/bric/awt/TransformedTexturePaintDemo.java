/*
 * @(#)TransformedTexturePaintDemo.java
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
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import com.bric.blog.Blurb;
import com.bric.geom.TransformUtils;
import com.bric.swing.BricApplet;

/** A simple demo of the {@link TransformedTexturePaint} class.
 * 
 */
@Blurb (
filename = "TransformedTexturePaint",
title = "TexturePaints and AffineTransforms",
releaseDate = "June 2008",
summary = "This discusses/demos a class that combines these two elements.",
instructions = "This simple applet demonstrates the <code>TransformedTexturePaint</code>.\n"+
"<p>Click and drag the square white handles to transform the image.",
link = "http://javagraphics.blogspot.com/2008/06/texturepaints-and-affinetransforms.html",
sandboxDemo = true
)
public class TransformedTexturePaintDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		try {
			JFrame f = new JFrame("Demo");
	        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			f.getContentPane().add(new TransformedTexturePaintDemo());
			f.pack();
			f.setVisible(true);
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	TransformedTexturePaintPanel panel = new TransformedTexturePaintPanel();
	
	class TransformedTexturePaintPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		
		BufferedImage image;
		Paint paint;
		Point2D p1, p2, p3;
	
		MouseInputAdapter mouseListener = new MouseInputAdapter() {
			int selectedNode;
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(selectedNode==1) {
					p1 = e.getPoint();
				} else if(selectedNode==2) {
					p2 = e.getPoint();
				} else if(selectedNode==3) {
					p3 = e.getPoint();
				}
				updatePaint();
			}
	
			@Override
			public void mousePressed(MouseEvent e) {
				double d1 = e.getPoint().distance(p1);
				double d2 = e.getPoint().distance(p2);
				double d3 = e.getPoint().distance(p3);
				if(d1<d2 && d1<d3) {
					selectedNode = 1;
				} else if(d2<d1 && d2<d3) {
					selectedNode = 2;
				} else {
					selectedNode = 3;
				}
			}
			
		};

		public TransformedTexturePaintPanel() throws IOException {
			setPreferredSize(new Dimension(500,500));
			image = new BufferedImage(100,100,BufferedImage.TYPE_INT_RGB);
			image = ImageIO.read(TransformedTexturePaintDemo.class.getResource("resources/pattern.png"));
			
			p1 = new Point2D.Double(500/2-image.getWidth()/2,500/2-image.getHeight()/2);
			p2 = new Point2D.Double(500/2+image.getWidth()/2,500/2-image.getHeight()/2);
			p3 = new Point2D.Double(500/2-image.getWidth()/2,500/2+image.getHeight()/2);
			
			updatePaint();
			
			addMouseListener(mouseListener);
			addMouseMotionListener(mouseListener);
		}
		protected void updatePaint() {
			AffineTransform transform = TransformUtils.createAffineTransform(
					getWidth()/2-image.getWidth()/2, getHeight()/2-image.getHeight()/2,
					getWidth()/2+image.getWidth()/2, getHeight()/2-image.getHeight()/2,
					getWidth()/2-image.getWidth()/2, getHeight()/2+image.getHeight()/2,
					p1.getX(), p1.getY(), 
					p2.getX(), p2.getY(), 
					p3.getX(), p3.getY());
			paint = new TransformedTexturePaint(image,
					new Rectangle(0,0,image.getWidth(),image.getHeight()),
					transform);
			repaint();
		}
		
		@Override
		public void setBounds(int x, int y, int width, int height) {
			//of course a ComponentListener is better, but this is quick & dirty
			super.setBounds(x, y, width, height);
			updatePaint();
		}

		@Override
		protected void paintComponent(Graphics g0) {
			Graphics2D g = (Graphics2D)g0;
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
					RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setPaint(paint);
			g.fillRect(0,0,getWidth(),getHeight());
			
			paintNode(g,p1);
			paintNode(g,p2);
			paintNode(g,p3);
		}
		
		private void paintNode(Graphics2D g, Point2D p) {
			int x = (int)(p.getX()+.5f);
			int y = (int)(p.getY()+.5f);
			g.setColor(Color.white);
			g.fillRect(x-4, y-4, 8, 8);
			g.setColor(Color.black);
			g.drawRect(x-4, y-4, 8, 8);
		}
	}
	
	public TransformedTexturePaintDemo() throws IOException {
		getContentPane().add(panel);
	}
}
