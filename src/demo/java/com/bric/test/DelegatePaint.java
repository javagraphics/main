/*
 * @(#)DelegatePaint.java
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
package com.bric.test;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.Raster;

/** This wraps an existing Paint object.
* <P>The purpose of this class is to mask from the underlying
* graphics pipeline when a special paint (like java.awt.Color)
* is being used.  This can help gauge whether a Paint class
* is getting special (optimized) treatment, and what it would
* get under "normal" circumstances.
*/
public class DelegatePaint implements Paint {
	Paint paint;
	
	public DelegatePaint(Paint p) {
		paint = p;
	}

	public PaintContext createContext(ColorModel cm, Rectangle deviceBounds,
			Rectangle2D userBounds, AffineTransform xform, RenderingHints hints) {
		return new DelegatePaintContext(paint.createContext(cm, deviceBounds, userBounds, xform, hints));
	}

	public int getTransparency() {
		return paint.getTransparency();
	}
}

class DelegatePaintContext implements PaintContext {
	PaintContext pc;
	
	public DelegatePaintContext(PaintContext paintContext) {
		this.pc = paintContext;
	}

	public void dispose() {
		pc.dispose();
	}

	public ColorModel getColorModel() {
		return pc.getColorModel();
	}

	public Raster getRaster(int x, int y, int w, int h) {
		return pc.getRaster(x, y, w, h);
	}
}
