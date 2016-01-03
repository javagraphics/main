/*
 * @(#)LargeNavigationPanelUI.java
 *
 * $Date: 2015-12-26 08:54:45 +0100 (Sa, 26 Dez 2015) $
 *
 * Copyright (c) 2015 by Jeremy Wood.
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
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicButtonUI;

import com.bric.blog.ResourceSample;
import com.bric.swing.NavigationPanel;
import com.bric.swing.resources.FirstIcon;
import com.bric.swing.resources.LastIcon;
import com.bric.swing.resources.TriangleIcon;

/** This NavigationPanelUI contains large buttons and a slider.
 * 
 * <!-- ======== START OF AUTOGENERATED SAMPLES ======== -->
 * <p><img src="https://javagraphics.java.net/resources/samples/LargeNavigationPanelUI/sample.png" alt="com.bric.plaf.LargeNavigationPanelUI.createDemo()">
 * <!-- ======== END OF AUTOGENERATED SAMPLES ======== -->
 */
@ResourceSample( sample = {"com.bric.plaf.LargeNavigationPanelUI.createDemo()"} )
public class LargeNavigationPanelUI extends NavigationPanelUI {
	
	/** Create a minimal demo for the javadoc. */
	public static NavigationPanel createDemo() {
		NavigationPanel navPanel = new NavigationPanel(5, 10);
		navPanel.setUI( new LargeNavigationPanelUI() );
		return navPanel;
	}
	
	@Override
	public void paint(Graphics g0, JComponent c) {
		Graphics2D g = (Graphics2D)g0.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Shape roundRect = new RoundRectangle2D.Float(0,0,c.getWidth(),c.getHeight(),9,9);
		g.setColor(Color.black);
		g.fill(roundRect);
		g.dispose();
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
		NavigationPanel navPanel = (NavigationPanel)c;
		Context context = getContext(navPanel);

		navPanel.removeAll();
		navPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 1; gbc.weighty = 1;
		gbc.insets = new Insets(3,3,3,3);
		navPanel.add(context.firstButton, gbc);
		gbc.gridx++;
		navPanel.add(context.prevButton, gbc);
		gbc.gridx++;
		navPanel.add(context.nextButton, gbc);
		gbc.gridx++;
		navPanel.add(context.lastButton, gbc);
		gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = GridBagConstraints.REMAINDER;
		navPanel.add(context.slider, gbc);
		
		navPanel.setBorder(new EmptyBorder(3,3,3,3));
		navPanel.setSize(navPanel.getPreferredSize());
		
		format(context.prevButton);
		format(context.nextButton);
		format(context.firstButton);
		format(context.lastButton);

		context.prevButton.setIcon(new TriangleIcon(SwingConstants.WEST, 24, 24, Color.lightGray));
		context.nextButton.setIcon(new TriangleIcon(SwingConstants.EAST, 24, 24, Color.lightGray));
		context.firstButton.setIcon(new FirstIcon(2, 24, 24, Color.lightGray));
		context.lastButton.setIcon(new LastIcon(2, 24, 24, Color.lightGray));

		context.prevButton.setRolloverIcon(new TriangleIcon(SwingConstants.WEST, 24, 24, Color.white));
		context.nextButton.setRolloverIcon(new TriangleIcon(SwingConstants.EAST, 24, 24, Color.white));
		context.firstButton.setRolloverIcon(new FirstIcon(2, 24, 24, Color.white));
		context.lastButton.setRolloverIcon(new LastIcon(2, 24, 24, Color.white));

		context.prevButton.setDisabledIcon(new TriangleIcon(SwingConstants.WEST, 24, 24, Color.darkGray));
		context.nextButton.setDisabledIcon(new TriangleIcon(SwingConstants.EAST, 24, 24, Color.darkGray));
		context.firstButton.setDisabledIcon(new FirstIcon(2, 24, 24, Color.darkGray));
		context.lastButton.setDisabledIcon(new LastIcon(2, 24, 24, Color.darkGray));
	}
	
	protected void format(JButton button) {
		button.setBorderPainted(false);
		button.setContentAreaFilled(false);
		button.setUI(new BasicButtonUI());
	}

}
