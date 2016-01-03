/*
 * @(#)KnotRenderer.java
 *
 * $Date: 2014-11-27 07:50:51 +0100 (Do, 27 Nov 2014) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * http://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.geom.knot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Area;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import com.bric.geom.intersect.IntersectionIdentifier;
import com.bric.geom.intersect.IntersectionIdentifier.IntersectionListener;
import com.bric.swing.BricApplet;

public class SwastikaKnotRenderer  {
	
	static class Applet extends BricApplet {
		private static final long serialVersionUID = 1L;
		
		Applet() {
			add(new KnotPanel());
		}
	}

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
				f.getContentPane().add(new SwastikaKnotRenderer.Applet());
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.pack();
				f.setVisible(true);
			}
		});
	}
	
	private static double getDistance(Shape path) {
		double distance = 0;
		double lastX = 0;
		double lastY = 0;
		double[] coords = new double[6];
		PathIterator i = path.getPathIterator(null, .0000001);
		while(!i.isDone()) {
			int j = i.currentSegment(coords);
			if(j==PathIterator.SEG_LINETO) {
				double dx = coords[0] - lastX;
				double dy = coords[1] - lastY;
				distance += Math.sqrt(dx*dx+dy*dy);
			}
			lastX = coords[0];
			lastY = coords[1];
			i.next();
		}
		return distance;
	}
	
	static class KnotPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public KnotPanel() {
			GeneralPath p = new GeneralPath();
			double incr = .1;
			for(double x = 0; x<2000; x+=incr) {
				p.reset();
				p.moveTo(-25,100);
				p.quadTo(x, 99, 25, 100);
				System.out.println(x+"\t"+getDistance(p));
				if(x<10) {
					incr = .1;
				} else if(x<100) {
					incr = 1;
				} else {
					incr = 10;
				}
			}
			
			setPreferredSize(new Dimension(300, 300));
			Timer timer = new Timer(500, new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					repaint();
				}
			});
			timer.start();
		}
		
		protected void paintComponent(Graphics g0) {
			Graphics2D g = (Graphics2D)g0;
			super.paintComponent(g);
			
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
			
			GeneralPath abstractShape = new GeneralPath();
			abstractShape.moveTo(getWidth()*1/3, getHeight()*5/6);
			abstractShape.curveTo(getWidth()*6/5, 0, -getWidth()*1/5, 0, getWidth()*2/3, getHeight()*5/6);
			
			SwastikaKnotRenderer renderer = new SwastikaKnotRenderer(abstractShape);
			renderer.paint(g);
		}
	}
	
	static class LineSegmentIterator {
		FlatteningPathIterator fpi;
		Line2D line = new Line2D.Double();
		double[] coords = new double[2];
		double lastX, lastY, moveX, moveY;
		
		public LineSegmentIterator(Shape shape) {
			fpi = new FlatteningPathIterator(shape.getPathIterator(null), .01);
			iterate();
		}
		
		public boolean hasNext() {
			return line!=null;
		}
		
		public Line2D getLine() {
			return line;
		}
		
		public void next() {
			iterate();
		}
		
		private void iterate() {
			if(fpi.isDone()) {
				line = null;
				return;
			}
			int k = fpi.currentSegment(coords);
			fpi.next();
			if(k==PathIterator.SEG_MOVETO) {
				lastX = coords[0];
				lastY = coords[1];
				moveX = lastX;
				moveY = lastY;
				iterate();
			} else if(k==PathIterator.SEG_LINETO){
				line.setLine(lastX, lastY, coords[0], coords[1]);
				lastX = coords[0];
				lastY = coords[1];
			} else if(k==PathIterator.SEG_CLOSE) {
				line.setLine(lastX, lastY, moveX, moveY);
			} else {
				throw new RuntimeException("unexpected segment type: "+k);
			}
		}
	}
	
	static class Intersection {
		double x, y;
		double theta1, theta2;

		public Intersection(double x,double y,double theta1,double theta2) {
			this.x = x;
			this.y = y;
			this.theta1 = theta1;
			this.theta2 = theta2;
		}
		
		@Override
		public String toString() {
			return "Intersection[ x="+x+", y="+y+", theta1="+theta1+", theta="+theta2+"]";
		}
	}
	
	Area a, b, borderA, borderB, middle;
	Shape originalShape;
	static float K1 = 16;
	static float K2 = 5;
	
	/** The milliseconds between flashes */
	static long FLASH_PERIOD = 2500;
	
	Area unifiedBorder;
	Area unifiedContent;
	
	List<Intersection> intersections = new ArrayList<Intersection>();
	
	SwastikaKnotRenderer(Shape shape) {
		originalShape = shape;
		
		middle = new Area(stroke(K2).createStrokedShape(originalShape));
		
		Area stroke1 = new Area(stroke(K1).createStrokedShape(originalShape));
		Area stroke2 = new Area(stroke(K2).createStrokedShape(originalShape));
		stroke1.subtract(stroke2);
		
		Area half = new Area(originalShape);
		a = new Area(stroke1);
		b = new Area(stroke1);
		a.subtract(half);
		b.intersect(half);
		
		stroke1 = new Area(stroke(K1+2*K2).createStrokedShape(originalShape));
		stroke2 = new Area(stroke(K1).createStrokedShape(originalShape));
		stroke1.subtract(stroke2);
		
		borderA = new Area(stroke1);
		borderB = new Area(stroke1);
		borderA.subtract(half);
		borderB.intersect(half);
		
		calculateIntersections();
		
		for(int j = 0; j<intersections.size(); j++) {
			Intersection i = intersections.get(j);
			Line2D tangentLine = new Line2D.Double(
				i.x + (K1+2*K2)/2*Math.cos(i.theta2),
				i.y + (K1+2*K2)/2*Math.sin(i.theta2),
				i.x - (K1+2*K2)/2*Math.cos(i.theta2),
				i.y - (K1+2*K2)/2*Math.sin(i.theta2)
			);
			Area z1 = new Area(stroke(K1+2*K2).createStrokedShape(tangentLine));
			Area z2 = new Area(stroke(K1).createStrokedShape(tangentLine));
			z1.subtract(z2);
			Area edges = new Area(b);
			edges.intersect(z1);
			borderB.add(edges);
			b.subtract(edges);

			tangentLine = new Line2D.Double(
				i.x + (K1+2*K2)/2*Math.cos(i.theta1),
				i.y + (K1+2*K2)/2*Math.sin(i.theta1),
				i.x - (K1+2*K2)/2*Math.cos(i.theta1),
				i.y - (K1+2*K2)/2*Math.sin(i.theta1)
			);
			z1 = new Area(stroke(K1+2*K2).createStrokedShape(tangentLine));
			z2 = new Area(stroke(K1).createStrokedShape(tangentLine));
			z1.subtract(z2);
			edges = new Area(a);
			edges.intersect(z1);
			borderA.add(edges);
			a.subtract(edges);
		}
		
		unifiedContent = new Area(a);
		unifiedContent.add(b);
		
		unifiedBorder = new Area(borderA);
		unifiedBorder.add(borderB);
		unifiedBorder.add(middle);
	}

	private void calculateIntersections() {
		IntersectionIdentifier i = IntersectionIdentifier.get();
		i.getIntersections(originalShape, originalShape, new IntersectionListener() {

			public void lineLineIntersection(double[] results, int size,
					int segmentIndex1, int segmentIndex2,
					double lastX1, double lastY1, double x1, double y1, 
					double lastX2, double lastY2, double x2, double y2) {
				double ax1 = x1 - lastX1;
				double ay1 = y1 - lastY1;
				double ax2 = x2 - lastX2;
				double ay2 = y2 - lastY2;
				for(int a = 0; a<size; a++) {
					double t1 = results[4*a+2];
					double t2 = results[4*a+3];
					if( (t1==1 && t2==0 && segmentIndex1==segmentIndex2+1) ||
							(t1==0 && t2==1 && segmentIndex1+1==segmentIndex2) ||
							(segmentIndex1==segmentIndex2 && Math.abs(t1-t2)<.001)) {
						continue;
					}
					Intersection intersection = new Intersection(
						results[4*a+0], results[4*a+1],
						Math.atan2( ay1, ax1 ),
						Math.atan2( ay2, ax2)
					);
					if(!containsIntersection(intersection)) {
						intersections.add(intersection);
					}
				}
			}

			public void lineQuadraticIntersection(double[] results, int size,
					int segmentIndex1, int segmentIndex2, 
					double lastX1, double lastY1, double x1, double y1, 
					double lastX2, double lastY2, double cx2, double cy2, double x2, double y2) {
				double ax1 = x1 - lastX1;
				double ay1 = y1 - lastY1;
				double ax2 = lastX2-2*cx2+x2;
				double bx2 = -2*lastX2+2*cx2;
				double ay2 = lastY2-2*cy2+y2;
				double by2 = -2*lastY2+2*cy2;
				for(int a = 0; a<size; a++) {
					double t1 = results[4*a+2];
					double t2 = results[4*a+3];
					if( (t1==1 && t2==0 && segmentIndex1==segmentIndex2+1) ||
							(t1==0 && t2==1 && segmentIndex1+1==segmentIndex2) ||
							(segmentIndex1==segmentIndex2 && Math.abs(t1-t2)<.001)) {
						continue;
					}
					Intersection intersection = new Intersection(
						results[4*a+0], results[4*a+1],
						Math.atan2( ay1, ax1 ),
						Math.atan2( 2*t2*ay2 + by2, 2*t2*ax2 + bx2)
					);
					if(!containsIntersection(intersection)) {
						intersections.add(intersection);
					}
				}
			}

			public void lineCubicIntersection(double[] results, int size,
					int segmentIndex1, int segmentIndex2, 
					double lastX1, double lastY1, double x1, double y1,
					double lastX2, double lastY2, double cx2a, double cy2a, double cx2b, double cy2b, double x2, double y2) {
				double ax1 = x1 - lastX1;
				double ay1 = y1 - lastY1;
				double ax2 = -lastX2+3*cx2a-3*cx2b+x2;
				double bx2 = 3*lastX2-6*cx2a+3*cx2b;
				double cx2 = -3*lastX2+3*cx2a;
				double ay2 = -lastY2+3*cy2a-3*cy2b+y2;
				double by2 = 3*lastY2-6*cy2a+3*cy2b;
				double cy2 = -3*lastY2+3*cy2a;
				
				for(int a = 0; a<size; a++) {
					double t1 = results[4*a+2];
					double t2 = results[4*a+3];
					if( (t1==1 && t2==0 && segmentIndex1==segmentIndex2+1) ||
							(t1==0 && t2==1 && segmentIndex1+1==segmentIndex2) ||
							(segmentIndex1==segmentIndex2 && Math.abs(t1-t2)<.001)) {
						continue;
					}
					Intersection intersection = new Intersection(
						results[4*a+0], results[4*a+1],
						Math.atan2( ay1, ax1 ),
						Math.atan2( 3*t2*t2*ay2 + 2*by2*t2 + cy2, 3*t2*t2*ax2 + 2*bx2*t2 + cx2)
					);
					if(!containsIntersection(intersection)) {
						intersections.add(intersection);
					}
				}
				
			}

			public void quadraticLineIntersection(double[] results, int size,
					int segmentIndex1, int segmentIndex2, double lastX1,
					double lastY1, double cx1, double cy1, double x1,
					double y1, double lastX2, double lastY2, double x2,
					double y2) {
				double ax1 = lastX1-2*cx1+x1;
				double bx1 = -2*lastX1+2*cx1;
				double ay1 = lastY1-2*cy1+y1;
				double by1 = -2*lastY1+2*cy1;
				double ax2 = x2 - lastX2;
				double ay2 = y2 - lastY2;
				for(int a = 0; a<size; a++) {
					double t1 = results[4*a+2];
					double t2 = results[4*a+3];
					if( (t1==1 && t2==0 && segmentIndex1==segmentIndex2+1) ||
							(t1==0 && t2==1 && segmentIndex1+1==segmentIndex2) ||
							(segmentIndex1==segmentIndex2 && Math.abs(t1-t2)<.001)) {
						continue;
					}
					Intersection intersection = new Intersection(
						results[4*a+0], results[4*a+1],
						Math.atan2( 2*t1*ay1 + by1, 2*t1*ax1 + bx1),
						Math.atan2( ay2, ax2 )
					);
					if(!containsIntersection(intersection)) {
						intersections.add(intersection);
					}
				}
			}

			public void quadraticQuadraticIntersection(double[] results,
					int size, int segmentIndex1, int segmentIndex2,
					double lastX1, double lastY1, double cx1, double cy1,
					double x1, double y1, double lastX2, double lastY2,
					double cx2, double cy2, double x2, double y2) {
				double ax1 = lastX1-2*cx1+x1;
				double bx1 = -2*lastX1+2*cx1;
				double ay1 = lastY1-2*cy1+y1;
				double by1 = -2*lastY1+2*cy1;
				double ax2 = lastX2-2*cx2+x2;
				double bx2 = -2*lastX2+2*cx2;
				double ay2 = lastY2-2*cy2+y2;
				double by2 = -2*lastY2+2*cy2;
				for(int a = 0; a<size; a++) {
					double t1 = results[4*a+2];
					double t2 = results[4*a+3];
					if( (t1==1 && t2==0 && segmentIndex1==segmentIndex2+1) ||
							(t1==0 && t2==1 && segmentIndex1+1==segmentIndex2) ||
							(segmentIndex1==segmentIndex2 && Math.abs(t1-t2)<.001)) {
						continue;
					}
					Intersection intersection = new Intersection(
						results[4*a+0], results[4*a+1],
						Math.atan2( 2*t1*ay1 + by1, 2*t1*ax1 + bx1),
						Math.atan2( 2*t2*ay2 + by2, 2*t2*ax2 + bx2)
					);
					if(!containsIntersection(intersection)) {
						intersections.add(intersection);
					}
				}
			}

			public void quadraticCubicIntersection(double[] results, int size,
					int segmentIndex1, int segmentIndex2, double lastX1,
					double lastY1, double cx1, double cy1, double x1,
					double y1, double lastX2, double lastY2, double cx2a,
					double cy2a, double cx2b, double cy2b, double x2, double y2) {
				double ax1 = lastX1-2*cx1+x1;
				double bx1 = -2*lastX1+2*cx1;
				double ay1 = lastY1-2*cy1+y1;
				double by1 = -2*lastY1+2*cy1;
				double ax2 = -lastX2+3*cx2a-3*cx2b+x2;
				double bx2 = 3*lastX2-6*cx2a+3*cx2b;
				double cx2 = -3*lastX2+3*cx2a;
				double ay2 = -lastY2+3*cy2a-3*cy2b+y2;
				double by2 = 3*lastY2-6*cy2a+3*cy2b;
				double cy2 = -3*lastY2+3*cy2a;
				
				for(int a = 0; a<size; a++) {
					double t1 = results[4*a+2];
					double t2 = results[4*a+3];
					if( (t1==1 && t2==0 && segmentIndex1==segmentIndex2+1) ||
							(t1==0 && t2==1 && segmentIndex1+1==segmentIndex2) ||
							(segmentIndex1==segmentIndex2 && Math.abs(t1-t2)<.001)) {
						continue;
					}
					Intersection intersection = new Intersection(
						results[4*a+0], results[4*a+1],
						Math.atan2( 2*t1*ay1 + by1, 2*t1*ax1 + bx1),
						Math.atan2( 3*t2*t2*ay2 + 2*by2*t2 + cy2, 3*t2*t2*ax2 + 2*bx2*t2 + cx2)
					);
					if(!containsIntersection(intersection)) {
						intersections.add(intersection);
					}
				}
			}

			public void cubicLineIntersection(double[] results, int size,
					int segmentIndex1, int segmentIndex2, double lastX1,
					double lastY1, double cx1a, double cy1a, double cx1b,
					double cy1b, double x1, double y1, double lastX2,
					double lastY2, double x2, double y2) {
				double ax1 = -lastX1+3*cx1a-3*cx1b+x1;
				double bx1 = 3*lastX1-6*cx1a+3*cx1b;
				double cx1 = -3*lastX1+3*cx1a;
				double ay1 = -lastY1+3*cy1a-3*cy1b+y1;
				double by1 = 3*lastY1-6*cy1a+3*cy1b;
				double cy1 = -3*lastY1+3*cy1a;
				double ax2 = x2 - lastX2;
				double ay2 = y2 - lastY2;
				
				for(int a = 0; a<size; a++) {
					double t1 = results[4*a+2];
					double t2 = results[4*a+3];
					if( (t1==1 && t2==0 && segmentIndex1==segmentIndex2+1) ||
							(t1==0 && t2==1 && segmentIndex1+1==segmentIndex2) ||
							(segmentIndex1==segmentIndex2 && Math.abs(t1-t2)<.001)) {
						continue;
					}
					Intersection intersection = new Intersection(
						results[4*a+0], results[4*a+1],
						Math.atan2( 3*t1*t1*ay1 + 2*by1*t1 + cy1, 3*t1*t1*ax1 + 2*bx1*t1 + cx1),
						Math.atan2( ay2, ax2)
					);
					if(!containsIntersection(intersection)) {
						intersections.add(intersection);
					}
				}
			}

			public void cubicCubicIntersection(double[] results, int size,
					int segmentIndex1, int segmentIndex2, double lastX1,
					double lastY1, double cx1a, double cy1a, double cx1b,
					double cy1b, double x1, double y1, double lastX2,
					double lastY2, double cx2a, double cy2a, double cx2b,
					double cy2b, double x2, double y2) {
				double ax1 = -lastX1+3*cx1a-3*cx1b+x1;
				double bx1 = 3*lastX1-6*cx1a+3*cx1b;
				double cx1 = -3*lastX1+3*cx1a;
				double ay1 = -lastY1+3*cy1a-3*cy1b+y1;
				double by1 = 3*lastY1-6*cy1a+3*cy1b;
				double cy1 = -3*lastY1+3*cy1a;
				double ax2 = -lastX2+3*cx2a-3*cx2b+x2;
				double bx2 = 3*lastX2-6*cx2a+3*cx2b;
				double cx2 = -3*lastX2+3*cx2a;
				double ay2 = -lastY2+3*cy2a-3*cy2b+y2;
				double by2 = 3*lastY2-6*cy2a+3*cy2b;
				double cy2 = -3*lastY2+3*cy2a;
				
				for(int a = 0; a<size; a++) {
					double t1 = results[4*a+2];
					double t2 = results[4*a+3];
					if( (t1==1 && t2==0 && segmentIndex1==segmentIndex2+1) ||
						(t1==0 && t2==1 && segmentIndex1+1==segmentIndex2) ||
						(segmentIndex1==segmentIndex2 && Math.abs(t1-t2)<.001)) {
						continue;
					}
					Intersection intersection = new Intersection(
						results[4*a+0], results[4*a+1],
						Math.atan2( 3*t1*t1*ay1 + 2*by1*t1 + cy1, 3*t1*t1*ax1 + 2*bx1*t1 + cx1),
						Math.atan2( 3*t2*t2*ay2 + 2*by2*t2 + cy2, 3*t2*t2*ax2 + 2*bx2*t2 + cx2)
					);
					if(!containsIntersection(intersection)) {
						intersections.add(intersection);
					}
				}
			}

			public void cubicQuadraticIntersection(double[] results, int size,
					int segmentIndex1, int segmentIndex2, double lastX1,
					double lastY1, double cx1a, double cy1a, double cx1b,
					double cy1b, double x1, double y1, double lastX2,
					double lastY2, double cx2, double cy2, double x2, double y2) {
				double ax1 = -lastX1+3*cx1a-3*cx1b+x1;
				double bx1 = 3*lastX1-6*cx1a+3*cx1b;
				double cx1 = -3*lastX1+3*cx1a;
				double ay1 = -lastY1+3*cy1a-3*cy1b+y1;
				double by1 = 3*lastY1-6*cy1a+3*cy1b;
				double cy1 = -3*lastY1+3*cy1a;
				double ax2 = lastX2-2*cx2+x2;
				double bx2 = -2*lastX2+2*cx2;
				double ay2 = lastY2-2*cy2+y2;
				double by2 = -2*lastY2+2*cy2;
				
				for(int a = 0; a<size; a++) {
					double t1 = results[4*a+2];
					double t2 = results[4*a+3];
					if( (t1==1 && t2==0 && segmentIndex1==segmentIndex2+1) ||
							(t1==0 && t2==1 && segmentIndex1+1==segmentIndex2) ||
							(segmentIndex1==segmentIndex2 && Math.abs(t1-t2)<.001)) {
						continue;
					}
					Intersection intersection = new Intersection(
						results[4*a+0], results[4*a+1],
						Math.atan2( 3*t1*t1*ay1 + 2*by1*t1 + cy1, 3*t1*t1*ax1 + 2*bx1*t1 + cx1),
						Math.atan2( 2*t2*ay2 + by2, 2*t2*ax2 + bx2)
					);
					if(!containsIntersection(intersection)) {
						intersections.add(intersection);
					}
				}
			}
			
		});
	}
	
	/** Each intersection will technically be encountered twice as we
	 * trace along the path: only keep a record of one instance of
	 * an intersection.
	 */
	private boolean containsIntersection(Intersection i) {
		for(Intersection t : intersections) {
			if(Math.abs(t.x - i.x)<.001 && 
					Math.abs(t.y - i.y)<.001) {
				if(Math.abs(t.theta1 - i.theta1)<.001 && Math.abs(t.theta2 - i.theta2)<.001) {
					return true;
				} else if(Math.abs(t.theta2 - i.theta1)<.001 && Math.abs(t.theta1 - i.theta2)<.001) {
					return true;
				}
			}
		}
		return false;
	}
	
	Stroke stroke(float size) {
		return new BasicStroke(size, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	}
	
	protected void paint(Graphics2D g) {
		long j = ( System.currentTimeMillis()%(FLASH_PERIOD*3) ) / FLASH_PERIOD;
		if(j==1) {
			g.setColor(new Color(0,200,100));
			g.fill(a);
			g.setColor(new Color(90,210,20));
			g.fill(b);
			
			g.setColor(Color.gray);
			g.fill(borderA);
			g.setColor(Color.darkGray);
			g.fill(borderB);
			g.setColor(Color.black);
			g.fill(middle);
		} else {
			g.setColor(new Color(190,250,220));
			g.fill(unifiedContent);
			g.setColor(new Color(90,210,20));
			g.fill(unifiedBorder);
			if(j==2) {
				for(int a = 0; a<intersections.size(); a++) {
					Intersection i = intersections.get(a);
					Line2D line = new Line2D.Double(
						i.x+K2*3*Math.cos(i.theta1),
						i.y+K2*3*Math.sin(i.theta1),
						i.x-K2*3*Math.cos(i.theta1),
						i.y-K2*3*Math.sin(i.theta1)
					);
					
					g.setStroke(stroke(K2));
					g.setColor(Color.blue);
					g.draw(line);
					
					line = new Line2D.Double(
							i.x+K2*3*Math.cos(i.theta2),
							i.y+K2*3*Math.sin(i.theta2),
							i.x-K2*3*Math.cos(i.theta2),
							i.y-K2*3*Math.sin(i.theta2)
						);
						
						g.setStroke(stroke(K2));
						g.setColor(Color.cyan);
						g.draw(line);
				}
			}
		}
	}
}
