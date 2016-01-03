/*
 * @(#)GifReaderDemo.java
 *
 * $Date: 2014-11-27 07:56:34 +0100 (Do, 27 Nov 2014) $
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
package com.bric.image.gif;

import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;

import javax.imageio.ImageIO;

import com.bric.io.IOUtils;

/** This helps the user select a file and then exports each frame as a PNG image.
 */
public class GifReaderDemo {


	public static void main(String[] args) {
		try {
			FileDialog fd = new FileDialog(new Frame());
			fd.setFilenameFilter(new FilenameFilter() {
				public boolean accept(File f, String n) {
					File f2 = new File(f, n);
					return (GifReader.isAnimatedGIF(f2));
				}
			});
			fd.show();
			if (fd.getFile() == null)
				throw new RuntimeException("No file selected.");
			File file = new File(fd.getDirectory() + fd.getFile());
			GifReader r = new GifReader(file);
			BufferedImage bi = r.getNextFrame(true);
			while (bi != null) {
				ImageIO.write(bi, "png", IOUtils.getUniqueFile(null, "Frame", true, true));
				bi = r.getNextFrame(true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}
