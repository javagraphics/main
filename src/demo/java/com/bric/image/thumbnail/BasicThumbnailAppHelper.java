/*
 * @(#)BasicThumbnailAppHelper.java
 *
 * $Date: 2014-05-07 01:27:21 +0200 (Mi, 07 Mai 2014) $
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
package com.bric.image.thumbnail;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import com.bric.blog.BlogHelper;

public class BasicThumbnailAppHelper extends BlogHelper {
	
	public static BufferedImage createBlurbGraphic(Dimension preferredSize) throws Exception {
		return BasicThumbnail.Aqua.create( BasicThumbnailApp.DEFAULT_IMAGE, preferredSize);
	}

}
