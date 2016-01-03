/*
 * @(#)GradientTest.java
 *
 * $Date: 2015-03-17 07:13:09 +0100 (Di, 17 Mär 2015) $
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
package com.bric.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;

import com.bric.awt.GradientTexturePaint;
import com.bric.awt.GradientTexturePaint.Cycle;
import com.bric.graphics.OptimizedGraphics2D;
import com.bric.swing.BasicConsole;
import com.bric.util.JVM;

/** Some performance tests for different gradients.
 * 
 * @see <a href="http://javagraphics.blogspot.com/2009/11/gradients-boring-discussion.html">Gradients: a Boring Discussion</a>
 */
public class GradientTest {

	static boolean optimize = false;
	static BufferedImage bi = new BufferedImage(900, 900, BufferedImage.TYPE_INT_ARGB);
	static Point2D p1 = new Point2D.Double(0,0);
	static Point2D p2 = new Point2D.Double((int)(bi.getWidth()*.8),bi.getHeight());
	static float[] stops = new float[] {0, .3f, .8f, 1};
	static float[] interval = new float[] {.3f, .5f, .2f};
	static Color[] colors = new Color[] {Color.red, new Color(255,255,0,100), Color.green, Color.blue};
	static Rectangle2D bounds = new Rectangle2D.Float(0,100,bi.getWidth(),bi.getHeight()-100);
	
	static GradientFactory currentFactory;
	static Paint sharedPaint;
	
	public static void main(String[] args) {
		BasicConsole console = BasicConsole.create("GradientTest", false, true, true);
		PrintStream out = console.createPrintStream(false);
		PrintStream err = console.createPrintStream(true);
		
		try {
			out.println(JVM.getProfile());
			out.println();
			out.println("OptimizedGraphics2D Active = "+optimize);
			out.println();
			
			OptimizedGraphics2D.testingOptimizations = true;
			
			Vector<Runnable> tests = new Vector<Runnable>();
			Vector<GradientFactory> gradients = new Vector<GradientFactory>();
			
			tests.add(new Runnable() {
				AffineTransform transform = new AffineTransform();
				Rectangle rect = new Rectangle(0,0,bi.getWidth(),bi.getHeight());
				RenderingHints hints = new RenderingHints(new Hashtable<RenderingHints.Key, Object>());
				public void run() {
					Paint gp = currentFactory.createGradient();
					PaintContext context = gp.createContext(ColorModel.getRGBdefault(), rect, rect, transform, hints);
					context.getRaster(0, 0, bi.getWidth(), bi.getHeight());
					context.dispose();
				}
				
				@Override
				public String toString() {
					return "getRaster(), using alpha ColorModel";
				}
			});
			
			tests.add(new Runnable() {
				AffineTransform transform = new AffineTransform();
				Rectangle rect = new Rectangle(0,0,bi.getWidth(),bi.getHeight());
				RenderingHints hints = new RenderingHints(new Hashtable<RenderingHints.Key, Object>());
	
			    ColorModel model = new DirectColorModel(32,
							      0x00ff0000,	// Red
							      0x0000ff00,	// Green
							      0x000000ff,	// Blue
							      0x00000000	// Alpha
							      );
				public void run() {
					Paint gp = currentFactory.createGradient();
					PaintContext context = gp.createContext(model, rect, rect, transform, hints);
					context.getRaster(0, 0, bi.getWidth(), bi.getHeight());
					context.dispose();
				}
				
				@Override
				public String toString() {
					return "getRaster(), using opaque ColorModel";
				}
			});
			
			tests.add( new Runnable() {
				public void run() {
					Paint gp = currentFactory.createGradient();
					Graphics2D g = bi.createGraphics();
					if(optimize)
						g = new OptimizedGraphics2D(g);
					g.setPaint(gp);
					g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
					g.dispose();
				}
				
				@Override
				public String toString() {
					return "Constructed Paint, fillRect";
				}
			});
			tests.add(new Runnable() {
				public void run() {
					Graphics2D g = bi.createGraphics();
					if(optimize)
						g = new OptimizedGraphics2D(g);
					g.setPaint(sharedPaint);
					g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
					g.dispose();
				}
				
				@Override
				public String toString() {
					return "Shared Paint, fillRect"
					;
				}
			});
			tests.add(new Runnable() {
				public void run() {
					Graphics2D g = bi.createGraphics();
					if(optimize)
						g = new OptimizedGraphics2D(g);
					g.setPaint(sharedPaint);
					g.scale(.2, .2);
					g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
					g.dispose();
				}
				
				@Override
				public String toString() {
					return "Shared Reduced Paint, fillRect";
				}
			});
			tests.add(new Runnable() {
				public void run() {
					Paint gp = currentFactory.createGradient();
					Graphics2D g = bi.createGraphics();
					if(optimize)
						g = new OptimizedGraphics2D(g);
					g.setPaint(gp);
					g.scale(.2, .2);
					g.fillRect(0, 0, bi.getWidth(), bi.getHeight());
					g.dispose();
				}
				
				@Override
				public String toString() {
					return "Constructed Reduced Paint, fillRect";
				}
			});
	
			tests.add( new Runnable() {
				public void run() {
					Paint gp = currentFactory.createGradient();
					Graphics2D g = bi.createGraphics();
					if(optimize)
						g = new OptimizedGraphics2D(g);
					g.setPaint(gp);
					g.fill(new Ellipse2D.Float(0, 0, bi.getWidth(), bi.getHeight()));
					g.dispose();
				}
				
				@Override
				public String toString() {
					return "Constructed Paint, fill(Ellipse)";
				}
			});
			tests.add( new Runnable() {
				public void run() {
					Graphics2D g = bi.createGraphics();
					if(optimize)
						g = new OptimizedGraphics2D(g);
					g.setPaint(sharedPaint);
					g.fill(new Ellipse2D.Float(0, 0, bi.getWidth(), bi.getHeight()));
					g.dispose();
				}
				
				@Override
				public String toString() {
					return "Shared Paint, fill(Ellipse)";
				}
			});
			tests.add( new Runnable() {
				public void run() {
					Graphics2D g = bi.createGraphics();
					if(optimize)
						g = new OptimizedGraphics2D(g);
					g.setPaint(sharedPaint);
					g.scale(.2, .2);
					g.fill(new Ellipse2D.Float(0, 0, bi.getWidth(), bi.getHeight()));
					g.dispose();
				}
				
				@Override
				public String toString() {
					return "Shared Reduced Paint, fill(Ellipse)";
				}
			});
			tests.add( new Runnable() {
				public void run() {
					Paint gp = currentFactory.createGradient();
					Graphics2D g = bi.createGraphics();
					if(optimize)
						g = new OptimizedGraphics2D(g);
					g.setPaint(gp);
					g.scale(.2, .2);
					g.fill(new Ellipse2D.Float(0, 0, bi.getWidth(), bi.getHeight()));
					g.dispose();
				}
				
				@Override
				public String toString() {
					return "Constructed Reduced Paint, fill(Ellipse)";
				}
			});
			
			gradients.add(new GradientFactory() {
				public Paint createGradient() {
					return new com.sun.glf.goodies.GradientPaintExt(p1,p2,interval,colors);
				}
				
				@Override
				public String toString() {
					return "Linear (Sun)";
				}
			});
			
			gradients.add(new GradientFactory() {
				public Paint createGradient() {
					return new GradientTexturePaint(colors, stops, p1, p2, Cycle.TILE);
				}
				
				@Override
				public String toString() {
					return "Textured";
				}
			});
			
			gradients.add(new GradientFactory() {
				public Paint createGradient() {
					return new org.apache.batik.ext.awt.LinearGradientPaint(p1,p2,stops,colors);
				}
				
				@Override
				public String toString() {
					return "Linear (Batik)";
				}
			});
			
			if(JVM.getMajorJavaVersion()>=1.6f) {
				gradients.add(new GradientFactory() {
					public Paint createGradient() {
						return create16LinearGradient();
					}
					
					@Override
					public String toString() {
						return "Linear (AWT1.6)";
					}
				});
				
				gradients.add(new GradientFactory() {
					public Paint createGradient() {
						return new DelegatePaint( create16LinearGradient() );
					}
					
					@Override
					public String toString() {
						return "Linear (Delegate1.6)";
					}
				});
			}
			
			TestInfo[][] results = new TestInfo[tests.size()][gradients.size()];
			
			for(int a = 0; a<tests.size(); a++) {
				Runnable runnable = tests.get(a);
				for(int b = 0; b<gradients.size(); b++) {
					currentFactory = gradients.get(b);
					sharedPaint = currentFactory.createGradient();
					TestInfo info = runTest(runnable);
					results[a][b] = info;
				}
			}
			
			//print times:
			out.println("This table compares the times of several different functions:\n");
			String s = "\t";
			for(int a = 0; a<gradients.size(); a++) {
				s += gradients.get(a)+"\t";
			}
			out.println(s);
			for(int a = 0; a<tests.size(); a++) {
				Runnable runnable = tests.get(a);
				s = runnable.toString()+"\t";
				for(int b = 0; b<gradients.size(); b++) {
					s += results[a][b].getMedianTime()+"\t";
				}
				out.println(s);
			}
			
			//print memory:
			out.println("\nThis table compares the memory allocation of several different functions:\n");
			s = "\t";
			for(int a = 0; a<gradients.size(); a++) {
				s += gradients.get(a)+"\t";
			}
			out.println(s);
			for(int a = 0; a<tests.size(); a++) {
				Runnable runnable = tests.get(a);
				s = runnable.toString()+"\t";
				for(int b = 0; b<gradients.size(); b++) {
					s += results[a][b].getMedianMemory()+"\t";
				}
				out.println(s);
			}
		} catch(Throwable t) {
			t.printStackTrace(err);
		}
	}
	
	private static Constructor<?> linearConstructor;
	protected static Paint create16LinearGradient() {
		try {
			if(linearConstructor==null) {
				Class<?> theClass = Class.forName("java.awt.LinearGradientPaint");
				linearConstructor = theClass.getConstructor( new Class[] { Point2D.class, Point2D.class, (new float[1]).getClass(), (new Color[1]).getClass() });
			}
			return (Paint)linearConstructor.newInstance(new Object[] {p1, p2, stops, colors});
		} catch(Throwable t) {
			t.printStackTrace();
			throw new NullPointerException();
		}
	}
	
	protected static TestInfo runTest(Runnable runnable) {
		TestInfo i = new TestInfo();

		Runtime.getRuntime().gc();
		Runtime.getRuntime().runFinalization();
		Runtime.getRuntime().gc();
		Runtime.getRuntime().runFinalization();
		
		for(int a = 0; a<i.time.length; a++) {
			
			i.memory[a] = Runtime.getRuntime().freeMemory();
			runnable.run();
			i.memory[a] = i.memory[a] - Runtime.getRuntime().freeMemory();
			
			//The times based on just one iteration were very small (many less than 30 ms),
			//so let's repeat the tests to get more meaningful numbers:
			i.time[a] = System.currentTimeMillis();
			for(int b = 0; b<20; b++) {
				runnable.run();
			}
			i.time[a] = System.currentTimeMillis()-i.time[a];
		}
		return i;
	}

	static class TestInfo {
		long[] time = new long[15];
		long[] memory = new long[time.length];
		
		@Override
		public String toString() {
			return ("median time = "+time[time.length/2]+" median memory alloc = "+memory[memory.length/2]);
		}
		
		public long getMedianTime() {
			Arrays.sort(time);
			return time[time.length/2];
		}
		
		public long getMedianMemory() {
			Arrays.sort(memory);
			return memory[memory.length/2];
		}
	}
	
	static interface GradientFactory {
		public Paint createGradient();
	}

}
