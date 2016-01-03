/*
 * @(#)GlyphCreationUI.java
 *
 * $Date: 2014-11-27 07:37:57 +0100 (Do, 27 Nov 2014) $
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
package com.bric.awt.text.writing.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.event.MouseInputAdapter;

import com.bric.animation.writing.WritingShape;
import com.bric.animation.writing.WritingStroke;
import com.bric.awt.text.writing.WritingFont;
import com.bric.geom.EmptyPathException;
import com.bric.geom.ShapeBounds;
import com.bric.geom.TransformUtils;
import com.bric.plaf.CubicPathCreationUI;
import com.bric.swing.ShapeCreationPanel;
import com.bric.swing.ShapeCreationPanel.DataModelListener;

/** A shape creation UI designed to support glyphs in a WritingFont.
 * 
 */
public class GlyphCreationUI extends CubicPathCreationUI {
	
	Settings settings;
	ShapeCreationPanel panel;
	
	int adjusting = 0;
	
	int left, top, bottom, right, width, height;
	WritingShape shape;
	WritingApp writingApp;
	
	public static enum Handle {
		WIDTH, MEDIAN, DESCENT
	}

	Handle selectedHandle, indicatedHandle;
	MouseInputAdapter handleMouseListener = new MouseInputAdapter() {

		public void mouseMoved(MouseEvent e) {
			ShapeCreationPanel scp = (ShapeCreationPanel)e.getComponent();
			Handle prevIndicatedHandle = indicatedHandle;
			indicatedHandle = getHandle(e);
			if(indicatedHandle!=null) {
				e.consume();
				scp.repaint();
			} else if(indicatedHandle!=prevIndicatedHandle) {
				scp.repaint();
			}
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			if(selectedHandle!=null) {
				selectedHandle = null;
				e.consume();
				e.getComponent().repaint();
			}
		}
		
		@Override
		public void mousePressed(MouseEvent e) {
			ShapeCreationPanel scp = (ShapeCreationPanel)e.getComponent();
			scp.requestFocus();

			Handle prevSelectedHandle = selectedHandle;
			selectedHandle = getHandle(e);
			if(selectedHandle!=null) {
				e.consume();
				scp.repaint();
			} else if(selectedHandle!=prevSelectedHandle) {
				scp.repaint();
			}
		}
		
		private Handle getHandle(MouseEvent e) {
			ShapeCreationPanel scp = (ShapeCreationPanel)e.getComponent();

			char ch = settings.get(Settings.SELECTED_CHAR);
			WritingFont font = settings.get(Settings.WRITING_FONT);
			WritingShape glyph = font==null ? null : font.getGlyph(ch);
			
			if(glyph==null) return null;
			Rectangle2D bounds = glyph.getBounds();
			AffineTransform tx = scp.getTransform();
			Point2D abstractEdge = new Point2D.Double(bounds.getWidth() / bounds.getHeight(), 1);
			Point2D panelEdge = tx.transform(abstractEdge, null);
			if(e.getPoint().distance(panelEdge)<=scp.getHandleSize()/2) {
				return Handle.WIDTH;
			}
			
			Point2D abstractMedian = new Point2D.Double( 0, bounds.getHeight() * (1-font.getProperty(WritingFont.MEDIAN)) );
			Point2D panelMedian = tx.transform(abstractMedian, null);
			if(e.getPoint().distance(panelMedian)<=scp.getHandleSize()/2) {
				return Handle.MEDIAN;
			}
			
			Point2D abstractDescent = new Point2D.Double( 0, bounds.getHeight() * (1+font.getProperty(WritingFont.DESCENT)) );
			Point2D panelDescent = tx.transform(abstractDescent, null);
			if(e.getPoint().distance(panelDescent)<=scp.getHandleSize()/2) {
				return Handle.DESCENT;
			}
			
			return null;
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if(selectedHandle!=null) {
				e.consume();

				ShapeCreationPanel scp = (ShapeCreationPanel)e.getComponent();
				Point2D p = new Point2D.Double(e.getX(), e.getY());
				try {
					scp.getTransform().createInverse().transform(p, p);
				} catch(NoninvertibleTransformException e2) {
					throw new RuntimeException(e2);
				}
				
				if(selectedHandle==Handle.WIDTH) {
					setWidth(p);
				} else if(selectedHandle==Handle.MEDIAN) {
					setMedian(p);
				} else if(selectedHandle==Handle.DESCENT) {
					setDescent(p);
				}
				e.getComponent().repaint();
			}
		}
		
		private void setWidth(Point2D abstractPoint) {
			char ch = settings.get(Settings.SELECTED_CHAR);
			WritingFont font = settings.get(Settings.WRITING_FONT);
			WritingShape glyph = font.getGlyph(ch);
			glyph.addPropertyChangeListener( boundsRefreshListener );
			Rectangle2D rect = glyph.getBounds();
			rect.setFrame(rect.getX(), rect.getY(), abstractPoint.getX() * rect.getHeight(), rect.getHeight());
			glyph.setBounds(rect);
			saveGlyph();
		}
		
		private void setMedian(Point2D abstractPoint) {
			WritingFont font = settings.get(Settings.WRITING_FONT);
			font.setProperty(WritingFont.MEDIAN, (float)(1-abstractPoint.getY()) );
		}
		
		private void setDescent(Point2D abstractPoint) {
			WritingFont font = settings.get(Settings.WRITING_FONT);
			font.setProperty(WritingFont.DESCENT, (float)(abstractPoint.getY()-1) );
		}
	};

	public GlyphCreationUI(WritingApp writingApp,ShapeCreationPanel shapePanel,Settings settings) {
		this.settings = settings;
		this.panel = shapePanel;
		this.writingApp = writingApp;
		
		panel.getDataModel().addListener(new DataModelListener() {

			public void shapeAdded(ShapeCreationPanel shapePanel,
					int shapeIndex, Shape shape) {
				if(adjusting>0) return;
				saveGlyph();
			}

			public void shapeRemoved(ShapeCreationPanel shapePanel,
					int shapeIndex, Shape shape) {
				if(adjusting>0) return;
				saveGlyph();
			}

			public void shapeChanged(ShapeCreationPanel shapePanel,
					int shapeIndex, Shape shape) {
				if(adjusting>0) return;
				saveGlyph();
			}
		});
		
		settings.addListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if(Settings.ONION_SKIN_ACTIVE.matches(evt) || 
						Settings.ONION_SKIN_FONT.matches(evt)) {
					panel.repaint();
				} else if( Settings.SELECTED_CHAR.matches(evt)) {
					updateGlyphConstants();
				} else if( Settings.WRITING_FONT.matches(evt)) {
					WritingFont font = GlyphCreationUI.this.settings.get(Settings.WRITING_FONT);
					font.addPropertyChangeListener( this );
					updateGlyphConstants();
				} else if( WritingFont.ITALICIZED_ANGLE.matches(evt) ||
						WritingFont.NIB_ANGLE.matches(evt) ||
						WritingFont.STYLE.matches(evt) ) {
					updateGlyphConstants();
				} else if(Settings.ANIMATION_ACTIVE.matches(evt) || 
						Settings.ANIMATION_TIME.matches(evt)) {
					panel.repaint();
				}
			}
		});
		panel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateSizeConstants();
			}
		});
		panel.setPreferredSize(new Dimension(320, 320));

		updateSizeConstants();
	}

	/** Extract the shape data model into a WritingShape for the WritingFont. */
	protected void saveGlyph() {
		WritingFont font = GlyphCreationUI.this.settings.get(Settings.WRITING_FONT);
		char ch = GlyphCreationUI.this.settings.get(Settings.SELECTED_CHAR);
		WritingShape glyph = font.getGlyph(ch);
		
		//we need to populate glyph with shapes:
		List<WritingStroke> strokes = glyph==null ? null : glyph.getStrokes();
		if(strokes!=null) {
			strokes.clear();
			Shape[] shapes = GlyphCreationUI.this.panel.getDataModel().getShapes();
			for(int a = 0; a<shapes.length; a++) {
				strokes.add(new WritingStroke(0, shapes[a]));
			}
		}
		
		writingApp.sampleTextArea.repaint();
	}
	
	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		c.addMouseListener(handleMouseListener);
		c.addMouseMotionListener(handleMouseListener);
	}

	@Override
	public void uninstallUI(JComponent c) {
		super.uninstallUI(c);
		c.removeMouseListener(handleMouseListener);
		c.removeMouseMotionListener(handleMouseListener);
	}
	
	private void updateSizeConstants() {
		left = panel.getWidth()*1/5;
		top = panel.getHeight()*1/5;
		bottom = panel.getHeight()*4/5;
		width = panel.getWidth();
		height = panel.getHeight();
		updateGlyphConstants();
	}
	
	AffineTransform transformAbstractToScreen = new AffineTransform();
	PropertyChangeListener boundsRefreshListener = new PropertyChangeListener() {
		public void propertyChange(PropertyChangeEvent evt) {
			if(WritingShape.BOUNDS_KEY.matches(evt)) {
				updateGlyphConstants();
			}
		}
	};
	
	private void updateGlyphConstants() {
		adjusting++;
		try {
			char ch = settings.get(Settings.SELECTED_CHAR);
			
			WritingFont font = settings.get(Settings.WRITING_FONT);
			if(font==null) {
				shape = null;
				right = left + (bottom - top);
			} else {
				shape = font.getGlyph(ch);
				Shape[] shapes;
				if(shape==null) {
					shapes = new Shape[] {};
					
					writingApp.animationController.setDuration( 10 );
				} else {
					shape.addPropertyChangeListener( boundsRefreshListener );
					List<WritingStroke> strokes = shape.getStrokes();
					shapes = new Shape[strokes.size()];
					for(int a = 0; a<shapes.length; a++) {
						shapes[a] = strokes.get(a).getShape();
					}
					
					writingApp.animationController.setDuration( shape.getDuration(1, 1) );
				}
				panel.getDataModel().setShapes(shapes);
				Rectangle2D rect = shape==null ? new Rectangle2D.Float(0,0,1,1) : shape.getBounds();
	
				right = (int)( left + (bottom - top)*rect.getWidth()/rect.getHeight());
				
				double scale = ((double)(bottom - top)) / rect.getHeight();
				transformAbstractToScreen.setToIdentity();
				transformAbstractToScreen.translate(left, top);
				transformAbstractToScreen.scale(scale, scale);
				
				panel.setTransform( transformAbstractToScreen );
			}
		} finally {
			adjusting--;
		}
		
		panel.repaint();
	}
	
	Stroke dottedStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10, new float[] { 4, 4}, 0);
	Stroke dottedStrokeLight = new BasicStroke(.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 10, new float[] { 4, 8}, 0);

	/** Return the playback time (in seconds) this should used for repainting, or null
	 * if we're not currently playing anything back.
	 */
	protected Float getPlaybackTime() {
		Boolean active = GlyphCreationUI.this.settings.get(Settings.ANIMATION_ACTIVE);
		if(active==null) active = false;
		Float time = GlyphCreationUI.this.settings.get(Settings.ANIMATION_TIME);
		if(time==null) time = 0f;
		if(active || writingApp.animationController.getSlider().hasFocus() ) {
			return time;
		} else {
			return null;
		}
	}
	
	@Override
	protected void paintShapes(Graphics2D g0, ShapeCreationPanel scp) {
		
		Graphics2D g = (Graphics2D)g0.create();
		Float playbackTime = getPlaybackTime();
		try {
			g.setColor(Color.white);
			g.fillRect(0,0,panel.getWidth(),panel.getHeight());
			
			g.setColor(Color.blue);
			g.setStroke(dottedStroke);
			g.drawLine( left, 0, left, height);
			g.drawLine( 0, top, width, top);
			g.drawLine( 0, bottom, width, bottom);
			
	
			boolean onionSkin = settings.get(Settings.ONION_SKIN_ACTIVE);
			if(onionSkin && playbackTime==null) {
				paintOnionSkin(g, top, left, bottom-top);
			}
			
			WritingFont font = settings.get(Settings.WRITING_FONT);
			if(font!=null) {
				Number italicizedAngle = font.getProperty(WritingFont.ITALICIZED_ANGLE);
				if(italicizedAngle==null) italicizedAngle = Math.PI/2;
				
				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
				
				double t = italicizedAngle.doubleValue();
				if(Math.abs(Math.sin(2*t))>.01) {
					Line2D italicizedLine = new Line2D.Double(left-1000*Math.cos(t), bottom-1000*Math.sin(t), 
							left+1000*Math.cos(t), bottom+1000*Math.sin(t));
					g.draw(italicizedLine);
				};
				Line2D rightLine = new Line2D.Double(right-1000*Math.cos(t), bottom-1000*Math.sin(t), 
						right+1000*Math.cos(t), bottom+1000*Math.sin(t));
				g.draw(rightLine);
				
				int medianY = (int)( bottom - (bottom-top)*font.getProperty(WritingFont.MEDIAN) );
				int descentY = (int)( bottom + (bottom-top)*font.getProperty(WritingFont.DESCENT) );
				
				g.setStroke(dottedStrokeLight);
				Line2D medianLine = new Line2D.Double(0, medianY, scp.getWidth(), medianY);
				g.draw(medianLine);

				Line2D descentLine = new Line2D.Double(0, descentY, scp.getWidth(), descentY);
				g.draw(descentLine);
				
				paintHandle(g, scp, right, bottom, Handle.WIDTH);
				paintHandle(g, scp, left, 
						medianY, 
						Handle.MEDIAN);
				paintHandle(g, scp, left, 
						(int)( bottom + (bottom-top)*font.getProperty(WritingFont.DESCENT) ), 
						Handle.DESCENT);
			}
		} finally {
			g.dispose();
		}	
		
		if(playbackTime==null) {
			super.paintShapes(g0, scp);
		} else {
			Graphics2D g2 = (Graphics2D)g0.create();
			try {
				char ch = settings.get(Settings.SELECTED_CHAR);
				WritingFont font = settings.get(Settings.WRITING_FONT);
				WritingShape glyph = font.getGlyph(ch);
				g2.setStroke(getStroke(panel, 0));
				glyph.paint(g2,
						new Rectangle2D.Double(left, top, right - left, bottom - top),
						playbackTime.floatValue(),
						1, //beats per second
						1); //pixels per second
			} finally {
				g2.dispose();
			}
		}
	}
	
	private void paintHandle(Graphics2D g,ShapeCreationPanel scp,int x,int y,Handle handle) {		
		g.setStroke(new BasicStroke(1));
		g.setColor(Color.blue);
		double r = scp.getHandleSize()/2;

		Ellipse2D ellipse = new Ellipse2D.Double(x - r, y - r, 2*r, 2*r);
		g.draw(ellipse);

		if(selectedHandle==handle) {
			g.setColor(new Color(0,0,255,200));
			g.fill(ellipse);
		} else if(indicatedHandle==handle) {
			g.setColor(new Color(0,0,255,100));
			g.fill(ellipse);
		}
	}
	
	@Override
	public Stroke getStroke(ShapeCreationPanel panel,int shapeIndex) {
		Stroke stroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
		WritingFont font = settings.get(Settings.WRITING_FONT);
		if(font!=null) {
			stroke = font.getRecommendedStroke(bottom-top);
		}
		return stroke;
	}
	
	protected void paintOnionSkin(Graphics2D g0,int top,int left,int targetHeight) {
		Graphics2D g = (Graphics2D)g0.create();
		Font font = settings.get(Settings.ONION_SKIN_FONT);
		try {
			GlyphVector sample = font.createGlyphVector( g.getFontRenderContext(),
					"X" );
			Shape sampleShape = sample.getOutline();
			GlyphVector glyph = font.createGlyphVector( g.getFontRenderContext(),
					""+settings.get(Settings.SELECTED_CHAR) );
			Shape shape = glyph.getOutline();
			Rectangle2D sampleBounds = ShapeBounds.getBounds(sampleShape);
			double scale = targetHeight / sampleBounds.getHeight();
			g.setColor(new Color(0,0,255,20));
			g.transform( TransformUtils.createAffineTransform(
					sampleBounds,
					new Rectangle2D.Double(left, top, sampleBounds.getWidth()*scale, targetHeight)
					) );
			g.fill(shape);
		} catch(EmptyPathException e) {
			//do nothing
		}
		g.dispose();
	}
}
