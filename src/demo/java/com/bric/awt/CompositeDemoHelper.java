/*
 * @(#)CompositeDemoHelper.java
 *
 * $Date: 2014-11-27 07:56:34 +0100 (Do, 27 Nov 2014) $
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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.TexturePaint;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import com.bric.animation.AnimationReader;
import com.bric.animation.ResettableAnimationReader;
import com.bric.awt.CompositeDemo.CompositePreview;
import com.bric.blog.BlogHelper;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;
import com.bric.reflect.Reflection;

public class CompositeDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		CompositeDemo demo = new CompositeDemo();
		demo.alpha.setValue(50);
		CompositePreview preview = demo.preview;
		preview.cleanDemo = true;
		preview.setSize(preview.getPreferredSize());
		BufferedImage image = new BufferedImage(preview.getWidth(), preview.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		preview.paint(g);
		g.dispose();
		return image;
	}

	
	public static File createAnimation(File dir) throws ClassNotFoundException, IOException {
		dir = new File(dir, "alphaComposites");
		if(!dir.mkdirs())
			throw new RuntimeException("mkdirs failed for "+dir.getAbsolutePath());
		
		AlphaComposite[] composites = new AlphaComposite[] {
				AlphaComposite.Clear,
				AlphaComposite.Dst,
				AlphaComposite.DstAtop,
				AlphaComposite.DstIn,
				AlphaComposite.DstOut,
				AlphaComposite.DstOver,
				AlphaComposite.Src,
				AlphaComposite.SrcAtop,
				AlphaComposite.SrcIn,
				AlphaComposite.SrcOut,
				AlphaComposite.SrcOver,
				AlphaComposite.Xor
		};
		
		for(AlphaComposite composite : composites) {
			String name = getName(composite);
			name = name.replace(" ", "");
			name = name.replace("[", "");
			name = name.replace("]", "");
			if(name.contains(",")) {
				name = name.substring(0,name.indexOf(","));
			}
			ResettableAnimationReader animation = createAnimation(composite, name, false);
			File gifFile = new File(dir, name+".gif");
			GifWriter.write(gifFile, animation, ColorReduction.FROM_ALL_FRAMES);

			animation = createAnimation(composite, name, true);
			gifFile = new File(dir, name+"_I.gif");
			GifWriter.write(gifFile, animation, ColorReduction.FROM_ALL_FRAMES);
		}
		
		return dir;
	}


	private static ResettableAnimationReader createAnimation(
			final AlphaComposite composite,final String name,final boolean addUsingImage) {
		return new ResettableAnimationReader() {
			int width = 150;
			int animHeight = 100;
			int textHeight = 18;
			int height = animHeight + textHeight;
			
			int frameCtr = 0;
			int totalFrames = 100;

			BufferedImage scratch = new BufferedImage(width, animHeight, BufferedImage.TYPE_INT_ARGB);
			BufferedImage bi = null;
			public BufferedImage getNextFrame(boolean cloneImage)
					throws IOException {
				if(frameCtr == totalFrames) return null;
				
				Graphics2D g = scratch.createGraphics();
				g.setComposite(AlphaComposite.Clear);
				g.fillRect(0,0,width,animHeight);
				g.setRenderingHints(qualityHints);
				g.setComposite(AlphaComposite.SrcOver);
				g.setPaint(new GradientPaint(0,0,new Color(255,100,100,0),0,animHeight,new Color(255,100,100)));
				g.fill(new Ellipse2D.Float(0,0,100,100));
				float opacity = ((float)frameCtr)/((float)totalFrames-1f);
				g.setComposite(composite.derive(opacity));
				if(addUsingImage) {
					g.drawImage(getSourceImage(), 0, 0, null);
				} else {
					g.setPaint(new GradientPaint(0,animHeight,new Color(100,100,255,0),0,0,new Color(100,100,255)));
					g.fill(new Ellipse2D.Float(50,0,100,100));
				}
				g.dispose();
				
				if(bi==null || cloneImage) {
					bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
				}
				g = bi.createGraphics();
				g.setRenderingHints(qualityHints);
				g.setColor(Color.white);
				g.fillRect(0,0,width,height);
				g.setPaint(getCheckerPaint(4));
				g.fillRect(0,textHeight,width,animHeight);
				g.drawImage(scratch, 0, textHeight, null);

				DecimalFormat format = new DecimalFormat("#");
				String numberString = format.format(opacity*100)+"%";
				BlogHelper.drawString(g, numberString, 105, textHeight-4,false);
				BlogHelper.drawString(g, name, 5, textHeight-4,false);
				
				g.dispose();
				
				frameCtr++;
				
				return bi;
			}

			BufferedImage srcImage = null;
			private Image getSourceImage() {
				if(srcImage==null) {
					srcImage = new BufferedImage(width, animHeight, BufferedImage.TYPE_INT_ARGB);
					Graphics2D g = srcImage.createGraphics();
					g.setPaint(new GradientPaint(0,animHeight,new Color(100,100,255,0),0,0,new Color(100,100,255)));
					g.fill(new Ellipse2D.Float(50,0,100,100));
					g.dispose();
				} 
				return srcImage;
			}

			private Paint getCheckerPaint(int tileSize) {
				BufferedImage bi = new BufferedImage(2*tileSize, 2*tileSize, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = bi.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0,0,bi.getWidth(),bi.getHeight());
				g.setColor(Color.lightGray);
				g.fillRect(0,0,tileSize,tileSize);
				g.fillRect(tileSize,tileSize,tileSize,tileSize);
				g.dispose();
				return new TexturePaint(bi, new Rectangle(0,0,bi.getWidth(),bi.getHeight()));
			}

			public double getDuration() {
				double sum = 0;
				for(int a = 0; a<getFrameCount(); a++) {
					sum += getFrameDuration(a);
				}
				return sum;
			}
			
			private double getFrameDuration(int index) {
				if(index==totalFrames-1 || index==0) {
					return 2;
				}
				return .04;
			}

			public int getFrameCount() {
				return totalFrames;
			}

			public int getLoopCount() {
				return AnimationReader.LOOP_FOREVER;
			}

			public double getFrameDuration() {
				return getFrameDuration(frameCtr-1);
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
	}


	private static String getName(AlphaComposite composite) {
		return Reflection.nameStaticField(AlphaComposite.class, composite.getRule());
	}
}
