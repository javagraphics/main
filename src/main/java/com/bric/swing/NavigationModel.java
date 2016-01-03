/*
 * @(#)NavigationModel.java
 *
 * $Date: 2015-12-26 08:54:45 +0100 (Sa, 26 Dez 2015) $
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
package com.bric.swing;

import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

import com.bric.util.ObservableProperties;
import com.bric.util.ObservableProperties.Edit;
import com.bric.util.ObservableProperties.Key;

public class NavigationModel {
	
	/** This navigation descriptor will simply say "X 1 of 2", where "X" is the argument you 
	 * provide when you construct this object ("Page", "Image", "Result", etc.)
	 */
	static class DefaultDescriptor implements Callable<String> {
		String word;
		NavigationModel model;
		
		public DefaultDescriptor(NavigationModel model,String word) {
			this.model = model;
			this.word = word;
		}
		@Override
		public String call() throws Exception {
			return word+" "+(model.getElementIndex()+1)+" of "+model.getElementCount();
		}
	}
	
	public static final Key<Integer> KEY_INDEX = new Key<>("index", Integer.class);
	public static final Key<Integer> KEY_SIZE = new Key<>("size", Integer.class);
	public static final Key<Callable> KEY_DESCRIPTOR = new Key<>("descriptor", Callable.class);
	public static final Key<Integer> KEY_BUTTON_DELAY = new Key<>("button-delay", Integer.class);
	public static final Key<Integer> KEY_BUTTON_CYCLE = new Key<>("button-cycle", Integer.class);
	public static final Key<Boolean> KEY_DRAGGABLE = new Key<>("draggable", Boolean.class);
	
	ObservableProperties properties = new ObservableProperties();
	
	public NavigationModel() {
		
	}

	public NavigationModel(String word,int currentIndex,int size) {
		this( (Callable<String>)null, currentIndex, size);
		setDescriptor(new DefaultDescriptor(this, word)); 
	}
	public NavigationModel(Callable<String> descriptor,int currentIndex,int size) {
		setElementIndex(currentIndex);
		setElementCount(size);
		setDescriptor(descriptor);
	}
	
	public void addPropertyChangeListener(PropertyChangeListener pcl) {
		properties.addListener(pcl);
	}
	
	public void removePropertyChangeListener(PropertyChangeListener pcl) {
		properties.removeListener(pcl);
	}
	
	public void addPropertyChangeListener(Key<?> property,PropertyChangeListener pcl) {
		properties.addListener( ObservableProperties.DEFAULT, property, pcl);
	}
	
	public void removePropertyChangeListener(Key<?> property,PropertyChangeListener pcl) {
		properties.removeListener( ObservableProperties.DEFAULT, property, pcl);
	}
	
	public boolean isDraggable() {
		Boolean b = properties.get(KEY_DRAGGABLE);
		if(b==null) return false;
		return b.booleanValue();
	}
	
	public void setDraggable(boolean b) {
		properties.set(KEY_DRAGGABLE, b);
	}
	
	public ObservableProperties getProperties() {
		return properties;
	}
	
	public void setElementIndex(int i) {
		properties.set(KEY_INDEX, i);
	}
	
	public void setElementCount(int size) {
		properties.set(KEY_SIZE, size);
	}
	
	public int getElementCount() {
		Integer i = properties.get(KEY_SIZE);
		if(i==null) return 0;
		return i;
	}
	
	public int getElementIndex() {
		Integer i = properties.get(KEY_INDEX);
		if(i==null) return -1;
		return i;
	}

	public void setButtonCycle(int cycle) {
		properties.set(KEY_BUTTON_CYCLE, cycle);
	}

	public void setButtonDelay(int delay) {
		properties.set(KEY_BUTTON_DELAY, delay);
	}
	
	@SuppressWarnings("unchecked")
	public Callable<String> getDescriptor() {
		return properties.get(KEY_DESCRIPTOR);
	}
	
	public void setDescriptor(Callable<String> descriptor) {
		properties.set(KEY_DESCRIPTOR, descriptor);
	}
	
	/** Returns number of milliseconds between loops for a button's timer.
	 * The default value is 50 ms.
	 */
	public int getButtonCycle() {
		Integer i = properties.get(KEY_BUTTON_CYCLE);
		if(i==null) return 50;
		return i;
	}
	
	/** Returns the delay (in ms) between clicking a navigation button and activating the repeating timer.
	 * The default value is 500 ms.
	 */
	public int getButtonDelay() {
		Integer i = properties.get(KEY_BUTTON_DELAY);
		if(i==null) return 500;
		return i;
	}
	
	@Override
	public String toString() {
		return properties.getMap().toString();
	}

	/** Define both the index and the count in one pass. */
	public void setElement(int index, int count) {
		Edit edit = properties.beginEdit();
		try {
			//set it to zero first just in case the old index > new count, and some
			//listeners make critical assumptions that index is always < count.
			setElementCount(count);
			setElementIndex(index);
		} finally {
			properties.endEdit(edit);
		}
	}
}
