/*
 * @(#)GifWriterDemo.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.image.gif;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import com.bric.animation.ResettableAnimationReader;

public class GifWriterDemo {

	static class DemoAnimationReader implements ResettableAnimationReader {
		int ctr = 0;
		
		int w, h;
		Paint transparentPattern;
		
		DemoAnimationReader(int width,int height) {
			this.w = width;
			this.h = height;
			int k = 64;
			BufferedImage patternImage = new BufferedImage(k, k, BufferedImage.TYPE_INT_ARGB);
			
			Graphics2D g = patternImage.createGraphics();
			g.setColor(new Color(0x44decb));
			g.fillRect(0,0,k/2,k/2);
			g.setColor(new Color(0x35ccab));
			g.fillRect(k/2,k/2,k/2,k/2);
			g.dispose();

			transparentPattern = new TexturePaint(patternImage, new Rectangle(0,0,k,k));
		}
		
		public BufferedImage getNextFrame(boolean cloneImage)
				throws IOException {
			if(ctr==getFrameCount()) return null;
			
			BufferedImage bi = new BufferedImage(
					getWidth(),
					getHeight(),
					BufferedImage.TYPE_INT_ARGB
					);
			Graphics2D g = bi.createGraphics();
			g.setComposite(AlphaComposite.Clear);
			g.fillRect(0,0,bi.getWidth(),bi.getHeight());
			g.setComposite(AlphaComposite.SrcOver);
			g.setPaint(transparentPattern);
			g.fillRect(0,0,bi.getWidth(),bi.getHeight());
			
			g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setFont(new Font("Verdana",0,h*12/100));
			FontMetrics fm = g.getFontMetrics();
			Rectangle2D rect = fm.getStringBounds(""+ctr, g);
			
			float r = (float)(Math.sqrt(rect.getWidth()*rect.getWidth()+rect.getHeight()*rect.getHeight()))/2f;
			Ellipse2D e = new Ellipse2D.Float( bi.getWidth()/2-r, bi.getHeight()/2-r, 2*r, 2*r );
			g.setColor(new Color(0xbbccfb));
			g.fill(e);
			g.setColor(Color.black);
			g.setStroke(new BasicStroke(3));
			g.draw(e);
			
			g.drawString(""+ctr, (float)(bi.getWidth()/2-rect.getWidth()/2), (float)(bi.getHeight()/2+rect.getHeight()/2-fm.getDescent()) );
			g.dispose();
			ctr++;
			return bi;
		}

		public double getDuration() {
			return getFrameCount()*getFrameDuration();
		}

		public int getFrameCount() {
			return 200;
		}

		public int getLoopCount() {
			return LOOP_FOREVER;
		}

		public double getFrameDuration() {
			return .1;
		}

		public int getWidth() {
			return w;
		}

		public int getHeight() {
			return h;
		}

		public void reset() {
			ctr = 0;
		}
		
	}

	/** Launch the GifWriterDemo app. 
     * @param args the application's arguments. (This is unused.)
     */
	public static void main(String[] args) {
		File destFile = new File("GifWriterDemo.gif");
		try {
			GifWriter.write(destFile, new DemoAnimationReader(400, 300), GifWriter.ColorReduction.FROM_FIRST_FRAME);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
}
