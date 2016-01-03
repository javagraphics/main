/*
 * @(#)CurveIntersectionApp.java
 *
 * $Date: 2014-11-27 07:56:11 +0100 (Do, 27 Nov 2014) $
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
package com.bric.math.symbolic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;

import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.bric.geom.intersect.IntersectionIdentifier;
import com.bric.geom.intersect.IntersectionIdentifier.Return;
import com.bric.plaf.CubicPathCreationUI;
import com.bric.reflect.Reflection;
import com.bric.swing.ShapeCreationPanel;
import com.bric.swing.ShapeCreationPanel.DataModelListener;

/** This is a helper app that can be used to identify pairs of cubic curves and
 * their intersections.
 * <p>In order to create unit tests that validate the integrity of the algebraic
 * transformations: we need a real-world data set. This app is what creates these
 * data sets to test the equations against.
 *
 */
public class CurveIntersectionApp extends JApplet {
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				JFrame f = new JFrame("Curve Intersection App");
				f.getContentPane().add(new CurveIntersectionApp());
				f.pack();
				f.setLocationRelativeTo(null);
				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				f.setVisible(true);
			}
		});
	}
	
	class Curve {
		Point2D[] controlPoints = new Point2D[4];

		double ax, bx, cx, dx, ay, by, cy, dy;
		
		Curve(Shape shape) {
			
			PathIterator iter = shape.getPathIterator(null);
			
			double[] coords = new double[6];
			while(iter.isDone()==false) {
				int k = iter.currentSegment(coords);
				if(k==PathIterator.SEG_MOVETO) {
					if(controlPoints[0]==null) {
						controlPoints[0] = new Point2D.Double(coords[0], coords[1]);
					} else {
						//there can only be one... ?
						throw new RuntimeException("unexpected additional moveto");
					}
				} else if(k==PathIterator.SEG_CUBICTO) {
					if(controlPoints[0]!=null && controlPoints[1]==null) {
						controlPoints[1] = new Point2D.Double(coords[0], coords[1]);
						controlPoints[2] = new Point2D.Double(coords[2], coords[3]);
						controlPoints[3] = new Point2D.Double(coords[4], coords[5]);
					} else {
						//there can only be one... ?
						throw new RuntimeException("unexpected additional cubicto");
					}
				} else {
					throw new RuntimeException("unexpected path segment: "+Reflection.nameStaticField(PathIterator.class, k));
				}
				iter.next();
			}

			for(int a = 0; a<controlPoints.length; a++) {
				if(controlPoints[a]==null)
					throw new RuntimeException("Missing control point "+a);
			}
			
			double x0 = controlPoints[0].getX();
			double cx0 = controlPoints[1].getX();
			double cx1 = controlPoints[2].getX();
			double x1 = controlPoints[3].getX();

			double y0 = controlPoints[0].getY();
			double cy0 = controlPoints[1].getY();
			double cy1 = controlPoints[2].getY();
			double y1 = controlPoints[3].getY();
			
			ax= -x0+3*cx0-3*cx1+x1;
			bx = 3*x0-6*cx0+3*cx1;
			cx = -3*x0+3*cx0;
			dx = x0;
			
			ay = -y0+3*cy0-3*cy1+y1;
			by = 3*y0-6*cy0+3*cy1;
			cy = -3*y0+3*cy0;
			dy = y0;
		}

		public Point2D evaluate(double t) {
			return new Point2D.Double( ax*t*t*t + bx*t*t + cx*t + dx, 
					ay*t*t*t + by*t*t + cy*t + dy );
		}
	}

	Curve[] curves = new Curve[] {};
	Point2D[] intersections = new Point2D.Double[] {};
	
	ShapeCreationPanel shapePanel = new ShapeCreationPanel() {
		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g0) {
			Graphics2D g = (Graphics2D)g0.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			Rectangle bounds = new Rectangle(0,0,getWidth(),getHeight());
			g.setColor(Color.lightGray);
			g.setStroke(new BasicStroke(.5f));
			
			for(int a = 0; a<curves.length; a++) {
				
				double[] incr = new double[] {-.0005, .0005};
				for(int b = 0; b<incr.length; b++) {
					double t = .5;
					Point2D lastPoint = curves[a].evaluate(t);
					traceCurve : while(true) {
						t += incr[b];
						Point2D thisPoint = curves[a].evaluate(t);
						g.draw(new Line2D.Double(lastPoint, thisPoint));
						lastPoint = thisPoint;
						
						if(!bounds.contains(lastPoint)) {
							break traceCurve;
						}
					}
				}
			}
			
			g.dispose();
			super.paintComponent(g0);
			
			g = (Graphics2D)g0.create();
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g.setColor(Color.red);
			Ellipse2D e = new Ellipse2D.Float();
			int r = 3;
			for(int a = 0; a<intersections.length; a++) {
				e.setFrame( intersections[a].getX()-r, intersections[a].getY()-r, 2*r, 2*r);
				g.fill(e);
			}
			g.dispose();
		}
	};
	JTextArea textArea = new JTextArea();
	JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, shapePanel, new JScrollPane(textArea) );
	CubicPathCreationUI creationUI = new CubicPathCreationUI();
	
	public CurveIntersectionApp() {
		getContentPane().add(splitPane);
		setPreferredSize(new Dimension(800, 800));
		shapePanel.setUI(creationUI);
		shapePanel.getDataModel().addShape(new CubicCurve2D.Float(100,100,125,125,125,275,100,300));
		shapePanel.getDataModel().addShape(new CubicCurve2D.Float(300-100,100,300-125,125,300-125,275,300-100,300));
		shapePanel.setPreferredSize(new Dimension(400, 400));
		
		textArea.setLineWrap(true);
		textArea.setEditable(false);
		textArea.setWrapStyleWord(true);
		
		shapePanel.getDataModel().addListener(new DataModelListener() {

			@Override
			public void shapeAdded(ShapeCreationPanel shapePanel,
					int shapeIndex, Shape shape) {
				throw new RuntimeException("adding is not allowed");
			}

			@Override
			public void shapeRemoved(ShapeCreationPanel shapePanel,
					int shapeIndex, Shape shape) {
				throw new RuntimeException("removing is not allowed");
			}

			@Override
			public void shapeChanged(ShapeCreationPanel shapePanel,
					int shapeIndex, Shape shape) {
				recalculateData();
			}
		});
		
		recalculateData();
	}
	
	protected void recalculateData() {
		Shape[] shapes = shapePanel.getDataModel().getShapes();
		curves = new Curve[shapes.length];
		StringBuffer sb = new StringBuffer();
		for(int a = 0; a<shapes.length; a++) {
			curves[a] = new Curve(shapes[a]);
			sb.append("Curve "+(a+1)+":");
			sb.append("\nm "+curves[a].controlPoints[0].getX()+" "+curves[a].controlPoints[0].getY()+" ");
			sb.append("c "+curves[a].controlPoints[1].getX()+" "+curves[a].controlPoints[1].getY()+" "
					+curves[a].controlPoints[2].getX()+" "+curves[a].controlPoints[2].getY()+" "
					+curves[a].controlPoints[3].getX()+" "+curves[a].controlPoints[3].getY() );
			sb.append("\nax="+curves[a].ax);
			sb.append("\nbx="+curves[a].bx);
			sb.append("\ncx="+curves[a].cx);
			sb.append("\ndx="+curves[a].dx);
			sb.append("\nay="+curves[a].ay);
			sb.append("\nby="+curves[a].by);
			sb.append("\ncy="+curves[a].cy);
			sb.append("\ndy="+curves[a].dy);
			sb.append("\n\n");
		}
		
		try {
			double[] d = new double[1024];
			int k = IntersectionIdentifier.get().cubicCubic(
					curves[0].ax, curves[0].bx, curves[0].cx, curves[0].dx, 
					curves[0].ay, curves[0].by, curves[0].cy, curves[0].dy, 
					curves[1].ax, curves[1].bx, curves[1].cx, curves[1].dx, 
					curves[1].ay, curves[1].by, curves[1].cy, curves[1].dy, 
					d, 0, Return.X_Y_T1_T2);
			
			intersections = new Point2D[k];
			for(int a = 0; a<k; a++) {
				intersections[a] = new Point2D.Double( d[4*a+0], d[4*a+1] );
				sb.append("Intersection "+(a+1)+":");
				sb.append("\n("+d[4*a+0]+", "+d[4*a+1]+")");
				sb.append("\nt1 = "+d[4*a+2]);
				sb.append("\nt2 = "+d[4*a+3]);
				sb.append("\n\n");
			}
		} catch(RuntimeException e) {
			e.printStackTrace();
		}
		
		shapePanel.repaint();
		textArea.setText(sb.toString().trim());
	}
}
