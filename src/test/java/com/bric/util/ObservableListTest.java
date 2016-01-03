/*
 * @(#)ObservableListTest.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
 *
 * Copyright (c) 2012 by Jeremy Wood.
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
package com.bric.util;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import junit.framework.TestCase;

import com.bric.util.ObservableList.RecursiveListenerModificationException;

public class ObservableListTest extends TestCase {
	
	static class LogListDataListener implements ListDataListener {
		List<ListDataEvent> log = new Vector<ListDataEvent>();

		public void contentsChanged(ListDataEvent e) {
			log.add(e);
		}

		public void intervalAdded(ListDataEvent e) {
			log.add(e);
		}

		public void intervalRemoved(ListDataEvent e) {
			log.add(e);
		}
		
		void pull(int type,int index0,int index1) {
			ListDataEvent e = log.remove(0);
			String string = e.toString();
			assertTrue(string, e.getType()==type );
			assertTrue(string, e.getIndex0()==index0 );
			assertTrue(string, e.getIndex1()==index1 );
		}
	}
	
	public void testAddMethod1() {
		ObservableList<String> list = new ObservableList<String>();
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		list.add("A");
		list.add("B");
		list.add("C");
		list.add("D");
		list.add("E");
		listener.pull( ListDataEvent.INTERVAL_ADDED, 0, 0);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 1, 1);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 2, 2);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 3, 3);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 4, 4);
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==5 );
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "C", "D", "E" }));
	}
	
	public void testAddMethod2() {
		ObservableList<String> list = new ObservableList<String>();
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		list.add(0, "A");
		list.add(1, "B");
		list.add(0, "C");
		list.add(1, "D");
		list.add(1, "E");
		listener.pull( ListDataEvent.INTERVAL_ADDED, 0, 0);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 1, 1);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 0, 0);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 1, 1);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 1, 1);
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==5 );
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"C", "E", "D", "A", "B" }));
	}
	
	private ObservableList<String> createList(char firstChar,char lastChar) {
		if(firstChar>lastChar)
			throw new IllegalArgumentException(firstChar+">="+lastChar);
		ObservableList<String> list = new ObservableList<String>();
		for(char c = firstChar; c<=lastChar; c++) {
			list.add( Character.toString(c) );
		}
		return list;
	}
	
	public void testAddAllMethod1() {
		ObservableList<String> list = new ObservableList<String>();
		List<String> otherList = createList('A', 'C');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		list.addAll(otherList);
		list.addAll(otherList);
		list.addAll(otherList);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 0, 2);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 3, 5);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 6, 8);
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==9 );
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "C", "A", "B", "C", "A", "B", "C" }));
	
		//make sure this method doesn't lock/timeout:
		list.addAll(list);
	}
	
	public void testAddAllMethod2() {
		ObservableList<String> list = new ObservableList<String>();
		List<String> otherList = createList('A', 'E');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		list.addAll(0, otherList);
		list.addAll(2, otherList);
		list.addAll(5, otherList);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 0, 4);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 2, 6);
		listener.pull( ListDataEvent.INTERVAL_ADDED, 5, 9);
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==15 );
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "A", "B", "C", "A", "B", "C", "D", "E", "D", "E", "C", "D", "E" }));

		//make sure this method doesn't lock/timeout:
		list.addAll(5, list);
	}
	
	public void testClearMethod() {
		ObservableList<String> list = createList('A', 'E');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		list.clear();
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 0, 4);
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==0 );
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] { }));
	}
	
	@SuppressWarnings("unchecked")
	public void testCloneMethod() {
		ObservableList<String> list = createList('A', 'E');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		ObservableList<Character> copy = (ObservableList<Character>)list.clone();
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==5 );
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] { "A", "B", "C", "D", "E"}));
		assertTrue( copy.toString(), Arrays.equals( copy.toArray(), new String[] { "A", "B", "C", "D", "E"}));
	}
	
	public void testContainsMethod() {
		ObservableList<String> list = createList('A', 'E');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		assertTrue( list.contains("A") );
		assertTrue( list.contains("B") );
		assertTrue( list.contains("C") );
		assertTrue( list.contains("D") );
		assertTrue( list.contains("E") );
		assertFalse( list.contains("a") );
		assertFalse( list.contains("b") );
		assertFalse( list.contains("c") );
		assertFalse( list.contains("F") );
		assertFalse( list.contains("G") );
		assertFalse( list.contains(null) );
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==5 );
	}
	
	public void testContainsAllMethod() {
		ObservableList<String> list = createList('A', 'E');
		ObservableList<String> otherList = createList('B', 'D');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		assertTrue( list.containsAll(otherList) );
		assertFalse( otherList.containsAll(list) );
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==5 );
		assertTrue( otherList.size()==3 );

		//make sure this method doesn't lock/timeout:
		assertTrue( list.containsAll(list) );
	}
	
	public void testEqualsMethod() {
		ObservableList<String> list1 = createList('A', 'E');
		ObservableList<String> list2 = createList('A', 'E');
		ObservableList<String> otherList = createList('B', 'D');
		LogListDataListener listener = new LogListDataListener();
		list1.addUnsynchronizedListener(listener, false, false);
		assertTrue( list1.equals(list2) );
		assertFalse( otherList.equals(list1) );
		assertFalse( list1.equals(otherList) );
		assertTrue( listener.log.isEmpty() );
		assertTrue( list1.size()==5 );
		assertTrue( otherList.size()==3 );
		assertTrue( list1.equals(list1) );
	}
	
	public void testGetMethod() {
		ObservableList<String> list = createList('A', 'E');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		assertEquals( list.get(0), "A" );
		assertEquals( list.get(1), "B" );
		assertEquals( list.get(2), "C" );
		assertEquals( list.get(3), "D" );
		assertEquals( list.get(4), "E" );
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==5 );
		try {
			list.get(-1);
			fail();
		} catch(RuntimeException e) {
			//pass
		}
		try {
			list.get(5);
			fail();
		} catch(RuntimeException e) {
			//pass
		}
	}
	
	public void testIndexOfMethod() {
		ObservableList<String> list = createList('A', 'E');
		list.addAll( createList('A', 'E') );
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		assertTrue( list.indexOf("A")==0 );
		assertTrue( list.indexOf("B")==1 );
		assertTrue( list.indexOf("C")==2 );
		assertTrue( list.indexOf("D")==3 );
		assertTrue( list.indexOf("E")==4 );
		assertTrue( list.indexOf("F")==-1 );
		assertTrue( list.indexOf("a")==-1 );
		assertTrue( list.indexOf(null)==-1 );
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==10 );
	}
	
	/** This was observed as a locked semaphore in T4L coverage tests.
	 * 
	 */
	public void testRecursiveAddListenerCase() {
		final ObservableList<Character> list = new ObservableList<Character>();
		final boolean[] passed = new boolean[] { false };
		list.addSynchronizedListener(new ListDataListener() {

			public void intervalAdded(ListDataEvent e) {
				contentsChanged(e);
			}

			public void intervalRemoved(ListDataEvent e) {
				contentsChanged(e);
			}

			public void contentsChanged(ListDataEvent e) {
				list.addSynchronizedListener(new ListDataListener() {

					public void intervalAdded(ListDataEvent e) {
						contentsChanged(e);
					}

					public void intervalRemoved(ListDataEvent e) {
						contentsChanged(e);
					}

					public void contentsChanged(ListDataEvent e) {
						passed[0] = true;
					}
					
				}, true);
			}
			
		}, true);
		
		list.add( 'A' );
		assertTrue( passed[0] );
	}
	
	public void testIsEmptyMethod() {
		ObservableList<String> list1 = createList('A', 'E');
		ObservableList<String> list2 = new ObservableList<String>();
		assertFalse( list1.isEmpty() );
		assertTrue( list2.isEmpty() );
	}
	
	public void testLastIndexOfMethod() {
		ObservableList<String> list = createList('A', 'E');
		list.addAll( createList('A', 'E') );
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		assertEquals( list.lastIndexOf("A"), 5 );
		assertEquals( list.lastIndexOf("B"), 6 );
		assertEquals( list.lastIndexOf("C"), 7 );
		assertEquals( list.lastIndexOf("D"), 8 );
		assertEquals( list.lastIndexOf("E"), 9 );
		assertEquals( list.lastIndexOf("F"), -1 );
		assertEquals( list.lastIndexOf("a"), -1 );
		assertEquals( list.lastIndexOf(null), -1 );
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.size()==10 );
	}

	/** The ObservableList.execute() method begins by obtaining 1
	 * permit from the readSemaphore, then evaluating whether the
	 * operation is a null-op or not. Then all the remaining
	 * readSemaphore permits are collected to perform the operation.
	 * 
	 * By itself: this logic will lead to a deadlock if several 
	 * competing threads obtain the initial permit and are waiting
	 * to collect the remaining permits.
	 * 
	 * To address this: a reentrant lock is used only for the
	 * execute() method. (If it is not reentrant and a recursive
	 * null-op occurs: more locking will follow. A recursive non-null-op
	 * will throw a RecursiveListenerModificationException.
	 * 
	 * @throws Exception if an error occurs.
	 */
	public void testWriteLock() throws Exception {
    	Thread possibleDeadlock = new Thread("ObservableList.execute() test") {
    		public void run() {
    	    	final String termA = "a";
    	    	final String termB = "b";
    			final ObservableList<String> list = new ObservableList<String>();
    			list.readSemaphore.acquireUninterruptibly();
    			Thread thread1 = new Thread() {
    				public void run() {
    					list.add( termA );
    				}
    			};
    			Thread thread2 = new Thread() {
    				public void run() {
    					list.add( termB );
    				}
    			};
    			thread1.start();
    			thread2.start();
    			list.readSemaphore.release();
    		}
    	};
    	assertThreadCompletion(possibleDeadlock, 2000);
	}
    
    protected void assertThreadCompletion(Thread thread,long duration) {
    	long start = System.currentTimeMillis();
    	thread.start();
    	while(thread.isAlive()) {
    		long elapsed = System.currentTimeMillis() - start;
    		if(elapsed>duration) {
    			fail("thread \""+thread.getName()+"\" took over "+duration+" ms");
    		}
    		long remaining = duration - elapsed;
    		long sleep = Math.max(10, Math.min(remaining, 100));
    		try {
    			Thread.sleep(sleep);
    		} catch(InterruptedException e) {}
    	}
    }
	
	public void testRemoveMethod1() {
		ObservableList<String> list = createList('A', 'E');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		assertEquals( list.remove(0), "A");
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"B", "C", "D", "E" }));
		assertEquals( list.remove(1), "C");
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"B", "D", "E" }));
		assertEquals( list.remove(0), "B");
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"D", "E" }));
		assertEquals( list.remove(1), "E");
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"D" }));
		assertEquals( list.remove(0), "D");
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] { }));
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 0, 0);
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 1, 1);
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 0, 0);
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 1, 1);
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 0, 0);
		assertTrue( listener.log.isEmpty() );
	}
	
	public void testRemoveMethod2() {
		ObservableList<String> list = createList('A', 'E');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		assertEquals( list.remove("F"), false );
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "C", "D", "E" }));
		assertEquals( list.remove("A"), true);
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"B", "C", "D", "E" }));
		assertEquals( list.remove("C"), true);
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"B", "D", "E" }));
		assertEquals( list.remove("B"), true);
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"D", "E" }));
		assertEquals( list.remove("E"), true);
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"D" }));
		assertEquals( list.remove("D"), true);
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] { }));
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 0, 0);
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 1, 1);
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 0, 0);
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 1, 1);
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 0, 0);
		assertTrue( listener.log.isEmpty() );
	}
	
	public void testRemoveAllMethod() {
		//nondetailed event:

		//because we asked for non-detailed events we have a redundant null-op
		//notification. But in the next test we do 
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('B', 'D');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, false, false);
			
			assertTrue( list.removeAll(otherList) );
			assertFalse( list.removeAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "E" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 5);
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 2);
			assertTrue( listener.log.isEmpty() );
		}
		
		//detailed event #1:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('B', 'D');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.removeAll(otherList) );
			assertFalse( list.removeAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "E" }));
			listener.pull( ListDataEvent.INTERVAL_REMOVED, 1, 3);
			assertTrue( listener.log.isEmpty() );
		}

		//detailed event #2:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = new ObservableList<String>();
			otherList.add("B");
			otherList.add("D");
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.removeAll(otherList) );
			assertFalse( list.removeAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "C", "E" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 1, 3);
			assertTrue( listener.log.isEmpty() );
		}
		
		{
			ObservableList<String> list = createList('A', 'E');
			list.removeAll(list);
		}
	}
	
	public void testRemoveRange() {
		ObservableList<String> list = createList('A', 'E');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		
		list.removeRange(1, 3);
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "E" }));
		listener.pull( ListDataEvent.INTERVAL_REMOVED, 1, 3);
		assertTrue( listener.log.isEmpty() );
	}
	
	public void testRetainAllMethod() {
		//nondetailed event:
		//a non-detailed listener is going to receive a redundant null-op
		//notification:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('B', 'D');
			otherList.add("Z");
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, false, false);
			
			assertTrue( list.retainAll(otherList) );
			assertFalse( list.retainAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"B", "C", "D" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 5);
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 3);
			assertTrue( listener.log.isEmpty() );
		}
		
		//detailed event #1:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('B', 'D');
			otherList.add("Z");
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.retainAll(otherList) );
			assertFalse( list.retainAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"B", "C", "D" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 5);
			assertTrue( listener.log.isEmpty() );
		}
		
		//detailed event #2:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('A', 'C');
			otherList.add("Z");
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.retainAll(otherList) );
			assertFalse( list.retainAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "C" }));
			listener.pull( ListDataEvent.INTERVAL_REMOVED, 3, 4);
			assertTrue( listener.log.isEmpty() );
		}
		
		//detailed event #3:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('C', 'E');
			otherList.add("Z");
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.retainAll(otherList) );
			assertFalse( list.retainAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"C", "D", "E"}));
			listener.pull( ListDataEvent.INTERVAL_REMOVED, 0, 1);
			assertTrue( listener.log.isEmpty() );
		}

		//detailed event #4:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = new ObservableList<String>();
			otherList.add("B");
			otherList.add("D");
			otherList.add("E");
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.retainAll(otherList) );
			assertFalse( list.retainAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"B", "D", "E"}));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 2);
			assertTrue( listener.log.isEmpty() );
		}

		//detailed event #5:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = new ObservableList<String>();
			otherList.add("A");
			otherList.add("B");
			otherList.add("D");
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.retainAll(otherList) );
			assertFalse( list.retainAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "D"}));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 2, 4);
			assertTrue( listener.log.isEmpty() );
		}
	}
	
	public void testSetAll() {
		//nondetailed event #1:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('a', 'd');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, false, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"a", "b", "c", "d" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 4);
			assertTrue( listener.log.isEmpty() );
		}
		
		//nondetailed event #2:
		//if the two lists are of equal size, the CONTENTS_CHANGED event should
		//reflect the first index where they differ
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('A', 'E');
			otherList.set(2, "c");
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, false, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "c", "D", "E" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 2, 4);
			assertTrue( listener.log.isEmpty() );
		}
		
		//nondetailed event #3:
		//two equal lists should return false with no events:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('A', 'E');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, false, false);
			
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "C", "D", "E"}));
			assertTrue( listener.log.isEmpty() );
		}
		
		//detailed event #1:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('B', 'D');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"B", "C", "D" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 4);
			assertTrue( listener.log.isEmpty() );
		}

		
		//detailed event #2:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('A', 'C');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "C" }));
			listener.pull( ListDataEvent.INTERVAL_REMOVED, 3, 4);
			assertTrue( listener.log.isEmpty() );
		}

		
		//detailed event #3:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('C', 'E');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"C", "D", "E" }));
			listener.pull( ListDataEvent.INTERVAL_REMOVED, 0, 1);
			assertTrue( listener.log.isEmpty() );
		}

		
		//detailed event #4:
		{
			ObservableList<String> list = createList('C', 'E');
			ObservableList<String> otherList = createList('A', 'E');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "C", "D", "E" }));
			listener.pull( ListDataEvent.INTERVAL_ADDED, 0, 1);
			assertTrue( listener.log.isEmpty() );
		}

		
		//detailed event #5:
		{
			ObservableList<String> list = createList('A', 'C');
			ObservableList<String> otherList = createList('A', 'E');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "C", "D", "E" }));
			listener.pull( ListDataEvent.INTERVAL_ADDED, 3, 4);
			assertTrue( listener.log.isEmpty() );
		}

		
		//detailed event #6:
		{
			ObservableList<String> list = createList('C', 'E');
			ObservableList<String> otherList = createList('A', 'G');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "C", "D", "E", "F", "G" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 2);
			assertTrue( listener.log.isEmpty() );
		}

		
		//detailed event #7:
		{
			ObservableList<String> list = createList('A', 'C');
			ObservableList<String> otherList = createList('B', 'E');
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"B", "C", "D", "E" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 2);
			assertTrue( listener.log.isEmpty() );
		}

		
		//detailed event #8:
		{
			ObservableList<String> list = createList('A', 'E');
			ObservableList<String> otherList = createList('A', 'E');
			otherList.set(2, "c");
			otherList.set(3, "d");
			LogListDataListener listener = new LogListDataListener();
			list.addUnsynchronizedListener(listener, true, false);
			
			assertTrue( list.setAll(otherList) );
			assertFalse( list.setAll(otherList) );
			assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] {"A", "B", "c", "d", "E" }));
			listener.pull( ListDataEvent.CONTENTS_CHANGED, 2, 3);
			assertTrue( listener.log.isEmpty() );
		}
		
		{
			ObservableList<String> list = createList('A', 'E');
			assertFalse( list.setAll(list) );
		}
	}
	
	public void testSetMethod() {
		ObservableList<String> list = createList('A', 'E');
		LogListDataListener listener = new LogListDataListener();
		list.addUnsynchronizedListener(listener, false, false);
		assertEquals( list.set(0, "a"), "A");
		assertEquals( list.set(1, "b"), "B");
		assertEquals( list.set(2, "c"), "C");
		assertEquals( list.set(3, "d"), "D");
		assertEquals( list.set(4, "e"), "E");
		
		try {
			list.set(-1, "z");
			fail();
		} catch(RuntimeException e) {}
		
		try {
			list.set(5, "z");
			fail();
		} catch(RuntimeException e) {}
		
		listener.pull( ListDataEvent.CONTENTS_CHANGED, 0, 0);
		listener.pull( ListDataEvent.CONTENTS_CHANGED, 1, 1);
		listener.pull( ListDataEvent.CONTENTS_CHANGED, 2, 2);
		listener.pull( ListDataEvent.CONTENTS_CHANGED, 3, 3);
		listener.pull( ListDataEvent.CONTENTS_CHANGED, 4, 4);
		assertTrue( listener.log.isEmpty() );
		assertTrue( list.toString(), Arrays.equals( list.toArray(), new String[] { "a", "b", "c", "d", "e" }));
	
		//does redundantly setting an element trigger events?
		assertEquals( list.set(0, "a"), "a");
		assertEquals( list.set(1, "b"), "b");
		assertEquals( list.set(2, "c"), "c");
		assertEquals( list.set(3, "d"), "d");
		assertEquals( list.set(4, "e"), "e");
		assertTrue( listener.log.isEmpty() );
		
	}
	
	static class RecursiveModificationListener implements ListDataListener {
		boolean expectFailure;
		int recursionCtr = 0;
		RecursiveModificationListener(boolean b) {
			expectFailure = b;
		}
		
		@SuppressWarnings("unchecked")
		public void contentsChanged(ListDataEvent e) {
			if(recursionCtr>0) return;
			
			recursionCtr++;
			try {
				ObservableList<String> list = (ObservableList<String>)e.getSource();
				list.add("Z");
				if(expectFailure)
					fail("expected failure, but modification passed");
			} catch(RecursiveListenerModificationException e2) {
				if(expectFailure==false)
					fail("modification failed, but no failure was expected");
			} finally {
				recursionCtr--;
			}
		}
		
		public void intervalAdded(ListDataEvent e) {
			contentsChanged(e);
		}
		
		public void intervalRemoved(ListDataEvent e) {
			contentsChanged(e);
		}
	}
	
	public void testRecursiveListenerModificationException() {
		//test #1:
		{
			ObservableList<String> list = createList('A', 'E');
			list.addUnsynchronizedListener( new RecursiveModificationListener(true), true, false);
			list.add("z");
		}
		
		//test #2:
		{
			ObservableList<String> list = createList('A', 'E');
			list.addUnsynchronizedListener( new RecursiveModificationListener(false), true, true);
			list.add("z");
		}

		//test #3:
		{
			ObservableList<String> list = createList('A', 'E');
			list.addSynchronizedListener( new RecursiveModificationListener(true), true );
			list.add("z");
		}
	}
	
	/** This makes sure a synchronized listener (
	 * @throws InterruptedException 
	 * 
	 */
	public void testSynchronizedAccess() throws InterruptedException {
		final ObservableList<String> list = createList('A', 'E');
		list.addSynchronizedListener(new ListDataListener() {
			
			private void checkAccess() {
				assertEquals( list, list.clone() );
				assertTrue( list.contains("B") );
				assertTrue( list.containsAll( createList('B', 'D') ) );
				assertEquals( list.get(5), "z" );
				assertTrue( list.size()==6 );
				assertFalse( list.isEmpty() );
				assertEquals( list.indexOf("z"), 5);
			}

			public void contentsChanged(ListDataEvent e) {
				checkAccess();
				
				//double-check that another thread can access things, too.
				Thread otherThread = new Thread() {
					public void run() {
						checkAccess();
					}
				};
				otherThread.start();
				try {
					otherThread.join();
				} catch(Exception e2) {
					e2.printStackTrace();
					fail();
				}
			}

			public void intervalAdded(ListDataEvent e) {
				contentsChanged(e);
			}

			public void intervalRemoved(ListDataEvent e) {
				contentsChanged(e);
			}
			
		}, true);
		list.add("z");
		
	}
}
