/*
 * @(#)ColorBandDemo.java
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
package com.bric.awt;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.bric.blog.Blurb;
import com.bric.swing.BricApplet;
import com.bric.swing.animation.AnimationController;

/** This small demo app features two horizontal gradients, and each shows
 * below it a zoomed-in image highlighting where pixels change.
 */
@Blurb (
filename = "ColorBandDemo",
title = "Gradients: Avoiding Color Banding",
releaseDate = "March 2014",
summary = "This article explores the problem of color banding in linear gradients and presents a partial solution.",
instructions = "This applet shows two horizontal gradients.\n"+
"<p>The first uses Java's default gradient implementation, and the second uses the revised "+
"<code>GradientTexturePaint</code>. Below each is a panel that amplifies where each pixel "+
"change occurs (toggling between black and gray).\n"+
"<p>Drag the scrubber (or play the animation) to observe color banding in progress.\n"+
"<p>This is meant to demonstrate that the second panel shows less severe color banding than Java's "+
"default implementation.",
link = "http://javagraphics.blogspot.com/2014/03/gradients-avoiding-color-banding.html",
sandboxDemo = true
)
public class ColorBandDemo extends BricApplet {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					String lf = UIManager.getSystemLookAndFeelClassName();
					UIManager.setLookAndFeel(lf);
				} catch (Throwable e) {
					e.printStackTrace();
				}
				
				JFrame f = new JFrame();
				f.getContentPane().add(new ColorBandDemo());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}

	final Color c1 = new Color(0xDDDDDD);
	final Color c2 = new Color(0xFFFFFF);
	int width = 600;
	int height = 100;
	BufferedImage defaultPaint = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage defaultPaintZoom = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage diffusedPaint = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	BufferedImage diffusedPaintZoom = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	JLabel defaultPaintLabel = new JLabel(new ImageIcon(defaultPaint));
	JLabel defaultPaintZoomLabel = new JLabel(new ImageIcon(defaultPaintZoom));
	JLabel diffusedPaintLabel = new JLabel(new ImageIcon(diffusedPaint));
	JLabel diffusedPaintZoomLabel = new JLabel(new ImageIcon(diffusedPaintZoom));
	
	
	AnimationController controller = new AnimationController();
	
	public ColorBandDemo() {
		getContentPane().setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1; c.fill = GridBagConstraints.BOTH;
		getContentPane().add(defaultPaintLabel, c);
		c.gridy++;
		getContentPane().add(defaultPaintZoomLabel, c);
		c.gridy++;
		getContentPane().add(diffusedPaintLabel, c);
		c.gridy++;
		getContentPane().add(diffusedPaintZoomLabel, c);
		c.gridy++; c.gridx = 0; c.weighty = 0;
		getContentPane().add(controller, c);
		
		controller.setDuration(10);
		
		controller.addPropertyChangeListener( AnimationController.TIME_PROPERTY, new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				refreshImages();
			}
		});
		refreshImages();
	}
	
	protected static Color tween(Color c1,Color c2,float f) {
		return new Color(
				(int)(c1.getRed()*(1-f) + c2.getRed()*f),
				(int)(c1.getGreen()*(1-f) + c2.getGreen()*f),
				(int)(c1.getBlue()*(1-f) + c2.getBlue()*f) );
	}
	
	private void refreshImages() {
		Color c3 = tween(c1, c2, controller.getTime()/controller.getDuration());
		
		{
			Paint p = new GradientPaint(0, 0, c1, width, 0, c3);
			Graphics2D g = defaultPaint.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
			g.setPaint(p);
			g.fillRect(0,0,defaultPaint.getWidth(),defaultPaint.getHeight());
			g.dispose();
		}
		
		{
			Paint p = new GradientTexturePaint(0, 0, c1, width, 0, c3);
			Graphics2D g = diffusedPaint.createGraphics();
			g.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
			g.setPaint(p);
			g.fillRect(0,0,diffusedPaint.getWidth(),diffusedPaint.getHeight());
			g.dispose();
		}

		zoom(defaultPaint, defaultPaintZoom);
		zoom(diffusedPaint, diffusedPaintZoom);
		
		getContentPane().repaint();
	}
	
	/** This mods each color component by two and then amplifies that
	 * value to show when pixel values change.
	 * @param source the incoming image
	 * @param dest the image to store the new representation in.
	 */
	private void zoom(BufferedImage source,BufferedImage dest) {
		int[] row = new int[width];
		for(int y = 0; y<height; y++) {
			source.getRaster().getDataElements(0, y, width, 1, row);
			for(int x = 0; x<row.length; x++) {
				int r = (row[x] >> 16 ) & 0xff;
				int g = (row[x] >> 8 ) & 0xff;
				int b = (row[x] >> 0 ) & 0xff;
				r = (r%2)*(256/2);
				g = (g%2)*(256/2);
				b = (b%2)*(256/2);
				row[x] = 0xff000000 + ( r << 16) + ( g << 8) + b;
			}
			dest.getRaster().setDataElements(0, y, width, 1, row);
		}
	}
}
