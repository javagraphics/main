/*
 * @(#)GraphicsWriterDemo.java
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
package com.bric.graphics;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.bric.awt.DualGraphics2D;
import com.bric.blog.Blurb;

/** A demo app for the {@link GraphicsWriter}.
 */
@Blurb (
filename = "GraphicsWriter",
title = "Graphics2D: Serializable Vector Graphics",
releaseDate = "December 2008",
link = "http://javagraphics.blogspot.com/2008/12/graphics2d-serializable-vector-graphics.html",
summary = "This has been a real life-saver when I'm trying to debug our graphics-rich desktop apps at work.\n"+
"<p>Whenever something isn't rendering right this lets you see the stack trace of each graphic instruction.",
sandboxDemo = true
)
public class GraphicsWriterDemo {

	
	/** A simple demo program. 
     * @param args the application's arguments. (This is unused.)
     */
	public static void main(String[] args) {
		BufferedImage bi1 = new BufferedImage(200,200,BufferedImage.TYPE_INT_ARGB);
		BufferedImage bi2 = new BufferedImage(200,200,BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = bi1.createGraphics();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0,0,bi1.getWidth(),bi1.getHeight());
		g.setComposite(AlphaComposite.SrcOver);
		GraphicsWriter w = new GraphicsWriter(true);
		DualGraphics2D g2 = new DualGraphics2D(g,w);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		g2.setColor(Color.black);
		g2.setTransform(AffineTransform.getRotateInstance(.3,100,100));
		g2.transform(AffineTransform.getScaleInstance(4, 4));
		g2.drawLine(0,0,200,200);
		g2.drawLine(0,200,200,0);
		g2.setTransform(AffineTransform.getScaleInstance(.5, .5));
		g2.setFont(new Font("Arial Narrow",0,50));
		g2.setColor(new Color(200,0,200));
		g2.drawString("Hi there and there...", 10, 55);
		g2.setPaint(new GradientPaint(0,0,Color.red,10,10,Color.yellow,true));
		try
		{
			Image img = javax.imageio.ImageIO
					.read(GraphicsWriter.class.getResource("resources/sandiego.jpg"));
			g2.drawImage(img, 50,50,350,350,0,0,250,223, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		g2.clipRect(5, 5, 60, 305);
		
		g2.transform(AffineTransform.getRotateInstance(-Math.PI/4,100,100));
		g2.fillOval(10, 10, 300, 300);
		g2.setStroke(new BasicStroke(4));
		g2.setColor(Color.green);
		g2.drawOval(10, 10, 300, 300);
		g2.dispose();
		
		g = bi2.createGraphics();
		g.setComposite(AlphaComposite.Clear);
		g.fillRect(0,0,bi1.getWidth(),bi1.getHeight());
		g.setComposite(AlphaComposite.SrcOver);
		w.paint(g);
		g.dispose();
		
		JTabbedPane tabs = new JTabbedPane();
		tabs.add(new ImagePanel(bi1),"Correct");
		tabs.add(new ImagePanel(bi2),"GraphicsWriter");
		JFrame f = new JFrame();
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		f.getContentPane().add(tabs);
		f.pack();
		f.setVisible(true);
		
		GraphicsWriterDebugger d = new GraphicsWriterDebugger(w);
		d.pack();
        d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		d.setVisible(true);
	}
	
	static class ImagePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		BufferedImage image;
		public ImagePanel(BufferedImage bi) {
			this.image = bi;
			setPreferredSize(new Dimension(bi.getWidth(),bi.getHeight()));
		}
		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.drawImage(image,0,0,null);
		}
	}
}
