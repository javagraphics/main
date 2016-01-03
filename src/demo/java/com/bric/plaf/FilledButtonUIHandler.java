/*
 * @(#)FilledButtonUIHandler.java
 *
 * $Date: 2014-05-07 08:23:21 +0200 (Mi, 07 Mai 2014) $
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

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Shape;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.ComponentUI;

public class FilledButtonUIHandler implements UIHandler {
	
	private static final String BUTTON_TEXT = "XYZ";
	private static Icon DEMO_ICON = new ImageIcon(UIDemo.class.getResource("vcard_add.png"));
	
	Vector<AbstractButton> buttons = new Vector<AbstractButton>();
	
	JCheckBox iconCheckBox = new JCheckBox("Show Icons",true);
	JCheckBox contentCheckBox = new JCheckBox("Paint Content",true);
	JCheckBox borderCheckBox = new JCheckBox("Paint Border",true);
	JCheckBox focusCheckBox = new JCheckBox("Paint Focus",true);
	JCheckBox textCheckBox = new JCheckBox("Show Text",true);
	JCheckBox blinkCheckBox = new JCheckBox("Blink Focus",false);
	JCheckBox shimmerCheckBox = new JCheckBox("Shimmer Effect",false);
	JCheckBox zoomCheckBox = new JCheckBox("Zoom Icon Effect",false);
	JCheckBox enabledCheckBox = new JCheckBox("Enabled",true);
	JComboBox componentTypes = new JComboBox();
	
	public FilledButtonUIHandler() {

		iconCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Icon icon = iconCheckBox.isSelected() ? DEMO_ICON : null;

				for(int a = 0; a<buttons.size(); a++) {
					(buttons.get(a) ).setIcon(icon);
				}
				
				pack();
			}
		});
		textCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textCheckBox.isSelected() ? BUTTON_TEXT : "";

				for(int a = 0; a<buttons.size(); a++) {
					(buttons.get(a) ).setText(text);
				}

				pack();
			}
		});
		
		blinkCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean blinking = blinkCheckBox.isSelected();
				for(int a = 0; a<buttons.size(); a++) {
					(buttons.get(a) ).putClientProperty("Focus.blink",
							new Boolean(blinking));
				}
			}
		});
		
		shimmerCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int a = 0; a<buttons.size(); a++) {
					AbstractButton button = (buttons.get(a) );
					if(shimmerCheckBox.isSelected()) {
						button.addMouseListener(ShimmerPaintUIEffect.mouseListener);
					} else {
						button.removeMouseListener(ShimmerPaintUIEffect.mouseListener);
					}
				}
			}
		});
		
		focusCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int a = 0; a<buttons.size(); a++) {
					AbstractButton button = (buttons.get(a) );
					button.setFocusPainted( focusCheckBox.isSelected() );
				}
			}
		});
		borderCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int a = 0; a<buttons.size(); a++) {
					AbstractButton button = (buttons.get(a) );
					button.setBorderPainted( borderCheckBox.isSelected() );
				}
			}
		});
		contentCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int a = 0; a<buttons.size(); a++) {
					AbstractButton button = (buttons.get(a) );
					button.setContentAreaFilled( contentCheckBox.isSelected() );
				}
			}
		});
		
		zoomCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int a = 0; a<buttons.size(); a++) {
					AbstractButton button = (buttons.get(a) );
					if(zoomCheckBox.isSelected()) {
						button.addActionListener(ZoomIconPaintUIEffect.actionListener);
					} else {
						button.removeActionListener(ZoomIconPaintUIEffect.actionListener);
					}
				}
			}
		});
		
		enabledCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for(int a = 0; a<buttons.size(); a++) {
					AbstractButton button = (buttons.get(a) );
					button.setEnabled(enabledCheckBox.isSelected());
				}
			}
		});

		componentTypes.addItem(JButton.class);
		componentTypes.addItem(JToggleButton.class);

		componentTypes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateVisibility();
			}
		});

		componentTypes.setRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Class<?> type = (Class<?>)value;
				String name = type.getName();
				name = name.substring(name.lastIndexOf('.')+1);
				
				return super.getListCellRendererComponent(list, name, index, isSelected,
						cellHasFocus);
			}
		});
		
		updateVisibility();
	}
	
	protected void updateVisibility() {
		Class<?> type = (Class<?>)componentTypes.getSelectedItem();
		if(type!=null) {
			for(int a = 0; a<buttons.size(); a++) {
				Component c = buttons.get(a);
				c.setVisible( c.getClass().equals(type) );
			}
		}
	}

	public JComponent[] getControls() {
		return new JComponent[] {
				iconCheckBox, contentCheckBox,
				borderCheckBox, focusCheckBox,
				textCheckBox, blinkCheckBox,
				shimmerCheckBox, zoomCheckBox,
				enabledCheckBox, componentTypes
		};
	}

	public JPanel makeDemoPanel(ComponentUI ui) {
		if((ui instanceof FilledButtonUI)==false)
			return null;
		
		String name = ui.getClass().getName();
		name = name.substring(name.lastIndexOf('.')+1);
		
		FilledButtonUI fui = (FilledButtonUI)ui;
		
		JPanel motherPanel = new JPanel(new GridBagLayout());
		
		GridBagConstraints c = new GridBagConstraints();
		JPanel segmentPanel = new JPanel(new GridBagLayout());
		String[] hPos = new String[] {"first", "middle", "last", "only"};
		String[] vPos = new String[] {"top", "middle", "bottom", "only"};
		
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		
		for(int h = 0; h<hPos.length; h++) {
			for(int v = 0; v<vPos.length; v++) {
				String toolTip = "this is a demo of \""+name+"\".";
				
				AbstractButton button1 = new JButton(BUTTON_TEXT);
				AbstractButton button2 = new JToggleButton(BUTTON_TEXT);
				
				for(int a = 0; a<2; a++) {
					AbstractButton button = (a==0) ? button1 : button2;
					button.setOpaque(false);
					button.setIcon(DEMO_ICON);
					button.putClientProperty("JButton.segmentHorizontalPosition", hPos[h]);
					button.putClientProperty("JButton.segmentVerticalPosition", vPos[v]);
					button.setUI( fui );
					button.setFont(UIManager.getFont("IconButton.font")); //miniature-ish
					buttons.add(button);
					button.setToolTipText(toolTip);
				}
				
				c.gridx = h; c.gridy = v;
				
				segmentPanel.add(button1,c);
				segmentPanel.add(button2,c);
			}
		}
		
		JPanel shapesPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.NONE;
		
		for(int z = 0; z<3; z++) {

			AbstractButton button1 = new JButton(BUTTON_TEXT);
			AbstractButton button2 = new JToggleButton(BUTTON_TEXT);

			String toolTip = "this is a demo of \""+name+"\"";
			
			Shape shape = null;
			if(z==0) {
				shape = new Ellipse2D.Float(0,0,20,20);
				toolTip = toolTip+" using an ellipse.";
			} else if(z==1) {
				GeneralPath diamond = new GeneralPath();
				diamond.moveTo(0, 0);
				diamond.lineTo(10, 10);
				diamond.lineTo(0, 20);
				diamond.lineTo(-10, 10);
				diamond.closePath();
				shape = diamond;
				toolTip = toolTip+" using a diamond.";
			} else {
				GeneralPath arrow = new GeneralPath();
				arrow.moveTo(0, -5);
				arrow.lineTo(20, -5);
				arrow.lineTo(20, -10);
				arrow.lineTo(30, 0);
				arrow.lineTo(20, 10);
				arrow.lineTo(20, 5);
				arrow.lineTo(0, 5);
				arrow.closePath();
				shape = arrow;
				toolTip = toolTip+" using an arrow.";
			}
			button1.putClientProperty(FilledButtonUI.SHAPE, shape);
			button2.putClientProperty(FilledButtonUI.SHAPE, shape);
			
			for(int a = 0; a<2; a++) {
				AbstractButton button = (a==0) ? button1 : button2;
				button.setOpaque(false);
				button.setIcon(DEMO_ICON);
				button.setUI( fui );
				button.setFont(UIManager.getFont("IconButton.font")); //miniature-ish
				button.setToolTipText(toolTip);
			}
			
			c.gridx = z; c.gridy = 0;
			
			shapesPanel.add(button1,c);
			shapesPanel.add(button2,c);
		}

		JPanel otherPanel = new JPanel(new GridBagLayout());
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		otherPanel.add( createComboBox(fui, name, false, false), c);
		c.gridx++; c.insets = new Insets(0,4,0,4);
		otherPanel.add( createComboBox(fui, name, true, false), c);
		c.gridx++; c.insets = new Insets(0,0,0,0);
		otherPanel.add( createComboBox(fui, name, false, true), c);
		
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 1;
		c.fill = GridBagConstraints.BOTH;
		motherPanel.add(segmentPanel, c);
		
		c.gridy++; c.weighty = 0; 
		c.fill = GridBagConstraints.HORIZONTAL;
		motherPanel.add(shapesPanel, c);
		
		c.gridy++; c.weighty = 1; 
		c.fill = GridBagConstraints.BOTH;
		motherPanel.add(otherPanel, c);
		
		segmentPanel.setOpaque(false);
		shapesPanel.setOpaque(false);
		motherPanel.setOpaque(false);
		otherPanel.setOpaque(false);
		return motherPanel;
	}
	
	protected JComboBox createComboBox(FilledButtonUI fui,String name,boolean isPopDown,boolean showSeparators) {
		JComboBox comboBox = new JComboBox();
		comboBox.putClientProperty(FilledButtonUI.SHOW_SEPARATORS, showSeparators);
		comboBox.putClientProperty(FilledComboBoxUI.IS_POP_DOWN_KEY, isPopDown);
		comboBox.setUI(fui.createComboBoxUI());
		comboBox.addItem("Item 1");
		comboBox.addItem("Item 2");
		comboBox.addItem("Item 3");
		String tooltip = "This is a demo of \""+name+"\" applied to a JComboBox";
		if(isPopDown) {
			tooltip += " (pop down)";
		} else {
			tooltip += " (pop up)";
		}
		if(showSeparators) {
			tooltip += " showing separators.";
		} else {
			tooltip +=".";
		}
		comboBox.setToolTipText(tooltip);
		return comboBox;
	}
	
	protected void pack() {
		if(buttons.size()>0) {
			Window w = SwingUtilities.getWindowAncestor( buttons.get(0) );
			if(w!=null) {
				w.pack();
			}
		}
	}

	public void updateControls(JInternalFrame selectedFrame, ComponentUI ui,
			Vector<Component> components) {
		buttons.removeAllElements();
		for(int a = 0; a<components.size(); a++) {
			if(components.get(a) instanceof AbstractButton)
				buttons.add( (AbstractButton)components.get(a) );
		}
		
	}

}
