/*
 * @(#)ByteBGRAConverter.java
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

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

/** A <code>PixelConverter</code> that converts all data to BGRA-formatted
 * bytes.
 */
public class ByteBGRAConverter extends PixelConverter implements
		BytePixelIterator {

	byte[] rTable, gTable, bTable, aTable;
	int[] intScratch;
	
	/**
	 * @param i
	 */
	public ByteBGRAConverter(PixelIterator i) {
		super(i);
	}

	public void skip() {
		if(byteIterator!=null) {
			byteIterator.skip();
		} else {
			intIterator.skip();
		}
	}

	public void next(byte[] dest) {
		if (byteIterator != null) {
			byte swap;
			byteIterator.next(dest);
			switch (originalType) {
			case BufferedImage.TYPE_4BYTE_ABGR:
			case BufferedImage.TYPE_4BYTE_ABGR_PRE:
				//TODO: deal with PixelIterator.TYPE_4BYTE's
				for (int a = 0; a < width; a++) {
					swap = dest[4 * a];
					dest[4 * a] = dest[4 * a + 1];
					dest[4 * a + 1] = dest[4 * a + 2];
					dest[4 * a + 2] = dest[4 * a + 3];
					dest[4 * a + 3] = swap;
				}
				break;
			case PixelIterator.TYPE_3BYTE_RGB:
				for (int a = width - 1; a >= 0; a--) {
					dest[4 * a] = dest[3 * a];
					dest[4 * a + 1] = dest[3 * a + 1];
					dest[4 * a + 2] = dest[3 * a + 2];
					dest[4 * a + 3] = -1;
				}
				break;
			case BufferedImage.TYPE_3BYTE_BGR:
				for (int a = width - 1; a >= 0; a--) {
					swap = dest[3 * a + 0];
					dest[4 * a] = dest[3 * a + 2];
					dest[4 * a + 1] = dest[3 * a + 1];
					dest[4 * a + 2] = swap;
					dest[4 * a + 3] = -1;
				}
				break;
			case BufferedImage.TYPE_BYTE_GRAY:
				for (int a = width - 1; a >= 0; a--) {
					dest[4 * a] = dest[a];
					dest[4 * a + 1] = dest[a];
					dest[4 * a + 2] = dest[a];
					dest[4 * a + 3] = -1;
				}
				break;
			case BufferedImage.TYPE_BYTE_INDEXED:
				if (rTable == null) {
					rTable = new byte[indexModel.getMapSize()];
					gTable = new byte[indexModel.getMapSize()];
					bTable = new byte[indexModel.getMapSize()];
					aTable = new byte[indexModel.getMapSize()];
					indexModel.getReds(rTable);
					indexModel.getGreens(gTable);
					indexModel.getBlues(bTable);
					indexModel.getAlphas(aTable);
				}
				for (int a = width - 1; a >= 0; a--) {
					int index = dest[a] & 0xff;
					dest[4 * a + 3] = aTable[index];
					dest[4 * a + 2] = rTable[index];
					dest[4 * a + 1] = gTable[index];
					dest[4 * a] = bTable[index];
				}
				break;
			default:
				throw new RuntimeException("Unrecognized type ("
						+ BufferedImageIterator.getTypeName(originalType) + ")");
			}
		} else {
			if (intScratch == null) {
				intScratch = new int[intIterator.getMinimumArrayLength()];
			}
			intIterator.next(intScratch);
			switch (originalType) {
			case BufferedImage.TYPE_INT_ARGB:
				for (int a = 0; a < width; a++) {
					dest[4 * a + 2] = (byte) ((intScratch[a] >> 16) & 0xff);
					dest[4 * a + 1] = (byte) ((intScratch[a] >> 8) & 0xff);
					dest[4 * a] = (byte) ((intScratch[a]) & 0xff);
					dest[4 * a + 3] = (byte) ((intScratch[a] >> 24) & 0xff);
				}
				break;
			case BufferedImage.TYPE_INT_ARGB_PRE:
				for (int a = 0; a < width; a++) {
					int alpha = (intScratch[a] >> 24) & 0xff;
					if (alpha > 0 && alpha < 255) {
						dest[4 * a] = (byte) (((intScratch[a]) & 0xff) * 255 / alpha);
						dest[4 * a + 1] = (byte) (((intScratch[a] >> 8) & 0xff) * 255 / alpha);
						dest[4 * a + 2] = (byte) (((intScratch[a] >> 16) & 0xff) * 255 / alpha);
					} else {
						dest[4 * a] = (byte) ((intScratch[a]) & 0xff);
						dest[4 * a + 1] = (byte) ((intScratch[a] >> 8) & 0xff);
						dest[4 * a + 2] = (byte) ((intScratch[a] >> 16) & 0xff);
					}
					dest[4 * a + 3] = (byte) (alpha);
				}
				break;
			case BufferedImage.TYPE_INT_RGB:
				for (int a = 0; a < width; a++) {
					dest[4 * a + 2] = (byte) ((intScratch[a] >> 16) & 0xff);
					dest[4 * a + 1] = (byte) ((intScratch[a] >> 8) & 0xff);
					dest[4 * a] = (byte) ((intScratch[a]) & 0xff);
					dest[4 * a + 3] = -1;
				}
				break;
			case BufferedImage.TYPE_INT_BGR:
				for (int a = 0; a < width; a++) {
					dest[4 * a + 2] = (byte) ((intScratch[a]) & 0xff);
					dest[4 * a + 1] = (byte) ((intScratch[a] >> 8) & 0xff);
					dest[4 * a] = (byte) ((intScratch[a] >> 16) & 0xff);
					dest[4 * a + 3] = -1;
				}
				break;
			default:
				throw new RuntimeException("Unrecognized type ("
						+ BufferedImageIterator.getTypeName(originalType) + ")");
			}
		}
	}

	public IndexColorModel getIndexColorModel() {
		return null;
	}

	public int getMinimumArrayLength() {
		if (byteIterator != null) {
			return Math.max(byteIterator.getMinimumArrayLength(),
					4 * getWidth());
		}
		return 4 * getWidth();
	}

	public int getType() {
		return BufferedImage.TYPE_4BYTE_ABGR;
	}

	public int getPixelSize() {
		return 4;
	}
}
