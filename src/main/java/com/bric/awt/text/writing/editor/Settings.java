/*
 * @(#)Settings.java
 *
 * $Date: 2014-11-27 07:37:57 +0100 (Do, 27 Nov 2014) $
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
package com.bric.awt.text.writing.editor;

import java.awt.Font;
import java.beans.PropertyChangeListener;
import java.io.File;

import com.bric.awt.text.writing.WritingFont;
import com.bric.util.ObservableProperties;
import com.bric.util.ObservableProperties.Key;

/** Settings used by the WritingApp.
 */
public class Settings {
	public static final Key<Character> SELECTED_CHAR = new Key<Character>("selected-char", Character.class);
	public static final Key<Integer> SELECTED_STROKE = new Key<Integer>("selected-stroke", Integer.class);
	public static final Key<WritingFont> WRITING_FONT = new Key<WritingFont>("selected-wfont", WritingFont.class);
	public static final Key<Font> ONION_SKIN_FONT = new Key<Font>("onion-skin-font", Font.class);
	public static final Key<Boolean> ONION_SKIN_ACTIVE = new Key<Boolean>("onion-skin-active", Boolean.class);
	public static final Key<File> FILE = new Key<File>("file", File.class);
	public static final Key<Float> ANIMATION_TIME = new Key<Float>("animation-time", Float.class);
	public static final Key<Boolean> ANIMATION_ACTIVE = new Key<Boolean>("animation-active", Boolean.class);
	
	ObservableProperties properties = new ObservableProperties();
	
	public Settings() {
		set(SELECTED_CHAR, 'a');
		set(SELECTED_STROKE, -1);
		set(ONION_SKIN_ACTIVE, false);
	}
	
	public <T> T get(Key<T> key) {
		return properties.get(key);
	}

	public <T> T set(Key<T> key,T t) {
		return properties.set(key, t);
	}
	
	public void addListener(PropertyChangeListener pcl) {
		properties.addListener(pcl);
	}
	
	public void removeListener(PropertyChangeListener pcl) {
		properties.removeListener(pcl);
	}
}
