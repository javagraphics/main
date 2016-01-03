/*
 * @(#)EmptyPaintable.java
 *
 * $Date: 2014-03-13 09:15:48 +0100 (Do, 13 Mär 2014) $
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

import java.awt.Color;
import java.awt.Graphics2D;

public class EmptyPaintable implements Paintable {
	Color color;
	int width, height;
	
	public EmptyPaintable(int width,int height) {
		this(null, width, height);
	}
	
	public EmptyPaintable(Color color,int width,int height) {
		this.width = width;
		this.height = height;
		this.color = color;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void paint(Graphics2D g) {
		if(color!=null) {
			g.setColor(color);
			g.fillRect(0, 0, width, height);
		}
	}

}
