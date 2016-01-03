/*
 * @(#)MultiThumbSliderDemoHelper.java
 *
 * $Date$
 *
 * Copyright (c) 2015 by Jeremy Wood.
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
import java.awt.image.BufferedImage;

import com.bric.blog.BlogHelper;
import com.bric.plaf.DefaultMultiThumbSliderUI;
import com.bric.plaf.MultiThumbSliderUI.Thumb;
import com.bric.plaf.VistaMultiThumbSliderUI;

public class MultiThumbSliderDemoHelper extends BlogHelper {

	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		MultiThumbSlider<?> jc = DefaultMultiThumbSliderUI.createDemo( VistaMultiThumbSliderUI.class, Thumb.Triangle );
		return paint(jc, null);
	}
}
