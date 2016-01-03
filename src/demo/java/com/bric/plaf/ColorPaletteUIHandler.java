/*
 * @(#)ColorPaletteUIHandler.java
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
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ComponentUI;

import com.bric.swing.ColorPalette;


class ColorPaletteUIHandler implements UIHandler {
	Vector<ColorPalette> palettes = new Vector<ColorPalette>();

	JPanel controls = new JPanel(new GridBagLayout());
	JRadioButton solid = new JRadioButton("Solid");
	JRadioButton continuous = new JRadioButton("Gradient");
	JRadioButton streaks = new JRadioButton("Streaks");
	JRadioButton none = new JRadioButton("None");
	JRadioButton shadow = new JRadioButton("Shadow");
	JRadioButton scribble = new JRadioButton("Scribble");
	JCheckBox saturatedPadding = new JCheckBox("Saturated");
	JCheckBox nonsaturatedPadding = new JCheckBox("Nonsaturated");
	
	ChangeListener changeListener = new ChangeListener() {
		public void stateChanged(ChangeEvent e) {
			ColorPalette cp = (ColorPalette)e.getSource();
			if(cp.isShowing())
				System.out.println(cp.getColor());
		}
	};

	ActionListener actionListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			String style;
			if(solid.isSelected()) {
				style = ColorSet.PALETTE_STYLE_DEFAULT;
			} else if(continuous.isSelected()) {
				style = ColorSet.PALETTE_STYLE_GRADIENT;
			} else {
				style = ColorSet.PALETTE_STYLE_STREAKS;
			}

			String cellStyle;
			if(none.isSelected()) {
				cellStyle = ColorSet.PALETTE_STYLE_DEFAULT;
			} else if(shadow.isSelected()) {
				cellStyle = ColorSet.PALETTE_CELL_STYLE_SHADOW;
			} else {
				cellStyle = ColorSet.PALETTE_CELL_STYLE_SCRIBBLE;
			}
			
			String padding = null;
			if(saturatedPadding.isSelected() && nonsaturatedPadding.isSelected()) {
				padding = HSBColorPaletteUI.PALETTE_PADDING_BOTH;
			} else if(saturatedPadding.isSelected()) {
				padding = HSBColorPaletteUI.PALETTE_PADDING_SATURATED;
			} else if(nonsaturatedPadding.isSelected()) {
				padding = HSBColorPaletteUI.PALETTE_PADDING_NONSATURATED;
			}

			for(int a = 0; a<palettes.size(); a++) {
				ColorPalette palette = palettes.get(a);
				palette.putClientProperty(ColorSet.PALETTE_STYLE_PROPERTY,style);
				palette.putClientProperty(ColorSet.PALETTE_CELL_STYLE_PROPERTY,cellStyle);
				palette.putClientProperty(HSBColorPaletteUI.PALETTE_PADDING_PROPERTY,padding);
			}
		}
	};
	
	public ColorPaletteUIHandler() {

		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.insets = new Insets(3,3,3,3);
		c.anchor = GridBagConstraints.EAST;
		controls.add(new JLabel("Palette Style:"),c);
		c.gridx++; c.anchor = GridBagConstraints.WEST;
		controls.add(solid, c);
		c.gridx++;
		controls.add(continuous,c);
		c.gridx++;
		controls.add(streaks,c);
		c.gridy++; c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		controls.add(new JLabel("Cell Style:"),c);
		c.gridx++; c.anchor = GridBagConstraints.WEST;
		controls.add(none, c);
		c.gridx++;
		controls.add(shadow, c);
		c.gridx++;
		controls.add(scribble, c);

		c.gridy++; c.gridx = 0;
		c.anchor = GridBagConstraints.EAST;
		controls.add(new JLabel("Padding:"),c);
		c.gridx++; c.anchor = GridBagConstraints.WEST;
		controls.add(saturatedPadding, c);
		c.gridx++;
		controls.add(nonsaturatedPadding, c);

		ButtonGroup group = new ButtonGroup();
		group.add(solid);
		group.add(continuous);
		group.add(streaks);
		
		group = new ButtonGroup();
		group.add(none);
		group.add(shadow);
		group.add(scribble);
		
		solid.addActionListener(actionListener);
		continuous.addActionListener(actionListener);
		streaks.addActionListener(actionListener);
		none.addActionListener(actionListener);
		shadow.addActionListener(actionListener);
		scribble.addActionListener(actionListener);
		saturatedPadding.addActionListener(actionListener);
		nonsaturatedPadding.addActionListener(actionListener);
		
		solid.doClick();
		none.doClick();
	}

	public JComponent[] getControls() {
		return new JComponent[] { controls };
	}

	public JPanel makeDemoPanel(ComponentUI ui) {
		JPanel panel = new JPanel(new GridBagLayout());
		ColorPalette palette = new ColorPalette();
		
		if(palettes.size()>0) {
			ColorPalette external = palettes.get(0);
			external.bind(palette);
			external.setColor(new Color(50,140,240));
		}
		
		palette.addChangeListener(changeListener);
		palettes.add(palette);
		palette.setUI( (ColorPaletteUI)ui );
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		c.insets = new Insets(10,10,10,10);
		panel.add(palette,c);
		panel.setOpaque(false);

		return panel;
	}

	public void updateControls(JInternalFrame selectedFrame,
			ComponentUI ui, Vector<Component> components) {
	}
}
