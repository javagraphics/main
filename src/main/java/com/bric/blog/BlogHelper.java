/*
 * @(#)BlogHelper.java
 *
 * $Date: 2014-11-27 07:50:51 +0100 (Do, 27 Nov 2014) $
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
package com.bric.blog;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.RenderingHints;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import com.bric.io.FileTreeIterator;


/** Common methods for Helper classes. Helper classes are intended to 
 * create resources for the javagraphics svn repository (also for use
 * in the javagraphics blog).
 * <p>The {@link ExecuteHelperMethodJob} looks for three types of public
 * static methods:
 * <p><table summary="Static Method Requirements" cellpadding="5" border="1">
 * <tr><td><i>Return Type</i></td><td><i>Arguments</i></td><td><i>Description</i></td></tr>
 * <tr><td>BufferedImage</td><td>Dimension</td><td>Create an image as the blurb graphic, using the Dimension argument as the recommended image size.<br>(You are welcome to return an image of any size, though, and the caller will scale it.)</td></tr>
 * <tr><td>File</td><td>File</td><td>The file argument is the directory to write a new file in. The return value is the newly written file.<br>This is used to write resources with a predetermined name referenced in blog articles or documentation.</td></tr>
 * <tr><td>File</td><td>Robot, File</td><td>Similar to the previous description, except this offers a Robot class to help with screen capture functionality.</td></tr>
 * </table>
 */
public class BlogHelper {
	protected static RenderingHints qualityHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	protected static RenderingHints speedHints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
	static {
		qualityHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		qualityHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		qualityHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		qualityHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
		qualityHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
		
		speedHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
		speedHints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
		speedHints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
		speedHints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		speedHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
	}
	
	/** Move the mouse to a point provided.
	 * 
	 * @param robot the robot used to move the mouse.
	 * @param jc the component to move the mouse to
	 * @param p the point (relative to the JComponent) to move the mouse to.
	 * @param moveMouse whether to animate the mouse movement
	 * @param clickCountOnArrival how many clicks to simulate on arrival
	 */
	protected static void animateMouse(Robot robot,JComponent jc,Point p,boolean moveMouse,int clickCountOnArrival) {
		Point copy = new Point(p.x, p.y);
		SwingUtilities.convertPointToScreen(copy, jc);
		if(moveMouse) {
			PointerInfo pi = MouseInfo.getPointerInfo();
			Point currentLoc = pi.getLocation();
			
			double ax = copy.x - currentLoc.x;
			double bx = currentLoc.x;
			double ay = copy.y - currentLoc.y;
			double by = currentLoc.y;
			
			for(double t = 0; t<1; t+=.05) {
				currentLoc.x = (int)(ax*t + bx);
				currentLoc.y = (int)(ay*t + by);
				robot.mouseMove(currentLoc.x, currentLoc.y);
				beat(40*4);
			}
		}
		robot.mouseMove(copy.x, copy.y);
		beat(40*4);
		
		if(clickCountOnArrival>0) {
			while(clickCountOnArrival>0) {
				robot.mousePress(InputEvent.BUTTON1_MASK);
				beat(20);
				robot.mouseRelease(InputEvent.BUTTON1_MASK);
				clickCountOnArrival--;
			}
			beat(200*4);
		}
	}
	
	/** Move the mouse to the center of the component provided.
	 * 
	 * @param robot the robot used to move the mouse.
	 * @param jc the component to move the mouse to
	 * @param moveMouse whether to animate the mouse movement
	 * @param clickOnArrival whether to click the mouse once it arrives at its destination
	 */
	protected static void animateMouse(Robot robot,JComponent jc,boolean moveMouse,boolean clickOnArrival) {
		Point p = new Point(jc.getWidth()/2, jc.getHeight()/2);
		animateMouse(robot, jc, p, moveMouse, clickOnArrival ? 1 : 0);
	}
	
	/** Pause for a certain number of milliseconds.
	 * This is used in creating screen capture animations.
	 * @param ms the milliseconds to wait.
	 */
	protected static void beat(int ms) {
		try {
			Thread.sleep(ms);
		} catch(Exception e) {}
	}

	/** Paint a Component as a BufferedImage using its preferred size. 
	 * 
	 * @param c the component to paint
	 * @param size an optional size to impose on the argument. If null,
	 * then the preferred size is used. 
	 * @return a rendering of the component.
	 */
	protected static BufferedImage paint(final Component c,Dimension size) {
		if(size==null)
			size = c.getPreferredSize();
		final BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
		try {
			Runnable runnable1 = new Runnable() {
				
				private void revalidate(Component c) {
					if(c instanceof Container) {
						Container c2 = (Container)c;
						LayoutManager lm = c2.getLayout();
						if(lm!=null)
							lm.layoutContainer(c2);
						for(int a = 0; a<c2.getComponentCount(); a++) {
							revalidate(c2.getComponent(a));
						}
					}
					c.validate();
					c.revalidate();
				}
				public void run() {
					c.setSize(new Dimension(bi.getWidth(),bi.getHeight()));
					revalidate(c);
				}
			};
			Runnable runnable2 = new Runnable() {
				public void run() {
					Graphics2D g = bi.createGraphics();
					c.paint(g);
					g.dispose();
				}
			};
			if(SwingUtilities.isEventDispatchThread()) {
				 runnable1.run();
				 runnable2.run();
			} else {
				SwingUtilities.invokeAndWait(runnable1);
				//Ugh. I'm not sure why this is necessary, but this
				//delay lets the PromptSearchDemo demo render
				//correctly.
				Thread.sleep(1000);
				SwingUtilities.invokeAndWait(runnable2);
			}
			return bi;
		} catch(RuntimeException e) {
			throw e;
		} catch(Throwable t) {
			throw new RuntimeException(t);
		}
	}

	/** Search the workspace for all public non-abstract implementations
	 * of an abstract super class.
	 * 
	 * @param parentClass the class we're searching for.
	 * @param requiredPackagePhrase an optional phrase that must be present in the package
	 * name of a subclass to include it. For example the Transition2DDemoHelper passed
	 * "com.bric.image.transition" for this argument. If null: all possible subclasses
	 * are returned.
	 * @return an array of non-abstract public subclasses matching this description. 
	 * @throws ClassNotFoundException if a class cannot be identified based on its file name.
	 */
	protected static Class<?>[] getNonAbstractSubclasses(Class parentClass,String requiredPackagePhrase) throws ClassNotFoundException {
		Set<Class<?>> returnValue = new HashSet<Class<?>>();
		WorkspaceContext context = new WorkspaceContext(null);
		File[] sourcePaths = context.getSourcePaths();
		for(File sourcePath : sourcePaths) {
			FileTreeIterator iter = new FileTreeIterator(sourcePath, "java");
			while(iter.hasNext()) {
				File javaFile = iter.next();
				try {
					String className = context.getClassName(javaFile);
					if(requiredPackagePhrase==null || className.contains(requiredPackagePhrase)) {
						Class<?> c = Class.forName(className);
						boolean isAbstract = (c.getModifiers() & Modifier.ABSTRACT) > 0;
						if(parentClass.isAssignableFrom(c) && (!isAbstract)) {
							returnValue.add(c);
						}
					}
				} catch(Throwable e) {}
			}
		}
		return returnValue.toArray(new Class[returnValue.size()]);
	}
	
	/** Return true if this method is static.
	 *
	 * @param m the method to check
	 * @return true if the argument is static
	 * 
	 */
	public static boolean isStatic(Method m) {
		return (m.getModifiers() & Modifier.STATIC)>0;
	}

	/** Draw a string in Verdana size 14.
	 * @param g the Graphics2D to paint to.
	 * @param string the string to paint
	 * @param x the x-coordinate of the string location
	 * @param y the y-coordinate of the string location
	 * @param includeOutline whether to paint a white outline
	 */
	public static void drawString(Graphics2D g, String string, 
			int x, int y,boolean includeOutline) {
		Font font = (new Font("Verdana", 0, 14));
		if(includeOutline) {
			Shape textShape = font.createGlyphVector(g.getFontRenderContext(), string).getOutline(x, y);
			g.setComposite(AlphaComposite.SrcOver);
			if(includeOutline) {
				g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g.setColor(Color.white);
				g.draw(textShape);
			}
			g.setColor(Color.black);
			g.fill(textShape);
		} else {
			g.setFont(font);
			g.setColor(Color.black);
			g.drawString(string, x, y);
		}
	}
	
	/** An automated model for executing a particular mouse script within a panel. */
	protected static class RobotMouseScript implements Runnable {
		final long[][] data;
		final long startTime = System.currentTimeMillis();
		final Component comp;
		final Robot robot;
		
		/** Create console output that can be used to create a RobotMouseScript.
		 */
		public static void installListener(Component comp) {
			MouseInputAdapter ml = new MouseInputAdapter() {

				@Override
				public void mousePressed(MouseEvent e) {
					System.out.println("\t{ MouseEvent.MOUSE_PRESSED, "+e.getX()+", "+e.getY()+", "+e.getWhen()+"L }, ");
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					System.out.println("\t{ MouseEvent.MOUSE_RELEASED, "+e.getX()+", "+e.getY()+", "+e.getWhen()+"L }, ");
				}

				@Override
				public void mouseDragged(MouseEvent e) {
					System.out.println("\t{ MouseEvent.MOUSE_DRAGGED, "+e.getX()+", "+e.getY()+", "+e.getWhen()+"L }, ");
				}

				@Override
				public void mouseMoved(MouseEvent e) {
					System.out.println("\t{ MouseEvent.MOUSE_MOVED, "+e.getX()+", "+e.getY()+", "+e.getWhen()+"L }, ");
				}
				
			};
			
			comp.addMouseListener(ml);
			comp.addMouseMotionListener(ml);
		}
		
		/**
		 * 
		 * @param data a series of 4-entry script events. The first item
		 * is the mouse event ID (MouseEvent.MOUSE_PRESSED, MOVED, DRAGGED or RELEASED).
		 * The next two items are the (x, y) coordinates. The last item is the time stamp.
		 * @param robot the robot to use to execute this script.
		 * @param comp the component to dispatch these events to.
		 */
		public RobotMouseScript(long[][] data,Robot robot,Component comp) {
			this.data = data;
			this.robot = robot;
			this.comp = comp;
			//change the time to a relative time (based at t=0)
			for(int a = data.length-1; a>=0; a--) {
				data[a][3] = data[a][3] - data[0][3];
			}
		}
		
		int ctr = 0;
		Runnable runnableItemIterator = new Runnable() {
			public void run() {
				runScriptItem(ctr);
				ctr++;
				if(ctr<data.length) {
					SwingUtilities.invokeLater(this);
				}
			}
		};
		
		/** Execute all items in this script. This must not be called on the EDT.
		 */
		public void run() {
			if(SwingUtilities.isEventDispatchThread())
				throw new RuntimeException("do not invoke on EDT");
			
			SwingUtilities.invokeLater(runnableItemIterator);
			while(ctr<data.length) {
				try {
					Thread.sleep(50);
				} catch(Exception e) {}
			}
			
		}
		
		void runScriptItem(int index) {
			int x = (int)data[index][1];
			int y = (int)data[index][2];
			Point p = new Point(x,y);
			SwingUtilities.convertPointToScreen(p, comp);
			robot.mouseMove(p.x, p.y);
			
			long targetElapsed = data[index][3] + startTime;
			long realElapsed = System.currentTimeMillis();
			long delay = targetElapsed - realElapsed;
			if(delay>0) {
				BlogHelper.beat((int)delay);
			}
			
			if(data[index][0]==MouseEvent.MOUSE_PRESSED) {
				comp.dispatchEvent(new MouseEvent(comp, MouseEvent.MOUSE_PRESSED,
						data[index][3] + startTime, 0, x, y, 1, false));
			} else if(data[index][0]==MouseEvent.MOUSE_RELEASED) {
				comp.dispatchEvent(new MouseEvent(comp, MouseEvent.MOUSE_RELEASED,
						data[index][3] + startTime, 0, x, y, 0, false));
			} else if(data[index][0]==MouseEvent.MOUSE_MOVED) {
				comp.dispatchEvent(new MouseEvent(comp, MouseEvent.MOUSE_MOVED,
						data[index][3] + startTime, 0, x, y, 0, false));
			} else if(data[index][0]==MouseEvent.MOUSE_DRAGGED) {
				comp.dispatchEvent(new MouseEvent(comp, MouseEvent.MOUSE_DRAGGED,
						data[index][3] + startTime, 0, x, y, 0, false));
			}
			comp.repaint();
		}
	}
	
	/** Set a slider value based on a fraction [0,1] value.
	 * 
	 * @param slider the slider to adjust.
	 * @param f a value of [0,1], where 0 will be mapped to the minimum slider value
	 * and 1 will be mapped to the maximum slider value.
	 */
	protected static void setSliderValue(JSlider slider,float f) {
		if(f<0 || f>1) throw new IllegalArgumentException("f ("+f+") must be between [0, 1]");
		int v = (int)(slider.getMinimum() + f*(slider.getMaximum() - slider.getMinimum()));
		slider.setValue(v);
	}
}
