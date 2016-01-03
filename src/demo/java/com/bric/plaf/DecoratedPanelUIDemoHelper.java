/*
 * @(#)DecoratedPanelUIDemoHelper.java
 *
 * $Date: 2015-06-26 18:18:22 +0200 (Fr, 26 Jun 2015) $
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
package com.bric.plaf;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.SoftBevelBorder;
import javax.swing.plaf.PanelUI;

import com.bric.animation.AnimationReader;
import com.bric.animation.ResettableAnimationReader;
import com.bric.blog.BlogHelper;
import com.bric.image.gif.GifWriter;
import com.bric.image.gif.GifWriter.ColorReduction;
import com.bric.plaf.DecoratedPanelUIDemo.MiddlePanel;
import com.bric.swing.resources.GlyphIcon;
import com.bric.util.ObservableProperties.Key;


public class DecoratedPanelUIDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		BufferedImage bi = new BufferedImage(250, 250, BufferedImage.TYPE_INT_ARGB);
		paintUI(DecoratedPanelUI.createPlasticUI(), bi, 250, 250);
		return bi;
	}
	
	protected static void paintUI(final PanelUI ui,final BufferedImage bi,final int w,final int h) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					JPanel panel = new JPanel(new GridBagLayout());
					panel.setOpaque(false);
					panel.setUI( ui );
					GridBagConstraints c = new GridBagConstraints();
					c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
					c.fill = GridBagConstraints.BOTH;
					panel.add(new MiddlePanel(null), c);
					panel.setSize(new Dimension(w, h));
					panel.getLayout().layoutContainer(panel);
					panel.validate();
					
					Graphics2D g = bi.createGraphics();
					panel.paint(g);
					g.dispose();
				}
			});
		} catch(RuntimeException e) {
			throw e;
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public static File createSamples(File dir) throws Exception {
		File screenshotDir = new File(dir, "decorated-panel");
		if(!screenshotDir.mkdirs())
			throw new IllegalArgumentException("mkdirs failed for "+screenshotDir.getAbsolutePath());
	
		{
			File screenshot = new File(screenshotDir, "screenshot.png");
			final DecoratedPanelUIDemo demo = new DecoratedPanelUIDemo();
			SwingUtilities.invokeAndWait(new Runnable() {
				public void run() {
					demo.plasticButton.doClick();
				}
			});
			BufferedImage bi = paint(demo.getContentPane(), new Dimension(800, 600));
			ImageIO.write(bi, "png", screenshot);
		}
		
		//used in the blog article for comparison, but not part of com.bric code:
		{
			File softBevelDemo = new File(screenshotDir, "soft-bevel.png");
			final BufferedImage bi = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						JPanel panel = new JPanel();
						panel.setBackground(Color.white);
						panel.setOpaque(true);
						JPanel innerPanel = new JPanel();
						innerPanel.setOpaque(true);
						innerPanel.setBackground(new Color(0xDDDDDD));
						innerPanel.setBorder(new SoftBevelBorder(SoftBevelBorder.LOWERED, 
								new Color(0xEEEEEE), new Color(0xFFFFFF),
								new Color(0xBBBBBB), new Color(0xCCCCCC)) );
						panel.setLayout(new GridBagLayout());
						GridBagConstraints c = new GridBagConstraints();
						c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
						c.fill = GridBagConstraints.BOTH;
						c.insets = new Insets(10,10,10,10);
						panel.add(innerPanel, c);

						panel.setPreferredSize(new Dimension(100, 100));
						panel.setSize(new Dimension(100, 100));
						panel.getLayout().layoutContainer(panel);
						panel.validate();
						
						Graphics2D g = bi.createGraphics();
						panel.paint(g);
						g.dispose();
					}
				});
			} catch(RuntimeException e) {
				throw e;
			} catch(Throwable t) {
				throw new RuntimeException(t);
			}
			ImageIO.write(bi, "png", softBevelDemo);
		}
		
		Method[] methods = DecoratedPanelUI.class.getMethods();
		for(Method method : methods) {
			if(method.getParameterTypes().length==0 && 
					method.getReturnType().equals(DecoratedPanelUI.class) && 
					isStatic(method)) {
				DecoratedPanelUI ui = (DecoratedPanelUI)method.invoke(null, new Object[] {});
				createScreenshot(screenshotDir, method.getName(), ui);
			}
		}
		
		{
			File glazeFile = new File(screenshotDir, "glaze-demo.gif");
			DecoratedPanelUI ui = new DecoratedPanelUI();
			ui.setGlazeOpacity(0);
			ui.setGlazeLevel(0);
			AttributeAnimation anim = new AttributeAnimation(ui,
					DecoratedPanelUI.GLAZE_OPACITY,
					DecoratedPanelUI.GLAZE_LEVEL );
			anim.addChapter( DecoratedPanelUI.GLAZE_OPACITY, 0f, 1f);
			anim.addChapter( DecoratedPanelUI.GLAZE_LEVEL, 0f, 1f);
			anim.addChapter( DecoratedPanelUI.GLAZE_OPACITY, 1f, 0f);
			anim.reset();
			GifWriter.write(glazeFile, anim, ColorReduction.FROM_ALL_FRAMES);
		}
		
		{
			File bevelFile = new File(screenshotDir, "bevel-demo.gif");
			DecoratedPanelUI ui = new DecoratedPanelUI();
			ui.setTopColor(new Color(0x999999));
			ui.setBottomColor(new Color(0x777777));
			ui.getBorder().setStrokeWidth(1);
			ui.getBorder().setBorderPaint(Color.darkGray);
			ui.getBorder().setCornerSize(30);
			ui.getBorder().setBevelShadowMaxBlend(.5f);
			ui.getBorder().setBevelHighlightMaxBlend(.5f);
			ui.getBorder().setBevelShadowAngle(0f);
			ui.getBorder().setBevelShadowLayerCount(0);
			ui.getBorder().setBevelHighlightLayerCount(0);
			AttributeAnimation anim = new AttributeAnimation(ui, 
					DecoratedPanelUI.Border.BEVEL_SHADOW_LAYER_COUNT,
					DecoratedPanelUI.Border.BEVEL_HIGHLIGHT_LAYER_COUNT,
					DecoratedPanelUI.Border.BEVEL_SHADOW_THETA,
					DecoratedPanelUI.Border.BEVEL_SHADOW_MAX_BLEND,
					DecoratedPanelUI.Border.BEVEL_HIGHLIGHT_MAX_BLEND
				);
			anim.addChapter( DecoratedPanelUI.Border.BEVEL_SHADOW_LAYER_COUNT, 0, 10);
			anim.addChapter( DecoratedPanelUI.Border.BEVEL_HIGHLIGHT_LAYER_COUNT, 0, 10);
			anim.addChapter( DecoratedPanelUI.Border.BEVEL_SHADOW_THETA, 0, (float)(2*Math.PI) );
			anim.addChapter( DecoratedPanelUI.Border.BEVEL_SHADOW_MAX_BLEND, .5f, 0);
			anim.addChapter( DecoratedPanelUI.Border.BEVEL_HIGHLIGHT_MAX_BLEND, .5f, 0);
			anim.reset();
			GifWriter.write(bevelFile, anim, ColorReduction.FROM_ALL_FRAMES);
		}
		
		{
			File scribbleFile = new File(screenshotDir, "scribble-demo.gif");
			DecoratedPanelUI ui = new DecoratedPanelUI();
			ui.setTopColor(new Color(0xFFFFFF));
			ui.setBottomColor(new Color(0xDDDDDD));
			ui.getBorder().setStrokeWidth(1);
			ui.getBorder().setBorderPaint(Color.lightGray);
			ui.getBorder().setDropShadowAlpha(.0125f);
			ui.getBorder().setDropShadowLayerCount(10);
			AttributeAnimation anim = new AttributeAnimation(ui, 
					DecoratedPanelUI.Border.SCRIBBLE_SIZE
				);
			anim.addChapter( DecoratedPanelUI.Border.SCRIBBLE_SIZE, 0, 5);
			anim.addChapter( DecoratedPanelUI.Border.SCRIBBLE_SIZE, 5, 0);
			anim.reset();
			GifWriter.write(scribbleFile, anim, ColorReduction.FROM_ALL_FRAMES);
		}
		
		{
			File cornerFile = new File(screenshotDir, "corner-demo.gif");
			DecoratedPanelUI ui = new DecoratedPanelUI();
			ui.setTopColor(new Color(0xDDDDDD));
			ui.setBottomColor(new Color(0xAAAAAA));
			ui.getBorder().setStrokeWidth(1);
			ui.getBorder().setBorderPaint(Color.darkGray);
			AttributeAnimation anim = new AttributeAnimation(ui, 
					DecoratedPanelUI.Border.CORNER_SIZE_LOWER_RIGHT,
					DecoratedPanelUI.Border.CURVE_LOWER_RIGHT
				);
			anim.addChapter( DecoratedPanelUI.Border.CORNER_SIZE_LOWER_RIGHT, 0, 100);
			anim.addChapter( DecoratedPanelUI.Border.CURVE_LOWER_RIGHT, 1, 0);
			anim.addChapter( DecoratedPanelUI.Border.CORNER_SIZE_LOWER_RIGHT, 100, 0 );
			anim.reset();
			GifWriter.write(cornerFile, anim, ColorReduction.FROM_ALL_FRAMES);
		}
		
		{
			File shadowFile = new File(screenshotDir, "shadow-demo.gif");
			DecoratedPanelUI ui = new DecoratedPanelUI();
			ui.setTopColor(new Color(0xFFFFFF));
			ui.getBorder().setCornerSize(20);
			ui.setBottomColor(new Color(0xDDDDDD));
			ui.getBorder().setStrokeWidth(.5f);
			ui.getBorder().setBorderPaint(Color.gray);
			ui.getBorder().setDropShadowAlpha(.02f);
			ui.getBorder().setDropShadowLayerCount(0);
			AttributeAnimation anim = new AttributeAnimation(ui, 
					DecoratedPanelUI.Border.DROP_SHADOW_ALPHA,
					DecoratedPanelUI.Border.DROP_SHADOW_LAYER_COUNT
				);
			anim.addChapter( DecoratedPanelUI.Border.DROP_SHADOW_LAYER_COUNT, 0, 10);
			anim.addChapter( DecoratedPanelUI.Border.DROP_SHADOW_ALPHA, .02, .01);
			anim.addChapter( DecoratedPanelUI.Border.DROP_SHADOW_LAYER_COUNT, 10, 0);
			anim.reset();
			GifWriter.write(shadowFile, anim, ColorReduction.FROM_ALL_FRAMES);
		}
		
		return screenshotDir;
	}
	
	private static class Chapter<T> {
		final Key<T> key;
		final T initialValue, finalValue;
		
		Chapter(Key<T> key,T initialValue,T finalValue) {
			this.key = key;
			this.initialValue = initialValue;
			this.finalValue = finalValue;
		}
		
		public T tween(float f) {
			if(f<0 || f>1) throw new IllegalArgumentException(""+f);
			if( key.getType().equals(Integer.class) ) {
				int i1 = ((Number)initialValue).intValue();
				int i2 = ((Number)finalValue).intValue();
				int v = (int)( i1*(1-f) + i2*f+.5f );
				return (T)(new Integer(v));
			} else if( key.getType().equals(Float.class) ) {
				float i1 = ((Number)initialValue).floatValue();
				float i2 = ((Number)finalValue).floatValue();
				float v = i1*(1-f) + i2*f;
				return (T)(new Float(v));
			}
			throw new RuntimeException("unsupported type: "+key.getType().getName());
		}
	}
	
	private static class AttributeAnimation implements ResettableAnimationReader {
		int frameCtr = 0;
		int framesPerChapter = 50;
		List<Chapter<?>> chapters = new ArrayList<Chapter<?>>();
		int imageWidth = 200;
		int imageHeight = 150;
		int textSize = 20;
		DecoratedPanelUI ui;
		Map<Key<?>, Object> originalAttributes = new HashMap<Key<?>, Object>();
		
		AttributeAnimation(DecoratedPanelUI ui, Key<?>... keysOfInterest) {
			this.ui = ui;
			for(Key<?> key : keysOfInterest) {
				if(DecoratedPanelUI.isSupported(key)) {
					originalAttributes.put(key, ui.getProperty(key));
				} else {
					originalAttributes.put(key, ui.getBorder().getProperty(key));
				}
			}
		}
		
		public void addChapter(Key<?> key,Object initialValue,Object finalValue) {
			chapters.add(new Chapter(key, initialValue, finalValue));
		}

		BufferedImage bi;
		public BufferedImage getNextFrame(boolean cloneImage)
				throws IOException {
			if(frameCtr==getFrameCount()) return null;
			
			if(bi==null || cloneImage) {
				bi = new BufferedImage(imageWidth, imageHeight + textSize, BufferedImage.TYPE_INT_RGB);
			}
			Graphics2D g = bi.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0,0,bi.getWidth(),bi.getHeight());
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

			int chapterIndex = frameCtr/framesPerChapter;
			Chapter chapter = chapters.get(chapterIndex);
			float progress = ((float)(frameCtr - chapterIndex*framesPerChapter)) / ((float)framesPerChapter);
			
			if(DecoratedPanelUI.isSupported(chapter.key)) {
				ui.setProperty(chapter.key, chapter.tween(progress));
			} else {
				ui.getBorder().setProperty(chapter.key, chapter.tween(progress));
			}
			
			paintUI(ui, bi, imageWidth, imageHeight);
			if(frameCtr%framesPerChapter!=0) {
				g.setColor(Color.black);
				g.setFont(new Font("Verdana", 0, 12));
				Object v = chapter.tween(progress);
				if(v instanceof Number) {
					DecimalFormat f = new DecimalFormat("#.##");
					v = f.format( ((Number)v).doubleValue() );
				}
				g.drawString( chapter.key.toString()+": "+v , 5, imageHeight + textSize*4/5);
			}
			
			frameCtr++;
			
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
			return chapters.size()*framesPerChapter;
		}

		public int getLoopCount() {
			return AnimationReader.LOOP_FOREVER;
		}

		public double getFrameDuration() {
			return getFrameDuration(frameCtr-1);
		}

		public double getFrameDuration(int index) {
			if(index%framesPerChapter==0) {
				return 3;
			}
			return .1;
		}

		public int getWidth() {
			return imageWidth;
		}

		public int getHeight() {
			return imageHeight + textSize;
		}

		public void reset() {
			frameCtr = 0;

			for(Key<?> key : originalAttributes.keySet()) {
				Object v = originalAttributes.get(key);
				set(key, v);
			}
		}
		
		private <T> void set(Key<T> key,Object v) {
			if(DecoratedPanelUI.isSupported(key)) {
				ui.setProperty(key, (T)v);
			} else {
				ui.getBorder().setProperty(key, (T)v);
			}
		}
		
	}
	
	private static File createScreenshot(File dir,String name,final DecoratedPanelUI ui) throws Exception {
		final BufferedImage bi = new BufferedImage(400, 250, BufferedImage.TYPE_INT_ARGB);
		
		SwingUtilities.invokeAndWait(new Runnable() {
			public void run() {
				JPanel panel = new JPanel(new GridBagLayout());
				panel.setOpaque(false);
				panel.setUI( ui );
				GridBagConstraints c = new GridBagConstraints();
				c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
				c.fill = GridBagConstraints.BOTH;
				MiddlePanel middlePanel = new MiddlePanel(GlyphIcon.RECYCLE);
				middlePanel.installText();
				panel.add(middlePanel, c);
				panel.setSize(new Dimension(bi.getWidth(), bi.getHeight()));
				panel.getLayout().layoutContainer(panel);
				panel.validate();
				
				Graphics2D g = bi.createGraphics();
				panel.paint(g);
				g.dispose();
			}
		});
		
		File imageFile = new File(dir, name+".png");
		ImageIO.write(bi, "png", imageFile);
		return imageFile;
	}
}
