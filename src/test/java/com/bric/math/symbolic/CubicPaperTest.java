/*
 * @(#)CubicPaperTest.java
 *
 * $Date: 2014-11-27 07:56:11 +0100 (Do, 27 Nov 2014) $
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
package com.bric.math.symbolic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import junit.framework.TestCase;

import org.junit.Test;

public class CubicPaperTest extends TestCase {
	static int TRIAL_COUNT = 10;

	@Test
	public void testPartialCubicRootElimination() {
		Random r = new Random();
		for(int trial = 0; trial<TRIAL_COUNT; trial++) {
			double c1 = 100*(r.nextDouble()-.5);
			double k = 100*(r.nextDouble()-.5);
			double c2 = 100*(r.nextDouble()-.5);
			
			double z = c1*Math.pow(k, 1.0/3.0)+c2*Math.pow(k, 2.0/3.0);
			double z2 = (z*z*z-c1*c1*c1*k-c2*c2*c2*k*k)/(3*c1*c2*k);
			assertEquals("trial="+trial, z, z2, .00001);
		}
	}
	
	@Test
	public void testExpressionPowers() {
		Expression e = new Expression(new Term("a"), new Term("b"));
		assertEquals(e.multiply(e), new Expression(
				new Term("a", "a"),
				new Term(2, "a", "b"),
				new Term("b", "b")));
		assertEquals(e.multiply(e).multiply(e), new Expression(
				new Term("a", "a", "a"),
				new Term(3, "a", "a", "b"),
				new Term(3, "a", "b", "b"),
				new Term("b", "b", "b")));

		e = new Expression(new Term("a"), new Term(-1, "b"));
		assertEquals(e.multiply(e), new Expression(
				new Term("a", "a"),
				new Term(-2, "a", "b"),
				new Term("b", "b")));
		assertEquals(e.multiply(e).multiply(e), new Expression(
				new Term("a", "a", "a"),
				new Term(-3, "a", "a", "b"),
				new Term(3, "a", "b", "b"),
				new Term(-1, "b", "b", "b")));
	}
	
	@Test
	public void testTermDivision() {
		Term a = new Term(4, "a", "a", "b", "b", "c", "c");
		Term k = new Term(2, "a", "c", "c");
		assertEquals( a.divideBy(k).toString(), "2*a*b^2");
		assertEquals( a.divideBy(k).divideBy(k).toString(), "b^2/(c^2)");
	}
	
	@Test
	public void testExpressionSubstitution() throws IllegalSubstitutionException {
		//a^2+a+b
		Expression e = new Expression(
			new Term("a", "a"),
			new Term("a"),
			new Term("b")
		);
		Expression a = new Expression(
			new Term(1),
			new Term(-1, "z")
		);
		//(1-z)^2+(1-z)+b = (1 - 2z + z^2)+(1-z)+b = 2-3z+z^2+b
		assertEquals( e.substitute("a", a), 
				//"2-3*z+z^2+b"
				new Expression(new Term(2), new Term(-3,"z"), new Term("z", "z"), new Term("b")));
		
		Expression complex = new Expression(
				new Term("a", "a", "a", "b"),
				new Term("a", "a", "c"),
				new Term("a")
		);
		a = new Expression( new Term(5) );
		e = complex.substitute("a", a);
		assertEquals(e.toString(), "125*b+25*c+5");
	}
	
	static int dataSetIDCtr = 1;
	
	class DataSet {
		double ax1, bx1, cx1, dx1, ay1, by1, cy1, dy1;
		double ax2, bx2, cx2, dx2, ay2, by2, cy2, dy2;
		double t1, t2;
		double x, y;
		final int id = dataSetIDCtr++;
		
		public DataSet(
				double ax1, double bx1, double cx1, double dx1,
				double ay1, double by1, double cy1, double dy1, double ax2,
				double bx2, double cx2, double dx2, double ay2, double by2,
				double cy2, double dy2, double t1, double t2, double x, double y) {
			this.ax1 = ax1;
			this.bx1 = bx1;
			this.cx1 = cx1;
			this.dx1 = dx1;
			this.ay1 = ay1;
			this.by1 = by1;
			this.cy1 = cy1;
			this.dy1 = dy1;
			this.ax2 = ax2;
			this.bx2 = bx2;
			this.cx2 = cx2;
			this.dx2 = dx2;
			this.ay2 = ay2;
			this.by2 = by2;
			this.cy2 = cy2;
			this.dy2 = dy2;
			this.t1 = t1;
			this.t2 = t2;
			this.x = x;
			this.y = y;
		}
		
		private Map<String, Number> createMap(boolean invert) {
			Map<String, Number> m = new HashMap<String, Number>();
			if(invert) {
				m.put("a_{x1}", ax1);
				m.put("b_{x1}", bx1);
				m.put("c_{x1}", cx1);
				m.put("d_{x1}", dx1);
				m.put("a_{y1}", ay1);
				m.put("b_{y1}", by1);
				m.put("c_{y1}", cy1);
				m.put("d_{y1}", dy1);

				m.put("a_{x2}", ax2);
				m.put("b_{x2}", bx2);
				m.put("c_{x2}", cx2);
				m.put("d_{x2}", dx2);
				m.put("a_{y2}", ay2);
				m.put("b_{y2}", by2);
				m.put("c_{y2}", cy2);
				m.put("d_{y2}", dy2);

				m.put("t_1", t1);
				m.put("t_2", t2);
			} else {
				m.put("a_{x1}", ax2);
				m.put("b_{x1}", bx2);
				m.put("c_{x1}", cx2);
				m.put("d_{x1}", dx2);
				m.put("a_{y1}", ay2);
				m.put("b_{y1}", by2);
				m.put("c_{y1}", cy2);
				m.put("d_{y1}", dy2);

				m.put("a_{x2}", ax1);
				m.put("b_{x2}", bx1);
				m.put("c_{x2}", cx1);
				m.put("d_{x2}", dx1);
				m.put("a_{y2}", ay1);
				m.put("b_{y2}", by1);
				m.put("c_{y2}", cy1);
				m.put("d_{y2}", dy1);

				m.put("t_1", t2);
				m.put("t_2", t1);
			}
			return m;
		}
	}
	
	/** TODO: implement a test that uses these real-world data sets.
	 * 
	 * @throws MissingVariablesException
	 * @throws ImaginaryException
	 */
	@Test
	public void testDataSets() throws MissingVariablesException, ImaginaryException {
		List<DataSet> dataSets = new ArrayList<DataSet>();
		
		//These data sets were physically observed using CurveIntersectionApp
		

		dataSets.add(new DataSet(
			1195.0, -1689.0, 873.0, 92.0, 9.0, -798.0, 831.0, 61.0,
			-554.0, 78.0, 246.0, 269.0, -99.0, 801.0, -714.0, 339.0,
			0.27885548052924003, 0.8029518185592963,
			230.01582422564934, 230.87127703160897 ));

		dataSets.add(new DataSet(
			1195.0, -1689.0, 873.0, 92.0, 9.0, -798.0, 831.0, 61.0,
			-554.0, 78.0, 246.0, 269.0, -99.0, 801.0, -714.0, 339.0,
			0.7986784334073767, 0.21835946730816352,
			320.6675279447781, 220.25296168319338 ));

		dataSets.add(new DataSet(
			1195.0, -1689.0, 873.0, 92.0, 9.0, -798.0, 831.0, 61.0,
			-554.0, 78.0, 246.0, 269.0, -99.0, 801.0, -714.0, 339.0,
			0.7986784334073767, 0.21835946730816352,
			320.6675279447781, 220.25296168319338 ));


		dataSets.add(new DataSet(
			1195.0, -1689.0, 873.0, 92.0, 9.0, -798.0, 831.0, 61.0,
			-1853.0, 2676.0, -1053.0, 343.0, 714.0, -825.0, 99.0, 256.0,
			0.35382017273804467, 0.14139505144884487,
			242.37295148127728, 255.52261789922153 ));
		
		dataSets.add(new DataSet(
				1195.0, -1689.0, 873.0, 92.0, 9.0, -798.0, 831.0, 61.0,
				-1853.0, 2676.0, -1053.0, 343.0, 714.0, -825.0, 99.0, 256.0,
				0.2504342235579724, 0.3546039215667196,
				223.4688942121081, 219.20379313976972 ));
		
		dataSets.add(new DataSet(
				1195.0, -1689.0, 873.0, 92.0, 9.0, -798.0, 831.0, 61.0,
				-1853.0, 2676.0, -1053.0, 343.0, 714.0, -825.0, 99.0, 256.0,
				0.2054278058006983, 0.9021527300986165,
				210.42135114244337, 198.11246362955606 ));
		
		dataSets.add(new DataSet(
				1195.0, -1689.0, 873.0, 92.0, 9.0, -798.0, 831.0, 61.0,
				-1853.0, 2676.0, -1053.0, 343.0, 714.0, -825.0, 99.0, 256.0,
				0.6832255166165664, 0.07087074369718083,
				281.15415770000175, 259.12666252523576 ));
		
	}
}
