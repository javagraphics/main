/*
 * @(#)MutableBufferedImage.java
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
package com.bric.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;

/** This is a <code>BufferedImage</code> that offers a <code>setProperty()</code> method.
 *
 */
public class MutableBufferedImage extends BufferedImage {
	
	Hashtable<String, Object> extraProperties = null;

	public MutableBufferedImage(ColorModel cm, WritableRaster r,
			boolean premultiplied, Hashtable<String, Object> properties) {
		super(cm, r, premultiplied, properties);
	}

	public MutableBufferedImage(int width, int height, int imageType,
			IndexColorModel cm) {
		super(width, height, imageType, cm);
	}

	public MutableBufferedImage(int width, int height, int imageType) {
		super(width, height, imageType);
	}

	@Override
	public synchronized Object getProperty(String name, ImageObserver observer) {
		if(extraProperties!=null) {
			Object value = extraProperties.get(name);
			if(value!=null)
				return value;
		}
		return super.getProperty(name, observer);
	}

	@Override
	public synchronized Object getProperty(String name) {
		if(extraProperties!=null) {
			Object value = extraProperties.get(name);
			if(value!=null)
				return value;
		}
		return super.getProperty(name);
	}

	@Override
	public synchronized String[] getPropertyNames() {
		ArrayList<String> list = new ArrayList<String>();
		String[] superNames = super.getPropertyNames();
		for(int a = 0; a<superNames.length; a++) {
			list.add(superNames[a]);
		}
		if(extraProperties!=null) {
			Enumeration<String> e = extraProperties.keys();
			while(e.hasMoreElements()) {
				String key = e.nextElement();
				list.add(key);
			}
		}
		return list.toArray(new String[list.size()]);
	}

	public synchronized void setProperty(String propertyName,Object value) {
		if(extraProperties==null)
			extraProperties = new Hashtable<String, Object>();
		extraProperties.put(propertyName, value);
	}
}
