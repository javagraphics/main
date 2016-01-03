/*
 * @(#)MathGDemo.java
 *
 * $Date: 2015-03-16 04:13:26 +0100 (Mo, 16 Mär 2015) $
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
package com.bric.math;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Random;

import com.bric.blog.Blurb;
import com.bric.swing.BasicConsole;
import com.bric.util.JVM;

/** This runs tests compared some {@link MathG} functions to Math functions.
 * TODO: check new 3rd party packages come up to defer towards instead?
 * TODO: use a proper 2D table to display results
 */
@Blurb (
filename = "MathG",
title = "Performance: Studying Math",
releaseDate = "May 2009",
summary = "The <code><a href=\"http://java.sun.com/javase/6/docs/api/java/lang/Math.html\">java.lang.Math</a></code> "+
"class was designed with accuracy in mind.\n"+
"<p>But if you're dealing with graphics: you can probably fudge on some accuracy in exchange for speed.  What methods can be improved?",
link = "http://javagraphics.blogspot.com/2009/05/math-studying-performance.html",
sandboxDemo = true
)
public class MathGDemo {

	/** Runs some tests comparing Math and MathG.
     * @param args the application's arguments. (This is unused.)
	 */
    public static void main(String[] args) {
		BasicConsole console = BasicConsole.create("MathGDemo", false, true, true);
		PrintStream out = console.createPrintStream(false);
		PrintStream err = console.createPrintStream(true);
		
		try {
			out.println(JVM.getProfile());
			out.println("Running comparison of Math vs MathG on "+System.getProperty("os.name")+" "+System.getProperty("os.version")+", Java "+System.getProperty("java.version"));
	    	//testIncreasingMax();
	    	testEverything(false, out);
		} catch(Throwable t) {
			t.printStackTrace(err);
		} finally {
	    	out.println("Done.");
		}
    }

    @SuppressWarnings("unused")
	private static void testEverything(boolean showBiggestError,PrintStream out) {
		out.println("\tCalling testEverything()");
    	long[] times = new long[200];
		double[] values = new double[1000000];
		double[] smallValues = new double[1000000];
		Random random = new Random(0);
		for(int a = 0; a<values.length; a++) {
			if(false) { //only positive numbers
				values[a] = (random.nextDouble())*10000;
			} else { //include negative numbers
				values[a] = (random.nextDouble()-.5)*10000*2;
			}
		}
		for(int a = 0; a<smallValues.length; a++) {
			smallValues[a] = random.nextDouble()*2-1;
		}
		
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.sin01(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.sin01() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.sin00004(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.sin00004() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				Math.sin(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMath.sin() median time: "+times[times.length/2]+" ms");
		
		/////////////////////////////
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.cos01(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.cos01() median time: "+times[times.length/2]+" ms");

		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.cos00004(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.cos00004() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				Math.cos(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMath.cos() median time: "+times[times.length/2]+" ms");

		////////////////////////////////
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.acos(smallValues[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.acos() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				Math.acos(smallValues[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMath.acos() median time: "+times[times.length/2]+" ms");

		////////////////////////////////
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.floorDouble(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.floorDouble() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.floorInt(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.floorInt() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				Math.floor(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMath.floorDouble() median time: "+times[times.length/2]+" ms");
		
		/////////////////////////
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.ceilDouble(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.ceilDouble() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.ceilInt(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.ceilInt() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				Math.ceil(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMath.ceilDouble() median time: "+times[times.length/2]+" ms");
		
		///////////////////////////////////

		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.roundDouble(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.roundDouble() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				MathG.roundInt(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMathG.roundInt() median time: "+times[times.length/2]+" ms");
		
		for(int a = 0; a<times.length; a++) {
			Thread.yield();
			times[a] = System.currentTimeMillis();
			for(int b = 0; b<values.length; b++) {
				Math.round(values[b]);
			}
			times[a] = System.currentTimeMillis()-times[a];
		}
		
		Arrays.sort(times);
		out.println("\tMath.round() median time: "+times[times.length/2]+" ms");
	
		/////////////
		
    }
    
    @SuppressWarnings("unused")
	private static void testIncreasingMax(PrintStream out,PrintStream err) {
		out.println("\tCalling testIncreasingMax()");
		double max = 2;
		while(max<2e10) {
			out.println("max: "+max);
			long[] times = new long[200];
			double[] values = new double[1000000];
			Random random = new Random(0);
			for(int a = 0; a<values.length; a++) {
				values[a] = (random.nextDouble()-.5)*max*2;
			}
			
			
			for(int a = 0; a<times.length; a++) {
				Thread.yield();
				times[a] = System.currentTimeMillis();
				for(int b = 0; b<values.length; b++) {
					MathG.sin01(values[b]);
				}
				times[a] = System.currentTimeMillis()-times[a];
			}
			
			Arrays.sort(times);
			out.println("\tMathG.sin01() median time: "+times[times.length/2]+" ms");
			
			for(int a = 0; a<times.length; a++) {
				Thread.yield();
				times[a] = System.currentTimeMillis();
				for(int b = 0; b<values.length; b++) {
					MathG.sin00004(values[b]);
				}
				times[a] = System.currentTimeMillis()-times[a];
			}
			
			Arrays.sort(times);
			out.println("\tMathG.sin00004() median time: "+times[times.length/2]+" ms");
			
			for(int a = 0; a<times.length; a++) {
				Thread.yield();
				times[a] = System.currentTimeMillis();
				for(int b = 0; b<values.length; b++) {
					Math.sin(values[b]);
				}
				times[a] = System.currentTimeMillis()-times[a];
			}
			
			Arrays.sort(times);
			out.println("\tMath.sin() median time: "+times[times.length/2]+" ms");
			
			
			double maxError = 0;
			for(int a = 0; a<values.length; a++) {
				double error = MathG.sin01(values[a])-Math.sin(values[a]);
				if(error<0) error = -error;
				if(error>maxError)
					maxError = error;
			}
			if(maxError>.011) { //this is not supposed to happen!
				err.println("max error for sin01 = "+maxError);
			}
			
			maxError = 0;
			for(int a = 0; a<values.length; a++) {
				double error = MathG.sin00004(values[a])-Math.sin(values[a]);
				if(error<0) error = -error;
				if(error>maxError)
					maxError = error;
			}
			if(maxError>.00004) { //this is not supposed to happen!
				err.println("max error for sin00004 = "+maxError);
			}
	
			max = max*10;
		}
	}
}
