/*
 * @(#)MinusIcon.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.swing.resources;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.Icon;

import com.bric.blog.ResourceSample;

/** A minus sign.
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/MinusIcon/sample.png" alt="new&#160;com.bric.swing.resources.MinusIcon(14,&#160;14,&#160;2,&#160;java.awt.Color.gray)">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 *
 * @see PlusIcon
 */
@ResourceSample ( sample = {"new com.bric.swing.resources.MinusIcon(14, 14, 2, java.awt.Color.gray)" })
public class MinusIcon implements Icon {
	final int width, height, strokeWidth;
	final Color color;

	public MinusIcon(int width,int height) {
		this(width, height, 2, null);
	}
	
	public MinusIcon(int width,int height,int strokeWidth,Color color) {
		this.width = width;
		this.height = height;
		this.strokeWidth = strokeWidth;
		this.color = color;
	}

	public int getIconHeight() {
		return height;
	}

	public int getIconWidth() {
		return width;
	}

	public void paintIcon(Component c, Graphics g0, int x, int y) {
		Graphics2D g = (Graphics2D)g0;
		g.setStroke(new BasicStroke(strokeWidth));
		if(color==null) {
			g.setColor(new Color(0x484848));
		} else {
			g.setColor(color);
		}
		g.drawLine(x, y+height/2, x+width, y+height/2);
	}
}
