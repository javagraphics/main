/*
 * @(#)ExploratoryGifEncoder.java
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import com.bric.animation.AnimationReader;
import com.bric.image.gif.block.GifGraphicControlExtension;
import com.bric.image.gif.block.GifGraphicControlExtension.DisposalMethod;
import com.bric.image.gif.block.GifImageDataBlock;
import com.bric.image.gif.block.GifImageDescriptor;
import com.bric.image.gif.block.GifLocalColorTable;
import com.bric.image.pixel.quantize.ColorSet;

/** This creates gifs that explore different combinations of
 * disposal methods.
 * <p>Here is a table that features the combinations of different
 * disposal methods using the same common graphics:
 * 
 * <p><table summary="Animated Samples of Gif Disposal Methods">
 * <tr><td></td><td>NONE</td>
 * <td>LEAVE</td>
 * <td>PREVIOUS</td>
 * <td>RESTORE</td></tr>
 * <tr><td>
 * NONE
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/NONE-NONE.gif" alt="none, none">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/NONE-LEAVE.gif" alt="none, leave">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/NONE-PREVIOUS.gif" alt="none, previous">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/NONE-RESTORE_BACKGROUND.gif" alt="none, restore">
 * </td></tr>
 * <tr><td>
 * LEAVE
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/LEAVE-NONE.gif" alt="leave, none">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/LEAVE-LEAVE.gif" alt="leave, leave">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/LEAVE-PREVIOUS.gif" alt="leave, previous">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/LEAVE-RESTORE_BACKGROUND.gif" alt="leave, restore">
 * </td></tr>
 * <tr><td>
 * PREVIOUS
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/PREVIOUS-NONE.gif" alt="previous, none">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/PREVIOUS-LEAVE.gif" alt="previous, leave">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/PREVIOUS-PREVIOUS.gif" alt="previous, previous">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/PREVIOUS-RESTORE_BACKGROUND.gif" alt="previous, restore">
 * </td></tr>
 * <tr><td>
 * RESTORE
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/RESTORE_BACKGROUND-NONE.gif" alt="restore, none">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/RESTORE_BACKGROUND-LEAVE.gif" alt="restore, leave">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/RESTORE_BACKGROUND-PREVIOUS.gif" alt="restore, previous">
 * </td>
 * <td>
 * <img src="https://javagraphics.java.net/resources/gif/disposal/RESTORE_BACKGROUND-RESTORE_BACKGROUND.gif" alt="restore, restore">
 * </td></tr>
 * </table>
 * <p>In my opinion the two most important questions this answers are:
 * <ul><li>What is the difference between "leave" and "none"? (Answer: nothing.)</li>
 * <li>Does "restore background" restore the entire frame? (Answer: no.)</li></ul>
 * <p>Also this program creates another set of 16 similar graphics that vary the background color index of each
 * frame. Surprisingly: this has no affect if there is a transparent pixel. (So even if you ask a frame to
 * restore to the background index, and provide a background color index (mapping to orange), then the
 * frame disposals by resetting the pixels to transparent -- not orange.)
 * 
 * @see GifGraphicControlExtension.DisposalMethod
 *
 */
public class ExploratoryGifEncoder extends GifEncoder {
	
	public static void main(String[] args) {
		try {
			for(int a = 0; a<2; a++) {
				boolean includeOrange = a==1;
				ColorSet colorSet = new ColorSet();
				colorSet.addColor(255, 0, 0);
				colorSet.addColor(Color.orange);
				colorSet.addColor(0, 255, 0);
				colorSet.addColor(0, 0, 255);
				colorSet.addColor(0, 0, 0);
				IndexColorModel icm = colorSet.createIndexColorModel(true, true);
				
				//the height of bars
				int barHeight = 4;
				
				BufferedImage img1 = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_INDEXED, icm);
				{
					Graphics2D g = img1.createGraphics();
					g.setColor(Color.red);
					for(int x = 0; x<img1.getWidth(); x+=2*barHeight) {
						g.fillRect(x, 0, barHeight, img1.getHeight());
					}
					if(includeOrange) {
						g.setColor(Color.orange);
						g.setStroke(new BasicStroke(3));
						g.drawLine(0,0,img1.getWidth(),img1.getHeight());
					}
					g.dispose();
				}
				BufferedImage img2 = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_INDEXED, icm);
				{
					Graphics2D g = img2.createGraphics();
					g.setColor(Color.blue);
					for(int y = 0; y<img1.getHeight(); y+=2*barHeight) {
						g.fillRect(0, y, img2.getWidth(), barHeight);
					}
					if(includeOrange) {
						g.setColor(Color.orange);
						g.setStroke(new BasicStroke(3));
						g.drawLine(img1.getWidth(),0,0,img1.getHeight());
					}
					g.dispose();
				}
				BufferedImage img3 = new BufferedImage(64, 64, BufferedImage.TYPE_BYTE_INDEXED, icm);
				{
					Graphics2D g = img3.createGraphics();
					g.setColor(Color.green);
					g.setStroke(new BasicStroke(barHeight*3/4));
					for(int y = 0; y<2*img1.getHeight(); y+=2*barHeight) {
						g.drawLine(0, y, y, 0);
					}
					if(includeOrange) {
						g.setColor(Color.orange);
						g.setStroke(new BasicStroke(3));
						g.drawLine(img1.getWidth()/2,0,img1.getWidth()/2,img1.getHeight());
					}
					g.dispose();
				}
				
				DisposalMethod[] methods = DisposalMethod.values();
				
				for(int m1 = 0; m1<methods.length; m1++) {
					for(int m2 = 0; m2<methods.length; m2++) {
						write(img1, methods[m1], img2, methods[m2], img3, includeOrange);
					}
				}
			}
		} catch(Throwable t) {
			t.printStackTrace();
		}
	}
	
	private static void write(BufferedImage img1,DisposalMethod m1,
			BufferedImage img2,DisposalMethod m2,
			BufferedImage img3,boolean useOrangeAsBackground) throws IOException {
		BufferedImage bi1 = label(img1, m1.toString());
		BufferedImage bi2 = label(img2, m2.toString());
		BufferedImage bi3 = img3;
		
		IndexColorModel icm = (IndexColorModel)img1.getColorModel();

		String filename;
		if(useOrangeAsBackground) {
			filename = "BKGND-"+m1.toString()+"-"+m2.toString()+".gif";
		} else {
			filename = m1.toString()+"-"+m2.toString()+".gif";
		}
		File file = new File(filename);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			ExploratoryGifEncoder encoder = new ExploratoryGifEncoder(
					new Instruction(m1, new Point(0,0)),
					new Instruction(m2, new Point(16,16)),
					new Instruction(DisposalMethod.NONE, new Point(32,32)) 
			);
			
			int backgroundIndex = icm.getTransparentPixel();
			if(useOrangeAsBackground) {
				for(int a = 0; a<icm.getMapSize(); a++) {
					if(Color.orange.getRed()==icm.getRed(a) &&
							Color.orange.getGreen()==icm.getGreen(a) &&
							Color.orange.getBlue()==icm.getBlue(a) ) {
						backgroundIndex = a;
					}
				}
			}
			GifWriter writer = new GifWriter(out,
					new Dimension(96, 96),
					icm, 
					AnimationReader.LOOP_FOREVER,
					backgroundIndex,
					encoder);
			
			writer.write(bi1, 4000, null);
			writer.write(bi2, 4000, null);
			writer.write(bi3, 4000, null);
			
			writer.close(true);
		} finally {
			if(out!=null) {
				out.close();
			}
		}
	}
	
	private static BufferedImage label(BufferedImage src,String label) {
		BufferedImage newImage = new BufferedImage(src.getWidth(), src.getHeight() + 16, src.getType(), (IndexColorModel)src.getColorModel());
		Graphics2D g = newImage.createGraphics();
		g.drawImage(src, 0, 0, null);
		g.setColor(Color.black);
		g.setFont(new Font("Verdana", 0, 12));
		g.drawString(label, 2, src.getHeight() + 12);
		g.dispose();
		return newImage;
	}
	
	static class Instruction {
		Point offset;
		DisposalMethod method;
		
		public Instruction(DisposalMethod m,Point p) {
			if(m==null) throw new NullPointerException();
			if(p==null) throw new NullPointerException();
			this.method = m;
			offset = p;
		}
	}
	
	Instruction[] instructions;
	
	ExploratoryGifEncoder(Instruction... instr) {
		this.instructions = instr;
	}

	int frameIndex = 0;
	@Override
	public void writeImage(OutputStream out, BufferedImage image,
			int frameDurationInCentiseconds, IndexColorModel globalColorModel,
			boolean writeLocalColorTable) throws IOException {

		if (frameDurationInCentiseconds < 0)
			frameDurationInCentiseconds = 0;
		GifGraphicControlExtension gce = new GifGraphicControlExtension(
				frameDurationInCentiseconds,
				instructions[frameIndex].method,
				globalColorModel.getTransparentPixel());
		gce.write(out);
	
		int localColorSize = 0;
		if (writeLocalColorTable) {
			localColorSize = globalColorModel.getMapSize();
			int k = 2;
			while (localColorSize > k) {
				k *= 2;
			}
			if (k > 256)
				throw new IllegalArgumentException("Illegal number of colors ("
						+ localColorSize + ").  There can only be 256 at most.");
			localColorSize = k;
		}

		Dimension d = new Dimension(image.getWidth(), image.getHeight());

		GifImageDescriptor id = new GifImageDescriptor(
				instructions[frameIndex].offset.x,
				instructions[frameIndex].offset.y,
				d.width, d.height,
				false, localColorSize);
		id.write(out);
		if (localColorSize > 0) {
			GifLocalColorTable ct = new GifLocalColorTable(globalColorModel);
			ct.write(out);
		}
		GifImageDataBlock dataBlock = new GifImageDataBlock(image, globalColorModel);
		dataBlock.write(out);
		frameIndex++;
	}

	@Override
	public void flush(OutputStream out) throws IOException {}

}
