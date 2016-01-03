/*
 * @(#)ColumnIcon.java
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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

import com.bric.blog.ResourceSample;

/** One of three icons used to toggle views in file browsers/dialogs.
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/ColumnIcon/sample.png" alt="new&#160;com.bric.swing.resources.ColumnIcon(12,&#160;12)">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 * 
 * @see ListIcon
 * @see TileIcon
 * @see StackIcon
 */
@ResourceSample( sample= { 
		"new com.bric.swing.resources.ColumnIcon(12, 12)" })
public class ColumnIcon implements Icon {
	final int w, h;
	public ColumnIcon(int width,int height) {
		w = width;
		h = height;
	}
	public int getIconHeight() {
		return h;
	}
	public int getIconWidth() {
		return w;
	}
	public void paintIcon(Component c, Graphics g, int x, int y) {
		g.setColor(Color.darkGray);
		int k = 5; //column width
		int dx = (w-k*(w/k))/2;
		g.translate(dx, 0);
		for(int myX = x; myX+k-1<x+w; myX+=k) {
			g.drawRect(myX,y,k,h);
		}
		g.translate(-dx, 0);
	}
}
