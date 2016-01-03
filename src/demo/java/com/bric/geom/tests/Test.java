/*
 * @(#)Test.java
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
package com.bric.geom.tests;

import java.util.Arrays;
import java.util.Vector;

import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.bric.util.FloatProperty;
import com.bric.util.Property;

public abstract class Test {
	FloatProperty successRate = new FloatProperty("Success Rate",0,1,0);
	FloatProperty completion = new FloatProperty("Completion",0,1,0);
	long[] times = new long[getCaseCount()];
	boolean paused = true;
	
	//Vector successListeners = new Vector();
	//Vector completionListeners = new Vector();
	Vector<ChangeListener> stateListeners = new Vector<ChangeListener>();
	boolean runningTest = false;

	Thread thread = new Thread() {
		@Override
		public void run() {
			synchronized(Test.this) {
				runningTest = true;
			}
			fireStateListeners();
			
			runTest();

			long[] times = getTimes();
			Arrays.sort(times);
			System.out.println("time = "+times[times.length/2]);
			
			synchronized(Test.this) {
				runningTest = false;
			}
			
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					fireStateListeners();
				}
			});
		}
	};
	
	public void setPaused(boolean b) {
		paused = b;
		fireStateListeners();
	}
	
	public Test() {
		thread.start();
		successRate.setUserAdjustable(false);
		completion.setUserAdjustable(false);
	}
	
	/** Runs the actual test. */
	protected abstract boolean runCase(int caseIndex);
	
	protected abstract String getName();
	
	/** This is called before <code>runCase()</code> and
	 * is <i>not</i> timed.
	 */
	protected abstract void prepCase(int caseIndex);
	
	public abstract Property[] getProperties();
	
	/** This should return a fixed constant.
	 * (No really: a fixed constant.  This is 
	 * called before the constructor.
	 */
	protected abstract int getCaseCount();
	
	public boolean isRunning() {
		synchronized(Test.this) {
			return runningTest && (paused==false);
		}
	}
	
	public void runTest() {
		getCompletion().setValue(0);
		int caseCount = getCaseCount();
		int successCount = 0;
		for(int a = 0; a<caseCount; a++) {
			if(a%1000==0)
			System.out.println(getName()+" test #"+a);
			while(paused) {
				try {
					Thread.sleep(150);
				} catch(Throwable t) {}
			}
			prepCase(a);
			long time = System.currentTimeMillis();
			boolean success;
			try {
				success = runCase(a);
			} catch(Exception e) {
				success = false;
			}
			if(success) {
				successCount++;
			} else {
				System.out.println("case #"+a+" failed");
			}
			getSuccessRate().setValue( (successCount)/(a+1f) );
			getCompletion().setValue( (a)/((float)caseCount+1) );
			time = System.currentTimeMillis()-time;
			times[a] = time;
		}
		getCompletion().setValue(1);
	}
	
	/** This returns the original array: don't modify it.
	 */
	public long[] getTimes() {
		return times;
	}
	
	public FloatProperty getSuccessRate() {
		return successRate;
	}
	
	
	public FloatProperty getCompletion() {
		return completion;
	}

	public void addStateListener(ChangeListener l) {
		if(stateListeners.contains(l))
			return;
		stateListeners.add(l);
	}
	
	protected void fireStateListeners() {
		for(int a = 0; a<stateListeners.size(); a++) {
			ChangeListener l = stateListeners.get(a);
			try {
				l.stateChanged(new ChangeEvent(this));
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
}
