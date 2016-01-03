/*
 * @(#)GenericImageSinglePassIteratorTest.java
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
package com.bric.image.pixel;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URL;

import junit.framework.TestCase;

import com.bric.image.ImageSize;
import com.bric.io.FileTreeIterator;
import com.bric.io.SuffixFilenameFilter;

public class GenericImageSinglePassIteratorTest extends TestCase {
	public static void testJPEGs() throws Exception {
		int ctr = 0;
		for(int size = 50; size<=400; size+=50) {
			Dimension maxSize = new Dimension(size, size);
			FileTreeIterator i = getJPEGIterator();
			while(i.hasNext()) {
				File file = i.next();
				Dimension originalSize = ImageSize.get(file);
				if(originalSize.width>size || originalSize.height>size) {
					URL url = file.toURI().toURL();
					BufferedImage thumbnail = GenericImageSinglePassIterator.createScaledImage( url, maxSize );
					int w = thumbnail.getWidth();
					int h = thumbnail.getHeight();
					assertTrue("thumbnail for \""+file.getName()+"\" is the wrong size.  Max dimension: "+size+" returned size: "+w+"x"+h, 
							w==maxSize.width || h==maxSize.height);
					ctr++;
				}
			}
		}
		System.out.println("testsJPEGs() successfully iterated over "+ctr+" combinations");
	}

	private static FileTreeIterator getJPEGIterator() {
		SuffixFilenameFilter filter = new SuffixFilenameFilter("jpg", "jpeg");
		char s = File.separatorChar;
		FileTreeIterator i = new FileTreeIterator(
				new File(System.getProperty("user.dir")+
						s+"tests"+s+"com"+s+"bric"+s+"image"+s+"resources"+s),
						filter);
		return i;
	}
}
