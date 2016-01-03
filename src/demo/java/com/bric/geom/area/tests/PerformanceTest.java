/*
 * @(#)PerformanceTest.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
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
package com.bric.geom.area.tests;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.Random;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import com.bric.geom.AreaX;
import com.bric.inspector.InspectorGridBagLayout;

public class PerformanceTest extends BasicTestElement {
	
	@Override
	public String getDescription() {
		return "This creates several identical randomly placed shapes and " +
			"combines them.  The test should automatically trigger and end the Shark profiler " +
			"to measure results.";
	}

	JLabel typeLabel = new JLabel("Type:");
	JComboBox type = new JComboBox();
	JLabel repeatLabel = new JLabel("Repeat:");
	JSpinner repeat = new JSpinner(new SpinnerNumberModel(5,1,1000,1));
	JLabel additionsLabel = new JLabel("Additions:");
	JSpinner additions = new JSpinner(new SpinnerNumberModel(100,1,1000,1));
	
	public PerformanceTest() {
		type.addItem("Linear");
		type.addItem("Quadratic");
		type.addItem("Cubic");
	}

	@Override
	public void addControls(InspectorGridBagLayout layout) {
		layout.addRow(typeLabel, type, false);
		layout.addRow(additionsLabel, additions, false);
		layout.addRow(repeatLabel, repeat, false);
	}
	
	@Override
	public String getName() {
		return "Performance Test";
	}

	@Override
	public void doTest() {
		try {
			Robot robot = new Robot();
			for(int a = 5; a>=1; a--) {
				if(a==5) {
					System.out.println("Starting Shark in "+a+"...");
				} else {
					System.out.println(a+"...");
				}
				delay(1000);
			}
			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(KeyEvent.VK_ESCAPE);
			robot.keyRelease(KeyEvent.VK_ALT);
			robot.keyRelease(KeyEvent.VK_ESCAPE);
	
			for(int a = 5; a>=1; a--) {
				if(a==5) {
					System.out.println("Running test in "+a+"...");
				} else {
					System.out.println(a+"...");
				}
				delay(1000);
			}
			
			testAdditions( ((Number)repeat.getValue()).intValue() ,((Number)additions.getValue()).intValue());
			
			robot.keyPress(KeyEvent.VK_ALT);
			robot.keyPress(KeyEvent.VK_ESCAPE);
			robot.keyRelease(KeyEvent.VK_ALT);
			robot.keyRelease(KeyEvent.VK_ESCAPE);
			System.out.println("Done.");
		} catch(AWTException e) {
			RuntimeException e2 = new RuntimeException();
			e2.initCause(e);
			throw e2;
		}
	}
		
	public void testAdditions(int iterations,int additions) {
		Random random = new Random(0);
		long[] times = new long[iterations];
		for(int a = 0; a<iterations; a++) {
			random.setSeed(0);
			AreaX sum = new AreaX();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<additions; b++) {
				if(cancelled) return;
				float percent = ((float)a)/((float)iterations);
				percent = percent + 1f/(iterations)*(b)/(additions);
				progress.setValue( (int)(percent*(progress.getMaximum()-progress.getMinimum()))+progress.getMinimum() );
				
				double x = 1000*random.nextDouble();
				double y = 1000*random.nextDouble();
				Shape shape;
				if(type.getSelectedIndex()==0) {
					shape = createDiamond((float)x, (float)y);
				} else if(type.getSelectedIndex()==1) {
					shape = createQuad((float)x, (float)y);
				} else { //use cubics
					shape = new Ellipse2D.Double(x, y, 30, 30);
				}
				AreaX k = new AreaX(shape);
				sum.add(k);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		Arrays.sort(times);
		System.out.println("Median Time: "+times[times.length/2]+" ms");
	}
}
