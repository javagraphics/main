/*
 * @(#)TileIcon.java
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
import java.awt.Graphics2D;

import javax.swing.Icon;

import com.bric.blog.ResourceSample;

/** One of three icons used to toggle views in file browsers/dialogs.
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/TileIcon/sample.png" alt="new&#160;com.bric.swing.resources.TileIcon(12,&#160;12)">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 * 
 * @see ColumnIcon
 * @see ListIcon
 * @see StackIcon
 */
@ResourceSample( sample= { 
		"new com.bric.swing.resources.TileIcon(12, 12)" })
public class TileIcon implements Icon {
	final int w, h;
	public TileIcon(int width,int height) {
		w = width;
		h = height;
	}
	public int getIconHeight() {
		return h;
	}
	public int getIconWidth() {
		return w;
	}
	public void paintIcon(Component c, Graphics g0, int x, int y) {
		Graphics2D g = (Graphics2D)g0.create();
		
		g.setColor(Color.darkGray);
		g.translate(x, y);
		int rows = (h+2)/6;
		int columns = (w+2)/6;
		int dy = h/2 - ((rows*6-2)/2);
		int dx = w/2 - ((columns*6-2)/2);
		g.translate(dx, dy);
		for(int row = 0; row<rows; row++) {
			for(int col = 0; col<columns; col++) {
				g.drawRect(col*6,row*6,3,3);
			}
		}
		g.dispose();
	}
}

