/*
 * @(#)StrokeHelper.java
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
package com.bric.awt;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;

import javax.imageio.ImageIO;

import com.bric.animation.AnimationReader;
import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.geom.ParametricPathIterator;
import com.bric.image.ImageBounds;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

public class StrokeHelper extends BlogHelper {
	
	protected synchronized static File getSampleDir(File resourcesDir) {
		File file = new File(resourcesDir, "strokes");
		if( (!file.exists()) && (!file.mkdirs()) )
			throw new RuntimeException("mkdirs failed for "+file.getAbsolutePath());
		return file;
	}
	
	public static void createLinearSample(File pngFile,
			Stroke stroke1,
			Stroke stroke2,
			Stroke stroke3) throws IOException {
		GeneralPath[] paths = new GeneralPath[3];
		float x = 20;
		int j = 50;
		
		paths[0] = new GeneralPath();
		paths[1] = new GeneralPath();
		paths[2] = new GeneralPath();
		
		paths[1].moveTo(x, j+30);
		paths[0].moveTo(x, j+50);
		paths[2].moveTo(x, 10);
		while(x<900) {
			paths[1].lineTo(x + j, j + 30);
			paths[1].lineTo(x + j, 30);
			paths[1].lineTo(x + 2*j, 30);
			paths[1].lineTo(x + 2*j, j + 30);
			
			float z1 = x + j + 10;
			float z2 = x + 2*j - 10;
			float p = j + 50;
			paths[0].lineTo( z1, p );
			paths[0].lineTo( (z1+z2)/2, p - 55 );
			paths[0].lineTo( z2, p );
			paths[0].lineTo( x + 2*j, p );

			z1 = x + 10;
			z2 = x + j - 10;
			p = 10;
			paths[2].lineTo( z1, p );
			paths[2].lineTo( (z1+z2)/2, p + 45 );
			paths[2].lineTo( z2, p );
			paths[2].lineTo( x + 2*j, p );
			
			x += 2*j;
		}
		
		BufferedImage bi = new BufferedImage(1000, 500, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setRenderingHints(qualityHints);
		g.setColor(Color.black);
		g.setStroke(stroke1);
		g.draw(paths[0]);
		g.setStroke(stroke2);
		g.draw(paths[1]);
		g.setStroke(stroke3);
		g.draw(paths[2]);
		g.dispose();
		Rectangle r = ImageBounds.getBounds(bi);
		ImageIO.write(bi.getSubimage(r.x, r.y, r.width, r.height), "png", pngFile);
	}
	
	/** This is askew, but I really like how it looks so I'm not inclined to fix it...
	 *
	 */
	protected static class SinePathIterator extends ParametricPathIterator {
		double k;
		
		public SinePathIterator(double height) {
			super(null);
			k = height;
		}
		
		@Override
		protected double getX(double t) {
			return 100*t;
		}

		@Override
		protected double getY(double t) {
			return k*Math.sin(2*Math.PI*t);
		}

		@Override
		protected double getDX(double t) {
			return 100;
		}

		@Override
		protected double getDY(double t) {
			return 2*Math.PI*k*Math.cos(2*Math.PI*t);
		}

		@Override
		protected double getMaxT() {
			return 10;
		}

		@Override
		protected double getNextT(double t) {
			return t+1;
		}
		
	}

	/** Create a sinusoidal wave of each stroke. Each stroke is separated vertically
	 * by 50 pixels.
	 * 
	 * @param pngFile the png file to write to.
	 * @param strokes the strokes to write.
	 * @throws IOException if an error occurs during IO.
	 */
	public static void createWavySamples(File pngFile,Stroke... strokes) throws IOException {
		GeneralPath wavyShape = new GeneralPath();
		
		wavyShape.append(new SinePathIterator(50), false);
		BufferedImage bi = new BufferedImage(800, 500, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = bi.createGraphics();
		g.setRenderingHints(qualityHints);
		g.setColor(Color.black);
		for(Stroke stroke : strokes) {
			g.translate(0,  50);
			g.setStroke(stroke);
			g.draw(wavyShape);
		}
		g.dispose();
		Rectangle r = ImageBounds.getBounds(bi);
		ImageIO.write(bi.getSubimage(r.x, r.y, r.width, r.height), "png", pngFile);
	}

	/** Create a sinusoidal wave of each stroke. Each stroke is separated vertically
	 * by 50 pixels.
	 * 
	 * @param gifFile the gif file to write to.
	 * @param constructor the stroke constructor to use
	 * @param text the text to paint in blue in the corner
	 * @param strokeWidths the widths of strokes to use
	 * @throws IOException if an error occurs during IO.
	 */
	public static void createWavySamples(File gifFile,final Constructor<?> constructor,final String text,final float... strokeWidths) throws IOException {

		final int width = 800;
		final int height = 50*(strokeWidths.length+1);
		ResettableAnimationReader animation = new ResettableAnimationReader() {
			int frameCtr = 0;
			int total = 100;
			
			BufferedImage bi;
			public BufferedImage getNextFrame(boolean cloneImage)
					throws IOException {
				if(frameCtr==total) return null;
				if(bi==null || cloneImage) {
					bi = new BufferedImage(width, height+10, BufferedImage.TYPE_INT_RGB);
				}
				
				float thickness = ((float)frameCtr)/((float)(total-1));

				GeneralPath wavyShape = new GeneralPath();
				wavyShape.append(new SinePathIterator(50), false);
				Graphics2D g = bi.createGraphics();
				g.setRenderingHints(qualityHints);
				g.setColor(Color.white);
				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
				g.setColor(Color.black);
				for(int a = 0; a<strokeWidths.length; a++) {
					Graphics2D g2 = (Graphics2D)g.create();
					try {
						Stroke stroke = (Stroke)constructor.newInstance(new Object[] { strokeWidths[a], thickness });
						g2.translate(0,  (a+1)*50);
						g2.setStroke(stroke);
						g2.draw(wavyShape);
					} catch(Throwable t) {
						throw new RuntimeException(t);
					} finally {
						g2.dispose();
					}
				}
				g.setColor(Color.blue);
				g.setFont(new Font("Verdana", 0, 14));
				DecimalFormat formatter = new DecimalFormat("#.##");
				String s = text+": "+formatter.format(thickness);
				g.drawString(s, 5, bi.getHeight()-5);
				g.dispose();
				
				frameCtr++;
				
				return bi;
			}
			
			public double getDuration() {
				double s = 0;
				for(int a = 0; a<getFrameCount(); a++) {
					s += getFrameDuration(a);
				}
				return s;
			}
			
			public int getFrameCount() {
				return total;
			}
			
			public int getLoopCount() {
				return AnimationReader.LOOP_FOREVER;
			}
			
			public double getFrameDuration() {
				return getFrameDuration(frameCtr-1);
			}
			
			private double getFrameDuration(int frameIndex) {
				if(frameIndex==0 || frameIndex==total-1) {
					return 2;
				}
				return .1;
			}
			
			public int getWidth() {
				return width;
			}
			
			public int getHeight() {
				return height;
			}
			
			public void reset() {
				frameCtr = 0;
			}
		};

		GifWriter.write(gifFile, animation, ColorReduction.FROM_FIRST_FRAME);
	}
}
