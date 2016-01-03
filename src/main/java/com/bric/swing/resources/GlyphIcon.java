/*
 * @(#)GlyphIcon.java
 *
 * $Date: 2015-12-26 19:04:34 +0100 (Sa, 26 Dez 2015) $
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
package com.bric.swing.resources;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.Icon;

import com.bric.blog.ResourceSample;
import com.bric.geom.EmptyPathException;
import com.bric.geom.ShapeBounds;
import com.bric.image.pixel.Scaling;


/** A silhouette icon of a character glyph.
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p>Here are some samples:
 * <table summary="Resource&#160;Samples&#160;for&#160;com.bric.swing.resources.GlyphIcon"><tr>
 * <td></td>
 * <td><img src="https://javagraphics.java.net/resources/samples/GlyphIcon/Note.png" alt="com.bric.swing.resources.GlyphIcon.NOTE"></td>
 * <td><img src="https://javagraphics.java.net/resources/samples/GlyphIcon/Recycle.png" alt="com.bric.swing.resources.GlyphIcon.RECYCLE"></td>
 * <td><img src="https://javagraphics.java.net/resources/samples/GlyphIcon/Writing Hand.png" alt="com.bric.swing.resources.GlyphIcon.WRITING_HAND"></td>
 * <td><img src="https://javagraphics.java.net/resources/samples/GlyphIcon/Flower.png" alt="com.bric.swing.resources.GlyphIcon.FLOWER"></td>
 * </tr><tr>
 * <td></td>
 * <td>Note</td>
 * <td>Recycle</td>
 * <td>Writing Hand</td>
 * <td>Flower</td>
 * </tr><tr>
 * <td></td>
 * <td><img src="https://javagraphics.java.net/resources/samples/GlyphIcon/Warning.png" alt="com.bric.swing.resources.GlyphIcon.WARNING"></td>
 * </tr><tr>
 * <td></td>
 * <td>Flower</td>
 * <td>Warning</td>
 * </tr><tr>
 * </tr></table>
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 */
@ResourceSample (
	sample = { "com.bric.swing.resources.GlyphIcon.NOTE",
			"com.bric.swing.resources.GlyphIcon.RECYCLE",
			"com.bric.swing.resources.GlyphIcon.WRITING_HAND",
			"com.bric.swing.resources.GlyphIcon.FLOWER",
			"com.bric.swing.resources.GlyphIcon.WARNING" },
	names = {"Note", "Recycle", "Writing Hand", "Flower", "Warning"}
)
public class GlyphIcon implements Icon {

	/** A glyph of 
	 * <a href="http://www.fileformat.info/info/unicode/char/266B/index.htm">unicode char 0x266b</a>: "BEAMED EIGHTH NOTES"
	 */
	public static final GlyphIcon NOTE = create(
			new Font("default",0,140),
			'\u266B',
			32,
			new Color(0,0,0,80) );

	/** A glyph of 
	 * <a href="http://www.fileformat.info/info/unicode/char/267A/index.htm">unicode char 0x267A</a>: "RECYCLING SYMBOL FOR GENERIC MATERIALS"
	 */
	public static final GlyphIcon RECYCLE = create(
			new Font("default",0,140),
			'\u267A',
			32,
			new Color(0,0,0,80) );

	/** A glyph of 
	 * <a href="http://www.fileformat.info/info/unicode/char/270D/index.htm">unicode char 0x270D</a>: "WRITING HAND"
	 */
	public static final GlyphIcon WRITING_HAND = create(
			new Font("default",0,80),
			'\u270D',
			32,
			new Color(0,0,0,80) );

	/** A glyph of 
	 * <a href="http://www.fileformat.info/info/unicode/char/2740/index.htm">unicode char 0x2740</a>: "WHITE FLORETTE"
	 */
	public static final GlyphIcon FLOWER = create(
			new Font("default",0,80),
			'\u2740',
			32,
			new Color(0,0,0,80) );

	/** A glyph of 
	 * <a href="http://www.fileformat.info/info/unicode/char/26A0/index.htm">unicode char 0x26A0</a>: "WARNING SIGN"
	 */
	public static final GlyphIcon WARNING = create(
			new Font("default",0,80),
			'\u26A0',
			32,
			new Color(0,0,0,80) );

	/** Create a GlyphIcon.
	 * 
	 * @param font the font to extract a glyph from.
	 * @param ch the character to retrieve the glyph of.
	 * @param maxSize the maximum width or height. The glyph will be scaled
	 * proportionally to fit within a square that is (maxSize x maxSize) pixels
	 * @param color the color to paint the glyph in.
	 */
	private static GlyphIcon create(Font font, char ch, int maxSize, Color color) {
		try {
			return new GlyphIcon(font, ch, maxSize, color);
		} catch(RuntimeException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	int w, h;
	final Color color;
	final Shape untransformedShape;
	
	/** Create a GlyphIcon.
	 * 
	 * @param font the font to extract a glyph from.
	 * @param ch the character to retrieve the glyph of.
	 * @param maxSize the maximum width or height. The glyph will be scaled
	 * proportionally to fit within a square that is (maxSize x maxSize) pixels
	 * @param color the color to paint the glyph in.
	 */
	public GlyphIcon(Font font, char ch, int maxSize, Color color) {
		FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
		untransformedShape = font.createGlyphVector(frc, new char[] {ch}).getOutline();
		Dimension d = getPreferredSize(maxSize, maxSize);
		this.w = d.width;
		this.h = d.height;
		this.color = color;
	}
	
	/** Return the preferred size of this icon if it were scaled
	 * to fit within (possibleWidth x possibleHeight).
	 * 
	 * @param possibleWidth the maximum possible width.
	 * @param possibleHeight the maximum possible height.
	 * @return the dimensions this icon should be scaled to. Either
	 * the width or the height should approximately match the arguments.
	 */
	public Dimension getPreferredSize(int possibleWidth,int possibleHeight) {
		try {
			Rectangle2D b = ShapeBounds.getBounds(untransformedShape);
			Dimension originalSize = new Dimension( 
					Math.max(1, (int)(b.getWidth()+.5)), 
					Math.max(1, (int)(b.getHeight()+.5)) );
			Dimension maxSize = new Dimension(possibleWidth, possibleHeight);
			return Scaling.scaleDimensionsProportionally(originalSize, maxSize);
		} catch(EmptyPathException e) {
			return new Dimension(possibleWidth, possibleHeight);
		}
	}

	public void paintIcon(Component c, Graphics g, int dx, int dy) {
		paintIcon(c, g, dx, dy, w, h);
	}
	
	/** Paint this icon so it stretches to a fixed with and height.
	 */
	public void paintIcon(Component c, Graphics g, int dx, int dy,int w,int h) {
		Graphics2D g2 = (Graphics2D)g.create();
		try {
			Rectangle2D b = ShapeBounds.getBounds(untransformedShape);
			double sx = ((double)w)/((double)b.getWidth());
			double sy = ((double)h)/((double)b.getHeight());
			g2.translate( dx, dy );
			g2.scale( sx, sy);
			g2.translate( - b.getX(), - b.getY());
			g2.setColor(color);
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.fill(untransformedShape);
		} catch(EmptyPathException e) {
			//do nothing
		} finally {
			g2.dispose();
		}
	}
	
	public int getIconWidth() {
		return w;
	}
	public int getIconHeight() {
		return h;
	}
	
}