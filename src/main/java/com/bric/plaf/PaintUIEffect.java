/*
 * @(#)UIPaintEffect.java
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

import java.awt.Graphics2D;

import javax.swing.JComponent;

public abstract class PaintUIEffect extends UIEffect {
	

	public PaintUIEffect(JComponent comp, int totalDuration,
			int updateInterval) {
		super(comp, totalDuration, updateInterval);
	}

	/** Paints this effect.
	 * @param g the graphics to paint to.  This will be
	 * a copy of the original <code>Graphics2D</code>
	 * destination, so it is safe to clip, transform, 
	 * and composite this however you want to without
	 * restoring its original state.
	 */
	public abstract void paint(Graphics2D g);
	
	public abstract boolean isBackground();
}
