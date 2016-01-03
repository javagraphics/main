/*
 * @(#)HSBColorPaletteUI.java
 *
 * $Date: 2014-06-04 13:36:30 +0200 (Mi, 04 Jun 2014) $
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

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JComponent;
import javax.swing.JScrollBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import com.bric.awt.ModifierTracker;
import com.bric.math.MutableInteger;
import com.bric.swing.ColorPalette;

public class HSBColorPaletteUI extends ScrollableColorPaletteUI {
	
	/** The client property that is associated with how the palette is vertically padded.
	 * This refers to whether the zeroeth and/or the last vertical cell represent a complete
	 * saturation/desaturation, or whether they are slightly offset.
	 */
	public static final String PALETTE_PADDING_PROPERTY = "palettePadding";

	/** A possible value for the {@link #PALETTE_PADDING_PROPERTY} . */
	public static final String PALETTE_PADDING_BOTH = "both";
	
	/** A possible value for the {@link #PALETTE_PADDING_PROPERTY} . */
	public static final String PALETTE_PADDING_SATURATED = "saturated";
	
	/** A possible value for the {@link #PALETTE_PADDING_PROPERTY} . */
	public static final String PALETTE_PADDING_NONSATURATED = "nonsaturated";
	
	private static final HueDistribution PLAIN_HUES = new HueDistribution( new float[] {0, 1} );
	final int rows;
	final int columns;
	final HueDistribution hues;

	public static ComponentUI createUI(JComponent jc) {
		return new HSBColorPaletteUI();
	}
	
	static PropertyChangeListener updateColorPropertyListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			ColorPalette cp = (ColorPalette)evt.getSource();
			ColorPaletteUI ui = cp.getUI();
			ui.updateColor(cp);
		}
	};
	
	static class ModifierChangeListener implements ChangeListener {
		ColorPalette cp;
		
		public ModifierChangeListener(ColorPalette cp) {
			this.cp = cp;
		}
		
		public void stateChanged(ChangeEvent e) {
			cp.repaint();
		}
	}
	
	public HSBColorPaletteUI() {
		this(PLAIN_HUES,8,12);
	}
	
	public HSBColorPaletteUI(HueDistribution hue,int rows,int columns) {
		this.rows = rows;
		this.columns = columns;
		this.hues = hue;
	}
	
	@Override
	protected int getHorizontalScrollMax(ColorPalette cp) {
		return 20;
	}

	@Override
	protected int getVerticalScrollMax(ColorPalette cp) {
		return 20;
	}
	
	@Override
	protected ColorSet getColorSet(ColorPalette cp) {
		return getColorSet(cp,isGrid());
	}

	protected ColorSet getColorSet(ColorPalette cp,boolean grid) {
		float alpha = 1-(getHorizontalScrollValue(cp))/20f;
		float saturation = 1-(getVerticalScrollValue(cp))/20f;
		return new HBColors(hues, grid, rows, columns, saturation, alpha, (String)cp.getClientProperty(PALETTE_PADDING_PROPERTY));
	}

	@Override
	public void paint(Graphics g, JComponent c) {
		updateScrollBarBounds((ColorPalette)c);
		
		super.paint(g, c);
	}
	
	protected boolean isGrid() {
		return !ModifierTracker.isAltDown();
	}
	
	/** This will reset this UI so it features the color provided.
	 */
	@Override
	protected void updateColor(ColorPalette cp) {

		HSBColorPaletteUI ui = (HSBColorPaletteUI)cp.getUI();

		JScrollBar hBar = ui.getHorizontalScrollBar(cp);
		JScrollBar vBar = ui.getVerticalScrollBar(cp);
		
		Color color = cp.getColor();
		float[] hsb = new float[3];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		
		HBColors colorSet = new HBColors(hues, true, rows, columns, hsb[1], 1, (String)cp.getClientProperty(PALETTE_PADDING_PROPERTY));
		Point2D newP = colorSet.getRelativePoint(color.getRGB());
		
		//if we're bleached past the point of having a hue, let's
		//tastefully pick our x-value:
		if(Math.abs(hsb[1])<.00001) {
			Point2D oldPoint = (Point2D)cp.getClientProperty(RELATIVE_POINT_PROPERTY);
			if(oldPoint!=null) {
				newP.setLocation( oldPoint.getX(), newP.getY());
			} else {
				//middle of the first column:
				newP.setLocation( .5f/(columns), newP.getY() );
			}
		}
		
		int alpha = color.getAlpha();
		int hMax = hBar.getMaximum();
		int hValue = (255-alpha)*hMax/255;
		
		int vMax = vBar.getMaximum();
		int vValue = (int)( (1-hsb[1])*vMax );
		
		
		MutableInteger adjusting = getAdjustingLock(cp);
		adjusting.value++;
		try {
			Boolean autoScroll = (Boolean)cp.getClientProperty("autoScroll");
			if(autoScroll==null) autoScroll = Boolean.TRUE;
			if(autoScroll.booleanValue()) {
				hBar.setValue( hValue );
				vBar.setValue( vValue );
			}
			cp.putClientProperty(RELATIVE_POINT_PROPERTY, newP);
		} finally {
			adjusting.value--;
		}
	}

	
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		
		ModifierChangeListener modifierListener = new ModifierChangeListener( (ColorPalette)c );
		c.putClientProperty("modifierListener",modifierListener);
		ModifierTracker.addAltChangeListener(modifierListener);
		
		Color color = UIManager.getColor("Panel.background");
		if(color==null)
			color = Color.white;
		c.setForeground(color);
		
		ColorPalette cp = (ColorPalette)c;
		cp.addPropertyChangeListener(PALETTE_PADDING_PROPERTY, repaintPropertyListener);
		cp.addPropertyChangeListener(PALETTE_PADDING_PROPERTY, updateColorPropertyListener);
	}

	@Override
	protected void scroll(ColorPalette cp, int dx, int dy) {
		Point2D p = (Point2D)cp.getClientProperty(RELATIVE_POINT_PROPERTY);
		Color oldPointColor = null;
		if(p!=null) {
			oldPointColor = new Color( getColorSet(cp).getRGB( (float)p.getX(), (float)p.getY() ), true );
			Color oldPaletteColor = cp.getColor();
			int ds = getRGBDistanceSquared( oldPointColor.getRGB(), oldPaletteColor.getRGB());
			if( ds > 20 ) {
				p = null;
			}
		}
		
		
		super.scroll(cp, dx, dy);
		
		if(p!=null) {
			int argb = getColorSet(cp).getRGB( (float)p.getX() , (float)p.getY() );
			MutableInteger adjusting = getAdjustingLock(cp);
			adjusting.value++;
			try {
				cp.setColor( new Color(argb,true) );
			} finally {
				adjusting.value--;
			}
		}
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		
		ModifierChangeListener modifierListener = (ModifierChangeListener)c.getClientProperty("shiftListener");
		ModifierTracker.removeShiftChangeListener(modifierListener);

		ColorPalette cp = (ColorPalette)c;
		cp.removePropertyChangeListener(PALETTE_PADDING_PROPERTY, repaintPropertyListener);
	}
	
	static class HBColors extends ColorSet {
		final float saturation;
		final float alpha;
		final float yHeight;
		final HueDistribution hues;
		
		/**
		 * 
		 * @param hueDistribution
		 * @param grid
		 * @param rows
		 * @param columns
		 * @param saturation
		 * @param alpha
		 * @param padding One of the PALETTE_PADDING_PROPERTY values.
		 */
		public HBColors(HueDistribution hueDistribution,boolean grid,int rows,int columns,float saturation,float alpha,String padding) {
			super(grid,rows,columns);
			this.hues = hueDistribution;
			this.saturation = saturation;
			this.alpha = alpha;
			
			float rowHeight = 1f/(rows);
			
			boolean saturatedPadding = false;
			boolean nonsaturatedPadding = false;
			if(PALETTE_PADDING_BOTH.equals(padding)) {
				saturatedPadding = true;
				nonsaturatedPadding = true;
			} else if(PALETTE_PADDING_SATURATED.equals(padding)) {
				saturatedPadding = true;
			} else if(PALETTE_PADDING_NONSATURATED.equals(padding)) {
				nonsaturatedPadding = true;
			}
			
			if(saturatedPadding==false && nonsaturatedPadding==false) {
				yHeight = 1;
			} else if(saturatedPadding && nonsaturatedPadding) {
				yHeight = (1-rowHeight);
			} else if(saturatedPadding==false && nonsaturatedPadding) {
				yHeight = (1-rowHeight)*(1-saturation)+1*(saturation);
			} else {
				yHeight = (1-rowHeight)*(saturation)+1*(1-saturation);
			}
		}

		@Override
		protected float getHighlightAlpha() {
			return alpha;
		}

		@Override
		public Point2D getRelativePoint(int rgb) {
			float[] hsb = new float[3];
			Color c = new Color(rgb);
			Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
			float hue = hues.evaluateInverse(hsb[0]);
			hue = hue+(.5f)/(columns);
			hue = hue%1f;
			float xFraction = hue;
			
			float brightness = hsb[2];
			
			float yFraction = (1-brightness)*yHeight+(1-yHeight)/2;
			
			return new Point2D.Float(xFraction, yFraction);
		}

		@Override
		public int getRGB(float xFraction, float yFraction) {
			float hue = xFraction;
			hue = hue-(.5f)/(columns);
			hue = (hue+1)%1;
			hue = hues.evaluate( hue );
			
			float brightness = (1-yFraction)/yHeight-(1-yHeight)/2;
			if(brightness<0) brightness = 0;
			if(brightness>1) brightness = 1;
			
			int rgb = Color.HSBtoRGB(hue, saturation, brightness);
			int alphaInt = (int)(255*alpha);
	
			return (alphaInt << 24) + (rgb & 0xffffff);
		}
		
		@Override
		public String toString() {
			return "HBColors[ alpha = "+alpha+" saturation = "+saturation+" ]";
		}
	}
}
