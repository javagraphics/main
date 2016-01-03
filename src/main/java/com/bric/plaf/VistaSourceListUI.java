/*
 * @(#)VistaSourceListUI.java
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
package com.bric.plaf;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;

public class VistaSourceListUI extends SourceListUI {

	@Override
	protected void paintBackground(Graphics2D g, Rectangle rowBounds,
			Object value, int row, boolean isSelected, boolean cellHasFocus) {
		if(isSelected) {
			RoundRectangle2D rect = new RoundRectangle2D.Float();
			rect.setRoundRect(rowBounds.x, rowBounds.y, rowBounds.width-1, rowBounds.height-1, 6, 6 );
			g.setPaint(new GradientPaint(0,rowBounds.y,new Color(0xE3F4Fb),0,rowBounds.y+rowBounds.height,new Color(0xCEE9FA)));
			g.fill(rect);
	
			rect.setRoundRect(rowBounds.x+1, rowBounds.y+1, rowBounds.width-3, rowBounds.height-3, 4, 4 );
			g.setColor(new Color(0xEAFDFF));
			g.draw(rect);
	
			rect.setRoundRect(rowBounds.x, rowBounds.y, rowBounds.width-1, rowBounds.height-1, 6, 6 );
			g.setColor(new Color(0xC7DBE4));
			g.draw(rect);
		}
	}

}
