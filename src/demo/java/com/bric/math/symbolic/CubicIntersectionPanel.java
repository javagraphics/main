/*
 * @(#)CubicIntersectionPanel.java
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
package com.bric.math.symbolic;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;


/**
 * A simple test to verify the equations to identify the intersection of 2 cubic
 * curves work. Click and drag the control points to make the cubic curves
 * intersect, and then see if the formula correctly identifies the intersection.
 * 
 */
public class CubicIntersectionPanel extends JPanel {
	private static final long serialVersionUID = 1L;

	/** A bezier curve. */
	public static class Curve {
		Point2D p1 = new Point2D.Double();
		Point2D p2 = new Point2D.Double();
		Point2D p3 = new Point2D.Double();
		Point2D p4 = new Point2D.Double();

		public Curve(double x1, double y1, double x2, double y2, double x3,
				double y3, double x4, double y4) {
			p1.setLocation(x1, y1);
			p2.setLocation(x2, y2);
			p3.setLocation(x3, y3);
			p4.setLocation(x4, y4);
		}

		/** Renders this curve. */
		public void paint(Graphics2D g) {
			CubicCurve2D c = new CubicCurve2D.Double(p1.getX(), p1.getY(), p2
					.getX(), p2.getY(), p3.getX(), p3.getY(), p4.getX(), p4.getY());

			g.setColor(Color.black);
			g.setStroke(new BasicStroke(1));
			g.draw(c);

			drawDot(g, p1);
			drawDot(g, p2);
			drawDot(g, p3);
			drawDot(g, p4);
		}

		public void drawDot(Graphics2D g, Point2D p) {
			g.setColor(Color.blue);
			Ellipse2D e = new Ellipse2D.Double(p.getX() - 2, p.getY() - 2, 4, 4);
			g.fill(e);
		}

		public int getSelectedPoint(MouseEvent e) {

			double d1 = p1.distance(e.getPoint());
			double d2 = p2.distance(e.getPoint());
			double d3 = p3.distance(e.getPoint());
			double d4 = p4.distance(e.getPoint());
			if (d1 <= d2 && d1 <= d3 && d1 <= d4 && d1 < 10) {
				return 1;
			} else if (d2 <= d1 && d2 <= d3 && d2 <= d4 && d2 < 10) {
				return 2;
			} else if (d3 <= d1 && d3 <= d2 && d3 <= d4 && d3 < 10) {
				return 3;
			} else if (d4 <= d1 && d4 <= d2 && d4 <= d3 && d4 < 10) {
				return 4;
			}

			return -1;
		}

		public void setPoint(int which, Point2D p) {
			if (which == 1)
				p1.setLocation(p);
			if (which == 2)
				p2.setLocation(p);
			if (which == 3)
				p3.setLocation(p);
			if (which == 4)
				p4.setLocation(p);
		}

		/**
		 * Returns the coefficients of the x-equation of this parametric curve: {a,
		 * b, c, d}
		 */
		public double[] getXCoeffs() {

			double a = -p1.getX() + 3 * p2.getX() - 3 * p3.getX() + p4.getX();
			double b = 3 * p1.getX() - 6 * p2.getX() + 3 * p3.getX();
			double c = -3 * p1.getX() + 3 * p2.getX();
			double d = p1.getX();

			return new double[] { a, b, c, d };
		}

		/**
		 * Returns the coefficients of the y-equation of this parametric curve: {a,
		 * b, c, d}
		 */
		public double[] getYCoeffs() {

			double a = -p1.getY() + 3 * p2.getY() - 3 * p3.getY() + p4.getY();
			double b = 3 * p1.getY() - 6 * p2.getY() + 3 * p3.getY();
			double c = -3 * p1.getY() + 3 * p2.getY();
			double d = p1.getY();

			return new double[] { a, b, c, d };
		}
	}

	/**
	 * Identify the approximate solutions of a polynomial.
	 * 
	 * Note this method is very approximate and crude.
	 * 
	 * @param c
	 *            the coefficients of the polynomial, where c[n] has a degree of
	 *            n.
	 * @return the approximate roots between [0,1].
	 */
	public static double[] findSolutions(double[] c, double min, double max,
			double incr) {
		Vector<Double> solutions = new Vector<Double>();
		findSolutions(c, min, max, incr, solutions);

		double[] array = new double[solutions.size()];
		for (int a = 0; a < array.length; a++) {
			array[a] = ((Double) solutions.get(a)).doubleValue();
		}
		return array;
	}

	private static void findSolutions(double[] c, double min, double max,
			double incr, Vector<Double> solutions) {
		double t = min;
		while (t < max) {
			double y1 = evaluate(t, c);
			t += incr;
			double y2 = evaluate(t, c);
			double m = y1 * y2;
			if (m <= 0) {
				applyNewtonsMethod(c, t - incr / 2, solutions);
			} else {
				// these equations often intersect -- or simply touch --
				// the x-axis and swoop back up,

				double d1 = evaluateDerivative(t - incr, c);
				double d2 = evaluateDerivative(t, c);

				if (d1 * d2 < 0 && m < .0000000001) {
					// we have an extrema somewhere near, and it might cross
					// the x-axis

					applyNewtonsMethod(c, t - incr / 2, solutions);
				}
			}
		}
	}

	private static double MIN = .00001;

	private static void applyNewtonsMethod(double[] c, double t,
			Vector<Double> solutions) {

		double dt;
		double t2 = t;

		int k = 0;
		double change = 1; // must be > 1 to get the loop moving

		while (change > MIN && k < 300) { // sometimes .0000001 is too strict;
											// 300 iterations may be our best
											// shot
			t = t2;

			dt = evaluateDerivative(t, c);
			if (dt == 0) {
				k = 300; // abort!
			} else {
				t2 = t - evaluate(t, c) / dt;

				change = t2 - t;
				if (change < 0)
					change = -change;
			}
			k++;
		}
		if (change <= MIN) {
			solutions.add(new Double(t));
		}
	}

	/**
	 * Evaluates a polynomial at a certain t value.
	 * 
	 * @param t
	 *            the t value to evaluate
	 * @param c
	 *            the coefficients of the polynomial, where c[n] has a degree of
	 *            n.
	 * @return the function at the t-value provided
	 */
	public static double evaluate(double t, double[] c) {
		double sum = 0;
		for (int a = c.length - 1; a >= 0; a--) {
			sum = sum * t + c[a];
		}
		return sum;
	}

	/**
	 * Evaluates the derivative a polynomial at a certain t value.
	 * 
	 * @param t
	 *            the t value to evaluate
	 * @param c
	 *            the coefficients of the polynomial, where c[n] has a degree of
	 *            n.
	 * @return the derivative at the t-value provided
	 */
	public static double evaluateDerivative(double t, double[] c) {
		double sum = 0;
		for (int a = c.length - 1; a > 0; a--) {
			sum = sum * t + c[a] * a;
		}
		return sum;
	}
	
	Curve curve1 = new Curve(9, 100, 50, 150, 80, 100, 200, 200);
	Curve curve2 = new Curve(90, 10, 30, 110, 20, 150, 100, 80);
	MouseInputAdapter mouseListener = new MouseInputAdapter() {
		Curve selectedCurve;
		int selectedPoint;

		public void mousePressed(MouseEvent e) {
			int i1 = curve1.getSelectedPoint(e);
			int i2 = curve2.getSelectedPoint(e);
			if (i1 != -1) {
				selectedCurve = curve1;
				selectedPoint = i1;
			} else if (i2 != -1) {
				selectedCurve = curve2;
				selectedPoint = i2;
			} else {
				selectedCurve = null;
				selectedPoint = -1;
			}
		}

		public void mouseReleased(MouseEvent e) {
			selectedCurve = null;
			selectedPoint = -1;
		}

		public void mouseDragged(MouseEvent e) {
			if (selectedCurve != null)
				selectedCurve.setPoint(selectedPoint, e.getPoint());
			repaint();
		}
	};

	public CubicIntersectionPanel() {
		setPreferredSize(new Dimension(600, 600));
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
	}

	public void paint(Graphics g0) {
		super.paint(g0);
		Graphics2D g = (Graphics2D) g0;
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		curve1.paint(g);
		curve2.paint(g);

		g.setColor(new Color(255, 0, 0, 100));

		Point2D[] p = getIntersections(curve1, curve2);
		for (int a = 0; a < p.length; a++) {
			Ellipse2D e = new Ellipse2D.Double(p[a].getX() - 2,
					p[a].getY() - 2, 4, 4);
			g.draw(e);
		}

		p = getIntersections(curve2, curve1);
		for (int a = 0; a < p.length; a++) {
			Ellipse2D e = new Ellipse2D.Double(p[a].getX() - 2,
					p[a].getY() - 2, 4, 4);
			g.draw(e);
		}
	}

	/** Returns the points where the two curves intersect, if any. */
	public static Point2D[] getIntersections(Curve c1, Curve c2) {
		double[] x1 = c1.getXCoeffs();
		double[] y1 = c1.getYCoeffs();
		double[] x2 = c2.getXCoeffs();
		double[] y2 = c2.getYCoeffs();

		double ax1ay2 = x1[0] * y2[0] - y1[0] * x2[0];
		double ax1by1 = x1[0] * y1[1] - y1[0] * x1[1];
		double ax1by2 = x1[0] * y2[1] - y1[0] * x2[1];
		double ax1cy1 = x1[0] * y1[2] - y1[0] * x1[2];
		double ax1cy2 = x1[0] * y2[2] - y1[0] * x2[2];
		double ax1dy1 = x1[0] * y1[3] - y1[0] * x1[3];
		double ax1dy2 = x1[0] * y2[3] - y1[0] * x2[3];
		double ax2by2 = x2[0] * y2[1] - y2[0] * x2[1];
		double ax2cy2 = x2[0] * y2[2] - y2[0] * x2[2];
		double ax2dy2 = x2[0] * y2[3] - y2[0] * x2[3];
		double bx1ay2 = x1[1] * y2[0] - y1[1] * x2[0];
		double bx1by2 = x1[1] * y2[1] - y1[1] * x2[1];
		double bx1cy1 = x1[1] * y1[2] - y1[1] * x1[2];
		double bx1cy2 = x1[1] * y2[2] - y1[1] * x2[2];
		double bx1dy1 = x1[1] * y1[3] - y1[1] * x1[3];
		double bx1dy2 = x1[1] * y2[3] - y1[1] * x2[3];
		double bx2cy2 = x2[1] * y2[2] - y2[1] * x2[2];
		double bx2dy2 = x2[1] * y2[3] - y2[1] * x2[3];
		double cx1ay2 = x1[2] * y2[0] - y1[2] * x2[0];
		double cx1by2 = x1[2] * y2[1] - y1[2] * x2[1];
		double cx1cy2 = x1[2] * y2[2] - y1[2] * x2[2];
		double cx1dy1 = x1[2] * y1[3] - y1[2] * x1[3];
		double cx1dy2 = x1[2] * y2[3] - y1[2] * x2[3];
		double cx2dy2 = x2[2] * y2[3] - y2[2] * x2[3];
		double dx1ay2 = x1[3] * y2[0] - y1[3] * x2[0];
		double dx1by2 = x1[3] * y2[1] - y1[3] * x2[1];
		double dx1cy2 = x1[3] * y2[2] - y1[3] * x2[2];
		double dx1dy2 = x1[3] * y2[3] - y1[3] * x2[3];

		// the magic equations follow:
		double[] coeffs = new double[19];
		coeffs[0] = ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1
				+ -6 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy2
				+ ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1dy1
				+ ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1dy1 * bx1dy1
				+ ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy2 * cx1dy2
				+ ax1by1 * ax1by1 * bx1dy2 * bx1dy2 * bx1dy2 * bx1dy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1dy1 * bx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1dy1 * bx1dy2 * bx1dy2 * bx1dy2
				+ -6 * ax1dy1 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1dy2 * bx1dy2
				+ 15 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy2 * ax1dy2
				+ 15 * ax1dy1 * ax1dy1 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -20 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy2 * ax1dy2 * ax1dy2
				+ ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1dy1 * cx1dy1
				+ ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1dy2 * cx1dy2
				+ 7 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * cx1dy1 * cx1dy1
				+ 2 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1dy1
				+ -1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1
				+ 3 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1dy2 * dx1dy2
				+ 7 * ax1by1 * ax1by1 * ax1dy2 * ax1dy2 * cx1dy2 * cx1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1dy2 * ax1dy2 * bx1dy2 * bx1dy2
				+ -1 * bx1cy1 * bx1cy1 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1dy2 * ax1dy2 * ax1dy2 * cx1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy2 * dx1dy2
				+ -8 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy2 * cx1dy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * dx1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy2 * ax1dy2 * dx1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1dy1
				+ -8 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy2 * ax1dy2 * cx1dy2
				+ -2 * ax1by1 * ax1dy2 * ax1dy2 * ax1dy2 * bx1dy2 * bx1dy2
				+ 2 * bx1cy1 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 2 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1dy1
				+ 6 * ax1by1 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2 * cx1dy2
				+ -2 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1
				+ 10 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1dy2
				+ -6 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1dy1
				+ 20 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1dy2
				+ 4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1dy1 * cx1dy2
				+ -24 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * dx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * bx1dy1 * cx1dy2
				+ -10 * bx1cy1 * ax1dy1 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy1 * dx1dy2
				+ -30 * ax1by1 * cx1dy1 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -6 * ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1dy2 * bx1dy2
				+ -24 * ax1by1 * ax1cy1 * ax1dy2 * ax1dy2 * ax1dy2 * dx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy2 * dx1dy2
				+ -20 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy2 * ax1dy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1dy2 * bx1dy2 * cx1dy2
				+ -60 * ax1by1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1dy2 * ax1dy2
				+ -2 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * bx1dy1 * cx1dy1
				+ -24 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy2 * dx1dy2
				+ -2 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1dy2 * cx1dy2
				+ 20 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy2 * ax1dy2 * ax1dy2
				+ -2 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1dy1 * cx1dy1
				+ 60 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy2 * ax1dy2 * cx1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy2 * bx1dy2 * cx1dy2
				+ -24 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy2 * ax1dy2 * dx1dy2
				+ 8 * ax1by1 * ax1cy1 * ax1dy1 * bx1dy2 * bx1dy2 * bx1dy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1dy2 * cx1dy2 * dx1dy2
				+ 8 * ax1by1 * ax1cy1 * bx1dy1 * bx1dy1 * bx1dy1 * ax1dy2 + 8
				* ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * dx1dy2 + -10
				* ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1dy2 + -4
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * dx1dy2 + -2
				* ax1by1 * bx1cy1 * ax1dy2 * ax1dy2 * bx1dy2 * bx1dy2 + 10
				* ax1by1 * ax1dy1 * ax1dy2 * ax1dy2 * bx1dy2 * bx1dy2 + -6
				* ax1by1 * ax1by1 * ax1dy2 * bx1dy2 * bx1dy2 * cx1dy2 + -4
				* ax1by1 * ax1by1 * ax1dy2 * ax1dy2 * bx1dy2 * dx1dy2 + -2
				* ax1by1 * bx1cy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1dy1 + 4
				* ax1by1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * dx1dy2 + -6
				* ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * cx1dy1 + 12
				* ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1dy1 * ax1dy2 + 4
				* ax1by1 * ax1cy1 * ax1cy1 * ax1dy2 * cx1dy2 * cx1dy2 + 6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * bx1dy2 + 4
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * bx1dy2 * cx1dy2 + 2
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * dx1dy2 + -12
				* ax1by1 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy2 * cx1dy2 + 2
				* ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1dy2 * bx1dy2 + -10
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * ax1dy2 * cx1dy2 + -6
				* ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * ax1dy2 * dx1dy2 + 2
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy2 * dx1dy2 + 18
				* ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * ax1dy2 * bx1dy2 + -2
				* ax1cy1 * ax1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * cx1dy1 + -2
				* ax1by1 * bx1dy1 * bx1dy1 * ax1dy2 * ax1dy2 * ax1dy2 + -6
				* ax1by1 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * cx1dy1 + -6
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy2 * bx1dy2 * dx1dy2 + -2
				* ax1by1 * ax1by1 * ax1by1 * cx1dy2 * cx1dy2 * cx1dy2 + -18
				* ax1by1 * ax1dy1 * bx1dy1 * ax1dy2 * ax1dy2 * bx1dy2 + -6
				* ax1by1 * ax1by1 * bx1cy1 * ax1dy2 * cx1dy2 * cx1dy2 + -10
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1dy2 * ax1dy2 + -4
				* ax1by1 * ax1cy1 * bx1cy1 * ax1dy2 * bx1dy2 * cx1dy2 + -18
				* ax1by1 * bx1cy1 * bx1cy1 * cx1dy1 * ax1dy2 * ax1dy2 + -12
				* ax1by1 * ax1cy1 * bx1cy1 * bx1cy1 * ax1dy2 * dx1dy2 + 16
				* ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * cx1dy2 + 10
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy2 * bx1dy2 + -6
				* ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1dy1 * dx1dy2 + -4
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1dy2 * cx1dy2 + 4
				* ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1dy2 * dx1dy2 + 10
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy2 + -12
				* ax1by1 * ax1cy1 * ax1dy1 * bx1dy1 * bx1dy2 * bx1dy2 + 18
				* ax1by1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * cx1dy2 + -24
				* ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * ax1dy2 * bx1dy2 + -12
				* ax1by1 * ax1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * dx1dy2 + -6
				* ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1dy2 * dx1dy2 + -10
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * bx1dy2 + 16
				* ax1by1 * ax1by1 * cx1dy1 * ax1dy2 * bx1dy2 * bx1dy2 + 4
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * cx1dy2 + 6
				* ax1by1 * ax1by1 * ax1cy1 * bx1dy2 * bx1dy2 * dx1dy2 + 2
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * cx1dy1 + 8
				* ax1cy1 * bx1cy1 * bx1dy1 * ax1dy2 * ax1dy2 * ax1dy2 + 6
				* ax1by1 * ax1by1 * bx1cy1 * ax1dy1 * cx1dy1 * cx1dy1 + -12
				* ax1cy1 * ax1cy1 * bx1dy1 * ax1dy2 * ax1dy2 * bx1dy2 + 4
				* ax1by1 * ax1cy1 * bx1cy1 * ax1dy1 * bx1dy1 * cx1dy1 + -24
				* ax1by1 * ax1by1 * cx1dy1 * ax1dy2 * ax1dy2 * cx1dy2 + 8
				* ax1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1dy2 + -18
				* ax1by1 * ax1by1 * ax1cy1 * ax1dy2 * cx1dy2 * dx1dy2 + -12
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1dy2 + -6
				* bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy2 * ax1dy2 + -24
				* ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * cx1dy1 * ax1dy2 + 6
				* ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1dy2 * dx1dy2 + -6
				* ax1by1 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * dx1dy2 + 12
				* ax1cy1 * ax1cy1 * ax1dy1 * bx1dy1 * ax1dy2 * bx1dy2 + -2
				* ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1dy1 * cx1dy2 + 42
				* ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * ax1dy2 * cx1dy2;
		coeffs[1] = 4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1dy1 * cx1cy2
				+ -4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1cy2 * cx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1dy1 * bx1cy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1cy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1cy2 * bx1dy2 * bx1dy2
				+ 4 * ax1by1 * ax1by1 * bx1cy2 * bx1dy2 * bx1dy2 * bx1dy2
				+ -2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1dy1 * cx1cy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1cy2 * cx1dy2
				+ -6 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1cy2
				+ 30 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1dy2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1dy2 * ax1dy2
				+ 60 * ax1dy1 * ax1dy1 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -30 * ax1dy1 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 6 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1cy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1cy2 * cx1dy2
				+ 24 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1cy2
				+ -4 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1cy2
				+ -32 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1cy2 * cx1dy2
				+ -4 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1cy2
				+ 12 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1cy2 * bx1dy2
				+ -8 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1cy2
				+ 144 * ax1by1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1dy2 * cx1dy2
				+ -18 * ax1by1 * ax1dy1 * ax1dy1 * bx1cy2 * ax1dy2 * bx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * cx1cy2 * ax1dy2
				+ -96 * ax1by1 * ax1dy1 * ax1cy2 * ax1dy2 * ax1dy2 * cx1dy2
				+ 36 * ax1by1 * ax1dy1 * bx1cy2 * ax1dy2 * ax1dy2 * bx1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1dy1 * cx1cy2 * ax1dy2 * ax1dy2
				+ 24 * ax1by1 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2 * cx1dy2
				+ 6 * ax1by1 * cx1cy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -10 * ax1by1 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2 * bx1dy2
				+ 2 * ax1cy1 * ax1cy1 * cx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1dy1 * ax1cy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1cy2 * cx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1dy1 * cx1dy1 * bx1cy2
				+ 24 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1cy2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * cx1dy1 * ax1cy2
				+ 10 * ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * cx1cy2
				+ -6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1cy2 * cx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * bx1dy1 * cx1cy2
				+ -6 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * ax1cy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * ax1cy2 * bx1dy2 * cx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * cx1cy2 * bx1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1cy2 * cx1dy2 * cx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy2 * bx1dy2 * cx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy2 * ax1dy2 * ax1dy2 * cx1dy2
				+ 6 * ax1by1 * ax1by1 * cx1dy1 * ax1cy2 * bx1dy2 * bx1dy2
				+ -6 * ax1by1 * ax1by1 * ax1cy2 * bx1dy2 * bx1dy2 * cx1dy2
				+ -10 * ax1by1 * ax1by1 * cx1cy2 * ax1dy2 * bx1dy2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * cx1cy2 * bx1dy2 * bx1dy2
				+ -24 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * cx1dy1 * ax1cy2
				+ -6 * ax1by1 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * dx1cy2
				+ 84 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * ax1cy2 * cx1dy2
				+ 24 * bx1cy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1dy2 * ax1dy2
				+ -42 * ax1by1 * ax1by1 * ax1dy1 * ax1cy2 * cx1dy2 * cx1dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1cy2 * dx1dy2
				+ -42 * ax1by1 * ax1by1 * cx1dy1 * cx1cy2 * ax1dy2 * ax1dy2
				+ 24 * ax1by1 * ax1by1 * ax1cy2 * ax1dy2 * cx1dy2 * cx1dy2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy2 * cx2dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy1 * dx1cy2
				+ -6 * ax1by1 * ax1by1 * ax1dy2 * ax1dy2 * bx1dy2 * cx2dy2
				+ 10 * bx1cy1 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2 + 6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * dx1cy2 + 10
				* ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1cy2 + -2
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1cy2 + 10
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * cx1cy2 + 2
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * bx1cy2 + -6
				* ax1by1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * cx1cy2 + 8
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1cy2 * bx1dy2 + 12
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * dx1cy2 * bx1dy2 + -12
				* ax1cy1 * ax1cy1 * bx1cy1 * bx1dy1 * bx1cy2 * ax1dy2 + 4
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1cy2 * ax1dy2 + -20
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1cy2 * cx1dy2 + -4
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy2 * bx1dy2 * bx1dy2 + 6
				* ax1cy1 * ax1cy1 * bx1cy1 * ax1cy2 * bx1dy2 * bx1dy2 + 10
				* ax1by1 * ax1by1 * ax1by1 * cx1cy2 * cx1dy2 * cx1dy2 + -2
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1cy2 * ax1dy2 * ax1dy2 + 16
				* ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1cy2 * bx1dy2 + -12
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1cy2 * dx1dy2 + -12
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * dx1cy2 * bx1dy2 + 12
				* ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1cy2 * ax1dy2 + -6
				* ax1by1 * ax1by1 * ax1by1 * ax1dy1 * dx1dy2 * cx2dy2 + 6
				* ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * ax1dy2 * cx2dy2 + -6
				* ax1by1 * bx1dy1 * bx1dy1 * ax1cy2 * ax1dy2 * ax1dy2 + -36
				* ax1by1 * ax1dy1 * bx1dy1 * ax1cy2 * ax1dy2 * bx1dy2 + 4
				* ax1by1 * bx1dy1 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2 + 12
				* ax1by1 * bx1dy1 * ax1cy2 * ax1dy2 * ax1dy2 * bx1dy2 + -24
				* ax1by1 * ax1dy1 * bx1cy2 * ax1dy2 * ax1dy2 * bx1dy2 + 12
				* ax1by1 * ax1dy1 * ax1cy2 * ax1dy2 * bx1dy2 * bx1dy2 + 12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * dx1cy2 + -10
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1dy1 * dx1cy2 + 6
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * cx1dy1 * ax1cy2 + -8
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1dy1 * cx2dy2 + -12
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * cx1cy2 * ax1dy2 + -12
				* ax1by1 * ax1cy1 * ax1cy1 * ax1dy1 * cx1cy2 * cx1dy2 + 4
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1cy2 * dx1dy2 + 8
				* ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * dx1cy2 + 8
				* ax1by1 * ax1cy1 * bx1dy1 * bx1dy1 * bx1dy1 * ax1cy2 + 18
				* ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1cy2 * dx1dy2 + -54
				* ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * cx1cy2 * bx1dy2 + -16
				* ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1cy2 * dx1dy2 + 24
				* ax1by1 * ax1by1 * bx1cy1 * ax1dy1 * dx1cy2 * bx1dy2 + -24
				* ax1by1 * bx1cy1 * ax1dy1 * bx1dy1 * ax1cy2 * bx1dy2 + -6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy2 * cx2dy2 + -4
				* ax1cy1 * ax1cy1 * ax1cy1 * cx1cy2 * ax1dy2 * bx1dy2 + 10
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy2 * ax1dy2 * cx1dy2 + -8
				* ax1by1 * ax1by1 * ax1by1 * bx1cy2 * cx1dy2 * dx1dy2 + -28
				* ax1by1 * ax1by1 * ax1by1 * cx1cy2 * bx1dy2 * dx1dy2 + 24
				* ax1by1 * ax1by1 * ax1by1 * dx1cy2 * bx1dy2 * cx1dy2 + 30
				* ax1by1 * ax1by1 * ax1dy1 * cx1cy2 * bx1dy2 * bx1dy2 + 24
				* ax1by1 * ax1cy1 * bx1dy1 * ax1cy2 * bx1dy2 * bx1dy2 + 8
				* ax1by1 * ax1by1 * ax1by1 * bx1dy2 * cx1dy2 * cx2dy2 + -8
				* ax1by1 * ax1cy1 * ax1cy2 * bx1dy2 * bx1dy2 * bx1dy2 + 8
				* ax1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1cy2 + -12
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1cy2 + -12
				* bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1dy2 + 12
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1cy2 * bx1dy2 + 12
				* ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * ax1cy2 * ax1dy2 + -42
				* ax1by1 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * cx2dy2 + 36
				* ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1dy2 * cx2dy2 + -12
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1cy2 * bx1dy2 * bx1dy2 + -12
				* ax1cy1 * ax1cy1 * bx1dy1 * bx1cy2 * ax1dy2 * ax1dy2 + 12
				* bx1cy1 * bx1cy1 * ax1dy1 * ax1cy2 * ax1dy2 * ax1dy2 + -8
				* ax1cy1 * bx1cy1 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2 + 12
				* ax1cy1 * ax1cy1 * bx1cy2 * ax1dy2 * ax1dy2 * bx1dy2 + -6
				* ax1by1 * ax1by1 * ax1cy1 * ax1dy2 * cx1dy2 * cx2dy2 + 8
				* ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx2dy2 + -16
				* ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1cy2 * ax1dy2 + -24
				* ax1cy1 * ax1dy1 * ax1dy1 * bx1dy1 * ax1cy2 * ax1dy2 + -48
				* ax1by1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1cy2 * ax1dy2 + -12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1cy2 * dx1dy2 + 18
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1cy2 * cx1dy2 + -24
				* ax1cy1 * bx1dy1 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2 + 24
				* ax1by1 * ax1cy1 * ax1dy1 * ax1dy2 * ax1dy2 * cx2dy2 + -16
				* bx1cy1 * ax1dy1 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2 + -12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy2 * ax1dy2 * dx1dy2 + -18
				* ax1cy1 * ax1cy1 * cx1dy1 * ax1cy2 * ax1dy2 * ax1dy2 + -4
				* ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1cy2 * dx1dy2 + -8
				* ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1cy2 * bx1dy2 + 36
				* ax1cy1 * ax1dy1 * ax1dy1 * bx1cy2 * ax1dy2 * ax1dy2 + -72
				* ax1cy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1dy2 * bx1dy2 + 72
				* ax1cy1 * ax1dy1 * bx1dy1 * ax1cy2 * ax1dy2 * ax1dy2;
		coeffs[2] = 3 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1cy2 * ax1cy2
				+ 6 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * ax1cy2 * ax1cy2
				+ 21 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * cx1cy2 * cx1cy2
				+ 9 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * cx1dy2 * cx1dy2
				+ 21 * ax1by1 * ax1by1 * cx1cy2 * cx1cy2 * ax1dy2 * ax1dy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * cx2dy2 * cx2dy2
				+ 12 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * cx1dy2 * cx1dy2
				+ -6 * bx1cy1 * bx1cy1 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1cy2 * bx1cy2
				+ 6 * ax1by1 * ax1by1 * bx1cy2 * bx1cy2 * bx1dy2 * bx1dy2
				+ ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1cy2 * cx1cy2
				+ 15 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2
				+ 90 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 15 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1cy2 * cx1cy2
				+ -6 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2
				+ 30 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1dy2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1dy2 * ax1dy2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1dy2
				+ 60 * ax1dy1 * ax1dy1 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -30 * ax1dy1 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -60 * ax1dy1 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 6 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1by2
				+ -4 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1dy1 * bx1by2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1by2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1by2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1cy2 * bx1cy2 * bx1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1by2 * cx1dy2
				+ 4 * ax1by1 * ax1by1 * bx1by2 * bx1dy2 * bx1dy2 * bx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy2 * bx1cy2 * ax1dy2 * bx1dy2
				+ 4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1dy1 * cx1by2
				+ -4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1by2 * cx1dy2
				+ -2 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1cy2 * cx1cy2
				+ -2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1dy1 * cx1by2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1by2 * cx1dy2
				+ -72 * ax1by1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1by2 * ax1dy2
				+ -48 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1by2 * ax1dy2
				+ 144 * ax1by1 * ax1dy1 * ax1dy1 * ax1by2 * ax1dy2 * cx1dy2
				+ 144 * ax1by1 * ax1dy1 * ax1dy1 * ax1cy2 * cx1cy2 * ax1dy2
				+ 36 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1by2 * ax1dy2
				+ -18 * ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * ax1dy2 * bx1dy2
				+ -18 * ax1by1 * ax1dy1 * ax1dy1 * bx1cy2 * bx1cy2 * ax1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * cx1by2 * ax1dy2
				+ -144 * ax1by1 * ax1dy1 * ax1by2 * ax1dy2 * ax1dy2 * cx1dy2
				+ -144 * ax1by1 * ax1dy1 * ax1cy2 * cx1cy2 * ax1dy2 * ax1dy2
				+ 36 * ax1by1 * ax1dy1 * bx1by2 * ax1dy2 * ax1dy2 * bx1dy2
				+ 18 * ax1by1 * ax1dy1 * bx1cy2 * bx1cy2 * ax1dy2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1dy1 * cx1by2 * ax1dy2 * ax1dy2
				+ 24 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1by2
				+ -4 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1by2
				+ 10 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1by2
				+ -24 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1cy2 * cx1cy2
				+ -4 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1by2
				+ 6 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1cy2 * bx1cy2
				+ -8 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1by2
				+ 24 * ax1by1 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2 * cx1dy2
				+ 6 * ax1by1 * cx1by2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 48 * ax1by1 * ax1cy2 * cx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -10 * ax1by1 * bx1by2 * ax1dy2 * ax1dy2 * ax1dy2 * bx1dy2
				+ -6 * ax1by1 * bx1cy2 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 8 * ax1cy1 * ax1cy1 * cx1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * ax1by2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * ax1by2 * bx1dy2 * cx1dy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1cy2 * cx1cy2 * bx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * cx1by2 * bx1dy2
				+ 24 * ax1by1 * ax1cy1 * ax1cy1 * bx1dy1 * cx1by2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * cx1dy1 * ax1by2
				+ 10 * ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * cx1by2
				+ -6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1by2 * cx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1cy2 * cx1cy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * bx1dy1 * cx1by2
				+ -6 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1by2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * cx1by2
				+ 6 * ax1by1 * ax1by1 * cx1dy1 * ax1by2 * bx1dy2 * bx1dy2
				+ -6 * ax1by1 * ax1by1 * ax1by2 * bx1dy2 * bx1dy2 * cx1dy2
				+ -10 * ax1by1 * ax1by1 * cx1by2 * ax1dy2 * bx1dy2 * bx1dy2
				+ -18 * ax1by1 * ax1by1 * ax1cy2 * cx1cy2 * bx1dy2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * cx1by2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1by2 * bx1dy2 * bx1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1dy1 * ax1by2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * cx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1cy2 * cx1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1cy2 * bx1cy2
				+ 12 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * ax1cy2 * ax1cy2
				+ -6 * ax1cy1 * ax1cy1 * cx1dy1 * ax1cy2 * ax1cy2 * ax1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1by2 * cx1dy2 * cx1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1cy2 * cx1cy2 * cx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy2 * bx1cy2 * cx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy2 * ax1cy2 * ax1dy2 * cx1dy2
				+ -24 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * cx1dy1 * ax1by2
				+ 84 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * ax1by2 * cx1dy2
				+ -36 * ax1by1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1cy2 * ax1cy2
				+ -40 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1dy2
				+ -20 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2
				+ 60 * bx1cy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1dy2 * ax1dy2
				+ 60 * bx1cy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1dy2
				+ -36 * ax1by1 * ax1by1 * cx1dy1 * ax1by2 * ax1dy2 * cx1dy2
				+ 24 * ax1by1 * ax1by1 * ax1by2 * ax1dy2 * cx1dy2 * cx1dy2
				+ -18 * ax1by1 * ax1by1 * bx1cy2 * bx1cy2 * ax1dy2 * cx1dy2
				+ -64 * bx1cy1 * ax1dy1 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -60 * bx1cy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 10 * bx1cy1 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 8 * bx1cy1 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 42 * ax1by1 * ax1by1 * ax1cy1 * ax1cy2 * cx1cy2 * dx1dy2
				+ -2 * ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1cy2 * bx1cy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy2 * bx1cy2 * cx1dy2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy2 * cx1cy2 * bx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * bx1cy2 * cx1cy2
				+ -24 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx2dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy2 * ax1dy2 * cx2dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1cy2 * dx1cy2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy2 * bx2dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy1 * dx1by2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy2 * ax1dy2 * bx2dy2
				+ -6 * ax1by1 * ax1by1 * ax1dy2 * ax1dy2 * bx1dy2 * bx2dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * dx1by2
				+ 36 * ax1by1 * ax1dy1 * ax1dy1 * cx1by2 * ax1dy2 * ax1dy2
				+ 36 * ax1by1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2 * cx1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * ax1dy2 * dx1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1cy2 * dx1cy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1cy2 * cx2dy2
				+ 24 * ax1cy1 * ax1dy1 * bx1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -18 * ax1cy1 * ax1cy1 * ax1dy1 * ax1cy2 * ax1cy2 * cx1dy2
				+ -48 * ax1by1 * ax1cy1 * dx1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * dx1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1by2 * ax1dy2 * ax1dy2 * dx1dy2
				+ 18 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1by2 * cx1dy2
				+ -36 * ax1by1 * cx1dy1 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax1dy2 * dx1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1cy2 * ax1dy2 * cx2dy2
				+ -18 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * ax1dy2 * ax1dy2
				+ -24 * ax1cy1 * ax1cy2 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1by2 * dx1dy2
				+ 36 * ax1cy1 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2 * bx1dy2
				+ -8 * ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1by2 * bx1dy2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * dx1by2
				+ 8 * ax1by1 * ax1cy1 * bx1dy1 * bx1dy1 * bx1dy1 * ax1by2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1dy2 * cx1dy2 * bx2dy2
				+ -8 * ax1by1 * ax1cy1 * ax1by2 * bx1dy2 * bx1dy2 * bx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * ax1dy2 * cx1dy2
				+ 4 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * cx1by2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * bx1by2
				+ 4 * bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1by2 + 6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * bx1by2 + 8
				* ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1cy2 * bx1cy2 + -12
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1cy2 * dx1cy2 + 6
				* ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1cy2 * ax1cy2 + -8
				* ax1by1 * bx1cy2 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2 + -12
				* ax1by1 * ax1by1 * dx1by2 * ax1dy2 * ax1dy2 * bx1dy2 + -12
				* ax1by1 * ax1by1 * bx1cy2 * ax1dy2 * ax1dy2 * cx2dy2 + 4
				* ax1by1 * bx1dy1 * bx1by2 * ax1dy2 * ax1dy2 * ax1dy2 + -6
				* ax1by1 * ax1cy2 * ax1cy2 * ax1dy2 * bx1dy2 * bx1dy2 + 12
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy2 * bx1dy2 * bx2dy2 + -28
				* ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1by2 * dx1dy2 + 24
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * bx2dy2 + -30
				* ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * cx1by2 * ax1dy2 + 4
				* ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1by2 * bx1dy2 + 16
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * bx1by2 * dx1dy2 + -12
				* ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * ax1by2 * dx1dy2 + 24
				* ax1by1 * ax1by1 * bx1cy1 * ax1dy1 * bx1by2 * dx1dy2 + -12
				* ax1by1 * ax1by1 * ax1by1 * ax1dy1 * dx1dy2 * bx2dy2 + -24
				* ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * bx1by2 * cx1dy2 + 6
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy2 * bx2dy2 + -24
				* ax1by1 * bx1cy1 * ax1dy1 * bx1dy1 * ax1by2 * bx1dy2 + -6
				* ax1by1 * bx1dy1 * bx1dy1 * ax1by2 * ax1dy2 * ax1dy2 + -4
				* ax1by1 * ax1by1 * ax1by1 * cx1by2 * bx1dy2 * dx1dy2 + -36
				* ax1by1 * ax1dy1 * bx1dy1 * ax1by2 * ax1dy2 * bx1dy2 + 6
				* ax1by1 * ax1by1 * ax1dy1 * cx1by2 * bx1dy2 * bx1dy2 + -24
				* ax1by1 * ax1dy1 * bx1dy1 * ax1cy2 * ax1cy2 * bx1dy2 + -8
				* ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1dy2 * dx1dy2 + -12
				* ax1by1 * ax1by1 * ax1dy1 * ax1cy2 * dx1cy2 * bx1dy2 + 24
				* ax1by1 * ax1by1 * ax1dy1 * bx1by2 * bx1dy2 * cx1dy2 + -18
				* ax1by1 * bx1dy1 * bx1dy1 * ax1cy2 * ax1cy2 * ax1dy2 + 24
				* ax1by1 * ax1cy1 * bx1dy1 * bx1by2 * ax1dy2 * bx1dy2 + 12
				* ax1by1 * ax1by1 * ax1by1 * dx1cy2 * dx1cy2 * ax1dy2 + 6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * dx1by2 + 12
				* ax1by1 * bx1dy1 * ax1cy2 * bx1cy2 * ax1dy2 * ax1dy2 + -4
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1dy1 * dx1by2 + 12
				* ax1by1 * bx1dy1 * ax1cy2 * ax1cy2 * ax1dy2 * bx1dy2 + -4
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * dx1by2 * bx1dy2 + 18
				* ax1by1 * ax1dy1 * ax1cy2 * ax1cy2 * bx1dy2 * bx1dy2 + -8
				* ax1by1 * ax1cy1 * ax1cy1 * ax1cy1 * dx1by2 * cx1dy2 + -12
				* ax1by1 * ax1by1 * ax1by1 * ax1dy1 * cx2dy2 * cx2dy2 + -12
				* ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1by2 * ax1dy2 + 12
				* ax1by1 * bx1cy1 * ax1dy1 * ax1dy1 * bx1cy2 * bx1cy2 + -4
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1by2 * dx1dy2 + -24
				* ax1by1 * ax1cy1 * ax1dy1 * bx1dy1 * bx1cy2 * bx1cy2 + -12
				* ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * cx1dy2 + -8
				* ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1cy2 * dx1cy2 + 2
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy2 * dx1cy2 + -24
				* ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1cy2 * cx1cy2 + -8
				* ax1by1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1cy2 * dx1cy2 + 6
				* ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1cy2 * dx1cy2 + -6
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * ax1cy2 * cx1cy2 + -6
				* ax1by1 * ax1by1 * ax1cy1 * bx1cy2 * bx1cy2 * dx1dy2 + -6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy2 * bx2dy2 + 12
				* ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1cy2 * cx2dy2 + 4
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1dy2 * bx2dy2 + 24
				* ax1by1 * ax1cy1 * ax1dy1 * bx1cy2 * bx1cy2 * bx1dy2 + 2
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy2 * cx2dy2 + 24
				* ax1by1 * ax1by1 * cx1dy1 * bx1cy2 * bx1cy2 * ax1dy2 + 8
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy2 * cx1cy2 * ax1dy2 + -16
				* ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1cy2 * cx2dy2 + -2
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1cy2 * cx1cy2 * bx1dy2 + 12
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1cy2 * ax1cy2 + 8
				* ax1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1by2 + -12
				* ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1cy2 * dx1cy2 + -12
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1by2 + 12
				* ax1by1 * ax1cy1 * ax1dy1 * bx1dy1 * ax1cy2 * cx1cy2 + -6
				* ax1by1 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * dx1by2 + 18
				* ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * ax1by2 * ax1dy2 + -18
				* bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1dy2 + 36
				* ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * bx1by2 * ax1dy2 + -48
				* ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * ax1by2 * bx1dy2 + 6
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * cx1by2 * cx1dy2 + -6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + 12
				* ax1by1 * ax1cy1 * bx1cy1 * ax1dy1 * bx1dy1 * cx1by2 + 6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + 2
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * cx1by2 + -6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + -6
				* ax1by1 * ax1by1 * ax1cy1 * cx1dy1 * cx1dy1 * bx1by2 + 6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + 10
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * cx1by2 + -6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + 4
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1by2 * ax1dy2 + 6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + 12
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1dy1 * bx2dy2 + -6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + -12
				* ax1cy1 * ax1cy1 * bx1cy1 * ax1dy1 * bx1by2 * bx1dy2 + 6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + 8
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1by2 * bx1dy2 + -6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + -20
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1by2 * cx1dy2 + 6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + -2
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1cy2 * bx1cy2 + -6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + 6
				* ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1cy2 * bx1cy2 + 6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + 12
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1cy2 * dx1cy2 + -6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + 2
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1cy2 * ax1cy2 + 6
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1dy2 + -10
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1cy2 * cx1cy2 + 12
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1by2 * bx1dy2 + 2
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1cy2 * bx1cy2 * bx1dy2 + -12
				* ax1cy1 * ax1cy1 * bx1dy1 * bx1by2 * ax1dy2 * ax1dy2 + -6
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy2 * bx1cy2 * cx1dy2 + -24
				* ax1by1 * ax1cy1 * ax1dy1 * bx1by2 * ax1dy2 * cx1dy2 + 12
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1cy2 * cx2dy2 + -30
				* ax1by1 * ax1by1 * ax1dy1 * ax1by2 * cx1dy2 * cx1dy2 + -2
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1cy2 * ax1cy2 * ax1dy2 + 12
				* ax1by1 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy2 * bx2dy2 + 10
				* ax1by1 * ax1by1 * ax1by1 * cx1cy2 * cx1cy2 * cx1dy2 + -6
				* ax1by1 * ax1by1 * cx1dy1 * cx1by2 * ax1dy2 * ax1dy2 + 2
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * bx1dy2 * bx1dy2 + -8
				* ax1cy1 * bx1cy1 * bx1by2 * ax1dy2 * ax1dy2 * ax1dy2 + -6
				* ax1by1 * ax1cy1 * ax1cy1 * cx1by2 * bx1dy2 * bx1dy2 + 12
				* ax1cy1 * ax1cy1 * bx1by2 * ax1dy2 * ax1dy2 * bx1dy2 + -12
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1dy2 * bx2dy2 + -6
				* ax1by1 * ax1by1 * ax1cy1 * ax1dy2 * cx1dy2 * bx2dy2 + -2
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1by2 * ax1dy2 * ax1dy2 + -14
				* ax1by1 * ax1by1 * ax1by1 * bx1cy1 * cx2dy2 * cx2dy2 + 10
				* ax1by1 * ax1by1 * ax1by1 * cx1by2 * cx1dy2 * cx1dy2 + -12
				* ax1by1 * bx1cy1 * ax1cy2 * bx1cy2 * ax1dy2 * bx1dy2 + -54
				* ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1cy2 * cx2dy2 + -60
				* ax1by1 * ax1by1 * ax1cy1 * cx1dy1 * ax1cy2 * cx2dy2 + -72
				* ax1by1 * ax1by1 * ax1dy1 * cx1cy2 * cx1cy2 * ax1dy2 + -24
				* ax1by1 * ax1cy1 * ax1dy1 * ax1cy2 * cx1cy2 * bx1dy2 + -12
				* ax1cy1 * ax1cy1 * bx1dy1 * ax1cy2 * ax1cy2 * bx1dy2;
		coeffs[3] = -6 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2
				+ 30 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1dy2
				+ 30 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1cy2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1dy2 * ax1dy2
				+ -120 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1cy2 * ax1dy2
				+ -20 * ax1dy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2
				+ 60 * ax1dy1 * ax1dy1 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 180 * ax1dy1 * ax1dy1 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 60 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -30 * ax1dy1 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -120 * ax1dy1 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -60 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 6 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 30 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 20 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1ay2
				+ -4 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1dy1 * bx1ay2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1ay2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1by2 * bx1cy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1dy2 * bx1dy2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * bx1by2 * bx1cy2 * bx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1dy1 * bx1cy2 * bx1cy2 * bx1cy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1ay2 * cx1dy2
				+ 4 * ax1by1 * ax1by1 * bx1ay2 * bx1dy2 * bx1dy2 * bx1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1by2 * cx1cy2
				+ 12 * ax1by1 * ax1by1 * bx1by2 * bx1cy2 * bx1dy2 * bx1dy2
				+ 4 * ax1by1 * ax1by1 * bx1cy2 * bx1cy2 * bx1cy2 * bx1dy2
				+ -4 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1ay2
				+ -2 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1ay2
				+ 12 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1dy2
				+ 12 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1by2 * bx1cy2
				+ 24 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1ay2
				+ 6 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1ay2
				+ -8 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * cx1dy2
				+ -16 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * bx1dy2
				+ -32 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * cx1cy2
				+ 6 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * cx1ay2 * ax1dy2
				+ -18 * ax1by1 * ax1dy1 * ax1dy1 * bx1ay2 * ax1dy2 * bx1dy2
				+ -36 * ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * bx1cy2 * ax1dy2
				+ 144 * ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1dy2 * cx1dy2
				+ 60 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1dy2 * ax1dy2
				+ 96 * ax1by1 * ax1dy1 * ax1dy1 * ax1by2 * cx1cy2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1dy1 * cx1ay2 * ax1dy2 * ax1dy2
				+ 36 * ax1by1 * ax1dy1 * bx1ay2 * ax1dy2 * ax1dy2 * bx1dy2
				+ 36 * ax1by1 * ax1dy1 * bx1by2 * bx1cy2 * ax1dy2 * ax1dy2
				+ -72 * ax1by1 * ax1dy1 * ax1ay2 * ax1dy2 * ax1dy2 * cx1dy2
				+ -8 * ax1by1 * ax1dy1 * cx1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -40 * ax1cy1 * ax1dy1 * bx1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -96 * ax1by1 * ax1dy1 * ax1by2 * cx1cy2 * ax1dy2 * ax1dy2
				+ 2 * ax1cy1 * ax1cy1 * cx1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -4 * ax1by1 * bx1ay2 * ax1dy2 * ax1dy2 * ax1dy2 * bx1dy2
				+ -12 * ax1by1 * bx1by2 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 24 * ax1by1 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2 * cx1dy2
				+ 6 * ax1by1 * cx1ay2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 10 * bx1cy1 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 32 * ax1by1 * ax1by2 * cx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1ay2
				+ 24 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * ax1by2 * ax1cy2
				+ -12 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * ax1cy2 * ax1dy2
				+ -8 * ax1cy1 * ax1cy1 * cx1dy1 * ax1cy2 * ax1cy2 * ax1cy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1dy1 * cx1dy1 * bx1ay2
				+ 24 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1ay2 * bx1dy2
				+ 24 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1by2 * bx1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1dy1 * ax1ay2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * cx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * cx1cy2
				+ 4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1dy1 * cx1ay2
				+ -6 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1ay2
				+ -20 * ax1by1 * bx1dy1 * bx1dy1 * ax1cy2 * ax1cy2 * ax1cy2
				+ 6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * cx1dy1 * ax1ay2
				+ 10 * ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * cx1ay2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * cx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1by2 * cx1cy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * bx1dy1 * cx1ay2
				+ 12 * ax1by1 * bx1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * bx1dy2 * cx1dy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1by2 * cx1cy2 * bx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * cx1ay2 * bx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1ay2 * ax1dy2 * ax1dy2 * cx1dy2
				+ 24 * ax1cy1 * ax1cy1 * ax1by2 * ax1cy2 * ax1dy2 * cx1dy2
				+ 8 * ax1cy1 * ax1cy1 * ax1cy2 * ax1cy2 * ax1cy2 * cx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1dy2 * cx1dy2
				+ -24 * ax1by1 * ax1cy1 * ax1cy1 * bx1by2 * bx1cy2 * cx1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1dy2 * cx1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1by2 * cx1cy2 * cx1dy2
				+ -4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1ay2 * cx1dy2
				+ -6 * ax1by1 * ax1ay2 * ax1dy2 * ax1dy2 * bx1dy2 * bx1dy2
				+ -20 * ax1by1 * ax1cy2 * ax1cy2 * ax1cy2 * bx1dy2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * bx1dy2 * bx1dy2
				+ -6 * ax1by1 * ax1by1 * ax1ay2 * bx1dy2 * bx1dy2 * cx1dy2
				+ -10 * ax1by1 * ax1by1 * cx1ay2 * ax1dy2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1by2 * cx1cy2 * bx1dy2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * cx1ay2 * bx1dy2 * bx1dy2
				+ 24 * ax1by1 * ax1dy1 * cx1dy1 * ax1cy2 * ax1cy2 * ax1cy2
				+ -24 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * cx1dy1 * ax1ay2
				+ 36 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * cx1dy2
				+ 36 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * ax1by2 * cx1cy2
				+ -36 * ax1by1 * ax1cy1 * ax1dy1 * bx1dy1 * bx1ay2 * bx1dy2
				+ -36 * ax1by1 * ax1cy1 * ax1dy1 * bx1dy1 * bx1by2 * bx1cy2
				+ 10 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2
				+ 20 * bx1cy1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2
				+ -32 * ax1by1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * cx1dy2
				+ 6 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * cx1ay2 * cx1dy2
				+ -54 * ax1by1 * ax1by1 * ax1dy1 * ax1ay2 * cx1dy2 * cx1dy2
				+ -84 * ax1by1 * ax1by1 * ax1dy1 * ax1by2 * cx1cy2 * cx1dy2
				+ 12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1dy2 * ax2dy2
				+ 24 * ax1by1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2 * cx1dy2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * ax1dy2 * cx1dy2 * cx1dy2
				+ -6 * ax1by1 * ax1by1 * ax1cy1 * ax1dy2 * cx1dy2 * ax2dy2
				+ 36 * ax1by1 * ax1by1 * ax1by2 * cx1cy2 * ax1dy2 * cx1dy2
				+ -36 * ax1by1 * ax1cy1 * bx1by2 * bx1cy2 * ax1dy2 * bx1dy2
				+ 20 * bx1cy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1by2 * bx1cy2
				+ -2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1dy1 * cx1ay2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1by2 * bx1cy2 * cx1dy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1ay2 * cx1dy2
				+ -18 * ax1by1 * ax1by1 * cx1dy1 * cx1ay2 * ax1dy2 * ax1dy2
				+ -12 * ax1by1 * ax1by1 * cx1dy1 * cx1by2 * ax1cy2 * ax1dy2
				+ 18 * ax1by1 * ax1by1 * cx1dy1 * ax1cy2 * bx1cy2 * bx1cy2
				+ -6 * ax1by1 * ax1by1 * cx1dy1 * ax1cy2 * ax1cy2 * cx1cy2
				+ 36 * ax1by1 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2 * cx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * cx1by2 * cx1cy2
				+ -18 * ax1by1 * ax1by1 * ax1dy1 * ax1cy2 * cx1cy2 * cx1cy2
				+ -18 * ax1by1 * ax1by1 * ax1cy2 * bx1cy2 * bx1cy2 * cx1dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * cx1cy2 * cx1dy2
				+ 36 * ax1by1 * ax1cy2 * ax1cy2 * cx1cy2 * ax1dy2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * cx1by2 * cx1cy2 * ax1dy2 * ax1dy2
				+ 18 * ax1by1 * ax1by1 * ax1cy2 * cx1cy2 * cx1cy2 * ax1dy2
				+ -4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1by2 * cx1cy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy2 * bx1cy2 * cx1cy2
				+ -6 * ax1by1 * ax1by1 * bx1cy1 * ax1cy2 * cx1cy2 * cx1cy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1by2 * cx1cy2
				+ 4 * ax1by1 * ax1cy1 * ax1cy1 * ax1cy2 * cx1cy2 * cx1cy2
				+ 40 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1dy2
				+ 40 * bx1cy1 * ax1dy1 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy2 * bx1cy2 * cx1cy2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy2 * ax2dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy2 * ax1cy2 * cx2dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy1 * dx1ay2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy2 * ax1cy2 * dx1cy2
				+ -40 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1cy2
				+ 24 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx2cy2
				+ -88 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * cx1cy2
				+ 36 * ax1by1 * ax1dy1 * ax1dy1 * cx1ay2 * ax1dy2 * ax1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1by2 * dx1cy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * ax1dy2 * dx1dy2
				+ 72 * ax1cy1 * ax1dy1 * ax1dy1 * bx1by2 * ax1cy2 * ax1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1by2 * cx2dy2
				+ 144 * ax1by1 * ax1dy1 * cx1dy1 * ax1by2 * ax1cy2 * ax1dy2
				+ 48 * ax1cy1 * ax1dy1 * ax1dy1 * ax1by2 * bx1cy2 * ax1dy2
				+ -120 * ax1cy1 * ax1dy1 * ax1by2 * bx1cy2 * ax1dy2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx2cy2
				+ -72 * ax1by1 * ax1cy1 * dx1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * dx1cy2
				+ -144 * ax1by1 * cx1dy1 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * dx1by2 * ax1cy2 * ax1dy2
				+ -144 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * ax1dy2 * cx2dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * cx2dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * dx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax1cy2 * dx1dy2
				+ 18 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * cx1dy2
				+ -36 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * ax1cy2 * cx1dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1dy2 * dx1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * bx2cy2 * ax1dy2 * ax1dy2
				+ -18 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1dy2 * ax1dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax1dy2 * cx2dy2
				+ 40 * ax1cy1 * ax1by2 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -24 * ax1cy1 * bx1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 24 * ax1by1 * cx1by2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 24 * ax1by1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * cx1dy2
				+ 24 * ax1by1 * ax1by2 * cx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -72 * ax1by1 * ax1dy1 * ax1cy2 * ax1cy2 * cx1cy2 * ax1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1by2 * ax1dy2 * ax1dy2 * cx2dy2
				+ -16 * ax1cy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * bx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy1 * bx2cy2
				+ 8 * ax1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1ay2 * ax1dy2
				+ -8 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * cx1by2 * bx1cy2
				+ -32 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1ay2 * ax1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1ay2 * dx1dy2
				+ -72 * ax1cy1 * ax1dy1 * ax1dy1 * bx1dy1 * ax1ay2 * ax1dy2
				+ -8 * ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1ay2 * bx1dy2
				+ 24 * ax1by1 * ax1cy1 * dx1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx2cy2 * bx1dy2
				+ -40 * bx1cy1 * ax1dy1 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 8 * ax1by1 * ax1by1 * bx1cy1 * cx1by2 * bx1cy2 * bx1dy2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * dx1ay2
				+ 8 * ax1by1 * ax1cy1 * bx1dy1 * bx1dy1 * bx1dy1 * ax1ay2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy2 * cx1cy2 * dx1cy2
				+ 8 * ax1by1 * ax1cy1 * ax1dy1 * bx1cy2 * bx1cy2 * bx1cy2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1dy2 * cx1dy2 * ax2dy2
				+ -8 * ax1by1 * ax1cy1 * ax1ay2 * bx1dy2 * bx1dy2 * bx1dy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy2 * cx1cy2 * cx2dy2
				+ -8 * ax1by1 * ax1cy1 * bx1cy2 * bx1cy2 * bx1cy2 * ax1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * dx1ay2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * bx1ay2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * ax2dy2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1dy1 * dx1ay2
				+ 16 * ax1by1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * ax2dy2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * bx1ay2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * dx1ay2 * ax1dy2
				+ 10 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * cx1ay2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * cx1ay2 * bx1dy2
				+ 6 * ax1cy1 * ax1cy1 * bx1cy1 * bx1dy1 * bx1dy1 * ax1ay2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * cx1dy2
				+ -2 * bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx2cy2
				+ -16 * ax1by1 * bx1by2 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 16 * ax1by1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * bx2cy2
				+ -12 * ax1by1 * ax1by1 * bx1cy2 * ax1dy2 * ax1dy2 * bx2dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * cx1by2 * bx1cy2
				+ -12 * ax1by1 * ax1by1 * bx1by2 * ax1dy2 * ax1dy2 * cx2dy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * dx1by2 * ax1cy2
				+ -12 * ax1by1 * ax1by2 * ax1cy2 * ax1dy2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * cx1cy2
				+ -24 * ax1by1 * ax1by1 * dx1ay2 * ax1dy2 * ax1dy2 * bx1dy2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * ax1cy2 * dx1cy2 * dx1cy2
				+ 4 * ax1by1 * bx1dy1 * bx1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * ax1cy2 * ax1cy2 * dx1cy2
				+ 12 * ax1by1 * ax1by1 * ax1ay2 * ax1dy2 * bx1dy2 * dx1dy2
				+ 16 * ax1by1 * bx1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * bx1dy2
				+ 16 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1by2 * bx1cy2
				+ 12 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * dx1cy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * dx1cy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * ax1cy2 * ax1cy2 * cx2dy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * dx1by2 * bx1cy2
				+ 12 * ax1by1 * ax1dy1 * ax1cy2 * bx1cy2 * bx1cy2 * ax1dy2
				+ 12 * ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1by2 * ax1cy2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1dy2 * bx1dy2
				+ 16 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1dy2
				+ 6 * ax1cy1 * ax1cy1 * bx1cy1 * ax1ay2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * dx1ay2 * bx1dy2
				+ -2 * bx1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1dy2 * ax1dy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1ay2 * dx1dy2
				+ 10 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1dy2 * cx1dy2
				+ 12 * ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1dy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy2 * ax2dy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * dx1dy2 * ax2dy2
				+ -4 * ax1by1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy2 * ax2dy2
				+ 6 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy2 * ax2dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * ax1dy2 * cx1dy2
				+ -6 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1dy2 * ax1dy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx2cy2 * bx1dy2
				+ -36 * ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * ax1dy2 * bx1dy2
				+ -16 * ax1by1 * ax1cy1 * ax1cy1 * ax1cy1 * bx2cy2 * cx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * dx1by2 * ax1cy2 * dx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * cx1by2 * bx1cy2 * ax1dy2
				+ -36 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * dx1cy2 * bx2dy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1cy2 * bx2dy2
				+ 24 * ax1by1 * ax1by1 * ax1dy1 * ax1by2 * bx1cy2 * dx1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1by2 * cx1cy2 * cx1dy2
				+ -12 * ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * bx1cy2 * ax1dy2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * ax1cy2 * cx2dy2 * cx2dy2
				+ -72 * ax1by1 * ax1dy1 * bx1dy1 * ax1by2 * ax1cy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * bx1dy2 * cx2dy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * dx1cy2 * ax1dy2 * bx2dy2
				+ -4 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * bx1dy2 * dx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * bx2cy2 * ax1dy2 * ax1dy2
				+ 30 * ax1by1 * ax1by1 * ax1dy1 * cx1ay2 * bx1dy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * dx1by2 * ax1cy2 * ax1dy2 * bx1dy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1dy2 * dx1dy2
				+ 12 * ax1by1 * ax1dy1 * ax1by2 * ax1cy2 * bx1dy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * bx1dy2 * dx1dy2
				+ 72 * ax1by1 * bx1dy1 * ax1by2 * ax1cy2 * ax1dy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * cx1dy1 * bx1ay2 * ax1dy2 * bx1dy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1by2 * cx2dy2
				+ 24 * ax1by1 * bx1cy1 * bx1dy1 * ax1ay2 * ax1dy2 * bx1dy2
				+ -40 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * bx1by2 * cx2dy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * ax2dy2
				+ 48 * ax1by1 * ax1by1 * cx1dy1 * bx1by2 * ax1cy2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1dy1 * ax2dy2
				+ -24 * ax1by1 * bx1cy1 * bx1dy1 * ax1by2 * ax1cy2 * bx1dy2
				+ 12 * ax1by1 * bx1cy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1cy2 * bx2dy2
				+ -4 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1ay2 * dx1dy2
				+ 36 * ax1by1 * ax1by1 * ax1cy1 * bx1by2 * dx1cy2 * bx1dy2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * bx1ay2 * ax1dy2
				+ 72 * ax1by1 * ax1cy1 * bx1dy1 * bx1by2 * ax1cy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * bx2cy2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * bx2cy2 * dx1dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1dy1 * bx2cy2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1by2 * cx2dy2
				+ 12 * ax1by1 * bx1cy1 * ax1dy1 * ax1dy1 * bx1by2 * bx1cy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1by2 * cx2dy2
				+ -4 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1by2 * dx1cy2
				+ 8 * ax1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1ay2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * bx1by2 * ax1cy2
				+ -12 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1ay2
				+ -8 * ax1cy1 * bx1cy1 * bx1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -6 * ax1by1 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * dx1ay2
				+ 12 * ax1cy1 * ax1cy1 * bx1ay2 * ax1dy2 * ax1dy2 * bx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * bx2cy2 * bx1dy2 * cx1dy2
				+ 30 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1ay2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx2cy2 * bx1dy2 * bx1dy2
				+ 24 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * bx1ay2 * ax1dy2
				+ 12 * ax1by1 * bx1cy1 * bx1by2 * bx1cy2 * ax1dy2 * ax1dy2
				+ 12 * ax1by1 * ax1by1 * ax1cy1 * cx1dy1 * dx1ay2 * ax1dy2
				+ -4 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * bx2dy2 * cx2dy2
				+ 36 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1cy2 * bx2dy2
				+ -24 * ax1by1 * ax1by1 * bx1by2 * ax1cy2 * bx1dy2 * cx1dy2
				+ 12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * bx2cy2 * dx1dy2
				+ -12 * ax1cy1 * ax1cy1 * bx1dy1 * bx1ay2 * ax1dy2 * ax1dy2
				+ -24 * ax1by1 * ax1cy1 * ax1cy1 * dx1by2 * bx1cy2 * ax1dy2
				+ -24 * ax1by1 * ax1cy1 * ax1dy1 * bx1ay2 * ax1dy2 * cx1dy2
				+ -24 * ax1cy1 * ax1cy1 * bx1dy1 * ax1by2 * bx1cy2 * ax1dy2
				+ 24 * ax1by1 * ax1by1 * ax1cy1 * ax1ay2 * cx1dy2 * dx1dy2 + 24
				* ax1by1 * ax1cy1 * ax1cy1 * dx1by2 * bx1cy2 * ax1dy2 + -8
				* ax1by1 * ax1by1 * cx1dy1 * ax1cy2 * bx1cy2 * bx1cy2 + 24
				* bx1cy1 * bx1cy1 * ax1dy1 * ax1by2 * ax1cy2 * ax1dy2 + 6
				* ax1by1 * ax1by1 * ax1dy1 * bx1cy2 * bx1cy2 * cx1cy2 + -72
				* ax1by1 * ax1by1 * cx1dy1 * cx1by2 * ax1cy2 * ax1dy2 + 12
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1by2 * bx1cy2 + -24
				* ax1cy1 * ax1cy1 * ax1dy1 * bx1by2 * ax1cy2 * bx1dy2 + 24
				* ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * bx1by2 * ax1cy2 + 12
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1ay2 * dx1dy2 + 18
				* ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1cy2 + 4
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1ay2 * ax1dy2 + 12
				* ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * cx1by2 * ax1cy2 + -12
				* ax1cy1 * ax1cy1 * bx1cy1 * bx1dy1 * ax1ay2 * bx1dy2 + 8
				* ax1cy1 * bx1cy1 * bx1dy1 * ax1cy2 * ax1cy2 * ax1cy2 + 8
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1ay2 * bx1dy2 + -12
				* ax1cy1 * ax1cy1 * bx1dy1 * ax1cy2 * ax1cy2 * bx1cy2 + -20
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1ay2 * cx1dy2 + 8
				* ax1by1 * ax1by1 * ax1cy2 * bx1cy2 * bx1cy2 * cx1dy2 + 4
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1by2 * ax1cy2 + -6
				* ax1by1 * ax1by1 * bx1cy2 * bx1cy2 * cx1cy2 * ax1dy2 + 12
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1by2 * dx1cy2 + 12
				* ax1cy1 * ax1cy1 * bx1by2 * bx1cy2 * ax1dy2 * ax1dy2 + -12
				* ax1cy1 * ax1cy1 * bx1cy1 * bx1dy1 * ax1by2 * bx1cy2 + 24
				* ax1by1 * ax1cy1 * bx1by2 * ax1cy2 * ax1dy2 * cx1dy2 + 8
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1by2 * bx1cy2 + 18
				* ax1by1 * ax1by1 * ax1by2 * ax1cy2 * cx1dy2 * cx1dy2 + -20
				* ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1by2 * cx1cy2 + 12
				* ax1by1 * ax1by1 * cx1by2 * ax1cy2 * ax1dy2 * cx1dy2 + -4
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1by2 * ax1cy2 * ax1dy2 + -8
				* ax1cy1 * bx1cy1 * ax1cy2 * ax1cy2 * ax1cy2 * bx1dy2 + 12
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1by2 * cx2dy2 + 12
				* ax1cy1 * ax1cy1 * ax1cy2 * ax1cy2 * bx1cy2 * bx1dy2 + 12
				* ax1cy1 * ax1cy1 * bx1cy1 * ax1by2 * bx1cy2 * bx1dy2 + -2
				* ax1by1 * ax1by1 * ax1by1 * cx1cy2 * cx1cy2 * cx1cy2 + -8
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * bx1cy2 * bx1dy2 + -4
				* ax1by1 * ax1cy1 * bx1cy1 * ax1cy2 * bx1cy2 * cx1cy2 + 20
				* ax1by1 * ax1by1 * ax1by1 * cx1by2 * cx1cy2 * cx1dy2;
		coeffs[4] = 30 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1cy2
				+ 15 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2
				+ -120 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1dy2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1dy2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2
				+ 180 * ax1dy1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 90 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ 180 * ax1dy1 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 15 * ax1dy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -120 * ax1dy1 * ax1ay2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -60 * ax1dy1 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -180 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -30 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 30 * ax1ay2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 15 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 60 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 15 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1ay2 * bx1cy2
				+ 6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1by2 * bx1by2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1cy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1by2 * bx1by2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1by2 * bx1cy2 * bx1cy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1ay2 * cx1cy2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * bx1cy2 * bx1dy2 * bx1dy2
				+ ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1by2 * cx1by2
				+ 6 * ax1by1 * ax1by1 * bx1by2 * bx1by2 * bx1dy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1by2 * bx1cy2 * bx1cy2 * bx1dy2
				+ ax1by1 * ax1by1 * bx1cy2 * bx1cy2 * bx1cy2 * bx1cy2
				+ 21 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1by2 * ax1by2
				+ 3 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * cx1by2 * cx1by2
				+ 6 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1by2 * bx1by2
				+ 21 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * cx1dy2 * cx1dy2
				+ 3 * ax1by1 * ax1by1 * cx1by2 * cx1by2 * ax1dy2 * ax1dy2
				+ 6 * ax1cy1 * ax1cy1 * bx1by2 * bx1by2 * ax1dy2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * cx1cy2 * cx1cy2
				+ ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1by2 * cx1by2
				+ 12 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1cy2
				+ 20 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1by2 * bx1by2
				+ -96 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * cx1cy2
				+ -24 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * cx1by2
				+ -40 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1cy2
				+ -36 * ax1by1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1cy2 * ax1dy2
				+ -18 * ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * bx1by2 * ax1dy2
				+ 96 * ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * cx1cy2 * ax1dy2
				+ 144 * ax1by1 * ax1dy1 * ax1dy1 * ax1by2 * cx1by2 * ax1dy2
				+ 48 * ax1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * bx1cy2 * ax1dy2
				+ 36 * ax1by1 * ax1dy1 * bx1ay2 * bx1cy2 * ax1dy2 * ax1dy2
				+ 18 * ax1by1 * ax1dy1 * bx1by2 * bx1by2 * ax1dy2 * ax1dy2
				+ -96 * ax1by1 * ax1dy1 * ax1ay2 * cx1cy2 * ax1dy2 * ax1dy2
				+ -144 * ax1by1 * ax1dy1 * ax1by2 * cx1by2 * ax1dy2 * ax1dy2
				+ -48 * ax1cy1 * ax1dy1 * ax1ay2 * bx1cy2 * ax1dy2 * ax1dy2
				+ -12 * ax1by1 * bx1ay2 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -20 * ax1by1 * bx1by2 * bx1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 96 * ax1by1 * ax1ay2 * cx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 24 * ax1by1 * ax1by2 * cx1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 40 * bx1cy1 * ax1ay2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 24 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1cy2
				+ 12 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * ax1by2 * ax1by2
				+ -12 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1cy2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * ax1by2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * ax1cy2 * ax1cy2
				+ 24 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1ay2 * bx1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1by2 * bx1by2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * cx1cy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * cx1by2
				+ -18 * ax1by1 * bx1dy1 * bx1dy1 * ax1by2 * ax1cy2 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * cx1cy2
				+ -6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1by2 * cx1by2
				+ 36 * ax1by1 * bx1dy1 * ax1by2 * ax1cy2 * ax1cy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * cx1cy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1by2 * cx1by2 * bx1dy2
				+ 24 * ax1by1 * ax1cy1 * bx1dy1 * ax1by2 * bx1by2 * bx1dy2
				+ 24 * ax1cy1 * ax1cy1 * ax1ay2 * ax1cy2 * ax1dy2 * cx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * ax1dy2 * cx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1by2 * ax1cy2 * ax1cy2 * cx1dy2
				+ -24 * ax1by1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1cy2 * cx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1by2 * bx1by2 * cx1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1cy2 * cx1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1by2 * cx1by2 * cx1dy2
				+ -18 * ax1by1 * ax1by2 * ax1cy2 * ax1cy2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1ay2 * cx1cy2 * bx1dy2 * bx1dy2
				+ -6 * ax1by1 * ax1by1 * ax1by2 * cx1by2 * bx1dy2 * bx1dy2
				+ -36 * ax1by1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1by2 * ax1by2
				+ 144 * ax1by1 * ax1dy1 * cx1dy1 * ax1by2 * ax1cy2 * ax1cy2
				+ 36 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * cx1cy2
				+ 36 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * ax1by2 * bx1by2
				+ -36 * ax1by1 * ax1cy1 * ax1dy1 * bx1dy1 * bx1ay2 * bx1cy2
				+ -12 * ax1by1 * ax1cy1 * ax1dy1 * bx1dy1 * bx1by2 * bx1by2
				+ -20 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2
				+ 24 * bx1cy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1dy2
				+ 60 * bx1cy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2
				+ -96 * ax1by1 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2 * cx1dy2
				+ -84 * ax1by1 * ax1by1 * ax1dy1 * ax1ay2 * cx1cy2 * cx1dy2
				+ -84 * ax1by1 * ax1by1 * ax1dy1 * ax1by2 * cx1by2 * cx1dy2
				+ 42 * ax1by1 * ax1by1 * ax1cy1 * ax1by2 * cx1by2 * dx1dy2
				+ 36 * ax1by1 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 * cx1dy2
				+ 144 * ax1by1 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2 * cx1dy2
				+ 36 * ax1by1 * ax1by1 * ax1ay2 * cx1cy2 * ax1dy2 * cx1dy2
				+ 36 * ax1by1 * ax1cy1 * ax1by2 * bx1by2 * ax1dy2 * cx1dy2
				+ -36 * ax1by1 * ax1cy1 * bx1ay2 * bx1cy2 * ax1dy2 * bx1dy2
				+ -12 * ax1by1 * ax1cy1 * bx1by2 * bx1by2 * ax1dy2 * bx1dy2
				+ -24 * bx1cy1 * ax1dy1 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ 20 * bx1cy1 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 60 * bx1cy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1ay2 * bx1cy2
				+ -2 * ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1by2 * bx1by2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * bx1cy2 * cx1dy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1by2 * bx1by2 * cx1dy2
				+ -12 * ax1by1 * ax1by1 * cx1dy1 * cx1ay2 * ax1cy2 * ax1dy2
				+ 48 * ax1by1 * ax1by1 * cx1dy1 * bx1by2 * ax1cy2 * bx1cy2
				+ -36 * ax1by1 * ax1by1 * cx1dy1 * ax1by2 * ax1cy2 * cx1cy2
				+ -6 * ax1by1 * ax1by1 * cx1dy1 * cx1by2 * ax1cy2 * ax1cy2
				+ 24 * ax1by1 * ax1cy1 * bx1dy1 * bx1by2 * ax1cy2 * bx1cy2
				+ -24 * ax1by1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * cx1cy2
				+ -6 * ax1by1 * cx1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 6 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * cx1ay2 * cx1cy2
				+ -48 * ax1by1 * ax1by1 * bx1by2 * ax1cy2 * bx1cy2 * cx1dy2
				+ 36 * ax1by1 * ax1by1 * ax1by2 * ax1cy2 * cx1cy2 * cx1dy2
				+ 6 * ax1by1 * ax1by1 * cx1by2 * ax1cy2 * ax1cy2 * cx1dy2
				+ 24 * ax1by1 * ax1cy2 * ax1cy2 * ax1cy2 * cx1cy2 * ax1dy2
				+ 6 * ax1by1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * cx1dy2
				+ 6 * ax1by1 * ax1by1 * cx1ay2 * cx1cy2 * ax1dy2 * ax1dy2
				+ 10 * bx1cy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 10 * ax1by1 * bx1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * bx1cy2
				+ -10 * ax1by1 * ax1cy2 * ax1cy2 * ax1cy2 * bx1cy2 * bx1dy2
				+ -24 * ax1by1 * ax1cy1 * bx1by2 * ax1cy2 * bx1cy2 * bx1dy2
				+ -4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1ay2 * cx1cy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1by2 * bx1cy2 * cx1cy2
				+ -12 * ax1by1 * ax1by1 * bx1cy1 * ax1by2 * cx1cy2 * cx1cy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1ay2 * cx1cy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy2 * ax1cy2 * ax1cy2 * cx1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1by2 * cx1cy2 * cx1cy2
				+ -24 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * dx1by2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * bx1by2 * cx1by2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * dx1by2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1by2 * cx1by2 * bx1dy2
				+ -24 * ax1by1 * ax1by1 * ax1by2 * ax1dy2 * bx1dy2 * bx2dy2
				+ 20 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1cy2 * cx1dy2
				+ 10 * ax1by1 * ax1by1 * ax1by1 * cx1by2 * cx1by2 * cx1dy2
				+ -48 * ax1cy1 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2 * bx1dy2
				+ -20 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1ay2 * cx1cy2
				+ -10 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1by2 * cx1by2
				+ -2 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1by2 * cx1by2
				+ 6 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * bx1cy2 * cx2dy2
				+ 4 * ax1by1 * bx1cy1 * ax1cy2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax1dy2 * bx2dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * bx1cy2 * dx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1by2 * bx2dy2
				+ -6 * ax1by1 * ax1cy1 * ax1cy2 * bx1cy2 * bx1cy2 * bx1cy2
				+ -10 * bx1cy1 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -2 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1by2 * dx1by2
				+ 18 * ax1by1 * ax1by1 * ax1by1 * ax1dy2 * bx2dy2 * bx2dy2
				+ -2 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * bx2dy2 * bx2dy2
				+ 6 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * dx1by2 * dx1by2
				+ -2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1cy2 * bx2cy2
				+ -8 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * cx1ay2 * bx1cy2
				+ 72 * ax1by1 * ax1cy1 * ax1ay2 * ax1dy2 * ax1dy2 * cx2dy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * dx1by2 * ax1dy2
				+ -72 * ax1by1 * cx1dy1 * ax1ay2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -144 * ax1by1 * cx1dy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1by2 * bx2dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * ax1by2 * cx1by2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * cx1by2 * ax1dy2
				+ 8 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * cx1cy2 * bx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * ax2cy2 * bx1dy2
				+ -12 * ax1cy1 * ax1cy1 * bx1dy1 * ax1by2 * ax1by2 * bx1dy2
				+ 24 * ax1by1 * cx1ay2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 12 * bx1cy1 * bx1cy1 * ax1dy1 * ax1by2 * ax1by2 * ax1dy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1cy2 * bx2cy2
				+ -4 * bx1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1cy2 * ax1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy1 * ax2cy2
				+ -12 * ax1cy1 * ax1cy1 * ax1dy1 * bx1by2 * bx1by2 * ax1dy2
				+ 36 * ax1cy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * bx1dy2
				+ 4 * bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1ay2 * ax1cy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * ax1cy2 * cx1cy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy2 * bx2cy2
				+ -8 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * bx1by2
				+ 12 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * cx1ay2 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * cx1by2 * ax1cy2 * cx1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1ay2 * cx2dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * dx1by2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1ay2 * bx1cy2 * bx1dy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1cy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * cx1by2 * ax1cy2 * cx1cy2 * ax1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1ay2 * dx1cy2
				+ -2 * bx1cy1 * bx1cy1 * bx1cy1 * ax1by2 * ax1by2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * ax1dy1 * cx1by2 * cx1by2
				+ -12 * ax1by1 * ax1cy1 * bx1cy1 * cx1dy1 * ax1by2 * bx1by2
				+ -4 * bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1by2 * ax1by2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1by2 * bx1by2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * bx1dy1 * cx1ay2 * bx1cy2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1ay2 * bx1cy2
				+ 18 * ax1by1 * ax1by1 * ax1by2 * cx1cy2 * cx1cy2 * ax1dy2
				+ 12 * bx1cy1 * bx1cy1 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * cx1dy1 * bx1by2 * bx1by2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1cy2 * bx1cy2 * bx2dy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * bx2cy2 * cx2dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * bx1dy2 * bx1dy2
				+ 2 * ax1by1 * ax1cy1 * ax1cy2 * ax1cy2 * bx1cy2 * cx1cy2
				+ ax1cy1 * ax1cy1 * ax1cy2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1by2 * bx2dy2
				+ 12 * bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax2cy2 * ax1dy2
				+ -12 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * ax1by2 * ax1by2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1dy2 * cx2dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax2cy2 * ax1dy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * dx1cy2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1by2 * bx2dy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * dx1ay2 * ax1cy2
				+ 18 * ax1by1 * ax1by1 * ax1dy1 * bx1by2 * bx1by2 * cx1dy2
				+ 36 * ax1by1 * ax1by1 * ax1cy1 * bx1by2 * bx1cy2 * cx2dy2
				+ 18 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1ay2 * ax1cy2
				+ -18 * ax1by1 * ax1by1 * ax1dy1 * ax1by2 * cx1cy2 * cx1cy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * bx1cy2 * cx1cy2 * bx2dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax2cy2
				+ -24 * ax1by1 * ax1cy1 * ax1dy1 * bx1by2 * ax1cy2 * cx1cy2
				+ -12 * ax1cy1 * ax1cy1 * bx1dy1 * bx1by2 * ax1cy2 * ax1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * cx1ay2 * ax1cy2 * cx1dy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1cy2 * ax2dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * cx1ay2 * bx1cy2 * ax1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * dx1cy2
				+ -24 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1ay2 * ax1cy2
				+ 24 * ax1by1 * ax1cy1 * bx1by2 * ax1cy2 * cx1cy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * bx1by2 * ax1cy2 * ax1cy2 * bx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * cx1ay2 * ax1cy2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1dy1 * ax2cy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * cx1ay2 * bx1cy2
				+ -24 * ax1cy1 * ax1cy1 * bx1dy1 * bx1ay2 * ax1cy2 * ax1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1by2 * ax1cy2 * ax1dy2 * cx2dy2
				+ 36 * ax1by1 * cx1by2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 24 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * bx1ay2 * ax1cy2
				+ 12 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1cy2
				+ 12 * ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1cy2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * bx1ay2 * ax1cy2
				+ 12 * ax1by1 * bx1cy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1dy1 * ax2cy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * ax2cy2
				+ -24 * ax1by1 * ax1by1 * bx1ay2 * ax1cy2 * bx1dy2 * cx1dy2
				+ -6 * ax1by1 * ax1by1 * ax1cy1 * ax2cy2 * bx1dy2 * bx1dy2
				+ 12 * ax1by1 * bx1cy1 * ax1ay2 * ax1cy2 * bx1dy2 * bx1dy2
				+ -24 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * bx1dy2 * cx2dy2
				+ 12 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * ax1ay2 * cx2dy2
				+ -4 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1ay2 * dx1cy2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2dy2 * cx2dy2
				+ 12 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * bx1cy2 * bx1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * ax1cy2 * dx1cy2
				+ 36 * ax1by1 * ax1dy1 * ax1dy1 * cx1by2 * ax1cy2 * ax1cy2
				+ -18 * ax1by1 * ax1by1 * ax1by2 * bx1by2 * bx1dy2 * cx1dy2
				+ 6 * ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1dy2 * bx2dy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * ax1dy2 * bx2dy2
				+ -18 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * ax1by2 * bx1by2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * bx1by2 * dx1by2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * dx1by2 * bx1cy2 * bx1cy2
				+ 16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1cy2 * bx2cy2
				+ -36 * ax1cy1 * bx1dy1 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ 36 * ax1by1 * ax1by1 * ax1cy1 * bx1by2 * bx1cy2 * dx1cy2
				+ 144 * ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1cy2 * ax1dy2
				+ -12 * ax1by1 * ax1cy1 * bx1cy1 * bx1by2 * ax1cy2 * cx1cy2
				+ -6 * ax1by1 * ax1by1 * ax1cy1 * bx1by2 * cx1cy2 * cx1cy2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * ax1by2 * dx1cy2 * dx1cy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1cy2 * dx1cy2
				+ 12 * ax1by1 * ax1by1 * ax1dy1 * bx1by2 * ax1cy2 * dx1cy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * bx1cy2 * bx2cy2
				+ -18 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * ax1by2 * cx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * bx2dy2
				+ -18 * ax1cy1 * ax1cy1 * ax1dy1 * cx1by2 * ax1cy2 * ax1cy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax1cy2 * dx1cy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * dx1by2 * ax1cy2 * ax1cy2
				+ 18 * ax1cy1 * ax1cy1 * ax1by2 * ax1cy2 * cx1cy2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy2 * bx2cy2 * ax1dy2
				+ 12 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1ay2 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * dx1ay2 * bx1cy2
				+ 4 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1cy2
				+ -12 * ax1by1 * bx1cy1 * ax1ay2 * ax1cy2 * ax1dy2 * cx1dy2
				+ 18 * ax1by1 * ax1by1 * ax1ay2 * ax1cy2 * cx1dy2 * cx1dy2
				+ 12 * ax1cy1 * ax1cy1 * bx1ay2 * bx1cy2 * ax1dy2 * ax1dy2
				+ 36 * ax1by1 * ax1cy1 * bx1ay2 * ax1cy2 * ax1dy2 * cx1dy2
				+ -36 * ax1by1 * ax1ay2 * ax1cy2 * ax1dy2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax2cy2 * ax1dy2 * ax1dy2 * bx1dy2
				+ 8 * ax1by1 * bx1ay2 * bx1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -12 * ax1by1 * bx1by2 * ax1cy2 * bx1cy2 * ax1dy2 * ax1dy2
				+ 24 * ax1by1 * ax1by1 * ax1cy2 * ax1dy2 * bx1dy2 * ax2dy2
				+ -6 * ax1by1 * ax1by2 * bx1cy2 * bx1cy2 * ax1dy2 * ax1dy2
				+ -24 * ax1by1 * ax1by2 * ax1cy2 * bx1cy2 * ax1dy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * bx1dy2 * bx2dy2
				+ 12 * ax1cy1 * ax1cy1 * bx1cy1 * ax1by2 * bx1by2 * bx1dy2
				+ 6 * ax1by1 * ax1cy1 * ax1cy1 * bx1by2 * bx1by2 * cx1dy2
				+ -10 * ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * bx1by2 * bx1dy2
				+ -42 * ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * ax1cy2 * bx1cy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * ax1by2 * dx1cy2 * cx2dy2
				+ 36 * ax1by1 * ax1by1 * ax1dy1 * bx1cy2 * bx2cy2 * ax1dy2
				+ 24 * ax1by1 * bx1dy1 * ax1by2 * ax1by2 * ax1dy2 * bx1dy2
				+ 60 * ax1by1 * ax1dy1 * bx1by2 * ax1cy2 * bx1cy2 * ax1dy2
				+ 24 * ax1by1 * ax1dy1 * bx1by2 * ax1cy2 * ax1cy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1by2 * ax1cy2 * ax1cy2 * dx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * ax1by2 * ax1dy2 * bx2dy2
				+ 18 * ax1by1 * ax1dy1 * ax1by2 * ax1by2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * bx2dy2 * bx2dy2
				+ -24 * ax1by1 * ax1dy1 * bx1dy1 * ax1by2 * ax1by2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * ax1by2 * dx1by2 * bx1dy2
				+ -18 * ax1by1 * bx1dy1 * bx1dy1 * ax1by2 * ax1by2 * ax1dy2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * dx1by2 * dx1by2 * ax1dy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * ax2dy2 * cx2dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * ax1cy2 * ax1dy2 * ax2dy2
				+ 12 * ax1by1 * bx1dy1 * ax1ay2 * bx1cy2 * ax1dy2 * ax1dy2
				+ -36 * ax1by1 * ax1by1 * ax1ay2 * ax1cy2 * bx1dy2 * dx1dy2
				+ 72 * ax1by1 * ax1dy1 * ax1ay2 * ax1cy2 * bx1dy2 * bx1dy2
				+ -48 * ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * ax1cy2 * bx1dy2
				+ -24 * ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * ax1cy2 * bx1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax2cy2 * ax1dy2
				+ -12 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1cy2 * ax1dy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * ax2cy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1cy2 * dx1dy2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * dx1ay2 * ax1cy2 * ax1dy2
				+ 72 * ax1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1cy2 * bx1dy2
				+ 144 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * ax1cy2 * cx2dy2
				+ -18 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1cy2 * bx2cy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * dx1ay2 * cx2dy2
				+ -72 * ax1by1 * ax1cy1 * ax1by2 * ax1cy2 * ax1cy2 * dx1dy2
				+ -72 * ax1cy1 * ax1dy1 * bx1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -144 * ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * ax1dy2 * cx2dy2
				+ -72 * ax1by1 * ax1dy1 * ax1ay2 * cx1cy2 * ax1dy2 * ax1dy2
				+ -72 * ax1cy1 * ax1dy1 * bx1ay2 * ax1cy2 * ax1dy2 * ax1dy2 + 4
				* ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1cy2 * bx2cy2 + 4
				* ax1by1 * ax1by1 * ax1cy1 * bx1cy1 * cx1cy2 * bx2cy2 + 48
				* ax1by1 * ax1cy1 * bx1dy1 * bx1ay2 * ax1cy2 * bx1dy2 + 16
				* ax1by1 * ax1by1 * ax1by1 * bx1ay2 * dx1cy2 * cx1dy2 + 48
				* ax1by1 * ax1by1 * ax1dy1 * bx1ay2 * cx1cy2 * bx1dy2 + -32
				* ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1cy2 * dx1dy2 + -4
				* ax1by1 * ax1by1 * ax1by1 * cx1ay2 * bx1cy2 * dx1dy2 + 12
				* ax1by1 * ax1by1 * ax1dy1 * cx1ay2 * bx1cy2 * bx1dy2 + 4
				* ax1by1 * ax1by1 * ax1by1 * cx1ay2 * dx1cy2 * bx1dy2 + 8
				* ax1by1 * ax1by1 * ax1by1 * cx1ay2 * dx1cy2 * bx1dy2 + 12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1cy2 * ax2dy2 + -36
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * ax1cy2 * cx1dy2 + 12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * cx2dy2 + -16
				* ax1by1 * ax1by1 * ax1by1 * cx1ay2 * dx1cy2 * bx1dy2 + 72
				* ax1by1 * ax1by1 * ax1cy1 * ax1dy1 * ax2cy2 * cx1dy2 + -72
				* ax1by1 * ax1by1 * ax1dy1 * cx1ay2 * cx1cy2 * ax1dy2 + 24
				* bx1cy1 * bx1cy1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1dy2 + -24
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * bx1cy2 * bx1dy2 + 72
				* ax1by1 * ax1by1 * ax1cy1 * dx1ay2 * ax1cy2 * cx1dy2 + -36
				* ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1ay2 * cx2dy2;
		coeffs[5] = 30 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2
				+ -120 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1dy2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1cy2
				+ 180 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2
				+ 180 * ax1dy1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 180 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2
				+ 60 * ax1dy1 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -120 * ax1dy1 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -180 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -180 * ax1dy1 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -120 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -6 * ax1dy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 30 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 60 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 60 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 60 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 6 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1ay2 * bx1by2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1by2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1cy2 * bx1cy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1by2 * bx1by2 * bx1cy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1ay2 * cx1by2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * bx1by2 * bx1dy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * bx1cy2 * bx1cy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1by2 * bx1by2 * bx1cy2 * bx1dy2
				+ 4 * ax1by1 * ax1by1 * bx1by2 * bx1cy2 * bx1cy2 * bx1cy2
				+ 12 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1by2
				+ -96 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * cx1by2
				+ -40 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2
				+ -36 * ax1by1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1by2 * ax1dy2
				+ 96 * ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * cx1by2 * ax1dy2
				+ 48 * ax1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * bx1by2 * ax1dy2
				+ 36 * ax1by1 * ax1dy1 * bx1ay2 * bx1by2 * ax1dy2 * ax1dy2
				+ -96 * ax1by1 * ax1dy1 * ax1ay2 * cx1by2 * ax1dy2 * ax1dy2
				+ -48 * ax1cy1 * ax1dy1 * ax1ay2 * bx1by2 * ax1dy2 * ax1dy2
				+ -12 * ax1by1 * bx1ay2 * bx1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 96 * ax1by1 * ax1ay2 * cx1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 40 * bx1cy1 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 24 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1by2
				+ -12 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1by2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1cy2 * ax1cy2
				+ -6 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * ax1by2 * ax1cy2
				+ 24 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1ay2 * bx1by2
				+ -24 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * cx1by2
				+ -18 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1cy2 * ax1cy2
				+ -18 * ax1by1 * bx1dy1 * bx1dy1 * ax1by2 * ax1by2 * ax1cy2
				+ -18 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * cx1by2
				+ -18 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * cx1ay2 * ax1by2
				+ 36 * ax1by1 * bx1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * bx1dy2
				+ 36 * ax1by1 * bx1dy1 * ax1by2 * ax1by2 * ax1cy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * cx1by2 * bx1dy2
				+ 24 * ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * ax1dy2 * cx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * cx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * ax1cy2 * cx1dy2
				+ -24 * ax1by1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1by2 * cx1dy2
				+ 24 * ax1by1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1by2 * cx1dy2
				+ -18 * ax1by1 * ax1ay2 * ax1cy2 * ax1cy2 * bx1dy2 * bx1dy2
				+ -18 * ax1by1 * ax1by2 * ax1by2 * ax1cy2 * bx1dy2 * bx1dy2
				+ -18 * ax1by1 * ax1by1 * ax1ay2 * cx1by2 * bx1dy2 * bx1dy2
				+ -18 * ax1by1 * ax1by1 * cx1ay2 * ax1by2 * bx1dy2 * bx1dy2
				+ 144 * ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1cy2 * ax1cy2
				+ 144 * ax1by1 * ax1dy1 * cx1dy1 * ax1by2 * ax1by2 * ax1cy2
				+ -36 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * bx1ay2 * bx1by2
				+ 84 * ax1by1 * ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * cx1by2
				+ -42 * ax1by1 * ax1by1 * ax1cy1 * ax1dy1 * cx1dy1 * ax2by2
				+ 60 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2
				+ 60 * bx1cy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1cy2
				+ -96 * ax1by1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * cx1dy2
				+ -96 * ax1by1 * ax1dy1 * ax1by2 * ax1by2 * ax1cy2 * cx1dy2
				+ -84 * ax1by1 * ax1by1 * ax1dy1 * ax1ay2 * cx1by2 * cx1dy2
				+ 12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2by2 * dx1dy2
				+ 144 * ax1by1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2 * cx1dy2
				+ 144 * ax1by1 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2 * cx1dy2
				+ -36 * ax1by1 * ax1by1 * bx1ay2 * bx1by2 * ax1dy2 * cx1dy2
				+ 84 * ax1by1 * ax1by1 * ax1ay2 * cx1by2 * ax1dy2 * cx1dy2
				+ -42 * ax1by1 * ax1by1 * ax1cy1 * ax2by2 * ax1dy2 * cx1dy2
				+ 60 * bx1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 60 * bx1cy1 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1ay2 * bx1by2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * bx1by2 * cx1dy2
				+ -36 * ax1by1 * ax1by1 * cx1dy1 * cx1ay2 * ax1by2 * ax1dy2
				+ -72 * ax1by1 * cx1dy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 48 * ax1by1 * ax1by1 * cx1dy1 * bx1ay2 * ax1cy2 * bx1cy2
				+ 6 * ax1by1 * ax1by1 * cx1dy1 * bx1by2 * bx1by2 * ax1cy2
				+ -36 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * ax1cy2 * cx1cy2
				+ -6 * ax1by1 * ax1by1 * cx1dy1 * cx1ay2 * ax1cy2 * ax1cy2
				+ -36 * ax1by1 * ax1by1 * cx1dy1 * ax1by2 * cx1by2 * ax1cy2
				+ 24 * ax1by1 * ax1cy1 * bx1dy1 * bx1ay2 * ax1cy2 * bx1cy2
				+ 96 * ax1by1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * cx1dy2
				+ -48 * ax1by1 * ax1by1 * bx1ay2 * ax1cy2 * bx1cy2 * cx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1by2 * bx1by2 * ax1cy2 * cx1dy2
				+ 36 * ax1by1 * ax1by1 * ax1ay2 * ax1cy2 * cx1cy2 * cx1dy2
				+ 6 * ax1by1 * ax1by1 * cx1ay2 * ax1cy2 * ax1cy2 * cx1dy2
				+ 36 * ax1by1 * ax1by1 * ax1by2 * cx1by2 * ax1cy2 * cx1dy2
				+ 16 * bx1cy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 36 * ax1by1 * bx1dy1 * ax1by2 * ax1cy2 * ax1cy2 * bx1cy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1by2 * cx1by2 * bx1cy2
				+ -36 * ax1by1 * ax1by2 * ax1cy2 * ax1cy2 * bx1cy2 * bx1dy2
				+ -24 * ax1by1 * ax1cy1 * bx1ay2 * ax1cy2 * bx1cy2 * bx1dy2
				+ -36 * ax1by1 * ax1cy1 * ax1by2 * bx1by2 * bx1cy2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * bx1cy2 * cx1cy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1by2 * bx1by2 * cx1cy2
				+ -12 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * cx1cy2 * cx1cy2
				+ -12 * ax1by1 * ax1by1 * bx1cy1 * ax1by2 * cx1by2 * cx1cy2
				+ 6 * ax1cy1 * ax1cy1 * ax1by2 * ax1cy2 * ax1cy2 * cx1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1cy2 * cx1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1by2 * cx1by2 * cx1cy2
				+ -48 * ax1cy1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * bx1dy2
				+ 2 * ax1cy1 * ax1cy1 * cx1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -4 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1ay2 * cx1by2
				+ -2 * ax1by1 * ax1cy2 * ax1cy2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -6 * ax1by1 * ax1by1 * ax1by2 * bx1cy2 * bx1cy2 * cx1cy2
				+ -10 * ax1by1 * ax1by1 * cx1by2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -6 * ax1by1 * ax1by1 * cx1dy1 * ax1by2 * ax1by2 * cx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * cx1cy2 * cx1dy2
				+ 6 * ax1by1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * cx1cy2
				+ 24 * ax1by1 * ax1by1 * ax1by2 * ax1cy2 * cx1cy2 * cx1cy2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1by2 * cx1by2 * bx1cy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1ay2 * cx1by2
				+ -48 * ax1cy1 * ax1dy1 * ax1by2 * ax1by2 * ax1cy2 * bx1dy2
				+ -20 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1ay2 * cx1by2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2dy2 * bx2dy2
				+ 12 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * bx1cy2 * bx2dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2dy2 * bx2dy2
				+ -2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1cy2 * ax2cy2
				+ -16 * bx1cy1 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -6 * ax1by1 * ax1by1 * ax1cy1 * ax1cy2 * cx1cy2 * bx2cy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1ay2 * dx1by2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1ay2 * dx1by2
				+ 20 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1by2 * cx1dy2
				+ 12 * ax1by1 * ax1by1 * dx1by2 * ax1cy2 * ax1cy2 * bx1cy2
				+ 2 * bx1cy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -12 * ax1by1 * bx1cy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1by2
				+ -12 * ax1by1 * bx1cy1 * bx1ay2 * bx1by2 * ax1dy2 * ax1dy2
				+ -144 * ax1by1 * cx1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -12 * ax1by1 * bx1cy1 * ax1ay2 * ax1by2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * bx1cy1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1by2
				+ 8 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * cx1by2 * bx1dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * ax2by2 * bx1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1by2 * bx2cy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * cx1by2 * ax1cy2
				+ 4 * bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1ay2 * ax1by2
				+ -4 * bx1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1by2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1by2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * dx1ay2 * ax1by2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax2by2 * ax1dy2
				+ -144 * ax1by1 * ax1dy1 * ax1by2 * cx1by2 * ax1cy2 * ax1dy2
				+ -144 * ax1by1 * ax1dy1 * ax1ay2 * ax1by2 * ax1dy2 * cx1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1by2 * ax1by2 * ax1cy2 * dx1dy2
				+ 144 * ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * ax1cy2 * cx2dy2
				+ 144 * ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1by2 * ax1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * ax1cy2 * dx1cy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * dx1by2 * ax1cy2
				+ 72 * ax1by1 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2 * cx1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1by2 * ax1by2 * ax1dy2 * cx2dy2
				+ -72 * ax1by1 * ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * dx1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax2by2 * ax1dy2
				+ -72 * ax1cy1 * ax1dy1 * bx1ay2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1ay2 * dx1by2 * ax1dy2 * ax1dy2
				+ -72 * ax1by1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1by2
				+ -72 * ax1by1 * ax1cy1 * ax1by2 * ax1cy2 * ax1cy2 * dx1cy2
				+ -72 * ax1by1 * ax1cy1 * ax1by2 * ax1cy2 * ax1cy2 * cx2dy2
				+ -72 * ax1cy1 * ax1dy1 * ax1by2 * ax1by2 * bx1cy2 * ax1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1ay2 * ax1cy2 * ax1dy2 * cx2dy2
				+ -72 * ax1by1 * ax1cy1 * ax2by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -72 * ax1cy1 * ax1dy1 * bx1ay2 * ax1by2 * ax1dy2 * ax1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1by2 * bx2cy2 * ax1dy2 * ax1dy2
				+ 72 * ax1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * bx1dy2
				+ 36 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1cy2 * dx1cy2
				+ 36 * ax1by1 * ax1by1 * cx1ay2 * ax1cy2 * cx1cy2 * ax1dy2
				+ 36 * ax1by1 * ax1by2 * ax1by2 * cx1cy2 * ax1dy2 * ax1dy2
				+ -18 * ax1cy1 * ax1cy1 * ax1dy1 * cx1ay2 * ax1cy2 * ax1cy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * dx1ay2 * ax1cy2 * bx1cy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * bx1by2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1by2 * bx1dy2
				+ -2 * ax1by1 * ax1by1 * ax1by1 * cx1by2 * cx1by2 * cx1cy2
				+ -6 * ax1by1 * ax1by1 * bx1cy1 * cx1by2 * cx1by2 * ax1cy2
				+ 4 * bx1cy1 * bx1cy1 * bx1cy1 * ax1by2 * ax1by2 * ax1cy2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * bx1by2 * bx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1ay2 * cx1cy2 * cx1cy2 * ax1dy2
				+ -8 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * cx1ay2 * bx1by2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1dy1 * ax2by2
				+ 72 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * ax1dy2 * bx1dy2
				+ -24 * ax1by1 * ax1by1 * ax1dy1 * cx1by2 * cx1by2 * ax1cy2
				+ 12 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1by2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * bx1cy2 * cx1cy2
				+ -12 * bx1cy1 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2
				+ 10 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * ax1cy2 * cx1cy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy2 * ax2cy2
				+ 24 * ax1cy1 * bx1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * bx1ay2 * bx1by2 * ax1dy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1ay2 * bx1cy2 * bx1cy2 * ax1dy2
				+ 36 * ax1by1 * cx1ay2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -84 * ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * ax1by2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * dx1dy2
				+ -12 * bx1cy1 * bx1cy1 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * bx1dy2 * bx1dy2
				+ 12 * ax1cy1 * ax1cy1 * bx1ay2 * ax1cy2 * ax1cy2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1cy2 * bx1cy2 * ax2dy2
				+ -12 * bx1cy1 * bx1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 12 * ax1by1 * ax1by1 * ax1by2 * cx1by2 * cx1cy2 * ax1dy2
				+ 4 * ax1by1 * bx1dy1 * bx1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 12 * ax1by1 * ax1by1 * ax1by2 * ax1cy2 * bx1cy2 * dx1cy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1cy2 * ax2cy2
				+ 36 * ax1by1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * cx1cy2
				+ 36 * ax1by1 * ax1dy1 * ax1dy1 * cx1ay2 * ax1cy2 * ax1cy2
				+ -48 * ax1by1 * cx1ay2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -30 * ax1by1 * ax1by2 * bx1by2 * bx1cy2 * ax1dy2 * ax1dy2
				+ 12 * ax1by1 * ax1dy1 * ax1ay2 * ax1by2 * bx1dy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * ax1by2 * ax1cy2 * bx1cy2 * cx2dy2
				+ -4 * ax1by1 * bx1by2 * ax1cy2 * ax1cy2 * ax1cy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1cy1 * bx1dy1 * ax1ay2 * bx2dy2
				+ -132 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * bx1by2 * bx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * dx1by2
				+ 48 * ax1by1 * ax1cy1 * ax1dy1 * bx1ay2 * bx1by2 * bx1dy2
				+ 18 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * cx1cy2 * ax1dy2
				+ 204 * ax1by1 * ax1by1 * cx1dy1 * bx1ay2 * ax1by2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1cy1 * dx1ay2 * ax1by2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * ax1by2 * cx1by2 * cx1cy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * bx2cy2 * ax1dy2
				+ -18 * ax1by1 * ax1by1 * ax1by1 * dx1ay2 * bx1cy2 * cx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1dy1 * cx1by2 * cx1by2 * ax1cy2
				+ 18 * ax1by1 * ax1by1 * cx1by2 * cx1by2 * ax1cy2 * ax1dy2
				+ -12 * ax1by1 * ax1by2 * ax1by2 * bx1cy2 * ax1dy2 * bx1dy2
				+ 16 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * ax1by2 * cx1dy2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx2dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1ay2 * dx1by2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1dy1 * ax2by2
				+ -8 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * ax2dy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1cy2 * ax2cy2
				+ 16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1ay2 * bx2dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * bx1dy1 * cx1ay2 * bx1by2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1ay2 * bx1by2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * ax1by2 * bx2dy2 * cx2dy2
				+ 24 * ax1by1 * ax1cy1 * ax1by2 * cx1by2 * bx1cy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * bx1cy2 * bx1dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1dy2 * bx2dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax2by2 * ax1dy2 * ax1dy2
				+ -16 * ax1cy1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1ay2 * ax1by2
				+ 36 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1cy2 * cx2dy2
				+ 12 * ax1by1 * ax1dy1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1by2
				+ 6 * ax1cy1 * bx1cy1 * bx1cy1 * bx1ay2 * ax1cy2 * ax1cy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1cy2 * bx1cy2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1cy2 * cx1cy2 * bx2cy2
				+ -8 * ax1by1 * ax1cy1 * ax1by2 * bx1cy2 * bx1cy2 * bx1cy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1ay2 * cx1by2 * bx1dy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1by2 * bx1dy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * bx1cy2 * cx1cy2 * ax2dy2
				+ 12 * ax1by1 * ax1cy1 * bx1cy1 * ax1ay2 * bx1cy2 * cx1cy2
				+ 72 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * cx1by2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * ax2by2 * ax1dy2 * ax1dy2
				+ -24 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * cx1ay2 * ax1by2
				+ 24 * ax1by1 * ax1by1 * dx1ay2 * ax1by2 * ax1dy2 * bx1dy2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1by2 * bx2cy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * dx1by2 * ax1dy2 * ax2dy2
				+ 18 * ax1by1 * ax1by1 * ax1dy1 * bx1by2 * bx1by2 * cx1cy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1by2 * bx2cy2
				+ 24 * ax1by1 * ax1cy1 * bx1dy1 * ax1by2 * bx1by2 * bx1cy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2cy2 * cx2dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * bx1by2 * ax2dy2
				+ -24 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * cx1by2 * bx1cy2
				+ -12 * ax1by1 * ax1ay2 * ax1by2 * ax1dy2 * bx1dy2 * bx1dy2
				+ 4 * ax1by1 * ax1by1 * ax2by2 * ax1dy2 * ax1dy2 * bx1dy2
				+ -36 * ax1by1 * ax1by1 * ax1dy1 * cx1ay2 * ax1cy2 * cx1cy2
				+ 18 * ax1cy1 * ax1cy1 * cx1ay2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -6 * ax1by1 * ax1by1 * ax1dy1 * ax1ay2 * cx1cy2 * cx1cy2
				+ 4 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * ax2by2
				+ -24 * ax1cy1 * ax1dy1 * bx1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -12 * ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * bx1cy2 * bx1cy2
				+ -12 * ax1cy1 * ax1cy1 * bx1dy1 * bx1ay2 * ax1cy2 * ax1cy2
				+ 12 * bx1cy1 * bx1cy1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy2 * ax2cy2 * ax1dy2
				+ -18 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * ax1by2 * cx1cy2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * bx2cy2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * dx1by2
				+ -8 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * dx1ay2 * bx1by2
				+ -12 * ax1cy1 * ax1cy1 * bx1dy1 * ax1by2 * ax1by2 * bx1cy2
				+ -24 * ax1by1 * cx1dy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -24 * ax1by1 * ax1by1 * bx1by2 * cx1by2 * bx1cy2 * ax1dy2
				+ -4 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * bx2cy2 * bx2dy2
				+ 12 * ax1by1 * bx1cy1 * ax1by2 * ax1by2 * bx1cy2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * ax1by2 * bx1by2 * cx2dy2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1by2 * cx2dy2
				+ 12 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * ax2by2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * bx2dy2
				+ 18 * ax1by1 * ax1by1 * ax1by2 * bx2cy2 * ax1dy2 * bx1dy2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * bx2cy2 * ax1dy2 * bx2dy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * dx1by2 * ax2dy2
				+ -24 * ax1cy1 * ax1cy1 * bx1dy1 * bx1ay2 * ax1by2 * ax1dy2
				+ -36 * ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1by2 * dx1dy2
				+ 72 * ax1by1 * ax1dy1 * bx1ay2 * ax1cy2 * bx1cy2 * ax1dy2
				+ -124 * ax1by1 * ax1by1 * ax1by1 * dx1ay2 * cx1by2 * bx1dy2
				+ 20 * ax1by1 * ax1by1 * ax1by1 * bx1dy1 * ax2by2 * cx1dy2
				+ 148 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * dx1by2 * bx1dy2
				+ -36 * ax1by1 * bx1ay2 * ax1cy2 * bx1cy2 * ax1dy2 * ax1dy2
				+ -6 * ax1by1 * ax1ay2 * bx1cy2 * bx1cy2 * ax1dy2 * ax1dy2
				+ -12 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * bx1dy2 * ax2dy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * ax1cy2 * ax2dy2 * cx2dy2
				+ 24 * ax1by1 * ax1by1 * ax1cy1 * ax1ay2 * dx1by2 * cx1dy2
				+ -24 * ax1by1 * ax1by1 * ax1dy1 * cx1ay2 * cx1by2 * ax1dy2
				+ 12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2by2 * dx1dy2
				+ 12 * ax1by1 * ax1dy1 * ax1ay2 * bx1cy2 * bx1cy2 * ax1dy2
				+ -24 * ax1by1 * ax1by1 * dx1ay2 * ax1cy2 * bx1cy2 * ax1dy2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * ax1cy2 * ax1cy2 * dx1dy2 + 24
				* ax1by1 * ax1by1 * ax1by1 * ax1cy2 * dx1cy2 * ax2dy2 + 12
				* ax1by1 * ax1by1 * ax1by1 * dx1by2 * dx1by2 * ax1cy2 + 12
				* ax1by1 * ax1by1 * ax1dy1 * ax1by2 * dx1by2 * bx1cy2 + -42
				* ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * bx1by2 * ax1cy2 + 24
				* ax1by1 * ax1by1 * ax1by1 * dx1by2 * ax1cy2 * bx2dy2 + -24
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * bx2cy2 + -36
				* ax1by1 * ax1by1 * ax1dy1 * bx1by2 * bx2cy2 * ax1dy2 + 60
				* ax1by1 * ax1dy1 * ax1by2 * bx1by2 * bx1cy2 * ax1dy2 + 24
				* ax1by1 * bx1dy1 * ax1by2 * ax1by2 * bx1cy2 * ax1dy2 + 12
				* ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx1cy2 * dx1dy2 + 12
				* ax1by1 * ax1by1 * ax1by1 * ax1ay2 * dx1cy2 * dx1cy2 + -18
				* ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * bx1cy2 * bx1cy2 + 12
				* ax1by1 * ax1by1 * ax1dy1 * bx1ay2 * ax1cy2 * dx1cy2 + -24
				* ax1by1 * ax1dy1 * ax1dy1 * bx1ay2 * ax1cy2 * bx1cy2 + 4
				* bx1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1cy2 * ax1cy2 + -4
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1cy2 * bx1cy2 + 4
				* ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1cy2 * cx1cy2 + -8
				* ax1by1 * ax1by1 * ax2by2 * ax1dy2 * ax1dy2 * bx1dy2 + -36
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * ax1by2 * cx1dy2 + 12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * bx2dy2 + 12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * ax2dy2 + -16
				* ax1by1 * bx1ay2 * ax1by2 * ax1dy2 * ax1dy2 * bx1dy2 + -8
				* ax1by1 * ax1by1 * bx1by2 * ax1dy2 * ax1dy2 * ax2dy2 + 8
				* ax1by1 * ax1cy1 * bx1by2 * ax1cy2 * ax1cy2 * cx1cy2 + 4
				* ax1cy1 * ax1cy1 * bx1by2 * ax1cy2 * ax1cy2 * bx1cy2 + 16
				* ax1by1 * ax1dy1 * ax1dy1 * bx1dy1 * bx1ay2 * ax1by2 + -8
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * dx1ay2 * bx1by2 + 12
				* ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1ay2 * bx2dy2 + 24
				* ax1by1 * ax1by1 * ax1cy1 * dx1ay2 * cx1by2 * ax1dy2 + -48
				* ax1by1 * ax1cy1 * ax1dy1 * bx1ay2 * ax1by2 * cx1dy2 + 24
				* ax1by1 * ax1cy1 * ax1cy1 * ax1dy1 * bx1ay2 * bx2dy2;
		coeffs[6] = 15 * ax1dy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1dy2
				+ -120 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2
				+ -20 * ax1dy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2
				+ 90 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2
				+ 360 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2
				+ 60 * ax1dy1 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 60 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1dy2
				+ 90 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ -60 * ax1dy1 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ -360 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -120 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -60 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ -180 * ax1dy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -30 * ax1dy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 15 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 120 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 60 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 20 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 90 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 30 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 6 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * bx1ay2 * bx1ay2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1ay2 * bx1dy2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1by2 * bx1cy2
				+ -4 * ax1by1 * ax1by1 * bx1dy1 * bx1by2 * bx1by2 * bx1by2
				+ ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * cx1ay2 * cx1ay2
				+ 6 * ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1dy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * bx1ay2 * bx1by2 * bx1cy2 * bx1dy2
				+ 4 * ax1by1 * ax1by1 * bx1ay2 * bx1cy2 * bx1cy2 * bx1cy2
				+ 4 * ax1by1 * ax1by1 * bx1by2 * bx1by2 * bx1by2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1by2 * bx1by2 * bx1cy2 * bx1cy2
				+ 3 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1ay2 * ax1ay2
				+ 6 * ax1cy1 * ax1cy1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1ay2
				+ 21 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * cx1ay2 * cx1ay2
				+ 21 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1dy2 * cx1dy2
				+ -18 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2dy2 * ax2dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * bx1dy2 * bx1dy2
				+ 21 * ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * ax1dy2 * ax1dy2
				+ -6 * bx1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2
				+ 6 * ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * ax1dy2 * ax1dy2
				+ 21 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * cx1cy2 * cx1cy2
				+ 3 * ax1by1 * ax1by1 * cx1by2 * cx1by2 * ax1cy2 * ax1cy2
				+ 6 * ax1cy1 * ax1cy1 * bx1by2 * bx1by2 * ax1cy2 * ax1cy2
				+ ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * cx1ay2 * cx1ay2
				+ 20 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1ay2
				+ -24 * ax1by1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * cx1ay2
				+ -20 * bx1cy1 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2
				+ -18 * ax1by1 * ax1dy1 * ax1dy1 * bx1ay2 * bx1ay2 * ax1dy2
				+ 144 * ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * cx1ay2 * ax1dy2
				+ 60 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1dy2
				+ 18 * ax1by1 * ax1dy1 * bx1ay2 * bx1ay2 * ax1dy2 * ax1dy2
				+ -144 * ax1by1 * ax1dy1 * ax1ay2 * cx1ay2 * ax1dy2 * ax1dy2
				+ -60 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2
				+ -20 * ax1by1 * bx1ay2 * bx1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 24 * ax1by1 * ax1ay2 * cx1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 20 * bx1cy1 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1ay2
				+ -6 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1dy2
				+ -12 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1by2 * ax1cy2
				+ -2 * ax1cy1 * ax1cy1 * cx1dy1 * ax1by2 * ax1by2 * ax1by2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * bx1ay2 * bx1ay2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * cx1ay2
				+ -36 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1by2 * ax1cy2
				+ -20 * ax1by1 * bx1dy1 * bx1dy1 * ax1by2 * ax1by2 * ax1by2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * cx1ay2
				+ 72 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * ax1cy2 * bx1dy2
				+ 12 * ax1by1 * bx1dy1 * ax1by2 * ax1by2 * ax1by2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * cx1ay2 * bx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1dy2 * cx1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * ax1cy2 * cx1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * ax1by2 * cx1dy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * cx1dy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1ay2 * cx1dy2
				+ -36 * ax1by1 * ax1ay2 * ax1by2 * ax1cy2 * bx1dy2 * bx1dy2
				+ -20 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * bx1dy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * bx1dy2 * bx1dy2
				+ -36 * ax1by1 * ax1dy1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1ay2
				+ 288 * ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1by2 * ax1cy2
				+ 48 * ax1by1 * ax1dy1 * cx1dy1 * ax1by2 * ax1by2 * ax1by2
				+ 12 * ax1by1 * ax1cy1 * ax1dy1 * bx1dy1 * ax1ay2 * cx1ay2
				+ 120 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2
				+ 20 * bx1cy1 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2
				+ -432 * ax1by1 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2 * cx1dy2
				+ -56 * ax1by1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * cx1dy2
				+ -84 * ax1by1 * ax1by1 * ax1dy1 * ax1ay2 * cx1ay2 * cx1dy2
				+ 42 * ax1by1 * ax1by1 * ax1cy1 * ax1ay2 * cx1ay2 * dx1dy2
				+ 36 * ax1by1 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2 * cx1dy2
				+ 288 * ax1by1 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2 * cx1dy2
				+ 48 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 * cx1dy2
				+ 120 * bx1cy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 20 * bx1cy1 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ -6 * ax1by1 * ax1by1 * bx1cy1 * cx1dy1 * bx1ay2 * bx1ay2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * bx1ay2 * cx1dy2
				+ -32 * ax1by1 * cx1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -48 * ax1by1 * cx1dy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ 24 * ax1by1 * ax1by1 * cx1dy1 * bx1ay2 * bx1by2 * ax1cy2
				+ -84 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * cx1by2 * ax1cy2
				+ -16 * ax1cy1 * bx1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -24 * ax1cy1 * bx1dy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * cx1ay2 * ax1by2 * cx1cy2
				+ 32 * ax1by1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * cx1dy2
				+ 108 * ax1by1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * cx1dy2
				+ -24 * ax1by1 * ax1by1 * bx1ay2 * bx1by2 * ax1cy2 * cx1dy2
				+ 84 * ax1by1 * ax1by1 * ax1ay2 * cx1by2 * ax1cy2 * cx1dy2
				+ 12 * ax1by1 * ax1by1 * cx1ay2 * ax1by2 * cx1cy2 * ax1dy2
				+ -96 * bx1cy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2
				+ 24 * bx1cy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 36 * ax1by1 * bx1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * bx1cy2
				+ 36 * ax1by1 * bx1dy1 * ax1by2 * ax1by2 * ax1cy2 * bx1cy2
				+ -36 * ax1by1 * ax1ay2 * ax1cy2 * ax1cy2 * bx1cy2 * bx1dy2
				+ -18 * ax1by1 * ax1by2 * ax1by2 * ax1cy2 * bx1cy2 * bx1dy2
				+ 4 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * bx1by2 * cx1cy2
				+ -24 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * cx1by2 * cx1cy2
				+ 6 * ax1cy1 * ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * cx1cy2
				+ 12 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * ax1cy2 * cx1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1by2 * cx1cy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1by2 * bx1by2 * cx1by2
				+ -6 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * ax1by2 * cx1by2
				+ 6 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * cx1by2 * ax1dy2
				+ -6 * ax1by1 * ax1cy1 * ax1cy1 * bx1by2 * bx1by2 * cx1by2
				+ 6 * ax1by1 * ax1cy1 * ax1cy1 * ax1by2 * cx1by2 * cx1by2
				+ -24 * ax1by1 * ax1by1 * ax1by2 * bx1by2 * cx1by2 * bx1dy2
				+ -6 * ax1by1 * ax1by2 * ax1cy2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -6 * ax1by1 * ax1by1 * ax1ay2 * bx1cy2 * bx1cy2 * cx1cy2
				+ -10 * ax1by1 * ax1by1 * cx1ay2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -18 * ax1by1 * ax1by1 * ax1by2 * cx1by2 * bx1cy2 * bx1cy2
				+ -24 * ax1by1 * ax1by1 * cx1dy1 * ax1by2 * ax1by2 * cx1by2
				+ 24 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * cx1by2 * cx1dy2
				+ 24 * ax1by1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * cx1cy2
				+ -18 * ax1by1 * ax1by1 * bx1by2 * bx1by2 * ax1cy2 * cx1cy2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * ax1cy2 * cx1cy2 * cx1cy2
				+ 36 * ax1by1 * ax1cy1 * ax1by2 * bx1by2 * ax1cy2 * cx1cy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1by2 * bx1by2 * cx1by2
				+ 2 * ax1by1 * ax1cy1 * bx1cy1 * ax1by2 * bx1by2 * cx1by2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1ay2 * ax2dy2
				+ 48 * ax1by1 * ax1cy1 * bx1dy1 * ax1ay2 * bx1ay2 * bx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * dx1ay2
				+ -24 * ax1by1 * ax1by1 * ax1ay2 * ax1dy2 * bx1dy2 * ax2dy2
				+ -24 * ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * dx1ay2
				+ 10 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * cx1dy2
				+ -16 * ax1cy1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * bx1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1dy2 * ax2dy2
				+ -10 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * cx1ay2 * cx1ay2
				+ -2 * ax1by1 * ax1cy1 * ax1cy1 * bx1cy1 * cx1ay2 * cx1ay2
				+ 2 * ax1cy1 * ax1cy1 * bx1cy1 * ax1by2 * ax1by2 * cx1by2
				+ -12 * ax1by1 * bx1cy1 * ax1by2 * bx1by2 * ax1cy2 * bx1cy2
				+ -6 * ax1by1 * ax1by1 * ax1cy1 * ax1cy2 * cx1cy2 * ax2cy2
				+ 24 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx1dy2 * bx2dy2
				+ -18 * ax1by1 * ax1by1 * ax1cy1 * ax1by2 * cx1by2 * bx2dy2
				+ 16 * ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * bx1dy2
				+ -18 * ax1by1 * ax1by1 * ax1cy1 * ax1by2 * cx1by2 * dx1by2
				+ 24 * ax1by1 * ax1by1 * dx1ay2 * ax1cy2 * ax1cy2 * bx1cy2
				+ 12 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * bx1cy2 * ax2dy2
				+ -4 * ax1by1 * bx1by2 * ax1cy2 * ax1cy2 * ax1cy2 * bx1cy2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * ax1by2 * ax1by2 * dx1by2
				+ 6 * ax1cy1 * bx1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 2 * ax1cy1 * ax1cy1 * cx1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 4 * ax1by1 * ax1by1 * ax1by1 * cx1by2 * cx1by2 * cx1by2
				+ 4 * bx1cy1 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1ay2 * dx1ay2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax1cy2 * bx2cy2
				+ -18 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * dx1ay2 * dx1ay2
				+ -14 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * bx2cy2 * bx2cy2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * ax1by2 * bx2dy2 * bx2dy2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * ax1by2 * dx1by2 * dx1by2
				+ 288 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * ax1cy2 * ax2dy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * ax1dy2 * ax2dy2
				+ 144 * ax1by1 * ax1cy1 * dx1ay2 * ax1by2 * ax1cy2 * ax1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1ay2 * ax2dy2
				+ 48 * ax1by1 * cx1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -144 * ax1by1 * ax1dy1 * ax1ay2 * ax1by2 * cx1cy2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * dx1dy2
				+ -144 * ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * ax1dy2 * cx2dy2
				+ 72 * ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * cx1cy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * dx1ay2 * ax1by2 * ax1cy2
				+ 18 * ax1by1 * ax1by1 * ax1by1 * ax1dy2 * ax2dy2 * ax2dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * cx1ay2 * ax1dy2
				+ -36 * ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * dx1by2 * ax1cy2
				+ -144 * ax1cy1 * ax1dy1 * ax1ay2 * bx1by2 * ax1cy2 * ax1dy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1ay2 * dx1ay2
				+ 12 * ax1by1 * ax1by1 * cx1dy1 * cx1dy1 * ax1ay2 * ax1ay2
				+ 12 * bx1cy1 * bx1cy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ -12 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * bx1cy2 * bx1cy2
				+ 18 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1ay2 * ax2dy2
				+ 36 * ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * cx1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * dx1ay2 * ax1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax1ay2 * dx1ay2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * bx1ay2 * cx1ay2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1ay2 * ax2dy2
				+ -72 * ax1by1 * ax1cy1 * ax1by2 * ax1by2 * ax1cy2 * cx2dy2
				+ -48 * ax1by1 * ax1dy1 * cx1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * ax1by2 * bx2dy2
				+ -24 * ax1cy1 * ax1cy1 * ax1dy1 * bx1ay2 * ax1by2 * bx1cy2
				+ 8 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * cx1by2 * bx1cy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * ax2by2 * bx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1by2 * bx1by2 * bx2dy2
				+ 12 * ax1by1 * ax1by1 * ax1ay2 * ax1cy2 * bx1cy2 * cx2dy2
				+ -4 * ax1by1 * bx1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * bx1dy2
				+ -12 * ax1cy1 * ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1dy2
				+ 12 * bx1cy1 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1dy1 * bx1ay2 * bx1ay2 * ax1dy2
				+ -24 * ax1by1 * cx1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1dy2
				+ -24 * ax1cy1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * bx1dy2
				+ 24 * ax1cy1 * ax1cy1 * bx1ay2 * ax1by2 * bx1cy2 * ax1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1ay2 * bx2cy2 * ax1dy2 * ax1dy2
				+ 12 * ax1by1 * ax1dy1 * ax1ay2 * ax1cy2 * bx1cy2 * bx1cy2
				+ 72 * ax1by1 * cx1ay2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 72 * ax1by1 * ax1by2 * cx1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 36 * bx1cy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -72 * ax1by1 * ax1dy1 * ax1by2 * cx1by2 * ax1cy2 * ax1cy2
				+ -72 * ax1by1 * ax1cy1 * ax1by2 * ax1by2 * ax1cy2 * dx1cy2
				+ -36 * ax1cy1 * bx1dy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ -72 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * cx2dy2
				+ -2 * bx1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1dy2
				+ 2 * bx1cy1 * bx1cy1 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1cy1 * ax2by2 * ax1cy2
				+ 4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * ax1by2 * cx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1by2 * bx1by2 * dx1by2
				+ 36 * ax1by1 * ax1cy1 * ax1dy1 * ax1by2 * ax1by2 * dx1by2
				+ -36 * ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1dy2
				+ -48 * ax1by1 * bx1cy1 * bx1ay2 * bx1by2 * ax1cy2 * ax1dy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * dx1by2 * dx1cy2
				+ -60 * ax1by1 * ax1by1 * ax1dy1 * dx1ay2 * bx1by2 * ax1cy2
				+ 36 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1dy2 * bx1dy2
				+ 12 * ax1by1 * ax1dy1 * cx1dy1 * ax1by2 * ax1by2 * ax1by2
				+ -84 * ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * bx1by2 * ax1cy2
				+ 12 * ax1by1 * ax1cy1 * bx1ay2 * ax1cy2 * ax1cy2 * cx1cy2
				+ 8 * bx1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1by2 * ax1cy2
				+ 4 * ax1cy1 * bx1cy1 * bx1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 6 * ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * bx1dy2 * bx1dy2
				+ -6 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1dy2
				+ -8 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1by2 * bx1cy2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * cx1ay2 * cx1by2 * ax1cy2
				+ 8 * ax1cy1 * ax1cy1 * ax1cy1 * cx1ay2 * ax1by2 * bx1cy2
				+ 12 * ax1by1 * ax1by1 * ax1cy1 * bx1cy1 * ax2by2 * cx1cy2
				+ -8 * ax1by1 * ax1cy1 * bx1by2 * bx1by2 * bx1by2 * ax1dy2
				+ -8 * ax1by1 * ax1cy1 * ax1ay2 * bx1cy2 * bx1cy2 * bx1cy2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1cy2 * cx1cy2 * ax2cy2
				+ -12 * ax1by1 * ax1by1 * bx1cy1 * cx1ay2 * cx1by2 * ax1cy2
				+ -4 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1by2 * cx1cy2
				+ 12 * ax1by1 * ax1by1 * ax1ay2 * dx1ay2 * ax1dy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * dx1ay2 * ax1dy2 * ax2dy2
				+ 24 * ax1by1 * ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * ax2dy2
				+ 24 * ax1by1 * ax1dy1 * bx1by2 * bx1by2 * ax1cy2 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1ay2 * ax2dy2
				+ 12 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx1cy2 * dx1cy2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * ax1by2 * dx1cy2 * bx2cy2
				+ 8 * ax1by1 * ax1cy1 * ax1dy1 * bx1by2 * bx1by2 * bx1by2
				+ 12 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * bx2cy2 * bx1dy2
				+ -12 * ax1by1 * ax1cy1 * bx1dy1 * ax1by2 * ax1by2 * cx1by2
				+ -4 * ax1cy1 * bx1cy1 * bx1dy1 * ax1by2 * ax1by2 * ax1by2
				+ -12 * ax1by1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1dy2 * bx1dy2
				+ -12 * ax1by1 * bx1cy1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1ay2
				+ -12 * ax1by1 * ax1by1 * bx1ay2 * ax1cy2 * ax1cy2 * dx1cy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * bx1dy1 * bx1ay2 * bx1ay2
				+ -8 * ax1by1 * bx1dy1 * bx1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1dy2
				+ -6 * ax1cy1 * ax1cy1 * bx1cy1 * bx1ay2 * bx1ay2 * ax1dy2
				+ 6 * ax1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1dy2
				+ -6 * ax1by1 * ax1by1 * ax1cy1 * cx1ay2 * cx1ay2 * bx1dy2
				+ 6 * ax1cy1 * ax1cy1 * bx1cy1 * ax1dy1 * bx1ay2 * bx1ay2
				+ -6 * ax1cy1 * bx1cy1 * bx1cy1 * bx1dy1 * ax1ay2 * ax1ay2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * cx1ay2 * cx1ay2
				+ 6 * ax1by1 * ax1by1 * bx1by2 * ax1cy2 * ax1cy2 * bx2dy2
				+ 6 * ax1by1 * ax1by1 * ax1by1 * ax1by2 * bx2cy2 * cx2dy2
				+ -12 * ax1by1 * ax1by1 * ax1by2 * bx1by2 * ax1cy2 * cx2dy2
				+ 12 * ax1by1 * ax1cy1 * ax1by2 * ax1by2 * cx1by2 * bx1dy2
				+ 4 * ax1cy1 * bx1cy1 * ax1by2 * ax1by2 * ax1by2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1by2 * dx1cy2
				+ 12 * ax1by1 * ax1by1 * bx1cy1 * ax1by2 * ax2cy2 * bx1dy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * bx1cy2 * bx2dy2
				+ 16 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * bx2cy2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * bx1ay2 * bx1by2 * ax1cy2 * cx1dy2
				+ 18 * ax1by1 * ax1by1 * ax1by1 * dx1by2 * ax1cy2 * bx2cy2
				+ -24 * ax1by1 * ax1by1 * ax1ay2 * bx1by2 * bx1cy2 * cx1dy2
				+ -42 * ax1by1 * ax1by2 * bx1by2 * ax1cy2 * bx1cy2 * ax1dy2
				+ -18 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1dy2
				+ 96 * ax1by1 * ax1by1 * ax1dy1 * cx1ay2 * bx1by2 * bx1cy2
				+ -84 * ax1by1 * ax1by1 * bx1ay2 * ax1by2 * cx1cy2 * bx1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * cx2dy2
				+ -72 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * bx2cy2 * ax1dy2
				+ 48 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * bx1by2 * dx1cy2
				+ -36 * ax1cy1 * ax1cy1 * ax1dy1 * cx1ay2 * ax1by2 * ax1cy2
				+ -80 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * ax1ay2 * bx1ay2
				+ -4 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * bx1ay2 * dx1ay2
				+ 4 * ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * ax1ay2 * bx1ay2
				+ 12 * ax1cy1 * ax1cy1 * ax1cy1 * dx1ay2 * ax1by2 * ax1cy2
				+ 6 * ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * bx1dy2 * cx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1cy2 * ax2dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * dx1ay2 * ax1dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * dx1cy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * dx1by2 * bx1cy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * dx1ay2 * bx1by2 * bx1cy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * cx1dy1 * ax2by2 * bx1cy2
				+ -12 * ax1by1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1dy2 * bx1dy2
				+ -8 * ax1by1 * ax1by1 * cx1dy1 * ax1by2 * bx1by2 * bx1by2
				+ 8 * ax1by1 * ax1by1 * ax1by2 * bx1by2 * bx1by2 * cx1dy2
				+ 12 * ax1by1 * bx1cy1 * ax1ay2 * bx1ay2 * ax1dy2 * bx1dy2
				+ -10 * ax1by1 * ax1by1 * bx1ay2 * cx1ay2 * ax1dy2 * bx1dy2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1dy2 * ax2dy2
				+ -4 * ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1dy2 * ax2dy2
				+ -4 * ax1by1 * ax1by1 * bx1ay2 * cx1ay2 * ax1dy2 * bx1dy2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1dy1 * ax2by2 * cx1cy2
				+ 40 * ax1by1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1dy2 * bx1dy2
				+ -72 * ax1by1 * ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * dx1cy2
				+ -40 * ax1by1 * ax1cy1 * ax1ay2 * bx1ay2 * bx1dy2 * bx1dy2
				+ 72 * ax1by1 * ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * dx1cy2
				+ -24 * ax1by1 * ax1by1 * bx1cy1 * ax1by2 * bx1by2 * dx1by2
				+ 72 * ax1by1 * ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * cx2dy2
				+ -48 * ax1by1 * ax1by1 * ax1dy1 * cx1ay2 * bx1by2 * bx1cy2
				+ 28 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * dx1ay2 * bx1dy2 + 24
				* ax1by1 * ax1by1 * ax1by1 * ax1by2 * ax2dy2 * cx2dy2 + 24
				* ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * bx1by2 * bx1cy2 + -24
				* ax1by1 * ax1by1 * bx1cy1 * ax1by2 * bx1by2 * bx2dy2 + 24
				* ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * bx1by2 * bx1cy2 + -24
				* ax1by1 * ax1by1 * ax1ay2 * bx1by2 * ax1dy2 * cx2dy2 + -12
				* ax1by1 * ax1ay2 * bx1by2 * bx1cy2 * ax1dy2 * ax1dy2 + 12
				* ax1by1 * ax1by1 * bx1by2 * ax2cy2 * ax1dy2 * ax1dy2 + 12
				* ax1by1 * bx1cy1 * bx1dy1 * ax1ay2 * bx1ay2 * ax1dy2 + -8
				* ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1by2 * bx2dy2 + -8
				* ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1by2 * dx1by2 + -12
				* ax1by1 * bx1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1dy2 + -6
				* ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * bx1ay2 * dx1dy2 + -10
				* ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1ay2 * dx1dy2 + -8
				* ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1ay2 * dx1dy2 + 8
				* ax1by1 * ax1by1 * ax1by1 * bx1ay2 * dx1ay2 * cx1dy2 + -24
				* ax1by1 * bx1cy1 * bx1dy1 * ax1ay2 * bx1ay2 * ax1dy2 + 24
				* ax1by1 * ax1by1 * ax1dy1 * bx1ay2 * cx1by2 * bx1cy2 + -12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax2by2 * ax1cy2 + 28
				* ax1by1 * ax1by1 * bx1dy1 * cx1dy1 * ax1ay2 * bx1ay2 + 48
				* ax1by1 * bx1cy1 * ax1dy1 * ax1ay2 * bx1by2 * bx1cy2 + -12
				* bx1cy1 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 + 12
				* bx1cy1 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 + -12
				* bx1cy1 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 + -12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * bx2cy2 * ax1dy2 + -24
				* ax1by1 * ax1by1 * bx1cy1 * ax1dy1 * bx1by2 * ax2cy2 + 36
				* ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * cx1cy2 * ax1dy2 + 72
				* ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * bx2cy2 * ax1dy2 + -72
				* ax1by1 * ax1ay2 * bx1by2 * bx1cy2 * ax1dy2 * ax1dy2 + ax1by1
				* ax1by1 * ax2by2 * bx1cy2 * ax1dy2 * ax1dy2 + -12 * ax1cy1
				* ax1cy1 * ax1cy1 * ax1by2 * ax2cy2 * ax1dy2 + 35 * ax1by1
				* ax1by1 * ax2by2 * bx1cy2 * ax1dy2 * ax1dy2 + -36 * ax1by1
				* ax1cy1 * ax1by2 * ax1by2 * ax1dy2 * bx2dy2 + 12 * ax1cy1
				* ax1by2 * ax1by2 * ax1by2 * ax1dy2 * bx1dy2 + 16 * ax1by1
				* ax1by1 * ax1by1 * bx1cy1 * dx1ay2 * bx2cy2 + 56 * ax1by1
				* ax1by1 * ax1by1 * bx1ay2 * cx1by2 * cx2dy2 + 40 * ax1by1
				* ax1by1 * ax1by1 * bx1ay2 * bx2cy2 * cx1dy2 + 42 * ax1by1
				* ax1by1 * bx1dy1 * cx1dy1 * ax1ay2 * bx1ay2 + -14 * ax1by1
				* ax1by1 * ax1dy1 * bx1dy1 * bx1ay2 * cx1ay2 + -28 * ax1by1
				* ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * cx1ay2 + -72 * ax1by1
				* ax1by1 * bx1dy1 * ax1ay2 * bx2cy2 * ax1dy2 + 24 * ax1by1
				* ax1cy1 * dx1ay2 * ax1cy2 * ax1cy2 * ax1cy2 + -72 * ax1by1
				* ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * dx1cy2 + -24 * ax1cy1
				* bx1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 + -24 * ax1by1
				* ax1by1 * ax1by1 * ax1ay2 * bx2cy2 * dx1dy2 + 72 * ax1by1
				* ax1dy1 * ax1ay2 * bx1by2 * bx1cy2 * ax1dy2 + -24 * ax1by1
				* ax1by1 * ax1by1 * dx1ay2 * ax1cy2 * bx2dy2 + 24 * ax1by1
				* ax1by1 * bx1ay2 * ax1by2 * dx1cy2 * ax1dy2 + 48 * ax1by1
				* ax1by1 * ax1dy1 * ax1ay2 * bx1cy2 * bx2dy2 + 96 * ax1by1
				* bx1dy1 * bx1ay2 * ax1by2 * ax1cy2 * ax1dy2 + 48 * ax1by1
				* ax1by1 * bx1ay2 * ax1by2 * ax1cy2 * dx1dy2 + -24 * ax1by1
				* ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * cx1dy2 + -12 * ax1by1
				* ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * ax2cy2 + 36 * ax1by1
				* ax1by1 * ax1dy1 * bx1ay2 * dx1by2 * ax1cy2 + -36 * ax1by1
				* ax1by1 * ax1dy1 * bx1ay2 * ax1by2 * dx1cy2 + -24 * ax1by1
				* ax1by1 * ax1by1 * ax1dy1 * ax2by2 * dx1cy2 + 24 * ax1cy1
				* ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * bx1dy2 + 28 * ax1by1
				* ax1dy1 * ax1by2 * bx1by2 * bx1by2 * ax1dy2 + 4 * ax1by1
				* ax1by1 * ax1by2 * ax1by2 * bx1by2 * dx1dy2 + -8 * ax1by1
				* ax1by1 * ax1by2 * ax1by2 * dx1by2 * bx1dy2 + 16 * ax1by1
				* ax1by1 * ax1by1 * ax1by2 * dx1by2 * bx2dy2 + 24 * ax1by1
				* ax1cy1 * ax1cy2 * ax1cy2 * ax1cy2 * ax2dy2 + -72 * ax1by1
				* ax1cy1 * ax1ay2 * ax1cy2 * ax1cy2 * cx2dy2 + 24 * ax1by1
				* ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * cx1dy2 + 48 * ax1cy1
				* ax1cy1 * bx1dy1 * ax1ay2 * bx1by2 * ax1cy2 + -48 * bx1cy1
				* bx1cy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2 + 48 * bx1cy1
				* bx1cy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2 + -48 * ax1cy1
				* ax1cy1 * ax1ay2 * bx1by2 * ax1cy2 * bx1dy2 + 72 * ax1by1
				* ax1cy1 * ax1ay2 * ax1by2 * bx1cy2 * cx1dy2 + 72 * ax1by1
				* ax1cy1 * bx1ay2 * cx1by2 * ax1cy2 * ax1dy2 + -36 * ax1by1
				* ax1by1 * ax1cy1 * ax1cy1 * ax2cy2 * bx2dy2 + -72 * ax1by1
				* ax1cy1 * cx1dy1 * bx1ay2 * ax1by2 * ax1cy2 + -72 * ax1by1
				* ax1cy1 * ax1dy1 * ax1ay2 * cx1by2 * bx1cy2 + -36 * ax1by1
				* ax1by1 * ax1cy1 * ax1cy1 * dx1by2 * ax2cy2 + 72 * ax1by1
				* ax1by1 * ax1cy1 * ax1cy1 * dx1by2 * ax2cy2;
		coeffs[7] = -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2
				+ -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2
				+ 180 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2
				+ 180 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2
				+ 180 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2
				+ 60 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2
				+ -180 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2 * ax1dy2
				+ -180 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ -360 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -30 * ax1dy1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -120 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2
				+ -60 * ax1dy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 60 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 60 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 180 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 30 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 60 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 60 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 6 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1ay2 * bx1cy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1by2 * bx1by2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1cy2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * bx1by2 * bx1by2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * bx1by2 * bx1cy2 * bx1cy2
				+ 4 * ax1by1 * ax1by1 * bx1by2 * bx1by2 * bx1by2 * bx1cy2
				+ 144 * ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * dx1dy2
				+ 144 * ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1cy2 * dx1dy2
				+ -12 * bx1cy1 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ -12 * bx1cy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 96 * bx1cy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2
				+ 96 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2
				+ -12 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2
				+ -12 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2
				+ -18 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * bx1dy2 * bx1dy2
				+ -18 * ax1by1 * ax1ay2 * ax1ay2 * ax1cy2 * bx1dy2 * bx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * cx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1cy2 * cx1dy2
				+ 36 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * ax1by2 * bx1dy2
				+ 36 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * bx1dy2
				+ -18 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1by2 * ax1by2
				+ -18 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1cy2
				+ -6 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1by2 * ax1by2
				+ -6 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1cy2
				+ -24 * ax1by1 * bx1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1cy2
				+ 12 * ax1by1 * bx1dy1 * ax1by2 * ax1by2 * ax1by2 * bx1cy2
				+ 72 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * ax1cy2 * bx1cy2
				+ -12 * ax1cy1 * ax1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * cx1cy2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * cx1cy2
				+ 8 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * ax1by2 * cx1cy2
				+ 24 * ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * ax1cy2 * cx1cy2
				+ 24 * ax1by1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1cy2 * bx1dy2
				+ -12 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * bx1cy2 * bx1dy2
				+ -72 * ax1by1 * ax1ay2 * ax1by2 * ax1cy2 * bx1cy2 * bx1dy2
				+ -84 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * cx1ay2 * ax1cy2
				+ -56 * ax1by1 * cx1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2
				+ -96 * ax1by1 * cx1dy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2
				+ 84 * ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * ax1cy2 * cx1dy2
				+ 56 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * cx1dy2
				+ 96 * ax1by1 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * cx1dy2
				+ -16 * ax1cy1 * bx1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2
				+ -48 * ax1cy1 * bx1dy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * bx1ay2 * cx1cy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * bx1by2 * cx1by2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1by2 * cx1by2
				+ 12 * ax1by1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1by2 * cx1by2
				+ -12 * ax1by1 * ax1by1 * ax1ay2 * bx1by2 * cx1by2 * bx1dy2
				+ -10 * ax1by1 * ax1ay2 * ax1cy2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -20 * ax1by1 * ax1by2 * ax1by2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -18 * ax1by1 * ax1by1 * ax1ay2 * cx1by2 * bx1cy2 * bx1cy2
				+ -30 * ax1by1 * ax1by1 * cx1ay2 * ax1by2 * bx1cy2 * bx1cy2
				+ 36 * ax1by1 * ax1by1 * cx1dy1 * bx1ay2 * ax1by2 * bx1by2
				+ -84 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * ax1by2 * cx1by2
				+ -36 * ax1by1 * ax1by1 * bx1ay2 * ax1by2 * bx1by2 * cx1dy2
				+ 84 * ax1by1 * ax1by1 * ax1ay2 * ax1by2 * cx1by2 * cx1dy2
				+ 24 * ax1by1 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * cx1cy2
				+ 36 * ax1by1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * cx1cy2
				+ 84 * ax1by1 * ax1by1 * ax1ay2 * cx1by2 * ax1cy2 * cx1cy2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * bx1by2 * cx1by2
				+ -4 * ax1by1 * cx1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 24 * ax1by1 * ax1by2 * cx1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 10 * ax1cy1 * bx1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -24 * ax1by1 * ax1by1 * ax1by2 * bx1by2 * cx1by2 * bx1cy2
				+ 24 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * cx1by2 * cx1cy2
				+ 48 * ax1by1 * ax1cy1 * bx1dy1 * ax1ay2 * bx1ay2 * bx1cy2
				+ 20 * bx1cy1 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 4 * ax1by1 * ax1by1 * ax1cy2 * ax1cy2 * bx1cy2 * ax2cy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1ay2 * ax2cy2
				+ -2 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1by2 * ax2by2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * bx2cy2
				+ 4 * bx1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1cy2
				+ 2 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1by2 * bx1by2
				+ -6 * ax1by1 * ax1cy1 * ax1cy1 * cx1ay2 * bx1by2 * bx1by2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1cy2
				+ 10 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1by2 * cx1by2
				+ 4 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * cx1cy2
				+ 12 * ax1by1 * ax1cy1 * bx1cy1 * ax1ay2 * bx1ay2 * cx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * cx1cy2
				+ -2 * bx1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1by2 * ax1by2
				+ -6 * ax1by1 * ax1by1 * bx1cy1 * cx1ay2 * cx1ay2 * ax1cy2
				+ -24 * ax1by1 * ax1cy1 * ax1ay2 * bx1by2 * bx1cy2 * bx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1by2 * bx1by2 * ax2dy2
				+ -24 * ax1by1 * ax1cy1 * bx1ay2 * bx1by2 * bx1by2 * ax1dy2
				+ -18 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * cx2dy2
				+ -36 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1cy2 * ax1dy2
				+ -36 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1cy2 * ax2dy2
				+ -36 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1cy2 * ax1dy2
				+ 30 * ax1by1 * ax1by1 * ax1cy1 * dx1ay2 * bx1by2 * bx1by2
				+ 24 * ax1by1 * ax1cy1 * bx1dy1 * ax1ay2 * bx1by2 * bx1by2
				+ 18 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * dx1cy2
				+ -36 * ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1ay2 * ax2cy2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * bx1cy2 * bx2cy2
				+ 6 * ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1by2 * bx2cy2
				+ -10 * ax1by1 * ax1by1 * bx1cy1 * ax1by2 * bx1by2 * bx2cy2
				+ -8 * ax1by1 * bx1cy1 * ax1by2 * bx1by2 * bx1by2 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * ax1cy2 * bx2cy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1cy2 * ax2cy2
				+ -28 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2cy2 * bx2cy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1by2 * ax2dy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2by2 * bx2dy2
				+ 24 * ax1by1 * bx1cy1 * bx1ay2 * bx1ay2 * ax1cy2 * ax1dy2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2cy2 * ax2dy2
				+ -24 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * dx1ay2 * bx1cy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * dx1ay2 * bx1by2 * cx1by2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1by2 * ax2by2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1ay2 * ax2cy2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * bx1ay2 * cx1cy2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * bx2cy2 * cx2dy2
				+ 14 * ax1by1 * ax1by1 * ax1by1 * ax1cy2 * bx2cy2 * bx2cy2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * bx2dy2 * bx2dy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2cy2 * bx2cy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2dy2 * cx2dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1by2 * ax2by2
				+ 12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2by2 * bx2dy2
				+ 36 * ax1by1 * ax1by1 * ax1by1 * ax1cy2 * ax2cy2 * bx2dy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * ax2cy2 * bx2cy2
				+ 14 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * ax2cy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2cy2 * ax2dy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * ax2cy2 * ax2dy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * dx1by2 * bx2dy2
				+ -18 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1by2 * ax2by2
				+ 18 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1ay2 * ax2cy2
				+ 42 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * dx1by2 * dx1by2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * dx1ay2 * ax1by2 * dx1by2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * dx1ay2 * dx1ay2 * ax1cy2 + -2
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1ay2 * bx1cy2 + -16
				* ax1by1 * ax1by2 * ax1by2 * ax1by2 * bx1cy2 * bx1dy2 + 24
				* ax1by1 * ax1by1 * bx1ay2 * ax1by2 * ax1cy2 * cx2dy2 + -12
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1dy2 * cx2dy2 + 12
				* ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * bx1cy2 * bx1dy2 + -4
				* ax1cy1 * ax1cy1 * ax1cy1 * cx1ay2 * ax1by2 * bx1by2 + 16
				* ax1by1 * ax1by1 * bx1by2 * ax1cy2 * ax1cy2 * bx2cy2 + 12
				* ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx1cy2 * bx2dy2 + 12
				* ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx2cy2 * bx1dy2 + -12
				* ax1by1 * ax1by2 * bx1by2 * bx1by2 * ax1cy2 * ax1dy2 + -12
				* ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1cy2 * bx2cy2 + -24
				* ax1by1 * ax1by1 * ax1dy1 * bx1ay2 * ax1by2 * bx2dy2 + 10
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * ax1by2 * cx1by2 + 20
				* ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * cx1ay2 * ax1cy2 + -12
				* ax1by1 * bx1ay2 * bx1by2 * ax1cy2 * ax1cy2 * ax1dy2 + -36
				* ax1by1 * bx1ay2 * bx1by2 * ax1cy2 * ax1cy2 * ax1dy2 + -12
				* ax1by1 * bx1ay2 * ax1by2 * ax1by2 * ax1dy2 * bx1dy2 + 6
				* ax1by1 * bx1ay2 * ax1by2 * bx1by2 * ax1dy2 * ax1dy2 + -18
				* ax1by1 * ax1by1 * bx1by2 * ax2by2 * ax1dy2 * ax1dy2 + -36
				* ax1by1 * bx1ay2 * ax1by2 * bx1by2 * ax1dy2 * ax1dy2 + -6
				* ax1by1 * bx1ay2 * bx1ay2 * ax1cy2 * ax1dy2 * ax1dy2 + -36
				* ax1by1 * ax1ay2 * bx1ay2 * bx1cy2 * ax1dy2 * ax1dy2 + 12
				* ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1cy2 * ax2dy2 + -48
				* ax1by1 * ax1by1 * ax1dy1 * ax1dy1 * bx1by2 * ax2by2 + -6
				* ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * bx1ay2 * bx1cy2 + 12
				* ax1cy1 * ax1cy1 * bx1ay2 * bx1by2 * ax1cy2 * ax1cy2 + 12
				* ax1cy1 * ax1cy1 * bx1ay2 * ax1by2 * ax1by2 * bx1dy2 + -30
				* ax1by1 * ax1by1 * ax1cy1 * dx1ay2 * ax1by2 * cx1by2 + -30
				* ax1by1 * ax1by1 * ax1cy1 * ax1ay2 * cx1ay2 * dx1cy2 + 4
				* ax1by1 * bx1dy1 * ax1by2 * ax1by2 * ax1by2 * bx1cy2 + 12
				* ax1by1 * ax1by1 * ax1by2 * ax1by2 * dx1by2 * bx1cy2 + 12
				* ax1by1 * ax1by1 * ax1by2 * bx1by2 * dx1by2 * ax1cy2 + 24
				* ax1by1 * bx1dy1 * ax1by2 * ax1by2 * bx1by2 * ax1cy2 + 36
				* ax1by1 * ax1by1 * bx1ay2 * dx1by2 * ax1cy2 * ax1cy2 + 48
				* ax1by1 * bx1dy1 * bx1ay2 * ax1by2 * ax1cy2 * ax1cy2 + 36
				* ax1by1 * ax1by1 * ax1ay2 * ax1by2 * bx1cy2 * dx1cy2 + 36
				* ax1by1 * ax1by1 * ax1ay2 * ax1by2 * bx1by2 * dx1dy2 + 12
				* ax1by1 * bx1dy1 * bx1ay2 * ax1by2 * ax1by2 * ax1dy2 + 36
				* ax1by1 * ax1by1 * dx1ay2 * ax1by2 * ax1by2 * bx1dy2 + 72
				* ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * bx1by2 * ax1dy2 + 12
				* ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1cy2 * ax1dy2 + 36
				* ax1by1 * ax1by1 * bx1ay2 * dx1ay2 * ax1cy2 * ax1dy2 + 36
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * dx1cy2 * bx1dy2 + 72
				* ax1by1 * bx1dy1 * ax1ay2 * bx1ay2 * ax1cy2 * ax1dy2 + -42
				* ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1by2 * ax1by2 + -72
				* ax1by1 * ax1by1 * ax1dy1 * dx1ay2 * ax1by2 * bx1by2 + -6
				* ax1by1 * ax1by1 * ax1dy1 * ax1ay2 * dx1ay2 * bx1cy2 + -18
				* ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * dx1cy2 + -36
				* ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * bx1ay2 * ax1cy2 + -12
				* ax1cy1 * ax1cy1 * bx1dy1 * bx1ay2 * ax1by2 * ax1by2 + -12
				* ax1cy1 * ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1cy2 + 30
				* ax1by1 * ax1by1 * ax1ay2 * ax1by2 * cx1cy2 * cx1cy2 + -60
				* ax1by1 * ax1by1 * ax1ay2 * cx1by2 * ax1cy2 * cx1cy2 + 18
				* ax1by1 * ax1by1 * cx1ay2 * cx1by2 * ax1cy2 * ax1cy2 + 30
				* ax1by1 * ax1by1 * ax1ay2 * cx1by2 * cx1by2 * ax1dy2 + -60
				* ax1by1 * ax1by1 * ax1ay2 * ax1by2 * cx1by2 * cx1dy2 + 18
				* ax1by1 * ax1by1 * cx1ay2 * ax1by2 * ax1by2 * cx1dy2 + 30
				* ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * ax1cy2 * ax1dy2 + -60
				* ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * ax1cy2 * cx1dy2 + 18
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1cy2 * cx1dy2 + 12
				* ax1by1 * ax1by1 * cx1dy1 * cx1ay2 * ax1by2 * ax1by2 + 12
				* ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * cx1cy2 + 4
				* ax1cy1 * bx1cy1 * ax1by2 * ax1by2 * ax1by2 * bx1cy2 + -6
				* ax1by1 * ax1cy1 * ax1by2 * ax1by2 * cx1by2 * bx1cy2 + 18
				* ax1by1 * ax1cy1 * ax1by2 * bx1by2 * cx1by2 * ax1cy2 + 24
				* ax1by1 * ax1cy1 * bx1ay2 * ax1by2 * ax1cy2 * cx1cy2 + 24
				* ax1by1 * ax1cy1 * bx1ay2 * ax1by2 * cx1by2 * ax1dy2 + 24
				* ax1by1 * ax1cy1 * ax1ay2 * cx1ay2 * bx1cy2 * ax1dy2 + -24
				* ax1by1 * ax1cy1 * ax1dy1 * bx1ay2 * ax1by2 * cx1by2 + -24
				* ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * cx1ay2 * bx1cy2 + 6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax2by2 * ax1cy2 * ax1cy2 + -12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax1cy2 * ax2cy2 + 12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax2by2 * ax1dy2 + 18
				* ax1cy1 * ax1cy1 * cx1ay2 * ax1by2 * ax1by2 * ax1dy2 + 6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax2cy2 * ax1dy2 + 18
				* ax1cy1 * ax1cy1 * ax1ay2 * cx1ay2 * ax1cy2 * ax1dy2 + -12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1by2 * ax2by2 + -18
				* ax1cy1 * ax1cy1 * ax1dy1 * cx1ay2 * ax1by2 * ax1by2 + -6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * ax2cy2 + -18
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * cx1ay2 * ax1cy2 + -32
				* ax1cy1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * bx1dy2 + -24
				* ax1cy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * bx1dy2 + 72
				* ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2 * bx1dy2 + -216
				* ax1cy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * bx1dy2 + -72
				* ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * cx1dy2 + 24
				* ax1by1 * ax1by2 * ax1by2 * ax1by2 * cx1cy2 * ax1dy2 + 144
				* ax1by1 * ax1ay2 * ax1by2 * ax1cy2 * cx1cy2 * ax1dy2 + 36
				* ax1by1 * ax1ay2 * ax1ay2 * cx1cy2 * ax1dy2 * ax1dy2 + 72
				* ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1dy2 * cx2dy2 + 72
				* ax1cy1 * ax1ay2 * ax1ay2 * bx1cy2 * ax1dy2 * ax1dy2 + -24
				* ax1by1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * cx1cy2 + -72
				* ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * cx1cy2 * ax1dy2 + 36
				* ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * cx1cy2 + 72
				* ax1cy1 * ax1by2 * ax1by2 * bx1by2 * ax1cy2 * ax1dy2 + -72
				* ax1by1 * ax1dy1 * ax1ay2 * cx1by2 * ax1cy2 * ax1cy2 + -144
				* ax1by1 * ax1dy1 * ax1ay2 * ax1by2 * cx1by2 * ax1dy2 + 72
				* ax1by1 * ax1cy1 * ax2by2 * ax1cy2 * ax1cy2 * ax1dy2 + 144
				* ax1cy1 * bx1ay2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2 + 72
				* ax1by1 * ax1cy1 * ax1by2 * ax2by2 * ax1dy2 * ax1dy2 + 108
				* ax1by1 * cx1ay2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2 + 72
				* ax1by1 * ax1ay2 * cx1ay2 * ax1cy2 * ax1dy2 * ax1dy2 + -72
				* ax1cy1 * ax1dy1 * bx1ay2 * ax1by2 * ax1cy2 * ax1cy2 + -144
				* ax1cy1 * ax1dy1 * ax1ay2 * bx1ay2 * ax1cy2 * ax1dy2 + 36
				* ax1by1 * ax1dy1 * ax1dy1 * cx1ay2 * ax1by2 * ax1by2 + 72
				* ax1by1 * ax1cy1 * ax1dy1 * ax1dy1 * ax1by2 * ax2by2 + 72
				* ax1cy1 * ax1dy1 * ax1dy1 * bx1ay2 * ax1by2 * ax1by2 + 72
				* ax1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * bx1ay2 * ax1cy2 + -24
				* ax1cy1 * bx1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 + -72
				* ax1by1 * ax1cy1 * ax1by2 * ax1by2 * dx1by2 * ax1cy2 + -144
				* ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * ax1cy2 * dx1cy2 + -72
				* ax1by1 * cx1dy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 + -72
				* ax1by1 * ax1cy1 * dx1ay2 * ax1by2 * ax1by2 * ax1dy2 + -144
				* ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1cy2 * dx1dy2 + -72
				* ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2 + 72
				* ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1by2 * ax1by2 + 72
				* ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1cy2;
		coeffs[8] = -60 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2
				+ 180 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1dy2
				+ 90 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2
				+ 180 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2
				+ 15 * ax1dy1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1by2
				+ -180 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2
				+ -180 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2
				+ -360 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2
				+ -120 * ax1dy1 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -30 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1dy2
				+ -60 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ 60 * ax1ay2 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 90 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 180 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 120 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 6 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 15 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ 60 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 15 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1ay2 * bx1by2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1by2 * bx1dy2
				+ 6 * ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1cy2 * bx1cy2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * bx1by2 * bx1by2 * bx1cy2
				+ ax1by1 * ax1by1 * bx1by2 * bx1by2 * bx1by2 * bx1by2
				+ 21 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1cy2 * cx1cy2
				+ 3 * ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * ax1cy2 * ax1cy2
				+ 6 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * cx1by2 * cx1by2
				+ 6 * ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * ax1cy2 * ax1cy2
				+ 144 * ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * dx1dy2
				+ -12 * bx1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2
				+ 96 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1dy2
				+ -12 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2
				+ -18 * ax1by1 * ax1ay2 * ax1ay2 * ax1by2 * bx1dy2 * bx1dy2
				+ 6 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * cx1dy2
				+ 36 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1by2 * bx1dy2
				+ -18 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1by2
				+ -6 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1by2
				+ 36 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * ax1by2 * bx1cy2
				+ 60 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * bx1cy2
				+ 6 * ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * cx1cy2
				+ 12 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1cy2 * cx1cy2
				+ -36 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * bx1cy2 * bx1dy2
				+ -60 * ax1by1 * ax1ay2 * ax1ay2 * ax1cy2 * bx1cy2 * bx1dy2
				+ -96 * ax1by1 * cx1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2
				+ -48 * ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2
				+ 96 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * cx1dy2
				+ 48 * ax1by1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * cx1dy2
				+ -48 * ax1cy1 * bx1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2
				+ -60 * ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2
				+ -24 * ax1by1 * bx1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1by2
				+ 10 * ax1by1 * bx1dy1 * ax1by2 * ax1by2 * ax1by2 * bx1by2
				+ -12 * ax1cy1 * ax1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * cx1by2
				+ -12 * ax1by1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * cx1by2
				+ 2 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * ax1by2 * cx1by2
				+ 24 * ax1by1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1by2 * bx1dy2
				+ -10 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * bx1by2 * bx1dy2
				+ -12 * ax1by1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1cy2 * bx1cy2
				+ -36 * ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * bx1cy2 * bx1cy2
				+ -20 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * bx1cy2 * bx1cy2
				+ -36 * ax1by1 * ax1ay2 * ax1by2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -84 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * cx1ay2 * ax1by2
				+ -24 * ax1by1 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * cx1by2
				+ 4 * ax1by1 * cx1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1by2
				+ 84 * ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * ax1by2 * cx1dy2
				+ 24 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * cx1by2 * ax1dy2
				+ -4 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * cx1dy2
				+ 24 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * cx1cy2
				+ 72 * ax1by1 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * cx1cy2
				+ -10 * ax1cy1 * bx1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1by2
				+ 6 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * bx1ay2 * cx1by2
				+ 8 * ax1by1 * ax1ay2 * cx1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 40 * ax1cy1 * bx1ay2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -12 * ax1by1 * ax1by1 * ax1ay2 * bx1by2 * cx1by2 * bx1cy2
				+ -36 * ax1by1 * ax1by1 * bx1ay2 * ax1by2 * bx1by2 * cx1cy2
				+ 84 * ax1by1 * ax1by1 * ax1ay2 * ax1by2 * cx1by2 * cx1cy2
				+ 36 * ax1by1 * ax1by2 * ax1by2 * cx1by2 * ax1cy2 * ax1cy2
				+ 48 * ax1by1 * ax1cy1 * bx1dy1 * ax1ay2 * bx1ay2 * bx1by2
				+ 16 * ax1by1 * ax1cy1 * ax2by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 10 * ax1cy1 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * bx1dy2
				+ 20 * bx1cy1 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ 6 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx1by2 * dx1by2
				+ 6 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx1by2 * bx2dy2
				+ -4 * ax1by1 * ax1by1 * bx1cy1 * bx1cy1 * bx1ay2 * ax2by2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1cy2 * ax2cy2
				+ 4 * bx1cy1 * bx1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1by2
				+ -4 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1by2
				+ 4 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * cx1by2
				+ 12 * ax1by1 * ax1cy1 * bx1cy1 * ax1ay2 * bx1ay2 * cx1by2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * cx1by2
				+ -6 * ax1by1 * ax1by1 * ax1cy1 * cx1ay2 * cx1ay2 * bx1by2
				+ 6 * ax1by1 * ax1by1 * ax1cy1 * bx1by2 * bx1by2 * ax2cy2
				+ -24 * ax1by1 * ax1cy1 * bx1ay2 * bx1by2 * bx1by2 * ax1cy2
				+ -18 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx2dy2
				+ -36 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1by2 * ax1dy2
				+ -36 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1by2 * ax2dy2
				+ -36 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1by2 * ax1dy2
				+ 18 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * dx1by2
				+ -36 * ax1by1 * ax1by1 * ax1cy1 * bx1dy1 * bx1ay2 * ax2by2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1by2 * ax2cy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2by2 * bx2cy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2cy2 * ax2cy2
				+ 18 * ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1cy2 * ax2cy2
				+ -6 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * ax1cy2 * ax2cy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2cy2 * ax2cy2
				+ 24 * ax1by1 * bx1cy1 * bx1ay2 * bx1ay2 * ax1by2 * ax1dy2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2by2 * ax2dy2
				+ -24 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * dx1ay2 * bx1by2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * dx1ay2 * ax2by2
				+ 24 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * bx1ay2 * cx1by2
				+ 18 * ax1by1 * ax1by1 * ax1by1 * ax1by2 * bx2cy2 * bx2cy2
				+ 12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2by2 * bx2cy2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * ax2by2 * ax1cy2 * bx2dy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2dy2 * bx2dy2
				+ 14 * ax1cy1 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * ax2by2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2by2 * ax2dy2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * ax2cy2 * ax2cy2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * ax2cy2 * ax2cy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * ax2by2 * ax2dy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * dx1by2 * bx2cy2
				+ 18 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * dx1ay2 * ax2by2
				+ -6 * ax1by1 * ax1by1 * ax1by1 * dx1ay2 * dx1ay2 * ax1by2
				+ -12 * ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * ax1cy2 * cx2dy2
				+ -12 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * bx1cy2 * bx1cy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1ay2 * bx1by2
				+ 24 * ax1by1 * ax1by1 * ax1by2 * bx1by2 * ax1cy2 * bx2cy2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * ax1by2 * bx1cy2 * bx2dy2
				+ -12 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1dy2 * bx2dy2
				+ -24 * ax1by1 * ax1by1 * ax1dy1 * bx1ay2 * ax1by2 * bx2cy2
				+ 3 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * bx1by2 * bx1by2
				+ 12 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * bx1by2 * bx1dy2
				+ 20 * ax1cy1 * ax1cy1 * ax1cy1 * bx1ay2 * cx1ay2 * ax1by2
				+ -12 * ax1by1 * ax1by1 * ax2by2 * ax1cy2 * ax1cy2 * bx1cy2
				+ -4 * ax1by1 * bx1ay2 * bx1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 24 * ax1by1 * ax1by1 * ax1by2 * ax1cy2 * bx1cy2 * ax2cy2
				+ -36 * ax1by1 * ax1by1 * ax1by2 * bx1by2 * ax1cy2 * ax2dy2
				+ -12 * ax1by1 * bx1ay2 * ax1by2 * ax1by2 * bx1cy2 * ax1dy2
				+ -72 * ax1by1 * bx1ay2 * ax1by2 * bx1by2 * ax1cy2 * ax1dy2
				+ 48 * ax1by1 * ax1by1 * ax1ay2 * ax1cy2 * bx1cy2 * ax2dy2
				+ -6 * ax1by1 * bx1ay2 * bx1ay2 * ax1by2 * ax1dy2 * ax1dy2
				+ -36 * ax1by1 * ax1ay2 * bx1ay2 * bx1by2 * ax1dy2 * ax1dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * ax2dy2
				+ -6 * ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * bx1ay2 * bx1by2
				+ 12 * ax1cy1 * ax1cy1 * bx1ay2 * ax1by2 * ax1by2 * bx1cy2
				+ -30 * ax1by1 * ax1by1 * ax1cy1 * ax1ay2 * cx1ay2 * dx1by2
				+ 36 * ax1by1 * ax1by1 * ax1ay2 * ax1by2 * bx1by2 * dx1cy2 + 12
				* ax1by1 * bx1dy1 * bx1ay2 * ax1by2 * ax1by2 * ax1cy2 + 36
				* ax1by1 * ax1by1 * dx1ay2 * ax1by2 * ax1by2 * bx1cy2 + 72
				* ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * bx1by2 * ax1cy2 + -12
				* ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * ax1cy2 * dx1cy2 + 48
				* ax1by1 * ax1by1 * ax1ay2 * dx1ay2 * ax1cy2 * bx1cy2 + 12
				* ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1by2 * ax1dy2 + 36
				* ax1by1 * ax1by1 * bx1ay2 * dx1ay2 * ax1by2 * ax1dy2 + 36
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * dx1by2 * bx1dy2 + 72
				* ax1by1 * bx1dy1 * ax1ay2 * bx1ay2 * ax1by2 * ax1dy2 + -6
				* ax1by1 * ax1by1 * ax1dy1 * ax1ay2 * dx1ay2 * bx1by2 + -18
				* ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * dx1by2 + -36
				* ax1by1 * ax1dy1 * bx1dy1 * ax1ay2 * bx1ay2 * ax1by2 + -12
				* ax1cy1 * ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1by2 + -4
				* ax1by1 * ax1by1 * ax1by2 * bx1by2 * bx1by2 * cx1by2 + 30
				* ax1by1 * ax1by1 * ax1ay2 * cx1by2 * cx1by2 * ax1cy2 + -60
				* ax1by1 * ax1by1 * ax1ay2 * ax1by2 * cx1by2 * cx1cy2 + 18
				* ax1by1 * ax1by1 * cx1ay2 * ax1by2 * ax1by2 * cx1cy2 + 30
				* ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * ax1by2 * ax1dy2 + -60
				* ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * ax1by2 * cx1dy2 + 18
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1by2 * cx1dy2 + 12
				* ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * cx1by2 + -2
				* ax1by1 * ax1cy1 * ax1by2 * bx1by2 * bx1by2 * bx1by2 + -2
				* ax1cy1 * bx1cy1 * ax1by2 * ax1by2 * ax1by2 * bx1by2 + 24
				* ax1by1 * ax1cy1 * bx1ay2 * ax1by2 * cx1by2 * ax1cy2 + 12
				* bx1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 + 36
				* ax1by1 * ax1cy1 * ax1ay2 * bx1ay2 * ax1cy2 * cx1cy2 + 24
				* ax1by1 * ax1cy1 * ax1ay2 * cx1ay2 * bx1by2 * ax1dy2 + -24
				* ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * cx1ay2 * bx1by2 + 12
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax2by2 * ax1cy2 + 18
				* ax1cy1 * ax1cy1 * cx1ay2 * ax1by2 * ax1by2 * ax1cy2 + 6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax2by2 * ax1dy2 + 18
				* ax1cy1 * ax1cy1 * ax1ay2 * cx1ay2 * ax1by2 * ax1dy2 + -6
				* ax1cy1 * ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * ax2by2 + -18
				* ax1cy1 * ax1cy1 * ax1dy1 * ax1ay2 * cx1ay2 * ax1by2 + -24
				* ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * bx1dy2 + -12
				* ax1cy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * bx1dy2 + -72
				* ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * cx1dy2 + 72
				* ax1by1 * ax1ay2 * ax1by2 * ax1by2 * cx1cy2 * ax1dy2 + 72
				* ax1by1 * ax1ay2 * ax1ay2 * ax1cy2 * cx1cy2 * ax1dy2 + -72
				* ax1by1 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * cx1cy2 + -72
				* ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * cx1cy2 + 36
				* ax1by1 * ax1ay2 * ax1ay2 * cx1by2 * ax1dy2 * ax1dy2 + 72
				* ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1dy2 * bx2dy2 + 72
				* ax1cy1 * ax1ay2 * ax1ay2 * bx1by2 * ax1dy2 * ax1dy2 + -72
				* ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * cx1by2 * ax1dy2 + 36
				* ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * cx1by2 + 144
				* ax1by1 * ax1cy1 * ax1by2 * ax2by2 * ax1cy2 * ax1dy2 + 72
				* ax1by1 * cx1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2 + 144
				* ax1cy1 * bx1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2 + 72
				* ax1cy1 * ax1ay2 * bx1ay2 * ax1cy2 * ax1cy2 * ax1dy2 + 72
				* ax1by1 * ax1ay2 * cx1ay2 * ax1by2 * ax1dy2 * ax1dy2 + -72
				* ax1cy1 * ax1dy1 * bx1ay2 * ax1by2 * ax1by2 * ax1cy2 + -144
				* ax1cy1 * ax1dy1 * ax1ay2 * bx1ay2 * ax1by2 * ax1dy2 + 72
				* ax1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * bx1ay2 * ax1by2 + -144
				* ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * dx1by2 * ax1cy2 + -72
				* ax1by1 * cx1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 + -72
				* ax1by1 * ax1cy1 * ax1ay2 * dx1ay2 * ax1cy2 * ax1cy2 + -144
				* ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * dx1dy2 + -72
				* ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1dy2 + 72
				* ax1by1 * ax1dy1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1by2;
		coeffs[9] = -20 * ax1dy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2
				+ 60 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2
				+ 180 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2
				+ 60 * ax1dy1 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2
				+ -60 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2
				+ -360 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2
				+ -60 * ax1dy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -120 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1dy2
				+ -180 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ -30 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2
				+ 20 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2 * ax1dy2
				+ 180 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 60 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 60 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ 180 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 30 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 30 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2
				+ 20 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ -4 * ax1by1 * ax1by1 * bx1dy1 * bx1ay2 * bx1ay2 * bx1ay2
				+ 4 * ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1ay2 * bx1dy2
				+ 12 * ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1by2 * bx1cy2
				+ 4 * ax1by1 * ax1by1 * bx1ay2 * bx1by2 * bx1by2 * bx1by2
				+ -4 * bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2
				+ 8 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2
				+ -4 * bx1cy1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2
				+ -20 * ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1dy2 * bx1dy2
				+ 8 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1dy2
				+ 12 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1dy2
				+ -20 * ax1by1 * bx1dy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2
				+ -8 * ax1cy1 * ax1cy1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1ay2
				+ 36 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1by2 * bx1cy2
				+ 6 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * cx1cy2
				+ -36 * ax1by1 * ax1ay2 * ax1ay2 * ax1by2 * bx1cy2 * bx1dy2
				+ -96 * ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2
				+ 96 * ax1by1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * cx1dy2
				+ -48 * ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2
				+ 36 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * ax1by2 * bx1by2
				+ 8 * ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * cx1by2
				+ -36 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * bx1by2 * bx1dy2
				+ -18 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * bx1cy2 * bx1cy2
				+ -20 * ax1by1 * ax1ay2 * ax1ay2 * ax1cy2 * bx1cy2 * bx1cy2
				+ -56 * ax1by1 * cx1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2
				+ 56 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * cx1dy2
				+ 144 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * cx1cy2
				+ 36 * ax1by1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * cx1cy2
				+ -16 * ax1cy1 * bx1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2
				+ 20 * bx1cy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 24 * ax1by1 * ax1ay2 * cx1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 8 * ax1by1 * bx1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1ay2
				+ 4 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * bx1ay2 * cx1ay2
				+ 2 * ax1cy1 * ax1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * cx1ay2
				+ 6 * ax1by1 * ax1cy1 * ax1cy1 * ax1ay2 * cx1ay2 * cx1ay2
				+ -6 * ax1by1 * ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * cx1ay2
				+ -8 * ax1by1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1ay2 * bx1dy2
				+ -4 * ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * cx1ay2 * bx1dy2
				+ 24 * ax1by1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1by2 * bx1cy2
				+ -10 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * bx1by2 * bx1cy2
				+ -24 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * cx1ay2
				+ 20 * ax1by1 * ax1by1 * cx1dy1 * ax1ay2 * bx1ay2 * bx1ay2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1ay2 * cx1dy2
				+ -20 * ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * bx1ay2 * cx1dy2
				+ 84 * ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * ax1by2 * cx1cy2
				+ 24 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * cx1by2 * ax1cy2
				+ -4 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * cx1cy2
				+ 2 * ax1by1 * ax1by1 * bx1cy1 * bx1ay2 * bx1ay2 * cx1ay2
				+ -6 * ax1by1 * ax1by1 * ax1ay2 * bx1by2 * bx1by2 * cx1by2
				+ -10 * ax1by1 * ax1by1 * cx1ay2 * ax1by2 * bx1by2 * bx1by2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * ax1by2 * cx1by2 * cx1by2
				+ 10 * ax1cy1 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * bx1cy2
				+ 6 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx1by2 * bx2cy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * dx1ay2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax2dy2
				+ -2 * ax1cy1 * ax1cy1 * ax1cy1 * ax1by2 * ax1by2 * ax2by2
				+ 2 * ax1by1 * ax1cy1 * bx1cy1 * ax1ay2 * bx1ay2 * cx1ay2
				+ 4 * ax1by1 * ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * cx1ay2
				+ -8 * ax1by1 * ax1cy1 * ax1ay2 * bx1by2 * bx1by2 * bx1by2
				+ -18 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx2cy2
				+ -36 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1by2 * ax1cy2
				+ -36 * ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1by2 * ax2cy2
				+ -36 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1by2 * ax1cy2
				+ 8 * ax1by1 * ax1by1 * ax1by1 * bx1by2 * cx1by2 * ax2by2
				+ 24 * ax1by1 * bx1cy1 * bx1ay2 * bx1ay2 * ax1by2 * ax1cy2
				+ -16 * ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2by2 * ax2cy2
				+ 6 * ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1ay2 * ax2dy2
				+ 18 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * bx1ay2 * ax2dy2
				+ 6 * ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1ay2 * dx1ay2
				+ 18 * ax1by1 * ax1by1 * bx1cy1 * ax1ay2 * bx1ay2 * dx1ay2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * bx2cy2 * bx2cy2
				+ 24 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2cy2 * bx2dy2
				+ -12 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2by2 * ax2cy2
				+ 14 * ax1by1 * ax1by1 * ax1by1 * ax1cy2 * ax2cy2 * ax2cy2
				+ 18 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2dy2 * ax2dy2
				+ -24 * ax1by1 * ax1by1 * ax1by1 * ax1dy1 * ax2by2 * ax2cy2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * dx1ay2 * ax2dy2
				+ 18 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * dx1ay2 * dx1ay2
				+ -24 * ax1by1 * ax1by1 * ax1ay2 * bx1by2 * ax2cy2 * ax1dy2
				+ -12 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1by2 * cx2dy2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * ax2by2 * bx1cy2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * bx1by2 * bx1cy2
				+ -12 * ax1by1 * bx1ay2 * ax1by2 * ax1by2 * ax1cy2 * bx1cy2
				+ 6 * ax1by1 * bx1ay2 * ax1by2 * bx1by2 * ax1cy2 * ax1cy2
				+ -18 * ax1by1 * ax1by1 * bx1by2 * ax2by2 * ax1cy2 * ax1cy2
				+ -36 * ax1by1 * bx1ay2 * ax1by2 * bx1by2 * ax1cy2 * ax1cy2
				+ 24 * ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx1by2 * ax2dy2
				+ -4 * ax1by1 * bx1ay2 * ax1by2 * ax1by2 * ax1by2 * bx1dy2
				+ -12 * ax1by1 * ax1by1 * ax1by2 * bx1by2 * ax2by2 * ax1dy2
				+ 16 * ax1by1 * ax1by1 * bx1ay2 * ax1cy2 * ax1cy2 * ax2cy2
				+ -12 * ax1by1 * bx1ay2 * bx1ay2 * ax1by2 * ax1cy2 * ax1dy2
				+ -72 * ax1by1 * ax1ay2 * bx1ay2 * bx1by2 * ax1cy2 * ax1dy2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * ax1dy2 * ax2dy2
				+ 12 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1by2 * ax2cy2
				+ -24 * ax1by1 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax2dy2
				+ 12 * ax1by1 * ax1by1 * dx1ay2 * ax1by2 * ax1by2 * bx1by2
				+ 4 * ax1by1 * bx1dy1 * bx1ay2 * ax1by2 * ax1by2 * ax1by2
				+ 12 * ax1by1 * ax1by1 * ax1ay2 * ax1by2 * bx1by2 * dx1by2
				+ 12 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1by2 * ax1cy2
				+ 36 * ax1by1 * ax1by1 * bx1ay2 * dx1ay2 * ax1by2 * ax1cy2
				+ 36 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * dx1by2 * bx1cy2
				+ 72 * ax1by1 * bx1dy1 * ax1ay2 * bx1ay2 * ax1by2 * ax1cy2
				+ 28 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * dx1ay2 * ax1dy2
				+ -24 * ax1by1 * ax1by1 * ax1dy1 * ax1ay2 * bx1ay2 * dx1ay2
				+ 30 * ax1by1 * ax1by1 * cx1ay2 * cx1ay2 * ax1by2 * ax1cy2
				+ -60 * ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * ax1by2 * cx1cy2
				+ 18 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1by2 * cx1cy2
				+ 4 * ax1cy1 * bx1cy1 * bx1ay2 * ax1by2 * ax1by2 * ax1by2
				+ 18 * ax1by1 * ax1cy1 * bx1ay2 * ax1by2 * ax1by2 * cx1by2
				+ -6 * ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * bx1by2 * cx1by2
				+ 24 * ax1by1 * ax1cy1 * ax1ay2 * cx1ay2 * bx1by2 * ax1cy2
				+ 4 * ax1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1dy2
				+ -6 * ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * cx1ay2 * bx1dy2
				+ 18 * ax1by1 * ax1cy1 * ax1ay2 * bx1ay2 * cx1ay2 * ax1dy2
				+ -4 * ax1cy1 * bx1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2
				+ 6 * ax1by1 * ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * cx1ay2
				+ -18 * ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * bx1ay2 * cx1ay2
				+ 6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax2by2 * ax1cy2
				+ 18 * ax1cy1 * ax1cy1 * ax1ay2 * cx1ay2 * ax1by2 * ax1cy2
				+ -24 * bx1cy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1dy2
				+ -32 * ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * bx1dy2
				+ -24 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * bx1dy2
				+ 24 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2 * bx1dy2
				+ -24 * ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1dy2
				+ -72 * ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * cx1cy2
				+ 72 * ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * bx2cy2 * ax1dy2
				+ 144 * ax1by1 * ax1ay2 * ax1ay2 * cx1by2 * ax1cy2 * ax1dy2
				+ -72 * ax1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * bx1by2 * ax1cy2
				+ -24 * ax1by1 * cx1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ 60 * ax1cy1 * bx1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 + 72
				* ax1by1 * ax1cy1 * ax1by2 * ax2by2 * ax1cy2 * ax1cy2 + -72
				* ax1by1 * ax1cy1 * ax1by2 * ax1by2 * ax1cy2 * ax2cy2 + 96
				* ax1cy1 * bx1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1dy2 + 72
				* ax1by1 * ax1cy1 * ax1by2 * ax1by2 * ax2by2 * ax1dy2 + 144
				* ax1cy1 * ax1ay2 * bx1ay2 * ax1by2 * ax1cy2 * ax1dy2 + 36
				* ax1by1 * ax1ay2 * ax1ay2 * cx1ay2 * ax1dy2 * ax1dy2 + -24
				* ax1by1 * ax1dy1 * cx1ay2 * ax1by2 * ax1by2 * ax1by2 + 36
				* ax1by1 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * cx1ay2 + -24
				* ax1cy1 * bx1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 + -72
				* ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * dx1by2 + -144
				* ax1by1 * ax1cy1 * ax1ay2 * dx1ay2 * ax1by2 * ax1cy2 + -72
				* ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 + -48
				* ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2 + -72
				* ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * dx1ay2 * ax1dy2 + -48
				* ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2 + 24
				* ax1cy1 * ax1dy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2;
		coeffs[10] = 60 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2
				+ 90 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2
				+ 4 * ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1ay2 * bx1cy2
				+ -6 * ax1dy1 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1by2
				+ 6 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1dy2
				+ 6 * ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1by2 * bx1by2
				+ 15 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 15 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ 60 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2 * ax1dy2
				+ 60 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 90 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2 * ax1dy2
				+ -120 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2
				+ -120 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2
				+ 120 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2
				+ -180 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2
				+ -180 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2
				+ 180 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 3 * ax1by1 * ax1by1 * ax1cy1 * ax1cy1 * ax2by2 * ax2by2
				+ 24 * ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1cy2 * ax1dy2
				+ 24 * ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * cx1dy2
				+ 24 * ax1by1 * cx1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2
				+ 72 * ax1cy1 * ax1ay2 * ax1ay2 * bx1ay2 * ax1cy2 * ax1dy2
				+ 24 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * cx1cy2
				+ -32 * bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2
				+ 72 * ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * bx1by2 * ax1cy2
				+ -32 * bx1cy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2
				+ -36 * ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2
				+ -48 * ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2
				+ 72 * ax1by1 * ax1cy1 * ax1dy1 * ax1ay2 * ax1by2 * ax2by2
				+ -24 * ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * dx1cy2
				+ -60 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2
				+ -72 * ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * cx1ay2 * ax1cy2
				+ -144 * ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * cx1by2
				+ 8 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2
				+ 36 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * bx1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1by2 * ax2by2
				+ -72 * ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * ax2by2 * ax1dy2
				+ 12 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * cx1by2
				+ 24 * bx1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2
				+ 8 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1cy2
				+ 144 * ax1by1 * ax1ay2 * ax1ay2 * ax1by2 * cx1by2 * ax1dy2
				+ -6 * ax1cy1 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax2cy2
				+ 36 * ax1cy1 * ax1ay2 * ax1ay2 * bx1by2 * ax1cy2 * ax1cy2
				+ -2 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * bx1by2 * bx1by2
				+ -72 * ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * ax1cy2 * ax2cy2
				+ 2 * bx1cy1 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1by2
				+ 24 * bx1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2
				+ 6 * ax1by1 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * cx1by2
				+ 144 * ax1by1 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * cx1cy2
				+ -8 * ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1ay2 * ax2cy2
				+ -8 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1ay2 * ax1cy2
				+ -16 * ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1cy2 * bx1dy2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1cy2 * ax2dy2
				+ -24 * ax1by1 * ax1ay2 * ax1ay2 * bx1ay2 * ax1cy2 * bx1dy2
				+ -16 * ax1by1 * bx1ay2 * ax1by2 * ax1by2 * ax1by2 * bx1cy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2cy2 * ax2dy2
				+ 24 * ax1by1 * ax1by1 * bx1ay2 * ax1by2 * ax1by2 * bx2cy2
				+ 16 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1cy2
				+ -24 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * bx1by2 * bx1cy2
				+ 24 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * dx1ay2 * bx1cy2
				+ 12 * ax1by1 * ax1by1 * ax1by1 * ax1by2 * ax2by2 * bx2cy2
				+ 24 * ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1ay2 * ax1cy2
				+ -16 * ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * bx1ay2 * cx1cy2
				+ -12 * ax1by1 * ax1by1 * ax1by1 * ax1ay2 * dx1ay2 * ax2cy2 + 6
				* ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * ax2cy2 + 24
				* ax1by1 * ax1by1 * ax1by1 * dx1ay2 * ax1by2 * ax2by2 + -24
				* ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2by2 * bx2dy2 + 12
				* ax1by1 * ax1by1 * bx1ay2 * dx1ay2 * ax1by2 * ax1by2 + 12
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1by2 * bx2dy2 + 6
				* ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * bx1by2 * bx1by2 + -6
				* ax1by1 * bx1ay2 * bx1ay2 * ax1by2 * ax1by2 * ax1dy2 + 36
				* ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1by2 * bx1by2 + -36
				* ax1by1 * ax1ay2 * bx1ay2 * ax1by2 * ax1by2 * bx1dy2 + 18
				* ax1by1 * ax1dy1 * bx1ay2 * bx1ay2 * ax1by2 * ax1by2 + -18
				* ax1by1 * ax1ay2 * ax1ay2 * bx1by2 * bx1by2 * ax1dy2 + -10
				* ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2by2 * ax2by2 + 24
				* ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2cy2 * bx2cy2 + -18
				* ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * ax1by2 * cx1by2 + -12
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1cy2 * bx2cy2 + -18
				* ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * bx1by2 * bx1by2 + -6
				* ax1by1 * bx1ay2 * bx1ay2 * ax1by2 * ax1cy2 * ax1cy2 + -12
				* ax1by1 * bx1cy1 * ax1ay2 * bx1ay2 * ax1by2 * bx1by2 + -36
				* ax1by1 * ax1ay2 * bx1ay2 * bx1by2 * ax1cy2 * ax1cy2 + -4
				* ax1by1 * ax1by1 * ax1by1 * bx1cy1 * ax2by2 * ax2by2 + -18
				* ax1by1 * ax1ay2 * ax1ay2 * ax1by2 * bx1cy2 * bx1cy2 + 6
				* ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * bx1by2 * bx1by2 + -8
				* ax1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1cy2 + -6
				* bx1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 + 12
				* ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * bx1ay2 * bx1cy2 + 6
				* ax1cy1 * ax1cy1 * bx1ay2 * bx1ay2 * ax1by2 * ax1by2 + 18
				* ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * cx1ay2 * ax1cy2 + 42
				* ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * ax1by2 * cx1by2 + 6
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1ay2 * cx1cy2;
		coeffs[11] = 60 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2
				+ -120 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1dy2
				+ 4 * ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1ay2 * bx1by2
				+ 6 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2
				+ -30 * ax1dy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1by2
				+ 30 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1dy2
				+ -60 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2
				+ 60 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1dy2 * ax1dy2
				+ 60 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * ax1dy2
				+ 60 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 60 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ -180 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2
				+ 180 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1dy2
				+ 6 * ax1cy1 * bx1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1by2
				+ -24 * ax1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1by2
				+ 4 * bx1cy1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1by2
				+ -24 * ax1by1 * cx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2
				+ 24 * ax1by1 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * cx1by2
				+ -24 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2
				+ 40 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * bx1dy2
				+ -72 * ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * cx1ay2 * ax1by2
				+ 24 * ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1by2 * ax1dy2 + 8
				* bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 + 56
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * cx1dy2 + 2
				* ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1by2 + -72
				* ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax2dy2 + 6
				* ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * cx1ay2 * ax1by2 + 36
				* ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * bx1cy2 + 20
				* bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 + -72
				* ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * ax2by2 * ax1cy2 + 60
				* ax1by1 * ax1ay2 * ax1ay2 * cx1ay2 * ax1cy2 * ax1cy2 + 24
				* bx1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 + 24
				* ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1cy2 * ax2cy2 + 144
				* ax1by1 * ax1ay2 * ax1ay2 * ax1by2 * cx1by2 * ax1cy2 + -8
				* ax1by1 * ax1by1 * ax1by1 * bx1ay2 * cx1ay2 * ax2by2 + -8
				* ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * bx1ay2 * ax1by2 + 16
				* ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1by2 + -16
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1by2 * bx1dy2 + 24
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * dx1ay2 * bx1by2 + 24
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1by2 * ax2dy2 + 24
				* ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * bx1ay2 * ax1by2 + -24
				* ax1by1 * ax1ay2 * ax1ay2 * bx1ay2 * ax1by2 * bx1dy2 + -12
				* ax1by1 * ax1by1 * ax1by1 * ax1ay2 * dx1ay2 * ax2by2 + -12
				* ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2by2 * ax2dy2 + -2
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1cy2 * bx1cy2 + -16
				* ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * bx1ay2 * cx1by2 + 6
				* ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * ax1cy2 * ax2cy2 + 6
				* ax1by1 * ax1by1 * ax1cy1 * bx1ay2 * bx1ay2 * ax2by2 + -18
				* ax1by1 * ax1ay2 * ax1ay2 * bx1ay2 * ax1cy2 * bx1cy2 + -24
				* ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2by2 * bx2cy2 + 4
				* ax1by1 * ax1by1 * ax1by2 * ax1by2 * bx1by2 * ax2by2 + 12
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1by2 * bx2cy2 + -10
				* ax1by1 * ax1ay2 * ax1by2 * ax1by2 * bx1by2 * bx1by2 + -6
				* ax1by1 * bx1ay2 * bx1ay2 * ax1by2 * ax1by2 * ax1cy2 + -8
				* ax1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1by2 + -36
				* ax1by1 * ax1ay2 * bx1ay2 * ax1by2 * ax1by2 * bx1cy2 + 12
				* ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * bx1ay2 * bx1by2 + -18
				* ax1by1 * ax1ay2 * ax1ay2 * bx1by2 * bx1by2 * ax1cy2 + 18
				* ax1by1 * ax1by1 * ax1ay2 * cx1ay2 * cx1ay2 * ax1by2 + 6
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1ay2 * cx1by2;
		coeffs[12] = 15 * ax1dy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2
				+ -30 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2
				+ ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1by2
				+ ax1by1 * ax1by1 * bx1ay2 * bx1ay2 * bx1ay2 * bx1ay2
				+ 15 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2 * ax1dy2
				+ 20 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 * ax1cy2
				+ 30 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2
				+ -60 * ax1dy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1by2
				+ 60 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1dy2
				+ 90 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 * ax1cy2
				+ -120 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2
				+ 120 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * ax1dy2
				+ 3 * ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1ay2 * cx1ay2
				+ 20 * bx1cy1 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2
				+ 4 * bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2
				+ -30 * ax1cy1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2
				+ 6 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * bx1dy2
				+ -24 * ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * dx1ay2
				+ 24 * ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1ay2 * ax1dy2
				+ 2 * ax1cy1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1ay2
				+ 40 * ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * bx1cy2
				+ 20 * bx1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 + 24
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1by2 * ax1cy2 + 60
				* ax1by1 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * cx1by2 + 56
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * cx1cy2 + -24
				* ax1by1 * ax1cy1 * ax1ay2 * ax1by2 * ax1by2 * ax2by2 + -72
				* ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax2cy2 + 6
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1ay2 * ax2dy2 + -10
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1ay2 * bx1dy2 + 6
				* ax1by1 * ax1dy1 * ax1ay2 * ax1ay2 * bx1ay2 * bx1ay2 + -2
				* ax1by1 * bx1ay2 * bx1ay2 * ax1by2 * ax1by2 * ax1by2 + 4
				* ax1by1 * bx1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1ay2 + -6
				* ax1by1 * ax1by1 * ax1ay2 * ax1by2 * bx1by2 * ax2by2 + -6
				* ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * bx1ay2 * cx1ay2 + -18
				* ax1by1 * ax1ay2 * bx1ay2 * ax1by2 * ax1by2 * bx1by2 + -2
				* ax1by1 * bx1cy1 * ax1ay2 * ax1ay2 * bx1ay2 * bx1ay2 + -16
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1by2 * bx1cy2 + -3
				* bx1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 + 24
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1by2 * ax2cy2 + 6
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * cx1ay2 * cx1ay2 + -24
				* ax1by1 * ax1ay2 * ax1ay2 * bx1ay2 * ax1by2 * bx1cy2 + 4
				* ax1cy1 * bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1ay2 + -12
				* ax1by1 * ax1by1 * ax1by1 * ax1ay2 * ax2by2 * ax2cy2;
		coeffs[13] = -30 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2
				+ -60 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 + 6
				* ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1by2 * ax1by2 + 30
				* ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * ax1dy2 + 60
				* ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1dy2 + 60
				* ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 * ax1cy2 + 60
				* ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1cy2 + 4
				* bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 + 20
				* bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 + 6
				* ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * bx1cy2 + 60
				* ax1by1 * ax1ay2 * ax1ay2 * cx1ay2 * ax1by2 * ax1by2 + 24
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1ay2 * ax1cy2 + 24
				* ax1by1 * ax1cy1 * ax1ay2 * ax1ay2 * ax1by2 * ax2by2 + -2
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1by2 * bx1by2 + 6
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1ay2 * ax2cy2 + 6
				* ax1by1 * ax1by1 * ax1ay2 * bx1ay2 * ax1by2 * ax2by2 + -10
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1ay2 * bx1cy2 + -18
				* ax1by1 * ax1ay2 * ax1ay2 * bx1ay2 * ax1by2 * bx1by2;
		coeffs[14] = -30 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2
				+ 30 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1dy2 + 15
				* ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2 * ax1cy2 + 15
				* ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 * ax1by2 + 60
				* ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1cy2 + 6
				* ax1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * bx1by2 + 4
				* bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 + 24
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * cx1ay2 * ax1by2 + 6
				* ax1by1 * ax1by1 * ax1ay2 * ax1ay2 * bx1ay2 * ax2by2 + -10
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1ay2 * bx1by2;
		coeffs[15] = -6 * ax1dy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2
				+ 6 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1dy2 + 20
				* ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2 * ax1by2 + 30
				* ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1cy2 + 2
				* bx1cy1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 + -2
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * bx1ay2 * bx1ay2 + 6
				* ax1by1 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * cx1ay2;
		coeffs[16] = 6 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1cy2
				+ 15 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2 * ax1by2;
		coeffs[17] = 6 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1by2;
		coeffs[18] = ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2 * ax1ay2;

		double max = coeffs[0];
		for (int a = 0; a < coeffs.length; a++) {
			if (Math.abs(coeffs[a]) > max) {
				max = Math.abs(coeffs[a]);
			}
		}
		StringBuffer sb = new StringBuffer();
		for (int a = 0; a < coeffs.length; a++) {
			coeffs[a] = coeffs[a] / max;
			if (sb.length() > 0)
				sb.append("+");
			sb.append(coeffs[a] + "*(x^" + a + ")");
		}
		// you can plug this equation into Grapher to see it yourself
		// System.out.println(sb);

		double[] t = findSolutions(coeffs, 0, 1, .0001);
		Point2D[] p = new Point2D.Double[t.length];

		for (int a = 0; a < p.length; a++) {
			p[a] = new Point2D.Double(((x2[0] * t[a] + x2[1]) * t[a] + x2[2])
					* t[a] + x2[3], ((y2[0] * t[a] + y2[1]) * t[a] + y2[2])
					* t[a] + y2[3]);
		}

		return p;
	}
}
