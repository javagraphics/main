/*
 * @(#)SampleJobExecutorTest.java
 *
 * $Date: 2014-11-27 08:00:30 +0100 (Do, 27 Nov 2014) $
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
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package com.bric.job;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import junit.framework.TestCase;

import org.junit.Test;

import com.bric.math.MutableInteger;

public class SampleJobExecutorTest extends TestCase {

	static class CountingJob extends SampleJob<Integer> {
		static int idCtr = 0;
		

		public CountingJob(long max) {
			super("counting-job-"+(idCtr++), max);
		}

		@Override
		public Integer[] doCalculate(long sampleIndex, long length) {
			Integer[] array = new Integer[(int)length];
			for(int a = 0; a<array.length; a++) {
				array[a] = (int)(sampleIndex+a);
			}
			return array;
		}
	}
	
	/** Make sure we assign every sample, exactly only once,
	 * and that all threads are used.
	 */
	@Test
	public void testCountingJob() {
		final Integer[] allResults = new Integer[100003];
		final List<Throwable> errors = new LinkedList<Throwable>();
		final Map<String, MutableInteger> usage = new TreeMap<String, MutableInteger>();
		CountingJob countingJob = new CountingJob(allResults.length);
		int threadCount = 10;
		SampleJobExecutor<Integer> executor = new SampleJobExecutor<Integer>(countingJob,threadCount,5) {

			
			@Override
			protected void processResults(long index, int length,
					Integer[] results) {
				String name = Thread.currentThread().getName();
				synchronized(usage) {
					MutableInteger i = usage.get(name);
					if(i==null) {
						i = new MutableInteger(0);
						usage.put(name, i);
					}
					i.value++;
				}
				for(int a = 0; a<length; a++) {
					//has it already been processed?
					if(allResults[(int)(index+a)]!=null)
						fail();
					
					allResults[(int)(index+a)] = results[a];
				}
			}

			@Override
			protected void processThrowable(long index, long length,
					Throwable throwable) {
				errors.add(throwable);
				super.processThrowable(index, length, throwable);
			}
			
		};
		executor.join();
		
		for(int a = 0; a<allResults.length; a++) {
			assertEquals( new Integer(a), allResults[a]);
		}
		
		/* It's technically possible this could execute normally
		 * with fewer threads, but that would be creepily unlikely...
		 */
		assertEquals( threadCount, usage.size());
		System.out.println(usage);
		
		assertEquals( 0, errors.size());
	}
}
