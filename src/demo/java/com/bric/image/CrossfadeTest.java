/*
 * @(#)CrossfadeTest.java
 *
 * $Date: 2015-04-05 04:28:47 +0200 (So, 05 Apr 2015) $
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
package com.bric.image;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.PrintStream;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import com.bric.blog.Blurb;
import com.bric.plaf.PlafPaintUtils;
import com.bric.swing.BasicConsole;
import com.bric.swing.animation.AnimationController;
import com.bric.util.JVM;

/** This app demonstrates how difficult it is to achieve a crossfade between translucent
 * images using AlphaComposites.
 * <p>Once you launch the app: 4 different crossfade implementations are depicted and compared,
 * and the first tab contains a more detailed explanation of the test involved.
 * 
 * @see <a href="http://javagraphics.blogspot.com/2008/06/crossfades-what-is-and-isnt-possible.html">Crossfades: What Is and Is Not Possible</a>
 */
@Blurb (
filename = "CrossfadeTest",
title = "Crossfades: What Is and Isn't Possible",
releaseDate = "June 2008",
summary = "As far as I can tell: there's not a good (efficient) way to crossfade two translucent images in Java2D.\n"+
"<p>(Crossfading opaque images?  That's easy.  But not translucent ones.)\n"+
"<p>This article focuses on discussion and not a downloadable end result. ",
link = "http://javagraphics.blogspot.com/2008/06/crossfades-what-is-and-isnt-possible.html",
sandboxDemo = true
)
public abstract class CrossfadeTest extends JPanel {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		final BasicConsole[] consoleWrapper = new BasicConsole[] { null };
		final CrossfadeTest[] tests = new CrossfadeTest[4];
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame("CrossfadeTest");
				JTabbedPane tabs = new JTabbedPane();
				consoleWrapper[0] = new BasicConsole(false, true);
				
				tests[0] = new CrossfadeTest("Simplest Composite") {
					private static final long serialVersionUID = 1L;

					/** This is the simplest implementation.  This fades out
					 * the first image while fading in the second image.
					 * 
					 * The problem with this approach is that at t = .5, the red
					 * square will be at about .75 opacity, instead of 100%.
					 * 
					 * The SrcOver rule states that: Ad' = As+Ad*(1-As), so if
					 * Ad and As are .5, then Ad' = .75.
					 * 
					 */
					@Override
					public void paintCrossfade(Graphics2D g,BufferedImage img1,BufferedImage img2,float f) {
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1-f));
						g.drawImage(img1, 0, 0, null);
						
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,f));
						g.drawImage(img2, 0, 0, null);
					}
				};
				
				tests[1] = new CrossfadeTest("Squared Composite") {
					private static final long serialVersionUID = 1L;
					
					/** This overly compensates certain opacities, so the
					 * first image is slower to fade away, and the second
					 * image is faster to fade in.
					 * 
					 * This is a little better; the red square appears mostly
					 * solid (although it may still be a little translucent sometimes),
					 * but the green square pulses, because now during the middle of the
					 * animation it will be *more* opaque than at the beginning or end.
					 * 
					 */
					@Override
					public void paintCrossfade(Graphics2D g,BufferedImage img1,BufferedImage img2,float f) {
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1-f*f));
						g.drawImage(img1, 0, 0, null);
						
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1-(1-f)*(1-f)));
						g.drawImage(img2, 0, 0, null);
					}
				};
				
				tests[2] = new CrossfadeTest("Complicated Composite") {
					private static final long serialVersionUID = 1L;
					
					@Override
					public void paintCrossfade(Graphics2D g,BufferedImage img1,BufferedImage img2,float f) {
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1-f));
						g.drawImage(img1, 0, 0, null);
						
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,f));
						g.drawImage(img2, 0, 0, null);
						
						float total = 1-f+f*f;

						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN,(float)(Math.pow(total,.2))));
						g.drawImage(img1, 0, 0, null);
						g.drawImage(img2, 0, 0, null);
						
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1-f));
						g.drawImage(img1, 0, 0, null);
						
						g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,f));
						g.drawImage(img2, 0, 0, null);
					}
				};
				
				tests[3] = new CrossfadeTest("Raster-based") {
					private static final long serialVersionUID = 1L;

					@Override
					public void paintCrossfade(Graphics2D g, BufferedImage img1,
							BufferedImage img2, float f) {
						int height = img1.getHeight();
						int width = img1.getWidth();
						
						if(img2.getWidth()!=width || 
								img2.getHeight()!=height) {
							throw new IllegalArgumentException("The two images must be of the same dimensions.  ("+width+"x"+height+" vs "+img2.getWidth()+"x"+img2.getHeight()+")");
						}
						
						if(img1.getType()!=BufferedImage.TYPE_INT_ARGB ||
								img2.getType()!=BufferedImage.TYPE_INT_ARGB) {
							throw new IllegalArgumentException("Both images must be of type TYPE_INT_ARGB.");
						}
						
						int argb1, argb2, r1, g1, b1, a1, r2, g2, b2, a2, r3, g3, b3, a3, r1b, g1b, b1b, r2b, g2b, b2b;
						int[] i1 = new int[width];
						int[] i2 = new int[width];
						int f1 = (int)(f*1000);
						int f2 = 1000-f1;
						BufferedImage tmpImg = new BufferedImage(width, 1, img1.getType());
						for(int y = 0; y<height; y++) {
							img1.getRaster().getDataElements(0, y, i1.length, 1, i1);
							img2.getRaster().getDataElements(0, y, i2.length, 1, i2);
							for(int x = 0; x<width; x++) {
								argb1 = i1[x];
								argb2 = i2[x];
								r1 = (argb1 >> 16) & 0xff;
								g1 = (argb1 >> 8) & 0xff;
								b1 = (argb1) & 0xff;
								a1 = (argb1 >> 24) & 0xff;
								r2 = (argb2 >> 16) & 0xff;
								g2 = (argb2 >> 8) & 0xff;
								b2 = (argb2) & 0xff;
								a2 = (argb2 >> 24) & 0xff;
								
								//TODO: this hackish approach works for this particular scenario,
								//but this won't work for environments where alpha is nonzero.
								if(a1==0) {
									r1 = r2;
									g1 = g2;
									b1 = b2;
								} else if(a2==0) {
									r2 = r1;
									g2 = g1;
									b2 = b1;
								}
								
								r3 = (r1*f2+r2*f1)/1000;
								g3 = (g1*f2+g2*f1)/1000;
								b3 = (b1*f2+b2*f1)/1000;
								a3 = (a1*f2+a2*f1)/1000;
								
								i1[x] = (a3 << 24) + (r3 << 16) + (g3 << 8) + (b3);
							}
							tmpImg.getRaster().setDataElements(0, 0, i1.length, 1, i1);
							g.drawImage(tmpImg,0,y,null);
						}
					}
				};

				JScrollPane scrollPane = new JScrollPane(consoleWrapper[0]);
				tabs.add(scrollPane, "Output");
				for(int a = 0; a<tests.length; a++) {
					tabs.add(tests[a], tests[a].getName());
				}
				scrollPane.setPreferredSize(new Dimension(500, 500));
				f.getContentPane().add(tabs);
				f.pack();
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setVisible(true);
				
				synchronized(consoleWrapper) {
					consoleWrapper.notifyAll();
				}
			}
		});
		
		while(consoleWrapper[0]==null) {
			synchronized(consoleWrapper) {
				try {
					consoleWrapper.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		PrintStream out = consoleWrapper[0].createPrintStream(false);
		PrintStream err = consoleWrapper[0].createPrintStream(true);
		
		try {
			out.println(JVM.getProfile());
			
			out.println("\nThis app demonstrates how difficult it is to crossfade two translucent images using AlphaComposites.");
			out.println("\nThis app takes two images and attempts to crossfade between them.");
			out.println("In both images: a red opaque square is in the lower-left corner, and a translucent green square is in the upper-right.");
			out.println("(Check the other tabs to see the images being used: at t=0 you see the first image, and at t=1 you see the second image).");
			out.println("\nIdeally the red and green squares should remain constant as we transition from t=0 to t=1.\n"
					+ "But the table below shows how much they deviate from their expected opacity. For more reading, see:\n"
					+ "http://javagraphics.blogspot.com/2008/06/crossfades-what-is-and-isnt-possible.html\n");
			out.println("\n(Note the \"Raster-based\" approach with zero error avoids AlphaComposites entirely.)\n");
			//Do we analyze and report on the performance of these tests?
			boolean includePerformance = false;
			
			for(int a = 0; a<tests.length; a++) {
				if(includePerformance)
					out.println("Studying "+tests[a].getName());
				long[] times = new long[includePerformance ? 20 : 1];
				long[] memory = new long[times.length];
				for(int b = 0; b<times.length; b++) {
					Runtime.getRuntime().runFinalization();
					Runtime.getRuntime().gc();
					memory[b] = Runtime.getRuntime().freeMemory();
					times[b] = System.currentTimeMillis();
					for(int c = 0; c<100; c++) {
						tests[a].paintTest(.5f);
					}
					times[b] = System.currentTimeMillis()-times[b];
					memory[b] = memory[b]-Runtime.getRuntime().freeMemory();
				}
				Arrays.sort(times);
				Arrays.sort(memory);
				if(includePerformance) {
					out.println("  For 100 calls to paintCrossfade()");
					out.println("    Median Time: "+times[times.length/2]+" ms");
					out.println("    Memory Allocation: "+memory[memory.length/2]+" ms");
				}
			}
			
			String s = "t\t";
			for(int b = 0; b<tests.length; b++) {
				s = s+tests[b].getName();
				if(b!=tests.length-1) {
					s = s+"\t";
				}
			}
			out.println(s);
			for(int a = 0; a<=100; a++) {
				float f = (a)/100f;
				s = f+"\t";
				for(int b = 0; b<tests.length; b++) {
					tests[b].paintTest(f);
					s = s+tests[b].getError();
					if(b!=tests.length-1) {
						s = s+"\t";
					}
				}
				out.println(s);
			}
		} catch(Exception e) {
			e.printStackTrace(err);
		}
	}
	
	static Rectangle upperLeft = new Rectangle(10,10,50,50);
	static Rectangle lowerLeft = new Rectangle(10,140,50,50);
	static Rectangle lowerRight = new Rectangle(140,140,50,50);
	static Rectangle upperRight = new Rectangle(140,10,50,50);
	static BufferedImage bi1 = new BufferedImage(200,200,BufferedImage.TYPE_INT_ARGB);
	static BufferedImage bi2 = new BufferedImage(200,200,BufferedImage.TYPE_INT_ARGB);
	static {
		Color greenish = new Color(0,255,0,50);
		Graphics2D g = bi1.createGraphics();
		g.setColor(Color.yellow);
		g.fill(upperLeft);
		g.setColor(Color.red);
		g.fill(lowerLeft);
		g.setColor(greenish);
		g.fill(upperRight);
		g.dispose();
		
		g = bi2.createGraphics();
		g.setColor(Color.red);
		g.fill(lowerLeft);
		g.setColor(Color.cyan);
		g.fill(lowerRight);
		g.setColor(greenish);
		g.fill(upperRight);
		g.dispose();
	}
	AnimationController controller = new AnimationController();
	
	BufferedImage test = new BufferedImage(200,200,BufferedImage.TYPE_INT_ARGB);
	public CrossfadeTest(String name) {
		setName(name);
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.anchor = GridBagConstraints.SOUTH;
		add(controller, c);
		controller.setDuration(5);
		controller.addPropertyChangeListener(AnimationController.TIME_PROPERTY, new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				repaint();
			}
		});
		setOpaque(false);
		setPreferredSize(new Dimension(200,200));
	}
	
	protected void paintTest(float f) {
		synchronized(test) {
			Graphics2D g2 = test.createGraphics();
			g2.setComposite(AlphaComposite.Clear);
			g2.fillRect(0,0,test.getWidth(),test.getHeight());
			g2.setComposite(AlphaComposite.SrcOver);
			paintCrossfade(g2,bi1,bi2,f);
			g2.dispose();
		}
	}
	
	protected float getError() {
		return (getError(lowerLeft)+getError(upperRight))/2f;
	}
	
	protected float getError(Rectangle r) {
		int x = (int)r.getCenterX();
		int y = (int)r.getCenterY();
		int argb1 = test.getRGB(x, y);
		int alpha1 = (argb1 >> 24) & 0xff;
		int argb2 = bi1.getRGB(x, y);
		int alpha2 = (argb2 >> 24) & 0xff;
		float error = alpha2-alpha1;
		error = error/255;
		if(error<0) error = -error;
		return error;
	}
	
	@Override
	protected void paintComponent(Graphics g0) {
		super.paintComponent(g0);
		
		Graphics2D g = (Graphics2D)g0.create();
		g.setPaint( PlafPaintUtils.getCheckerBoard(16));
		g.fillRect(0,0,getWidth(),getHeight());
		
		float f = controller.getTime() / controller.getDuration();
		synchronized(test) {
			paintTest(f);
			g.drawImage(test, 0, 0, getWidth(), getHeight()-controller.getHeight(), null);
		}
		g.dispose();
	}
	
	public abstract void paintCrossfade(Graphics2D g,BufferedImage img1,BufferedImage img2,float f);
}
