/*
 * @(#)PngTests.java
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
package com.bric.image.png;

import java.awt.image.BufferedImage;
import java.io.File;

import junit.framework.TestCase;

import com.bric.image.pixel.GenericImageSinglePassIterator;
import com.bric.image.pixel.IntPixelIterator;
import com.bric.io.FileTreeIterator;
import com.bric.io.SuffixFilenameFilter;

public class PngTests extends TestCase {

	private FileTreeIterator getInterlacedPNGs() {
		SuffixFilenameFilter filter = new SuffixFilenameFilter("png");
		char s = File.separatorChar;
		
		//for fun/thoroughness: try switching this
		//boolean to false to widen the net of images
		//we test against:		
		File base = new File(System.getProperty("user.dir")+
						s+"tests"+s+"com"+s+"bric"+s+"image"+s+"resources"+s+"interlace_pngs"+s);
		FileTreeIterator i = new FileTreeIterator(
				base,
				filter);
		return i;
	}
	
	/** The GenericImageSinglePassIterator consumer
	 * received multiple calls to setPixels()
	 * as the image is de-interlaced.
	 * 
	 * This test will fail if we only listen to the
	 * first call to setPixels(..), but if we listen
	 * to the subsequent calls: the image loads correctly.
	 * 
	 * Revision 1332 resolved this issue.
	 * 
	 */
	public void testInterlacedPNGs() {
		FileTreeIterator iter = getInterlacedPNGs();
		while(iter.hasNext()) {
			File file = iter.next();
			IntPixelIterator pixelIter = GenericImageSinglePassIterator.getIntIterator(file, BufferedImage.TYPE_INT_ARGB);
			int[] row = new int[pixelIter.getMinimumArrayLength()];
			int y = 0;
			while(pixelIter.isDone()==false) {
				pixelIter.next(row);
				if(y>5 && (y%2==1) && y<pixelIter.getHeight()-5) {
					boolean hasData = false;
					for(int x = 0; x<pixelIter.getWidth(); x++) {
						int alpha = (row[x] >> 24) & 0xff;
						if(alpha>128)
							hasData = true;
					}
					assertTrue( hasData );
				}
				y++;
			}
		}
	}
}
