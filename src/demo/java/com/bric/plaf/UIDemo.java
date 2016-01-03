/*
 * @(#)UIDemo.java
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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ComponentUI;

import com.bric.swing.BricApplet;

/** A demo framework for different ComponentUI's */
public abstract class UIDemo extends BricApplet {
	private static final long serialVersionUID = 1L;
	static {
		try {
			String lf = UIManager.getSystemLookAndFeelClassName();
			UIManager.setLookAndFeel(lf);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isMac = (System.getProperty("os.name").toLowerCase().indexOf("mac")!=-1);
	private static final String ORIGINAL_UI = "UIDemo.originalUI";
	
	protected Vector<Component> components = new Vector<Component>();
	
	JList<JInternalFrame> list;
	JScrollPane scrollPane;
	JDesktopPane desktop;
	JSplitPane splitPane;
	
	JComboBox background = new JComboBox();
	protected UIHandler[] handlers;
	JPanel controlPanel = new JPanel(new GridBagLayout());
	Vector<JComponent> controls = new Vector<JComponent>();
	
	public UIDemo(ComponentUI[] uis,UIHandler handler) {
		this(uis, new UIHandler[] { handler });
	}
	
	public UIDemo(ComponentUI[] uis,UIHandler[] handlers) {
		super();
		this.handlers = handlers;
		
		list = new JList(makeFrames(uis));
		scrollPane = new JScrollPane(list);
		desktop = new JDesktopPane();
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,scrollPane,desktop);
		Color bkgnd = desktop.getBackground();
		
		if(bkgnd.getRed()<30 && bkgnd.getBlue()<30 && bkgnd.getGreen()<30) {
			//on vista the bkgnd is black?  boo.
			desktop.setBackground(Color.white);
		}

		getContentPane().setBackground(Color.white);
		if(getContentPane() instanceof JComponent)
			((JComponent)getContentPane()).setOpaque(true);
		
		for(int a = 0; a<handlers.length; a++) {
			JComponent[] handlerControls = handlers[a].getControls();
			for(int b = 0; b<handlerControls.length; b++) {
				handlerControls[b].setFocusable(false);
				controls.add(handlerControls[b]);
			}
		}
		controls.add(background);

		getContentPane().setLayout(new GridBagLayout());
		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				updateDemo();
			}
		});
		

		GridBagConstraints c = new GridBagConstraints();
		c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0;
		c.weightx = 1; c.weighty = 0;
		c.insets = new Insets(5,5,5,5);
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = GridBagConstraints.REMAINDER;
		getContentPane().add(controlPanel,c);
		c.gridy++; c.weighty = 1;
		getContentPane().add(splitPane,c);

		list.setCellRenderer(new DefaultListCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(JList list,
					Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				value = ((JInternalFrame)value).getTitle();
				return super.getListCellRendererComponent(list, value, index, isSelected,
						cellHasFocus);
			}
		});
		
		setOpaque(controlPanel,false);
		setFocusable(controlPanel,false);
		
		desktop.setPreferredSize(new Dimension(300,300));
		
		background.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Color color = (Color)background.getSelectedItem();
				for(int a = 0; a<components.size(); a++) {
					JComponent comp = (JComponent)components.get(a);
					JRootPane rootPane = SwingUtilities.getRootPane(comp);
					rootPane.setBackground( color );
					rootPane.getParent().setBackground( color );
				}
			}
		});
		
		background.addItem(new Color(255,255,255) {
			private static final long serialVersionUID = 1L;

			@Override
			public String toString() {
				return "White Background";
			}
		});
		background.addItem(new Color(236,233,216) {
			private static final long serialVersionUID = 1L;

			@Override
			public String toString() {
				return "XP Background";
			}
		});
		background.addItem(new Color(150,150,150) {
			private static final long serialVersionUID = 1L;

			@Override
			public String toString() {
				return "Dark Background";
			}
		});
		
		background.setSelectedIndex(1); //trigger the listener
		background.setSelectedIndex(0);
		
		updateDemo();
	}
	
	private static void setFocusable(JComponent c,boolean b) {
		c.setFocusable(b);
		for(int a = 0; a<c.getComponentCount(); a++) {
			if(c.getComponent(a) instanceof JComponent)
				setFocusable( (JComponent)c.getComponent(a),b);
		}
	}
	
	private static void setOpaque(JComponent c,boolean b) {
		c.setOpaque(b);
		for(int a = 0; a<c.getComponentCount(); a++) {
			if(c.getComponent(a) instanceof JComponent)
				setOpaque( (JComponent)c.getComponent(a),b);
		}
	}
	
	private void updateDemo() {
		int i = list.getSelectedIndex();
		if(i==-1) {
			list.setSelectedIndex(0);
			return;
		}
		
		JInternalFrame frame = (JInternalFrame)list.getSelectedValue();
		ComponentUI ui = (ComponentUI)frame.getClientProperty(ORIGINAL_UI);
		
		for(int a = 0; a<handlers.length; a++) {
			handlers[a].updateControls(frame, ui, components);
		}
		
		JInternalFrame current = desktop.getSelectedFrame();
		if(current==frame)
			return;
		
		desktop.removeAll();
		frame.pack();
		
		desktop.add(frame);
		desktop.setSelectedFrame(frame);
		desktop.repaint();
		
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
		c.insets = new Insets(3,3,3,3);
		c.anchor = GridBagConstraints.WEST;
		int width = 0;
		for(int a = 0; a<controls.size(); a++) {
			JComponent control = controls.get(a);
			int thisWidth = control.getPreferredSize().width+4;
			if(control.isVisible()) {
				if(width+thisWidth>400) {
					width = 0;
					c.gridx = 0;
					c.gridy++;
				}
				controlPanel.add(control,c);
				width += thisWidth;
				c.gridx++;
				if(c.gridx==4) {
					width = 0;
					c.gridx = 0;
					c.gridy++;
				}
			}
		}
		setOpaque(controlPanel,false);
	}
	
	/** Create a set of JInternalFrames: one showing off each
	 * element in the argument array.
	 */
	private JInternalFrame[] makeFrames(ComponentUI[] uis) {
		Vector<JInternalFrame> frames = new Vector<JInternalFrame>();
		
		for(int a = 0; a<uis.length; a++) {
			String name = uis[a].getClass().getName();
			name = name.substring(name.lastIndexOf('.')+1);
			
			JInternalFrame frame = new JInternalFrame(name,true,false);
			frame.putClientProperty(ORIGINAL_UI, uis[a]);
			frame.setVisible(true);
			frame.getContentPane().setLayout(new GridBagLayout());
			GridBagConstraints c = new GridBagConstraints();
			c.gridx = 0; c.gridy = 0; c.weightx = 1; c.weighty = 1;
			c.fill = GridBagConstraints.BOTH;
			c.insets = new Insets(10,10,10,10);
			JComponent panel = null;
			for(int b = 0; b<handlers.length && panel==null; b++) {
				panel = handlers[b].makeDemoPanel(uis[a]);
				recordComponents(panel);
			}
			frame.getContentPane().add(panel,c);
			frame.pack();

			frames.add(frame);
		}
		
		return frames.toArray(new JInternalFrame[frames.size()]);
	}
	
	private void recordComponents(JComponent jc) {
		if(!(jc instanceof JPanel)) {
			components.add(jc);
		}
		if(jc instanceof JComboBox)
			return;
		for(int a = 0; a<jc.getComponentCount(); a++) {
			if(jc.getComponent(a) instanceof JComponent)
				recordComponents( (JComponent)jc.getComponent(a) );
		}
	}
}
