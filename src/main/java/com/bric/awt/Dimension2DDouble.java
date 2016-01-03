/*
 * @(#)Dimension2DDouble.java
 *
 * $Date: 2015-01-24 08:24:41 +0100 (Sa, 24 Jan 2015) $
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
package com.bric.awt;

import java.awt.Dimension;
import java.awt.geom.Dimension2D;

public class Dimension2DDouble extends Dimension2D {
	double width, height;

	public Dimension2DDouble(Dimension d) {
		this(d.width, d.height);
	}
	
	public Dimension2DDouble(double width,double height) {
		this.width = width;
		this.height = height;
	}
	
	@Override
	public String toString() {
		return "Dimension2DDouble[width="+width+" height="+height+"]";
	}
	
	@Override
	public double getHeight() {
		return height;
	}

	@Override
	public double getWidth() {
		return width;
	}

	@Override
	public Object clone() {
		return new Dimension2DDouble(width, height);
	}

	@Override
	public void setSize(double width, double height) {
		this.width = width;
		this.height = height;
	}

}
