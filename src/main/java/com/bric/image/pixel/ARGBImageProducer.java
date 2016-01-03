/*
 * @(#)ARGBImageProducer.java
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
package com.bric.image.pixel;

import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.util.Vector;

/** A simple <code>ImageProducer</code> based on a
 * <code>PixelIterator</code>.
 *
 */
public class ARGBImageProducer implements ImageProducer {
	Vector<ImageConsumer> v = new Vector<ImageConsumer>();
	IntARGBConverter i;

	public ARGBImageProducer(PixelIterator i) {
		if (i instanceof IntARGBConverter) {
			this.i = (IntARGBConverter) i;
		} else {
			this.i = new IntARGBConverter(i);
		}
	}

	public void addConsumer(ImageConsumer c) {
		if (v.contains(c) == false)
			v.add(c);
	}

	public boolean isConsumer(ImageConsumer c) {
		return v.contains(c);
	}

	public void removeConsumer(ImageConsumer c) {
		v.remove(c);
	}

	public void requestTopDownLeftRightResend(ImageConsumer c) {
		startProduction(c);
	}

	/** Color model used for ARGB */
	static ColorModel cm = new DirectColorModel(24, 0x00ff0000, 0x0000ff00,
			0x000000ff);

	public void startProduction(ImageConsumer c) {
		addConsumer(c);
		int w = i.getWidth();
		int[] data = new int[w];
		c.setDimensions(w, i.getHeight());
		int y = 0;
		while (i.isDone() == false) {
			i.next(data);
			c.setPixels(0, y, w, 1, cm, data, 0, 1);
			y++;

		}
		c.imageComplete(ImageConsumer.STATICIMAGEDONE);
	}
}
