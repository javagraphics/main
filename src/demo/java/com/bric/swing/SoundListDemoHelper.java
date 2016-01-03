/*
 * @(#)SoundListDemoHelper.java
 *
 * $Date: 2014-05-07 01:18:06 +0200 (Mi, 07 Mai 2014) $
 *
 * Copyright (c) 2014 by Jeremy Wood.
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
package com.bric.swing;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import com.bric.blog.BlogHelper;
import com.bric.swing.resources.MusicIcon;

public class SoundListDemoHelper extends BlogHelper {
	
	public static BufferedImage createBlurbGraphic(Dimension preferredSize) {
		int s = Math.min( preferredSize.width, preferredSize.height );
		BufferedImage image = new BufferedImage( s, s, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		MusicIcon icon = new MusicIcon( image.getWidth(), image.getHeight(), 
				MusicIcon.PlayToggleIcon.DEFAULT_WIDTH,
				MusicIcon.PlayToggleIcon.DEFAULT_WIDTH );
		icon.getPlayToggleIcon().setOpacity(1);
		icon.getPlayToggleIcon().setTrackSize(1);
		icon.getPlayToggleIcon().setTrackCompletion(.625f);
		icon.paintIcon(null, g, 0, 0);
		g.dispose();
		
		return image;
	}
}
