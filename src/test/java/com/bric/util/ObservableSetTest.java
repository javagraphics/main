/*
 * @(#)ObservableSetTest.java
 *
 * $Date$
 *
 * Copyright (c) 2015 by Jeremy Wood.
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.bric.util.ObservableSet.Listener;
import com.bric.util.ObservableSet.RecursiveListenerModificationException;
import com.bric.util.ObservableSet.SetDataEvent;

public class ObservableSetTest extends TestCase {
	
	Set<String> RAINBOW;
	Set<String> WARM;
	Set<String> WARM_PLUS_BLACK;
	Set<String> COOL;
	Set<String> COOL_PLUS_BLACK;
	
	public ObservableSetTest() {
		RAINBOW = new HashSet<String>();
		WARM = new HashSet<String>();
		COOL = new HashSet<String>();
		RAINBOW.add("red");
		RAINBOW.add("orange");
		RAINBOW.add("yellow");
		RAINBOW.add("green");
		RAINBOW.add("blue");
		RAINBOW.add("indigo");
		RAINBOW.add("violet");

		WARM.add("red");
		WARM.add("orange");
		WARM.add("yellow");
		WARM_PLUS_BLACK = new HashSet<String>(WARM);
		WARM_PLUS_BLACK.add("black");

		COOL.add("green");
		COOL.add("blue");
		COOL.add("indigo");
		COOL.add("violet");
		COOL_PLUS_BLACK = new HashSet<String>(COOL);
		COOL_PLUS_BLACK.add("black");

		RAINBOW = Collections.unmodifiableSet(RAINBOW);
		WARM = Collections.unmodifiableSet(WARM);
		COOL = Collections.unmodifiableSet(COOL);
		WARM_PLUS_BLACK = Collections.unmodifiableSet(WARM_PLUS_BLACK);
		COOL_PLUS_BLACK = Collections.unmodifiableSet(COOL_PLUS_BLACK);
	}
	
	public void testRetainAll() {
		final ObservableSet<String> set = new ObservableSet<String>(String.class);
		set.addAll(RAINBOW);

		assertEquals(true, set.retainAll(WARM_PLUS_BLACK));
		assertEquals(WARM, set);
	}
	
	public void testClear() {
		final ObservableSet<String> set = new ObservableSet<String>(String.class);
		set.addAll(RAINBOW);
		final List<String> listenerNotifications = new ArrayList<String>();

		set.addListener(new Listener<String>() {
			@Override
			public void elementsAdded(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsChanged(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsRemoved(SetDataEvent<String> event) {
				String[] addedElements = event.getAddedElements();
				assertEquals(0, addedElements.length);
				String[] removedElements = event.getRemovedElements();
				for(String s : removedElements) {
					listenerNotifications.add(s);
				}
			}
		});
		
		set.clear();
		assertEquals(7, listenerNotifications.size());
		assertEquals(0, set.size());
	}
	
	public void testRemoveAll() {
		final ObservableSet<String> set = new ObservableSet<String>(String.class);
		set.addAll(RAINBOW);
		final List<String> listenerNotifications = new ArrayList<String>();

		set.addListener(new Listener<String>() {
			@Override
			public void elementsAdded(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsChanged(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsRemoved(SetDataEvent<String> event) {
				String[] addedElements = event.getAddedElements();
				assertEquals(0, addedElements.length);
				String[] removedElements = event.getRemovedElements();
				for(String s : removedElements) {
					listenerNotifications.add(s);
				}
			}
		});
		
		assertEquals(true, set.removeAll(WARM));
		assertEquals(3, listenerNotifications.size());
		assertEquals(COOL, set);

		assertEquals(false, set.removeAll(WARM));
		assertEquals(3, listenerNotifications.size());
		
		set.addListener(new Listener<String>() {
			@Override
			public void elementsAdded(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsChanged(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsRemoved(SetDataEvent<String> event) {
				try {
					set.add("recursive removing!");
					fail();
				} catch(RecursiveListenerModificationException e) {
					//pass!
				}
			}
		});
		
		assertEquals(true, set.removeAll(COOL_PLUS_BLACK));
		assertEquals(7, listenerNotifications.size());

		assertEquals(false, set.removeAll(COOL_PLUS_BLACK));
		assertEquals(7, listenerNotifications.size());
	}
	
	public void testRemove() {
		final ObservableSet<String> set = new ObservableSet<String>(String.class);
		set.addAll(RAINBOW);
		final List<String> listenerNotifications = new ArrayList<String>();

		set.addListener(new Listener<String>() {
			@Override
			public void elementsAdded(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsChanged(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsRemoved(SetDataEvent<String> event) {
				String[] addedElements = event.getAddedElements();
				assertEquals(0, addedElements.length);
				String[] removedElements = event.getRemovedElements();
				for(String s : removedElements) {
					listenerNotifications.add(s);
				}
			}
		});
		
		assertEquals(true, set.remove("red"));
		assertEquals(1, listenerNotifications.size());
		assertEquals(6, set.size());

		assertEquals(false, set.remove("red"));
		assertEquals(1, listenerNotifications.size());
	}
	
	public void testAdd() {
		final ObservableSet<String> set = new ObservableSet<String>(String.class);
		final List<String> listenerNotifications = new ArrayList<String>();
		
		set.addListener(new Listener<String>() {
			@Override
			public void elementsAdded(SetDataEvent<String> event) {
				String[] removedElements = event.getRemovedElements();
				assertEquals(0, removedElements.length);
				String[] addedElements = event.getAddedElements();
				for(String s : addedElements) {
					listenerNotifications.add(s);
				}
			}

			@Override
			public void elementsChanged(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsRemoved(SetDataEvent<String> event) {
				fail("this method should not be called");
			}
		});
		
		assertEquals(true, set.add("hello"));
		assertEquals(1, listenerNotifications.size());
		assertEquals("hello", listenerNotifications.get(0));

		assertEquals(false, set.add("hello"));
		assertEquals(1, listenerNotifications.size());
		
		set.addListener(new Listener<String>() {
			@Override
			public void elementsAdded(SetDataEvent<String> event) {
				try {
					set.add("recursive adding!");
					fail();
				} catch(RecursiveListenerModificationException e) {
					//pass!
				}
			}

			@Override
			public void elementsChanged(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsRemoved(SetDataEvent<String> event) {
				fail("this method should not be called");
			}
		});
		
		set.add("world");
		assertEquals(2, listenerNotifications.size());
		assertEquals("world", listenerNotifications.get(1));
	}

	public void testAddAll() {
		final ObservableSet<String> set = new ObservableSet<String>(String.class);
		final List<String> listenerNotifications = new ArrayList<String>();
		
		set.addListener(new Listener<String>() {
			@Override
			public void elementsAdded(SetDataEvent<String> event) {
				String[] removedElements = event.getRemovedElements();
				assertEquals(0, removedElements.length);
				String[] addedElements = event.getAddedElements();
				for(String s : addedElements) {
					listenerNotifications.add(s);
				}
			}

			@Override
			public void elementsChanged(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsRemoved(SetDataEvent<String> event) {
				fail("this method should not be called");
			}
		});
		
		assertEquals(true, set.addAll(WARM));
		assertEquals(3, listenerNotifications.size());
		assertEquals(WARM, set);

		assertEquals(false, set.addAll(WARM));
		assertEquals(3, listenerNotifications.size());
		
		set.addListener(new Listener<String>() {
			@Override
			public void elementsAdded(SetDataEvent<String> event) {
				try {
					set.add("recursive adding!");
					fail();
				} catch(RecursiveListenerModificationException e) {
					//pass!
				}
			}

			@Override
			public void elementsChanged(SetDataEvent<String> event) {
				fail("this method should not be called");
			}

			@Override
			public void elementsRemoved(SetDataEvent<String> event) {
				fail("this method should not be called");
			}
		});
		
		set.addAll(COOL);
		assertEquals(7, listenerNotifications.size());
		assertEquals(RAINBOW, set);
		
		//make sure if there are 4 elements in the operand and 3 already
		//exist in our set that our listener is only really notified of the
		//one new addition
		set.addAll(WARM_PLUS_BLACK);
		assertEquals(8, listenerNotifications.size());
	}
}
