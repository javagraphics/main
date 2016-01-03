/*
 * @(#)StarThrobberUI.java
 *
 * $Date: 2014-06-06 20:04:49 +0200 (Fr, 06 Jun 2014) $
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
package com.bric.plaf;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import javax.swing.JComponent;

/** This complicated <code>ThrobberUI</code> renders pivoting
 * lines of a common 5-point star. There are at least a dozen lines
 * rendered in each frame of varying levels of opacity, all moving.
 * The result is that the star is never fully rendered, but it is
 * visually identifiable.
 * <p><table summary="Sample Animations of StarThrobberUI" cellpadding="10"><tr>
 * <td><img src="https://javagraphics.java.net/resources/throbber/StarThrobberUI.gif" alt="StarThrobberUI"></td>
 * <td><img src="https://javagraphics.java.net/resources/throbber/StarThrobberUIx2.gif" alt="StarThrobberUI, Magnified 2x"></td>
 * <td><img src="https://javagraphics.java.net/resources/throbber/StarThrobberUIx4.gif" alt="StarThrobberUI, Magnified 4x"></td>
 * </tr></table>
 * <p>On installation: the component's foreground is set to dark gray,
 * but if that is changed then that color is used to render this animation.
 * <P>The default period for this animation is 900, but you can modify
 * this with the period client properties {@link ThrobberUI#PERIOD_KEY} or
 * {@link ThrobberUI#PERIOD_MULTIPLIER_KEY}.
 *
 */
public class StarThrobberUI extends ThrobberUI {
	
	public static final int DEFAULT_PERIOD = 900;

	public StarThrobberUI() {
		super(1000/100);
	}

	@Override
	protected void paintForeground(Graphics2D g, JComponent jc, Dimension size,Float fixedFraction) {
		float w = size.width;
		float h = size.height;

		float f;
		if(fixedFraction!=null) {
			f = fixedFraction;
		} else {
			int p = getPeriod(jc, DEFAULT_PERIOD);
			float t = System.currentTimeMillis()%p;
			f = t / p;
		}
		f = 1-f;
		
		Point2D[] tips = new Point2D[5];
		for(int a = 0; a<tips.length; a++) {
			double x = w/2 - Math.sin(a*2*Math.PI/tips.length);
			double y = h/2 - Math.cos(a*2*Math.PI/tips.length);
			tips[a] = new Point2D.Double(x, y);
		}

		AffineTransform tx = new AffineTransform();
		tx.translate(w/2, h/2);
		tx.scale( w/2, w/2);
		tx.translate(-w/2, -h/2);

		Color color = jc==null ? getDefaultForeground() : jc.getForeground();
		g.setColor(color);
		g.setStroke(new BasicStroke(1f));

		float max = 14;
		for(int j = 0; j<max; j++)  {
			float k = (f+j/max);
			int a = (int)((k*tips.length));
			float z = k*tips.length - a;
			drawLine(g, tips, z, tx, (a*3)%5, (float)Math.pow((j+1)/(max),1.5f) );
		}
	}

	private void drawLine(Graphics2D g, Point2D[] tips, float f,AffineTransform tx,int tipIndex,float alpha) {
		Point2D end = tween( tips[tipIndex], tips[(tipIndex+1)%tips.length], f );
		Point2D start = tips[(tipIndex+tips.length-2)%tips.length];

		GeneralPath path = new GeneralPath();
		path.moveTo( (float)start.getX(), (float)start.getY());
		path.lineTo( (float)end.getX(), (float)end.getY());
		path.transform(tx);
		
		g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
		g.draw(path);
	}

	private Point2D tween(Point2D a, Point2D b,float f) {
		return new Point2D.Double( a.getX()*(1-f)+b.getX()*f, a.getY()*(1-f)+b.getY()*f );
	}

	@Override
	public Color getDefaultForeground() {
		return Color.darkGray;
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(16, 16);
	}

}
