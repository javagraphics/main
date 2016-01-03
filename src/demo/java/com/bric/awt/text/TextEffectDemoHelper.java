/*
 * @(#)TextEffectDemoHelper.java
 *
 * $Date: 2014-05-07 01:05:32 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.awt.text;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.bric.animation.AnimationReader;
import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;

/** Helper methods for the TextEffectDemo.
 */
public class TextEffectDemoHelper extends BlogHelper
{
	static Font impact = new Font("Impact", 0, 48);
	static Font arialBlack = new Font("Arial Black", 0, 48);
	
	public static File createExplodeDemo(File dir) {
		Dimension d = new Dimension(400, 80);
		File gifFile = new File(dir, "explode-text-effect.gif");
		TextEffect effect = new ExplodeTextEffect(impact, "kalopsia", d.width, d.height);
		write(gifFile, effect, d);
		return gifFile;
	}
	
	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		BufferedImage bi = new BufferedImage(preferredSize.width,preferredSize.height, BufferedImage.TYPE_INT_ARGB);
		Font font = new Font("Impact",0,(int)(preferredSize.height*.7f));
		TextEffect effect = new WaveTextEffect(font, "eek!", bi.getWidth(), bi.getHeight());
		Graphics2D g = bi.createGraphics();
		effect.paint(g, .5f);
		g.dispose();
		return bi;
	}
	
	public static File createOutlineDemo(File dir) {
		Dimension d = new Dimension(400, 80);
		File gifFile = new File(dir, "outline-text-effect.gif");
		TextEffect effect = new OutlineTextEffect(impact, "shenanigans", d.width, d.height);
		write(gifFile, effect, d);
		return gifFile;
	}
	
	public static File createSimpleImage(File dir) throws Exception {
		Dimension d = new Dimension(400, 80);
		String s = "folderol";
		BlockLetter[] letters = new BlockLetter[s.length()];
		float width = 0;
		int padding = 3;
		for(int a = 0; a<letters.length; a++) {
			char c = s.charAt(a);
			letters[a] = new BlockLetter.Simple(c, impact, new Color(0xdd8822));
			width += letters[a].getCharWidth() + padding;
			letters[a].setDepth(15);
		}
		
		BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,d.width,d.height);
		
		float x = d.width/2 - width/2;
		float y = d.height*3/4;
		for(int a = 0; a<letters.length; a++) {
			letters[a].paint(g, x, y);
			x += letters[a].getCharWidth() + padding;
		}
		
		g.dispose();
		

		File pngFile = new File(dir, "simple-block-text.png");
		ImageIO.write(bi, "png", pngFile);
		return pngFile;
	}
	
	public static File createGradientImage(File dir) throws Exception {
		Dimension d = new Dimension(400, 80);
		String s = "gardyloo";
		BlockLetter[] letters = new BlockLetter[s.length()];
		float width = 0;
		int padding = 3;
		for(int a = 0; a<letters.length; a++) {
			char c = s.charAt(a);
			letters[a] = new BlockLetter.Gradient(c, impact, new Color(250, 240, 140)) {

				@Override
				protected Color[] getGradientColors(double theta) {
					Color c = new Color(
						(int)(200+50*Math.sin(4*theta)), 
						(int)(180+60*Math.sin(4*theta)),
						Math.max(0, (int)(40+100*Math.sin(4*theta)) )
					);
					return new Color[] { c, Color.white };
				}
				
			};
			width += letters[a].getCharWidth() + padding;
			letters[a].setDepth(15);
		}
		
		BufferedImage bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = bi.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0,0,d.width,d.height);
		
		float x = d.width/2 - width/2;
		float y = d.height*3/4;
		for(int a = 0; a<letters.length; a++) {
			letters[a].paint(g, x, y);
			x += letters[a].getCharWidth() + padding;
		}
		
		g.dispose();
		

		File pngFile = new File(dir, "gradient-block-text.png");
		ImageIO.write(bi, "png", pngFile);
		return pngFile;
	}
	
	public static File createPunchDemo(File dir) {
		Dimension d = new Dimension(400, 80);
		File gifFile = new File(dir, "punch-text-effect.gif");
		TextEffect effect = new PunchTextEffect(impact, "skedaddle", d.width, d.height);
		write(gifFile, effect, d);
		return gifFile;
	}
	
	public static File createWaveDemo(File dir) {
		Dimension d = new Dimension(400, 80);
		File gifFile = new File(dir, "wave-text-effect.gif");
		TextEffect effect = new WaveTextEffect(impact, "octothorpe", d.width, d.height);
		write(gifFile, effect, d);
		return gifFile;
	}
	
	private static void write(File gifFile,final TextEffect effect,final Dimension d) {
		final int frameCount = 160;
		AnimationReader anim = new ResettableAnimationReader() {

			BufferedImage bi;
			int ctr = 0;
			
			public BufferedImage getNextFrame(boolean cloneImage)
					throws IOException {
				if(ctr==frameCount) return null;
				if(bi==null || cloneImage) {
					bi = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_RGB);
				}
				Graphics2D g = bi.createGraphics();
				g.setColor(Color.white);
				g.fillRect(0,0,d.width,d.height);
				float f = ((float)ctr)/((float)(frameCount-1));
				effect.paint(g, f);
				g.dispose();
				ctr++;
				return bi;
			}

			public double getDuration() {
				double sum = 0;
				for(int a = 0; a<getFrameCount(); a++) {
					sum += getFrameDuration(a);
				}
				return sum;
			}

			public int getFrameCount() {
				return frameCount;
			}

			public int getLoopCount() {
				return AnimationReader.LOOP_FOREVER;
			}
			
			public double getFrameDuration() {
				return getFrameDuration(ctr - 1);
			}

			public double getFrameDuration(int frameIndex) {
				if(frameIndex==getFrameCount()-1)
					return 1.0;
				return .010;
			}

			public int getWidth() {
				return d.width;
			}

			public int getHeight() {
				return d.height;
			}

			public void reset() {
				ctr = 0;
			}
		};

		try {
			GifWriter.write(gifFile, anim, ColorReduction.FROM_ALL_FRAMES);
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
}
